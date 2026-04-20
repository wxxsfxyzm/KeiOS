package com.example.keios.ui.page.main.settings.section

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.res.stringResource
import com.example.keios.R
import com.example.keios.core.prefs.AppThemeMode
import com.example.keios.ui.page.main.os.appLucideAlertIcon
import com.example.keios.ui.page.main.os.appLucideConfigIcon
import com.example.keios.ui.page.main.os.appLucideLayersIcon
import com.example.keios.ui.page.main.os.appLucideTimeIcon
import com.example.keios.ui.page.main.os.osLucideCopyIcon
import com.example.keios.ui.page.main.settings.support.SettingsActionItem
import com.example.keios.ui.page.main.settings.support.SettingsGroupCard
import com.example.keios.ui.page.main.settings.support.SettingsToggleItem
import com.example.keios.ui.page.main.widget.glass.AppDropdownSelector
import com.example.keios.ui.page.main.widget.glass.GlassVariant

@Composable
internal fun SettingsVisualSection(
    preloadingEnabled: Boolean,
    onPreloadingEnabledChanged: (Boolean) -> Unit,
    homeIconHdrEnabled: Boolean,
    onHomeIconHdrChanged: (Boolean) -> Unit,
    appThemeMode: AppThemeMode,
    onAppThemeModeChanged: (AppThemeMode) -> Unit,
    showThemeModePopup: Boolean,
    onShowThemeModePopupChange: (Boolean) -> Unit,
    themePopupAnchorBounds: IntRect?,
    onThemePopupAnchorBoundsChange: (IntRect?) -> Unit,
    enabledCardColor: Color,
    disabledCardColor: Color
) {
    val visualGroupActive = preloadingEnabled || homeIconHdrEnabled
    val themeModeOptions = listOf(
        AppThemeMode.FOLLOW_SYSTEM to stringResource(R.string.settings_theme_follow_system),
        AppThemeMode.LIGHT to stringResource(R.string.settings_theme_light_mode),
        AppThemeMode.DARK to stringResource(R.string.settings_theme_dark_mode)
    )
    val currentThemeLabel =
        themeModeOptions.firstOrNull { it.first == appThemeMode }?.second
            ?: stringResource(R.string.settings_theme_follow_system)
    val themeSummary = when (appThemeMode) {
        AppThemeMode.FOLLOW_SYSTEM -> stringResource(R.string.settings_theme_summary_follow_system)
        AppThemeMode.LIGHT -> stringResource(R.string.settings_theme_summary_light)
        AppThemeMode.DARK -> stringResource(R.string.settings_theme_summary_dark)
    }
    SettingsGroupCard(
        header = stringResource(R.string.settings_group_visual_header),
        title = stringResource(R.string.settings_group_visual_title),
        sectionIcon = appLucideLayersIcon(),
        containerColor = if (visualGroupActive) enabledCardColor else disabledCardColor
    ) {
        SettingsActionItem(
            title = stringResource(R.string.settings_theme_mode_title),
            summary = themeSummary
        ) {
            AppDropdownSelector(
                selectedText = currentThemeLabel,
                options = themeModeOptions.map { it.second },
                selectedIndex = themeModeOptions.indexOfFirst { it.first == appThemeMode }
                    .coerceAtLeast(0),
                expanded = showThemeModePopup,
                anchorBounds = themePopupAnchorBounds,
                onExpandedChange = onShowThemeModePopupChange,
                onSelectedIndexChange = { selectedIndex ->
                    onAppThemeModeChanged(themeModeOptions[selectedIndex].first)
                },
                onAnchorBoundsChange = onThemePopupAnchorBoundsChange,
                backdrop = null,
                variant = GlassVariant.SheetAction
            )
        }
        SettingsToggleItem(
            title = stringResource(R.string.settings_preloading_title),
            summary = if (preloadingEnabled) {
                stringResource(R.string.settings_preloading_summary_enabled)
            } else {
                stringResource(R.string.settings_preloading_summary_disabled)
            },
            checked = preloadingEnabled,
            onCheckedChange = onPreloadingEnabledChanged,
            infoKey = stringResource(R.string.common_scope),
            infoValue = stringResource(R.string.settings_preloading_scope)
        )
        SettingsToggleItem(
            title = stringResource(R.string.settings_home_shine_title),
            summary = if (homeIconHdrEnabled) {
                stringResource(R.string.settings_home_shine_summary_enabled)
            } else {
                stringResource(R.string.settings_home_shine_summary_disabled)
            },
            checked = homeIconHdrEnabled,
            onCheckedChange = onHomeIconHdrChanged,
            infoKey = stringResource(R.string.common_scope),
            infoValue = stringResource(R.string.settings_home_shine_scope)
        )
    }
}

