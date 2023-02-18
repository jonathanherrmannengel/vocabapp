package de.herrmann_engel.rbv.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.activities.FileTools
import de.herrmann_engel.rbv.databinding.RecViewImageBinding
import de.herrmann_engel.rbv.db.DB_Media_Link_Card
import de.herrmann_engel.rbv.utils.ContextTools


class AdapterMediaLinkCardImages(
    private val media: ArrayList<DB_Media_Link_Card>
) : RecyclerView.Adapter<AdapterMediaLinkCardImages.ViewHolder>() {
    class ViewHolder(val binding: RecViewImageBinding) : RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RecViewImageBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val context = viewHolder.binding.root.context
        viewHolder.binding.recImg.setBackgroundColor(
            ContextCompat.getColor(
                context,
                R.color.warn_red
            )
        )
        val uri =
            (ContextTools().getActivity(context) as FileTools).getImageUri(media[position].file)
        if (uri != null) {
            Picasso.get().load(uri).fit().centerCrop().into(viewHolder.binding.recImg)
            viewHolder.binding.recImg.setBackgroundColor(Color.TRANSPARENT)
        }
        val currentMedia = media[position]
        viewHolder.binding.recImg.setOnClickListener {
            if (uri != null) {
                (ContextTools().getActivity(context) as FileTools).showImageDialog(currentMedia.file)
            } else if (!(ContextTools().getActivity(context) as FileTools).existsMediaFile(currentMedia.file)) {
                (ContextTools().getActivity(context) as FileTools).showMissingDialog(currentMedia.card)
            }
        }
    }

    override fun getItemCount(): Int {
        return media.size
    }

}
