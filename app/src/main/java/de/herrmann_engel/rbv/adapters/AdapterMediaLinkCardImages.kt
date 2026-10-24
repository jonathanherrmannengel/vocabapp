package de.herrmann_engel.rbv.adapters

import android.app.Dialog
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.databinding.RecViewImageBinding
import de.herrmann_engel.rbv.db.DbMedia
import de.herrmann_engel.rbv.media.MediaFiles


/**
 * Grid of image previews of a card. Clicking an image shows it fullscreen.
 */
class AdapterMediaLinkCardImages(
    private val media: List<DbMedia>,
    private val cardId: Int,
    private val dialog: Dialog,
    private val mediaFiles: MediaFiles
) : RecyclerView.Adapter<AdapterMediaLinkCardImages.ViewHolder>() {
    class ViewHolder(val binding: RecViewImageBinding) : RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RecViewImageBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val context = viewHolder.binding.root.context
        // Red background marks images that cannot be loaded; removed once the image is found
        viewHolder.binding.recImg.setBackgroundColor(
            ContextCompat.getColor(
                context,
                R.color.warn_red
            )
        )
        val currentMediaLink = media[position]
        val fileId = currentMediaLink.uid
        val uri = mediaFiles.getImageUri(fileId)
        if (uri != null) {
            viewHolder.binding.recImg.load(uri)
            viewHolder.binding.recImg.setBackgroundColor(Color.TRANSPARENT)
        }
        viewHolder.binding.recImg.setOnClickListener {
            mediaFiles.showImageDialog(
                fileId,
                cardId,
                dialog
            )
        }
    }

    override fun getItemCount(): Int {
        return media.size
    }

}
