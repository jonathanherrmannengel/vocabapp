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
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import de.herrmann_engel.rbv.Globals
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.activities.ViewCard
import de.herrmann_engel.rbv.adapters.compare.ListCardCompare
import de.herrmann_engel.rbv.databinding.RecViewBinding
import de.herrmann_engel.rbv.db.DB_Card_With_Meta
import de.herrmann_engel.rbv.utils.StringTools

class AdapterCards(
    private val cards: MutableList<DB_Card_With_Meta>,
    private var uiFontSizeBig: Boolean,
    private var reverse: Boolean,
    private val packNo: Int,
    private val collectionNo: Int
) : RecyclerView.Adapter<AdapterCards.ViewHolder>() {
    class ViewHolder(val binding: RecViewBinding) : RecyclerView.ViewHolder(binding.root)

    private val stringTools = StringTools()

    fun updateContent(
        cardsListNew: List<DB_Card_With_Meta>,
        reverse: Boolean,
    ) {
        val diffResult =
            DiffUtil.calculateDiff(
                ListCardCompare(
                    cards,
                    cardsListNew,
                    this.reverse != reverse
                )
            )
        this.reverse = reverse
        cards.clear()
        cards.addAll(cardsListNew)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RecViewBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        viewGroup.context.getSharedPreferences(Globals.SETTINGS_NAME, Context.MODE_PRIVATE)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val context = viewHolder.binding.root.context
        if (uiFontSizeBig) {
            viewHolder.binding.recName.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                context.resources.getDimension(R.dimen.rec_view_font_size_big)
            )
            viewHolder.binding.recDesc.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                context.resources.getDimension(R.dimen.rec_view_font_size_below_big)
            )
        }
        viewHolder.binding.recName.setTextColor(
            ContextCompat.getColor(
                context,
                R.color.default_text
            )
        )
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
            var cardText = if (reverse) {
                cards[position].formattedBack ?: cards[position].card.back
            } else cards[position].formattedFront ?: cards[position].card.front
            cardText = System.getProperty("line.separator")?.let { cardText.replace(it, " ") }
            cardText = stringTools.shorten(cardText)
            viewHolder.binding.recName.text =
                String.format("%s (%d)", cardText, cards[position].card.known)
            if (packNo < 0) {
                try {
                    val color = cards[position].packColor
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
            val extra = cards[position].card.uid
            viewHolder.binding.recName.setOnClickListener {
                val intent = Intent(context, ViewCard::class.java)
                intent.putExtra("collection", collectionNo)
                intent.putExtra("pack", packNo)
                intent.putExtra("card", extra)
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int {
        return 1.coerceAtLeast(cards.size)
    }
}
