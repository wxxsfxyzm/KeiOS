package com.example.keios.mcp.framework.notification.builder

import android.app.Notification

interface InstallerNotificationBuilder {
    fun build(payload: NotificationPayload): Notification
}

