package de.herrmann_engel.rbv.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.herrmann_engel.rbv.databinding.RecViewSmallBinding
import de.herrmann_engel.rbv.db.DbTag
import de.herrmann_engel.rbv.utils.StringTools

/**
 * Tag list with checkboxes in the advanced search. Changes are reported to onCheckedChange.
 */
class AdapterTagsAdvancedSearch(
    private val tags: List<DbTag>,
    // Called when a row is bound, so that it shows the current selection
    private val isChecked: (tagId: Int) -> Boolean,
    private val onCheckedChange: (tagId: Int, checked: Boolean) -> Unit
) : RecyclerView.Adapter<AdapterTagsAdvancedSearch.ViewHolder>() {
    class ViewHolder(val binding: RecViewSmallBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RecViewSmallBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        val viewHolder = ViewHolder(binding)
        // Only taps are reported, not the state set when a row is bound
        binding.recSmallCheckbox.setOnClickListener {
            val position = viewHolder.bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                onCheckedChange(tags[position].uid, binding.recSmallCheckbox.isChecked)
            }
        }
        return viewHolder
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.binding.recSmallName.text = StringTools.shorten(tags[position].name)
        if (tags[position].emoji.isBlank()) {
            viewHolder.binding.recSmallDesc.visibility = View.GONE
        } else {
            viewHolder.binding.recSmallDesc.visibility = View.VISIBLE
            viewHolder.binding.recSmallDesc.text = tags[position].emoji
        }
        viewHolder.binding.recSmallCheckbox.contentDescription = tags[position].name
        viewHolder.binding.recSmallCheckbox.isChecked = isChecked(tags[position].uid)
    }

    override fun getItemCount(): Int {
        return tags.size
    }
}
