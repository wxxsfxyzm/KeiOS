package com.example.keios.ui.page.main

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ProviderInfo
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow.Companion.Clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.keios.BuildConfig
import com.example.keios.core.system.ShizukuApiUtils
import com.example.keios.ui.page.main.widget.StatusPill
import com.example.keios.ui.page.main.widget.StatusLabelText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Album
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.ExpandLess
import top.yukonga.miuix.kmp.icon.extended.ExpandMore
import top.yukonga.miuix.kmp.icon.extended.Filter
import top.yukonga.miuix.kmp.icon.extended.GridView
import top.yukonga.miuix.kmp.icon.extended.Info
import top.yukonga.miuix.kmp.icon.extended.Layers
import top.yukonga.miuix.kmp.icon.extended.ListView
import top.yukonga.miuix.kmp.icon.extended.Lock
import top.yukonga.miuix.kmp.icon.extended.Notes
import top.yukonga.miuix.kmp.icon.extended.Ok
import top.yukonga.miuix.kmp.icon.extended.Refresh
import top.yukonga.miuix.kmp.icon.extended.Report
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.icon.extended.Timer
import top.yukonga.miuix.kmp.icon.extended.Tune
import top.yukonga.miuix.kmp.icon.extended.Update
import top.yukonga.miuix.kmp.theme.MiuixTheme

private const val KEIOS_PROJECT_URL = "https://github.com/hosizoraru/KeiOS"

private data class PermissionExplain(
    val title: String,
    val purpose: String,
    val usedIn: String
)

private data class AboutPermissionEntry(
    val name: String,
    val title: String,
    val granted: Boolean,
    val purpose: String,
    val usedIn: String
)

private data class AboutComponentEntry(
    val type: String,
    val name: String,
    val exported: Boolean,
    val purpose: String,
    val usedIn: String,
    val extra: List<Pair<String, String>> = emptyList()
)

private val permissionExplainMap = mapOf(
    "android.permission.QUERY_ALL_PACKAGES" to PermissionExplain(
        title = "读取应用列表",
        purpose = "查询本机已安装应用包名",
        usedIn = "GitHub 页面新增跟踪时，筛选并选择本机 App"
    ),
    "android.permission.INTERNET" to PermissionExplain(
        title = "网络访问",
        purpose = "允许发起 HTTP/HTTPS 网络请求",
        usedIn = "GitHub 版本检查、BA 数据抓取、MCP 对外通信"
    ),
    "android.permission.ACCESS_NETWORK_STATE" to PermissionExplain(
        title = "网络状态",
        purpose = "读取当前网络连接状态",
        usedIn = "请求/通知链路状态判断与容错"
    ),
    "android.permission.POST_NOTIFICATIONS" to PermissionExplain(
        title = "通知权限",
        purpose = "显示系统通知、实时动态与超级岛通知",
        usedIn = "MCP 常驻通知、BA AP 通知、GitHub 刷新通知"
    ),
    "android.permission.POST_PROMOTED_NOTIFICATIONS" to PermissionExplain(
        title = "推广通知能力",
        purpose = "扩展通知展示能力（系统支持时）",
        usedIn = "Live Update / 超级岛样式通知适配"
    ),
    "android.permission.FOREGROUND_SERVICE" to PermissionExplain(
        title = "前台服务",
        purpose = "允许启动前台服务并持续运行",
        usedIn = "MCP KeepAlive 保活与通知心跳"
    ),
    "android.permission.FOREGROUND_SERVICE_SPECIAL_USE" to PermissionExplain(
        title = "特殊用途前台服务",
        purpose = "允许以 specialUse 类型运行前台服务",
        usedIn = "HyperOS 实时动态 / 超级岛通知保活场景"
    )
)

private val componentExplainMap = mapOf(
    "com.example.keios.mcp.McpKeepAliveService" to PermissionExplain(
        title = "MCP 常驻服务",
        purpose = "维持 MCP 服务状态并定时刷新前台通知",
        usedIn = "MCP 页面启动/停止服务；BA AP 通知联动"
    ),
    "com.example.keios.feature.notification.NotificationActionReceiver" to PermissionExplain(
        title = "通知动作接收器",
        purpose = "处理通知按钮动作（如已读）",
        usedIn = "GitHub 刷新通知的“已读”动作"
    ),
    "rikka.shizuku.ShizukuProvider" to PermissionExplain(
        title = "Shizuku Provider",
        purpose = "接入 Shizuku 权限桥接能力",
        usedIn = "Shizuku 状态检查与能力调用"
    )
)

