package com.example.keios.feature.github.domain

import android.content.Context
import com.example.keios.feature.github.data.remote.GitHubAtomReleaseStrategy
import com.example.keios.feature.github.data.remote.GitHubReleaseLookupStrategy
import com.example.keios.feature.github.data.remote.GitHubReleaseStrategyRegistry
import com.example.keios.feature.github.data.remote.GitHubVersionUtils
import com.example.keios.feature.github.model.GitHubReleaseChannel
import com.example.keios.feature.github.model.GitHubRepositoryReleaseSnapshot
import com.example.keios.feature.github.model.GitHubCheckCacheEntry
import com.example.keios.feature.github.model.GitHubTrackedApp
import com.example.keios.feature.github.model.GitHubTrackedReleaseCheck
import com.example.keios.feature.github.model.GitHubTrackedReleaseStatus

object GitHubReleaseCheckService {
    fun evaluateTrackedApp(
        context: Context,
        item: GitHubTrackedApp,
        strategy: GitHubReleaseLookupStrategy? = null
    ): GitHubTrackedReleaseCheck {
        val lookupConfig = GitHubReleaseStrategyRegistry.loadLookupConfig()
        val localVersion = runCatching {
            GitHubVersionUtils.localVersionName(context, item.packageName)
        }.getOrDefault("unknown")
        val localVersionCode = runCatching {
            GitHubVersionUtils.localVersionCode(context, item.packageName)
        }.getOrDefault(-1L)
        val effectiveStrategy = strategy ?: GitHubReleaseStrategyRegistry.resolveConfiguredStrategy().getOrElse { error ->
            return GitHubTrackedReleaseCheck(
                strategyId = lookupConfig.selectedStrategy.storageId,
                localVersion = localVersion,
                localVersionCode = localVersionCode,
                status = GitHubTrackedReleaseStatus.Failed,
                message = "检查失败: ${error.message ?: "unknown"}"
            )
        }

        val snapshot = effectiveStrategy.loadSnapshot(item.owner, item.repo).getOrElse { error ->
            return GitHubTrackedReleaseCheck(
                strategyId = effectiveStrategy.id,
                localVersion = localVersion,
                localVersionCode = localVersionCode,
                status = GitHubTrackedReleaseStatus.Failed,
                message = "检查失败: ${error.message ?: "unknown"}"
            )
        }

        return evaluateSnapshot(
            item = item,
            localVersion = localVersion,
            localVersionCode = localVersionCode,
            snapshot = snapshot,
            checkAllTrackedPreReleases = lookupConfig.checkAllTrackedPreReleases
        )
    }

    internal fun evaluateSnapshot(
        item: GitHubTrackedApp,
        localVersion: String,
        localVersionCode: Long,
        snapshot: GitHubRepositoryReleaseSnapshot,
        checkAllTrackedPreReleases: Boolean = false
    ): GitHubTrackedReleaseCheck {
        val matchedEntry = snapshot.feed.entries.firstOrNull { entry ->
            GitHubVersionUtils.compareVersionToStructuredCandidates(localVersion, entry.versionCandidates) == 0
        }
        val latestStable = snapshot.latestStable
        val latestPre = snapshot.latestPreRelease
        val localChannel = matchedEntry?.channel
            ?: GitHubVersionUtils.classifyVersionChannel(localVersion)
            ?: GitHubReleaseChannel.UNKNOWN
        val isLocalPreReleaseInstalled = matchedEntry?.isLikelyPreRelease == true || localChannel.isPreRelease
        val inspectPreRelease = checkAllTrackedPreReleases || item.preferPreRelease || isLocalPreReleaseInstalled

        val stableCmp = GitHubVersionUtils.compareVersionToStructuredCandidates(localVersion, latestStable.versionCandidates)
        val latestPreIsRelevant = latestPre != null && GitHubVersionUtils.isRelevantPreRelease(
            preReleaseCandidates = latestPre.versionCandidates,
            stableCandidates = latestStable.versionCandidates,
            preReleaseUpdatedAtMillis = latestPre.updatedAtMillis,
            stableUpdatedAtMillis = latestStable.updatedAtMillis
        )
        val latestPreCmp = if (latestPre != null) {
            GitHubVersionUtils.compareVersionToStructuredCandidates(localVersion, latestPre.versionCandidates)
        } else {
            null
        }

        val hasPreReleaseUpdate = inspectPreRelease &&
            latestPreIsRelevant &&
            (latestPreCmp?.let { it < 0 } == true)
        val stableHasUpdate = stableCmp?.let { it < 0 } == true
        val recommendsPreRelease = hasPreReleaseUpdate &&
            (item.preferPreRelease || (isLocalPreReleaseInstalled && !stableHasUpdate))
        val hasUpdate = stableHasUpdate || recommendsPreRelease

        val preReleaseInfo = when {
            inspectPreRelease && latestPre != null -> latestPre.displayVersion
            inspectPreRelease && isLocalPreReleaseInstalled && matchedEntry != null -> matchedEntry.displayVersion
            else -> ""
        }
        val showPreReleaseInfo = inspectPreRelease && preReleaseInfo.isNotBlank()

        val status = when {
            recommendsPreRelease -> GitHubTrackedReleaseStatus.PreReleaseUpdateAvailable
            stableHasUpdate -> GitHubTrackedReleaseStatus.UpdateAvailable
            hasPreReleaseUpdate -> GitHubTrackedReleaseStatus.PreReleaseOptional
            inspectPreRelease && isLocalPreReleaseInstalled -> GitHubTrackedReleaseStatus.PreReleaseTracked
            stableCmp != null && hasUpdate == false -> GitHubTrackedReleaseStatus.UpToDate
            matchedEntry != null -> GitHubTrackedReleaseStatus.MatchedRelease
            else -> GitHubTrackedReleaseStatus.ComparisonUncertain
        }

        return GitHubTrackedReleaseCheck(
            strategyId = snapshot.strategyId,
            localVersion = localVersion,
            localVersionCode = localVersionCode,
            matchedRelease = matchedEntry,
            stableRelease = latestStable,
            preRelease = latestPre,
            hasUpdate = hasUpdate,
            hasPreReleaseUpdate = hasPreReleaseUpdate,
            recommendsPreRelease = recommendsPreRelease,
            isPreReleaseInstalled = isLocalPreReleaseInstalled,
            preReleaseInfo = preReleaseInfo,
            showPreReleaseInfo = showPreReleaseInfo,
            status = status,
            message = status.defaultMessage
        )
    }

