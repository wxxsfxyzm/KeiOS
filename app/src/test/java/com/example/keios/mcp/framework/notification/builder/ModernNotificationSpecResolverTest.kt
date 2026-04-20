package com.example.keios.mcp.framework.notification.builder

import android.app.PendingIntent
import com.example.keios.mcp.notification.McpNotificationPayload
import kotlin.test.assertEquals
import org.junit.Test
import sun.misc.Unsafe

class ModernNotificationSpecResolverTest {
    @Test
    fun `default running session uses capped client progress`() {
        val spec = ModernNotificationSpecResolver.resolve(
            createState(
                serverName = "Local MCP",
                running = true,
                port = 8080,
                clients = 3,
                ongoing = true
            )
        )

        assertEquals(ModernNotificationKind.DEFAULT, spec.kind)
        assertEquals(72, spec.progressPercent)
        assertEquals(ModernShortCriticalMode.SHORT_TEXT, spec.shortCriticalMode)
        assertEquals(true, spec.ongoing)
        assertEquals(true, spec.requestPromotedOngoing)
    }

    @Test
    fun `blue archive ap uses ratio progress`() {
        val spec = ModernNotificationSpecResolver.resolve(
            createState(
                serverName = McpNotificationPayload.BA_AP_SERVER_NAME,
                running = true,
                port = 4,
                clients = 8,
                ongoing = false
            )
        )

        assertEquals(ModernNotificationKind.BA_AP, spec.kind)
        assertEquals(50, spec.progressPercent)
        assertEquals(ModernShortCriticalMode.SHORT_TEXT, spec.shortCriticalMode)
        assertEquals(true, spec.ongoing)
    }

    @Test
    fun `cafe visit keeps full progress and online text`() {
        val spec = ModernNotificationSpecResolver.resolve(
            createState(
                serverName = McpNotificationPayload.BA_CAFE_VISIT_SERVER_NAME,
                running = true,
                port = 0,
                clients = 0,
                ongoing = false
            )
        )

        assertEquals(ModernNotificationKind.BA_CAFE_VISIT, spec.kind)
        assertEquals(100, spec.progressPercent)
        assertEquals(ModernShortCriticalMode.ONLINE_TEXT, spec.shortCriticalMode)
        assertEquals(true, spec.requestPromotedOngoing)
    }

    @Test
    fun `stopped session clears live update emphasis`() {
        val spec = ModernNotificationSpecResolver.resolve(
            createState(
                serverName = "Local MCP",
                running = false,
                port = 8080,
                clients = 5,
                ongoing = false
            )
        )

        assertEquals(0, spec.progressPercent)
        assertEquals(ModernShortCriticalMode.NONE, spec.shortCriticalMode)
        assertEquals(false, spec.ongoing)
        assertEquals(false, spec.requestPromotedOngoing)
    }

    private fun createState(
        serverName: String,
        running: Boolean,
        port: Int,
        clients: Int,
        ongoing: Boolean
    ): McpNotificationPayload {
        val pendingIntent = createFakePendingIntent()
        return McpNotificationPayload(
            serverName = serverName,
            running = running,
            port = port,
            path = "demo",
            clients = clients,
            ongoing = ongoing,
            onlyAlertOnce = true,
            openPendingIntent = pendingIntent,
            stopPendingIntent = pendingIntent
        )
    }

    private fun createFakePendingIntent(): PendingIntent {
        val unsafeField = Unsafe::class.java.getDeclaredField("theUnsafe").apply {
            isAccessible = true
        }
        val unsafe = unsafeField.get(null) as Unsafe
        return unsafe.allocateInstance(PendingIntent::class.java) as PendingIntent
    }
}
