package com.example.keios.ui.page.main.student

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.keios.ui.page.main.widget.CopyModeSelectionContainer
import com.example.keios.ui.page.main.widget.GlassTextButton
import com.example.keios.ui.page.main.widget.GlassVariant
import com.example.keios.ui.page.main.widget.buildTextCopyPayload
import com.example.keios.ui.page.main.widget.copyModeAwareRow
import com.kyant.backdrop.backdrops.LayerBackdrop
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun GuideSimulateAbilityCard(
    data: GuideSimulateData,
    backdrop: LayerBackdrop
) {
    var selectedAbility by rememberSaveable(
        data.initialRows.size,
        data.maxRows.size
    ) { mutableStateOf("最大培养") }
    val selectedRows = if (selectedAbility == "最大培养") data.maxRows else data.initialRows
    val selectedHint = if (selectedAbility == "最大培养") data.maxHint else data.initialHint
    val initialValueByKey = remember(data.initialRows) {
        linkedMapOf<String, String>().apply {
            data.initialRows.forEach { row ->
                val key = normalizeProfileFieldKey(row.key)
                if (key.isBlank() || row.value.isBlank()) return@forEach
                putIfAbsent(key, row.value.trim())
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.defaultColors(
            color = Color(0x223B82F6),
            contentColor = MiuixTheme.colorScheme.onBackground
        ),
        onClick = {}
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "角色能力",
                    color = MiuixTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Medium
                )
                listOf("初始能力", "最大培养").forEach { option ->
                    val selected = selectedAbility == option
                    GlassTextButton(
                        backdrop = backdrop,
                        text = option,
                        textColor = if (selected) Color(0xFF2563EB) else MiuixTheme.colorScheme.onBackgroundVariant,
                        containerColor = if (selected) Color(0x443B82F6) else null,
                        variant = GlassVariant.Compact,
                        onClick = { selectedAbility = option }
                    )
                }
            }
            selectedHint.takeIf { it.isNotBlank() }?.let { hint ->
                Text(
                    text = hint.trim('*').trim(),
                    color = Color(0xFF60A5FA),
                    style = MiuixTheme.textStyles.body2,
                    modifier = Modifier.guideTabCopyable(
                        buildGuideTabCopyPayload("角色能力说明", hint)
                    )
                )
            }
            if (selectedRows.isNotEmpty()) {
                selectedRows.forEach { row ->
                    val deltaText = if (selectedAbility == "最大培养") {
                        buildSimulateMaxDeltaText(
                            maxValue = row.value,
                            initialValue = initialValueByKey[normalizeProfileFieldKey(row.key)]
                        )
                    } else {
                        ""
                    }
                    GuideSimulateRowItem(
                        row = row,
                        backdrop = backdrop,
                        valueDelta = deltaText
                    )
                }
            } else {
                Text(
                    text = "暂无能力数据。",
                    color = MiuixTheme.colorScheme.onBackgroundVariant
                )
            }
        }
    }
}

@Composable
internal fun GuideSimulateSectionCard(
    title: String,
    rows: List<BaGuideRow>,
    hint: String,
    backdrop: LayerBackdrop
) {
    val levelRowIndex = if (title == "能力解放") {
        rows.indexOfFirst { row ->
            Regex("""^\d+级$""").matches(normalizeProfileFieldKey(row.key))
        }
    } else {
        -1
    }
    val levelCapsule = when {
        levelRowIndex >= 0 -> rows[levelRowIndex].key.trim()
        else -> extractSimulateLevelCapsule(hint)
    }
    val displayRows = if (levelRowIndex >= 0) {
        rows.filterIndexed { index, _ -> index != levelRowIndex }
    } else {
        rows
    }
    val displayCapsule = if (title == "爱用品" && displayRows.isEmpty()) {
        ""
    } else {
        levelCapsule
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.defaultColors(
            color = Color(0x223B82F6),
            contentColor = MiuixTheme.colorScheme.onBackground
        ),
        onClick = {}
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GuideSimulateCardTitleRow(
                title = title,
                capsule = displayCapsule,
                backdrop = backdrop
            )
            if (displayRows.isNotEmpty()) {
                displayRows.forEach { row ->
                    GuideSimulateRowItem(
                        row = row,
                        backdrop = backdrop
                    )
                }
            } else {
                Text(
                    text = if (title == "爱用品") "此学生暂未佩戴爱用品。" else "暂无${title}数据。",
                    color = MiuixTheme.colorScheme.onBackgroundVariant
                )
            }
        }
    }
}

