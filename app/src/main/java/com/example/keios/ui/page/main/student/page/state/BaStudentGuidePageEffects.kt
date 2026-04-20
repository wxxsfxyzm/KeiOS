package com.example.keios.ui.page.main.student.page.state

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.keios.R
import com.example.keios.ui.page.main.ba.support.BASettingsStore
import com.example.keios.ui.page.main.student.BaGuideTempMediaCache
import com.example.keios.ui.page.main.student.BaStudentGuideInfo
import com.example.keios.ui.page.main.student.BaStudentGuideStore
import com.example.keios.ui.page.main.student.GuideBottomTab
import com.example.keios.ui.page.main.student.clearGuideBgmLoopScope
import com.example.keios.ui.page.main.student.clearGuideBgmPlaybackScope
import com.example.keios.ui.page.main.student.fetchGuideInfo
import com.example.keios.ui.page.main.student.page.support.collectGuideStaticImagePrefetchUrls
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
internal fun BindBaStudentGuidePlayerLifecycleEffects(
    context: Context,
    sourceUrl: String,
    voicePlayer: ExoPlayer
) {
    DisposableEffect(voicePlayer) {
        onDispose {
            runCatching { voicePlayer.release() }
        }
    }

    DisposableEffect(sourceUrl) {
        onDispose {
            clearGuideBgmLoopScope(sourceUrl)
            clearGuideBgmPlaybackScope(sourceUrl)
            BaGuideTempMediaCache.clearGuideCache(context, sourceUrl)
        }
    }
}

