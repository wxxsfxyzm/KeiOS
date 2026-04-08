package com.example.keios.ui.page.main.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.Shadow
import com.kyant.capsule.ContinuousCapsule
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun GlassIconButton(
    backdrop: Backdrop?,
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconTint = MiuixTheme.colorScheme.primary
    val fallbackSurface = MiuixTheme.colorScheme.surfaceContainer
    Box(
        modifier = modifier
            .width(40.dp)
            .height(40.dp)
            .clip(ContinuousCapsule)
            .clickable(onClick = onClick)
            .then(
                if (backdrop != null) {
                    Modifier.drawBackdrop(
                        backdrop = backdrop,
                        shape = { ContinuousCapsule },
                        effects = {
                            vibrancy()
                            blur(7.dp.toPx())
                            lens(18.dp.toPx(), 18.dp.toPx())
                        },
                        highlight = { Highlight.Default.copy(alpha = 0.9f) },
                        shadow = { Shadow.Default.copy(color = Color.Black.copy(alpha = 0.12f)) },
                        onDrawSurface = { drawRect(Color.White.copy(alpha = 0.20f)) }
                    )
                } else {
                    Modifier.background(fallbackSurface.copy(alpha = 0.9f))
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint
        )
    }
}

@Composable
fun GlassTextButton(
    backdrop: Backdrop?,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = MiuixTheme.colorScheme.primary
    val fallbackSurface = MiuixTheme.colorScheme.surfaceContainer
    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 40.dp)
            .clip(ContinuousCapsule)
            .clickable(onClick = onClick)
            .then(
                if (backdrop != null) {
                    Modifier.drawBackdrop(
                        backdrop = backdrop,
                        shape = { ContinuousCapsule },
                        effects = {
                            vibrancy()
                            blur(7.dp.toPx())
                            lens(18.dp.toPx(), 18.dp.toPx())
                        },
                        highlight = { Highlight.Default.copy(alpha = 0.9f) },
                        shadow = { Shadow.Default.copy(color = Color.Black.copy(alpha = 0.12f)) },
                        onDrawSurface = { drawRect(Color.White.copy(alpha = 0.20f)) }
                    )
                } else {
                    Modifier.background(fallbackSurface.copy(alpha = 0.9f))
                }
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = textColor)
    }
}

