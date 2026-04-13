package com.example.keios.feature.github.data.local

import com.example.keios.feature.github.model.GitHubCheckCacheEntry
import com.example.keios.feature.github.model.GitHubLookupConfig
import com.example.keios.feature.github.model.GitHubLookupStrategyOption
import com.example.keios.feature.github.model.GitHubTrackedApp
import com.tencent.mmkv.MMKV
import org.json.JSONArray
import org.json.JSONObject

object GitHubTrackStore {
    private const val KV_ID = "github_track_store"
    private const val KEY_ITEMS = "tracked_items"
    private const val KEY_CHECK_CACHE = "tracked_check_cache"
    private const val KEY_LAST_REFRESH_MS = "last_full_refresh_ms"
    private const val KEY_REFRESH_INTERVAL_HOURS = "refresh_interval_hours"
    private const val KEY_LOOKUP_STRATEGY = "lookup_strategy"
    private const val KEY_GITHUB_API_TOKEN = "github_api_token"

    @Volatile
    private var didAutoRefreshInSession: Boolean = false

    private fun kv(): MMKV = MMKV.mmkvWithID(KV_ID)

    fun load(): List<GitHubTrackedApp> {
        val raw = kv().decodeString(KEY_ITEMS).orEmpty()
        if (raw.isBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val obj = array.optJSONObject(i) ?: continue
                    val repoUrl = obj.optString("repoUrl").trim()
                    val owner = obj.optString("owner").trim()
                    val repo = obj.optString("repo").trim()
                    val packageName = obj.optString("packageName").trim()
                    val appLabel = obj.optString("appLabel").trim()
                    if (repoUrl.isNotBlank() && owner.isNotBlank() && repo.isNotBlank() && packageName.isNotBlank()) {
                        add(
                            GitHubTrackedApp(
                                repoUrl = repoUrl,
                                owner = owner,
                                repo = repo,
                                packageName = packageName,
                                appLabel = appLabel.ifBlank { packageName },
                                checkPreRelease = if (obj.has("checkPreRelease")) {
                                    obj.optBoolean("checkPreRelease", false)
                                } else {
                                    false
                                }
                            )
                        )
                    }
                }
            }
        }.getOrDefault(emptyList())
    }

    fun save(items: List<GitHubTrackedApp>) {
        val array = JSONArray()
        items.forEach { item ->
            val obj = JSONObject()
                .put("repoUrl", item.repoUrl)
                .put("owner", item.owner)
                .put("repo", item.repo)
                .put("packageName", item.packageName)
                .put("appLabel", item.appLabel)
                .put("checkPreRelease", item.checkPreRelease)
            array.put(obj)
        }
        kv().encode(KEY_ITEMS, array.toString())
    }

    fun loadCheckCache(): Pair<Map<String, GitHubCheckCacheEntry>, Long> {
        val raw = kv().decodeString(KEY_CHECK_CACHE).orEmpty()
        val ts = kv().decodeLong(KEY_LAST_REFRESH_MS, 0L)
        if (raw.isBlank()) return emptyMap<String, GitHubCheckCacheEntry>() to ts
        val map = runCatching {
            val obj = JSONObject(raw)
            buildMap {
                val keys = obj.keys()
                while (keys.hasNext()) {
                    val id = keys.next()
                    val item = obj.optJSONObject(id) ?: continue
                    put(
                        id,
                        GitHubCheckCacheEntry(
                            loading = false,
                            localVersion = item.optString("localVersion"),
                            localVersionCode = item.optLong("localVersionCode", -1L),
                            latestTag = item.optString("latestTag"),
                            latestStableRawTag = item.optString("latestStableRawTag"),
                            latestStableUrl = item.optString("latestStableUrl"),
                            latestPreRawTag = item.optString("latestPreRawTag"),
                            latestPreUrl = item.optString("latestPreUrl"),
                            hasUpdate = if (item.has("hasUpdate")) item.optBoolean("hasUpdate") else null,
                            message = item.optString("message"),
                            isPreRelease = item.optBoolean("isPreRelease", false),
                            preReleaseInfo = item.optString("preReleaseInfo"),
                            showPreReleaseInfo = item.optBoolean("showPreReleaseInfo", false),
                            hasPreReleaseUpdate = item.optBoolean("hasPreReleaseUpdate", false),
                            sourceStrategyId = item.optString("sourceStrategyId")
                        )
                    )
                }
            }
        }.getOrDefault(emptyMap())
        return map to ts
    }

    fun saveCheckCache(states: Map<String, GitHubCheckCacheEntry>, lastRefreshMs: Long) {
        val obj = JSONObject()
        states.forEach { (id, state) ->
            obj.put(
                id,
                JSONObject()
                    .put("localVersion", state.localVersion)
                    .put("localVersionCode", state.localVersionCode)
                    .put("latestTag", state.latestTag)
                    .put("latestStableRawTag", state.latestStableRawTag)
                    .put("latestStableUrl", state.latestStableUrl)
                    .put("latestPreRawTag", state.latestPreRawTag)
                    .put("latestPreUrl", state.latestPreUrl)
                    .put("hasUpdate", state.hasUpdate)
                    .put("message", state.message)
                    .put("isPreRelease", state.isPreRelease)
                    .put("preReleaseInfo", state.preReleaseInfo)
                    .put("showPreReleaseInfo", state.showPreReleaseInfo)
                    .put("hasPreReleaseUpdate", state.hasPreReleaseUpdate)
                    .put("sourceStrategyId", state.sourceStrategyId)
            )
        }
        kv().encode(KEY_CHECK_CACHE, obj.toString())
        kv().encode(KEY_LAST_REFRESH_MS, lastRefreshMs)
    }

    fun clearCheckCache() {
        kv().removeValueForKey(KEY_CHECK_CACHE)
        kv().removeValueForKey(KEY_LAST_REFRESH_MS)
    }

    fun shouldAutoRefreshOnceInSession(): Boolean = !didAutoRefreshInSession

    fun markAutoRefreshDoneInSession() {
        didAutoRefreshInSession = true
    }

    fun loadRefreshIntervalHours(defaultValue: Int = 3): Int {
        val value = kv().decodeInt(KEY_REFRESH_INTERVAL_HOURS, defaultValue)
        return if (value in setOf(1, 3, 6, 12)) value else defaultValue
    }

    fun saveRefreshIntervalHours(hours: Int) {
        if (hours in setOf(1, 3, 6, 12)) {
            kv().encode(KEY_REFRESH_INTERVAL_HOURS, hours)
        }
    }

    fun loadLookupConfig(): GitHubLookupConfig {
        return GitHubLookupConfig(
            selectedStrategy = GitHubLookupStrategyOption.fromStorageId(
                kv().decodeString(KEY_LOOKUP_STRATEGY).orEmpty()
            ),
            apiToken = kv().decodeString(KEY_GITHUB_API_TOKEN).orEmpty().trim()
        )
    }

    fun saveLookupConfig(config: GitHubLookupConfig) {
        kv().encode(KEY_LOOKUP_STRATEGY, config.selectedStrategy.storageId)
        kv().encode(KEY_GITHUB_API_TOKEN, config.apiToken.trim())
    }
}
