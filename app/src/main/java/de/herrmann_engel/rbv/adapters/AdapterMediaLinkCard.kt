package de.herrmann_engel.rbv.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.RecyclerView
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.activities.FileTools
import de.herrmann_engel.rbv.db.DB_Media_Link_Card
import de.herrmann_engel.rbv.db.utils.DB_Helper_Delete
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get

class AdapterMediaLinkCard(
    private val media: ArrayList<DB_Media_Link_Card>,
    private val cardNo: Int,
    private val folder: String?,
    private val c: Context
) : RecyclerView.Adapter<AdapterMediaLinkCard.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val fileNameTextView: TextView = view.findViewById(R.id.rec_media_link_card_name)
        val openButton: Button = view.findViewById(R.id.rec_media_link_card_open)
        val shareButton: Button = view.findViewById(R.id.rec_media_link_card_share)
        val deleteButton: Button = view.findViewById(R.id.rec_media_link_card_delete)
        val missingTextView: TextView = view.findViewById(R.id.rec_media_link_card_missing)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.rec_view_media_link_card, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.shareButton.visibility = View.GONE
        viewHolder.openButton.visibility = View.GONE
        viewHolder.deleteButton.visibility = View.GONE
        viewHolder.missingTextView.visibility = View.GONE
        if (media.isEmpty()) {
            viewHolder.fileNameTextView.text = c.getString(R.string.no_media)
        } else {
            viewHolder.deleteButton.visibility = View.VISIBLE
            val dbHelperGet = DB_Helper_Get(c)
            val currentMedia = dbHelperGet.getSingleMedia(media[position].file)
            if (currentMedia != null) {
                val fileName = currentMedia.file
                viewHolder.fileNameTextView.text = fileName
                val outputDirectory = DocumentFile.fromTreeUri(c, Uri.parse(folder))
                if (outputDirectory?.findFile(fileName) != null) {
                    viewHolder.shareButton.visibility = View.VISIBLE
                    viewHolder.shareButton.setOnClickListener {
                        (c as FileTools).shareFile(media[position].file)
                    }
                    viewHolder.openButton.visibility = View.VISIBLE
                    viewHolder.openButton.setOnClickListener {
                        (c as FileTools).openFile(media[position].file)
                    }
                    viewHolder.deleteButton.setOnClickListener {
                        if (deleteItem(position)) {
                            (c as FileTools).showDeleteDialog(fileName)
                        }
                    }
                } else {
                    viewHolder.missingTextView.text = c.resources.getString(R.string.media_missing)
                    viewHolder.missingTextView.visibility = View.VISIBLE
                    viewHolder.deleteButton.setOnClickListener {
                        deleteItem(position)
                    }
                }
            } else {
                viewHolder.missingTextView.text = c.resources.getString(R.string.media_missing_db)
                viewHolder.missingTextView.visibility = View.VISIBLE
                viewHolder.deleteButton.setOnClickListener {
                    deleteItem(position)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return 1.coerceAtLeast(media.size)
    }

    private fun deleteItem(position: Int): Boolean {
        val fileId = media[position].file
        val dbHelperGet = DB_Helper_Get(c)
        val dbHelperDelete = DB_Helper_Delete(c)
        dbHelperDelete.deleteMediaLink(fileId, cardNo)
        media.remove(media[position])
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, media.size)
        if (dbHelperGet.getAllMediaLinksByFile(fileId).size == 0) {
            dbHelperDelete.deleteMedia(fileId)
            return true
        }
        return false
    }
}
