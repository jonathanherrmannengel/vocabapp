package de.herrmann_engel.rbv.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.databinding.RecViewSmallBinding
import de.herrmann_engel.rbv.db.PackWithMeta
import de.herrmann_engel.rbv.utils.StringTools
import de.herrmann_engel.rbv.utils.colorAt
import java.util.Locale

/**
 * Pack list with checkboxes in the advanced search, showing the number of cards and the
 * collection of each pack. Changes are reported to onCheckedChange.
 */
class AdapterPacksAdvancedSearch(
    private val packs: List<PackWithMeta>,
    // Called when a row is bound, so that it shows the current selection
    private val isChecked: (packId: Int) -> Boolean,
    private val onCheckedChange: (packId: Int, checked: Boolean) -> Unit
) : RecyclerView.Adapter<AdapterPacksAdvancedSearch.ViewHolder>() {
    class ViewHolder(val binding: RecViewSmallBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RecViewSmallBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        val viewHolder = ViewHolder(binding)
        // Only taps are reported, not the state set when a row is bound
        binding.recSmallCheckbox.setOnClickListener {
            val position = viewHolder.bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                onCheckedChange(packs[position].pack.uid, binding.recSmallCheckbox.isChecked)
            }
        }
        return viewHolder
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val context = viewHolder.binding.root.context
        val pack = packs[position].pack
        context.resources.colorAt(R.array.pack_color_list, pack.colors)
            ?.let { viewHolder.binding.recSmallName.setTextColor(it) }
        viewHolder.binding.recSmallName.text = String.format(
            Locale.ROOT,
            "%s (%d)",
            StringTools.shorten(pack.name),
            packs[position].counter
        )
        val collectionName = StringTools.shorten(packs[position].collectionName ?: "")
        viewHolder.binding.recSmallDesc.text = collectionName
        viewHolder.binding.recSmallCheckbox.contentDescription = String.format(
            context.resources.getString(R.string.advanced_search_packs_checkbox_description),
            pack.name,
            collectionName
        )
        viewHolder.binding.recSmallCheckbox.isChecked = isChecked(pack.uid)
    }

    override fun getItemCount(): Int {
        return packs.size
    }
}
