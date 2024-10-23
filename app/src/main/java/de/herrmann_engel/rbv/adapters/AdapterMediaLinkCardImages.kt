package de.herrmann_engel.rbv.adapters

import android.app.Dialog
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
    private val mediaLinks: ArrayList<DB_Media_Link_Card>,
    private val dialog: Dialog
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
        val currentMediaLink = mediaLinks[position]
        val fileId = currentMediaLink.file
        val cardId = currentMediaLink.card
        val uri = (ContextTools().getActivity(context) as FileTools).getImageUri(fileId)
        if (uri != null) {
            Picasso.get().load(uri).fit().centerCrop().into(viewHolder.binding.recImg)
            viewHolder.binding.recImg.setBackgroundColor(Color.TRANSPARENT)
        }
        viewHolder.binding.recImg.setOnClickListener {
            (ContextTools().getActivity(context) as FileTools).showImageDialog(fileId, cardId, dialog)
        }
    }

    override fun getItemCount(): Int {
        return mediaLinks.size
    }

}
