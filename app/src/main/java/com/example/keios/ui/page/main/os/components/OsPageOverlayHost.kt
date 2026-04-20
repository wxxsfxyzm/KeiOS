package com.example.keios.ui.page.main.os.components

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.Composable
import com.example.keios.R
import com.example.keios.ui.page.main.os.OsGoogleSystemServiceConfig
import com.example.keios.ui.page.main.os.state.OsPageOverlayState
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
    overlayState: OsPageOverlayState,
    visibleCardsTitle: String,
    visibleCardsHint: String,
    visibleCards: Set<OsSectionCard>,
    applyCardVisibility: suspend (OsSectionCard, Boolean) -> Unit,
    visibleActivitiesTitle: String,
    visibleActivitiesDesc: String,
    activityShortcutCards: List<OsActivityShortcutCard>,
    defaultActivityCardTitle: String,
    cardTransferInProgress: Boolean,
    onExportAllActivityCards: () -> Unit,
    onImportAllActivityCards: () -> Unit,
    applyActivityCardVisibility: suspend (String, Boolean) -> Unit,
    visibleShellCardsTitle: String,
    visibleShellCardsDesc: String,
    shellRunnerVisible: Boolean,
    shellCommandCards: List<OsShellCommandCard>,
    onExportAllShellCards: () -> Unit,
    onImportAllShellCards: () -> Unit,
    applyShellCommandCardVisibility: suspend (String, Boolean) -> Unit,
    editShellCommandCardTitle: String,
    onShellCommandCardsChange: (List<OsShellCommandCard>) -> Unit,
    onRemoveShellCommandCardExpanded: (String) -> Unit,
    shellCardCommandRequiredToast: String,
    shellCardSavedToast: String,
    shellCardDeletedToast: String,
    shellCardDeleteDialogTitle: String,
    addActivityCardTitle: String,
    editActivityCardTitle: String,
    noMatchedResultsText: String,
    onActivityShortcutCardsChange: (List<OsActivityShortcutCard>) -> Unit,
    onRemoveActivityCardExpanded: (String) -> Unit,
    googleSystemServiceDefaults: OsGoogleSystemServiceConfig,
    googleSystemServiceDefaultTitle: String,
    googleSystemServiceDefaultIntentFlags: String,
    activityCardDeletedToast: String,
    activityCardDeleteDialogTitle: String
) {
    OsPageOverlaySheets(
        showCardManager = overlayState.showCardManager,
        visibleCardsTitle = visibleCardsTitle,
        sheetBackdrop = sheetBackdrop,
        cardsHintText = visibleCardsHint,
        visibleCards = visibleCards,
        onDismissCardManager = { overlayState.onShowCardManagerChange(false) },
        onCardVisibilityChange = { card, checked ->
            scope.launch { applyCardVisibility(card, checked) }
        },
        showActivityVisibilityManager = overlayState.showActivityVisibilityManager,
        visibleActivitiesTitle = visibleActivitiesTitle,
        activityHintText = visibleActivitiesDesc,
        activityShortcutCards = activityShortcutCards,
        defaultActivityCardTitle = defaultActivityCardTitle,
        cardTransferInProgress = cardTransferInProgress,
        onExportAllActivityCards = onExportAllActivityCards,
        onImportAllActivityCards = onImportAllActivityCards,
        onDismissActivityVisibilityManager = { overlayState.onShowActivityVisibilityManagerChange(false) },
        onActivityCardVisibilityChange = { cardId, checked ->
            scope.launch { applyActivityCardVisibility(cardId, checked) }
        },
        showShellCardVisibilityManager = overlayState.showShellCardVisibilityManager,
        visibleShellCardsTitle = visibleShellCardsTitle,
        visibleShellCardsDesc = visibleShellCardsDesc,
        shellRunnerVisible = shellRunnerVisible,
        onShellRunnerVisibilityChange = { checked ->
            scope.launch { applyCardVisibility(OsSectionCard.SHELL_RUNNER, checked) }
        },
        shellCommandCards = shellCommandCards,
        onExportAllShellCards = onExportAllShellCards,
        onImportAllShellCards = onImportAllShellCards,
        onDismissShellVisibilityManager = { overlayState.onShowShellCardVisibilityManagerChange(false) },
        onShellCommandCardVisibilityChange = { cardId, checked ->
            scope.launch { applyShellCommandCardVisibility(cardId, checked) }
        },
        showShellCommandCardEditor = overlayState.showShellCommandCardEditor,
        editShellCommandCardTitle = editShellCommandCardTitle,
        shellCommandCardDraft = overlayState.shellCommandCardDraft,
        onShellCommandCardDraftChange = overlayState.onShellCommandCardDraftChange,
        showShellCardDeleteAction = !overlayState.editingShellCommandCardId.isNullOrBlank(),
        onDeleteShellCommandCard = {
            val targetId = overlayState.editingShellCommandCardId.orEmpty().trim()
            if (targetId.isBlank()) {
                overlayState.onShowShellCommandCardEditorChange(false)
                overlayState.onShowShellCardDeleteConfirmChange(false)
                return@OsPageOverlaySheets
            }
            overlayState.onShowShellCardDeleteConfirmChange(true)
        },
        onDismissShellCommandCardEditor = {
            overlayState.onShowShellCommandCardEditorChange(false)
            overlayState.onShowShellCardDeleteConfirmChange(false)
        },
        onSaveShellCommandCard = {
            val targetId = overlayState.editingShellCommandCardId.orEmpty().trim()
            if (targetId.isBlank()) {
                Toast.makeText(context, shellCardCommandRequiredToast, Toast.LENGTH_SHORT).show()
                return@OsPageOverlaySheets
            }
            val updated = OsShellCommandCardStore.updateCard(
                cardId = targetId,
                title = overlayState.shellCommandCardDraft.title,
                subtitle = overlayState.shellCommandCardDraft.subtitle,
                command = overlayState.shellCommandCardDraft.command
            )
            if (updated == null) {
                Toast.makeText(context, shellCardCommandRequiredToast, Toast.LENGTH_SHORT).show()
                return@OsPageOverlaySheets
            }
            onShellCommandCardsChange(OsShellCommandCardStore.loadCards())
            Toast.makeText(context, shellCardSavedToast, Toast.LENGTH_SHORT).show()
            overlayState.onShowShellCommandCardEditorChange(false)
            overlayState.onShowShellCardDeleteConfirmChange(false)
        },
        showActivityShortcutEditor = overlayState.showActivityShortcutEditor,
        activityEditorTitle = if (overlayState.activityCardEditMode == OsActivityCardEditMode.Add) {
            addActivityCardTitle
        } else {
            editActivityCardTitle
        },
        activityShortcutDraft = overlayState.activityShortcutDraft,
        onActivityShortcutDraftChange = overlayState.onActivityShortcutDraftChange,
        onOpenActivitySuggestionSheet = { target ->
            overlayState.onGoogleSystemServiceSuggestionTargetChange(target)
            when (target) {
                ShortcutSuggestionField.PackageName -> {
                    overlayState.onGoogleSystemServicePackageSuggestionQueryChange("")
                }
                ShortcutSuggestionField.ClassName -> {
                    overlayState.onGoogleSystemServiceClassSuggestionQueryChange("")
                }
                else -> Unit
            }
            overlayState.onShowActivitySuggestionSheetChange(true)
        },
        showBuiltInActivityCardBadge = overlayState.editingActivityShortcutBuiltIn,
        showDeleteActivityAction = overlayState.activityCardEditMode == OsActivityCardEditMode.Edit &&
            !overlayState.editingActivityShortcutCardId.isNullOrBlank(),
        onDeleteActivityCard = {
            val targetId = overlayState.editingActivityShortcutCardId.orEmpty().trim()
            if (targetId.isBlank()) {
                overlayState.onShowActivityShortcutEditorChange(false)
                overlayState.onShowActivityCardDeleteConfirmChange(false)
                return@OsPageOverlaySheets
            }
            overlayState.onShowActivityCardDeleteConfirmChange(true)
        },
        onDismissActivityEditor = {
            overlayState.onShowActivityShortcutEditorChange(false)
            overlayState.onShowActivityCardDeleteConfirmChange(false)
            overlayState.onEditingActivityShortcutBuiltInChange(false)
        },
        onSaveActivityEditor = {
            val normalized = normalizeActivityShortcutConfig(
                config = overlayState.activityShortcutDraft,
                defaults = googleSystemServiceDefaults
            )
            val updatedCards = if (overlayState.activityCardEditMode == OsActivityCardEditMode.Add) {
                activityShortcutCards + OsActivityShortcutCard(
                    id = newOsActivityShortcutCardId(),
                    visible = true,
                    isBuiltInSample = false,
                    config = normalized
                )
            } else {
                val targetId = overlayState.editingActivityShortcutCardId
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
            overlayState.onShowActivityShortcutEditorChange(false)
            overlayState.onShowActivityCardDeleteConfirmChange(false)
            overlayState.onEditingActivityShortcutBuiltInChange(false)
        },
        showActivitySuggestionSheet = overlayState.showActivitySuggestionSheet,
        suggestionTarget = overlayState.googleSystemServiceSuggestionTarget,
        packageSuggestions = overlayState.googleSystemServicePackageSuggestions,
        packageSuggestionsLoading = overlayState.googleSystemServicePackageSuggestionsLoading,
        packageSuggestionQuery = overlayState.googleSystemServicePackageSuggestionQuery,
        onPackageSuggestionQueryChange = overlayState.onGoogleSystemServicePackageSuggestionQueryChange,
        classSuggestions = overlayState.googleSystemServiceClassSuggestions,
        classSuggestionsLoading = overlayState.googleSystemServiceClassSuggestionsLoading,
        classSuggestionQuery = overlayState.googleSystemServiceClassSuggestionQuery,
        onClassSuggestionQueryChange = overlayState.onGoogleSystemServiceClassSuggestionQueryChange,
        noMatchedResultsText = noMatchedResultsText,
        onDismissSuggestionSheet = { overlayState.onShowActivitySuggestionSheetChange(false) },
        onApplySuggestion = { suggestion: ShortcutSuggestionItem ->
            overlayState.onActivityShortcutDraftChange(
                applyGoogleSystemServiceSuggestion(
                    draft = overlayState.activityShortcutDraft,
                    target = overlayState.googleSystemServiceSuggestionTarget,
                    item = suggestion,
                    defaultIntentFlags = googleSystemServiceDefaultIntentFlags
                )
            )
            overlayState.onShowActivitySuggestionSheetChange(false)
        },
        onApplyExplicitActionRecommendation = {
            overlayState.onActivityShortcutDraftChange(
                overlayState.activityShortcutDraft.copy(intentAction = Intent.ACTION_VIEW)
            )
        },
        onApplyImplicitActionRecommendation = {
            overlayState.onActivityShortcutDraftChange(
                applyShortcutImplicitDefaults(
                    draft = overlayState.activityShortcutDraft,
                    defaultIntentFlags = googleSystemServiceDefaultIntentFlags
                )
            )
        },
        onApplyExplicitCategoryRecommendation = {
            overlayState.onActivityShortcutDraftChange(
                overlayState.activityShortcutDraft.copy(intentCategory = "")
            )
        },
        onApplyImplicitCategoryRecommendation = {
            overlayState.onActivityShortcutDraftChange(
                applyShortcutImplicitDefaults(
                    draft = overlayState.activityShortcutDraft,
                    defaultIntentFlags = googleSystemServiceDefaultIntentFlags
                )
            )
        },
        showShellCardDeleteConfirm = overlayState.showShellCardDeleteConfirm,
        shellCardDeleteDialogTitle = shellCardDeleteDialogTitle,
        shellCardDeleteDialogSummary = context.getString(
            R.string.os_shell_card_delete_dialog_summary,
            overlayState.shellCommandCardDraft.title.ifBlank {
                defaultOsShellCommandCardTitle(overlayState.shellCommandCardDraft.command)
            }
        ),
        onDismissShellCardDeleteConfirm = { overlayState.onShowShellCardDeleteConfirmChange(false) },
        onConfirmShellCardDelete = {
            val targetId = overlayState.editingShellCommandCardId.orEmpty().trim()
            overlayState.onShowShellCardDeleteConfirmChange(false)
            if (targetId.isBlank()) return@OsPageOverlaySheets
            onShellCommandCardsChange(OsShellCommandCardStore.deleteCard(targetId))
            onRemoveShellCommandCardExpanded(targetId)
            overlayState.onEditingShellCommandCardIdChange(null)
            overlayState.onShowShellCommandCardEditorChange(false)
            Toast.makeText(context, shellCardDeletedToast, Toast.LENGTH_SHORT).show()
        },
        showActivityCardDeleteConfirm = overlayState.showActivityCardDeleteConfirm,
        activityCardDeleteDialogTitle = activityCardDeleteDialogTitle,
        activityCardDeleteDialogSummary = context.getString(
            R.string.os_activity_card_delete_dialog_summary,
            overlayState.activityShortcutDraft.title.ifBlank { googleSystemServiceDefaultTitle }
        ),
        onDismissActivityCardDeleteConfirm = { overlayState.onShowActivityCardDeleteConfirmChange(false) },
        onConfirmActivityCardDelete = {
            val targetId = overlayState.editingActivityShortcutCardId.orEmpty().trim()
            overlayState.onShowActivityCardDeleteConfirmChange(false)
            if (targetId.isBlank()) return@OsPageOverlaySheets
            val updatedCards = activityShortcutCards.filterNot { card -> card.id == targetId }
            onActivityShortcutCardsChange(updatedCards)
            onRemoveActivityCardExpanded(targetId)
            OsActivityShortcutCardStore.saveCards(
                cards = updatedCards,
                defaults = googleSystemServiceDefaults
            )
            overlayState.onEditingActivityShortcutCardIdChange(null)
            overlayState.onShowActivityShortcutEditorChange(false)
            overlayState.onShowActivitySuggestionSheetChange(false)
            overlayState.onEditingActivityShortcutBuiltInChange(false)
            Toast.makeText(context, activityCardDeletedToast, Toast.LENGTH_SHORT).show()
        }
    )
}
