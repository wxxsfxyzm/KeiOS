package com.example.keios.ui.page.main.student.page.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.keios.R
import com.example.keios.ui.page.main.student.BaStudentGuideInfo
import com.example.keios.ui.page.main.student.GuideBottomTab
import com.example.keios.ui.page.main.student.tabcontent.renderBaStudentGuideTabContent
import com.example.keios.ui.page.main.widget.glass.FrostedBlock
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.ProgressIndicatorDefaults
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun BaStudentGuidePagerContent(
    sourceUrl: String,
    info: BaStudentGuideInfo?,
    error: String?,
    pagerState: PagerState,
    bottomTabs: List<GuideBottomTab>,
    syncProgress: Float,
    activationCount: Int,
    surfaceColor: Color,
    accent: Color,
    innerPadding: PaddingValues,
    farJumpAlpha: Float,
    navBackdrop: LayerBackdrop,
    galleryCacheRevision: Int,
    selectedVoiceLanguage: String,
    playingVoiceUrl: String,
    isVoicePlaying: Boolean,
    voicePlayProgress: Float,
    includeTargetPageInHeavyRender: Boolean,
    guidePagerBeyondViewportPageCount: Int,
    nestedScrollConnection: NestedScrollConnection,
    onOpenExternal: (String) -> Unit,
    onOpenGuide: (String) -> Unit,
    onSaveMedia: (String, String) -> Unit,
    onToggleVoicePlayback: (String) -> Unit,
    onSelectedVoiceLanguageChange: (String) -> Unit
) {
    val context = LocalContext.current
    HorizontalPager(
        state = pagerState,
        key = { index -> bottomTabs[index].name },
        overscrollEffect = null,
        beyondViewportPageCount = guidePagerBeyondViewportPageCount,
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = farJumpAlpha }
            .layerBackdrop(navBackdrop)
    ) { pageIndex ->
        val pageBottomTab = bottomTabs.getOrElse(pageIndex) { GuideBottomTab.Archive }
        val isVoiceTab = pageBottomTab == GuideBottomTab.Voice
        val shouldRenderHeavyContent =
            pageIndex == pagerState.currentPage ||
                pageIndex == pagerState.settledPage ||
                (includeTargetPageInHeavyRender && pageIndex == pagerState.targetPage)
        val pageListState = rememberSaveable(
            sourceUrl,
            pageBottomTab.name,
            saver = LazyListState.Saver
        ) {
            LazyListState()
        }
        val pageBackdrop: LayerBackdrop = key("page-$activationCount-$sourceUrl-$pageIndex") {
            rememberLayerBackdrop {
                drawRect(surfaceColor)
                drawContent()
            }
        }
        Box(modifier = Modifier.fillMaxSize()) {
            if (shouldRenderHeavyContent) {
                LazyColumn(
                    state = pageListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(nestedScrollConnection),
                    contentPadding = PaddingValues(
                        top = innerPadding.calculateTopPadding(),
                        bottom = innerPadding.calculateBottomPadding() + 16.dp,
                        start = 16.dp,
                        end = 16.dp
                    )
                ) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                SmallTitle(pageBottomTab.label)
                            }
                            if (sourceUrl.isNotBlank()) {
                                val foregroundColor = when {
                                    info == null && error.isNullOrBlank() -> Color(0xFF3B82F6)
                                    !error.isNullOrBlank() -> Color(0xFFEF4444)
                                    else -> Color(0xFF22C55E)
                                }
                                CircularProgressIndicator(
                                    progress = syncProgress,
                                    size = 18.dp,
                                    strokeWidth = 2.dp,
                                    colors = ProgressIndicatorDefaults.progressIndicatorColors(
                                        foregroundColor = foregroundColor,
                                        backgroundColor = foregroundColor.copy(alpha = 0.30f),
                                    ),
                                )
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(12.dp)) }
                    if (sourceUrl.isBlank()) {
                        item {
                            FrostedBlock(
                                backdrop = pageBackdrop,
                                title = stringResource(R.string.guide_empty_student_title),
                                subtitle = stringResource(R.string.guide_empty_student_subtitle),
                                accent = accent
                            )
                        }
                    } else {
                        renderBaStudentGuideTabContent(
                            activeBottomTab = pageBottomTab,
                            info = info,
                            error = error,
                            backdrop = pageBackdrop,
                            accent = accent,
                            context = context,
                            sourceUrl = sourceUrl,
                            galleryCacheRevision = galleryCacheRevision,
                            playingVoiceUrl = if (isVoiceTab) playingVoiceUrl else "",
                            isVoicePlaying = isVoiceTab && isVoicePlaying,
                            voicePlayProgress = if (isVoiceTab) voicePlayProgress else 0f,
                            selectedVoiceLanguage = if (isVoiceTab) selectedVoiceLanguage else "",
                            onOpenExternal = onOpenExternal,
                            onOpenGuide = onOpenGuide,
                            onSaveMedia = onSaveMedia,
                            onToggleVoicePlayback = if (isVoiceTab) onToggleVoicePlayback else { _ -> },
                            onSelectedVoiceLanguageChange = if (isVoiceTab) onSelectedVoiceLanguageChange else { _ -> }
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.fillMaxSize())
            }
            if (shouldRenderHeavyContent && sourceUrl.isNotBlank() && info == null && error.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = innerPadding.calculateTopPadding(),
                            bottom = innerPadding.calculateBottomPadding(),
                            start = 20.dp,
                            end = 20.dp
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.q_862c2944),
                            contentDescription = null,
                            modifier = Modifier
                                .size(112.dp)
                        )
                        Text(
                            text = stringResource(R.string.guide_loading_title),
                            color = MiuixTheme.colorScheme.onBackground,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
