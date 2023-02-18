package de.herrmann_engel.rbv.adapters

import android.app.Dialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.herrmann_engel.rbv.Globals
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.activities.FileTools
import de.herrmann_engel.rbv.databinding.DiaConfirmBinding
import de.herrmann_engel.rbv.databinding.DiaRecBinding
import de.herrmann_engel.rbv.databinding.RecViewBinding
import de.herrmann_engel.rbv.db.DB_Card_With_Meta
import de.herrmann_engel.rbv.db.DB_Media
import de.herrmann_engel.rbv.db.utils.DB_Helper_Delete
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get
import de.herrmann_engel.rbv.utils.ContextTools
import de.herrmann_engel.rbv.utils.FormatCards
import de.herrmann_engel.rbv.utils.SortCards


class AdapterMediaManage(
    private val media: ArrayList<DB_Media>
) : RecyclerView.Adapter<AdapterMediaManage.ViewHolder>() {
    class ViewHolder(val binding: RecViewBinding) : RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RecViewBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val context = viewHolder.binding.root.context
        viewHolder.binding.recName.text = media[position].file
        viewHolder.binding.recName.setOnClickListener {
            val activity = ContextTools().getActivity(context)
            if (activity != null) {
                val dbHelperGet = DB_Helper_Get(context)
                val cards: MutableList<DB_Card_With_Meta> =
                    dbHelperGet.getAllCardsByMediaWithMeta(media[position].uid)
                FormatCards(context).formatCards(cards)
                SortCards().sortCards(
                    cards,
                    Globals.SORT_ALPHABETICAL
                )
                val dialog = Dialog(context, R.style.dia_view)
                if (cards.isEmpty()) {
                    val bindingDialog: DiaConfirmBinding =
                        DiaConfirmBinding.inflate(activity.layoutInflater)
                    dialog.setContentView(bindingDialog.root)
                    dialog.setTitle(context.resources.getString(R.string.delete))
                    dialog.window!!.setLayout(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT
                    )
                    bindingDialog.diaConfirmDesc.text =
                        context.resources.getString(R.string.media_no_card)
                    bindingDialog.diaConfirmDesc.visibility = View.VISIBLE
                    val currentMedia = media[position]
                    bindingDialog.diaConfirmYes.setOnClickListener {
                        val dbHelperDelete = DB_Helper_Delete(context)
                        val fileName = currentMedia.file
                        dbHelperDelete.deleteMedia(currentMedia.uid)
                        media.remove(currentMedia)
                        notifyItemRemoved(position)
                        notifyItemRangeChanged(position, media.size)
                        dialog.dismiss()
                        if ((ContextTools().getActivity(context) as FileTools).existsMediaFile(
                                fileName
                            )
                        ) {
                            (ContextTools().getActivity(context) as FileTools).showDeleteDialog(
                                fileName
                            )
                        }
                    }
                    bindingDialog.diaConfirmNo.setOnClickListener { dialog.dismiss() }
                } else {
                    val bindingDialog: DiaRecBinding =
                        DiaRecBinding.inflate(activity.layoutInflater)
                    dialog.setContentView(bindingDialog.root)
                    dialog.setTitle(context.resources.getString(R.string.media_linked_cards))
                    dialog.window!!.setLayout(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT
                    )
                    val adapter = AdapterMediaManageLinkedCards(cards, dialog)
                    bindingDialog.diaRec.adapter = adapter
                    bindingDialog.diaRec.layoutManager = LinearLayoutManager(context)
                }
                dialog.show()
            }
        }
    }

    override fun getItemCount(): Int {
        return media.size
    }
}
