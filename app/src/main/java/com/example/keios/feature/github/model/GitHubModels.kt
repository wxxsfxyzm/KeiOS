package com.example.keios.feature.github.model

data class GitHubTrackedApp(
    val repoUrl: String,
    val owner: String,
    val repo: String,
    val packageName: String,
    val appLabel: String,
    val checkPreRelease: Boolean = false
) {
    val id: String
        get() = "$owner/$repo|$packageName"
}

data class InstalledAppItem(
    val label: String,
    val packageName: String
)

data class GitHubReleaseVersionSignals(
    val displayVersion: String,
    val rawTag: String,
    val rawName: String,
    val candidates: List<String>
)

data class GitHubCheckCacheEntry(
    val loading: Boolean = false,
    val localVersion: String = "",
    val localVersionCode: Long = -1L,
    val latestTag: String = "",
    val latestStableRawTag: String = "",
    val latestPreRawTag: String = "",
    val hasUpdate: Boolean? = null,
    val message: String = "",
    val isPreRelease: Boolean = false,
    val preReleaseInfo: String = "",
    val showPreReleaseInfo: Boolean = false,
    val hasPreReleaseUpdate: Boolean = false
)

data class GitHubAtomReleaseEntry(
    val tag: String,
    val title: String,
    val link: String,
    val candidates: List<String>,
    val isLikelyPreRelease: Boolean
)
