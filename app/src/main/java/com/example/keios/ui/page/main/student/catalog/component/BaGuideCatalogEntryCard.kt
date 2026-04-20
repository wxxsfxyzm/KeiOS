package com.example.keios.ui.page.main.student.catalog.component

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.keios.R
import com.example.keios.ui.page.main.student.catalog.BaGuideCatalogEntry
import com.example.keios.ui.page.main.student.catalog.BaGuideCatalogIconCache
import com.example.keios.ui.page.main.widget.core.AppStatusPillSize
import com.example.keios.ui.page.main.widget.core.AppTypographyTokens
import com.example.keios.ui.page.main.widget.core.CardLayoutRhythm
import com.example.keios.ui.page.main.widget.glass.GlassIconButton
import com.example.keios.ui.page.main.widget.glass.GlassVariant
import com.example.keios.ui.page.main.widget.status.StatusPill
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.FavoritesFill
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun BaGuideCatalogEntryCard(
    entry: BaGuideCatalogEntry,
    isFavorite: Boolean,
    onOpenGuide: (String) -> Unit,
    onToggleFavorite: (Long) -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val copiedToast = stringResource(R.string.guide_toast_item_copied)
    val copyPayload = remember(entry) {
        buildString {
            append(entry.name.ifBlank { "未知角色" })
            append(" | ID ")
            append(entry.contentId)
            if (entry.aliasDisplay.isNotBlank()) {
                append('\n')
                append(entry.aliasDisplay)
            }
            if (entry.detailUrl.isNotBlank()) {
                append('\n')
                append(entry.detailUrl)
            }
        }
    }
    val copyAction = remember(context, clipboard, copiedToast, copyPayload) {
        {
            clipboard.setText(AnnotatedString(copyPayload))
            Toast.makeText(context, copiedToast, Toast.LENGTH_SHORT).show()
        }
    }
    val cardShape = RoundedCornerShape(16.dp)
    val favoriteActionColor = if (isFavorite) {
        Color(0xFFEC4899)
    } else {
        Color(0xFF3B82F6)
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isFavorite) {
                    Color(0x99EC4899)
                } else {
                    MiuixTheme.colorScheme.primary.copy(alpha = 0.24f)
                },
                shape = cardShape
            )
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onOpenGuide(entry.detailUrl) },
                onLongClick = copyAction
            ),
        cornerRadius = 16.dp,
        colors = CardDefaults.defaultColors(
            color = if (isFavorite) {
                Color(0x33EC4899)
            } else {
                MiuixTheme.colorScheme.primary.copy(alpha = 0.12f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = CardLayoutRhythm.cardHorizontalPadding,
                    vertical = CardLayoutRhythm.cardVerticalPadding
                ),
            horizontalArrangement = Arrangement.spacedBy(CardLayoutRhythm.controlRowGap),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(56.dp),
                contentAlignment = Alignment.Center
            ) {
                if (entry.iconUrl.isBlank()) {
                    CatalogAvatarFallback(iconRes = entry.tab.iconRes)
                } else {
                    CatalogAvatarImage(
                        imageUrl = entry.iconUrl,
                        fallbackRes = entry.tab.iconRes
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(CardLayoutRhythm.controlRowTextGap)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(CardLayoutRhythm.infoRowGap),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = entry.name,
                            modifier = Modifier.weight(1f),
                            color = MiuixTheme.colorScheme.onBackground,
                            fontSize = AppTypographyTokens.CompactTitle.fontSize,
                            lineHeight = AppTypographyTokens.CompactTitle.lineHeight,
                            fontWeight = AppTypographyTokens.CompactTitle.fontWeight,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        StatusPill(
                            label = "ID ${entry.contentId}",
                            color = MiuixTheme.colorScheme.primary,
                            size = AppStatusPillSize.Compact
                        )
                    }
                    if (entry.aliasDisplay.isNotBlank()) {
                        Text(
                            text = entry.aliasDisplay,
                            color = MiuixTheme.colorScheme.onBackgroundVariant,
                            fontSize = AppTypographyTokens.Supporting.fontSize,
                            lineHeight = AppTypographyTokens.Supporting.lineHeight,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            GlassIconButton(
                backdrop = null,
                icon = MiuixIcons.Regular.FavoritesFill,
                contentDescription = if (isFavorite) {
                    stringResource(R.string.ba_catalog_cd_unfavorite_student)
                } else {
                    stringResource(R.string.ba_catalog_cd_favorite_student)
                },
                onClick = { onToggleFavorite(entry.contentId) },
                width = 34.dp,
                height = 34.dp,
                variant = GlassVariant.Floating,
                iconTint = favoriteActionColor,
                containerColor = favoriteActionColor
            )
        }
    }
}

@Composable
private fun CatalogAvatarImage(
    imageUrl: String,
    fallbackRes: Int
) {
    val context = LocalContext.current
    val bitmap by produceState<Bitmap?>(initialValue = BaGuideCatalogIconCache.get(imageUrl), imageUrl) {
        value = withContext(Dispatchers.IO) { BaGuideCatalogIconCache.getOrLoad(context, imageUrl) }
    }
    val rendered = bitmap
    if (rendered == null) {
        CatalogAvatarFallback(iconRes = fallbackRes)
        return
    }
    Image(
        bitmap = rendered.asImageBitmap(),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(12.dp))
    )
}

@Composable
private fun CatalogAvatarFallback(iconRes: Int) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MiuixTheme.colorScheme.surfaceContainer.copy(alpha = 0.42f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = MiuixTheme.colorScheme.onBackgroundVariant,
            modifier = Modifier.size(28.dp)
        )
    }
}
