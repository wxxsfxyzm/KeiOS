package com.example.keios.ui.page.main.student.fetch.parser

import com.example.keios.ui.page.main.student.BaGuideGalleryItem
import com.example.keios.ui.page.main.student.GuideTab
import com.example.keios.ui.page.main.student.fetch.canonicalVoiceLanguageLabel
import com.example.keios.ui.page.main.student.isExpressionGalleryItem
import com.example.keios.ui.page.main.student.fetch.looksLikeImageUrl
import com.example.keios.ui.page.main.student.fetch.normalizeImageUrl
import com.example.keios.ui.page.main.student.fetch.stripHtml
import org.json.JSONArray
import org.json.JSONObject

internal fun firstImageFromAny(any: Any?, sourceUrl: String, depth: Int = 0): String {
    if (any == null || depth > 4) return ""
    return when (any) {
        is String -> {
            val normalized = normalizeImageUrl(sourceUrl, any)
            if (looksLikeImageUrl(normalized)) normalized else ""
        }

        is JSONObject -> {
            val keys = any.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val found = firstImageFromAny(any.opt(key), sourceUrl, depth + 1)
                if (found.isNotBlank()) return found
            }
            ""
        }

        is JSONArray -> {
            for (i in 0 until any.length()) {
                val found = firstImageFromAny(any.opt(i), sourceUrl, depth + 1)
                if (found.isNotBlank()) return found
            }
            ""
        }

        else -> ""
    }
}

internal fun normalizeGuideTabLabel(raw: String): String {
    return stripHtml(raw)
        .replace(Regex("\\s+"), "")
        .replace("（", "(")
        .replace("）", ")")
        .trim()
}

internal fun mapGuideTabByLabel(rawLabel: String, strict: Boolean): GuideTab? {
    val label = normalizeGuideTabLabel(rawLabel)
    if (label.isBlank()) return null
    if (strict) {
        return when (label) {
            GuideTab.Skills.label -> GuideTab.Skills
            GuideTab.Profile.label -> GuideTab.Profile
            GuideTab.Voice.label -> GuideTab.Voice
            GuideTab.Gallery.label -> GuideTab.Gallery
            GuideTab.Simulate.label -> GuideTab.Simulate
            else -> null
        }
    }
    return when {
        label.contains("语音") || label.contains("台词") -> GuideTab.Voice
        label.contains("影画") || label.contains("鉴赏") -> GuideTab.Gallery
        label.contains("养成") || label.contains("模拟") -> GuideTab.Simulate
        label.contains("档案") -> GuideTab.Profile
        label.contains("技能") -> GuideTab.Skills
        else -> null
    }
}

internal fun findImageByKnownKeys(obj: JSONObject, sourceUrl: String): String {
    val keys = listOf(
        "icon", "iconUrl", "icon_url", "tabIcon", "tab_icon",
        "img", "image", "imageUrl", "image_url",
        "thumb", "cover", "src", "url"
    )
    keys.forEach { key ->
        val any = obj.opt(key) ?: return@forEach
        val fromAny = when (any) {
            is String -> normalizeImageUrl(sourceUrl, any)
            is JSONObject, is JSONArray -> firstImageFromAny(any, sourceUrl)
            else -> ""
        }
        if (looksLikeImageUrl(fromAny)) return fromAny
    }
    return ""
}

