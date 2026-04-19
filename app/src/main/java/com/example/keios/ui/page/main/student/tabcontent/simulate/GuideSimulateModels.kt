package com.example.keios.ui.page.main.student.tabcontent.simulate

import com.example.keios.ui.page.main.student.fetch.normalizeGuideUrl
import com.example.keios.ui.page.main.student.tabcontent.profile.normalizeProfileFieldKey
import com.example.keios.ui.page.main.student.tabcontent.profile.normalizedTopDataStatKeys
import com.example.keios.ui.page.main.student.tabcontent.profile.splitGuideCompositeValues
import java.util.LinkedHashMap
import java.util.Locale
import kotlin.collections.plusAssign
import kotlin.math.abs

internal const val GUIDE_SIMULATE_CACHE_MAX_SIZE = 96
internal val guideSimulateDataCache = object : java.util.LinkedHashMap<String, GuideSimulateData>(
    GUIDE_SIMULATE_CACHE_MAX_SIZE,
    0.75f,
    true
) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, GuideSimulateData>?): Boolean {
        return size > GUIDE_SIMULATE_CACHE_MAX_SIZE
    }
}

internal data class GuideSimulateData(
    val initialHint: String = "",
    val initialRows: List<com.example.keios.ui.page.main.student.BaGuideRow> = emptyList(),
    val maxHint: String = "",
    val maxRows: List<com.example.keios.ui.page.main.student.BaGuideRow> = emptyList(),
    val weaponHint: String = "",
    val weaponRows: List<com.example.keios.ui.page.main.student.BaGuideRow> = emptyList(),
    val equipmentHint: String = "",
    val equipmentRows: List<com.example.keios.ui.page.main.student.BaGuideRow> = emptyList(),
    val favorHint: String = "",
    val favorRows: List<com.example.keios.ui.page.main.student.BaGuideRow> = emptyList(),
    val unlockHint: String = "",
    val unlockRows: List<com.example.keios.ui.page.main.student.BaGuideRow> = emptyList(),
    val bondHint: String = "",
    val bondRows: List<com.example.keios.ui.page.main.student.BaGuideRow> = emptyList()
)

internal data class SimulateEquipmentGroup(
    val slotLabel: String,
    val itemName: String,
    val tierText: String,
    val iconUrl: String,
    val statRows: List<com.example.keios.ui.page.main.student.BaGuideRow>
)

internal data class SimulateBondGroup(
    val roleLabel: String,
    val iconUrl: String,
    val statRows: List<com.example.keios.ui.page.main.student.BaGuideRow>
)

internal data class SimulateUnlockViewData(
    val levelCapsule: String,
    val rows: List<com.example.keios.ui.page.main.student.BaGuideRow>
)

internal data class SimulateWeaponViewData(
    val imageUrl: String,
    val statRows: List<com.example.keios.ui.page.main.student.BaGuideRow>
)

internal fun sanitizeSimulateFavorRows(rows: List<com.example.keios.ui.page.main.student.BaGuideRow>): List<com.example.keios.ui.page.main.student.BaGuideRow> {
    if (rows.isEmpty()) return emptyList()
    return rows
        .filterNot { row ->
            row.key.isBlank() &&
                row.value.isBlank() &&
                row.imageUrl.isBlank() &&
                row.imageUrls.isEmpty()
        }
        .filterNot { row ->
            val normalizedKey = normalizeProfileFieldKey(row.key)
            val hasMedia = row.imageUrl.isNotBlank() || row.imageUrls.isNotEmpty()
            val isTierMetaKey = Regex(
                """^t\d+(效果|所需升级材料|技能图标)$""",
                RegexOption.IGNORE_CASE
            ).matches(normalizedKey)
            isTierMetaKey && row.value.isBlank() && !hasMedia
        }
        .filterNot { row ->
            val normalizedKey = normalizeProfileFieldKey(row.key)
            val normalizedValue = normalizeProfileFieldKey(row.value)
            val hasMedia = row.imageUrl.isNotBlank() || row.imageUrls.isNotEmpty()
            val hasNumericValue = Regex("""\d""").containsMatchIn(row.value)
            val isBrokenStatPair =
                isLikelySimulateStatLabel(row.key) &&
                    isLikelySimulateStatLabel(row.value) &&
                    !hasNumericValue &&
                    !hasMedia
            val isTierLabel = Regex("""^T\d+$""", RegexOption.IGNORE_CASE).matches(normalizedKey)
            val isTierOnlyPlaceholder =
                isTierLabel &&
                    normalizedValue.isBlank() &&
                    !hasMedia
            isBrokenStatPair || isTierOnlyPlaceholder
        }
        .distinctBy { row ->
            val packedImages = row.imageUrls.joinToString("|")
            "${normalizeProfileFieldKey(row.key)}|${row.value.trim()}|${row.imageUrl.trim()}|$packedImages"
        }
}

