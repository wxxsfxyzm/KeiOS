package com.example.keios.ui.page.main

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.net.toUri
import com.example.keios.R
import com.example.keios.core.background.AppBackgroundScheduler
import com.example.keios.core.system.AppPackageChangedEvent
import com.example.keios.feature.github.data.local.AppIconCache
import com.example.keios.feature.github.data.local.GitHubReleaseAssetCacheStore
import com.example.keios.feature.github.data.local.GitHubTrackStore
import com.example.keios.feature.github.data.remote.GitHubApiTokenReleaseStrategy
import com.example.keios.feature.github.data.remote.GitHubReleaseAssetFile
import com.example.keios.feature.github.data.remote.GitHubReleaseAssetRepository
import com.example.keios.feature.github.data.remote.GitHubReleaseStrategyRegistry
import com.example.keios.feature.github.data.remote.GitHubVersionUtils
import com.example.keios.feature.github.domain.GitHubReleaseCheckService
import com.example.keios.feature.github.domain.GitHubStrategyBenchmarkService
import com.example.keios.feature.github.model.GitHubApiCredentialStatus
import com.example.keios.feature.github.model.GitHubLookupConfig
import com.example.keios.feature.github.model.GitHubLookupStrategyOption
import com.example.keios.feature.github.model.GitHubStrategyLoadTrace
import com.example.keios.feature.github.model.GitHubTrackedApp
import com.example.keios.feature.github.model.InstalledAppItem
import com.example.keios.feature.github.notification.GitHubRefreshNotificationHelper
import com.example.keios.ui.page.main.github.asset.apkAssetTarget
import com.example.keios.ui.page.main.github.query.DownloaderOption
import com.example.keios.ui.page.main.github.query.OnlineShareTargetOption
import com.example.keios.ui.page.main.github.state.toCacheEntry
import com.example.keios.ui.page.main.github.state.toUi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class GitHubPageActions(
    private val context: Context,
    private val scope: CoroutineScope,
    private val state: GitHubPageState,
    private val systemDmOption: DownloaderOption,
    private val openLinkFailureMessage: String
) {
    private val minHandleIntervalMs = 1200L
    private val handledAtByPackage = mutableMapOf<String, Long>()
    private val packageUpdateActions = setOf(
        Intent.ACTION_PACKAGE_ADDED,
        Intent.ACTION_PACKAGE_REPLACED,
        Intent.ACTION_PACKAGE_CHANGED
    )

    fun persistCheckCache(refreshTimestamp: Long = state.lastRefreshMs) {
        val states = state.trackedItems.associate { item ->
            val itemState = state.checkStates[item.id] ?: VersionCheckUi()
            item.id to itemState.toCacheEntry()
        }
        GitHubTrackStore.saveCheckCache(states, refreshTimestamp)
    }

    fun cancelRefreshAll(reason: String? = null) {
        if (state.refreshAllJob?.isActive == true) {
            state.refreshAllJob?.cancel()
            state.refreshAllJob = null
            val trackedCount = state.trackedItems.size
            if (trackedCount > 0) {
                val checkedCount = (state.refreshProgress * trackedCount.toFloat()).toInt()
                    .coerceIn(0, trackedCount)
                val updatableCount = state.trackedItems.count { state.checkStates[it.id]?.hasUpdate == true }
                val failedCount = state.trackedItems.count { state.checkStates[it.id]?.failed == true }
                GitHubRefreshNotificationHelper.notifyCancelled(
                    context = context,
                    current = checkedCount,
                    total = trackedCount,
                    trackedCount = trackedCount,
                    updatableCount = updatableCount,
                    failedCount = failedCount
                )
            } else {
                GitHubRefreshNotificationHelper.cancel(context)
            }
            state.overviewRefreshState = if (state.trackedItems.isEmpty()) {
                OverviewRefreshState.Idle
            } else if (state.checkStates.isNotEmpty()) {
                OverviewRefreshState.Cached
            } else {
                OverviewRefreshState.Idle
            }
            state.refreshProgress = 0f
            reason?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun openStrategySheet() {
        val config = GitHubTrackStore.loadLookupConfig()
        state.lookupConfig = config
        state.selectedStrategyInput = config.selectedStrategy
        state.githubApiTokenInput = config.apiToken
        state.showApiTokenPlainText = false
        state.credentialCheckRunning = false
        state.credentialCheckError = null
        state.credentialCheckStatus = null
        state.strategyBenchmarkError = null
        state.strategyBenchmarkReport = null
        state.recommendedTokenGuideExpanded = false
        state.showStrategySheet = true
    }

    fun closeStrategySheet() {
        state.dismissStrategySheet()
    }

    fun openCheckLogicSheet() {
        val config = GitHubTrackStore.loadLookupConfig()
        state.lookupConfig = config
        state.checkAllTrackedPreReleasesInput = config.checkAllTrackedPreReleases
        state.aggressiveApkFilteringInput = config.aggressiveApkFiltering
        state.onlineShareTargetPackageInput = config.onlineShareTargetPackage
        state.preferredDownloaderPackageInput = config.preferredDownloaderPackage
        state.refreshIntervalHoursInput = GitHubTrackStore.loadRefreshIntervalHours()
        state.showCheckLogicIntervalPopup = false
        state.showDownloaderPopup = false
        state.showOnlineShareTargetPopup = false
        state.showCheckLogicSheet = true
    }

    fun closeCheckLogicSheet() {
        state.dismissCheckLogicSheet()
    }

    suspend fun reloadApps(forceRefresh: Boolean = false) {
        state.appList = withContext(Dispatchers.IO) {
            GitHubVersionUtils.queryInstalledLaunchableApps(
                context = context,
                forceRefresh = forceRefresh
            )
        }
        withContext(Dispatchers.IO) {
            AppIconCache.preload(context, state.appList.map { it.packageName })
        }
        state.appListLoaded = true
    }

    suspend fun initializePage() {
        AppBackgroundScheduler.scheduleGitHubRefresh(context)
        reloadApps(forceRefresh = false)
        val hasTracked = state.trackedItems.isNotEmpty()
        val hasCachedForTracked = state.trackedItems.any { item ->
            state.checkStates.containsKey(item.id)
        }
        val stale = hasTracked && state.lastRefreshMs > 0L &&
            (System.currentTimeMillis() - state.lastRefreshMs) >=
            state.refreshIntervalHours * 60L * 60L * 1000L
        when {
            !hasCachedForTracked && hasTracked -> refreshAllTracked(showToast = false)
            stale -> refreshAllTracked(showToast = false)
            hasCachedForTracked -> state.overviewRefreshState = OverviewRefreshState.Cached
            else -> state.overviewRefreshState = OverviewRefreshState.Idle
        }
    }

    private suspend fun resolveItemState(item: GitHubTrackedApp): VersionCheckUi {
        return withContext(Dispatchers.IO) {
            GitHubReleaseCheckService.evaluateTrackedApp(context, item).toUi()
        }
    }

    fun refreshItem(
        item: GitHubTrackedApp,
        showToastOnError: Boolean = false,
        onUpdated: ((VersionCheckUi) -> Unit)? = null
    ) {
        scope.launch {
            state.checkStates[item.id] = VersionCheckUi(loading = true)
            val itemState = resolveItemState(item)
            if (state.trackedItems.none { it.id == item.id }) return@launch
            if (showToastOnError && itemState.failed) {
                Toast.makeText(context, itemState.message, Toast.LENGTH_SHORT).show()
            }
            state.checkStates[item.id] = itemState
            persistCheckCache()
            onUpdated?.invoke(itemState)
        }
    }

    fun refreshAllTracked(showToast: Boolean = true) {
        val snapshot = state.trackedItems.toList()
        if (snapshot.isEmpty()) {
            if (showToast) {
                Toast.makeText(
                    context,
                    context.getString(R.string.github_toast_no_checkable_item),
                    Toast.LENGTH_SHORT
                ).show()
            }
            state.overviewRefreshState = OverviewRefreshState.Idle
            state.refreshProgress = 0f
            GitHubRefreshNotificationHelper.cancel(context)
            return
        }
        state.refreshAllJob?.cancel()
        state.refreshAllJob = scope.launch {
            GitHubTrackStore.clearCheckCache()
            state.lastRefreshMs = 0L
            state.overviewRefreshState = OverviewRefreshState.Refreshing
            state.refreshProgress = 0f
            val totalCount = snapshot.size
            var updatableCount = 0
            var failedCount = 0
            GitHubRefreshNotificationHelper.notifyProgress(
                context = context,
                current = 0,
                total = totalCount,
                trackedCount = totalCount,
                updatableCount = 0,
                failedCount = 0
            )
            snapshot.forEach { item ->
                state.checkStates[item.id] = VersionCheckUi(
                    loading = true,
                    message = context.getString(R.string.github_msg_checking)
                )
            }
            snapshot.forEachIndexed { index, item ->
                val itemState = resolveItemState(item)
                if (state.trackedItems.any { it.id == item.id }) {
                    state.checkStates[item.id] = itemState
                }
                if (itemState.hasUpdate == true) {
                    updatableCount += 1
                }
                if (itemState.failed) {
                    failedCount += 1
                }
                state.refreshProgress = (index + 1).toFloat() / snapshot.size.toFloat()
                GitHubRefreshNotificationHelper.notifyProgress(
                    context = context,
                    current = index + 1,
                    total = totalCount,
                    trackedCount = totalCount,
                    updatableCount = updatableCount,
                    failedCount = failedCount
                )
                if (showToast && itemState.failed) {
                    Toast.makeText(
                        context,
                        context.getString(
                            R.string.github_toast_repo_message,
                            item.owner,
                            item.repo,
                            itemState.message
                        ),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                if (index < snapshot.lastIndex) delay(120)
            }
            state.overviewRefreshState = OverviewRefreshState.Completed
            state.lastRefreshMs = System.currentTimeMillis()
            state.refreshProgress = 1f
            persistCheckCache(state.lastRefreshMs)
            GitHubRefreshNotificationHelper.notifyCompleted(
                context = context,
                total = totalCount,
                trackedCount = totalCount,
                updatableCount = updatableCount,
                failedCount = failedCount
            )
            if (showToast) {
                Toast.makeText(
                    context,
                    context.getString(R.string.github_toast_check_completed),
                    Toast.LENGTH_SHORT
                ).show()
            }
            state.refreshAllJob = null
        }
    }

    fun applyLookupConfig() {
        val previousConfig = GitHubTrackStore.loadLookupConfig()
        val sanitizedToken = state.githubApiTokenInput.trim()
        val newConfig = GitHubLookupConfig(
            selectedStrategy = state.selectedStrategyInput,
            apiToken = sanitizedToken,
            checkAllTrackedPreReleases = previousConfig.checkAllTrackedPreReleases,
            aggressiveApkFiltering = previousConfig.aggressiveApkFiltering,
            onlineShareTargetPackage = previousConfig.onlineShareTargetPackage
        )
        GitHubTrackStore.saveLookupConfig(newConfig)
        state.lookupConfig = newConfig
        closeStrategySheet()

        val strategyChanged = previousConfig.selectedStrategy != newConfig.selectedStrategy
        val activeTokenChanged = newConfig.selectedStrategy == GitHubLookupStrategyOption.GitHubApiToken &&
            previousConfig.apiToken != newConfig.apiToken
        when {
            strategyChanged || activeTokenChanged -> {
                GitHubReleaseStrategyRegistry.clearAllCaches()
                GitHubTrackStore.clearCheckCache()
                scope.launch(Dispatchers.IO) {
                    GitHubReleaseAssetCacheStore.clearAll()
                }
                state.checkStates.clear()
                state.clearAllAssetUiState()
                state.assetSourceSignature = state.buildAssetSourceSignature(newConfig)
                state.lastRefreshMs = 0L
                state.refreshProgress = 0f
                state.overviewRefreshState = OverviewRefreshState.Idle
                if (state.trackedItems.isNotEmpty()) {
                    Toast.makeText(
                        context,
                        context.getString(
                            R.string.github_toast_strategy_switched_recheck,
                            newConfig.selectedStrategy.label
                        ),
                        Toast.LENGTH_SHORT
                    ).show()
                    refreshAllTracked(showToast = true)
                } else {
                    Toast.makeText(
                        context,
                        context.getString(
                            R.string.github_toast_strategy_switched,
                            newConfig.selectedStrategy.label
                        ),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            previousConfig.apiToken != newConfig.apiToken -> {
                Toast.makeText(
                    context,
                    context.getString(R.string.github_toast_api_credential_saved),
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> {
                Toast.makeText(
                    context,
                    context.getString(R.string.github_toast_strategy_unchanged),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun applyCheckLogicSheet(installedOnlineShareTargets: List<OnlineShareTargetOption>) {
        val previousConfig = GitHubTrackStore.loadLookupConfig()
        val previousRefreshIntervalHours = GitHubTrackStore.loadRefreshIntervalHours()
        val newConfig = previousConfig.copy(
            checkAllTrackedPreReleases = state.checkAllTrackedPreReleasesInput,
            aggressiveApkFiltering = state.aggressiveApkFilteringInput,
            onlineShareTargetPackage = state.onlineShareTargetPackageInput.trim().takeIf { selected ->
                installedOnlineShareTargets.any { it.packageName == selected }
            }.orEmpty(),
            preferredDownloaderPackage = state.preferredDownloaderPackageInput.trim()
        )
        GitHubTrackStore.saveLookupConfig(newConfig)
        GitHubTrackStore.saveRefreshIntervalHours(state.refreshIntervalHoursInput)
        state.lookupConfig = newConfig
        state.refreshIntervalHours = state.refreshIntervalHoursInput
        AppBackgroundScheduler.scheduleGitHubRefresh(context)
        closeCheckLogicSheet()

        val checkScopeChanged =
            previousConfig.checkAllTrackedPreReleases != newConfig.checkAllTrackedPreReleases
        val filteringChanged = previousConfig.aggressiveApkFiltering != newConfig.aggressiveApkFiltering
        val intervalChanged = previousRefreshIntervalHours != state.refreshIntervalHoursInput
        when {
            checkScopeChanged || filteringChanged -> {
                GitHubTrackStore.clearCheckCache()
                state.checkStates.clear()
                state.clearAllAssetUiState()
                state.lastRefreshMs = 0L
                state.refreshProgress = 0f
                state.overviewRefreshState = OverviewRefreshState.Idle
                if (state.trackedItems.isNotEmpty()) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.github_toast_check_logic_updated_recheck),
                        Toast.LENGTH_SHORT
                    ).show()
                    refreshAllTracked(showToast = true)
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.github_toast_check_logic_saved),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            intervalChanged -> {
                Toast.makeText(
                    context,
                    context.getString(R.string.github_toast_refresh_interval_saved),
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> {
                Toast.makeText(
                    context,
                    context.getString(R.string.github_toast_check_logic_unchanged),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun runStrategyBenchmark() {
        if (state.strategyBenchmarkRunning) return
        val targets = GitHubStrategyBenchmarkService.buildTargets(state.trackedItems.toList())
        if (targets.isEmpty()) {
            Toast.makeText(
                context,
                context.getString(R.string.github_toast_require_track_item),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        scope.launch {
            state.strategyBenchmarkRunning = true
            state.strategyBenchmarkError = null
            val benchmarkToken = state.githubApiTokenInput.trim()
            runCatching {
                withContext(Dispatchers.IO) {
                    GitHubStrategyBenchmarkService.compareTargets(
                        targets = targets,
                        apiToken = benchmarkToken
                    )
                }
            }.onSuccess { report ->
                state.strategyBenchmarkReport = report
            }.onFailure { error ->
                state.strategyBenchmarkError = error.message ?: "unknown"
            }
            state.strategyBenchmarkRunning = false
        }
    }

    fun runCredentialCheck() {
        if (state.credentialCheckRunning) return
        scope.launch {
            state.credentialCheckRunning = true
            state.credentialCheckError = null
            state.credentialCheckStatus = null
            try {
                val token = state.githubApiTokenInput.trim()
                val trace: GitHubStrategyLoadTrace<GitHubApiCredentialStatus> =
                    withContext(Dispatchers.IO) {
                        GitHubApiTokenReleaseStrategy(token).checkCredentialTrace()
                    }
                state.credentialCheckStatus = trace.result.getOrNull()
                state.credentialCheckError = trace.result.exceptionOrNull()?.message
            } finally {
                state.credentialCheckRunning = false
            }
        }
    }

    fun handleInstalledOnlineShareTargetsChanged(
        installedOnlineShareTargets: List<OnlineShareTargetOption>
    ) {
        if (state.onlineShareTargetPackageInput.isNotBlank() &&
            installedOnlineShareTargets.none { it.packageName == state.onlineShareTargetPackageInput }
        ) {
            state.onlineShareTargetPackageInput = ""
        }
        if (state.lookupConfig.onlineShareTargetPackage.isNotBlank() &&
            installedOnlineShareTargets.none { it.packageName == state.lookupConfig.onlineShareTargetPackage }
        ) {
            val updatedConfig = state.lookupConfig.copy(onlineShareTargetPackage = "")
            GitHubTrackStore.saveLookupConfig(updatedConfig)
            state.lookupConfig = updatedConfig
        }
    }

    fun openExternalUrl(
        url: String,
        failureMessage: String = openLinkFailureMessage
    ) {
        runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }.onFailure {
            Toast.makeText(context, failureMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun resolvePreferredAssetUrl(asset: GitHubReleaseAssetFile): String {
        val token = state.lookupConfig.apiToken.trim()
        val preferApiAsset =
            state.lookupConfig.selectedStrategy == GitHubLookupStrategyOption.GitHubApiToken
        return withContext(Dispatchers.IO) {
            GitHubReleaseAssetRepository.resolvePreferredDownloadUrl(
                asset = asset,
                useApiAssetUrl = preferApiAsset,
                apiToken = token
            ).getOrElse { asset.downloadUrl }
        }
    }

    fun shareApkLink(asset: GitHubReleaseAssetFile) {
        scope.launch {
            val resolvedUrl = resolvePreferredAssetUrl(asset)
            val onlineSharePackage = state.lookupConfig.onlineShareTargetPackage.trim()
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, asset.name)
                putExtra(Intent.EXTRA_TEXT, resolvedUrl)
                if (onlineSharePackage.isNotBlank()) {
                    `package` = onlineSharePackage
                    putExtra("channel", "Online")
                    putExtra("extra_channel", "Online")
                    putExtra("online_channel", true)
                }
            }
            runCatching {
                if (onlineSharePackage.isNotBlank()) {
                    context.startActivity(intent)
                } else {
                    context.startActivity(
                        Intent.createChooser(intent, context.getString(R.string.github_share_apk_link_title))
                    )
                }
            }.onFailure {
                Toast.makeText(
                    context,
                    context.getString(R.string.github_toast_share_link_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun enqueueWithSystemDownloadManager(url: String, fileName: String) {
        val request = DownloadManager.Request(url.toUri()).apply {
            setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE
            )
            setTitle(fileName)
            setDescription(fileName)
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        }
        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
            ?: throw IllegalStateException("download manager unavailable")
        manager.enqueue(request)
    }

    fun openApkInDownloader(asset: GitHubReleaseAssetFile) {
        scope.launch {
            val resolvedUrl = resolvePreferredAssetUrl(asset)
            val preferredPackage = state.lookupConfig.preferredDownloaderPackage.trim()
            runCatching {
                when (preferredPackage) {
                    systemDmOption.packageName -> {
                        enqueueWithSystemDownloadManager(resolvedUrl, asset.name)
                        Toast.makeText(
                            context,
                            context.getString(R.string.github_toast_downloader_system_builtin),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    "" -> {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(resolvedUrl)).apply {
                                addCategory(Intent.CATEGORY_BROWSABLE)
                            }
                        )
                        Toast.makeText(
                            context,
                            context.getString(R.string.github_toast_downloader_system_default),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else -> {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(resolvedUrl)).apply {
                                addCategory(Intent.CATEGORY_BROWSABLE)
                                setPackage(preferredPackage)
                            }
                        )
                        Toast.makeText(
                            context,
                            context.getString(R.string.github_toast_downloader_selected),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }.recoverCatching {
                if (preferredPackage.isNotBlank() && preferredPackage != systemDmOption.packageName) {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(resolvedUrl)).apply {
                            addCategory(Intent.CATEGORY_BROWSABLE)
                        }
                    )
                    Toast.makeText(
                        context,
                        context.getString(R.string.github_toast_downloader_fallback_system),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    throw it
                }
            }.onFailure {
                Toast.makeText(
                    context,
                    context.getString(R.string.github_toast_open_downloader_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun clearApkAssetUiState(itemId: String) {
        state.clearAssetUiState(itemId)
    }

    fun clearApkAssetCache(item: GitHubTrackedApp, itemState: VersionCheckUi) {
        val target = itemState.apkAssetTarget(
            owner = item.owner,
            repo = item.repo,
            context = context,
            alwaysLatestRelease = item.alwaysShowLatestReleaseDownloadButton
        ) ?: return
        val preferHtml = state.lookupConfig.selectedStrategy == GitHubLookupStrategyOption.AtomFeed
        val hasApiToken = state.lookupConfig.apiToken.isNotBlank()
        val releaseUrl = target.releaseUrl
        val normalizedRawTag = target.rawTag
        val cacheKeyDefault = GitHubReleaseAssetCacheStore.buildCacheKey(
            owner = item.owner,
            repo = item.repo,
            rawTag = normalizedRawTag,
            releaseUrl = releaseUrl,
            preferHtml = preferHtml,
            aggressiveFiltering = state.lookupConfig.aggressiveApkFiltering,
            includeAllAssets = false,
            hasApiToken = hasApiToken
        )
        val cacheKeyAllAssets = GitHubReleaseAssetCacheStore.buildCacheKey(
            owner = item.owner,
            repo = item.repo,
            rawTag = normalizedRawTag,
            releaseUrl = releaseUrl,
            preferHtml = preferHtml,
            aggressiveFiltering = state.lookupConfig.aggressiveApkFiltering,
            includeAllAssets = true,
            hasApiToken = hasApiToken
        )
        scope.launch(Dispatchers.IO) {
            GitHubReleaseAssetCacheStore.clear(cacheKeyDefault)
            GitHubReleaseAssetCacheStore.clear(cacheKeyAllAssets)
        }
    }

    fun loadApkAssets(
        item: GitHubTrackedApp,
        itemState: VersionCheckUi,
        toggleOnlyWhenCached: Boolean = true,
        includeAllAssets: Boolean = false
    ) {
        val alwaysLatestRelease = item.alwaysShowLatestReleaseDownloadButton
        val target = itemState.apkAssetTarget(
            owner = item.owner,
            repo = item.repo,
            context = context,
            alwaysLatestRelease = alwaysLatestRelease
        )
        if (target == null) {
            val fallbackUrl = if (alwaysLatestRelease) {
                GitHubVersionUtils.buildReleaseUrl(item.owner, item.repo)
            } else {
                itemState.statusActionUrl(item.owner, item.repo)
            }
            if (fallbackUrl.isNotBlank()) {
                openExternalUrl(fallbackUrl)
            } else {
                Toast.makeText(
                    context,
                    context.getString(R.string.github_toast_no_apk_to_load),
                    Toast.LENGTH_SHORT
                ).show()
            }
            return
        }

        val cachedBundle = state.apkAssetBundles[item.id]
        if (
            toggleOnlyWhenCached &&
            cachedBundle != null &&
            state.matchesAssetSourceSignature(cachedBundle) &&
            cachedBundle.tagName.equals(target.rawTag, ignoreCase = true) &&
            cachedBundle.showingAllAssets == includeAllAssets
        ) {
            state.apkAssetExpanded[item.id] = !(state.apkAssetExpanded[item.id] ?: false)
            state.apkAssetErrors.remove(item.id)
            return
        }

        state.apkAssetExpanded[item.id] = true
        state.apkAssetLoading[item.id] = true
        state.apkAssetErrors.remove(item.id)
        scope.launch {
            val preferHtml = state.lookupConfig.selectedStrategy == GitHubLookupStrategyOption.AtomFeed
            val refreshIntervalHours = GitHubTrackStore.loadRefreshIntervalHours()
            val assetCacheKey = GitHubReleaseAssetCacheStore.buildCacheKey(
                owner = item.owner,
                repo = item.repo,
                rawTag = target.rawTag,
                releaseUrl = target.releaseUrl,
                preferHtml = preferHtml,
                aggressiveFiltering = state.lookupConfig.aggressiveApkFiltering,
                includeAllAssets = includeAllAssets,
                hasApiToken = state.lookupConfig.apiToken.isNotBlank()
            )
            val persistedBundle = withContext(Dispatchers.IO) {
                GitHubReleaseAssetCacheStore.load(
                    cacheKey = assetCacheKey,
                    refreshIntervalHours = refreshIntervalHours
                )
            }
            if (persistedBundle != null && state.matchesAssetSourceSignature(persistedBundle)) {
                state.apkAssetLoading[item.id] = false
                state.apkAssetBundles[item.id] = persistedBundle
                state.apkAssetErrors[item.id] = if (persistedBundle.assets.isEmpty()) {
                    if (includeAllAssets) {
                        context.getString(
                            R.string.github_msg_assets_no_downloadable_except_source,
                            target.label
                        )
                    } else {
                        context.getString(
                            R.string.github_msg_assets_no_downloadable,
                            target.label
                        )
                    }
                } else {
                    ""
                }
                return@launch
            } else if (persistedBundle != null) {
                withContext(Dispatchers.IO) {
                    GitHubReleaseAssetCacheStore.clear(assetCacheKey)
                }
            }
            val result = withContext(Dispatchers.IO) {
                GitHubReleaseAssetRepository.fetchApkAssets(
                    owner = item.owner,
                    repo = item.repo,
                    rawTag = target.rawTag,
                    releaseUrl = target.releaseUrl,
                    preferHtml = preferHtml,
                    aggressiveFiltering = state.lookupConfig.aggressiveApkFiltering,
                    includeAllAssets = includeAllAssets,
                    apiToken = state.lookupConfig.apiToken
                )
            }
            state.apkAssetLoading[item.id] = false
            result.onSuccess { bundle ->
                val persistedBundle = bundle.copy(
                    sourceConfigSignature = state.buildAssetSourceSignature()
                )
                state.apkAssetBundles[item.id] = persistedBundle
                scope.launch(Dispatchers.IO) {
                    GitHubReleaseAssetCacheStore.save(
                        cacheKey = assetCacheKey,
                        bundle = persistedBundle
                    )
                }
                state.apkAssetErrors[item.id] = if (persistedBundle.assets.isEmpty()) {
                    if (includeAllAssets) {
                        context.getString(
                            R.string.github_msg_assets_no_downloadable_except_source,
                            target.label
                        )
                    } else {
                        context.getString(
                            R.string.github_msg_assets_no_downloadable,
                            target.label
                        )
                    }
                } else {
                    ""
                }
            }.onFailure { error ->
                state.apkAssetErrors[item.id] = error.message
                    ?: context.getString(R.string.github_error_load_apk_assets_failed)
            }
        }
    }

    fun openTrackSheetForAdd() {
        state.resetTrackEditor()
        state.showAddSheet = true
    }

    fun openTrackSheetForEdit(item: GitHubTrackedApp) {
        state.editingTrackedItem = item
        state.repoUrlInput = item.repoUrl
        state.selectedApp = state.appList.firstOrNull { it.packageName == item.packageName }
            ?: InstalledAppItem(label = item.appLabel, packageName = item.packageName)
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
        val app = state.selectedApp
        val parsed = GitHubVersionUtils.parseOwnerRepo(state.repoUrlInput)
        if (app == null || parsed == null) {
            Toast.makeText(
                context,
                context.getString(R.string.github_toast_fill_repo_and_select_app),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        val newItem = GitHubTrackedApp(
            repoUrl = state.repoUrlInput.trim(),
            owner = parsed.first,
            repo = parsed.second,
            packageName = app.packageName,
            appLabel = app.label,
            preferPreRelease = state.preferPreReleaseInput,
            alwaysShowLatestReleaseDownloadButton = state.alwaysShowLatestReleaseDownloadButtonInput
        )
        val editing = state.editingTrackedItem
        if (editing == null) {
            if (state.trackedItems.any { it.id == newItem.id }) {
                Toast.makeText(
                    context,
                    context.getString(R.string.github_toast_track_exists),
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
            state.trackedItems.add(newItem)
            saveTracked()
            refreshItem(newItem, showToastOnError = true)
            Toast.makeText(
                context,
                context.getString(R.string.github_toast_track_added),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            val duplicate = state.trackedItems.any { it.id == newItem.id && it.id != editing.id }
            if (duplicate) {
                Toast.makeText(
                    context,
                    context.getString(R.string.github_toast_track_exists),
                    Toast.LENGTH_SHORT
                ).show()
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
            }
            saveTracked()
            refreshItem(newItem, showToastOnError = true)
            Toast.makeText(
                context,
                context.getString(R.string.github_toast_track_updated),
                Toast.LENGTH_SHORT
            ).show()
        }
        state.dismissTrackSheet()
    }

    fun confirmDeletePendingItem() {
        if (state.deleteInProgress) return
        state.pendingDeleteItem?.let { deleting ->
            state.deleteInProgress = true
            try {
                cancelRefreshAll()
                state.trackedItems.remove(deleting)
                state.checkStates.remove(deleting.id)
                saveTracked()
                persistCheckCache()
                Toast.makeText(
                    context,
                    context.getString(R.string.github_toast_track_deleted, deleting.appLabel),
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                state.deleteInProgress = false
            }
        }
        state.pendingDeleteItem = null
    }

    suspend fun handlePackageChangedEvent(event: AppPackageChangedEvent) {
        val packageName = event.packageName.trim()
        if (packageName.isBlank()) return
        if (event.action !in packageUpdateActions) return

        val matchedItems = state.trackedItems.filter { it.packageName == packageName }
        if (matchedItems.isEmpty()) return

        val lastHandledAt = handledAtByPackage[packageName] ?: 0L
        if ((event.atMillis - lastHandledAt).coerceAtLeast(0L) < minHandleIntervalMs) {
            return
        }
        handledAtByPackage[packageName] = event.atMillis

        reloadApps(forceRefresh = true)
        matchedItems.forEach { item ->
            val wasAssetExpanded = state.apkAssetExpanded[item.id] == true
            val previousState = state.checkStates[item.id] ?: VersionCheckUi()
            clearApkAssetUiState(item.id)
            clearApkAssetCache(item, previousState)
            refreshItem(item = item, showToastOnError = false) { updatedState ->
                val canLoadApkAssets = item.alwaysShowLatestReleaseDownloadButton ||
                    updatedState.hasUpdate == true ||
                    updatedState.recommendsPreRelease ||
                    updatedState.hasPreReleaseUpdate
                if (wasAssetExpanded && canLoadApkAssets) {
                    loadApkAssets(
                        item = item,
                        itemState = updatedState,
                        toggleOnlyWhenCached = false,
                        includeAllAssets = false
                    )
                }
            }
        }
    }

    private fun saveTracked() {
        GitHubTrackStore.save(state.trackedItems.toList())
        AppBackgroundScheduler.scheduleGitHubRefresh(context)
    }
}
