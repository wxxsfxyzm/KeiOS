package com.example.keios.ui.page.main.student.page

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.keios.R
import com.example.keios.feature.ba.data.remote.GameKeeFetchHelper
import com.example.keios.ui.page.main.host.pager.animateTabSwitch
import com.example.keios.ui.page.main.ba.support.BASettingsStore
import com.example.keios.ui.page.main.student.BaStudentGuideInfo
import com.example.keios.ui.page.main.student.BaGuideGalleryItem
import com.example.keios.ui.page.main.student.BaGuideTempMediaCache
import com.example.keios.ui.page.main.student.BaStudentGuideStore
import com.example.keios.ui.page.main.student.createGameKeeMediaSourceFactory
import com.example.keios.ui.page.main.student.GuideBottomTab
import com.example.keios.ui.page.main.student.fetchGuideInfo
import com.example.keios.ui.page.main.student.hasRenderableGalleryMedia
import com.example.keios.ui.page.main.student.isMemoryHallFileGalleryItem
import com.example.keios.ui.page.main.student.isRenderableGalleryStaticImageUrl
import com.example.keios.ui.page.main.student.fetch.normalizeGuideUrl
import com.example.keios.ui.page.main.student.fetch.extractGuideContentIdFromUrl
import com.example.keios.ui.page.main.student.page.component.BaStudentGuideBottomBar
import com.example.keios.ui.page.main.student.page.component.BaStudentGuidePagerContent
import com.example.keios.ui.page.main.student.page.support.GuideMediaSaveRequest
import com.example.keios.ui.page.main.student.page.support.buildGuideMediaSaveRequest
import com.example.keios.ui.page.main.student.page.support.copyGuideMediaToUri
import com.example.keios.ui.page.main.student.page.support.createUniqueDocumentInTree
import com.example.keios.ui.page.main.student.page.support.normalizeGuidePlaybackSource
import com.example.keios.ui.page.main.student.page.support.rememberGuideSyncProgress
import com.example.keios.ui.page.main.student.page.state.BindBaStudentGuideInfoLoadEffect
import com.example.keios.ui.page.main.student.page.state.BindBaStudentGuidePagerSyncEffects
import com.example.keios.ui.page.main.student.page.state.BindBaStudentGuidePlayerLifecycleEffects
import com.example.keios.ui.page.main.student.page.state.BindBaStudentGuidePrefetchEffects
import com.example.keios.ui.page.main.student.page.state.BindBaStudentGuideSourceRestoreEffect
import com.example.keios.ui.page.main.student.page.state.BindBaStudentGuideVoiceListenerEffect
import com.example.keios.ui.page.main.student.page.state.BindBaStudentGuideVoiceProgressEffect
import com.example.keios.ui.page.main.student.clearGuideBgmLoopScope
import com.example.keios.ui.page.main.student.clearGuideBgmPlaybackScope
import com.example.keios.ui.perf.ReportPagerPerformanceState
import com.example.keios.ui.page.main.widget.motion.AppMotionTokens
import com.example.keios.ui.page.main.widget.glass.UiPerformanceBudget
import com.example.keios.ui.page.main.widget.chrome.LiquidGlassBottomBar
import com.example.keios.ui.page.main.widget.chrome.LiquidGlassBottomBarItem
import com.example.keios.ui.page.main.widget.chrome.LiquidActionBar
import com.example.keios.ui.page.main.widget.chrome.LiquidActionItem
import com.example.keios.ui.page.main.widget.motion.LocalTransitionAnimationsEnabled
import com.example.keios.ui.page.main.widget.motion.appFloatingEnter
import com.example.keios.ui.page.main.widget.motion.appFloatingExit
import com.example.keios.ui.page.main.widget.chrome.liquidGlassBottomBarItemContentColor
import com.example.keios.core.prefs.UiPrefs
import com.example.keios.ui.page.main.widget.motion.resolvedMotionDuration
import com.example.keios.core.ui.effect.getMiuixAppBarColor
import com.example.keios.core.ui.effect.rememberMiuixBlurBackdrop
import com.example.keios.ui.page.main.os.appLucideBackIcon
import com.example.keios.ui.page.main.os.appLucideRefreshIcon
import com.example.keios.ui.page.main.os.appLucideShareIcon
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun BaStudentGuidePage(
    liquidActionBarLayeredStyleEnabled: Boolean = true,
    preloadingEnabled: Boolean = false,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val transitionAnimationsEnabled = LocalTransitionAnimationsEnabled.current
    val preloadPolicy = remember(preloadingEnabled) {
        UiPerformanceBudget.resolvePreloadPolicy(preloadingEnabled)
    }
    val defaultPageTitle = stringResource(R.string.guide_page_title_default)
    val shareSourceEmptyText = stringResource(R.string.guide_share_source_empty)
    val shareSourceChooserTitle = stringResource(R.string.guide_share_source_chooser_title)
    val shareSourceFailedText = stringResource(R.string.common_share_failed)
    val openLinkFailedText = stringResource(R.string.common_open_link_failed)
    val shareSourceContentDescription = stringResource(R.string.guide_cd_share_source)
    val refreshContentDescription = stringResource(R.string.common_refresh)
    val accent = MiuixTheme.colorScheme.primary
    val surfaceColor = MiuixTheme.colorScheme.surface
    // Keep backdrop allocation stable per page lifecycle to avoid RenderThread native crashes
    // when rapidly switching guide tabs on some HyperOS builds.
    var activationCount by rememberSaveable { mutableIntStateOf(0) }
    DisposableEffect(Unit) {
        activationCount++
        onDispose { }
    }
    // Keep top-level backdrop only for navigator/pager layer and bottom bar.
    val navBackdrop: LayerBackdrop = key("nav-$activationCount") {
        rememberLayerBackdrop {
            drawRect(surfaceColor)
            drawContent()
        }
    }
    // Top action bar uses its own backdrop instance to avoid cross-layer recursion.
    val topBarBackdrop: LayerBackdrop = key("topbar-$activationCount") {
        rememberLayerBackdrop {
            drawRect(surfaceColor)
            drawContent()
        }
    }
    val topBarMaterialBackdrop = rememberMiuixBlurBackdrop(enableBlur = true)
    val scrollBehavior = MiuixScrollBehavior()

    var sourceUrl by rememberSaveable { mutableStateOf(BaStudentGuideStore.loadCurrentUrl()) }
    var info by remember(sourceUrl) { mutableStateOf<BaStudentGuideInfo?>(null) }
    val guideSyncToken = info?.syncedAtMs ?: -1L
    var loading by remember(sourceUrl) { mutableStateOf(sourceUrl.isNotBlank()) }
    var error by remember { mutableStateOf<String?>(null) }
    var refreshSignal by remember { mutableStateOf(0) }
    var manualRefreshRequested by remember { mutableStateOf(false) }
    var selectedBottomTabIndex by rememberSaveable(sourceUrl) { mutableIntStateOf(0) }
    var selectedVoiceLanguage by rememberSaveable(sourceUrl) { mutableStateOf("") }
    var playingVoiceUrl by rememberSaveable(sourceUrl) { mutableStateOf("") }
    var isVoicePlaying by remember(sourceUrl) { mutableStateOf(false) }
    var voicePlayProgress by remember(sourceUrl) { mutableFloatStateOf(0f) }
    var pendingCustomSaveRequest by remember { mutableStateOf<GuideMediaSaveRequest?>(null) }
    var pendingFixedSaveRequest by remember { mutableStateOf<GuideMediaSaveRequest?>(null) }
    var galleryPrefetchRequested by rememberSaveable(sourceUrl, guideSyncToken) { mutableStateOf(false) }
    var staticImagePrefetchStage by rememberSaveable(sourceUrl, guideSyncToken) { mutableIntStateOf(0) }
    var galleryCacheRevision by remember(sourceUrl) { mutableIntStateOf(0) }
    val bottomTabs = GuideBottomTab.entries
    val pagerState = rememberPagerState(
        initialPage = selectedBottomTabIndex,
        pageCount = { bottomTabs.size }
    )
    ReportPagerPerformanceState(
        scope = "guide_detail_pager",
        currentPage = bottomTabs.getOrElse(pagerState.currentPage) { GuideBottomTab.Archive }.name,
        targetPage = bottomTabs.getOrElse(pagerState.targetPage) { GuideBottomTab.Archive }.name,
        scrolling = pagerState.isScrollInProgress
    )
    val activeBottomTab = bottomTabs.getOrElse(pagerState.currentPage) { GuideBottomTab.Archive }
    val pageScope = rememberCoroutineScope()
    var tabJumpJob by remember { mutableStateOf<Job?>(null) }
    val syncProgress = rememberGuideSyncProgress(
        loading = loading,
        animationsEnabled = transitionAnimationsEnabled
    )
    val navigationBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val liquidBottomBarEnabled = remember { UiPrefs.isLiquidBottomBarEnabled() }
    var showBottomBar by remember { mutableStateOf(true) }
    val farJumpAlpha = remember { Animatable(1f) }
    val bottomBarNestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < -1f && showBottomBar) {
                    showBottomBar = false
                }
                if (available.y > 1f && !showBottomBar) {
                    showBottomBar = true
                }
                return Offset.Zero
            }
        }
    }
    val pageTitle = info?.title?.ifBlank { defaultPageTitle } ?: defaultPageTitle
    val voicePlayer = remember(context, sourceUrl) {
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(createGameKeeMediaSourceFactory(context))
            .build()
    }
    val customSaveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val request = pendingCustomSaveRequest
        pendingCustomSaveRequest = null
        val targetUri = result.data?.data
        if (result.resultCode != Activity.RESULT_OK || request == null || targetUri == null) {
            return@rememberLauncherForActivityResult
        }
        pageScope.launch {
            val success = withContext(Dispatchers.IO) {
                copyGuideMediaToUri(context, request.sourceUrl, targetUri)
            }
            if (success) {
                Toast.makeText(
                    context,
                    context.getString(R.string.guide_media_save_success, request.fileName),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(context, context.getString(R.string.guide_media_save_failed), Toast.LENGTH_SHORT).show()
            }
        }
    }
    val fixedFolderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val request = pendingFixedSaveRequest
        if (result.resultCode != Activity.RESULT_OK) {
            pendingFixedSaveRequest = null
            return@rememberLauncherForActivityResult
        }
        val treeUri = result.data?.data
        if (request == null || treeUri == null) {
            pendingFixedSaveRequest = null
            return@rememberLauncherForActivityResult
        }
        runCatching {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(treeUri, flags)
        }
        BASettingsStore.saveMediaSaveFixedTreeUri(treeUri.toString())
        pendingFixedSaveRequest = null
        pageScope.launch {
            val success = withContext(Dispatchers.IO) {
                val treeDoc = DocumentFile.fromTreeUri(context, treeUri) ?: return@withContext false
                val targetDoc = createUniqueDocumentInTree(
                    tree = treeDoc,
                    mimeType = request.mimeType,
                    fileName = request.fileName
                ) ?: return@withContext false
                copyGuideMediaToUri(context, request.sourceUrl, targetDoc.uri)
            }
            if (success) {
                Toast.makeText(
                    context,
                    context.getString(R.string.guide_media_save_success, request.fileName),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(context, context.getString(R.string.guide_media_save_failed), Toast.LENGTH_SHORT).show()
            }
        }
    }

    BindBaStudentGuidePlayerLifecycleEffects(
        context = context,
        sourceUrl = sourceUrl,
        voicePlayer = voicePlayer
    )
    BindBaStudentGuideVoiceListenerEffect(
        context = context,
        voicePlayer = voicePlayer,
        playingVoiceUrl = playingVoiceUrl,
        onPlayingVoiceUrlChange = { playingVoiceUrl = it },
        onIsVoicePlayingChange = { isVoicePlaying = it },
        onVoicePlayProgressChange = { voicePlayProgress = it }
    )

    fun shareSource() {
        val raw = info?.sourceUrl?.ifBlank { sourceUrl } ?: sourceUrl
        val target = normalizeGuideUrl(raw)
        if (target.isBlank()) {
            Toast.makeText(context, shareSourceEmptyText, Toast.LENGTH_SHORT).show()
            return
        }
        runCatching {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, target)
            }
            val chooser = Intent.createChooser(intent, shareSourceChooserTitle).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        }.onFailure {
            Toast.makeText(context, shareSourceFailedText, Toast.LENGTH_SHORT).show()
        }
    }

    fun openExternal(rawUrl: String) {
        val target = normalizeGuideUrl(rawUrl)
        if (target.isBlank()) return
        runCatching {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(target)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }.onFailure {
            Toast.makeText(context, openLinkFailedText, Toast.LENGTH_SHORT).show()
        }
    }

    fun openGuideInPage(rawUrl: String) {
        val normalized = normalizeGuideUrl(rawUrl)
        val contentId = extractGuideContentIdFromUrl(normalized)
        val target = if (contentId != null && contentId > 0L) {
            "https://www.gamekee.com/ba/tj/$contentId.html"
        } else {
            normalized
        }
        if (target.isBlank()) return
        if (target == sourceUrl) return
        manualRefreshRequested = false
        BaStudentGuideStore.setCurrentUrl(target)
        sourceUrl = target
        error = null
        refreshSignal += 1
    }

    fun saveGuideMedia(rawMediaUrl: String, rawTitle: String) {
        val studentNamePrefix = info?.title?.trim().orEmpty()
        val request = buildGuideMediaSaveRequest(
            rawUrl = rawMediaUrl,
            rawTitle = rawTitle,
            rawPrefix = studentNamePrefix
        )
        if (request == null) {
            Toast.makeText(context, context.getString(R.string.guide_media_save_empty), Toast.LENGTH_SHORT).show()
            return
        }
        val useFixedSaveLocation = BASettingsStore.loadMediaSaveCustomEnabled()
        if (!useFixedSaveLocation) {
            pendingCustomSaveRequest = request
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = request.mimeType
                putExtra(Intent.EXTRA_TITLE, request.fileName)
            }
            customSaveLauncher.launch(intent)
            return
        }

        val fixedTreeUriRaw = BASettingsStore.loadMediaSaveFixedTreeUri()
        val fixedTreeUri = fixedTreeUriRaw.takeIf { it.isNotBlank() }?.let { raw ->
            runCatching { Uri.parse(raw) }.getOrNull()
        }
        if (fixedTreeUri != null) {
            pageScope.launch {
                val success = withContext(Dispatchers.IO) {
                    val treeDoc = DocumentFile.fromTreeUri(context, fixedTreeUri)
                        ?: return@withContext false
                    val targetDoc = createUniqueDocumentInTree(
                        tree = treeDoc,
                        mimeType = request.mimeType,
                        fileName = request.fileName
                    ) ?: return@withContext false
                    copyGuideMediaToUri(context, request.sourceUrl, targetDoc.uri)
                }
                if (success) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.guide_media_save_success, request.fileName),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    BASettingsStore.saveMediaSaveFixedTreeUri("")
                    pendingFixedSaveRequest = request
                    val pickerIntent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                        addFlags(
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                        )
                    }
                    fixedFolderLauncher.launch(pickerIntent)
                }
            }
            return
        }

        pendingFixedSaveRequest = request
        val pickerIntent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
            )
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, Uri.parse("content://com.android.externalstorage.documents/tree/primary%3ADownload"))
            }
        }
        fixedFolderLauncher.launch(pickerIntent)
    }

    fun toggleVoicePlayback(rawAudioUrl: String) {
        val target = normalizeGuidePlaybackSource(rawAudioUrl)
        if (target.isBlank()) return
        runCatching {
            if (playingVoiceUrl == target) {
                if (voicePlayer.isPlaying) {
                    voicePlayer.pause()
                    playingVoiceUrl = ""
                    isVoicePlaying = false
                    voicePlayProgress = 0f
                } else {
                    voicePlayer.play()
                    playingVoiceUrl = target
                    isVoicePlaying = true
                }
            } else {
                voicePlayer.setMediaItem(MediaItem.fromUri(target))
                voicePlayer.prepare()
                voicePlayer.play()
                playingVoiceUrl = target
                isVoicePlaying = true
                voicePlayProgress = 0f
            }
        }.onFailure {
            playingVoiceUrl = ""
            isVoicePlaying = false
            voicePlayProgress = 0f
            Toast.makeText(context, "语音播放失败", Toast.LENGTH_SHORT).show()
        }
    }

    fun selectBottomTab(index: Int) {
        if (index !in bottomTabs.indices) return
        val fromIndex = if (pagerState.isScrollInProgress) {
            pagerState.targetPage
        } else {
            pagerState.settledPage
        }
        if (index == fromIndex && !pagerState.isScrollInProgress) return
        showBottomBar = true
        selectedBottomTabIndex = index
        tabJumpJob?.cancel()
        tabJumpJob = pageScope.launch {
            pagerState.animateTabSwitch(
                fromIndex = fromIndex,
                targetIndex = index,
                animationsEnabled = transitionAnimationsEnabled,
                onFarJumpBefore = {
                    // Keep far jump immediate: dim instantly, jump, then fade in.
                    // This avoids lingering on the previous tab for an extra frame.
                    farJumpAlpha.snapTo(0.94f)
                },
                onFarJumpAfter = {
                    farJumpAlpha.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = resolvedMotionDuration(
                                AppMotionTokens.farJumpRestoreEmphasisMs,
                                transitionAnimationsEnabled
                            ),
                            easing = if (transitionAnimationsEnabled) FastOutSlowInEasing else LinearEasing
                        )
                    )
                }
            )
        }
    }

    BindBaStudentGuideSourceRestoreEffect(
        sourceUrl = sourceUrl,
        onSourceUrlChange = { sourceUrl = it },
        onErrorChange = { error = it }
    )
    BindBaStudentGuidePagerSyncEffects(
        sourceUrl = sourceUrl,
        bottomTabsSize = bottomTabs.size,
        selectedBottomTabIndex = selectedBottomTabIndex,
        pagerState = pagerState,
        onSelectedBottomTabIndexChange = { selectedBottomTabIndex = it }
    )
    BindBaStudentGuideVoiceProgressEffect(
        activeBottomTab = activeBottomTab,
        isVoicePlaying = isVoicePlaying,
        playingVoiceUrl = playingVoiceUrl,
        voicePlayer = voicePlayer,
        onVoicePlayProgressChange = { voicePlayProgress = it }
    )
    BindBaStudentGuidePrefetchEffects(
        context = context,
        sourceUrl = sourceUrl,
        guideSyncToken = guideSyncToken,
        info = info,
        activeBottomTab = activeBottomTab,
        galleryPrefetchRequested = galleryPrefetchRequested,
        onGalleryPrefetchRequestedChange = { galleryPrefetchRequested = it },
        staticImagePrefetchStage = staticImagePrefetchStage,
        onStaticImagePrefetchStageChange = { staticImagePrefetchStage = it },
        initialPrefetchCount = preloadPolicy.guideStaticPrefetchInitialCount,
        galleryExtraPrefetchCount = preloadPolicy.guideStaticPrefetchGalleryExtraCount,
        onGalleryCacheRevisionIncrease = { galleryCacheRevision += 1 }
    )
    BindBaStudentGuideInfoLoadEffect(
        context = context,
        sourceUrl = sourceUrl,
        refreshSignal = refreshSignal,
        transitionAnimationsEnabled = transitionAnimationsEnabled,
        initialFetchDelayMs = preloadPolicy.initialFetchDelayMs,
        manualRefreshRequested = manualRefreshRequested,
        onManualRefreshRequestedChange = { manualRefreshRequested = it },
        currentSourceUrlProvider = { sourceUrl },
        currentInfoProvider = { info },
        onInfoChange = { info = it },
        onErrorChange = { error = it },
        onLoadingChange = { loading = it }
    )
    val shareIcon = appLucideShareIcon()
    val refreshIcon = appLucideRefreshIcon()
    val actionItems = remember(shareSourceContentDescription, refreshContentDescription) {
        listOf(
            LiquidActionItem(
                icon = shareIcon,
                contentDescription = shareSourceContentDescription,
                onClick = { shareSource() }
            ),
            LiquidActionItem(
                icon = refreshIcon,
                contentDescription = refreshContentDescription,
                onClick = {
                    manualRefreshRequested = true
                    refreshSignal += 1
                }
            )
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MiuixTheme.colorScheme.background)
            .nestedScroll(bottomBarNestedScrollConnection),
        topBar = {
            TopAppBar(
                title = pageTitle,
                largeTitle = pageTitle,
                scrollBehavior = scrollBehavior,
                color = topBarMaterialBackdrop.getMiuixAppBarColor(),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = appLucideBackIcon(),
                            contentDescription = null,
                            tint = MiuixTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    Box {
                        LiquidActionBar(
                            backdrop = topBarBackdrop,
                            layeredStyleEnabled = liquidActionBarLayeredStyleEnabled,
                            items = actionItems
                        )
                    }
                }
            )
        },
        bottomBar = {
            BaStudentGuideBottomBar(
                visible = showBottomBar,
                navigationBarBottom = navigationBarBottom,
                bottomTabs = bottomTabs.toList(),
                selectedPage = pagerState.targetPage,
                backdrop = navBackdrop,
                isLiquidEffectEnabled = liquidBottomBarEnabled,
                onSelectTab = ::selectBottomTab
            )
        }
    ) { innerPadding ->
        BaStudentGuidePagerContent(
            sourceUrl = sourceUrl,
            info = info,
            error = error,
            pagerState = pagerState,
            bottomTabs = bottomTabs.toList(),
            syncProgress = syncProgress,
            activationCount = activationCount,
            surfaceColor = surfaceColor,
            accent = accent,
            innerPadding = innerPadding,
            farJumpAlpha = farJumpAlpha.value,
            navBackdrop = navBackdrop,
            galleryCacheRevision = galleryCacheRevision,
            selectedVoiceLanguage = selectedVoiceLanguage,
            playingVoiceUrl = playingVoiceUrl,
            isVoicePlaying = isVoicePlaying,
            voicePlayProgress = voicePlayProgress,
            includeTargetPageInHeavyRender = preloadPolicy.includeTargetPageInHeavyRender,
            guidePagerBeyondViewportPageCount = preloadPolicy.guidePagerBeyondViewportPageCount,
            nestedScrollConnection = scrollBehavior.nestedScrollConnection,
            onOpenExternal = ::openExternal,
            onOpenGuide = ::openGuideInPage,
            onSaveMedia = ::saveGuideMedia,
            onToggleVoicePlayback = ::toggleVoicePlayback,
            onSelectedVoiceLanguageChange = { selectedVoiceLanguage = it }
        )
    }
}
