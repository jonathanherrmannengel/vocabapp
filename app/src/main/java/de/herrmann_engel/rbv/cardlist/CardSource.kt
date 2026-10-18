package de.herrmann_engel.rbv.cardlist

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * The cards shown by a card list (see [de.herrmann_engel.rbv.activities.ListCards]), passed
 * between screens as extra (see putCardSource).
 */
sealed interface CardSource : Parcelable {
    @Parcelize
    data object AllCards : CardSource

    @Parcelize
    data class Collection(val collectionId: Int) : CardSource

    /**
     * Cards of a pack. listCollectionId is the collection whose pack list the pack was opened
     * from (null: the list of all packs); only its packs are offered when moving cards.
     */
    @Parcelize
    data class Pack(val packId: Int, val listCollectionId: Int?) : CardSource

    /**
     * Results of the advanced search. A null value disables the corresponding filter.
     *
     * @param packIds         packs to include
     * @param tagIds          tags to include (a card needs at least one of them)
     * @param progressGreater true: progress >= progressThreshold, false: progress <=
     *                        progressThreshold
     * @param repetitionOlder true: last repetition older than repetitionDays days, false: newer
     *                        or equal
     */
    @Parcelize
    data class Search(
        val packIds: List<Int>?,
        val tagIds: List<Int>?,
        val progressGreater: Boolean,
        val progressThreshold: Int?,
        val repetitionOlder: Boolean,
        val repetitionDays: Int?
    ) : CardSource
}
