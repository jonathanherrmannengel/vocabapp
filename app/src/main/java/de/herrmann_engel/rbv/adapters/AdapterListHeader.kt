package de.herrmann_engel.rbv.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.databinding.RecViewCollectionOrPackBinding

/**
 * First row of the collection and pack lists: an entry for everything in the list (e.g. all
 * cards of the shown packs) with the number of entries, or a welcome message while the list is
 * empty. The list itself is shown by a second adapter below (see ConcatAdapter).
 */
class AdapterListHeader(
    private val name: String,
    private val desc: String,
    private val welcomeText: (Context) -> CharSequence,
    private val onClick: () -> Unit,
    private var counter: Int,
    private var listEmpty: Boolean,
    private var uiFontSizeBig: Boolean
) : RecyclerView.Adapter<AdapterListHeader.ViewHolder>() {
    class ViewHolder(val binding: RecViewCollectionOrPackBinding) :
        RecyclerView.ViewHolder(binding.root)

    // Redraws the row if one of the values has changed
    fun update(counter: Int, listEmpty: Boolean, uiFontSizeBig: Boolean) {
        if (counter == this.counter && listEmpty == this.listEmpty &&
            uiFontSizeBig == this.uiFontSizeBig
        ) {
            return
        }
        this.counter = counter
        this.listEmpty = listEmpty
        this.uiFontSizeBig = uiFontSizeBig
        notifyItemChanged(0)
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
        val binding = viewHolder.binding
        val context = binding.root.context
        binding.applyFontSize(uiFontSizeBig)
        val (background, backgroundBehind) = binding.backgroundLayers()
        background.mutate()
        backgroundBehind.mutate()
        binding.recCollectionsName.setTextColor(
            ContextCompat.getColor(context, R.color.default_text)
        )
        if (listEmpty) {
            binding.root.setOnClickListener(null)
            binding.root.isClickable = false
            binding.recCollectionsName.text = welcomeText(context)
            binding.recCollectionsName.textAlignment = View.TEXT_ALIGNMENT_CENTER
            binding.recCollectionsDesc.visibility = View.GONE
            binding.recCollectionsPreviewText.visibility = View.GONE
            binding.recCollectionsNumberText.visibility = View.GONE
            background.setStroke(0, Color.rgb(0, 0, 0))
            background.setColor(Color.argb(0, 0, 0, 0))
            backgroundBehind.setStroke(0, Color.rgb(0, 0, 0))
            return
        }
        binding.root.setOnClickListener { onClick() }
        binding.recCollectionsName.text = name
        binding.recCollectionsName.textAlignment = View.TEXT_ALIGNMENT_INHERIT
        binding.recCollectionsDesc.visibility = View.VISIBLE
        binding.recCollectionsDesc.text = desc
        binding.recCollectionsPreviewText.visibility = View.VISIBLE
        binding.recCollectionsPreviewText.text = "…"
        binding.recCollectionsPreviewText.setTextColor(
            ContextCompat.getColor(context, R.color.default_text)
        )
        binding.recCollectionsPreviewText.setBackgroundColor(
            ContextCompat.getColor(context, R.color.pack_default_item_background)
        )
        binding.recCollectionsNumberText.visibility = View.VISIBLE
        binding.showCounter(counter)
        background.setStroke(2, ContextCompat.getColor(context, R.color.pack_default_item_stroke))
        background.setColor(
            ContextCompat.getColor(context, R.color.pack_default_item_background_alpha)
        )
        backgroundBehind.setStroke(
            1,
            ContextCompat.getColor(context, R.color.pack_default_item_background)
        )
    }

    override fun getItemCount(): Int = 1
}
