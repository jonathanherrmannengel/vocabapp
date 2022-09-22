package de.herrmann_engel.rbv.adapters

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import de.herrmann_engel.rbv.Globals
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.activities.ListCards
import de.herrmann_engel.rbv.activities.ListPacks
import de.herrmann_engel.rbv.db.DB_Pack
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get
import de.herrmann_engel.rbv.utils.StringTools

class AdapterPacks(
    private val pack: List<DB_Pack>,
    private val c: Context,
    private val collection: Int
) : RecyclerView.Adapter<AdapterPacks.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val layout: LinearLayout = view.findViewById(R.id.rec_collections_preview_layout)
        val collectionName: TextView = view.findViewById(R.id.rec_collections_parent)
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
            view.findViewById<TextView>(R.id.rec_collections_parent)
                .setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    c.resources.getDimension(R.dimen.rec_view_font_size_above_big)
                )
        }
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val backgroundLayerList = viewHolder.layout.background as LayerDrawable
        val background =
            backgroundLayerList.findDrawableByLayerId(R.id.rec_view_collection_or_pack_background_main) as GradientDrawable
        val backgroundBehind =
            backgroundLayerList.findDrawableByLayerId(R.id.rec_view_collection_or_pack_background_behind) as GradientDrawable
        if (position == 0 && pack.isEmpty()) {
            viewHolder.layout.background = null;
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
            viewHolder.collectionName.visibility = View.GONE
            viewHolder.textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
            viewHolder.descView.visibility = View.GONE
            viewHolder.previewView.visibility = View.GONE
            viewHolder.numberText.visibility = View.GONE
        } else if (position == 0) {
            viewHolder.layout.setOnClickListener {
                val intent = Intent(c.applicationContext, ListCards::class.java)
                intent.putExtra("pack", -1)
                intent.putExtra("collection", collection)
                c.startActivity(intent)
                (c as ListPacks).finish()
            }
            viewHolder.textView.text = c.resources.getString(R.string.all_cards)
            viewHolder.descView.visibility = View.VISIBLE
            viewHolder.descView.text =
                if (collection == -1) {
                    c.resources.getString(R.string.all_cards_desc)
                } else {
                    c.resources.getString(R.string.all_cards_desc_by_pack)
                }
            viewHolder.previewView.text = "…"
            val dbHelperGet =
                DB_Helper_Get(c.applicationContext)
            val size =
                if (collection == -1) {
                    dbHelperGet.allCards.size
                } else {
                    dbHelperGet.getAllCardsByCollection(collection).size
                }
            viewHolder.numberText.text = size.toString()
            viewHolder.collectionName.visibility = View.GONE
            viewHolder.textView.setTextColor(Color.rgb(0, 0, 0))
            viewHolder.previewView.setTextColor(Color.rgb(0, 0, 0))
            viewHolder.previewView.setBackgroundColor(Color.rgb(185, 185, 185))
            background.mutate()
            background.setStroke(2, Color.rgb(85, 85, 85))
            background.setColor(Color.argb(75, 185, 185, 185))
            backgroundBehind.mutate()
            backgroundBehind.setStroke(1, Color.rgb(185, 185, 185))
        } else {
            val extra = pack[position - 1].uid
            viewHolder.layout.setOnClickListener {
                val intent = Intent(c.applicationContext, ListCards::class.java)
                intent.putExtra("pack", extra)
                intent.putExtra("collection", collection)
                c.startActivity(intent)
                (c as ListPacks).finish()
            }
            viewHolder.textView.text = StringTools().shorten(pack[position - 1].name)
            if (pack[position - 1].desc.isEmpty()) {
                viewHolder.descView.visibility = View.GONE
            } else {
                viewHolder.descView.visibility = View.VISIBLE
                viewHolder.descView.text = StringTools()
                    .shorten(pack[position - 1].desc)
            }
            val emojiText = pack[position - 1].emoji
            viewHolder.previewView.text =
                if (emojiText.isNullOrEmpty()) {
                    val pattern = Regex("^(\\P{M}\\p{M}*+).*")
                    pack[position - 1].name.replace(pattern, "$1")
                } else {
                    emojiText
                }
            val dbHelperGet =
                DB_Helper_Get(c.applicationContext)
            val size = dbHelperGet.getAllCardsByPack(pack[position - 1].uid).size
            viewHolder.numberText.text = size.toString()
            if (collection == -1) {
                try {
                    val collectionName =
                        StringTools()
                            .shorten(
                                DB_Helper_Get(c)
                                    .getSingleCollection(
                                        pack[position - 1].collection
                                    )
                                    .name
                            )
                    viewHolder.collectionName.visibility = View.VISIBLE
                    viewHolder.collectionName.text = collectionName
                } catch (e: Exception) {
                    Toast.makeText(c.applicationContext, R.string.error, Toast.LENGTH_SHORT).show()
                }
            }
            val color = pack[position - 1].colors
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
                background.mutate()
                background.setStroke(2, colors.getColor(color, 0))
                background.setColor(colorsBackgroundAlpha.getColor(color, 0))
                backgroundBehind.mutate()
                backgroundBehind.setStroke(1, colorsBackground.getColor(color, 0))
            }
            colors.recycle()
            colorsBackground.recycle()
            colorsBackgroundAlpha.recycle()
        }
    }

    override fun getItemCount(): Int {
        return pack.size + 1
    }
}
