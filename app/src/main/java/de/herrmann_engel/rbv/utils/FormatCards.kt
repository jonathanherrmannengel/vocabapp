package de.herrmann_engel.rbv.utils

import android.content.Context
import android.content.SharedPreferences
import de.herrmann_engel.rbv.Globals
import de.herrmann_engel.rbv.Globals.LIST_ACCURATE_SIZE
import de.herrmann_engel.rbv.db.DB_Card_With_Meta
import io.noties.markwon.Markwon
import java.util.function.Consumer

class FormatCards(val context: Context) {

    private val settings: SharedPreferences =
        context.getSharedPreferences(Globals.SETTINGS_NAME, Context.MODE_PRIVATE)
    private val formatCards: Boolean = settings.getBoolean("format_cards", false)
    private val formatCardsNotes: Boolean = settings.getBoolean("format_card_notes", false)
    private val formatString = StringTools()
    private val markwon = Markwon.create(context)

    private fun formatCard(card: DB_Card_With_Meta, inaccurate: Boolean) {
        if (inaccurate) {
            if (formatCards) {
                card.card.front = formatString.unformat(card.card.front)
                card.card.back = formatString.unformat(card.card.back)
            }
            if (formatCardsNotes) {
                card.card.notes = formatString.unformat(card.card.notes)
            }
        } else {
            if (formatCards) {
                card.card.front = formatString.format(card.card.front).toString()
                card.card.back = formatString.format(card.card.back).toString()
            }
            if (formatCardsNotes) {
                card.card.notes = markwon.toMarkdown(card.card.notes).toString()
            }
        }
    }

    fun formatCard(card: DB_Card_With_Meta) {
        formatCard(card, false)
    }

    fun formatCards(list: MutableList<DB_Card_With_Meta>) {
        if (formatCards || formatCardsNotes) {
            list.forEach(Consumer { l: DB_Card_With_Meta ->
                formatCard(l, list.size > LIST_ACCURATE_SIZE)
            })
        }
    }
}
