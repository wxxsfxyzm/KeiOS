package com.example.keios.ui.page.main.student

internal fun parseGuideDetailFromContentJson(raw: String, sourceUrl: String): GuideDetailExtract {
    if (raw.isBlank()) return GuideDetailExtract()
    val trimmed = raw.trimStart()
    if (trimmed.startsWith("[")) {
        return parseGuideDetailFromArrayContentJson(trimmed, sourceUrl)
    }
    return parseGuideDetailFromObjectContentJson(trimmed, sourceUrl)
}
