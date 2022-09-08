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
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class AdapterCollections(private val collection: List<DB_Collection>, private val c: Context) :
        RecyclerView.Adapter<AdapterCollections.ViewHolder>() {
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
                            c.resources.getDimension(R.dimen.rec_view_font_size_big)
                    )
        }
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        if (position == 0 && collection.isEmpty()) {
            val welcomeText = SpannableString(c.resources.getString(R.string.welcome_collection))
            val welcomeTextDrawableAdd = ContextCompat.getDrawable(c, R.drawable.outline_add_24)
            welcomeTextDrawableAdd?.setTint(c.getColor(R.color.light_black))
            welcomeTextDrawableAdd?.setBounds(
                    0,
                    0,
                    welcomeTextDrawableAdd.intrinsicWidth,
                    welcomeTextDrawableAdd.intrinsicHeight
            )
            val welcomeTextImageAdd =
                    welcomeTextDrawableAdd?.let { ImageSpan(it, ImageSpan.ALIGN_BOTTOM) }
            val indexAdd = welcomeText.indexOf("+")
            welcomeText.setSpan(
                    welcomeTextImageAdd,
                    indexAdd,
                    indexAdd + 1,
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )
            val welcomeTextDrawableImport =
                    ContextCompat.getDrawable(c, R.drawable.outline_file_download_24)
            welcomeTextDrawableImport?.setTint(c.getColor(R.color.light_black))
            welcomeTextDrawableImport?.setBounds(
                    0,
                    0,
                    welcomeTextDrawableImport.intrinsicWidth,
                    welcomeTextDrawableImport.intrinsicHeight
            )
            val welcomeTextImageImport =
                    welcomeTextDrawableImport?.let { ImageSpan(it, ImageSpan.ALIGN_BOTTOM) }
            val indexImport = welcomeText.indexOf("↓")
            welcomeText.setSpan(
                    welcomeTextImageImport,
                    indexImport,
                    indexImport + 1,
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )
            viewHolder.textView.text = welcomeText
        } else if (position == 0) {
            viewHolder.textView.text = c.resources.getString(R.string.all_packs)
            viewHolder.textView.setOnClickListener {
                val intent = Intent(c.applicationContext, ListPacks::class.java)
                intent.putExtra("collection", -1)
                c.startActivity(intent)
                (c as ListCollections).finish()
            }
        } else {
            viewHolder.textView.text = collection[position - 1].name
            val extra = collection[position - 1].uid
            viewHolder.textView.setOnClickListener {
                val intent = Intent(c.applicationContext, ListPacks::class.java)
                intent.putExtra("collection", extra)
                c.startActivity(intent)
                (c as ListCollections).finish()
            }
        }
    }

    override fun getItemCount(): Int {
        return collection.size + 1
    }
}
