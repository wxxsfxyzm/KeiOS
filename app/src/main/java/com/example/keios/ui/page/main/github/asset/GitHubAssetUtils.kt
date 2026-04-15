package com.example.keios.ui.page.main.github.asset

import com.example.keios.feature.github.data.remote.GitHubReleaseAssetBundle
import com.example.keios.feature.github.data.remote.GitHubReleaseAssetFile
import com.example.keios.feature.github.data.remote.GitHubReleaseAssetRepository
import com.example.keios.feature.github.data.remote.GitHubVersionUtils
import com.example.keios.ui.page.main.VersionCheckUi
import java.util.concurrent.TimeUnit

internal data class ApkAssetTarget(
    val rawTag: String,
    val releaseUrl: String,
    val label: String
)

internal fun VersionCheckUi.apkAssetTarget(owner: String, repo: String): ApkAssetTarget? {
    val stableTag = latestStableRawTag.ifBlank {
        GitHubReleaseAssetRepository.parseReleaseTagFromUrl(latestStableUrl)
    }
    val preTag = latestPreRawTag.ifBlank {
        GitHubReleaseAssetRepository.parseReleaseTagFromUrl(latestPreUrl)
    }
    return when {
        recommendsPreRelease && preTag.isNotBlank() -> ApkAssetTarget(
            rawTag = preTag,
            releaseUrl = latestPreUrl.ifBlank { GitHubVersionUtils.buildReleaseTagUrl(owner, repo, preTag) },
            label = "预发资源"
        )
        hasUpdate == true && stableTag.isNotBlank() -> ApkAssetTarget(
            rawTag = stableTag,
            releaseUrl = latestStableUrl.ifBlank { GitHubVersionUtils.buildReleaseTagUrl(owner, repo, stableTag) },
            label = "稳定资源"
        )
        hasPreReleaseUpdate && preTag.isNotBlank() -> ApkAssetTarget(
            rawTag = preTag,
            releaseUrl = latestPreUrl.ifBlank { GitHubVersionUtils.buildReleaseTagUrl(owner, repo, preTag) },
            label = "预发资源"
        )
        else -> null
    }
}

internal fun formatAssetSize(sizeBytes: Long): String {
    if (sizeBytes <= 0L) return "未知"
    val kb = 1024L
    val mb = kb * 1024L
    val gb = mb * 1024L
    return when {
        sizeBytes >= gb -> String.format("%.1fG", sizeBytes.toDouble() / gb.toDouble())
        sizeBytes >= mb -> String.format("%.1fM", sizeBytes.toDouble() / mb.toDouble())
        sizeBytes >= kb -> String.format("%.0fK", sizeBytes.toDouble() / kb.toDouble())
        else -> "${sizeBytes}B"
    }
}

internal fun prefersApiAssetTransport(asset: GitHubReleaseAssetFile): Boolean {
    return asset.apiAssetUrl.isNotBlank()
}

internal fun assetTransportLabel(asset: GitHubReleaseAssetFile): String {
    return if (prefersApiAssetTransport(asset)) "API" else "直链"
}

internal fun bundleTransportLabel(bundle: GitHubReleaseAssetBundle?): String? {
    bundle ?: return null
    return if (bundle.assets.any { prefersApiAssetTransport(it) }) "API" else "直链"
}

internal fun bundleCommitLabel(bundle: GitHubReleaseAssetBundle?): String? {
    return bundle?.shortCommitSha?.trim().takeIf { !it.isNullOrBlank() }
}

internal fun assetAbiLabel(fileName: String): String? {
    val lower = fileName.lowercase()
    return when {
        "arm64-v8a" in lower || "aarch64" in lower ||
            Regex("(^|[^a-z0-9])arm64([^a-z0-9]|$)").containsMatchIn(lower) -> "arm64"
        "universal" in lower || "fat" in lower -> "universal"
        "armeabi-v7a" in lower || "armv7" in lower -> "armeabi-v7a"
        Regex("(^|[^a-z0-9])armeabi([^a-z0-9]|$)").containsMatchIn(lower) -> "armeabi"
        "x86_64" in lower -> "x86_64"
        Regex("""(^|[^a-z0-9])x86([^a-z0-9]|$)""").containsMatchIn(lower) -> "x86"
        else -> null
    }
}

internal fun assetFileExtensionLabel(fileName: String): String? {
    val trimmedName = fileName.trim()
    val extension = trimmedName.substringAfterLast('.', "")
        .takeIf { '.' in trimmedName && it.isNotBlank() }
        ?.lowercase()
        ?: return null
    return extension
}

internal fun assetDisplayName(fileName: String): String {
    val trimmedName = fileName.trim()
    val extension = assetFileExtensionLabel(trimmedName) ?: return trimmedName
    return trimmedName.removeSuffix(".$extension")
}

internal fun assetRelativeTimeLabel(updatedAtMillis: Long?): String? {
    val updatedAt = updatedAtMillis ?: return null
    val diffMillis = (System.currentTimeMillis() - updatedAt).coerceAtLeast(0L)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis)
    val hours = TimeUnit.MILLISECONDS.toHours(diffMillis)
    val days = TimeUnit.MILLISECONDS.toDays(diffMillis)
    return when {
        minutes < 1L -> "just now"
        minutes < 60L -> "$minutes min ago"
        hours < 24L -> "$hours hr ago"
        days < 7L -> "$days day${if (days == 1L) "" else "s"} ago"
        days < 30L -> "last week"
        days < 60L -> "last month"
        else -> "${days / 30L} mo ago"
    }
}
