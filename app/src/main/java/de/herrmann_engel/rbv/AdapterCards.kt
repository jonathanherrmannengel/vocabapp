package de.herrmann_engel.rbv

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import android.view.ViewGroup
import android.view.LayoutInflater
import android.content.Intent
import android.view.View
import java.lang.Exception

class AdapterCards(
    private val cards: List<DB_Card>,
    private val c: Context,
    private val reverse: Boolean,
    private val sort: Int,
    private val packNo: Int,
    private val collectionNo: Int
) : RecyclerView.Adapter<AdapterCards.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.rec_name)

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.rec_view, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        if (cards.isEmpty()) {
            viewHolder.textView.text = c.resources.getString(R.string.welcome_card)
        } else {
            viewHolder.textView.text = String.format(
                "%s (%d)",
                if (reverse) cards[position].back else cards[position].front,
                cards[position].known
            )
            if (packNo == -1) {
                val dbHelperGet = DB_Helper_Get(c.applicationContext)
                try {
                    val color = dbHelperGet.getSinglePack(cards[position].pack).colors
                    val colors = c.resources.obtainTypedArray(R.array.pack_color_list)
                    if (color < colors.length() && color >= 0) {
                        viewHolder.textView.setTextColor(colors.getColor(color, 0))
                    }
                    colors.recycle()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            val extra = cards[position].uid
            viewHolder.textView.setOnClickListener {
                val intent = Intent(c.applicationContext, ViewCard::class.java)
                intent.putExtra("collection", collectionNo)
                intent.putExtra("pack", packNo)
                intent.putExtra("card", extra)
                intent.putExtra("reverse", reverse)
                intent.putExtra("sort", sort)
                c.startActivity(intent)
                (c as ListCards).finish()
            }
        }
    }

    override fun getItemCount(): Int {
        return 1.coerceAtLeast(cards.size)
    }
}