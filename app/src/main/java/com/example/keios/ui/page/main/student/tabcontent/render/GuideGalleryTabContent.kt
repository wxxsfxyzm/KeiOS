package com.example.keios.ui.page.main.student.tabcontent.render

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.keios.ui.page.main.student.BaGuideGalleryItem
import com.example.keios.ui.page.main.student.BaGuideRow
import com.example.keios.ui.page.main.student.BaGuideTempMediaCache
import com.example.keios.ui.page.main.student.BaStudentGuideInfo
import com.example.keios.ui.page.main.student.expressionGalleryOrder
import com.example.keios.ui.page.main.student.hasRenderableGalleryMedia
import com.example.keios.ui.page.main.student.isChocolateGalleryItem
import com.example.keios.ui.page.main.student.isExpressionGalleryItem
import com.example.keios.ui.page.main.student.isInteractiveFurnitureGalleryItem
import com.example.keios.ui.page.main.student.isMemoryHallFileGalleryItem
import com.example.keios.ui.page.main.student.isMemoryHallGalleryItem
import com.example.keios.ui.page.main.student.isOfficialIntroGalleryItem
import com.example.keios.ui.page.main.student.isPreviewVideoCategoryGalleryItem
import com.example.keios.ui.page.main.student.isPreviewVideoGalleryItem
import com.example.keios.ui.page.main.student.isRenderableGalleryAudioUrl
import com.example.keios.ui.page.main.student.isRenderableGalleryImageUrl
import com.example.keios.ui.page.main.student.normalizeGalleryTitle
import com.example.keios.ui.page.main.student.profileRowsForDisplay
import com.example.keios.ui.page.main.student.section.GuideGalleryCardItem
import com.example.keios.ui.page.main.student.section.gallery.GuideGalleryExpressionCardItem
import com.example.keios.ui.page.main.student.section.gallery.GuideGalleryUnlockLevelCardItem
import com.example.keios.ui.page.main.student.section.gallery.GuideGalleryVideoGroupCardItem
import com.example.keios.ui.page.main.student.tabcontent.profile.GuideGalleryRelatedLinkRows
import com.example.keios.ui.page.main.student.tabcontent.profile.GuideProfileSectionHeader
import com.example.keios.ui.page.main.student.tabcontent.profile.isGalleryRelatedProfileLinkRow
import com.example.keios.ui.page.main.student.tabcontent.profile.normalizeProfileFieldKey
import com.example.keios.ui.page.main.widget.glass.FrostedBlock
import com.kyant.backdrop.backdrops.LayerBackdrop
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

