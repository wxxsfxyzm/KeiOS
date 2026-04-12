package com.example.keios.core.prefs

import com.tencent.mmkv.MMKV

object UiPrefs {
    private const val KV_ID = "ui_prefs"
    private const val KEY_LIQUID_BOTTOM_BAR = "liquid_bottom_bar"
    private const val KEY_CARD_PRESS_FEEDBACK = "card_press_feedback"
    private const val KEY_HOME_ICON_HDR = "home_icon_hdr"
    private const val KEY_VISIBLE_BOTTOM_PAGES = "visible_bottom_pages"
    private val DEFAULT_VISIBLE_BOTTOM_PAGE_NAMES = setOf("Os", "Mcp", "GitHub", "Ba")

    private fun kv(): MMKV = MMKV.mmkvWithID(KV_ID)

    fun isLiquidBottomBarEnabled(defaultValue: Boolean = true): Boolean {
        return kv().decodeBool(KEY_LIQUID_BOTTOM_BAR, defaultValue)
    }

    fun setLiquidBottomBarEnabled(value: Boolean) {
        kv().encode(KEY_LIQUID_BOTTOM_BAR, value)
    }

    fun isCardPressFeedbackEnabled(defaultValue: Boolean = true): Boolean {
        return kv().decodeBool(KEY_CARD_PRESS_FEEDBACK, defaultValue)
    }

    fun setCardPressFeedbackEnabled(value: Boolean) {
        kv().encode(KEY_CARD_PRESS_FEEDBACK, value)
    }

    fun isHomeIconHdrEnabled(defaultValue: Boolean = true): Boolean {
        return kv().decodeBool(KEY_HOME_ICON_HDR, defaultValue)
    }

    fun setHomeIconHdrEnabled(value: Boolean) {
        kv().encode(KEY_HOME_ICON_HDR, value)
    }

    fun loadVisibleBottomPageNames(): Set<String> {
        val store = kv()
        if (!store.containsKey(KEY_VISIBLE_BOTTOM_PAGES)) return DEFAULT_VISIBLE_BOTTOM_PAGE_NAMES
        val raw = store.decodeString(KEY_VISIBLE_BOTTOM_PAGES, "").orEmpty()
        if (raw.isBlank()) return emptySet()
        return raw
            .split(',')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
    }

    fun saveVisibleBottomPageNames(names: Set<String>) {
        val normalized = names
            .filter { it.isNotBlank() && it != "Home" }
            .joinToString(separator = ",")
        kv().encode(KEY_VISIBLE_BOTTOM_PAGES, normalized)
    }
}