internal fun sanitizeSimulateBondRows(rows: List<com.example.keios.ui.page.main.student.BaGuideRow>): List<com.example.keios.ui.page.main.student.BaGuideRow> {
    if (rows.isEmpty()) return emptyList()
    return rows.filterNot { row ->
        val normalizedKey = normalizeProfileFieldKey(row.key)
        Regex("""^羁绊角色\d+$""").matches(normalizedKey) &&
            row.value.isBlank() &&
            row.imageUrl.isBlank() &&
            row.imageUrls.isEmpty()
    }
}

internal fun parseSimulateEquipmentGhostMediaUrl(row: com.example.keios.ui.page.main.student.BaGuideRow): String {
    val key = row.key.trim()
    if (key.isBlank()) return ""
    if (row.value.trim().isNotBlank()) return ""
    val looksLikeUrl = key.startsWith("//") ||
        key.startsWith("http://", ignoreCase = true) ||
        key.startsWith("https://", ignoreCase = true)
    if (!looksLikeUrl) return ""
    val isMediaUrl = Regex(
        """(?i)\.(png|jpe?g|webp|gif|bmp|mp4|webm|mkv|mov|mp3|ogg|wav|m4a)(\?.*)?$"""
    ).containsMatchIn(key)
    if (!isMediaUrl) return ""
    return normalizeGuideUrl(key)
}

internal fun isSimulateEquipmentGhostMediaRow(row: com.example.keios.ui.page.main.student.BaGuideRow): Boolean {
    return parseSimulateEquipmentGhostMediaUrl(row).isNotBlank()
}

internal fun buildSimulateEquipmentGroups(rows: List<com.example.keios.ui.page.main.student.BaGuideRow>): List<SimulateEquipmentGroup> {
    if (rows.isEmpty()) return emptyList()

    val groups = mutableListOf<SimulateEquipmentGroup>()
    var currentSlot = ""
    var currentItemName = ""
    var currentTierText = ""
    var currentIcon = ""
    val currentStats = mutableListOf<com.example.keios.ui.page.main.student.BaGuideRow>()

    fun commitGroup() {
        if (currentSlot.isBlank() && currentItemName.isBlank() && currentStats.isEmpty()) return
        groups += SimulateEquipmentGroup(
            slotLabel = currentSlot.ifBlank { "装备" },
            itemName = currentItemName,
            tierText = currentTierText,
            iconUrl = currentIcon,
            statRows = currentStats.toList()
        )
        currentSlot = ""
        currentItemName = ""
        currentTierText = ""
        currentIcon = ""
        currentStats.clear()
    }

    rows.forEach { row ->
        val key = row.key.trim()
        val normalizedKey = normalizeProfileFieldKey(key)
        val rowIcon = row.imageUrl.trim().ifBlank { row.imageUrls.firstOrNull().orEmpty() }

        if (Regex("""^\d+号装备$""").matches(normalizedKey)) {
            commitGroup()
            currentSlot = key
            if (rowIcon.isNotBlank()) {
                currentIcon = rowIcon
            }
            return@forEach
        }

        val ghostMediaUrl = parseSimulateEquipmentGhostMediaUrl(row)
        if (ghostMediaUrl.isNotBlank()) {
            currentIcon = ghostMediaUrl
            return@forEach
        }

        val isMetaRow = !isLikelySimulateStatLabel(key) && !isSimulateSubHeader(key)
        if (currentItemName.isBlank() && isMetaRow) {
            currentItemName = key
            currentTierText = row.value.trim()
            if (currentIcon.isBlank() && rowIcon.isNotBlank()) {
                currentIcon = rowIcon
            }
            return@forEach
        }

        if (key.isBlank() && row.value.isBlank()) return@forEach
        currentStats += row.copy(imageUrl = "", imageUrls = emptyList())
    }
    commitGroup()

    return groups
}

