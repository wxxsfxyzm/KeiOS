package com.example.keios.ui.page.main.home.model

import com.example.keios.feature.github.data.local.GitHubTrackStore
import com.example.keios.feature.github.model.GitHubLookupStrategyOption
import com.tencent.mmkv.MMKV
import java.util.Locale
import java.util.concurrent.TimeUnit

internal fun formatGitHubCacheAgo(
    lastRefreshMs: Long,
    notRefreshedText: String,
    justNowText: String,
    nowMs: Long = System.currentTimeMillis()
): String {
    if (lastRefreshMs <= 0L) return notRefreshedText
    val deltaMs = (nowMs - lastRefreshMs).coerceAtLeast(0L)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(deltaMs)
    if (minutes <= 0L) return justNowText
    if (minutes < 60L) return "${minutes}m"
    val hours = minutes / 60L
    val remainMinutes = minutes % 60L
    return if (remainMinutes == 0L) "${hours}h" else "${hours}h ${remainMinutes}m"
}

private const val HOME_BA_KV_ID = "ba_page_settings"
private const val HOME_PAGE_PREFS_KV_ID = "home_page_prefs"
private const val HOME_VISIBLE_OVERVIEW_CARDS_KEY = "home_visible_overview_cards"
private const val HOME_BA_DEFAULT_FRIEND_CODE = "ARISUKEI"
private const val HOME_BA_AP_LIMIT_MAX = 240
private const val HOME_BA_AP_MAX = 999
private val HOME_BA_CAFE_DAILY_AP_BY_LEVEL = intArrayOf(
    92,
    152,
    222,
    302,
    390,
    460,
    530,
    600,
    570,
    740
)

internal enum class HomeOverviewCard {
    MCP,
    GITHUB,
    BA
}

internal fun loadHomeVisibleOverviewCards(): Set<HomeOverviewCard> {
    val kv = MMKV.mmkvWithID(HOME_PAGE_PREFS_KV_ID)
    val raw = kv.decodeString(HOME_VISIBLE_OVERVIEW_CARDS_KEY, "").orEmpty().trim()
    if (raw.isBlank()) return HomeOverviewCard.entries.toSet()
    val parsed = raw.split(',')
        .mapNotNull { name ->
            HomeOverviewCard.entries.firstOrNull { it.name == name.trim() }
        }
        .toSet()
    return parsed.ifEmpty { HomeOverviewCard.entries.toSet() }
}

internal fun saveHomeVisibleOverviewCards(cards: Set<HomeOverviewCard>) {
    val kv = MMKV.mmkvWithID(HOME_PAGE_PREFS_KV_ID)
    val serialized = cards.joinToString(",") { it.name }
    kv.encode(HOME_VISIBLE_OVERVIEW_CARDS_KEY, serialized)
}

data class HomeGitHubOverview(
    val trackedCount: Int = 0,
    val cacheHitCount: Int = 0,
    val updatableCount: Int = 0,
    val preReleaseUpdateCount: Int = 0,
    val strategy: GitHubLookupStrategyOption = GitHubLookupStrategyOption.AtomFeed,
    val apiTokenConfigured: Boolean = false,
    val cachedRefreshMs: Long = 0L,
    val loaded: Boolean = false
)

data class HomeBaOverview(
    val activated: Boolean = false,
    val apCurrent: Int = 0,
    val apLimit: Int = HOME_BA_AP_LIMIT_MAX,
    val cafeStored: Int = 0,
    val cafeCap: Int = HOME_BA_CAFE_DAILY_AP_BY_LEVEL.last(),
    val loaded: Boolean = false
)

fun loadHomeGitHubOverview(): HomeGitHubOverview {
    val snapshot = GitHubTrackStore.loadSnapshot()
    val activeStrategyId = snapshot.lookupConfig.selectedStrategy.storageId
    val matchedCacheByTrackId = snapshot.items.associate { item ->
        val cache = snapshot.checkCache[item.id]
            ?.takeIf { entry ->
                entry.sourceStrategyId.ifBlank { GitHubLookupStrategyOption.AtomFeed.storageId } == activeStrategyId
            }
        item.id to cache
    }
    val cacheHitCount = matchedCacheByTrackId.count { it.value != null }
    return HomeGitHubOverview(
        trackedCount = snapshot.items.size,
        cacheHitCount = cacheHitCount,
        updatableCount = matchedCacheByTrackId.count { it.value?.hasUpdate == true },
        preReleaseUpdateCount = matchedCacheByTrackId.count { it.value?.hasPreReleaseUpdate == true },
        strategy = snapshot.lookupConfig.selectedStrategy,
        apiTokenConfigured = snapshot.lookupConfig.apiToken.isNotBlank(),
        cachedRefreshMs = if (cacheHitCount > 0) snapshot.lastRefreshMs else 0L,
        loaded = true
    )
}

fun loadHomeBaOverview(): HomeBaOverview {
    val kv = MMKV.mmkvWithID(HOME_BA_KV_ID)

    val friendCode = kv.decodeString("id_friend_code", HOME_BA_DEFAULT_FRIEND_CODE)
        .orEmpty()
        .uppercase(Locale.ROOT)
        .filter { it in 'A'..'Z' }
        .take(8)
        .let { if (it.length == 8) it else HOME_BA_DEFAULT_FRIEND_CODE }
    val activated = friendCode != HOME_BA_DEFAULT_FRIEND_CODE

    val apLimit = kv.decodeInt("ap_limit", HOME_BA_AP_LIMIT_MAX).coerceIn(0, HOME_BA_AP_LIMIT_MAX)
    val apCurrentExact = if (kv.containsKey("ap_current_exact")) {
        kv.decodeString("ap_current_exact", "0")?.toDoubleOrNull() ?: 0.0
    } else {
        kv.decodeInt("ap_current", 0).toDouble()
    }
    val apCurrent = apCurrentExact.coerceIn(0.0, HOME_BA_AP_MAX.toDouble()).toInt()

    val cafeLevel = kv.decodeInt("cafe_level", 1).coerceIn(1, 10)
    val cafeCap = HOME_BA_CAFE_DAILY_AP_BY_LEVEL[cafeLevel - 1]
    val cafeStoredRaw = kv.decodeString("cafe_stored_ap", "0")?.toDoubleOrNull() ?: 0.0
    val cafeStored = cafeStoredRaw.coerceAtLeast(0.0).toInt().coerceAtMost(cafeCap)

    return HomeBaOverview(
        activated = activated,
        apCurrent = apCurrent,
        apLimit = apLimit,
        cafeStored = cafeStored,
        cafeCap = cafeCap,
        loaded = true
    )
}
