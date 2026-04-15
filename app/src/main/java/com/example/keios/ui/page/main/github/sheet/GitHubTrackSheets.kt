package com.example.keios.ui.page.main.github.sheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.dp
import com.example.keios.feature.github.model.GitHubLookupConfig
import com.example.keios.feature.github.model.GitHubTrackedApp
import com.example.keios.feature.github.model.InstalledAppItem
import com.example.keios.ui.page.main.RefreshIntervalOption
import com.example.keios.ui.page.main.GitHubAppCandidateRow
import com.example.keios.ui.page.main.GitHubSelectedAppCard
import com.example.keios.ui.page.main.GitHubStatusPalette
import com.example.keios.ui.page.main.github.query.DownloaderOption
import com.example.keios.ui.page.main.github.query.OnlineShareTargetOption
import com.example.keios.ui.page.main.github.query.noOnlineShareTargetOption
import com.example.keios.ui.page.main.github.query.systemDefaultDownloaderOption
import com.example.keios.ui.page.main.github.query.systemDownloadManagerOption
import com.example.keios.ui.page.main.widget.GlassIconButton
import com.example.keios.ui.page.main.widget.GlassSearchField
import com.example.keios.ui.page.main.widget.GlassTextButton
import com.example.keios.ui.page.main.widget.GlassVariant
import com.example.keios.ui.page.main.widget.LiquidDropdownColumn
import com.example.keios.ui.page.main.widget.LiquidDropdownImpl
import com.example.keios.ui.page.main.widget.MiuixInfoItem
import com.example.keios.ui.page.main.widget.SheetContentColumn
import com.example.keios.ui.page.main.widget.SheetControlRow
import com.example.keios.ui.page.main.widget.SheetDescriptionText
import com.example.keios.ui.page.main.widget.SheetInputTitle
import com.example.keios.ui.page.main.widget.SheetSectionCard
import com.example.keios.ui.page.main.widget.SheetSectionTitle
import com.example.keios.ui.page.main.widget.SnapshotWindowBottomSheet
import com.example.keios.ui.page.main.widget.SnapshotWindowListPopup
import com.example.keios.ui.page.main.widget.SnapshotPopupPlacement
import com.example.keios.ui.page.main.widget.StatusPill
import com.example.keios.ui.page.main.widget.capturePopupAnchor
import com.kyant.backdrop.backdrops.LayerBackdrop
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.Ok
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog

