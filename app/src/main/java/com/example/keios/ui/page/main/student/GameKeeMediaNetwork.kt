package com.example.keios.ui.page.main.student

import android.content.Context
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.database.StandaloneDatabaseProvider
import java.io.File

private const val GAMEKEE_REFERER = "https://www.gamekee.com/"
private const val GAMEKEE_ORIGIN = "https://www.gamekee.com"
private const val GAMEKEE_FIREFOX_ANDROID_UA =
    "Mozilla/5.0 (Android 15; Mobile; rv:140.0) Gecko/140.0 Firefox/140.0"
private const val GAMEKEE_MEDIA_CACHE_DIR = "ba_gamekee_media3_cache"
private const val GAMEKEE_MEDIA_CACHE_MAX_BYTES = 384L * 1024L * 1024L

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

    @Synchronized
    fun get(context: Context): SimpleCache {
        simpleCache?.let { return it }
        val appContext = context.applicationContext
        val cacheDir = File(appContext.cacheDir, GAMEKEE_MEDIA_CACHE_DIR).apply { mkdirs() }
        val cache = SimpleCache(
            cacheDir,
            LeastRecentlyUsedCacheEvictor(GAMEKEE_MEDIA_CACHE_MAX_BYTES),
            StandaloneDatabaseProvider(appContext)
        )
        simpleCache = cache
        return cache
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
