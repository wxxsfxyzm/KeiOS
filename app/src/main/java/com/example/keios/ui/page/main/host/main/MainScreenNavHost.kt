package com.example.keios.ui.page.main.host.main

import android.content.pm.PackageInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.keios.core.prefs.AppThemeMode
import com.example.keios.mcp.server.McpServerManager
import com.example.keios.ui.navigation.KeiosRoute
import com.example.keios.ui.navigation.Navigator
import com.example.keios.ui.page.main.mcp.skill.page.McpSkillPage
import com.example.keios.ui.page.main.about.page.AboutPage
import com.example.keios.ui.page.main.host.pager.MainPagerLayout
import com.example.keios.ui.page.main.settings.page.SettingsPage
import com.example.keios.ui.page.main.student.catalog.page.BaGuideCatalogPage
import com.example.keios.ui.page.main.student.page.BaStudentGuidePage
import com.example.keios.ui.page.main.widget.motion.LocalTransitionAnimationsEnabled

@Composable
internal fun MainScreenNavHost(
    backStack: MutableList<NavKey>,
    navigator: Navigator,
    pagerCoordinator: MainScreenPagerCoordinator,
    prefsState: MainScreenUiPrefsState,
    appLabel: String,
    packageInfo: PackageInfo?,
    onCheckOrRequestShizuku: () -> Unit,
    notificationPermissionGranted: Boolean,
    mcpServerManager: McpServerManager,
    appThemeMode: AppThemeMode,
    onAppThemeModeChanged: (AppThemeMode) -> Unit
) {
    val entryProvider = entryProvider<NavKey> {
        entry<KeiosRoute.Main> {
            MainPagerLayout(
                navigator = navigator,
                settingsReturnToken = pagerCoordinator.settingsReturnToken,
                liquidBottomBarEnabled = pagerCoordinator.liquidBottomBarEnabled,
                liquidActionBarLayeredStyleEnabled = pagerCoordinator.liquidActionBarLayeredStyleEnabled,
                cardPressFeedbackEnabled = pagerCoordinator.cardPressFeedbackEnabled,
                homeIconHdrEnabled = pagerCoordinator.homeIconHdrEnabled,
                preloadingEnabled = pagerCoordinator.preloadingEnabled,
                nonHomeBackgroundEnabled = pagerCoordinator.nonHomeBackgroundEnabled,
                nonHomeBackgroundUri = pagerCoordinator.nonHomeBackgroundUri,
                nonHomeBackgroundOpacity = pagerCoordinator.nonHomeBackgroundOpacity,
                visibleBottomPageNames = pagerCoordinator.visibleBottomPageNames,
                onVisibleBottomPageNamesChange = pagerCoordinator.onVisibleBottomPageNamesChange,
                shizukuStatus = pagerCoordinator.shizukuStatus,
                shizukuApiUtils = pagerCoordinator.shizukuApiUtils,
                mcpServerManager = pagerCoordinator.mcpServerManager,
                onOpenGuideDetail = pagerCoordinator.onOpenGuideDetail,
                requestedBottomPage = pagerCoordinator.requestedBottomPage,
                requestedBottomPageToken = pagerCoordinator.requestedBottomPageToken,
                requestedGitHubRefreshToken = pagerCoordinator.requestedGitHubRefreshToken,
                onRequestedBottomPageConsumed = pagerCoordinator.onRequestedBottomPageConsumed
            )
        }
        entry<KeiosRoute.Settings> {
            SettingsPage(
                liquidBottomBarEnabled = prefsState.liquidBottomBarEnabled,
                onLiquidBottomBarChanged = prefsState::updateLiquidBottomBarEnabled,
                liquidActionBarLayeredStyleEnabled = prefsState.liquidActionBarLayeredStyleEnabled,
                onLiquidActionBarLayeredStyleChanged = prefsState::updateLiquidActionBarLayeredStyleEnabled,
                liquidGlassSwitchEnabled = prefsState.liquidGlassSwitchEnabled,
                onLiquidGlassSwitchChanged = prefsState::updateLiquidGlassSwitchEnabled,
                transitionAnimationsEnabled = prefsState.transitionAnimationsEnabled,
                onTransitionAnimationsChanged = prefsState::updateTransitionAnimationsEnabled,
                cardPressFeedbackEnabled = prefsState.cardPressFeedbackEnabled,
                onCardPressFeedbackChanged = prefsState::updateCardPressFeedbackEnabled,
                homeIconHdrEnabled = prefsState.homeIconHdrEnabled,
                onHomeIconHdrChanged = prefsState::updateHomeIconHdrEnabled,
                preloadingEnabled = prefsState.preloadingEnabled,
                onPreloadingEnabledChanged = prefsState::updatePreloadingEnabled,
                nonHomeBackgroundEnabled = prefsState.nonHomeBackgroundEnabled,
                onNonHomeBackgroundEnabledChanged = prefsState::updateNonHomeBackgroundEnabled,
                nonHomeBackgroundUri = prefsState.nonHomeBackgroundUri,
                onNonHomeBackgroundUriChanged = prefsState::updateNonHomeBackgroundUri,
                nonHomeBackgroundOpacity = prefsState.nonHomeBackgroundOpacity,
                onNonHomeBackgroundOpacityChanged = prefsState::updateNonHomeBackgroundOpacity,
                superIslandNotificationEnabled = prefsState.superIslandNotificationEnabled,
                onSuperIslandNotificationChanged = prefsState::updateSuperIslandNotificationEnabled,
                superIslandBypassRestrictionEnabled = prefsState.superIslandBypassRestrictionEnabled,
                onSuperIslandBypassRestrictionChanged = prefsState::updateSuperIslandBypassRestrictionEnabled,
                logDebugEnabled = prefsState.logDebugEnabled,
                onLogDebugChanged = prefsState::updateLogDebugEnabled,
                textCopyCapabilityExpanded = prefsState.textCopyCapabilityExpanded,
                onTextCopyCapabilityExpandedChanged = prefsState::updateTextCopyCapabilityExpanded,
                cacheDiagnosticsEnabled = prefsState.cacheDiagnosticsEnabled,
                onCacheDiagnosticsChanged = prefsState::updateCacheDiagnosticsEnabled,
                appThemeMode = appThemeMode,
                onAppThemeModeChanged = onAppThemeModeChanged,
                onBack = { navigator.pop() }
            )
        }
        entry<KeiosRoute.McpSkill> {
            McpSkillPage(
                mcpServerManager = mcpServerManager,
                onBack = { navigator.pop() }
            )
        }
        entry<KeiosRoute.About> {
            AboutPage(
                appLabel = appLabel,
                packageInfo = packageInfo,
                notificationPermissionGranted = notificationPermissionGranted,
                shizukuStatus = pagerCoordinator.shizukuStatus,
                shizukuApiUtils = pagerCoordinator.shizukuApiUtils,
                onCheckShizuku = onCheckOrRequestShizuku,
                onBack = { navigator.pop() }
            )
        }
        entry<KeiosRoute.BaStudentGuide> {
            BaStudentGuidePage(
                liquidActionBarLayeredStyleEnabled = prefsState.liquidActionBarLayeredStyleEnabled,
                preloadingEnabled = prefsState.preloadingEnabled,
                onBack = { navigator.pop() }
            )
        }
        entry<KeiosRoute.BaGuideCatalog> {
            BaGuideCatalogPage(
                liquidActionBarLayeredStyleEnabled = prefsState.liquidActionBarLayeredStyleEnabled,
                preloadingEnabled = prefsState.preloadingEnabled,
                onBack = { navigator.pop() },
                onOpenGuide = pagerCoordinator.onOpenGuideDetail
            )
        }
    }
    val entries = rememberDecoratedNavEntries(
        backStack = backStack,
        entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
        entryProvider = entryProvider,
    )
    CompositionLocalProvider(LocalTransitionAnimationsEnabled provides prefsState.transitionAnimationsEnabled) {
        NavDisplay(
            entries = entries,
            onBack = { navigator.pop() },
            modifier = Modifier.fillMaxSize()
        )
    }
}
