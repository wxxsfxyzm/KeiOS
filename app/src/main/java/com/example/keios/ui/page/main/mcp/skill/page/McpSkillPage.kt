package com.example.keios.ui.page.main.mcp.skill.page

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.keios.mcp.server.McpServerManager
import com.example.keios.ui.page.main.mcp.skill.component.McpSkillContentList
import com.example.keios.ui.page.main.mcp.skill.state.rememberMcpSkillPageContentState
import com.example.keios.ui.page.main.mcp.skill.state.rememberMcpSkillPageTextBundle
import com.example.keios.ui.page.main.widget.chrome.AppPageScaffold
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun McpSkillPage(
    mcpServerManager: McpServerManager,
    onBack: () -> Unit
) {
    val textBundle = rememberMcpSkillPageTextBundle()
    val listState = rememberLazyListState()
    val scrollBehavior = MiuixScrollBehavior()
    val titleColor = MiuixTheme.colorScheme.onBackground
    val subtitleColor = MiuixTheme.colorScheme.onBackgroundVariant
    val accentColor = MiuixTheme.colorScheme.primary
    val codeColor = MiuixTheme.colorScheme.primary.copy(alpha = 0.10f)
    val contentState = rememberMcpSkillPageContentState(
        mcpServerManager = mcpServerManager,
        listState = listState,
        emptyMarkdown = textBundle.emptyMarkdown,
        defaultRootTitle = textBundle.defaultRootTitle,
        defaultOverviewTitle = textBundle.defaultOverviewTitle,
        defaultContentTitle = textBundle.defaultContentTitle,
        emptyContentText = textBundle.emptyContentText
    )

    AppPageScaffold(
        title = textBundle.pageTitle,
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
        McpSkillContentList(
            innerPadding = innerPadding,
            listState = listState,
            nestedScrollConnection = scrollBehavior.nestedScrollConnection,
            contentState = contentState,
            markdownSubtitle = textBundle.markdownSubtitle,
            emptyItemText = textBundle.emptyItemText,
            titleColor = titleColor,
            subtitleColor = subtitleColor,
            accentColor = accentColor,
            codeColor = codeColor,
            modifier = Modifier.fillMaxSize()
        )
    }
}
