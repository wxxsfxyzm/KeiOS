package os.kei.ui.page.main.student

import os.kei.ui.page.main.student.fetch.normalizeGuideUrl

internal const val BA_GUIDE_KV_ID = "ba_student_guide"
internal const val BA_GUIDE_KEY_CURRENT_URL = "current_url"
internal const val BA_GUIDE_KEY_LEGACY_CACHE_PREFIX = "cache_"
internal const val BA_GUIDE_KEY_V2_CACHE_PREFIX = "guide_cache_v2_"
internal const val BA_GUIDE_KEY_V2_INDEX = "guide_cache_v2_index"
internal const val BA_GUIDE_CACHE_SCHEMA_VERSION = 2
internal const val BA_GUIDE_MEMORY_CACHE_LIMIT = 8

internal const val CACHE_SUFFIX_META = "meta"
internal const val CACHE_SUFFIX_STATS = "stats"
internal const val CACHE_SUFFIX_SKILL = "skill"
internal const val CACHE_SUFFIX_PROFILE = "profile"
internal const val CACHE_SUFFIX_GALLERY = "gallery"
internal const val CACHE_SUFFIX_GROWTH = "growth"
internal const val CACHE_SUFFIX_SIMULATE = "simulate"
internal const val CACHE_SUFFIX_VOICE_ROWS = "voice_rows"
internal const val CACHE_SUFFIX_VOICE_ENTRIES = "voice_entries"

internal val BA_GUIDE_CACHE_REQUIRED_SUFFIXES = listOf(
    CACHE_SUFFIX_META,
    CACHE_SUFFIX_STATS,
    CACHE_SUFFIX_SKILL,
    CACHE_SUFFIX_PROFILE,
    CACHE_SUFFIX_GALLERY,
    CACHE_SUFFIX_GROWTH,
    CACHE_SUFFIX_SIMULATE,
    CACHE_SUFFIX_VOICE_ROWS,
    CACHE_SUFFIX_VOICE_ENTRIES
)

internal fun normalizeStudentGuideSourceUrl(url: String): String {
    return normalizeGuideUrl(url).trim()
}

internal fun guideCacheId(url: String): String {
    return normalizeStudentGuideSourceUrl(url).hashCode().toUInt().toString(16)
}

internal fun guideLegacyCacheKey(url: String): String {
    return BA_GUIDE_KEY_LEGACY_CACHE_PREFIX + guideCacheId(url)
}

internal fun guideV2EntryPrefix(id: String): String {
    return "$BA_GUIDE_KEY_V2_CACHE_PREFIX${id}_"
}

internal fun guideV2CacheKey(id: String, suffix: String): String {
    return "${guideV2EntryPrefix(id)}$suffix"
}
