package de.herrmann_engel.rbv.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface.BOLD
import android.graphics.Typeface.NORMAL
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.util.TypedValue
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import de.herrmann_engel.rbv.Globals
import de.herrmann_engel.rbv.Globals.MAX_SIZE_CARDS_CONTEXTUAL_MENU_PRINT
import de.herrmann_engel.rbv.Globals.MAX_SIZE_CARDS_CONTEXTUAL_MENU_SELECT
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.actions.CardActions
import de.herrmann_engel.rbv.activities.ViewCard
import de.herrmann_engel.rbv.adapters.compare.ListCardCompare
import de.herrmann_engel.rbv.databinding.RecViewCardBinding
import de.herrmann_engel.rbv.db.DB_Card
import de.herrmann_engel.rbv.db.DB_Card_With_Meta
import de.herrmann_engel.rbv.utils.ContextTools
import de.herrmann_engel.rbv.utils.StringTools
import java.util.Locale

class AdapterCards(
    private val cards: MutableList<DB_Card_With_Meta>,
    private val uiFontSizeBig: Boolean,
    private var reverse: Boolean,
    private var showBothSides: Boolean,
    private val packNo: Int,
    private val collectionNo: Int
) : RecyclerView.Adapter<AdapterCards.ViewHolder>() {
    class ViewHolder(val binding: RecViewCardBinding) : RecyclerView.ViewHolder(binding.root)

    private val stringTools = StringTools()
    private var contextualMenuMode: ActionMode? = null
    private val contextualMenuModePayload = "contextualMode"
    private var contextualMenuModeActivity: Activity? = null
    private val contextualMenuModeCardIdList: ArrayList<Int> = ArrayList()
    private val contextualMenuModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            val inflater: MenuInflater = mode.menuInflater
            inflater.inflate(R.menu.menu_list_cards_context, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            menu.findItem(R.id.menu_list_cards_context_delete).isVisible =
                contextualMenuModeCardIdList.isNotEmpty()
            menu.findItem(R.id.menu_list_cards_context_move).isVisible =
                contextualMenuModeCardIdList.isNotEmpty() && packNo >= 0
            menu.findItem(R.id.menu_list_cards_context_print).isVisible =
                contextualMenuModeCardIdList.isNotEmpty() && contextualMenuModeCardIdList.size <= MAX_SIZE_CARDS_CONTEXTUAL_MENU_PRINT
            menu.findItem(R.id.menu_list_cards_context_select_all).isVisible =
                contextualMenuModeCardIdList.size < cards.size && cards.size <= MAX_SIZE_CARDS_CONTEXTUAL_MENU_SELECT
            return true
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.menu_list_cards_context_delete -> {
                    val contextualMenuModeCardList: ArrayList<DB_Card> = ArrayList()
                    for (id in contextualMenuModeCardIdList) {
                        cards.find { c -> c.card.uid == id }
                            ?.let { contextualMenuModeCardList.add(it.card) }
                    }
                    contextualMenuModeActivity?.let {
                        CardActions(it).delete(
                            contextualMenuModeCardList
                        )
                    }
                    mode.finish()
                    true
                }

                R.id.menu_list_cards_context_move -> {
                    val contextualMenuModeCardList: ArrayList<DB_Card> = ArrayList()
                    for (id in contextualMenuModeCardIdList) {
                        cards.find { c -> c.card.uid == id }
                            ?.let { contextualMenuModeCardList.add(it.card) }
                    }
                    contextualMenuModeActivity?.let {
                        CardActions(it).move(
                            contextualMenuModeCardList,
                            collectionNo
                        )
                    }
                    mode.finish()
                    true
                }

                R.id.menu_list_cards_context_print -> {
                    val contextualMenuModeCardList: ArrayList<DB_Card> = ArrayList()
                    for (id in contextualMenuModeCardIdList) {
                        cards.find { c -> c.card.uid == id }
                            ?.let { contextualMenuModeCardList.add(it.card) }
                    }
                    contextualMenuModeActivity?.let {
                        CardActions(it).print(
                            contextualMenuModeCardList
                        )
                    }
                    mode.finish()
                    true
                }

                R.id.menu_list_cards_context_select_all -> {
                    cards.forEach {
                        val card = it.card.uid
                        if (!contextualMenuModeCardIdList.contains(card)
                        ) {
                            contextualMenuModeSelectItem(card)
                        }
                    }
                    true
                }

                else -> false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            for (id in contextualMenuModeCardIdList) {
                contextualMenuModeFormatCard(id)
            }
            contextualMenuMode = null
        }
    }

    fun updateContent(
        cardsListNew: List<DB_Card_With_Meta>,
        reverse: Boolean,
        showBothSides: Boolean
    ) {
        val diffResult =
            DiffUtil.calculateDiff(
                ListCardCompare(
                    cards,
                    cardsListNew,
                    this.reverse != reverse || this.showBothSides != showBothSides
                )
            )
        this.reverse = reverse
        this.showBothSides = showBothSides
        cards.clear()
        cards.addAll(cardsListNew)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RecViewCardBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        viewGroup.context.getSharedPreferences(Globals.SETTINGS_NAME, Context.MODE_PRIVATE)
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
        if (cards.isEmpty()) {
            if (packNo >= 0) {
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
        if (contextualMenuMode != null && contextualMenuModeCardIdList.contains(cards[position].card.uid)) {
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

    private fun contextualMenuModeSelectItem(id: Int) {
        contextualMenuModeCardIdList.add(id)
        if (contextualMenuModeCardIdList.size == 1 || contextualMenuModeCardIdList.size == cards.size || contextualMenuModeCardIdList.size > MAX_SIZE_CARDS_CONTEXTUAL_MENU_PRINT) {
            contextualMenuMode?.invalidate()
        }
        contextualMenuModeFormatCard(id)
        contextualMenuModeSelectedTitle()
    }

    private fun contextualMenuModeFormatCard(id: Int) {
        notifyItemChanged(cards.indexOfFirst { c -> c.card.uid == id }, contextualMenuModePayload)
    }

    private fun contextualMenuModeSelectedTitle() {
        contextualMenuMode?.title = contextualMenuModeActivity?.resources?.let {
            String.format(
                it.getString(R.string.selected_title),
                contextualMenuModeCardIdList.size,
                cards.size
            )
        }
    }

    private fun prepareCardSide(back: Boolean, card: DB_Card_With_Meta, viewHolder: ViewHolder) {
        val context = viewHolder.binding.root.context
        val recText = if (back) {
            viewHolder.binding.recBack
        } else viewHolder.binding.recFront
        var cardText = if (back) {
            card.formattedBack ?: card.card.back
        } else card.formattedFront ?: card.card.front
        cardText = cardText.replace(System.lineSeparator(), " ")
        cardText = stringTools.shorten(cardText)
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
        if (packNo < 0) {
            val colors =
                context.resources.obtainTypedArray(R.array.pack_color_list)
            val color = card.packColor
            if (color >= 0 && color < colors.length()) {
                recText.setTextColor(colors.getColor(color, 0))
            }
            colors.recycle()
        }
        val extra = card.card.uid
        recText.setOnClickListener {
            if (contextualMenuMode != null) {
                if (contextualMenuModeCardIdList.contains(extra)) {
                    contextualMenuModeCardIdList.remove(extra)
                    if (contextualMenuModeCardIdList.size == cards.size - 1 || contextualMenuModeCardIdList.size == MAX_SIZE_CARDS_CONTEXTUAL_MENU_PRINT) {
                        contextualMenuMode?.invalidate()
                    }
                    contextualMenuModeFormatCard(extra)
                    contextualMenuModeSelectedTitle()
                    if (contextualMenuModeCardIdList.isEmpty()) {
                        contextualMenuMode?.finish()
                    }
                } else {
                    contextualMenuModeSelectItem(extra)
                }
            } else {
                val intent = Intent(context, ViewCard::class.java)
                intent.putExtra("collection", collectionNo)
                intent.putExtra("pack", packNo)
                intent.putExtra("card", extra)
                context.startActivity(intent)
            }
        }
        recText.setOnLongClickListener {
            if (contextualMenuMode != null) {
                return@setOnLongClickListener false
            }
            contextualMenuModeCardIdList.clear()
            contextualMenuModeActivity = ContextTools().getActivity(context)
            contextualMenuMode =
                contextualMenuModeActivity?.startActionMode(contextualMenuModeCallback)
            contextualMenuModeSelectItem(extra)
            return@setOnLongClickListener true
        }
    }
}
