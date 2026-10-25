package de.herrmann_engel.rbv.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.databinding.RecViewFilesBinding
import de.herrmann_engel.rbv.db.DbMedia
import de.herrmann_engel.rbv.media.MediaFiles

/**
 * List of the media files linked to a card (in the card's media settings). Missing files are
 * marked. The delete button removes the media from the list and reports it (onRemove).
 */
class AdapterMediaLinkCard(
    private val media: MutableList<DbMedia>,
    private val mediaFiles: MediaFiles,
    private val onRemove: (DbMedia) -> Unit
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
            if (mediaFiles.existsMediaFile(fileName)) {
                viewHolder.binding.recFilesShare.visibility = View.VISIBLE
                viewHolder.binding.recFilesShare.setOnClickListener {
                    mediaFiles.shareFile(fileId)
                }
                viewHolder.binding.recFilesOpen.visibility = View.VISIBLE
                viewHolder.binding.recFilesOpen.setOnClickListener {
                    mediaFiles.openFile(fileId)
                }
            } else {
                viewHolder.binding.recFilesMissing.text =
                    context.resources.getString(R.string.media_missing)
                viewHolder.binding.recFilesMissing.visibility = View.VISIBLE
            }
            viewHolder.binding.recFilesDelete.setOnClickListener {
                onRemove(currentMedia)
                media.remove(currentMedia)
                notifyItemRemoved(viewHolder.bindingAdapterPosition)
                notifyItemRangeChanged(viewHolder.bindingAdapterPosition, media.size)
            }
        }
    }

    override fun getItemCount(): Int {
        return 1.coerceAtLeast(media.size)
    }
}
