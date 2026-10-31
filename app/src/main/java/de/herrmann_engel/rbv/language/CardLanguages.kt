package de.herrmann_engel.rbv.language

import android.content.Context
import android.os.Build
import android.text.SpannableString
import android.text.Spanned
import android.text.style.LocaleSpan
import android.view.textclassifier.TextClassificationManager
import android.view.textclassifier.TextLanguage
import androidx.annotation.RequiresApi
import de.herrmann_engel.rbv.db.CardWithMeta
import de.herrmann_engel.rbv.db.utils.DbHelperGet
import de.herrmann_engel.rbv.utils.StringTools
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

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
 * The language of one side of a pack's cards and where it comes from: chosen for the pack, chosen
 * for its collection, detected, or none (then the texts are treated as written in the device
 * language).
 */
data class ResolvedLanguage(val locale: Locale?, val source: Source) {
    enum class Source { PACK, COLLECTION, DETECTED, NONE }
}

// The languages of the front and back sides of a pack's cards with their sources
data class ResolvedLanguages(val front: ResolvedLanguage, val back: ResolvedLanguage) {
    val locales: SideLanguages
        get() = SideLanguages(front.locale, back.locale)
}

/**
 * Finds out which languages the cards of packs are written in, so that screen readers can read them
 * with a matching voice. A language chosen for the pack takes precedence over one chosen for its
 * collection, which takes precedence over a detected one. Languages are detected from a sample of
 * each pack's cards (Android 10+) and cached for the app's lifetime; a pack is detected again when
 * its number of cards has changed.
 */
class CardLanguages(private val context: Context) {
    // The languages detected for a pack and the number of cards the pack had then
    private data class Detection(val cardCount: Int, val languages: SideLanguages)

    // Sets the languages of the cards' packs; detects them if needed, so not on the main thread
    fun fill(cards: List<CardWithMeta>) {
        val languages = cards.mapTo(HashSet()) { it.card.packId }
            .associateWith { resolve(it, detect = true).locales }
        cards.forEach { it.languages = languages.getValue(it.card.packId) }
    }

    /**
     * Sets the languages of the card's pack, using only already detected languages (usually
     * detected when a card list was loaded); can be used on the main thread.
     */
    fun fillCached(card: CardWithMeta) {
        card.languages = cached(card.card.packId)
    }

    // The languages of the pack, using only already detected languages (see fillCached)
    fun cached(packId: Int): SideLanguages = resolve(packId, detect = false).locales

    // The languages of the pack with their sources; detects them if needed, so not on the main
    // thread
    fun resolve(packId: Int): ResolvedLanguages = resolve(packId, detect = true)

    private fun resolve(packId: Int, detect: Boolean): ResolvedLanguages {
        val dbHelperGet = DbHelperGet(context)
        val pack = dbHelperGet.getSinglePack(packId)
        val collection = pack?.let { dbHelperGet.getSingleCollection(it.collectionId) }
        val packFront = languageOf(pack?.frontLanguage)
        val packBack = languageOf(pack?.backLanguage)
        val collectionFront = languageOf(collection?.frontLanguage)
        val collectionBack = languageOf(collection?.backLanguage)
        // Only needed for sides without a chosen language
        val needsDetection =
            (packFront ?: collectionFront) == null || (packBack ?: collectionBack) == null
        val detected = when {
            !needsDetection -> SideLanguages.UNKNOWN
            detect -> detected(packId, dbHelperGet)
            else -> cache[packId]?.languages ?: SideLanguages.UNKNOWN
        }
        fun side(packLocale: Locale?, collectionLocale: Locale?, detected: Locale?) = when {
            packLocale != null -> ResolvedLanguage(packLocale, ResolvedLanguage.Source.PACK)
            collectionLocale != null ->
                ResolvedLanguage(collectionLocale, ResolvedLanguage.Source.COLLECTION)
            detected != null -> ResolvedLanguage(detected, ResolvedLanguage.Source.DETECTED)
            else -> ResolvedLanguage(null, ResolvedLanguage.Source.NONE)
        }
        return ResolvedLanguages(
            side(packFront, collectionFront, detected.front),
            side(packBack, collectionBack, detected.back)
        )
    }

    // The detected languages of the pack, from the cache if its number of cards has not changed
    private fun detected(packId: Int, dbHelperGet: DbHelperGet): SideLanguages {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return SideLanguages.UNKNOWN
        }
        val cardCount = dbHelperGet.countCardsInPack(packId)
        cache[packId]?.let {
            if (it.cardCount == cardCount) {
                return it.languages
            }
        }
        val sample = dbHelperGet.getCardSampleByPack(packId, SAMPLE_SIZE)
        val languages =
            SideLanguages(detect(sample.map { it.front }), detect(sample.map { it.back }))
        cache[packId] = Detection(cardCount, languages)
        return languages
    }

    // The language of the texts taken together; null if it is not clear enough
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun detect(texts: List<String>): Locale? {
        // Formatting markup is no part of the language
        val text = texts.joinToString(System.lineSeparator()) { StringTools.unformat(it, false) }
        if (text.isBlank()) {
            return null
        }
        return try {
            val classifier = context.getSystemService(TextClassificationManager::class.java)
                ?.textClassifier ?: return null
            val result = classifier.detectLanguage(TextLanguage.Request.Builder(text).build())
            if (result.localeHypothesisCount == 0) {
                return null
            }
            // The hypotheses are ordered by confidence
            val locale = result.getLocale(0)
            if (result.getConfidenceScore(locale) >= MIN_CONFIDENCE) locale.toLocale() else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    companion object {
        // Number of cards per pack the detection is based on
        private const val SAMPLE_SIZE = 50

        // Languages detected with a lower confidence (0 to 1) are treated as unknown
        private const val MIN_CONFIDENCE = 0.9f

        private val cache = ConcurrentHashMap<Int, Detection>()
    }
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
