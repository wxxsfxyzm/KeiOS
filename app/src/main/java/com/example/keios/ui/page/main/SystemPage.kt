package com.example.keios.ui.page.main

import android.app.ActivityManager
import android.content.Context
import android.content.pm.FeatureInfo
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.keios.ui.page.main.widget.MiuixExpandableSection
import com.example.keios.ui.page.main.widget.MiuixInfoItem
import com.example.keios.ui.utils.ShizukuApiUtils
import com.example.keios.ui.utils.getAllJavaPropString
import com.example.keios.ui.utils.getAllSystemProperties
import com.kyant.backdrop.Backdrop
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class InfoRow(
    val key: String,
    val value: String
)

private enum class SectionKind {
    SYSTEM,
    SECURE,
    GLOBAL,
    ANDROID,
    JAVA,
    LINUX
}

private data class SectionState(
    val rows: List<InfoRow> = emptyList(),
    val loading: Boolean = false,
    val loadedFresh: Boolean = false
)

private data class CachedSections(
    val system: List<InfoRow> = emptyList(),
    val secure: List<InfoRow> = emptyList(),
    val global: List<InfoRow> = emptyList(),
    val android: List<InfoRow> = emptyList(),
    val java: List<InfoRow> = emptyList(),
    val linux: List<InfoRow> = emptyList()
)

private object TopInfoKeys {
    val system = linkedSetOf(
        "locked_apps",
        "long_press_camera_key",
        "long_press_home_key",
        "long_press_power_key"
    )

    val secure = linkedSetOf(
        "share_camera",
        "share_launcher",
        "share_unlock",
        "theme_rear_widget",
        "voice_interaction_service",
        "voice_recognition_service"
    )

    val global = linkedSetOf(
        "adb_allowrd_connection_time",
        "adb_enabled",
        "adb_notification_shown",
        "adb_wifi_enabled",
        "database_creation_buildid",
        "device_name",
        "effect_implementer",
        "is_auto_update",
        "is_beta",
        "key_last_update_timestamp_0",
        "key_miui_font_weight_scale",
        "lastVersion",
        "lastVersionProjection",
        "last_screenshot_time",
        "lc3Enable",
        "lea_device_status",
        "mi_bt_plugin_consume_traffic_month",
        "mi_cloud_data_fetch_time",
        "mi_fastconnect_consume_traffic_day",
        "mi_fastconnect_consume_traffic_month",
        "mi_fc_last_sync_data_time",
        "mi_flashlight_strength",
        "mi_is_temp_high_close_torch",
        "miui_current_version_branch",
        "miui_is_last_auto_update",
        "miui_memory_size",
        "miui_nearby_download_device_time",
        "miui_phone_rssi_threshold",
        "miui_pre_big_miui_version",
        "miui_pre_codebase",
        "miui_pre_version",
        "miui_process_exit_falg",
        "miui_ram_size",
        "miui_terms_agreed_time",
        "miui_update_ready",
        "miui_version_name",
        "network_watchlist_last_report_time",
        "sc_last_check_dm_time_0",
        "task_stack_view_layout_style",
        "upload_apk_enable",
        "usb_mass_storage_enabled",
        "xiaomi_mi_play_last_playing_package_name"
    )

    val java = linkedSetOf(
        "android.icu.cldr.version",
        "android.icu.library.version",
        "android.icu.unicode.version",
        "android.openssl.version",
        "android.zlib.version",
        "file.encoding",
        "http.agent",
        "java.class.version",
        "java.runtime.version",
        "java.vm.version",
        "os.arch",
        "os.name",
        "os.version",
        "user.language",
        "user.locale",
        "user.name",
        "user.region"
    )

    val linux = linkedSetOf(
        "uname-a",
        "getenforce",
        "proc.version",
        "toybox --version",
        "env.ANDROID_SOCKET_zygote"
    )
}

