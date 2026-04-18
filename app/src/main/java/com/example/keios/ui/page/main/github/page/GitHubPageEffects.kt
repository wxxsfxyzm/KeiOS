package com.example.keios.ui.page.main

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.foundation.lazy.LazyListState
import com.example.keios.R
import com.example.keios.core.system.AppPackageChangedEvents
import com.example.keios.feature.github.data.local.GitHubTrackSnapshot
import com.example.keios.feature.github.data.local.GitHubTrackStore
import com.example.keios.feature.github.data.remote.GitHubVersionUtils
import com.example.keios.ui.page.main.github.query.OnlineShareTargetOption
import com.example.keios.ui.page.main.github.state.toUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun BindGitHubTrackSnapshotEffect(
    state: GitHubPageState
) {
    val trackSnapshot by produceState(initialValue = GitHubTrackSnapshot()) {
        value = withContext(Dispatchers.IO) { GitHubTrackStore.loadSnapshot() }
    }

    LaunchedEffect(trackSnapshot) {
        val activeStrategyId = trackSnapshot.lookupConfig.selectedStrategy.storageId
        val snapshotAssetSourceSignature = listOf(
            "asset-v2",
            trackSnapshot.lookupConfig.selectedStrategy.storageId,
            trackSnapshot.lookupConfig.apiToken.isNotBlank().toString(),
            trackSnapshot.lookupConfig.aggressiveApkFiltering.toString()
        ).joinToString("|")
        state.lookupConfig = trackSnapshot.lookupConfig
        state.selectedStrategyInput = trackSnapshot.lookupConfig.selectedStrategy
        state.githubApiTokenInput = trackSnapshot.lookupConfig.apiToken
        state.checkAllTrackedPreReleasesInput = trackSnapshot.lookupConfig.checkAllTrackedPreReleases
        state.aggressiveApkFilteringInput = trackSnapshot.lookupConfig.aggressiveApkFiltering
        state.onlineShareTargetPackageInput = trackSnapshot.lookupConfig.onlineShareTargetPackage
        state.preferredDownloaderPackageInput = trackSnapshot.lookupConfig.preferredDownloaderPackage
        state.refreshIntervalHours = trackSnapshot.refreshIntervalHours
        state.refreshIntervalHoursInput = trackSnapshot.refreshIntervalHours
        if (
            state.assetSourceSignature.isNotBlank() &&
            state.assetSourceSignature != snapshotAssetSourceSignature
        ) {
            state.clearAllAssetUiState()
        }
        state.assetSourceSignature = snapshotAssetSourceSignature

        state.trackedItems.clear()
        state.trackedItems.addAll(trackSnapshot.items)

        val cachedStates = trackSnapshot.checkCache
        val cachedRefreshMs = trackSnapshot.lastRefreshMs
        state.checkStates.clear()
        trackSnapshot.items.forEach { item ->
            cachedStates[item.id]
                ?.takeIf { cache ->
                    val sourceId = cache.sourceStrategyId.ifBlank {
                        com.example.keios.feature.github.model.GitHubLookupStrategyOption.AtomFeed.storageId
                    }
                    sourceId == activeStrategyId
                }
                ?.let { cached ->
                    state.checkStates[item.id] = cached.toUi()
                }
        }
        state.lastRefreshMs = cachedRefreshMs

        val hasTracked = trackSnapshot.items.isNotEmpty()
        val hasCachedForTracked = trackSnapshot.items.any { item ->
            cachedStates[item.id]?.let { cache ->
                val sourceId = cache.sourceStrategyId.ifBlank {
                    com.example.keios.feature.github.model.GitHubLookupStrategyOption.AtomFeed.storageId
                }
                sourceId == activeStrategyId
            } == true
        }
        state.overviewRefreshState = when {
            hasCachedForTracked -> OverviewRefreshState.Cached
            hasTracked -> OverviewRefreshState.Refreshing
            else -> OverviewRefreshState.Idle
        }
    }
}

@Composable
internal fun BindGitHubPageEffects(
    context: Context,
    listState: LazyListState,
    scrollToTopSignal: Int,
    state: GitHubPageState,
    actions: GitHubPageActions,
    installedOnlineShareTargets: List<OnlineShareTargetOption>,
    onLaunchAppListPermission: (Intent) -> Unit,
    onActionBarInteractingChanged: (Boolean) -> Unit
) {
    DisposableEffect(Unit) {
        onDispose { onActionBarInteractingChanged(false) }
    }

    LaunchedEffect(installedOnlineShareTargets) {
        actions.handleInstalledOnlineShareTargetsChanged(installedOnlineShareTargets)
    }

    LaunchedEffect(Unit) {
        actions.initializePage()
    }

    LaunchedEffect(scrollToTopSignal) {
        if (scrollToTopSignal > 0) {
            listState.animateScrollToItem(0)
        }
    }

    LaunchedEffect(state.appListLoaded, state.appList) {
        if (state.appListLoaded && state.appList.isEmpty() && !state.hasAutoRequestedPermission) {
            state.hasAutoRequestedPermission = true
            val intent = GitHubVersionUtils.buildAppListPermissionIntent(context)
            if (intent != null) {
                onLaunchAppListPermission(intent)
            } else {
                Toast.makeText(
                    context,
                    context.getString(R.string.github_toast_open_permission_page_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    LaunchedEffect(Unit) {
        AppPackageChangedEvents.events.collect { event ->
            actions.handlePackageChangedEvent(event)
        }
    }
}
