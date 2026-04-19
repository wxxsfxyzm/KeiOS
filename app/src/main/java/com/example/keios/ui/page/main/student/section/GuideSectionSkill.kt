package com.example.keios.ui.page.main.student.section

import com.example.keios.ui.page.main.widget.GlassVariant
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.keios.ui.page.main.student.GuideRemoteIcon
import com.example.keios.ui.page.main.student.GuideSkillCardModel
import com.example.keios.ui.page.main.student.guideCircledNumbers
import com.example.keios.ui.page.main.student.guideSkillTypeBracketPattern
import com.example.keios.ui.page.main.student.guideSkillTypeCircledSuffixPattern
import com.example.keios.ui.page.main.student.guideSkillTypeNumericSuffixPattern
import com.example.keios.ui.page.main.student.guideSkillTypeStateSplitPattern
import com.example.keios.ui.page.main.student.sanitizeGuideSkillLabelForDisplay
import com.example.keios.ui.page.main.widget.GlassTextButton
import com.example.keios.ui.page.main.widget.CopyModeSelectionContainer
import com.kyant.backdrop.Backdrop
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import com.example.keios.ui.page.main.widget.AppDropdownSelector
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun GuideSkillCardItem(
    card: GuideSkillCardModel,
    backdrop: Backdrop?,
    modifier: Modifier = Modifier
) {
    var showLevelPopup by remember(card.id) { mutableStateOf(false) }
    var levelPopupAnchorBounds by remember(card.id) { mutableStateOf<IntRect?>(null) }
    var skillTitleRowHeightPx by remember(card.id) { mutableStateOf(0) }
    val levelOptions = card.levelOptions
    var selectedLevel by rememberSaveable(card.id) { mutableStateOf(card.defaultLevel) }
    var typeCapsuleHeightPx by remember(card.id, selectedLevel) { mutableStateOf(0) }
    var typeStateBlockHeightPx by remember(card.id, selectedLevel) { mutableStateOf(0) }
    var typeSubRowHeightPx by remember(card.id, selectedLevel) { mutableStateOf(0) }
    var skillNameLineCount by remember(card.id, selectedLevel) { mutableStateOf(1) }
    var descriptionLineCount by remember(card.id, selectedLevel) { mutableStateOf(1) }
    val density = LocalDensity.current

    LaunchedEffect(card.id, card.defaultLevel, levelOptions) {
        if (levelOptions.isEmpty()) {
            selectedLevel = card.defaultLevel
        } else if (selectedLevel !in levelOptions) {
            selectedLevel = card.defaultLevel
        }
    }

    val skillDesc = card.descriptionFor(selectedLevel)
    val skillCost = card.costFor(selectedLevel)
    val displayLevel = selectedLevel.ifBlank { card.defaultLevel }
    val parsedSkillType = remember(card.type) { parseGuideSkillTypeMeta(card.type) }
    val displaySkillType = parsedSkillType.baseType
    val skillTypeStateTags = parsedSkillType.stateTags
    val skillTypeVariantBadge = parsedSkillType.variantIndex?.let(::toGuideCircledNumber)
    val hasTypeStateBlock = skillTypeStateTags.isNotEmpty()
    val hasTypeSubRow = skillTypeVariantBadge != null || levelOptions.isNotEmpty()
    val isExSkill = remember(card.type) { card.type.contains("EX", ignoreCase = true) }
    val hasSkillMetaColumn = remember(displaySkillType, skillCost, levelOptions, skillTypeVariantBadge, skillTypeStateTags) {
        displaySkillType.isNotBlank() ||
            skillCost.isNotBlank() ||
            levelOptions.isNotEmpty() ||
            skillTypeVariantBadge != null ||
            skillTypeStateTags.isNotEmpty()
    }
    val metaColumnShouldTopAlign = descriptionLineCount >= 3
    val skillNameTooLong = skillNameLineCount > 1
    val typeAlignToTitleOffset = if (
        displaySkillType.isNotBlank() &&
        !skillNameTooLong &&
        skillTitleRowHeightPx > 0 &&
        typeCapsuleHeightPx > 0
    ) {
        with(density) { ((skillTitleRowHeightPx - typeCapsuleHeightPx).coerceAtLeast(0) / 2).toDp() }
    } else {
        0.dp
    }
    val descriptionTopOffsetDp = with(density) { skillTitleRowHeightPx.toDp() } + 8.dp
    val stateBlockHeightDp = if (hasTypeStateBlock) {
        if (typeStateBlockHeightPx > 0) {
            with(density) { typeStateBlockHeightPx.toDp() }
        } else {
            26.dp
        }
    } else {
        0.dp
    }
    val subRowHeightDp = if (hasTypeSubRow) {
        if (typeSubRowHeightPx > 0) {
            with(density) { typeSubRowHeightPx.toDp() }
        } else {
            30.dp
        }
    } else {
        0.dp
    }
    val occupiedBeforeCostDp = when {
        displaySkillType.isNotBlank() -> {
            val typeBottomSpacing = when {
                hasTypeStateBlock && hasTypeSubRow -> 4.dp + stateBlockHeightDp + 4.dp + subRowHeightDp + 4.dp
                hasTypeStateBlock -> 4.dp + stateBlockHeightDp + 4.dp
                hasTypeSubRow -> 4.dp + subRowHeightDp + 4.dp
                else -> 4.dp
            }
            typeAlignToTitleOffset + with(density) { typeCapsuleHeightPx.toDp() } + typeBottomSpacing
        }
        hasTypeStateBlock && hasTypeSubRow -> stateBlockHeightDp + 4.dp + subRowHeightDp + 4.dp
        hasTypeStateBlock -> stateBlockHeightDp + 4.dp
        hasTypeSubRow -> subRowHeightDp + 4.dp
        else -> 0.dp
    }
    val costAlignToDescriptionOffset = if (metaColumnShouldTopAlign) {
        (descriptionTopOffsetDp - occupiedBeforeCostDp).coerceAtLeast(0.dp)
    } else {
        0.dp
    }
    val skillCopyPayload = remember(
        card.name,
        displaySkillType,
        displayLevel,
        skillCost,
        skillDesc,
        skillTypeStateTags,
        skillTypeVariantBadge
    ) {
        buildGuideSkillCopyPayload(
            name = card.name,
            skillType = displaySkillType,
            level = displayLevel,
            cost = skillCost,
            desc = skillDesc,
            stateTags = skillTypeStateTags,
            variantBadge = skillTypeVariantBadge
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.defaultColors(
            color = Color(0x223B82F6),
            contentColor = MiuixTheme.colorScheme.onBackground
        ),
        onClick = {}
    ) {
        CopyModeSelectionContainer {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .guideCopyable(skillCopyPayload)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onSizeChanged { skillTitleRowHeightPx = it.height },
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (card.iconUrl.isNotBlank()) {
                            GuideRemoteIcon(
                                imageUrl = card.iconUrl,
                                modifier = Modifier.alignBy { it.measuredHeight / 2 },
                                iconWidth = 34.dp,
                                iconHeight = 34.dp
                            )
                        }
                        Text(
                            text = card.name,
                            modifier = Modifier
                                .weight(1f)
                                .alignBy { it.measuredHeight / 2 },
                            color = MiuixTheme.colorScheme.onBackground,
                            maxLines = if (isExSkill) 2 else 1,
                            overflow = TextOverflow.Ellipsis,
                            onTextLayout = { layoutResult ->
                                val safeLineCount = layoutResult.lineCount.coerceAtLeast(1)
                                if (skillNameLineCount != safeLineCount) {
                                    skillNameLineCount = safeLineCount
                                }
                            }
                        )
                    }
                    GuideSkillDescriptionText(
                        description = skillDesc.ifBlank { "暂未解析到技能描述。" },
                        glossaryIcons = card.glossaryIcons,
                        descriptionIcons = card.descriptionIconsFor(selectedLevel),
                        onLineCountChanged = { lineCount ->
                            val safeLineCount = lineCount.coerceAtLeast(1)
                            if (descriptionLineCount != safeLineCount) {
                                descriptionLineCount = safeLineCount
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (hasSkillMetaColumn) {
                    Column(
                        modifier = Modifier.widthIn(min = 68.dp, max = 90.dp),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (displaySkillType.isNotBlank()) {
                            if (typeAlignToTitleOffset > 0.dp) {
                                Spacer(modifier = Modifier.height(typeAlignToTitleOffset))
                            }
                            Box(modifier = Modifier.onSizeChanged { typeCapsuleHeightPx = it.height }) {
                                GlassTextButton(
                                    backdrop = backdrop,
                                    text = displaySkillType,
                                    enabled = false,
                                    textColor = Color(0xFF3B82F6),
                                    variant = GlassVariant.Compact,
                                    minHeight = 30.dp,
                                    horizontalPadding = 10.dp,
                                    verticalPadding = 6.dp,
                                    onClick = {}
                                )
                            }
                        }
                        if (hasTypeStateBlock) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onSizeChanged { typeStateBlockHeightPx = it.height },
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                skillTypeStateTags.forEach { stateTag ->
                                    GuideSkillStateTagButton(
                                        label = stateTag,
                                        backdrop = backdrop
                                    )
                                }
                            }
                        }
                        if (hasTypeSubRow) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onSizeChanged { typeSubRowHeightPx = it.height },
                                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (skillTypeVariantBadge != null) {
                                    GuideSkillVariantBadge(
                                        label = skillTypeVariantBadge
                                    )
                                }
                                if (levelOptions.isNotEmpty()) {
                                    AppDropdownSelector(
                                        selectedText = displayLevel,
                                        options = levelOptions,
                                        selectedIndex = levelOptions.indexOf(selectedLevel).coerceAtLeast(0),
                                        expanded = showLevelPopup,
                                        anchorBounds = levelPopupAnchorBounds,
                                        onExpandedChange = { showLevelPopup = it },
                                        onSelectedIndexChange = { selected ->
                                            selectedLevel = levelOptions[selected]
                                        },
                                        onAnchorBoundsChange = { levelPopupAnchorBounds = it },
                                        backdrop = backdrop,
                                        variant = GlassVariant.Compact,
                                        minHeight = 30.dp,
                                        horizontalPadding = 10.dp,
                                        verticalPadding = 6.dp
                                    )
                                }
                            }
                        }
                        if (costAlignToDescriptionOffset > 0.dp && skillCost.isNotBlank()) {
                            Spacer(modifier = Modifier.height(costAlignToDescriptionOffset))
                        }
                        if (skillCost.isNotBlank()) {
                            GlassTextButton(
                                backdrop = backdrop,
                                text = "COST:$skillCost",
                                enabled = false,
                                textColor = Color(0xFF3B82F6),
                                variant = GlassVariant.Compact,
                                minHeight = 30.dp,
                                horizontalPadding = 10.dp,
                                verticalPadding = 6.dp,
                                onClick = {}
                            )
                        }
                    }
                }
            }
        }
    }
}

