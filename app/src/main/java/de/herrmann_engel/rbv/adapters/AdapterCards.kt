package de.herrmann_engel.rbv.adapters

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface.BOLD
import android.graphics.Typeface.NORMAL
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.Menu
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import de.herrmann_engel.rbv.Globals.MAX_SIZE_CARDS_CONTEXTUAL_MENU_PRINT
import de.herrmann_engel.rbv.Globals.MAX_SIZE_CARDS_CONTEXTUAL_MENU_SELECT
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.actions.CardActions
import de.herrmann_engel.rbv.activities.ViewCard
import de.herrmann_engel.rbv.adapters.compare.ListCardCompare
import de.herrmann_engel.rbv.cardlist.CardSource
import de.herrmann_engel.rbv.databinding.RecViewCardBinding
import de.herrmann_engel.rbv.db.DB_Card
import de.herrmann_engel.rbv.db.DB_Card_With_Meta
import de.herrmann_engel.rbv.utils.StringTools
import de.herrmann_engel.rbv.utils.colorAt
import de.herrmann_engel.rbv.utils.putCardId
import de.herrmann_engel.rbv.utils.putCardSource
import java.util.Locale

/**
 * Card list. Depending on the settings, the front, the back (reverse) or both sides are shown.
 * If the list is empty, a single welcome item is shown.
 *
 * Unless the list shows a single pack, cards are colored by their pack.
 */
