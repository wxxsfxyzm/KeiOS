package os.kei.ui.page.main.student.fetch

import org.json.JSONArray
import org.json.JSONObject
import kotlin.collections.plusAssign

internal fun extractVideoUrlsFromAny(sourceUrl: String, any: Any?, depth: Int = 0): List<String> {
    if (any == null || depth > 6) return emptyList()
    return when (any) {
        is String -> {
            buildList {
                val normalized = normalizeMediaUrl(sourceUrl, any)
                if (looksLikeVideoUrl(normalized)) add(normalized)
                any.split(",", ";")
                    .map { normalizeMediaUrl(sourceUrl, it.trim()) }
                    .filter { looksLikeVideoUrl(it) }
                    .forEach { add(it) }
            }.distinct()
        }

        is JSONObject -> {
            val result = mutableListOf<String>()
            val keys = any.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                result += extractVideoUrlsFromAny(sourceUrl, any.opt(key), depth + 1)
            }
            result.distinct()
        }

        is JSONArray -> {
            val result = mutableListOf<String>()
            for (i in 0 until any.length()) {
                result += extractVideoUrlsFromAny(sourceUrl, any.opt(i), depth + 1)
            }
            result.distinct()
        }

        else -> emptyList()
    }
}
