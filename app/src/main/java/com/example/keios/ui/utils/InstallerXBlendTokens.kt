package com.example.keios.ui.utils

import androidx.compose.ui.graphics.Color
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurBlendMode

/**
 * Inspired by InstallerX-Revived blend token strategy.
 * This keeps all glass blend presets centralized and reusable.
 */
object InstallerXBlendTokens {

    val BottomBarLight = listOf(
        BlendColorEntry(Color(0x7F040404), BlurBlendMode.Overlay),
        BlendColorEntry(Color(0x26F1F1F1), BlurBlendMode.ColorDodge),
        BlendColorEntry(Color(0x1AC8C8C8), BlurBlendMode.SrcOver)
    )

    val BottomBarDark = listOf(
        BlendColorEntry(Color(0x667A7A7A), BlurBlendMode.ColorBurn),
        BlendColorEntry(Color(0x33747474), BlurBlendMode.Overlay),
        BlendColorEntry(Color(0x322B2B2B), BlurBlendMode.SrcOver)
    )

    val CardLight = listOf(
        BlendColorEntry(Color(0x4DA9A9A9), BlurBlendMode.Luminosity),
        BlendColorEntry(Color(0x1A2B2B2B), BlurBlendMode.PlusDarker)
    )

    val CardDark = listOf(
        BlendColorEntry(Color(0x4C000000), BlurBlendMode.ColorBurn),
        BlendColorEntry(Color(0x80030303), BlurBlendMode.SrcOver)
    )
}