private object SystemInfoCache {
    private const val KV_ID = "system_info_cache"
    private const val KEY_SYSTEM = "section_system_table"
    private const val KEY_SECURE = "section_secure_table"
    private const val KEY_GLOBAL = "section_global_table"
    private const val KEY_ANDROID = "section_android_properties"
    private const val KEY_JAVA = "section_java_properties"
    private const val KEY_LINUX = "section_linux_environment"

    private fun encodeRows(rows: List<InfoRow>): String {
        return rows.joinToString("\n") { row ->
            "${Uri.encode(row.key)}\t${Uri.encode(row.value)}"
        }
    }

    private fun decodeRows(raw: String?): List<InfoRow> {
        if (raw.isNullOrBlank()) return emptyList()
        return raw.lineSequence().mapNotNull { line ->
            val index = line.indexOf('\t')
            if (index <= 0) return@mapNotNull null
            val key = Uri.decode(line.substring(0, index))
            val value = Uri.decode(line.substring(index + 1))
            InfoRow(key, value)
        }.toList()
    }

    fun read(): CachedSections {
        val kv = MMKV.mmkvWithID(KV_ID)
        return CachedSections(
            system = decodeRows(kv.decodeString(KEY_SYSTEM)),
            secure = decodeRows(kv.decodeString(KEY_SECURE)),
            global = decodeRows(kv.decodeString(KEY_GLOBAL)),
            android = decodeRows(kv.decodeString(KEY_ANDROID)),
            java = decodeRows(kv.decodeString(KEY_JAVA)),
            linux = decodeRows(kv.decodeString(KEY_LINUX))
        )
    }

    fun write(section: SectionKind, rows: List<InfoRow>) {
        val kv = MMKV.mmkvWithID(KV_ID)
        when (section) {
            SectionKind.SYSTEM -> kv.encode(KEY_SYSTEM, encodeRows(rows))
            SectionKind.SECURE -> kv.encode(KEY_SECURE, encodeRows(rows))
            SectionKind.GLOBAL -> kv.encode(KEY_GLOBAL, encodeRows(rows))
            SectionKind.ANDROID -> kv.encode(KEY_ANDROID, encodeRows(rows))
            SectionKind.JAVA -> kv.encode(KEY_JAVA, encodeRows(rows))
            SectionKind.LINUX -> kv.encode(KEY_LINUX, encodeRows(rows))
        }
    }
}

private fun matches(row: InfoRow, query: String): Boolean {
    if (query.isBlank()) return true
    return row.key.contains(query, ignoreCase = true) || row.value.contains(query, ignoreCase = true)
}

private fun filterRows(rows: List<InfoRow>, query: String): List<InfoRow> {
    if (query.isBlank()) return rows
    return rows.filter { matches(it, query) }
}

private fun isInvalidValue(raw: String): Boolean {
    val value = raw.trim()
    if (value.isBlank()) return true
    val normalized = value.lowercase()
    if (normalized == "n/a" || normalized == "na" || normalized == "unknown" || normalized == "null") return true
    if (normalized == "not found" || normalized == "none") return true
    if (normalized.contains("permission denial")) return true
    return false
}

private fun formatEpochMillis(value: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return formatter.format(Date(value))
}

private fun tryFormatTimeValue(key: String, value: String): String {
    val k = key.lowercase()
    val likelyTime = k.contains("time") || k.contains("timestamp") || k.contains("build_time")
    if (!likelyTime) return value

    val num = value.toLongOrNull() ?: return value
    val millis = if (num in 1..99_999_999_999L) num * 1000 else num
    if (millis <= 0L) return value
    return runCatching { formatEpochMillis(millis) }.getOrDefault(value)
}

private fun cleanRows(rows: List<InfoRow>): List<InfoRow> {
    val seen = LinkedHashSet<String>()
    return rows
        .map {
            val formatted = tryFormatTimeValue(it.key, it.value.trim())
            InfoRow(it.key.trim(), formatted.trim())
        }
        .filter { it.key.isNotBlank() && !isInvalidValue(it.value) }
        .filter { seen.add("${it.key}\u0000${it.value}") }
}

