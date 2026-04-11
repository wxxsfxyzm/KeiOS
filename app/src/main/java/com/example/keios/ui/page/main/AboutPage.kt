package com.example.keios.ui.page.main

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.keios.ui.page.main.widget.FrostedBlock
import com.example.keios.ui.page.main.widget.MiuixInfoItem
import com.example.keios.ui.page.main.widget.StatusPill
import com.example.keios.core.system.ShizukuApiUtils
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme

private fun formatTime(epochMillis: Long): String {
    if (epochMillis <= 0L) return "N/A"
    return runCatching {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        formatter.format(Date(epochMillis))
    }.getOrDefault("N/A")
}

@Composable
fun AboutPage(
    appLabel: String,
    packageInfo: PackageInfo?,
    notificationPermissionGranted: Boolean,
    shizukuStatus: String,
    shizukuApiUtils: ShizukuApiUtils,
    onCheckShizuku: () -> Unit,
    contentBottomPadding: Dp = 72.dp,
    scrollToTopSignal: Int = 0,
    onBack: (() -> Unit)? = null
) {
    val accent = MiuixTheme.colorScheme.primary
    val subtitleColor = MiuixTheme.colorScheme.onBackgroundVariant
    val readyColor = Color(0xFF2E7D32)
    val notReadyColor = Color(0xFFC62828)
    val listState = rememberLazyListState()
    val scrollBehavior = MiuixScrollBehavior()
    val surfaceColor = MiuixTheme.colorScheme.surface

    val backdrop: LayerBackdrop = rememberLayerBackdrop {
        drawRect(surfaceColor)
        drawContent()
    }

    LaunchedEffect(scrollToTopSignal) {
        if (scrollToTopSignal > 0) listState.animateScrollToItem(0)
    }

    val appInfo: ApplicationInfo? = packageInfo?.applicationInfo
    val shizukuDetailMap = remember(shizukuStatus) {
        shizukuApiUtils.detailedRows().toMap()
    }
    val shizukuReady = shizukuStatus.contains("granted", ignoreCase = true)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = "About",
                scrollBehavior = scrollBehavior,
                color = MiuixTheme.colorScheme.surface,
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = MiuixIcons.Regular.Back,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            )
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
                start = 16.dp,
                end = 16.dp
            )
        ) {
            item {
                SmallTitle("应用信息")
            }

            item { Spacer(modifier = Modifier.height(14.dp)) }

            item {
                FrostedBlock(
                    backdrop = backdrop,
                    title = "App",
                    subtitle = "应用身份与运行时信息",
                    accent = accent,
                    content = {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            MiuixInfoItem(
                                "名称",
                                appLabel
                            )
                            MiuixInfoItem(
                                "包名",
                                packageInfo?.packageName ?: "unknown"
                            )
                            MiuixInfoItem(
                                "版本",
                                "${packageInfo?.versionName ?: "unknown"} (${packageInfo?.longVersionCode ?: -1})"
                            )
                            MiuixInfoItem(
                                "Debuggable",
                                (((appInfo?.flags ?: 0) and ApplicationInfo.FLAG_DEBUGGABLE) != 0).toString()
                            )
                            MiuixInfoItem(
                                "运行时",
                                "API ${Build.VERSION.SDK_INT} · Security Patch ${Build.VERSION.SECURITY_PATCH ?: "unknown"}"
                            )
                            MiuixInfoItem(
                                "最后更新",
                                packageInfo?.lastUpdateTime?.let(::formatTime) ?: "unknown"
                            )
                            MiuixInfoItem(
                                "框架",
                                "Miuix UI 0.9.0 · Shizuku API ${ShizukuApiUtils.API_VERSION}"
                            )
                        }
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(14.dp)) }

            item {
                FrostedBlock(
                    backdrop = backdrop,
                    title = "Permissions",
                    subtitle = "权限与服务状态",
                    accent = if (shizukuReady) readyColor else notReadyColor,
                    content = {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            StatusPill(
                                label = if (shizukuReady) "Shizuku Ready" else "Shizuku Limited",
                                color = if (shizukuReady) readyColor else notReadyColor
                            )
                            Text(
                                text = if (shizukuReady) {
                                    "当前已具备完整 Shizuku 能力"
                                } else {
                                    "当前未就绪，点击下方可重新检查"
                                },
                                color = subtitleColor
                            )
                            MiuixInfoItem(
                                "通知权限",
                                if (notificationPermissionGranted) "granted" else "not granted"
                            )
                            MiuixInfoItem(
                                "Shizuku uname",
                                shizukuDetailMap["Shizuku uname"] ?: "N/A",
                                onClick = onCheckShizuku
                            )
                            MiuixInfoItem(
                                "SELinux",
                                shizukuDetailMap["Shizuku getenforce"] ?: "N/A",
                                onClick = onCheckShizuku
                            )
                        }
                    }
                )
            }
        }
    }
}
