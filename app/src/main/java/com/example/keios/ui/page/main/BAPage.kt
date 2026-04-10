package com.example.keios.ui.page.main

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.keios.ui.page.main.widget.GlassIconButton
import com.example.keios.ui.page.main.widget.LiquidActionBar
import com.example.keios.ui.page.main.widget.LiquidActionItem
import com.example.keios.ui.page.main.widget.MiuixInfoItem
import com.example.keios.ui.page.main.widget.StatusPill
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.AddCircle
import top.yukonga.miuix.kmp.icon.extended.Refresh
import top.yukonga.miuix.kmp.theme.MiuixTheme

private enum class BAInitState {
    Empty,
    Draft
}

@Composable
fun BAPage(
    contentBottomPadding: Dp = 72.dp,
    scrollToTopSignal: Int = 0,
    onActionBarInteractingChanged: (Boolean) -> Unit = {}
) {
    val listState = rememberLazyListState()
    val scrollBehavior = MiuixScrollBehavior()
    val surfaceColor = MiuixTheme.colorScheme.surface
    val backdrop: LayerBackdrop = rememberLayerBackdrop {
        drawRect(surfaceColor)
        drawContent()
    }
    var initState by remember { mutableStateOf(BAInitState.Empty) }

    DisposableEffect(Unit) {
        onDispose { onActionBarInteractingChanged(false) }
    }

    LaunchedEffect(scrollToTopSignal) {
        if (scrollToTopSignal > 0) listState.animateScrollToItem(0)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column {
                TopAppBar(
                    title = "BA",
                    scrollBehavior = scrollBehavior,
                    color = MiuixTheme.colorScheme.surface,
                    actions = {
                        Box {
                            LiquidActionBar(
                                backdrop = backdrop,
                                items = listOf(
                                    LiquidActionItem(
                                        icon = MiuixIcons.Regular.Refresh,
                                        contentDescription = "刷新",
                                        onClick = { }
                                    ),
                                    LiquidActionItem(
                                        icon = MiuixIcons.Regular.AddCircle,
                                        contentDescription = "新建",
                                        onClick = { initState = BAInitState.Draft }
                                    )
                                ),
                                onInteractionChanged = onActionBarInteractingChanged
                            )
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            state = listState,
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding() + contentBottomPadding + 16.dp,
                start = 12.dp,
                end = 12.dp
            )
        ) {
            item { SmallTitle("BA 工作区") }
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = {
                                if (initState == BAInitState.Empty) {
                                    initState = BAInitState.Draft
                                }
                            },
                            onLongClick = { initState = BAInitState.Empty }
                        ),
                    colors = CardDefaults.defaultColors(
                        color = when (initState) {
                            BAInitState.Empty -> Color(0x33F59E0B)
                            BAInitState.Draft -> Color(0x333B82F6)
                        },
                        contentColor = MiuixTheme.colorScheme.onBackground
                    ),
                    showIndication = true,
                    onClick = {
                        if (initState == BAInitState.Empty) {
                            initState = BAInitState.Draft
                        }
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Overview", color = MiuixTheme.colorScheme.onBackground)
                            StatusPill(
                                label = if (initState == BAInitState.Empty) "Init" else "Draft",
                                color = if (initState == BAInitState.Empty) Color(0xFFF59E0B) else Color(0xFF3B82F6)
                            )
                        }
                        Text(
                            text = if (initState == BAInitState.Empty) {
                                "BA 模块已创建，等待配置初始内容"
                            } else {
                                "BA 初始界面已就绪，可继续扩展业务内容"
                            },
                            color = MiuixTheme.colorScheme.onBackgroundVariant
                        )
                        MiuixInfoItem(
                            key = "状态",
                            value = if (initState == BAInitState.Empty) "未初始化" else "草稿中"
                        )
                        MiuixInfoItem(
                            key = "入口",
                            value = "底栏 BA 板块"
                        )
                        MiuixInfoItem(
                            key = "下一步",
                            value = "按你的需求开始实现 BA 页面内容"
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(10.dp)) }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.defaultColors(
                        color = MiuixTheme.colorScheme.surface.copy(alpha = 0.66f),
                        contentColor = MiuixTheme.colorScheme.onBackground
                    ),
                    showIndication = false
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Quick Actions", color = MiuixTheme.colorScheme.onBackground)
                            GlassIconButton(
                                backdrop = backdrop,
                                icon = MiuixIcons.Regular.AddCircle,
                                contentDescription = "初始化 BA",
                                onClick = { initState = BAInitState.Draft }
                            )
                        }
                        Text(
                            text = "先搭好壳子和交互入口，后续我们可以直接往这里填 BA 功能。",
                            color = MiuixTheme.colorScheme.onBackgroundVariant
                        )
                    }
                }
            }
        }
    }
}
