package de.herrmann_engel.rbv.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.activities.ListPacks
import de.herrmann_engel.rbv.adapters.compare.ListCollectionCompare
import de.herrmann_engel.rbv.databinding.RecViewCollectionOrPackBinding
import de.herrmann_engel.rbv.db.CollectionWithMeta
import de.herrmann_engel.rbv.utils.StringTools
import de.herrmann_engel.rbv.utils.colorAt
import de.herrmann_engel.rbv.utils.putListCollection
import java.util.Locale

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
        val (background, backgroundBehind) = viewHolder.binding.backgroundLayers()
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
        viewHolder.binding.recCollectionsPreviewText.text =
            currentCollection.emoji.ifBlank {
                // Without emoji, show the first character of the name (including combining marks)
                val pattern = Regex("^(\\P{M}\\p{M}*+).*")
                currentCollection.name.replace(pattern, "$1")
            }
        viewHolder.binding.recCollectionsNumberText.text = collections[position].counter.toString()
        viewHolder.binding.recCollectionsNumberText.contentDescription = String.format(
            Locale.ROOT, "%d %s",
            collections[position].counter,
            context.resources.getString(R.string.items)
        )
        val resources = context.resources
        val color = resources.colorAt(R.array.pack_color_list, currentCollection.colors)
        val colorBackground =
            resources.colorAt(R.array.pack_color_background_light, currentCollection.colors)
        val colorBackgroundAlpha = resources.colorAt(
            R.array.pack_color_background_light_alpha,
            currentCollection.colors
        )
        if (color != null && colorBackground != null && colorBackgroundAlpha != null) {
            viewHolder.binding.recCollectionsName.setTextColor(color)
            viewHolder.binding.recCollectionsPreviewText.setTextColor(color)
            viewHolder.binding.recCollectionsPreviewText.setBackgroundColor(colorBackground)
            background.mutate()
            background.setStroke(2, color)
            background.setColor(colorBackgroundAlpha)
            backgroundBehind.mutate()
            backgroundBehind.setStroke(1, colorBackground)
        }
    }

    override fun getItemCount(): Int {
        return collections.size
    }
}
