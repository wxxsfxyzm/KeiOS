package com.example.keios.ui.page.main

import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.keios.mcp.McpNotificationHelper
import com.example.keios.ui.page.main.widget.GlassIconButton
import com.example.keios.ui.page.main.widget.GlassSearchField
import com.example.keios.ui.page.main.widget.GlassTextButton
import com.example.keios.ui.page.main.widget.LiquidActionBar
import com.example.keios.ui.page.main.widget.LiquidActionItem
import com.example.keios.ui.page.main.widget.MiuixInfoItem
import com.example.keios.ui.page.main.widget.StatusPill
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.delay
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.DropdownImpl
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.AddCircle
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.Edit
import top.yukonga.miuix.kmp.icon.extended.Ok
import top.yukonga.miuix.kmp.icon.extended.Refresh
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowBottomSheet
import top.yukonga.miuix.kmp.window.WindowListPopup
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class BAInitState {
    Empty,
    Draft
}

private const val BA_AP_MAX = 240
private const val BA_AP_REGEN_INTERVAL_MS = 6 * 60 * 1000L
private const val BA_AP_REGEN_TICK_MS = 30_000L

private fun formatBaDateTime(epochMillis: Long): String {
    if (epochMillis <= 0L) return "未同步"
    return runCatching {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(epochMillis))
    }.getOrDefault("未同步")
}

private object BASettingsStore {
    private const val KV_ID = "ba_page_settings"
    private const val KEY_SERVER_INDEX = "server_index"
    private const val KEY_AP_LIMIT = "ap_limit"
    private const val KEY_AP_NOTIFY_ENABLED = "ap_notify_enabled"
    private const val KEY_AP_NOTIFY_THRESHOLD = "ap_notify_threshold"
    private const val KEY_AP_CURRENT = "ap_current"
    private const val KEY_AP_REGEN_BASE_MS = "ap_regen_base_ms"
    private const val KEY_AP_SYNC_MS = "ap_sync_ms"

    private const val DEFAULT_SERVER_INDEX = 2
    private const val DEFAULT_AP_LIMIT = BA_AP_MAX
    private const val DEFAULT_AP_NOTIFY_THRESHOLD = 120
    private const val DEFAULT_AP_CURRENT = 0

    private fun kv(): MMKV = MMKV.mmkvWithID(KV_ID)

    fun loadServerIndex(): Int = kv().decodeInt(KEY_SERVER_INDEX, DEFAULT_SERVER_INDEX).coerceIn(0, 2)

    fun saveServerIndex(index: Int) {
        kv().encode(KEY_SERVER_INDEX, index.coerceIn(0, 2))
    }

    fun loadApLimit(): Int = kv().decodeInt(KEY_AP_LIMIT, DEFAULT_AP_LIMIT).coerceIn(0, BA_AP_MAX)

    fun saveApLimit(limit: Int) {
        kv().encode(KEY_AP_LIMIT, limit.coerceIn(0, BA_AP_MAX))
    }

    fun loadApNotifyEnabled(): Boolean = kv().decodeBool(KEY_AP_NOTIFY_ENABLED, false)

    fun saveApNotifyEnabled(enabled: Boolean) {
        kv().encode(KEY_AP_NOTIFY_ENABLED, enabled)
    }

    fun loadApNotifyThreshold(): Int =
        kv().decodeInt(KEY_AP_NOTIFY_THRESHOLD, DEFAULT_AP_NOTIFY_THRESHOLD).coerceIn(0, BA_AP_MAX)

    fun saveApNotifyThreshold(threshold: Int) {
        kv().encode(KEY_AP_NOTIFY_THRESHOLD, threshold.coerceIn(0, BA_AP_MAX))
    }

    fun loadApCurrent(): Int = kv().decodeInt(KEY_AP_CURRENT, DEFAULT_AP_CURRENT).coerceIn(0, BA_AP_MAX)

    fun saveApCurrent(current: Int) {
        kv().encode(KEY_AP_CURRENT, current.coerceIn(0, BA_AP_MAX))
    }

    fun loadApRegenBaseMs(): Long = kv().decodeLong(KEY_AP_REGEN_BASE_MS, 0L)

    fun saveApRegenBaseMs(epochMs: Long) {
        kv().encode(KEY_AP_REGEN_BASE_MS, epochMs.coerceAtLeast(0L))
    }

    fun loadApSyncMs(): Long = kv().decodeLong(KEY_AP_SYNC_MS, 0L)

