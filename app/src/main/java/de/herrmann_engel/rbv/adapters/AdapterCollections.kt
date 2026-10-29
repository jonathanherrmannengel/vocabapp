package de.herrmann_engel.rbv.adapters

import android.content.Intent
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO
import android.view.View.IMPORTANT_FOR_ACCESSIBILITY_YES
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.actions.CollectionActions
import de.herrmann_engel.rbv.activities.ListPacks
import de.herrmann_engel.rbv.adapters.compare.ListCollectionCompare
import de.herrmann_engel.rbv.databinding.RecViewCollectionOrPackBinding
import de.herrmann_engel.rbv.db.CollectionWithMeta
import de.herrmann_engel.rbv.db.DbCollection
import de.herrmann_engel.rbv.utils.StringTools
import de.herrmann_engel.rbv.utils.putListCollection

/**
 * Collection list of the start screen. The row for all packs and the welcome message are shown by
 * [AdapterListHeader]. A long click starts the selection of several collections to delete them.
 */
class AdapterCollections(
    private val collections: MutableList<CollectionWithMeta>,
    private var uiFontSizeBig: Boolean,
    private val collectionActions: CollectionActions
) :
    RecyclerView.Adapter<AdapterCollections.ViewHolder>() {

    class ViewHolder(val binding: RecViewCollectionOrPackBinding) :
        RecyclerView.ViewHolder(binding.root)

    // Selection of several collections (started by long click) to delete them
    private val selection = SelectionMode(
        R.menu.menu_list_cards_context,
        itemCount = { collections.size },
        redrawItem = { id ->
            notifyItemChanged(
                collections.indexOfFirst { it.collection.uid == id },
                SelectionMode.PAYLOAD
            )
        },
        redrawAll = { notifyItemRangeChanged(0, collections.size, SelectionMode.PAYLOAD) },
        prepareMenu = ::prepareSelectionMenu,
        onAction = ::onSelectionAction
    )

    private fun prepareSelectionMenu(menu: Menu, selectedIds: Set<Int>) {
        menu.findItem(R.id.menu_list_cards_context_delete).isVisible = selectedIds.isNotEmpty()
        menu.findItem(R.id.menu_list_cards_context_move).isVisible = false
        menu.findItem(R.id.menu_list_cards_context_print).isVisible = false
        menu.findItem(R.id.menu_list_cards_context_select_all).isVisible =
            selectedIds.size < collections.size
    }

    private fun onSelectionAction(itemId: Int, selectedIds: List<Int>): Boolean {
        when (itemId) {
            R.id.menu_list_cards_context_delete ->
                collectionActions.delete(selectedCollections(selectedIds))

            R.id.menu_list_cards_context_select_all -> {
                selection.selectAll(collections.map { it.collection.uid })
                return true
            }

            else -> return false
        }
        selection.finish()
        return true
    }

    // In the order of the ids
    private fun selectedCollections(ids: List<Int>): ArrayList<DbCollection> {
        val collectionsById = collections.associateBy { it.collection.uid }
        return ArrayList(ids.mapNotNull { collectionsById[it]?.collection })
    }

    // Updates the list; all items are redrawn if the font size setting has changed
    fun updateSettingsAndContent(
        collectionListNew: List<CollectionWithMeta>,
        uiFontSizeBig: Boolean
    ) {
        val updateAllContent = this.uiFontSizeBig != uiFontSizeBig
        this.uiFontSizeBig = uiFontSizeBig
        updateContent(collectionListNew, updateAllContent)
    }

    private fun updateContent(
        collectionListNew: List<CollectionWithMeta>,
        updateAllContent: Boolean
    ) {
        val diffResult =
            DiffUtil.calculateDiff(
                ListCollectionCompare(
                    collections,
                    collectionListNew,
                    updateAllContent
                )
            )
        collections.clear()
        collections.addAll(collectionListNew)
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
        val currentCollection = collections[position].collection
        val collectionId = currentCollection.uid
        viewHolder.binding.root.setOnClickListener {
            // In selection mode a click toggles the selection, otherwise it opens the collection
            if (selection.isActive) {
                selection.toggle(collectionId)
            } else {
                val intent = Intent(context, ListPacks::class.java)
                intent.putListCollection(collectionId)
                context.startActivity(intent)
            }
        }
        viewHolder.binding.root.setOnLongClickListener { selection.start(context, collectionId) }
        viewHolder.binding.recCollectionsName.text =
            StringTools.shorten(currentCollection.name)
        val selected = selection.isSelected(currentCollection.uid)
        if (selected) {
            viewHolder.binding.recCollectionsPreviewText.contentDescription =
                context.resources.getString(R.string.selected)
            viewHolder.binding.recCollectionsPreviewText.importantForAccessibility =
                IMPORTANT_FOR_ACCESSIBILITY_YES
            viewHolder.binding.recCollectionsName.setTypeface(null, Typeface.BOLD)
        }
        val desc = currentCollection.desc
        if (desc.isBlank()) {
            viewHolder.binding.recCollectionsDesc.visibility = View.GONE
        } else {
            viewHolder.binding.recCollectionsDesc.visibility = View.VISIBLE
            viewHolder.binding.recCollectionsDesc.text = StringTools.shorten(desc)
        }
        viewHolder.binding.showPreview(currentCollection.name, currentCollection.emoji, selected)
        viewHolder.binding.showCounter(collections[position].counter)
        viewHolder.binding.applyColors(currentCollection.colors, selected)
    }

    override fun getItemCount(): Int {
        return collections.size
    }
}
