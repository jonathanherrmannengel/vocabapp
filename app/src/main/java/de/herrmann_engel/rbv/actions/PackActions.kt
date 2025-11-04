package de.herrmann_engel.rbv.actions

import android.app.Activity
import android.app.Dialog
import android.view.View
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.activities.PackActionsActivity
import de.herrmann_engel.rbv.adapters.AdapterCollectionsMovePack
import de.herrmann_engel.rbv.databinding.DiaConfirmBinding
import de.herrmann_engel.rbv.databinding.DiaRecBinding
import de.herrmann_engel.rbv.db.DB_Pack
import de.herrmann_engel.rbv.db.utils.DB_Helper_Delete
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get

class PackActions(val activity: Activity) {

    private fun delete(packs: ArrayList<DB_Pack>, forceDelete: Boolean) {
        val confirmDeleteDialog = Dialog(activity, R.style.dia_view)
        val bindingConfirmDeleteDialog = DiaConfirmBinding.inflate(
            activity.layoutInflater
        )
        confirmDeleteDialog.setContentView(bindingConfirmDeleteDialog.root)
        confirmDeleteDialog.setTitle(activity.resources.getString(R.string.delete))
        confirmDeleteDialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        val dbHelperGet = DB_Helper_Get(activity)
        if (!forceDelete) {
            if (packs.size > 1) {
                var numberOfCards = 0
                for (pack in packs) {
                    numberOfCards += dbHelperGet.countCardsInPack(pack.uid)
                }
                bindingConfirmDeleteDialog.diaConfirmDesc.text =
                    activity.resources.getQuantityString(
                        R.plurals.delete_multiple_packs,
                        numberOfCards,
                        packs.size,
                        numberOfCards
                    )
                bindingConfirmDeleteDialog.diaConfirmDesc.visibility = View.VISIBLE
            } else if (dbHelperGet.countCardsInPack(packs[0].uid) > 0) {
                bindingConfirmDeleteDialog.diaConfirmDesc.setText(R.string.delete_pack_with_cards)
                bindingConfirmDeleteDialog.diaConfirmDesc.visibility = View.VISIBLE
            }
        }
        bindingConfirmDeleteDialog.diaConfirmYes.setOnClickListener {
            if ((packs.size <= 1 && dbHelperGet.countCardsInPack(packs[0].uid) == 0) || forceDelete) {
                val dbHelperDelete = DB_Helper_Delete(activity)
                val packIds = arrayListOf<Int>()
                for (pack in packs) {
                    packIds.add(pack.uid)
                    dbHelperDelete.deletePack(pack, forceDelete)
                }
                (activity as PackActionsActivity).deletedPacks(packIds)
            } else {
                delete(packs, true)
            }
            confirmDeleteDialog.dismiss()
        }
        bindingConfirmDeleteDialog.diaConfirmNo.setOnClickListener { confirmDeleteDialog.dismiss() }
        confirmDeleteDialog.show()
    }

    fun delete(packs: ArrayList<DB_Pack>) {
        delete(packs, false)
    }

    fun delete(pack: DB_Pack) {
        delete(arrayListOf(pack))
    }

    fun move(packs: ArrayList<DB_Pack>) {
        val moveDialog = Dialog(activity, R.style.dia_view)
        val bindingMoveDialog = DiaRecBinding.inflate(
            activity.layoutInflater
        )
        moveDialog.setContentView(bindingMoveDialog.root)
        moveDialog.setTitle(activity.resources.getString(R.string.move_pack))
        moveDialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        val dbHelperGet = DB_Helper_Get(activity)
        val collections = dbHelperGet.allCollections
        val adapter = AdapterCollectionsMovePack(collections, packs, moveDialog)
        bindingMoveDialog.diaRec.adapter = adapter
        bindingMoveDialog.diaRec.layoutManager = LinearLayoutManager(activity)
        moveDialog.show()
    }

    fun move(pack: DB_Pack) {
        move(arrayListOf(pack))
    }

}
