package com.example.keios.ui.page.main.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun AppTopBar(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    showSubtitle: Boolean = false,
    bottomSpacing: Dp = 6.dp,
    compactTopOffset: Dp = (-6).dp,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        SmallTopAppBar(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = compactTopOffset),
            title = "",
            color = Color.Transparent,
            navigationIcon = {
                Row {
                    navigationIcon?.invoke()
                    if (navigationIcon != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = title,
                        color = MiuixTheme.colorScheme.onBackground
                    )
                }
            },
            actions = actions
        )
        if (showSubtitle && !subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                color = MiuixTheme.colorScheme.onBackgroundVariant,
                modifier = Modifier.padding(horizontal = 14.dp)
            )
        }
        Spacer(modifier = Modifier.height(bottomSpacing))
    }
}
