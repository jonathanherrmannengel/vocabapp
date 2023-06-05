package de.herrmann_engel.rbv.adapters.compare

import androidx.recyclerview.widget.DiffUtil
import de.herrmann_engel.rbv.db.DB_Pack_With_Meta

class ListPackCompare(
    private val oldList: List<DB_Pack_With_Meta>,
    private val newList: List<DB_Pack_With_Meta>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        if (oldList.size == 1 || newList.size == 1) {
            return false
        }
        if (oldList[oldItemPosition].pack == null || newList[newItemPosition].pack == null) {
            return oldList[oldItemPosition].pack == newList[newItemPosition].pack
        }
        return oldList[oldItemPosition].pack.uid == newList[newItemPosition].pack.uid
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

}