    fun GitHubTrackedReleaseCheck.toCacheEntry(): GitHubCheckCacheEntry {
        return GitHubCheckCacheEntry(
            loading = false,
            localVersion = localVersion,
            localVersionCode = localVersionCode,
            latestTag = stableRelease?.displayVersion.orEmpty(),
            latestStableRawTag = stableRelease?.rawTag.orEmpty(),
            latestStableUrl = stableRelease?.link.orEmpty(),
            latestPreRawTag = preRelease?.rawTag.orEmpty(),
            latestPreUrl = preRelease?.link.orEmpty(),
            hasUpdate = hasUpdate,
            message = message,
            isPreRelease = isPreReleaseInstalled,
            preReleaseInfo = preReleaseInfo,
            showPreReleaseInfo = showPreReleaseInfo,
            hasPreReleaseUpdate = hasPreReleaseUpdate,
            recommendsPreRelease = recommendsPreRelease,
            sourceStrategyId = strategyId
        )
    }

    fun fromCacheEntry(entry: GitHubCheckCacheEntry): GitHubTrackedReleaseCheck {
        return GitHubTrackedReleaseCheck(
            strategyId = entry.sourceStrategyId.ifBlank { GitHubAtomReleaseStrategy.id },
            localVersion = entry.localVersion,
            localVersionCode = entry.localVersionCode,
            stableRelease = entry.latestTag.takeIf { it.isNotBlank() }?.let {
                com.example.keios.feature.github.model.GitHubReleaseVersionSignals(
                    displayVersion = entry.latestTag,
                    rawTag = entry.latestStableRawTag,
                    rawName = entry.latestTag,
                    link = entry.latestStableUrl
                )
            },
            preRelease = entry.latestPreRawTag.takeIf { it.isNotBlank() || entry.preReleaseInfo.isNotBlank() }?.let {
                com.example.keios.feature.github.model.GitHubReleaseVersionSignals(
                    displayVersion = entry.preReleaseInfo.ifBlank { entry.latestPreRawTag },
                    rawTag = entry.latestPreRawTag,
                    rawName = entry.preReleaseInfo.ifBlank { entry.latestPreRawTag },
                    link = entry.latestPreUrl
                )
            },
            hasUpdate = entry.hasUpdate,
            hasPreReleaseUpdate = entry.hasPreReleaseUpdate,
            recommendsPreRelease = entry.recommendsPreRelease ||
                entry.message == GitHubTrackedReleaseStatus.PreReleaseUpdateAvailable.defaultMessage,
            isPreReleaseInstalled = entry.isPreRelease,
            preReleaseInfo = entry.preReleaseInfo,
            showPreReleaseInfo = entry.showPreReleaseInfo,
            status = GitHubTrackedReleaseStatus.entries.firstOrNull { it.defaultMessage == entry.message }
                ?: if (entry.message.startsWith("检查失败")) {
                    GitHubTrackedReleaseStatus.Failed
                } else {
                    GitHubTrackedReleaseStatus.ComparisonUncertain
                },
            message = entry.message
        )
    }
}
