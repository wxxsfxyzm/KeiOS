package com.example.keios.ui.page.main.github.sheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.dp
import com.example.keios.R
import com.example.keios.feature.github.model.GitHubLookupConfig
import com.example.keios.feature.github.model.GitHubTrackedApp
import com.example.keios.feature.github.model.InstalledAppItem
import com.example.keios.ui.page.main.github.RefreshIntervalOption
import com.example.keios.ui.page.main.github.GitHubAppCandidateRow
import com.example.keios.ui.page.main.github.GitHubSelectedAppCard
import com.example.keios.ui.page.main.github.GitHubStatusPalette
import com.example.keios.ui.page.main.github.page.action.GitHubTrackImportPreview
import com.example.keios.ui.page.main.os.appLucideCloseIcon
import com.example.keios.ui.page.main.os.appLucideConfirmIcon
import com.example.keios.ui.page.main.github.query.DownloaderOption
import com.example.keios.ui.page.main.github.query.OnlineShareTargetOption
import com.example.keios.ui.page.main.github.query.noOnlineShareTargetOption
import com.example.keios.ui.page.main.github.query.systemDefaultDownloaderOption
import com.example.keios.ui.page.main.github.query.systemDownloadManagerOption
import com.example.keios.ui.page.main.widget.glass.AppDropdownSelector
import com.example.keios.ui.page.main.widget.glass.GlassIconButton
import com.example.keios.ui.page.main.widget.glass.GlassSearchField
import com.example.keios.ui.page.main.widget.glass.GlassTextButton
import com.example.keios.ui.page.main.widget.glass.GlassVariant
import com.example.keios.ui.page.main.widget.core.MiuixInfoItem
import com.example.keios.ui.page.main.widget.sheet.SheetContentColumn
import com.example.keios.ui.page.main.widget.sheet.SheetControlRow
import com.example.keios.ui.page.main.widget.sheet.SheetDescriptionText
import com.example.keios.ui.page.main.widget.sheet.SheetInputTitle
import com.example.keios.ui.page.main.widget.sheet.SheetSectionCard
import com.example.keios.ui.page.main.widget.sheet.SheetSectionTitle
import com.example.keios.ui.page.main.widget.sheet.SnapshotWindowBottomSheet
import com.example.keios.ui.page.main.widget.status.StatusPill
import com.kyant.backdrop.backdrops.LayerBackdrop
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog

