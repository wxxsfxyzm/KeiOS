package com.example.keios.ui.page.main.mcp.skill.component

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.keios.ui.page.main.mcp.skill.support.buildInlineStyledText
import com.example.keios.ui.page.main.mcp.util.copyToClipboard
import com.example.keios.ui.page.main.widget.core.AppSurfaceCard
import com.example.keios.ui.page.main.widget.support.CopyModeSelectionContainer
import com.example.keios.ui.page.main.widget.support.copyModeAwareRow
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton

@Composable
internal fun McpSkillClawGuideCard(
    title: String,
    summary: String,
    prompt: String,
    copyButtonText: String,
    copiedToastText: String,
    titleColor: Color,
    subtitleColor: Color,
    accentColor: Color,
    codeColor: Color
) {
    val context = LocalContext.current
    AppSurfaceCard(
        containerColor = Color(0x223B82F6),
        contentColor = titleColor,
        showIndication = false
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = titleColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                )
                TextButton(
                    text = copyButtonText,
                    onClick = {
                        copyToClipboard(context, "claw-skill-prompt", prompt)
                        Toast.makeText(context, copiedToastText, Toast.LENGTH_SHORT).show()
                    }
                )
            }

            CopyModeSelectionContainer {
                Text(
                    text = buildInlineStyledText(
                        text = summary,
                        baseStyle = SpanStyle(color = subtitleColor),
                        accentStyle = SpanStyle(
                            color = accentColor,
                            background = accentColor.copy(alpha = 0.10f),
                            fontWeight = FontWeight.Medium
                        ),
                        linkStyle = SpanStyle(
                            color = accentColor,
                            textDecoration = TextDecoration.Underline,
                            fontWeight = FontWeight.Medium
                        )
                    ),
                    color = subtitleColor,
                    fontSize = 14.sp,
                    lineHeight = 21.sp
                )
            }

            AppSurfaceCard(
                containerColor = codeColor,
                contentColor = titleColor,
                showIndication = false
            ) {
                CopyModeSelectionContainer {
                    Text(
                        text = prompt,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .copyModeAwareRow(copyPayload = prompt),
                        color = titleColor,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                }
            }
        }
    }
}
