package com.example.keios.ui.page.main.ba

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.keios.ui.page.main.BA_DEFAULT_FRIEND_CODE
import com.example.keios.ui.page.main.BA_DEFAULT_NICKNAME
import com.example.keios.ui.page.main.BAInitState
import com.example.keios.ui.page.main.BaCalendarEntry
import com.example.keios.ui.page.main.BaPoolEntry
import com.example.keios.ui.page.main.activityProgress
import com.example.keios.ui.page.main.cafeDailyCapacity
import com.example.keios.ui.page.main.calculateApFullAtMs
import com.example.keios.ui.page.main.calculateApNextPointAtMs
import com.example.keios.ui.page.main.calculateInviteTicketAvailableMs
import com.example.keios.ui.page.main.calculateNextHeadpatAvailableMs
import com.example.keios.ui.page.main.displayAp
import com.example.keios.ui.page.main.formatBaDateTime
import com.example.keios.ui.page.main.formatBaDateTimeNoSeconds
import com.example.keios.ui.page.main.formatBaDateTimeNoYearInTimeZone
import com.example.keios.ui.page.main.formatBaRemainingTime
import com.example.keios.ui.page.main.nextArenaRefreshMs
import com.example.keios.ui.page.main.nextCafeStudentRefreshMs
import com.example.keios.ui.page.main.poolProgress
import com.example.keios.ui.page.main.serverRefreshTimeZone
import com.example.keios.ui.page.main.widget.GlassIconButton
import com.example.keios.ui.page.main.widget.GlassSearchField
import com.example.keios.ui.page.main.widget.GlassTextButton
import com.example.keios.ui.page.main.widget.GlassVariant
import com.example.keios.ui.page.main.widget.LiquidDropdownColumn
import com.example.keios.ui.page.main.widget.LiquidDropdownImpl
import com.example.keios.ui.page.main.widget.SnapshotPopupPlacement
import com.example.keios.ui.page.main.widget.SnapshotWindowListPopup
import com.example.keios.ui.page.main.widget.capturePopupAnchor
import com.kyant.backdrop.Backdrop
import top.yukonga.miuix.kmp.basic.LinearProgressIndicator
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.basic.ProgressIndicatorDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Refresh
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
private fun BaCardHeader(
    title: String,
    subtitle: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            BaGlassBadge(
                text = title,
                color = accentColor,
            )
            Text(
                text = subtitle,
                color = MiuixTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        trailing?.let { actions ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = actions,
            )
        }
    }
}

@Composable
private fun BaActionStatusPanel(
    backdrop: Backdrop?,
    title: String,
    accentColor: Color,
    countdownText: String,
    supportingText: String,
    buttonText: String,
    buttonEnabled: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
) {
    BaGlassPanel(
        backdrop = backdrop,
        modifier = Modifier.fillMaxWidth(),
        accentColor = accentColor,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = title,
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = supportingText,
                    color = MiuixTheme.colorScheme.onBackgroundVariant.copy(alpha = 0.92f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = countdownText,
                    color = Color(0xFF60A5FA),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                GlassTextButton(
                    backdrop = backdrop,
                    text = buttonText,
                    textColor = accentColor,
                    enabled = buttonEnabled,
                    variant = GlassVariant.Content,
                    onClick = onClick,
                    onLongClick = onLongClick,
                )
            }
        }
    }
}

