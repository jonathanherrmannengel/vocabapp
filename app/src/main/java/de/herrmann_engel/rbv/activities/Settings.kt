package de.herrmann_engel.rbv.activities

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import de.herrmann_engel.rbv.Globals
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.databinding.ActivitySettingsBinding
import de.herrmann_engel.rbv.databinding.DiaInfoBinding

/**
 * App settings. All changes are saved immediately to the settings preferences.
 * Extends [FileTools] to update the .nomedia file when the gallery setting changes.
 */
class Settings : FileTools() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val settings = getSharedPreferences(Globals.SETTINGS_NAME, MODE_PRIVATE)
        val settingsEdit = settings.edit()
        // Default sort order of card lists
        binding.settingsSort.clearCheck()
        when (settings.getInt("default_sort", Globals.SORT_CARDS_DEFAULT)) {
            Globals.SORT_CARDS_ALPHABETICAL -> {
                binding.settingsSortAlphabetical.isChecked = true
            }

            Globals.SORT_CARDS_RANDOM -> {
                binding.settingsSortRandom.isChecked = true
            }

            Globals.SORT_CARDS_REPETITION -> {
                binding.settingsSortRepetition.isChecked = true
            }

            Globals.SORT_CARDS_MIXED -> {
                binding.settingsSortMixed.isChecked = true
            }

            else -> {
                binding.settingsSortNormal.isChecked = true
            }
        }
        binding.settingsSort.setOnCheckedChangeListener { _, id: Int ->
            val sortNew = when (id) {
                R.id.settings_sort_alphabetical -> {
                    Globals.SORT_CARDS_ALPHABETICAL
                }

                R.id.settings_sort_random -> {
                    Globals.SORT_CARDS_RANDOM
                }

                R.id.settings_sort_repetition -> {
                    Globals.SORT_CARDS_REPETITION
                }

                R.id.settings_sort_mixed -> {
                    Globals.SORT_CARDS_MIXED
                }

                else -> {
                    Globals.SORT_CARDS_DEFAULT
                }
            }
            settingsEdit.putInt("default_sort", sortNew)
            settingsEdit.apply()
        }
        // Side(s) of the cards shown in card lists
        binding.settingsFlashcardListSide.clearCheck()
        when (settings.getInt("flashcard_list_side", Globals.SORT_CARDS_DEFAULT)) {
            Globals.FLASHCARD_LIST_SIDE_BACK -> {
                binding.settingsFlashcardListSideBack.isChecked = true
            }

            Globals.FLASHCARD_LIST_SIDE_BOTH -> {
                binding.settingsFlashcardListSideBoth.isChecked = true
            }

            else -> {
                binding.settingsFlashcardListSideFront.isChecked = true
            }
        }
        binding.settingsFlashcardListSide.setOnCheckedChangeListener { _, id: Int ->
            val sideNew = when (id) {
                R.id.settings_flashcard_list_side_back -> {
                    Globals.FLASHCARD_LIST_SIDE_BACK
                }

                R.id.settings_flashcard_list_side_both -> {
                    Globals.FLASHCARD_LIST_SIDE_BOTH
                }

                else -> {
                    Globals.FLASHCARD_LIST_SIDE_FRONT
                }
            }
            settingsEdit.putInt("flashcard_list_side", sideNew)
            settingsEdit.apply()
        }
        // Simple formatting markup on cards (see StringTools) and Markdown in notes
        val formatCards = settings.getBoolean("format_cards", false)
        binding.settingsFormatCards.isChecked = formatCards
        binding.settingsFormatCards.setOnCheckedChangeListener { _, c: Boolean ->
            settingsEdit.putBoolean("format_cards", c)
            settingsEdit.apply()
        }
        binding.settingsFormatCardsInfo.setOnClickListener {
            val infoDialog = Dialog(this, R.style.dia_view)
            val bindingInfoDialog = DiaInfoBinding.inflate(
                layoutInflater
            )
            infoDialog.setContentView(bindingInfoDialog.root)
            infoDialog.setTitle(resources.getString(R.string.info))
            infoDialog.window!!.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
            bindingInfoDialog.diaInfoText.setText(R.string.settings_format_cards_info)
            infoDialog.show()
        }
        val formatCardNotes = settings.getBoolean("format_card_notes", false)
        binding.settingsFormatCardNotes.isChecked = formatCardNotes
        binding.settingsFormatCardNotes.setOnCheckedChangeListener { _, c: Boolean ->
            settingsEdit.putBoolean("format_card_notes", c)
            settingsEdit.apply()
        }
        // If set, an unknown card in query mode resets its progress to 0 instead of decreasing it by 1
        val queryModeResetProgress = settings.getBoolean("query_mode_reset_progress", false)
        binding.settingsQueryModeResetProgress.isChecked = queryModeResetProgress
        binding.settingsQueryModeResetProgress.setOnCheckedChangeListener { _, c: Boolean ->
            settingsEdit.putBoolean("query_mode_reset_progress", c)
            settingsEdit.apply()
        }
        val uiBgImages = settings.getBoolean("ui_bg_images", true)
        binding.settingsUiBackgroundImages.isChecked = uiBgImages
        binding.settingsUiBackgroundImages.setOnCheckedChangeListener { _, c: Boolean ->
            settingsEdit.putBoolean("ui_bg_images", c)
            settingsEdit.apply()
            setBgImage(c, binding)
        }
        val uiFontSize = settings.getBoolean("ui_font_size", false)
        binding.settingsUiIncreaseFontSize.isChecked = uiFontSize
        binding.settingsUiIncreaseFontSize.setOnCheckedChangeListener { _, c: Boolean ->
            settingsEdit.putBoolean("ui_font_size", c)
            settingsEdit.apply()
        }
        // Theme: day, night or follow system; applied immediately
        binding.settingsUiMode.clearCheck()
        when (settings.getInt("ui_mode", Globals.UI_MODE_DAY)) {
            Globals.UI_MODE_NIGHT -> {
                binding.settingsUiModeNight.isChecked = true
            }

            Globals.UI_MODE_DAY -> {
                binding.settingsUiModeDay.isChecked = true
            }

            else -> {
                binding.settingsUiModeAuto.isChecked = true
            }
        }
        binding.settingsUiMode.setOnCheckedChangeListener { _, id: Int ->
            var uiModeNew = Globals.UI_MODE_AUTO
            if (id == R.id.settings_ui_mode_night) {
                uiModeNew = Globals.UI_MODE_NIGHT
            } else if (id == R.id.settings_ui_mode_day) {
                uiModeNew = Globals.UI_MODE_DAY
            }
            settingsEdit.putInt("ui_mode", uiModeNew)
            settingsEdit.apply()
            when (uiModeNew) {
                Globals.UI_MODE_NIGHT -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }

                Globals.UI_MODE_DAY -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }

                else -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
            }
        }
        // Show or hide the media folder in gallery apps
        val mediaInGallery = settings.getBoolean("media_in_gallery", true)
        binding.settingsMediaInGallery.isChecked = mediaInGallery
        binding.settingsMediaInGallery.setOnCheckedChangeListener { _, c: Boolean ->
            settingsEdit.putBoolean("media_in_gallery", c)
            settingsEdit.apply()
            handleNoMediaFile()
        }
        setBgImage(settings.getBoolean("ui_bg_images", true), binding)
        // Edge-to-edge: keep the content clear of the system bars
        ViewCompat.setOnApplyWindowInsetsListener(binding.settingsContainer) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(insets.left, insets.top, insets.right, insets.bottom)
            WindowInsetsCompat.CONSUMED
        }
        enableEdgeToEdge()
    }

    private fun setBgImage(show: Boolean, binding: ActivitySettingsBinding) {
        if (show) {
            binding.settingsBackgroundImage.visibility = View.VISIBLE
            binding.settingsBackgroundImage.setImageDrawable(
                AppCompatResources.getDrawable(
                    this,
                    R.drawable.bg_settings
                )
            )
        } else {
            binding.settingsBackgroundImage.visibility = View.GONE
        }
    }

    override fun notifyFolderSet() {}
    override fun notifyMissingAction(id: Int) {}
}
