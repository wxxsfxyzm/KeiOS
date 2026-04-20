package com.example.keios.ui.page.main

import androidx.activity.ComponentActivity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.size.Precision
import coil3.size.Scale
import com.example.keios.core.log.AppLogger
import com.example.keios.core.system.ShizukuApiUtils
import com.example.keios.mcp.server.McpServerManager
import com.example.keios.ui.navigation.KeiosRoute
import com.example.keios.ui.navigation.Navigator
import com.example.keios.ui.page.main.model.BottomPage
import com.example.keios.ui.page.main.widget.glass.UiPerformanceBudget
import com.example.keios.ui.page.main.widget.motion.AppMotionTokens
import com.example.keios.ui.page.main.widget.motion.LocalTransitionAnimationsEnabled
import com.example.keios.ui.page.main.widget.motion.resolvedMotionDuration
import com.example.keios.ui.perf.ReportPagerPerformanceState
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun MainPagerLayout(
    navigator: Navigator,
    settingsReturnToken: Int,
    liquidBottomBarEnabled: Boolean,
    liquidActionBarLayeredStyleEnabled: Boolean,
    cardPressFeedbackEnabled: Boolean,
    homeIconHdrEnabled: Boolean,
    preloadingEnabled: Boolean,
    nonHomeBackgroundEnabled: Boolean,
    nonHomeBackgroundUri: String,
    nonHomeBackgroundOpacity: Float,
    visibleBottomPageNames: Set<String>,
    onVisibleBottomPageNamesChange: (Set<String>) -> Unit,
    shizukuStatus: String,
    shizukuApiUtils: ShizukuApiUtils,
    mcpServerManager: McpServerManager,
    onOpenGuideDetail: (String) -> Unit,
    requestedBottomPage: String?,
    requestedBottomPageToken: Int,
    requestedGitHubRefreshToken: Int,
    onRequestedBottomPageConsumed: () -> Unit
) {
    val transitionAnimationsEnabled = LocalTransitionAnimationsEnabled.current
    val insets = rememberMainPagerInsets()
    val coordinator = rememberMainPagerCoordinator(
        settingsReturnToken = settingsReturnToken,
        transitionAnimationsEnabled = transitionAnimationsEnabled,
        preloadingEnabled = preloadingEnabled,
        nonHomeBackgroundEnabled = nonHomeBackgroundEnabled,
        nonHomeBackgroundUri = nonHomeBackgroundUri,
        visibleBottomPageNames = visibleBottomPageNames,
        onVisibleBottomPageNamesChange = onVisibleBottomPageNamesChange,
        mcpServerManager = mcpServerManager,
        requestedBottomPage = requestedBottomPage,
        requestedBottomPageToken = requestedBottomPageToken,
        onRequestedBottomPageConsumed = onRequestedBottomPageConsumed
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MiuixTheme.colorScheme.background)
            .nestedScroll(coordinator.nestedScrollConnection),
        bottomBar = {
            MainPagerBottomBar(
                visible = coordinator.showBottomBar,
                navigationBarBottom = insets.navigationBarBottom,
                tabs = coordinator.tabs,
                selectedPageIndex = coordinator.pagerState.targetPage,
                backdrop = coordinator.backdrop,
                reduceEffectsDuringPagerScroll = coordinator.pagerState.isScrollInProgress,
                liquidBottomBarEnabled = liquidBottomBarEnabled,
                onPageSelected = { index ->
                    if (index != coordinator.pagerState.targetPage) {
                        coordinator.onPageSelected(index)
                    }
                }
            )
        }
    ) { _ ->
        Box(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = coordinator.pagerState,
                key = { index -> coordinator.tabs[index].name },
                userScrollEnabled = coordinator.pagerScrollEnabled,
                overscrollEffect = null,
                beyondViewportPageCount = coordinator.pagerRuntime.resolvedBeyondViewportPageCount,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = coordinator.farJumpAlpha }
                    .layerBackdrop(coordinator.backdrop)
            ) { pageIndex ->
                MainPagerPageHost(
                    pageType = coordinator.tabs[pageIndex],
                    pageIndex = pageIndex,
                    pagerRuntime = coordinator.pagerRuntime,
                    visibleBottomPages = coordinator.visibleTabsSnapshot,
                    shizukuStatus = shizukuStatus,
                    shizukuApiUtils = shizukuApiUtils,
                    cardPressFeedbackEnabled = cardPressFeedbackEnabled,
                    liquidActionBarLayeredStyleEnabled = liquidActionBarLayeredStyleEnabled,
                    homeIconHdrEnabled = homeIconHdrEnabled,
                    preloadingEnabled = preloadingEnabled,
                    mcpServerManager = mcpServerManager,
                    homeMcpOverview = coordinator.homeMcpOverview,
                    homeGitHubOverview = coordinator.homeGitHubOverview,
                    homeBaOverview = coordinator.homeBaOverview,
                    homeTopInset = insets.homeTopInset,
                    homeBottomInset = insets.homeBottomInset,
                    bottomOverlayPadding = insets.bottomOverlayPadding,
                    requestedGitHubRefreshToken = requestedGitHubRefreshToken,
                    osScrollToTopSignal = coordinator.osScrollToTopSignal,
                    baScrollToTopSignal = coordinator.baScrollToTopSignal,
                    mcpScrollToTopSignal = coordinator.mcpScrollToTopSignal,
                    githubScrollToTopSignal = coordinator.githubScrollToTopSignal,
                    onBottomPageVisibilityChange = coordinator.onBottomPageVisibilityChange,
                    onOpenSettings = { navigator.pushSingleTop(KeiosRoute.Settings) },
                    onOpenAbout = { navigator.pushSingleTop(KeiosRoute.About) },
                    onOpenPoolGuideDetail = onOpenGuideDetail,
                    onOpenBaGuideCatalog = { navigator.pushSingleTop(KeiosRoute.BaGuideCatalog) },
                    onOpenMcpSkill = { navigator.pushSingleTop(KeiosRoute.McpSkill) },
                    onActionBarInteractingChanged = coordinator.onActionBarInteractingChanged
                )
            }

            if (coordinator.pagerRuntime.shouldRenderNonHomeBackground) {
                NonHomePageBackground(
                    enabled = coordinator.hasNonHomeBackground,
                    imageUri = coordinator.effectiveNonHomeBackgroundUri,
                    opacity = nonHomeBackgroundOpacity,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

private data class MainPagerInsets(
    val navigationBarBottom: Dp,
    val homeTopInset: Dp,
    val homeBottomInset: Dp,
    val bottomOverlayPadding: Dp
)

private data class MainPagerHomeOverviewState(
    val homeMcpOverview: HomeMcpOverview,
    val homeGitHubOverview: HomeGitHubOverview,
    val homeBaOverview: HomeBaOverview
)

private data class MainPagerCoordinatorState(
    val tabs: List<BottomPage>,
    val visibleTabsSnapshot: Set<BottomPage>,
    val pagerState: PagerState,
    val pagerRuntime: MainPagerRuntimeSnapshot,
    val homeMcpOverview: HomeMcpOverview,
    val homeGitHubOverview: HomeGitHubOverview,
    val homeBaOverview: HomeBaOverview,
    val pagerScrollEnabled: Boolean,
    val showBottomBar: Boolean,
    val nestedScrollConnection: NestedScrollConnection,
    val backdrop: LayerBackdrop,
    val farJumpAlpha: Float,
    val hasNonHomeBackground: Boolean,
    val effectiveNonHomeBackgroundUri: String,
    val onPageSelected: (Int) -> Unit,
    val onActionBarInteractingChanged: (Boolean) -> Unit,
    val onBottomPageVisibilityChange: (BottomPage, Boolean) -> Unit,
    val osScrollToTopSignal: Int,
    val baScrollToTopSignal: Int,
    val mcpScrollToTopSignal: Int,
    val githubScrollToTopSignal: Int
)

@Composable
private fun rememberMainPagerInsets(): MainPagerInsets {
    val density = LocalDensity.current
    val navigationBarBottom = with(density) { WindowInsets.navigationBars.getBottom(this).toDp() }
    val systemInsets = WindowInsets.safeDrawing.union(WindowInsets.navigationBars).asPaddingValues()
    return remember(navigationBarBottom, systemInsets) {
        MainPagerInsets(
            navigationBarBottom = navigationBarBottom,
            homeTopInset = systemInsets.calculateTopPadding(),
            homeBottomInset = systemInsets.calculateBottomPadding(),
            bottomOverlayPadding = 112.dp + navigationBarBottom
        )
    }
}

@Composable
private fun rememberMainPagerHomeOverviewState(
    mcpServerManager: McpServerManager,
    settingsReturnToken: Int
): MainPagerHomeOverviewState {
    val mcpUiState by mcpServerManager.uiState.collectAsState()
    var homeGitHubOverview by remember { mutableStateOf(HomeGitHubOverview()) }
    var homeBaOverview by remember { mutableStateOf(HomeBaOverview()) }
    val homeMcpOverview = remember(mcpUiState) {
        HomeMcpOverview(
            running = mcpUiState.running,
            runningSinceEpochMs = mcpUiState.runningSinceEpochMs,
            port = mcpUiState.port,
            endpointPath = mcpUiState.endpointPath,
            serverName = mcpUiState.serverName,
            authTokenConfigured = mcpUiState.authToken.isNotBlank(),
            connectedClients = mcpUiState.connectedClients,
            allowExternal = mcpUiState.allowExternal
        )
    }

    suspend fun refreshHomeOverviewState(reason: String) {
        val (baOverview, githubOverview) = withContext(Dispatchers.IO) {
            val loadedBaOverview = runCatching { loadHomeBaOverview() }
                .onFailure { error ->
                    AppLogger.w(
                        "MainScreen",
                        "loadHomeBaOverview failed (reason=$reason)",
                        error
                    )
                }
                .getOrElse { HomeBaOverview(loaded = true) }
            val loadedGitHubOverview = runCatching { loadHomeGitHubOverview() }
                .onFailure { error ->
                    AppLogger.w(
                        "MainScreen",
                        "loadHomeGitHubOverview failed (reason=$reason)",
                        error
                    )
                }
                .getOrElse { HomeGitHubOverview(loaded = true) }
            loadedBaOverview to loadedGitHubOverview
        }
        homeBaOverview = baOverview
        homeGitHubOverview = githubOverview
    }

    LaunchedEffect(Unit) {
        if (homeBaOverview.loaded && homeGitHubOverview.loaded) return@LaunchedEffect
        refreshHomeOverviewState(reason = "initial")
    }
    LaunchedEffect(settingsReturnToken) {
        if (settingsReturnToken <= 0) return@LaunchedEffect
        refreshHomeOverviewState(reason = "settings_return_$settingsReturnToken")
    }

    return remember(homeMcpOverview, homeGitHubOverview, homeBaOverview) {
        MainPagerHomeOverviewState(
            homeMcpOverview = homeMcpOverview,
            homeGitHubOverview = homeGitHubOverview,
            homeBaOverview = homeBaOverview
        )
    }
}

@Composable
private fun rememberMainPagerBackdrop(): LayerBackdrop {
    val context = LocalContext.current
    var backdropGeneration by rememberSaveable { mutableIntStateOf(0) }
    val activityLifecycle = remember(context) { (context as? ComponentActivity)?.lifecycle }

    DisposableEffect(activityLifecycle) {
        val lifecycle = activityLifecycle ?: return@DisposableEffect onDispose { }
        var appWentBackground = false
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> appWentBackground = true
                Lifecycle.Event.ON_START -> {
                    if (appWentBackground) {
                        backdropGeneration++
                        appWentBackground = false
                    }
                }
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    val surfaceColor = MiuixTheme.colorScheme.surface
    return key("main-backdrop-$backdropGeneration") {
        rememberLayerBackdrop {
            drawRect(surfaceColor)
            drawContent()
        }
    }
}

@Composable
private fun rememberMainPagerCoordinator(
    settingsReturnToken: Int,
    transitionAnimationsEnabled: Boolean,
    preloadingEnabled: Boolean,
    nonHomeBackgroundEnabled: Boolean,
    nonHomeBackgroundUri: String,
    visibleBottomPageNames: Set<String>,
    onVisibleBottomPageNamesChange: (Set<String>) -> Unit,
    mcpServerManager: McpServerManager,
    requestedBottomPage: String?,
    requestedBottomPageToken: Int,
    onRequestedBottomPageConsumed: () -> Unit
): MainPagerCoordinatorState {
    val preloadPolicy = remember(preloadingEnabled) {
        UiPerformanceBudget.resolvePreloadPolicy(preloadingEnabled)
    }
    val effectiveNonHomeBackgroundUri = remember(nonHomeBackgroundEnabled, nonHomeBackgroundUri) {
        if (nonHomeBackgroundEnabled) nonHomeBackgroundUri.trim() else ""
    }
    val hasNonHomeBackground = remember(effectiveNonHomeBackgroundUri) {
        effectiveNonHomeBackgroundUri.isNotBlank()
    }
    val tabs = remember(visibleBottomPageNames) {
        BottomPage.entries.filter { page ->
            page == BottomPage.Home || visibleBottomPageNames.contains(page.name)
        }
    }
    val initialPageIndex = remember(tabs, requestedBottomPage, requestedBottomPageToken) {
        val target = requestedBottomPage?.trim().orEmpty()
        tabs.indexOfFirst { it.name == target }
            .takeIf { it >= 0 }
            ?: 0
    }
    val visibleTabsSnapshot = remember(tabs) { tabs.toSet() }
    val pagerState = rememberPagerState(
        initialPage = initialPageIndex,
        pageCount = { tabs.size }
    )
    val coroutineScope = rememberCoroutineScope()
    var tabJumpJob by remember { mutableStateOf<Job?>(null) }
    var pagerScrollEnabled by remember { mutableStateOf(true) }
    var showBottomBar by remember { mutableStateOf(true) }
    var temporaryBeyondViewportCount by remember { mutableStateOf<Int?>(null) }
    val farJumpAlpha = remember { Animatable(1f) }
    val homeOverviewState = rememberMainPagerHomeOverviewState(
        mcpServerManager = mcpServerManager,
        settingsReturnToken = settingsReturnToken
    )
    val backdrop = rememberMainPagerBackdrop()

    val pagerRuntime = buildMainPagerRuntimeSnapshot(
        tabs = tabs,
        currentPageIndex = pagerState.currentPage,
        targetPageIndex = pagerState.targetPage,
        settledPageIndex = pagerState.settledPage,
        isPagerScrollInProgress = pagerState.isScrollInProgress,
        preloadPolicy = preloadPolicy,
        temporaryBeyondViewportCount = temporaryBeyondViewportCount,
        hasNonHomeBackground = hasNonHomeBackground
    )

    LaunchedEffect(settingsReturnToken, transitionAnimationsEnabled) {
        if (settingsReturnToken <= 0) return@LaunchedEffect
        temporaryBeyondViewportCount = 0
        withFrameNanos { }
        delay(resolvedMotionDuration(220, transitionAnimationsEnabled).toLong())
        temporaryBeyondViewportCount = null
    }
    LaunchedEffect(pagerRuntime.homePageBottomBarPinned) {
        if (pagerRuntime.homePageBottomBarPinned && !showBottomBar) {
            showBottomBar = true
        }
    }
    LaunchedEffect(tabs.size) {
        val lastIndex = tabs.lastIndex
        if (lastIndex >= 0 && pagerState.currentPage > lastIndex) {
            pagerState.scrollToPage(lastIndex)
        }
    }

    val farJumpBefore: suspend () -> Unit = {
        if (!transitionAnimationsEnabled) {
            farJumpAlpha.snapTo(1f)
        } else {
            farJumpAlpha.snapTo(1f)
            farJumpAlpha.animateTo(
                targetValue = 0.92f,
                animationSpec = tween(
                    durationMillis = resolvedMotionDuration(
                        AppMotionTokens.farJumpDimMs,
                        transitionAnimationsEnabled
                    )
                )
            )
        }
    }
    val farJumpAfter: suspend () -> Unit = {
        if (!transitionAnimationsEnabled) {
            farJumpAlpha.snapTo(1f)
        } else {
            farJumpAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = resolvedMotionDuration(
                        AppMotionTokens.farJumpRestoreMs,
                        transitionAnimationsEnabled
                    )
                )
            )
        }
    }

    val nestedScrollConnection = remember(pagerRuntime.homePageBottomBarPinned, showBottomBar) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (pagerRuntime.homePageBottomBarPinned) {
                    if (!showBottomBar) {
                        showBottomBar = true
                    }
                    return Offset.Zero
                }
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
    val onActionBarInteractingChanged: (Boolean) -> Unit = { interacting ->
        pagerScrollEnabled = !interacting
    }
    val onBottomPageVisibilityChange: (BottomPage, Boolean) -> Unit = { page, visible ->
        if (page != BottomPage.Home) {
            val updated = visibleBottomPageNames
                .toMutableSet()
                .apply {
                    if (visible) add(page.name) else remove(page.name)
                }
                .toSet()
            onVisibleBottomPageNamesChange(updated)
        }
    }
    val onPageSelected: (Int) -> Unit = { index ->
        if (index in tabs.indices) {
            showBottomBar = true
            if (index != pagerRuntime.stablePageIndex || pagerRuntime.isPagerScrollInProgress) {
                tabJumpJob?.cancel()
                tabJumpJob = coroutineScope.launch {
                    pagerState.animateTabSwitch(
                        fromIndex = pagerRuntime.stablePageIndex,
                        targetIndex = index,
                        animationsEnabled = transitionAnimationsEnabled,
                        onFarJumpBefore = farJumpBefore,
                        onFarJumpAfter = farJumpAfter
                    )
                }
            }
        }
    }

    LaunchedEffect(requestedBottomPageToken, requestedBottomPage, tabs) {
        val target = requestedBottomPage ?: return@LaunchedEffect
        val index = tabs.indexOfFirst { it.name == target }
        if (index >= 0 && index != pagerRuntime.stablePageIndex) {
            tabJumpJob?.cancel()
            tabJumpJob = coroutineScope.launch {
                pagerState.animateTabSwitch(
                    fromIndex = pagerRuntime.stablePageIndex,
                    targetIndex = index,
                    animationsEnabled = transitionAnimationsEnabled,
                    onFarJumpBefore = farJumpBefore,
                    onFarJumpAfter = farJumpAfter
                )
            }
            showBottomBar = true
        }
        onRequestedBottomPageConsumed()
    }

    ReportPagerPerformanceState(
        scope = "main_pager",
        currentPage = tabs.getOrElse(pagerState.currentPage) { BottomPage.Home }.name,
        targetPage = tabs.getOrElse(pagerState.targetPage) { BottomPage.Home }.name,
        scrolling = pagerState.isScrollInProgress
    )

    return remember(
        tabs,
        visibleTabsSnapshot,
        pagerState,
        pagerRuntime,
        homeOverviewState,
        pagerScrollEnabled,
        showBottomBar,
        nestedScrollConnection,
        backdrop,
        farJumpAlpha.value,
        hasNonHomeBackground,
        effectiveNonHomeBackgroundUri,
        onPageSelected,
        onActionBarInteractingChanged,
        onBottomPageVisibilityChange
    ) {
        MainPagerCoordinatorState(
            tabs = tabs,
            visibleTabsSnapshot = visibleTabsSnapshot,
            pagerState = pagerState,
            pagerRuntime = pagerRuntime,
            homeMcpOverview = homeOverviewState.homeMcpOverview,
            homeGitHubOverview = homeOverviewState.homeGitHubOverview,
            homeBaOverview = homeOverviewState.homeBaOverview,
            pagerScrollEnabled = pagerScrollEnabled,
            showBottomBar = showBottomBar,
            nestedScrollConnection = nestedScrollConnection,
            backdrop = backdrop,
            farJumpAlpha = farJumpAlpha.value,
            hasNonHomeBackground = hasNonHomeBackground,
            effectiveNonHomeBackgroundUri = effectiveNonHomeBackgroundUri,
            onPageSelected = onPageSelected,
            onActionBarInteractingChanged = onActionBarInteractingChanged,
            onBottomPageVisibilityChange = onBottomPageVisibilityChange,
            osScrollToTopSignal = 0,
            baScrollToTopSignal = 0,
            mcpScrollToTopSignal = 0,
            githubScrollToTopSignal = 0
        )
    }
}

@Composable
private fun NonHomePageBackground(
    enabled: Boolean,
    imageUri: String,
    opacity: Float,
    modifier: Modifier = Modifier
) {
    if (!enabled || imageUri.isBlank()) return
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val (targetWidthPx, targetHeightPx) = remember(configuration, density) {
        with(density) {
            val width = configuration.screenWidthDp.dp.roundToPx().coerceAtLeast(1)
            val height = configuration.screenHeightDp.dp.roundToPx().coerceAtLeast(1)
            width to height
        }
    }
    val request = remember(imageUri, targetWidthPx, targetHeightPx) {
        ImageRequest.Builder(context)
            .data(imageUri)
            .size(targetWidthPx, targetHeightPx)
            .scale(Scale.FILL)
            .precision(Precision.INEXACT)
            .build()
    }
    AsyncImage(
        model = request,
        contentDescription = null,
        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
        alpha = opacity.coerceIn(0f, 1f),
        modifier = modifier
    )
}
