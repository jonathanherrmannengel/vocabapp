package de.herrmann_engel.rbv.utils

import de.herrmann_engel.rbv.cardlist.CardSort
import de.herrmann_engel.rbv.db.DB_Card_With_Meta

private fun compareCardsAlphabetical(a: String, b: String): Int {
    return a.compareTo(b, ignoreCase = true)
}

// Sorts the card list according to the sort order
fun sortCards(list: MutableList<DB_Card_With_Meta>, sort: CardSort) {
    when (sort) {
        // By front, then by back
        CardSort.ALPHABETICAL -> {
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

        CardSort.RANDOM -> {
            list.shuffle()
        }

        // Most recently repeated first, then newest first
        CardSort.REPETITION -> {
            list.sortWith(
                compareByDescending<DB_Card_With_Meta> { it.card.lastRepetition }
                    .thenByDescending { it.card.date }
            )
        }

        // Weighted random order preferring cards with little progress (up to 0.4) and cards
        // often repeated but rarely known (up to 0.3) plus a random part (up to 0.3)
        CardSort.MIXED -> {
            list.forEach { c ->
                val known = c.card.known
                val repetitions = c.card.countRepetitions
                val progressPart = if (known <= 5) (1 - known / 5.0) * 0.4 else 0.0
                val repetitionPart = if (repetitions > 0 && known <= repetitions) {
                    (1 - known / repetitions.toDouble()) * 0.3
                } else 0.0
                c.mixedWeight = progressPart + repetitionPart + Math.random() * 0.3
            }
            list.sortWith(compareByDescending { it.mixedWeight })
        }

        // Least known first, then newest first
        CardSort.DEFAULT -> {
            list.sortWith(
                compareBy<DB_Card_With_Meta> { it.card.known }.thenByDescending { it.card.date }
            )
        }
    }
}
