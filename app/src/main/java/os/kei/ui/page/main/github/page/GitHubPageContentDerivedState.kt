package os.kei.ui.page.main.github.page

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import os.kei.ui.page.main.github.GitHubSortMode
import os.kei.ui.page.main.github.section.GitHubOverviewMetrics
import kotlinx.coroutines.delay

private const val pendingShareImportCardVisibleWindowMs = 90_000L
private const val pendingShareImportCardTickMs = 15_000L

internal data class GitHubPageContentDerivedState(
    val trackedUi: GitHubPageDerivedState,
    val appLastUpdatedAtByTrackId: Map<String, Long>,
    val pendingShareImportRepoOverlapCount: Int,
    val showPendingShareImportCard: Boolean
)

@Composable
internal fun rememberGitHubPageContentDerivedState(
    state: GitHubPageState
): GitHubPageContentDerivedState {
    val trackedUi by remember(state) {
        derivedStateOf {
            val filteredTracked = state.trackedItems.filter { item ->
                state.trackedSearch.isBlank() ||
                    item.owner.contains(state.trackedSearch, ignoreCase = true) ||
                    item.repo.contains(state.trackedSearch, ignoreCase = true) ||
                    item.appLabel.contains(state.trackedSearch, ignoreCase = true) ||
                    item.packageName.contains(state.trackedSearch, ignoreCase = true)
            }
            val isSortUpdatable: (os.kei.feature.github.model.GitHubTrackedApp) -> Boolean = { item ->
                item.alwaysShowLatestReleaseDownloadButton || state.checkStates[item.id]?.hasUpdate == true
            }
            val sortedTracked = when (state.sortMode) {
                GitHubSortMode.UpdateFirst -> filteredTracked.sortedWith(
                    compareByDescending<os.kei.feature.github.model.GitHubTrackedApp> {
                        isSortUpdatable(it)
                    }
                        .thenByDescending { state.checkStates[it.id]?.hasPreReleaseUpdate == true }
                        .thenBy { it.appLabel.lowercase() }
                )
                GitHubSortMode.NameAsc -> filteredTracked.sortedBy { it.appLabel.lowercase() }
                GitHubSortMode.PreReleaseFirst -> filteredTracked.sortedWith(
                    compareByDescending<os.kei.feature.github.model.GitHubTrackedApp> {
                        state.checkStates[it.id]?.isPreRelease == true
                    }
                        .thenByDescending { isSortUpdatable(it) }
                        .thenBy { it.appLabel.lowercase() }
                )
            }
            val trackedCount = state.trackedItems.size
            val updatableCount = state.trackedItems.count { state.checkStates[it.id]?.hasUpdate == true }
            val preReleaseCount = state.trackedItems.count { state.checkStates[it.id]?.isPreRelease == true }
            val preReleaseUpdateCount =
                state.trackedItems.count { state.checkStates[it.id]?.hasPreReleaseUpdate == true }
            val failedCount = state.trackedItems.count { state.checkStates[it.id]?.failed == true }
            val stableLatestCount = state.trackedItems.count {
                val itemState = state.checkStates[it.id]
                itemState?.hasUpdate == false && itemState.isPreRelease.not()
            }
            GitHubPageDerivedState(
                filteredTracked = filteredTracked,
                sortedTracked = sortedTracked,
                overviewMetrics = GitHubOverviewMetrics(
                    trackedCount = trackedCount,
                    updatableCount = updatableCount,
                    stableLatestCount = stableLatestCount,
                    preReleaseCount = preReleaseCount,
                    preReleaseUpdateCount = preReleaseUpdateCount,
                    failedCount = failedCount
                )
            )
        }
    }
    val appLastUpdatedAtByTrackId by remember {
        derivedStateOf {
            buildMap<String, Long> {
                val appUpdatedAtByPackage = buildMap<String, Long> {
                    state.trackedFirstInstallAtByPackage.forEach { (packageName, firstInstallAtMillis) ->
                        if (packageName.isNotBlank() && firstInstallAtMillis > 0L) {
                            put(packageName, firstInstallAtMillis)
                        }
                    }
                    state.appList
                        .filter { it.packageName.isNotBlank() && it.lastUpdateTimeMs > 0L }
                        .forEach { put(it.packageName, it.lastUpdateTimeMs) }
                }
                state.trackedItems.forEach { item ->
                    val byPackage = appUpdatedAtByPackage[item.packageName]
                    val byTrackId = state.trackedAddedAtById[item.id]
                    val updatedAt = byPackage?.takeIf { it > 0L } ?: byTrackId?.takeIf { it > 0L }
                    if (updatedAt != null) {
                        put(item.id, updatedAt)
                    }
                }
            }
        }
    }
    val pendingShareImportRepoOverlapCount by remember(
        state.pendingShareImportTrack,
        state.trackedItems
    ) {
        derivedStateOf {
            val pending = state.pendingShareImportTrack ?: return@derivedStateOf 0
            state.trackedItems.count { item ->
                item.owner.equals(pending.owner, ignoreCase = true) &&
                    item.repo.equals(pending.repo, ignoreCase = true)
            }
        }
    }
    val pendingShareImportCardNowMillis by produceState(
        initialValue = System.currentTimeMillis(),
        key1 = state.pendingShareImportTrack?.armedAtMillis
    ) {
        value = System.currentTimeMillis()
        if (state.pendingShareImportTrack == null) return@produceState
        while (true) {
            delay(pendingShareImportCardTickMs)
            value = System.currentTimeMillis()
        }
    }
    val showPendingShareImportCard by remember(
        state.pendingShareImportTrack,
        pendingShareImportRepoOverlapCount,
        pendingShareImportCardNowMillis
    ) {
        derivedStateOf {
            val pending = state.pendingShareImportTrack ?: return@derivedStateOf false
            val ageMs = (pendingShareImportCardNowMillis - pending.armedAtMillis).coerceAtLeast(0L)
            val withinVisibleWindow = ageMs <= pendingShareImportCardVisibleWindowMs
            withinVisibleWindow || pendingShareImportRepoOverlapCount > 0
        }
    }
    return remember(
        trackedUi,
        appLastUpdatedAtByTrackId,
        pendingShareImportRepoOverlapCount,
        showPendingShareImportCard
    ) {
        GitHubPageContentDerivedState(
            trackedUi = trackedUi,
            appLastUpdatedAtByTrackId = appLastUpdatedAtByTrackId,
            pendingShareImportRepoOverlapCount = pendingShareImportRepoOverlapCount,
            showPendingShareImportCard = showPendingShareImportCard
        )
    }
}
