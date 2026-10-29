package de.herrmann_engel.rbv.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import de.herrmann_engel.rbv.activities.ListPacks
import de.herrmann_engel.rbv.adapters.compare.ListCollectionCompare
import de.herrmann_engel.rbv.databinding.RecViewCollectionOrPackBinding
import de.herrmann_engel.rbv.db.CollectionWithMeta
import de.herrmann_engel.rbv.utils.StringTools
import de.herrmann_engel.rbv.utils.putListCollection

/**
 * Collection list of the start screen. The row for all packs and the welcome message are shown by
 * [AdapterListHeader].
 */
class AdapterCollections(
    private val collections: MutableList<CollectionWithMeta>,
    private var uiFontSizeBig: Boolean
) :
    RecyclerView.Adapter<AdapterCollections.ViewHolder>() {

    class ViewHolder(val binding: RecViewCollectionOrPackBinding) :
        RecyclerView.ViewHolder(binding.root)

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
        val currentCollection = collections[position].collection
        val collectionId = currentCollection.uid
        viewHolder.binding.root.setOnClickListener {
            val intent = Intent(
                context,
                ListPacks::class.java
            )
            intent.putListCollection(collectionId)
            context.startActivity(intent)
        }
        viewHolder.binding.recCollectionsName.text =
            StringTools.shorten(currentCollection.name)
        val desc = currentCollection.desc
        if (desc.isBlank()) {
            viewHolder.binding.recCollectionsDesc.visibility = View.GONE
        } else {
            viewHolder.binding.recCollectionsDesc.visibility = View.VISIBLE
            viewHolder.binding.recCollectionsDesc.text = StringTools.shorten(desc)
        }
        viewHolder.binding.showPreview(currentCollection.name, currentCollection.emoji)
        viewHolder.binding.showCounter(collections[position].counter)
        viewHolder.binding.applyColors(currentCollection.colors)
    }

    override fun getItemCount(): Int {
        return collections.size
    }
}
