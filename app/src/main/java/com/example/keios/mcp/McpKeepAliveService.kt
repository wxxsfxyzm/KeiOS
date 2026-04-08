package com.example.keios.mcp

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.content.ContextCompat

class McpKeepAliveService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        McpNotificationHelper.ensureChannel(this)
        when (intent?.action) {
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }

            ACTION_START,
            ACTION_UPDATE,
            null -> {
                val notification = buildNotification(
                    running = intent?.getBooleanExtra(EXTRA_RUNNING, false) == true,
                    port = intent?.getIntExtra(EXTRA_PORT, 38888) ?: 38888,
                    path = intent?.getStringExtra(EXTRA_PATH).orEmpty().ifBlank { "/mcp" },
                    clients = intent?.getIntExtra(EXTRA_CLIENTS, 0) ?: 0
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    startForeground(
                        McpNotificationHelper.KEEPALIVE_NOTIFICATION_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                    )
                } else {
                    startForeground(McpNotificationHelper.KEEPALIVE_NOTIFICATION_ID, notification)
                }
                return START_STICKY
            }
        }
        return START_STICKY
    }

    private fun buildNotification(
        running: Boolean,
        port: Int,
        path: String,
        clients: Int
    ): Notification {
        return McpNotificationHelper.buildNotification(
            context = this,
            running = running,
            port = port,
            path = path,
            clients = clients,
            ongoing = true
        )
    }

    companion object {
        private const val ACTION_START = "com.example.keios.mcp.keepalive.START"
        private const val ACTION_UPDATE = "com.example.keios.mcp.keepalive.UPDATE"
        private const val ACTION_STOP = "com.example.keios.mcp.keepalive.STOP"
        private const val EXTRA_RUNNING = "running"
        private const val EXTRA_PORT = "port"
        private const val EXTRA_PATH = "path"
        private const val EXTRA_CLIENTS = "clients"

        fun startOrUpdate(
            context: Context,
            running: Boolean,
            port: Int,
            path: String,
            clients: Int,
            forceStart: Boolean
        ) {
            val intent = Intent(context, McpKeepAliveService::class.java).apply {
                action = if (forceStart) ACTION_START else ACTION_UPDATE
                putExtra(EXTRA_RUNNING, running)
                putExtra(EXTRA_PORT, port)
                putExtra(EXTRA_PATH, path)
                putExtra(EXTRA_CLIENTS, clients)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, McpKeepAliveService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
