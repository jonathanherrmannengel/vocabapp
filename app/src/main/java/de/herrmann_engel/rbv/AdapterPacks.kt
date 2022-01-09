package de.herrmann_engel.rbv

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import android.view.ViewGroup
import android.view.LayoutInflater
import android.content.Intent
import android.view.View

class AdapterPacks(
    private val pack: List<DB_Pack>,
    private val c: Context,
    private val collection: Int
) : RecyclerView.Adapter<AdapterPacks.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.rec_name)

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.rec_view, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        if (position == 0 && pack.isEmpty()) {
            viewHolder.textView.text = c.resources.getString(R.string.welcome_pack)
        } else if (position == 0) {
            viewHolder.textView.text = c.resources.getString(R.string.all_cards)
            viewHolder.textView.setOnClickListener {
                val intent = Intent(c.applicationContext, ListCards::class.java)
                intent.putExtra("pack", -1)
                intent.putExtra("collection", collection)
                c.startActivity(intent)
                (c as ListPacks).finish()
            }
        } else {
            val colors = c.resources.obtainTypedArray(R.array.pack_color_list)
            val color = pack[position - 1].colors
            if (color < colors.length() && color >= 0) {
                viewHolder.textView.setTextColor(colors.getColor(color, 0))
            }
            colors.recycle()
            val dbHelperGet = DB_Helper_Get(c.applicationContext)
            val size = dbHelperGet.getAllCardsByPack(pack[position - 1].uid).size
            viewHolder.textView.text = String.format("%s (%d)", pack[position - 1].name, size)
            val extra = pack[position - 1].uid
            viewHolder.textView.setOnClickListener {
                val intent = Intent(c.applicationContext, ListCards::class.java)
                intent.putExtra("pack", extra)
                intent.putExtra("collection", collection)
                c.startActivity(intent)
                (c as ListPacks).finish()
            }
        }
    }

    override fun getItemCount(): Int {
        return pack.size + 1
    }
}