package com.example.keios.mcp.domain.notification

import android.app.Notification
import com.example.keios.mcp.notification.McpNotificationPayload
import com.example.keios.mcp.framework.notification.builder.NotificationRenderStyle

interface SessionNotifier {
    data class NotificationBuildResult(
        val notification: Notification,
        val style: NotificationRenderStyle,
        val useXiaomiMagic: Boolean
    )

    fun build(payload: McpNotificationPayload): NotificationBuildResult
}

