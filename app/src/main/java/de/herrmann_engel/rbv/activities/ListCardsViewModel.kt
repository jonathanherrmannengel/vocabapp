package de.herrmann_engel.rbv.activities

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import de.herrmann_engel.rbv.cardlist.CardSort
import de.herrmann_engel.rbv.db.DB_Card_With_Meta

/**
 * State of [ListCards] that has to survive the activity being recreated (e.g. after a theme or
 * language change).
 *
 * The loaded cards are only kept in memory; if the app process is killed in the background,
 * they are loaded (and sorted) again. The small values (display settings, search, current card
 * of the query mode) are additionally saved, so they also survive the process being killed.
 */
class ListCardsViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    // All cards of this list (sorted), null until loaded
    var cardsList: MutableList<DB_Card_With_Meta>? = null

    // False until the display settings have been initialized from the app settings
    val hasDisplaySettings: Boolean
        get() = savedState.contains(KEY_LIST_SORT)

    var listSort: CardSort
        get() = CardSort.fromValue(savedState[KEY_LIST_SORT] ?: CardSort.DEFAULT.value)
        set(value) {
            savedState[KEY_LIST_SORT] = value.value
        }

    var frontBackReverse: Boolean
        get() = savedState[KEY_FRONT_BACK_REVERSE] ?: false
        set(value) {
            savedState[KEY_FRONT_BACK_REVERSE] = value
        }

    var showFrontAndBack: Boolean
        get() = savedState[KEY_SHOW_FRONT_AND_BACK] ?: false
        set(value) {
            savedState[KEY_SHOW_FRONT_AND_BACK] = value
        }

    var searchQuery: String?
        get() = savedState[KEY_SEARCH_QUERY]
        set(value) {
            savedState[KEY_SEARCH_QUERY] = value
        }

    // Id of the current card if query mode is open, otherwise null. The id is stored instead of
    // the position, because the order may change when the cards are loaded again (e.g. with
    // random sort after the process was killed).
    var queryCardId: Int?
        get() = savedState[KEY_QUERY_CARD_ID]
        set(value) {
            savedState[KEY_QUERY_CARD_ID] = value
        }

    companion object {
        private const val KEY_LIST_SORT = "listSort"
        private const val KEY_FRONT_BACK_REVERSE = "frontBackReverse"
        private const val KEY_SHOW_FRONT_AND_BACK = "showFrontAndBack"
        private const val KEY_SEARCH_QUERY = "searchQuery"
        private const val KEY_QUERY_CARD_ID = "queryCardId"
    }
}
