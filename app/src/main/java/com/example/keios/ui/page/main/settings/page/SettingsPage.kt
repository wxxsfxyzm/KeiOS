package com.example.keios.ui.page.main.settings.page

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.dp
import com.example.keios.R
import com.example.keios.core.prefs.AppThemeMode
import com.example.keios.core.prefs.CacheEntrySummary
import com.example.keios.core.prefs.CacheStores
import com.example.keios.core.log.AppLogStore
import com.example.keios.ui.page.main.settings.support.NON_HOME_BACKGROUND_OPACITY_DEFAULT
import com.example.keios.ui.page.main.settings.support.NON_HOME_BACKGROUND_OPACITY_KEY_POINTS
import com.example.keios.ui.page.main.settings.support.NON_HOME_BACKGROUND_OPACITY_MAGNET_THRESHOLD
import com.example.keios.ui.page.main.settings.support.NON_HOME_BACKGROUND_OPACITY_MAX
import com.example.keios.ui.page.main.settings.support.NON_HOME_BACKGROUND_OPACITY_MIN
import com.example.keios.ui.page.main.settings.support.SettingsActionItem
import com.example.keios.ui.page.main.settings.support.SettingsCacheRow
import com.example.keios.ui.page.main.settings.support.SettingsGroupCard
import com.example.keios.ui.page.main.settings.support.SettingsInfoItem
import com.example.keios.ui.page.main.settings.support.SettingsToggleItem
import com.example.keios.ui.page.main.settings.support.createNonHomeBackgroundCropOutputUri
import com.example.keios.ui.page.main.settings.support.deleteManagedNonHomeBackgroundFile
import com.example.keios.ui.page.main.settings.support.formatBytes
import com.example.keios.ui.page.main.settings.support.formatLogTime
import com.example.keios.ui.page.main.settings.support.formatOpacityPercent
import com.example.keios.ui.page.main.settings.support.resolveNonHomeBackgroundAspectRatio
import com.example.keios.ui.page.main.settings.support.resolveNonHomeBackgroundCropSize
import com.example.keios.ui.page.main.settings.section.SettingsAnimationSection
import com.example.keios.ui.page.main.settings.section.SettingsBackgroundSection
import com.example.keios.ui.page.main.settings.section.SettingsCacheSection
import com.example.keios.ui.page.main.settings.section.SettingsComponentEffectsSection
import com.example.keios.ui.page.main.settings.section.SettingsCopySection
import com.example.keios.ui.page.main.settings.section.SettingsLogSection
import com.example.keios.ui.page.main.settings.section.SettingsNotifySection
import com.example.keios.ui.page.main.settings.section.SettingsVisualSection
import com.example.keios.ui.page.main.os.appLucideBackIcon
import com.example.keios.ui.page.main.os.appLucideAlertIcon
import com.example.keios.ui.page.main.os.appLucideConfigIcon
import com.example.keios.ui.page.main.os.appLucideLayersIcon
import com.example.keios.ui.page.main.os.appLucideMediaIcon
import com.example.keios.ui.page.main.os.appLucideNotesIcon
import com.example.keios.ui.page.main.os.appLucidePackageIcon
import com.example.keios.ui.page.main.os.appLucideTimeIcon
import com.example.keios.ui.page.main.os.osLucideCopyIcon
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.example.keios.ui.page.main.widget.core.AppControlRow
import com.example.keios.ui.page.main.widget.core.AppDualActionRow
import com.example.keios.ui.page.main.widget.glass.AppDropdownSelector
import com.example.keios.ui.page.main.widget.core.AppFeatureCard
import com.example.keios.ui.page.main.widget.core.AppInfoRow
import com.example.keios.ui.page.main.widget.chrome.AppPageLazyColumn
import com.example.keios.ui.page.main.widget.chrome.AppPageScaffold
import com.example.keios.ui.page.main.widget.core.CardLayoutRhythm
import com.example.keios.ui.page.main.widget.core.AppTypographyTokens
import com.example.keios.ui.page.main.widget.glass.GlassTextButton
import com.example.keios.ui.page.main.widget.glass.GlassVariant
import com.example.keios.ui.page.main.widget.glass.LiquidGlassSwitch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.min
import kotlin.math.roundToInt
import com.yalantis.ucrop.UCrop
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Slider
import top.yukonga.miuix.kmp.basic.SliderDefaults
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

