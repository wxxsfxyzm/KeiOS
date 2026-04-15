package com.example.keios.ui.page.main.github.sheet

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.keios.feature.github.model.GitHubApiAuthMode
import com.example.keios.feature.github.model.GitHubApiCredentialStatus
import com.example.keios.feature.github.model.GitHubLookupConfig
import com.example.keios.feature.github.model.GitHubLookupStrategyOption
import com.example.keios.feature.github.model.GitHubStrategyBenchmarkReport
import com.example.keios.ui.page.main.GitHubCredentialStatusCard
import com.example.keios.ui.page.main.GitHubRecommendedTokenGuideCard
import com.example.keios.ui.page.main.GitHubStatusPalette
import com.example.keios.ui.page.main.GitHubStrategyBenchmarkCard
import com.example.keios.ui.page.main.GitHubStrategyDraftSummaryCard
import com.example.keios.ui.page.main.GitHubStrategyGuideCard
import com.example.keios.ui.page.main.buildGitHubFineGrainedTokenTemplateUrl
import com.example.keios.ui.page.main.githubFineGrainedPatDocsUrl
import com.example.keios.ui.page.main.githubRecommendedTokenGuide
import com.example.keios.ui.page.main.githubStrategyGuides
import com.example.keios.ui.page.main.widget.GlassIconButton
import com.example.keios.ui.page.main.widget.GlassSearchField
import com.example.keios.ui.page.main.widget.GlassTextButton
import com.example.keios.ui.page.main.widget.GlassVariant
import com.example.keios.ui.page.main.widget.SheetActionGroup
import com.example.keios.ui.page.main.widget.SheetContentColumn
import com.example.keios.ui.page.main.widget.SheetControlRow
import com.example.keios.ui.page.main.widget.SheetDescriptionText
import com.example.keios.ui.page.main.widget.SheetFieldBlock
import com.example.keios.ui.page.main.widget.SheetSectionCard
import com.example.keios.ui.page.main.widget.SheetSectionTitle
import com.example.keios.ui.page.main.widget.SnapshotWindowBottomSheet
import com.example.keios.ui.page.main.widget.StatusPill
import com.kyant.backdrop.backdrops.LayerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.Ok
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun GitHubStrategySheet(
    show: Boolean,
    backdrop: LayerBackdrop,
    lookupConfig: GitHubLookupConfig,
    selectedStrategyInput: GitHubLookupStrategyOption,
    githubApiTokenInput: String,
    showApiTokenPlainText: Boolean,
    credentialCheckRunning: Boolean,
    credentialCheckError: String?,
    credentialCheckStatus: GitHubApiCredentialStatus?,
    strategyBenchmarkRunning: Boolean,
    strategyBenchmarkError: String?,
    strategyBenchmarkReport: GitHubStrategyBenchmarkReport?,
    trackedCount: Int,
    recommendedTokenGuideExpanded: Boolean,
    onDismissRequest: () -> Unit,
    onApply: () -> Unit,
    onSelectedStrategyChange: (GitHubLookupStrategyOption) -> Unit,
    onTokenInputChange: (String) -> Unit,
    onToggleTokenVisibility: () -> Unit,
    onRunCredentialCheck: () -> Unit,
    onRunStrategyBenchmark: () -> Unit,
    onRecommendedTokenGuideExpandedChange: (Boolean) -> Unit,
    onOpenExternalUrl: (String, String) -> Unit
) {
    SnapshotWindowBottomSheet(
        show = show,
        title = "GitHub 抓取方案",
        onDismissRequest = onDismissRequest,
        startAction = {
            GlassIconButton(
                backdrop = backdrop,
                variant = GlassVariant.Bar,
                icon = MiuixIcons.Regular.Close,
                contentDescription = "关闭",
                onClick = onDismissRequest
            )
        },
        endAction = {
            GlassIconButton(
                backdrop = backdrop,
                variant = GlassVariant.Bar,
                icon = MiuixIcons.Regular.Ok,
                contentDescription = "保存方案",
                onClick = onApply
            )
        }
    ) {
        val sanitizedTokenInput = githubApiTokenInput.trim()
        val draftChanged = selectedStrategyInput != lookupConfig.selectedStrategy ||
            sanitizedTokenInput != lookupConfig.apiToken
        val tokenStatusLabel = when {
            selectedStrategyInput != GitHubLookupStrategyOption.GitHubApiToken -> "未使用"
            sanitizedTokenInput.isBlank() -> "游客"
            else -> "已填写"
        }
        val tokenStatusColor = when {
            selectedStrategyInput != GitHubLookupStrategyOption.GitHubApiToken ->
                MiuixTheme.colorScheme.onBackgroundVariant
            sanitizedTokenInput.isBlank() -> GitHubStatusPalette.PreRelease
            else -> GitHubStatusPalette.Update
        }
        val credentialAvailabilityLabel = when {
            credentialCheckRunning -> "检测中"
            credentialCheckStatus != null -> "可用"
            credentialCheckError != null -> "不可用"
            else -> "未检测"
        }
        val credentialAvailabilityColor = when {
            credentialCheckRunning -> MiuixTheme.colorScheme.primary
            credentialCheckStatus != null -> GitHubStatusPalette.Update
            credentialCheckError != null -> GitHubStatusPalette.Error
            else -> MiuixTheme.colorScheme.onBackgroundVariant
        }

        SheetContentColumn(verticalSpacing = 8.dp) {
            SheetSectionTitle("配置摘要")
            GitHubStrategyDraftSummaryCard(
                selectedStrategy = selectedStrategyInput,
                tokenInput = sanitizedTokenInput,
                trackedCount = trackedCount,
                changed = draftChanged
            )

            SheetSectionTitle("抓取方案")
            githubStrategyGuides.forEach { guide ->
                GitHubStrategyGuideCard(
                    guide = guide,
                    selected = selectedStrategyInput == guide.option,
                    onSelect = { onSelectedStrategyChange(guide.option) }
                )
            }

            if (selectedStrategyInput == GitHubLookupStrategyOption.GitHubApiToken) {
                SheetSectionTitle("凭证设置")
                SheetSectionCard {
                    SheetControlRow(label = "Token 状态") {
                        StatusPill(
                            label = tokenStatusLabel,
                            color = tokenStatusColor
                        )
                    }
                    SheetControlRow(label = "可用性") {
                        StatusPill(
                            label = credentialAvailabilityLabel,
                            color = credentialAvailabilityColor
                        )
                    }
                    SheetFieldBlock(
                        title = "GitHub API Token",
                        summary = if (sanitizedTokenInput.isBlank()) {
                            "当前走游客 API，可随时补 token"
                        } else {
                            "当前已填 token，可在此替换"
                        },
                        trailing = {
                            GlassTextButton(
                                backdrop = backdrop,
                                variant = GlassVariant.SheetAction,
                                text = if (showApiTokenPlainText) "隐藏 Token" else "显示 Token",
                                onClick = onToggleTokenVisibility
                            )
                        }
                    ) {
                        GlassSearchField(
                            value = githubApiTokenInput,
                            onValueChange = onTokenInputChange,
                            label = "GitHub API token（选填）",
                            backdrop = backdrop,
                            variant = GlassVariant.SheetInput,
                            singleLine = true,
                            visualTransformation = if (showApiTokenPlainText) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            }
                        )
                    }
                }
                SheetSectionTitle("立即验证")
                SheetSectionCard {
                    SheetActionGroup {
                        GlassTextButton(
                            backdrop = backdrop,
                            variant = GlassVariant.SheetAction,
                            text = if (credentialCheckRunning) "检测中..." else "检测当前凭证",
                            enabled = !credentialCheckRunning,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = onRunCredentialCheck
                        )
                    }
                    SheetDescriptionText(
                        text = "留空时自动走游客 API，适合少量追踪；若项目变多或遇到限流，再补本地 token 即可。token 仅保存在当前设备 MMKV。"
                    )
                    SheetDescriptionText(
                        text = "当前这套 GitHub API 检查与资源下载只需 Fine-grained PAT 的 `Contents: Read`。它既能读 release 元数据，也能走 assets API 下载 APK；若是 private 仓库，还需 token 本身有该仓库访问权。"
                    )
                    credentialCheckError?.let { error ->
                        SheetDescriptionText(
                            text = "凭证检测失败：$error"
                        )
                    }
                }
                credentialCheckStatus?.let { status ->
                    GitHubCredentialStatusCard(status = status)
                    SheetDescriptionText(
                        text = if (status.authMode == GitHubApiAuthMode.Guest) {
                            "当前游客 API 可用，但额度较低；追踪项目变多后建议补 token。"
                        } else {
                            "当前 token 已被 GitHub API 接受，但这不等于它对所有 private 仓库都有权限。"
                        }
                    )
                }
                GitHubStrategyBenchmarkSection(
                    backdrop = backdrop,
                    trackedCount = trackedCount,
                    strategyBenchmarkRunning = strategyBenchmarkRunning,
                    strategyBenchmarkError = strategyBenchmarkError,
                    strategyBenchmarkReport = strategyBenchmarkReport,
                    onRunStrategyBenchmark = onRunStrategyBenchmark
                )
                SheetDescriptionText(
                    text = "API 方案直接走 Releases API。首次体验可先用游客模式；长期追踪较多仓库时，补专用 token 会更稳。"
                )
                SheetSectionTitle("推荐新建")
                GitHubRecommendedTokenGuideCard(
                    guide = githubRecommendedTokenGuide,
                    expanded = recommendedTokenGuideExpanded,
                    onExpandedChange = onRecommendedTokenGuideExpandedChange
                )
                SheetActionGroup {
                    GlassTextButton(
                        backdrop = backdrop,
                        variant = GlassVariant.SheetAction,
                        text = "打开预填创建页",
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            onOpenExternalUrl(
                                buildGitHubFineGrainedTokenTemplateUrl(),
                                "无法打开 GitHub 创建页"
                            )
                        }
                    )
                    GlassTextButton(
                        backdrop = backdrop,
                        variant = GlassVariant.SheetAction,
                        text = "查看官方说明",
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            onOpenExternalUrl(
                                githubFineGrainedPatDocsUrl,
                                "无法打开官方说明"
                            )
                        }
                    )
                }
                SheetDescriptionText(
                    text = "建议单独创建 KeiOS 专用 Fine-grained token，不要复用 classic token，这样权限面更小。"
                )
            } else {
                SheetSectionTitle("方案说明")
                SheetSectionCard {
                    SheetDescriptionText(
                        text = "Atom 方案无需 Token，适合公开仓库和轻量检查；若你更看重稳定性、资源读取或 private 仓库支持，可切到 API。"
                    )
                }
                GitHubStrategyBenchmarkSection(
                    backdrop = backdrop,
                    trackedCount = trackedCount,
                    strategyBenchmarkRunning = strategyBenchmarkRunning,
                    strategyBenchmarkError = strategyBenchmarkError,
                    strategyBenchmarkReport = strategyBenchmarkReport,
                    onRunStrategyBenchmark = onRunStrategyBenchmark
                )
            }
        }
    }
}

