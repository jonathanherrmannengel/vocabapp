package de.herrmann_engel.rbv.actions

import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.adapters.AdapterCollectionsMovePack
import de.herrmann_engel.rbv.databinding.DiaRecBinding
import de.herrmann_engel.rbv.db.AppDatabase
import de.herrmann_engel.rbv.db.DB_Pack
import de.herrmann_engel.rbv.db.utils.DB_Helper_Delete
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get
import de.herrmann_engel.rbv.utils.createDialog
import de.herrmann_engel.rbv.utils.runInBackground
import de.herrmann_engel.rbv.utils.showConfirmDialog

/**
 * Dialogs for actions on one or more packs: delete and move to another collection.
 * The listener is notified after packs have been deleted or moved.
 */
class PackActions(val activity: ComponentActivity, private val listener: Listener) {
    interface Listener {
        fun deletedPacks(packIds: ArrayList<Int>)
        fun movedPacks(packIds: ArrayList<Int>)
    }

    /**
     * Asks for confirmation and deletes the packs. If multiple packs or a pack containing cards
     * are to be deleted, a second confirmation is required (forceDelete is set for the second
     * dialog) and the contained cards are deleted as well.
     */
    private fun delete(packs: ArrayList<DB_Pack>, forceDelete: Boolean) {
        val dbHelperGet = DB_Helper_Get(activity)
        val text = when {
            forceDelete -> null
            packs.size > 1 -> {
                val numberOfCards = packs.sumOf { dbHelperGet.countCardsInPack(it.uid) }
                activity.resources.getQuantityString(
                    R.plurals.delete_multiple_packs,
                    numberOfCards,
                    packs.size,
                    numberOfCards
                )
            }

            dbHelperGet.countCardsInPack(packs[0].uid) > 0 ->
                activity.getString(R.string.delete_pack_with_cards)

            else -> null
        }
        showConfirmDialog(activity, activity.getString(R.string.delete), text) {
            if ((packs.size <= 1 && dbHelperGet.countCardsInPack(packs[0].uid) == 0) || forceDelete) {
                val dbHelperDelete = DB_Helper_Delete(activity)
                val packIds = ArrayList(packs.map { it.uid })
                runInBackground(activity, {
                    // All packs are deleted or none
                    AppDatabase.getInstance(activity).runInTransaction {
                        packs.forEach { dbHelperDelete.deletePack(it, forceDelete) }
                    }
                }) { listener.deletedPacks(packIds) }
            } else {
                delete(packs, true)
            }
        }
    }

    fun delete(packs: ArrayList<DB_Pack>) {
        delete(packs, false)
    }

    fun delete(pack: DB_Pack) {
        delete(arrayListOf(pack))
    }

    // Lets the user choose a collection to move the packs to
    fun move(packs: ArrayList<DB_Pack>) {
        val bindingMoveDialog = DiaRecBinding.inflate(
            activity.layoutInflater
        )
        val moveDialog = createDialog(
            activity,
            bindingMoveDialog.root,
            activity.getString(R.string.move_pack)
        )
        val dbHelperGet = DB_Helper_Get(activity)
        val collections = dbHelperGet.allCollections
        val adapter = AdapterCollectionsMovePack(collections, packs, moveDialog, listener::movedPacks)
        bindingMoveDialog.diaRec.adapter = adapter
        bindingMoveDialog.diaRec.layoutManager = LinearLayoutManager(activity)
        moveDialog.show()
    }

    fun move(pack: DB_Pack) {
        move(arrayListOf(pack))
    }

}
