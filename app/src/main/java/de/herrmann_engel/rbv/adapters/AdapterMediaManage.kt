package de.herrmann_engel.rbv.adapters

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.herrmann_engel.rbv.Globals
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.activities.FileTools
import de.herrmann_engel.rbv.db.DB_Media
import de.herrmann_engel.rbv.db.utils.DB_Helper_Delete
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get
import de.herrmann_engel.rbv.utils.SortCards


class AdapterMediaManage(
    private val media: ArrayList<DB_Media>,
    private val c: Context
) : RecyclerView.Adapter<AdapterMediaManage.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.rec_name)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.rec_view, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.textView.text = media[position].file
        viewHolder.textView.setOnClickListener {
            val dbHelperGet = DB_Helper_Get(c)
            val ids = dbHelperGet.getAllMediaLinkCardIdsByMedia(media[position].uid)
            val cards = SortCards().sortCards(
                dbHelperGet.getAllCardsByIds(ids as java.util.ArrayList<Int>?),
                Globals.SORT_ALPHABETICAL
            )
            val dialog = Dialog(c, R.style.dia_view)
            if (cards.isEmpty()) {
                dialog.setContentView(R.layout.dia_confirm)
                dialog.setTitle(c.resources.getString(R.string.delete))
                dialog.window!!.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT
                )
                val yesButton = dialog.findViewById<Button>(R.id.dia_confirm_yes)
                val noButton = dialog.findViewById<Button>(R.id.dia_confirm_no)
                val confirmDeleteDesc: TextView = dialog.findViewById(R.id.dia_confirm_desc)
                confirmDeleteDesc.text = c.resources.getString(R.string.media_no_card)
                confirmDeleteDesc.visibility = View.VISIBLE
                yesButton.setOnClickListener {
                    val dbHelperDelete = DB_Helper_Delete(c)
                    val fileName = media[position].file
                    dbHelperDelete.deleteMedia(media[position].uid)
                    media.remove(media[position])
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, media.size)
                    dialog.dismiss()
                    if ((c as FileTools).existsMediaFile(fileName)) {
                        c.showDeleteDialog(fileName)
                    }
                }
                noButton.setOnClickListener { dialog.dismiss() }
            } else {
                dialog.setContentView(R.layout.dia_rec)
                dialog.setTitle(c.resources.getString(R.string.media_linked_cards))
                dialog.window!!.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT
                )
                val recyclerView: RecyclerView = dialog.findViewById(R.id.dia_rec)
                val adapter = AdapterMediaManageLinkedCards(cards, c)
                recyclerView.adapter = adapter
                recyclerView.layoutManager = LinearLayoutManager(c)
            }
            dialog.show()
        }
    }

    override fun getItemCount(): Int {
        return media.size
    }
}