private fun loadPackageDetailInfo(context: Context): PackageInfo? {
    val flags = PackageManager.GET_PERMISSIONS or
        PackageManager.GET_SERVICES or
        PackageManager.GET_RECEIVERS or
        PackageManager.GET_PROVIDERS
    return runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.PackageInfoFlags.of(flags.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, flags)
        }
    }.getOrNull()
}

private fun buildPermissionEntries(
    packageInfo: PackageInfo?,
    notificationPermissionGranted: Boolean
): List<AboutPermissionEntry> {
    val names = packageInfo?.requestedPermissions?.toList().orEmpty()
    val flags = packageInfo?.requestedPermissionsFlags
    if (names.isEmpty()) return emptyList()
    return names.mapIndexed { index, permissionName ->
        val explain = permissionExplainMap[permissionName] ?: PermissionExplain(
            title = permissionName.substringAfterLast('.'),
            purpose = "系统权限",
            usedIn = "当前版本未标注具体用途"
        )
        val flagGranted = flags?.getOrNull(index)?.let {
            (it and PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0
        } ?: true
        val granted = if (permissionName == android.Manifest.permission.POST_NOTIFICATIONS) {
            notificationPermissionGranted
        } else {
            flagGranted
        }
        AboutPermissionEntry(
            name = permissionName,
            title = explain.title,
            granted = granted,
            purpose = explain.purpose,
            usedIn = explain.usedIn
        )
    }
}

private fun formatFgsType(serviceInfo: ServiceInfo): String {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return "N/A"
    val type = serviceInfo.foregroundServiceType
    if (type == 0) return "none"
    val labels = buildList {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
            (type and ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE) != 0
        ) {
            add("specialUse")
        }
        if ((type and ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC) != 0) add("dataSync")
        if ((type and ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK) != 0) add("mediaPlayback")
        if ((type and ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL) != 0) add("phoneCall")
        if ((type and ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION) != 0) add("location")
        if ((type and ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE) != 0) add("connectedDevice")
        if ((type and ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION) != 0) add("mediaProjection")
        if ((type and ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA) != 0) add("camera")
        if ((type and ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE) != 0) add("microphone")
        if ((type and ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH) != 0) add("health")
        if ((type and ServiceInfo.FOREGROUND_SERVICE_TYPE_REMOTE_MESSAGING) != 0) add("remoteMessaging")
        if ((type and ServiceInfo.FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED) != 0) add("systemExempted")
    }
    return labels.joinToString(" | ").ifBlank { type.toString() }
}

private fun buildComponentEntries(packageInfo: PackageInfo?): List<AboutComponentEntry> {
    val services = packageInfo?.services.orEmpty().map { service ->
        val explain = componentExplainMap[service.name] ?: PermissionExplain(
            title = service.name.substringAfterLast('.'),
            purpose = "应用内部服务组件",
            usedIn = "当前版本未标注具体用途"
        )
        AboutComponentEntry(
            type = "Service",
            name = explain.title,
            exported = service.exported,
            purpose = explain.purpose,
            usedIn = explain.usedIn,
            extra = listOf(
                "Class" to service.name,
                "FGS Type" to formatFgsType(service)
            )
        )
    }
    val receivers = packageInfo?.receivers.orEmpty().map { receiver ->
        val explain = componentExplainMap[receiver.name] ?: PermissionExplain(
            title = receiver.name.substringAfterLast('.'),
            purpose = "应用广播接收组件",
            usedIn = "当前版本未标注具体用途"
        )
        AboutComponentEntry(
            type = "Receiver",
            name = explain.title,
            exported = receiver.exported,
            purpose = explain.purpose,
            usedIn = explain.usedIn,
            extra = listOf("Class" to receiver.name)
        )
    }
    val providers = packageInfo?.providers.orEmpty().map { provider ->
        val explain = componentExplainMap[provider.name] ?: PermissionExplain(
            title = provider.name.substringAfterLast('.'),
            purpose = "应用内容提供组件",
            usedIn = "当前版本未标注具体用途"
        )
        AboutComponentEntry(
            type = "Provider",
            name = explain.title,
            exported = provider.exported,
            purpose = explain.purpose,
            usedIn = explain.usedIn,
            extra = listOf(
                "Class" to provider.name,
                "Authority" to provider.authority.orEmpty().ifBlank { "N/A" }
            )
        )
    }
    return services + receivers + providers
}

