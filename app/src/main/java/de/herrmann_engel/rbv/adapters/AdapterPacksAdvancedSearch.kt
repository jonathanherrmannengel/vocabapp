package de.herrmann_engel.rbv.adapters

import android.view.LayoutInflater
import android.view.View
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
    // Called on every bind: rows are rebound (e.g. when scrolled back into view) and must then
    // show the current selection
    private val isChecked: (packId: Int) -> Boolean,
    private val onCheckedChange: (packId: Int, checked: Boolean) -> Unit
) : RecyclerView.Adapter<AdapterPacksAdvancedSearch.ViewHolder>() {
    class ViewHolder(val binding: RecViewSmallBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RecViewSmallBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val context = viewHolder.binding.root.context
        // Not recyclable, so that checkbox listeners are not triggered for recycled views
        viewHolder.setIsRecyclable(false)
        // Placeholder item if there are no packs
        if (packs.isEmpty()) {
            viewHolder.binding.recSmallName.text =
                context.resources.getString(R.string.welcome_pack)
            viewHolder.binding.recSmallDesc.visibility = View.GONE
            viewHolder.binding.recSmallCheckbox.visibility = View.GONE
        } else {
            val pack = packs[position].pack
            context.resources.colorAt(R.array.pack_color_list, pack.colors)
                ?.let { viewHolder.binding.recSmallName.setTextColor(it) }
            val size = packs[position].counter
            var checkBoxContentDescription = pack.name
            viewHolder.binding.recSmallName.text =
                String.format(
                    Locale.ROOT,
                    "%s (%d)",
                    StringTools.shorten(pack.name),
                    size
                )
            val collectionName = StringTools.shorten(packs[position].collectionName ?: "")
            viewHolder.binding.recSmallDesc.visibility = View.VISIBLE
            viewHolder.binding.recSmallDesc.text = collectionName
            checkBoxContentDescription = String.format(
                context.resources.getString(R.string.advanced_search_packs_checkbox_description),
                checkBoxContentDescription,
                collectionName
            )
            viewHolder.binding.recSmallCheckbox.contentDescription = checkBoxContentDescription
            val packId = pack.uid
            viewHolder.binding.recSmallCheckbox.isChecked = isChecked(packId)
            viewHolder.binding.recSmallCheckbox.setOnCheckedChangeListener { _, checked ->
                onCheckedChange(packId, checked)
            }
        }
    }

    override fun getItemCount(): Int {
        return 1.coerceAtLeast(packs.size)
    }
}
