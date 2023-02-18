package de.herrmann_engel.rbv.adapters

import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.activities.FileTools
import de.herrmann_engel.rbv.databinding.RecViewBinding
import de.herrmann_engel.rbv.db.DB_Media_Link_Card
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get
import de.herrmann_engel.rbv.utils.ContextTools


class AdapterMediaLinkCardAll(
    private val mediaLinks: ArrayList<DB_Media_Link_Card>,
    private val onlyImages: Boolean
) : RecyclerView.Adapter<AdapterMediaLinkCardAll.ViewHolder>() {
    class ViewHolder(val binding: RecViewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RecViewBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val context = viewHolder.binding.root.context
        val currentMediaLink = mediaLinks[position]
        val fileId = currentMediaLink.file
        val dbHelperGet = DB_Helper_Get(context)
        val currentMedia = dbHelperGet.getSingleMedia(fileId)
        if (currentMedia != null) {
            val fileName = currentMedia.file
            val fileNameSpannable = SpannableString(fileName)
            fileNameSpannable.setSpan(UnderlineSpan(), 0, fileName.length, 0)
            viewHolder.binding.recName.text = fileNameSpannable
            viewHolder.binding.recName.setOnClickListener {
                if (onlyImages) {
                    (ContextTools().getActivity(context) as FileTools).showImageDialog(fileId)
                } else {
                    (ContextTools().getActivity(context) as FileTools).openFile(fileId)
                }
            }
        } else {
            viewHolder.binding.recName.text = context.resources.getString(R.string.media_missing_db)
            viewHolder.binding.recName.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.warn_red
                )
            )
        }
    }

    override fun getItemCount(): Int {
        return mediaLinks.size
    }
}
