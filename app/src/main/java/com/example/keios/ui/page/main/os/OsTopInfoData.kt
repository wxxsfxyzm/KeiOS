package com.example.keios.ui.page.main

internal data class TopInfoTopic(
    val order: Int,
    val title: String
)

internal fun topInfoTopicOf(key: String): TopInfoTopic {
    val k = key.lowercase()
    return when {
        k.startsWith("long_press_") -> TopInfoTopic(0, "按键长按")
        k.contains("fbo") || k == "key_fbo_data" -> TopInfoTopic(1, "FBO")
        k.contains("dex2oat") || k.contains("dexopt") -> TopInfoTopic(2, "Dex 优化")
        k.startsWith("tango.") || k.contains("tango") -> TopInfoTopic(3, "Tango")
        k.contains("aod") -> TopInfoTopic(4, "AOD")
        k.contains("zygote") -> TopInfoTopic(5, "Zygote")
        k.contains("density") -> TopInfoTopic(6, "显示密度")
        k.contains("autofill") || k.contains("credential") -> TopInfoTopic(7, "自动填充与凭据")
        k.startsWith("gsm.") || k.contains("gsm") -> TopInfoTopic(8, "GSM / 蜂窝网络")
        k.contains("level") -> TopInfoTopic(9, "Level / 等级")
        k.startsWith("adb_") || k.contains("adb") -> TopInfoTopic(10, "ADB / 调试")
        k.startsWith("voice_") || k.contains("assistant") || k.contains("recognition") -> TopInfoTopic(11, "语音与助手")
        k.startsWith("share_") -> TopInfoTopic(12, "共享相关")
        k.contains("bluetooth") || k.contains("bt_") || k.contains("lc3") || k.contains("lea_") -> TopInfoTopic(13, "蓝牙音频")
        k.contains("usb") -> TopInfoTopic(14, "USB")
        k.contains("vulkan") || k.contains("opengl") || k.contains("egl") || k.contains("graphics") || k.contains("hwui") -> TopInfoTopic(15, "图形渲染")
        k.startsWith("miui_") || k.startsWith("ro.miui") || k.startsWith("ro.mi.") || k.contains("xiaomi") -> TopInfoTopic(16, "MIUI / Xiaomi")
        k.contains("version") || k.contains("build") || k.contains("fingerprint") || k.contains("security_patch") -> TopInfoTopic(17, "版本与构建")
        k.contains("time") || k.contains("timestamp") -> TopInfoTopic(18, "时间戳")
        k.startsWith("java.") || k.startsWith("android.") || k.startsWith("os.") || k.startsWith("user.") -> TopInfoTopic(19, "Java / 系统属性")
        k.startsWith("env.") || k == "uname-a" || k == "proc.version" || k == "toybox --version" || k == "getenforce" -> TopInfoTopic(20, "Linux 环境")
        else -> TopInfoTopic(99, "其他")
    }
}

internal fun sortTopInfoRows(rows: List<InfoRow>): List<InfoRow> {
    return rows.sortedWith(
        compareBy<InfoRow>(
            { topInfoTopicOf(it.key).order },
            { detectValueType(it.value).rank },
            { it.key.lowercase() }
        )
    )
}

internal fun groupTopInfoRows(rows: List<InfoRow>): List<Pair<String, List<InfoRow>>> {
    val grouped = rows.groupBy { topInfoTopicOf(it.key) }
    return grouped.entries
        .sortedBy { it.key.order }
        .map { entry -> entry.key.title to entry.value }
}

internal fun removeTopInfoRows(section: SectionKind, rows: List<InfoRow>): List<InfoRow> {
    val keySet = when (section) {
        SectionKind.SYSTEM -> TopInfoKeys.system
        SectionKind.SECURE -> TopInfoKeys.secure
        SectionKind.GLOBAL -> TopInfoKeys.global
        SectionKind.ANDROID -> TopInfoKeys.android
        SectionKind.JAVA -> TopInfoKeys.java
        SectionKind.LINUX -> TopInfoKeys.linux
    }
    return rows.filterNot { keySet.contains(it.key) }
}

private fun mapRows(rows: List<InfoRow>): Map<String, String> = rows.associate { it.key to it.value }

internal fun buildTopInfoRows(
    systemRows: List<InfoRow>,
    secureRows: List<InfoRow>,
    globalRows: List<InfoRow>,
    androidRows: List<InfoRow>,
    javaRows: List<InfoRow>,
    linuxRows: List<InfoRow>
): List<InfoRow> {
    val systemMap = mapRows(systemRows)
    val secureMap = mapRows(secureRows)
    val globalMap = mapRows(globalRows)
    val androidMap = mapRows(androidRows)
    val javaMap = mapRows(javaRows)
    val linuxMap = mapRows(linuxRows)

    val rows = mutableListOf<InfoRow>()
    TopInfoKeys.system.forEach { key -> systemMap[key]?.let { rows += InfoRow(key, it) } }
    TopInfoKeys.secure.forEach { key -> secureMap[key]?.let { rows += InfoRow(key, it) } }
    TopInfoKeys.global.forEach { key -> globalMap[key]?.let { rows += InfoRow(key, it) } }
    TopInfoKeys.android.forEach { key -> androidMap[key]?.let { rows += InfoRow(key, it) } }
    TopInfoKeys.java.forEach { key -> javaMap[key]?.let { rows += InfoRow(key, it) } }
    TopInfoKeys.linux.forEach { key -> linuxMap[key]?.let { rows += InfoRow(key, it) } }
    return sortTopInfoRows(cleanRows(rows))
}
