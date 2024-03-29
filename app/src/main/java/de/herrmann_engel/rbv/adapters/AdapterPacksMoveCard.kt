package de.herrmann_engel.rbv.adapters

import android.app.Dialog
import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import de.herrmann_engel.rbv.Globals
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.activities.CardActionsActivity
import de.herrmann_engel.rbv.databinding.RecViewBinding
import de.herrmann_engel.rbv.db.DB_Card
import de.herrmann_engel.rbv.db.DB_Pack
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get
import de.herrmann_engel.rbv.db.utils.DB_Helper_Update
import de.herrmann_engel.rbv.utils.ContextTools
import de.herrmann_engel.rbv.utils.StringTools

class AdapterPacksMoveCard(
    private val pack: List<DB_Pack>,
    private val collectionNo: Int,
    private val cards: ArrayList<DB_Card>,
    private val dialog: Dialog
) : RecyclerView.Adapter<AdapterPacksMoveCard.ViewHolder>() {
    class ViewHolder(val binding: RecViewBinding) : RecyclerView.ViewHolder(binding.root)

    private val stringTools = StringTools()

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
        val color = pack[position].colors
        if (color < colors.length() && color >= 0) {
            viewHolder.binding.recName.setTextColor(colors.getColor(color, 0))
        }
        colors.recycle()
        viewHolder.binding.recName.text = pack[position].name
        if (collectionNo == -1) {
            try {
                val collectionName =
                    stringTools.shorten(
                        DB_Helper_Get(context)
                            .getSingleCollection(pack[position].collection)
                            .name
                    )
                viewHolder.binding.recDesc.visibility = View.VISIBLE
                viewHolder.binding.recDesc.text = collectionName
            } catch (e: Exception) {
                Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show()
            }
        }
        val currentPackId = pack[position].uid
        viewHolder.binding.recName.setOnClickListener {
            val updateHelper =
                DB_Helper_Update(context)
            val cardIds = arrayListOf<Int>()
            for (card in cards) {
                if (card.pack != currentPackId) {
                    cardIds.add(card.uid)
                    card.pack = currentPackId
                    updateHelper.updateCard(card)
                }
            }
            (ContextTools().getActivity(context) as CardActionsActivity).movedCards(cardIds)
            dialog.dismiss()
        }
    }

    override fun getItemCount(): Int {
        return pack.size
    }
}
