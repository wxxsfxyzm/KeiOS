package com.example.keios.ui.page.main.mcp.skill.page

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.keios.R
import com.example.keios.mcp.server.McpServerManager
import com.example.keios.ui.page.main.mcp.skill.component.SkillSectionCard
import com.example.keios.ui.page.main.mcp.skill.state.rememberMcpSkillPageContentState
import com.example.keios.ui.page.main.widget.chrome.AppPageLazyColumn
import com.example.keios.ui.page.main.widget.chrome.AppPageScaffold
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun McpSkillPage(
    mcpServerManager: McpServerManager,
    onBack: () -> Unit
) {
    val pageTitle = stringResource(R.string.mcp_skill_page_title)
    val markdownSubtitle = stringResource(R.string.mcp_skill_page_subtitle)
    val emptyMarkdown = stringResource(R.string.mcp_skill_markdown_empty)
    val emptyItemText = stringResource(R.string.mcp_skill_section_empty_items)
    val defaultRootTitle = stringResource(R.string.mcp_skill_section_default_root)
    val defaultOverviewTitle = stringResource(R.string.mcp_skill_section_default_overview)
    val defaultContentTitle = stringResource(R.string.mcp_skill_section_default_content)
    val emptyContentText = stringResource(R.string.mcp_skill_section_empty_content)

    val listState = rememberLazyListState()
    val scrollBehavior = MiuixScrollBehavior()
    val titleColor = MiuixTheme.colorScheme.onBackground
    val subtitleColor = MiuixTheme.colorScheme.onBackgroundVariant
    val accentColor = MiuixTheme.colorScheme.primary
    val codeColor = MiuixTheme.colorScheme.primary.copy(alpha = 0.10f)
    val contentState = rememberMcpSkillPageContentState(
        mcpServerManager = mcpServerManager,
        listState = listState,
        emptyMarkdown = emptyMarkdown,
        defaultRootTitle = defaultRootTitle,
        defaultOverviewTitle = defaultOverviewTitle,
        defaultContentTitle = defaultContentTitle,
        emptyContentText = emptyContentText
    )

    AppPageScaffold(
        title = pageTitle,
        modifier = Modifier.fillMaxSize(),
        scrollBehavior = scrollBehavior,
        topBarColor = MiuixTheme.colorScheme.surface,
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = MiuixIcons.Regular.Back,
                    contentDescription = null,
                    tint = MiuixTheme.colorScheme.onSurface
                )
            }
        }
    ) { innerPadding ->
        AppPageLazyColumn(
            innerPadding = innerPadding,
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
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
}
