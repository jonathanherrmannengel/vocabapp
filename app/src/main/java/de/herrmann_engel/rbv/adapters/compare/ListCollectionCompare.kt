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
        return oldList[oldItemPosition].collection.uid == newList[newItemPosition].collection.uid
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return !updateAllContent && oldList[oldItemPosition] == newList[newItemPosition]
    }

}