@Composable
private fun GitHubStrategyBenchmarkSection(
    backdrop: LayerBackdrop,
    trackedCount: Int,
    strategyBenchmarkRunning: Boolean,
    strategyBenchmarkError: String?,
    strategyBenchmarkReport: GitHubStrategyBenchmarkReport?,
    onRunStrategyBenchmark: () -> Unit
) {
    SheetSectionTitle("本地对比")
    SheetSectionCard {
        SheetDescriptionText(
            text = if (trackedCount == 0) {
                "会用当前已追踪仓库做对比，最多抽 6 个样本；当前还没有可用样本。"
            } else {
                "会跑一轮冷启动和一轮缓存复测，直接对比 Atom、游客 API、Token API 的耗时与命中率。"
            }
        )
        if (trackedCount > 0) {
            SheetActionGroup {
                GlassTextButton(
                    backdrop = backdrop,
                    variant = GlassVariant.SheetAction,
                    text = if (strategyBenchmarkRunning) "对比中..." else "运行双策略对比",
                    enabled = !strategyBenchmarkRunning,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onRunStrategyBenchmark
                )
            }
        }
        strategyBenchmarkError?.let { error ->
            SheetDescriptionText(
                text = "对比测试失败：$error"
            )
        }
    }
    strategyBenchmarkReport?.results?.forEach { result ->
        GitHubStrategyBenchmarkCard(result = result)
    }
}
