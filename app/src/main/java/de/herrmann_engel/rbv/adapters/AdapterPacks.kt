package de.herrmann_engel.rbv.adapters

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.util.TypedValue
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO
import android.view.View.IMPORTANT_FOR_ACCESSIBILITY_YES
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import de.herrmann_engel.rbv.Globals.LIST_CARDS_GET_DB_COLLECTIONS_ALL
import de.herrmann_engel.rbv.Globals.LIST_CARDS_GET_DB_PACKS_ALL
import de.herrmann_engel.rbv.Globals.MAX_SIZE_PACKS_CONTEXTUAL_MENU_SELECT
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.actions.PackActions
import de.herrmann_engel.rbv.activities.ListCards
import de.herrmann_engel.rbv.adapters.compare.ListPackCompare
import de.herrmann_engel.rbv.databinding.RecViewCollectionOrPackBinding
import de.herrmann_engel.rbv.db.DB_Pack
import de.herrmann_engel.rbv.db.DB_Pack_With_Meta
import de.herrmann_engel.rbv.utils.ContextTools
import de.herrmann_engel.rbv.utils.StringTools
import java.util.Locale

class AdapterPacks(
    private val packs: MutableList<DB_Pack_With_Meta>,
    private val uiFontSizeBig: Boolean,
    private val collection: Int
) : RecyclerView.Adapter<AdapterPacks.ViewHolder>() {
    class ViewHolder(val binding: RecViewCollectionOrPackBinding) :
        RecyclerView.ViewHolder(binding.root)

    private val stringTools = StringTools()
    private var contextualMenuMode: ActionMode? = null
    private val contextualMenuModePayload = "contextualMode"
    private var contextualMenuModeActivity: Activity? = null
    private val contextualMenuModePackIdList: MutableList<Int> = ArrayList()
    private val contextualMenuModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            val inflater: MenuInflater = mode.menuInflater
            inflater.inflate(R.menu.menu_list_cards_context, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            menu.findItem(R.id.menu_list_cards_context_delete).isVisible =
                contextualMenuModePackIdList.isNotEmpty()
            menu.findItem(R.id.menu_list_cards_context_move).isVisible =
                contextualMenuModePackIdList.isNotEmpty() && collection >= 0
            menu.findItem(R.id.menu_list_cards_context_print).isVisible = false
            menu.findItem(R.id.menu_list_cards_context_select_all).isVisible =
                contextualMenuModePackIdList.size < packs.size - 1 && packs.size - 1 <= MAX_SIZE_PACKS_CONTEXTUAL_MENU_SELECT
            return true
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.menu_list_cards_context_delete -> {
                    val contextualMenuModePackList: ArrayList<DB_Pack> = ArrayList()
                    for (id in contextualMenuModePackIdList) {
                        packs.find { p -> p.pack?.uid == id }
                            ?.let { contextualMenuModePackList.add(it.pack) }
                    }
                    contextualMenuModeActivity?.let {
                        PackActions(it).delete(
                            contextualMenuModePackList
                        )
                    }
                    mode.finish()
                    true
                }

                R.id.menu_list_cards_context_move -> {
                    val contextualMenuModePackList: ArrayList<DB_Pack> = ArrayList()
                    for (id in contextualMenuModePackIdList) {
                        packs.find { p -> p.pack?.uid == id }
                            ?.let { contextualMenuModePackList.add(it.pack) }
                    }
                    contextualMenuModeActivity?.let {
                        PackActions(it).move(
                            contextualMenuModePackList
                        )
                    }
                    mode.finish()
                    true
                }

                R.id.menu_list_cards_context_select_all -> {
                    packs.forEach {
                        if (it.pack != null) {
                            val pack = it.pack.uid
                            if (!contextualMenuModePackIdList.contains(pack)
                            ) {
                                contextualMenuModeSelectItem(pack)
                            }
                        }
                    }
                    true
                }

                else -> false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            for (id in contextualMenuModePackIdList) {
                contextualMenuModeFormatPack(id)
            }
            contextualMenuMode = null
        }
    }

    fun updateContent(
        packListNew: List<DB_Pack_With_Meta>
    ) {
        val diffResult =
            DiffUtil.calculateDiff(
                ListPackCompare(
                    packs,
                    packListNew
                )
            )
        packs.clear()
        packs.addAll(packListNew)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding = RecViewCollectionOrPackBinding.inflate(
            LayoutInflater.from(viewGroup.context),
            viewGroup,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val context = viewHolder.binding.root.context
        if (uiFontSizeBig) {
            viewHolder.binding.recCollectionsName.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                context.resources.getDimension(R.dimen.rec_view_font_size_big)
            )
            viewHolder.binding.recCollectionsDesc.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                context.resources.getDimension(R.dimen.rec_view_font_size_below_big)
            )
            viewHolder.binding.recCollectionsParent.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                context.resources.getDimension(R.dimen.rec_view_font_size_above_big)
            )
        }
        viewHolder.binding.recCollectionsPreviewText.contentDescription = null
        viewHolder.binding.recCollectionsPreviewText.importantForAccessibility =
            IMPORTANT_FOR_ACCESSIBILITY_NO
        viewHolder.binding.recCollectionsName.setTypeface(null, Typeface.NORMAL)
        val backgroundLayerList = viewHolder.binding.root.background as LayerDrawable
        val background =
            backgroundLayerList.findDrawableByLayerId(R.id.rec_view_collection_or_pack_background_main) as GradientDrawable
        val backgroundBehind =
            backgroundLayerList.findDrawableByLayerId(R.id.rec_view_collection_or_pack_background_behind) as GradientDrawable
        viewHolder.binding.recCollectionsParent.visibility = View.GONE
        viewHolder.binding.recCollectionsName.textAlignment = View.TEXT_ALIGNMENT_INHERIT
        viewHolder.binding.recCollectionsPreviewText.visibility = View.VISIBLE
        viewHolder.binding.recCollectionsNumberText.visibility = View.VISIBLE
        if (position == 0 && packs.size == 1) {
            if (collection >= 0) {
                val text =
                    String.format(
                        "%s %s",
                        context.resources.getString(R.string.welcome_pack),
                        context.resources.getString(R.string.welcome_pack_create)
                    )
                val addText = SpannableString(text)
                val addTextDrawable = ContextCompat.getDrawable(context, R.drawable.outline_add_24)
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
                viewHolder.binding.recCollectionsName.text = addText
            } else {
                viewHolder.binding.recCollectionsName.text =
                    context.resources.getString(R.string.welcome_pack)
            }
            viewHolder.binding.recCollectionsName.textAlignment = View.TEXT_ALIGNMENT_CENTER
            viewHolder.binding.recCollectionsDesc.visibility = View.GONE
            viewHolder.binding.recCollectionsPreviewText.visibility = View.GONE
            viewHolder.binding.recCollectionsNumberText.visibility = View.GONE
            viewHolder.binding.recCollectionsName.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.default_text
                )
            )
            background.mutate()
            background.setStroke(0, Color.rgb(0, 0, 0))
            background.setColor(Color.argb(0, 0, 0, 0))
            backgroundBehind.mutate()
            backgroundBehind.setStroke(0, Color.rgb(0, 0, 0))
        } else if (position == 0) {
            viewHolder.binding.root.setOnClickListener {
                val intent = Intent(context, ListCards::class.java)
                intent.putExtra("collection", collection)
                intent.putExtra("pack", LIST_CARDS_GET_DB_PACKS_ALL)
                context.startActivity(intent)
            }
            viewHolder.binding.recCollectionsName.text =
                context.resources.getString(R.string.all_cards)
            viewHolder.binding.recCollectionsDesc.visibility = View.VISIBLE
            viewHolder.binding.recCollectionsDesc.text =
                if (collection == LIST_CARDS_GET_DB_COLLECTIONS_ALL) {
                    context.resources.getString(R.string.all_cards_desc)
                } else {
                    context.resources.getString(R.string.all_cards_desc_by_pack)
                }
            viewHolder.binding.recCollectionsPreviewText.text = "…"
            if (packs[position].counter != -1) {
                viewHolder.binding.recCollectionsNumberText.visibility = View.VISIBLE
                viewHolder.binding.recCollectionsNumberText.text =
                    packs[position].counter.toString()
                viewHolder.binding.recCollectionsNumberText.contentDescription = String.format(
                    Locale.ROOT, "%d %s",
                    packs[position].counter,
                    context.resources.getString(R.string.items)
                )
            } else {
                viewHolder.binding.recCollectionsNumberText.visibility = View.GONE
            }

            viewHolder.binding.recCollectionsName.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.default_text
                )
            )
            viewHolder.binding.recCollectionsPreviewText.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.default_text
                )
            )
            viewHolder.binding.recCollectionsPreviewText.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.pack_default_item_background
                )
            )
            background.mutate()
            background.setStroke(
                2, ContextCompat.getColor(
                    context,
                    R.color.pack_default_item_stroke
                )
            )
            background.setColor(
                ContextCompat.getColor(
                    context,
                    R.color.pack_default_item_background_alpha
                )
            )
            backgroundBehind.mutate()
            backgroundBehind.setStroke(
                1, ContextCompat.getColor(
                    context,
                    R.color.pack_default_item_background
                )
            )
        } else {
            val currentPack = packs[position].pack
            val extra = currentPack.uid
            viewHolder.binding.root.setOnClickListener {
                if (contextualMenuMode != null) {
                    if (contextualMenuModePackIdList.contains(extra)) {
                        contextualMenuModePackIdList.remove(extra)
                        if (contextualMenuModePackIdList.size == packs.size - 2) {
                            contextualMenuMode?.invalidate()
                        }
                        contextualMenuModeFormatPack(extra)
                        contextualMenuModeSelectedTitle()
                        if (contextualMenuModePackIdList.isEmpty()) {
                            contextualMenuMode?.finish()
                        }
                    } else {
                        contextualMenuModeSelectItem(extra)
                    }
                } else {
                    val intent = Intent(context, ListCards::class.java)
                    intent.putExtra("collection", collection)
                    intent.putExtra("pack", extra)
                    context.startActivity(intent)
                }
            }
            viewHolder.binding.root.setOnLongClickListener {
                if (contextualMenuMode != null) {
                    return@setOnLongClickListener false
                }
                contextualMenuModePackIdList.clear()
                contextualMenuModeActivity = ContextTools().getActivity(context)
                contextualMenuMode = contextualMenuModeActivity?.startActionMode(contextualMenuModeCallback)
                contextualMenuModeSelectItem(extra)
                return@setOnLongClickListener true
            }
            viewHolder.binding.recCollectionsName.text =
                stringTools.shorten(currentPack.name)
            if (contextualMenuMode != null && contextualMenuModePackIdList.contains(currentPack.uid)) {
                viewHolder.binding.recCollectionsPreviewText.contentDescription =
                    context.resources.getString(R.string.selected)
                viewHolder.binding.recCollectionsPreviewText.importantForAccessibility =
                    IMPORTANT_FOR_ACCESSIBILITY_YES
                viewHolder.binding.recCollectionsName.setTypeface(null, Typeface.BOLD)
            }
            if (currentPack.desc.isNullOrEmpty()) {
                viewHolder.binding.recCollectionsDesc.visibility = View.GONE
            } else {
                viewHolder.binding.recCollectionsDesc.visibility = View.VISIBLE
                viewHolder.binding.recCollectionsDesc.text =
                    stringTools.shorten(currentPack.desc)
            }
            val emojiText = currentPack.emoji
            viewHolder.binding.recCollectionsPreviewText.text =
                if (contextualMenuMode != null && contextualMenuModePackIdList.contains(currentPack.uid)) {
                    "✓"
                } else if (emojiText.isNullOrEmpty()) {
                    val pattern = Regex("^(\\P{M}\\p{M}*+).*")
                    currentPack.name.replace(pattern, "$1")
                } else {
                    emojiText
                }
            if (packs[position].counter != -1) {
                viewHolder.binding.recCollectionsNumberText.visibility = View.VISIBLE
                viewHolder.binding.recCollectionsNumberText.text =
                    packs[position].counter.toString()
                viewHolder.binding.recCollectionsNumberText.contentDescription = String.format(
                    Locale.ROOT, "%d %s",
                    packs[position].counter,
                    context.resources.getString(R.string.items)
                )
            } else {
                viewHolder.binding.recCollectionsNumberText.visibility = View.GONE
            }
            if (collection == LIST_CARDS_GET_DB_COLLECTIONS_ALL) {
                viewHolder.binding.recCollectionsParent.visibility = View.VISIBLE
                viewHolder.binding.recCollectionsParent.text = packs[position].collectionName
            }
            val colors = context.resources.obtainTypedArray(R.array.pack_color_list)
            val colorsBackground =
                context.resources.obtainTypedArray(R.array.pack_color_background_light)
            val colorsBackgroundAlpha =
                context.resources.obtainTypedArray(R.array.pack_color_background_light_alpha)
            val minimalLength = colors.length().coerceAtMost(colorsBackground.length())
                .coerceAtMost(colorsBackgroundAlpha.length())
            val color = currentPack.colors
            if (color in 0..<minimalLength) {
                viewHolder.binding.recCollectionsName.setTextColor(colors.getColor(color, 0))
                viewHolder.binding.recCollectionsPreviewText.setTextColor(
                    if (contextualMenuMode != null && contextualMenuModePackIdList.contains(
                            currentPack.uid
                        )
                    ) {
                        ContextCompat.getColor(
                            context,
                            R.color.default_text
                        )
                    } else {
                        colors.getColor(color, 0)
                    }
                )
                viewHolder.binding.recCollectionsPreviewText.setBackgroundColor(
                    if (contextualMenuMode != null && contextualMenuModePackIdList.contains(
                            currentPack.uid
                        )
                    ) {
                        Color.TRANSPARENT
                    } else {
                        colorsBackground.getColor(
                            color,
                            0
                        )
                    }
                )
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
        return packs.size
    }

    private fun contextualMenuModeSelectItem(id: Int) {
        contextualMenuModePackIdList.add(id)
        if (contextualMenuModePackIdList.size == 1 || contextualMenuModePackIdList.size == packs.size - 1) {
            contextualMenuMode?.invalidate()
        }
        contextualMenuModeFormatPack(id)
        contextualMenuModeSelectedTitle()
    }

    private fun contextualMenuModeFormatPack(id: Int) {
        notifyItemChanged(packs.indexOfFirst { p -> p.pack?.uid == id }, contextualMenuModePayload)
    }

    private fun contextualMenuModeSelectedTitle() {
        contextualMenuMode?.title = contextualMenuModeActivity?.resources?.let {
            String.format(
                it.getString(R.string.selected_title),
                contextualMenuModePackIdList.size,
                packs.size - 1
            )
        }
    }
}
