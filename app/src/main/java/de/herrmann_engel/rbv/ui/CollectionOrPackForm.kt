package de.herrmann_engel.rbv.ui

import android.content.res.ColorStateList
import android.os.Build
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.View.OnFocusChangeListener
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.EmojiTheming
import com.vanniktech.emoji.inputfilters.OnlyEmojisInputFilter
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.databinding.ActivityCollectionOrPackFormBinding
import de.herrmann_engel.rbv.db.DbCollection
import de.herrmann_engel.rbv.language.LanguagePicker
import de.herrmann_engel.rbv.language.collectionLanguageText
import de.herrmann_engel.rbv.language.languageOf
import de.herrmann_engel.rbv.language.packLanguageText
import de.herrmann_engel.rbv.utils.StringTools
import de.herrmann_engel.rbv.utils.showConfirmDialog
import kotlin.math.roundToInt

/**
 * Form for the name, description, emoji, languages of the cards' sides and color of a collection
 * or pack, shared by the screens that create and edit them. Shows the given initial values and
 * asks before discarding changes when the user leaves the screen.
 *
 * @param nameOf R.string.collection_name or R.string.pack_name, used in the hint of the name field
 * @param collection for a pack, the collection it belongs to, whose languages apply to sides
 *                   without a chosen language; null for a collection
 */
