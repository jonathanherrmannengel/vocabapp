package de.herrmann_engel.rbv.utils

import de.herrmann_engel.rbv.Globals
import de.herrmann_engel.rbv.db.DB_Card_With_Meta
import java.util.Locale
import java.util.regex.Pattern

/**
 * Filters card lists by a search query (case-insensitive) matching front, back, notes or tags.
 */
class SearchCards {
    private fun hasNoMatch(source: String?, pattern: Pattern): Boolean {
        return source == null || !pattern.matcher(source).find()
    }

    // Faster variant for large lists: compares lowercased strings instead of using a regex
    private fun hasNoMatchInaccurate(source: String?, query: String): Boolean {
        return source == null || !source.lowercase(Locale.ROOT).contains(query)
    }

    /**
     * Removes all cards from the given list that do not match the query.
     */
    fun searchCards(input: MutableList<DB_Card_With_Meta>, query: String) {
        if (input.size > Globals.MAX_SIZE_CARDS_LIST_ACCURATE) {
            val queryLower = query.lowercase(Locale.ROOT)
            input.removeIf { l: DB_Card_With_Meta ->
                hasNoMatchInaccurate(
                    l.formattedFront ?: l.card.front,
                    queryLower
                ) && hasNoMatchInaccurate(
                    l.formattedBack ?: l.card.back,
                    queryLower
                ) && hasNoMatchInaccurate(
                    l.card.notes,
                    queryLower
                ) && (l.tagNames.isNullOrBlank() || hasNoMatchInaccurate(
                    l.tagNames,
                    queryLower
                ))
            }
        } else {
            // Matches the query literally, ignoring case; shared by all cards and fields
            val pattern = Pattern.compile(
                Pattern.quote(query),
                Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE
            )
            input.removeIf { l: DB_Card_With_Meta ->
                hasNoMatch(
                    l.formattedFront ?: l.card.front,
                    pattern
                ) && hasNoMatch(
                    l.formattedBack ?: l.card.back,
                    pattern
                ) && hasNoMatch(
                    l.card.notes,
                    pattern
                ) && (l.tagNames.isNullOrBlank() || hasNoMatch(
                    l.tagNames,
                    pattern
                ))
            }
        }
    }
}
