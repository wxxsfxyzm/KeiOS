package com.example.keios.ui.page.main.student

import com.tencent.mmkv.MMKV
import org.json.JSONArray
import org.json.JSONObject

private const val BA_GUIDE_KV_ID = "ba_student_guide"
private const val BA_GUIDE_KEY_CURRENT_URL = "current_url"
private const val BA_GUIDE_KEY_CACHE_PREFIX = "cache_"

object BaStudentGuideStore {
    private fun kv(): MMKV = MMKV.mmkvWithID(BA_GUIDE_KV_ID)

    private fun encodeGuideRows(rows: List<BaGuideRow>): JSONArray {
        return JSONArray().apply {
            rows.forEach { row ->
                put(
                    JSONObject().apply {
                        put("k", row.key)
                        put("v", row.value)
                        put("img", row.imageUrl)
                    }
                )
            }
        }
    }

    private fun decodeGuideRows(obj: JSONObject, key: String): List<BaGuideRow> {
        val arr = obj.optJSONArray(key) ?: return emptyList()
        return buildList {
            for (i in 0 until arr.length()) {
                val row = arr.optJSONObject(i) ?: continue
                val k = row.optString("k").trim()
                val v = row.optString("v").trim()
                val img = row.optString("img").trim()
                if (k.isBlank() && v.isBlank() && img.isBlank()) continue
                add(BaGuideRow(key = k, value = v, imageUrl = img))
            }
        }
    }

    private fun encodeGalleryItems(items: List<BaGuideGalleryItem>): JSONArray {
        return JSONArray().apply {
            items.forEach { item ->
                put(
                    JSONObject().apply {
                        put("t", item.title)
                        put("img", item.imageUrl)
                    }
                )
            }
        }
    }

    private fun decodeGalleryItems(obj: JSONObject, key: String): List<BaGuideGalleryItem> {
        val arr = obj.optJSONArray(key) ?: return emptyList()
        return buildList {
            for (i in 0 until arr.length()) {
                val item = arr.optJSONObject(i) ?: continue
                val title = item.optString("t").trim()
                val imageUrl = item.optString("img").trim()
                if (imageUrl.isBlank()) continue
                add(BaGuideGalleryItem(title = title, imageUrl = imageUrl))
            }
        }
    }

    fun setCurrentUrl(url: String) {
        kv().encode(BA_GUIDE_KEY_CURRENT_URL, url.trim())
    }

    fun loadCurrentUrl(): String = kv().decodeString(BA_GUIDE_KEY_CURRENT_URL, "").orEmpty()

    private fun cacheKey(url: String): String {
        val id = url.trim().hashCode().toUInt().toString(16)
        return BA_GUIDE_KEY_CACHE_PREFIX + id
    }

    fun saveInfo(info: BaStudentGuideInfo) {
        val statsArr = JSONArray().apply {
            info.stats.forEach { (k, v) ->
                put(
                    JSONObject().apply {
                        put("k", k)
                        put("v", v)
                    }
                )
            }
        }
        val raw = JSONObject().apply {
            put("sourceUrl", info.sourceUrl)
            put("title", info.title)
            put("subtitle", info.subtitle)
            put("description", info.description)
            put("imageUrl", info.imageUrl)
            put("summary", info.summary)
            put("syncedAtMs", info.syncedAtMs)
            put("stats", statsArr)
            put("skillRows", encodeGuideRows(info.skillRows))
            put("profileRows", encodeGuideRows(info.profileRows))
            put("galleryItems", encodeGalleryItems(info.galleryItems))
            put("growthRows", encodeGuideRows(info.growthRows))
            put("voiceRows", encodeGuideRows(info.voiceRows))
            put("tabSkillIconUrl", info.tabSkillIconUrl)
            put("tabProfileIconUrl", info.tabProfileIconUrl)
            put("tabVoiceIconUrl", info.tabVoiceIconUrl)
            put("tabGalleryIconUrl", info.tabGalleryIconUrl)
            put("tabSimulateIconUrl", info.tabSimulateIconUrl)
        }.toString()
        kv().encode(cacheKey(info.sourceUrl), raw)
    }

    fun loadInfo(url: String): BaStudentGuideInfo? {
        val source = url.trim()
        if (source.isBlank()) return null
        val raw = kv().decodeString(cacheKey(source), "").orEmpty()
        if (raw.isBlank()) return null
        return runCatching {
            val obj = JSONObject(raw)
            val stats = mutableListOf<Pair<String, String>>()
            val arr = obj.optJSONArray("stats") ?: JSONArray()
            for (i in 0 until arr.length()) {
                val row = arr.optJSONObject(i) ?: continue
                val k = row.optString("k").trim()
                val v = row.optString("v").trim()
                if (k.isNotBlank() && v.isNotBlank()) stats += k to v
            }
            BaStudentGuideInfo(
                sourceUrl = obj.optString("sourceUrl").ifBlank { source },
                title = obj.optString("title"),
                subtitle = obj.optString("subtitle"),
                description = obj.optString("description"),
                imageUrl = obj.optString("imageUrl"),
                summary = obj.optString("summary"),
                stats = stats,
                skillRows = decodeGuideRows(obj, "skillRows"),
                profileRows = decodeGuideRows(obj, "profileRows"),
                galleryItems = decodeGalleryItems(obj, "galleryItems"),
                growthRows = decodeGuideRows(obj, "growthRows"),
                voiceRows = decodeGuideRows(obj, "voiceRows"),
                tabSkillIconUrl = obj.optString("tabSkillIconUrl").trim(),
                tabProfileIconUrl = obj.optString("tabProfileIconUrl").trim(),
                tabVoiceIconUrl = obj.optString("tabVoiceIconUrl").trim(),
                tabGalleryIconUrl = obj.optString("tabGalleryIconUrl").trim(),
                tabSimulateIconUrl = obj.optString("tabSimulateIconUrl").trim(),
                syncedAtMs = obj.optLong("syncedAtMs", 0L)
            )
        }.getOrNull()
    }
}