@Composable
internal fun GuideSimulateEquipmentCard(
    title: String,
    rows: List<BaGuideRow>,
    hint: String,
    backdrop: LayerBackdrop
) {
    val groups = buildSimulateEquipmentGroups(rows)
    val hintCapsule = extractSimulateLevelCapsule(hint)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.defaultColors(
            color = Color(0x223B82F6),
            contentColor = MiuixTheme.colorScheme.onBackground
        ),
        onClick = {}
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GuideSimulateCardTitleRow(
                title = title,
                capsule = hintCapsule,
                backdrop = backdrop
            )

            if (groups.isNotEmpty()) {
                groups.forEach { group ->
                    val groupCopyPayload = buildGuideTabCopyPayload(
                        group.slotLabel.ifBlank { "装备" },
                        listOf(group.itemName.trim(), group.tierText.trim())
                            .filter { it.isNotBlank() }
                            .joinToString(" · ")
                            .ifBlank { "-" }
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .guideTabCopyable(groupCopyPayload),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(0.52f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (group.iconUrl.isNotBlank()) {
                                GuideRemoteIcon(
                                    imageUrl = group.iconUrl,
                                    iconWidth = 40.dp,
                                    iconHeight = 40.dp
                                )
                            }
                            group.itemName.takeIf { it.isNotBlank() }?.let { name ->
                                Text(
                                    text = name,
                                    color = MiuixTheme.colorScheme.onBackgroundVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.weight(0.48f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GuideSimulateInlineCapsule(
                                text = group.slotLabel,
                                backdrop = backdrop
                            )
                        }
                    }

                    group.statRows.forEach { row ->
                        GuideSimulateRowItem(
                            row = row,
                            backdrop = backdrop
                        )
                    }
                }
            } else if (rows.isNotEmpty()) {
                rows.forEach { row ->
                    GuideSimulateRowItem(
                        row = row,
                        backdrop = backdrop
                    )
                }
            } else {
                Text(
                    text = "暂无装备数据。",
                    color = MiuixTheme.colorScheme.onBackgroundVariant
                )
            }
        }
    }
}

@Composable
internal fun GuideSimulateUnlockCard(
    title: String,
    rows: List<BaGuideRow>,
    hint: String,
    backdrop: LayerBackdrop
) {
    val viewData = buildSimulateUnlockViewData(rows, hint)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.defaultColors(
            color = Color(0x223B82F6),
            contentColor = MiuixTheme.colorScheme.onBackground
        ),
        onClick = {}
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GuideSimulateCardTitleRow(
                title = title,
                capsule = viewData.levelCapsule,
                backdrop = backdrop
            )

            if (viewData.rows.isNotEmpty()) {
                viewData.rows.forEach { row ->
                    val iconUrl = row.imageUrl.trim().ifBlank { row.imageUrls.firstOrNull().orEmpty() }
                    if (iconUrl.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GuideRemoteIcon(
                                imageUrl = iconUrl,
                                iconWidth = 78.dp,
                                iconHeight = 62.dp
                            )
                            GuideSimulateRowItem(
                                row = row.copy(imageUrl = "", imageUrls = emptyList()),
                                backdrop = backdrop
                            )
                        }
                    } else {
                        GuideSimulateRowItem(
                            row = row,
                            backdrop = backdrop
                        )
                    }
                }
            } else {
                Text(
                    text = "暂无能力解放数据。",
                    color = MiuixTheme.colorScheme.onBackgroundVariant
                )
            }
        }
    }
}

@Composable
internal fun GuideSimulateBondCard(
    title: String,
    rows: List<BaGuideRow>,
    hint: String,
    backdrop: LayerBackdrop
) {
    val groups = buildSimulateBondGroups(rows)
    val levelCapsule = extractSimulateLevelCapsule(hint)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.defaultColors(
            color = Color(0x223B82F6),
            contentColor = MiuixTheme.colorScheme.onBackground
        ),
        onClick = {}
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GuideSimulateCardTitleRow(
                title = title,
                capsule = levelCapsule,
                backdrop = backdrop
            )

            if (groups.isNotEmpty()) {
                groups.forEach { group ->
                    val groupCopyPayload = buildGuideTabCopyPayload(
                        "羁绊角色",
                        group.roleLabel.ifBlank { "羁绊角色" }
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .guideTabCopyable(groupCopyPayload),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        if (group.iconUrl.isNotBlank()) {
                            Box(modifier = Modifier.width(112.dp)) {
                                GuideRemoteImage(
                                    imageUrl = group.iconUrl,
                                    imageHeight = 86.dp
                                )
                            }
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = group.roleLabel,
                                color = MiuixTheme.colorScheme.onBackgroundVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            group.statRows.forEach { stat ->
                                GuideSimulateRowItem(
                                    row = stat,
                                    backdrop = backdrop
                                )
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "暂无羁绊等级奖励数据。",
                    color = MiuixTheme.colorScheme.onBackgroundVariant
                )
            }
        }
    }
}

@Composable
internal fun GuideSimulateWeaponCard(
    title: String,
    rows: List<BaGuideRow>,
    hint: String,
    backdrop: LayerBackdrop
) {
    val viewData = buildSimulateWeaponViewData(rows)
    val levelCapsule = extractSimulateLevelCapsule(hint)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.defaultColors(
            color = Color(0x223B82F6),
            contentColor = MiuixTheme.colorScheme.onBackground
        ),
        onClick = {}
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GuideSimulateCardTitleRow(
                title = title,
                capsule = levelCapsule,
                backdrop = backdrop
            )

            if (viewData.imageUrl.isNotBlank() || viewData.statRows.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    if (viewData.imageUrl.isNotBlank()) {
                        Box(modifier = Modifier.width(112.dp)) {
                            GuideRemoteImage(
                                imageUrl = viewData.imageUrl,
                                imageHeight = 72.dp
                            )
                        }
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        viewData.statRows.forEach { row ->
                            GuideSimulateRowItem(
                                row = row,
                                backdrop = backdrop
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "暂无专武数据。",
                    color = MiuixTheme.colorScheme.onBackgroundVariant
                )
            }
        }
    }
}

@Composable
internal fun GuideSimulateCardTitleRow(
    title: String,
    capsule: String,
    backdrop: LayerBackdrop
) {
    val copyPayload = remember(title, capsule) {
        buildGuideTabCopyPayload(title, capsule.ifBlank { "-" })
    }
    CopyModeSelectionContainer {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .guideTabCopyable(copyPayload),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = MiuixTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            capsule.takeIf { it.isNotBlank() }?.let { label ->
                GuideSimulateInlineCapsule(
                    text = label,
                    backdrop = backdrop
                )
            }
        }
    }
}

