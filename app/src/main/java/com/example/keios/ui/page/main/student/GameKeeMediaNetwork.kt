package com.example.keios.ui.page.main.student

import android.content.Context
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheSpan
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.database.StandaloneDatabaseProvider
import com.example.keios.ui.page.main.ba.BASettingsStore
import com.tencent.mmkv.MMKV
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

private const val GAMEKEE_REFERER = "https://www.gamekee.com/"
private const val GAMEKEE_ORIGIN = "https://www.gamekee.com"
private const val GAMEKEE_FIREFOX_ANDROID_UA =
    "Mozilla/5.0 (Android 15; Mobile; rv:140.0) Gecko/140.0 Firefox/140.0"
private const val GAMEKEE_MEDIA_CACHE_DIR = "ba_gamekee_media3_cache"
private const val GAMEKEE_MEDIA_CACHE_MAX_BYTES = 384L * 1024L * 1024L
private const val GAMEKEE_MEDIA_CACHE_META_KV_ID = "ba_gamekee_media3_cache_meta"
private const val KEY_MEDIA_LAST_CLEANUP_MS = "media_last_cleanup_ms"
private const val DEFAULT_MEDIA_REFRESH_HOURS = 12
private const val MIN_CLEANUP_CHECK_INTERVAL_MS = 15L * 60L * 1000L
private const val MIN_STALE_THRESHOLD_MS = 6L * 60L * 60L * 1000L

private val GAMEKEE_DEFAULT_HEADERS = mapOf(
    "Accept" to "*/*",
    "Accept-Language" to "zh-CN",
    "Referer" to GAMEKEE_REFERER,
    "Origin" to GAMEKEE_ORIGIN,
    "User-Agent" to GAMEKEE_FIREFOX_ANDROID_UA,
    "device-num" to "1",
    "game-alias" to "ba"
)

private object GameKeeMediaPlayerCache {
    @Volatile
    private var simpleCache: SimpleCache? = null
    private val cleanupStore: MMKV by lazy { MMKV.mmkvWithID(GAMEKEE_MEDIA_CACHE_META_KV_ID) }
    private val cleanupRunning = AtomicBoolean(false)

    @Synchronized
    fun get(context: Context): SimpleCache {
        simpleCache?.let { cache ->
            scheduleCleanupIfNeeded(cache)
            return cache
        }
        val appContext = context.applicationContext
        val cacheDir = File(appContext.cacheDir, GAMEKEE_MEDIA_CACHE_DIR).apply { mkdirs() }
        val cache = SimpleCache(
            cacheDir,
            LeastRecentlyUsedCacheEvictor(GAMEKEE_MEDIA_CACHE_MAX_BYTES),
            StandaloneDatabaseProvider(appContext)
        )
        simpleCache = cache
        scheduleCleanupIfNeeded(cache)
        return cache
    }

    private fun scheduleCleanupIfNeeded(cache: SimpleCache) {
        val now = System.currentTimeMillis()
        val checkIntervalMs = cleanupCheckIntervalMs()
        val lastCleanupMs = cleanupStore.decodeLong(KEY_MEDIA_LAST_CLEANUP_MS, 0L)
        if ((now - lastCleanupMs).coerceAtLeast(0L) < checkIntervalMs) return
        if (!cleanupRunning.compareAndSet(false, true)) return

        thread(name = "KeiOS-GameKeeMediaCacheCleanup", isDaemon = true) {
            try {
                val runNow = System.currentTimeMillis()
                val lastRun = cleanupStore.decodeLong(KEY_MEDIA_LAST_CLEANUP_MS, 0L)
                if ((runNow - lastRun).coerceAtLeast(0L) < checkIntervalMs) return@thread

                val staleThresholdMs = staleThresholdMs()
                pruneStaleResources(cache, runNow, staleThresholdMs)
                cleanupStore.encode(KEY_MEDIA_LAST_CLEANUP_MS, runNow)
            } finally {
                cleanupRunning.set(false)
            }
        }
    }

    private fun pruneStaleResources(
        cache: SimpleCache,
        nowMs: Long,
        staleThresholdMs: Long
    ) {
        val keys = runCatching { cache.keys }.getOrDefault(emptySet())
        if (keys.isEmpty()) return
        keys.forEach { key ->
            val newestTouch = newestTouchMs(cache, key)
            if (newestTouch <= 0L) return@forEach
            if ((nowMs - newestTouch).coerceAtLeast(0L) >= staleThresholdMs) {
                runCatching { cache.removeResource(key) }
            }
        }
    }

    private fun newestTouchMs(cache: SimpleCache, key: String): Long {
        val spans = runCatching { cache.getCachedSpans(key) }.getOrDefault(emptySet<CacheSpan>())
        if (spans.isEmpty()) return 0L
        var latest = 0L
        spans.forEach { span ->
            val fromTouch = span.lastTouchTimestamp.takeIf { it > 0L } ?: 0L
            val fromFile = span.file?.lastModified()?.takeIf { it > 0L } ?: 0L
            latest = maxOf(latest, fromTouch, fromFile)
        }
        return latest
    }

    private fun cleanupCheckIntervalMs(): Long {
        val refreshMs = refreshIntervalMs()
        return (refreshMs / 2L).coerceAtLeast(MIN_CLEANUP_CHECK_INTERVAL_MS)
    }

    private fun staleThresholdMs(): Long {
        val refreshMs = refreshIntervalMs()
        return (refreshMs * 2L).coerceAtLeast(MIN_STALE_THRESHOLD_MS)
    }

    private fun refreshIntervalMs(): Long {
        val hours = runCatching { BASettingsStore.loadCalendarRefreshIntervalHours() }
            .getOrDefault(DEFAULT_MEDIA_REFRESH_HOURS)
            .coerceAtLeast(1)
        return hours.toLong() * 60L * 60L * 1000L
    }
}

internal fun createGameKeeHttpDataSourceFactory(): DefaultHttpDataSource.Factory {
    return DefaultHttpDataSource.Factory()
        .setAllowCrossProtocolRedirects(true)
        .setConnectTimeoutMs(8_000)
        .setReadTimeoutMs(12_000)
        .setUserAgent(GAMEKEE_FIREFOX_ANDROID_UA)
        .setDefaultRequestProperties(GAMEKEE_DEFAULT_HEADERS)
}

internal fun createGameKeeMediaSourceFactory(context: Context): DefaultMediaSourceFactory {
    val httpFactory = createGameKeeHttpDataSourceFactory()
    val upstreamFactory = DefaultDataSource.Factory(context, httpFactory)
    val cacheDataSourceFactory = CacheDataSource.Factory()
        .setCache(GameKeeMediaPlayerCache.get(context))
        .setUpstreamDataSourceFactory(upstreamFactory)
        .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    return DefaultMediaSourceFactory(cacheDataSourceFactory)
}
