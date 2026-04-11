package com.example.keios.mcp

import android.app.Notification
import android.content.Context

interface McpNotificationBuilder {
    fun build(context: Context, payload: McpNotificationPayload): Notification
}

