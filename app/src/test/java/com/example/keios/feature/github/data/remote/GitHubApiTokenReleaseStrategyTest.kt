package com.example.keios.feature.github.data.remote

import com.example.keios.feature.github.model.GitHubReleaseChannel
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GitHubApiTokenReleaseStrategyTest {
    private val strategy = GitHubApiTokenReleaseStrategy("test-token")

    @Test
    fun `parser filters drafts and keeps newest release first`() {
        val entries = strategy.parseReleaseEntries(
            json = """
                [
                  {
                    "id": 1,
                    "node_id": "R_1",
                    "tag_name": "v1.1.0",
                    "name": "Version 1.1.0",
                    "html_url": "https://github.com/demo/app/releases/tag/v1.1.0",
                    "body": "Stable build",
                    "draft": false,
                    "prerelease": false,
                    "published_at": "2026-04-12T08:00:00Z",
                    "author": {
                      "login": "demo",
                      "avatar_url": "https://avatars.githubusercontent.com/u/1"
                    }
                  },
                  {
                    "id": 2,
                    "node_id": "R_2",
                    "tag_name": "v1.2.0-beta1",
                    "name": "Version 1.2.0 Beta 1",
                    "html_url": "https://github.com/demo/app/releases/tag/v1.2.0-beta1",
                    "body": "Preview build",
                    "draft": false,
                    "prerelease": true,
                    "published_at": "2026-04-13T08:00:00Z",
                    "author": {
                      "login": "demo",
                      "avatar_url": "https://avatars.githubusercontent.com/u/1"
                    }
                  },
                  {
                    "id": 3,
                    "node_id": "R_3",
                    "tag_name": "v1.3.0",
                    "name": "Version 1.3.0",
                    "html_url": "https://github.com/demo/app/releases/tag/v1.3.0",
                    "body": "Draft build",
                    "draft": true,
                    "prerelease": false,
                    "published_at": "2026-04-14T08:00:00Z",
                    "author": {
                      "login": "demo",
                      "avatar_url": "https://avatars.githubusercontent.com/u/1"
                    }
                  }
                ]
            """.trimIndent(),
            owner = "demo",
            repo = "app"
        )

        assertEquals(2, entries.size)
        assertEquals("v1.2.0-beta1", entries.first().tag)
        assertTrue(entries.first().isLikelyPreRelease)
        assertFalse(entries.last().isLikelyPreRelease)
    }

    @Test
    fun `prerelease flag upgrades stable looking tag to preview channel`() {
        val entry = strategy.parseReleaseEntries(
            json = """
                [
                  {
                    "id": 9,
                    "node_id": "R_9",
                    "tag_name": "v2.0.0",
                    "name": "Version 2.0.0",
                    "html_url": "https://github.com/demo/app/releases/tag/v2.0.0",
                    "body": "Candidate build",
                    "draft": false,
                    "prerelease": true,
                    "published_at": "2026-04-13T12:00:00Z",
                    "author": {
                      "login": "demo",
                      "avatar_url": "https://avatars.githubusercontent.com/u/1"
                    }
                  }
                ]
            """.trimIndent(),
            owner = "demo",
            repo = "app"
        ).single()

        assertTrue(entry.isLikelyPreRelease)
        assertEquals(GitHubReleaseChannel.PREVIEW, entry.channel)
    }
}
