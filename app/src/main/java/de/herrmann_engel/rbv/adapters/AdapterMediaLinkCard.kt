package de.herrmann_engel.rbv.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.RecyclerView
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.databinding.RecViewFilesBinding
import de.herrmann_engel.rbv.db.DB_Media
import de.herrmann_engel.rbv.db.utils.DB_Helper_Delete
import de.herrmann_engel.rbv.media.MediaFiles

/**
 * List of the media files linked to a card (in the card's media settings). Missing files are
 * marked. The delete button removes the link; if the media is then unused, its database entry
 * is deleted too and the user is asked whether to delete the file.
 */
class AdapterMediaLinkCard(
    private val media: MutableList<DB_Media>,
    private val cardNo: Int,
    private val outputDirectory: DocumentFile?,
    private val mediaFiles: MediaFiles
) : RecyclerView.Adapter<AdapterMediaLinkCard.ViewHolder>() {
    class ViewHolder(val binding: RecViewFilesBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding = RecViewFilesBinding.inflate(
            LayoutInflater.from(viewGroup.context),
            viewGroup,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val context = viewHolder.binding.root.context
        viewHolder.binding.recFilesShare.visibility = View.GONE
        viewHolder.binding.recFilesOpen.visibility = View.GONE
        viewHolder.binding.recFilesDelete.visibility = View.GONE
        viewHolder.binding.recFilesMissing.visibility = View.GONE
        // Placeholder item if the card has no media
        if (media.isEmpty()) {
            viewHolder.binding.recFilesName.text = context.getString(R.string.no_media)
        } else {
            viewHolder.binding.recFilesDelete.visibility = View.VISIBLE
            val currentMedia = media[position]
            val fileId = currentMedia.uid
            val fileName = currentMedia.file
            viewHolder.binding.recFilesName.text = fileName
            // Without a media folder, all files count as missing
            if (outputDirectory?.findFile(fileName) != null) {
                viewHolder.binding.recFilesShare.visibility = View.VISIBLE
                viewHolder.binding.recFilesShare.setOnClickListener {
                    mediaFiles.shareFile(fileId)
                }
                viewHolder.binding.recFilesOpen.visibility = View.VISIBLE
                viewHolder.binding.recFilesOpen.setOnClickListener {
                    mediaFiles.openFile(fileId)
                }
                viewHolder.binding.recFilesDelete.setOnClickListener {
                    if (deleteItem(currentMedia, viewHolder, context)) {
                        mediaFiles.showDeleteDialog(
                            fileName
                        )
                    }
                }
            } else {
                viewHolder.binding.recFilesMissing.text =
                    context.resources.getString(R.string.media_missing)
                viewHolder.binding.recFilesMissing.visibility = View.VISIBLE
                viewHolder.binding.recFilesDelete.setOnClickListener {
                    deleteItem(currentMedia, viewHolder, context)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return 1.coerceAtLeast(media.size)
    }

    // Removes the link to the card; returns true if the media entry was deleted because it is no longer used
    private fun deleteItem(
        currentMedia: DB_Media,
        viewHolder: ViewHolder,
        context: Context
    ): Boolean {
        val mediaDeleted = DB_Helper_Delete(context).deleteMediaLink(currentMedia.uid, cardNo)
        media.remove(currentMedia)
        notifyItemRemoved(viewHolder.bindingAdapterPosition)
        notifyItemRangeChanged(viewHolder.bindingAdapterPosition, media.size)
        return mediaDeleted
    }
}
