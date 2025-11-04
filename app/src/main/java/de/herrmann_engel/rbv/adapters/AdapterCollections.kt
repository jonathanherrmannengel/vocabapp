package de.herrmann_engel.rbv.adapters

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import de.herrmann_engel.rbv.Globals.LIST_CARDS_GET_DB_COLLECTIONS_ALL
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.activities.ListPacks
import de.herrmann_engel.rbv.adapters.compare.ListCollectionCompare
import de.herrmann_engel.rbv.databinding.RecViewCollectionOrPackBinding
import de.herrmann_engel.rbv.db.DB_Collection_With_Meta
import de.herrmann_engel.rbv.utils.StringTools
import java.util.Locale

class AdapterCollections(
    private val collections: MutableList<DB_Collection_With_Meta>,
    private var uiFontSizeBig: Boolean
) :
    RecyclerView.Adapter<AdapterCollections.ViewHolder>() {

    class ViewHolder(val binding: RecViewCollectionOrPackBinding) :
        RecyclerView.ViewHolder(binding.root)

    private val stringTools = StringTools()

    fun updateSettingsAndContent(
        collectionListNew: List<DB_Collection_With_Meta>,
        uiFontSizeBig: Boolean
    ) {
        val updateAllContent = this.uiFontSizeBig != uiFontSizeBig
        this.uiFontSizeBig = uiFontSizeBig
        updateContent(collectionListNew, updateAllContent)
    }

    private fun updateContent(
        collectionListNew: List<DB_Collection_With_Meta>,
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
        if (uiFontSizeBig) {
            viewHolder.binding.recCollectionsName.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                context.resources.getDimension(R.dimen.rec_view_font_size_big)
            )
            viewHolder.binding.recCollectionsDesc.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                context.resources.getDimension(R.dimen.rec_view_font_size_below_big)
            )
        } else {
            viewHolder.binding.recCollectionsName.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                context.resources.getDimension(R.dimen.rec_view_font_size_default)
            )
            viewHolder.binding.recCollectionsDesc.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                context.resources.getDimension(R.dimen.rec_view_font_size_below_default)
            )
        }
        val backgroundLayerList =
            viewHolder.binding.root.background as LayerDrawable
        val background =
            backgroundLayerList.findDrawableByLayerId(R.id.rec_view_collection_or_pack_background_main) as GradientDrawable
        val backgroundBehind =
            backgroundLayerList.findDrawableByLayerId(R.id.rec_view_collection_or_pack_background_behind) as GradientDrawable
        viewHolder.binding.recCollectionsName.textAlignment = View.TEXT_ALIGNMENT_INHERIT
        viewHolder.binding.recCollectionsPreviewText.visibility = View.VISIBLE
        viewHolder.binding.recCollectionsNumberText.visibility = View.VISIBLE
        when (position) {
            0 if collections.size == 1 -> {
                val welcomeText =
                    SpannableString(context.resources.getString(R.string.welcome_collection))
                val welcomeTextDrawableAdd = ContextCompat.getDrawable(
                    context,
                    R.drawable.outline_add_24
                )
                welcomeTextDrawableAdd?.setTint(context.getColor(R.color.light_black))
                welcomeTextDrawableAdd?.setBounds(
                    0,
                    0,
                    welcomeTextDrawableAdd.intrinsicWidth,
                    welcomeTextDrawableAdd.intrinsicHeight
                )
                val welcomeTextImageAdd =
                    welcomeTextDrawableAdd?.let { ImageSpan(it, ImageSpan.ALIGN_BOTTOM) }
                val indexAdd = welcomeText.indexOf("+")
                welcomeText.setSpan(
                    welcomeTextImageAdd,
                    indexAdd,
                    indexAdd + 1,
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                )
                val welcomeTextDrawableImport =
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.outline_file_download_24
                    )
                welcomeTextDrawableImport?.setTint(
                    context.getColor(
                        R.color.light_black
                    )
                )
                welcomeTextDrawableImport?.setBounds(
                    0,
                    0,
                    welcomeTextDrawableImport.intrinsicWidth,
                    welcomeTextDrawableImport.intrinsicHeight
                )
                val welcomeTextImageImport =
                    welcomeTextDrawableImport?.let { ImageSpan(it, ImageSpan.ALIGN_BOTTOM) }
                val indexImport = welcomeText.indexOf("↓")
                welcomeText.setSpan(
                    welcomeTextImageImport,
                    indexImport,
                    indexImport + 1,
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                )
                viewHolder.binding.recCollectionsName.text = welcomeText
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
            }

            0 -> {
                viewHolder.binding.root.setOnClickListener {
                    val intent = Intent(
                        context,
                        ListPacks::class.java
                    )
                    intent.putExtra("collection", LIST_CARDS_GET_DB_COLLECTIONS_ALL)
                    context.startActivity(intent)
                }
                viewHolder.binding.recCollectionsName.text =
                    context.resources.getString(R.string.all_packs)
                viewHolder.binding.recCollectionsDesc.visibility = View.VISIBLE
                viewHolder.binding.recCollectionsDesc.text =
                    viewHolder.binding.recCollectionsDesc.resources.getString(R.string.all_packs_desc)
                viewHolder.binding.recCollectionsPreviewText.text = "…"
                if (collections[position].counter != -1) {
                    viewHolder.binding.recCollectionsNumberText.visibility = View.VISIBLE
                    viewHolder.binding.recCollectionsNumberText.text =
                        collections[position].counter.toString()
                    viewHolder.binding.recCollectionsNumberText.contentDescription = String.format(
                        Locale.ROOT, "%d %s",
                        collections[position].counter,
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
            }

            else -> {
                val currentCollection = collections[position].collection
                val extra = currentCollection.uid
                viewHolder.binding.root.setOnClickListener {
                    val intent = Intent(
                        context,
                        ListPacks::class.java
                    )
                    intent.putExtra("collection", extra)
                    context.startActivity(intent)
                }
                viewHolder.binding.recCollectionsName.text =
                    stringTools.shorten(currentCollection.name)
                if (currentCollection.desc.isNullOrEmpty()) {
                    viewHolder.binding.recCollectionsDesc.visibility = View.GONE
                } else {
                    viewHolder.binding.recCollectionsDesc.visibility = View.VISIBLE
                    viewHolder.binding.recCollectionsDesc.text = stringTools
                        .shorten(currentCollection.desc)
                }
                val emojiText = currentCollection.emoji
                viewHolder.binding.recCollectionsPreviewText.text =
                    if (emojiText.isNullOrEmpty()) {
                        val pattern = Regex("^(\\P{M}\\p{M}*+).*")
                        currentCollection.name.replace(pattern, "$1")
                    } else {
                        emojiText
                    }
                if (collections[position].counter != -1) {
                    viewHolder.binding.recCollectionsNumberText.visibility = View.VISIBLE
                    viewHolder.binding.recCollectionsNumberText.text =
                        collections[position].counter.toString()
                    viewHolder.binding.recCollectionsNumberText.contentDescription = String.format(
                        Locale.ROOT, "%d %s",
                        collections[position].counter,
                        context.resources.getString(R.string.items)
                    )
                } else {
                    viewHolder.binding.recCollectionsNumberText.visibility = View.GONE
                }
                val colors =
                    context.resources.obtainTypedArray(R.array.pack_color_list)
                val colorsBackground =
                    context.resources.obtainTypedArray(R.array.pack_color_background_light)
                val colorsBackgroundAlpha =
                    context.resources.obtainTypedArray(R.array.pack_color_background_light_alpha)
                val minimalLength = colors.length().coerceAtMost(colorsBackground.length())
                    .coerceAtMost(colorsBackgroundAlpha.length())
                val color = currentCollection.colors
                if (color in 0..<minimalLength) {
                    viewHolder.binding.recCollectionsName.setTextColor(colors.getColor(color, 0))
                    viewHolder.binding.recCollectionsPreviewText.setTextColor(
                        colors.getColor(
                            color,
                            0
                        )
                    )
                    viewHolder.binding.recCollectionsPreviewText.setBackgroundColor(
                        colorsBackground.getColor(
                            color,
                            0
                        )
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
    }

    override fun getItemCount(): Int {
        return collections.size
    }
}
