package com.example.keios.ui.page.main.widget

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.Dp

fun appPageContentPadding(
    innerPadding: PaddingValues,
    bottomExtra: Dp = AppChromeTokens.pageBottomInsetExtra
): PaddingValues {
    return PaddingValues(
        top = innerPadding.calculateTopPadding(),
        bottom = innerPadding.calculateBottomPadding() + bottomExtra,
        start = AppChromeTokens.pageHorizontalPadding,
        end = AppChromeTokens.pageHorizontalPadding
    )
}

fun appPageBottomPaddingWithFloatingOverlay(contentBottomPadding: Dp): Dp {
    return contentBottomPadding + AppChromeTokens.pageFloatingOverlayBottomExtra
}
