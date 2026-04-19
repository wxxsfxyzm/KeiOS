package com.example.keios.ui.page.main.student

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import com.example.keios.ui.page.main.student.catalog.BaGuideCatalogStore
import com.example.keios.ui.page.main.student.catalog.BaGuideCatalogTab
import com.example.keios.ui.page.main.student.fetch.extractGuideContentIdFromUrl
import com.example.keios.ui.page.main.widget.buildTextCopyPayload
import com.example.keios.ui.page.main.widget.copyModeAwareRow
import com.example.keios.ui.page.main.widget.rememberLightTextCopyAction
import java.util.concurrent.ConcurrentHashMap

internal val npcSatelliteGuideFlagCache = ConcurrentHashMap<Long, Boolean>()

internal fun isNpcSatelliteGuideSource(sourceUrl: String): Boolean {
    val contentId = extractGuideContentIdFromUrl(sourceUrl) ?: return false
    if (contentId <= 0L) return false
    npcSatelliteGuideFlagCache[contentId]?.let { return it }
    val bundle = BaGuideCatalogStore.loadBundle() ?: return false
    val isNpcSatellite = bundle.entries(BaGuideCatalogTab.NpcSatellite).any { entry ->
        entry.contentId == contentId
    }
    npcSatelliteGuideFlagCache[contentId] = isNpcSatellite
    return isNpcSatellite
}

internal fun buildGuideTabCopyPayload(key: String, value: String): String {
    return buildTextCopyPayload(key, value)
}

@Composable
internal fun rememberGuideTabCopyAction(copyPayload: String): () -> Unit {
    val quickCopyAction = rememberLightTextCopyAction(copyPayload)
    return remember(quickCopyAction) {
        { quickCopyAction?.invoke() }
    }
}

internal fun Modifier.guideTabCopyable(
    copyPayload: String,
    onClick: (() -> Unit)? = null
): Modifier = composed {
    this.copyModeAwareRow(
        copyPayload = copyPayload,
        onClick = onClick
    )
}
