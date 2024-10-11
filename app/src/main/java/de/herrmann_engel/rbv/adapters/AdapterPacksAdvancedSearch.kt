package de.herrmann_engel.rbv.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.activities.AdvancedSearch
import de.herrmann_engel.rbv.databinding.RecViewSmallBinding
import de.herrmann_engel.rbv.db.DB_Pack
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get
import de.herrmann_engel.rbv.utils.ContextTools
import de.herrmann_engel.rbv.utils.StringTools
import java.util.Locale

class AdapterPacksAdvancedSearch(
    private val pack: List<DB_Pack>,
    private val packList: ArrayList<Int>
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
        viewHolder.setIsRecyclable(false)
        if (pack.isEmpty()) {
            viewHolder.binding.recSmallName.text =
                context.resources.getString(R.string.welcome_pack)
            viewHolder.binding.recSmallDesc.visibility = View.GONE
            viewHolder.binding.recSmallCheckbox.visibility = View.GONE
        } else {
            val colors = context.resources.obtainTypedArray(R.array.pack_color_list)
            val color = pack[position].colors
            if (color < colors.length() && color >= 0) {
                viewHolder.binding.recSmallName.setTextColor(colors.getColor(color, 0))
            }
            colors.recycle()
            val dbHelperGet =
                DB_Helper_Get(context)
            val size = dbHelperGet.countCardsInPack(pack[position].uid)
            var checkBoxContentDescription = pack[position].name
            viewHolder.binding.recSmallName.text =
                String.format(
                    Locale.ROOT,
                    "%s (%d)",
                    stringTools.shorten(pack[position].name),
                    size
                )
            try {
                val collectionName =
                    stringTools
                        .shorten(
                            DB_Helper_Get(context)
                                .getSingleCollection(pack[position].collection)
                                .name
                        )
                viewHolder.binding.recSmallDesc.visibility = View.VISIBLE
                viewHolder.binding.recSmallDesc.text = collectionName
                checkBoxContentDescription = String.format(
                    context.resources.getString(R.string.advanced_search_packs_checkbox_description),
                    checkBoxContentDescription,
                    collectionName
                )
            } catch (e: Exception) {
                Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show()
            }
            viewHolder.binding.recSmallCheckbox.contentDescription = checkBoxContentDescription
            val extra = pack[position].uid
            viewHolder.binding.recSmallCheckbox.isChecked = packList.contains(extra)
            viewHolder.binding.recSmallCheckbox.setOnCheckedChangeListener { _, checked ->
                if (checked) {
                    (ContextTools().getActivity(context) as AdvancedSearch).addToPackList(extra)
                } else {
                    (ContextTools().getActivity(context) as AdvancedSearch).removeFromPackList(extra)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return pack.size.coerceAtLeast(1)
    }
}