@Composable
internal fun BindBaStudentGuideVoiceListenerEffect(
    context: Context,
    voicePlayer: ExoPlayer,
    playingVoiceUrl: String,
    onPlayingVoiceUrlChange: (String) -> Unit,
    onIsVoicePlayingChange: (Boolean) -> Unit,
    onVoicePlayProgressChange: (Float) -> Unit
) {
    DisposableEffect(voicePlayer, playingVoiceUrl) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                val active = isPlaying && playingVoiceUrl.isNotBlank()
                onIsVoicePlayingChange(active)
                if (!active) {
                    onVoicePlayProgressChange(0f)
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                onVoicePlayProgressChange(0f)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_ENDED, Player.STATE_IDLE -> {
                        onPlayingVoiceUrlChange("")
                        onIsVoicePlayingChange(false)
                        onVoicePlayProgressChange(0f)
                    }

                    Player.STATE_READY -> {
                        onIsVoicePlayingChange(
                            voicePlayer.isPlaying && playingVoiceUrl.isNotBlank()
                        )
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                onPlayingVoiceUrlChange("")
                onIsVoicePlayingChange(false)
                onVoicePlayProgressChange(0f)
                Toast.makeText(
                    context,
                    context.getString(
                        R.string.guide_toast_voice_play_failed_with_reason,
                        error.errorCodeName
                    ),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        voicePlayer.addListener(listener)
        onDispose {
            voicePlayer.removeListener(listener)
        }
    }
}

@Composable
internal fun BindBaStudentGuideSourceRestoreEffect(
    sourceUrl: String,
    onSourceUrlChange: (String) -> Unit,
    onErrorChange: (String?) -> Unit
) {
    LaunchedEffect(Unit) {
        val latestStored = BaStudentGuideStore.loadCurrentUrl()
        if (latestStored.isNotBlank() && latestStored != sourceUrl) {
            onSourceUrlChange(latestStored)
            onErrorChange(null)
        }
    }
}

@Composable
internal fun BindBaStudentGuidePagerSyncEffects(
    sourceUrl: String,
    bottomTabsSize: Int,
    selectedBottomTabIndex: Int,
    pagerState: PagerState,
    onSelectedBottomTabIndexChange: (Int) -> Unit
) {
    LaunchedEffect(sourceUrl, bottomTabsSize) {
        val targetIndex = selectedBottomTabIndex.coerceIn(0, (bottomTabsSize - 1).coerceAtLeast(0))
        if (pagerState.currentPage != targetIndex) {
            pagerState.scrollToPage(targetIndex)
        }
    }

    LaunchedEffect(pagerState.settledPage) {
        if (selectedBottomTabIndex != pagerState.settledPage) {
            onSelectedBottomTabIndexChange(pagerState.settledPage)
        }
    }
}

@Composable
internal fun BindBaStudentGuideVoiceProgressEffect(
    activeBottomTab: GuideBottomTab,
    isVoicePlaying: Boolean,
    playingVoiceUrl: String,
    voicePlayer: ExoPlayer,
    onVoicePlayProgressChange: (Float) -> Unit
) {
    val voiceTabActive = activeBottomTab == GuideBottomTab.Voice
    val voicePlayingForProgress = if (voiceTabActive) isVoicePlaying else false
    val voiceUrlForProgress = if (voiceTabActive) playingVoiceUrl else ""

    LaunchedEffect(voiceTabActive, voicePlayingForProgress, voiceUrlForProgress) {
        if (!voiceTabActive || !voicePlayingForProgress || voiceUrlForProgress.isBlank()) {
            onVoicePlayProgressChange(0f)
            return@LaunchedEffect
        }
        while (voiceTabActive && voicePlayingForProgress && voiceUrlForProgress.isNotBlank()) {
            val duration = voicePlayer.duration
            val position = voicePlayer.currentPosition
            onVoicePlayProgressChange(
                if (duration > 0L && position >= 0L) {
                    (position.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
                } else {
                    0f
                }
            )
            delay(220)
        }
    }
}

@Composable
internal fun BindBaStudentGuidePrefetchEffects(
    context: Context,
    sourceUrl: String,
    guideSyncToken: Long,
    info: BaStudentGuideInfo?,
    activeBottomTab: GuideBottomTab,
    galleryPrefetchRequested: Boolean,
    onGalleryPrefetchRequestedChange: (Boolean) -> Unit,
    staticImagePrefetchStage: Int,
    onStaticImagePrefetchStageChange: (Int) -> Unit,
    initialPrefetchCount: Int,
    galleryExtraPrefetchCount: Int,
    onGalleryCacheRevisionIncrease: () -> Unit
) {
    LaunchedEffect(activeBottomTab) {
        if (activeBottomTab == GuideBottomTab.Gallery) {
            onGalleryPrefetchRequestedChange(true)
        }
    }

    LaunchedEffect(sourceUrl, guideSyncToken) {
        if (staticImagePrefetchStage >= 1) return@LaunchedEffect
        val guide = info ?: return@LaunchedEffect
        onStaticImagePrefetchStageChange(1)

        val urls = collectGuideStaticImagePrefetchUrls(guide)
            .take(initialPrefetchCount)
        if (urls.isEmpty()) return@LaunchedEffect

        withContext(Dispatchers.IO) {
            BaGuideTempMediaCache.prefetchForGuide(
                context = context,
                sourceUrl = sourceUrl,
                rawUrls = urls
            )
        }
        onGalleryCacheRevisionIncrease()
    }

    LaunchedEffect(sourceUrl, galleryPrefetchRequested, guideSyncToken) {
        if (!galleryPrefetchRequested) return@LaunchedEffect
        if (staticImagePrefetchStage >= 2) return@LaunchedEffect
        val guide = info ?: return@LaunchedEffect
        onStaticImagePrefetchStageChange(2)

        val urls = collectGuideStaticImagePrefetchUrls(guide)
            .drop(initialPrefetchCount)
            .take(galleryExtraPrefetchCount)
        if (urls.isEmpty()) return@LaunchedEffect

        withContext(Dispatchers.IO) {
            BaGuideTempMediaCache.prefetchForGuide(
                context = context,
                sourceUrl = sourceUrl,
                rawUrls = urls
            )
        }
        onGalleryCacheRevisionIncrease()
    }
}

@Composable
internal fun BindBaStudentGuideInfoLoadEffect(
    context: Context,
    sourceUrl: String,
    refreshSignal: Int,
    transitionAnimationsEnabled: Boolean,
    initialFetchDelayMs: Int,
    manualRefreshRequested: Boolean,
    onManualRefreshRequestedChange: (Boolean) -> Unit,
    currentSourceUrlProvider: () -> String,
    currentInfoProvider: () -> BaStudentGuideInfo?,
    onInfoChange: (BaStudentGuideInfo?) -> Unit,
    onErrorChange: (String?) -> Unit,
    onLoadingChange: (Boolean) -> Unit
) {
    LaunchedEffect(sourceUrl, refreshSignal) {
        if (refreshSignal == 0 && transitionAnimationsEnabled && initialFetchDelayMs > 0) {
            delay(initialFetchDelayMs.toLong())
        }
        val requestUrl = sourceUrl
        if (requestUrl.isBlank()) return@LaunchedEffect
        val manualRefresh = manualRefreshRequested
        onManualRefreshRequestedChange(false)
        onLoadingChange(true)

        val now = System.currentTimeMillis()
        val refreshIntervalHours = withContext(Dispatchers.IO) {
            BASettingsStore.loadCalendarRefreshIntervalHours()
        }
        val cacheSnapshot = withContext(Dispatchers.IO) {
            BaStudentGuideStore.loadInfoSnapshot(requestUrl)
        }
        if (requestUrl != currentSourceUrlProvider()) return@LaunchedEffect

        val cacheExpired = BaStudentGuideStore.isCacheExpired(
            snapshot = cacheSnapshot,
            refreshIntervalHours = refreshIntervalHours,
            nowMs = now
        )
        val cacheComplete = cacheSnapshot.isComplete && cacheSnapshot.info != null
        if (!manualRefresh && cacheComplete && !cacheExpired) {
            onInfoChange(cacheSnapshot.info)
            onErrorChange(null)
            onLoadingChange(false)
            return@LaunchedEffect
        }

        if (cacheComplete) {
            onInfoChange(cacheSnapshot.info)
            onErrorChange(null)
        } else if (cacheSnapshot.hasCache && !cacheSnapshot.isComplete) {
            onInfoChange(null)
        }

        val shouldClearLocalCache =
            manualRefresh || (cacheSnapshot.hasCache && (cacheExpired || !cacheSnapshot.isComplete))
        if (shouldClearLocalCache) {
            withContext(Dispatchers.IO) {
                BaStudentGuideStore.clearCachedInfo(requestUrl)
                BaGuideTempMediaCache.clearGuideCache(context, requestUrl)
            }
            if (requestUrl != currentSourceUrlProvider()) return@LaunchedEffect
        }

        val result = withContext(Dispatchers.IO) {
            runCatching { fetchGuideInfo(requestUrl) }
        }
        if (requestUrl != currentSourceUrlProvider()) return@LaunchedEffect
        result.onSuccess { latest ->
            if (requestUrl != currentSourceUrlProvider()) return@onSuccess
            onInfoChange(latest)
            onErrorChange(null)
            withContext(Dispatchers.IO) { BaStudentGuideStore.saveInfo(latest) }
        }.onFailure {
            if (requestUrl != currentSourceUrlProvider()) return@onFailure
            val hasInfo = currentInfoProvider() != null
            onErrorChange(if (hasInfo) "网络请求失败，已显示本地缓存" else "图鉴信息加载失败")
        }
        if (requestUrl != currentSourceUrlProvider()) return@LaunchedEffect
        onLoadingChange(false)
    }
}
