package com.example.keios.ui.page.main.student

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.keios.ui.page.main.widget.CopyModeSelectionContainer
import com.example.keios.ui.page.main.widget.FrostedBlock
import com.example.keios.ui.page.main.widget.GlassTextButton
import com.example.keios.ui.page.main.widget.GlassVariant
import com.kyant.backdrop.backdrops.LayerBackdrop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

internal fun LazyListScope.renderGuideProfileTabContent(
    tabLabel: String,
    info: BaStudentGuideInfo?,
    error: String?,
    backdrop: LayerBackdrop,
    accent: Color,
    context: Context,
    sourceUrl: String,
    galleryCacheRevision: Int,
    onOpenExternal: (String) -> Unit,
    onOpenGuide: (String) -> Unit,
    onSaveMedia: (url: String, title: String) -> Unit
) {
    val guide = info
    if (guide == null) {
        item {
            FrostedBlock(
                backdrop = backdrop,
                title = tabLabel,
                subtitle = info?.subtitle?.ifBlank { "GameKee" } ?: "GameKee",
                accent = accent,
                content = {
                    error?.takeIf { it.isNotBlank() }?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = it,
                            color = MiuixTheme.colorScheme.error,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            )
        }
        return
    }

    val profileRowsBase = guide.profileRowsForDisplay()
        .filterNot(::shouldHideMovedHeaderRow)
        .filterNot(::isGrowthTitleVoiceRow)
        .filterNot(::isVoicePlaceholderRow)
        .filterNot(::isProfileSectionHeaderRow)
        .filterNot(::isGalleryRelatedProfileLinkRow)
    val sameNameRoleRows = profileRowsBase.filter(::isSameNameRoleRow)
    val sameNameRoleItems = buildSameNameRoleItems(sameNameRoleRows)
    val sameNameRoleHint = sameNameRoleRows.firstNotNullOfOrNull { row ->
        extractSameNameRoleHint(row)
    }.orEmpty()
    val hasTopDataHeader = profileRowsBase.any { row ->
        normalizeProfileFieldKey(row.key) == normalizeProfileFieldKey("顶级数据")
    }
    val hasInitialDataHeader = profileRowsBase.any { row ->
        normalizeProfileFieldKey(row.key) == normalizeProfileFieldKey("初始数据")
    }
    val allProfileRows = profileRowsBase.filterNot { row ->
        isSkillMigratedProfileRow(
            row = row,
            hasTopDataHeader = hasTopDataHeader,
            hasInitialDataHeader = hasInitialDataHeader
        ) || isSameNameRoleRow(row)
    }
    val nicknameRows = buildProfileCardRows(
        rows = allProfileRows,
        specs = profileNicknameFieldSpecs
    )
    val studentInfoRows = buildProfileCardRows(
        rows = allProfileRows,
        specs = profileStudentInfoFieldSpecs
    )
    val hobbyRows = buildProfileCardRows(
        rows = allProfileRows,
        specs = profileHobbyFieldSpecs
    )
    val giftPreferenceRows = allProfileRows
        .filter(::isGiftPreferenceProfileRow)
        .let(::sortProfileRowsByKeyNumbers)
    val giftPreferenceItems = buildGiftPreferenceItems(giftPreferenceRows)
    val chocolateInfoRows = allProfileRows.filter { row ->
        val key = row.key.trim()
        key.contains("巧克力", ignoreCase = true)
    }.let(::sortProfileRowsByKeyNumbers)
    val furnitureInfoRows = allProfileRows.filter { row ->
        val key = row.key.trim()
        key.contains("互动家具", ignoreCase = true)
    }.let(::sortProfileRowsByKeyNumbers)
    val normalProfileRows = allProfileRows.filterNot { row ->
        val key = row.key.trim()
        key.contains("巧克力", ignoreCase = true) ||
            key.contains("互动家具", ignoreCase = true) ||
            isGiftPreferenceProfileRow(row) ||
            isStructuredProfileCardRow(row)
    }
    val chocolateGalleryItems = guide.galleryItems
        .filter(::isChocolateGalleryItem)
        .filter(::hasRenderableGalleryMedia)
        .distinctBy {
            val media = it.mediaUrl.ifBlank { it.imageUrl }
            "${it.mediaType}|$media"
        }
        .let(::sortGalleryItemsByTitleNumbers)
    val furnitureGalleryItems = guide.galleryItems
        .filter(::isInteractiveFurnitureGalleryItem)
        .filter(::hasRenderableGalleryMedia)
        .distinctBy {
            val media = it.mediaUrl.ifBlank { it.imageUrl }
            "${it.mediaType}|$media"
        }
        .let(::sortGalleryItemsByTitleNumbers)

    if (!error.isNullOrBlank()) {
        item {
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
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = error.orEmpty(),
                        color = MiuixTheme.colorScheme.error,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        item { Spacer(modifier = Modifier.height(10.dp)) }
    }

    if (nicknameRows.isNotEmpty()) {
        item {
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
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GuideProfileSectionHeader(
                        title = "学生昵称"
                    )
                    GuideProfileInfoRows(rows = nicknameRows) { row ->
                        GuideProfileInfoItem(
                            key = row.key.ifBlank { "信息" },
                            value = row.value.ifBlank { "-" }
                        )
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(10.dp)) }
    }

    if (studentInfoRows.isNotEmpty()) {
        item {
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
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GuideProfileSectionHeader(
                        title = "学生信息"
                    )
                    GuideProfileInfoRows(rows = studentInfoRows) { row ->
                        val normalizedKey = normalizeProfileFieldKey(row.key)
                        if (normalizedKey == profileRoleReferenceFieldKey) {
                            val externalLink = remember(row.value) {
                                extractProfileExternalLink(row.value)
                            }
                            val resolvedTitle by produceState(
                                initialValue = if (externalLink.isNotBlank()) {
                                    profileLinkTitleCache[externalLink].orEmpty()
                                } else {
                                    ""
                                },
                                key1 = externalLink
                            ) {
                                value = if (externalLink.isBlank()) {
                                    ""
                                } else {
                                    withContext(Dispatchers.IO) {
                                        resolveProfileLinkTitle(externalLink)
                                    }
                                }
                            }
                            val displayValue = when {
                                externalLink.isBlank() -> row.value.ifBlank { "-" }
                                resolvedTitle.isNotBlank() -> resolvedTitle
                                else -> fallbackProfileLinkTitle(externalLink)
                            }
                            GuideProfileInfoItem(
                                key = row.key.ifBlank { "信息" },
                                value = displayValue,
                                onClick = externalLink.takeIf { it.isNotBlank() }?.let { link ->
                                    { onOpenExternal(link) }
                                },
                                valueColor = if (externalLink.isNotBlank()) {
                                    Color(0xFF5FA8FF)
                                } else {
                                    null
                                },
                                preferCapsule = false
                            )
                        } else {
                            GuideProfileInfoItem(
                                key = row.key.ifBlank { "信息" },
                                value = row.value.ifBlank { "-" }
                            )
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(10.dp)) }
    }

    if (hobbyRows.isNotEmpty()) {
        item {
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
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GuideProfileSectionHeader(
                        title = "学生爱好"
                    )
                    GuideProfileInfoRows(rows = hobbyRows) { row ->
                        GuideProfileInfoItem(
                            key = row.key.ifBlank { "信息" },
                            value = row.value.ifBlank { "-" },
                            preferCapsule = false
                        )
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(10.dp)) }
    }

    if (giftPreferenceItems.isNotEmpty()) {
        item {
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
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GuideProfileSectionHeader(
                        title = "礼物偏好"
                    )
                    GuideGiftPreferenceGrid(
                        items = giftPreferenceItems
                    )
                }
            }
        }
        item { Spacer(modifier = Modifier.height(10.dp)) }
    }

    item {
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
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                GuideProfileSectionHeader(
                    title = "相关同名角色"
                )
                sameNameRoleHint.takeIf { it.isNotBlank() }?.let { hint ->
                    CopyModeSelectionContainer {
                        Text(
                            text = hint,
                            color = MiuixTheme.colorScheme.onBackgroundVariant,
                            modifier = Modifier.guideTabCopyable(
                                buildGuideTabCopyPayload("相关同名角色", hint)
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (sameNameRoleItems.isEmpty()) {
                    Text(
                        text = "暂无同名角色条目。",
                        color = MiuixTheme.colorScheme.onBackgroundVariant
                    )
                } else {
                    sameNameRoleItems.forEachIndexed { index, role ->
                        if (index > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        val roleCopyPayload = buildString {
                            append(role.name.ifBlank { "同名角色" })
                            role.linkUrl.trim().takeIf { it.isNotBlank() }?.let { link ->
                                append('\n')
                                append(link)
                            }
                        }
                        CopyModeSelectionContainer {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .guideTabCopyable(
                                        buildGuideTabCopyPayload(
                                            "同名角色",
                                            roleCopyPayload
                                        )
                                    ),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val previewImage = role.imageUrl.trim()
                                if (previewImage.isNotBlank()) {
                                    GuideRemoteIcon(
                                        imageUrl = previewImage,
                                        iconWidth = 68.dp,
                                        iconHeight = 68.dp
                                    )
                                }
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val link = role.linkUrl.trim()
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = role.name.ifBlank { "同名角色" },
                                            color = MiuixTheme.colorScheme.onBackground,
                                            modifier = Modifier.weight(1f),
                                            maxLines = Int.MAX_VALUE,
                                            overflow = TextOverflow.Clip
                                        )
                                        if (link.isNotBlank()) {
                                            GlassTextButton(
                                                backdrop = backdrop,
                                                text = "图鉴",
                                                textColor = Color(0xFF3B82F6),
                                                variant = GlassVariant.Compact,
                                                onClick = { onOpenGuide(link) }
                                            )
                                        }
                                    }
                                    if (link.isBlank()) {
                                        Text(
                                            text = "暂无可跳转链接",
                                            color = MiuixTheme.colorScheme.onBackgroundVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    item { Spacer(modifier = Modifier.height(10.dp)) }

    if (normalProfileRows.isNotEmpty()) {
        item {
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
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GuideProfileRowsSection(
                        rows = normalProfileRows,
                        emptyText = "暂未解析到学生档案数据。"
                    )
                }
            }
        }
    } else if (
        nicknameRows.isEmpty() &&
        studentInfoRows.isEmpty() &&
        hobbyRows.isEmpty() &&
        giftPreferenceItems.isEmpty()
    ) {
        item {
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
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "暂未解析到学生档案数据。",
                        color = MiuixTheme.colorScheme.onBackgroundVariant
                    )
                }
            }
        }
    }

    if (chocolateInfoRows.isNotEmpty() || chocolateGalleryItems.isNotEmpty()) {
        item { Spacer(modifier = Modifier.height(10.dp)) }
        item {
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
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GuideProfileSectionHeader(
                        title = "巧克力"
                    )
                    GuideProfileInfoRows(rows = chocolateInfoRows) { row ->
                        val value = row.value.ifBlank { "-" }
                        GuideProfileInfoItem(
                            key = row.key.ifBlank { "信息" },
                            value = value
                        )
                    }
                    chocolateGalleryItems.forEachIndexed { index, chocolateItem ->
                        if (chocolateInfoRows.isNotEmpty() || index > 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                        GuideGalleryCardItem(
                            item = chocolateItem,
                            backdrop = backdrop,
                            onOpenMedia = onOpenExternal,
                            onSaveMedia = onSaveMedia,
                            audioLoopScopeKey = sourceUrl,
                            mediaUrlResolver = { raw ->
                                galleryCacheRevision.let {
                                    BaGuideTempMediaCache.resolveCachedUrl(
                                        context = context,
                                        sourceUrl = sourceUrl,
                                        rawUrl = raw
                                    )
                                }
                            },
                            embedded = true,
                            showMediaTypeLabel = false
                        )
                    }
                }
            }
        }
    }

    if (furnitureInfoRows.isNotEmpty() || furnitureGalleryItems.isNotEmpty()) {
        item { Spacer(modifier = Modifier.height(10.dp)) }
        item {
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
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GuideProfileSectionHeader(
                        title = "互动家具"
                    )
                    GuideProfileInfoRows(rows = furnitureInfoRows) { row ->
                        val value = row.value.ifBlank { "-" }
                        GuideProfileInfoItem(
                            key = row.key.ifBlank { "信息" },
                            value = value,
                            preferCapsule = false
                        )
                    }
                    furnitureGalleryItems.forEachIndexed { index, furnitureItem ->
                        if (furnitureInfoRows.isNotEmpty() || index > 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                        GuideGalleryCardItem(
                            item = furnitureItem,
                            backdrop = backdrop,
                            onOpenMedia = onOpenExternal,
                            onSaveMedia = onSaveMedia,
                            audioLoopScopeKey = sourceUrl,
                            mediaUrlResolver = { raw ->
                                galleryCacheRevision.let {
                                    BaGuideTempMediaCache.resolveCachedUrl(
                                        context = context,
                                        sourceUrl = sourceUrl,
                                        rawUrl = raw
                                    )
                                }
                            },
                            embedded = true,
                            showMediaTypeLabel = false
                        )
                    }
                }
            }
        }
    }
}
