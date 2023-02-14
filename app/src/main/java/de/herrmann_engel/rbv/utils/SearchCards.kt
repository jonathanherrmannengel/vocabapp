package de.herrmann_engel.rbv.utils

import de.herrmann_engel.rbv.Globals
import de.herrmann_engel.rbv.db.DB_Card_With_Meta
import java.util.*
import java.util.regex.Pattern

class SearchCards {
    private fun hasNoMatch(source: String?, query: String): Boolean {
        return if (source == null) {
            true
        } else !Pattern.compile(
            Pattern.quote(query),
            Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE
        ).matcher(source)
            .find()
    }

    private fun hasNoMatchInaccurate(source: String?, query: String): Boolean {
        return if (source == null) {
            true
        } else !source.lowercase(Locale.getDefault()).contains(query)
    }

    fun searchCards(input: MutableList<DB_Card_With_Meta>, query: String) {
        if (input.size > Globals.LIST_ACCURATE_SIZE) {
            val queryLower = query.lowercase(Locale.getDefault())
            input.removeIf { l: DB_Card_With_Meta ->
                hasNoMatchInaccurate(
                    l.card.front,
                    queryLower
                ) && hasNoMatchInaccurate(l.card.back, queryLower) && hasNoMatchInaccurate(
                    l.card.notes,
                    queryLower
                )
            }
        } else {
            input.removeIf { l: DB_Card_With_Meta ->
                hasNoMatch(
                    l.card.front,
                    query
                ) && hasNoMatch(l.card.back, query) && hasNoMatch(l.card.notes, query)
            }
        }
    }
}
