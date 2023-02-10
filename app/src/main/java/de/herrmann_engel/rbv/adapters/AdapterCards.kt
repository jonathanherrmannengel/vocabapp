package de.herrmann_engel.rbv.adapters

import android.content.Context
import android.content.Intent
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import de.herrmann_engel.rbv.Globals
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.activities.ListCards
import de.herrmann_engel.rbv.activities.ViewCard
import de.herrmann_engel.rbv.databinding.RecViewBinding
import de.herrmann_engel.rbv.db.DB_Card
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get
import de.herrmann_engel.rbv.utils.ContextTools
import de.herrmann_engel.rbv.utils.StringTools

class AdapterCards(
    private val cards: List<DB_Card>,
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
    class ViewHolder(val binding: RecViewBinding) : RecyclerView.ViewHolder(binding.root)

    private val stringTools = StringTools()

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RecViewBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        val settings =
            viewGroup.context.getSharedPreferences(Globals.SETTINGS_NAME, Context.MODE_PRIVATE)
        if (settings.getBoolean("ui_font_size", false)) {
            binding.recName.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                viewGroup.context.resources.getDimension(R.dimen.rec_view_font_size_big)
            )
            binding.recDesc.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                viewGroup.context.resources.getDimension(R.dimen.rec_view_font_size_below_big)
            )
        }
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val context = viewHolder.binding.root.context
        if (cards.isEmpty()) {
            if (packNo < 0) {
                viewHolder.binding.recName.text =
                    context.resources.getString(R.string.welcome_card)
            } else {
                val text =
                    String.format(
                        "%s %s",
                        context.resources.getString(R.string.welcome_card),
                        context.resources.getString(R.string.welcome_card_create)
                    )
                val addText = SpannableString(text)
                val addTextDrawable = ContextCompat.getDrawable(
                    context,
                    R.drawable.outline_add_24
                )
                addTextDrawable?.setTint(context.getColor(R.color.light_black))
                addTextDrawable?.setBounds(
                    0,
                    0,
                    addTextDrawable.intrinsicWidth,
                    addTextDrawable.intrinsicHeight
                )
                val addTextImage = addTextDrawable?.let { ImageSpan(it, ImageSpan.ALIGN_BOTTOM) }
                val index = addText.indexOf("+")
                addText.setSpan(addTextImage, index, index + 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
                viewHolder.binding.recName.text = addText
            }
        } else {
            var cardText = if (reverse) cards[position].back else cards[position].front
            cardText = System.getProperty("line.separator")?.let { cardText.replace(it, " ") }
            cardText = stringTools.shorten(cardText)
            viewHolder.binding.recName.text =
                String.format("%s (%d)", cardText, cards[position].known)
            if (packNo < 0) {
                val dbHelperGet =
                    DB_Helper_Get(context)
                try {
                    val color = dbHelperGet.getSinglePack(cards[position].pack).colors
                    val colors =
                        context.resources.obtainTypedArray(R.array.pack_color_list)
                    if (color < colors.length() && color >= 0) {
                        viewHolder.binding.recName.setTextColor(colors.getColor(color, 0))
                    }
                    colors.recycle()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            val extra = cards[position].uid
            viewHolder.binding.recName.setOnClickListener {
                val intent = Intent(context, ViewCard::class.java)
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
                context.startActivity(intent)
                (ContextTools().getActivity(context) as ListCards).finish()
            }
        }
    }

    override fun getItemCount(): Int {
        return 1.coerceAtLeast(cards.size)
    }
}
