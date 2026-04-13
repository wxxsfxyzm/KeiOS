package com.example.keios.ui.page.main.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun SheetContentColumn(
    modifier: Modifier = Modifier,
    scrollable: Boolean = true,
    verticalSpacing: Dp = 12.dp,
    content: @Composable () -> Unit,
) {
    val scrollModifier = if (scrollable) {
        Modifier.verticalScroll(rememberScrollState())
    } else {
        Modifier
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(scrollModifier)
            .navigationBarsPadding()
            .imePadding()
            .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(verticalSpacing)
    ) {
        content()
    }
}

@Composable
fun SheetRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

@Composable
fun SheetInputTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        color = MiuixTheme.colorScheme.onBackgroundVariant.copy(alpha = 0.92f),
        modifier = modifier
    )
}

@Composable
fun SheetSectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    danger: Boolean = false,
) {
    Text(
        text = text,
        color = if (danger) MiuixTheme.colorScheme.error else MiuixTheme.colorScheme.onBackground,
        fontWeight = FontWeight.Medium,
        modifier = modifier
    )
}

@Composable
fun SheetDescriptionText(
    text: String,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    val shape = RoundedCornerShape(12.dp)
    Text(
        text = text,
        color = MiuixTheme.colorScheme.onBackgroundVariant.copy(alpha = 0.96f),
        fontSize = 14.sp,
        lineHeight = 20.sp,
        textAlign = TextAlign.Start,
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MiuixTheme.colorScheme.surfaceContainer.copy(alpha = 0.52f))
            .border(
                width = 1.dp,
                color = MiuixTheme.colorScheme.onBackgroundVariant.copy(alpha = 0.12f),
                shape = shape
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        maxLines = maxLines,
        overflow = overflow,
    )
}

@Composable
fun SheetSectionCard(
    modifier: Modifier = Modifier,
    verticalSpacing: Dp = 8.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MiuixTheme.colorScheme.surfaceContainer.copy(alpha = 0.56f))
            .border(
                width = 1.dp,
                color = MiuixTheme.colorScheme.onBackgroundVariant.copy(alpha = 0.14f),
                shape = shape
            )
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(verticalSpacing),
        content = content
    )
}

@Composable
fun SheetControlRow(
    label: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
    labelColor: Color = MiuixTheme.colorScheme.onBackground,
    minHeight: Dp = 40.dp,
    trailing: @Composable RowScope.() -> Unit,
) {
    SheetControlRow(
        modifier = modifier,
        summary = summary,
        minHeight = minHeight,
        labelContent = {
            Text(
                text = label,
                color = labelColor,
                fontWeight = FontWeight.Medium
            )
        },
        trailing = trailing
    )
}

@Composable
fun SheetControlRow(
    modifier: Modifier = Modifier,
    summary: String? = null,
    minHeight: Dp = 40.dp,
    labelContent: @Composable ColumnScope.() -> Unit,
    trailing: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = minHeight),
            verticalArrangement = Arrangement.Center
        ) {
            labelContent()
            summary?.takeIf { it.isNotBlank() }?.let { text ->
                Text(
                    text = text,
                    color = MiuixTheme.colorScheme.onBackgroundVariant.copy(alpha = 0.92f),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = trailing
        )
    }
}
