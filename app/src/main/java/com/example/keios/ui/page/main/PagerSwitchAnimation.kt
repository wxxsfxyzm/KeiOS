package com.example.keios.ui.page.main

import androidx.compose.foundation.pager.PagerState
import kotlin.math.abs

internal suspend fun PagerState.animateTabSwitch(
    fromIndex: Int,
    targetIndex: Int
) {
    val total = pageCount
    if (total <= 0) return

    val from = fromIndex.coerceIn(0, total - 1)
    val target = targetIndex.coerceIn(0, total - 1)
    if (target == from && !isScrollInProgress) return

    val distance = abs(target - from)
    if (distance <= 1) {
        animateScrollToPage(target)
        return
    }

    val nearTarget = if (target > from) target - 1 else target + 1
    val anchor = nearTarget.coerceIn(0, total - 1)

    if (currentPage != anchor || isScrollInProgress) {
        scrollToPage(anchor)
    }
    animateScrollToPage(target)
}
