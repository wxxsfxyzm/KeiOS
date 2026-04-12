package com.example.keios.ui.page.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.example.keios.ui.page.main.widget.MiuixInfoItem
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun SettingsPage(
    liquidBottomBarEnabled: Boolean,
    onLiquidBottomBarChanged: (Boolean) -> Unit,
    cardPressFeedbackEnabled: Boolean,
    onCardPressFeedbackChanged: (Boolean) -> Unit,
    homeIconHdrEnabled: Boolean,
    onHomeIconHdrChanged: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    val titleColor = MiuixTheme.colorScheme.onBackground
    val subtitleColor = MiuixTheme.colorScheme.onBackgroundVariant
    val enabledCardColor = Color(0x2234C759)
    val disabledCardColor = Color(0x2264748B)

    val scrollBehavior = MiuixScrollBehavior()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = "Settings",
                scrollBehavior = scrollBehavior,
                color = MiuixTheme.colorScheme.surface,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = MiuixIcons.Regular.Back,
                            contentDescription = null,
                            tint = MiuixTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding() + 16.dp,
                start = 16.dp,
                end = 16.dp
            )
        ) {
            item { SmallTitle("界面与样式") }
            item { Spacer(modifier = Modifier.height(14.dp)) }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.defaultColors(
                        color = if (liquidBottomBarEnabled) enabledCardColor else disabledCardColor,
                        contentColor = MiuixTheme.colorScheme.onBackground
                    ),
                    onClick = {}
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Bottom Bar", color = titleColor)

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onLiquidBottomBarChanged(!liquidBottomBarEnabled) },
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "液态玻璃底栏",
                                    color = titleColor,
                                    modifier = Modifier.weight(1f)
                                )
                                Switch(
                                    checked = liquidBottomBarEnabled,
                                    onCheckedChange = { checked -> onLiquidBottomBarChanged(checked) }
                                )
                            }
                            Text(
                                text = if (liquidBottomBarEnabled) "已启用玻璃模糊与高光" else "使用纯色底栏",
                                color = subtitleColor
                            )
                        }

                        MiuixInfoItem(
                            key = "样式说明",
                            value = "胶囊底栏 + 选中态高亮 + 轻量玻璃质感"
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(10.dp)) }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.defaultColors(
                        color = if (cardPressFeedbackEnabled) enabledCardColor else disabledCardColor,
                        contentColor = MiuixTheme.colorScheme.onBackground
                    ),
                    onClick = {}
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Card Feedback", color = titleColor)

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCardPressFeedbackChanged(!cardPressFeedbackEnabled) },
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "卡片按压反馈",
                                    color = titleColor,
                                    modifier = Modifier.weight(1f)
                                )
                                Switch(
                                    checked = cardPressFeedbackEnabled,
                                    onCheckedChange = { checked -> onCardPressFeedbackChanged(checked) }
                                )
                            }
                            Text(
                                text = if (cardPressFeedbackEnabled) {
                                    "启用所有支持 PressFeedback 的卡片反馈"
                                } else {
                                    "全局禁用 Sink / Tilt 等卡片按压反馈"
                                },
                                color = subtitleColor
                            )
                        }

                        MiuixInfoItem(
                            key = "作用范围",
                            value = "System / MCP / GitHub / BA 等支持按压反馈的卡片"
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(10.dp)) }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.defaultColors(
                        color = if (homeIconHdrEnabled) enabledCardColor else disabledCardColor,
                        contentColor = MiuixTheme.colorScheme.onBackground
                    ),
                    onClick = {}
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Home Shine", color = titleColor)

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onHomeIconHdrChanged(!homeIconHdrEnabled) },
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "主页图标与标题 HDR 高光",
                                    color = titleColor,
                                    modifier = Modifier.weight(1f)
                                )
                                Switch(
                                    checked = homeIconHdrEnabled,
                                    onCheckedChange = { checked -> onHomeIconHdrChanged(checked) }
                                )
                            }
                            Text(
                                text = if (homeIconHdrEnabled) {
                                    "启用主页 Kei 图标与 KeiOS 标题联动高光（亮屏时更明显）"
                                } else {
                                    "关闭主页图标与标题高光，减少夜间眩光感"
                                },
                                color = subtitleColor
                            )
                        }

                        MiuixInfoItem(
                            key = "作用范围",
                            value = "影响主页 Kei 图标与 KeiOS 标题的联动高光效果"
                        )
                    }
                }
            }
        }
    }
}
