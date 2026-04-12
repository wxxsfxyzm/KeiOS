package com.example.keios.feature.github.data.remote

import com.example.keios.feature.github.model.GitHubAtomFeed
import com.example.keios.feature.github.model.GitHubAtomReleaseEntry
import com.example.keios.feature.github.model.GitHubReleaseSignalSource
import com.example.keios.feature.github.model.GitHubReleaseVersionSignals
import com.example.keios.feature.github.model.GitHubRepositoryReleaseSnapshot
import com.example.keios.feature.github.model.GitHubVersionCandidateSource
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.net.URLDecoder
import java.time.Instant
import java.util.concurrent.TimeUnit

private data class CachedValue<T>(
    val value: T,
    val timestamp: Long
)

object GitHubAtomReleaseStrategy : GitHubReleaseLookupStrategy {
    override val id: String = "atom_feed"

    private const val CACHE_TTL_MS = 90_000L
    private const val GITHUB_USER_AGENT = "KeiOS-App/1.0 (Android)"

    private val feedCache = mutableMapOf<String, CachedValue<Result<GitHubAtomFeed>>>()
    private val stableCache = mutableMapOf<String, CachedValue<Result<GitHubReleaseVersionSignals>>>()

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

    private val githubNoRedirectClient: OkHttpClient by lazy {
        githubClient.newBuilder()
            .followRedirects(false)
            .followSslRedirects(false)
            .build()
    }

    override fun loadSnapshot(owner: String, repo: String): Result<GitHubRepositoryReleaseSnapshot> {
        return runCatching {
            val feed = fetchAtomFeed(owner, repo).getOrThrow()
            val latestStable = fetchLatestStableSignal(owner, repo, feed).getOrThrow()
            val latestPre = pickPreferredEntry(feed.entries.filter { it.isLikelyPreRelease })
                ?.toReleaseSignal(GitHubReleaseSignalSource.AtomEntry)

            GitHubRepositoryReleaseSnapshot(
                strategyId = id,
                feed = feed,
                latestStable = latestStable,
                latestPreRelease = latestPre
            )
        }
    }

    fun fetchAtomFeed(owner: String, repo: String): Result<GitHubAtomFeed> {
        val key = "$owner/$repo"
        val now = System.currentTimeMillis()
        feedCache[key]?.takeIf { now - it.timestamp < CACHE_TTL_MS }?.let { return it.value }

        val url = "https://github.com/$owner/$repo/releases.atom"
        val result = fetch(url).map { body ->
            parseAtomFeed(xml = body, feedUrl = url)
        }
        feedCache[key] = CachedValue(result, now)
        return result
    }

    fun fetchReleaseEntries(owner: String, repo: String, limit: Int = 30): Result<List<GitHubAtomReleaseEntry>> {
        return fetchAtomFeed(owner, repo).map { it.entries.take(limit) }
    }

