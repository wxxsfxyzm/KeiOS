package com.example.keios.ui.page.main.mcp.skill.component

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.example.keios.ui.page.main.mcp.skill.state.McpSkillPageContentState
import com.example.keios.ui.page.main.widget.chrome.AppPageLazyColumn
import top.yukonga.miuix.kmp.basic.SmallTitle

@Composable
internal fun McpSkillContentList(
    innerPadding: PaddingValues,
    listState: LazyListState,
    nestedScrollConnection: NestedScrollConnection,
    contentState: McpSkillPageContentState,
    markdownSubtitle: String,
    emptyItemText: String,
    titleColor: Color,
    subtitleColor: Color,
    accentColor: Color,
    codeColor: Color,
    modifier: Modifier = Modifier
) {
    AppPageLazyColumn(
        innerPadding = innerPadding,
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection),
        topExtra = 0.dp,
        bottomExtra = 16.dp,
        sectionSpacing = 10.dp
    ) {
        item {
            SmallTitle(
                modifier = Modifier.alpha(contentState.subtitleAlpha),
                text = markdownSubtitle
            )
        }
        item {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((10 * contentState.subtitleAlpha).dp)
            )
        }
        items(items = contentState.sections) { section ->
            SkillSectionCard(
                section = section,
                titleColor = titleColor,
                subtitleColor = subtitleColor,
                accentColor = accentColor,
                codeColor = codeColor,
                emptyItemText = emptyItemText
            )
        }
    }
}