internal fun buildSimulateUnlockViewData(
    rows: List<com.example.keios.ui.page.main.student.BaGuideRow>,
    hint: String
): SimulateUnlockViewData {
    if (rows.isEmpty()) {
        return SimulateUnlockViewData(
            levelCapsule = extractSimulateLevelCapsule(hint),
            rows = emptyList()
        )
    }

    val levelRowIndex = rows.indexOfFirst { row ->
        Regex("""^\d+级$""").matches(normalizeProfileFieldKey(row.key))
    }
    val capsule = when {
        levelRowIndex >= 0 -> rows[levelRowIndex].key.trim()
        else -> extractSimulateLevelCapsule(hint)
    }
    val contentRows = if (levelRowIndex >= 0) {
        rows.filterIndexed { index, _ -> index != levelRowIndex }
    } else {
        rows
    }
    return SimulateUnlockViewData(
        levelCapsule = capsule,
        rows = contentRows
    )
}

internal fun buildSimulateBondGroups(rows: List<com.example.keios.ui.page.main.student.BaGuideRow>): List<SimulateBondGroup> {
    if (rows.isEmpty()) return emptyList()

    val groups = mutableListOf<SimulateBondGroup>()
    var currentRole = ""
    var currentIcon = ""
    val currentRows = mutableListOf<com.example.keios.ui.page.main.student.BaGuideRow>()

    fun commitGroup() {
        if (currentRole.isBlank() && currentRows.isEmpty()) return
        groups += SimulateBondGroup(
            roleLabel = currentRole.ifBlank { "羁绊角色" },
            iconUrl = currentIcon,
            statRows = currentRows.toList()
        )
        currentRole = ""
        currentIcon = ""
        currentRows.clear()
    }

    rows.forEach { row ->
        val key = row.key.trim()
        val normalizedKey = normalizeProfileFieldKey(key)
        val rowIcon = row.imageUrl.trim().ifBlank { row.imageUrls.firstOrNull().orEmpty() }

        if (Regex("""^羁绊角色\d+$""").matches(normalizedKey)) {
            commitGroup()
            currentRole = key
            currentIcon = rowIcon
            return@forEach
        }

        if (key.isBlank() && row.value.isBlank()) return@forEach
        currentRows += row.copy(
            imageUrl = "",
            imageUrls = emptyList()
        )
    }
    commitGroup()

    return groups
}

internal fun buildSimulateWeaponViewData(rows: List<com.example.keios.ui.page.main.student.BaGuideRow>): SimulateWeaponViewData {
    if (rows.isEmpty()) return SimulateWeaponViewData("", emptyList())
    val imageUrl = rows.firstNotNullOfOrNull { row ->
        row.imageUrl.trim().ifBlank { row.imageUrls.firstOrNull().orEmpty() }.takeIf { it.isNotBlank() }
    }.orEmpty()
    val statRows = rows
        .filter { row ->
            val key = row.key.trim()
            val value = row.value.trim()
            key.isNotBlank() || value.isNotBlank()
        }
        .map { row ->
            val rowIcon = row.imageUrl.trim().ifBlank { row.imageUrls.firstOrNull().orEmpty() }
            if (imageUrl.isNotBlank() && rowIcon == imageUrl) {
                row.copy(imageUrl = "", imageUrls = emptyList())
            } else {
                row
            }
        }
    return SimulateWeaponViewData(
        imageUrl = imageUrl,
        statRows = statRows
    )
}

