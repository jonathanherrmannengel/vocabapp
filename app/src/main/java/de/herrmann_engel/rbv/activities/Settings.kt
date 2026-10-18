package de.herrmann_engel.rbv.activities

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.content.res.AppCompatResources
import de.herrmann_engel.rbv.Globals
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.databinding.ActivitySettingsBinding
import de.herrmann_engel.rbv.databinding.DiaInfoBinding
import de.herrmann_engel.rbv.media.MediaFileManager
import de.herrmann_engel.rbv.settings.AppSettings

/**
 * App settings screen. All changes are saved immediately (see [AppSettings]).
 * Uses a [MediaFileManager] to update the .nomedia file when the gallery setting changes.
 */
class Settings : RBVActivity() {
    private val mediaFileManager = MediaFileManager(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val settings = AppSettings(this)
        // Default sort order of card lists
        binding.settingsSort.clearCheck()
        when (settings.defaultSort) {
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
            settings.defaultSort = sortNew
        }
        // Side(s) of the cards shown in card lists
        binding.settingsFlashcardListSide.clearCheck()
        when (settings.flashcardListSide) {
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
            settings.flashcardListSide = sideNew
        }
        // Simple formatting markup on cards (see StringTools) and Markdown in notes
        val formatCards = settings.formatCards
        binding.settingsFormatCards.isChecked = formatCards
        binding.settingsFormatCards.setOnCheckedChangeListener { _, c: Boolean ->
            settings.formatCards = c
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
        val formatCardNotes = settings.formatCardNotes
        binding.settingsFormatCardNotes.isChecked = formatCardNotes
        binding.settingsFormatCardNotes.setOnCheckedChangeListener { _, c: Boolean ->
            settings.formatCardNotes = c
        }
        // If set, an unknown card in query mode resets its progress to 0 instead of decreasing it by 1
        val queryModeResetProgress = settings.queryModeResetProgress
        binding.settingsQueryModeResetProgress.isChecked = queryModeResetProgress
        binding.settingsQueryModeResetProgress.setOnCheckedChangeListener { _, c: Boolean ->
            settings.queryModeResetProgress = c
        }
        val uiBgImages = settings.uiBackgroundImages
        binding.settingsUiBackgroundImages.isChecked = uiBgImages
        binding.settingsUiBackgroundImages.setOnCheckedChangeListener { _, c: Boolean ->
            settings.uiBackgroundImages = c
            setBgImage(c, binding)
        }
        val uiFontSize = settings.uiFontSizeBig
        binding.settingsUiIncreaseFontSize.isChecked = uiFontSize
        binding.settingsUiIncreaseFontSize.setOnCheckedChangeListener { _, c: Boolean ->
            settings.uiFontSizeBig = c
        }
        // Theme: day, night or follow system; applied immediately
        binding.settingsUiMode.clearCheck()
        when (settings.uiMode) {
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
            settings.uiMode = uiModeNew
            settings.applyUiMode()
        }
        // Show or hide the media folder in gallery apps
        val mediaInGallery = settings.mediaInGallery
        binding.settingsMediaInGallery.isChecked = mediaInGallery
        binding.settingsMediaInGallery.setOnCheckedChangeListener { _, c: Boolean ->
            settings.mediaInGallery = c
            mediaFileManager.handleNoMediaFile()
        }
        setBgImage(settings.uiBackgroundImages, binding)
        setUpEdgeToEdge(binding.settingsContainer)
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

}
