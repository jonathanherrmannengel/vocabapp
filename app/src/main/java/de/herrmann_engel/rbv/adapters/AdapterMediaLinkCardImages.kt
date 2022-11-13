package de.herrmann_engel.rbv.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.activities.FileTools
import de.herrmann_engel.rbv.db.DB_Media_Link_Card


class AdapterMediaLinkCardImages(
    private val media: ArrayList<DB_Media_Link_Card>,
    private val c: Context
) : RecyclerView.Adapter<AdapterMediaLinkCardImages.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.rec_img)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.rec_view_image, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.imageView.setBackgroundColor(ContextCompat.getColor(c, R.color.warn_red))
        val uri = (c as FileTools).getImageUri(media[position].file)
        if (uri != null) {
            Picasso.get().load(uri).fit().centerCrop().into(viewHolder.imageView)
            viewHolder.imageView.setBackgroundColor(Color.TRANSPARENT)
        }
        viewHolder.imageView.setOnClickListener {
            if (uri != null) {
                c.showImageDialog(media[position].file)
            } else if (!c.existsMediaFile(media[position].file)) {
                c.showMissingDialog(media[position].card)
            }
        }
    }

    override fun getItemCount(): Int {
        return media.size
    }

}
