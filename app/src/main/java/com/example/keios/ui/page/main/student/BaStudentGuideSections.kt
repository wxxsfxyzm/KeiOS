package com.example.keios.ui.page.main.student

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.example.keios.ui.page.main.student.section.buildGuideCopyPayload
import com.example.keios.ui.page.main.student.section.rememberGuideCopyAction
import com.example.keios.ui.page.main.widget.core.MiuixInfoItem
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

internal const val IMAGE_TAP_DISMISS_GESTURE_COOLDOWN_MS = 260L
internal const val IMAGE_TAP_DISMISS_SCALE_EPSILON = 0.035f
internal const val IMAGE_TAP_DISMISS_OFFSET_EPSILON_PX = 18f
internal const val IMAGE_BACK_GESTURE_TRANSLATION_FACTOR = 0.2f
internal const val IMAGE_BACK_GESTURE_CONTENT_FADE_FACTOR = 0.16f
internal const val IMAGE_BACK_GESTURE_SCRIM_FADE_FACTOR = 0.72f
internal const val GUIDE_INLINE_GIF_CACHE_SCOPE = "https://www.gamekee.com/__guide_inline_gif_scope"
internal val guideCircledNumbers = listOf(
    "①", "②", "③", "④", "⑤", "⑥", "⑦", "⑧", "⑨", "⑩",
    "⑪", "⑫", "⑬", "⑭", "⑮", "⑯", "⑰", "⑱", "⑲", "⑳"
)
internal val guideSkillTypeNumericSuffixPattern = Regex("""^(.*?)[\s\-_]*(\d{1,2})$""")
internal val guideSkillTypeCircledSuffixPattern = Regex("""^(.*?)([①②③④⑤⑥⑦⑧⑨⑩⑪⑫⑬⑭⑮⑯⑰⑱⑲⑳])$""")
internal val guideSkillTypeBracketPattern = Regex("""[（(【\[]([^()（）【】\[\]]+)[)）】\]]""")
internal val guideSkillTypeStateSplitPattern = Regex("""\s*[、,，/／|｜+＋]\s*""")

internal enum class GuideVideoControlAction {
    TogglePlayPause
}

@Composable
fun GuideRowsSection(
    rows: List<BaGuideRow>,
    emptyText: String,
    imageHeight: androidx.compose.ui.unit.Dp = 96.dp
) {
    if (rows.isEmpty()) {
        Text(emptyText, color = MiuixTheme.colorScheme.onBackgroundVariant)
        return
    }
    val visibleRows = rows.take(120)
    visibleRows.forEachIndexed { index, row ->
        val key = row.key.ifBlank { "信息" }
        val hasImage = row.imageUrl.isNotBlank()
        val value = row.value
            .takeIf { it.isNotBlank() && it != "图片" }
            ?: if (hasImage) "见下图" else "-"
        MiuixInfoItem(
            key = key,
            value = value,
            onLongClick = rememberGuideCopyAction(buildGuideCopyPayload(key, value))
        )
        if (hasImage) {
            Spacer(modifier = androidx.compose.ui.Modifier.height(6.dp))
            GuideRemoteImage(
                imageUrl = row.imageUrl,
                imageHeight = imageHeight
            )
        }
        if (index < visibleRows.lastIndex) {
            Spacer(modifier = androidx.compose.ui.Modifier.height(8.dp))
        }
    }
}
