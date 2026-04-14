package com.example.keios.mcp.framework.notification.builder

import com.example.keios.mcp.McpNotificationPayload

data class NotificationPayload(
    val state: McpNotificationPayload,
    val settings: UserSettings,
    val environment: EnvironmentContext
)

data class UserSettings(
    val miIslandOuterGlow: Boolean
)

data class EnvironmentContext(
    val channelId: String,
    val isHyperOS: Boolean
)

enum class NotificationRenderStyle {
    MI_ISLAND,
    LIVE_UPDATE,
    LEGACY
}

