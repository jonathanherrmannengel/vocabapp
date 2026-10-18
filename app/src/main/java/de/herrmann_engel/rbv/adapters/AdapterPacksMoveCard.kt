package de.herrmann_engel.rbv.adapters

import android.app.Dialog
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.databinding.RecViewBinding
import de.herrmann_engel.rbv.db.AppDatabase
import de.herrmann_engel.rbv.db.DB_Card
import de.herrmann_engel.rbv.db.DB_Pack_With_Meta
import de.herrmann_engel.rbv.db.utils.DB_Helper_Update
import de.herrmann_engel.rbv.settings.AppSettings
import de.herrmann_engel.rbv.utils.StringTools
import de.herrmann_engel.rbv.utils.colorAt

/**
 * Pack list in the "move card" dialog. Clicking a pack moves the cards there and reports the
 * ids of the moved cards to onMoved.
 */
class AdapterPacksMoveCard(
    private val packs: List<DB_Pack_With_Meta>,
    // null: packs of all collections are listed
    private val collectionId: Int?,
    private val cards: List<DB_Card>,
    private val dialog: Dialog,
    private val onMoved: (ArrayList<Int>) -> Unit
) : RecyclerView.Adapter<AdapterPacksMoveCard.ViewHolder>() {
    class ViewHolder(val binding: RecViewBinding) : RecyclerView.ViewHolder(binding.root)

    private val stringTools = StringTools()

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
        val pack = packs[position].pack!!
        context.resources.colorAt(R.array.pack_color_list, pack.colors)
            ?.let { viewHolder.binding.recName.setTextColor(it) }
        viewHolder.binding.recName.text = pack.name
        // Packs of all collections are listed: show the collection name as well
        if (collectionId == null) {
            val collectionName = stringTools.shorten(packs[position].collectionName ?: "")
            viewHolder.binding.recDesc.visibility = View.VISIBLE
            viewHolder.binding.recDesc.text = collectionName
        }
        val currentPackId = pack.uid
        viewHolder.binding.recName.setOnClickListener {
            val updateHelper =
                DB_Helper_Update(context)
            val cardIds = arrayListOf<Int>()
            // Only cards not already in the target pack are moved and reported
            // All cards are moved or none
            AppDatabase.getInstance(context).runInTransaction {
                for (card in cards) {
                    if (card.pack != currentPackId) {
                        cardIds.add(card.uid)
                        card.pack = currentPackId
                        updateHelper.updateCard(card)
                    }
                }
            }
            onMoved(cardIds)
            dialog.dismiss()
        }
    }

    override fun getItemCount(): Int {
        return packs.size
    }
}