private fun execRuntimeCommand(command: String): String? {
    return runCatching {
        val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
        process.inputStream.bufferedReader().use { it.readText() }.trim()
    }.getOrNull()?.ifBlank { null }
}

private fun parseKeyValueLines(raw: String?): List<InfoRow> {
    if (raw.isNullOrBlank()) return emptyList()
    return raw.lineSequence()
        .map { it.trim() }
        .filter { it.isNotBlank() && it.contains("=") }
        .mapNotNull { line ->
            val index = line.indexOf('=')
            if (index <= 0) return@mapNotNull null
            InfoRow(
                key = line.substring(0, index).trim(),
                value = line.substring(index + 1).trim()
            )
        }
        .toList()
}

private fun commandRows(command: String, shizukuApiUtils: ShizukuApiUtils): List<InfoRow> {
    val shizuku = shizukuApiUtils.execCommand(command)
    val runtime = if (shizuku.isNullOrBlank()) execRuntimeCommand(command) else null
    return parseKeyValueLines(shizuku ?: runtime)
}

private fun decodeVulkanApiVersion(version: Int): String {
    if (version <= 0) return ""
    val major = version shr 22
    val minor = (version shr 12) and 0x3ff
    val patch = version and 0xfff
    return "$major.$minor.$patch"
}

private fun featureVersion(features: Array<FeatureInfo>?, featureName: String): Int {
    return features?.firstOrNull { it.name == featureName }?.version ?: 0
}

private fun boolFeatureRow(pm: PackageManager, featureName: String, label: String): InfoRow {
    return InfoRow(label, pm.hasSystemFeature(featureName).toString())
}

private fun graphicsRows(context: Context): List<InfoRow> {
    val pm = context.packageManager
    val features = pm.systemAvailableFeatures
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    val glEsVersion = activityManager?.deviceConfigurationInfo?.glEsVersion.orEmpty()

    val vulkanVersionEncoded = featureVersion(features, "android.hardware.vulkan.version")
    val vulkanLevel = featureVersion(features, "android.hardware.vulkan.level")
    val vulkanVersionText = decodeVulkanApiVersion(vulkanVersionEncoded)

    return listOf(
        InfoRow("graphics.opengl.es", glEsVersion),
        InfoRow("graphics.vulkan.version", vulkanVersionText),
        InfoRow("graphics.vulkan.level", if (vulkanLevel > 0) vulkanLevel.toString() else ""),
        boolFeatureRow(pm, "android.hardware.vulkan.version", "feature.vulkan.version"),
        boolFeatureRow(pm, "android.hardware.vulkan.level", "feature.vulkan.level"),
        boolFeatureRow(pm, "android.hardware.opengles.aep", "feature.opengl.aep"),
        boolFeatureRow(pm, PackageManager.FEATURE_OPENGLES_EXTENSION_PACK, "feature.opengles.extension_pack"),
        InfoRow("prop.ro.hardware.egl", execRuntimeCommand("getprop ro.hardware.egl").orEmpty()),
        InfoRow("prop.ro.hardware.vulkan", execRuntimeCommand("getprop ro.hardware.vulkan").orEmpty())
    )
}

