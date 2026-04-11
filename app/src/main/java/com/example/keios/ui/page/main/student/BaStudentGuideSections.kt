package com.example.keios.ui.page.main.student

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.keios.ba.helper.GameKeeFetchHelper
import com.example.keios.ui.page.main.widget.MiuixInfoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun GuideRemoteImage(
    imageUrl: String,
    modifier: Modifier = Modifier,
    imageHeight: androidx.compose.ui.unit.Dp = 220.dp
) {
    val target = remember(imageUrl) { normalizeGuideUrl(imageUrl) }
    if (target.isBlank()) return
    val bitmap by produceState<Bitmap?>(initialValue = null, target) {
        value = withContext(Dispatchers.IO) {
            runCatching { GameKeeFetchHelper.fetchImage(target) }.getOrNull()
        }
    }
    val rendered = bitmap ?: return
    Image(
        bitmap = rendered.asImageBitmap(),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .fillMaxWidth()
            .height(imageHeight)
            .clip(RoundedCornerShape(14.dp))
    )
}

@Composable
fun GuideRemoteIcon(
    imageUrl: String,
    modifier: Modifier = Modifier,
    iconWidth: androidx.compose.ui.unit.Dp = 20.dp,
    iconHeight: androidx.compose.ui.unit.Dp = iconWidth
) {
    val target = remember(imageUrl) { normalizeGuideUrl(imageUrl) }
    if (target.isBlank()) return
    val bitmap by produceState<Bitmap?>(initialValue = null, target) {
        value = withContext(Dispatchers.IO) {
            runCatching { GameKeeFetchHelper.fetchImage(target) }.getOrNull()
        }
    }
    val rendered = bitmap ?: return
    Image(
        bitmap = rendered.asImageBitmap(),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = modifier
            .width(iconWidth)
            .height(iconHeight)
    )
}

@Composable
fun GuideRowsSection(
    rows: List<BaGuideRow>,
    emptyText: String,
    imageHeight: androidx.compose.ui.unit.Dp = 96.dp
) {
    if (rows.isEmpty()) {
        Text(emptyText, color = MiuixTheme.colorScheme.onBackgroundVariant)
        return
    }
    val visibleRows = rows.take(120)
    visibleRows.forEachIndexed { index, row ->
        val key = row.key.ifBlank { "信息" }
        val hasImage = row.imageUrl.isNotBlank()
        val value = row.value
            .takeIf { it.isNotBlank() && it != "图片" }
            ?: if (hasImage) "见下图" else "-"
        MiuixInfoItem(key, value)
        if (hasImage) {
            Spacer(modifier = Modifier.height(6.dp))
            GuideRemoteImage(
                imageUrl = row.imageUrl,
                imageHeight = imageHeight
            )
        }
        if (index < visibleRows.lastIndex) {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun GuideGallerySection(
    items: List<BaGuideGalleryItem>,
    emptyText: String
) {
    if (items.isEmpty()) {
        Text(emptyText, color = MiuixTheme.colorScheme.onBackgroundVariant)
        return
    }
    val visibleItems = items.distinctBy { it.imageUrl }.take(24)
    visibleItems.forEachIndexed { index, item ->
        if (item.title.isNotBlank()) {
            Text(item.title, color = MiuixTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(6.dp))
        }
        GuideRemoteImage(
            imageUrl = item.imageUrl,
            imageHeight = 220.dp
        )
        if (index < visibleItems.lastIndex) {
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun GuideProfileMetaLine(item: BaGuideMetaItem) {
    val isPosition = item.title == "位置"
    val summary = if (isPosition) "" else item.value.ifBlank { "-" }
    val titleSlotWidth = 70.dp
    val iconSlotWidth = 34.dp
    val iconSlotHeight = 24.dp
    val iconWidth = if (isPosition) 30.dp else 20.dp
    val iconHeight = 20.dp

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = item.title,
            color = MiuixTheme.colorScheme.onBackgroundVariant,
            modifier = Modifier.width(titleSlotWidth),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Box(
            modifier = Modifier
                .width(iconSlotWidth)
                .height(iconSlotHeight),
            contentAlignment = Alignment.Center
        ) {
            if (item.imageUrl.isNotBlank()) {
                GuideRemoteIcon(
                    imageUrl = item.imageUrl,
                    iconWidth = iconWidth,
                    iconHeight = iconHeight
                )
            }
        }
        if (!isPosition) {
            Text(
                text = summary,
                color = MiuixTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        } else {
            Spacer(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun GuideCombatMetaTile(
    item: BaGuideMetaItem,
    modifier: Modifier = Modifier
) {
    val value = item.value.ifBlank { "-" }
    val adaptiveWide = item.title.contains("战术") || item.title == "武器类型"
    val titleWidth = 112.dp
    val iconWidth = if (adaptiveWide) 28.dp else 18.dp
    val iconHeight = if (adaptiveWide) 18.dp else 18.dp
    val extraIconWidth = 30.dp
    val extraIconHeight = 18.dp
    val iconSlotWidth = 30.dp
    val iconSlotHeight = 22.dp
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MiuixTheme.colorScheme.surfaceContainer.copy(alpha = 0.36f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = item.title,
            color = MiuixTheme.colorScheme.onBackgroundVariant,
            modifier = Modifier.width(titleWidth),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = value,
            color = MiuixTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Box(
            modifier = Modifier
                .width(iconSlotWidth)
                .height(iconSlotHeight),
            contentAlignment = Alignment.Center
        ) {
            if (item.imageUrl.isNotBlank()) {
                GuideRemoteIcon(
                    imageUrl = item.imageUrl,
                    iconWidth = iconWidth,
                    iconHeight = iconHeight
                )
            }
        }
        if (item.extraImageUrl.isNotBlank()) {
            Box(
                modifier = Modifier
                    .width(iconSlotWidth)
                    .height(iconSlotHeight),
                contentAlignment = Alignment.Center
            ) {
                GuideRemoteIcon(
                    imageUrl = item.extraImageUrl,
                    iconWidth = extraIconWidth,
                    iconHeight = extraIconHeight
                )
            }
        }
    }
}

fun showLoadingText(loading: Boolean, hasInfo: Boolean): Boolean {
    return loading && hasInfo
}

