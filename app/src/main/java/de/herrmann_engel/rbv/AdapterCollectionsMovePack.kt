package de.herrmann_engel.rbv

import android.app.Dialog
import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdapterCollectionsMovePack(
    private val collection: List<DB_Collection>,
    private val pack: DB_Pack,
    private val c: Context,
    private val dialog: Dialog
) : RecyclerView.Adapter<AdapterCollectionsMovePack.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.rec_name)
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
        viewHolder.textView.text = collection[position].name
        viewHolder.textView.setOnClickListener {
            val updateHelper = DB_Helper_Update(c)
            pack.collection = collection[position].uid
            updateHelper.updatePack(pack)
            (c as ViewPack).movedPack()
            dialog.dismiss()
        }
    }

    override fun getItemCount(): Int {
        return collection.size
    }
}
