package com.example.keios.ui.page.main.student.catalog.state

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.example.keios.ui.page.main.host.pager.animateTabSwitch
import com.example.keios.ui.page.main.student.catalog.BaGuideCatalogTab
import com.example.keios.ui.page.main.widget.chrome.LiquidActionItem
import com.example.keios.ui.page.main.widget.motion.AppMotionTokens
import com.example.keios.ui.page.main.widget.motion.resolvedMotionDuration
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
internal fun rememberBaGuideCatalogTopBarActionItems(
    sortIcon: androidx.compose.ui.graphics.vector.ImageVector,
    refreshIcon: androidx.compose.ui.graphics.vector.ImageVector,
    sortActionContentDescription: String,
    refreshActionContentDescription: String,
    showSortPopup: Boolean,
    onShowSortPopupChange: (Boolean) -> Unit,
    onRefreshRequest: () -> Unit
): List<LiquidActionItem> {
    return remember(
        sortIcon,
        refreshIcon,
        sortActionContentDescription,
        refreshActionContentDescription,
        showSortPopup
    ) {
        listOf(
            LiquidActionItem(
                icon = sortIcon,
                contentDescription = sortActionContentDescription,
                onClick = { onShowSortPopupChange(!showSortPopup) }
            ),
            LiquidActionItem(
                icon = refreshIcon,
                contentDescription = refreshActionContentDescription,
                onClick = onRefreshRequest
            )
        )
    }
}

@Composable
internal fun rememberBaGuideCatalogTabSelectCoordinator(
    tabs: List<BaGuideCatalogTab>,
    pagerState: androidx.compose.foundation.pager.PagerState,
    transitionAnimationsEnabled: Boolean,
    farJumpAlpha: Animatable<Float, androidx.compose.animation.core.AnimationVector1D>,
    onSelectedTabIndexChange: (Int) -> Unit,
    onSortPopupChange: (Boolean) -> Unit
): (Int) -> Unit {
    val pagerScope = rememberCoroutineScope()
    var tabJumpJob by remember { mutableStateOf<Job?>(null) }

    return remember(
        tabs,
        pagerState,
        transitionAnimationsEnabled,
        farJumpAlpha,
        onSelectedTabIndexChange,
        onSortPopupChange,
        pagerScope
    ) {
        { index: Int ->
            if (tabs.isNotEmpty()) {
                val safeIndex = index.coerceIn(0, tabs.lastIndex)
                val stablePageIndex = if (pagerState.isScrollInProgress) {
                    pagerState.targetPage
                } else {
                    pagerState.settledPage
                }
                if (safeIndex != stablePageIndex) {
                    onSelectedTabIndexChange(safeIndex)
                    onSortPopupChange(false)
                    tabJumpJob?.cancel()
                    tabJumpJob = pagerScope.launch {
                        pagerState.animateTabSwitch(
                            fromIndex = stablePageIndex,
                            targetIndex = safeIndex,
                            animationsEnabled = transitionAnimationsEnabled,
                            onFarJumpBefore = {
                                farJumpAlpha.snapTo(1f)
                                farJumpAlpha.animateTo(
                                    targetValue = 0.92f,
                                    animationSpec = tween(
                                        durationMillis = resolvedMotionDuration(
                                            AppMotionTokens.farJumpDimMs,
                                            transitionAnimationsEnabled
                                        )
                                    )
                                )
                            },
                            onFarJumpAfter = {
                                farJumpAlpha.animateTo(
                                    targetValue = 1f,
                                    animationSpec = tween(
                                        durationMillis = resolvedMotionDuration(
                                            AppMotionTokens.farJumpRestoreMs,
                                            transitionAnimationsEnabled
                                        )
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
