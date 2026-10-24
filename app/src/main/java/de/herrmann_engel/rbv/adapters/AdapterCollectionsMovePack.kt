package de.herrmann_engel.rbv.adapters

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.databinding.RecViewBinding
import de.herrmann_engel.rbv.db.DB_Collection
import de.herrmann_engel.rbv.settings.AppSettings
import de.herrmann_engel.rbv.utils.colorAt

/**
 * Collection list in the "move pack" dialog. Clicking a collection reports its id to onSelected.
 */
class AdapterCollectionsMovePack(
    private val collections: List<DB_Collection>,
    private val onSelected: (collectionId: Int) -> Unit
) : RecyclerView.Adapter<AdapterCollectionsMovePack.ViewHolder>() {
    class ViewHolder(val binding: RecViewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding = RecViewBinding.inflate(
            LayoutInflater.from(viewGroup.context),
            viewGroup,
            false
        )
        val settings = AppSettings(viewGroup.context)
        // Larger font size setting
        if (settings.uiFontSizeBig) {
            binding.recName.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                viewGroup.context.resources.getDimension(R.dimen.rec_view_font_size_big)
            )
            binding.recDesc.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                viewGroup.context.resources.getDimension(R.dimen.rec_view_font_size_below_big)
            )
        }
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val context = viewHolder.binding.root.context
        context.resources.colorAt(R.array.pack_color_list, collections[position].colors)
            ?.let { viewHolder.binding.recName.setTextColor(it) }
        viewHolder.binding.recName.text = collections[position].name
        val collectionId = collections[position].uid
        viewHolder.binding.recName.setOnClickListener { onSelected(collectionId) }
    }

    override fun getItemCount(): Int {
        return collections.size
    }
}
