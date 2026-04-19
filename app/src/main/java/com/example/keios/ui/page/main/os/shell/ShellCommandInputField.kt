package com.example.keios.ui.page.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.keios.ui.page.main.widget.AppTypographyTokens
import top.yukonga.miuix.kmp.theme.MiuixTheme

@androidx.compose.runtime.Composable
internal fun ShellCommandInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    minHeight: Dp = 136.dp,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val shape: CornerBasedShape = RoundedCornerShape(18.dp)
    val textColor = MiuixTheme.colorScheme.onBackground
    val placeholderColor = MiuixTheme.colorScheme.onBackgroundVariant.copy(alpha = if (isDark) 0.72f else 0.64f)
    val borderColor = if (isDark) {
        Color(0xFF9CCBFF).copy(alpha = 0.30f)
    } else {
        Color(0xFFBFD8F8).copy(alpha = 0.92f)
    }
    val baseColor = if (isDark) {
        Color(0xFF121A24).copy(alpha = 0.46f)
    } else {
        Color.White.copy(alpha = 0.72f)
    }
    val overlayColor = if (isDark) {
        Color(0xFF82B6F5).copy(alpha = 0.09f)
    } else {
        Color(0xFFE4F1FF).copy(alpha = 0.26f)
    }
    val textStyle = TextStyle(
        color = textColor,
        fontSize = AppTypographyTokens.Body.fontSize,
        lineHeight = AppTypographyTokens.Body.lineHeight,
        platformStyle = PlatformTextStyle(includeFontPadding = false)
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(baseColor, shape)
            .background(overlayColor, shape)
            .border(width = 1.dp, color = borderColor, shape = shape)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = false,
            textStyle = textStyle,
            cursorBrush = SolidColor(MiuixTheme.colorScheme.primary),
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = minHeight),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = minHeight)
                ) {
                    if (value.isBlank()) {
                        BasicText(
                            text = label,
                            style = textStyle.copy(color = placeholderColor),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}
