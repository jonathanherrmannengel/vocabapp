package de.herrmann_engel.rbv.adapters

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
import de.herrmann_engel.rbv.Globals
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.activities.ListCards
import de.herrmann_engel.rbv.activities.ViewCard
import de.herrmann_engel.rbv.db.DB_Card
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get
import de.herrmann_engel.rbv.utils.StringTools

class AdapterCards(
    private val cards: List<DB_Card>,
    private val c: Context,
    private val reverse: Boolean,
    private val sort: Int,
    private val packNo: Int,
    private val packNos: ArrayList<Int>?,
    private val searchQuery: String?,
    private val collectionNo: Int,
    private val progressGreater: Boolean?,
    private val progressNumber: Int?,
    private val savedList: ArrayList<Int>?,
    private val savedListSeed: Long?
) : RecyclerView.Adapter<AdapterCards.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.rec_name)
    }

    private val stringTools = StringTools()

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
        if (cards.isEmpty()) {
            if (packNo < 0) {
                viewHolder.textView.text = c.resources.getString(R.string.welcome_card)
            } else {
                val text =
                    String.format(
                        "%s %s",
                        c.resources.getString(R.string.welcome_card),
                        c.resources.getString(R.string.welcome_card_create)
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
        } else {
            var cardText = if (reverse) cards[position].back else cards[position].front
            cardText = cardText.replace(System.getProperty("line.separator"), " ")
            cardText = stringTools.shorten(cardText)
            viewHolder.textView.text = String.format("%s (%d)", cardText, cards[position].known)
            if (packNo < 0) {
                val dbHelperGet =
                    DB_Helper_Get(c.applicationContext)
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
                intent.putIntegerArrayListExtra("packs", packNos)
                intent.putExtra("card", extra)
                intent.putExtra("reverse", reverse)
                intent.putExtra("sort", sort)
                intent.putExtra("searchQuery", searchQuery)
                intent.putExtra("cardPosition", viewHolder.bindingAdapterPosition)
                intent.putExtra("progressGreater", progressGreater)
                intent.putExtra("progressNumber", progressNumber)
                intent.putIntegerArrayListExtra("savedList", savedList)
                intent.putExtra("savedListSeed", savedListSeed)
                c.startActivity(intent)
                (c as ListCards).finish()
            }
        }
    }

    override fun getItemCount(): Int {
        return 1.coerceAtLeast(cards.size)
    }
}
