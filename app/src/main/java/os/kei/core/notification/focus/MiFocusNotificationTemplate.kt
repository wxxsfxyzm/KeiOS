package os.kei.core.notification.focus

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Icon
import android.os.Bundle
import com.xzakota.hyper.notification.focus.FocusNotification
import os.kei.R

internal data class MiFocusNotificationAction(
    val key: String,
    val title: String,
    val pendingIntent: PendingIntent,
    val iconResId: Int? = null,
    val isHighlighted: Boolean = false,
    val collapsePanel: Boolean? = null,
    val backgroundColor: String = "#006EFF",
    val titleColor: String = "#FFFFFF"
)

internal data class MiFocusNotificationSpec(
    val title: String,
    val content: String,
    val compactTitle: String,
    val compactContent: String? = null,
    val displayIconResId: Int,
    val actionIconResId: Int = displayIconResId,
    val tickerIconResId: Int = R.drawable.ic_notification_logo,
    val allowFloat: Boolean = true,
    val islandFirstFloat: Boolean = true,
    val outerGlow: Boolean = false,
    val aodTitle: String = compactTitle,
    val embedTextButtons: Boolean = false,
    val actions: List<MiFocusNotificationAction> = emptyList()
)

internal object MiFocusNotificationTemplate {
    fun build(context: Context, spec: MiFocusNotificationSpec): Bundle {
        val compactTitle = spec.compactTitle.ifBlank { spec.title }
        val compactContent = spec.compactContent?.trim()?.takeIf { it.isNotEmpty() }

        return FocusNotification.buildV3 {
            val tickerLightKey = createPicture(
                "mi_focus_ticker_light",
                Icon.createWithResource(context, spec.tickerIconResId).setTint(Color.BLACK)
            )
            val tickerDarkKey = createPicture(
                "mi_focus_ticker_dark",
                Icon.createWithResource(context, spec.tickerIconResId).setTint(Color.WHITE)
            )
            val displayLightKey = createPicture(
                "mi_focus_display_light",
                Icon.createWithResource(context, spec.displayIconResId)
            )

            islandFirstFloat = spec.islandFirstFloat
            enableFloat = spec.allowFloat
            updatable = true
            ticker = compactTitle
            tickerPic = tickerLightKey

            if (spec.outerGlow) {
                outEffectSrc = "outer_glow"
            }

            island {
                islandProperty = 1
                bigIslandArea {
                    imageTextInfoLeft {
                        type = 1
                        picInfo {
                            type = 1
                            pic = displayLightKey
                        }
                    }
                    imageTextInfoRight {
                        type = 3
                        textInfo {
                            title = compactTitle
                            content = compactContent
                            narrowFont = compactTitle.length >= 6
                        }
                    }
                }
                smallIslandArea {
                    picInfo {
                        type = 1
                        pic = displayLightKey
                    }
                }
            }

            baseInfo {
                type = 2
                title = spec.title
                content = spec.content.ifBlank { " " }
            }

            iconTextInfo {
                title = spec.title
                content = spec.content.ifBlank { " " }
                animIconInfo {
                    type = 0
                    src = displayLightKey
                }
            }

            picInfo {
                type = 1
                pic = tickerLightKey
                picDark = tickerDarkKey
            }

            if (spec.embedTextButtons && spec.actions.isNotEmpty()) {
                textButton {
                    spec.actions.take(2).forEach { actionItem ->
                        addActionInfo {
                            type = 2
                            val nativeAction = Notification.Action.Builder(
                                Icon.createWithResource(
                                    context,
                                    actionItem.iconResId ?: spec.actionIconResId
                                ),
                                actionItem.title,
                                actionItem.pendingIntent
                            ).build()
                            action = createAction(actionItem.key, nativeAction)
                            actionTitle = actionItem.title
                            clickWithCollapse = actionItem.collapsePanel
                            if (actionItem.isHighlighted) {
                                actionBgColor = actionItem.backgroundColor
                                actionBgColorDark = actionItem.backgroundColor
                                actionTitleColor = actionItem.titleColor
                                actionTitleColorDark = actionItem.titleColor
                            }
                        }
                    }
                }
            }
        }
    }
}
