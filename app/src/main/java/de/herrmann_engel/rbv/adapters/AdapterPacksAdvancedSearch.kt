package de.herrmann_engel.rbv.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.activities.AdvancedSearch
import de.herrmann_engel.rbv.db.DB_Pack
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get
import de.herrmann_engel.rbv.utils.StringTools

class AdapterPacksAdvancedSearch(
    private val pack: List<DB_Pack>,
    private val c: Context,
    private val packList: ArrayList<Int>,
) : RecyclerView.Adapter<AdapterPacksAdvancedSearch.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkBox: CheckBox = view.findViewById(R.id.rec_small_checkbox)
        val textView: TextView = view.findViewById(R.id.rec_small_name)
        val textViewDesc: TextView = view.findViewById(R.id.rec_small_desc)
    }

    private val stringTools = StringTools()

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.rec_view_small, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.setIsRecyclable(false)
        if (pack.isEmpty()) {
            viewHolder.textView.text = c.resources.getString(R.string.welcome_pack)
            viewHolder.textViewDesc.visibility = View.GONE
            viewHolder.checkBox.visibility = View.GONE
        } else {
            val colors = c.resources.obtainTypedArray(R.array.pack_color_list)
            val color = pack[position].colors
            if (color < colors.length() && color >= 0) {
                viewHolder.textView.setTextColor(colors.getColor(color, 0))
            }
            colors.recycle()
            val dbHelperGet =
                DB_Helper_Get(c.applicationContext)
            val size = dbHelperGet.countCardsInPack(pack[position].uid)
            var checkBoxContentDescription = pack[position].name
            viewHolder.textView.text = String.format("%s (%d)", pack[position].name, size)
            try {
                val collectionName =
                    stringTools
                        .shorten(
                            DB_Helper_Get(c)
                                .getSingleCollection(pack[position].collection)
                                .name
                        )
                viewHolder.textViewDesc.visibility = View.VISIBLE
                viewHolder.textViewDesc.text = collectionName
                checkBoxContentDescription = String.format(
                    c.resources.getString(R.string.advanced_search_packs_checkbox_description),
                    checkBoxContentDescription,
                    collectionName
                )
            } catch (e: Exception) {
                Toast.makeText(c.applicationContext, R.string.error, Toast.LENGTH_SHORT).show()
            }
            viewHolder.checkBox.contentDescription = checkBoxContentDescription
            val extra = pack[position].uid
            if (packList.contains(extra)) {
                viewHolder.checkBox.isChecked = true
            }
            viewHolder.checkBox.setOnClickListener {
                if ((it as CheckBox).isChecked) {
                    (c as AdvancedSearch).addToPackList(extra)
                } else {
                    (c as AdvancedSearch).removeFromPackList(extra)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return pack.size.coerceAtLeast(1)
    }
}
