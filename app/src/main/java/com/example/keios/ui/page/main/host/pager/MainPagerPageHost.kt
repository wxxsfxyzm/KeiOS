package com.example.keios.ui.page.main.host.pager

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.example.keios.mcp.server.McpServerManager
import com.example.keios.core.system.ShizukuApiUtils
import com.example.keios.ui.page.main.ba.BAPage
import com.example.keios.ui.page.main.github.page.GitHubPage
import com.example.keios.ui.page.main.home.model.HomeBaOverview
import com.example.keios.ui.page.main.home.model.HomeGitHubOverview
import com.example.keios.ui.page.main.home.model.HomeMcpOverview
import com.example.keios.ui.page.main.home.HomePage
import com.example.keios.ui.page.main.model.BottomPage
import com.example.keios.ui.page.main.mcp.McpPage
import com.example.keios.ui.page.main.os.OsPage

@Composable
internal fun MainPagerPageHost(
    pageType: BottomPage,
    pageIndex: Int,
    pagerRuntime: MainPagerRuntimeSnapshot,
    visibleBottomPages: Set<BottomPage>,
    shizukuStatus: String,
    shizukuApiUtils: ShizukuApiUtils,
    cardPressFeedbackEnabled: Boolean,
    liquidActionBarLayeredStyleEnabled: Boolean,
    homeIconHdrEnabled: Boolean,
    preloadingEnabled: Boolean,
    mcpServerManager: McpServerManager,
    homeMcpOverview: HomeMcpOverview,
    homeGitHubOverview: HomeGitHubOverview,
    homeBaOverview: HomeBaOverview,
    homeTopInset: Dp,
    homeBottomInset: Dp,
    bottomOverlayPadding: Dp,
    requestedGitHubRefreshToken: Int,
    osScrollToTopSignal: Int,
    baScrollToTopSignal: Int,
    mcpScrollToTopSignal: Int,
    githubScrollToTopSignal: Int,
    onBottomPageVisibilityChange: (BottomPage, Boolean) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenPoolGuideDetail: (String) -> Unit,
    onOpenBaGuideCatalog: () -> Unit,
    onOpenMcpSkill: () -> Unit,
    onActionBarInteractingChanged: (Boolean) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (pageType) {
            BottomPage.Home -> {
                HomePage(
                    shizukuStatus = shizukuStatus,
                    mcpOverview = homeMcpOverview,
                    homeGitHubOverview = homeGitHubOverview,
                    homeBaOverview = homeBaOverview,
                    homeIconHdrEnabled = homeIconHdrEnabled,
                    runtime = pagerRuntime.pageRuntime(
                        pageIndex = pageIndex,
                        contentTopPadding = homeTopInset,
                        contentBottomPadding = homeBottomInset
                    ),
                    liquidActionBarLayeredStyleEnabled = liquidActionBarLayeredStyleEnabled,
                    visibleBottomPages = visibleBottomPages,
                    onBottomPageVisibilityChange = onBottomPageVisibilityChange,
                    onOpenSettings = onOpenSettings,
                    onOpenAbout = onOpenAbout,
                    onActionBarInteractingChanged = onActionBarInteractingChanged
                )
            }

            BottomPage.Os -> {
                OsPage(
                    runtime = pagerRuntime.pageRuntime(
                        pageIndex = pageIndex,
                        scrollToTopSignal = osScrollToTopSignal,
                        contentBottomPadding = bottomOverlayPadding
                    ),
                    shizukuStatus = shizukuStatus,
                    shizukuApiUtils = shizukuApiUtils,
                    cardPressFeedbackEnabled = cardPressFeedbackEnabled,
                    liquidActionBarLayeredStyleEnabled = liquidActionBarLayeredStyleEnabled,
                    onActionBarInteractingChanged = onActionBarInteractingChanged
                )
            }

            BottomPage.Ba -> {
                BAPage(
                    runtime = pagerRuntime.pageRuntime(
                        pageIndex = pageIndex,
                        scrollToTopSignal = baScrollToTopSignal,
                        contentBottomPadding = bottomOverlayPadding
                    ),
                    preloadingEnabled = preloadingEnabled,
                    cardPressFeedbackEnabled = cardPressFeedbackEnabled,
                    liquidActionBarLayeredStyleEnabled = liquidActionBarLayeredStyleEnabled,
                    onOpenPoolStudentGuide = onOpenPoolGuideDetail,
                    onOpenGuideCatalog = onOpenBaGuideCatalog,
                    onActionBarInteractingChanged = onActionBarInteractingChanged
                )
            }

            BottomPage.Mcp -> {
                McpPage(
                    mcpServerManager = mcpServerManager,
                    runtime = pagerRuntime.pageRuntime(
                        pageIndex = pageIndex,
                        scrollToTopSignal = mcpScrollToTopSignal,
                        contentBottomPadding = bottomOverlayPadding
                    ),
                    cardPressFeedbackEnabled = cardPressFeedbackEnabled,
                    liquidActionBarLayeredStyleEnabled = liquidActionBarLayeredStyleEnabled,
                    onOpenSkill = onOpenMcpSkill,
                    onActionBarInteractingChanged = onActionBarInteractingChanged
                )
            }

            BottomPage.GitHub -> {
                GitHubPage(
                    runtime = pagerRuntime.pageRuntime(
                        pageIndex = pageIndex,
                        scrollToTopSignal = githubScrollToTopSignal,
                        contentBottomPadding = bottomOverlayPadding
                    ),
                    externalRefreshTriggerToken = requestedGitHubRefreshToken,
                    cardPressFeedbackEnabled = cardPressFeedbackEnabled,
                    liquidActionBarLayeredStyleEnabled = liquidActionBarLayeredStyleEnabled,
                    onActionBarInteractingChanged = onActionBarInteractingChanged
                )
            }
        }
    }
}
