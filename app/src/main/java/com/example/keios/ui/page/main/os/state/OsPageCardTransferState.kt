package com.example.keios.ui.page.main.os.state

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.example.keios.R
import com.example.keios.ui.page.main.os.OsGoogleSystemServiceConfig
import com.example.keios.ui.page.main.os.shell.OsShellCommandCard
import com.example.keios.ui.page.main.os.shell.OsShellCommandCardStore
import com.example.keios.ui.page.main.os.shortcut.OsActivityShortcutCard
import com.example.keios.ui.page.main.os.shortcut.OsActivityShortcutCardStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal data class OsPageCardTransferState(
    val exportLauncher: ActivityResultLauncher<String>,
    val importLauncher: ActivityResultLauncher<Array<String>>
)

@Composable
internal fun rememberOsPageCardTransferState(
    context: Context,
    scope: CoroutineScope,
    overlayState: OsPageOverlayState,
    activityShortcutCards: List<OsActivityShortcutCard>,
    onActivityShortcutCardsChange: (List<OsActivityShortcutCard>) -> Unit,
    activityCardExpanded: SnapshotStateMap<String, Boolean>,
    shellCommandCards: List<OsShellCommandCard>,
    onShellCommandCardsChange: (List<OsShellCommandCard>) -> Unit,
    shellCommandCardExpanded: SnapshotStateMap<String, Boolean>,
    googleSystemServiceDefaults: OsGoogleSystemServiceConfig,
    googleSettingsBuiltInSampleDefaults: OsGoogleSystemServiceConfig,
    cardImportFailedWithReason: String,
    exportSuccessText: String
): OsPageCardTransferState {
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        val content = overlayState.pendingExportContent
        if (uri == null || content.isNullOrBlank()) return@rememberLauncherForActivityResult
        runCatching {
            context.contentResolver.openOutputStream(uri)?.bufferedWriter().use { writer ->
                writer?.write(content)
            }
        }.onSuccess {
            Toast.makeText(context, exportSuccessText, Toast.LENGTH_SHORT).show()
        }.onFailure {
            Toast.makeText(
                context,
                context.getString(R.string.common_export_failed_with_reason, it.javaClass.simpleName),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        val target = overlayState.pendingImportTarget
        overlayState.onPendingImportTargetChange(null)
        if (uri == null || target == null) {
            overlayState.onCardTransferInProgressChange(false)
            return@rememberLauncherForActivityResult
        }
        scope.launch {
            runCatching {
                val raw = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.bufferedReader().use { reader ->
                        reader?.readText().orEmpty()
                    }
                }
                when (target) {
                    OsCardImportTarget.Activity -> {
                        val result = OsActivityShortcutCardStore.importCardsFromJsonMerged(
                            raw = raw,
                            defaults = googleSystemServiceDefaults,
                            builtInSampleDefaults = googleSettingsBuiltInSampleDefaults
                        )
                        onActivityShortcutCardsChange(result.cards)
                        val validIds = result.cards.mapTo(mutableSetOf()) { it.id }
                        activityCardExpanded.keys.retainAll(validIds)
                        if (!validIds.contains(overlayState.editingActivityShortcutCardId.orEmpty())) {
                            overlayState.onShowActivityShortcutEditorChange(false)
                            overlayState.onShowActivityCardDeleteConfirmChange(false)
                            overlayState.onEditingActivityShortcutCardIdChange(null)
                        }
                        Toast.makeText(
                            context,
                            context.getString(
                                R.string.os_activity_card_toast_imported_summary,
                                result.addedCount,
                                result.updatedCount,
                                result.unchangedCount
                            ),
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    OsCardImportTarget.Shell -> {
                        val result = OsShellCommandCardStore.importCardsFromJsonMerged(raw)
                        onShellCommandCardsChange(result.cards)
                        val validIds = result.cards.mapTo(mutableSetOf()) { it.id }
                        shellCommandCardExpanded.keys.retainAll(validIds)
                        if (!validIds.contains(overlayState.editingShellCommandCardId.orEmpty())) {
                            overlayState.onShowShellCommandCardEditorChange(false)
                            overlayState.onShowShellCardDeleteConfirmChange(false)
                            overlayState.onEditingShellCommandCardIdChange(null)
                        }
                        Toast.makeText(
                            context,
                            context.getString(
                                R.string.os_shell_card_toast_imported_summary,
                                result.addedCount,
                                result.updatedCount,
                                result.unchangedCount
                            ),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }.onFailure { error ->
                Toast.makeText(
                    context,
                    String.format(
                        cardImportFailedWithReason,
                        error.message ?: error.javaClass.simpleName
                    ),
                    Toast.LENGTH_SHORT
                ).show()
            }
            overlayState.onCardTransferInProgressChange(false)
        }
    }

    return OsPageCardTransferState(
        exportLauncher = exportLauncher,
        importLauncher = importLauncher
    )
}
