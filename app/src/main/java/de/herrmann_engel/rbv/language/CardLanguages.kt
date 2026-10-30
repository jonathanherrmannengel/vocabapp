package de.herrmann_engel.rbv.language

import android.text.SpannableString
import android.text.Spanned
import android.text.style.LocaleSpan
import de.herrmann_engel.rbv.db.CardWithMeta
import java.util.Locale

/**
 * The languages of the front and back sides of a pack's cards; null if unknown, then the texts are
 * treated as written in the device language.
 */
data class SideLanguages(val front: Locale?, val back: Locale?) {
    fun of(back: Boolean): Locale? = if (back) this.back else front

    companion object {
        val UNKNOWN = SideLanguages(null, null)
    }
}

/**
 * Finds out which languages the cards of packs are written in, so that screen readers can read them
 * with a matching voice. The languages will come from the pack, else from its collection, else from
 * a detection; none of these sources exists yet, so all languages are unknown.
 */
class CardLanguages {
    // Sets the languages of the cards' packs
    fun fill(cards: List<CardWithMeta>) {
        val languages = forPacks(cards.mapTo(HashSet()) { it.card.packId })
        cards.forEach { it.languages = languages.getValue(it.card.packId) }
    }

    fun fill(card: CardWithMeta) {
        fill(listOf(card))
    }

    private fun forPacks(packIds: Collection<Int>): Map<Int, SideLanguages> =
        packIds.associateWith { SideLanguages.UNKNOWN }
}

/**
 * The text marked as written in the language (a LocaleSpan over its whole length), so that screen
 * readers use a matching voice and CJK texts get the right font. Spans of the text are kept; the
 * text is returned unchanged if the language is unknown.
 */
fun CharSequence.inLanguage(locale: Locale?): CharSequence {
    if (locale == null || isEmpty()) {
        return this
    }
    val result = SpannableString(this)
    result.setSpan(LocaleSpan(locale), 0, length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
    return result
}