private fun formatTime(epochMillis: Long): String {
    if (epochMillis <= 0L) return "N/A"
    return runCatching {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        formatter.format(Date(epochMillis))
    }.getOrDefault("N/A")
}

private fun openExternalUrl(context: Context, url: String): Boolean {
    return runCatching {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        true
    }.getOrDefault(false)
}

@Composable
private fun AboutCompactRow(
    title: String,
    modifier: Modifier = Modifier,
    titleIcon: ImageVector? = null,
    onClick: (() -> Unit)? = null,
    valueContent: @Composable RowScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(clickableModifier)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier.width(104.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (titleIcon != null) {
                    Icon(
                        imageVector = titleIcon,
                        contentDescription = null,
                        tint = MiuixTheme.colorScheme.onBackgroundVariant
                    )
                }
                Text(
                    text = title,
                    color = MiuixTheme.colorScheme.onBackgroundVariant,
                    maxLines = Int.MAX_VALUE,
                    overflow = Clip
                )
            }
        }
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
            content = valueContent
        )
    }
}

@Composable
private fun AboutCompactInfoRow(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    titleIcon: ImageVector? = null,
    valueColor: Color = MiuixTheme.colorScheme.onBackground,
    onClick: (() -> Unit)? = null
) {
    AboutCompactRow(
        title = title,
        modifier = modifier,
        titleIcon = titleIcon,
        onClick = onClick
    ) {
        Text(
            text = value,
            color = valueColor,
            maxLines = Int.MAX_VALUE,
            overflow = Clip,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun AboutCompactPillRow(
    title: String,
    label: String,
    color: Color,
    titleIcon: ImageVector? = null,
    onClick: (() -> Unit)? = null
) {
    AboutCompactRow(
        title = title,
        titleIcon = titleIcon,
        onClick = onClick
    ) {
        StatusPill(
            label = label,
            color = color
        )
    }
}

@Composable
private fun AboutSectionCard(
    cardColor: Color,
    title: String,
    subtitle: String,
    titleColor: Color,
    subtitleColor: Color,
    sectionIcon: ImageVector? = null,
    collapsible: Boolean = false,
    expanded: Boolean = true,
    onExpandedChange: (Boolean) -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.defaultColors(
            color = cardColor,
            contentColor = MiuixTheme.colorScheme.onBackground
        ),
        onClick = {
            if (collapsible) {
                onExpandedChange(!expanded)
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (sectionIcon != null) {
                        Icon(
                            imageVector = sectionIcon,
                            contentDescription = null,
                            tint = titleColor
                        )
                    }
                    Text(
                        text = title,
                        color = titleColor
                    )
                }
                if (collapsible) {
                    Icon(
                        imageVector = if (expanded) MiuixIcons.Regular.ExpandLess else MiuixIcons.Regular.ExpandMore,
                        contentDescription = if (expanded) "收起" else "展开",
                        tint = titleColor
                    )
                }
            }
            Text(
                text = subtitle,
                color = subtitleColor
            )
            if (!collapsible || expanded) {
                content()
            }
        }
    }
}

@Composable
private fun AboutPermissionEntryView(
    entry: AboutPermissionEntry,
    accent: Color,
    grantedColor: Color,
    deniedColor: Color
) {
    val statusColor = if (entry.granted) grantedColor else deniedColor
    val statusLabel = if (entry.granted) StatusLabelText.Authorized else StatusLabelText.Unauthorized
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        AboutCompactInfoRow(
            title = "权限",
            value = entry.title,
            titleIcon = MiuixIcons.Regular.Lock,
            valueColor = accent
        )
        AboutCompactPillRow(
            title = "授权",
            label = statusLabel,
            titleIcon = if (entry.granted) MiuixIcons.Regular.Ok else MiuixIcons.Regular.Close,
            color = statusColor
        )
        AboutCompactInfoRow(
            title = "系统名",
            value = entry.name,
            titleIcon = MiuixIcons.Regular.Notes,
            valueColor = MiuixTheme.colorScheme.onBackgroundVariant
        )
        AboutCompactInfoRow(
            title = "功能",
            value = entry.purpose,
            titleIcon = MiuixIcons.Regular.Tune
        )
        AboutCompactInfoRow(
            title = "用到哪里",
            value = entry.usedIn,
            titleIcon = MiuixIcons.Regular.Layers
        )
    }
}

