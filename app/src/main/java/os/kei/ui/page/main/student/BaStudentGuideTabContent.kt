package os.kei.ui.page.main.student

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import os.kei.ui.page.main.student.catalog.BaGuideCatalogStore
import os.kei.ui.page.main.student.catalog.BaGuideCatalogTab
import os.kei.ui.page.main.student.fetch.extractGuideContentIdFromUrl
import os.kei.ui.page.main.widget.support.buildTextCopyPayload
import os.kei.ui.page.main.widget.support.copyModeAwareRow
import os.kei.ui.page.main.widget.support.rememberLightTextCopyAction
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