    private fun fetchLatestStableSignal(
        owner: String,
        repo: String,
        feed: GitHubAtomFeed
    ): Result<GitHubReleaseVersionSignals> {
        val key = "$owner/$repo"
        val now = System.currentTimeMillis()
        stableCache[key]?.takeIf { now - it.timestamp < CACHE_TTL_MS }?.let { return it.value }

        val request = Request.Builder()
            .url("https://github.com/$owner/$repo/releases/latest")
            .get()
            .header("User-Agent", GITHUB_USER_AGENT)
            .build()

        val result = runCatching {
            githubNoRedirectClient.newCall(request).execute().use { response ->
                val location = response.header("Location").orEmpty()
                val finalUrl = when {
                    location.isNotBlank() -> location
                    response.request.url.toString().contains("/releases/tag/") -> response.request.url.toString()
                    else -> ""
                }

                if (finalUrl.contains("/releases/tag/")) {
                    val rawTag = URLDecoder.decode(
                        finalUrl.substringAfterLast("/releases/tag/").trim('/'),
                        Charsets.UTF_8.name()
                    )
                    val matchedEntry = feed.entries.firstOrNull { entry ->
                        entry.tag.equals(rawTag, ignoreCase = true) ||
                            GitHubVersionUtils.compareCandidateSetsWithSources(
                                GitHubVersionUtils.normalizeVersionCandidates(rawTag),
                                entry.versionCandidates
                            ) == 0
                    }
                    matchedEntry?.toReleaseSignal(
                        source = GitHubReleaseSignalSource.LatestRedirect,
                        linkOverride = finalUrl
                    ) ?: GitHubReleaseVersionSignals(
                        displayVersion = rawTag,
                        rawTag = rawTag,
                        rawName = rawTag,
                        link = finalUrl,
                        updatedAtMillis = feed.updatedAtMillis,
                        versionCandidates = GitHubVersionUtils.buildVersionCandidates(
                            GitHubVersionCandidateSource.Tag to rawTag
                        ),
                        source = GitHubReleaseSignalSource.LatestRedirect,
                        channel = GitHubAtomHeuristics.detectReleaseChannel(rawTag, rawTag, ""),
                        authorName = ""
                    )
                } else {
                    pickPreferredEntry(feed.entries.filter { !it.isLikelyPreRelease })
                        ?.toReleaseSignal(GitHubReleaseSignalSource.AtomFallback)
                        ?: pickPreferredEntry(feed.entries)
                            ?.toReleaseSignal(GitHubReleaseSignalSource.AtomFallback)
                        ?: error("no release entries")
                }
            }
        }

        stableCache[key] = CachedValue(result, now)
        return result
    }

    override fun clearCaches() {
        feedCache.clear()
        stableCache.clear()
    }

