package de.herrmann_engel.rbv.adapters

import android.content.Context
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.activities.FileTools
import de.herrmann_engel.rbv.db.DB_Media_Link_Card
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get


class AdapterMediaLinkCardAll(
    private val media: ArrayList<DB_Media_Link_Card>,
    private val onlyImages: Boolean,
    private val c: Context
) : RecyclerView.Adapter<AdapterMediaLinkCardAll.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.rec_name)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.rec_view, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val dbHelperGet = DB_Helper_Get(c)
        val currentMedia = dbHelperGet.getSingleMedia(media[position].file)
        if (currentMedia != null) {
            val fileName = currentMedia.file
            val fileNameSpannable = SpannableString(fileName)
            fileNameSpannable.setSpan(UnderlineSpan(), 0, fileName.length, 0)
            viewHolder.textView.text = fileNameSpannable
            viewHolder.textView.setOnClickListener {
                if (onlyImages) {
                    (c as FileTools).showImageDialog(media[position].file)
                } else {
                    (c as FileTools).openFile(media[position].file)
                }
            }
        } else {
            viewHolder.textView.text = c.resources.getString(R.string.media_missing_db)
            viewHolder.textView.setTextColor(ContextCompat.getColor(c, R.color.warn_red))
        }
    }

    override fun getItemCount(): Int {
        return media.size
    }
}
