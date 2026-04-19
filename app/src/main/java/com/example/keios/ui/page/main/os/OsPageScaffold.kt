package com.example.keios.ui.page.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.keios.ui.page.main.widget.AppChromeTokens
import com.example.keios.ui.page.main.widget.AppPageScaffold
import com.example.keios.ui.page.main.widget.AppTopBarSearchField
import com.example.keios.ui.page.main.widget.LiquidActionBar
import com.example.keios.ui.page.main.widget.LiquidActionItem
import com.kyant.backdrop.backdrops.LayerBackdrop
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.GridView
import top.yukonga.miuix.kmp.icon.extended.Layers
import top.yukonga.miuix.kmp.icon.extended.Refresh

@Composable
internal fun OsPageScaffoldShell(
    scrollBehavior: ScrollBehavior,
    topBarColor: Color,
    topBarBackdrop: LayerBackdrop,
    layeredStyleEnabled: Boolean,
    manageCardsContentDescription: String,
    manageActivitiesContentDescription: String,
    manageShellCardsContentDescription: String,
    refreshParamsContentDescription: String,
    refreshing: Boolean,
    onOpenCardManager: () -> Unit,
    onOpenActivityVisibilityManager: () -> Unit,
    onOpenShellCardVisibilityManager: () -> Unit,
    onRefresh: () -> Unit,
    onActionBarInteractingChanged: (Boolean) -> Unit,
    searchBarVisible: Boolean,
    queryInput: String,
    onQueryInputChange: (String) -> Unit,
    searchLabel: String,
    content: @Composable (PaddingValues) -> Unit
) {
    AppPageScaffold(
        title = "",
        modifier = Modifier.fillMaxSize(),
        largeTitle = "OS",
        scrollBehavior = scrollBehavior,
        topBarColor = topBarColor,
        actions = {
            LiquidActionBar(
                backdrop = topBarBackdrop,
                layeredStyleEnabled = layeredStyleEnabled,
                items = listOf(
                    LiquidActionItem(
                        icon = MiuixIcons.Regular.Layers,
                        contentDescription = manageCardsContentDescription,
                        onClick = onOpenCardManager
                    ),
                    LiquidActionItem(
                        icon = MiuixIcons.Regular.GridView,
                        contentDescription = manageActivitiesContentDescription,
                        onClick = onOpenActivityVisibilityManager
                    ),
                    LiquidActionItem(
                        icon = osLucideShellIcon(),
                        contentDescription = manageShellCardsContentDescription,
                        onClick = onOpenShellCardVisibilityManager
                    ),
                    LiquidActionItem(
                        icon = MiuixIcons.Regular.Refresh,
                        contentDescription = refreshParamsContentDescription,
                        onClick = {
                            if (refreshing) return@LiquidActionItem
                            onRefresh()
                        }
                    )
                ),
                onInteractionChanged = onActionBarInteractingChanged
            )
        },
        searchBarVisible = searchBarVisible,
        searchBarAnimationLabelPrefix = "osSearchBar",
        searchBarContent = {
            Column {
                AppTopBarSearchField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppChromeTokens.searchFieldHorizontalPadding),
                    value = queryInput,
                    onValueChange = onQueryInputChange,
                    label = searchLabel,
                    backdrop = topBarBackdrop
                )
            }
        },
        content = content
    )
}
