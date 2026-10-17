package de.herrmann_engel.rbv.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.databinding.RecViewSmallBinding
import de.herrmann_engel.rbv.db.DB_Pack_With_Meta
import de.herrmann_engel.rbv.utils.StringTools
import java.util.Locale

/**
 * Pack list with checkboxes in the advanced search, showing the number of cards and the
 * collection of each pack. Changes are reported to onCheckedChange.
 */
class AdapterPacksAdvancedSearch(
    private val packs: List<DB_Pack_With_Meta>,
    // Called on every bind: rows are rebound (e.g. when scrolled back into view) and must then
    // show the current selection
    private val isChecked: (packId: Int) -> Boolean,
    private val onCheckedChange: (packId: Int, checked: Boolean) -> Unit
) : RecyclerView.Adapter<AdapterPacksAdvancedSearch.ViewHolder>() {
    class ViewHolder(val binding: RecViewSmallBinding) : RecyclerView.ViewHolder(binding.root)

    private val stringTools = StringTools()

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
            val colors = context.resources.obtainTypedArray(R.array.pack_color_list)
            val pack = packs[position].pack!!
            val color = pack.colors
            if (color >= 0 && color < colors.length()) {
                viewHolder.binding.recSmallName.setTextColor(colors.getColor(color, 0))
            }
            colors.recycle()
            val size = packs[position].counter
            var checkBoxContentDescription = pack.name
            viewHolder.binding.recSmallName.text =
                String.format(
                    Locale.ROOT,
                    "%s (%d)",
                    stringTools.shorten(pack.name),
                    size
                )
            val collectionName = stringTools.shorten(packs[position].collectionName ?: "")
            viewHolder.binding.recSmallDesc.visibility = View.VISIBLE
            viewHolder.binding.recSmallDesc.text = collectionName
            checkBoxContentDescription = String.format(
                context.resources.getString(R.string.advanced_search_packs_checkbox_description),
                checkBoxContentDescription,
                collectionName
            )
            viewHolder.binding.recSmallCheckbox.contentDescription = checkBoxContentDescription
            val extra = pack.uid
            viewHolder.binding.recSmallCheckbox.isChecked = isChecked(extra)
            viewHolder.binding.recSmallCheckbox.setOnCheckedChangeListener { _, checked ->
                onCheckedChange(extra, checked)
            }
        }
    }

    override fun getItemCount(): Int {
        return 1.coerceAtLeast(packs.size)
    }
}
