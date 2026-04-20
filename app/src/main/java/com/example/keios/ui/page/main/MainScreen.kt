package com.example.keios.ui.page.main

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.pm.PackageInfo
import android.os.Build
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.produceState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.keios.R
import com.example.keios.mcp.notification.McpNotificationHelper
import com.example.keios.mcp.server.McpServerManager
import com.example.keios.ui.navigation.KeiosRoute
import com.example.keios.ui.navigation.Navigator
import com.example.keios.ui.page.main.about.AboutPage
import com.example.keios.ui.page.main.model.BottomPage
import com.example.keios.ui.page.main.student.BaStudentGuideStore
import com.example.keios.ui.page.main.student.fetch.extractGuideContentIdFromUrl
import com.example.keios.ui.page.main.student.fetch.normalizeGuideUrl
import com.example.keios.ui.perf.ReportPagerPerformanceState
import com.example.keios.ui.page.main.widget.motion.AppMotionTokens
import com.example.keios.ui.page.main.widget.glass.UiPerformanceBudget
import com.example.keios.ui.page.main.widget.motion.LocalTransitionAnimationsEnabled
import com.example.keios.ui.page.main.widget.motion.resolvedMotionDuration
import com.example.keios.core.log.AppLogger
import com.example.keios.core.system.ShizukuApiUtils
import com.example.keios.core.prefs.AppThemeMode
import com.example.keios.core.prefs.UiPrefs
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.size.Precision
import coil3.size.Scale
import com.example.keios.ui.page.main.mcp.McpPage
import com.example.keios.ui.page.main.os.OsPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun MainScreen(
    appLabel: String,
    packageInfo: PackageInfo?,
    shizukuStatus: String,
    onCheckOrRequestShizuku: () -> Unit,
    notificationPermissionGranted: Boolean,
    onRequestNotificationPermission: () -> Unit,
    shizukuApiUtils: ShizukuApiUtils,
    mcpServerManager: McpServerManager,
    appThemeMode: AppThemeMode,
    onAppThemeModeChanged: (AppThemeMode) -> Unit,
    requestedBottomPage: String?,
    requestedBottomPageToken: Int,
    requestedGitHubRefreshToken: Int,
    onRequestedBottomPageConsumed: () -> Unit
) {
    val backStack = remember { mutableStateListOf<NavKey>().apply { add(KeiosRoute.Main) } }
    val navigator = remember { Navigator(backStack) }
    val context = LocalContext.current
    val currentAppLabel by rememberUpdatedState(appLabel)
    val currentPackageInfo by rememberUpdatedState(packageInfo)
    val currentShizukuStatus by rememberUpdatedState(shizukuStatus)
    val currentNotificationPermissionGranted by rememberUpdatedState(notificationPermissionGranted)
    val currentAppThemeMode by rememberUpdatedState(appThemeMode)
    val currentOnCheckOrRequestShizuku by rememberUpdatedState(onCheckOrRequestShizuku)
    val currentOnRequestNotificationPermission by rememberUpdatedState(onRequestNotificationPermission)
    val currentOnAppThemeModeChanged by rememberUpdatedState(onAppThemeModeChanged)
    var mainResumeFromSettingsToken by rememberSaveable { mutableIntStateOf(0) }
    var previousTopRoute by remember { mutableStateOf<NavKey?>(null) }

    LaunchedEffect(backStack.lastOrNull()) {
        val currentTopRoute = backStack.lastOrNull()
        if (previousTopRoute == KeiosRoute.Settings && currentTopRoute == KeiosRoute.Main) {
            mainResumeFromSettingsToken++
        }
        previousTopRoute = currentTopRoute
    }

    LaunchedEffect(requestedBottomPageToken, requestedBottomPage) {
        if (requestedBottomPage.isNullOrBlank()) return@LaunchedEffect
        navigator.popUntil { it == KeiosRoute.Main }
    }

    val uiPrefsSnapshot by produceState(
        initialValue = UiPrefs.defaultSnapshot(currentAppThemeMode)
    ) {
        value = withContext(Dispatchers.IO) { UiPrefs.loadSnapshot() }
    }
    var liquidBottomBarEnabled by remember(uiPrefsSnapshot) { mutableStateOf(uiPrefsSnapshot.liquidBottomBarEnabled) }
    var liquidActionBarLayeredStyleEnabled by remember(uiPrefsSnapshot) {
        mutableStateOf(uiPrefsSnapshot.liquidActionBarLayeredStyleEnabled)
    }
    var liquidGlassSwitchEnabled by remember(uiPrefsSnapshot) {
        mutableStateOf(uiPrefsSnapshot.liquidGlassSwitchEnabled)
    }
    var transitionAnimationsEnabled by remember(uiPrefsSnapshot) {
        mutableStateOf(uiPrefsSnapshot.transitionAnimationsEnabled)
    }
    var cardPressFeedbackEnabled by remember(uiPrefsSnapshot) { mutableStateOf(uiPrefsSnapshot.cardPressFeedbackEnabled) }
    var homeIconHdrEnabled by remember(uiPrefsSnapshot) { mutableStateOf(uiPrefsSnapshot.homeIconHdrEnabled) }
    var preloadingEnabled by remember(uiPrefsSnapshot) { mutableStateOf(uiPrefsSnapshot.preloadingEnabled) }
    var nonHomeBackgroundEnabled by remember(uiPrefsSnapshot) { mutableStateOf(uiPrefsSnapshot.nonHomeBackgroundEnabled) }
    var nonHomeBackgroundUri by remember(uiPrefsSnapshot) { mutableStateOf(uiPrefsSnapshot.nonHomeBackgroundUri) }
    var nonHomeBackgroundOpacity by remember(uiPrefsSnapshot) { mutableStateOf(uiPrefsSnapshot.nonHomeBackgroundOpacity) }
    var superIslandNotificationEnabled by remember(uiPrefsSnapshot) { mutableStateOf(uiPrefsSnapshot.superIslandNotificationEnabled) }
    var superIslandBypassRestrictionEnabled by remember(uiPrefsSnapshot) {
        mutableStateOf(uiPrefsSnapshot.superIslandBypassRestrictionEnabled)
    }
    var logDebugEnabled by remember(uiPrefsSnapshot) { mutableStateOf(uiPrefsSnapshot.logDebugEnabled) }
    var textCopyCapabilityExpanded by remember(uiPrefsSnapshot) {
        mutableStateOf(uiPrefsSnapshot.textCopyCapabilityExpanded)
    }
    var cacheDiagnosticsEnabled by remember(uiPrefsSnapshot) { mutableStateOf(uiPrefsSnapshot.cacheDiagnosticsEnabled) }
    var visibleBottomPageNames by remember(uiPrefsSnapshot) { mutableStateOf(uiPrefsSnapshot.visibleBottomPageNames) }
    val view = LocalView.current
    val poolGuideMissingText = stringResource(R.string.main_toast_pool_guide_missing)

    val openGuideDetail: (String) -> Unit = { rawUrl ->
        val normalized = normalizeGuideUrl(rawUrl)
        val contentId = if (normalized.isBlank()) null else extractGuideContentIdFromUrl(normalized)
        if (contentId == null || contentId <= 0L) {
            Toast.makeText(context, poolGuideMissingText, Toast.LENGTH_SHORT).show()
        } else {
            val canonicalGuideUrl = "https://www.gamekee.com/ba/tj/$contentId.html"
            BaStudentGuideStore.setCurrentUrl(canonicalGuideUrl)
            navigator.push(KeiosRoute.BaStudentGuide(nonce = System.nanoTime()))
        }
    }
    if (!view.isInEditMode) {
        SideEffect {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return@SideEffect
            val activity = view.context as? Activity ?: return@SideEffect
            runCatching {
                activity.window.colorMode = if (homeIconHdrEnabled) {
                    ActivityInfo.COLOR_MODE_HDR
                } else {
                    ActivityInfo.COLOR_MODE_DEFAULT
                }
            }
        }
    }

    val entryProvider = entryProvider<NavKey> {
            entry<KeiosRoute.Main> {
                // Removed the dangerous isTopRoute check. The route should manage its own state naturally.
                MainPagerLayout(
                    navigator = navigator,
                    settingsReturnToken = mainResumeFromSettingsToken,
                    liquidBottomBarEnabled = liquidBottomBarEnabled,
                    liquidActionBarLayeredStyleEnabled = liquidActionBarLayeredStyleEnabled,
                    cardPressFeedbackEnabled = cardPressFeedbackEnabled,
                    homeIconHdrEnabled = homeIconHdrEnabled,
                    preloadingEnabled = preloadingEnabled,
                    nonHomeBackgroundEnabled = nonHomeBackgroundEnabled,
                    nonHomeBackgroundUri = nonHomeBackgroundUri,
                    nonHomeBackgroundOpacity = nonHomeBackgroundOpacity,
                    visibleBottomPageNames = visibleBottomPageNames,
                    onVisibleBottomPageNamesChange = { names ->
                        visibleBottomPageNames = names
                        UiPrefs.saveVisibleBottomPageNames(names)
                    },
                    shizukuStatus = currentShizukuStatus,
                    shizukuApiUtils = shizukuApiUtils,
                    mcpServerManager = mcpServerManager,
                    onOpenGuideDetail = openGuideDetail,
                    requestedBottomPage = requestedBottomPage,
                    requestedBottomPageToken = requestedBottomPageToken,
                    requestedGitHubRefreshToken = requestedGitHubRefreshToken,
                    onRequestedBottomPageConsumed = onRequestedBottomPageConsumed
                )
            }
            entry<KeiosRoute.Settings> {
                SettingsPage(
                    liquidBottomBarEnabled = liquidBottomBarEnabled,
                    onLiquidBottomBarChanged = {
                        liquidBottomBarEnabled = it
                        UiPrefs.setLiquidBottomBarEnabled(it)
                    },
                    liquidActionBarLayeredStyleEnabled = liquidActionBarLayeredStyleEnabled,
                    onLiquidActionBarLayeredStyleChanged = {
                        liquidActionBarLayeredStyleEnabled = it
                        UiPrefs.setLiquidActionBarLayeredStyleEnabled(it)
                    },
                    liquidGlassSwitchEnabled = liquidGlassSwitchEnabled,
                    onLiquidGlassSwitchChanged = {
                        liquidGlassSwitchEnabled = it
                        UiPrefs.setLiquidGlassSwitchEnabled(it)
                    },
                    transitionAnimationsEnabled = transitionAnimationsEnabled,
                    onTransitionAnimationsChanged = {
                        transitionAnimationsEnabled = it
                        UiPrefs.setTransitionAnimationsEnabled(it)
                    },
                    cardPressFeedbackEnabled = cardPressFeedbackEnabled,
                    onCardPressFeedbackChanged = {
                        cardPressFeedbackEnabled = it
                        UiPrefs.setCardPressFeedbackEnabled(it)
                    },
                    homeIconHdrEnabled = homeIconHdrEnabled,
                    onHomeIconHdrChanged = {
                        homeIconHdrEnabled = it
                        UiPrefs.setHomeIconHdrEnabled(it)
                    },
                    preloadingEnabled = preloadingEnabled,
                    onPreloadingEnabledChanged = {
                        preloadingEnabled = it
                        UiPrefs.setPreloadingEnabled(it)
                    },
                    nonHomeBackgroundEnabled = nonHomeBackgroundEnabled,
                    onNonHomeBackgroundEnabledChanged = {
                        nonHomeBackgroundEnabled = it
                        UiPrefs.setNonHomeBackgroundEnabled(it)
                    },
                    nonHomeBackgroundUri = nonHomeBackgroundUri,
                    onNonHomeBackgroundUriChanged = {
                        nonHomeBackgroundUri = it
                        UiPrefs.setNonHomeBackgroundUri(it)
                    },
                    nonHomeBackgroundOpacity = nonHomeBackgroundOpacity,
                    onNonHomeBackgroundOpacityChanged = {
                        nonHomeBackgroundOpacity = it
                        UiPrefs.setNonHomeBackgroundOpacity(it)
                    },
                    superIslandNotificationEnabled = superIslandNotificationEnabled,
                    onSuperIslandNotificationChanged = {
                        superIslandNotificationEnabled = it
                        UiPrefs.setSuperIslandNotificationEnabled(it)
                        mcpServerManager.refreshNotificationNow()
                        McpNotificationHelper.refreshCurrentNotificationStyle(view.context.applicationContext)
                    },
                    superIslandBypassRestrictionEnabled = superIslandBypassRestrictionEnabled,
                    onSuperIslandBypassRestrictionChanged = {
                        superIslandBypassRestrictionEnabled = it
                        UiPrefs.setSuperIslandBypassRestrictionEnabled(it)
                        mcpServerManager.refreshNotificationNow()
                        McpNotificationHelper.refreshCurrentNotificationStyle(view.context.applicationContext)
                    },
                    logDebugEnabled = logDebugEnabled,
                    onLogDebugChanged = {
                        logDebugEnabled = it
                        UiPrefs.setLogDebugEnabled(it)
                        AppLogger.setDebugEnabled(it)
                    },
                    textCopyCapabilityExpanded = textCopyCapabilityExpanded,
                    onTextCopyCapabilityExpandedChanged = {
                        textCopyCapabilityExpanded = it
                        UiPrefs.setTextCopyCapabilityExpanded(it)
                    },
                    cacheDiagnosticsEnabled = cacheDiagnosticsEnabled,
                    onCacheDiagnosticsChanged = {
                        cacheDiagnosticsEnabled = it
                        UiPrefs.setCacheDiagnosticsEnabled(it)
                    },
                    appThemeMode = currentAppThemeMode,
                    onAppThemeModeChanged = currentOnAppThemeModeChanged,
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
                    appLabel = currentAppLabel,
                    packageInfo = currentPackageInfo,
                    notificationPermissionGranted = currentNotificationPermissionGranted,
                    shizukuStatus = currentShizukuStatus,
                    shizukuApiUtils = shizukuApiUtils,
                    onCheckShizuku = currentOnCheckOrRequestShizuku,
                    onBack = { navigator.pop() }
                )
            }
            entry<KeiosRoute.BaStudentGuide> {
                BaStudentGuidePage(
                    liquidActionBarLayeredStyleEnabled = liquidActionBarLayeredStyleEnabled,
                    preloadingEnabled = preloadingEnabled,
                    onBack = { navigator.pop() }
                )
            }
            entry<KeiosRoute.BaGuideCatalog> {
                BaGuideCatalogPage(
                    liquidActionBarLayeredStyleEnabled = liquidActionBarLayeredStyleEnabled,
                    preloadingEnabled = preloadingEnabled,
                    onBack = { navigator.pop() },
                    onOpenGuide = { sourceUrl ->
                        openGuideDetail(sourceUrl)
                    }
                )
            }
        }
    val entries = rememberDecoratedNavEntries(
        backStack = backStack,
        entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
        entryProvider = entryProvider,
    )

    CompositionLocalProvider(LocalTransitionAnimationsEnabled provides transitionAnimationsEnabled) {
        NavDisplay(
            entries = entries,
            onBack = { navigator.pop() },
            modifier = Modifier.fillMaxSize()
        )
    }
}