@Composable
internal fun GitHubCheckLogicSheet(
    show: Boolean,
    backdrop: LayerBackdrop,
    lookupConfig: GitHubLookupConfig,
    refreshIntervalHours: Int,
    refreshIntervalHoursInput: Int,
    checkAllTrackedPreReleasesInput: Boolean,
    aggressiveApkFilteringInput: Boolean,
    onlineShareTargetPackageInput: String,
    preferredDownloaderPackageInput: String,
    installedOnlineShareTargets: List<OnlineShareTargetOption>,
    showCheckLogicIntervalPopup: Boolean,
    showDownloaderPopup: Boolean,
    showOnlineShareTargetPopup: Boolean,
    checkLogicIntervalPopupAnchorBounds: IntRect?,
    downloaderPopupAnchorBounds: IntRect?,
    onlineShareTargetPopupAnchorBounds: IntRect?,
    downloaderOptions: List<DownloaderOption>,
    onDismissRequest: () -> Unit,
    onApply: () -> Unit,
    onRefreshIntervalHoursInputChange: (Int) -> Unit,
    onCheckAllTrackedPreReleasesInputChange: (Boolean) -> Unit,
    onAggressiveApkFilteringInputChange: (Boolean) -> Unit,
    onPreferredDownloaderPackageInputChange: (String) -> Unit,
    onOnlineShareTargetPackageInputChange: (String) -> Unit,
    onShowCheckLogicIntervalPopupChange: (Boolean) -> Unit,
    onShowDownloaderPopupChange: (Boolean) -> Unit,
    onShowOnlineShareTargetPopupChange: (Boolean) -> Unit,
    onCheckLogicIntervalPopupAnchorBoundsChange: (IntRect?) -> Unit,
    onDownloaderPopupAnchorBoundsChange: (IntRect?) -> Unit,
    onOnlineShareTargetPopupAnchorBoundsChange: (IntRect?) -> Unit
) {
    SnapshotWindowBottomSheet(
        show = show,
        title = "检查与下载",
        onDismissRequest = onDismissRequest,
        startAction = {
            GlassIconButton(
                backdrop = backdrop,
                variant = GlassVariant.Bar,
                icon = MiuixIcons.Regular.Close,
                contentDescription = "关闭",
                onClick = onDismissRequest
            )
        },
        endAction = {
            GlassIconButton(
                backdrop = backdrop,
                variant = GlassVariant.Bar,
                icon = MiuixIcons.Regular.Ok,
                contentDescription = "保存检查逻辑",
                onClick = onApply
            )
        }
    ) {
        val selectedRefreshOption = RefreshIntervalOption.fromHours(refreshIntervalHoursInput)
        val allDownloaderOptions = remember(downloaderOptions) {
            listOf(systemDefaultDownloaderOption, systemDownloadManagerOption) + downloaderOptions
        }
        val onlineShareTargetOptions = remember(installedOnlineShareTargets) {
            listOf(noOnlineShareTargetOption) + installedOnlineShareTargets
        }
        val selectedDownloaderLabel = allDownloaderOptions.firstOrNull {
            it.packageName == preferredDownloaderPackageInput
        }?.label ?: systemDefaultDownloaderOption.label
        val selectedOnlineShareTargetLabel = onlineShareTargetOptions.firstOrNull {
            it.packageName == onlineShareTargetPackageInput
        }?.label ?: noOnlineShareTargetOption.label
        val logicChanged = refreshIntervalHoursInput != refreshIntervalHours ||
            checkAllTrackedPreReleasesInput != lookupConfig.checkAllTrackedPreReleases ||
            aggressiveApkFilteringInput != lookupConfig.aggressiveApkFiltering ||
            onlineShareTargetPackageInput != lookupConfig.onlineShareTargetPackage ||
            preferredDownloaderPackageInput != lookupConfig.preferredDownloaderPackage

        SheetContentColumn(verticalSpacing = 8.dp) {
            SheetSectionTitle("当前摘要")
            SheetSectionCard {
                SheetControlRow(label = "更新间隔", summary = "超时后自动视为过期") {
                    Box(
                        modifier = Modifier.capturePopupAnchor { onCheckLogicIntervalPopupAnchorBoundsChange(it) }
                    ) {
                        GlassTextButton(
                            backdrop = backdrop,
                            variant = GlassVariant.SheetAction,
                            text = selectedRefreshOption.label,
                            onClick = { onShowCheckLogicIntervalPopupChange(!showCheckLogicIntervalPopup) }
                        )
                        if (showCheckLogicIntervalPopup) {
                            SnapshotWindowListPopup(
                                show = showCheckLogicIntervalPopup,
                                alignment = PopupPositionProvider.Align.BottomEnd,
                                anchorBounds = checkLogicIntervalPopupAnchorBounds,
                                placement = SnapshotPopupPlacement.ButtonEnd,
                                onDismissRequest = { onShowCheckLogicIntervalPopupChange(false) },
                                enableWindowDim = false
                            ) {
                                LiquidDropdownColumn {
                                    val options = RefreshIntervalOption.entries
                                    options.forEachIndexed { index, option ->
                                        LiquidDropdownImpl(
                                            text = option.label,
                                            optionSize = options.size,
                                            isSelected = selectedRefreshOption == option,
                                            index = index,
                                            onSelectedIndexChange = { selectedIndex ->
                                                onRefreshIntervalHoursInputChange(options[selectedIndex].hours)
                                                onShowCheckLogicIntervalPopupChange(false)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                SheetControlRow(
                    label = "检查所有追踪项的预发行最新版",
                    summary = "额外检查每个项目的最新预发，但不代表自动推荐安装"
                ) {
                    Switch(
                        checked = checkAllTrackedPreReleasesInput,
                        onCheckedChange = onCheckAllTrackedPreReleasesInputChange
                    )
                }
                SheetControlRow(
                    label = "更激进的过滤方式",
                    summary = "忽略 `armeabi-v7a`、`armeabi`、`x86_64`、`x86`；若有 arm64-v8a 也忽略 universal"
                ) {
                    Switch(
                        checked = aggressiveApkFilteringInput,
                        onCheckedChange = onAggressiveApkFilteringInputChange
                    )
                }
                SheetControlRow(label = "下载器", summary = "用于直链下载跳转；默认跟随系统") {
                    Box(
                        modifier = Modifier.capturePopupAnchor { onDownloaderPopupAnchorBoundsChange(it) }
                    ) {
                        GlassTextButton(
                            backdrop = backdrop,
                            variant = GlassVariant.SheetAction,
                            text = selectedDownloaderLabel,
                            onClick = { onShowDownloaderPopupChange(!showDownloaderPopup) }
                        )
                        if (showDownloaderPopup) {
                            SnapshotWindowListPopup(
                                show = showDownloaderPopup,
                                alignment = PopupPositionProvider.Align.BottomEnd,
                                anchorBounds = downloaderPopupAnchorBounds,
                                placement = SnapshotPopupPlacement.ButtonEnd,
                                onDismissRequest = { onShowDownloaderPopupChange(false) },
                                enableWindowDim = false
                            ) {
                                LiquidDropdownColumn {
                                    allDownloaderOptions.forEachIndexed { index, option ->
                                        LiquidDropdownImpl(
                                            text = option.label,
                                            optionSize = allDownloaderOptions.size,
                                            isSelected = preferredDownloaderPackageInput == option.packageName,
                                            index = index,
                                            onSelectedIndexChange = { selectedIndex ->
                                                onPreferredDownloaderPackageInputChange(
                                                    allDownloaderOptions[selectedIndex].packageName
                                                )
                                                onShowDownloaderPopupChange(false)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                SheetControlRow(
                    label = "分享到安装器",
                    summary = if (installedOnlineShareTargets.isNotEmpty()) {
                        "分享时可直接发送到支持直接边下载边安装的安装器"
                    } else {
                        "未检测到支持的安装器，默认不联动"
                    }
                ) {
                    Box(
                        modifier = Modifier.capturePopupAnchor {
                            onOnlineShareTargetPopupAnchorBoundsChange(it)
                        }
                    ) {
                        GlassTextButton(
                            backdrop = backdrop,
                            variant = GlassVariant.SheetAction,
                            text = selectedOnlineShareTargetLabel,
                            onClick = { onShowOnlineShareTargetPopupChange(!showOnlineShareTargetPopup) }
                        )
                        if (showOnlineShareTargetPopup) {
                            SnapshotWindowListPopup(
                                show = showOnlineShareTargetPopup,
                                alignment = PopupPositionProvider.Align.BottomEnd,
                                anchorBounds = onlineShareTargetPopupAnchorBounds,
                                placement = SnapshotPopupPlacement.ButtonEnd,
                                onDismissRequest = { onShowOnlineShareTargetPopupChange(false) },
                                enableWindowDim = false
                            ) {
                                LiquidDropdownColumn {
                                    onlineShareTargetOptions.forEachIndexed { index, option ->
                                        LiquidDropdownImpl(
                                            text = option.label,
                                            optionSize = onlineShareTargetOptions.size,
                                            isSelected = onlineShareTargetPackageInput == option.packageName,
                                            index = index,
                                            onSelectedIndexChange = { selectedIndex ->
                                                onOnlineShareTargetPackageInputChange(
                                                    onlineShareTargetOptions[selectedIndex].packageName
                                                )
                                                onShowOnlineShareTargetPopupChange(false)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                SheetControlRow(label = "保存状态") {
                    StatusPill(
                        label = if (logicChanged) "待保存" else "已同步",
                        color = if (logicChanged) {
                            GitHubStatusPalette.PreRelease
                        } else {
                            MiuixTheme.colorScheme.onBackgroundVariant
                        }
                    )
                }
            }

            SheetSectionTitle("说明")
            SheetSectionCard {
                SheetDescriptionText(
                    text = "全局开关只决定是否顺手检查最新预发；单条目的“优先预发行版本”才决定推荐更新时是否偏向预发。"
                )
                SheetDescriptionText(
                    text = "这样就把“想知道有没有新预发”和“真的想装预发”拆开了。"
                )
                SheetDescriptionText(
                    text = "若设备主要只关心 arm64，可开启“更激进的过滤方式”。它会过滤 `armeabi-v7a`、`armeabi`、`x86_64`、`x86`；只有同一 release 已存在 `arm64-v8a` 时，才会连带过滤 universal。"
                )
                SheetDescriptionText(
                    text = "该选项只影响“分享”动作，默认不联动。目前仅识别 `com.rosan.installer.x.revived` 与 `io.github.vvb2060.packageinstaller`，可将分享直接发送到支持边下载边安装的安装器。"
                )
                SheetDescriptionText(
                    text = "下载器列表会先扫描支持 `ACTION_VIEW` + `CATEGORY_BROWSABLE` 的候选应用，再优先标记更像下载器的条目；“系统内置下载器”会直接走 Android DownloadManager。"
                )
            }
        }
    }
}

@Composable
internal fun GitHubTrackEditSheet(
    show: Boolean,
    backdrop: LayerBackdrop,
    editingTrackedItem: GitHubTrackedApp?,
    repoUrlInput: String,
    appSearch: String,
    pickerExpanded: Boolean,
    selectedApp: InstalledAppItem?,
    appList: List<InstalledAppItem>,
    preferPreReleaseInput: Boolean,
    onDismissRequest: () -> Unit,
    onApply: () -> Unit,
    onRepoUrlInputChange: (String) -> Unit,
    onAppSearchChange: (String) -> Unit,
    onPickerExpandedChange: (Boolean) -> Unit,
    onSelectedAppChange: (InstalledAppItem?) -> Unit,
    onPreferPreReleaseInputChange: (Boolean) -> Unit,
    onRequestDelete: () -> Unit
) {
    SnapshotWindowBottomSheet(
        show = show,
        title = if (editingTrackedItem == null) "新增跟踪" else "编辑跟踪",
        onDismissRequest = onDismissRequest,
        startAction = {
            GlassIconButton(
                backdrop = backdrop,
                variant = GlassVariant.Bar,
                icon = MiuixIcons.Regular.Close,
                contentDescription = "关闭",
                onClick = onDismissRequest
            )
        },
        endAction = {
            GlassIconButton(
                backdrop = backdrop,
                variant = GlassVariant.Bar,
                icon = MiuixIcons.Regular.Ok,
                contentDescription = if (editingTrackedItem == null) "确认新增" else "确认保存",
                onClick = onApply
            )
        }
    ) {
        SheetContentColumn(verticalSpacing = 8.dp) {
            SheetSectionTitle("仓库与应用")
            SheetSectionCard {
                SheetInputTitle("GitHub 项目地址")
                GlassSearchField(
                    value = repoUrlInput,
                    onValueChange = onRepoUrlInputChange,
                    label = "GitHub 项目地址",
                    backdrop = backdrop,
                    variant = GlassVariant.SheetInput,
                    singleLine = true
                )
                SheetInputTitle("筛选本机 App")
                GlassSearchField(
                    value = appSearch,
                    onValueChange = onAppSearchChange,
                    label = "名称或包名",
                    backdrop = backdrop,
                    variant = GlassVariant.SheetInput,
                    singleLine = true
                )
                SheetControlRow(
                    label = "已选应用",
                    summary = if (selectedApp == null) "未选择" else null
                ) {
                    GlassTextButton(
                        backdrop = backdrop,
                        variant = GlassVariant.SheetAction,
                        text = if (pickerExpanded) "收起列表" else "选择应用",
                        onClick = { onPickerExpandedChange(!pickerExpanded) }
                    )
                }
                selectedApp?.let { app ->
                    GitHubSelectedAppCard(selectedApp = app)
                }
            }
            SheetSectionTitle("检查选项")
            SheetSectionCard {
                SheetControlRow(
                    label = "优先预发行版本",
                    summary = "仅影响这个项目的推荐更新目标，不影响全局是否检查预发行"
                ) {
                    Switch(
                        checked = preferPreReleaseInput,
                        onCheckedChange = onPreferPreReleaseInputChange
                    )
                }
            }
            if (pickerExpanded) {
                val filteredApps = appList.filter { app ->
                    appSearch.isBlank() ||
                        app.label.contains(appSearch, ignoreCase = true) ||
                        app.packageName.contains(appSearch, ignoreCase = true)
                }.take(80)
                SheetSectionTitle("应用候选")
                SheetSectionCard(verticalSpacing = 6.dp) {
                    if (filteredApps.isEmpty()) {
                        MiuixInfoItem("应用列表", "没有匹配结果")
                    } else {
                        filteredApps.forEach { app ->
                            GitHubAppCandidateRow(
                                app = app,
                                selected = selectedApp?.packageName == app.packageName,
                                onClick = {
                                    onSelectedAppChange(app)
                                    onPickerExpandedChange(false)
                                }
                            )
                        }
                    }
                }
            }
            if (editingTrackedItem != null) {
                SheetSectionTitle(
                    text = "危险操作",
                    danger = true
                )
                SheetSectionCard {
                    GlassTextButton(
                        backdrop = backdrop,
                        variant = GlassVariant.SheetDangerAction,
                        text = "删除跟踪",
                        textColor = MiuixTheme.colorScheme.error,
                        onClick = onRequestDelete
                    )
                }
            }
        }
    }
}

@Composable
internal fun GitHubDeleteTrackDialog(
    pendingDeleteItem: GitHubTrackedApp?,
    deleteInProgress: Boolean,
    onDismissRequest: () -> Unit,
    onCancel: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    WindowDialog(
        show = pendingDeleteItem != null,
        title = "删除跟踪",
        summary = pendingDeleteItem?.let { "确定删除 ${it.appLabel} (${it.owner}/${it.repo}) 吗？" },
        onDismissRequest = onDismissRequest
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.height(16.dp))
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    modifier = Modifier.weight(1f),
                    text = "取消",
                    onClick = onCancel
                )
                TextButton(
                    modifier = Modifier.weight(1f),
                    text = if (deleteInProgress) "删除中..." else "删除",
                    colors = ButtonDefaults.textButtonColors(
                        color = MiuixTheme.colorScheme.error,
                        textColor = MiuixTheme.colorScheme.onError
                    ),
                    onClick = onConfirmDelete
                )
            }
        }
    }
}