internal data class GuideSkillTypeMeta(
    val baseType: String,
    val variantIndex: Int? = null,
    val stateTags: List<String> = emptyList()
)

internal data class GuideSkillOwnedTypeMeta(
    val ownerTag: String,
    val skillType: String
)

private val guideSkillOwnedTypePattern = Regex(
    """^(「[^」]{1,40}」|『[^』]{1,40}』|【[^】]{1,40}】|[A-Za-z0-9\u4E00-\u9FFF·・\-\s]{1,40})\s*的\s*(.+)$"""
)

internal fun parseGuideSkillTypeMeta(raw: String): GuideSkillTypeMeta {
    val cleaned = sanitizeGuideSkillLabelForDisplay(raw).trim()
    if (cleaned.isBlank()) return GuideSkillTypeMeta(baseType = "")

    var variantIndex: Int? = null
    val stateTags = mutableListOf<String>()
    val stateCandidates = guideSkillTypeBracketPattern
        .findAll(cleaned)
        .map { it.groupValues.getOrElse(1) { "" }.trim() }
        .filter { it.isNotBlank() }
        .toList()

    stateCandidates.forEach { candidate ->
        val tokens = candidate
            .split(guideSkillTypeStateSplitPattern)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .ifEmpty { listOf(candidate) }
        tokens.forEach { token ->
            val tokenMeta = parseGuideSkillTypeToken(token)
            if (variantIndex == null && tokenMeta.variantIndex != null) {
                variantIndex = tokenMeta.variantIndex
            }
            val tag = normalizeGuideSkillStateTag(tokenMeta.base.ifBlank { token.trim() })
            if (tag.isNotBlank()) {
                stateTags += tag
            }
        }
    }

    val baseCandidate = guideSkillTypeBracketPattern
        .replace(cleaned, "")
        .replace(Regex("""\s{2,}"""), " ")
        .trim(' ', '-', '_', '/', '／', '|', '｜')
        .trim()
    val ownedTypeMeta = splitGuideOwnedSkillType(baseCandidate.ifBlank { cleaned })
    val baseToken = ownedTypeMeta?.skillType ?: baseCandidate.ifBlank { cleaned }
    val baseMeta = parseGuideSkillTypeToken(baseToken)
    if (variantIndex == null) {
        variantIndex = baseMeta.variantIndex
    }
    val ownerTag = ownedTypeMeta
        ?.ownerTag
        ?.let(::normalizeGuideSkillStateTag)
        .orEmpty()
    if (ownerTag.isNotBlank()) {
        stateTags.add(0, ownerTag)
    }

    return GuideSkillTypeMeta(
        baseType = baseMeta.base.ifBlank { cleaned },
        variantIndex = variantIndex,
        stateTags = stateTags
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
    )
}

