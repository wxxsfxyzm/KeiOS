package com.example.keios.ui.page.main.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.foundation.Image
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shadow as ComposeTextShadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.keios.R
import com.example.keios.ui.page.main.home.model.HomeBaOverview
import com.example.keios.ui.page.main.home.model.HomeGitHubOverview
import com.example.keios.ui.page.main.home.model.HomeMcpOverview
import com.example.keios.ui.page.main.home.model.HomeOverviewCard
import com.example.keios.ui.page.main.home.model.formatGitHubCacheAgo
import com.example.keios.ui.page.main.home.model.loadHomeVisibleOverviewCards
import com.example.keios.ui.page.main.home.model.saveHomeVisibleOverviewCards
import com.example.keios.ui.page.main.home.state.rememberHomePageHeroMotionState
import com.example.keios.ui.page.main.home.state.rememberHomePageOverviewCardState
import com.example.keios.ui.page.main.host.pager.MainPageRuntime
import com.example.keios.ui.page.main.model.BottomPage
import com.example.keios.ui.page.main.widget.glass.GlassIconButton
import com.example.keios.ui.page.main.widget.glass.GlassVariant
import com.example.keios.ui.page.main.widget.chrome.LiquidActionBar
import com.example.keios.ui.page.main.widget.chrome.LiquidActionItem
import com.example.keios.ui.page.main.widget.sheet.SheetContentColumn
import com.example.keios.ui.page.main.widget.sheet.SheetControlRow
import com.example.keios.ui.page.main.widget.sheet.SheetDescriptionText
import com.example.keios.ui.page.main.widget.sheet.SheetSectionCard
import com.example.keios.ui.page.main.widget.sheet.SheetSectionTitle
import com.example.keios.ui.page.main.widget.sheet.SnapshotWindowBottomSheet
import com.example.keios.ui.page.main.widget.status.StatusPill
import com.example.keios.ui.page.main.widget.status.StatusLabelText
import com.example.keios.feature.github.model.GitHubLookupStrategyOption
import com.example.keios.ui.page.main.mcp.util.formatMcpUptimeText
import com.example.keios.ui.page.main.os.appLucideCloseIcon
import com.example.keios.ui.page.main.os.appLucideInfoIcon
import com.example.keios.ui.page.main.os.appLucideLayersIcon
import com.example.keios.ui.page.main.os.osLucideSettingsIcon
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop as rememberActionBarBackdrop
import com.rosan.installer.ui.library.effect.BgEffectBackground
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.blur.isRenderEffectSupported
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop as rememberMiuixLayerBackdrop
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun HomePage(
    shizukuStatus: String,
    mcpOverview: HomeMcpOverview = HomeMcpOverview(),
    homeGitHubOverview: HomeGitHubOverview = HomeGitHubOverview(),
    homeBaOverview: HomeBaOverview = HomeBaOverview(),
    homeIconHdrEnabled: Boolean,
    runtime: MainPageRuntime = MainPageRuntime(),
    liquidActionBarLayeredStyleEnabled: Boolean = true,
    visibleBottomPages: Set<BottomPage>,
    onBottomPageVisibilityChange: (BottomPage, Boolean) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit,
    onActionBarInteractingChanged: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val layoutDirection = LocalLayoutDirection.current
    val density = LocalDensity.current
    val lazyListState = rememberLazyListState()
    val topAppBarScrollBehavior = MiuixScrollBehavior()

    val blurEnabled = isRenderEffectSupported()
    val dynamicBackgroundEnabled = isRuntimeShaderSupported()
    val effectBackgroundEnabled = isRuntimeShaderSupported()
    val surfaceColor = MiuixTheme.colorScheme.surface
    val backdrop = rememberMiuixLayerBackdrop()
    val actionBarBackdrop = rememberActionBarBackdrop {
        drawRect(surfaceColor)
        drawContent()
    }
    val homeCardBackdrop = rememberActionBarBackdrop {
        drawContent()
    }

    val shizukuGranted = shizukuStatus.contains("granted", ignoreCase = true)
    val runningColor = Color(0xFF2E7D32)
    val stoppedColor = Color(0xFFC62828)
    val inactiveColor = MiuixTheme.colorScheme.onBackgroundVariant
    val githubCacheColor = Color(0xFFF59E0B)

    val homeAppVersionUnknownFallback = stringResource(R.string.home_app_version_unknown_fallback)
    val homeAppVersionUnknown = stringResource(R.string.home_app_version_unknown)
    val appVersionText = remember(homeAppVersionUnknownFallback, homeAppVersionUnknown) {
        runCatching {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            "v${info.versionName ?: homeAppVersionUnknownFallback} (${info.longVersionCode})"
        }.getOrDefault(homeAppVersionUnknown)
    }

    val githubOverview = homeGitHubOverview
    val baOverview = homeBaOverview
    val trackedCount = githubOverview.trackedCount
    val cacheHitCount = githubOverview.cacheHitCount
    val updatableCount = githubOverview.updatableCount
    val preReleaseUpdateCount = githubOverview.preReleaseUpdateCount
    val cacheStateColor = when {
        !githubOverview.loaded -> inactiveColor
        cacheHitCount > 0 -> githubCacheColor
        else -> inactiveColor
    }

    val homeStatusLoading = stringResource(R.string.home_status_loading)
    val homeGitHubUnconfigured = stringResource(R.string.home_github_status_unconfigured)
    val homeGitHubNoCache = stringResource(R.string.home_github_status_no_cache)
    val homeGitHubPendingRefresh = stringResource(R.string.home_github_status_pending_refresh)
    val homeJustNow = stringResource(R.string.home_time_just_now)
    val homeNa = stringResource(R.string.common_na)
    val homeAppName = stringResource(R.string.app_name)
    val homeTagline = stringResource(R.string.home_header_tagline)
    val homeStatusMcp = stringResource(R.string.page_mcp_title)
    val homeStatusGitHub = stringResource(R.string.github_page_title)
    val homeStatusBa = stringResource(R.string.home_status_ba)
    val homeStatusShizuku = stringResource(R.string.home_status_shizuku)
    val homeCardMcp = stringResource(R.string.home_card_title_mcp)
    val homeCardGitHub = stringResource(R.string.home_card_title_github_cache)
    val homeCardBa = stringResource(R.string.home_card_title_ba)
    val homeVisibleCardsTitle = stringResource(R.string.home_sheet_visible_cards_title)
    val homeVisibleCardsDesc = stringResource(R.string.home_sheet_visible_cards_desc)
    val homeStatStatus = stringResource(R.string.home_stat_status)
    val homeStatRuntime = stringResource(R.string.home_stat_runtime)
    val homeStatClients = stringResource(R.string.home_stat_clients)
    val homeStatNetwork = stringResource(R.string.home_stat_network)
    val homeStatPort = stringResource(R.string.home_stat_port)
    val homeStatPath = stringResource(R.string.home_stat_path)
    val homeStatService = stringResource(R.string.home_stat_service)
    val homeStatToken = stringResource(R.string.home_stat_token)
    val homeStatStableUpdates = stringResource(R.string.home_stat_stable_updates)
    val homeStatPreReleaseUpdates = stringResource(R.string.home_stat_prerelease_updates)
    val homeStatTracked = stringResource(R.string.home_stat_tracked)
    val homeStatCached = stringResource(R.string.home_stat_cached)
    val homeStatStrategy = stringResource(R.string.home_stat_strategy)
    val homeStatApi = stringResource(R.string.home_stat_api)
    val homeStatLastUpdate = stringResource(R.string.home_stat_last_update)
    val homeStatAp = stringResource(R.string.home_stat_ap)
    val homeStatCafeAp = stringResource(R.string.home_stat_cafe_ap)
    val homeStatApRemaining = stringResource(R.string.home_stat_ap_remaining)
    val homeBaStatusActive = stringResource(R.string.home_ba_status_active)
    val homeBaStatusInactive = stringResource(R.string.home_ba_status_inactive)
    val homeMcpRuntimePending = stringResource(R.string.mcp_runtime_pending)
    val homeCommonFilled = stringResource(R.string.common_filled)
    val homeCommonNotUsed = stringResource(R.string.common_not_used)
    val networkModeText = if (mcpOverview.allowExternal) {
        stringResource(R.string.mcp_network_mode_lan_accessible)
    } else {
        stringResource(R.string.mcp_network_mode_local_only_short)
    }
    val mcpRuntimeText = if (!mcpOverview.running || mcpOverview.runningSinceEpochMs <= 0L) {
        homeMcpRuntimePending
    } else {
        formatMcpUptimeText(System.currentTimeMillis() - mcpOverview.runningSinceEpochMs)
    }
    val mcpStatusText = if (mcpOverview.running) {
        stringResource(R.string.home_mcp_status_running)
    } else {
        stringResource(R.string.home_mcp_status_stopped)
    }
    val mcpTokenStatusText = if (mcpOverview.authTokenConfigured) homeCommonFilled else homeCommonNotUsed
    val githubStrategyText = when (githubOverview.strategy) {
        GitHubLookupStrategyOption.AtomFeed -> stringResource(R.string.github_overview_strategy_atom)
        GitHubLookupStrategyOption.GitHubApiToken -> stringResource(R.string.github_overview_strategy_api)
    }
    val githubApiText = when {
        githubOverview.strategy != GitHubLookupStrategyOption.GitHubApiToken -> homeCommonNotUsed
        githubOverview.apiTokenConfigured -> homeCommonFilled
        else -> stringResource(R.string.common_guest)
    }
    val cacheRefreshLine = formatGitHubCacheAgo(
        lastRefreshMs = githubOverview.cachedRefreshMs,
        notRefreshedText = stringResource(R.string.github_refresh_ago_not_refreshed),
        justNowText = homeJustNow
    )
    val githubLastUpdateLine = when {
        !githubOverview.loaded -> homeStatusLoading
        trackedCount == 0 -> homeGitHubUnconfigured
        cacheHitCount == 0 -> homeGitHubNoCache
        else -> cacheRefreshLine
    }
    val githubUpdatableLine = when {
        !githubOverview.loaded -> homeStatusLoading
        trackedCount == 0 -> stringResource(R.string.github_overview_value_count, 0)
        cacheHitCount == 0 -> homeGitHubPendingRefresh
        else -> stringResource(R.string.github_overview_value_count, updatableCount)
    }
    val githubPreReleaseUpdateLine = when {
        !githubOverview.loaded || trackedCount == 0 || cacheHitCount == 0 ->
            stringResource(R.string.github_overview_value_count, 0)
        else -> stringResource(R.string.github_overview_value_count, preReleaseUpdateCount)
    }
    val trackedCountLine = stringResource(R.string.github_overview_value_count, trackedCount)
    val cacheHitCountLine = stringResource(R.string.github_overview_value_count, cacheHitCount)
    val baApLine = if (baOverview.loaded) {
        stringResource(R.string.home_value_fraction, baOverview.apCurrent, baOverview.apLimit)
    } else {
        homeStatusLoading
    }
    val baCafeApLine = if (baOverview.loaded) {
        stringResource(R.string.home_value_fraction, baOverview.cafeStored, baOverview.cafeCap)
    } else {
        homeStatusLoading
    }
    val baActivationLine = if (baOverview.loaded) {
        if (baOverview.activated) homeBaStatusActive else homeBaStatusInactive
    } else {
        homeStatusLoading
    }
    val baApRemainingLine = if (baOverview.loaded) {
        (baOverview.apLimit - baOverview.apCurrent).coerceAtLeast(0).toString()
    } else {
        homeStatusLoading
    }

    var actionBarSelectedIndex by rememberSaveable { mutableIntStateOf(1) }
    var showBottomPageEditor by rememberSaveable { mutableStateOf(false) }
    var visibleOverviewCards by remember { mutableStateOf(loadHomeVisibleOverviewCards()) }

    fun setHomeOverviewCardVisible(card: HomeOverviewCard, visible: Boolean) {
        val updated = visibleOverviewCards.toMutableSet().apply {
            if (visible) add(card) else remove(card)
        }.toSet()
        visibleOverviewCards = updated
        saveHomeVisibleOverviewCards(updated)
    }

    DisposableEffect(Unit) {
        onDispose { onActionBarInteractingChanged(false) }
    }
    val layersIcon = appLucideLayersIcon()
    val aboutIcon = appLucideInfoIcon()
    val settingsIcon = osLucideSettingsIcon()
    val editBottomPagesContentDescription = stringResource(R.string.home_cd_edit_bottom_pages)
    val aboutContentDescription = stringResource(R.string.about_page_title)
    val settingsContentDescription = stringResource(R.string.settings_title)
    val homeActionItems = remember(
        editBottomPagesContentDescription,
        aboutContentDescription,
        settingsContentDescription,
        onOpenAbout,
        onOpenSettings
    ) {
        listOf(
            LiquidActionItem(
                icon = layersIcon,
                contentDescription = editBottomPagesContentDescription,
                onClick = {
                    actionBarSelectedIndex = 0
                    showBottomPageEditor = true
                }
            ),
            LiquidActionItem(
                icon = aboutIcon,
                contentDescription = aboutContentDescription,
                onClick = {
                    actionBarSelectedIndex = 1
                    onOpenAbout()
                }
            ),
            LiquidActionItem(
                icon = settingsIcon,
                contentDescription = settingsContentDescription,
                onClick = {
                    actionBarSelectedIndex = 2
                    onOpenSettings()
                }
            )
        )
    }
    val hiddenOverviewCardCount = (HomeOverviewCard.entries.size - visibleOverviewCards.size).coerceAtLeast(0)
    val heroMotionState = rememberHomePageHeroMotionState(
        lazyListState = lazyListState,
        homeIconHdrEnabled = homeIconHdrEnabled,
        runtime = runtime,
        hiddenOverviewCardCount = hiddenOverviewCardCount
    )
    val overviewCardState = rememberHomePageOverviewCardState(
        homeStatusMcp = homeStatusMcp,
        homeStatusGitHub = homeStatusGitHub,
        homeStatusBa = homeStatusBa,
        homeStatusShizuku = homeStatusShizuku,
        mcpRunning = mcpOverview.running,
        cacheStateColor = cacheStateColor,
        baLoaded = baOverview.loaded,
        baActivated = baOverview.activated,
        shizukuGranted = shizukuGranted,
        runningColor = runningColor,
        stoppedColor = stoppedColor,
        inactiveColor = inactiveColor,
        homeStatStatus = homeStatStatus,
        mcpStatusText = mcpStatusText,
        homeStatRuntime = homeStatRuntime,
        mcpRuntimeText = mcpRuntimeText,
        homeStatClients = homeStatClients,
        mcpConnectedClients = mcpOverview.connectedClients,
        homeStatNetwork = homeStatNetwork,
        networkModeText = networkModeText,
        homeStatPort = homeStatPort,
        mcpPort = mcpOverview.port,
        homeStatPath = homeStatPath,
        mcpEndpointPath = mcpOverview.endpointPath,
        homeStatService = homeStatService,
        mcpServerName = mcpOverview.serverName,
        homeStatToken = homeStatToken,
        mcpTokenStatusText = mcpTokenStatusText,
        homeStatStableUpdates = homeStatStableUpdates,
        githubUpdatableLine = githubUpdatableLine,
        homeStatPreReleaseUpdates = homeStatPreReleaseUpdates,
        githubPreReleaseUpdateLine = githubPreReleaseUpdateLine,
        homeStatTracked = homeStatTracked,
        trackedCountLine = trackedCountLine,
        homeStatCached = homeStatCached,
        cacheHitCountLine = cacheHitCountLine,
        homeStatStrategy = homeStatStrategy,
        githubStrategyText = githubStrategyText,
        homeStatApi = homeStatApi,
        githubApiText = githubApiText,
        homeStatLastUpdate = homeStatLastUpdate,
        githubLastUpdateLine = githubLastUpdateLine,
        baActivationLine = baActivationLine,
        homeStatAp = homeStatAp,
        baApLine = baApLine,
        homeStatCafeAp = homeStatCafeAp,
        baCafeApLine = baCafeApLine,
        homeStatApRemaining = homeStatApRemaining,
        baApRemainingLine = baApRemainingLine
    )

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = "",
                scrollBehavior = topAppBarScrollBehavior,
                color = MiuixTheme.colorScheme.surface.copy(alpha = if (heroMotionState.scrollProgress == 1f) 1f else 0f),
                titleColor = MiuixTheme.colorScheme.onSurface.copy(alpha = heroMotionState.topBarProgress),
                actions = {
                    LiquidActionBar(
                        backdrop = actionBarBackdrop,
                        layeredStyleEnabled = liquidActionBarLayeredStyleEnabled,
                        reduceEffectsDuringPagerScroll = runtime.isPagerScrollInProgress,
                        items = homeActionItems,
                        selectedIndex = actionBarSelectedIndex,
                        onInteractionChanged = onActionBarInteractingChanged
                    )
                }
            )
        }
    ) { innerPadding ->
        HomePageControlSheet(
            show = showBottomPageEditor,
            actionBarBackdrop = actionBarBackdrop,
            visibleBottomPages = visibleBottomPages,
            visibleOverviewCards = visibleOverviewCards,
            homeSheetTitle = stringResource(R.string.home_sheet_bottom_pages_title),
            visiblePagesTitle = stringResource(R.string.home_sheet_visible_pages_title),
            visiblePagesDesc = stringResource(R.string.home_sheet_visible_pages_desc),
            visibleCardsTitle = homeVisibleCardsTitle,
            visibleCardsDesc = homeVisibleCardsDesc,
            homeCardMcp = homeCardMcp,
            homeCardGitHub = homeCardGitHub,
            homeCardBa = homeCardBa,
            onDismissRequest = { showBottomPageEditor = false },
            onBottomPageVisibilityChange = onBottomPageVisibilityChange,
            onOverviewCardVisibilityChange = ::setHomeOverviewCardVisible
        )

        val horizontalSafeInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal).asPaddingValues()
        val listContentPadding = PaddingValues(
            start = horizontalSafeInsets.calculateStartPadding(layoutDirection),
            top = innerPadding.calculateTopPadding() + runtime.contentTopPadding,
            end = horizontalSafeInsets.calculateEndPadding(layoutDirection),
            bottom = innerPadding.calculateBottomPadding() + runtime.contentBottomPadding + 16.dp
        )
        val logoPadding = PaddingValues(
            top = innerPadding.calculateTopPadding() + runtime.contentTopPadding + 24.dp,
            start = horizontalSafeInsets.calculateStartPadding(layoutDirection),
            end = horizontalSafeInsets.calculateEndPadding(layoutDirection),
        )

        BgEffectBackground(
            dynamicBackground = dynamicBackgroundEnabled,
            modifier = Modifier.fillMaxSize(),
            bgModifier = Modifier,
            effectBackground = effectBackgroundEnabled,
            alpha = heroMotionState.bgAlpha,
        ) {
            HomePageHero(
                homeIconHdrEnabled = homeIconHdrEnabled,
                hdrSweepProgress = heroMotionState.hdrSweepProgress,
                homeHeaderSinkOffset = heroMotionState.homeHeaderSinkOffset,
                logoPadding = logoPadding,
                layoutDirection = layoutDirection,
                homeAppName = homeAppName,
                homeTagline = homeTagline,
                appVersionText = appVersionText,
                iconProgress = heroMotionState.iconProgress,
                titleProgress = heroMotionState.titleProgress,
                summaryProgress = heroMotionState.summaryProgress,
                statusPills = overviewCardState.homeHeaderStatusPills,
                onHeroHeightChanged = heroMotionState.onHeroHeightPxChanged,
                onIconBottomChanged = heroMotionState.onIconBottomChanged,
                onTitleBottomChanged = heroMotionState.onTitleBottomChanged,
                onSummaryBottomChanged = heroMotionState.onSummaryBottomChanged
            )

            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
                contentPadding = listContentPadding,
            ) {
                item(key = "logo_spacer") {
                    HomePageHeroSpacer(
                        logoHeightDp = heroMotionState.logoHeightDp,
                        logoPadding = logoPadding,
                        listContentPadding = listContentPadding,
                        homeHeaderSinkOffset = heroMotionState.homeHeaderSinkOffset,
                        onLogoHeightPxChanged = heroMotionState.onLogoHeightPxChanged,
                        onLogoAreaBottomChanged = heroMotionState.onLogoAreaBottomChanged
                    )
                }

                item(key = "home_content") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = listContentPadding.calculateBottomPadding())
                    ) {
                        HomePageOverviewCards(
                            visibleOverviewCards = visibleOverviewCards,
                            homeCardBackdrop = homeCardBackdrop,
                            blurEnabled = blurEnabled,
                            homeNa = homeNa,
                            homeCardMcp = homeCardMcp,
                            mcpStats = overviewCardState.mcpOverviewStats,
                            homeCardGitHub = homeCardGitHub,
                            githubStats = overviewCardState.githubOverviewStats,
                            homeCardBa = homeCardBa,
                            baStats = overviewCardState.baOverviewStats
                        )
                    }
                }
            }
        }
    }
}
