package com.example.keios.ui.page.main.mcp.model

import androidx.compose.ui.graphics.Color

internal data class McpOverviewMetric(
    val label: String,
    val value: String,
    val valueColor: Color? = null,
    val spanFullWidth: Boolean = false,
    val valueMaxLines: Int = 2,
    val labelWeight: Float = 0.58f,
    val valueWeight: Float = 0.42f
)

internal fun String.toMcpTokenPreview(): String {
    val token = trim()
    if (token.isBlank()) return ""
    if (token.length <= 8) return token
    return "${token.take(4)}...${token.takeLast(4)}"
}
