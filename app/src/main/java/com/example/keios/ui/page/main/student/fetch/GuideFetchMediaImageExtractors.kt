package com.example.keios.ui.page.main.student.fetch

import org.json.JSONArray
import org.json.JSONObject
import kotlin.collections.plusAssign

internal fun extractImageUrlsFromHtml(sourceUrl: String, raw: String): List<String> {
    if (raw.isBlank()) return emptyList()
    val regex = Regex(
        """(?i)<img[^>]+src\s*=\s*["']([^"']+)["'][^>]*>"""
    )
    return regex.findAll(raw)
        .mapNotNull { match ->
            normalizeImageUrl(sourceUrl, match.groupValues.getOrNull(1).orEmpty())
        }
        .filter { it.isNotBlank() }
        .distinct()
        .toList()
}

internal fun extractImageUrlsFromAny(sourceUrl: String, any: Any?, depth: Int = 0): List<String> {
    if (any == null || depth > 6) return emptyList()
    return when (any) {
        is String -> {
            val normalized = normalizeImageUrl(sourceUrl, any)
            buildList {
                if (looksLikeImageUrl(normalized)) add(normalized)
                addAll(
                    any.split(",", ";")
                        .map { normalizeImageUrl(sourceUrl, it.trim()) }
                        .filter { looksLikeImageUrl(it) }
                )
                addAll(extractImageUrlsFromHtml(sourceUrl, any))
            }.distinct()
        }

        is JSONObject -> {
            val result = mutableListOf<String>()
            val keys = any.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                result += extractImageUrlsFromAny(sourceUrl, any.opt(key), depth + 1)
            }
            result.distinct()
        }

        is JSONArray -> {
            val result = mutableListOf<String>()
            for (i in 0 until any.length()) {
                result += extractImageUrlsFromAny(sourceUrl, any.opt(i), depth + 1)
            }
            result.distinct()
        }

        else -> emptyList()
    }
}