internal fun extractGuideTabIcons(root: JSONObject, sourceUrl: String): Map<GuideTab, String> {
    val strict = mutableMapOf<GuideTab, String>()
    val fuzzy = mutableMapOf<GuideTab, String>()
    val labelKeys = listOf("name", "title", "label", "tabName", "tab_name", "text", "key")

    fun tryPut(tab: GuideTab?, icon: String, strictMode: Boolean) {
        if (tab == null || icon.isBlank()) return
        if (strictMode) {
            strict.putIfAbsent(tab, icon)
        } else {
            fuzzy.putIfAbsent(tab, icon)
        }
    }

    fun walk(any: Any?) {
        when (any) {
            is JSONObject -> {
                val label = labelKeys
                    .asSequence()
                    .map { key -> any.optString(key).trim() }
                    .firstOrNull { it.isNotBlank() }
                    .orEmpty()
                val icon = findImageByKnownKeys(any, sourceUrl)
                if (label.isNotBlank() && icon.isNotBlank()) {
                    tryPut(mapGuideTabByLabel(label, strict = true), icon, strictMode = true)
                    tryPut(mapGuideTabByLabel(label, strict = false), icon, strictMode = false)
                }

                val keys = any.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val value = any.opt(key)
                    val keyTabStrict = mapGuideTabByLabel(key, strict = true)
                    val keyTabFuzzy = mapGuideTabByLabel(key, strict = false)
                    if ((keyTabStrict != null || keyTabFuzzy != null) && value != null) {
                        val iconByValue = firstImageFromAny(value, sourceUrl)
                        if (iconByValue.isNotBlank()) {
                            tryPut(keyTabStrict, iconByValue, strictMode = true)
                            tryPut(keyTabFuzzy, iconByValue, strictMode = false)
                        }
                    }
                    walk(value)
                }
            }

            is JSONArray -> {
                for (i in 0 until any.length()) {
                    walk(any.opt(i))
                }
            }
        }
    }

    walk(root.opt("dataSource"))
    walk(root.opt("tabs"))
    walk(root)

    return buildMap {
        GuideTab.entries.forEach { tab ->
            val icon = strict[tab].orEmpty().ifBlank { fuzzy[tab].orEmpty() }
            if (icon.isNotBlank()) put(tab, icon)
        }
    }
}

internal fun looksLikeMediaTokenText(raw: String): Boolean {
    val value = raw.trim()
    if (value.isBlank()) return false
    val lower = value.lowercase()
    if (lower.startsWith("http://") || lower.startsWith("https://") || lower.startsWith("//")) return true
    if (lower.contains("cdnimg") || lower.contains("gamekee.com/")) return true
    return Regex("""\.(png|jpg|jpeg|webp|gif|bmp|svg|avif|mp4|webm|mov|m3u8|mp3|ogg|wav|m4a|aac)(\?.*)?(#.*)?$""")
        .containsMatchIn(lower)
}

internal fun extractEditorTextLines(any: Any?, depth: Int = 0): List<String> {
    if (any == null || depth > 10) return emptyList()
    return when (any) {
        is String -> {
            val plain = stripHtml(any)
            if (plain.isBlank() || looksLikeMediaTokenText(plain)) {
                emptyList()
            } else {
                listOf(plain)
            }
        }

        is JSONObject -> {
            val lines = mutableListOf<String>()
            val direct = any.opt("text")
            if (direct is String) {
                val text = stripHtml(direct)
                if (text.isNotBlank()) lines += text
            }
            val richKeys = listOf("children", "data", "content", "title", "name", "label", "desc", "value")
            richKeys.forEach { key ->
                if (!any.has(key)) return@forEach
                lines += extractEditorTextLines(any.opt(key), depth + 1)
            }
            lines
        }

        is JSONArray -> {
            buildList {
                for (i in 0 until any.length()) {
                    addAll(extractEditorTextLines(any.opt(i), depth + 1))
                }
            }
        }

        else -> emptyList()
    }
}

internal fun extractEditorText(any: Any?, separator: String = " "): String {
    val lines = extractEditorTextLines(any)
        .map { it.trim() }
        .filter { it.isNotBlank() }
    if (lines.isEmpty()) return ""
    return lines.joinToString(separator).trim()
}

internal fun normalizeArrayGalleryTitle(rawTitle: String): String {
    val title = stripHtml(rawTitle).replace(Regex("\\s+"), "").trim()
    if (title.isBlank()) return "影画"
    if (title == "表情包" || title.startsWith("表情包(") || title.startsWith("表情包（")) {
        return title.replace("表情包", "角色表情包")
    }
    if (title == "差分" || title == "表情差分") return "角色表情"
    if (title.startsWith("表情")) {
        val suffix = title.removePrefix("表情").trim()
        return "角色表情$suffix"
    }
    if (title.contains("表情差分")) {
        return title.replace("表情差分", "角色表情")
    }
    if (title.contains("差分")) {
        val context = title
            .replace("差分", "")
            .trim('（', '）', '(', ')', '-', '·', ' ')
        if (context.isBlank()) return "角色表情"
        return "角色表情（$context）"
    }
    return title
}

