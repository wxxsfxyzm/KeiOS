package com.example.keios.ui.page.main.settings.section

import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.example.keios.R
import com.example.keios.core.log.AppLogStore
import com.example.keios.ui.page.main.os.appLucideNotesIcon
import com.example.keios.ui.page.main.settings.support.SettingsGroupCard
import com.example.keios.ui.page.main.settings.support.SettingsInfoItem
import com.example.keios.ui.page.main.settings.support.SettingsToggleItem
import com.example.keios.ui.page.main.settings.support.formatBytes
import com.example.keios.ui.page.main.settings.support.formatLogTime
import com.example.keios.ui.page.main.widget.core.AppDualActionRow
import com.example.keios.ui.page.main.widget.glass.GlassTextButton
import com.example.keios.ui.page.main.widget.glass.GlassVariant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun SettingsLogSection(
    logDebugEnabled: Boolean,
    onLogDebugChanged: (Boolean) -> Unit,
    logStats: AppLogStore.Stats,
    exportingLogZip: Boolean,
    onExportingLogZipChange: (Boolean) -> Unit,
    clearingLogs: Boolean,
    onClearingLogsChange: (Boolean) -> Unit,
    onLogReloadSignal: () -> Unit,
    logExportLauncher: ActivityResultLauncher<String>,
    enabledCardColor: Color,
    disabledCardColor: Color
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val logGroupActive = logDebugEnabled || logStats.fileCount > 0
    val logLatestText = if (logStats.latestModifiedAtMs <= 0L) {
        stringResource(R.string.settings_log_stat_latest_empty)
    } else {
        formatLogTime(logStats.latestModifiedAtMs)
    }
    SettingsGroupCard(
        header = stringResource(R.string.settings_group_log_header),
        title = stringResource(R.string.settings_group_log_title),
        sectionIcon = appLucideNotesIcon(),
        containerColor = if (logGroupActive) enabledCardColor else disabledCardColor
    ) {
        SettingsToggleItem(
            title = stringResource(R.string.settings_log_debug_title),
            summary = if (logDebugEnabled) {
                stringResource(R.string.settings_log_debug_summary_enabled)
            } else {
                stringResource(R.string.settings_log_debug_summary_disabled)
            },
            checked = logDebugEnabled,
            onCheckedChange = onLogDebugChanged,
            infoKey = stringResource(R.string.common_scope),
            infoValue = stringResource(R.string.settings_log_scope)
        )
        SettingsInfoItem(
            key = stringResource(R.string.common_note),
            value = if (logDebugEnabled) {
                stringResource(R.string.settings_log_note_enabled)
            } else {
                stringResource(R.string.settings_log_note_disabled)
            }
        )
        SettingsInfoItem(
            key = stringResource(R.string.settings_log_stat_size),
            value = formatBytes(logStats.totalBytes)
        )
        SettingsInfoItem(
            key = stringResource(R.string.settings_log_stat_files),
            value = stringResource(R.string.settings_log_stat_files_count, logStats.fileCount)
        )
        SettingsInfoItem(
            key = stringResource(R.string.settings_log_stat_latest),
            value = logLatestText
        )
        AppDualActionRow(
            first = { modifier ->
                GlassTextButton(
                    backdrop = null,
                    variant = GlassVariant.SheetPrimaryAction,
                    text = if (exportingLogZip) {
                        stringResource(R.string.common_processing)
                    } else {
                        stringResource(R.string.settings_log_action_export_zip)
                    },
                    modifier = modifier,
                    textColor = MiuixTheme.colorScheme.primary,
                    enabled = !exportingLogZip && !clearingLogs,
                    onClick = {
                        onExportingLogZipChange(true)
                        val stamp = SimpleDateFormat(
                            "yyyyMMdd-HHmmss",
                            Locale.getDefault()
                        ).format(Date())
                        logExportLauncher.launch("keios-logs-$stamp.zip")
                    }
                )
            },
            second = { modifier ->
                GlassTextButton(
                    backdrop = null,
                    variant = GlassVariant.SheetDangerAction,
                    text = if (clearingLogs) {
                        stringResource(R.string.common_processing)
                    } else {
                        stringResource(R.string.settings_log_action_clear)
                    },
                    modifier = modifier,
                    textColor = MiuixTheme.colorScheme.error,
                    enabled = !exportingLogZip && !clearingLogs,
                    onClick = {
                        scope.launch {
                            onClearingLogsChange(true)
                            val result = withContext(Dispatchers.IO) {
                                runCatching { AppLogStore.clear(context) }
                            }
                            onClearingLogsChange(false)
                            if (result.isSuccess) {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.settings_log_toast_cleared),
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                val reason = result.exceptionOrNull()?.javaClass?.simpleName
                                    ?: context.getString(R.string.common_unknown)
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.settings_log_toast_clear_failed, reason),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            onLogReloadSignal()
                        }
                    }
                )
            }
        )
    }
}
