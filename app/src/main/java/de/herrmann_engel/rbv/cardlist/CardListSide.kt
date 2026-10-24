package de.herrmann_engel.rbv.cardlist

/**
 * Side(s) of the cards shown in card lists. value is stored in the settings and must not change.
 */
enum class CardListSide(val value: Int) {
    FRONT(0),
    BACK(1),
    BOTH(2);

    companion object {
        // Unknown values (e.g. from a newer app version) count as FRONT
        fun fromValue(value: Int): CardListSide =
            entries.firstOrNull { it.value == value } ?: FRONT
    }
}
