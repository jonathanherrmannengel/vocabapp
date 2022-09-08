package de.herrmann_engel.rbv

import android.content.Context
import android.content.Intent
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class AdapterPacks(
        private val pack: List<DB_Pack>,
        private val c: Context,
        private val collection: Int
) : RecyclerView.Adapter<AdapterPacks.ViewHolder>() {
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
        if (position == 0 && pack.isEmpty()) {
            if (collection == -1) {
                viewHolder.textView.text = c.resources.getString(R.string.welcome_pack)
            } else {
                val text =
                        String.format(
                                "%s %s",
                                c.resources.getString(R.string.welcome_pack),
                                c.resources.getString(R.string.welcome_pack_create)
                        )
                val addText = SpannableString(text)
                val addTextDrawable = ContextCompat.getDrawable(c, R.drawable.outline_add_24)
                addTextDrawable?.setTint(c.getColor(R.color.light_black))
                addTextDrawable?.setBounds(
                        0,
                        0,
                        addTextDrawable.intrinsicWidth,
                        addTextDrawable.intrinsicHeight
                )
                val addTextImage = addTextDrawable?.let { ImageSpan(it, ImageSpan.ALIGN_BOTTOM) }
                val index = addText.indexOf("+")
                addText.setSpan(addTextImage, index, index + 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
                viewHolder.textView.text = addText
            }
            viewHolder.textViewDesc.visibility = View.GONE
        } else if (position == 0) {
            viewHolder.textView.text = c.resources.getString(R.string.all_cards)
            viewHolder.textView.setOnClickListener {
                val intent = Intent(c.applicationContext, ListCards::class.java)
                intent.putExtra("pack", -1)
                intent.putExtra("collection", collection)
                c.startActivity(intent)
                (c as ListPacks).finish()
            }
            viewHolder.textViewDesc.visibility = View.GONE
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
            if (collection == -1) {
                try {
                    val collectionNameMaxLength = 50
                    var collectionName =
                            DB_Helper_Get(c).getSingleCollection(pack[position - 1].collection).name
                    if (collectionName.length > collectionNameMaxLength) {
                        collectionName =
                                collectionName.substring(0, collectionNameMaxLength - 1) + "…"
                    }
                    viewHolder.textViewDesc.visibility = View.VISIBLE
                    viewHolder.textViewDesc.text = collectionName
                } catch (e: Exception) {
                    Toast.makeText(c.applicationContext, R.string.error, Toast.LENGTH_SHORT).show()
                }
            }
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
