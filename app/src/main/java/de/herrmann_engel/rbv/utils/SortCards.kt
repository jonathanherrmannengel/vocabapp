package de.herrmann_engel.rbv.utils

import de.herrmann_engel.rbv.Globals.MAX_SIZE_CARDS_LIST_ACCURATE
import de.herrmann_engel.rbv.cardlist.CardSort
import de.herrmann_engel.rbv.db.CardWithMeta
import java.text.CollationKey
import java.text.Collator
import java.util.Locale

// The language shared by all texts, else the app's language (e.g. for lists of mixed packs)
private fun sharedLanguage(languages: List<Locale?>, appLocale: Locale): Locale =
    languages.distinct().singleOrNull() ?: appLocale

// A card with the collation key of its front for the alphabetical order
private class AlphabeticalEntry(val card: CardWithMeta, val front: CollationKey)

/**
 * Sorts the card list according to the sort order. Alphabetically, each side is ordered by the
 * rules of the language its cards are written in if they share one (e.g. a Swedish pack: å, ä, ö
 * at the end), else by those of the app's language (appLocale); large lists are only compared
 * character by character, ignoring case, as these rules take considerably longer.
 */
fun sortCards(list: MutableList<CardWithMeta>, sort: CardSort, appLocale: Locale) {
    when (sort) {
        // By front, then by back
        CardSort.ALPHABETICAL -> if (list.size > MAX_SIZE_CARDS_LIST_ACCURATE) {
            list.sortWith(
                compareBy<CardWithMeta, String>(String.CASE_INSENSITIVE_ORDER) {
                    it.formattedFront ?: it.card.front
                }.thenBy(String.CASE_INSENSITIVE_ORDER) { it.formattedBack ?: it.card.back }
            )
        } else {
            val frontCollator =
                Collator.getInstance(sharedLanguage(list.map { it.languages.front }, appLocale))
            val backCollator =
                Collator.getInstance(sharedLanguage(list.map { it.languages.back }, appLocale))
            // The keys of the fronts are computed once per card instead of once per comparison
            // (several times faster); the backs are only compared for equal fronts
            val sorted = list.map {
                val front = it.formattedFront ?: it.card.front
                AlphabeticalEntry(it, frontCollator.getCollationKey(front))
            }.sortedWith { a, b ->
                a.front.compareTo(b.front).takeIf { it != 0 } ?: backCollator.compare(
                    a.card.formattedBack ?: a.card.card.back,
                    b.card.formattedBack ?: b.card.card.back
                )
            }
            list.clear()
            sorted.mapTo(list) { it.card }
        }

        CardSort.RANDOM -> {
            list.shuffle()
        }

        // Most recently repeated first, then newest first
        CardSort.REPETITION -> {
            list.sortWith(
                compareByDescending<CardWithMeta> { it.card.lastRepetition }
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
                compareBy<CardWithMeta> { it.card.known }.thenByDescending { it.card.date }
            )
        }
    }
}
