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
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.activities.ListCards
import de.herrmann_engel.rbv.adapters.compare.ListPackCompare
import de.herrmann_engel.rbv.databinding.RecViewCollectionOrPackBinding
import de.herrmann_engel.rbv.db.DB_Pack_With_Meta
import de.herrmann_engel.rbv.utils.StringTools

class AdapterPacks(
    private val pack: MutableList<DB_Pack_With_Meta>,
    private val uiFontSizeBig: Boolean,
    private val collection: Int
) : RecyclerView.Adapter<AdapterPacks.ViewHolder>() {
    class ViewHolder(val binding: RecViewCollectionOrPackBinding) :
        RecyclerView.ViewHolder(binding.root)

    private val stringTools = StringTools()

    fun updateContent(
        packListNew: List<DB_Pack_With_Meta>
    ) {
        val diffResult =
            DiffUtil.calculateDiff(
                ListPackCompare(
                    pack,
                    packListNew
                )
            )
        pack.clear()
        pack.addAll(packListNew)
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
        val backgroundLayerList = viewHolder.binding.root.background as LayerDrawable
        val background =
            backgroundLayerList.findDrawableByLayerId(R.id.rec_view_collection_or_pack_background_main) as GradientDrawable
        val backgroundBehind =
            backgroundLayerList.findDrawableByLayerId(R.id.rec_view_collection_or_pack_background_behind) as GradientDrawable
        viewHolder.binding.recCollectionsParent.visibility = View.GONE
        viewHolder.binding.recCollectionsName.textAlignment = View.TEXT_ALIGNMENT_INHERIT
        viewHolder.binding.recCollectionsPreviewText.visibility = View.VISIBLE
        viewHolder.binding.recCollectionsNumberText.visibility = View.VISIBLE
        if (position == 0 && pack.size == 1) {
            if (collection == -1) {
                viewHolder.binding.recCollectionsName.text =
                    context.resources.getString(R.string.welcome_pack)
            } else {
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
                intent.putExtra("pack", -1)
                context.startActivity(intent)
            }
            viewHolder.binding.recCollectionsName.text =
                context.resources.getString(R.string.all_cards)
            viewHolder.binding.recCollectionsDesc.visibility = View.VISIBLE
            viewHolder.binding.recCollectionsDesc.text =
                if (collection == -1) {
                    context.resources.getString(R.string.all_cards_desc)
                } else {
                    context.resources.getString(R.string.all_cards_desc_by_pack)
                }
            viewHolder.binding.recCollectionsPreviewText.text = "…"
            viewHolder.binding.recCollectionsNumberText.text = pack[position].counter.toString()

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
            val currentPack = pack[position].pack
            val extra = currentPack.uid
            viewHolder.binding.root.setOnClickListener {
                val intent = Intent(context, ListCards::class.java)
                intent.putExtra("collection", collection)
                intent.putExtra("pack", extra)
                context.startActivity(intent)
            }
            viewHolder.binding.recCollectionsName.text =
                stringTools.shorten(currentPack.name)
            if (currentPack.desc.isNullOrEmpty()) {
                viewHolder.binding.recCollectionsDesc.visibility = View.GONE
            } else {
                viewHolder.binding.recCollectionsDesc.visibility = View.VISIBLE
                viewHolder.binding.recCollectionsDesc.text =
                    stringTools.shorten(currentPack.desc)
            }
            val emojiText = currentPack.emoji
            viewHolder.binding.recCollectionsPreviewText.text =
                if (emojiText.isNullOrEmpty()) {
                    val pattern = Regex("^(\\P{M}\\p{M}*+).*")
                    currentPack.name.replace(pattern, "$1")
                } else {
                    emojiText
                }
            viewHolder.binding.recCollectionsNumberText.text = pack[position].counter.toString()
            if (collection == -1) {
                try {
                    viewHolder.binding.recCollectionsParent.visibility = View.VISIBLE
                    viewHolder.binding.recCollectionsParent.text = pack[position].collectionName
                } catch (e: Exception) {
                    Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show()
                }
            }
            val color = currentPack.colors
            val colors = context.resources.obtainTypedArray(R.array.pack_color_list)
            val colorsBackground =
                context.resources.obtainTypedArray(R.array.pack_color_background_light)
            val colorsBackgroundAlpha =
                context.resources.obtainTypedArray(R.array.pack_color_background_light_alpha)
            if (color < colors.length() &&
                color < colorsBackground.length() &&
                color < colorsBackgroundAlpha.length() &&
                color >= 0
            ) {
                viewHolder.binding.recCollectionsName.setTextColor(colors.getColor(color, 0))
                viewHolder.binding.recCollectionsPreviewText.setTextColor(colors.getColor(color, 0))
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

    override fun getItemCount(): Int {
        return pack.size
    }
}
