package com.example.keios.ui.page.main.ba

import android.content.Context
import android.net.Uri
import com.example.keios.feature.ba.data.remote.GameKeeFetchHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest

internal object BaCalendarPoolImageCache {
    private const val ROOT_DIR = "ba_calendar_pool_media"
    private const val MAX_PARALLEL_DOWNLOADS = 3

    private enum class Category(val folderName: String) {
        Calendar("calendar"),
        Pool("pool"),
    }

    private fun rootDir(context: Context): File = File(context.cacheDir, ROOT_DIR)

    private fun categoryDir(context: Context, category: Category, serverIndex: Int): File {
        return File(rootDir(context), "${category.folderName}/${serverIndex.coerceIn(0, 2)}")
    }

    private fun normalizeRemoteTarget(raw: String): String {
        val normalized = normalizeGameKeeImageLink(raw.trim())
        if (normalized.isBlank()) return ""
        if (normalized.startsWith("file://")) return ""
        return normalized
    }

    private fun fileExtFromUrl(url: String): String {
        val normalized = url.substringBefore('?').substringBefore('#')
        val segment = runCatching { Uri.parse(normalized).lastPathSegment.orEmpty() }
            .getOrDefault("")
            .substringAfterLast('.', "")
            .lowercase()
        val ext = when (segment) {
            "png", "jpg", "jpeg", "webp", "gif", "bmp", "svg" -> segment
            else -> "bin"
        }
        return ".$ext"
    }

    private fun targetFileName(normalizedRemoteUrl: String): String {
        return sha1(normalizedRemoteUrl) + fileExtFromUrl(normalizedRemoteUrl)
    }

    private fun targetFile(
        context: Context,
        category: Category,
        serverIndex: Int,
        normalizedRemoteUrl: String
    ): File {
        return File(categoryDir(context, category, serverIndex), targetFileName(normalizedRemoteUrl))
    }

    suspend fun prefetchForCalendar(
        context: Context,
        serverIndex: Int,
        entries: List<BaCalendarEntry>
    ) {
        prefetchForCategory(
            context = context,
            category = Category.Calendar,
            serverIndex = serverIndex,
            rawUrls = entries.map { it.imageUrl }
        )
    }

    suspend fun prefetchForPool(
        context: Context,
        serverIndex: Int,
        entries: List<BaPoolEntry>
    ) {
        prefetchForCategory(
            context = context,
            category = Category.Pool,
            serverIndex = serverIndex,
            rawUrls = entries.map { it.imageUrl }
        )
    }

    private suspend fun prefetchForCategory(
        context: Context,
        category: Category,
        serverIndex: Int,
        rawUrls: List<String>
    ) = withContext(Dispatchers.IO) {
        val targets = rawUrls
            .map(::normalizeRemoteTarget)
            .filter { it.isNotBlank() }
            .distinct()
        if (targets.isEmpty()) return@withContext

        val dir = categoryDir(context, category, serverIndex)
        dir.mkdirs()

        coroutineScope {
            val semaphore = Semaphore(MAX_PARALLEL_DOWNLOADS)
            targets.map { url ->
                async(Dispatchers.IO) {
                    semaphore.withPermit {
                        val file = targetFile(context, category, serverIndex, url)
                        if (file.exists() && file.length() > 0L) return@withPermit
                        runCatching { GameKeeFetchHelper.downloadToFile(url, file) }
                        if (file.exists() && file.length() <= 0L) {
                            runCatching { file.delete() }
                        }
                    }
                }
            }.awaitAll()
        }
    }

    suspend fun pruneCalendarStale(
        context: Context,
        serverIndex: Int,
        entries: List<BaCalendarEntry>
    ) {
        pruneCategoryStale(
            context = context,
            category = Category.Calendar,
            serverIndex = serverIndex,
            rawUrls = entries.map { it.imageUrl }
        )
    }