    private fun fetch(url: String): Result<String> = runCatching {
        val request = Request.Builder()
            .url(url)
            .get()
            .header("User-Agent", GITHUB_USER_AGENT)
            .build()
        githubClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("HTTP ${response.code}")
            response.body.string()
        }
    }

    private fun parseAtomFeed(
        xml: String,
        feedUrl: String
    ): GitHubAtomFeed {
        val parser = XmlPullParserFactory.newInstance().newPullParser().apply {
            setInput(xml.reader())
        }

        var feedTitle = ""
        var feedUpdatedAt: Long? = null
        val entries = mutableListOf<GitHubAtomReleaseEntry>()

        var eventType = parser.eventType
        var inEntry = false
        var inAuthor = false

        var entryId = ""
        var entryUpdatedText = ""
        var entryLink = ""
        var entryTitle = ""
        var entryContentHtml = ""
        var entryAuthorName = ""
        var entryAuthorAvatarUrl = ""

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    val name = parser.name.orEmpty()
                    when {
                        name == "entry" -> {
                            inEntry = true
                            inAuthor = false
                            entryId = ""
                            entryUpdatedText = ""
                            entryLink = ""
                            entryTitle = ""
                            entryContentHtml = ""
                            entryAuthorName = ""
                            entryAuthorAvatarUrl = ""
                        }

                        name == "author" && inEntry -> inAuthor = true

                        name == "title" && !inEntry -> {
                            feedTitle = GitHubAtomHeuristics.decodeXmlEscapes(parser.nextText()).trim()
                        }

                        name == "updated" && !inEntry -> {
                            feedUpdatedAt = parser.nextText().trim().parseIsoInstantOrNull()
                        }

                        name == "id" && inEntry -> {
                            entryId = GitHubAtomHeuristics.decodeXmlEscapes(parser.nextText()).trim()
                        }

                        name == "updated" && inEntry -> {
                            entryUpdatedText = parser.nextText().trim()
                        }

                        name == "title" && inEntry -> {
                            entryTitle = GitHubAtomHeuristics.decodeXmlEscapes(parser.nextText()).trim()
                        }

                        name == "content" && inEntry -> {
                            entryContentHtml = GitHubAtomHeuristics.decodeXmlEscapes(parser.nextText()).trim()
                        }

                        name == "name" && inEntry && inAuthor -> {
                            entryAuthorName = GitHubAtomHeuristics.decodeXmlEscapes(parser.nextText()).trim()
                        }

                        name == "link" && inEntry -> {
                            val rel = parser.getAttributeValue(null, "rel").orEmpty()
                            val href = parser.getAttributeValue(null, "href").orEmpty()
                            if (rel == "alternate" && href.isNotBlank()) {
                                entryLink = href
                            }
                        }

                        name.endsWith("thumbnail") && inEntry -> {
                            entryAuthorAvatarUrl = parser.getAttributeValue(null, "url").orEmpty()
                        }
                    }
                }

                XmlPullParser.END_TAG -> {
                    val name = parser.name.orEmpty()
                    when {
                        name == "author" && inEntry -> inAuthor = false
                        name == "entry" && inEntry -> {
                            val effectiveLink = entryLink.ifBlank { entryId }
                            val derivedTag = extractTag(effectiveLink, entryTitle, entryId)
                            if (derivedTag.isNotBlank()) {
                                val contentText = GitHubAtomHeuristics.htmlToPlainText(entryContentHtml)
                                val contentPreview = GitHubAtomHeuristics.buildContentPreview(contentText)
                                val channel = GitHubAtomHeuristics.detectReleaseChannel(
                                    tag = derivedTag,
                                    title = entryTitle,
                                    contentPreview = contentPreview
                                )
                                entries += GitHubAtomReleaseEntry(
                                    entryId = entryId,
                                    tag = derivedTag,
                                    title = entryTitle.ifBlank { derivedTag },
                                    link = effectiveLink,
                                    updatedAtMillis = entryUpdatedText.parseIsoInstantOrNull(),
                                    contentHtml = entryContentHtml,
                                    contentText = contentText,
                                    authorName = entryAuthorName,
                                    authorAvatarUrl = entryAuthorAvatarUrl,
                                    versionCandidates = GitHubVersionUtils.buildVersionCandidates(
                                        GitHubVersionCandidateSource.Tag to derivedTag,
                                        GitHubVersionCandidateSource.Title to entryTitle,
                                        GitHubVersionCandidateSource.Link to effectiveLink,
                                        GitHubVersionCandidateSource.Id to entryId.substringAfterLast('/'),
                                        GitHubVersionCandidateSource.Content to contentPreview
                                    ),
                                    channel = channel,
                                    isLikelyPreRelease = channel.isPreRelease
                                )
                            }
                            inEntry = false
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        val sortedEntries = entries.sortedWith(
            compareByDescending<GitHubAtomReleaseEntry> { it.updatedAtMillis ?: Long.MIN_VALUE }
                .thenByDescending { it.link }
        )

        return GitHubAtomFeed(
            title = feedTitle,
            feedUrl = feedUrl,
            updatedAtMillis = feedUpdatedAt,
            entries = sortedEntries
        )
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

    private fun extractTag(
        link: String,
        title: String,
        entryId: String
    ): String {
        return when {
            link.contains("/releases/tag/") -> URLDecoder.decode(
                link.substringAfter("/releases/tag/").trim('/'),
                Charsets.UTF_8.name()
            )

            entryId.isNotBlank() -> entryId.substringAfterLast('/').trim()
            else -> title.trim()
        }
    }

    private fun GitHubAtomReleaseEntry.toReleaseSignal(
        source: GitHubReleaseSignalSource,
        linkOverride: String? = null
    ): GitHubReleaseVersionSignals {
        return GitHubReleaseVersionSignals(
            displayVersion = displayVersion,
            rawTag = tag,
            rawName = title,
            link = linkOverride ?: link,
            updatedAtMillis = updatedAtMillis,
            versionCandidates = versionCandidates,
            source = source,
            channel = channel,
            authorName = authorName
        )
    }

    private fun String.parseIsoInstantOrNull(): Long? {
        return runCatching { Instant.parse(this).toEpochMilli() }.getOrNull()
    }
}
