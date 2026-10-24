package de.herrmann_engel.rbv.adapters

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO
import android.view.View.IMPORTANT_FOR_ACCESSIBILITY_YES
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import de.herrmann_engel.rbv.Globals.MAX_SIZE_PACKS_CONTEXTUAL_MENU_SELECT
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.actions.PackActions
import de.herrmann_engel.rbv.activities.ListCards
import de.herrmann_engel.rbv.adapters.compare.ListPackCompare
import de.herrmann_engel.rbv.cardlist.CardSource
import de.herrmann_engel.rbv.databinding.RecViewCollectionOrPackBinding
import de.herrmann_engel.rbv.db.DB_Pack
import de.herrmann_engel.rbv.db.DB_Pack_With_Meta
import de.herrmann_engel.rbv.utils.StringTools
import de.herrmann_engel.rbv.utils.colorAt
import de.herrmann_engel.rbv.utils.findActivity
import de.herrmann_engel.rbv.utils.putCardSource
import java.util.Locale

/**
 * Pack list of a collection (or of all collections if collectionId is null). The row for all
 * cards and the welcome message are shown by [AdapterListHeader].
 */
class AdapterPacks(
    private val packs: MutableList<DB_Pack_With_Meta>,
    private var uiFontSizeBig: Boolean,
    private val collectionId: Int?,
    private val packActions: PackActions
) : RecyclerView.Adapter<AdapterPacks.ViewHolder>() {
    class ViewHolder(val binding: RecViewCollectionOrPackBinding) :
        RecyclerView.ViewHolder(binding.root)

    // Contextual action mode (started by long click) to select multiple packs and delete or move them
    private var contextualMenuMode: ActionMode? = null
    private val contextualMenuModePayload = "contextualMode"
    private var contextualMenuModeActivity: Activity? = null
    private val contextualMenuModePackIdList: ArrayList<Int> = ArrayList()
    private val contextualMenuModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            val inflater: MenuInflater = mode.menuInflater
            inflater.inflate(R.menu.menu_list_cards_context, menu)
            return true
        }

        // Moving is only possible inside a collection; "select all" only for lists that are not too long.
        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            menu.findItem(R.id.menu_list_cards_context_delete).isVisible =
                contextualMenuModePackIdList.isNotEmpty()
            menu.findItem(R.id.menu_list_cards_context_move).isVisible =
                contextualMenuModePackIdList.isNotEmpty() && collectionId != null
            menu.findItem(R.id.menu_list_cards_context_print).isVisible = false
            menu.findItem(R.id.menu_list_cards_context_select_all).isVisible =
                contextualMenuModePackIdList.size < packs.size && packs.size <= MAX_SIZE_PACKS_CONTEXTUAL_MENU_SELECT
            return true
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.menu_list_cards_context_delete -> {
                    val contextualMenuModePackList: ArrayList<DB_Pack> = ArrayList()
                    for (id in contextualMenuModePackIdList) {
                        packs.find { p -> p.pack.uid == id }
                            ?.let { contextualMenuModePackList.add(it.pack) }
                    }
                    packActions.delete(contextualMenuModePackList)
                    mode.finish()
                    true
                }

                R.id.menu_list_cards_context_move -> {
                    val contextualMenuModePackList: ArrayList<DB_Pack> = ArrayList()
                    for (id in contextualMenuModePackIdList) {
                        packs.find { p -> p.pack.uid == id }
                            ?.let { contextualMenuModePackList.add(it.pack) }
                    }
                    packActions.move(contextualMenuModePackList)
                    mode.finish()
                    true
                }

                R.id.menu_list_cards_context_select_all -> {
                    packs.forEach {
                        val packId = it.pack.uid
                        if (!contextualMenuModePackIdList.contains(packId)) {
                            contextualMenuModeSelectItem(packId)
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

    // Updates the list; all items are redrawn if the font size setting has changed
    fun updateSettingsAndContent(
        packListNew: List<DB_Pack_With_Meta>,
        uiFontSizeBig: Boolean
    ) {
        val updateAllContent = this.uiFontSizeBig != uiFontSizeBig
        this.uiFontSizeBig = uiFontSizeBig
        updateContent(packListNew, updateAllContent)
    }

    // Replaces the list content, using DiffUtil to only update changed items
    private fun updateContent(
        packListNew: List<DB_Pack_With_Meta>,
        updateAllContent: Boolean
    ) {
        val diffResult =
            DiffUtil.calculateDiff(
                ListPackCompare(
                    packs,
                    packListNew,
                    updateAllContent
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
        viewHolder.binding.applyFontSize(uiFontSizeBig)
        viewHolder.binding.recCollectionsPreviewText.contentDescription = null
        viewHolder.binding.recCollectionsPreviewText.importantForAccessibility =
            IMPORTANT_FOR_ACCESSIBILITY_NO
        viewHolder.binding.recCollectionsName.setTypeface(null, Typeface.NORMAL)
        val (background, backgroundBehind) = viewHolder.binding.backgroundLayers()
        viewHolder.binding.recCollectionsParent.visibility = View.GONE
        val currentPack = packs[position].pack
        val packId = currentPack.uid
        viewHolder.binding.root.setOnClickListener {
            // In selection mode a click toggles the selection, otherwise it opens the pack
            if (contextualMenuMode != null) {
                if (contextualMenuModePackIdList.contains(packId)) {
                    contextualMenuModePackIdList.remove(packId)
                    if (contextualMenuModePackIdList.size == packs.size - 1) {
                        contextualMenuMode?.invalidate()
                    }
                    contextualMenuModeFormatPack(packId)
                    contextualMenuModeSelectedTitle()
                    if (contextualMenuModePackIdList.isEmpty()) {
                        contextualMenuMode?.finish()
                    }
                } else {
                    contextualMenuModeSelectItem(packId)
                }
            } else {
                val intent = Intent(context, ListCards::class.java)
                intent.putCardSource(CardSource.Pack(packId, collectionId))
                context.startActivity(intent)
            }
        }
        viewHolder.binding.root.setOnLongClickListener {
            if (contextualMenuMode != null) {
                return@setOnLongClickListener false
            }
            contextualMenuModePackIdList.clear()
            contextualMenuModeActivity = context.findActivity()
            contextualMenuMode =
                contextualMenuModeActivity?.startActionMode(contextualMenuModeCallback)
            contextualMenuModeSelectItem(packId)
            return@setOnLongClickListener true
        }
        viewHolder.binding.recCollectionsName.text =
            StringTools.shorten(currentPack.name)
        if (contextualMenuMode != null && contextualMenuModePackIdList.contains(currentPack.uid)) {
            viewHolder.binding.recCollectionsPreviewText.contentDescription =
                context.resources.getString(R.string.selected)
            viewHolder.binding.recCollectionsPreviewText.importantForAccessibility =
                IMPORTANT_FOR_ACCESSIBILITY_YES
            viewHolder.binding.recCollectionsName.setTypeface(null, Typeface.BOLD)
        }
        val desc = currentPack.desc
        if (desc.isBlank()) {
            viewHolder.binding.recCollectionsDesc.visibility = View.GONE
        } else {
            viewHolder.binding.recCollectionsDesc.visibility = View.VISIBLE
            viewHolder.binding.recCollectionsDesc.text = StringTools.shorten(desc)
        }
        viewHolder.binding.recCollectionsPreviewText.text =
            if (contextualMenuMode != null && contextualMenuModePackIdList.contains(
                    currentPack.uid
                )
            ) {
                "✓"
            } else if (currentPack.emoji.isBlank()) {
                val pattern = Regex("^(\\P{M}\\p{M}*+).*")
                currentPack.name.replace(pattern, "$1")
            } else {
                currentPack.emoji
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
        // Packs of all collections are listed: show the collection name as well
        if (collectionId == null) {
            viewHolder.binding.recCollectionsParent.visibility = View.VISIBLE
            viewHolder.binding.recCollectionsParent.text = packs[position].collectionName
        }
        val resources = context.resources
        val color = resources.colorAt(R.array.pack_color_list, currentPack.colors)
        val colorBackground =
            resources.colorAt(R.array.pack_color_background_light, currentPack.colors)
        val colorBackgroundAlpha =
            resources.colorAt(R.array.pack_color_background_light_alpha, currentPack.colors)
        if (color != null && colorBackground != null && colorBackgroundAlpha != null) {
            viewHolder.binding.recCollectionsName.setTextColor(color)
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
                    color
                }
            )
            viewHolder.binding.recCollectionsPreviewText.setBackgroundColor(
                if (contextualMenuMode != null && contextualMenuModePackIdList.contains(
                        currentPack.uid
                    )
                ) {
                    Color.TRANSPARENT
                } else {
                    colorBackground
                }
            )
            background.mutate()
            background.setStroke(2, color)
            background.setColor(colorBackgroundAlpha)
            backgroundBehind.mutate()
            backgroundBehind.setStroke(1, colorBackground)
        }
    }

    override fun getItemCount(): Int {
        return packs.size
    }

    // Selects a pack; the menu is updated when the selection becomes non-empty or complete
    private fun contextualMenuModeSelectItem(id: Int) {
        contextualMenuModePackIdList.add(id)
        if (contextualMenuModePackIdList.size == 1 || contextualMenuModePackIdList.size == packs.size) {
            contextualMenuMode?.invalidate()
        }
        contextualMenuModeFormatPack(id)
        contextualMenuModeSelectedTitle()
    }

    // Redraws a pack to show its selection state
    private fun contextualMenuModeFormatPack(id: Int) {
        notifyItemChanged(packs.indexOfFirst { p -> p.pack.uid == id }, contextualMenuModePayload)
    }

    private fun contextualMenuModeSelectedTitle() {
        contextualMenuMode?.title = contextualMenuModeActivity?.resources?.let {
            String.format(
                it.getString(R.string.selected_title),
                contextualMenuModePackIdList.size,
                packs.size
            )
        }
    }
}