@Composable
internal fun SettingsAnimationSection(
    transitionAnimationsEnabled: Boolean,
    onTransitionAnimationsChanged: (Boolean) -> Unit,
    enabledCardColor: Color,
    disabledCardColor: Color
) {
    SettingsGroupCard(
        header = stringResource(R.string.settings_group_animation_header),
        title = stringResource(R.string.settings_group_animation_title),
        sectionIcon = appLucideTimeIcon(),
        containerColor = if (transitionAnimationsEnabled) enabledCardColor else disabledCardColor
    ) {
        SettingsToggleItem(
            title = stringResource(R.string.settings_transition_animations_title),
            summary = if (transitionAnimationsEnabled) {
                stringResource(R.string.settings_transition_animations_summary_enabled)
            } else {
                stringResource(R.string.settings_transition_animations_summary_disabled)
            },
            checked = transitionAnimationsEnabled,
            onCheckedChange = onTransitionAnimationsChanged,
            infoKey = stringResource(R.string.common_scope),
            infoValue = stringResource(R.string.settings_transition_animations_scope)
        )
    }
}

@Composable
internal fun SettingsComponentEffectsSection(
    liquidActionBarLayeredStyleEnabled: Boolean,
    onLiquidActionBarLayeredStyleChanged: (Boolean) -> Unit,
    liquidBottomBarEnabled: Boolean,
    onLiquidBottomBarChanged: (Boolean) -> Unit,
    liquidGlassSwitchEnabled: Boolean,
    onLiquidGlassSwitchChanged: (Boolean) -> Unit,
    cardPressFeedbackEnabled: Boolean,
    onCardPressFeedbackChanged: (Boolean) -> Unit,
    enabledCardColor: Color,
    disabledCardColor: Color
) {
    val componentEffectsGroupActive = liquidActionBarLayeredStyleEnabled ||
        liquidBottomBarEnabled ||
        liquidGlassSwitchEnabled ||
        cardPressFeedbackEnabled
    SettingsGroupCard(
        header = stringResource(R.string.settings_group_component_effects_header),
        title = stringResource(R.string.settings_group_component_effects_title),
        sectionIcon = appLucideConfigIcon(),
        containerColor = if (componentEffectsGroupActive) enabledCardColor else disabledCardColor
    ) {
        SettingsToggleItem(
            title = stringResource(R.string.settings_actionbar_style_title),
            summary = if (liquidActionBarLayeredStyleEnabled) {
                stringResource(R.string.settings_actionbar_style_summary_enabled)
            } else {
                stringResource(R.string.settings_actionbar_style_summary_disabled)
            },
            checked = liquidActionBarLayeredStyleEnabled,
            onCheckedChange = onLiquidActionBarLayeredStyleChanged,
            infoKey = stringResource(R.string.common_scope),
            infoValue = stringResource(R.string.settings_actionbar_style_scope)
        )
        SettingsToggleItem(
            title = stringResource(R.string.settings_bottom_bar_title),
            summary = stringResource(R.string.settings_bottom_bar_summary),
            checked = liquidBottomBarEnabled,
            onCheckedChange = onLiquidBottomBarChanged
        )
        SettingsToggleItem(
            title = stringResource(R.string.settings_liquid_switch_title),
            summary = if (liquidGlassSwitchEnabled) {
                stringResource(R.string.settings_liquid_switch_summary_enabled)
            } else {
                stringResource(R.string.settings_liquid_switch_summary_disabled)
            },
            checked = liquidGlassSwitchEnabled,
            onCheckedChange = onLiquidGlassSwitchChanged,
            infoKey = stringResource(R.string.common_scope),
            infoValue = stringResource(R.string.settings_liquid_switch_scope)
        )
        SettingsToggleItem(
            title = stringResource(R.string.settings_card_feedback_title),
            summary = if (cardPressFeedbackEnabled) {
                stringResource(R.string.settings_card_feedback_summary_enabled)
            } else {
                stringResource(R.string.settings_card_feedback_summary_disabled)
            },
            checked = cardPressFeedbackEnabled,
            onCheckedChange = onCardPressFeedbackChanged,
            infoKey = stringResource(R.string.common_scope),
            infoValue = stringResource(R.string.settings_card_feedback_scope)
        )
    }
}

