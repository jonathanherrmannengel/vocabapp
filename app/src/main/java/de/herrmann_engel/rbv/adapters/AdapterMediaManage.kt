package de.herrmann_engel.rbv.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.cardlist.CardSort
import de.herrmann_engel.rbv.databinding.DiaRecBinding
import de.herrmann_engel.rbv.databinding.RecViewBinding
import de.herrmann_engel.rbv.db.DB_Card_With_Meta
import de.herrmann_engel.rbv.db.DB_Media
import de.herrmann_engel.rbv.db.utils.DB_Helper_Delete
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get
import de.herrmann_engel.rbv.media.MediaFiles
import de.herrmann_engel.rbv.settings.AppSettings
import de.herrmann_engel.rbv.utils.createDialog
import de.herrmann_engel.rbv.utils.fillFormattedTexts
import de.herrmann_engel.rbv.utils.findActivity
import de.herrmann_engel.rbv.utils.showConfirmDialog
import de.herrmann_engel.rbv.utils.sortCards


/**
 * List of all media entries. Clicking one shows the cards it is linked to; unused media
 * can be deleted (database entry and, after confirmation, the file).
 */
class AdapterMediaManage(
    private val media: MutableList<DB_Media>,
    private val mediaFiles: MediaFiles
) : RecyclerView.Adapter<AdapterMediaManage.ViewHolder>() {
    class ViewHolder(val binding: RecViewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RecViewBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val context = viewHolder.binding.root.context
        val currentMedia = media[position]
        val fileName = currentMedia.file
        viewHolder.binding.recName.text = fileName
        viewHolder.binding.recName.setOnClickListener {
            val activity = context.findActivity()
            if (activity != null) {
                val dbHelperGet = DB_Helper_Get(context)
                val cards: MutableList<DB_Card_With_Meta> =
                    dbHelperGet.getAllCardsByMediaWithMeta(currentMedia.uid)
                val settings = AppSettings(context)
                if (settings.formatCards) {
                    fillFormattedTexts(cards)
                }
                sortCards(
                    cards,
                    CardSort.ALPHABETICAL
                )
                // Media not linked to any card: offer to delete it
                if (cards.isEmpty()) {
                    showConfirmDialog(
                        activity,
                        activity.getString(R.string.delete),
                        activity.getString(R.string.media_no_card)
                    ) {
                        DB_Helper_Delete(context).deleteMedia(currentMedia.uid)
                        media.remove(currentMedia)
                        notifyItemRemoved(position)
                        notifyItemRangeChanged(position, media.size)
                        if (mediaFiles.existsMediaFile(fileName)) {
                            mediaFiles.showDeleteDialog(fileName)
                        }
                    }
                } else {
                    val bindingDialog = DiaRecBinding.inflate(activity.layoutInflater)
                    val dialog = createDialog(
                        activity,
                        bindingDialog.root,
                        activity.getString(R.string.media_linked_cards)
                    )
                    bindingDialog.diaRec.adapter = AdapterMediaManageLinkedCards(cards, dialog)
                    bindingDialog.diaRec.layoutManager = LinearLayoutManager(context)
                    dialog.show()
                }
            } else {
                Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int {
        return media.size
    }
}
