package de.herrmann_engel.rbv.adapters.compare

import androidx.recyclerview.widget.DiffUtil
import de.herrmann_engel.rbv.db.DB_Card_With_Meta
import de.herrmann_engel.rbv.utils.CompareDataObjects

class ListCardCompare(
    private val oldList: List<DB_Card_With_Meta>,
    private val newList: List<DB_Card_With_Meta>,
    private val updateAllContent: Boolean
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
        if (oldList[oldItemPosition].card == null || newList[newItemPosition].card == null) {
            return oldList[oldItemPosition].card == newList[newItemPosition].card
        }
        return oldList[oldItemPosition].card.uid == newList[newItemPosition].card.uid
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        if (updateAllContent) {
            return false
        }
        return CompareDataObjects().areTheySame(oldList[oldItemPosition], newList[newItemPosition])
    }

}
