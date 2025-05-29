package de.herrmann_engel.rbv.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.RecyclerView
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.activities.FileTools
import de.herrmann_engel.rbv.databinding.RecViewFilesBinding
import de.herrmann_engel.rbv.db.DB_Media
import de.herrmann_engel.rbv.db.utils.DB_Helper_Delete
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get
import de.herrmann_engel.rbv.utils.ContextTools

class AdapterMediaLinkCard(
    private val media: MutableList<DB_Media>,
    private val cardNo: Int,
    private val folder: String?
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
        if (media.isEmpty()) {
            viewHolder.binding.recFilesName.text = context.getString(R.string.no_media)
        } else {
            viewHolder.binding.recFilesDelete.visibility = View.VISIBLE
            val currentMedia = media[position]
            val fileId = currentMedia.uid
            val fileName = currentMedia.file
            viewHolder.binding.recFilesName.text = fileName
            val outputDirectory = DocumentFile.fromTreeUri(context, Uri.parse(folder))
            if (outputDirectory?.findFile(fileName) != null) {
                viewHolder.binding.recFilesShare.visibility = View.VISIBLE
                viewHolder.binding.recFilesShare.setOnClickListener {
                    (ContextTools().getActivity(context) as FileTools).shareFile(fileId)
                }
                viewHolder.binding.recFilesOpen.visibility = View.VISIBLE
                viewHolder.binding.recFilesOpen.setOnClickListener {
                    (ContextTools().getActivity(context) as FileTools).openFile(fileId)
                }
                viewHolder.binding.recFilesDelete.setOnClickListener {
                    if (deleteItem(currentMedia, viewHolder, context)) {
                        (ContextTools().getActivity(context) as FileTools).showDeleteDialog(
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

    private fun deleteItem(
        currentMedia: DB_Media,
        viewHolder: ViewHolder,
        context: Context
    ): Boolean {
        val fileId = currentMedia.uid
        val dbHelperGet = DB_Helper_Get(context)
        val dbHelperDelete = DB_Helper_Delete(context)
        dbHelperDelete.deleteMediaLink(fileId, cardNo)
        media.remove(currentMedia)
        notifyItemRemoved(viewHolder.bindingAdapterPosition)
        notifyItemRangeChanged(viewHolder.bindingAdapterPosition, media.size)
        if (!dbHelperGet.mediaHasLink(fileId)) {
            dbHelperDelete.deleteMedia(fileId)
            return true
        }
        return false
    }
}
