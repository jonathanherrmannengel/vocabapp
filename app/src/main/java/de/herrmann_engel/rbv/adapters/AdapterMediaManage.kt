package de.herrmann_engel.rbv.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.herrmann_engel.rbv.databinding.RecViewBinding
import de.herrmann_engel.rbv.db.DbMedia


/**
 * List of all media entries. Clicking one reports it to onSelected (see ManageMedia).
 */
class AdapterMediaManage(
    private val media: MutableList<DbMedia>,
    private val onSelected: (media: DbMedia) -> Unit
) : RecyclerView.Adapter<AdapterMediaManage.ViewHolder>() {
    class ViewHolder(val binding: RecViewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RecViewBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val currentMedia = media[position]
        viewHolder.binding.recName.text = currentMedia.file
        viewHolder.binding.recName.setOnClickListener { onSelected(currentMedia) }
    }

    override fun getItemCount(): Int {
        return media.size
    }

    // Removes a deleted media entry from the list
    fun remove(item: DbMedia) {
        val position = media.indexOf(item)
        if (position >= 0) {
            media.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}
