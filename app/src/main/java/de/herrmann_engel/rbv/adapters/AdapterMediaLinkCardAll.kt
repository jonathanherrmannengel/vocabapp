package de.herrmann_engel.rbv.adapters

import android.app.Dialog
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.herrmann_engel.rbv.databinding.RecViewBinding
import de.herrmann_engel.rbv.db.DB_Media
import de.herrmann_engel.rbv.utils.MediaFiles


/**
 * List of the media files of a card by file name. Clicking a file shows it as image
 * (if onlyImages is set) or opens it in another app.
 */
class AdapterMediaLinkCardAll(
    private val media: List<DB_Media>,
    private val cardNo: Int,
    private val onlyImages: Boolean,
    private val dialog: Dialog,
    private val mediaFiles: MediaFiles
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
                mediaFiles.showImageDialog(
                    fileId,
                    cardNo,
                    dialog
                )
            } else {
                mediaFiles.openFile(fileId)
            }
        }
    }

    override fun getItemCount(): Int {
        return media.size
    }
}
