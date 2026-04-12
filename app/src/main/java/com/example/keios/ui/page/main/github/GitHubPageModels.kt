package com.example.keios.ui.page.main

import kotlin.math.max

internal data class VersionCheckUi(
    val loading: Boolean = false,
    val localVersion: String = "",
    val localVersionCode: Long = -1L,
    val latestTag: String = "",
    val latestStableRawTag: String = "",
    val latestPreRawTag: String = "",
    val hasUpdate: Boolean? = null,
    val message: String = "",
    val isPreRelease: Boolean = false,
    val preReleaseInfo: String = "",
    val showPreReleaseInfo: Boolean = false,
    val hasPreReleaseUpdate: Boolean = false
)

internal enum class GitHubSortMode(val label: String) {
    UpdateFirst("更新优先"),
    NameAsc("名称 A-Z"),
    PreReleaseFirst("预发行优先")
}

internal enum class OverviewRefreshState {
    Idle,
    Cached,
    Refreshing,
    Completed
}

internal enum class RefreshIntervalOption(val hours: Int, val label: String) {
    Hour1(1, "1 小时"),
    Hour3(3, "3 小时"),
    Hour6(6, "6 小时"),
    Hour12(12, "12 小时");

    companion object {
        fun fromHours(hours: Int): RefreshIntervalOption {
            return entries.firstOrNull { it.hours == hours } ?: Hour3
        }
    }
}

internal fun formatRefreshAgo(lastRefreshMs: Long, nowMs: Long = System.currentTimeMillis()): String {
    if (lastRefreshMs <= 0L) return "未刷新"
    val deltaMs = max(0L, nowMs - lastRefreshMs)
    val minutes = deltaMs / 60_000L
    if (minutes <= 0L) return "刚刚"
    if (minutes < 60L) return "$minutes 分钟前"
    val hours = minutes / 60L
    val mins = minutes % 60L
    return if (mins == 0L) "$hours 小时前" else "$hours 小时 $mins 分钟前"
}
