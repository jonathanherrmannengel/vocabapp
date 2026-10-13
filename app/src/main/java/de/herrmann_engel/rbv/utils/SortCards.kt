package de.herrmann_engel.rbv.utils

import de.herrmann_engel.rbv.Globals
import de.herrmann_engel.rbv.db.DB_Card_With_Meta

/**
 * Sorts card lists according to the sort constants in [Globals].
 */
class SortCards {
    private fun compareCardsAlphabetical(a: String, b: String): Int {
        return a.compareTo(b, ignoreCase = true)
    }

    fun sortCards(list: MutableList<DB_Card_With_Meta>, sort: Int) {
        when (sort) {
            // By front, then by back
            Globals.SORT_CARDS_ALPHABETICAL -> {
                list.sortWith(Comparator { a, b ->
                    return@Comparator compareCardsAlphabetical(
                        a.formattedFront ?: a.card.front,
                        b.formattedFront ?: b.card.front
                    ).let {
                        if (it == 0) {
                            compareCardsAlphabetical(
                                a.formattedBack ?: a.card.back,
                                b.formattedBack ?: b.card.back
                            )
                        } else it
                    }
                })
            }

            Globals.SORT_CARDS_RANDOM -> {
                list.shuffle()
            }

            // Most recently repeated first, then newest first
            Globals.SORT_CARDS_REPETITION -> {
                list.sortWith(compareByDescending<DB_Card_With_Meta> { it.card.lastRepetition }.thenByDescending { it.card.date })
            }

            // Weighted random order preferring cards with little progress (up to 0.4) and cards
            // often repeated but rarely known (up to 0.3) plus a random part (up to 0.3)
            Globals.SORT_CARDS_MIXED -> {
                list.forEach { c ->
                    c.mixedWeight = (if (c.card.known <= 5) {
                        (1 - c.card.known / 5.0) * 0.4
                    } else 0.0) + (if (c.card.countRepetitions > 0 && c.card.known <= c.card.countRepetitions) {
                        (1 - c.card.known / c.card.countRepetitions.toDouble()) * 0.3
                    } else 0.0) + Math.random() * 0.3
                }
                list.sortWith(compareByDescending { it.mixedWeight })
            }

            // Default: least known first, then newest first
            else -> {
                list.sortWith(compareBy<DB_Card_With_Meta> { it.card.known }.thenByDescending { it.card.date })
            }
        }
    }
}
