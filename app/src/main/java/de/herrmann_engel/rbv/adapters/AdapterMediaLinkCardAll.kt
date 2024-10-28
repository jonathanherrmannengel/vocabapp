package de.herrmann_engel.rbv.adapters

import android.app.Dialog
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.herrmann_engel.rbv.activities.FileTools
import de.herrmann_engel.rbv.databinding.RecViewBinding
import de.herrmann_engel.rbv.db.DB_Media
import de.herrmann_engel.rbv.utils.ContextTools


class AdapterMediaLinkCardAll(
    private val media: ArrayList<DB_Media>,
    private val cardNo: Int,
    private val onlyImages: Boolean,
    private val dialog: Dialog
) : RecyclerView.Adapter<AdapterMediaLinkCardAll.ViewHolder>() {
    class ViewHolder(val binding: RecViewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RecViewBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val context = viewHolder.binding.root.context
        val currentMedia = media[position]
        val fileId = currentMedia.uid
        val fileName = currentMedia.file
        val fileNameSpannable = SpannableString(fileName)
        fileNameSpannable.setSpan(UnderlineSpan(), 0, fileName.length, 0)
        viewHolder.binding.recName.text = fileNameSpannable
        viewHolder.binding.recName.setOnClickListener {
            if (onlyImages) {
                (ContextTools().getActivity(context) as FileTools).showImageDialog(
                    fileId,
                    cardNo,
                    dialog
                )
            } else {
                (ContextTools().getActivity(context) as FileTools).openFile(fileId)
            }
        }
    }

    override fun getItemCount(): Int {
        return media.size
    }
}
