package com.example.keios.feature.github.data.remote

import com.example.keios.feature.github.model.GitHubRepositoryReleaseSnapshot

interface GitHubReleaseLookupStrategy {
    val id: String

    fun loadSnapshot(owner: String, repo: String): Result<GitHubRepositoryReleaseSnapshot>

    fun clearCaches()
}
