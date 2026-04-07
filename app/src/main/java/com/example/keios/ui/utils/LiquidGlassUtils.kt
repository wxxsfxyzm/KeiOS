package com.example.keios.ui.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.blur.BlurColors
import top.yukonga.miuix.kmp.blur.BlurDefaults
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.isRenderEffectSupported
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.textureBlur

@Composable
fun rememberBottomBarBlurColors(): BlurColors {
    val isDark = isSystemInDarkTheme()
    return remember(isDark) {
        BlurColors(
            blendColors = if (isDark) {
                InstallerXBlendTokens.BottomBarDark
            } else {
                InstallerXBlendTokens.BottomBarLight
            }
        )
    }
}

@Composable
fun rememberCardBlurColors(): BlurColors {
    val isDark = isSystemInDarkTheme()
    return remember(isDark) {
        BlurColors(
            blendColors = if (isDark) {
                InstallerXBlendTokens.CardDark
            } else {
                InstallerXBlendTokens.CardLight
            }
        )
    }
}

fun Modifier.installerXLiquidGlass(
    backdrop: LayerBackdrop,
    blurColors: BlurColors,
    cornerRadiusDp: Int,
    blurRadius: Float,
    contentBlendMode: BlendMode = BlendMode.SrcOver,
): Modifier {
    val shape = RoundedCornerShape(cornerRadiusDp.dp)
    val blurEnabled = isRenderEffectSupported() || isRuntimeShaderSupported()
    return this
        .textureBlur(
            backdrop = backdrop,
            shape = shape,
            blurRadius = blurRadius,
            noiseCoefficient = BlurDefaults.NoiseCoefficient,
            colors = blurColors,
            contentBlendMode = contentBlendMode,
            enabled = blurEnabled
        )
        .background(Color.White.copy(alpha = 0.16f), shape)
        .border(1.dp, Color.White.copy(alpha = 0.20f), shape)
}