internal fun extractSimulateLevelCapsule(rawHint: String): String {
    val hint = rawHint.trim().trim('*').trim()
    if (hint.isBlank()) return ""

    Regex("""(?i)Lv\s*\d+""")
        .find(hint)
        ?.value
        ?.replace(" ", "")
        ?.let { raw ->
            val digits = Regex("""\d+""").find(raw)?.value.orEmpty()
            if (digits.isNotBlank()) return "Lv$digits"
        }

    Regex("""(?i)T\d+""")
        .find(hint)
        ?.value
        ?.replace(" ", "")
        ?.uppercase()
        ?.let { return it }

    Regex("""\d+级""")
        .find(hint)
        ?.value
        ?.let { return it }

    return ""
}

internal fun isSimulateSubHeader(key: String): Boolean {
    val normalized = normalizeProfileFieldKey(key)
    if (Regex("""^\d+号装备$""").matches(normalized)) return true
    if (Regex("""^羁绊角色\d+$""").matches(normalized)) return true
    if (Regex("""^\d+级$""").matches(normalized)) return true
    return false
}

internal fun simulateStatGlyphForKey(raw: String): String? {
    val key = normalizeProfileFieldKey(raw)
    return when (key) {
        normalizeProfileFieldKey("攻击力") -> "✢"
        normalizeProfileFieldKey("防御力") -> "⛨"
        normalizeProfileFieldKey("生命值") -> "♥"
        normalizeProfileFieldKey("治愈力") -> "✚"
        normalizeProfileFieldKey("命中值") -> "◎"
        normalizeProfileFieldKey("闪避值") -> "◌"
        normalizeProfileFieldKey("暴击值") -> "✶"
        normalizeProfileFieldKey("暴击伤害") -> "✹"
        normalizeProfileFieldKey("稳定值") -> "≋"
        normalizeProfileFieldKey("射程") -> "➚"
        normalizeProfileFieldKey("群控强化力") -> "⬆"
        normalizeProfileFieldKey("群控抵抗力") -> "⬡"
        normalizeProfileFieldKey("装弹数") -> "☰"
        normalizeProfileFieldKey("防御无视值") -> "⊘"
        normalizeProfileFieldKey("受恢复率") -> "⟳"
        normalizeProfileFieldKey("COST恢复力") -> "⌛"
        normalizeProfileFieldKey("暴击抵抗值") -> "⛯"
        normalizeProfileFieldKey("暴伤抵抗率"),
        normalizeProfileFieldKey("暴击伤害抵抗率") -> "✺"
        else -> null
    }
}

internal fun buildSimulateMaxDeltaText(
    maxValue: String,
    initialValue: String?
): String {
    val maxText = maxValue.trim()
    val initialText = initialValue?.trim().orEmpty()
    if (maxText.isBlank() || initialText.isBlank()) return ""
    if (Regex("""\([+-]\d+(\.\d+)?\)""").containsMatchIn(maxText)) return ""

    val maxNumber = extractComparableNumber(maxText) ?: return ""
    val initialNumber = extractComparableNumber(initialText) ?: return ""
    val diff = maxNumber - initialNumber
    if (abs(diff) < 0.0001) return ""

    val sign = if (diff > 0) "+" else "-"
    val absDiff = abs(diff)
    val deltaText = if (abs(absDiff - absDiff.toLong().toDouble()) < 0.0001) {
        absDiff.toLong().toString()
    } else {
        String.format(Locale.US, "%.2f", absDiff).trimEnd('0').trimEnd('.')
    }
    return "($sign$deltaText)"
}

internal fun extractComparableNumber(raw: String): Double? {
    val normalized = raw.replace(",", "").trim()
    val numberText = Regex("""-?\d+(\.\d+)?""")
        .find(normalized)
        ?.value
        .orEmpty()
    return numberText.toDoubleOrNull()
}

