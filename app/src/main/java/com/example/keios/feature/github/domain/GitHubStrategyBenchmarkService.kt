package com.example.keios.feature.github.domain

import com.example.keios.feature.github.data.remote.GitHubApiTokenReleaseStrategy
import com.example.keios.feature.github.data.remote.GitHubAtomReleaseStrategy
import com.example.keios.feature.github.model.GitHubRepoTarget
import com.example.keios.feature.github.model.GitHubStrategyBenchmarkReport
import com.example.keios.feature.github.model.GitHubStrategyBenchmarkResult
import com.example.keios.feature.github.model.GitHubStrategyBenchmarkSample
import com.example.keios.feature.github.model.GitHubStrategyLoadTrace
import com.example.keios.feature.github.model.GitHubTrackedApp
import com.example.keios.feature.github.model.GitHubRepositoryReleaseSnapshot

object GitHubStrategyBenchmarkService {
    private const val DEFAULT_TARGET_LIMIT = 6

    fun buildTargets(
        trackedItems: List<GitHubTrackedApp>,
        limit: Int = DEFAULT_TARGET_LIMIT
    ): List<GitHubRepoTarget> {
        return trackedItems
            .map { GitHubRepoTarget(owner = it.owner, repo = it.repo) }
            .distinctBy { it.id }
            .take(limit)
    }

    fun compareTargets(
        targets: List<GitHubRepoTarget>,
        apiToken: String = ""
    ): GitHubStrategyBenchmarkReport {
        val distinctTargets = targets
            .distinctBy { it.id }
            .take(DEFAULT_TARGET_LIMIT)
        if (distinctTargets.isEmpty()) {
            return GitHubStrategyBenchmarkReport(
                targets = emptyList(),
                results = emptyList()
            )
        }

        val apiStrategy = GitHubApiTokenReleaseStrategy(apiToken = apiToken.trim())
        val runners = listOf(
            StrategyRunner(
                strategyId = GitHubAtomReleaseStrategy.id,
                displayName = "Atom",
                clearCaches = { GitHubAtomReleaseStrategy.clearCaches() },
                load = { target -> GitHubAtomReleaseStrategy.loadSnapshotTrace(target.owner, target.repo) }
            ),
            StrategyRunner(
                strategyId = apiStrategy.id,
                displayName = "API",
                clearCaches = { apiStrategy.clearCaches() },
                load = { target -> apiStrategy.loadSnapshotTrace(target.owner, target.repo) }
            )
        )

        return GitHubStrategyBenchmarkReport(
            targets = distinctTargets,
            results = runners.map { runner -> runner.run(distinctTargets) }
        )
    }

    private data class StrategyRunner(
        val strategyId: String,
        val displayName: String,
        val clearCaches: () -> Unit,
        val load: (GitHubRepoTarget) -> GitHubStrategyLoadTrace<GitHubRepositoryReleaseSnapshot>
    ) {
        fun run(targets: List<GitHubRepoTarget>): GitHubStrategyBenchmarkResult {
            clearCaches()
            val coldSamples = targets.map { target -> load(target).toSample(target) }
            val warmSamples = targets.map { target -> load(target).toSample(target) }
            val authMode = coldSamples.firstOrNull()?.authMode ?: warmSamples.firstOrNull()?.authMode

            return GitHubStrategyBenchmarkResult(
                strategyId = strategyId,
                displayName = displayName,
                authMode = authMode,
                coldSamples = coldSamples.map { it.sample },
                warmSamples = warmSamples.map { it.sample }
            )
        }
    }

    private data class SampleEnvelope(
        val sample: GitHubStrategyBenchmarkSample,
        val authMode: com.example.keios.feature.github.model.GitHubApiAuthMode?
    )

    private fun GitHubStrategyLoadTrace<GitHubRepositoryReleaseSnapshot>.toSample(
        target: GitHubRepoTarget
    ): SampleEnvelope {
        val errorMessage = result.exceptionOrNull()?.message.orEmpty()
        return SampleEnvelope(
            sample = GitHubStrategyBenchmarkSample(
                target = target,
                success = result.isSuccess,
                fromCache = fromCache,
                elapsedMs = elapsedMs,
                message = errorMessage
            ),
            authMode = authMode
        )
    }
}
