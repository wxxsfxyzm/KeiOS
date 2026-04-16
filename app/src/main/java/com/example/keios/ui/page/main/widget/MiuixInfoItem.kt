package com.example.keios.ui.page.main.widget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun MiuixInfoItem(
    key: String,
    value: String,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    valueColor: Color? = null
) {
    val displayKey = key.ifBlank { "信息" }
    val displayValue = value.ifBlank { "N/A" }
    val copyPayload = buildTextCopyPayload(displayKey, displayValue)
    CopyModeSelectionContainer {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .copyModeAwareRow(
                    copyPayload = copyPayload,
                    onClick = onClick,
                    onLongClick = onLongClick
                )
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = displayKey,
                color = MiuixTheme.colorScheme.onBackgroundVariant,
                modifier = Modifier.weight(0.42f),
                maxLines = Int.MAX_VALUE,
                overflow = TextOverflow.Clip
            )
            Text(
                text = displayValue,
                color = valueColor ?: MiuixTheme.colorScheme.onBackground,
                modifier = Modifier.weight(0.58f),
                textAlign = TextAlign.End,
                maxLines = Int.MAX_VALUE,
                overflow = TextOverflow.Clip,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
