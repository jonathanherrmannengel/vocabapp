package de.herrmann_engel.rbv.adapters

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO
import android.view.View.IMPORTANT_FOR_ACCESSIBILITY_YES
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.actions.PackActions
import de.herrmann_engel.rbv.activities.ListCards
import de.herrmann_engel.rbv.adapters.compare.ListPackCompare
import de.herrmann_engel.rbv.cardlist.CardSource
import de.herrmann_engel.rbv.databinding.RecViewCollectionOrPackBinding
import de.herrmann_engel.rbv.db.DbPack
import de.herrmann_engel.rbv.db.PackWithMeta
import de.herrmann_engel.rbv.utils.StringTools
import de.herrmann_engel.rbv.utils.colorAt
import de.herrmann_engel.rbv.utils.putCardSource
import java.util.Locale

/**
 * Pack list of a collection (or of all collections if collectionId is null). The row for all
 * cards and the welcome message are shown by [AdapterListHeader].
 */
class AdapterPacks(
    private val packs: MutableList<PackWithMeta>,
    private var uiFontSizeBig: Boolean,
    private val collectionId: Int?,
    private val packActions: PackActions
) : RecyclerView.Adapter<AdapterPacks.ViewHolder>() {
    class ViewHolder(val binding: RecViewCollectionOrPackBinding) :
        RecyclerView.ViewHolder(binding.root)

    // Selection of several packs (started by long click) to delete or move them
    private val selection = SelectionMode(
        R.menu.menu_list_cards_context,
        itemCount = { packs.size },
        redrawItem = { id ->
            notifyItemChanged(packs.indexOfFirst { it.pack.uid == id }, SelectionMode.PAYLOAD)
        },
        redrawAll = { notifyItemRangeChanged(0, packs.size, SelectionMode.PAYLOAD) },
        prepareMenu = ::prepareSelectionMenu,
        onAction = ::onSelectionAction
    )

    // Moving is only possible inside a collection
    private fun prepareSelectionMenu(menu: Menu, selectedIds: Set<Int>) {
        menu.findItem(R.id.menu_list_cards_context_delete).isVisible = selectedIds.isNotEmpty()
        menu.findItem(R.id.menu_list_cards_context_move).isVisible =
            selectedIds.isNotEmpty() && collectionId != null
        menu.findItem(R.id.menu_list_cards_context_print).isVisible = false
        menu.findItem(R.id.menu_list_cards_context_select_all).isVisible =
            selectedIds.size < packs.size
    }

    private fun onSelectionAction(itemId: Int, selectedIds: List<Int>): Boolean {
        when (itemId) {
            R.id.menu_list_cards_context_delete -> packActions.delete(selectedPacks(selectedIds))

            R.id.menu_list_cards_context_move -> packActions.move(selectedPacks(selectedIds))

            R.id.menu_list_cards_context_select_all -> {
                selection.selectAll(packs.map { it.pack.uid })
                return true
            }

            else -> return false
        }
        selection.finish()
        return true
    }

    // In the order of the ids
    private fun selectedPacks(ids: List<Int>): ArrayList<DbPack> {
        val packsById = packs.associateBy { it.pack.uid }
        return ArrayList(ids.mapNotNull { packsById[it]?.pack })
    }

    // Updates the list; all items are redrawn if the font size setting has changed
    fun updateSettingsAndContent(
        packListNew: List<PackWithMeta>,
        uiFontSizeBig: Boolean
    ) {
        val updateAllContent = this.uiFontSizeBig != uiFontSizeBig
        this.uiFontSizeBig = uiFontSizeBig
        updateContent(packListNew, updateAllContent)
    }

    // Replaces the list content, using DiffUtil to only update changed items
    private fun updateContent(
        packListNew: List<PackWithMeta>,
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
            if (selection.isActive) {
                selection.toggle(packId)
            } else {
                val intent = Intent(context, ListCards::class.java)
                intent.putCardSource(CardSource.Pack(packId, collectionId))
                context.startActivity(intent)
            }
        }
        viewHolder.binding.root.setOnLongClickListener { selection.start(context, packId) }
        viewHolder.binding.recCollectionsName.text =
            StringTools.shorten(currentPack.name)
        val selected = selection.isSelected(currentPack.uid)
        if (selected) {
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
            if (selected) {
                "✓"
            } else if (currentPack.emoji.isBlank()) {
                val pattern = Regex("^(\\P{M}\\p{M}*+).*")
                currentPack.name.replace(pattern, "$1")
            } else {
                currentPack.emoji
            }
        viewHolder.binding.recCollectionsNumberText.text = packs[position].counter.toString()
        viewHolder.binding.recCollectionsNumberText.contentDescription = String.format(
            Locale.ROOT, "%d %s",
            packs[position].counter,
            context.resources.getString(R.string.items)
        )
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
                if (selected) {
                    ContextCompat.getColor(
                        context,
                        R.color.default_text
                    )
                } else {
                    color
                }
            )
            viewHolder.binding.recCollectionsPreviewText.setBackgroundColor(
                if (selected) {
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
}
