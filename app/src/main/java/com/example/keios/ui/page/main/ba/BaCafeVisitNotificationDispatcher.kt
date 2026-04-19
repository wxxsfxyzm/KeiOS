package com.example.keios.ui.page.main.ba

import android.content.Context
import android.content.pm.PackageManager
import com.example.keios.R
import com.example.keios.mcp.service.McpKeepAliveService
import com.example.keios.mcp.notification.McpNotificationHelper
import com.example.keios.mcp.notification.McpNotificationPayload
import com.example.keios.ui.page.main.ba.support.baServerLabel
import com.example.keios.ui.page.main.ba.support.serverRefreshTimeZone
import java.util.Calendar

internal object BaCafeVisitNotificationDispatcher {
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
        val detailLine = buildVisitDetailLine(
            context = context,
            serverIndex = serverIndex,
            slotMs = slotMs
        )

        runCatching {
            McpKeepAliveService.startOrUpdate(
                context = context,
                serverName = McpNotificationPayload.BA_CAFE_VISIT_SERVER_NAME,
                running = true,
                port = 0,
                path = detailLine,
                clients = 0,
                forceStart = true,
                notificationId = McpNotificationHelper.BA_CAFE_VISIT_NOTIFICATION_ID,
                heartbeatEnabled = false
            )
        }.onFailure {
            McpNotificationHelper.notifyTest(
                context = context,
                serverName = McpNotificationPayload.BA_CAFE_VISIT_SERVER_NAME,
                running = true,
                port = 0,
                path = detailLine,
                clients = 0,
            )
        }
        return true
    }

    private fun buildVisitDetailLine(
        context: Context,
        serverIndex: Int,
        slotMs: Long,
    ): String {
        val timeZone = serverRefreshTimeZone(serverIndex)
        val calendar = Calendar.getInstance(timeZone).apply {
            timeInMillis = slotMs.coerceAtLeast(0L)
        }
        val slotHour = calendar.get(Calendar.HOUR_OF_DAY).coerceIn(0, 23)
        return context.getString(
            R.string.ba_cafe_visit_notification_content_detail,
            baServerLabel(serverIndex),
            slotHour
        )
    }
}
