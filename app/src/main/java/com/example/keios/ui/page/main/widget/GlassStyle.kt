package com.example.keios.ui.page.main.widget

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class GlassVariant {
    Content,
    Compact,
    Sheet,
    Bar
}

internal data class GlassStyle(
    val blur: Dp,
    val baseColor: Color,
    val overlayColor: Color,
    val borderColor: Color,
    val highlightAlpha: Float,
    val shadowAlpha: Float,
    val fallbackAlpha: Float,
    val lensStart: Dp,
    val lensEnd: Dp,
    val showBorder: Boolean
)

internal fun glassStyle(
    isDark: Boolean,
    variant: GlassVariant,
    blurRadius: Dp?
): GlassStyle {
    return when (variant) {
        GlassVariant.Bar -> GlassStyle(
            blur = 8.dp,
            baseColor = Color.Transparent,
            overlayColor = Color.Transparent,
            borderColor = if (isDark) Color.White.copy(alpha = 0.10f) else Color.White.copy(alpha = 0.82f),
            highlightAlpha = 1f,
            shadowAlpha = if (isDark) 0.20f else 0.10f,
            fallbackAlpha = if (isDark) 0.40f else 0.56f,
            lensStart = 24.dp,
            lensEnd = 24.dp,
            showBorder = true
        )
        GlassVariant.Sheet -> {
            val blur = blurRadius ?: if (isDark) 6.dp else 8.dp
            if (isDark) {
                GlassStyle(
                    blur = blur,
                    baseColor = Color(0xFF15181E).copy(alpha = 0.22f),
                    overlayColor = Color.White.copy(alpha = 0.05f),
                    borderColor = Color.White.copy(alpha = 0.16f),
                    highlightAlpha = 0.62f,
                    shadowAlpha = 0.10f,
                    fallbackAlpha = 0.74f,
                    lensStart = 24.dp,
                    lensEnd = 24.dp,
                    showBorder = true
                )
            } else {
                GlassStyle(
                    blur = blur,
                    baseColor = Color.White.copy(alpha = 0.68f),
                    overlayColor = Color(0xFFDCEBFF).copy(alpha = 0.24f),
                    borderColor = Color.White.copy(alpha = 0.98f),
                    highlightAlpha = 1f,
                    shadowAlpha = 0.18f,
                    fallbackAlpha = 0.96f,
                    lensStart = 25.dp,
                    lensEnd = 25.dp,
                    showBorder = true
                )
            }
        }
        GlassVariant.Compact -> {
            val blur = blurRadius ?: if (isDark) 5.dp else 6.dp
            if (isDark) {
                GlassStyle(
                    blur = blur,
                    baseColor = Color(0xFF13161B).copy(alpha = 0.18f),
                    overlayColor = Color.White.copy(alpha = 0.04f),
                    borderColor = Color.White.copy(alpha = 0.14f),
                    highlightAlpha = 0.54f,
                    shadowAlpha = 0.09f,
                    fallbackAlpha = 0.72f,
                    lensStart = 22.dp,
                    lensEnd = 22.dp,
                    showBorder = true
                )
            } else {
                GlassStyle(
                    blur = blur,
                    baseColor = Color.White.copy(alpha = 0.60f),
                    overlayColor = Color(0xFFDCEBFF).copy(alpha = 0.18f),
                    borderColor = Color.White.copy(alpha = 0.95f),
                    highlightAlpha = 1f,
                    shadowAlpha = 0.15f,
                    fallbackAlpha = 0.92f,
                    lensStart = 22.dp,
                    lensEnd = 22.dp,
                    showBorder = true
                )
            }
        }
        GlassVariant.Content -> {
            val blur = blurRadius ?: if (isDark) 7.dp else 11.dp
            if (isDark) {
                GlassStyle(
                    blur = blur,
                    baseColor = Color(0xFF0F1115).copy(alpha = 0.62f),
                    overlayColor = Color.White.copy(alpha = 0.07f),
                    borderColor = Color.White.copy(alpha = 0.26f),
                    highlightAlpha = 0.96f,
                    shadowAlpha = 0.18f,
                    fallbackAlpha = 0.92f,
                    lensStart = 26.dp,
                    lensEnd = 28.dp,
                    showBorder = true
                )
            } else {
                GlassStyle(
                    blur = blur,
                    baseColor = Color.White.copy(alpha = 0.66f),
                    overlayColor = Color(0xFFDCEBFF).copy(alpha = 0.30f),
                    borderColor = Color.White.copy(alpha = 0.96f),
                    highlightAlpha = 1f,
                    shadowAlpha = 0.18f,
                    fallbackAlpha = 0.98f,
                    lensStart = 26.dp,
                    lensEnd = 28.dp,
                    showBorder = true
                )
            }
        }
    }
}