@Composable
internal fun GuideSimulateInlineCapsule(
    text: String,
    backdrop: LayerBackdrop
) {
    GlassTextButton(
        backdrop = backdrop,
        text = text,
        enabled = false,
        textColor = Color(0xFF60A5FA),
        variant = GlassVariant.Compact,
        onClick = {}
    )
}

@Composable
internal fun GuideSimulateRowItem(
    row: BaGuideRow,
    backdrop: LayerBackdrop,
    valueDelta: String = ""
) {
    val key = row.key.trim().ifBlank { "信息" }
    val value = row.value.trim()
    val rowCopyAction = rememberGuideTabCopyAction(buildGuideTabCopyPayload(key, value.ifBlank { "-" }))
    val iconUrl = row.imageUrl.trim().ifBlank { row.imageUrls.firstOrNull().orEmpty() }
    val statGlyph = simulateStatGlyphForKey(key)
    if (isSimulateSubHeader(key)) {
        CopyModeSelectionContainer {
            Row(
                modifier = Modifier.copyModeAwareRow(
                    copyPayload = buildGuideTabCopyPayload(key, value.ifBlank { "-" }),
                    onLongClick = rowCopyAction
                ),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (iconUrl.isNotBlank()) {
                    GuideRemoteIcon(
                        imageUrl = iconUrl,
                        iconWidth = 24.dp,
                        iconHeight = 24.dp
                    )
                }
                GlassTextButton(
                    backdrop = backdrop,
                    text = key,
                    enabled = false,
                    textColor = Color(0xFF3B82F6),
                    variant = GlassVariant.Compact,
                    onClick = {}
                )
            }
        }
        return
    }

    val valueColor = when {
        value.contains("%") -> Color(0xFF5FA8FF)
        value.matches(Regex("""(?i)^T\d+.*$""")) -> Color(0xFF5FA8FF)
        value.matches(Regex("""(?i)^Lv\d+.*$""")) -> Color(0xFF5FA8FF)
        key.contains("COST", ignoreCase = true) -> Color(0xFF5FA8FF)
        else -> MiuixTheme.colorScheme.onBackground
    }

    CopyModeSelectionContainer {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .copyModeAwareRow(
                    copyPayload = buildGuideTabCopyPayload(key, value.ifBlank { "-" }),
                    onLongClick = rowCopyAction
                ),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(0.45f),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (iconUrl.isNotBlank()) {
                    GuideRemoteIcon(
                        imageUrl = iconUrl,
                        iconWidth = 24.dp,
                        iconHeight = 24.dp
                    )
                } else if (statGlyph != null) {
                    Text(
                        text = statGlyph,
                        color = MiuixTheme.colorScheme.onBackgroundVariant,
                        modifier = Modifier.width(20.dp),
                        textAlign = TextAlign.Center
                    )
                }
                Text(
                    text = key,
                    color = MiuixTheme.colorScheme.onBackgroundVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(
                modifier = Modifier.weight(0.55f),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value.ifBlank { "-" },
                    color = valueColor,
                    textAlign = TextAlign.End,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                valueDelta.takeIf { it.isNotBlank() }?.let { delta ->
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = delta,
                        color = Color(0xFFE3B547),
                        maxLines = 1,
                        overflow = TextOverflow.Clip
                    )
                }
            }
        }
    }
}
