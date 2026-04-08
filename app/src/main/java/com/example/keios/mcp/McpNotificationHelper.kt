package com.example.keios.mcp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.keios.MainActivity

object McpNotificationHelper {

    const val CHANNEL_ID = "mcp_keepalive_channel"
    const val KEEPALIVE_NOTIFICATION_ID = 38888
    private const val TEST_NOTIFICATION_ID = 38889

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val existing = manager.getNotificationChannel(CHANNEL_ID)
        if (existing != null) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "MCP KeepAlive",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "用于维持 MCP Server 前台保活与超级岛状态显示"
            setShowBadge(false)
        }
        manager.createNotificationChannel(channel)
    }

    fun buildNotification(
        context: Context,
        running: Boolean,
        port: Int,
        path: String,
        clients: Int,
        ongoing: Boolean
    ): android.app.Notification {
        val title = if (running) "MCP Server 正在后台运行" else "MCP Server 通知测试"
        val content = if (running) {
            "MCP 协议 · 端口 $port · 在线 $clients"
        } else {
            "MCP 协议 · 端口 $port · 测试通知"
        }
        val shortText = if (running) "MCP $port" else "MCP Test"

        val openIntent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            1101,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(context.applicationInfo.icon)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$content\nEndpoint: http://127.0.0.1:$port$path"))
            .setContentIntent(openPendingIntent)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(ongoing)
            .setOnlyAlertOnce(true)

        McpIslandNotificationBuilder.buildExtras(
            context = context,
            title = title,
            content = content,
            shortText = shortText
        )?.let(builder::addExtras)

        return builder.build()
    }

    fun notifyTest(
        context: Context,
        running: Boolean,
        port: Int,
        path: String,
        clients: Int
    ) {
        ensureChannel(context)
        NotificationManagerCompat.from(context).notify(
            TEST_NOTIFICATION_ID,
            buildNotification(
                context = context,
                running = running,
                port = port,
                path = path,
                clients = clients,
                ongoing = false
            )
        )
    }
}

