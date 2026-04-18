package com.example.keios.ui.page.main

import com.example.keios.R
import com.example.keios.feature.github.data.remote.GitHubVersionUtils
import com.example.keios.feature.github.model.GitHubTrackedApp

internal class GitHubTrackActions(
    private val env: GitHubPageActionEnvironment,
    private val refreshActions: GitHubRefreshActions
) {
    private val state get() = env.state
    private val packageNamePattern = Regex("""^[A-Za-z][A-Za-z0-9_]*(\.[A-Za-z0-9_]+)+$""")

    fun openTrackSheetForAdd() {
        state.resetTrackEditor()
        state.showAddSheet = true
    }

    fun openTrackSheetForEdit(item: GitHubTrackedApp) {
        state.editingTrackedItem = item
        state.repoUrlInput = item.repoUrl
        state.packageNameInput = item.packageName
        state.selectedApp = state.appList.firstOrNull {
            it.packageName.equals(item.packageName, ignoreCase = true)
        }
        state.appSearch = ""
        state.pickerExpanded = false
        state.preferPreReleaseInput = item.preferPreRelease
        state.alwaysShowLatestReleaseDownloadButtonInput = item.alwaysShowLatestReleaseDownloadButton
        state.showAddSheet = true
    }

    fun dismissTrackSheet() {
        state.dismissTrackSheet()
    }

    fun requestDeleteEditingItem() {
        state.pendingDeleteItem = state.editingTrackedItem
        state.dismissTrackSheet()
    }

    fun applyTrackSheet() {
        val parsed = GitHubVersionUtils.parseOwnerRepo(state.repoUrlInput)
        if (parsed == null) {
            env.toast(R.string.github_toast_fill_repo_and_select_app)
            return
        }
        val manualPackage = state.packageNameInput.trim()
        val resolvedPackageName = manualPackage
        if (resolvedPackageName.isNotBlank() && !packageNamePattern.matches(resolvedPackageName)) {
            env.toast(R.string.github_toast_invalid_package_name)
            return
        }
        val matchedInstalledApp = resolvedPackageName
            .takeIf { it.isNotBlank() }
            ?.let { packageName ->
                state.appList.firstOrNull { item ->
                    item.packageName.equals(packageName, ignoreCase = true)
                }
            }
        val resolvedAppLabel = when {
            matchedInstalledApp != null -> matchedInstalledApp.label
            resolvedPackageName.isNotBlank() -> resolvedPackageName
            else -> "${parsed.first}/${parsed.second}"
        }
        val newItem = GitHubTrackedApp(
            repoUrl = state.repoUrlInput.trim(),
            owner = parsed.first,
            repo = parsed.second,
            packageName = resolvedPackageName,
            appLabel = resolvedAppLabel,
            preferPreRelease = state.preferPreReleaseInput,
            alwaysShowLatestReleaseDownloadButton = state.alwaysShowLatestReleaseDownloadButtonInput
        )
        val editing = state.editingTrackedItem
        if (editing == null) {
            if (state.trackedItems.any { it.id == newItem.id }) {
                env.toast(R.string.github_toast_track_exists)
                return
            }
            state.trackedItems.add(newItem)
            env.saveTrackedItems(refreshTrackIds = setOf(newItem.id))
            env.toast(R.string.github_toast_track_added)
        } else {
            val duplicate = state.trackedItems.any { it.id == newItem.id && it.id != editing.id }
            if (duplicate) {
                env.toast(R.string.github_toast_track_exists)
                return
            }
            val index = state.trackedItems.indexOfFirst { it.id == editing.id }
            if (index >= 0) {
                state.trackedItems[index] = newItem
            } else {
                state.trackedItems.add(newItem)
            }
            if (editing.id != newItem.id) {
                state.checkStates.remove(editing.id)
                state.trackedCardExpanded.remove(editing.id)
                state.clearAssetUiState(editing.id)
            }
            env.saveTrackedItems(refreshTrackIds = setOf(newItem.id))
            env.toast(R.string.github_toast_track_updated)
        }
        state.dismissTrackSheet()
    }

    fun confirmDeletePendingItem() {
        if (state.deleteInProgress) return
        state.pendingDeleteItem?.let { deleting ->
            state.deleteInProgress = true
            try {
                refreshActions.cancelRefreshAll()
                state.trackedItems.remove(deleting)
                state.checkStates.remove(deleting.id)
                state.trackedCardExpanded.remove(deleting.id)
                state.clearAssetUiState(deleting.id)
                env.saveTrackedItems()
                refreshActions.persistCheckCache()
                env.toast(R.string.github_toast_track_deleted, deleting.appLabel)
            } finally {
                state.deleteInProgress = false
            }
        }
        state.pendingDeleteItem = null
    }
}
