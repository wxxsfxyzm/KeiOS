package os.kei.ui.page.main.os

import com.tencent.mmkv.MMKV

internal object OsCardVisibilityStore {
    private const val KV_ID = "os_card_visibility_state"
    private const val LEGACY_KV_ID = "os_ui_state"
    private const val KEY_VISIBLE_CARDS = "visible_os_cards"
    private val DEFAULT_VISIBLE = OsSectionCard.entries.map { it.name }.toSet()
    private val store: MMKV by lazy { MMKV.mmkvWithID(KV_ID) }
    private val legacyStore: MMKV by lazy { MMKV.mmkvWithID(LEGACY_KV_ID) }

    private fun resolveVisibleCards(raw: String): Set<OsSectionCard> {
        if (raw.isBlank()) return emptySet()
        val names = raw.split(',').map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        val resolved = OsSectionCard.entries.filter { names.contains(it.name) }.toSet()
        return if (resolved.isEmpty() && names == DEFAULT_VISIBLE) OsSectionCard.entries.toSet() else resolved
    }

    fun loadVisibleCards(): Set<OsSectionCard> {
        val newStore = store
        if (newStore.containsKey(KEY_VISIBLE_CARDS)) {
            return resolveVisibleCards(newStore.decodeString(KEY_VISIBLE_CARDS, "").orEmpty())
                .ifEmpty { OsSectionCard.entries.toSet() }
        }
        val legacy = legacyStore
        if (!legacy.containsKey(KEY_VISIBLE_CARDS)) return OsSectionCard.entries.toSet()
        val migrated = resolveVisibleCards(legacy.decodeString(KEY_VISIBLE_CARDS, "").orEmpty())
            .ifEmpty { OsSectionCard.entries.toSet() }
        saveVisibleCards(migrated)
        return migrated
    }

    fun saveVisibleCards(cards: Set<OsSectionCard>) {
        store.encode(
            KEY_VISIBLE_CARDS,
            cards.map { it.name }.sorted().joinToString(",")
        )
        legacyStore.removeValueForKey(KEY_VISIBLE_CARDS)
    }
}
