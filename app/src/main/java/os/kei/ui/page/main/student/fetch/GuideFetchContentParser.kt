package os.kei.ui.page.main.student.fetch

import os.kei.ui.page.main.student.fetch.parser.parseGuideDetailFromArrayContentJson
import os.kei.ui.page.main.student.fetch.parser.parseGuideDetailFromObjectContentJson

internal fun parseGuideDetailFromContentJson(raw: String, sourceUrl: String): GuideDetailExtract {
    if (raw.isBlank()) return GuideDetailExtract()
    val trimmed = raw.trimStart()
    if (trimmed.startsWith("[")) {
        return parseGuideDetailFromArrayContentJson(trimmed, sourceUrl)
    }
    return parseGuideDetailFromObjectContentJson(trimmed, sourceUrl)
}
