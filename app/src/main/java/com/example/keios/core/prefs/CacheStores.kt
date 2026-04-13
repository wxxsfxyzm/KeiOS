package com.example.keios.core.prefs

import android.content.Context
import com.example.keios.feature.github.data.local.AppIconCache
import com.example.keios.feature.github.data.local.GitHubTrackStore
import com.example.keios.feature.github.data.remote.GitHubReleaseStrategyRegistry
import com.example.keios.mcp.McpServerManager
import com.example.keios.ui.page.main.BASettingsStore
import com.example.keios.ui.page.main.OsCardVisibilityStore
import com.example.keios.ui.page.main.OsInfoCache
import com.example.keios.ui.page.main.OsSectionCard
import com.example.keios.ui.page.main.student.BaGuideTempMediaCache
import com.example.keios.ui.page.main.student.BaStudentGuideStore

internal data class CacheEntrySummary(
    val id: String,
    val title: String,
    val summary: String,
    val detail: String,
    val clearLabel: String
)

internal object CacheStores {
    fun list(context: Context): List<CacheEntrySummary> {
        return listOf(
            githubSummary(),
            baCalendarSummary(),
            baStudentGuideSummary(),
            osSummary(),
            appIconSummary(),
            baTempMediaSummary(context),
            mcpSummary()
        )
    }

    fun clear(context: Context, id: String) {
        when (id) {
            "github" -> {
                GitHubReleaseStrategyRegistry.clearAllCaches()
                GitHubTrackStore.clearCheckCache()
                AppIconCache.clear()
            }
            "ba_calendar" -> BASettingsStore.clearCalendarAndPoolCaches()
            "ba_student_guide" -> BaStudentGuideStore.clearAllCachedInfo()
            "os_info" -> OsInfoCache.clearAll()
            "app_icon" -> AppIconCache.clear()
            "ba_temp_media" -> BaGuideTempMediaCache.clearAll(context)
            "mcp_prefs" -> McpServerManager.clearSavedCacheOnly()
        }
    }

    private fun githubSummary(): CacheEntrySummary {
        val snapshot = GitHubTrackStore.loadSnapshot()
        val detail = buildString {
            append("跟踪 ${snapshot.items.size} 项")
            append(" · 检查缓存 ${snapshot.checkCache.size} 项")
            if (snapshot.lastRefreshMs > 0L) append(" · 已有刷新记录") else append(" · 暂无刷新记录")
        }
        return CacheEntrySummary(
            id = "github",
            title = "主页 / GitHub",
            summary = "版本检查结果、策略缓存与关联图标缓存",
            detail = detail,
            clearLabel = "清理"
        )
    }

    private fun baCalendarSummary(): CacheEntrySummary {
        val snapshot = BASettingsStore.loadSnapshot()
        val calendar = BASettingsStore.loadCalendarCacheSnapshot(snapshot.serverIndex)
        val pool = BASettingsStore.loadPoolCacheSnapshot(snapshot.serverIndex)
        val detail = buildString {
            append("当前服区 ${snapshot.serverIndex}")
            append(" · 日程缓存 ")
            append(if (calendar.raw.isNotBlank()) "已缓存" else "空")
            append(" · 池子缓存 ")
            append(if (pool.raw.isNotBlank()) "已缓存" else "空")
        }
        return CacheEntrySummary(
            id = "ba_calendar",
            title = "BA 页面",
            summary = "活动日程、卡池数据与同步时间",
            detail = detail,
            clearLabel = "清理"
        )
    }

    private fun baStudentGuideSummary(): CacheEntrySummary {
        val count = BaStudentGuideStore.cachedEntryCount()
        return CacheEntrySummary(
            id = "ba_student_guide",
            title = "BA 图鉴页",
            summary = "角色图鉴详情页面缓存",
            detail = "已缓存 $count 条",
            clearLabel = "清理"
        )
    }

    private fun osSummary(): CacheEntrySummary {
        val visible = OsCardVisibilityStore.loadVisibleCards().size
        val cachedSections = OsInfoCache.cachedSectionCount(OsSectionCard.entries.toSet())
        return CacheEntrySummary(
            id = "os_info",
            title = "系统页面",
            summary = "各类系统表与属性缓存",
            detail = "显示卡片 $visible 张 · 已缓存分区 $cachedSections 个",
            clearLabel = "清理"
        )
    }

    private fun appIconSummary(): CacheEntrySummary {
        return CacheEntrySummary(
            id = "app_icon",
            title = "GitHub 图标",
            summary = "GitHub 页面与相关列表的图标内存缓存",
            detail = "当前内存命中 ${AppIconCache.size()} 项",
            clearLabel = "清理"
        )
    }

    private fun baTempMediaSummary(context: Context): CacheEntrySummary {
        val fileCount = BaGuideTempMediaCache.cacheFileCount(context)
        return CacheEntrySummary(
            id = "ba_temp_media",
            title = "图鉴媒体",
            summary = "图鉴页临时图片与媒体文件",
            detail = "文件 $fileCount 个",
            clearLabel = "清理"
        )
    }

    private fun mcpSummary(): CacheEntrySummary {
        val snapshot = McpServerManager.loadSavedCacheSummary()
        return CacheEntrySummary(
            id = "mcp_prefs",
            title = "MCP 页面",
            summary = "服务名称、端口、鉴权 token 等本地持久项",
            detail = snapshot,
            clearLabel = "重置"
        )
    }
}
