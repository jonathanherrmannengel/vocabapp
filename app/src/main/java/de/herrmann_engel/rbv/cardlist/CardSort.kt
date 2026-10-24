package de.herrmann_engel.rbv.cardlist

/**
 * Sort orders of card lists (see SortCards). value is stored in the settings and must not change.
 */
enum class CardSort(val value: Int) {
    DEFAULT(0),
    RANDOM(1),
    ALPHABETICAL(2),
    REPETITION(3),
    MIXED(4);

    companion object {
        // Unknown values (e.g. from a newer app version) count as DEFAULT
        fun fromValue(value: Int): CardSort =
            entries.firstOrNull { it.value == value } ?: DEFAULT
    }
}
