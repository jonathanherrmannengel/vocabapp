package de.herrmann_engel.rbv.adapters.compare

import androidx.recyclerview.widget.DiffUtil
import de.herrmann_engel.rbv.db.DB_Pack_With_Meta

/**
 * DiffUtil callback for pack lists. If updateAllContent is set, all items are rebound.
 */
class ListPackCompare(
    private val oldList: List<DB_Pack_With_Meta>,
    private val newList: List<DB_Pack_With_Meta>,
    private val updateAllContent: Boolean
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].pack.uid == newList[newItemPosition].pack.uid
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return !updateAllContent && oldList[oldItemPosition] == newList[newItemPosition]
    }

}
