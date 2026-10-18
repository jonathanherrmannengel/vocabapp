package de.herrmann_engel.rbv.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.herrmann_engel.rbv.databinding.RecViewSmallBinding
import de.herrmann_engel.rbv.db.DB_Tag
import de.herrmann_engel.rbv.utils.StringTools

/**
 * Tag list with checkboxes in the advanced search. Changes are reported to onCheckedChange.
 */
class AdapterTagsAdvancedSearch(
    private val tags: List<DB_Tag>,
    // Called on every bind: rows are rebound (e.g. when scrolled back into view) and must then
    // show the current selection
    private val isChecked: (tagId: Int) -> Boolean,
    private val onCheckedChange: (tagId: Int, checked: Boolean) -> Unit
) : RecyclerView.Adapter<AdapterTagsAdvancedSearch.ViewHolder>() {
    class ViewHolder(val binding: RecViewSmallBinding) : RecyclerView.ViewHolder(binding.root)

    private val stringTools = StringTools()

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RecViewSmallBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        // Not recyclable, so that checkbox listeners are not triggered for recycled views
        viewHolder.setIsRecyclable(false)
        viewHolder.binding.recSmallName.text = stringTools.shorten(tags[position].name)
        if (tags[position].emoji.isNullOrBlank()) {
            viewHolder.binding.recSmallDesc.visibility = View.GONE
        } else {
            viewHolder.binding.recSmallDesc.visibility = View.VISIBLE
            viewHolder.binding.recSmallDesc.text = tags[position].emoji
        }
        viewHolder.binding.recSmallCheckbox.contentDescription = tags[position].name
        val tagId = tags[position].uid
        viewHolder.binding.recSmallCheckbox.isChecked = isChecked(tagId)
        viewHolder.binding.recSmallCheckbox.setOnCheckedChangeListener { _, checked ->
            onCheckedChange(tagId, checked)
        }
    }

    override fun getItemCount(): Int {
        return tags.size
    }
}
