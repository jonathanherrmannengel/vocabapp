package de.herrmann_engel.rbv.utils

import de.herrmann_engel.rbv.Globals
import de.herrmann_engel.rbv.db.DB_Card_With_Meta

class SortCards {
    private fun compareCardsAlphabetical(a: String, b: String): Int {
        return a.compareTo(b, ignoreCase = true)
    }

    fun sortCardsWithMeta(list: MutableList<DB_Card_With_Meta>, sort: Int) {
        when (sort) {
            Globals.SORT_ALPHABETICAL -> {
                list.sortWith(Comparator { a, b ->
                    return@Comparator if (a.card == null || b.card == null) {
                        0
                    } else {
                        compareCardsAlphabetical(a.card.front, b.card.front).let {
                            if (it == 0) {
                                compareCardsAlphabetical(a.card.back, b.card.back)
                            } else it
                        }
                    }
                })
            }
            Globals.SORT_RANDOM -> {
                list.shuffle()
            }
            else -> {
                list.sortWith(compareBy<DB_Card_With_Meta?> { it?.card?.known }.thenByDescending { it?.card?.date })
            }
        }
    }
}
