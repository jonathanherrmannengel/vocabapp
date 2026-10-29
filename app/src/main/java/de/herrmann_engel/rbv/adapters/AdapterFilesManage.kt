package de.herrmann_engel.rbv.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.databinding.RecViewFilesBinding
import de.herrmann_engel.rbv.media.MediaFiles
import de.herrmann_engel.rbv.media.MediaFolderFile


/**
 * Lists files in the media folder that are not registered as media in the database,
 * with buttons to open, share and delete them. Deleting is reported to onDelete.
 */
class AdapterFilesManage(
    private val files: MutableList<MediaFolderFile>,
    private val mediaFiles: MediaFiles,
    private val onDelete: (MediaFolderFile) -> Unit
) : RecyclerView.Adapter<AdapterFilesManage.ViewHolder>() {
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
        viewHolder.binding.recFilesMissing.visibility = View.GONE
        // Placeholder item if there are no such files
        if (files.isEmpty()) {
            viewHolder.binding.recFilesName.text =
                context.resources.getString(R.string.no_files_without_media)
            viewHolder.binding.recFilesOpen.visibility = View.GONE
            viewHolder.binding.recFilesShare.visibility = View.GONE
            viewHolder.binding.recFilesDelete.visibility = View.GONE
        } else {
            val currentFile = files[position]
            viewHolder.binding.recFilesName.text = currentFile.name
            viewHolder.binding.recFilesShare.setOnClickListener {
                mediaFiles.shareFile(currentFile.name)
            }
            viewHolder.binding.recFilesOpen.setOnClickListener {
                mediaFiles.openFile(currentFile.name)
            }
            viewHolder.binding.recFilesDelete.setOnClickListener { onDelete(currentFile) }
        }
    }

    override fun getItemCount(): Int {
        return 1.coerceAtLeast(files.size)
    }

    // Removes a deleted file from the list; the last one is replaced by the placeholder item
    fun remove(file: MediaFolderFile) {
        val position = files.indexOf(file)
        if (position < 0) {
            return
        }
        files.removeAt(position)
        if (files.isEmpty()) {
            notifyItemChanged(0)
        } else {
            notifyItemRemoved(position)
        }
    }
}
