package com.example.keios.feature.github.data.remote

import com.example.keios.feature.github.model.GitHubAtomFeed
import com.example.keios.feature.github.model.GitHubAtomReleaseEntry
import com.example.keios.feature.github.model.GitHubLookupStrategyOption
import com.example.keios.feature.github.model.GitHubReleaseChannel
import com.example.keios.feature.github.model.GitHubReleaseSignalSource
import com.example.keios.feature.github.model.GitHubReleaseVersionSignals
import com.example.keios.feature.github.model.GitHubRepositoryReleaseSnapshot
import com.example.keios.feature.github.model.GitHubVersionCandidateSource
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.util.concurrent.TimeUnit

private data class GitHubApiCachedValue<T>(
    val value: T,
    val timestamp: Long
)

class GitHubApiTokenReleaseStrategy(
    private val apiToken: String
) : GitHubReleaseLookupStrategy {
    override val id: String = GitHubLookupStrategyOption.GitHubApiToken.storageId

    override fun loadSnapshot(owner: String, repo: String): Result<GitHubRepositoryReleaseSnapshot> {
        return runCatching {
            require(apiToken.isNotBlank()) { "GitHub API Token 方案需要先填写 GitHub API token" }
            val entries = fetchReleaseEntries(owner, repo).getOrThrow()
            val latestStableEntry = pickPreferredEntry(entries.filter { !it.isLikelyPreRelease })
                ?: pickPreferredEntry(entries)
                ?: error("no release entries")
            val latestPreEntry = pickPreferredEntry(entries.filter { it.isLikelyPreRelease })
            val updatedAt = entries.maxOfOrNull { it.updatedAtMillis ?: Long.MIN_VALUE }
                ?.takeIf { it > Long.MIN_VALUE }

            GitHubRepositoryReleaseSnapshot(
                strategyId = id,
                feed = GitHubAtomFeed(
                    title = "$owner/$repo releases",
                    feedUrl = buildApiUrl(owner, repo),
                    updatedAtMillis = updatedAt,
                    entries = entries
                ),
                latestStable = latestStableEntry.toReleaseSignal(),
                latestPreRelease = latestPreEntry?.toReleaseSignal()
            )
        }
    }

    fun fetchReleaseEntries(owner: String, repo: String, limit: Int = 30): Result<List<GitHubAtomReleaseEntry>> {
        val key = cacheKey(owner, repo)
        val now = System.currentTimeMillis()
        releaseCache[key]?.takeIf { now - it.timestamp < CACHE_TTL_MS }?.let { return it.value }

        val result = fetch(buildApiUrl(owner, repo)).map { body ->
            parseReleaseEntries(
                json = body,
                owner = owner,
                repo = repo
            ).take(limit)
        }
        releaseCache[key] = GitHubApiCachedValue(result, now)
        return result
    }

    override fun clearCaches() {
        clearSharedCaches()
    }

    private fun fetch(url: String): Result<String> = runCatching {
        val request = Request.Builder()
            .url(url)
            .get()
            .header("Accept", "application/vnd.github+json")
            .header("Authorization", "Bearer $apiToken")
            .header("X-GitHub-Api-Version", GITHUB_API_VERSION)
            .header("User-Agent", GITHUB_USER_AGENT)
            .build()

        githubClient.newCall(request).execute().use { response ->
            val bodyText = response.body.string()
            if (!response.isSuccessful) {
                error(buildErrorMessage(response.code, bodyText))
            }
            bodyText
        }
    }

    internal fun parseReleaseEntries(
        json: String,
        owner: String,
        repo: String
    ): List<GitHubAtomReleaseEntry> {
        val array = JSONArray(json)
        val entries = buildList {
            for (index in 0 until array.length()) {
                val release = array.optJSONObject(index) ?: continue
                if (release.optBoolean("draft", false)) continue

                val rawTag = release.optString("tag_name").trim()
                val name = release.optString("name").trim().ifBlank { rawTag }
                val htmlUrl = release.optString("html_url").trim().ifBlank {
                    GitHubVersionUtils.buildReleaseUrl(owner, repo)
                }
                val releaseId = release.opt("id")?.toString().orEmpty()
                val body = release.optString("body")
                val contentPreview = GitHubAtomHeuristics.buildContentPreview(body)
                val heuristicsChannel = GitHubAtomHeuristics.detectReleaseChannel(
                    tag = rawTag,
                    title = name,
                    contentPreview = contentPreview
                )
                val prereleaseFlag = release.optBoolean("prerelease", false)
                val channel = when {
                    prereleaseFlag && !heuristicsChannel.isPreRelease -> GitHubReleaseChannel.PREVIEW
                    else -> heuristicsChannel
                }
                val author = release.optJSONObject("author")
                val authorName = author?.optString("login").orEmpty().trim()
                val authorAvatarUrl = author?.optString("avatar_url").orEmpty().trim()
                val publishedAtMillis = release.optString("published_at").parseIsoInstantOrNull()
                    ?: release.optString("created_at").parseIsoInstantOrNull()

                add(
                    GitHubAtomReleaseEntry(
                        entryId = release.optString("node_id").ifBlank { releaseId },
                        tag = rawTag.ifBlank { name },
                        title = name,
                        link = htmlUrl,
                        updatedAtMillis = publishedAtMillis,
                        contentHtml = "",
                        contentText = body,
                        authorName = authorName,
                        authorAvatarUrl = authorAvatarUrl,
                        versionCandidates = GitHubVersionUtils.buildVersionCandidates(
                            GitHubVersionCandidateSource.Tag to rawTag,
                            GitHubVersionCandidateSource.Title to name,
                            GitHubVersionCandidateSource.Link to htmlUrl,
                            GitHubVersionCandidateSource.Id to releaseId,
                            GitHubVersionCandidateSource.Content to contentPreview
                        ),
                        channel = channel,
                        isLikelyPreRelease = prereleaseFlag || channel.isPreRelease
                    )
                )
            }
        }

        return entries.sortedWith(
            compareByDescending<GitHubAtomReleaseEntry> { it.updatedAtMillis ?: Long.MIN_VALUE }
                .thenByDescending { it.link }
        )
    }

    private fun buildApiUrl(owner: String, repo: String): String {
        return "https://api.github.com/repos/$owner/$repo/releases?per_page=30"
    }

    private fun cacheKey(owner: String, repo: String): String {
        return "${apiToken.hashCode()}|$owner/$repo"
    }

    private fun buildErrorMessage(code: Int, bodyText: String): String {
        val apiMessage = runCatching {
            JSONObject(bodyText).optString("message").trim()
        }.getOrDefault("")
        return when (code) {
            401 -> "GitHub API token 无效或已过期"
            403 -> "GitHub API 被拒绝访问${apiMessage.takeIf { it.isNotBlank() }?.let { ": $it" } ?: ""}"
            404 -> "仓库不存在，或当前 token 无权访问该仓库"
            else -> "GitHub API 请求失败 (HTTP $code${apiMessage.takeIf { it.isNotBlank() }?.let { ", $it" } ?: ""})"
        }
    }

    private fun pickPreferredEntry(entries: List<GitHubAtomReleaseEntry>): GitHubAtomReleaseEntry? {
        return entries.maxWithOrNull { left, right ->
            val byVersion = GitHubVersionUtils.compareStructuredCandidateSets(
                left.versionCandidates,
                right.versionCandidates
            )
            when {
                byVersion != null && byVersion != 0 -> byVersion
                else -> compareValues(
                    left.updatedAtMillis ?: Long.MIN_VALUE,
                    right.updatedAtMillis ?: Long.MIN_VALUE
                )
            }
        }
    }

    private fun GitHubAtomReleaseEntry.toReleaseSignal(): GitHubReleaseVersionSignals {
        return GitHubReleaseVersionSignals(
            displayVersion = displayVersion,
            rawTag = tag,
            rawName = title,
            link = link,
            updatedAtMillis = updatedAtMillis,
            versionCandidates = versionCandidates,
            source = GitHubReleaseSignalSource.GitHubApi,
            channel = channel,
            authorName = authorName
        )
    }

    private fun String.parseIsoInstantOrNull(): Long? {
        return runCatching {
            if (isBlank()) null else Instant.parse(this).toEpochMilli()
        }.getOrNull()
    }

    companion object {
        private const val CACHE_TTL_MS = 90_000L
        private const val GITHUB_API_VERSION = "2022-11-28"
        private const val GITHUB_USER_AGENT = "KeiOS-App/1.0 (Android)"

        private val releaseCache = mutableMapOf<String, GitHubApiCachedValue<Result<List<GitHubAtomReleaseEntry>>>>()

        private val githubClient: OkHttpClient by lazy {
            OkHttpClient.Builder()
                .callTimeout(10, TimeUnit.SECONDS)
                .connectTimeout(8, TimeUnit.SECONDS)
                .readTimeout(8, TimeUnit.SECONDS)
                .writeTimeout(8, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .followRedirects(true)
                .followSslRedirects(true)
                .fastFallback(true)
                .build()
        }

        fun clearSharedCaches() {
            releaseCache.clear()
        }
    }
}
