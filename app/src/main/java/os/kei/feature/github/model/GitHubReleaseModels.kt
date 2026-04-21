package os.kei.feature.github.model

enum class GitHubReleaseChannel(val isPreRelease: Boolean) {
    DEV(true),
    ALPHA(true),
    BETA(true),
    RC(true),
    PREVIEW(true),
    STABLE(false),
    UNKNOWN(false)
}

enum class GitHubReleaseSignalSource {
    LatestRedirect,
    AtomEntry,
    AtomFallback,
    GitHubApi
}

enum class GitHubVersionCandidateSource(val priority: Int) {
    Tag(0),
    Title(1),
    Link(2),
    Id(3),
    Content(4)
}

enum class GitHubTrackedReleaseStatus(val defaultMessage: String) {
    UpdateAvailable("发现更新"),
    PreReleaseUpdateAvailable("预发有更新"),
    PreReleaseOptional("预发可选"),
    PreReleaseTracked("预发行"),
    UpToDate("已是最新"),
    MatchedRelease("已匹配发行"),
    ComparisonUncertain("版本格式无法精确比较"),
    Failed("检查失败")
}

data class GitHubVersionCandidate(
    val value: String,
    val source: GitHubVersionCandidateSource
)

data class GitHubReleaseVersionSignals(
    val displayVersion: String,
    val rawTag: String,
    val rawName: String,
    val link: String = "",
    val updatedAtMillis: Long? = null,
    val versionCandidates: List<GitHubVersionCandidate> = emptyList(),
    val source: GitHubReleaseSignalSource = GitHubReleaseSignalSource.AtomFallback,
    val channel: GitHubReleaseChannel = GitHubReleaseChannel.UNKNOWN,
    val authorName: String = ""
) {
    val candidates: List<String>
        get() = versionCandidates.map { it.value }
}

data class GitHubAtomReleaseEntry(
    val entryId: String = "",
    val tag: String,
    val title: String,
    val link: String,
    val updatedAtMillis: Long? = null,
    val contentHtml: String = "",
    val contentText: String = "",
    val authorName: String = "",
    val authorAvatarUrl: String = "",
    val versionCandidates: List<GitHubVersionCandidate> = emptyList(),
    val channel: GitHubReleaseChannel = GitHubReleaseChannel.UNKNOWN,
    val isLikelyPreRelease: Boolean
) {
    val displayVersion: String
        get() = title.ifBlank { tag }

    val candidates: List<String>
        get() = versionCandidates.map { it.value }
}

data class GitHubAtomFeed(
    val title: String = "",
    val feedUrl: String = "",
    val updatedAtMillis: Long? = null,
    val entries: List<GitHubAtomReleaseEntry> = emptyList()
)

data class GitHubRepositoryReleaseSnapshot(
    val strategyId: String,
    val feed: GitHubAtomFeed,
    val latestStable: GitHubReleaseVersionSignals,
    val hasStableRelease: Boolean = true,
    val latestPreRelease: GitHubReleaseVersionSignals? = null,
    val fetchedAtMillis: Long = System.currentTimeMillis()
)

data class GitHubTrackedReleaseCheck(
    val strategyId: String,
    val localVersion: String,
    val localVersionCode: Long = -1L,
    val matchedRelease: GitHubAtomReleaseEntry? = null,
    val stableRelease: GitHubReleaseVersionSignals? = null,
    val preRelease: GitHubReleaseVersionSignals? = null,
    val hasStableRelease: Boolean = true,
    val hasUpdate: Boolean? = null,
    val hasPreReleaseUpdate: Boolean = false,
    val recommendsPreRelease: Boolean = false,
    val isPreReleaseInstalled: Boolean = false,
    val preReleaseInfo: String = "",
    val showPreReleaseInfo: Boolean = false,
    val releaseHint: String = "",
    val status: GitHubTrackedReleaseStatus = GitHubTrackedReleaseStatus.ComparisonUncertain,
    val message: String = status.defaultMessage
)
