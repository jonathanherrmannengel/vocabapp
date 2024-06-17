package de.herrmann_engel.rbv.utils

import de.herrmann_engel.rbv.Globals.LIST_ACCURATE_SIZE
import de.herrmann_engel.rbv.db.DB_Card_With_Meta
import java.util.function.Consumer

class FormatCards {

    private val formatString = StringTools()

    fun formatCard(card: DB_Card_With_Meta, inaccurate: Boolean) {
        card.formattingIsInaccurate = inaccurate
        card.formattedFront = formatString.unformat(card.card.front, inaccurate)
        card.formattedBack = formatString.unformat(card.card.back, inaccurate)
    }

    fun formatCards(list: MutableList<DB_Card_With_Meta>) {
        list.forEach(Consumer { l: DB_Card_With_Meta ->
            formatCard(l, list.size > LIST_ACCURATE_SIZE)
        })
    }
}
