package com.example.keios.ui.page.main.os.shell

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.keios.R
import com.example.keios.ui.page.main.os.appLucideCloseIcon
import com.example.keios.ui.page.main.widget.GlassIconButton
import com.example.keios.ui.page.main.widget.GlassVariant
import com.example.keios.ui.page.main.widget.SheetContentColumn
import com.example.keios.ui.page.main.widget.SheetControlRow
import com.example.keios.ui.page.main.widget.SheetSectionCard
import com.example.keios.ui.page.main.widget.SheetDescriptionText
import com.example.keios.ui.page.main.widget.SnapshotWindowBottomSheet
import top.yukonga.miuix.kmp.basic.Switch

@Composable
internal fun OsShellSettingsSheet(
    show: Boolean,
    onDismissRequest: () -> Unit,
    persistInputEnabled: Boolean,
    onPersistInputEnabledChange: (Boolean) -> Unit,
    persistOutputEnabled: Boolean,
    onPersistOutputEnabledChange: (Boolean) -> Unit
) {
    SnapshotWindowBottomSheet(
        show = show,
        title = stringResource(R.string.os_shell_settings_sheet_title),
        onDismissRequest = onDismissRequest,
        startAction = {
            GlassIconButton(
                backdrop = null,
                variant = GlassVariant.Bar,
                icon = appLucideCloseIcon(),
                contentDescription = stringResource(R.string.common_close),
                onClick = onDismissRequest
            )
        }
    ) {
        SheetContentColumn(verticalSpacing = 10.dp) {
            SheetSectionCard(verticalSpacing = 10.dp) {
                SheetControlRow(
                    label = stringResource(R.string.os_shell_settings_persist_input_label),
                    summary = stringResource(R.string.os_shell_settings_persist_input_summary)
                ) {
                    Switch(
                        checked = persistInputEnabled,
                        onCheckedChange = onPersistInputEnabledChange
                    )
                }
                SheetControlRow(
                    label = stringResource(R.string.os_shell_settings_persist_output_label),
                    summary = stringResource(R.string.os_shell_settings_persist_output_summary)
                ) {
                    Switch(
                        checked = persistOutputEnabled,
                        onCheckedChange = onPersistOutputEnabledChange
                    )
                }
            }
            SheetDescriptionText(
                text = stringResource(R.string.os_shell_settings_desc)
            )
        }
    }
}