internal fun normalizeArrayProfileKey(rawKey: String): String {
    val key = stripHtml(rawKey).trim()
    if (key.isBlank()) return ""
    return when (key) {
        "所属" -> "所属学园"
        "学院" -> "所属学园"
        "社团" -> "所属社团"
        "兴趣" -> "兴趣爱好"
        "兴趣爱好（补充）" -> "兴趣爱好"
        "其他名字" -> "其他译名"
        "其他称呼" -> "其他译名"
        "译名" -> "其他译名"
        "国际服译名" -> "其他译名"
        "其他翻译" -> "其他译名"
        "黑话(别名)" -> "其他译名"
        "日语全名" -> "假名注音"
        else -> key
    }
}

internal fun parseVoiceCvByLanguageFromRaw(raw: String): Map<String, String> {
    if (raw.isBlank()) return emptyMap()
    val normalized = stripHtml(raw)
        .replace('｜', '|')
        .replace('：', ':')
        .replace('，', ',')
        .replace('；', ';')
        .replace('\u3000', ' ')
        .trim()
    if (normalized.isBlank()) return emptyMap()

    val labelPattern = "日配|日语|日|jp|jpn|中配|中|cn|国语|国配|中文|韩配|韩|kr|kor|korean|官翻|官中|官方翻译|官方中文"
    val pairRegex = Regex(
        """(?i)($labelPattern)\s*(?:[\|:：\-－—])\s*([\s\S]*?)(?=(?:\s*[,，;；/／\n]\s*|\s+)(?:$labelPattern)\s*(?:[\|:：\-－—])|$)"""
    )
    val out = linkedMapOf<String, String>()

    fun assign(labelRaw: String, valueRaw: String) {
        val label = canonicalVoiceLanguageLabel(labelRaw)
        if (label.isBlank()) return
        val value = valueRaw.trim()
            .trim(',', '，', ';', '；', '|', '/', '／')
            .trim()
        if (value.isBlank()) return
        if (out[label].isNullOrBlank()) {
            out[label] = value
        }
    }

    pairRegex.findAll(normalized).forEach { match ->
        val label = match.groupValues.getOrNull(1).orEmpty()
        val value = match.groupValues.getOrNull(2).orEmpty()
        assign(label, value)
    }

    if (out.isEmpty() && normalized.isNotBlank()) {
        out["日配"] = normalized
    }

    if (out.isEmpty()) return emptyMap()
    val ordered = linkedMapOf<String, String>()
    listOf("日配", "中配", "韩配", "官翻").forEach { key ->
        out[key]?.takeIf { it.isNotBlank() }?.let { ordered[key] = it }
    }
    out.forEach { (key, value) ->
        if (key !in ordered && value.isNotBlank()) {
            ordered[key] = value
        }
    }
    return ordered
}

internal fun normalizedGuideGalleryTitle(rawTitle: String): String = rawTitle.replace(" ", "").trim()

internal fun guideGalleryCategoryOrder(rawTitle: String): Int {
    val title = normalizedGuideGalleryTitle(rawTitle)
    return when {
        title.startsWith("立绘") -> 0
        title.startsWith("回忆大厅") && !title.startsWith("回忆大厅视频") -> 1
        title.startsWith("回忆大厅视频") -> 2
        title.startsWith("BGM") -> 3
        title.startsWith("官方介绍") -> 4
        title.startsWith("本家画") -> 5
        title.startsWith("官方衍生") -> 6
        title.startsWith("TV动画设定图") -> 7
        title.startsWith("设定集") -> 8
        isExpressionGalleryItem(BaGuideGalleryItem(title = title, imageUrl = "", mediaUrl = "")) -> 9
        title.startsWith("互动家具") -> 10
        title.startsWith("情人节巧克力") -> 11
        title.startsWith("巧克力图") -> 12
        title.startsWith("PV") -> 13
        title.startsWith("角色演示") -> 14
        title.startsWith("Live") -> 15
        else -> 99
    }
}

internal fun guideGalleryTitleGroupKey(rawTitle: String): String {
    val normalized = normalizedGuideGalleryTitle(rawTitle)
    return normalized.replace(Regex("""\d+$"""), "")
}

internal fun guideGalleryItemIndex(rawTitle: String): Int {
    return Regex("""(\d+)(?!.*\d)""")
        .find(normalizedGuideGalleryTitle(rawTitle))
        ?.groupValues
        ?.getOrNull(1)
        ?.toIntOrNull()
        ?: Int.MAX_VALUE
}
