package os.kei.ui.page.main.mcp.skill.model

internal sealed interface MarkdownBlock {
    data class Heading(val level: Int, val text: String) : MarkdownBlock
    data class Paragraph(val text: String) : MarkdownBlock
    data class Bullet(val text: String) : MarkdownBlock
    data class Ordered(val index: Int, val text: String) : MarkdownBlock
    data class Code(val text: String) : MarkdownBlock
}

internal data class SkillSection(
    val level: Int,
    val title: String,
    val items: List<SkillSectionItem>
)

internal sealed interface SkillSectionItem {
    data class SubHeading(val level: Int, val text: String) : SkillSectionItem
    data class Paragraph(val text: String) : SkillSectionItem
    data class Bullet(val text: String) : SkillSectionItem
    data class Ordered(val index: Int, val text: String) : SkillSectionItem
    data class Code(val text: String) : SkillSectionItem
}

internal sealed interface InlineToken {
    data class Plain(val text: String) : InlineToken
    data class Emphasis(val text: String) : InlineToken
    data class Code(val text: String) : InlineToken
    data class Link(val label: String, val url: String) : InlineToken
}
