package de.herrmann_engel.rbv.adapters

import android.app.Dialog
import android.graphics.Color
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.EmojiTheming
import com.vanniktech.emoji.inputfilters.OnlyEmojisInputFilter
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.databinding.DiaEditTagBinding
import de.herrmann_engel.rbv.databinding.RecViewTagsBinding
import de.herrmann_engel.rbv.db.DB_Tag
import de.herrmann_engel.rbv.db.utils.DB_Helper_Delete
import de.herrmann_engel.rbv.db.utils.DB_Helper_Update
import de.herrmann_engel.rbv.utils.ContextTools
import de.herrmann_engel.rbv.utils.StringTools

class AdapterTagLinkCard(
    private val tagList: MutableList<DB_Tag>,
    private val cardNo: Int,
) : RecyclerView.Adapter<AdapterTagLinkCard.ViewHolder>() {
    class ViewHolder(val binding: RecViewTagsBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding = RecViewTagsBinding.inflate(
            LayoutInflater.from(viewGroup.context),
            viewGroup,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val context = viewHolder.binding.root.context
        viewHolder.binding.recTagsDelete.visibility = View.GONE
        viewHolder.binding.recTagsEdit.visibility = View.GONE
        if (tagList.isEmpty()) {
            viewHolder.binding.recTagsName.text = context.getString(R.string.no_tags)
        } else {
            viewHolder.binding.recTagsDelete.visibility = View.VISIBLE
            viewHolder.binding.recTagsEdit.visibility = View.VISIBLE
            val currentTag = tagList[position]
            viewHolder.binding.recTagsName.text = currentTag.name
            viewHolder.binding.recTagsDelete.setOnClickListener {
                val dbHelperDelete = DB_Helper_Delete(context)
                dbHelperDelete.deleteTagLink(currentTag.uid, cardNo)
                tagList.remove(currentTag)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, tagList.size)
            }
            viewHolder.binding.recTagsEdit.setOnClickListener {
                val activity = ContextTools().getActivity(context)
                if (activity != null) {
                    val editDialog = Dialog(context, R.style.dia_view)
                    val bindingEditDialog: DiaEditTagBinding =
                        DiaEditTagBinding.inflate(activity.layoutInflater)
                    editDialog.setContentView(bindingEditDialog.root)
                    editDialog.setTitle(R.string.edit)
                    editDialog.window!!.setLayout(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT
                    )
                    bindingEditDialog.editTagName.setText(currentTag.name)
                    bindingEditDialog.editTagEmoji.setText(currentTag.emoji)
                    if (!currentTag.color.isNullOrBlank()) {
                        try {
                            bindingEditDialog.editTagColor.color =
                                Color.parseColor(currentTag.color)
                        } catch (_: Exception) {
                        }
                    }
                    val emojiPopup = EmojiPopup(
                        bindingEditDialog.root, bindingEditDialog.editTagEmoji,
                        EmojiTheming(
                            ContextCompat.getColor(context, R.color.light_grey_default),
                            ContextCompat.getColor(context, R.color.light_black),
                            ContextCompat.getColor(context, R.color.highlight),
                            ContextCompat.getColor(context, R.color.button),
                            ContextCompat.getColor(context, R.color.light_black),
                            ContextCompat.getColor(context, R.color.dark_grey)
                        )
                    )
                    bindingEditDialog.editTagEmoji.filters = arrayOf<InputFilter>(
                        OnlyEmojisInputFilter()
                    )
                    bindingEditDialog.editTagEmoji.onFocusChangeListener =
                        View.OnFocusChangeListener { _, hasFocus: Boolean ->
                            if (hasFocus) {
                                if (!emojiPopup.isShowing) {
                                    emojiPopup.show()
                                }
                            } else if (emojiPopup.isShowing) {
                                emojiPopup.dismiss()
                            }
                        }
                    bindingEditDialog.editTagEmoji.setOnClickListener {
                        if (!emojiPopup.isShowing) {
                            emojiPopup.show()
                        }
                    }
                    bindingEditDialog.editTagEmoji.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(
                            s: CharSequence,
                            start: Int,
                            count: Int,
                            after: Int
                        ) {
                        }

                        override fun onTextChanged(
                            s: CharSequence,
                            start: Int,
                            before: Int,
                            count: Int
                        ) {
                            bindingEditDialog.editTagEmoji.removeTextChangedListener(this)
                            bindingEditDialog.editTagEmoji.setText(
                                s.subSequence(
                                    start,
                                    start + count
                                )
                            )
                            bindingEditDialog.editTagEmoji.addTextChangedListener(this)
                        }

                        override fun afterTextChanged(s: Editable) {}
                    })
                    var color: Int? = null
                    bindingEditDialog.editTagColor.setOnColorChangeListener { _, newColor ->
                        color = newColor
                    }
                    bindingEditDialog.editTagSave.setOnClickListener {
                        if (!bindingEditDialog.editTagName.text.isNullOrBlank()) {
                            val newTag = DB_Tag()
                            newTag.uid = currentTag.uid
                            newTag.name = bindingEditDialog.editTagName.text.toString()
                            newTag.emoji =
                                StringTools().firstEmoji(bindingEditDialog.editTagEmoji.text.toString())
                            newTag.color =
                                color?.let { it1 -> "#" + Integer.toHexString(it1).substring(2) }
                            val dbHelperUpdate = DB_Helper_Update(context)
                            if (dbHelperUpdate.updateTag(newTag)) {
                                tagList[position] = newTag
                                notifyItemChanged(position)
                                editDialog.dismiss()
                            } else {
                                Toast.makeText(context, R.string.error, Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(context, R.string.error_values, Toast.LENGTH_LONG).show()
                        }
                    }
                    bindingEditDialog.editTagCancel.setOnClickListener {
                        editDialog.dismiss()
                    }
                    editDialog.setCancelable(false)
                    editDialog.setCanceledOnTouchOutside(false)
                    editDialog.show()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return 1.coerceAtLeast(tagList.size)
    }
}
