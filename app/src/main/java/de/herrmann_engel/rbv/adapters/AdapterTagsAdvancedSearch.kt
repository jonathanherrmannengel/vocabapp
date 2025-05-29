package de.herrmann_engel.rbv.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.herrmann_engel.rbv.activities.AdvancedSearch
import de.herrmann_engel.rbv.databinding.RecViewSmallBinding
import de.herrmann_engel.rbv.db.DB_Tag
import de.herrmann_engel.rbv.utils.ContextTools
import de.herrmann_engel.rbv.utils.StringTools

class AdapterTagsAdvancedSearch(
    private val tags: List<DB_Tag>,
    private val tagIdsCheckedInitially: List<Int>
) : RecyclerView.Adapter<AdapterTagsAdvancedSearch.ViewHolder>() {
    class ViewHolder(val binding: RecViewSmallBinding) : RecyclerView.ViewHolder(binding.root)

    private val stringTools = StringTools()

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RecViewSmallBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val context = viewHolder.binding.root.context
        viewHolder.setIsRecyclable(false)
        viewHolder.binding.recSmallName.text = stringTools.shorten(tags[position].name)
        if (tags[position].emoji.isNullOrBlank()) {
            viewHolder.binding.recSmallDesc.visibility = View.GONE
        } else {
            viewHolder.binding.recSmallDesc.visibility = View.VISIBLE
            viewHolder.binding.recSmallDesc.text = tags[position].emoji
        }
        viewHolder.binding.recSmallCheckbox.contentDescription = tags[position].name
        val extra = tags[position].uid
        viewHolder.binding.recSmallCheckbox.isChecked = tagIdsCheckedInitially.contains(extra)
        viewHolder.binding.recSmallCheckbox.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                (ContextTools().getActivity(context) as AdvancedSearch).addToTagList(extra)
            } else {
                (ContextTools().getActivity(context) as AdvancedSearch).removeFromTagList(extra)
            }
        }
    }

    override fun getItemCount(): Int {
        return tags.size
    }
}
