package de.herrmann_engel.rbv.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.RecyclerView
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.activities.FileTools
import de.herrmann_engel.rbv.databinding.RecViewFilesBinding
import de.herrmann_engel.rbv.utils.ContextTools


class AdapterFilesManage(
    private val files: ArrayList<DocumentFile>
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
        if (files.isEmpty()) {
            viewHolder.binding.recFilesName.text =
                context.resources.getString(R.string.no_files_without_media)
            viewHolder.binding.recFilesOpen.visibility = View.GONE
            viewHolder.binding.recFilesShare.visibility = View.GONE
            viewHolder.binding.recFilesDelete.visibility = View.GONE
        } else {
            val currentFile = files[position]
            viewHolder.binding.recFilesName.text = currentFile.name
            viewHolder.binding.recFilesShare.visibility = View.VISIBLE
            viewHolder.binding.recFilesShare.setOnClickListener {
                (ContextTools().getActivity(context) as FileTools).shareFile(currentFile.name)
            }
            viewHolder.binding.recFilesOpen.visibility = View.VISIBLE
            viewHolder.binding.recFilesOpen.setOnClickListener {
                (ContextTools().getActivity(context) as FileTools).openFile(currentFile.name)
            }
            viewHolder.binding.recFilesDelete.setOnClickListener {
                val dialog = (ContextTools().getActivity(context) as FileTools).showDeleteDialog(
                    currentFile.name,
                    context.resources.getString(R.string.delete_file_without_media_info)
                )
                dialog.setOnDismissListener {
                    if (!(ContextTools().getActivity(context) as FileTools).existsMediaFile(
                            currentFile.name
                        )
                    ) {
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