internal val LocalSettingsLiquidGlassSwitchEnabled = staticCompositionLocalOf { false }

@Composable
fun SettingsPage(
    liquidBottomBarEnabled: Boolean,
    onLiquidBottomBarChanged: (Boolean) -> Unit,
    liquidActionBarLayeredStyleEnabled: Boolean,
    onLiquidActionBarLayeredStyleChanged: (Boolean) -> Unit,
    liquidGlassSwitchEnabled: Boolean,
    onLiquidGlassSwitchChanged: (Boolean) -> Unit,
    transitionAnimationsEnabled: Boolean,
    onTransitionAnimationsChanged: (Boolean) -> Unit,
    cardPressFeedbackEnabled: Boolean,
    onCardPressFeedbackChanged: (Boolean) -> Unit,
    homeIconHdrEnabled: Boolean,
    onHomeIconHdrChanged: (Boolean) -> Unit,
    preloadingEnabled: Boolean,
    onPreloadingEnabledChanged: (Boolean) -> Unit,
    nonHomeBackgroundEnabled: Boolean,
    onNonHomeBackgroundEnabledChanged: (Boolean) -> Unit,
    nonHomeBackgroundUri: String,
    onNonHomeBackgroundUriChanged: (String) -> Unit,
    nonHomeBackgroundOpacity: Float,
    onNonHomeBackgroundOpacityChanged: (Float) -> Unit,
    superIslandNotificationEnabled: Boolean,
    onSuperIslandNotificationChanged: (Boolean) -> Unit,
    superIslandBypassRestrictionEnabled: Boolean,
    onSuperIslandBypassRestrictionChanged: (Boolean) -> Unit,
    logDebugEnabled: Boolean,
    onLogDebugChanged: (Boolean) -> Unit,
    textCopyCapabilityExpanded: Boolean,
    onTextCopyCapabilityExpandedChanged: (Boolean) -> Unit,
    cacheDiagnosticsEnabled: Boolean,
    onCacheDiagnosticsChanged: (Boolean) -> Unit,
    appThemeMode: AppThemeMode,
    onAppThemeModeChanged: (AppThemeMode) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settingsTitle = stringResource(R.string.settings_title)
    val titleColor = MiuixTheme.colorScheme.onBackground
    val subtitleColor = MiuixTheme.colorScheme.onBackgroundVariant.copy(alpha = 0.90f)
    val enabledCardColor = MiuixTheme.colorScheme.surfaceContainer.copy(alpha = 0.46f)
    val disabledCardColor = Color(0x2264748B)
    val surfaceColor = MiuixTheme.colorScheme.surface

    var showThemeModePopup by remember { mutableStateOf(false) }
    var themePopupAnchorBounds by remember { mutableStateOf<IntRect?>(null) }
    var cacheReloadSignal by remember { mutableIntStateOf(0) }
    var clearingCacheId by remember { mutableStateOf<String?>(null) }
    var clearingAllCaches by remember { mutableStateOf(false) }
    var logReloadSignal by remember { mutableIntStateOf(0) }
    var exportingLogZip by remember { mutableStateOf(false) }
    var clearingLogs by remember { mutableStateOf(false) }
    var activationCount by rememberSaveable { mutableIntStateOf(0) }
    DisposableEffect(Unit) {
        activationCount++
        onDispose { }
    }
    val contentBackdrop: LayerBackdrop = key("settings-content-$activationCount") {
        rememberLayerBackdrop {
            drawRect(surfaceColor)
            drawContent()
        }
    }
    val themeModeOptions = listOf(
        AppThemeMode.FOLLOW_SYSTEM to stringResource(R.string.settings_theme_follow_system),
        AppThemeMode.LIGHT to stringResource(R.string.settings_theme_light_mode),
        AppThemeMode.DARK to stringResource(R.string.settings_theme_dark_mode)
    )
    val currentThemeLabel =
        themeModeOptions.firstOrNull { it.first == appThemeMode }?.second
            ?: stringResource(R.string.settings_theme_follow_system)
    val themeSummary = when (appThemeMode) {
        AppThemeMode.FOLLOW_SYSTEM -> stringResource(R.string.settings_theme_summary_follow_system)
        AppThemeMode.LIGHT -> stringResource(R.string.settings_theme_summary_light)
        AppThemeMode.DARK -> stringResource(R.string.settings_theme_summary_dark)
    }
    var cacheEntries by remember(cacheDiagnosticsEnabled) {
        mutableStateOf<List<CacheEntrySummary>?>(if (cacheDiagnosticsEnabled) null else emptyList())
    }
    var cacheEntriesLoading by remember(cacheDiagnosticsEnabled) {
        mutableStateOf(cacheDiagnosticsEnabled)
    }
    LaunchedEffect(cacheDiagnosticsEnabled, cacheReloadSignal) {
        if (!cacheDiagnosticsEnabled) {
            cacheEntries = emptyList()
            cacheEntriesLoading = false
            return@LaunchedEffect
        }
        cacheEntriesLoading = cacheEntries == null
        cacheEntries = withContext(Dispatchers.IO) { CacheStores.list(context) }
        cacheEntriesLoading = false
    }
    val logStats by produceState(
        initialValue = AppLogStore.Stats.Empty,
        logReloadSignal,
        logDebugEnabled
    ) {
        do {
            value = withContext(Dispatchers.IO) {
                AppLogStore.stats(context)
            }
            if (!logDebugEnabled) break
            delay(1200)
        } while (true)
    }
    val visualGroupActive = preloadingEnabled || homeIconHdrEnabled
    val animationGroupActive = transitionAnimationsEnabled
    val componentEffectsGroupActive = liquidActionBarLayeredStyleEnabled ||
        liquidBottomBarEnabled ||
        liquidGlassSwitchEnabled ||
        cardPressFeedbackEnabled
    val backgroundGroupActive = nonHomeBackgroundEnabled || nonHomeBackgroundUri.isNotBlank()
    val notifyGroupActive = superIslandNotificationEnabled || superIslandBypassRestrictionEnabled
    val logGroupActive = logDebugEnabled || logStats.fileCount > 0
    val logLatestText = if (logStats.latestModifiedAtMs <= 0L) {
        stringResource(R.string.settings_log_stat_latest_empty)
    } else {
        formatLogTime(logStats.latestModifiedAtMs)
    }
    val logExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri == null) {
            exportingLogZip = false
            return@rememberLauncherForActivityResult
        }
        scope.launch {
            val result = withContext(Dispatchers.IO) { AppLogStore.exportZipToUri(context, uri) }
            exportingLogZip = false
            if (result.isSuccess) {
                Toast.makeText(
                    context,
                    context.getString(R.string.settings_log_toast_exported),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val reason = result.exceptionOrNull()?.javaClass?.simpleName ?: context.getString(R.string.common_unknown)
                Toast.makeText(
                    context,
                    context.getString(R.string.settings_log_toast_export_failed, reason),
                    Toast.LENGTH_SHORT
                ).show()
            }
            logReloadSignal++
        }
    }
    val nonHomeBackgroundCropLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        val cropError = data?.let { UCrop.getError(it) }
        if (result.resultCode != Activity.RESULT_OK) {
            if (cropError != null) {
                val reason = cropError.javaClass.simpleName.ifBlank {
                    context.getString(R.string.common_unknown)
                }
                Toast.makeText(
                    context,
                    context.getString(R.string.settings_non_home_background_toast_crop_failed, reason),
                    Toast.LENGTH_SHORT
                ).show()
            }
            return@rememberLauncherForActivityResult
        }
        val outputUri = data?.let { UCrop.getOutput(it) } ?: run {
            Toast.makeText(
                context,
                context.getString(
                    R.string.settings_non_home_background_toast_crop_failed,
                    context.getString(R.string.common_unknown)
                ),
                Toast.LENGTH_SHORT
            ).show()
            return@rememberLauncherForActivityResult
        }
        deleteManagedNonHomeBackgroundFile(context, nonHomeBackgroundUri)
        onNonHomeBackgroundUriChanged(outputUri.toString())
        if (!nonHomeBackgroundEnabled) onNonHomeBackgroundEnabledChanged(true)
        Toast.makeText(
            context,
            context.getString(R.string.settings_non_home_background_toast_selected),
            Toast.LENGTH_SHORT
        ).show()
    }
    val nonHomeBackgroundPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        runCatching {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
        val outputUri = createNonHomeBackgroundCropOutputUri(context)
        val (aspectRatioX, aspectRatioY) = resolveNonHomeBackgroundAspectRatio(context)
        val (maxResultWidth, maxResultHeight) = resolveNonHomeBackgroundCropSize(context)
        val cropOptions = UCrop.Options().apply {
            setToolbarTitle(context.getString(R.string.settings_non_home_background_crop_title))
            setCompressionFormat(Bitmap.CompressFormat.JPEG)
            setCompressionQuality(92)
            setFreeStyleCropEnabled(false)
            setHideBottomControls(false)
            setShowCropFrame(true)
            setShowCropGrid(true)
        }
        val cropIntent = runCatching {
            UCrop.of(uri, outputUri)
                .withAspectRatio(aspectRatioX, aspectRatioY)
                .withMaxResultSize(maxResultWidth, maxResultHeight)
                .withOptions(cropOptions)
                .getIntent(context)
        }.getOrElse { error ->
            val reason = error.javaClass.simpleName.ifBlank {
                context.getString(R.string.common_unknown)
            }
            Toast.makeText(
                context,
                context.getString(R.string.settings_non_home_background_toast_crop_failed, reason),
                Toast.LENGTH_SHORT
            ).show()
            return@rememberLauncherForActivityResult
        }
        cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        nonHomeBackgroundCropLauncher.launch(cropIntent)
    }

    val scrollBehavior = MiuixScrollBehavior()
    val listState = rememberLazyListState()

    AppPageScaffold(
        title = settingsTitle,
        modifier = Modifier.fillMaxSize(),
        scrollBehavior = scrollBehavior,
        topBarColor = Color.Transparent,
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = appLucideBackIcon(),
                    contentDescription = null,
                    tint = MiuixTheme.colorScheme.onSurface
                )
            }
        }
    ) { innerPadding ->
        CompositionLocalProvider(
            LocalSettingsLiquidGlassSwitchEnabled provides liquidGlassSwitchEnabled
        ) {
            AppPageLazyColumn(
                innerPadding = innerPadding,
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                sectionSpacing = 12.dp
            ) {
                item {
                    SettingsVisualSection(
                        preloadingEnabled = preloadingEnabled,
                        onPreloadingEnabledChanged = onPreloadingEnabledChanged,
                        homeIconHdrEnabled = homeIconHdrEnabled,
                        onHomeIconHdrChanged = onHomeIconHdrChanged,
                        appThemeMode = appThemeMode,
                        onAppThemeModeChanged = onAppThemeModeChanged,
                        showThemeModePopup = showThemeModePopup,
                        onShowThemeModePopupChange = { showThemeModePopup = it },
                        themePopupAnchorBounds = themePopupAnchorBounds,
                        onThemePopupAnchorBoundsChange = { themePopupAnchorBounds = it },
                        enabledCardColor = enabledCardColor,
                        disabledCardColor = disabledCardColor
                    )
                }
                item {
                    SettingsAnimationSection(
                        transitionAnimationsEnabled = transitionAnimationsEnabled,
                        onTransitionAnimationsChanged = onTransitionAnimationsChanged,
                        enabledCardColor = enabledCardColor,
                        disabledCardColor = disabledCardColor
                    )
                }
                item {
                    SettingsComponentEffectsSection(
                        liquidActionBarLayeredStyleEnabled = liquidActionBarLayeredStyleEnabled,
                        onLiquidActionBarLayeredStyleChanged = onLiquidActionBarLayeredStyleChanged,
                        liquidBottomBarEnabled = liquidBottomBarEnabled,
                        onLiquidBottomBarChanged = onLiquidBottomBarChanged,
                        liquidGlassSwitchEnabled = liquidGlassSwitchEnabled,
                        onLiquidGlassSwitchChanged = onLiquidGlassSwitchChanged,
                        cardPressFeedbackEnabled = cardPressFeedbackEnabled,
                        onCardPressFeedbackChanged = onCardPressFeedbackChanged,
                        enabledCardColor = enabledCardColor,
                        disabledCardColor = disabledCardColor
                    )
                }
                item {
                    SettingsBackgroundSection(
                        nonHomeBackgroundEnabled = nonHomeBackgroundEnabled,
                        onNonHomeBackgroundEnabledChanged = onNonHomeBackgroundEnabledChanged,
                        nonHomeBackgroundUri = nonHomeBackgroundUri,
                        onNonHomeBackgroundUriChanged = onNonHomeBackgroundUriChanged,
                        nonHomeBackgroundOpacity = nonHomeBackgroundOpacity,
                        onNonHomeBackgroundOpacityChanged = onNonHomeBackgroundOpacityChanged,
                        backgroundPickerLauncher = nonHomeBackgroundPickerLauncher,
                        onClearBackground = {
                            deleteManagedNonHomeBackgroundFile(context, nonHomeBackgroundUri)
                            Toast.makeText(
                                context,
                                context.getString(R.string.settings_non_home_background_toast_cleared),
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        enabledCardColor = enabledCardColor,
                        disabledCardColor = disabledCardColor
                    )
                }
                item {
                    SettingsLogSection(
                        logDebugEnabled = logDebugEnabled,
                        onLogDebugChanged = onLogDebugChanged,
                        logStats = logStats,
                        exportingLogZip = exportingLogZip,
                        onExportingLogZipChange = { exportingLogZip = it },
                        clearingLogs = clearingLogs,
                        onClearingLogsChange = { clearingLogs = it },
                        onLogReloadSignal = { logReloadSignal++ },
                        logExportLauncher = logExportLauncher,
                        enabledCardColor = enabledCardColor,
                        disabledCardColor = disabledCardColor
                    )
                }
                item {
                    SettingsNotifySection(
                        superIslandNotificationEnabled = superIslandNotificationEnabled,
                        onSuperIslandNotificationChanged = onSuperIslandNotificationChanged,
                        superIslandBypassRestrictionEnabled = superIslandBypassRestrictionEnabled,
                        onSuperIslandBypassRestrictionChanged = onSuperIslandBypassRestrictionChanged,
                        enabledCardColor = enabledCardColor,
                        disabledCardColor = disabledCardColor
                    )
                }
                item {
                    SettingsCopySection(
                        textCopyCapabilityExpanded = textCopyCapabilityExpanded,
                        onTextCopyCapabilityExpandedChanged = onTextCopyCapabilityExpandedChanged,
                        enabledCardColor = enabledCardColor,
                        disabledCardColor = disabledCardColor
                    )
                }
                item {
                    SettingsCacheSection(
                        cacheDiagnosticsEnabled = cacheDiagnosticsEnabled,
                        onCacheDiagnosticsChanged = onCacheDiagnosticsChanged,
                        cacheEntries = cacheEntries,
                        cacheEntriesLoading = cacheEntriesLoading,
                        clearingAllCaches = clearingAllCaches,
                        onClearingAllCachesChange = { clearingAllCaches = it },
                        clearingCacheId = clearingCacheId,
                        onClearingCacheIdChange = { clearingCacheId = it },
                        onCacheReload = { cacheReloadSignal++ },
                        enabledCardColor = enabledCardColor,
                        disabledCardColor = disabledCardColor
                    )
                }
            }
        }
    }
}