    fun saveApSyncMs(epochMs: Long) {
        kv().encode(KEY_AP_SYNC_MS, epochMs.coerceAtLeast(0L))
    }
}

@Composable
fun BAPage(
    contentBottomPadding: Dp = 72.dp,
    scrollToTopSignal: Int = 0,
    onActionBarInteractingChanged: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val scrollBehavior = MiuixScrollBehavior()
    val surfaceColor = MiuixTheme.colorScheme.surface
    val backdrop: LayerBackdrop = rememberLayerBackdrop {
        drawRect(surfaceColor)
        drawContent()
    }
    val serverOptions = remember { listOf("国服", "国际服", "日服") }

    var initState by remember { mutableStateOf(BAInitState.Empty) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showServerPopup by remember { mutableStateOf(false) }

    var serverIndex by remember { mutableIntStateOf(BASettingsStore.loadServerIndex()) }
    var apLimit by remember { mutableIntStateOf(BASettingsStore.loadApLimit()) }
    var apCurrent by remember {
        mutableIntStateOf(
            BASettingsStore.loadApCurrent().coerceIn(0, BASettingsStore.loadApLimit().coerceIn(0, BA_AP_MAX))
        )
    }
    var apRegenBaseMs by remember { mutableLongStateOf(BASettingsStore.loadApRegenBaseMs()) }
    var apSyncMs by remember { mutableLongStateOf(BASettingsStore.loadApSyncMs()) }
    var apNotifyEnabled by remember { mutableStateOf(BASettingsStore.loadApNotifyEnabled()) }
    var apNotifyThreshold by remember { mutableIntStateOf(BASettingsStore.loadApNotifyThreshold()) }

    var sheetServerIndex by remember { mutableIntStateOf(serverIndex) }
    var sheetApLimitText by remember { mutableStateOf(apLimit.toString()) }
    var sheetApNotifyEnabled by remember { mutableStateOf(apNotifyEnabled) }
    var sheetApNotifyThresholdText by remember { mutableStateOf(apNotifyThreshold.toString()) }

    var apCurrentInput by remember { mutableStateOf(apCurrent.toString()) }
    var apLimitInput by remember { mutableStateOf(apLimit.toString()) }
    var apLastNotifiedLevel by remember { mutableIntStateOf(-1) }

    fun ensureRegenBase(nowMs: Long = System.currentTimeMillis()) {
        if (apRegenBaseMs <= 0L) {
            apRegenBaseMs = nowMs
            BASettingsStore.saveApRegenBaseMs(nowMs)
        }
    }

    fun updateCurrentAp(newValue: Int, markSync: Boolean) {
        val nowMs = System.currentTimeMillis()
        val clamped = newValue.coerceIn(0, apLimit.coerceIn(0, BA_AP_MAX))
        apCurrent = clamped
        BASettingsStore.saveApCurrent(clamped)
        apRegenBaseMs = nowMs
        BASettingsStore.saveApRegenBaseMs(nowMs)
        if (markSync) {
            apSyncMs = nowMs
            BASettingsStore.saveApSyncMs(nowMs)
        }
    }

    fun updateApLimit(newLimit: Int) {
        val clamped = newLimit.coerceIn(0, BA_AP_MAX)
        apLimit = clamped
        BASettingsStore.saveApLimit(clamped)
        if (apCurrent > clamped) {
            apCurrent = clamped
            BASettingsStore.saveApCurrent(clamped)
        }
        ensureRegenBase()
    }

    fun applyApRegen(nowMs: Long = System.currentTimeMillis()) {
        val limit = apLimit.coerceIn(0, BA_AP_MAX)
        if (limit <= 0) {
            if (apCurrent != 0) {
                apCurrent = 0
                BASettingsStore.saveApCurrent(0)
            }
            apRegenBaseMs = nowMs
            BASettingsStore.saveApRegenBaseMs(nowMs)
            return
        }

        if (apCurrent > limit) {
            apCurrent = limit
            BASettingsStore.saveApCurrent(limit)
        }

        ensureRegenBase(nowMs)

        if (apCurrent >= limit) {
            if (apRegenBaseMs != nowMs) {
                apRegenBaseMs = nowMs
                BASettingsStore.saveApRegenBaseMs(nowMs)
            }
            return
        }

        val elapsed = (nowMs - apRegenBaseMs).coerceAtLeast(0L)
        val gained = (elapsed / BA_AP_REGEN_INTERVAL_MS).toInt()
        if (gained <= 0) return

        val nextAp = (apCurrent + gained).coerceAtMost(limit)
        apCurrent = nextAp
        BASettingsStore.saveApCurrent(nextAp)

        apRegenBaseMs = if (nextAp >= limit) {
            nowMs
        } else {
            apRegenBaseMs + gained * BA_AP_REGEN_INTERVAL_MS
        }
        BASettingsStore.saveApRegenBaseMs(apRegenBaseMs)
    }

    fun calculateApFullAtMs(nowMs: Long = System.currentTimeMillis()): Long {
        val limit = apLimit.coerceIn(0, BA_AP_MAX)
        if (limit <= 0) return nowMs
        val current = apCurrent.coerceIn(0, limit)
        if (current >= limit) return nowMs

        val base = apRegenBaseMs.takeIf { it > 0L } ?: nowMs
        val elapsed = (nowMs - base).coerceAtLeast(0L)
        val remainder = elapsed % BA_AP_REGEN_INTERVAL_MS
        val pointsNeeded = limit - current

        val untilNextPoint = if (remainder == 0L) BA_AP_REGEN_INTERVAL_MS else BA_AP_REGEN_INTERVAL_MS - remainder
        return nowMs + untilNextPoint + (pointsNeeded - 1L) * BA_AP_REGEN_INTERVAL_MS
    }

    fun sendApTestNotification(showToast: Boolean = true): Boolean {
        val notificationsGranted = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        if (!notificationsGranted) {
            if (showToast) Toast.makeText(context, "请先授予通知权限", Toast.LENGTH_SHORT).show()
            return false
        }
        McpNotificationHelper.notifyTest(
            context = context,
            serverName = "BlueArchive AP",
            running = true,
            port = apCurrent,
            path = "阈值:$apNotifyThreshold",
            clients = apLimit
        )
        if (showToast) Toast.makeText(context, "已发送AP测试通知", Toast.LENGTH_SHORT).show()
        return true
    }

    fun tryApThresholdNotification() {
        if (!apNotifyEnabled) {
            apLastNotifiedLevel = -1
            return
        }
        val threshold = apNotifyThreshold.coerceIn(0, BA_AP_MAX)
        if (apCurrent < threshold) {
            apLastNotifiedLevel = -1
            return
        }
        if (apCurrent == apLastNotifiedLevel) return
        if (sendApTestNotification(showToast = false)) {
            apLastNotifiedLevel = apCurrent
        }
    }

    fun openSettingsSheet() {
        sheetServerIndex = serverIndex
        sheetApLimitText = apLimit.toString()
        sheetApNotifyEnabled = apNotifyEnabled
        sheetApNotifyThresholdText = apNotifyThreshold.toString()
        showServerPopup = false
        showSettingsSheet = true
    }

    fun closeSettingsSheet() {
        showSettingsSheet = false
        showServerPopup = false
        sheetServerIndex = serverIndex
        sheetApLimitText = apLimit.toString()
        sheetApNotifyEnabled = apNotifyEnabled
        sheetApNotifyThresholdText = apNotifyThreshold.toString()
    }

    fun saveSettings() {
        val savedServer = sheetServerIndex.coerceIn(0, serverOptions.lastIndex)
        val savedApLimit = sheetApLimitText.toIntOrNull()?.coerceIn(0, BA_AP_MAX) ?: BA_AP_MAX
        val savedThreshold = sheetApNotifyThresholdText.toIntOrNull()?.coerceIn(0, BA_AP_MAX) ?: 120

        BASettingsStore.saveServerIndex(savedServer)
        serverIndex = savedServer

        updateApLimit(savedApLimit)

        BASettingsStore.saveApNotifyEnabled(sheetApNotifyEnabled)
        BASettingsStore.saveApNotifyThreshold(savedThreshold)
        apNotifyEnabled = sheetApNotifyEnabled
        apNotifyThreshold = savedThreshold

        applyApRegen()
        showSettingsSheet = false
        showServerPopup = false
    }

    DisposableEffect(Unit) {
        onDispose { onActionBarInteractingChanged(false) }
    }

    LaunchedEffect(scrollToTopSignal) {
        if (scrollToTopSignal > 0) listState.animateScrollToItem(0)
    }

    LaunchedEffect(Unit) {
        ensureRegenBase()
        applyApRegen()
        while (true) {
            delay(BA_AP_REGEN_TICK_MS)
            applyApRegen()
        }
    }

    LaunchedEffect(apCurrent) {
        val target = apCurrent.toString()
        if (apCurrentInput != target) apCurrentInput = target
    }

    LaunchedEffect(apLimit) {
        val target = apLimit.toString()
        if (apLimitInput != target) apLimitInput = target
    }

    LaunchedEffect(apCurrent, apNotifyEnabled, apNotifyThreshold) {
        tryApThresholdNotification()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column {
                TopAppBar(
                    title = "BlueArchive",
                    scrollBehavior = scrollBehavior,
                    color = MiuixTheme.colorScheme.surface,
                    actions = {
                        Box {
                            LiquidActionBar(
                                backdrop = backdrop,
                                items = listOf(
                                    LiquidActionItem(
                                        icon = MiuixIcons.Regular.Refresh,
                                        contentDescription = "刷新",
                                        onClick = { applyApRegen() }
                                    ),
                                    LiquidActionItem(
                                        icon = MiuixIcons.Regular.Edit,
                                        contentDescription = "编辑",
                                        onClick = { openSettingsSheet() }
                                    ),
                                    LiquidActionItem(
                                        icon = MiuixIcons.Regular.AddCircle,
                                        contentDescription = "新建",
                                        onClick = { initState = BAInitState.Draft }
                                    )
                                ),
                                onInteractionChanged = onActionBarInteractingChanged
                            )
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            state = listState,
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding() + contentBottomPadding + 16.dp,
                start = 12.dp,
                end = 12.dp
            )
        ) {
            item { SmallTitle("夏莱办公室") }
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = {
                                if (initState == BAInitState.Empty) {
                                    initState = BAInitState.Draft
                                }
                            },
                            onLongClick = { initState = BAInitState.Empty }
                        ),
                    colors = CardDefaults.defaultColors(
                        color = when (initState) {
                            BAInitState.Empty -> Color(0x33F59E0B)
                            BAInitState.Draft -> Color(0x333B82F6)
                        },
                        contentColor = MiuixTheme.colorScheme.onBackground
                    ),
                    showIndication = true,
                    onClick = {
                        if (initState == BAInitState.Empty) {
                            initState = BAInitState.Draft
                        }
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Overview", color = MiuixTheme.colorScheme.onBackground)
                            StatusPill(
                                label = if (initState == BAInitState.Empty) "Init" else "Draft",
                                color = if (initState == BAInitState.Empty) Color(0xFFF59E0B) else Color(0xFF3B82F6)
                            )
                        }
                        MiuixInfoItem(
                            key = "状态",
                            value = if (initState == BAInitState.Empty) "未初始化" else "草稿中"
                        )
                        MiuixInfoItem(
                            key = "服务器",
                            value = serverOptions[serverIndex]
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("AP", color = MiuixTheme.colorScheme.onBackground)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                GlassSearchField(
                                    modifier = Modifier.width(72.dp),
                                    value = apCurrentInput,
                                    onValueChange = { input ->
                                        val digits = input.filter { it.isDigit() }.take(3)
                                        if (digits.isBlank()) {
                                            apCurrentInput = ""
                                        } else {
                                            val normalized = digits.toIntOrNull()?.coerceIn(0, apLimit.coerceIn(0, BA_AP_MAX))
                                            if (normalized != null) {
                                                apCurrentInput = normalized.toString()
                                            }
                                        }
                                    },
                                    onImeActionDone = {
                                        val finalValue = apCurrentInput.toIntOrNull()?.coerceIn(0, apLimit.coerceIn(0, BA_AP_MAX)) ?: 0
                                        updateCurrentAp(finalValue, markSync = true)
                                        apCurrentInput = finalValue.toString()
                                    },
                                    label = "0",
                                    backdrop = backdrop,
                                    singleLine = true,
                                    textAlign = TextAlign.Center,
                                    fontSize = 18.sp
                                )
                                Text("/", color = MiuixTheme.colorScheme.onBackgroundVariant)
                                GlassSearchField(
                                    modifier = Modifier.width(72.dp),
                                    value = apLimitInput,
                                    onValueChange = { input ->
                                        val digits = input.filter { it.isDigit() }.take(3)
                                        if (digits.isBlank()) {
                                            apLimitInput = ""
                                        } else {
                                            val normalized = digits.toIntOrNull()?.coerceIn(0, BA_AP_MAX)
                                            if (normalized != null) {
                                                apLimitInput = normalized.toString()
                                            }
                                        }
                                    },
                                    onImeActionDone = {
                                        val finalValue = apLimitInput.toIntOrNull()?.coerceIn(0, BA_AP_MAX) ?: BA_AP_MAX
                                        updateApLimit(finalValue)
                                        applyApRegen()
                                        apLimitInput = finalValue.toString()
                                    },
                                    label = "240",
                                    backdrop = backdrop,
                                    singleLine = true,
                                    textAlign = TextAlign.Center,
                                    fontSize = 18.sp
                                )
                            }
                        }

                        MiuixInfoItem(
                            key = "AP同步时间",
                            value = formatBaDateTime(apSyncMs)
                        )
                        MiuixInfoItem(
                            key = "AP回满时间",
                            value = formatBaDateTime(calculateApFullAtMs())
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(10.dp)) }

            item {
                GlassTextButton(
                    backdrop = backdrop,
                    text = "测试AP通知",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { sendApTestNotification(showToast = true) }
                )
            }
        }
    }

    WindowBottomSheet(
        show = showSettingsSheet,
        title = "BA 配置",
        onDismissRequest = { closeSettingsSheet() },
        startAction = {
            GlassIconButton(
                backdrop = backdrop,
                icon = MiuixIcons.Regular.Close,
                contentDescription = "关闭",
                onClick = { closeSettingsSheet() }
            )
        },
        endAction = {
            GlassIconButton(
                backdrop = backdrop,
                icon = MiuixIcons.Regular.Ok,
                contentDescription = "保存",
                onClick = { saveSettings() }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "服务器",
                    color = MiuixTheme.colorScheme.onBackground
                )
                Box {
                    GlassTextButton(
                        backdrop = backdrop,
                        text = serverOptions[sheetServerIndex],
                        onClick = { showServerPopup = !showServerPopup }
                    )
                    if (showServerPopup) {
                        WindowListPopup(
                            show = showServerPopup,
                            alignment = PopupPositionProvider.Align.BottomEnd,
                            onDismissRequest = { showServerPopup = false },
                            enableWindowDim = false
                        ) {
                            ListPopupColumn {
                                serverOptions.forEachIndexed { index, server ->
                                    DropdownImpl(
                                        text = server,
                                        optionSize = serverOptions.size,
                                        isSelected = sheetServerIndex == index,
                                        index = index,
                                        onSelectedIndexChange = { selected ->
                                            sheetServerIndex = selected
                                            showServerPopup = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AP 上限",
                    color = MiuixTheme.colorScheme.onBackground
                )
                GlassSearchField(
                    modifier = Modifier.width(92.dp),
                    value = sheetApLimitText,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }.take(3)
                        if (digits.isBlank()) {
                            sheetApLimitText = ""
                        } else {
                            val normalized = digits.toIntOrNull()?.coerceIn(0, BA_AP_MAX)
                            sheetApLimitText = normalized?.toString() ?: ""
                        }
                    },
                    onImeActionDone = {
                        val normalized = sheetApLimitText.toIntOrNull()?.coerceIn(0, BA_AP_MAX) ?: BA_AP_MAX
                        sheetApLimitText = normalized.toString()
                    },
                    label = "240",
                    backdrop = backdrop,
                    singleLine = true,
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AP通知",
                    color = MiuixTheme.colorScheme.onBackground
                )
                Switch(
                    checked = sheetApNotifyEnabled,
                    onCheckedChange = { checked -> sheetApNotifyEnabled = checked }
                )
            }

            if (sheetApNotifyEnabled) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "提醒阈值",
                        color = MiuixTheme.colorScheme.onBackground
                    )
                    GlassSearchField(
                        modifier = Modifier.width(92.dp),
                        value = sheetApNotifyThresholdText,
                        onValueChange = { input ->
                            val digits = input.filter { it.isDigit() }.take(3)
                            if (digits.isBlank()) {
                                sheetApNotifyThresholdText = ""
                            } else {
                                val normalized = digits.toIntOrNull()?.coerceIn(0, BA_AP_MAX)
                                sheetApNotifyThresholdText = normalized?.toString() ?: ""
                            }
                        },
                        onImeActionDone = {
                            val normalized = sheetApNotifyThresholdText.toIntOrNull()?.coerceIn(0, BA_AP_MAX) ?: 120
                            sheetApNotifyThresholdText = normalized.toString()
                        },
                        label = "120",
                        backdrop = backdrop,
                        singleLine = true,
                        textAlign = TextAlign.Center,
                        fontSize = 18.sp
                    )
                }
            }

            Text(
                text = "不同服务器时区不同，建议按实际游玩服务器设置。",
                color = MiuixTheme.colorScheme.onBackgroundVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
