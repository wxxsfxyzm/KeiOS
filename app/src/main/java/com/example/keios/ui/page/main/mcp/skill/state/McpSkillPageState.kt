package com.example.keios.ui.page.main.mcp.skill.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import com.example.keios.mcp.server.McpServerManager
import com.example.keios.ui.page.main.mcp.skill.model.SkillSection
import com.example.keios.ui.page.main.mcp.skill.support.buildSkillSections
import com.example.keios.ui.page.main.mcp.skill.support.parseMarkdownBlocks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Stable
internal data class McpSkillPageContentState(
    val sections: List<SkillSection>
)

@Composable
internal fun rememberMcpSkillPageContentState(
    mcpServerManager: McpServerManager,
    emptyMarkdown: String,
    defaultRootTitle: String,
    defaultOverviewTitle: String,
    defaultContentTitle: String,
    emptyContentText: String
): McpSkillPageContentState {
    val markdown by produceState(initialValue = "", mcpServerManager) {
        value = withContext(Dispatchers.IO) {
            mcpServerManager.getSkillMarkdown()
        }
    }
    val sections by produceState(
        initialValue = emptyList<SkillSection>(),
        markdown,
        emptyMarkdown,
        defaultRootTitle,
        defaultOverviewTitle,
        defaultContentTitle,
        emptyContentText
    ) {
        value = withContext(Dispatchers.Default) {
            val blocks = parseMarkdownBlocks(markdown.ifBlank { emptyMarkdown })
            buildSkillSections(
                blocks = blocks,
                defaultRootTitle = defaultRootTitle,
                defaultOverviewTitle = defaultOverviewTitle,
                defaultContentTitle = defaultContentTitle,
                emptyContentText = emptyContentText
            )
        }
    }

    return remember(sections) {
        McpSkillPageContentState(
            sections = sections
        )
    }
}
