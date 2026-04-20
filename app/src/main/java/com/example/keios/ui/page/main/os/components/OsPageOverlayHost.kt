package com.example.keios.ui.page.main.os.components

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.Composable
import com.example.keios.R
import com.example.keios.ui.page.main.os.OsGoogleSystemServiceConfig
import com.example.keios.ui.page.main.os.OsSectionCard
import com.example.keios.ui.page.main.os.shell.OsShellCommandCard
import com.example.keios.ui.page.main.os.shell.OsShellCommandCardStore
import com.example.keios.ui.page.main.os.shell.defaultOsShellCommandCardTitle
import com.example.keios.ui.page.main.os.shortcut.OsActivityCardEditMode
import com.example.keios.ui.page.main.os.shortcut.OsActivityShortcutCard
import com.example.keios.ui.page.main.os.shortcut.OsActivityShortcutCardStore
import com.example.keios.ui.page.main.os.shortcut.ShortcutActivityClassOption
import com.example.keios.ui.page.main.os.shortcut.ShortcutInstalledAppOption
import com.example.keios.ui.page.main.os.shortcut.ShortcutSuggestionField
import com.example.keios.ui.page.main.os.shortcut.ShortcutSuggestionItem
import com.example.keios.ui.page.main.os.shortcut.applyGoogleSystemServiceSuggestion
import com.example.keios.ui.page.main.os.shortcut.applyShortcutImplicitDefaults
import com.example.keios.ui.page.main.os.shortcut.newOsActivityShortcutCardId
import com.example.keios.ui.page.main.os.shortcut.normalizeActivityShortcutConfig
import com.kyant.backdrop.backdrops.LayerBackdrop
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
internal fun OsPageOverlayHost(
    context: Context,
    scope: CoroutineScope,
    sheetBackdrop: LayerBackdrop,
    showCardManager: Boolean,
    visibleCardsTitle: String,
    visibleCardsHint: String,
    visibleCards: Set<OsSectionCard>,
    onShowCardManagerChange: (Boolean) -> Unit,
    applyCardVisibility: suspend (OsSectionCard, Boolean) -> Unit,
    showActivityVisibilityManager: Boolean,
    visibleActivitiesTitle: String,
    visibleActivitiesDesc: String,
    activityShortcutCards: List<OsActivityShortcutCard>,
    defaultActivityCardTitle: String,
    cardTransferInProgress: Boolean,
    onExportAllActivityCards: () -> Unit,
    onImportAllActivityCards: () -> Unit,
    onShowActivityVisibilityManagerChange: (Boolean) -> Unit,
    applyActivityCardVisibility: suspend (String, Boolean) -> Unit,
    showShellCardVisibilityManager: Boolean,
    visibleShellCardsTitle: String,
    visibleShellCardsDesc: String,
    shellRunnerVisible: Boolean,
    onShowShellCardVisibilityManagerChange: (Boolean) -> Unit,
    shellCommandCards: List<OsShellCommandCard>,
    onExportAllShellCards: () -> Unit,
    onImportAllShellCards: () -> Unit,
    applyShellCommandCardVisibility: suspend (String, Boolean) -> Unit,
    showShellCommandCardEditor: Boolean,
    editShellCommandCardTitle: String,
    shellCommandCardDraft: OsShellCommandCard,
    onShellCommandCardDraftChange: (OsShellCommandCard) -> Unit,
    editingShellCommandCardId: String?,
    onEditingShellCommandCardIdChange: (String?) -> Unit,
    onShowShellCommandCardEditorChange: (Boolean) -> Unit,
    showShellCardDeleteConfirm: Boolean,
    onShowShellCardDeleteConfirmChange: (Boolean) -> Unit,
    onShellCommandCardsChange: (List<OsShellCommandCard>) -> Unit,
    onRemoveShellCommandCardExpanded: (String) -> Unit,
    shellCardCommandRequiredToast: String,
    shellCardSavedToast: String,
    shellCardDeletedToast: String,
    shellCardDeleteDialogTitle: String,
    showActivityShortcutEditor: Boolean,
    activityCardEditMode: OsActivityCardEditMode,
    addActivityCardTitle: String,
    editActivityCardTitle: String,
    activityShortcutDraft: OsGoogleSystemServiceConfig,
    onActivityShortcutDraftChange: (OsGoogleSystemServiceConfig) -> Unit,
    showActivitySuggestionSheet: Boolean,
    onShowActivitySuggestionSheetChange: (Boolean) -> Unit,
    googleSystemServiceSuggestionTarget: ShortcutSuggestionField,
    onGoogleSystemServiceSuggestionTargetChange: (ShortcutSuggestionField) -> Unit,
    googleSystemServicePackageSuggestions: List<ShortcutInstalledAppOption>,
    googleSystemServicePackageSuggestionsLoading: Boolean,
    googleSystemServicePackageSuggestionQuery: String,
    onGoogleSystemServicePackageSuggestionQueryChange: (String) -> Unit,
    googleSystemServiceClassSuggestions: List<ShortcutActivityClassOption>,
    googleSystemServiceClassSuggestionsLoading: Boolean,
    googleSystemServiceClassSuggestionQuery: String,
    onGoogleSystemServiceClassSuggestionQueryChange: (String) -> Unit,
    noMatchedResultsText: String,
    editingActivityShortcutCardId: String?,
    onEditingActivityShortcutCardIdChange: (String?) -> Unit,
    editingActivityShortcutBuiltIn: Boolean,
    onEditingActivityShortcutBuiltInChange: (Boolean) -> Unit,
    onShowActivityShortcutEditorChange: (Boolean) -> Unit,
    showActivityCardDeleteConfirm: Boolean,
    onShowActivityCardDeleteConfirmChange: (Boolean) -> Unit,
    onActivityShortcutCardsChange: (List<OsActivityShortcutCard>) -> Unit,
    onRemoveActivityCardExpanded: (String) -> Unit,
    googleSystemServiceDefaults: OsGoogleSystemServiceConfig,
    googleSystemServiceDefaultTitle: String,
    googleSystemServiceDefaultIntentFlags: String,
    activityCardDeletedToast: String,
    activityCardDeleteDialogTitle: String
) {
    OsPageOverlaySheets(
        showCardManager = showCardManager,
        visibleCardsTitle = visibleCardsTitle,
        sheetBackdrop = sheetBackdrop,
        cardsHintText = visibleCardsHint,
        visibleCards = visibleCards,
        onDismissCardManager = { onShowCardManagerChange(false) },
        onCardVisibilityChange = { card, checked ->
            scope.launch { applyCardVisibility(card, checked) }
        },
        showActivityVisibilityManager = showActivityVisibilityManager,
        visibleActivitiesTitle = visibleActivitiesTitle,
        activityHintText = visibleActivitiesDesc,
        activityShortcutCards = activityShortcutCards,
        defaultActivityCardTitle = defaultActivityCardTitle,
        cardTransferInProgress = cardTransferInProgress,
        onExportAllActivityCards = onExportAllActivityCards,
        onImportAllActivityCards = onImportAllActivityCards,
        onDismissActivityVisibilityManager = { onShowActivityVisibilityManagerChange(false) },
        onActivityCardVisibilityChange = { cardId, checked ->
            scope.launch { applyActivityCardVisibility(cardId, checked) }
        },
        showShellCardVisibilityManager = showShellCardVisibilityManager,
        visibleShellCardsTitle = visibleShellCardsTitle,
        visibleShellCardsDesc = visibleShellCardsDesc,
        shellRunnerVisible = shellRunnerVisible,
        onShellRunnerVisibilityChange = { checked ->
            scope.launch { applyCardVisibility(OsSectionCard.SHELL_RUNNER, checked) }
        },
        shellCommandCards = shellCommandCards,
        onExportAllShellCards = onExportAllShellCards,
        onImportAllShellCards = onImportAllShellCards,
        onDismissShellVisibilityManager = { onShowShellCardVisibilityManagerChange(false) },
        onShellCommandCardVisibilityChange = { cardId, checked ->
            scope.launch { applyShellCommandCardVisibility(cardId, checked) }
        },
        showShellCommandCardEditor = showShellCommandCardEditor,
        editShellCommandCardTitle = editShellCommandCardTitle,
        shellCommandCardDraft = shellCommandCardDraft,
        onShellCommandCardDraftChange = onShellCommandCardDraftChange,
        showShellCardDeleteAction = !editingShellCommandCardId.isNullOrBlank(),
        onDeleteShellCommandCard = {
            val targetId = editingShellCommandCardId.orEmpty().trim()
            if (targetId.isBlank()) {
                onShowShellCommandCardEditorChange(false)
                onShowShellCardDeleteConfirmChange(false)
                return@OsPageOverlaySheets
            }
            onShowShellCardDeleteConfirmChange(true)
        },
        onDismissShellCommandCardEditor = {
            onShowShellCommandCardEditorChange(false)
            onShowShellCardDeleteConfirmChange(false)
        },
        onSaveShellCommandCard = {
            val targetId = editingShellCommandCardId.orEmpty().trim()
            if (targetId.isBlank()) {
                Toast.makeText(context, shellCardCommandRequiredToast, Toast.LENGTH_SHORT).show()
                return@OsPageOverlaySheets
            }
            val updated = OsShellCommandCardStore.updateCard(
                cardId = targetId,
                title = shellCommandCardDraft.title,
                subtitle = shellCommandCardDraft.subtitle,
                command = shellCommandCardDraft.command
            )
            if (updated == null) {
                Toast.makeText(context, shellCardCommandRequiredToast, Toast.LENGTH_SHORT).show()
                return@OsPageOverlaySheets
            }
            onShellCommandCardsChange(OsShellCommandCardStore.loadCards())
            Toast.makeText(context, shellCardSavedToast, Toast.LENGTH_SHORT).show()
            onShowShellCommandCardEditorChange(false)
            onShowShellCardDeleteConfirmChange(false)
        },
        showActivityShortcutEditor = showActivityShortcutEditor,
        activityEditorTitle = if (activityCardEditMode == OsActivityCardEditMode.Add) {
            addActivityCardTitle
        } else {
            editActivityCardTitle
        },
        activityShortcutDraft = activityShortcutDraft,
        onActivityShortcutDraftChange = onActivityShortcutDraftChange,
        onOpenActivitySuggestionSheet = { target ->
            onGoogleSystemServiceSuggestionTargetChange(target)
            when (target) {
                ShortcutSuggestionField.PackageName -> {
                    onGoogleSystemServicePackageSuggestionQueryChange("")
                }
                ShortcutSuggestionField.ClassName -> {
                    onGoogleSystemServiceClassSuggestionQueryChange("")
                }
                else -> Unit
            }
            onShowActivitySuggestionSheetChange(true)
        },
        showBuiltInActivityCardBadge = editingActivityShortcutBuiltIn,
        showDeleteActivityAction = activityCardEditMode == OsActivityCardEditMode.Edit &&
            !editingActivityShortcutCardId.isNullOrBlank(),
        onDeleteActivityCard = {
            val targetId = editingActivityShortcutCardId.orEmpty().trim()
            if (targetId.isBlank()) {
                onShowActivityShortcutEditorChange(false)
                onShowActivityCardDeleteConfirmChange(false)
                return@OsPageOverlaySheets
            }
            onShowActivityCardDeleteConfirmChange(true)
        },
        onDismissActivityEditor = {
            onShowActivityShortcutEditorChange(false)
            onShowActivityCardDeleteConfirmChange(false)
            onEditingActivityShortcutBuiltInChange(false)
        },
        onSaveActivityEditor = {
            val normalized = normalizeActivityShortcutConfig(
                config = activityShortcutDraft,
                defaults = googleSystemServiceDefaults
            )
            val updatedCards = if (activityCardEditMode == OsActivityCardEditMode.Add) {
                activityShortcutCards + OsActivityShortcutCard(
                    id = newOsActivityShortcutCardId(),
                    visible = true,
                    isBuiltInSample = false,
                    config = normalized
                )
            } else {
                val targetId = editingActivityShortcutCardId
                if (targetId.isNullOrBlank()) {
                    activityShortcutCards + OsActivityShortcutCard(
                        id = newOsActivityShortcutCardId(),
                        visible = true,
                        isBuiltInSample = false,
                        config = normalized
                    )
                } else {
                    activityShortcutCards.map { card ->
                        if (card.id == targetId) card.copy(config = normalized) else card
                    }
                }
            }
            onActivityShortcutCardsChange(updatedCards)
            OsActivityShortcutCardStore.saveCards(
                cards = updatedCards,
                defaults = googleSystemServiceDefaults
            )
            Toast.makeText(
                context,
                context.getString(R.string.os_google_system_service_toast_saved),
                Toast.LENGTH_SHORT
            ).show()
            onShowActivityShortcutEditorChange(false)
            onShowActivityCardDeleteConfirmChange(false)
            onEditingActivityShortcutBuiltInChange(false)
        },
        showActivitySuggestionSheet = showActivitySuggestionSheet,
        suggestionTarget = googleSystemServiceSuggestionTarget,
        packageSuggestions = googleSystemServicePackageSuggestions,
        packageSuggestionsLoading = googleSystemServicePackageSuggestionsLoading,
        packageSuggestionQuery = googleSystemServicePackageSuggestionQuery,
        onPackageSuggestionQueryChange = onGoogleSystemServicePackageSuggestionQueryChange,
        classSuggestions = googleSystemServiceClassSuggestions,
        classSuggestionsLoading = googleSystemServiceClassSuggestionsLoading,
        classSuggestionQuery = googleSystemServiceClassSuggestionQuery,
        onClassSuggestionQueryChange = onGoogleSystemServiceClassSuggestionQueryChange,
        noMatchedResultsText = noMatchedResultsText,
        onDismissSuggestionSheet = { onShowActivitySuggestionSheetChange(false) },
        onApplySuggestion = { suggestion: ShortcutSuggestionItem ->
            onActivityShortcutDraftChange(
                applyGoogleSystemServiceSuggestion(
                    draft = activityShortcutDraft,
                    target = googleSystemServiceSuggestionTarget,
                    item = suggestion,
                    defaultIntentFlags = googleSystemServiceDefaultIntentFlags
                )
            )
            onShowActivitySuggestionSheetChange(false)
        },
        onApplyExplicitActionRecommendation = {
            onActivityShortcutDraftChange(activityShortcutDraft.copy(intentAction = Intent.ACTION_VIEW))
        },
        onApplyImplicitActionRecommendation = {
            onActivityShortcutDraftChange(
                applyShortcutImplicitDefaults(
                    draft = activityShortcutDraft,
                    defaultIntentFlags = googleSystemServiceDefaultIntentFlags
                )
            )
        },
        onApplyExplicitCategoryRecommendation = {
            onActivityShortcutDraftChange(activityShortcutDraft.copy(intentCategory = ""))
        },
        onApplyImplicitCategoryRecommendation = {
            onActivityShortcutDraftChange(
                applyShortcutImplicitDefaults(
                    draft = activityShortcutDraft,
                    defaultIntentFlags = googleSystemServiceDefaultIntentFlags
                )
            )
        },
        showShellCardDeleteConfirm = showShellCardDeleteConfirm,
        shellCardDeleteDialogTitle = shellCardDeleteDialogTitle,
        shellCardDeleteDialogSummary = context.getString(
            R.string.os_shell_card_delete_dialog_summary,
            shellCommandCardDraft.title.ifBlank {
                defaultOsShellCommandCardTitle(shellCommandCardDraft.command)
            }
        ),
        onDismissShellCardDeleteConfirm = { onShowShellCardDeleteConfirmChange(false) },
        onConfirmShellCardDelete = {
            val targetId = editingShellCommandCardId.orEmpty().trim()
            onShowShellCardDeleteConfirmChange(false)
            if (targetId.isBlank()) return@OsPageOverlaySheets
            onShellCommandCardsChange(OsShellCommandCardStore.deleteCard(targetId))
            onRemoveShellCommandCardExpanded(targetId)
            onEditingShellCommandCardIdChange(null)
            onShowShellCommandCardEditorChange(false)
            Toast.makeText(context, shellCardDeletedToast, Toast.LENGTH_SHORT).show()
        },
        showActivityCardDeleteConfirm = showActivityCardDeleteConfirm,
        activityCardDeleteDialogTitle = activityCardDeleteDialogTitle,
        activityCardDeleteDialogSummary = context.getString(
            R.string.os_activity_card_delete_dialog_summary,
            activityShortcutDraft.title.ifBlank { googleSystemServiceDefaultTitle }
        ),
        onDismissActivityCardDeleteConfirm = { onShowActivityCardDeleteConfirmChange(false) },
        onConfirmActivityCardDelete = {
            val targetId = editingActivityShortcutCardId.orEmpty().trim()
            onShowActivityCardDeleteConfirmChange(false)
            if (targetId.isBlank()) return@OsPageOverlaySheets
            val updatedCards = activityShortcutCards.filterNot { card -> card.id == targetId }
            onActivityShortcutCardsChange(updatedCards)
            onRemoveActivityCardExpanded(targetId)
            OsActivityShortcutCardStore.saveCards(
                cards = updatedCards,
                defaults = googleSystemServiceDefaults
            )
            onEditingActivityShortcutCardIdChange(null)
            onShowActivityShortcutEditorChange(false)
            onShowActivitySuggestionSheetChange(false)
            onEditingActivityShortcutBuiltInChange(false)
            Toast.makeText(context, activityCardDeletedToast, Toast.LENGTH_SHORT).show()
        }
    )
}
