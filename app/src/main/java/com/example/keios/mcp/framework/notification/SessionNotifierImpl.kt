package com.example.keios.mcp.framework.notification

import com.example.keios.core.prefs.UiPrefs
import com.example.keios.mcp.notification.McpNotificationPayload
import com.example.keios.mcp.domain.notification.SessionNotifier
import com.example.keios.mcp.framework.notification.builder.EnvironmentContext
import com.example.keios.mcp.framework.notification.builder.LegacyNotificationBuilder
import com.example.keios.mcp.framework.notification.builder.MiIslandNotificationBuilder
import com.example.keios.mcp.framework.notification.builder.ModernNotificationBuilder
import com.example.keios.mcp.framework.notification.builder.NotificationPayload
import com.example.keios.mcp.framework.notification.builder.NotificationRenderStyle
import com.example.keios.mcp.framework.notification.builder.UserSettings

class SessionNotifierImpl(
    private val helper: NotificationHelper
) : SessionNotifier {

    private val modernBuilder by lazy { ModernNotificationBuilder(helper.context) }
    private val legacyBuilder by lazy { LegacyNotificationBuilder(helper.context) }
    private val miIslandBuilder by lazy { MiIslandNotificationBuilder(helper.context) }

    override fun build(payload: McpNotificationPayload): SessionNotifier.NotificationBuildResult {
        val preferSuperIsland = UiPrefs.isSuperIslandNotificationEnabled(defaultValue = false)
        val bypassRestriction = UiPrefs.isSuperIslandBypassRestrictionEnabled(defaultValue = false)
        val style = resolveStyle(preferSuperIsland = preferSuperIsland)
        val wrapped = NotificationPayload(
            state = payload,
            settings = UserSettings(miIslandOuterGlow = payload.outerGlow),
            environment = EnvironmentContext(
                channelId = helper.resolveChannel(style),
                isHyperOS = helper.isHyperOS
            )
        )
        val notification = when (style) {
            NotificationRenderStyle.MI_ISLAND -> miIslandBuilder.build(wrapped)
            NotificationRenderStyle.LIVE_UPDATE -> {
                if (helper.isModernLiveUpdateEligible) modernBuilder.build(wrapped) else legacyBuilder.build(wrapped)
            }
            NotificationRenderStyle.LEGACY -> legacyBuilder.build(wrapped)
        }
        return SessionNotifier.NotificationBuildResult(
            notification = notification,
            style = style,
            useXiaomiMagic = style == NotificationRenderStyle.MI_ISLAND && bypassRestriction
        )
    }

    private fun resolveStyle(preferSuperIsland: Boolean): NotificationRenderStyle {
        if (preferSuperIsland && helper.isSupportMiIsland) {
            return NotificationRenderStyle.MI_ISLAND
        }
        return NotificationRenderStyle.LIVE_UPDATE
    }
}
