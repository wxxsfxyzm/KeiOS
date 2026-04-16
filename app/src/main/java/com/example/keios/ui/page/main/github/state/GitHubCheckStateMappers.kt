package com.example.keios.ui.page.main.github.state

import com.example.keios.feature.github.model.GitHubCheckCacheEntry
import com.example.keios.feature.github.model.GitHubTrackedReleaseStatus
import com.example.keios.feature.github.model.GitHubTrackedReleaseCheck
import com.example.keios.ui.page.main.VersionCheckUi

internal fun VersionCheckUi.toCacheEntry(): GitHubCheckCacheEntry = GitHubCheckCacheEntry(
    loading = false,
    localVersion = localVersion,
    localVersionCode = localVersionCode,
    latestTag = latestTag,
    latestStableName = latestStableName,
    latestStableRawTag = latestStableRawTag,
    latestStableUrl = latestStableUrl,
    latestStableUpdatedAtMillis = latestStableUpdatedAtMillis,
    latestPreName = latestPreName,
    latestPreRawTag = latestPreRawTag,
    latestPreUrl = latestPreUrl,
    latestPreUpdatedAtMillis = latestPreUpdatedAtMillis,
    hasStableRelease = hasStableRelease,
    hasUpdate = hasUpdate,
    message = message,
    isPreRelease = isPreRelease,
    preReleaseInfo = preReleaseInfo,
    showPreReleaseInfo = showPreReleaseInfo,
    hasPreReleaseUpdate = hasPreReleaseUpdate,
    recommendsPreRelease = recommendsPreRelease,
    releaseHint = releaseHint,
    sourceStrategyId = sourceStrategyId
)

internal fun GitHubCheckCacheEntry.toUi(): VersionCheckUi = VersionCheckUi(
    loading = false,
    localVersion = localVersion,
    localVersionCode = localVersionCode,
    latestTag = latestTag,
    latestStableName = latestStableName,
    latestStableRawTag = latestStableRawTag,
    latestStableUrl = latestStableUrl,
    latestStableUpdatedAtMillis = latestStableUpdatedAtMillis,
    latestPreName = latestPreName,
    latestPreRawTag = latestPreRawTag,
    latestPreUrl = latestPreUrl,
    latestPreUpdatedAtMillis = latestPreUpdatedAtMillis,
    hasStableRelease = hasStableRelease,
    hasUpdate = hasUpdate,
    message = message,
    isPreRelease = isPreRelease,
    preReleaseInfo = preReleaseInfo,
    showPreReleaseInfo = showPreReleaseInfo,
    hasPreReleaseUpdate = hasPreReleaseUpdate,
    recommendsPreRelease = recommendsPreRelease,
    releaseHint = releaseHint,
    failed = message.startsWith(GitHubTrackedReleaseStatus.Failed.defaultMessage),
    sourceStrategyId = sourceStrategyId
)

internal fun GitHubTrackedReleaseCheck.toUi(): VersionCheckUi = VersionCheckUi(
    loading = false,
    localVersion = localVersion,
    localVersionCode = localVersionCode,
    latestTag = stableRelease?.displayVersion.orEmpty(),
    latestStableName = stableRelease?.rawName.orEmpty(),
    latestStableRawTag = stableRelease?.rawTag.orEmpty(),
    latestStableUrl = stableRelease?.link.orEmpty(),
    latestStableUpdatedAtMillis = stableRelease?.updatedAtMillis ?: -1L,
    latestPreName = preRelease?.rawName.orEmpty(),
    latestPreRawTag = preRelease?.rawTag.orEmpty(),
    latestPreUrl = preRelease?.link.orEmpty(),
    latestPreUpdatedAtMillis = preRelease?.updatedAtMillis ?: -1L,
    hasStableRelease = hasStableRelease,
    hasUpdate = hasUpdate,
    message = message,
    isPreRelease = isPreReleaseInstalled,
    preReleaseInfo = preReleaseInfo,
    showPreReleaseInfo = showPreReleaseInfo,
    hasPreReleaseUpdate = hasPreReleaseUpdate,
    recommendsPreRelease = recommendsPreRelease,
    releaseHint = releaseHint,
    failed = status == GitHubTrackedReleaseStatus.Failed,
    sourceStrategyId = strategyId
)