internal fun buildGuideSimulateCacheKey(rows: List<com.example.keios.ui.page.main.student.BaGuideRow>): String {
    var hash = 17
    rows.forEach { row ->
        hash = 31 * hash + normalizeProfileFieldKey(row.key).hashCode()
        hash = 31 * hash + row.value.trim().hashCode()
        hash = 31 * hash + row.imageUrl.trim().hashCode()
        row.imageUrls.forEach { image ->
            hash = 31 * hash + image.trim().hashCode()
        }
    }
    return "${rows.size}|$hash"
}

internal fun buildGuideSimulateData(rows: List<com.example.keios.ui.page.main.student.BaGuideRow>): GuideSimulateData {
    if (rows.isEmpty()) return GuideSimulateData()
    val cacheKey = buildGuideSimulateCacheKey(rows)
    synchronized(guideSimulateDataCache) {
        guideSimulateDataCache[cacheKey]?.let { return it }
    }
    val sections = linkedMapOf<String, MutableList<com.example.keios.ui.page.main.student.BaGuideRow>>()
    val hints = mutableMapOf<String, String>()
    var currentSection = ""

    rows.forEach { row ->
        val header = resolveSimulateSectionName(row.key)
        if (header != null) {
            currentSection = header
            sections.getOrPut(header) { mutableListOf() }
            val hint = row.value.trim().trim('*').trim()
            if (hint.isNotBlank()) {
                hints[header] = hint
            }
            return@forEach
        }
        if (currentSection.isBlank()) return@forEach

        val cleaned = row.copy(
            key = row.key.trim(),
            value = row.value.trim(),
            imageUrl = row.imageUrl.trim(),
            imageUrls = row.imageUrls.map { it.trim() }.filter { it.isNotBlank() }.distinct()
        )
        if (
            cleaned.key.isBlank() &&
            cleaned.value.isBlank() &&
            cleaned.imageUrl.isBlank() &&
            cleaned.imageUrls.isEmpty()
        ) return@forEach
        sections.getOrPut(currentSection) { mutableListOf() } += cleaned
    }

    val computed = GuideSimulateData(
        initialHint = hints["初始数据"].orEmpty(),
        initialRows = expandSimulateRows(sections["初始数据"].orEmpty()),
        maxHint = hints["顶级数据"].orEmpty(),
        maxRows = expandSimulateRows(sections["顶级数据"].orEmpty()),
        weaponHint = hints["专武"].orEmpty(),
        weaponRows = expandSimulateRows(sections["专武"].orEmpty()),
        equipmentHint = hints["装备"].orEmpty(),
        equipmentRows = expandSimulateRows(sections["装备"].orEmpty()),
        favorHint = hints["爱用品"].orEmpty(),
        favorRows = sanitizeSimulateFavorRows(
            expandSimulateRows(sections["爱用品"].orEmpty())
        ),
        unlockHint = hints["能力解放"].orEmpty(),
        unlockRows = expandSimulateRows(sections["能力解放"].orEmpty()),
        bondHint = hints["羁绊等级奖励"].orEmpty(),
        bondRows = sanitizeSimulateBondRows(
            expandSimulateRows(sections["羁绊等级奖励"].orEmpty())
        )
    )
    synchronized(guideSimulateDataCache) {
        guideSimulateDataCache[cacheKey] = computed
    }
    return computed
}

internal fun resolveSimulateSectionName(rawKey: String): String? {
    val normalized = normalizeProfileFieldKey(rawKey)
    return when {
        normalized == normalizeProfileFieldKey("初始数据") -> "初始数据"
        normalized == normalizeProfileFieldKey("顶级数据") -> "顶级数据"
        normalized == normalizeProfileFieldKey("专武") -> "专武"
        normalized == normalizeProfileFieldKey("装备") -> "装备"
        normalized == normalizeProfileFieldKey("爱用品") -> "爱用品"
        normalized == normalizeProfileFieldKey("能力解放") -> "能力解放"
        normalized == normalizeProfileFieldKey("羁绊等级奖励") -> "羁绊等级奖励"
        else -> null
    }
}

