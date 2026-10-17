package de.herrmann_engel.rbv.adapters.compare

import androidx.recyclerview.widget.DiffUtil
import de.herrmann_engel.rbv.db.DB_Collection_With_Meta

/**
 * DiffUtil callback for collection lists. If updateAllContent is set, all items are rebound.
 */
class ListCollectionCompare(
    private val oldList: List<DB_Collection_With_Meta>,
    private val newList: List<DB_Collection_With_Meta>,
    private val updateAllContent: Boolean
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        // An empty list is displayed as a single placeholder item, so single items are always redrawn
        if (oldList.size == 1 || newList.size == 1) {
            return false
        }
        // The first item ("all packs") is a placeholder without collection
        val oldCollection = oldList[oldItemPosition].collection
        val newCollection = newList[newItemPosition].collection
        if (oldCollection == null || newCollection == null) {
            return oldCollection == newCollection
        }
        return oldCollection.uid == newCollection.uid
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return !updateAllContent && oldList[oldItemPosition] == newList[newItemPosition]
    }

}
