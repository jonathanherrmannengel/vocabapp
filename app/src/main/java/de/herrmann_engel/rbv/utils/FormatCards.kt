package de.herrmann_engel.rbv.utils

import de.herrmann_engel.rbv.Globals.MAX_SIZE_CARDS_LIST_ACCURATE
import de.herrmann_engel.rbv.db.CardWithMeta

// Fills the formatted (markup-free) front and back of a card, used for searching and sorting
fun fillFormattedText(card: CardWithMeta, inaccurate: Boolean) {
    card.formattingIsInaccurate = inaccurate
    card.formattedFront = StringTools.unformat(card.card.front, inaccurate)
    card.formattedBack = StringTools.unformat(card.card.back, inaccurate)
}

// Formats all cards of the list; large lists use the faster, less accurate unformatting
fun fillFormattedTexts(list: MutableList<CardWithMeta>) {
    list.forEach { fillFormattedText(it, list.size > MAX_SIZE_CARDS_LIST_ACCURATE) }
}
