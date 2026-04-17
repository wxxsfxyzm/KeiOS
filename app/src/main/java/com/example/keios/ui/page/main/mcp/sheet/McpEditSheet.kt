package com.example.keios.ui.page.main

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.dp
import com.example.keios.R
import com.example.keios.mcp.McpServerManager
import com.example.keios.ui.page.main.widget.GlassIconButton
import com.example.keios.ui.page.main.widget.GlassSearchField
import com.example.keios.ui.page.main.widget.GlassTextButton
import com.example.keios.ui.page.main.widget.GlassVariant
import com.example.keios.ui.page.main.widget.SheetActionGroup
import com.example.keios.ui.page.main.widget.SheetChoiceCard
import com.example.keios.ui.page.main.widget.SheetContentColumn
import com.example.keios.ui.page.main.widget.SheetControlRow
import com.example.keios.ui.page.main.widget.SheetSectionCard
import com.example.keios.ui.page.main.widget.SheetSectionTitle
import com.example.keios.ui.page.main.widget.StatusLabelText
import com.example.keios.ui.page.main.widget.SnapshotWindowBottomSheet
import com.kyant.backdrop.backdrops.LayerBackdrop
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.Ok

@Composable
internal fun McpEditServiceSheet(
    show: Boolean,
    backdrop: LayerBackdrop,
    serverName: String,
    onServerNameChange: (String) -> Unit,
    serverNameFieldWidth: androidx.compose.ui.unit.Dp,
    portText: String,
    onPortTextChange: (String) -> Unit,
    portFieldWidth: androidx.compose.ui.unit.Dp,
    allowExternal: Boolean,
    onAllowExternalChange: (Boolean) -> Unit,
    mcpServerManager: McpServerManager,
    unknownText: String,
    onDismissRequest: () -> Unit,
    onShowResetTokenConfirm: () -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    SnapshotWindowBottomSheet(
        show = show,
        title = stringResource(R.string.mcp_sheet_edit_service_title),
        onDismissRequest = onDismissRequest,
        startAction = {
            GlassIconButton(
                backdrop = backdrop,
                variant = GlassVariant.Bar,
                icon = MiuixIcons.Regular.Close,
                contentDescription = stringResource(R.string.common_close),
                onClick = onDismissRequest
            )
        },
        endAction = {
            GlassIconButton(
                backdrop = backdrop,
                variant = GlassVariant.Bar,
                icon = MiuixIcons.Regular.Ok,
                contentDescription = stringResource(R.string.common_save),
                onClick = {
                    val port = portText.toIntOrNull()
                    if (port == null) {
                        Toast.makeText(context, context.getString(R.string.common_port_invalid), Toast.LENGTH_SHORT).show()
                        return@GlassIconButton
                    }
                    mcpServerManager.updateServerName(serverName)
                    mcpServerManager.updatePort(port).onFailure {
                        Toast.makeText(
                            context,
                            context.getString(
                                R.string.common_save_failed_with_reason,
                                it.message ?: unknownText
                            ),
                            Toast.LENGTH_SHORT
                        ).show()
                        return@GlassIconButton
                    }
                    mcpServerManager.updateAllowExternal(allowExternal).onFailure {
                        Toast.makeText(
                            context,
                            context.getString(
                                R.string.common_save_failed_with_reason,
                                it.message ?: unknownText
                            ),
                            Toast.LENGTH_SHORT
                        ).show()
                        return@GlassIconButton
                    }
                    Toast.makeText(
                        context,
                        context.getString(R.string.mcp_toast_saved_requires_restart),
                        Toast.LENGTH_SHORT
                    ).show()
                    onDismissRequest()
                }
            )
        }
    ) {
        SheetContentColumn {
            SheetSectionTitle(stringResource(R.string.mcp_sheet_section_basic))
            SheetSectionCard {
                SheetControlRow(label = stringResource(R.string.mcp_overview_label_service_name)) {
                    GlassSearchField(
                        value = serverName,
                        onValueChange = onServerNameChange,
                        label = stringResource(R.string.mcp_input_service_name_hint),
                        backdrop = backdrop,
                        variant = GlassVariant.SheetInput,
                        singleLine = true,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(serverNameFieldWidth)
                    )
                }
                SheetControlRow(label = stringResource(R.string.mcp_sheet_label_service_port)) {
                    GlassSearchField(
                        value = portText,
                        onValueChange = { onPortTextChange(it.filter(Char::isDigit).take(5)) },
                        label = stringResource(R.string.mcp_input_port_hint),
                        backdrop = backdrop,
                        variant = GlassVariant.SheetInput,
                        singleLine = true,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(portFieldWidth)
                    )
                }
            }
            SheetSectionTitle(stringResource(R.string.mcp_sheet_section_network_access))
            SheetActionGroup {
                McpNetworkModeOption(
                    title = stringResource(R.string.mcp_network_mode_local_only_short),
                    summary = stringResource(R.string.mcp_network_mode_local_only_summary),
                    selected = !allowExternal,
                    onClick = { onAllowExternalChange(false) }
                )
                McpNetworkModeOption(
                    title = stringResource(R.string.mcp_network_mode_lan_short),
                    summary = stringResource(R.string.mcp_network_mode_lan_summary),
                    selected = allowExternal,
                    onClick = { onAllowExternalChange(true) }
                )
            }
            SheetSectionTitle(
                text = stringResource(R.string.common_danger_zone),
                danger = true
            )
            SheetSectionCard {
                GlassTextButton(
                    backdrop = backdrop,
                    variant = GlassVariant.SheetDangerAction,
                    text = stringResource(R.string.mcp_action_reset_token),
                    textColor = MiuixTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onShowResetTokenConfirm
                )
            }
        }
    }
}

@Composable
private fun McpNetworkModeOption(
    title: String,
    summary: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val selectedColor = androidx.compose.ui.graphics.Color(0xFF22C55E)
    SheetChoiceCard(
        title = title,
        summary = summary,
        selected = selected,
        onSelect = onClick,
        accentColor = selectedColor,
        selectedLabel = StatusLabelText.Activated
    )
}
