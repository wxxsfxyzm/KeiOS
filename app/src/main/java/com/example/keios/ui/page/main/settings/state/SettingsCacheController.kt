package com.example.keios.ui.page.main.settings.state

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.keios.core.prefs.CacheEntrySummary
import com.example.keios.core.prefs.CacheStores
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Stable
internal class SettingsCacheController {
    var cacheEntries by mutableStateOf<List<CacheEntrySummary>?>(emptyList())
    var cacheEntriesLoading by mutableStateOf(false)
    var clearingCacheId by mutableStateOf<String?>(null)
    var clearingAllCaches by mutableStateOf(false)
    var cacheReloadSignal by mutableStateOf(0)

    fun requestCacheReload() {
        cacheReloadSignal += 1
    }
}

@Composable
internal fun rememberSettingsCacheController(
    context: Context,
    cacheDiagnosticsEnabled: Boolean
): SettingsCacheController {
    val controller = remember { SettingsCacheController() }
    LaunchedEffect(cacheDiagnosticsEnabled, controller.cacheReloadSignal) {
        if (!cacheDiagnosticsEnabled) {
            controller.cacheEntries = emptyList()
            controller.cacheEntriesLoading = false
            controller.clearingCacheId = null
            controller.clearingAllCaches = false
            return@LaunchedEffect
        }
        controller.cacheEntriesLoading = controller.cacheEntries == null
        controller.cacheEntries = withContext(Dispatchers.IO) { CacheStores.list(context) }
        controller.cacheEntriesLoading = false
    }
    return controller
}
