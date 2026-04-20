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
import com.example.keios.core.system.ShizukuApiUtils
import com.example.keios.mcp.server.McpServerManager
import com.example.keios.ui.navigation.KeiosRoute
import com.example.keios.ui.navigation.Navigator
import com.example.keios.ui.page.main.BaGuideCatalogPage
import com.example.keios.ui.page.main.BaStudentGuidePage
import com.example.keios.ui.page.main.McpSkillPage
import com.example.keios.ui.page.main.SettingsPage
import com.example.keios.ui.page.main.about.AboutPage
import com.example.keios.ui.page.main.host.pager.MainPagerLayout
import com.example.keios.ui.page.main.widget.motion.LocalTransitionAnimationsEnabled

@Composable
internal fun MainScreenNavHost(
    backStack: MutableList<NavKey>,
    navigator: Navigator,
    settingsReturnToken: Int,
    prefsState: MainScreenUiPrefsState,
    appLabel: String,
    packageInfo: PackageInfo?,
    shizukuStatus: String,
    onCheckOrRequestShizuku: () -> Unit,
    notificationPermissionGranted: Boolean,
    shizukuApiUtils: ShizukuApiUtils,
    mcpServerManager: McpServerManager,
    appThemeMode: AppThemeMode,
    onAppThemeModeChanged: (AppThemeMode) -> Unit,
    requestedBottomPage: String?,
    requestedBottomPageToken: Int,
    requestedGitHubRefreshToken: Int,
    onRequestedBottomPageConsumed: () -> Unit,
    onOpenGuideDetail: (String) -> Unit
) {
    val entryProvider = entryProvider<NavKey> {
        entry<KeiosRoute.Main> {
            MainPagerLayout(
                navigator = navigator,
                settingsReturnToken = settingsReturnToken,
                liquidBottomBarEnabled = prefsState.liquidBottomBarEnabled,
                liquidActionBarLayeredStyleEnabled = prefsState.liquidActionBarLayeredStyleEnabled,
                cardPressFeedbackEnabled = prefsState.cardPressFeedbackEnabled,
                homeIconHdrEnabled = prefsState.homeIconHdrEnabled,
                preloadingEnabled = prefsState.preloadingEnabled,
                nonHomeBackgroundEnabled = prefsState.nonHomeBackgroundEnabled,
                nonHomeBackgroundUri = prefsState.nonHomeBackgroundUri,
                nonHomeBackgroundOpacity = prefsState.nonHomeBackgroundOpacity,
                visibleBottomPageNames = prefsState.visibleBottomPageNames,
                onVisibleBottomPageNamesChange = prefsState::updateVisibleBottomPageNames,
                shizukuStatus = shizukuStatus,
                shizukuApiUtils = shizukuApiUtils,
                mcpServerManager = mcpServerManager,
                onOpenGuideDetail = onOpenGuideDetail,
                requestedBottomPage = requestedBottomPage,
                requestedBottomPageToken = requestedBottomPageToken,
                requestedGitHubRefreshToken = requestedGitHubRefreshToken,
                onRequestedBottomPageConsumed = onRequestedBottomPageConsumed
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
                shizukuStatus = shizukuStatus,
                shizukuApiUtils = shizukuApiUtils,
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
                onOpenGuide = onOpenGuideDetail
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
