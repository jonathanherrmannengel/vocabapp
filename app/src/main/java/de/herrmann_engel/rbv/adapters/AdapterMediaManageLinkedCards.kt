package de.herrmann_engel.rbv.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.activities.EditCardMedia
import de.herrmann_engel.rbv.activities.ManageMedia
import de.herrmann_engel.rbv.db.DB_Card
import de.herrmann_engel.rbv.utils.StringTools


class AdapterMediaManageLinkedCards(
    private val cards: List<DB_Card>,
    private val c: Context
) : RecyclerView.Adapter<AdapterMediaManageLinkedCards.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.rec_name)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.rec_view, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        var cardText = cards[position].front.replace(System.getProperty("line.separator"), " ")
        cardText = StringTools().shorten(cardText)
        viewHolder.textView.text = cardText
        viewHolder.textView.setOnClickListener {
            val intent = Intent(c.applicationContext, EditCardMedia::class.java)
            intent.putExtra("fromMediaManager", true)
            intent.putExtra("card", cards[position].uid)
            c.startActivity(intent)
            (c as ManageMedia).finish()
        }
    }

    override fun getItemCount(): Int {
        return cards.size
    }
}
