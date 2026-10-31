package de.herrmann_engel.rbv.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import de.herrmann_engel.rbv.databinding.RecViewLanguageBinding

/**
 * Lists the languages of the language picker (see LanguagePicker), the selected one with a check
 * mark. A search shows only the matching languages, without the entry for no chosen language and
 * without group separators. Clicking an entry picks it.
 */
class AdapterLanguages(
    private val entries: List<Entry>,
    private val selectedTag: String?,
    private val onPick: (String?) -> Unit
) : RecyclerView.Adapter<AdapterLanguages.ViewHolder>() {
    /**
     * An entry of the list: tag is null for the entry without a chosen language; desc may contain
     * a LocaleSpan; code is shown after desc, but not read by screen readers; search is the
     * lowercase text a search query is looked up in; endsGroup draws a separator below the entry.
     */
    data class Entry(
        val tag: String?,
        val name: String,
        val desc: CharSequence,
        val code: String,
        val search: String,
        val endsGroup: Boolean
    )

    class ViewHolder(val binding: RecViewLanguageBinding) : RecyclerView.ViewHolder(binding.root)

    private var shown = entries

    // Shows the entries matching the query (lowercase), or all entries for an empty query
    fun filter(query: String) {
        val old = shown
        shown = if (query.isBlank()) {
            entries
        } else {
            entries.filter { it.tag != null && it.search.contains(query.trim()) }
        }
        DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = old.size
            override fun getNewListSize() = shown.size
            override fun areItemsTheSame(oldPosition: Int, newPosition: Int) =
                old[oldPosition].tag == shown[newPosition].tag
            // The separators are only shown without a search
            override fun areContentsTheSame(oldPosition: Int, newPosition: Int) =
                (old === entries) == (shown === entries)
        }).dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RecViewLanguageBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val entry = shown[position]
        val selected = entry.tag == selectedTag
        viewHolder.binding.recLanguageName.text = entry.name
        viewHolder.binding.recLanguageDesc.text = entry.desc
        viewHolder.binding.recLanguageDesc.visibility =
            if (entry.desc.isEmpty()) View.GONE else View.VISIBLE
        viewHolder.binding.recLanguageCode.text = entry.code
        viewHolder.binding.recLanguageCode.visibility =
            if (entry.code.isEmpty()) View.GONE else View.VISIBLE
        viewHolder.binding.recLanguageSelected.visibility =
            if (selected) View.VISIBLE else View.INVISIBLE
        val separated = entry.endsGroup && shown === entries
        viewHolder.binding.recLanguageSeparator.visibility =
            if (separated) View.VISIBLE else View.GONE
        // Screen readers read the row's texts (a content description would lose the language of
        // the own name) and announce the selection
        viewHolder.binding.recLanguageRow.isSelected = selected
        viewHolder.binding.recLanguageRow.setOnClickListener { onPick(entry.tag) }
    }

    override fun getItemCount(): Int {
        return shown.size
    }
}
