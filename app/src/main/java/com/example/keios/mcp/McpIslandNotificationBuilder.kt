package com.example.keios.mcp

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Icon
import com.example.keios.R
import com.xzakota.hyper.notification.focus.FocusNotification

object McpIslandNotificationBuilder {

    fun buildExtras(
        context: Context,
        title: String,
        content: String,
        shortText: String
    ) = runCatching {
        val lightIcon = Icon.createWithResource(context, R.drawable.ic_notification_logo).setTint(Color.BLACK)
        val darkIcon = Icon.createWithResource(context, R.drawable.ic_notification_logo).setTint(Color.WHITE)

        FocusNotification.buildV3 {
            val lightIconKey = createPicture("mcp_logo_light", lightIcon)
            val darkIconKey = createPicture("mcp_logo_dark", darkIcon)

            islandFirstFloat = true
            enableFloat = true
            updatable = true
            ticker = shortText
            tickerPic = lightIconKey
            outEffectSrc = "outer_glow"

            island {
                islandProperty = 1
                bigIslandArea {
                    imageTextInfoLeft {
                        type = 1
                        picInfo {
                            type = 1
                            pic = lightIconKey
                        }
                    }
                    imageTextInfoRight {
                        type = 3
                        textInfo {
                            this.title = shortText
                        }
                    }
                }
                smallIslandArea {
                    picInfo {
                        type = 1
                        pic = lightIconKey
                    }
                }
            }

            baseInfo {
                type = 2
                this.title = title
                this.content = if (content.isBlank()) " " else content
            }

            iconTextInfo {
                this.title = title
                this.content = if (content.isBlank()) " " else content
                animIconInfo {
                    type = 0
                    src = lightIconKey
                }
            }

            picInfo {
                type = 1
                pic = lightIconKey
                picDark = darkIconKey
            }

        }
    }.getOrNull()
}
