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

internal fun formatFutureEta(targetMs: Long?, nowMs: Long = System.currentTimeMillis()): String {
    if (targetMs == null || targetMs <= 0L) return "未知"
    val deltaMs = (targetMs - nowMs).coerceAtLeast(0L)
    val minutes = deltaMs / 60_000L
    if (minutes <= 0L) return "即将恢复"
    if (minutes < 60L) return "$minutes 分钟后"
    val hours = minutes / 60L
    val mins = minutes % 60L
    return if (mins == 0L) "$hours 小时后" else "$hours 小时 $mins 分钟后"
}

internal fun strategyLabelForId(id: String): String {
    return GitHubLookupStrategyOption.fromStorageId(id).label
}

internal fun GitHubLookupStrategyOption.overviewLabel(): String {
    return when (this) {
        GitHubLookupStrategyOption.AtomFeed -> "Atom"
        GitHubLookupStrategyOption.GitHubApiToken -> "API"
    }
}

internal fun GitHubLookupConfig.overviewApiLabel(): String {
    return when {
        selectedStrategy != GitHubLookupStrategyOption.GitHubApiToken -> "未使用"
        apiToken.isBlank() -> "游客"
        else -> apiToken.maskedApiPreview()
    }
}

private fun String.maskedApiPreview(): String {
    val token = trim()
    if (token.isBlank()) return "游客"

    val prefix = when {
        token.startsWith("github_pat_") -> "PAT"
        token.startsWith("ghp_") -> "GHP"
        token.startsWith("gho_") -> "GHO"
        token.startsWith("ghu_") -> "GHU"
        token.startsWith("ghs_") -> "GHS"
        token.startsWith("ghr_") -> "GHR"
        else -> "KEY"
    }
    val body = token.substringAfterLast('_', token)
    val head = when {
        body.length >= 10 -> body.take(4)
        body.length >= 6 -> body.take(3)
        else -> body.take(2)
    }
    val tail = when {
        body.length >= 10 -> body.takeLast(4)
        body.length >= 6 -> body.takeLast(3)
        else -> body.takeLast(2)
    }
    return if (body.length <= 4) {
        "$prefix $body"
    } else {
        "$prefix $head…$tail"
    }
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
            "空 token 时也可先走游客 API，适合少量追踪快速体验。"
        ),
        cons = listOf(
            "游客 API 额度很低，追踪项目较多时更容易遇到限流。",
            "token 失效、权限不足或被撤销时会退回错误或访问受限。"
        ),
        requirement = "token 选填；未填写时自动使用游客 API。"
    )
)