@Composable
internal fun SettingsNotifySection(
    superIslandNotificationEnabled: Boolean,
    onSuperIslandNotificationChanged: (Boolean) -> Unit,
    superIslandBypassRestrictionEnabled: Boolean,
    onSuperIslandBypassRestrictionChanged: (Boolean) -> Unit,
    enabledCardColor: Color,
    disabledCardColor: Color
) {
    val notifyGroupActive = superIslandNotificationEnabled || superIslandBypassRestrictionEnabled
    SettingsGroupCard(
        header = stringResource(R.string.settings_group_notify_header),
        title = stringResource(R.string.settings_group_notify_title),
        sectionIcon = appLucideAlertIcon(),
        containerColor = if (notifyGroupActive) enabledCardColor else disabledCardColor
    ) {
        SettingsToggleItem(
            title = stringResource(R.string.settings_super_island_style_title),
            summary = if (superIslandNotificationEnabled) {
                stringResource(R.string.settings_super_island_style_summary_enabled)
            } else {
                stringResource(R.string.settings_super_island_style_summary_disabled)
            },
            checked = superIslandNotificationEnabled,
            onCheckedChange = onSuperIslandNotificationChanged,
            infoKey = stringResource(R.string.common_scope),
            infoValue = stringResource(R.string.settings_super_island_style_scope)
        )
        SettingsToggleItem(
            title = stringResource(R.string.settings_super_island_bypass_title),
            summary = if (superIslandBypassRestrictionEnabled) {
                stringResource(R.string.settings_super_island_bypass_summary_enabled)
            } else {
                stringResource(R.string.settings_super_island_bypass_summary_disabled)
            },
            checked = superIslandBypassRestrictionEnabled,
            onCheckedChange = onSuperIslandBypassRestrictionChanged,
            infoKey = stringResource(R.string.common_note),
            infoValue = stringResource(R.string.settings_super_island_bypass_note)
        )
    }
}

@Composable
internal fun SettingsCopySection(
    textCopyCapabilityExpanded: Boolean,
    onTextCopyCapabilityExpandedChanged: (Boolean) -> Unit,
    enabledCardColor: Color,
    disabledCardColor: Color
) {
    SettingsGroupCard(
        header = stringResource(R.string.settings_group_copy_header),
        title = stringResource(R.string.settings_group_copy_title),
        sectionIcon = osLucideCopyIcon(),
        containerColor = if (textCopyCapabilityExpanded) enabledCardColor else disabledCardColor
    ) {
        SettingsToggleItem(
            title = stringResource(R.string.settings_copy_capability_title),
            summary = if (textCopyCapabilityExpanded) {
                stringResource(R.string.settings_copy_capability_summary_enabled)
            } else {
                stringResource(R.string.settings_copy_capability_summary_disabled)
            },
            checked = textCopyCapabilityExpanded,
            onCheckedChange = onTextCopyCapabilityExpandedChanged,
            infoKey = stringResource(R.string.common_note),
            infoValue = if (textCopyCapabilityExpanded) {
                stringResource(R.string.settings_copy_capability_note_enabled)
            } else {
                stringResource(R.string.settings_copy_capability_note_disabled)
            }
        )
    }
}