private fun componentTypeIcon(type: String): ImageVector {
    return when (type.lowercase(Locale.ROOT)) {
        "service" -> MiuixIcons.Regular.Settings
        "receiver" -> MiuixIcons.Regular.Refresh
        "provider" -> MiuixIcons.Regular.GridView
        else -> MiuixIcons.Regular.ListView
    }
}

private fun componentExtraIcon(label: String): ImageVector {
    return when (label.lowercase(Locale.ROOT)) {
        "class" -> MiuixIcons.Regular.Notes
        "fgs type" -> MiuixIcons.Regular.Filter
        "authority" -> MiuixIcons.Regular.Info
        else -> MiuixIcons.Regular.Info
    }
}

private fun buildRowIcon(label: String): ImageVector {
    return when (label) {
        "Kotlin", "Gradle", "Java", "JVM Target" -> MiuixIcons.Regular.Tune
        "Compile SDK", "Min SDK", "Target SDK", "Runtime API" -> MiuixIcons.Regular.Filter
        else -> MiuixIcons.Regular.Info
    }
}

private fun uiFrameworkRowIcon(label: String): ImageVector {
    return when (label) {
        "UI 框架" -> MiuixIcons.Regular.GridView
        "声明式界面" -> MiuixIcons.Regular.Layers
        "页面导航" -> MiuixIcons.Regular.ListView
        "玻璃材质" -> MiuixIcons.Regular.Album
        "权限桥接" -> MiuixIcons.Regular.Lock
        else -> MiuixIcons.Regular.Info
    }
}

private fun networkServiceRowIcon(label: String): ImageVector {
    return when (label) {
        "MCP SDK" -> MiuixIcons.Regular.Info
        "Ktor", "OkHttp" -> MiuixIcons.Regular.Settings
        else -> MiuixIcons.Regular.Info
    }
}

private fun mediaStorageRowIcon(label: String): ImageVector {
    return when (label) {
        "Media3" -> MiuixIcons.Regular.Album
        "ZoomImage" -> MiuixIcons.Regular.GridView
        "MMKV" -> MiuixIcons.Regular.Lock
        else -> MiuixIcons.Regular.Info
    }
}

@Composable
private fun AboutComponentEntryView(
    entry: AboutComponentEntry,
    accent: Color,
    exportedColor: Color,
    internalColor: Color
) {
    val exportColor = if (entry.exported) exportedColor else internalColor
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        AboutCompactInfoRow(
            title = entry.type,
            value = entry.name,
            titleIcon = componentTypeIcon(entry.type),
            valueColor = accent
        )
        AboutCompactPillRow(
            title = "暴露状态",
            label = if (entry.exported) "exported" else "internal",
            titleIcon = if (entry.exported) MiuixIcons.Regular.Report else MiuixIcons.Regular.Lock,
            color = exportColor
        )
        AboutCompactInfoRow(
            title = "功能",
            value = entry.purpose,
            titleIcon = MiuixIcons.Regular.Tune
        )
        AboutCompactInfoRow(
            title = "用到哪里",
            value = entry.usedIn,
            titleIcon = MiuixIcons.Regular.Layers
        )
        entry.extra.forEach { (k, v) ->
            AboutCompactInfoRow(
                title = k,
                value = v,
                titleIcon = componentExtraIcon(k)
            )
        }
    }
}

private fun normalizeLower(value: String): String = value.trim().lowercase(Locale.ROOT)

private fun selinuxStatusColor(selinuxState: String): Color {
    return when (normalizeLower(selinuxState)) {
        "enforcing" -> Color(0xFF2E7D32)
        "permissive" -> Color(0xFFB26A00)
        "disabled" -> Color(0xFFC62828)
        "n/a", "", "unknown" -> Color(0xFF6B7280)
        else -> Color(0xFF2563EB)
    }
}

