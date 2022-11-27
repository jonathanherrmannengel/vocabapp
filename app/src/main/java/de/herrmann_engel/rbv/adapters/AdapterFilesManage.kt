package de.herrmann_engel.rbv.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.RecyclerView
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.activities.FileTools


class AdapterFilesManage(
    private val files: ArrayList<DocumentFile>,
    private val c: Context
) : RecyclerView.Adapter<AdapterFilesManage.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val fileNameTextView: TextView = view.findViewById(R.id.rec_files_name)
        val openButton: Button = view.findViewById(R.id.rec_files_open)
        val shareButton: Button = view.findViewById(R.id.rec_files_share)
        val deleteButton: Button = view.findViewById(R.id.rec_files_delete)
        val missingTextView: TextView = view.findViewById(R.id.rec_files_missing)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.rec_view_files, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.missingTextView.visibility = View.GONE
        if (files.isEmpty()) {
            viewHolder.fileNameTextView.text =
                c.resources.getString(R.string.no_files_without_media)
            viewHolder.openButton.visibility = View.GONE
            viewHolder.shareButton.visibility = View.GONE
            viewHolder.deleteButton.visibility = View.GONE
        } else {
            viewHolder.fileNameTextView.text = files[position].name
            viewHolder.shareButton.visibility = View.VISIBLE
            viewHolder.shareButton.setOnClickListener {
                (c as FileTools).shareFile(files[position].name)
            }
            viewHolder.openButton.visibility = View.VISIBLE
            viewHolder.openButton.setOnClickListener {
                (c as FileTools).openFile(files[position].name)
            }
            viewHolder.deleteButton.setOnClickListener {
                val dialog = (c as FileTools).showDeleteDialog(
                    files[position].name,
                    c.resources.getString(R.string.delete_file_without_media_info)
                )
                dialog.setOnDismissListener {
                    if (!c.existsMediaFile(files[position].name)) {
                        files.remove(files[position])
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
