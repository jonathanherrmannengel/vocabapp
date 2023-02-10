package de.herrmann_engel.rbv.adapters

import android.content.Context
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
import androidx.recyclerview.widget.RecyclerView
import de.herrmann_engel.rbv.Globals
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.activities.ListCards
import de.herrmann_engel.rbv.activities.ListPacks
import de.herrmann_engel.rbv.databinding.RecViewCollectionOrPackBinding
import de.herrmann_engel.rbv.db.DB_Pack
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get
import de.herrmann_engel.rbv.utils.ContextTools
import de.herrmann_engel.rbv.utils.StringTools

class AdapterPacks(
    private val pack: List<DB_Pack>,
    private val collection: Int
) : RecyclerView.Adapter<AdapterPacks.ViewHolder>() {
    class ViewHolder(val binding: RecViewCollectionOrPackBinding) :
        RecyclerView.ViewHolder(binding.root)

    private val stringTools = StringTools()

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding = RecViewCollectionOrPackBinding.inflate(
            LayoutInflater.from(viewGroup.context),
            viewGroup,
            false
        )
        val settings =
            viewGroup.context.getSharedPreferences(Globals.SETTINGS_NAME, Context.MODE_PRIVATE)
        if (settings.getBoolean("ui_font_size", false)) {
            binding.recCollectionsName.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                viewGroup.context.resources.getDimension(R.dimen.rec_view_font_size_big)
            )
            binding.recCollectionsDesc.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                viewGroup.context.resources.getDimension(R.dimen.rec_view_font_size_below_big)
            )
            binding.recCollectionsParent.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                viewGroup.context.resources.getDimension(R.dimen.rec_view_font_size_above_big)
            )
        }
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val context = viewHolder.binding.root.context
        val backgroundLayerList = viewHolder.binding.root.background as LayerDrawable
        val background =
            backgroundLayerList.findDrawableByLayerId(R.id.rec_view_collection_or_pack_background_main) as GradientDrawable
        val backgroundBehind =
            backgroundLayerList.findDrawableByLayerId(R.id.rec_view_collection_or_pack_background_behind) as GradientDrawable
        if (position == 0 && pack.isEmpty()) {
            viewHolder.binding.root.background = null
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
            viewHolder.binding.recCollectionsParent.visibility = View.GONE
            viewHolder.binding.recCollectionsName.textAlignment = View.TEXT_ALIGNMENT_CENTER
            viewHolder.binding.recCollectionsDesc.visibility = View.GONE
            viewHolder.binding.recCollectionsPreviewText.visibility = View.GONE
            viewHolder.binding.recCollectionsNumberText.visibility = View.GONE
        } else if (position == 0) {
            viewHolder.binding.root.setOnClickListener {
                val intent = Intent(context, ListCards::class.java)
                intent.putExtra("pack", -1)
                intent.putExtra("collection", collection)
                context.startActivity(intent)
                (ContextTools().getActivity(context) as ListPacks).finish()
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
            val dbHelperGet =
                DB_Helper_Get(context)
            val size =
                if (collection == -1) {
                    dbHelperGet.countCards()
                } else {
                    dbHelperGet.countCardsInCollection(collection)
                }
            viewHolder.binding.recCollectionsNumberText.text = size.toString()
            viewHolder.binding.recCollectionsParent.visibility = View.GONE
            viewHolder.binding.recCollectionsName.setTextColor(Color.rgb(0, 0, 0))
            viewHolder.binding.recCollectionsPreviewText.setTextColor(Color.rgb(0, 0, 0))
            viewHolder.binding.recCollectionsPreviewText.setBackgroundColor(
                Color.rgb(
                    185,
                    185,
                    185
                )
            )
            background.mutate()
            background.setStroke(2, Color.rgb(85, 85, 85))
            background.setColor(Color.argb(75, 185, 185, 185))
            backgroundBehind.mutate()
            backgroundBehind.setStroke(1, Color.rgb(185, 185, 185))
        } else {
            val extra = pack[position - 1].uid
            viewHolder.binding.root.setOnClickListener {
                val intent = Intent(context, ListCards::class.java)
                intent.putExtra("pack", extra)
                intent.putExtra("collection", collection)
                context.startActivity(intent)
                (ContextTools().getActivity(context) as ListPacks).finish()
            }
            viewHolder.binding.recCollectionsName.text =
                stringTools.shorten(pack[position - 1].name)
            if (pack[position - 1].desc.isEmpty()) {
                viewHolder.binding.recCollectionsDesc.visibility = View.GONE
            } else {
                viewHolder.binding.recCollectionsDesc.visibility = View.VISIBLE
                viewHolder.binding.recCollectionsDesc.text =
                    stringTools.shorten(pack[position - 1].desc)
            }
            val emojiText = pack[position - 1].emoji
            viewHolder.binding.recCollectionsPreviewText.text =
                if (emojiText.isNullOrEmpty()) {
                    val pattern = Regex("^(\\P{M}\\p{M}*+).*")
                    pack[position - 1].name.replace(pattern, "$1")
                } else {
                    emojiText
                }
            val dbHelperGet =
                DB_Helper_Get(context)
            val size = dbHelperGet.countCardsInPack(pack[position - 1].uid)
            viewHolder.binding.recCollectionsNumberText.text = size.toString()
            if (collection == -1) {
                try {
                    val collectionName =
                        stringTools.shorten(
                            DB_Helper_Get(context)
                                .getSingleCollection(
                                    pack[position - 1].collection
                                )
                                .name
                        )
                    viewHolder.binding.recCollectionsParent.visibility = View.VISIBLE
                    viewHolder.binding.recCollectionsParent.text = collectionName
                } catch (e: Exception) {
                    Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show()
                }
            }
            val color = pack[position - 1].colors
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
        return pack.size + 1
    }
}