internal fun LazyListScope.renderGuideGalleryTabContent(
    tabLabel: String,
    info: BaStudentGuideInfo?,
    error: String?,
    backdrop: LayerBackdrop,
    accent: Color,
    context: Context,
    sourceUrl: String,
    galleryCacheRevision: Int,
    onOpenExternal: (String) -> Unit,
    onSaveMedia: (url: String, title: String) -> Unit
) {
    val guide = info
    if (guide == null) {
        item {
            FrostedBlock(
                backdrop = backdrop,
                title = tabLabel,
                subtitle = info?.subtitle?.ifBlank { "GameKee" } ?: "GameKee",
                accent = accent
            )
        }
        return
    }

    val galleryItems = if (guide.galleryItems.isNotEmpty()) {
        guide.galleryItems
            .filter(::hasRenderableGalleryMedia)
            .distinctBy {
                val media = it.mediaUrl.ifBlank { it.imageUrl }
                "${it.mediaType}|$media"
            }
    } else {
        listOfNotNull(
            guide.imageUrl.takeIf { it.isNotBlank() }?.let {
                BaGuideGalleryItem(
                    title = "立绘",
                    imageUrl = it,
                    mediaType = "image",
                    mediaUrl = it
                ).takeIf(::hasRenderableGalleryMedia)
            }
        )
    }
    val cleanedGalleryItems = galleryItems.filterNot(::isMemoryHallFileGalleryItem)
    val galleryRelatedLinkRows = guide.profileRowsForDisplay()
        .filter(::isGalleryRelatedProfileLinkRow)
        .distinctBy { row ->
            "${normalizeProfileFieldKey(row.key)}|${row.value.trim()}"
        }
        .take(10)
    val memoryHallPreview = cleanedGalleryItems
        .firstOrNull {
            isMemoryHallGalleryItem(it) && isRenderableGalleryImageUrl(it.imageUrl)
        }
        ?.imageUrl
        .orEmpty()
    val previewVideoGroups = run {
        val orderedCategories = cleanedGalleryItems
            .asSequence()
            .mapNotNull { item ->
                if (!isPreviewVideoGalleryItem(item)) return@mapNotNull null
                val normalized = normalizeGalleryTitle(item.title)
                when {
                    normalized.startsWith("回忆大厅视频") -> "回忆大厅视频"
                    normalized.startsWith("PV") -> "PV"
                    normalized.startsWith("角色演示") -> "角色演示"
                    else -> null
                }
            }
            .distinct()
            .toList()

        orderedCategories.mapNotNull { category ->
            val categoryFallbackPreview =
                if (category == "回忆大厅视频" && isRenderableGalleryImageUrl(memoryHallPreview)) {
                    memoryHallPreview
                } else {
                    ""
                }
            val categoryItems = cleanedGalleryItems
                .asSequence()
                .filter(::isPreviewVideoGalleryItem)
                .filter { item ->
                    val normalized = normalizeGalleryTitle(item.title)
                    when (category) {
                        "回忆大厅视频" -> normalized.startsWith("回忆大厅视频")
                        "PV" -> normalized.startsWith("PV")
                        "角色演示" -> normalized.startsWith("角色演示")
                        else -> false
                    }
                }
                .mapNotNull { item ->
                    val currentPreview = item.imageUrl
                    val preview = when {
                        isRenderableGalleryImageUrl(currentPreview) -> currentPreview
                        categoryFallbackPreview.isNotBlank() -> categoryFallbackPreview
                        else -> ""
                    }
                    if (preview.isBlank()) {
                        null
                    } else if (preview == currentPreview) {
                        item
                    } else {
                        item.copy(imageUrl = preview)
                    }
                }
                .toList()
            categoryItems.takeIf { it.isNotEmpty() }?.let { category to it }
        }
    }
    val memoryHallVideoGroup = previewVideoGroups.firstOrNull { it.first == "回忆大厅视频" }
    val trailingVideoGroups = previewVideoGroups.filterNot { it.first == "回忆大厅视频" }
    val pvAndRoleVideoGroups = trailingVideoGroups.filter { (title, _) ->
        title == "PV" || title == "角色演示"
    }
    val otherTrailingVideoGroups = trailingVideoGroups.filterNot { (title, _) ->
        title == "PV" || title == "角色演示"
    }
    val displayGalleryItems = cleanedGalleryItems
        .filterNot(::isPreviewVideoCategoryGalleryItem)
        .filterNot(::isChocolateGalleryItem)
        .filterNot(::isInteractiveFurnitureGalleryItem)
        .filter { item ->
            when (item.mediaType.lowercase()) {
                "audio" -> isRenderableGalleryAudioUrl(item.mediaUrl)
                else -> isRenderableGalleryImageUrl(item.imageUrl)
            }
        }
    val memoryUnlockLevel = cleanedGalleryItems
        .asSequence()
        .map { it.memoryUnlockLevel }
        .firstOrNull { it.isNotBlank() }
        .orEmpty()
        .ifBlank {
            val fallback = guide.profileRows
                .firstOrNull { it.key.trim() == "回忆大厅解锁等级" }
                ?.value
                .orEmpty()
            Regex("""\d+""").find(fallback)?.value.orEmpty().ifBlank { fallback }
        }
    val expressionItems = displayGalleryItems
        .withIndex()
        .filter { isExpressionGalleryItem(it.value) }
        .sortedBy { indexed ->
            expressionGalleryOrder(indexed.value.title, indexed.index + 1)
        }
        .map { it.value }
    val firstExpressionIndex = displayGalleryItems.indexOfFirst(::isExpressionGalleryItem)
    val firstMemoryHallIndex = displayGalleryItems.indexOfFirst(::isMemoryHallGalleryItem)
    val lastOfficialIntroIndex = displayGalleryItems.indexOfLast(::isOfficialIntroGalleryItem)

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
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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

    if (displayGalleryItems.isNotEmpty() || previewVideoGroups.isNotEmpty()) {
        var renderedCount = 0
        var insertedUnlockLevel = false
        var insertedMemoryHallVideoNearGallery = false
        var insertedPvRoleAfterOfficial = false
        var insertedGalleryRelatedLinks = false
        displayGalleryItems.forEachIndexed { index, item ->
            val isExpression = isExpressionGalleryItem(item)
            if (isExpression && index != firstExpressionIndex) {
                return@forEachIndexed
            }
            if (renderedCount > 0) {
                item { Spacer(modifier = Modifier.height(10.dp)) }
            }

            if (!insertedUnlockLevel &&
                memoryUnlockLevel.isNotBlank() &&
                index == firstMemoryHallIndex
            ) {
                item {
                    GuideGalleryUnlockLevelCardItem(
                        level = memoryUnlockLevel,
                        backdrop = backdrop
                    )
                }
                item { Spacer(modifier = Modifier.height(10.dp)) }
                insertedUnlockLevel = true
            }

            item {
                if (isExpression && expressionItems.isNotEmpty()) {
                    GuideGalleryExpressionCardItem(
                        title = "角色表情",
                        items = expressionItems,
                        backdrop = backdrop,
                        onOpenMedia = onOpenExternal,
                        onSaveMedia = onSaveMedia,
                        mediaUrlResolver = { raw ->
                            galleryCacheRevision.let {
                                BaGuideTempMediaCache.resolveCachedUrl(
                                    context = context,
                                    sourceUrl = sourceUrl,
                                    rawUrl = raw
                                )
                            }
                        }
                    )
                } else {
                    GuideGalleryCardItem(
                        item = item,
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
                        }
                    )
                }
            }
            renderedCount += 1

            if (!insertedPvRoleAfterOfficial &&
                pvAndRoleVideoGroups.isNotEmpty() &&
                index == lastOfficialIntroIndex
            ) {
                pvAndRoleVideoGroups.forEach { (title, items) ->
                    if (renderedCount > 0) {
                        item { Spacer(modifier = Modifier.height(10.dp)) }
                    }
                    item {
                        GuideGalleryVideoGroupCardItem(
                            title = title,
                            items = items,
                            previewFallbackUrl = "",
                            backdrop = backdrop,
                            onOpenMedia = onOpenExternal,
                            onSaveMedia = onSaveMedia,
                            mediaUrlResolver = { raw ->
                                galleryCacheRevision.let {
                                    BaGuideTempMediaCache.resolveCachedUrl(
                                        context = context,
                                        sourceUrl = sourceUrl,
                                        rawUrl = raw
                                    )
                                }
                            }
                        )
                    }
                    renderedCount += 1
                }
                insertedPvRoleAfterOfficial = true

                if (!insertedGalleryRelatedLinks && galleryRelatedLinkRows.isNotEmpty()) {
                    if (renderedCount > 0) {
                        item { Spacer(modifier = Modifier.height(10.dp)) }
                    }
                    item {
                        GuideGalleryRelatedLinksCard(
                            rows = galleryRelatedLinkRows,
                            onOpenExternal = onOpenExternal
                        )
                    }
                    renderedCount += 1
                    insertedGalleryRelatedLinks = true
                }
            }

            if (!insertedMemoryHallVideoNearGallery &&
                memoryHallVideoGroup != null &&
                index == firstMemoryHallIndex
            ) {
                item { Spacer(modifier = Modifier.height(10.dp)) }
                item {
                    GuideGalleryVideoGroupCardItem(
                        title = memoryHallVideoGroup.first,
                        items = memoryHallVideoGroup.second,
                        previewFallbackUrl = memoryHallPreview,
                        backdrop = backdrop,
                        onOpenMedia = onOpenExternal,
                        onSaveMedia = onSaveMedia,
                        mediaUrlResolver = { raw ->
                            galleryCacheRevision.let {
                                BaGuideTempMediaCache.resolveCachedUrl(
                                    context = context,
                                    sourceUrl = sourceUrl,
                                    rawUrl = raw
                                )
                            }
                        }
                    )
                }
                renderedCount += 1
                insertedMemoryHallVideoNearGallery = true
            }
        }

        if (!insertedUnlockLevel &&
            memoryUnlockLevel.isNotBlank() &&
            memoryHallVideoGroup != null
        ) {
            if (renderedCount > 0) {
                item { Spacer(modifier = Modifier.height(10.dp)) }
            }
            item {
                GuideGalleryUnlockLevelCardItem(
                    level = memoryUnlockLevel,
                    backdrop = backdrop
                )
            }
            insertedUnlockLevel = true
            renderedCount += 1
        }

        if (!insertedMemoryHallVideoNearGallery && memoryHallVideoGroup != null) {
            if (renderedCount > 0) {
                item { Spacer(modifier = Modifier.height(10.dp)) }
            }
            item {
                GuideGalleryVideoGroupCardItem(
                    title = memoryHallVideoGroup.first,
                    items = memoryHallVideoGroup.second,
                    previewFallbackUrl = memoryHallPreview,
                    backdrop = backdrop,
                    onOpenMedia = onOpenExternal,
                    onSaveMedia = onSaveMedia,
                    mediaUrlResolver = { raw ->
                        galleryCacheRevision.let {
                            BaGuideTempMediaCache.resolveCachedUrl(
                                context = context,
                                sourceUrl = sourceUrl,
                                rawUrl = raw
                            )
                        }
                    }
                )
            }
            renderedCount += 1
        }

        if (!insertedPvRoleAfterOfficial) {
            pvAndRoleVideoGroups.forEach { (title, items) ->
                if (renderedCount > 0) {
                    item { Spacer(modifier = Modifier.height(10.dp)) }
                }
                item {
                    GuideGalleryVideoGroupCardItem(
                        title = title,
                        items = items,
                        previewFallbackUrl = "",
                        backdrop = backdrop,
                        onOpenMedia = onOpenExternal,
                        onSaveMedia = onSaveMedia,
                        mediaUrlResolver = { raw ->
                            galleryCacheRevision.let {
                                BaGuideTempMediaCache.resolveCachedUrl(
                                    context = context,
                                    sourceUrl = sourceUrl,
                                    rawUrl = raw
                                )
                            }
                        }
                    )
                }
                renderedCount += 1
            }
            insertedPvRoleAfterOfficial = pvAndRoleVideoGroups.isNotEmpty()
            if (!insertedGalleryRelatedLinks && galleryRelatedLinkRows.isNotEmpty()) {
                if (renderedCount > 0) {
                    item { Spacer(modifier = Modifier.height(10.dp)) }
                }
                item {
                    GuideGalleryRelatedLinksCard(
                        rows = galleryRelatedLinkRows,
                        onOpenExternal = onOpenExternal
                    )
                }
                renderedCount += 1
                insertedGalleryRelatedLinks = true
            }
        }

        otherTrailingVideoGroups.forEach { (title, items) ->
            if (renderedCount > 0) {
                item { Spacer(modifier = Modifier.height(10.dp)) }
            }
            item {
                GuideGalleryVideoGroupCardItem(
                    title = title,
                    items = items,
                    previewFallbackUrl = "",
                    backdrop = backdrop,
                    onOpenMedia = onOpenExternal,
                    onSaveMedia = onSaveMedia,
                    mediaUrlResolver = { raw ->
                        galleryCacheRevision.let {
                            BaGuideTempMediaCache.resolveCachedUrl(
                                context = context,
                                sourceUrl = sourceUrl,
                                rawUrl = raw
                            )
                        }
                    }
                )
            }
            renderedCount += 1
        }

        if (!insertedGalleryRelatedLinks && galleryRelatedLinkRows.isNotEmpty()) {
            if (renderedCount > 0) {
                item { Spacer(modifier = Modifier.height(10.dp)) }
            }
            item {
                GuideGalleryRelatedLinksCard(
                    rows = galleryRelatedLinkRows,
                    onOpenExternal = onOpenExternal
                )
            }
        }
    } else {
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
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "暂未解析到影画鉴赏内容，点击右上角刷新后重试。",
                        color = MiuixTheme.colorScheme.onBackgroundVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun GuideGalleryRelatedLinksCard(
    rows: List<BaGuideRow>,
    onOpenExternal: (String) -> Unit
) {
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
                title = "影画相关链接"
            )
            GuideGalleryRelatedLinkRows(
                rows = rows,
                onOpenExternal = onOpenExternal
            )
        }
    }
}
