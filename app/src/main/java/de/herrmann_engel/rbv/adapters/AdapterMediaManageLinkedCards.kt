package de.herrmann_engel.rbv.adapters

import android.app.Dialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.herrmann_engel.rbv.activities.EditCardMedia
import de.herrmann_engel.rbv.databinding.RecViewBinding
import de.herrmann_engel.rbv.db.CardWithMeta
import de.herrmann_engel.rbv.utils.StringTools
import de.herrmann_engel.rbv.utils.putCardId


/**
 * Lists the cards a media file is linked to (in the media management dialog).
 * Clicking a card opens its media settings.
 */
class AdapterMediaManageLinkedCards(
    private val cards: List<CardWithMeta>,
    private val dialog: Dialog
) : RecyclerView.Adapter<AdapterMediaManageLinkedCards.ViewHolder>() {
    class ViewHolder(val binding: RecViewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RecViewBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val context = viewHolder.binding.root.context
        var cardText = cards[position].formattedFront ?: cards[position].card.front
        cardText = cardText.replace(System.lineSeparator(), " ")
        cardText = StringTools.shorten(cardText)
        viewHolder.binding.recName.text = cardText
        val cardId = cards[position].card.uid
        viewHolder.binding.recName.setOnClickListener {
            val intent = Intent(context, EditCardMedia::class.java)
            intent.putCardId(cardId)
            context.startActivity(intent)
            dialog.dismiss()
        }
    }

    override fun getItemCount(): Int {
        return cards.size
    }
}
