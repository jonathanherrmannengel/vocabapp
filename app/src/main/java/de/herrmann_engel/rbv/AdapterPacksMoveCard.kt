package de.herrmann_engel.rbv

import android.app.Dialog
import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class AdapterPacksMoveCard(
        private val pack: List<DB_Pack>,
        private val collectionNo: Int,
        private val c: Context,
        private val card: DB_Card,
        private val dialog: Dialog
) : RecyclerView.Adapter<AdapterPacksMoveCard.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.rec_name)
        val textViewDesc: TextView = view.findViewById(R.id.rec_desc)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view =
                LayoutInflater.from(viewGroup.context).inflate(R.layout.rec_view, viewGroup, false)
        val settings = c.getSharedPreferences(Globals.SETTINGS_NAME, Context.MODE_PRIVATE)
        if (settings.getBoolean("ui_font_size", false)) {
            view.findViewById<TextView>(R.id.rec_name)
                    .setTextSize(
                            TypedValue.COMPLEX_UNIT_PX,
                            c.resources.getDimension(R.dimen.rec_view_font_size_big)
                    )
            view.findViewById<TextView>(R.id.rec_desc)
                    .setTextSize(
                            TypedValue.COMPLEX_UNIT_PX,
                            c.resources.getDimension(R.dimen.rec_view_font_size_below_big)
                    )
        }
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val colors = c.resources.obtainTypedArray(R.array.pack_color_list)
        val color = pack[position].colors
        if (color < colors.length() && color >= 0) {
            viewHolder.textView.setTextColor(colors.getColor(color, 0))
        }
        colors.recycle()
        viewHolder.textView.text = pack[position].name
        if (collectionNo == -1) {
            try {
                val collectionNameMaxLength = 50
                var collectionName =
                        DB_Helper_Get(c).getSingleCollection(pack[position].collection).name
                if (collectionName.length > collectionNameMaxLength) {
                    collectionName = collectionName.substring(0, collectionNameMaxLength - 1) + "…"
                }
                viewHolder.textViewDesc.visibility = View.VISIBLE
                viewHolder.textViewDesc.text = collectionName
            } catch (e: Exception) {
                Toast.makeText(c.applicationContext, R.string.error, Toast.LENGTH_SHORT).show()
            }
        }
        viewHolder.textView.setOnClickListener {
            val updateHelper = DB_Helper_Update(c)
            card.pack = pack[position].uid
            updateHelper.updateCard(card)
            (c as ViewCard).movedCard()
            dialog.dismiss()
        }
    }

    override fun getItemCount(): Int {
        return pack.size
    }
}