private fun selinuxStatusLabel(selinuxState: String): String {
    return when (normalizeLower(selinuxState)) {
        "enforcing" -> "强制"
        "permissive" -> "宽容"
        "disabled" -> "关闭"
        "n/a", "", "unknown" -> "未知"
        else -> selinuxState.ifBlank { "未知" }
    }
}

@Composable
fun AboutPage(
    appLabel: String,
    packageInfo: PackageInfo?,
    notificationPermissionGranted: Boolean,
    shizukuStatus: String,
    shizukuApiUtils: ShizukuApiUtils,
    onCheckShizuku: () -> Unit,
    contentBottomPadding: Dp = 72.dp,
    scrollToTopSignal: Int = 0,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val accent = MiuixTheme.colorScheme.primary
    val subtitleColor = MiuixTheme.colorScheme.onBackgroundVariant
    val readyColor = Color(0xFF2E7D32)
    val notReadyColor = Color(0xFFC62828)
    val infoCardColor = Color(0x223B82F6)
    val buildCardColor = Color(0x223B82F6)
    val uiFrameworkCardColor = Color(0x2233A1F4)
    val networkServiceCardColor = Color(0x2222C55E)
    val mediaStorageCardColor = Color(0x2260A5FA)
    val listState = rememberLazyListState()
    val scrollBehavior = MiuixScrollBehavior()
    var buildExpanded by rememberSaveable { mutableStateOf(false) }
    var uiFrameworkExpanded by rememberSaveable { mutableStateOf(false) }
    var networkExpanded by rememberSaveable { mutableStateOf(false) }
    var mediaExpanded by rememberSaveable { mutableStateOf(false) }
    var permissionExpanded by rememberSaveable { mutableStateOf(false) }
    var componentExpanded by rememberSaveable { mutableStateOf(false) }
    var runtimeExpanded by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(scrollToTopSignal) {
        if (scrollToTopSignal > 0) listState.animateScrollToItem(0)
    }

    val packageDetailInfo = remember(context) {
        loadPackageDetailInfo(context)
    }
    val permissionEntries = remember(packageDetailInfo, notificationPermissionGranted) {
        buildPermissionEntries(packageDetailInfo, notificationPermissionGranted)
    }
    val componentEntries = remember(packageDetailInfo) {
        buildComponentEntries(packageDetailInfo)
    }

    val appInfo: ApplicationInfo? = packageInfo?.applicationInfo
    val shizukuDetailMap = remember(shizukuStatus) {
        shizukuApiUtils.detailedRows().toMap()
    }
    val buildRows = remember {
        listOf(
            "Kotlin" to KotlinVersion.CURRENT.toString(),
            "Gradle" to BuildConfig.GRADLE_VERSION,
            "Java" to BuildConfig.JAVA_VERSION,
            "JVM Target" to BuildConfig.JVM_TARGET_VERSION,
            "Compile SDK" to BuildConfig.COMPILE_SDK_VERSION.toString(),
            "Min SDK" to BuildConfig.MIN_SDK_VERSION.toString(),
            "Target SDK" to BuildConfig.TARGET_SDK_VERSION.toString(),
            "Runtime API" to Build.VERSION.SDK_INT.toString()
        )
    }
    val uiFrameworkRows = remember {
        listOf(
            "UI 框架" to "Miuix UI ${BuildConfig.MIUIX_VERSION}",
            "声明式界面" to "Jetpack Compose ${BuildConfig.COMPOSE_VERSION}",
            "页面导航" to "Navigation3 ${BuildConfig.NAVIGATION3_VERSION}",
            "玻璃材质" to "Backdrop ${BuildConfig.BACKDROP_VERSION} · Capsule ${BuildConfig.CAPSULE_VERSION}",
            "权限桥接" to "Shizuku API ${ShizukuApiUtils.API_VERSION}"
        )
    }
    val networkServiceRows = remember {
        listOf(
            "MCP SDK" to BuildConfig.MCP_KOTLIN_SDK_VERSION,
            "Ktor" to BuildConfig.KTOR_VERSION,
            "OkHttp" to BuildConfig.OKHTTP_VERSION
        )
    }
    val mediaStorageRows = remember {
        listOf(
            "Media3" to BuildConfig.MEDIA3_VERSION,
            "ZoomImage" to BuildConfig.ZOOMIMAGE_VERSION,
            "MMKV" to BuildConfig.MMKV_VERSION
        )
    }
    val shizukuReady = shizukuStatus.contains("granted", ignoreCase = true)
    val permissionCardColor = if (shizukuReady) Color(0x2222C55E) else Color(0x22EF4444)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = "About",
                scrollBehavior = scrollBehavior,
                color = Color.Transparent,
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = MiuixIcons.Regular.Back,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            )
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
                start = 16.dp,
                end = 16.dp
            )
        ) {
            item {
                SmallTitle("应用信息")
            }

            item { Spacer(modifier = Modifier.height(14.dp)) }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.defaultColors(
                        color = infoCardColor,
                        contentColor = MiuixTheme.colorScheme.onBackground
                    ),
                    onClick = {}
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "App",
                            color = accent
                        )
                        Text(
                            text = "应用身份与运行时信息",
                            color = subtitleColor
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                            AboutCompactInfoRow("名称", appLabel, titleIcon = MiuixIcons.Regular.Info)
                            AboutCompactInfoRow("包名", packageInfo?.packageName ?: "unknown", titleIcon = MiuixIcons.Regular.Notes)
                            AboutCompactInfoRow(
                                "版本",
                                "${packageInfo?.versionName ?: "unknown"} (${packageInfo?.longVersionCode ?: -1})",
                                titleIcon = MiuixIcons.Regular.Update
                            )
                            AboutCompactInfoRow(
                                "项目地址",
                                KEIOS_PROJECT_URL,
                                titleIcon = MiuixIcons.Regular.Layers,
                                valueColor = accent,
                                onClick = {
                                    if (!openExternalUrl(context, KEIOS_PROJECT_URL)) {
                                        Toast.makeText(context, "无法打开链接", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                            AboutCompactInfoRow(
                                "最后更新",
                                packageInfo?.lastUpdateTime?.let(::formatTime) ?: "unknown",
                                titleIcon = MiuixIcons.Regular.Timer
                            )
                            AboutCompactInfoRow(
                                "Debug",
                                (((appInfo?.flags ?: 0) and ApplicationInfo.FLAG_DEBUGGABLE) != 0).toString(),
                                titleIcon = MiuixIcons.Regular.Report
                            )
                            AboutCompactInfoRow(
                                "API Level",
                                Build.VERSION.SDK_INT.toString(),
                                titleIcon = MiuixIcons.Regular.Filter
                            )
                            AboutCompactInfoRow(
                                "安全补丁",
                                Build.VERSION.SECURITY_PATCH ?: "unknown",
                                titleIcon = MiuixIcons.Regular.Lock
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(14.dp)) }

            item {
                AboutSectionCard(
                    cardColor = buildCardColor,
                    title = "构建与SDK",
                    subtitle = "Kotlin / Gradle / Java 与构建目标",
                    titleColor = accent,
                    subtitleColor = subtitleColor,
                    sectionIcon = MiuixIcons.Regular.Tune,
                    collapsible = true,
                    expanded = buildExpanded,
                    onExpandedChange = { buildExpanded = it }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                        buildRows.forEach { (title, value) ->
                            AboutCompactInfoRow(title = title, value = value, titleIcon = buildRowIcon(title))
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(14.dp)) }

            item {
                AboutSectionCard(
                    cardColor = uiFrameworkCardColor,
                    title = "UI与框架",
                    subtitle = "页面渲染、导航与材质能力",
                    titleColor = accent,
                    subtitleColor = subtitleColor,
                    sectionIcon = MiuixIcons.Regular.GridView,
                    collapsible = true,
                    expanded = uiFrameworkExpanded,
                    onExpandedChange = { uiFrameworkExpanded = it }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                        uiFrameworkRows.forEach { (title, value) ->
                            AboutCompactInfoRow(title = title, value = value, titleIcon = uiFrameworkRowIcon(title))
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(14.dp)) }

            item {
                AboutSectionCard(
                    cardColor = networkServiceCardColor,
                    title = "网络与服务",
                    subtitle = "MCP与请求服务栈",
                    titleColor = readyColor,
                    subtitleColor = subtitleColor,
                    sectionIcon = MiuixIcons.Regular.Settings,
                    collapsible = true,
                    expanded = networkExpanded,
                    onExpandedChange = { networkExpanded = it }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                        networkServiceRows.forEach { (title, value) ->
                            AboutCompactInfoRow(title = title, value = value, titleIcon = networkServiceRowIcon(title))
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(14.dp)) }

            item {
                AboutSectionCard(
                    cardColor = mediaStorageCardColor,
                    title = "媒体与存储",
                    subtitle = "播放器、图像与本地存储能力",
                    titleColor = accent,
                    subtitleColor = subtitleColor,
                    sectionIcon = MiuixIcons.Regular.Album,
                    collapsible = true,
                    expanded = mediaExpanded,
                    onExpandedChange = { mediaExpanded = it }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                        mediaStorageRows.forEach { (title, value) ->
                            AboutCompactInfoRow(title = title, value = value, titleIcon = mediaStorageRowIcon(title))
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(14.dp)) }

            item {
                AboutSectionCard(
                    cardColor = permissionCardColor,
                    title = "运行时状态",
                    subtitle = "通知/Shizuku/系统状态快速检查",
                    titleColor = if (shizukuReady) readyColor else notReadyColor,
                    subtitleColor = subtitleColor,
                    sectionIcon = MiuixIcons.Regular.Info,
                    collapsible = true,
                    expanded = runtimeExpanded,
                    onExpandedChange = { runtimeExpanded = it }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 0.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AboutCompactPillRow(
                            "通知权限",
                            label = if (notificationPermissionGranted) StatusLabelText.Authorized else StatusLabelText.Unauthorized,
                            titleIcon = MiuixIcons.Regular.Report,
                            color = if (notificationPermissionGranted) readyColor else notReadyColor
                        )
                        AboutCompactPillRow(
                            "SELinux",
                            label = StatusLabelText.selinux(shizukuDetailMap["Shizuku getenforce"] ?: "N/A"),
                            titleIcon = MiuixIcons.Regular.Lock,
                            color = selinuxStatusColor(shizukuDetailMap["Shizuku getenforce"] ?: "N/A"),
                            onClick = onCheckShizuku
                        )
                        AboutCompactInfoRow(
                            "uname",
                            shizukuDetailMap["Shizuku uname"] ?: "N/A",
                            titleIcon = MiuixIcons.Regular.Notes,
                            valueColor = accent,
                            onClick = onCheckShizuku
                        )
                        AboutCompactInfoRow(
                            "权限总数",
                            permissionEntries.size.toString(),
                            titleIcon = MiuixIcons.Regular.ListView
                        )
                        AboutCompactInfoRow(
                            "服务组件总数",
                            componentEntries.size.toString(),
                            titleIcon = MiuixIcons.Regular.Layers
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(14.dp)) }

            item {
                AboutSectionCard(
                    cardColor = Color(0x2248A6FF),
                    title = "权限清单",
                    subtitle = "每个权限对应功能与具体使用位置",
                    titleColor = accent,
                    subtitleColor = subtitleColor,
                    sectionIcon = MiuixIcons.Regular.Lock,
                    collapsible = true,
                    expanded = permissionExpanded,
                    onExpandedChange = { permissionExpanded = it }
                ) {
                    if (permissionEntries.isEmpty()) {
                        AboutCompactInfoRow("状态", "未读取到权限信息")
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            permissionEntries.forEachIndexed { index, entry ->
                                AboutPermissionEntryView(
                                    entry = entry,
                                    accent = accent,
                                    grantedColor = readyColor,
                                    deniedColor = notReadyColor
                                )
                                if (index < permissionEntries.lastIndex) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(14.dp)) }

            item {
                AboutSectionCard(
                    cardColor = Color(0x2234D399),
                    title = "服务与组件",
                    subtitle = "Service / Receiver / Provider 与用途",
                    titleColor = readyColor,
                    subtitleColor = subtitleColor,
                    sectionIcon = MiuixIcons.Regular.ListView,
                    collapsible = true,
                    expanded = componentExpanded,
                    onExpandedChange = { componentExpanded = it }
                ) {
                    if (componentEntries.isEmpty()) {
                        AboutCompactInfoRow("状态", "未读取到服务组件信息")
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            componentEntries.forEachIndexed { index, entry ->
                                AboutComponentEntryView(
                                    entry = entry,
                                    accent = accent,
                                    exportedColor = Color(0xFFB26A00),
                                    internalColor = readyColor
                                )
                                if (index < componentEntries.lastIndex) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