class AdapterCards(
    private val cards: MutableList<DB_Card_With_Meta>,
    private var uiFontSizeBig: Boolean,
    private var reverse: Boolean,
    private var showBothSides: Boolean,
    private val cardSource: CardSource,
    private val cardActions: CardActions
) : RecyclerView.Adapter<AdapterCards.ViewHolder>() {
    class ViewHolder(val binding: RecViewCardBinding) : RecyclerView.ViewHolder(binding.root)

    // Selection of several cards (started by long click) to delete, move or print them
    private val selection = SelectionMode(
        R.menu.menu_list_cards_context,
        itemCount = { cards.size },
        redrawItem = { id ->
            notifyItemChanged(cards.indexOfFirst { it.card.uid == id }, SelectionMode.PAYLOAD)
        },
        redrawAll = { notifyItemRangeChanged(0, cards.size, SelectionMode.PAYLOAD) },
        prepareMenu = ::prepareSelectionMenu,
        onAction = ::onSelectionAction
    )

    // Moving is only possible inside a pack; printing and "select all" are limited to a maximum
    // number of cards
    private fun prepareSelectionMenu(menu: Menu, selectedIds: Set<Int>) {
        menu.findItem(R.id.menu_list_cards_context_delete).isVisible = selectedIds.isNotEmpty()
        menu.findItem(R.id.menu_list_cards_context_move).isVisible =
            selectedIds.isNotEmpty() && cardSource is CardSource.Pack
        menu.findItem(R.id.menu_list_cards_context_print).isVisible =
            selectedIds.isNotEmpty() && selectedIds.size <= MAX_SIZE_CARDS_CONTEXTUAL_MENU_PRINT
        menu.findItem(R.id.menu_list_cards_context_select_all).isVisible =
            selectedIds.size < cards.size && cards.size <= MAX_SIZE_CARDS_CONTEXTUAL_MENU_SELECT
    }

    private fun onSelectionAction(itemId: Int, selectedIds: List<Int>): Boolean {
        when (itemId) {
            R.id.menu_list_cards_context_delete -> cardActions.delete(selectedCards(selectedIds))

            R.id.menu_list_cards_context_move -> if (cardSource is CardSource.Pack) {
                cardActions.move(selectedCards(selectedIds), cardSource.listCollectionId)
            }

            R.id.menu_list_cards_context_print -> cardActions.print(selectedCards(selectedIds))

            R.id.menu_list_cards_context_select_all -> {
                selection.selectAll(cards.map { it.card.uid })
                return true
            }

            else -> return false
        }
        selection.finish()
        return true
    }

    // In the order of the ids
    private fun selectedCards(ids: List<Int>): ArrayList<DB_Card> {
        val cardsById = cards.associateBy { it.card.uid }
        return ArrayList(ids.mapNotNull { cardsById[it]?.card })
    }

    // Replaces the list content, using DiffUtil to only update changed items; all items are
    // redrawn if the displayed side(s) or the font size setting changed
    fun updateContent(
        cardsListNew: List<DB_Card_With_Meta>,
        reverse: Boolean,
        showBothSides: Boolean,
        uiFontSizeBig: Boolean
    ) {
        val diffResult =
            DiffUtil.calculateDiff(
                ListCardCompare(
                    cards,
                    cardsListNew,
                    this.reverse != reverse || this.showBothSides != showBothSides || this.uiFontSizeBig != uiFontSizeBig
                )
            )
        this.reverse = reverse
        this.showBothSides = showBothSides
        this.uiFontSizeBig = uiFontSizeBig
        cards.clear()
        cards.addAll(cardsListNew)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RecViewCardBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val context = viewHolder.binding.root.context
        if (uiFontSizeBig) {
            viewHolder.binding.recFront.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                context.resources.getDimension(R.dimen.rec_view_font_size_big)
            )
            viewHolder.binding.recBack.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                context.resources.getDimension(R.dimen.rec_view_font_size_big)
            )
        } else {
            viewHolder.binding.recFront.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                context.resources.getDimension(R.dimen.rec_view_font_size_default)
            )
            viewHolder.binding.recBack.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                context.resources.getDimension(R.dimen.rec_view_font_size_default)
            )
        }
        viewHolder.binding.recFront.setTextColor(
            ContextCompat.getColor(
                context,
                R.color.default_text
            )
        )
        viewHolder.binding.recBack.setTextColor(
            ContextCompat.getColor(
                context,
                R.color.default_text
            )
        )
        viewHolder.binding.root.weightSum = 1f
        viewHolder.binding.recFront.visibility = VISIBLE
        viewHolder.binding.recBack.visibility = GONE
        viewHolder.binding.recMargin.visibility = GONE
        // Welcome message (with an icon replacing the "+" if cards can be created here)
        if (cards.isEmpty()) {
            if (cardSource is CardSource.Pack) {
                val text =
                    String.format(
                        Locale.ROOT,
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
                viewHolder.binding.recFront.text = addText
            } else {
                viewHolder.binding.recFront.text =
                    context.resources.getString(R.string.welcome_card)
            }
        } else {
            if (showBothSides) {
                viewHolder.binding.root.weightSum = 2.1f
                viewHolder.binding.recBack.visibility = VISIBLE
                viewHolder.binding.recMargin.visibility = VISIBLE
            } else if (reverse) {
                viewHolder.binding.recFront.visibility = GONE
                viewHolder.binding.recBack.visibility = VISIBLE
            }
            prepareCardSide(false, cards[position], viewHolder)
            prepareCardSide(true, cards[position], viewHolder)
        }
        // The welcome item of an empty list cannot be selected
        if (cards.isNotEmpty() && selection.isSelected(cards[position].card.uid)) {
            viewHolder.binding.recFront.contentDescription = String.format(
                Locale.ROOT,
                "%s: %s",
                context.resources.getString(R.string.selected),
                viewHolder.binding.recFront.contentDescription
            )
            viewHolder.binding.recFront.setTypeface(null, BOLD)
            viewHolder.binding.recBack.contentDescription = String.format(
                Locale.ROOT,
                "%s: %s",
                context.resources.getString(R.string.selected),
                viewHolder.binding.recBack.contentDescription
            )
            viewHolder.binding.recBack.setTypeface(null, BOLD)
            viewHolder.binding.root.setBackgroundColor(
                ContextCompat.getColor(
                    viewHolder.binding.root.context,
                    R.color.background_select
                )
            )
        } else {
            viewHolder.binding.recFront.setTypeface(null, NORMAL)
            viewHolder.binding.recBack.setTypeface(null, NORMAL)
            viewHolder.binding.root.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    override fun getItemCount(): Int {
        return 1.coerceAtLeast(cards.size)
    }

    /**
     * Fills the text view of one side of a card (shortened, single line) and sets the click
     * listeners. If only one side is shown, the progress is appended.
     */
    private fun prepareCardSide(back: Boolean, card: DB_Card_With_Meta, viewHolder: ViewHolder) {
        val context = viewHolder.binding.root.context
        val recText = if (back) {
            viewHolder.binding.recBack
        } else viewHolder.binding.recFront
        var cardText = if (back) {
            card.formattedBack ?: card.card.back
        } else card.formattedFront ?: card.card.front
        cardText = cardText.replace(System.lineSeparator(), " ")
        cardText = StringTools.shorten(cardText)
        if (showBothSides) {
            recText.text = cardText
            recText.contentDescription = cardText
        } else {
            recText.text =
                String.format(Locale.ROOT, "%s (%d)", cardText, card.card.known)
            recText.contentDescription =
                String.format(
                    Locale.ROOT,
                    "%s (%s: %d)",
                    cardText,
                    context.resources.getString(R.string.card_known),
                    card.card.known
                )
        }
        if (cardSource !is CardSource.Pack) {
            context.resources.colorAt(R.array.pack_color_list, card.packColor)
                ?.let { recText.setTextColor(it) }
        }
        val cardId = card.card.uid
        recText.setOnClickListener {
            // In selection mode a click toggles the selection, otherwise it opens the card
            if (selection.isActive) {
                selection.toggle(cardId)
            } else {
                val intent = Intent(context, ViewCard::class.java)
                intent.putCardSource(cardSource)
                intent.putCardId(cardId)
                context.startActivity(intent)
            }
        }
        recText.setOnLongClickListener { selection.start(context, cardId) }
    }
}
