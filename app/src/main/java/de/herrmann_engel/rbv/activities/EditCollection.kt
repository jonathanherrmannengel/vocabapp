package de.herrmann_engel.rbv.activities

import android.app.Dialog
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.view.Menu
import android.view.View.OnFocusChangeListener
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.EmojiTheming
import com.vanniktech.emoji.inputfilters.OnlyEmojisInputFilter
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.databinding.ActivityEditCollectionOrPackBinding
import de.herrmann_engel.rbv.databinding.DiaConfirmBinding
import de.herrmann_engel.rbv.db.DB_Collection
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get
import de.herrmann_engel.rbv.db.utils.DB_Helper_Update
import de.herrmann_engel.rbv.utils.StringTools
import kotlin.math.roundToInt

class EditCollection : RBVActivity() {
    private lateinit var binding: ActivityEditCollectionOrPackBinding
    private lateinit var collection: DB_Collection
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditCollectionOrPackBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val dbHelperGet = DB_Helper_Get(this)
        binding.editCollectionOrPackDescLayout.hint =
            String.format(getString(R.string.optional), getString(R.string.collection_or_pack_desc))
        val emojiPopup = EmojiPopup(
            binding.root, binding.editCollectionOrPackEmoji,
            EmojiTheming(
                ContextCompat.getColor(this, R.color.light_grey_default),
                ContextCompat.getColor(this, R.color.light_black),
                ContextCompat.getColor(this, R.color.highlight),
                ContextCompat.getColor(this, R.color.button),
                ContextCompat.getColor(this, R.color.light_black),
                ContextCompat.getColor(this, R.color.dark_grey)
            )
        )
        binding.editCollectionOrPackEmoji.filters = arrayOf<InputFilter>(OnlyEmojisInputFilter())
        binding.editCollectionOrPackEmoji.onFocusChangeListener =
            OnFocusChangeListener { _, hasFocus: Boolean ->
                if (hasFocus) {
                    if (!emojiPopup.isShowing) {
                        emojiPopup.show()
                    }
                } else if (emojiPopup.isShowing) {
                    emojiPopup.dismiss()
                }
            }
        binding.editCollectionOrPackEmoji.setOnClickListener {
            if (!emojiPopup.isShowing) {
                emojiPopup.show()
            }
        }
        binding.editCollectionOrPackEmojiLayout.hint = String.format(
            getString(R.string.optional),
            getString(R.string.collection_or_pack_emoji)
        )
        val collectionNo = intent.extras!!.getInt("collection")
        collection = dbHelperGet.getSingleCollection(collectionNo)
        binding.editCollectionOrPackName.setText(collection.name)
        binding.editCollectionOrPackDesc.setText(collection.desc)
        binding.editCollectionOrPackEmoji.setText(collection.emoji)
        binding.editCollectionOrPackEmoji.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                binding.editCollectionOrPackEmoji.removeTextChangedListener(this)
                binding.editCollectionOrPackEmoji.setText(s.subSequence(start, start + count))
                binding.editCollectionOrPackEmoji.addTextChangedListener(this)
            }

            override fun afterTextChanged(s: Editable) {}
        })
        val colorNames = resources.obtainTypedArray(R.array.pack_color_names)
        val colors = resources.obtainTypedArray(R.array.pack_color_main)
        val colorsStatusBar = resources.obtainTypedArray(R.array.pack_color_statusbar)
        val colorsBackground = resources.obtainTypedArray(R.array.pack_color_background)
        val minimalLength =
            colorNames.length().coerceAtMost(colors.length()).coerceAtMost(colorsStatusBar.length())
                .coerceAtMost(colorsBackground.length())
        var i = 0
        while (i < minimalLength) {
            val colorName = colorNames.getString(i)
            val color = colors.getColor(i, 0)
            val colorStatusBar = colorsStatusBar.getColor(i, 0)
            val colorBackground = colorsBackground.getColor(i, 0)
            if (collection.colors == i) {
                setColors(color, colorStatusBar, colorBackground)
            }
            val colorView = ImageButton(this)
            colorView.setImageDrawable(color.toDrawable())
            val margin =
                (10 * (resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
            val lp = LinearLayout.LayoutParams(margin * 3, margin * 3)
            lp.setMargins(margin, margin, margin, margin)
            colorView.layoutParams = lp
            colorView.setPadding(0, 0, 0, 0)
            val finalI = i
            colorView.setOnClickListener {
                setColors(color, colorStatusBar, colorBackground)
                collection.colors = finalI
            }
            colorView.contentDescription = colorName
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                colorView.tooltipText = colorName
            }
            binding.colorPicker.addView(colorView)
            i++
        }
        colorNames.recycle()
        colors.recycle()
        colorsStatusBar.recycle()
        colorsBackground.recycle()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val name = binding.editCollectionOrPackName.text.toString()
                val desc = binding.editCollectionOrPackDesc.text.toString()
                if (collection.name == name && collection.desc == desc) {
                    finish()
                } else {
                    val confirmCancelDialog = Dialog(this@EditCollection, R.style.dia_view)
                    val bindingConfirmCancelDialog = DiaConfirmBinding.inflate(layoutInflater)
                    confirmCancelDialog.setContentView(bindingConfirmCancelDialog.root)
                    confirmCancelDialog.setTitle(resources.getString(R.string.discard_changes))
                    confirmCancelDialog.window!!.setLayout(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT
                    )
                    bindingConfirmCancelDialog.diaConfirmYes.setOnClickListener { finish() }
                    bindingConfirmCancelDialog.diaConfirmNo.setOnClickListener { confirmCancelDialog.dismiss() }
                    confirmCancelDialog.show()
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_edit, menu)
        menu.findItem(R.id.menu_edit_save).setOnMenuItemClickListener {
            collection.name = binding.editCollectionOrPackName.text.toString()
            collection.desc = binding.editCollectionOrPackDesc.text.toString()
            if (binding.editCollectionOrPackEmoji.text != null) {
                collection.emoji =
                    StringTools().firstEmoji(binding.editCollectionOrPackEmoji.text.toString())
            } else {
                collection.emoji = null
            }
            val dbHelperUpdate = DB_Helper_Update(this)
            if (dbHelperUpdate.updateCollection(collection)) {
                finish()
            } else {
                Toast.makeText(this, R.string.error_values, Toast.LENGTH_SHORT).show()
            }
            return@setOnMenuItemClickListener true
        }
        return true
    }

    private fun setColors(main: Int, statusBar: Int, background: Int) {
        supportActionBar?.setBackgroundDrawable(statusBar.toDrawable())
        binding.editCollectionOrPackNameLayout.boxStrokeColor = main
        binding.editCollectionOrPackNameLayout.hintTextColor = ColorStateList.valueOf(main)
        binding.editCollectionOrPackDescLayout.boxStrokeColor = main
        binding.editCollectionOrPackDescLayout.hintTextColor = ColorStateList.valueOf(main)
        binding.editCollectionOrPackEmojiLayout.boxStrokeColor = main
        binding.editCollectionOrPackEmojiLayout.hintTextColor = ColorStateList.valueOf(main)
        binding.root.setBackgroundColor(background)
    }
}
