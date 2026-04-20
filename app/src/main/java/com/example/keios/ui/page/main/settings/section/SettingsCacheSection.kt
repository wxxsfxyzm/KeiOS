package com.example.keios.ui.page.main.settings.section

import android.widget.Toast
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.keios.R
import com.example.keios.core.prefs.CacheEntrySummary
import com.example.keios.core.prefs.CacheStores
import com.example.keios.ui.page.main.os.appLucidePackageIcon
import com.example.keios.ui.page.main.settings.support.SettingsCacheRow
import com.example.keios.ui.page.main.settings.support.SettingsGroupCard
import com.example.keios.ui.page.main.settings.support.SettingsToggleItem
import com.example.keios.ui.page.main.widget.core.AppTypographyTokens
import com.example.keios.ui.page.main.widget.glass.GlassTextButton
import com.example.keios.ui.page.main.widget.glass.GlassVariant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun SettingsCacheSection(
    cacheDiagnosticsEnabled: Boolean,
    onCacheDiagnosticsChanged: (Boolean) -> Unit,
    cacheEntries: List<CacheEntrySummary>?,
    cacheEntriesLoading: Boolean,
    clearingAllCaches: Boolean,
    onClearingAllCachesChange: (Boolean) -> Unit,
    clearingCacheId: String?,
    onClearingCacheIdChange: (String?) -> Unit,
    onCacheReload: () -> Unit,
    enabledCardColor: Color,
    disabledCardColor: Color
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val subtitleColor = MiuixTheme.colorScheme.onBackgroundVariant.copy(alpha = 0.90f)
    SettingsGroupCard(
        header = stringResource(R.string.settings_cache_header),
        title = stringResource(R.string.settings_cache_diagnostics_title),
        sectionIcon = appLucidePackageIcon(),
        containerColor = if (cacheDiagnosticsEnabled) enabledCardColor else disabledCardColor
    ) {
        SettingsToggleItem(
            title = stringResource(R.string.settings_cache_diagnostics_title),
            summary = if (cacheDiagnosticsEnabled) {
                stringResource(R.string.settings_cache_diagnostics_summary_enabled)
            } else {
                stringResource(R.string.settings_cache_diagnostics_summary_disabled)
            },
            checked = cacheDiagnosticsEnabled,
            onCheckedChange = onCacheDiagnosticsChanged,
            infoKey = stringResource(R.string.common_scope),
            infoValue = if (cacheDiagnosticsEnabled) {
                stringResource(R.string.settings_cache_scope_enabled)
            } else {
                stringResource(R.string.settings_cache_scope_disabled)
            }
        )
        when {
            !cacheDiagnosticsEnabled -> {
                Text(
                    text = stringResource(R.string.settings_cache_disabled_desc),
                    color = subtitleColor,
                    fontSize = AppTypographyTokens.Supporting.fontSize,
                    lineHeight = AppTypographyTokens.Supporting.lineHeight
                )
            }
            cacheEntries == null && cacheEntriesLoading -> {
                Text(
                    text = stringResource(R.string.settings_cache_loading_desc),
                    color = subtitleColor,
                    fontSize = AppTypographyTokens.Supporting.fontSize,
                    lineHeight = AppTypographyTokens.Supporting.lineHeight
                )
            }
            cacheEntries.isNullOrEmpty() -> {
                Text(
                    text = stringResource(R.string.settings_cache_empty_desc),
                    color = subtitleColor,
                    fontSize = AppTypographyTokens.Supporting.fontSize,
                    lineHeight = AppTypographyTokens.Supporting.lineHeight
                )
            }
            else -> {
                GlassTextButton(
                    backdrop = null,
                    variant = GlassVariant.SheetDangerAction,
                    text = if (clearingAllCaches) {
                        stringResource(R.string.common_processing)
                    } else {
                        stringResource(R.string.settings_cache_action_clear_all)
                    },
                    modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                    textColor = MiuixTheme.colorScheme.error,
                    enabled = !clearingAllCaches && clearingCacheId == null,
                    onClick = {
                        scope.launch {
                            onClearingAllCachesChange(true)
                            val result = withContext(Dispatchers.IO) {
                                runCatching { CacheStores.clearAll(context) }
                            }
                            onClearingAllCachesChange(false)
                            if (result.isSuccess) {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.settings_cache_toast_cleared_all),
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                val reason = result.exceptionOrNull()?.javaClass?.simpleName
                                    ?: context.getString(R.string.common_unknown)
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.settings_cache_toast_clear_all_failed, reason),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            onCacheReload()
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                cacheEntries.forEachIndexed { index, entry ->
                    SettingsCacheRow(
                        entry = entry,
                        clearing = clearingAllCaches || clearingCacheId == entry.id,
                        onClear = {
                            if (clearingAllCaches || clearingCacheId != null) return@SettingsCacheRow
                            scope.launch {
                                onClearingCacheIdChange(entry.id)
                                try {
                                    withContext(Dispatchers.IO) {
                                        CacheStores.clear(context, entry.id)
                                    }
                                    onCacheReload()
                                } finally {
                                    onClearingCacheIdChange(null)
                                }
                            }
                        }
                    )
                    if (index < cacheEntries.lastIndex) {
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }
    }
}