private fun capabilityRows(context: Context): List<InfoRow> {
    val pm = context.packageManager
    return listOf(
        boolFeatureRow(pm, PackageManager.FEATURE_BLUETOOTH, "feature.bluetooth"),
        boolFeatureRow(pm, PackageManager.FEATURE_BLUETOOTH_LE, "feature.bluetooth_le"),
        boolFeatureRow(pm, PackageManager.FEATURE_WIFI, "feature.wifi"),
        boolFeatureRow(pm, PackageManager.FEATURE_WIFI_AWARE, "feature.wifi_aware"),
        boolFeatureRow(pm, PackageManager.FEATURE_NFC, "feature.nfc"),
        boolFeatureRow(pm, PackageManager.FEATURE_USB_HOST, "feature.usb_host"),
        boolFeatureRow(pm, PackageManager.FEATURE_CAMERA, "feature.camera"),
        boolFeatureRow(pm, PackageManager.FEATURE_CAMERA_FRONT, "feature.camera_front"),
        boolFeatureRow(pm, PackageManager.FEATURE_CAMERA_FLASH, "feature.camera_flash"),
        boolFeatureRow(pm, PackageManager.FEATURE_SENSOR_ACCELEROMETER, "feature.sensor.accelerometer"),
        boolFeatureRow(pm, PackageManager.FEATURE_SENSOR_GYROSCOPE, "feature.sensor.gyroscope"),
        boolFeatureRow(pm, PackageManager.FEATURE_SENSOR_BAROMETER, "feature.sensor.barometer"),
        boolFeatureRow(pm, PackageManager.FEATURE_FINGERPRINT, "feature.fingerprint"),
        boolFeatureRow(pm, PackageManager.FEATURE_LOCATION_GPS, "feature.location.gps"),
        boolFeatureRow(pm, PackageManager.FEATURE_TELEPHONY, "feature.telephony")
    )
}

private fun buildSectionRows(
    section: SectionKind,
    context: Context,
    shizukuStatus: String,
    shizukuApiUtils: ShizukuApiUtils
): List<InfoRow> {
    return when (section) {
        SectionKind.SYSTEM -> cleanRows(commandRows("settings list system", shizukuApiUtils))
        SectionKind.SECURE -> cleanRows(commandRows("settings list secure", shizukuApiUtils))
        SectionKind.GLOBAL -> cleanRows(commandRows("settings list global", shizukuApiUtils))
        SectionKind.ANDROID -> cleanRows(
            graphicsRows(context) +
                capabilityRows(context) +
                getAllSystemProperties.toSortedMap().map { InfoRow(it.key, it.value) }
        )
        SectionKind.JAVA -> cleanRows(
            getAllJavaPropString.toSortedMap().map { InfoRow(it.key, it.value) }
        )
        SectionKind.LINUX -> {
            val runtimeUname = execRuntimeCommand("uname -a")
            val runtimeGetenforce = execRuntimeCommand("getenforce")
            val runtimeProcVersion = execRuntimeCommand("cat /proc/version")
            val runtimeToybox = execRuntimeCommand("toybox --version")

            val shizukuUname = shizukuApiUtils.execCommand("uname -a")
            val shizukuGetenforce = shizukuApiUtils.execCommand("getenforce")
            val shizukuProcVersion = shizukuApiUtils.execCommand("cat /proc/version")
            val shizukuToybox = shizukuApiUtils.execCommand("toybox --version")

            val envRows = System.getenv().toSortedMap().map { InfoRow("env.${it.key}", it.value) }
            cleanRows(
                listOf(
                    InfoRow("Shizuku Status", shizukuStatus),
                    InfoRow("uname-a", shizukuUname?.lineSequence()?.firstOrNull() ?: runtimeUname.orEmpty()),
                    InfoRow("getenforce", shizukuGetenforce?.lineSequence()?.firstOrNull() ?: runtimeGetenforce.orEmpty()),
                    InfoRow("proc.version", shizukuProcVersion?.lineSequence()?.firstOrNull() ?: runtimeProcVersion.orEmpty()),
                    InfoRow("toybox --version", shizukuToybox?.lineSequence()?.firstOrNull() ?: runtimeToybox.orEmpty())
                ) + envRows
            )
        }
    }
}

private fun removeTopInfoRows(section: SectionKind, rows: List<InfoRow>): List<InfoRow> {
    val keySet = when (section) {
        SectionKind.SYSTEM -> TopInfoKeys.system
        SectionKind.SECURE -> TopInfoKeys.secure
        SectionKind.GLOBAL -> TopInfoKeys.global
        SectionKind.JAVA -> TopInfoKeys.java
        SectionKind.LINUX -> TopInfoKeys.linux
        SectionKind.ANDROID -> emptySet()
    }
    return rows.filterNot { keySet.contains(it.key) }
}

