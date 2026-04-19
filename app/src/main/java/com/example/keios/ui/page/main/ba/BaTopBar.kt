package com.example.keios.ui.page.main.ba

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.example.keios.R
import com.example.keios.ui.page.main.os.appLucideConfigIcon
import com.example.keios.ui.page.main.os.appLucideEditIcon
import com.example.keios.ui.page.main.os.appLucideRefreshIcon
import com.example.keios.ui.page.main.ba.support.BaCalendarRefreshIntervalOption
import com.example.keios.ui.page.main.os.osLucideCopyIcon
import com.example.keios.ui.page.main.widget.chrome.AppTopBarSection
import com.example.keios.ui.page.main.widget.chrome.LiquidActionBar
import com.example.keios.ui.page.main.widget.chrome.LiquidActionBarPopupAnchors
import com.example.keios.ui.page.main.widget.chrome.LiquidActionItem
import com.example.keios.ui.page.main.widget.sheet.SnapshotPopupPlacement
import com.example.keios.ui.page.main.widget.sheet.SnapshotWindowListPopup
import com.example.keios.ui.page.main.widget.glass.LiquidDropdownColumn
import com.example.keios.ui.page.main.widget.glass.LiquidDropdownImpl
import com.kyant.backdrop.backdrops.LayerBackdrop
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.basic.ScrollBehavior

@Composable
internal fun BaTopBar(
    backdrop: LayerBackdrop,
    liquidActionBarLayeredStyleEnabled: Boolean,
    topBarColor: Color,
    scrollBehavior: ScrollBehavior?,
    showCalendarIntervalPopup: Boolean,
    calendarRefreshIntervalHours: Int,
    onShowSettings: () -> Unit,
    onShowCalendarIntervalPopupChange: (Boolean) -> Unit,
    onCopyFriendCode: () -> Unit,
    onRefreshAll: () -> Unit,
    onCalendarRefreshIntervalSelected: (Int) -> Unit,
    onInteractionChanged: (Boolean) -> Unit,
) {
    AppTopBarSection(
        title = "",
        largeTitle = stringResource(R.string.ba_topbar_title),
        scrollBehavior = scrollBehavior,
        color = topBarColor,
        actions = {
            Box {
                LiquidActionBar(
                    backdrop = backdrop,
                    layeredStyleEnabled = liquidActionBarLayeredStyleEnabled,
                    items = listOf(
                        LiquidActionItem(
                            icon = appLucideEditIcon(),
                            contentDescription = stringResource(R.string.ba_cd_edit),
                            onClick = onShowSettings,
                        ),
                        LiquidActionItem(
                            icon = appLucideConfigIcon(),
                            contentDescription = stringResource(R.string.ba_cd_refresh_interval),
                            onClick = { onShowCalendarIntervalPopupChange(!showCalendarIntervalPopup) },
                        ),
                        LiquidActionItem(
                            icon = osLucideCopyIcon(),
                            contentDescription = stringResource(R.string.ba_cd_copy_friend_code),
                            onClick = onCopyFriendCode,
                        ),
                        LiquidActionItem(
                            icon = appLucideRefreshIcon(),
                            contentDescription = stringResource(R.string.ba_cd_refresh),
                            onClick = onRefreshAll,
                        ),
                    ),
                    onInteractionChanged = onInteractionChanged,
                )

                LiquidActionBarPopupAnchors(itemCount = 4) { slotIndex, popupAnchorBounds ->
                    if (slotIndex == 1 && showCalendarIntervalPopup) {
                        SnapshotWindowListPopup(
                            show = showCalendarIntervalPopup,
                            alignment = PopupPositionProvider.Align.BottomStart,
                            anchorBounds = popupAnchorBounds,
                            placement = SnapshotPopupPlacement.ActionBarCenter,
                            onDismissRequest = { onShowCalendarIntervalPopupChange(false) },
                            enableWindowDim = false,
                        ) {
                            LiquidDropdownColumn {
                                val options = BaCalendarRefreshIntervalOption.entries
                                val selected = BaCalendarRefreshIntervalOption.fromHours(
                                    calendarRefreshIntervalHours,
                                )
                                options.forEachIndexed { index, option ->
                                    LiquidDropdownImpl(
                                        text = option.label,
                                        optionSize = options.size,
                                        isSelected = selected == option,
                                        index = index,
                                        onSelectedIndexChange = { selectedIndex ->
                                            onCalendarRefreshIntervalSelected(options[selectedIndex].hours)
                                            onShowCalendarIntervalPopupChange(false)
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
    )
}
