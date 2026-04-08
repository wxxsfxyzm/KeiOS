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
import com.example.keios.R
import java.util.concurrent.TimeUnit
import rikka.shizuku.Shizuku

object McpNotificationHelper {

    const val CHANNEL_ID = "mcp_keepalive_channel"
    const val CHANNEL_ID_ISLAND = "mcp_super_island_channel"
    const val KEEPALIVE_NOTIFICATION_ID = 38888
    private const val TEST_NOTIFICATION_ID = 38889
    private const val ISLAND_STATUS_NOTIFICATION_ID = 38890

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            val keepalive = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.mcp_keepalive_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.mcp_keepalive_channel_desc)
                setShowBadge(false)
            }
            manager.createNotificationChannel(keepalive)
        }
        if (manager.getNotificationChannel(CHANNEL_ID_ISLAND) == null) {
            val island = NotificationChannel(
                CHANNEL_ID_ISLAND,
                context.getString(R.string.mcp_island_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.mcp_island_channel_desc)
                setShowBadge(false)
            }
            manager.createNotificationChannel(island)
        }
    }

    fun buildForegroundNotification(
        context: Context,
        running: Boolean,
        port: Int,
        path: String,
        clients: Int,
        ongoing: Boolean
    ): android.app.Notification {
        val title = if (running) "MCP Server 正在后台运行" else "MCP Server 已停止"
        val content = if (running) {
            "MCP 协议 · 端口 $port · 在线 $clients"
        } else {
            "点击返回应用重新启动 MCP 服务"
        }
        val shortText = if (running) "MCP $port" else "MCP Offline"

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
            .setSmallIcon(R.drawable.ic_notification_logo)
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

    fun buildIslandNotification(
        context: Context,
        running: Boolean,
        port: Int,
        path: String,
        clients: Int,
        forTest: Boolean
    ): android.app.Notification {
        val title = if (running) "MCP Server 在线" else "MCP Server 通知测试"
        val content = if (running) {
            "MCP 协议 · 端口 $port · 在线 $clients"
        } else {
            "MCP 协议 · 端口 $port$path"
        }
        val shortText = if (running) "MCP ${port} Online" else "MCP Test"
        val openIntent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            1201,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(context, CHANNEL_ID_ISLAND)
            .setSmallIcon(R.drawable.ic_notification_logo)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$content\nEndpoint: http://127.0.0.1:$port$path"))
            .setContentIntent(openPendingIntent)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(running && !forTest)
            .setOnlyAlertOnce(!forTest)
            .setAutoCancel(forTest)

        McpIslandNotificationBuilder.buildExtras(
            context = context,
            title = title,
            content = content,
            shortText = shortText
        )?.let(builder::addExtras)

        return builder.build()
    }

    fun notifyIslandStatus(
        context: Context,
        running: Boolean,
        port: Int,
        path: String,
        clients: Int
    ) {
        ensureChannel(context)
        val notification = buildIslandNotification(
            context = context,
            running = running,
            port = port,
            path = path,
            clients = clients,
            forTest = false
        )
        notifyWithXiaomiMagic(
            context = context,
            notificationId = ISLAND_STATUS_NOTIFICATION_ID,
            notification = notification
        )
    }

    fun notifyTest(
        context: Context,
        running: Boolean,
        port: Int,
        path: String,
        clients: Int
    ) {
        ensureChannel(context)
        val notification = buildIslandNotification(
            context = context,
            running = running,
            port = port,
            path = path,
            clients = clients,
            forTest = true
        )
        notifyWithXiaomiMagic(
            context = context,
            notificationId = TEST_NOTIFICATION_ID,
            notification = notification
        )
    }

    private fun notifyWithXiaomiMagic(
        context: Context,
        notificationId: Int,
        notification: android.app.Notification
    ) {
        val manager = NotificationManagerCompat.from(context)
        if (!shouldUseXiaomiMagic(context)) {
            manager.notify(notificationId, notification)
            return
        }
        Thread {
            runCatching {
                runShizukuCommand("cmd connectivity set-chain3-enabled true")
                runShizukuCommand("cmd connectivity set-package-networking-enabled false com.xiaomi.xmsf")
                manager.notify(notificationId, notification)
                Thread.sleep(120)
            }
            runCatching {
                runShizukuCommand("cmd connectivity set-package-networking-enabled true com.xiaomi.xmsf")
            }
        }.start()
    }

    private fun shouldUseXiaomiMagic(context: Context): Boolean {
        val maker = Build.MANUFACTURER.lowercase()
        val brand = Build.BRAND.lowercase()
        val isXiaomi = maker.contains("xiaomi") || brand.contains("xiaomi") || brand.contains("redmi") || brand.contains("poco")
        if (!isXiaomi) return false
        return runCatching {
            if (!Shizuku.pingBinder()) return@runCatching false
            if (Shizuku.checkSelfPermission() != android.content.pm.PackageManager.PERMISSION_GRANTED) return@runCatching false
            val uidMethod = Shizuku::class.java.methods.firstOrNull {
                it.name == "getUid" && it.parameterTypes.isEmpty()
            } ?: return@runCatching false
            val uid = (uidMethod.invoke(null) as? Int) ?: return@runCatching false
            uid == 2000
        }.getOrDefault(false)
    }

    private fun runShizukuCommand(command: String): String? {
        return runCatching {
            val method = Shizuku::class.java.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java
            )
            method.isAccessible = true
            val process = method.invoke(null, arrayOf("sh", "-c", command), null, null) as Process
            val out = process.inputStream.bufferedReader().use { it.readText() }
            val err = process.errorStream.bufferedReader().use { it.readText() }
            process.waitFor(1200, TimeUnit.MILLISECONDS)
            out.ifBlank { err }.trim().ifBlank { null }
        }.getOrNull()
    }
}