internal fun splitGuideOwnedSkillType(raw: String): GuideSkillOwnedTypeMeta? {
    val normalized = raw.trim().replace(Regex("""\s+"""), " ")
    if (normalized.isBlank()) return null
    val match = guideSkillOwnedTypePattern.matchEntire(normalized) ?: return null
    val ownerTag = match.groupValues.getOrNull(1)?.trim().orEmpty()
    val skillType = match.groupValues.getOrNull(2)?.trim().orEmpty()
    if (ownerTag.isBlank() || skillType.isBlank()) return null
    if (!skillType.contains("技能")) return null
    return GuideSkillOwnedTypeMeta(
        ownerTag = ownerTag,
        skillType = skillType
    )
}

internal data class GuideSkillTypeTokenMeta(
    val base: String,
    val variantIndex: Int? = null
)

internal fun normalizeGuideSkillStateTag(raw: String): String {
    val cleaned = raw.trim()
    if (cleaned.isBlank()) return ""
    val compact = cleaned.replace(" ", "").replace("　", "")
    return if (
        compact.startsWith("对") &&
        compact.endsWith("使用") &&
        compact.length > 3
    ) {
        compact.removeSuffix("使用")
    } else {
        cleaned
    }
}

internal fun parseGuideSkillTypeToken(raw: String): GuideSkillTypeTokenMeta {
    val cleaned = raw.trim()
    if (cleaned.isBlank()) return GuideSkillTypeTokenMeta(base = "")

    val circledMatch = guideSkillTypeCircledSuffixPattern.matchEntire(cleaned)
    if (circledMatch != null) {
        val base = circledMatch.groupValues[1].trim()
        val circled = circledMatch.groupValues[2]
        val index = guideCircledNumbers.indexOf(circled).takeIf { it >= 0 }?.plus(1)
        return GuideSkillTypeTokenMeta(
            base = if (base.isBlank()) cleaned else base,
            variantIndex = index
        )
    }

    val numericMatch = guideSkillTypeNumericSuffixPattern.matchEntire(cleaned)
    if (numericMatch != null) {
        val base = numericMatch.groupValues[1].trim()
        val index = numericMatch.groupValues[2].toIntOrNull()?.takeIf { it > 0 }
        if (index != null) {
            return GuideSkillTypeTokenMeta(
                base = if (base.isBlank()) cleaned else base,
                variantIndex = index
            )
        }
    }

    return GuideSkillTypeTokenMeta(base = cleaned)
}

internal fun toGuideCircledNumber(index: Int): String {
    return guideCircledNumbers.getOrNull(index - 1) ?: index.toString()
}

@Composable
internal fun GuideSkillVariantBadge(
    label: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(Color(0x223B82F6))
            .border(width = 1.dp, color = Color(0x663B82F6), shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color(0xFF3B82F6),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

@Composable
internal fun GuideSkillStateTagButton(
    label: String,
    backdrop: Backdrop?,
    modifier: Modifier = Modifier
) {
    GlassTextButton(
        backdrop = backdrop,
        text = label,
        enabled = false,
        textColor = Color(0xFF3B82F6),
        variant = GlassVariant.Compact,
        minHeight = 26.dp,
        horizontalPadding = 8.dp,
        verticalPadding = 5.dp,
        modifier = modifier,
        onClick = {}
    )
}
