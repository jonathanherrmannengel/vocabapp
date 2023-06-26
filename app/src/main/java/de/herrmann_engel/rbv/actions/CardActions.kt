package de.herrmann_engel.rbv.actions

import android.app.Activity
import android.app.Dialog
import android.view.View
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.activities.CardActionsActivity
import de.herrmann_engel.rbv.adapters.AdapterPacksMoveCard
import de.herrmann_engel.rbv.databinding.DiaConfirmBinding
import de.herrmann_engel.rbv.databinding.DiaRecBinding
import de.herrmann_engel.rbv.db.DB_Card
import de.herrmann_engel.rbv.db.DB_Pack
import de.herrmann_engel.rbv.db.utils.DB_Helper_Delete
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get

class CardActions(val activity: Activity) {

    private fun delete(cards: ArrayList<DB_Card>, forceDelete: Boolean) {
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
        if (cards.size > 1 && !forceDelete) {
            bindingConfirmDeleteDialog.diaConfirmDesc.text =
                String.format(
                    activity.resources.getString(R.string.delete_multiple_cards),
                    cards.size
                )
            bindingConfirmDeleteDialog.diaConfirmDesc.visibility = View.VISIBLE
        }
        bindingConfirmDeleteDialog.diaConfirmYes.setOnClickListener {
            if (cards.size <= 1 || forceDelete) {
                val dbHelperDelete = DB_Helper_Delete(activity)
                val cardIds = arrayListOf<Int>()
                for (card in cards) {
                    cardIds.add(card.uid)
                    dbHelperDelete.deleteCard(card)
                }
                (activity as CardActionsActivity).deletedCards(cardIds)
            } else {
                delete(cards, true)
            }
            confirmDeleteDialog.dismiss()
        }
        bindingConfirmDeleteDialog.diaConfirmNo.setOnClickListener { confirmDeleteDialog.dismiss() }
        confirmDeleteDialog.show()
    }

    fun delete(cards: ArrayList<DB_Card>) {
        delete(cards, false)
    }

    fun delete(card: DB_Card) {
        delete(arrayListOf(card))
    }

    fun move(cards: ArrayList<DB_Card>, collectionNo: Int) {

        val moveDialog = Dialog(activity, R.style.dia_view)
        val bindingMoveDialog = DiaRecBinding.inflate(
            activity.layoutInflater
        )
        moveDialog.setContentView(bindingMoveDialog.root)
        moveDialog.setTitle(activity.resources.getString(R.string.move_card))
        moveDialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        val dbHelperGet = DB_Helper_Get(activity)
        val packs: List<DB_Pack> = if (collectionNo == -1) {
            dbHelperGet.allPacks
        } else {
            dbHelperGet.getAllPacksByCollection(collectionNo)
        }
        val adapter = AdapterPacksMoveCard(packs, collectionNo, cards, moveDialog)
        bindingMoveDialog.diaRec.adapter = adapter
        bindingMoveDialog.diaRec.layoutManager = LinearLayoutManager(activity)
        moveDialog.show()
    }

    fun move(card: DB_Card, collectionNo: Int) {
        move(arrayListOf(card), collectionNo)
    }

}
