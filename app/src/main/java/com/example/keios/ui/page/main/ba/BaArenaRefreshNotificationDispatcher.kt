package com.example.keios.ui.page.main.ba

import android.content.Context
import android.content.pm.PackageManager
import com.example.keios.R
import com.example.keios.mcp.service.McpKeepAliveService
import com.example.keios.mcp.notification.McpNotificationHelper
import com.example.keios.mcp.notification.McpNotificationPayload
import com.example.keios.ui.page.main.ba.support.baServerLabel
import com.example.keios.ui.page.main.ba.support.serverRefreshTimeZone

internal object BaArenaRefreshNotificationDispatcher {
    fun send(
        context: Context,
        serverIndex: Int,
        slotMs: Long,
    ): Boolean {
        val notificationsGranted = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        if (!notificationsGranted) return false

        val detailLine = buildRefreshDetailLine(
            context = context,
            serverIndex = serverIndex,
            slotMs = slotMs
        )

        runCatching {
            McpKeepAliveService.startOrUpdate(
                context = context,
                serverName = McpNotificationPayload.BA_ARENA_REFRESH_SERVER_NAME,
                running = true,
                port = 0,
                path = detailLine,
                clients = 0,
                forceStart = true,
                notificationId = McpNotificationHelper.BA_ARENA_REFRESH_NOTIFICATION_ID,
                heartbeatEnabled = false
            )
        }.onFailure {
            McpNotificationHelper.notifyTest(
                context = context,
                serverName = McpNotificationPayload.BA_ARENA_REFRESH_SERVER_NAME,
                running = true,
                port = 0,
                path = detailLine,
                clients = 0,
            )
        }
        return true
    }

    private fun buildRefreshDetailLine(
        context: Context,
        serverIndex: Int,
        slotMs: Long,
    ): String {
        val timeZone = serverRefreshTimeZone(serverIndex)
        val calendar = java.util.Calendar.getInstance(timeZone).apply {
            timeInMillis = slotMs.coerceAtLeast(0L)
        }
        val slotHour = calendar.get(java.util.Calendar.HOUR_OF_DAY).coerceIn(0, 23)
        return context.getString(
            R.string.ba_arena_refresh_notification_content_detail,
            baServerLabel(serverIndex),
            slotHour
        )
    }
}