@Composable
internal fun GitHubCheckLogicSheet(
    show: Boolean,
    backdrop: LayerBackdrop,
    lookupConfig: GitHubLookupConfig,
    trackedCount: Int,
    refreshIntervalHours: Int,
    refreshIntervalHoursInput: Int,
    checkAllTrackedPreReleasesInput: Boolean,
    aggressiveApkFilteringInput: Boolean,
    shareImportLinkageEnabledInput: Boolean,
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
    exportInProgress: Boolean,
    importInProgress: Boolean,
    onDismissRequest: () -> Unit,
    onApply: () -> Unit,
    onExportTrackedItems: () -> Unit,
    onImportTrackedItems: () -> Unit,
    onRefreshIntervalHoursInputChange: (Int) -> Unit,
    onCheckAllTrackedPreReleasesInputChange: (Boolean) -> Unit,
    onAggressiveApkFilteringInputChange: (Boolean) -> Unit,
    onShareImportLinkageEnabledInputChange: (Boolean) -> Unit,
    onPreferredDownloaderPackageInputChange: (String) -> Unit,
    onOnlineShareTargetPackageInputChange: (String) -> Unit,
    onShowCheckLogicIntervalPopupChange: (Boolean) -> Unit,
    onShowDownloaderPopupChange: (Boolean) -> Unit,
    onShowOnlineShareTargetPopupChange: (Boolean) -> Unit,
    onCheckLogicIntervalPopupAnchorBoundsChange: (IntRect?) -> Unit,
    onDownloaderPopupAnchorBoundsChange: (IntRect?) -> Unit,
    onOnlineShareTargetPopupAnchorBoundsChange: (IntRect?) -> Unit
) {
    val context = LocalContext.current
    SnapshotWindowBottomSheet(
        show = show,
        title = stringResource(R.string.github_check_sheet_title),
        onDismissRequest = onDismissRequest,
        startAction = {
            GlassIconButton(
                backdrop = backdrop,
                variant = GlassVariant.Bar,
                icon = appLucideCloseIcon(),
                contentDescription = stringResource(R.string.common_close),
                onClick = onDismissRequest
            )
        },
        endAction = {
            GlassIconButton(
                backdrop = backdrop,
                variant = GlassVariant.Bar,
                icon = appLucideConfirmIcon(),
                contentDescription = stringResource(R.string.github_check_sheet_cd_save),
                onClick = onApply
            )
        }
    ) {
        val selectedRefreshOption = RefreshIntervalOption.fromHours(refreshIntervalHoursInput)
        val allDownloaderOptions = remember(downloaderOptions) {
            listOf(systemDefaultDownloaderOption(context), systemDownloadManagerOption(context)) + downloaderOptions
        }
        val onlineShareTargetOptions = remember(installedOnlineShareTargets) {
            listOf(noOnlineShareTargetOption(context)) + installedOnlineShareTargets
        }
        val selectedDownloaderLabel = allDownloaderOptions.firstOrNull {
            it.packageName == preferredDownloaderPackageInput
        }?.label ?: systemDefaultDownloaderOption(context).label
        val selectedOnlineShareTargetLabel = onlineShareTargetOptions.firstOrNull {
            it.packageName == onlineShareTargetPackageInput
        }?.label ?: noOnlineShareTargetOption(context).label
        val logicChanged = refreshIntervalHoursInput != refreshIntervalHours ||
            checkAllTrackedPreReleasesInput != lookupConfig.checkAllTrackedPreReleases ||
            aggressiveApkFilteringInput != lookupConfig.aggressiveApkFiltering ||
            shareImportLinkageEnabledInput != lookupConfig.shareImportLinkageEnabled ||
            onlineShareTargetPackageInput != lookupConfig.onlineShareTargetPackage ||
            preferredDownloaderPackageInput != lookupConfig.preferredDownloaderPackage

        SheetContentColumn(verticalSpacing = 8.dp) {
            SheetSectionTitle(stringResource(R.string.github_check_sheet_section_checks))
            SheetSectionCard {
                SheetControlRow(
                    label = stringResource(R.string.github_check_sheet_label_refresh_interval),
                    summary = stringResource(R.string.github_check_sheet_summary_refresh_interval)
                ) {
                    AppDropdownSelector(
                        selectedText = stringResource(selectedRefreshOption.labelRes),
                        options = RefreshIntervalOption.entries.map { option ->
                            context.getString(option.labelRes)
                        },
                        selectedIndex = RefreshIntervalOption.entries.indexOf(selectedRefreshOption),
                        expanded = showCheckLogicIntervalPopup,
                        anchorBounds = checkLogicIntervalPopupAnchorBounds,
                        onExpandedChange = onShowCheckLogicIntervalPopupChange,
                        onSelectedIndexChange = { selectedIndex ->
                            onRefreshIntervalHoursInputChange(RefreshIntervalOption.entries[selectedIndex].hours)
                        },
                        onAnchorBoundsChange = onCheckLogicIntervalPopupAnchorBoundsChange,
                        backdrop = backdrop,
                        variant = GlassVariant.SheetAction
                    )
                }
                SheetControlRow(
                    label = stringResource(R.string.github_check_sheet_label_prerelease_check),
                    summary = stringResource(R.string.github_check_sheet_summary_prerelease_check)
                ) {
                    Switch(
                        checked = checkAllTrackedPreReleasesInput,
                        onCheckedChange = onCheckAllTrackedPreReleasesInputChange
                    )
                }
                SheetControlRow(
                    label = stringResource(R.string.github_check_sheet_label_aggressive_filter),
                    summary = stringResource(R.string.github_check_sheet_summary_aggressive_filter)
                ) {
                    Switch(
                        checked = aggressiveApkFilteringInput,
                        onCheckedChange = onAggressiveApkFilteringInputChange
                    )
                }
                SheetControlRow(
                    label = stringResource(R.string.github_check_sheet_label_save_state)
                ) {
                    StatusPill(
                        label = if (logicChanged) {
                            stringResource(R.string.common_pending_save)
                        } else {
                            stringResource(R.string.common_synced)
                        },
                        color = if (logicChanged) {
                            GitHubStatusPalette.PreRelease
                        } else {
                            GitHubStatusPalette.Update
                        }
                    )
                }
            }

            SheetSectionTitle(stringResource(R.string.github_check_sheet_section_transfer))
            SheetSectionCard {
                SheetControlRow(
                    label = stringResource(R.string.github_check_sheet_label_share_import_linkage),
                    summary = stringResource(R.string.github_check_sheet_summary_share_import_linkage)
                ) {
                    Switch(
                        checked = shareImportLinkageEnabledInput,
                        onCheckedChange = onShareImportLinkageEnabledInputChange
                    )
                }
                SheetControlRow(
                    label = stringResource(R.string.github_check_sheet_label_downloader),
                    summary = stringResource(R.string.github_check_sheet_summary_downloader)
                ) {
                    AppDropdownSelector(
                        selectedText = selectedDownloaderLabel,
                        options = allDownloaderOptions.map { it.label },
                        selectedIndex = allDownloaderOptions.indexOfFirst {
                            preferredDownloaderPackageInput == it.packageName
                        }.coerceAtLeast(0),
                        expanded = showDownloaderPopup,
                        anchorBounds = downloaderPopupAnchorBounds,
                        onExpandedChange = onShowDownloaderPopupChange,
                        onSelectedIndexChange = { selectedIndex ->
                            onPreferredDownloaderPackageInputChange(
                                allDownloaderOptions[selectedIndex].packageName
                            )
                        },
                        onAnchorBoundsChange = onDownloaderPopupAnchorBoundsChange,
                        backdrop = backdrop,
                        variant = GlassVariant.SheetAction
                    )
                }
                SheetControlRow(
                    label = stringResource(R.string.github_check_sheet_label_share_to_installer),
                    summary = if (installedOnlineShareTargets.isNotEmpty()) {
                        stringResource(R.string.github_check_sheet_summary_share_available)
                    } else {
                        stringResource(R.string.github_check_sheet_summary_share_unavailable)
                    }
                ) {
                    AppDropdownSelector(
                        selectedText = selectedOnlineShareTargetLabel,
                        options = onlineShareTargetOptions.map { it.label },
                        selectedIndex = onlineShareTargetOptions.indexOfFirst {
                            onlineShareTargetPackageInput == it.packageName
                        }.coerceAtLeast(0),
                        expanded = showOnlineShareTargetPopup,
                        anchorBounds = onlineShareTargetPopupAnchorBounds,
                        onExpandedChange = onShowOnlineShareTargetPopupChange,
                        onSelectedIndexChange = { selectedIndex ->
                            onOnlineShareTargetPackageInputChange(
                                onlineShareTargetOptions[selectedIndex].packageName
                            )
                        },
                        onAnchorBoundsChange = onOnlineShareTargetPopupAnchorBoundsChange,
                        backdrop = backdrop,
                        variant = GlassVariant.SheetAction
                    )
                }
            }

            SheetSectionTitle(stringResource(R.string.github_check_sheet_section_tracks))
            SheetSectionCard {
                SheetControlRow(
                    label = stringResource(R.string.github_check_sheet_label_track_count),
                    summary = stringResource(R.string.github_check_sheet_summary_track_count)
                ) {
                    StatusPill(
                        label = stringResource(
                            R.string.github_check_sheet_value_track_count,
                            trackedCount
                        ),
                        color = if (trackedCount > 0) {
                            GitHubStatusPalette.Active
                        } else {
                            MiuixTheme.colorScheme.onBackgroundVariant
                        }
                    )
                }
                SheetInputTitle(stringResource(R.string.github_check_sheet_label_track_transfer))
                SheetDescriptionText(
                    text = stringResource(R.string.github_check_sheet_summary_track_transfer)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        GlassTextButton(
                            backdrop = backdrop,
                            text = if (exportInProgress) {
                                stringResource(R.string.github_check_sheet_action_exporting)
                            } else {
                                stringResource(R.string.github_check_sheet_action_export_tracks)
                            },
                            onClick = onExportTrackedItems,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !exportInProgress && !importInProgress,
                            variant = GlassVariant.SheetAction
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        GlassTextButton(
                            backdrop = backdrop,
                            text = if (importInProgress) {
                                stringResource(R.string.github_check_sheet_action_importing)
                            } else {
                                stringResource(R.string.github_check_sheet_action_import_tracks)
                            },
                            onClick = onImportTrackedItems,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !exportInProgress && !importInProgress,
                            variant = GlassVariant.SheetAction
                        )
                    }
                }
            }

            SheetSectionTitle(stringResource(R.string.github_check_sheet_section_notes))
            SheetSectionCard {
                SheetDescriptionText(
                    text = stringResource(R.string.github_check_sheet_note_1)
                )
                SheetDescriptionText(
                    text = stringResource(R.string.github_check_sheet_note_2)
                )
                SheetDescriptionText(
                    text = stringResource(R.string.github_check_sheet_note_3)
                )
            }
        }
    }
}

