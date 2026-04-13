package com.example.keios.ui.page.main

import com.example.keios.feature.github.model.GitHubLookupConfig
import com.example.keios.feature.github.model.GitHubLookupStrategyOption
import kotlin.math.max

internal data class VersionCheckUi(
    val loading: Boolean = false,
    val localVersion: String = "",
    val localVersionCode: Long = -1L,
    val latestTag: String = "",
    val latestStableRawTag: String = "",
    val latestStableUrl: String = "",
    val latestPreRawTag: String = "",
    val latestPreUrl: String = "",
    val hasUpdate: Boolean? = null,
    val message: String = "",
    val isPreRelease: Boolean = false,
    val preReleaseInfo: String = "",
    val showPreReleaseInfo: Boolean = false,
    val hasPreReleaseUpdate: Boolean = false,
    val sourceStrategyId: String = ""
)

internal data class GitHubStrategyGuide(
    val option: GitHubLookupStrategyOption,
    val summary: String,
    val pros: List<String>,
    val cons: List<String>,
    val requirement: String
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

internal fun GitHubLookupConfig.tokenStatusLabel(): String {
    return when {
        selectedStrategy.requiresToken && apiToken.isBlank() -> "未填写"
        selectedStrategy.requiresToken -> "已填写"
        else -> "无需 Token"
    }
}

internal fun strategyLabelForId(id: String): String {
    return GitHubLookupStrategyOption.fromStorageId(id).label
}

internal val githubStrategyGuides: List<GitHubStrategyGuide> = listOf(
    GitHubStrategyGuide(
        option = GitHubLookupStrategyOption.AtomFeed,
        summary = "通过仓库 releases.atom 和 latest 跳转推断稳定版/预发行。",
        pros = listOf(
            "无需 Token，开箱即用，适合公开仓库。",
            "配置最少，切换成本低。"
        ),
        cons = listOf(
            "依赖 Atom feed 和页面跳转，结构变化时更容易受影响。",
            "对私有仓库和更精细的 release 元数据支持较弱。"
        ),
        requirement = "无需额外凭证。"
    ),
    GitHubStrategyGuide(
        option = GitHubLookupStrategyOption.GitHubApiToken,
        summary = "通过 GitHub Releases API 直接读取 release 列表和 prerelease 标记。",
        pros = listOf(
            "数据结构更稳定，能直接识别 GitHub 的 prerelease 标记。",
            "更适合高频检查，也更容易覆盖私有仓库场景。"
        ),
        cons = listOf(
            "必须由用户提供 GitHub API token，并自行维护有效期与权限。",
            "token 失效、权限不足或被撤销时会直接导致检查失败。"
        ),
        requirement = "选择此方案时需要填写 GitHub API token。"
    )
)