@Composable
internal fun BaOverviewCard(
    backdrop: Backdrop?,
    idFriendCode: String,
    uiNowMs: Long,
    apSyncMs: Long,
    apLimit: Int,
    apCurrent: Double,
    apRegenBaseMs: Long,
    apCurrentInput: String,
    onApCurrentInputChange: (String) -> Unit,
    onApCurrentDone: () -> Unit,
    apLimitInput: String,
    onApLimitInputChange: (String) -> Unit,
    onApLimitDone: () -> Unit,
    cafeStoredAp: Double,
    cafeLevel: Int,
    serverOptions: List<String>,
    serverIndex: Int,
    showOverviewServerPopup: Boolean,
    overviewServerPopupAnchorBounds: IntRect?,
    onOverviewServerPopupAnchorBoundsChange: (IntRect?) -> Unit,
    onOverviewServerPopupChange: (Boolean) -> Unit,
    onServerSelected: (Int) -> Unit,
    onClaimCafeStoredAp: () -> Unit,
    initState: BAInitState,
    onInitStateChange: (BAInitState) -> Unit,
) {
    val isWorkActivated = idFriendCode != BA_DEFAULT_FRIEND_CODE
    val apNextPointAt = calculateApNextPointAtMs(
        apLimit = apLimit,
        apCurrent = apCurrent,
        apRegenBaseMs = apRegenBaseMs,
        nowMs = uiNowMs,
    )
    val apFullAt = calculateApFullAtMs(
        apLimit = apLimit,
        apCurrent = apCurrent,
        apRegenBaseMs = apRegenBaseMs,
        nowMs = uiNowMs,
    )
    val apNextPointRemain = formatBaRemainingTime(apNextPointAt, uiNowMs)
    val apSyncTimeText = if (apSyncMs > 0L) formatBaDateTime(apSyncMs) else "未同步"
    val apFullText = formatBaRemainingTime(apFullAt, uiNowMs)
    val apFullTimeText = formatBaDateTime(apFullAt)
    val accentBlue = Color(0xFF3B82F6)
    val accentGreen = Color(0xFF22C55E)
    val accentAmber = Color(0xFFF59E0B)
    val countdownBlue = Color(0xFF60A5FA)
    val stateAccent = if (isWorkActivated) accentBlue else accentAmber
    val activationSummary = if (isWorkActivated) {
        "好友码已接入，办公室数据会持续参与 AP 与咖啡厅调度。"
    } else {
        "仍在使用默认好友码，点按卡片可快速恢复草稿状态。"
    }

    BaGlassCard(
        backdrop = backdrop,
        accentColor = stateAccent,
        accentAlpha = if (isWorkActivated) 0.13f else 0.11f,
        onClick = {
            if (initState == BAInitState.Empty) {
                onInitStateChange(BAInitState.Draft)
            }
        },
        onLongClick = { onInitStateChange(BAInitState.Empty) },
    ) {
        BaCardHeader(
            title = "办公室总览",
            subtitle = if (isWorkActivated) "AP 调度与咖啡厅储备已联动" else "等待初始化接管办公室状态",
            accentColor = stateAccent,
            trailing = {
                Text(
                    text = if (initState == BAInitState.Empty) "点按启用" else "长按清空",
                    color = MiuixTheme.colorScheme.onBackgroundVariant.copy(alpha = 0.88f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
        )

        BaGlassPanel(
            backdrop = backdrop,
            accentColor = stateAccent,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "服务器",
                        color = MiuixTheme.colorScheme.onBackgroundVariant.copy(alpha = 0.92f),
                    )
                    Text(
                        text = activationSummary,
                        color = MiuixTheme.colorScheme.onBackground,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Box(
                    modifier = Modifier.capturePopupAnchor { onOverviewServerPopupAnchorBoundsChange(it) },
                ) {
                    GlassTextButton(
                        backdrop = backdrop,
                        text = serverOptions[serverIndex],
                        variant = GlassVariant.Content,
                        onClick = { onOverviewServerPopupChange(!showOverviewServerPopup) },
                    )
                    if (showOverviewServerPopup) {
                        SnapshotWindowListPopup(
                            show = showOverviewServerPopup,
                            alignment = PopupPositionProvider.Align.BottomEnd,
                            anchorBounds = overviewServerPopupAnchorBounds,
                            placement = SnapshotPopupPlacement.ButtonEnd,
                            onDismissRequest = { onOverviewServerPopupChange(false) },
                            enableWindowDim = false,
                        ) {
                            LiquidDropdownColumn {
                                serverOptions.forEachIndexed { index, server ->
                                    LiquidDropdownImpl(
                                        text = server,
                                        optionSize = serverOptions.size,
                                        isSelected = serverIndex == index,
                                        index = index,
                                        onSelectedIndexChange = { selected -> onServerSelected(selected) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        BaGlassPanel(
            backdrop = backdrop,
            accentColor = accentGreen,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "AP 调度",
                        color = accentGreen,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "当前值与上限会直接参与回满预测。",
                        color = MiuixTheme.colorScheme.onBackgroundVariant.copy(alpha = 0.92f),
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    GlassSearchField(
                        modifier = Modifier.width(72.dp),
                        value = apCurrentInput,
                        onValueChange = onApCurrentInputChange,
                        onImeActionDone = onApCurrentDone,
                        label = "0",
                        backdrop = backdrop,
                        variant = GlassVariant.SheetInput,
                        singleLine = true,
                        textAlign = TextAlign.Center,
                        fontSize = 18.sp,
                        textColor = accentGreen,
                    )
                    Text("/", color = MiuixTheme.colorScheme.onBackgroundVariant)
                    GlassSearchField(
                        modifier = Modifier.width(72.dp),
                        value = apLimitInput,
                        onValueChange = onApLimitInputChange,
                        onImeActionDone = onApLimitDone,
                        label = "240",
                        backdrop = backdrop,
                        variant = GlassVariant.SheetInput,
                        singleLine = true,
                        textAlign = TextAlign.Center,
                        fontSize = 18.sp,
                        textColor = accentGreen,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BaGlassBadge(
                    text = "下一个 $apNextPointRemain",
                    color = countdownBlue,
                )
                BaGlassBadge(
                    text = "回满 $apFullText",
                    color = accentBlue,
                )
            }
        }

        BaGlassPanel(
            backdrop = backdrop,
            accentColor = accentGreen,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "咖啡厅储备",
                        color = accentGreen,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "按当前咖啡厅等级自动累计，可随时手动领取。",
                        color = MiuixTheme.colorScheme.onBackgroundVariant.copy(alpha = 0.92f),
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    GlassTextButton(
                        backdrop = backdrop,
                        text = "领取",
                        textColor = accentGreen,
                        variant = GlassVariant.Content,
                        onClick = onClaimCafeStoredAp,
                    )
                    GlassTextButton(
                        backdrop = backdrop,
                        text = "${displayAp(cafeStoredAp)}/${cafeDailyCapacity(cafeLevel)}",
                        textColor = accentGreen,
                        enabled = false,
                        variant = GlassVariant.Content,
                        onClick = {},
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top,
        ) {
            BaGlassMetricPanel(
                backdrop = backdrop,
                label = "AP Sync",
                value = apSyncTimeText,
                secondary = "下一个 $apNextPointRemain",
                accentColor = accentBlue,
                valueColor = accentBlue,
                modifier = Modifier.weight(1f),
            )
            BaGlassMetricPanel(
                backdrop = backdrop,
                label = "AP Full",
                value = apFullTimeText,
                secondary = "剩余 $apFullText",
                accentColor = countdownBlue,
                valueColor = countdownBlue,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
internal fun BaCafeCard(
    backdrop: Backdrop?,
    uiNowMs: Long,
    serverIndex: Int,
    coffeeHeadpatMs: Long,
    coffeeInvite1UsedMs: Long,
    coffeeInvite2UsedMs: Long,
    onTouchHead: () -> Unit,
    onForceResetHeadpatCooldown: () -> Unit,
    onUseInviteTicket1: () -> Unit,
    onForceResetInviteTicket1Cooldown: () -> Unit,
    onUseInviteTicket2: () -> Unit,
    onForceResetInviteTicket2Cooldown: () -> Unit,
) {
    val accentPink = Color(0xFFF472B6)
    val accentYellow = Color(0xFFF59E0B)
    val countdownBlue = Color(0xFF60A5FA)
    val nextHeadpatAt = calculateNextHeadpatAvailableMs(coffeeHeadpatMs, serverIndex)
    val nextStudentRefreshAt = nextCafeStudentRefreshMs(uiNowMs, serverIndex)
    val nextArenaRefreshAt = nextArenaRefreshMs(uiNowMs, serverIndex)
    val nextHeadpatText = if (coffeeHeadpatMs <= 0L || nextHeadpatAt <= uiNowMs) "0s" else formatBaRemainingTime(nextHeadpatAt, uiNowMs)
    val nextStudentRefreshText = formatBaRemainingTime(nextStudentRefreshAt, uiNowMs)
    val nextArenaRefreshText = formatBaRemainingTime(nextArenaRefreshAt, uiNowMs)
    val invite1AvailableAt = calculateInviteTicketAvailableMs(coffeeInvite1UsedMs)
    val invite2AvailableAt = calculateInviteTicketAvailableMs(coffeeInvite2UsedMs)
    val invite1Ready = coffeeInvite1UsedMs <= 0L || invite1AvailableAt <= uiNowMs
    val invite2Ready = coffeeInvite2UsedMs <= 0L || invite2AvailableAt <= uiNowMs
    val invite1Color = if (invite1Ready) accentPink else accentYellow
    val invite2Color = if (invite2Ready) accentPink else accentYellow
    val invite1Text = if (invite1Ready) "0s" else formatBaRemainingTime(invite1AvailableAt, uiNowMs)
    val invite2Text = if (invite2Ready) "0s" else formatBaRemainingTime(invite2AvailableAt, uiNowMs)
    val invite1TimeText = formatBaDateTimeNoSeconds(if (invite1Ready) uiNowMs else invite1AvailableAt)
    val invite2TimeText = formatBaDateTimeNoSeconds(if (invite2Ready) uiNowMs else invite2AvailableAt)

    BaGlassCard(
        backdrop = backdrop,
        accentColor = accentPink,
        accentAlpha = 0.11f,
    ) {
        BaCardHeader(
            title = "咖啡厅循环",
            subtitle = "竞技场刷新、学生访问与互动冷却一屏汇总",
            accentColor = accentPink,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top,
        ) {
            BaGlassMetricPanel(
                backdrop = backdrop,
                label = "竞技场",
                value = nextArenaRefreshText,
                secondary = "下次刷新",
                accentColor = accentPink,
                valueColor = countdownBlue,
                modifier = Modifier.weight(1f),
            )
            BaGlassMetricPanel(
                backdrop = backdrop,
                label = "学生访问",
                value = nextStudentRefreshText,
                secondary = "下次刷新",
                accentColor = accentPink,
                valueColor = countdownBlue,
                modifier = Modifier.weight(1f),
            )
        }

        BaActionStatusPanel(
            backdrop = backdrop,
            title = "摸摸头",
            accentColor = accentPink,
            countdownText = nextHeadpatText,
            supportingText = if (coffeeHeadpatMs > 0L) {
                "上次记录 ${formatBaDateTimeNoSeconds(coffeeHeadpatMs)}"
            } else {
                "尚未记录，当前可立即执行。"
            },
            buttonText = "摸摸头",
            buttonEnabled = coffeeHeadpatMs <= 0L || nextHeadpatAt <= uiNowMs,
            onClick = onTouchHead,
            onLongClick = onForceResetHeadpatCooldown,
        )

        BaActionStatusPanel(
            backdrop = backdrop,
            title = "邀请券 1",
            accentColor = invite1Color,
            countdownText = invite1Text,
            supportingText = "下次可用 $invite1TimeText",
            buttonText = "邀请券1",
            buttonEnabled = invite1Ready,
            onClick = onUseInviteTicket1,
            onLongClick = onForceResetInviteTicket1Cooldown,
        )

        BaActionStatusPanel(
            backdrop = backdrop,
            title = "邀请券 2",
            accentColor = invite2Color,
            countdownText = invite2Text,
            supportingText = "下次可用 $invite2TimeText",
            buttonText = "邀请券2",
            buttonEnabled = invite2Ready,
            onClick = onUseInviteTicket2,
            onLongClick = onForceResetInviteTicket2Cooldown,
        )
    }
}

@Composable
internal fun BaCalendarCard(
    backdrop: Backdrop?,
    serverOptions: List<String>,
    serverIndex: Int,
    uiNowMs: Long,
    baCalendarEntries: List<BaCalendarEntry>,
    baCalendarLoading: Boolean,
    baCalendarError: String?,
    baCalendarLastSyncMs: Long,
    showEndedActivities: Boolean,
    showCalendarPoolImages: Boolean,
    onRefreshCalendar: () -> Unit,
    onOpenCalendarLink: (String) -> Unit,
) {
    val accentBlue = Color(0xFF3B82F6)
    val accentGreen = Color(0xFF22C55E)
    val accentAmber = Color(0xFFF59E0B)
    val countdownBlue = Color(0xFF60A5FA)
    val serverTimeZone = serverRefreshTimeZone(serverIndex)
    val visibleCalendarEntries = if (showEndedActivities) {
        baCalendarEntries
    } else {
        baCalendarEntries.filter { it.endAtMs > uiNowMs }
    }

    BaGlassCard(
        backdrop = backdrop,
        accentColor = accentBlue,
        accentAlpha = 0.11f,
    ) {
        BaCardHeader(
            title = "活动日历",
            subtitle = "GameKee · ${serverOptions[serverIndex]}",
            accentColor = accentBlue,
            trailing = {
                BaGlassBadge(
                    text = if (baCalendarLoading) "同步中..." else formatBaDateTimeNoYearInTimeZone(
                        baCalendarLastSyncMs,
                        serverTimeZone,
                    ),
                    color = countdownBlue,
                )
                GlassIconButton(
                    backdrop = backdrop,
                    icon = MiuixIcons.Regular.Refresh,
                    contentDescription = "刷新活动日历",
                    variant = GlassVariant.Content,
                    onClick = onRefreshCalendar,
                )
            },
        )

        when {
            !baCalendarError.isNullOrBlank() -> {
                BaGlassPanel(
                    backdrop = backdrop,
                    accentColor = accentAmber,
                ) {
                    Text(
                        text = baCalendarError.orEmpty(),
                        color = accentAmber,
                    )
                }
            }

            !baCalendarLoading && visibleCalendarEntries.isEmpty() -> {
                BaGlassPanel(
                    backdrop = backdrop,
                    accentColor = accentBlue,
                ) {
                    Text(
                        text = if (showEndedActivities) "暂无活动" else "暂无进行中或即将开始的活动",
                        color = MiuixTheme.colorScheme.onBackgroundVariant,
                    )
                }
            }

            else -> {
                visibleCalendarEntries.forEach { activity ->
                    val isEnded = activity.endAtMs <= uiNowMs
                    val remainTarget = if (activity.isRunning || isEnded) activity.endAtMs else activity.beginAtMs
                    val remainText = if (isEnded) "已结束" else formatBaRemainingTime(remainTarget, uiNowMs)
                    val statusText = when {
                        activity.isRunning -> "进行中"
                        isEnded -> "已结束"
                        else -> "即将开始"
                    }
                    val statusColor = when {
                        activity.isRunning -> accentGreen
                        isEnded -> MiuixTheme.colorScheme.onBackgroundVariant
                        else -> accentBlue
                    }

                    BaGlassPanel(
                        backdrop = backdrop,
                        modifier = Modifier.fillMaxWidth(),
                        accentColor = statusColor,
                        onClick = { onOpenCalendarLink(activity.linkUrl) },
                        onLongClick = { onOpenCalendarLink(activity.linkUrl) },
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            BaGlassBadge(
                                text = statusText,
                                color = statusColor,
                            )
                            Text(
                                text = remainText,
                                color = countdownBlue,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Text(
                            text = "${activity.kindName} · ${activity.title}",
                            color = MiuixTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (showCalendarPoolImages) {
                            com.example.keios.ui.page.main.GameKeeCoverImage(
                                imageUrl = activity.imageUrl,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        Text(
                            text = "${formatBaDateTimeNoYearInTimeZone(activity.beginAtMs, serverTimeZone)} - ${formatBaDateTimeNoYearInTimeZone(activity.endAtMs, serverTimeZone)}",
                            color = MiuixTheme.colorScheme.onBackgroundVariant.copy(alpha = 0.92f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        LinearProgressIndicator(
                            progress = activityProgress(activity, uiNowMs),
                            modifier = Modifier.fillMaxWidth(),
                            height = 6.dp,
                            colors = ProgressIndicatorDefaults.progressIndicatorColors(
                                foregroundColor = if (activity.isRunning) accentGreen else accentBlue,
                                backgroundColor = MiuixTheme.colorScheme.secondaryContainer.copy(alpha = 0.56f),
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun BaPoolCard(
    backdrop: Backdrop?,
    serverOptions: List<String>,
    serverIndex: Int,
    uiNowMs: Long,
    baPoolEntries: List<BaPoolEntry>,
    baPoolLoading: Boolean,
    baPoolError: String?,
    baPoolLastSyncMs: Long,
    showEndedPools: Boolean,
    showCalendarPoolImages: Boolean,
    onRefreshPool: () -> Unit,
    onOpenPoolStudentGuide: (String) -> Unit,
    onOpenCalendarLink: (String) -> Unit,
) {
    val accentBlue = Color(0xFF3B82F6)
    val accentGreen = Color(0xFF22C55E)
    val accentAmber = Color(0xFFF59E0B)
    val countdownBlue = Color(0xFF60A5FA)
    val serverTimeZone = serverRefreshTimeZone(serverIndex)
    val visiblePoolEntries = if (showEndedPools) {
        baPoolEntries
    } else {
        baPoolEntries.filter { it.endAtMs > uiNowMs }
    }

    BaGlassCard(
        backdrop = backdrop,
        accentColor = accentBlue,
        accentAlpha = 0.11f,
    ) {
        BaCardHeader(
            title = "卡池信息",
            subtitle = "GameKee · ${serverOptions[serverIndex]} · 点按进入图鉴",
            accentColor = accentBlue,
            trailing = {
                BaGlassBadge(
                    text = if (baPoolLoading) "同步中..." else formatBaDateTimeNoYearInTimeZone(
                        baPoolLastSyncMs,
                        serverTimeZone,
                    ),
                    color = countdownBlue,
                )
                GlassIconButton(
                    backdrop = backdrop,
                    icon = MiuixIcons.Regular.Refresh,
                    contentDescription = "刷新卡池",
                    variant = GlassVariant.Content,
                    onClick = onRefreshPool,
                )
            },
        )

        when {
            !baPoolError.isNullOrBlank() -> {
                BaGlassPanel(
                    backdrop = backdrop,
                    accentColor = accentAmber,
                ) {
                    Text(
                        text = baPoolError.orEmpty(),
                        color = accentAmber,
                    )
                }
            }

            !baPoolLoading && visiblePoolEntries.isEmpty() -> {
                BaGlassPanel(
                    backdrop = backdrop,
                    accentColor = accentBlue,
                ) {
                    Text(
                        text = "暂无进行中或即将开始的卡池",
                        color = MiuixTheme.colorScheme.onBackgroundVariant,
                    )
                }
            }

            else -> {
                visiblePoolEntries.forEach { pool ->
                    val isEnded = pool.endAtMs <= uiNowMs
                    val remainTarget = if (pool.isRunning || isEnded) pool.endAtMs else pool.startAtMs
                    val remainText = if (isEnded) "已结束" else formatBaRemainingTime(remainTarget, uiNowMs)
                    val statusText = when {
                        pool.isRunning -> "进行中"
                        isEnded -> "已结束"
                        else -> "即将开始"
                    }
                    val statusColor = when {
                        pool.isRunning -> accentGreen
                        isEnded -> MiuixTheme.colorScheme.onBackgroundVariant
                        else -> accentBlue
                    }

                    BaGlassPanel(
                        backdrop = backdrop,
                        modifier = Modifier.fillMaxWidth(),
                        accentColor = statusColor,
                        onClick = { onOpenPoolStudentGuide(pool.linkUrl) },
                        onLongClick = { onOpenCalendarLink(pool.linkUrl) },
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            BaGlassBadge(
                                text = statusText,
                                color = statusColor,
                            )
                            Text(
                                text = remainText,
                                color = countdownBlue,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Text(
                            text = "${pool.tagName} · ${pool.name}",
                            color = MiuixTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (showCalendarPoolImages) {
                            com.example.keios.ui.page.main.GameKeeCoverImage(
                                imageUrl = pool.imageUrl,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        Text(
                            text = "${formatBaDateTimeNoYearInTimeZone(pool.startAtMs, serverTimeZone)} - ${formatBaDateTimeNoYearInTimeZone(pool.endAtMs, serverTimeZone)}",
                            color = MiuixTheme.colorScheme.onBackgroundVariant.copy(alpha = 0.92f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        LinearProgressIndicator(
                            progress = poolProgress(pool, uiNowMs),
                            modifier = Modifier.fillMaxWidth(),
                            height = 6.dp,
                            colors = ProgressIndicatorDefaults.progressIndicatorColors(
                                foregroundColor = if (pool.isRunning) accentGreen else accentBlue,
                                backgroundColor = MiuixTheme.colorScheme.secondaryContainer.copy(alpha = 0.56f),
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun BaIdCard(
    backdrop: Backdrop?,
    idNicknameInput: String,
    onIdNicknameInputChange: (String) -> Unit,
    onSaveIdNickname: () -> Unit,
    idFriendCodeInput: String,
    onIdFriendCodeInputChange: (String) -> Unit,
    onSaveIdFriendCode: () -> Unit,
) {
    val nicknameLengthForWidth = idNicknameInput.ifEmpty { BA_DEFAULT_NICKNAME }.length.coerceIn(1, 10)
    val nicknameFieldWidth = (nicknameLengthForWidth * 11 + 34).coerceIn(72, 124).dp
    val friendCodeLengthForWidth = idFriendCodeInput.ifEmpty { BA_DEFAULT_FRIEND_CODE }.length.coerceIn(1, 8)
    val friendCodeFieldWidth = (friendCodeLengthForWidth * 11 + 34).coerceIn(92, 128).dp
    val accentBlue = Color(0xFF3B82F6)

    BaGlassCard(
        backdrop = backdrop,
        accentColor = accentBlue,
        accentAlpha = 0.10f,
    ) {
        BaCardHeader(
            title = "ID 卡",
            subtitle = "昵称与好友码会写入 BA 身份标识",
            accentColor = accentBlue,
        )

        BaGlassPanel(
            backdrop = backdrop,
            accentColor = accentBlue,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "昵称",
                        color = MiuixTheme.colorScheme.onBackgroundVariant.copy(alpha = 0.92f),
                    )
                    Text(
                        text = "将自动拼接为老师称呼展示。",
                        color = MiuixTheme.colorScheme.onBackground,
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    GlassSearchField(
                        modifier = Modifier.width(nicknameFieldWidth),
                        value = idNicknameInput,
                        onValueChange = onIdNicknameInputChange,
                        onImeActionDone = onSaveIdNickname,
                        label = BA_DEFAULT_NICKNAME,
                        backdrop = backdrop,
                        variant = GlassVariant.SheetInput,
                        singleLine = true,
                        textAlign = TextAlign.Center,
                    )
                    BaGlassBadge(
                        text = "老师",
                        color = accentBlue,
                    )
                }
            }
        }

        BaGlassPanel(
            backdrop = backdrop,
            accentColor = accentBlue,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "好友码",
                        color = MiuixTheme.colorScheme.onBackgroundVariant.copy(alpha = 0.92f),
                    )
                    Text(
                        text = "仅保留大写字母并限制为 8 位。",
                        color = MiuixTheme.colorScheme.onBackground,
                    )
                }
                GlassSearchField(
                    modifier = Modifier.width(friendCodeFieldWidth),
                    value = idFriendCodeInput,
                    onValueChange = onIdFriendCodeInputChange,
                    onImeActionDone = onSaveIdFriendCode,
                    label = BA_DEFAULT_FRIEND_CODE,
                    backdrop = backdrop,
                    variant = GlassVariant.SheetInput,
                    singleLine = true,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
internal fun BaDebugCard(
    backdrop: Backdrop?,
    onSendApTestNotification: () -> Unit,
    onTestCafePlus3Hours: () -> Unit,
) {
    val accentAmber = Color(0xFFF59E0B)

    BaGlassCard(
        backdrop = backdrop,
        accentColor = accentAmber,
        accentAlpha = 0.10f,
    ) {
        BaCardHeader(
            title = "Debug",
            subtitle = "保留调试入口，但外观统一到新的玻璃卡片体系",
            accentColor = accentAmber,
        )

        BaGlassPanel(
            backdrop = backdrop,
            accentColor = accentAmber,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GlassTextButton(
                    backdrop = backdrop,
                    text = "AP 通知",
                    textColor = accentAmber,
                    variant = GlassVariant.Content,
                    onClick = onSendApTestNotification,
                )
                GlassTextButton(
                    backdrop = backdrop,
                    text = "咖啡厅 3h AP",
                    textColor = accentAmber,
                    variant = GlassVariant.Content,
                    onClick = onTestCafePlus3Hours,
                )
            }
        }
    }
}