class CollectionOrPackForm(
    private val activity: ComponentActivity,
    private val binding: ActivityCollectionOrPackFormBinding,
    @StringRes nameOf: Int,
    private val collection: DbCollection?,
    initial: Values
) {
    // The values of a collection or pack as edited in the form; languages are tags or null
    data class Values(
        val name: String,
        val desc: String,
        val emoji: String,
        val colors: Int,
        val frontLanguage: String?,
        val backLanguage: String?
    )

    private var colors = initial.colors

    // Language tags chosen for the front and back sides, null if none
    var frontLanguage = initial.frontLanguage
        private set

    var backLanguage = initial.backLanguage
        private set

    // The values as shown when the form was opened; leaving the form asks only if they changed
    private val shown: Values

    val name: String
        get() = binding.collectionOrPackName.text.toString()

    val desc: String
        get() = binding.collectionOrPackDesc.text.toString()

    // The single emoji entered, empty if there is none
    val emoji: String
        get() = StringTools.firstCharacter(binding.collectionOrPackEmoji.text.toString())

    // Index of the selected color (see R.array.pack_color_main)
    val selectedColors: Int
        get() = colors

    init {
        binding.collectionOrPackNameLayout.hint = activity.getString(
            R.string.collection_or_pack_name_format,
            activity.getString(nameOf),
            activity.getString(R.string.collection_or_pack_name)
        )
        binding.collectionOrPackDescLayout.hint = activity.getString(
            R.string.optional,
            activity.getString(R.string.collection_or_pack_desc)
        )
        binding.collectionOrPackEmojiLayout.hint = activity.getString(
            R.string.optional,
            activity.getString(R.string.collection_or_pack_emoji)
        )
        // Set before the text, so that a stored value that is not an emoji is not shown
        binding.collectionOrPackEmoji.filters = arrayOf<InputFilter>(OnlyEmojisInputFilter())
        binding.collectionOrPackName.setText(initial.name)
        binding.collectionOrPackDesc.setText(initial.desc)
        binding.collectionOrPackEmoji.setText(initial.emoji)
        setUpEmojiField()
        setUpLanguageFields()
        setUpColorPicker()
        shown = currentValues()
        // Ask before discarding changes
        activity.onBackPressedDispatcher.addCallback(activity, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (hasChanges()) {
                    showConfirmDialog(activity, activity.getString(R.string.discard_changes)) {
                        activity.finish()
                    }
                } else {
                    activity.finish()
                }
            }
        })
    }

    private fun currentValues() = Values(
        name,
        desc,
        binding.collectionOrPackEmoji.text.toString(),
        colors,
        frontLanguage,
        backLanguage
    )

    private fun hasChanges(): Boolean = currentValues() != shown

    // Emojis are entered via an emoji keyboard popup; the field keeps a single emoji
    private fun setUpEmojiField() {
        val emojiField = binding.collectionOrPackEmoji
        val emojiPopup = EmojiPopup(
            binding.root, emojiField,
            EmojiTheming(
                ContextCompat.getColor(activity, R.color.light_grey_default),
                ContextCompat.getColor(activity, R.color.light_black),
                ContextCompat.getColor(activity, R.color.highlight),
                ContextCompat.getColor(activity, R.color.button),
                ContextCompat.getColor(activity, R.color.light_black),
                ContextCompat.getColor(activity, R.color.dark_grey)
            )
        )
        emojiField.onFocusChangeListener = OnFocusChangeListener { _, hasFocus: Boolean ->
            if (hasFocus) {
                if (!emojiPopup.isShowing) {
                    emojiPopup.show()
                }
            } else if (emojiPopup.isShowing) {
                emojiPopup.dismiss()
            }
        }
        emojiField.setOnClickListener {
            if (!emojiPopup.isShowing) {
                emojiPopup.show()
            }
        }
        // Keep only the newly entered emoji
        emojiField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                emojiField.removeTextChangedListener(this)
                emojiField.setText(s.subSequence(start, start + count))
                emojiField.addTextChangedListener(this)
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    // The language fields show the chosen languages; a tap opens the language picker
    private fun setUpLanguageFields() {
        for (back in listOf(false, true)) {
            showLanguage(back)
            languageField(back).setOnClickListener { pickLanguage(back) }
            languageLayout(back).setEndIconOnClickListener { pickLanguage(back) }
        }
    }

    private fun languageLayout(back: Boolean): TextInputLayout = if (back) {
        binding.collectionOrPackBackLanguageLayout
    } else {
        binding.collectionOrPackFrontLanguageLayout
    }

    private fun languageField(back: Boolean): TextInputEditText = if (back) {
        binding.collectionOrPackBackLanguage
    } else {
        binding.collectionOrPackFrontLanguage
    }

    // The language tag chosen for the side, null if none
    private fun chosenLanguage(back: Boolean): String? = if (back) backLanguage else frontLanguage

    // Shows the language chosen for the side; for a pack, the collection's applies without one
    private fun showLanguage(back: Boolean) {
        val tag = chosenLanguage(back)
        languageField(back).setText(
            if (collection == null) {
                collectionLanguageText(activity, tag)
            } else {
                packLanguageText(activity, tag, collectionLanguage(back))
            }
        )
    }

    // Opens the language picker for the side and shows the picked language
    private fun pickLanguage(back: Boolean) {
        val title = activity.getString(
            if (back) R.string.language_back_title else R.string.language_front_title
        )
        // Packs only differ from collections if the collection has a chosen language
        val collectionTag = collectionLanguage(back)
        val noneName: String
        val noneDesc: String
        when {
            languageOf(collectionTag) != null -> {
                noneName = activity.getString(R.string.language_like_collection_title)
                noneDesc = collectionLanguageText(activity, collectionTag)
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                noneName = activity.getString(R.string.language_automatic_title)
                // For a pack it goes without saying
                noneDesc = if (collection == null) {
                    activity.getString(R.string.language_detected_per_pack)
                } else {
                    ""
                }
            }

            else -> {
                noneName = activity.getString(R.string.language_device_title)
                noneDesc = ""
            }
        }
        LanguagePicker(activity).show(title, noneName, noneDesc, chosenLanguage(back)) {
            if (back) backLanguage = it else frontLanguage = it
            showLanguage(back)
        }
    }

    // The language chosen for the side of the pack's collection; null for collections
    private fun collectionLanguage(back: Boolean): String? =
        if (back) collection?.backLanguage else collection?.frontLanguage

    // One button per available color; the selected color is applied to the form
    private fun setUpColorPicker() {
        val resources = activity.resources
        val colorNames = resources.obtainTypedArray(R.array.pack_color_names)
        val colorsMain = resources.obtainTypedArray(R.array.pack_color_main)
        val colorsBackground = resources.obtainTypedArray(R.array.pack_color_background)
        val minimalLength = colorNames.length().coerceAtMost(colorsMain.length())
            .coerceAtMost(colorsBackground.length())
        // 10dp in pixels
        val margin = (10 * resources.displayMetrics.density).roundToInt()
        for (i in 0..<minimalLength) {
            val colorName = colorNames.getString(i)
            val color = colorsMain.getColor(i, 0)
            val colorBackground = colorsBackground.getColor(i, 0)
            if (colors == i) {
                applyColors(color, colorBackground)
            }
            val colorView = ImageButton(activity)
            colorView.setImageDrawable(color.toDrawable())
            val lp = LinearLayout.LayoutParams(margin * 3, margin * 3)
            lp.setMargins(margin, margin, margin, margin)
            colorView.layoutParams = lp
            colorView.setPadding(0, 0, 0, 0)
            colorView.setOnClickListener {
                applyColors(color, colorBackground)
                colors = i
            }
            colorView.contentDescription = colorName
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                colorView.tooltipText = colorName
            }
            binding.collectionOrPackColors.addView(colorView)
        }
        colorNames.recycle()
        colorsMain.recycle()
        colorsBackground.recycle()
    }

    private fun applyColors(main: Int, background: Int) {
        val mainList = ColorStateList.valueOf(main)
        for (layout in listOf(
            binding.collectionOrPackNameLayout,
            binding.collectionOrPackDescLayout,
            binding.collectionOrPackEmojiLayout,
            binding.collectionOrPackFrontLanguageLayout,
            binding.collectionOrPackBackLanguageLayout
        )) {
            layout.boxStrokeColor = main
            layout.hintTextColor = mainList
        }
        binding.root.setBackgroundColor(background)
    }
}
