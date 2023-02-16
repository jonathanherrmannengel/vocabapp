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

    fun formatCard(card: DB_Card_With_Meta, inaccurate: Boolean) {
        card.formattingIsInaccurate = inaccurate
        if (inaccurate) {
            if (formatCards) {
                card.formattedFront = formatString.unformat(card.card.front)
                card.formattedBack = formatString.unformat(card.card.back)
            }
            if (formatCardsNotes) {
                card.formattedNotes = formatString.unformat(card.card.notes)
            }
        } else {
            if (formatCards) {
                card.formattedFront = formatString.format(card.card.front).toString()
                card.formattedBack = formatString.format(card.card.back).toString()
            }
            if (formatCardsNotes) {
                card.formattedNotes = markwon.toMarkdown(card.card.notes).toString()
            }
        }
    }

    fun formatCards(list: MutableList<DB_Card_With_Meta>) {
        if (formatCards || formatCardsNotes) {
            list.forEach(Consumer { l: DB_Card_With_Meta ->
                formatCard(l, list.size > LIST_ACCURATE_SIZE)
            })
        }
    }
}