internal fun isLikelySimulateStatLabel(raw: String): Boolean {
    val normalized = normalizeProfileFieldKey(raw)
    if (normalized in normalizedTopDataStatKeys) return true
    val extraStatKeys = setOf(
        "暴伤抵抗率",
        "暴击抵抗值",
        "暴伤抵抗值"
    ).map(::normalizeProfileFieldKey).toSet()
    if (normalized in extraStatKeys) return true
    return normalized.endsWith("值") || normalized.endsWith("率")
}

internal fun expandSimulateRows(rows: List<com.example.keios.ui.page.main.student.BaGuideRow>): List<com.example.keios.ui.page.main.student.BaGuideRow> {
    if (rows.isEmpty()) return emptyList()
    val expanded = mutableListOf<com.example.keios.ui.page.main.student.BaGuideRow>()
    rows.forEach { row ->
        val key = row.key.trim()
        val value = row.value.trim()
        val icon = row.imageUrl.trim().ifBlank { row.imageUrls.firstOrNull().orEmpty() }
        val images = row.imageUrls.ifEmpty { listOfNotNull(icon.takeIf { it.isNotBlank() }) }
        if (key.isBlank() && value.isBlank() && images.isEmpty()) return@forEach

        if (value.isBlank()) {
            if (key.isNotBlank() || icon.isNotBlank()) {
                expanded += _root_ide_package_.com.example.keios.ui.page.main.student.BaGuideRow(
                    key = key.ifBlank { "信息" },
                    value = "",
                    imageUrl = icon,
                    imageUrls = images
                )
            }
            return@forEach
        }

        val tokens = splitGuideCompositeValues(value)
        if (tokens.isEmpty()) {
            expanded += _root_ide_package_.com.example.keios.ui.page.main.student.BaGuideRow(
                key = key.ifBlank { "信息" },
                value = value,
                imageUrl = icon,
                imageUrls = images
            )
            return@forEach
        }

        val firstTokenLooksLikeStat = isLikelySimulateStatLabel(tokens.first())
        var index = 0
        if (!firstTokenLooksLikeStat) {
            expanded += _root_ide_package_.com.example.keios.ui.page.main.student.BaGuideRow(
                key = key.ifBlank { "等级" },
                value = tokens.first().trim(),
                imageUrl = icon,
                imageUrls = images
            )
            index = 1
        } else if (key.isNotBlank() && !isLikelySimulateStatLabel(key) && !isSimulateSubHeader(key)) {
            expanded += _root_ide_package_.com.example.keios.ui.page.main.student.BaGuideRow(
                key = key,
                value = "",
                imageUrl = icon,
                imageUrls = images
            )
        } else if (icon.isNotBlank() && key.isNotBlank()) {
            expanded += _root_ide_package_.com.example.keios.ui.page.main.student.BaGuideRow(
                key = key,
                value = "",
                imageUrl = icon,
                imageUrls = images
            )
        }

        var pairIndex = 0
        while (index + 1 < tokens.size) {
            val statKey = tokens[index].trim()
            val statValue = tokens[index + 1].trim()
            if (statKey.isNotBlank() && statValue.isNotBlank()) {
                val pairIcon = if (images.size > 1) images.getOrNull(pairIndex).orEmpty() else ""
                expanded += _root_ide_package_.com.example.keios.ui.page.main.student.BaGuideRow(
                    key = statKey,
                    value = statValue,
                    imageUrl = pairIcon,
                    imageUrls = listOfNotNull(pairIcon.takeIf { it.isNotBlank() })
                )
            }
            pairIndex += 1
            index += 2
        }
    }

    return expanded.distinctBy { row ->
        val packedImages = row.imageUrls.joinToString("|")
        "${normalizeProfileFieldKey(row.key)}|${row.value.trim()}|${row.imageUrl.trim()}|$packedImages"
    }
}
