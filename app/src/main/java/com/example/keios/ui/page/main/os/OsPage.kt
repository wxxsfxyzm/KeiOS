package com.example.keios.ui.page.main

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.FeatureInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.core.tween
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.keios.R
import com.example.keios.ui.page.main.widget.AppInfoRow
import com.example.keios.ui.page.main.widget.AppOverviewCard
import com.example.keios.ui.page.main.widget.AppChromeTokens
import com.example.keios.ui.page.main.widget.AppPageLazyColumn
import com.example.keios.ui.page.main.widget.AppPageScaffold
import com.example.keios.ui.page.main.widget.AppTopBarSearchField
import com.example.keios.ui.page.main.widget.CardLayoutRhythm
import com.example.keios.ui.page.main.widget.AppTypographyTokens
import com.example.keios.ui.page.main.widget.LiquidActionBar
import com.example.keios.ui.page.main.widget.LiquidActionItem
import com.example.keios.ui.page.main.widget.GlassIconButton
import com.example.keios.ui.page.main.widget.GlassSearchField
import com.example.keios.ui.page.main.widget.GlassVariant
import com.example.keios.ui.page.main.widget.MiuixAccordionCard
import com.example.keios.ui.page.main.widget.SheetContentColumn
import com.example.keios.ui.page.main.widget.SheetChoiceCard
import com.example.keios.ui.page.main.widget.SheetControlRow
import com.example.keios.ui.page.main.widget.SheetFieldBlock
import com.example.keios.ui.page.main.widget.SheetDescriptionText
import com.example.keios.ui.page.main.widget.SheetSectionCard
import com.example.keios.ui.page.main.widget.SheetSectionTitle
import com.example.keios.ui.page.main.widget.SnapshotWindowBottomSheet
import com.example.keios.ui.page.main.widget.StatusPill
import com.example.keios.ui.page.main.widget.StatusLabelText
import com.example.keios.ui.page.main.widget.copyModeAwareRow
import com.example.keios.core.system.ShizukuApiUtils
import com.example.keios.core.system.getAllJavaPropString
import com.example.keios.core.system.getAllSystemProperties
import com.example.keios.ui.page.main.os.OsPageViewModel
import com.rosan.installer.ui.library.effect.getMiuixAppBarColor
import com.rosan.installer.ui.library.effect.rememberMiuixBlurBackdrop
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.viewmodel.compose.viewModel
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.ProgressIndicatorDefaults
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.AddCircle
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.Download
import top.yukonga.miuix.kmp.icon.extended.Filter
import top.yukonga.miuix.kmp.icon.extended.GridView
import top.yukonga.miuix.kmp.icon.extended.Info
import top.yukonga.miuix.kmp.icon.extended.Layers
import top.yukonga.miuix.kmp.icon.extended.ListView
import top.yukonga.miuix.kmp.icon.extended.Lock
import top.yukonga.miuix.kmp.icon.extended.Ok
import top.yukonga.miuix.kmp.icon.extended.Play
import top.yukonga.miuix.kmp.icon.extended.Refresh
import top.yukonga.miuix.kmp.icon.extended.Tune
import top.yukonga.miuix.kmp.icon.extended.Update
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OsPage(
    scrollToTopSignal: Int,
    isPageActive: Boolean = true,
    shizukuStatus: String,
    shizukuApiUtils: ShizukuApiUtils,
    cardPressFeedbackEnabled: Boolean = true,
    liquidActionBarLayeredStyleEnabled: Boolean = true,
    contentBottomPadding: Dp = 72.dp,
    enableSearchBar: Boolean = true,
    onActionBarInteractingChanged: (Boolean) -> Unit = {}
) {
    val isDark = isSystemInDarkTheme()
    val inactive = MiuixTheme.colorScheme.onBackgroundVariant
    val titleColor = MiuixTheme.colorScheme.onBackground
    val cachedColor = Color(0xFFF59E0B)
    val refreshingColor = Color(0xFF3B82F6)
    val syncedColor = Color(0xFF22C55E)
    val context = LocalContext.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val exportSuccessText = stringResource(R.string.common_export_success)
    val noRefreshableCardText = stringResource(R.string.os_toast_no_refreshable_card)
    val refreshCompletedText = stringResource(R.string.os_toast_refresh_completed)
    val manageCardsContentDescription = stringResource(R.string.os_action_manage_cards)
    val manageActivitiesContentDescription = stringResource(R.string.os_action_manage_activities)
    val refreshParamsContentDescription = stringResource(R.string.os_action_refresh_params)
    val searchLabel = stringResource(R.string.os_search_label)
    val visibleCardsTitle = stringResource(R.string.os_sheet_visible_cards_title)
    val visibleActivitiesTitle = stringResource(R.string.os_sheet_visible_activities_title)
    val googleSystemServiceDefaultTitle = stringResource(R.string.os_section_google_system_service_title)
    val googleSystemServiceDefaultSubtitle = stringResource(R.string.os_google_system_service_default_subtitle)
    val googleSystemServiceDefaultAppName = stringResource(R.string.os_google_system_service_default_app_name)
    val googleSystemServiceDefaultIntentFlags =
        stringResource(R.string.os_google_system_service_default_intent_flags)
    val googleSystemServiceDefaults = remember(
        googleSystemServiceDefaultTitle,
        googleSystemServiceDefaultSubtitle,
        googleSystemServiceDefaultAppName,
        googleSystemServiceDefaultIntentFlags
    ) {
        OsGoogleSystemServiceConfig(
            title = googleSystemServiceDefaultTitle,
            subtitle = googleSystemServiceDefaultSubtitle,
            appName = googleSystemServiceDefaultAppName,
            intentFlags = googleSystemServiceDefaultIntentFlags
        ).normalized()
    }
    val noMatchedResultsText = stringResource(R.string.common_no_matched_results)
    val shizukuReady = shizukuStatus.contains("granted", ignoreCase = true)
    val initialUiSnapshot = remember { OsUiStateStore.loadSnapshot() }
    var cacheLoaded by remember { mutableStateOf(false) }
    var cachePersisted by remember { mutableStateOf(false) }
    val osPageViewModel: OsPageViewModel = viewModel()
    val queryInput by osPageViewModel.queryInput.collectAsState()
    val queryApplied by osPageViewModel.queryApplied.collectAsState()
    var topInfoExpanded by remember { mutableStateOf(initialUiSnapshot.topInfoExpanded) }
    var googleSystemServiceExpanded by remember { mutableStateOf(initialUiSnapshot.googleSystemServiceExpanded) }
    var systemTableExpanded by remember { mutableStateOf(initialUiSnapshot.systemTableExpanded) }
    var secureTableExpanded by remember { mutableStateOf(initialUiSnapshot.secureTableExpanded) }
    var globalTableExpanded by remember { mutableStateOf(initialUiSnapshot.globalTableExpanded) }
    var androidPropsExpanded by remember { mutableStateOf(initialUiSnapshot.androidPropsExpanded) }
    var javaPropsExpanded by remember { mutableStateOf(initialUiSnapshot.javaPropsExpanded) }
    var linuxEnvExpanded by remember { mutableStateOf(initialUiSnapshot.linuxEnvExpanded) }
    var visibleCards by remember { mutableStateOf(initialUiSnapshot.visibleCards) }
    var googleSystemServiceConfig by remember {
        mutableStateOf(
            OsShortcutCardStore.loadGoogleSystemServiceConfig(
                defaults = googleSystemServiceDefaults
            )
        )
    }
    var googleSystemServiceDraft by remember { mutableStateOf(googleSystemServiceConfig) }
    var showGoogleSystemServiceEditor by rememberSaveable { mutableStateOf(false) }
    var showGoogleSystemServiceSuggestionSheet by rememberSaveable { mutableStateOf(false) }
    var googleSystemServiceSuggestionTarget by remember {
        mutableStateOf(ShortcutSuggestionField.IntentAction)
    }
    var googleSystemServicePackageSuggestions by remember {
        mutableStateOf<List<ShortcutInstalledAppOption>>(emptyList())
    }
    var googleSystemServicePackageSuggestionsLoading by remember { mutableStateOf(false) }
    var googleSystemServicePackageSuggestionQuery by rememberSaveable { mutableStateOf("") }
    var googleSystemServiceClassSuggestions by remember {
        mutableStateOf<List<ShortcutActivityClassOption>>(emptyList())
    }
    var googleSystemServiceClassSuggestionsLoading by remember { mutableStateOf(false) }
    var googleSystemServiceClassSuggestionQuery by rememberSaveable { mutableStateOf("") }
    var uiStatePersistenceReady by remember { mutableStateOf(false) }
    var showCardManager by rememberSaveable { mutableStateOf(false) }
    var showActivityVisibilityManager by rememberSaveable { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val scrollBehavior = MiuixScrollBehavior()
    var pendingExportContent by remember { mutableStateOf<String?>(null) }
    var exportingCard by remember { mutableStateOf<OsSectionCard?>(null) }
    var refreshing by remember { mutableStateOf(false) }
    var refreshProgress by remember { mutableStateOf(0f) }
    var showSearchBar by remember { mutableStateOf(true) }
    var searchBarHideOffsetPx by remember { mutableStateOf(0f) }
    val surfaceColor = MiuixTheme.colorScheme.surface
    var activationCount by rememberSaveable { mutableIntStateOf(0) }
    DisposableEffect(Unit) {
        activationCount++
        onDispose { }
    }
    val topBarBackdrop: LayerBackdrop = key("os-topbar-$activationCount") {
        rememberLayerBackdrop {
            drawRect(surfaceColor)
            drawContent()
        }
    }
    val contentBackdrop: LayerBackdrop = key("os-content-$activationCount") {
        rememberLayerBackdrop {
            drawRect(surfaceColor)
            drawContent()
        }
    }
    val sheetBackdrop: LayerBackdrop = key("os-sheet-$activationCount") {
        rememberLayerBackdrop {
            drawRect(surfaceColor)
            drawContent()
        }
    }
    val topBarMaterialBackdrop = rememberMiuixBlurBackdrop(enableBlur = true)
    val searchBarHideThresholdPx = remember(density) { with(density) { 28.dp.toPx() } }
    val searchBarScrollConnection = remember(searchBarHideThresholdPx) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < -1f) {
                    if (showSearchBar) {
                        searchBarHideOffsetPx = (searchBarHideOffsetPx + (-available.y)).coerceAtMost(searchBarHideThresholdPx)
                        if (searchBarHideOffsetPx >= searchBarHideThresholdPx) {
                            showSearchBar = false
                            searchBarHideOffsetPx = 0f
                        }
                    }
                }
                if (available.y > 1f) {
                    showSearchBar = true
                    searchBarHideOffsetPx = 0f
                }
                return Offset.Zero
            }
        }
    }
    DisposableEffect(Unit) {
        onDispose { onActionBarInteractingChanged(false) }
    }
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        val content = pendingExportContent
        if (uri == null || content.isNullOrBlank()) return@rememberLauncherForActivityResult
        runCatching {
            context.contentResolver.openOutputStream(uri)?.bufferedWriter().use { writer ->
                writer?.write(content)
            }
        }.onSuccess {
            Toast.makeText(context, exportSuccessText, Toast.LENGTH_SHORT).show()
        }.onFailure {
            Toast.makeText(
                context,
                context.getString(R.string.common_export_failed_with_reason, it.javaClass.simpleName),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    var sectionStates by remember {
        mutableStateOf(
            mapOf(
                SectionKind.SYSTEM to SectionState(),
                SectionKind.SECURE to SectionState(),
                SectionKind.GLOBAL to SectionState(),
                SectionKind.ANDROID to SectionState(),
                SectionKind.JAVA to SectionState(),
                SectionKind.LINUX to SectionState()
            )
        )
    }
    fun updateSection(section: SectionKind, transform: (SectionState) -> SectionState) {
        sectionStates = sectionStates.toMutableMap().also { map ->
            val old = map[section] ?: SectionState()
            map[section] = transform(old)
        }
    }
    suspend fun ensureLoad(section: SectionKind, forceRefresh: Boolean = false) {
        if (!visibleSectionKinds(visibleCards).contains(section)) return
        val current = sectionStates[section] ?: SectionState()
        if (current.loading) return
        if (!forceRefresh) {
            if (current.loadedFresh) return
            if (current.rows.isNotEmpty()) return
        }
        updateSection(section) { it.copy(loading = true) }
        val fresh = withContext(Dispatchers.IO) {
            buildSectionRows(section, context, shizukuStatus, shizukuApiUtils)
        }
        updateSection(section) { it.copy(rows = fresh, loading = false, loadedFresh = true) }
        cachePersisted = withContext(Dispatchers.IO) {
            OsInfoCache.write(section, fresh)
            OsInfoCache.readSnapshot(visibleSectionKinds(visibleCards)).hasPersistedCache
        }
    }

    suspend fun applyCardVisibility(card: OsSectionCard, visible: Boolean) {
        val updated = visibleCards.toMutableSet().apply {
            if (visible) add(card) else remove(card)
        }.toSet()
        visibleCards = updated
        withContext(Dispatchers.IO) { OsCardVisibilityStore.saveVisibleCards(updated) }
        when (card) {
            OsSectionCard.TOP_INFO -> {
                if (!visible) topInfoExpanded = false
            }
            OsSectionCard.GOOGLE_SYSTEM_SERVICE -> {
                if (!visible) {
                    googleSystemServiceExpanded = false
                }
            }
            OsSectionCard.SYSTEM -> {
                if (!visible) {
                    systemTableExpanded = false
                    updateSection(SectionKind.SYSTEM) { SectionState() }
                    withContext(Dispatchers.IO) { OsInfoCache.clear(SectionKind.SYSTEM) }
                } else ensureLoad(SectionKind.SYSTEM, forceRefresh = true)
            }
            OsSectionCard.SECURE -> {
                if (!visible) {
                    secureTableExpanded = false
                    updateSection(SectionKind.SECURE) { SectionState() }
                    withContext(Dispatchers.IO) { OsInfoCache.clear(SectionKind.SECURE) }
                } else ensureLoad(SectionKind.SECURE, forceRefresh = true)
            }
            OsSectionCard.GLOBAL -> {
                if (!visible) {
                    globalTableExpanded = false
                    updateSection(SectionKind.GLOBAL) { SectionState() }
                    withContext(Dispatchers.IO) { OsInfoCache.clear(SectionKind.GLOBAL) }
                } else ensureLoad(SectionKind.GLOBAL, forceRefresh = true)
            }
            OsSectionCard.ANDROID -> {
                if (!visible) {
                    androidPropsExpanded = false
                    updateSection(SectionKind.ANDROID) { SectionState() }
                    withContext(Dispatchers.IO) { OsInfoCache.clear(SectionKind.ANDROID) }
                } else ensureLoad(SectionKind.ANDROID, forceRefresh = true)
            }
            OsSectionCard.JAVA -> {
                if (!visible) {
                    javaPropsExpanded = false
                    updateSection(SectionKind.JAVA) { SectionState() }
                    withContext(Dispatchers.IO) { OsInfoCache.clear(SectionKind.JAVA) }
                } else ensureLoad(SectionKind.JAVA, forceRefresh = true)
            }
            OsSectionCard.LINUX -> {
                if (!visible) {
                    linuxEnvExpanded = false
                    updateSection(SectionKind.LINUX) { SectionState() }
                    withContext(Dispatchers.IO) { OsInfoCache.clear(SectionKind.LINUX) }
                } else ensureLoad(SectionKind.LINUX, forceRefresh = true)
            }
        }
        cachePersisted = withContext(Dispatchers.IO) {
            OsInfoCache.readSnapshot(visibleSectionKinds(visibleCards)).hasPersistedCache
        }
    }

    suspend fun refreshAllSections() {
        refreshing = true
        refreshProgress = 0f
        try {
            val targets = SectionKind.entries.filter { visibleSectionKinds(visibleCards).contains(it) }
            val sectionCount = targets.size.coerceAtLeast(1)
            targets.forEachIndexed { index, section ->
                ensureLoad(section, forceRefresh = true)
                refreshProgress = (index + 1).toFloat() / sectionCount.toFloat()
            }
            Toast.makeText(
                context,
                if (targets.isEmpty()) noRefreshableCardText else refreshCompletedText,
                Toast.LENGTH_SHORT
            ).show()
        } finally {
            refreshing = false
        }
    }

    LaunchedEffect(scrollToTopSignal) {
        if (scrollToTopSignal > 0) listState.animateScrollToItem(0)
    }

    LaunchedEffect(Unit) {
        val visibleSections = visibleSectionKinds(visibleCards)
        val snapshot = withContext(Dispatchers.IO) {
            OsInfoCache.readSnapshot(visibleSections)
        }
        val cached = snapshot.cached
        sectionStates = mapOf(
            SectionKind.SYSTEM to SectionState(rows = if (visibleSections.contains(SectionKind.SYSTEM)) cached.system else emptyList()),
            SectionKind.SECURE to SectionState(rows = if (visibleSections.contains(SectionKind.SECURE)) cached.secure else emptyList()),
            SectionKind.GLOBAL to SectionState(rows = if (visibleSections.contains(SectionKind.GLOBAL)) cached.global else emptyList()),
            SectionKind.ANDROID to SectionState(rows = if (visibleSections.contains(SectionKind.ANDROID)) cached.android else emptyList()),
            SectionKind.JAVA to SectionState(rows = if (visibleSections.contains(SectionKind.JAVA)) cached.java else emptyList()),
            SectionKind.LINUX to SectionState(rows = if (visibleSections.contains(SectionKind.LINUX)) cached.linux else emptyList())
        )
        cachePersisted = snapshot.hasPersistedCache
        cacheLoaded = true
        uiStatePersistenceReady = true
        if (isPageActive) {
            visibleSections.forEach { section ->
                ensureLoad(section, forceRefresh = false)
            }
        }
    }

    LaunchedEffect(shizukuReady) {
        updateSection(SectionKind.SYSTEM) { it.copy(loadedFresh = false) }
        updateSection(SectionKind.SECURE) { it.copy(loadedFresh = false) }
        updateSection(SectionKind.GLOBAL) { it.copy(loadedFresh = false) }
        updateSection(SectionKind.LINUX) { it.copy(loadedFresh = false) }
    }

    LaunchedEffect(topInfoExpanded) {
        if (!uiStatePersistenceReady) return@LaunchedEffect
        withContext(Dispatchers.IO) { OsUiStateStore.setTopInfoExpanded(topInfoExpanded) }
    }
    LaunchedEffect(googleSystemServiceExpanded) {
        if (!uiStatePersistenceReady) return@LaunchedEffect
        withContext(Dispatchers.IO) {
            OsUiStateStore.setGoogleSystemServiceExpanded(googleSystemServiceExpanded)
        }
    }
    LaunchedEffect(systemTableExpanded) {
        if (!uiStatePersistenceReady) return@LaunchedEffect
        withContext(Dispatchers.IO) { OsUiStateStore.setOsSystemTableExpanded(systemTableExpanded) }
    }
    LaunchedEffect(secureTableExpanded) {
        if (!uiStatePersistenceReady) return@LaunchedEffect
        withContext(Dispatchers.IO) { OsUiStateStore.setSecureTableExpanded(secureTableExpanded) }
    }
    LaunchedEffect(globalTableExpanded) {
        if (!uiStatePersistenceReady) return@LaunchedEffect
        withContext(Dispatchers.IO) { OsUiStateStore.setGlobalTableExpanded(globalTableExpanded) }
    }
    LaunchedEffect(androidPropsExpanded) {
        if (!uiStatePersistenceReady) return@LaunchedEffect
        withContext(Dispatchers.IO) { OsUiStateStore.setAndroidPropsExpanded(androidPropsExpanded) }
    }
    LaunchedEffect(javaPropsExpanded) {
        if (!uiStatePersistenceReady) return@LaunchedEffect
        withContext(Dispatchers.IO) { OsUiStateStore.setJavaPropsExpanded(javaPropsExpanded) }
    }
    LaunchedEffect(linuxEnvExpanded) {
        if (!uiStatePersistenceReady) return@LaunchedEffect
        withContext(Dispatchers.IO) { OsUiStateStore.setLinuxEnvExpanded(linuxEnvExpanded) }
    }

    LaunchedEffect(systemTableExpanded, visibleCards, cacheLoaded, isPageActive) {
        if (!cacheLoaded) return@LaunchedEffect
        if (isPageActive && systemTableExpanded && isCardVisible(visibleCards, OsSectionCard.SYSTEM)) ensureLoad(SectionKind.SYSTEM)
    }
    LaunchedEffect(secureTableExpanded, visibleCards, cacheLoaded, isPageActive) {
        if (!cacheLoaded) return@LaunchedEffect
        if (isPageActive && secureTableExpanded && isCardVisible(visibleCards, OsSectionCard.SECURE)) ensureLoad(SectionKind.SECURE)
    }
    LaunchedEffect(globalTableExpanded, visibleCards, cacheLoaded, isPageActive) {
        if (!cacheLoaded) return@LaunchedEffect
        if (isPageActive && globalTableExpanded && isCardVisible(visibleCards, OsSectionCard.GLOBAL)) ensureLoad(SectionKind.GLOBAL)
    }
    LaunchedEffect(androidPropsExpanded, visibleCards, cacheLoaded, isPageActive) {
        if (!cacheLoaded) return@LaunchedEffect
        if (isPageActive && androidPropsExpanded && isCardVisible(visibleCards, OsSectionCard.ANDROID)) ensureLoad(SectionKind.ANDROID)
    }
    LaunchedEffect(javaPropsExpanded, visibleCards, cacheLoaded, isPageActive) {
        if (!cacheLoaded) return@LaunchedEffect
        if (isPageActive && javaPropsExpanded && isCardVisible(visibleCards, OsSectionCard.JAVA)) ensureLoad(SectionKind.JAVA)
    }
    LaunchedEffect(linuxEnvExpanded, visibleCards, cacheLoaded, isPageActive) {
        if (!cacheLoaded) return@LaunchedEffect
        if (isPageActive && linuxEnvExpanded && isCardVisible(visibleCards, OsSectionCard.LINUX)) ensureLoad(SectionKind.LINUX)
    }
    LaunchedEffect(
        showGoogleSystemServiceSuggestionSheet,
        googleSystemServiceSuggestionTarget,
        googleSystemServiceDraft.packageName
    ) {
        if (!showGoogleSystemServiceSuggestionSheet) return@LaunchedEffect
        when (googleSystemServiceSuggestionTarget) {
            ShortcutSuggestionField.PackageName -> {
                googleSystemServicePackageSuggestionsLoading = true
                runCatching {
                    withContext(Dispatchers.IO) { loadInstalledAppOptions(context) }
                }.onSuccess { apps ->
                    googleSystemServicePackageSuggestions = apps
                }.onFailure {
                    googleSystemServicePackageSuggestions = emptyList()
                }
                googleSystemServicePackageSuggestionsLoading = false
            }

            ShortcutSuggestionField.ClassName -> {
                val targetPackageName = googleSystemServiceDraft.packageName.trim()
                if (targetPackageName.isBlank()) {
                    googleSystemServiceClassSuggestions = emptyList()
                    return@LaunchedEffect
                }
                googleSystemServiceClassSuggestionsLoading = true
                runCatching {
                    withContext(Dispatchers.IO) {
                        loadActivityClassOptions(
                            context = context,
                            packageName = targetPackageName
                        )
                    }
                }.onSuccess { classes ->
                    googleSystemServiceClassSuggestions = classes
                }.onFailure {
                    googleSystemServiceClassSuggestions = emptyList()
                }
                googleSystemServiceClassSuggestionsLoading = false
            }

            else -> Unit
        }
    }

    val systemRows = sectionStates[SectionKind.SYSTEM]?.rows ?: emptyList()
    val secureRows = sectionStates[SectionKind.SECURE]?.rows ?: emptyList()
    val globalRows = sectionStates[SectionKind.GLOBAL]?.rows ?: emptyList()
    val androidRows = sectionStates[SectionKind.ANDROID]?.rows ?: emptyList()
    val javaRows = sectionStates[SectionKind.JAVA]?.rows ?: emptyList()
    val linuxRows = sectionStates[SectionKind.LINUX]?.rows ?: emptyList()

    val topInfoRows = remember(systemRows, secureRows, globalRows, androidRows, javaRows, linuxRows) {
        buildTopInfoRows(systemRows, secureRows, globalRows, androidRows, javaRows, linuxRows)
    }

    val prunedSystemRows = remember(systemRows) { removeTopInfoRows(SectionKind.SYSTEM, systemRows) }
    val prunedSecureRows = remember(secureRows) { removeTopInfoRows(SectionKind.SECURE, secureRows) }
    val prunedGlobalRows = remember(globalRows) { removeTopInfoRows(SectionKind.GLOBAL, globalRows) }
    val prunedAndroidRows = remember(androidRows) { removeTopInfoRows(SectionKind.ANDROID, androidRows) }
    val prunedJavaRows = remember(javaRows) { removeTopInfoRows(SectionKind.JAVA, javaRows) }
    val prunedLinuxRows = remember(linuxRows) { removeTopInfoRows(SectionKind.LINUX, linuxRows) }

    val q = queryApplied.trim()
    val displayedTopInfoRows = remember(q, topInfoRows, topInfoExpanded) {
        if (q.isBlank() && !topInfoExpanded) topInfoRows else sortRowsByType(filterRows(topInfoRows, q))
    }
    val displayedSystemRows = remember(q, prunedSystemRows, systemTableExpanded) {
        if (q.isBlank() && !systemTableExpanded) prunedSystemRows else sortRowsByType(filterRows(prunedSystemRows, q))
    }
    val displayedSecureRows = remember(q, prunedSecureRows, secureTableExpanded) {
        if (q.isBlank() && !secureTableExpanded) prunedSecureRows else sortRowsByType(filterRows(prunedSecureRows, q))
    }
    val displayedGlobalRows = remember(q, prunedGlobalRows, globalTableExpanded) {
        if (q.isBlank() && !globalTableExpanded) prunedGlobalRows else sortRowsByType(filterRows(prunedGlobalRows, q))
    }
    val displayedAndroidRows = remember(q, prunedAndroidRows, androidPropsExpanded) {
        if (q.isBlank() && !androidPropsExpanded) prunedAndroidRows else sortRowsByType(filterRows(prunedAndroidRows, q))
    }
    val displayedJavaRows = remember(q, prunedJavaRows, javaPropsExpanded) {
        if (q.isBlank() && !javaPropsExpanded) prunedJavaRows else sortRowsByType(filterRows(prunedJavaRows, q))
    }
    val displayedLinuxRows = remember(q, prunedLinuxRows, linuxEnvExpanded) {
        if (q.isBlank() && !linuxEnvExpanded) prunedLinuxRows else sortRowsByType(filterRows(prunedLinuxRows, q))
    }
    val groupedTopInfoRows = remember(displayedTopInfoRows, topInfoExpanded, q) {
        if (q.isBlank() && !topInfoExpanded) emptyList() else groupTopInfoRows(displayedTopInfoRows)
    }
    val totalRowsCount = remember(
        topInfoRows.size,
        prunedSystemRows.size,
        prunedSecureRows.size,
        prunedGlobalRows.size,
        prunedAndroidRows.size,
        prunedJavaRows.size,
        prunedLinuxRows.size
    ) {
        topInfoRows.size +
            prunedSystemRows.size +
            prunedSecureRows.size +
            prunedGlobalRows.size +
            prunedAndroidRows.size +
            prunedJavaRows.size +
            prunedLinuxRows.size
    }
    val visibleRowsCount = remember(
        displayedTopInfoRows.size,
        displayedSystemRows.size,
        displayedSecureRows.size,
        displayedGlobalRows.size,
        displayedAndroidRows.size,
        displayedJavaRows.size,
        displayedLinuxRows.size
    ) {
        displayedTopInfoRows.size +
            displayedSystemRows.size +
            displayedSecureRows.size +
            displayedGlobalRows.size +
            displayedAndroidRows.size +
            displayedJavaRows.size +
            displayedLinuxRows.size
    }

    suspend fun exportCard(card: OsSectionCard) {
        if (exportingCard != null) return
        exportingCard = card
        try {
            when (card) {
                OsSectionCard.TOP_INFO -> {
                    visibleSectionKinds(visibleCards).forEach { section ->
                        ensureLoad(section, forceRefresh = false)
                    }
                }

                else -> {
                    sectionKindByCard(card)?.let { section ->
                        ensureLoad(section, forceRefresh = false)
                    }
                }
            }

            val rows = currentRowsForCard(
                card = card,
                sectionStates = sectionStates,
                googleSystemServiceConfig = googleSystemServiceConfig,
                googleSystemServiceDefaults = googleSystemServiceDefaults,
                context = context
            )
            val generatedAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val payload = buildOsCardJson(
                generatedAt = generatedAt,
                shizukuStatus = shizukuStatus,
                cardTitle = card.title,
                rows = rows
            )
            val exportStamp = SimpleDateFormat("yyyyMMdd-HHmmss-SSS", Locale.getDefault()).format(Date())
            val fileName = "keios-os-${exportSlug(card)}-$exportStamp.json"
            pendingExportContent = payload
            exportLauncher.launch(fileName)
        } catch (t: Throwable) {
            Toast.makeText(context, "导出失败: ${t.javaClass.simpleName}", Toast.LENGTH_SHORT).show()
        } finally {
            exportingCard = null
        }
    }

    val currentVisibleSectionKinds = visibleSectionKinds(visibleCards)
    val visibleSectionStates = currentVisibleSectionKinds.mapNotNull { sectionStates[it] }
    val loadedFreshCount = visibleSectionStates.count { it.loadedFresh }
    val cachedSectionCount = visibleSectionStates.count { !it.loadedFresh && it.rows.isNotEmpty() }
    val sectionCount = currentVisibleSectionKinds.size
    val overviewState = when {
        refreshing -> SystemOverviewState.Refreshing
        loadedFreshCount == sectionCount && sectionCount > 0 -> SystemOverviewState.Completed
        cachePersisted || cachedSectionCount > 0 -> SystemOverviewState.Cached
        else -> SystemOverviewState.Idle
    }
    val statusLabel = when (overviewState) {
        SystemOverviewState.Cached -> StatusLabelText.Cached
        SystemOverviewState.Refreshing -> StatusLabelText.Syncing
        SystemOverviewState.Completed -> StatusLabelText.Synced
        SystemOverviewState.Idle -> StatusLabelText.PendingSync
    }
    val statusColor = when (overviewState) {
        SystemOverviewState.Cached -> cachedColor
        SystemOverviewState.Refreshing -> refreshingColor
        SystemOverviewState.Completed -> syncedColor
        SystemOverviewState.Idle -> inactive
    }
    val overviewCardColor = if (isDark) {
        statusColor.copy(alpha = 0.16f)
    } else {
        statusColor.copy(alpha = 0.09f)
    }
    val overviewBorderColor = if (isDark) {
        statusColor.copy(alpha = 0.32f)
    } else {
        statusColor.copy(alpha = 0.26f)
    }
    val indicatorProgress = when (overviewState) {
        SystemOverviewState.Refreshing -> refreshProgress.coerceIn(0f, 1f)
        SystemOverviewState.Completed,
        SystemOverviewState.Cached -> 1f
        SystemOverviewState.Idle -> 0f
    }
    val indicatorBg = when (overviewState) {
        SystemOverviewState.Refreshing -> Color(0x553B82F6)
        SystemOverviewState.Completed -> Color(0x5522C55E)
        SystemOverviewState.Cached -> Color(0x55F59E0B)
        SystemOverviewState.Idle -> MiuixTheme.colorScheme.surface
    }
    val overviewMetrics = remember(
        topInfoRows.size,
        totalRowsCount,
        visibleRowsCount,
        sectionCount,
        loadedFreshCount
    ) {
        listOf(
            OsOverviewMetric(
                label = context.getString(R.string.os_overview_metric_visible_rows),
                value = "$visibleRowsCount/$totalRowsCount"
            ),
            OsOverviewMetric(
                label = context.getString(R.string.os_overview_metric_top_info),
                value = context.getString(R.string.common_item_count, topInfoRows.size)
            ),
            OsOverviewMetric(
                label = context.getString(R.string.os_overview_metric_fresh_coverage),
                value = "$loadedFreshCount/$sectionCount"
            ),
            OsOverviewMetric(
                label = context.getString(R.string.os_overview_metric_visible_sections),
                value = context.getString(R.string.os_overview_metric_visible_sections_value, sectionCount)
            )
        )
    }

    AppPageScaffold(
        title = "",
        modifier = Modifier.fillMaxSize(),
        largeTitle = "OS",
        scrollBehavior = scrollBehavior,
        topBarColor = topBarMaterialBackdrop.getMiuixAppBarColor(),
        actions = {
            LiquidActionBar(
                backdrop = topBarBackdrop,
                layeredStyleEnabled = liquidActionBarLayeredStyleEnabled,
                items = listOf(
                    LiquidActionItem(
                        icon = MiuixIcons.Regular.Layers,
                        contentDescription = manageCardsContentDescription,
                        onClick = { showCardManager = true }
                    ),
                    LiquidActionItem(
                        icon = MiuixIcons.Regular.GridView,
                        contentDescription = manageActivitiesContentDescription,
                        onClick = { showActivityVisibilityManager = true }
                    ),
                    LiquidActionItem(
                        icon = MiuixIcons.Regular.Refresh,
                        contentDescription = refreshParamsContentDescription,
                        onClick = {
                            if (refreshing) return@LiquidActionItem
                            scope.launch { refreshAllSections() }
                        }
                    )
                ),
                onInteractionChanged = onActionBarInteractingChanged
            )
        },
        searchBarVisible = enableSearchBar && showSearchBar,
        searchBarAnimationLabelPrefix = "osSearchBar",
        searchBarContent = {
            Column {
                AppTopBarSearchField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppChromeTokens.searchFieldHorizontalPadding),
                    value = queryInput,
                    onValueChange = { osPageViewModel.updateQueryInput(it) },
                    label = searchLabel,
                    backdrop = topBarBackdrop
                )
            }
        }
    ) { innerPadding ->
        OsCardVisibilityManagerSheet(
            show = showCardManager,
            title = visibleCardsTitle,
            sheetBackdrop = sheetBackdrop,
            cardsHintText = "隐藏卡片后会清空对应缓存；重新显示时会立即重新获取并缓存。",
            onDismissRequest = { showCardManager = false },
            isCardVisible = { card -> isCardVisible(visibleCards, card) },
            onCardVisibilityChange = { card, checked ->
                scope.launch { applyCardVisibility(card, checked) }
            }
        )
        OsActivityVisibilityManagerSheet(
            show = showActivityVisibilityManager,
            title = visibleActivitiesTitle,
            sheetBackdrop = sheetBackdrop,
            activityHintText = stringResource(R.string.os_sheet_visible_activities_desc),
            googleSystemServiceConfig = googleSystemServiceConfig,
            googleSystemServiceDefaults = googleSystemServiceDefaults,
            onDismissRequest = { showActivityVisibilityManager = false },
            isCardVisible = { card -> isCardVisible(visibleCards, card) },
            onCardVisibilityChange = { card, checked ->
                scope.launch { applyCardVisibility(card, checked) }
            }
        )
        OsGoogleSystemServiceEditorSheet(
            show = showGoogleSystemServiceEditor,
            sheetBackdrop = sheetBackdrop,
            draft = googleSystemServiceDraft,
            onDraftChange = { googleSystemServiceDraft = it },
            onOpenSuggestionSheet = { target ->
                googleSystemServiceSuggestionTarget = target
                when (target) {
                    ShortcutSuggestionField.PackageName -> googleSystemServicePackageSuggestionQuery = ""
                    ShortcutSuggestionField.ClassName -> googleSystemServiceClassSuggestionQuery = ""
                    else -> Unit
                }
                showGoogleSystemServiceSuggestionSheet = true
            },
            onDismissRequest = { showGoogleSystemServiceEditor = false },
            onSave = {
                val normalized = googleSystemServiceDraft.normalized(googleSystemServiceDefaults)
                googleSystemServiceConfig = normalized
                scope.launch(Dispatchers.IO) {
                    OsShortcutCardStore.saveGoogleSystemServiceConfig(
                        normalized,
                        defaults = googleSystemServiceDefaults
                    )
                }
                Toast.makeText(
                    context,
                    context.getString(R.string.os_google_system_service_toast_saved),
                    Toast.LENGTH_SHORT
                ).show()
                showGoogleSystemServiceEditor = false
            }
        )
        OsGoogleSystemServiceSuggestionSheet(
            show = showGoogleSystemServiceSuggestionSheet,
            target = googleSystemServiceSuggestionTarget,
            draft = googleSystemServiceDraft,
            sheetBackdrop = sheetBackdrop,
            packageSuggestions = googleSystemServicePackageSuggestions,
            packageSuggestionsLoading = googleSystemServicePackageSuggestionsLoading,
            packageSuggestionQuery = googleSystemServicePackageSuggestionQuery,
            onPackageSuggestionQueryChange = { googleSystemServicePackageSuggestionQuery = it },
            classSuggestions = googleSystemServiceClassSuggestions,
            classSuggestionsLoading = googleSystemServiceClassSuggestionsLoading,
            classSuggestionQuery = googleSystemServiceClassSuggestionQuery,
            onClassSuggestionQueryChange = { googleSystemServiceClassSuggestionQuery = it },
            noMatchedResultsText = noMatchedResultsText,
            onDismissRequest = { showGoogleSystemServiceSuggestionSheet = false },
            onApplySuggestion = { suggestion ->
                googleSystemServiceDraft = applyGoogleSystemServiceSuggestion(
                    draft = googleSystemServiceDraft,
                    target = googleSystemServiceSuggestionTarget,
                    item = suggestion,
                    defaultIntentFlags = googleSystemServiceDefaultIntentFlags
                )
                showGoogleSystemServiceSuggestionSheet = false
            },
            onApplyExplicitActionRecommendation = {
                googleSystemServiceDraft = googleSystemServiceDraft.copy(
                    intentAction = Intent.ACTION_VIEW
                )
            },
            onApplyImplicitActionRecommendation = {
                googleSystemServiceDraft = applyShortcutImplicitDefaults(
                    draft = googleSystemServiceDraft,
                    defaultIntentFlags = googleSystemServiceDefaultIntentFlags
                )
            },
            onApplyExplicitCategoryRecommendation = {
                googleSystemServiceDraft = googleSystemServiceDraft.copy(
                    intentCategory = ""
                )
            },
            onApplyImplicitCategoryRecommendation = {
                googleSystemServiceDraft = applyShortcutImplicitDefaults(
                    draft = googleSystemServiceDraft,
                    defaultIntentFlags = googleSystemServiceDefaultIntentFlags
                )
            }
        )
        OsPageMainList(
            context = context,
            listState = listState,
            innerPadding = innerPadding,
            searchBarScrollConnection = searchBarScrollConnection,
            scrollBehaviorConnection = scrollBehavior.nestedScrollConnection,
            contentBackdrop = contentBackdrop,
            isDark = isDark,
            titleColor = titleColor,
            cardPressFeedbackEnabled = cardPressFeedbackEnabled,
            refreshing = refreshing,
            overviewState = overviewState,
            indicatorProgress = indicatorProgress,
            statusColor = statusColor,
            indicatorBg = indicatorBg,
            statusLabel = statusLabel,
            overviewCardColor = overviewCardColor,
            overviewBorderColor = overviewBorderColor,
            overviewMetrics = overviewMetrics,
            noMatchedResultsText = noMatchedResultsText,
            query = q,
            displayedTopInfoRows = displayedTopInfoRows,
            groupedTopInfoRows = groupedTopInfoRows,
            topInfoExpanded = topInfoExpanded,
            onTopInfoExpandedChange = { topInfoExpanded = it },
            googleSystemServiceConfig = googleSystemServiceConfig,
            googleSystemServiceDefaults = googleSystemServiceDefaults,
            googleSystemServiceExpanded = googleSystemServiceExpanded,
            onGoogleSystemServiceExpandedChange = { googleSystemServiceExpanded = it },
            onOpenGoogleSystemServiceActivity = {
                val normalized = googleSystemServiceConfig.normalized(googleSystemServiceDefaults)
                if (normalized.packageName.isBlank()) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.os_google_system_service_toast_invalid_target),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    runCatching {
                        launchGoogleSystemServiceActivity(
                            context = context,
                            config = googleSystemServiceConfig,
                            defaults = googleSystemServiceDefaults
                        )
                    }.onFailure { error ->
                        Toast.makeText(
                            context,
                            context.getString(
                                R.string.os_google_system_service_toast_open_failed,
                                error.javaClass.simpleName
                            ),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            },
            onOpenGoogleSystemServiceEditor = {
                googleSystemServiceDraft = googleSystemServiceConfig.normalized(googleSystemServiceDefaults)
                showGoogleSystemServiceEditor = true
            },
            displayedSystemRows = displayedSystemRows,
            displayedSecureRows = displayedSecureRows,
            displayedGlobalRows = displayedGlobalRows,
            displayedAndroidRows = displayedAndroidRows,
            displayedJavaRows = displayedJavaRows,
            displayedLinuxRows = displayedLinuxRows,
            prunedSystemRows = prunedSystemRows,
            prunedSecureRows = prunedSecureRows,
            prunedGlobalRows = prunedGlobalRows,
            prunedAndroidRows = prunedAndroidRows,
            prunedJavaRows = prunedJavaRows,
            prunedLinuxRows = prunedLinuxRows,
            systemTableExpanded = systemTableExpanded,
            onSystemTableExpandedChange = { systemTableExpanded = it },
            secureTableExpanded = secureTableExpanded,
            onSecureTableExpandedChange = { secureTableExpanded = it },
            globalTableExpanded = globalTableExpanded,
            onGlobalTableExpandedChange = { globalTableExpanded = it },
            androidPropsExpanded = androidPropsExpanded,
            onAndroidPropsExpandedChange = { androidPropsExpanded = it },
            javaPropsExpanded = javaPropsExpanded,
            onJavaPropsExpandedChange = { javaPropsExpanded = it },
            linuxEnvExpanded = linuxEnvExpanded,
            onLinuxEnvExpandedChange = { linuxEnvExpanded = it },
            isCardVisible = { card -> isCardVisible(visibleCards, card) },
            sectionSubtitle = { section, size ->
                sectionSubtitle(
                    sectionStates = sectionStates,
                    context = context,
                    section = section,
                    size = size
                )
            },
            exportingCard = exportingCard,
            onExportCard = { card -> scope.launch { exportCard(card) } },
            onRefreshAll = { scope.launch { refreshAllSections() } }
        )
    }
}
