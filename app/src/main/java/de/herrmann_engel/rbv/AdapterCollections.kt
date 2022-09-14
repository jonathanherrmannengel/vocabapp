package de.herrmann_engel.rbv

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class AdapterCollections(private val collection: List<DB_Collection>, private val c: Context) :
        RecyclerView.Adapter<AdapterCollections.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val layout: LinearLayout = view.findViewById(R.id.rec_collections_preview_layout)
        val textView: TextView = view.findViewById(R.id.rec_collections_name)
        val descView: TextView = view.findViewById(R.id.rec_collections_desc)
        val previewView: TextView = view.findViewById(R.id.rec_collections_preview_text)
        val numberText: TextView = view.findViewById(R.id.rec_collections_number_text)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view =
                LayoutInflater.from(viewGroup.context)
                        .inflate(R.layout.rec_view_collection_or_pack, viewGroup, false)
        val settings = c.getSharedPreferences(Globals.SETTINGS_NAME, Context.MODE_PRIVATE)
        if (settings.getBoolean("ui_font_size", false)) {
            view.findViewById<TextView>(R.id.rec_collections_name)
                    .setTextSize(
                            TypedValue.COMPLEX_UNIT_PX,
                            c.resources.getDimension(R.dimen.rec_view_font_size_big)
                    )
            view.findViewById<TextView>(R.id.rec_collections_desc)
                    .setTextSize(
                            TypedValue.COMPLEX_UNIT_PX,
                            c.resources.getDimension(R.dimen.rec_view_font_size_below_big)
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
            viewHolder.textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
            viewHolder.descView.visibility = View.GONE
            viewHolder.previewView.visibility = View.GONE
            viewHolder.numberText.visibility = View.GONE
        } else if (position == 0) {
            viewHolder.layout.setOnClickListener {
                val intent = Intent(c.applicationContext, ListPacks::class.java)
                intent.putExtra("collection", -1)
                c.startActivity(intent)
                (c as ListCollections).finish()
            }
            viewHolder.textView.text = c.resources.getString(R.string.all_packs)
            viewHolder.descView.visibility = View.VISIBLE
            viewHolder.descView.text = c.resources.getString(R.string.all_packs_desc)
            viewHolder.previewView.text = "…"
            val dbHelperGet = DB_Helper_Get(c.applicationContext)
            val size = dbHelperGet.allPacks.size
            viewHolder.numberText.text = size.toString()
            viewHolder.previewView.setBackgroundColor(Color.rgb(185, 185, 185))
            val background = viewHolder.layout.background as GradientDrawable
            background.mutate()
            background.setStroke(2, Color.rgb(85, 85, 85))
            background.setColor(Color.argb(75, 185, 185, 185))
        } else {
            val extra = collection[position - 1].uid
            viewHolder.layout.setOnClickListener {
                val intent = Intent(c.applicationContext, ListPacks::class.java)
                intent.putExtra("collection", extra)
                c.startActivity(intent)
                (c as ListCollections).finish()
            }
            viewHolder.textView.text = StringTools().shorten(collection[position - 1].name)
            if (collection[position - 1].desc.isEmpty()) {
                viewHolder.descView.visibility = View.GONE
            } else {
                viewHolder.descView.visibility = View.VISIBLE
                viewHolder.descView.text = StringTools().shorten(collection[position - 1].desc)
            }
            val emojiText = collection[position - 1].emoji
            viewHolder.previewView.text =
                    if (emojiText.isNullOrEmpty()) {
                        val pattern = Regex("^(\\P{M}\\p{M}*+).*")
                        collection[position - 1].name.replace(pattern, "$1")
                    } else {
                        emojiText
                    }
            val dbHelperGet = DB_Helper_Get(c.applicationContext)
            val size = dbHelperGet.getAllPacksByCollection(collection[position - 1].uid).size
            viewHolder.numberText.text = size.toString()

            val color = collection[position - 1].colors
            val colors = c.resources.obtainTypedArray(R.array.pack_color_list)
            val colorsBackground = c.resources.obtainTypedArray(R.array.pack_color_background_light)
            val colorsBackgroundAlpha =
                    c.resources.obtainTypedArray(R.array.pack_color_background_light_alpha)
            if (color < colors.length() &&
                            color < colorsBackground.length() &&
                            color < colorsBackgroundAlpha.length() &&
                            color >= 0
            ) {
                viewHolder.textView.setTextColor(colors.getColor(color, 0))
                viewHolder.previewView.setTextColor(colors.getColor(color, 0))
                viewHolder.previewView.setBackgroundColor(colorsBackground.getColor(color, 0))
                val background = viewHolder.layout.background as GradientDrawable
                background.mutate()
                background.setStroke(2, colors.getColor(color, 0))
                background.setColor(colorsBackgroundAlpha.getColor(color, 0))
            }
            colors.recycle()
            colorsBackground.recycle()
            colorsBackgroundAlpha.recycle()
        }
    }

    override fun getItemCount(): Int {
        return collection.size + 1
    }
}
