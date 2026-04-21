package com.example.keios.ui.page.main.mcp.skill.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.example.keios.R

@Stable
internal data class McpSkillPageTextBundle(
    val pageTitle: String,
    val markdownSubtitle: String,
    val emptyMarkdown: String,
    val emptyItemText: String,
    val defaultRootTitle: String,
    val defaultOverviewTitle: String,
    val defaultContentTitle: String,
    val emptyContentText: String,
    val clawCardTitle: String,
    val clawCardSummary: String,
    val clawPrompt: String,
    val copyClawPromptText: String,
    val clawPromptCopiedToast: String
)

@Composable
internal fun rememberMcpSkillPageTextBundle(): McpSkillPageTextBundle {
    val pageTitle = stringResource(R.string.mcp_skill_page_title)
    val markdownSubtitle = stringResource(R.string.mcp_skill_page_subtitle)
    val emptyMarkdown = stringResource(R.string.mcp_skill_markdown_empty)
    val emptyItemText = stringResource(R.string.mcp_skill_section_empty_items)
    val defaultRootTitle = stringResource(R.string.mcp_skill_section_default_root)
    val defaultOverviewTitle = stringResource(R.string.mcp_skill_section_default_overview)
    val defaultContentTitle = stringResource(R.string.mcp_skill_section_default_content)
    val emptyContentText = stringResource(R.string.mcp_skill_section_empty_content)
    val clawCardTitle = stringResource(R.string.mcp_skill_claw_card_title)
    val clawCardSummary = stringResource(R.string.mcp_skill_claw_card_summary)
    val clawPrompt = stringResource(R.string.mcp_skill_claw_prompt)
    val copyClawPromptText = stringResource(R.string.mcp_skill_action_copy_claw_prompt)
    val clawPromptCopiedToast = stringResource(R.string.mcp_skill_toast_claw_prompt_copied)
    return remember(
        pageTitle,
        markdownSubtitle,
        emptyMarkdown,
        emptyItemText,
        defaultRootTitle,
        defaultOverviewTitle,
        defaultContentTitle,
        emptyContentText,
        clawCardTitle,
        clawCardSummary,
        clawPrompt,
        copyClawPromptText,
        clawPromptCopiedToast
    ) {
        McpSkillPageTextBundle(
            pageTitle = pageTitle,
            markdownSubtitle = markdownSubtitle,
            emptyMarkdown = emptyMarkdown,
            emptyItemText = emptyItemText,
            defaultRootTitle = defaultRootTitle,
            defaultOverviewTitle = defaultOverviewTitle,
            defaultContentTitle = defaultContentTitle,
            emptyContentText = emptyContentText,
            clawCardTitle = clawCardTitle,
            clawCardSummary = clawCardSummary,
            clawPrompt = clawPrompt,
            copyClawPromptText = copyClawPromptText,
            clawPromptCopiedToast = clawPromptCopiedToast
        )
    }
}
