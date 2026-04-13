package com.example.keios.feature.github.data.local

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.LruCache

object AppIconCache {
    private val cache = object : LruCache<String, Bitmap>(120) {}
    @Volatile
    private var lastUpdatedAtMs: Long = 0L

    fun get(packageName: String): Bitmap? = synchronized(cache) { cache.get(packageName) }

    fun getOrLoad(context: Context, packageName: String): Bitmap? {
        get(packageName)?.let { return it }
        val bitmap = runCatching {
            val drawable = context.packageManager.getApplicationIcon(packageName)
            drawableToBitmap(drawable)
        }.getOrNull() ?: return null
        synchronized(cache) { cache.put(packageName, bitmap) }
        lastUpdatedAtMs = System.currentTimeMillis()
        return bitmap
    }

    fun preload(context: Context, packageNames: List<String>) {
        packageNames.forEach { pkg ->
            if (get(pkg) == null) {
                runCatching { getOrLoad(context, pkg) }
            }
        }
    }

    fun size(): Int = synchronized(cache) { cache.size() }

    fun estimatedMemoryBytes(): Long = synchronized(cache) {
        cache.snapshot().values.fold(0L) { acc, bitmap -> acc + bitmap.byteCount.toLong() }
    }

    fun lastUpdatedAtMs(): Long = lastUpdatedAtMs

    fun clear() {
        synchronized(cache) { cache.evictAll() }
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) return drawable.bitmap
        val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 96
        val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 96
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}