private fun mapRows(rows: List<InfoRow>): Map<String, String> = rows.associate { it.key to it.value }

private fun buildTopInfoRows(
    systemRows: List<InfoRow>,
    secureRows: List<InfoRow>,
    globalRows: List<InfoRow>,
    javaRows: List<InfoRow>,
    linuxRows: List<InfoRow>
): List<InfoRow> {
    val systemMap = mapRows(systemRows)
    val secureMap = mapRows(secureRows)
    val globalMap = mapRows(globalRows)
    val javaMap = mapRows(javaRows)
    val linuxMap = mapRows(linuxRows)

    val rows = mutableListOf<InfoRow>()
    TopInfoKeys.system.forEach { key -> systemMap[key]?.let { rows += InfoRow("System.$key", it) } }
    TopInfoKeys.secure.forEach { key -> secureMap[key]?.let { rows += InfoRow("Secure.$key", it) } }
    TopInfoKeys.global.forEach { key -> globalMap[key]?.let { rows += InfoRow("Global.$key", it) } }
    TopInfoKeys.java.forEach { key -> javaMap[key]?.let { rows += InfoRow("Java.$key", it) } }
    TopInfoKeys.linux.forEach { key -> linuxMap[key]?.let { rows += InfoRow("Linux.$key", it) } }
    return cleanRows(rows)
}

