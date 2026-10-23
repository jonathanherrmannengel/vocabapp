package de.herrmann_engel.rbv.utils

import android.content.Intent
import androidx.core.content.IntentCompat
import de.herrmann_engel.rbv.cardlist.CardSource

private const val EXTRA_COLLECTION = "collection"
private const val EXTRA_PACK = "pack"
private const val EXTRA_CARD = "card"
private const val EXTRA_CARD_ADDED = "cardAdded"
private const val EXTRA_CARD_UPDATED = "cardUpdated"
private const val EXTRA_CARD_DELETED = "cardDeleted"
private const val EXTRA_BACK_TO_LIST = "backToList"
private const val EXTRA_LIST_COLLECTION = "listCollection"
private const val EXTRA_CARD_SOURCE = "cardSource"

// The collection, pack or card a screen shows or edits. The get functions return null if the
// intent does not have the extra.
fun Intent.putCollectionId(collectionId: Int): Intent = putExtra(EXTRA_COLLECTION, collectionId)

fun Intent.getCollectionId(): Int? = getIntExtraOrNull(EXTRA_COLLECTION)

fun Intent.putPackId(packId: Int): Intent = putExtra(EXTRA_PACK, packId)

fun Intent.getPackId(): Int? = getIntExtraOrNull(EXTRA_PACK)

fun Intent.putCardId(cardId: Int): Intent = putExtra(EXTRA_CARD, cardId)

fun Intent.getCardId(): Int? = getIntExtraOrNull(EXTRA_CARD)

// A card that has been added, updated or deleted, passed to the card list when returning to it
// (see ListCards.onNewIntent), so that the list can be updated without loading it again
fun Intent.putAddedCardId(cardId: Int): Intent = putExtra(EXTRA_CARD_ADDED, cardId)

fun Intent.getAddedCardId(): Int? = getIntExtraOrNull(EXTRA_CARD_ADDED)

fun Intent.putUpdatedCardId(cardId: Int): Intent = putExtra(EXTRA_CARD_UPDATED, cardId)

fun Intent.getUpdatedCardId(): Int? = getIntExtraOrNull(EXTRA_CARD_UPDATED)

fun Intent.putDeletedCardId(cardId: Int): Intent = putExtra(EXTRA_CARD_DELETED, cardId)

fun Intent.getDeletedCardId(): Int? = getIntExtraOrNull(EXTRA_CARD_DELETED)

// Card editing: return to the card list after closing instead of the previous screen
fun Intent.putBackToList(backToList: Boolean): Intent = putExtra(EXTRA_BACK_TO_LIST, backToList)

fun Intent.getBackToList(): Boolean = getBooleanExtra(EXTRA_BACK_TO_LIST, false)

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
private fun Intent.getIntExtraOrNull(name: String): Int? =
    if (hasExtra(name)) getIntExtra(name, 0) else null
