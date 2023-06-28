package de.herrmann_engel.rbv.adapters

import android.app.Dialog
import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.herrmann_engel.rbv.Globals
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.activities.PackActionsActivity
import de.herrmann_engel.rbv.databinding.RecViewBinding
import de.herrmann_engel.rbv.db.DB_Collection
import de.herrmann_engel.rbv.db.DB_Pack
import de.herrmann_engel.rbv.db.utils.DB_Helper_Update
import de.herrmann_engel.rbv.utils.ContextTools

class AdapterCollectionsMovePack(
    private val collection: List<DB_Collection>,
    private val packs: ArrayList<DB_Pack>,
    private val dialog: Dialog
) : RecyclerView.Adapter<AdapterCollectionsMovePack.ViewHolder>() {
    class ViewHolder(val binding: RecViewBinding) : RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding = RecViewBinding.inflate(
            LayoutInflater.from(viewGroup.context),
            viewGroup,
            false
        )
        val settings =
            viewGroup.context.getSharedPreferences(Globals.SETTINGS_NAME, Context.MODE_PRIVATE)
        if (settings.getBoolean("ui_font_size", false)) {
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
        val colors = context.resources.obtainTypedArray(R.array.pack_color_list)
        val color = collection[position].colors
        if (color < colors.length() && color >= 0) {
            viewHolder.binding.recName.setTextColor(colors.getColor(color, 0))
        }
        colors.recycle()
        viewHolder.binding.recName.text = collection[position].name
        val currentCollectionId = collection[position].uid
        viewHolder.binding.recName.setOnClickListener {
            val updateHelper =
                DB_Helper_Update(context)
            val packIds = arrayListOf<Int>()
            for (pack in packs) {
                if (pack.collection != currentCollectionId) {
                    packIds.add(pack.uid)
                    pack.collection = currentCollectionId
                    updateHelper.updatePack(pack)
                }
            }
            (ContextTools().getActivity(context) as PackActionsActivity).movedPacks(packIds)
            dialog.dismiss()
        }
    }

    override fun getItemCount(): Int {
        return collection.size
    }
}