@Composable
fun SystemPage(
    backdrop: Backdrop?,
    scrollToTopSignal: Int,
    shizukuStatus: String,
    shizukuApiUtils: ShizukuApiUtils,
    contentBottomPadding: Dp = 72.dp
) {
    val context = LocalContext.current
    val shizukuReady = shizukuStatus.contains("granted", ignoreCase = true)
    val cached = remember { SystemInfoCache.read() }
    var query by remember { mutableStateOf("") }
    var topInfoExpanded by remember { mutableStateOf(true) }
    var systemTableExpanded by remember { mutableStateOf(true) }
    var secureTableExpanded by remember { mutableStateOf(false) }
    var globalTableExpanded by remember { mutableStateOf(false) }
    var androidPropsExpanded by remember { mutableStateOf(false) }
    var javaPropsExpanded by remember { mutableStateOf(false) }
    var linuxEnvExpanded by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    var sectionStates by remember {
        mutableStateOf(
            mapOf(
                SectionKind.SYSTEM to SectionState(rows = cached.system),
                SectionKind.SECURE to SectionState(rows = cached.secure),
                SectionKind.GLOBAL to SectionState(rows = cached.global),
                SectionKind.ANDROID to SectionState(rows = cached.android),
                SectionKind.JAVA to SectionState(rows = cached.java),
                SectionKind.LINUX to SectionState(rows = cached.linux)
            )
        )
    }

    fun updateSection(section: SectionKind, transform: (SectionState) -> SectionState) {
        sectionStates = sectionStates.toMutableMap().also { map ->
            val old = map[section] ?: SectionState()
            map[section] = transform(old)
        }
    }

    suspend fun ensureLoad(section: SectionKind) {
        val current = sectionStates[section] ?: SectionState()
        if (current.loading || current.loadedFresh) return
        updateSection(section) { it.copy(loading = true) }
        val fresh = withContext(Dispatchers.IO) {
            buildSectionRows(section, context, shizukuStatus, shizukuApiUtils)
        }
        updateSection(section) { it.copy(rows = fresh, loading = false, loadedFresh = true) }
        withContext(Dispatchers.IO) {
            SystemInfoCache.write(section, fresh)
        }
    }

    LaunchedEffect(scrollToTopSignal) {
        if (scrollToTopSignal > 0) scrollState.animateScrollTo(0)
    }

    LaunchedEffect(shizukuReady) {
        updateSection(SectionKind.SYSTEM) { it.copy(loadedFresh = false) }
        updateSection(SectionKind.SECURE) { it.copy(loadedFresh = false) }
        updateSection(SectionKind.GLOBAL) { it.copy(loadedFresh = false) }
        updateSection(SectionKind.LINUX) { it.copy(loadedFresh = false) }
    }

    LaunchedEffect(systemTableExpanded) {
        if (systemTableExpanded) ensureLoad(SectionKind.SYSTEM)
    }
    LaunchedEffect(secureTableExpanded) {
        if (secureTableExpanded) ensureLoad(SectionKind.SECURE)
    }
    LaunchedEffect(globalTableExpanded) {
        if (globalTableExpanded) ensureLoad(SectionKind.GLOBAL)
    }
    LaunchedEffect(androidPropsExpanded) {
        if (androidPropsExpanded) ensureLoad(SectionKind.ANDROID)
    }
    LaunchedEffect(javaPropsExpanded) {
        if (javaPropsExpanded) ensureLoad(SectionKind.JAVA)
    }
    LaunchedEffect(linuxEnvExpanded) {
        if (linuxEnvExpanded) ensureLoad(SectionKind.LINUX)
    }

    val systemRows = sectionStates[SectionKind.SYSTEM]?.rows ?: emptyList()
    val secureRows = sectionStates[SectionKind.SECURE]?.rows ?: emptyList()
    val globalRows = sectionStates[SectionKind.GLOBAL]?.rows ?: emptyList()
    val androidRows = sectionStates[SectionKind.ANDROID]?.rows ?: emptyList()
    val javaRows = sectionStates[SectionKind.JAVA]?.rows ?: emptyList()
    val linuxRows = sectionStates[SectionKind.LINUX]?.rows ?: emptyList()

    val topInfoRows = remember(systemRows, secureRows, globalRows, javaRows, linuxRows) {
        buildTopInfoRows(systemRows, secureRows, globalRows, javaRows, linuxRows)
    }

    val prunedSystemRows = remember(systemRows) { removeTopInfoRows(SectionKind.SYSTEM, systemRows) }
    val prunedSecureRows = remember(secureRows) { removeTopInfoRows(SectionKind.SECURE, secureRows) }
    val prunedGlobalRows = remember(globalRows) { removeTopInfoRows(SectionKind.GLOBAL, globalRows) }
    val prunedJavaRows = remember(javaRows) { removeTopInfoRows(SectionKind.JAVA, javaRows) }
    val prunedLinuxRows = remember(linuxRows) { removeTopInfoRows(SectionKind.LINUX, linuxRows) }

    val q = query.trim()
    val filteredTopInfoRows = remember(q, topInfoRows) { filterRows(topInfoRows, q) }
    val filteredSystemRows = remember(q, prunedSystemRows) { filterRows(prunedSystemRows, q) }
    val filteredSecureRows = remember(q, prunedSecureRows) { filterRows(prunedSecureRows, q) }
    val filteredGlobalRows = remember(q, prunedGlobalRows) { filterRows(prunedGlobalRows, q) }
    val filteredAndroidRows = remember(q, androidRows) { filterRows(androidRows, q) }
    val filteredJavaRows = remember(q, prunedJavaRows) { filterRows(prunedJavaRows, q) }
    val filteredLinuxRows = remember(q, prunedLinuxRows) { filterRows(prunedLinuxRows, q) }

    fun sectionSubtitle(section: SectionKind, size: Int): String {
        val state = sectionStates[section] ?: SectionState()
        return when {
            state.loading -> "加载中..."
            !state.loadedFresh && state.rows.isNotEmpty() -> "$size 条（缓存）"
            !state.loadedFresh && state.rows.isEmpty() -> "未加载"
            else -> "$size 条"
        }
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(bottom = contentBottomPadding)
        ) {
            Text(text = "System", modifier = Modifier.padding(top = 6.dp))
            Text(text = "系统参数与属性", modifier = Modifier.padding(top = 4.dp))
            Spacer(modifier = Modifier.height(10.dp))
            TextField(
                value = query,
                onValueChange = { query = it },
                label = "搜索系统参数",
                useLabelAsPlaceholder = true,
                singleLine = true
            )
            Spacer(modifier = Modifier.height(14.dp))

            MiuixExpandableSection(
                backdrop = backdrop,
                title = "TopInfo",
                subtitle = "${filteredTopInfoRows.size} 条",
                expanded = topInfoExpanded,
                onExpandedChange = { topInfoExpanded = it }
            ) {
                if (filteredTopInfoRows.isEmpty()) Text(text = "No matched results.")
                else filteredTopInfoRows.forEach { row -> MiuixInfoItem(row.key, row.value) }
            }

            Spacer(modifier = Modifier.height(12.dp))

            MiuixExpandableSection(
                backdrop = backdrop,
                title = "System Table",
                subtitle = sectionSubtitle(SectionKind.SYSTEM, filteredSystemRows.size),
                expanded = systemTableExpanded,
                onExpandedChange = { systemTableExpanded = it }
            ) {
                if (filteredSystemRows.isEmpty()) Text(text = "No matched results.")
                else filteredSystemRows.forEach { row -> MiuixInfoItem(row.key, row.value) }
            }

            Spacer(modifier = Modifier.height(12.dp))

            MiuixExpandableSection(
                backdrop = backdrop,
                title = "Secure Table",
                subtitle = sectionSubtitle(SectionKind.SECURE, filteredSecureRows.size),
                expanded = secureTableExpanded,
                onExpandedChange = { secureTableExpanded = it }
            ) {
                if (filteredSecureRows.isEmpty()) Text(text = "No matched results.")
                else filteredSecureRows.forEach { row -> MiuixInfoItem(row.key, row.value) }
            }

            Spacer(modifier = Modifier.height(12.dp))

            MiuixExpandableSection(
                backdrop = backdrop,
                title = "Global Table",
                subtitle = sectionSubtitle(SectionKind.GLOBAL, filteredGlobalRows.size),
                expanded = globalTableExpanded,
                onExpandedChange = { globalTableExpanded = it }
            ) {
                if (filteredGlobalRows.isEmpty()) Text(text = "No matched results.")
                else filteredGlobalRows.forEach { row -> MiuixInfoItem(row.key, row.value) }
            }

            Spacer(modifier = Modifier.height(12.dp))

            MiuixExpandableSection(
                backdrop = backdrop,
                title = "Android Properties",
                subtitle = sectionSubtitle(SectionKind.ANDROID, filteredAndroidRows.size),
                expanded = androidPropsExpanded,
                onExpandedChange = { androidPropsExpanded = it }
            ) {
                if (filteredAndroidRows.isEmpty()) Text(text = "No matched results.")
                else filteredAndroidRows.forEach { row -> MiuixInfoItem(row.key, row.value) }
            }

            Spacer(modifier = Modifier.height(12.dp))

            MiuixExpandableSection(
                backdrop = backdrop,
                title = "Java Properties",
                subtitle = sectionSubtitle(SectionKind.JAVA, filteredJavaRows.size),
                expanded = javaPropsExpanded,
                onExpandedChange = { javaPropsExpanded = it }
            ) {
                if (filteredJavaRows.isEmpty()) Text(text = "No matched results.")
                else filteredJavaRows.forEach { row -> MiuixInfoItem(row.key, row.value) }
            }

            Spacer(modifier = Modifier.height(12.dp))

            MiuixExpandableSection(
                backdrop = backdrop,
                title = "Linux environment",
                subtitle = sectionSubtitle(SectionKind.LINUX, filteredLinuxRows.size),
                expanded = linuxEnvExpanded,
                onExpandedChange = { linuxEnvExpanded = it }
            ) {
                if (filteredLinuxRows.isEmpty()) Text(text = "No matched results.")
                else filteredLinuxRows.forEach { row -> MiuixInfoItem(row.key, row.value) }
            }
        }
    }
}
