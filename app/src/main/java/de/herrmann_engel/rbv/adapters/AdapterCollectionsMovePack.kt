package de.herrmann_engel.rbv.adapters

import android.app.Dialog
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.databinding.RecViewBinding
import de.herrmann_engel.rbv.db.AppDatabase
import de.herrmann_engel.rbv.db.DB_Collection
import de.herrmann_engel.rbv.db.DB_Pack
import de.herrmann_engel.rbv.db.utils.DB_Helper_Update
import de.herrmann_engel.rbv.settings.AppSettings
import de.herrmann_engel.rbv.utils.colorAt

/**
 * Collection list in the "move pack" dialog. Clicking a collection moves the packs there and
 * reports the ids of the moved packs to onMoved.
 */
class AdapterCollectionsMovePack(
    private val collection: List<DB_Collection>,
    private val packs: List<DB_Pack>,
    private val dialog: Dialog,
    private val onMoved: (ArrayList<Int>) -> Unit
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
        context.resources.colorAt(R.array.pack_color_list, collection[position].colors)
            ?.let { viewHolder.binding.recName.setTextColor(it) }
        viewHolder.binding.recName.text = collection[position].name
        val currentCollectionId = collection[position].uid
        viewHolder.binding.recName.setOnClickListener {
            val updateHelper =
                DB_Helper_Update(context)
            val packIds = arrayListOf<Int>()
            // Only packs not already in the target collection are moved and reported
            // All packs are moved or none
            AppDatabase.getInstance(context).runInTransaction {
                for (pack in packs) {
                    if (pack.collection != currentCollectionId) {
                        packIds.add(pack.uid)
                        pack.collection = currentCollectionId
                        updateHelper.updatePack(pack)
                    }
                }
            }
            onMoved(packIds)
            dialog.dismiss()
        }
    }

    override fun getItemCount(): Int {
        return collection.size
    }
}
