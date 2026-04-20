package com.example.keios.ui.page.main.host.main

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import com.example.keios.ui.page.main.student.fetch.extractGuideContentIdFromUrl
import com.example.keios.ui.page.main.student.fetch.normalizeGuideUrl

@Composable
internal fun rememberMainScreenOpenGuideDetailAction(
    poolGuideMissingText: String,
    onNavigateToCanonicalGuide: (String) -> Unit
): (String) -> Unit {
    val context = LocalContext.current
    val latestNavigate by rememberUpdatedState(onNavigateToCanonicalGuide)
    val latestMissingToastText by rememberUpdatedState(poolGuideMissingText)
    return remember(context) {
        { rawUrl ->
            val normalized = normalizeGuideUrl(rawUrl)
            val contentId = if (normalized.isBlank()) null else extractGuideContentIdFromUrl(normalized)
            if (contentId == null || contentId <= 0L) {
                Toast.makeText(context, latestMissingToastText, Toast.LENGTH_SHORT).show()
            } else {
                latestNavigate("https://www.gamekee.com/ba/tj/$contentId.html")
            }
        }
    }
}
