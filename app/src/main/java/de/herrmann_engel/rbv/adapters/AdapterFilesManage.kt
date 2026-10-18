package de.herrmann_engel.rbv.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.RecyclerView
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.databinding.RecViewFilesBinding
import de.herrmann_engel.rbv.media.MediaFiles


/**
 * Lists files in the media folder that are not registered as media in the database,
 * with buttons to open, share and delete them.
 */
class AdapterFilesManage(
    private val files: MutableList<DocumentFile>,
    private val mediaFiles: MediaFiles
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
                mediaFiles.shareFile(currentFile.name!!)
            }
            viewHolder.binding.recFilesOpen.setOnClickListener {
                mediaFiles.openFile(currentFile.name!!)
            }
            viewHolder.binding.recFilesDelete.setOnClickListener {
                mediaFiles.showDeleteDialog(
                    currentFile.name!!,
                    context.resources.getString(R.string.delete_file_without_media_info)
                ) {
                    // Remove the item if the file has actually been deleted
                    if (!mediaFiles.existsMediaFile(currentFile.name)) {
                        files.remove(currentFile)
                        notifyItemRemoved(position)
                        notifyItemRangeChanged(position, files.size)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return 1.coerceAtLeast(files.size)
    }
}
