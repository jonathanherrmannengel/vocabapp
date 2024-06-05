package de.herrmann_engel.rbv.adapters

import android.app.Dialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.herrmann_engel.rbv.activities.EditCardMedia
import de.herrmann_engel.rbv.databinding.RecViewBinding
import de.herrmann_engel.rbv.db.DB_Card_With_Meta
import de.herrmann_engel.rbv.utils.StringTools


class AdapterMediaManageLinkedCards(
    private val cards: List<DB_Card_With_Meta>,
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
        cardText = StringTools().shorten(cardText)
        viewHolder.binding.recName.text = cardText
        val extra = cards[position].card.uid
        viewHolder.binding.recName.setOnClickListener {
            val intent = Intent(context, EditCardMedia::class.java)
            intent.putExtra("card", extra)
            context.startActivity(intent)
            dialog.dismiss()
        }
    }

    override fun getItemCount(): Int {
        return cards.size
    }
}
