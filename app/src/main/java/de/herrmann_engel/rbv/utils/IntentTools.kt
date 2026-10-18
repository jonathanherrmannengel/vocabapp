package de.herrmann_engel.rbv.utils

import android.content.Intent
import androidx.core.content.IntentCompat
import de.herrmann_engel.rbv.cardlist.CardSource

private const val EXTRA_LIST_COLLECTION = "listCollection"
private const val EXTRA_CARD_SOURCE = "cardSource"

/**
 * The collection of the pack list (see [de.herrmann_engel.rbv.activities.ListPacks]), null for
 * the list of all packs. The extra is left out for null.
 */
fun Intent.putListCollection(collectionId: Int?): Intent =
    if (collectionId == null) this else putExtra(EXTRA_LIST_COLLECTION, collectionId)

fun Intent.getListCollection(): Int? = getIntExtraOrNull(EXTRA_LIST_COLLECTION)

// The cards of the card list (see [CardSource])
fun Intent.putCardSource(cardSource: CardSource): Intent = putExtra(EXTRA_CARD_SOURCE, cardSource)

fun Intent.getCardSource(): CardSource? =
    IntentCompat.getParcelableExtra(this, EXTRA_CARD_SOURCE, CardSource::class.java)

// The int extra with the given name, null if the intent does not have it
fun Intent.getIntExtraOrNull(name: String): Int? =
    if (hasExtra(name)) getIntExtra(name, 0) else null