    suspend fun prunePoolStale(
        context: Context,
        serverIndex: Int,
        entries: List<BaPoolEntry>
    ) {
        pruneCategoryStale(
            context = context,
            category = Category.Pool,
            serverIndex = serverIndex,
            rawUrls = entries.map { it.imageUrl }
        )
    }

    private suspend fun pruneCategoryStale(
        context: Context,
        category: Category,
        serverIndex: Int,
        rawUrls: List<String>
    ) = withContext(Dispatchers.IO) {
        val dir = categoryDir(context, category, serverIndex)
        if (!dir.exists()) return@withContext

        val keepNames = rawUrls
            .map(::normalizeRemoteTarget)
            .filter { it.isNotBlank() }
            .map(::targetFileName)
            .toSet()

        dir.listFiles()?.forEach { file ->
            if (file.isFile && file.name !in keepNames) {
                runCatching { file.delete() }
            }
        }
    }

    fun resolveCalendarImageUrl(
        context: Context,
        serverIndex: Int,
        rawUrl: String,
        localOnly: Boolean
    ): String {
        return resolveCategoryImageUrl(
            context = context,
            category = Category.Calendar,
            serverIndex = serverIndex,
            rawUrl = rawUrl,
            localOnly = localOnly
        )
    }

    fun resolvePoolImageUrl(
        context: Context,
        serverIndex: Int,
        rawUrl: String,
        localOnly: Boolean
    ): String {
        return resolveCategoryImageUrl(
            context = context,
            category = Category.Pool,
            serverIndex = serverIndex,
            rawUrl = rawUrl,
            localOnly = localOnly
        )
    }

    private fun resolveCategoryImageUrl(
        context: Context,
        category: Category,
        serverIndex: Int,
        rawUrl: String,
        localOnly: Boolean
    ): String {
        val raw = rawUrl.trim()
        if (raw.isBlank()) return ""
        if (raw.startsWith("file://")) return raw
        val normalizedRemote = normalizeRemoteTarget(raw)
        if (normalizedRemote.isBlank()) return if (localOnly) "" else normalizeGameKeeImageLink(raw)
        val file = targetFile(context, category, serverIndex, normalizedRemote)
        if (file.exists() && file.length() > 0L) {
            return Uri.fromFile(file).toString()
        }
        return if (localOnly) "" else normalizedRemote
    }

    fun applyCachedCalendarImageUrls(
        context: Context,
        serverIndex: Int,
        entries: List<BaCalendarEntry>,
        localOnly: Boolean
    ): List<BaCalendarEntry> {
        if (entries.isEmpty()) return emptyList()
        return entries.map { entry ->
            entry.copy(
                imageUrl = resolveCalendarImageUrl(
                    context = context,
                    serverIndex = serverIndex,
                    rawUrl = entry.imageUrl,
                    localOnly = localOnly
                )
            )
        }
    }

    fun applyCachedPoolImageUrls(
        context: Context,
        serverIndex: Int,
        entries: List<BaPoolEntry>,
        localOnly: Boolean
    ): List<BaPoolEntry> {
        if (entries.isEmpty()) return emptyList()
        return entries.map { entry ->
            entry.copy(
                imageUrl = resolvePoolImageUrl(
                    context = context,
                    serverIndex = serverIndex,
                    rawUrl = entry.imageUrl,
                    localOnly = localOnly
                )
            )
        }
    }

    fun clearCalendarCache(context: Context, serverIndex: Int) {
        runCatching { categoryDir(context, Category.Calendar, serverIndex).deleteRecursively() }
    }

    fun clearPoolCache(context: Context, serverIndex: Int) {
        runCatching { categoryDir(context, Category.Pool, serverIndex).deleteRecursively() }
    }

    private fun sha1(raw: String): String {
        val md = MessageDigest.getInstance("SHA-1")
        val bytes = md.digest(raw.toByteArray())
        return buildString(bytes.size * 2) {
            bytes.forEach { b -> append("%02x".format(b)) }
        }
    }
}
