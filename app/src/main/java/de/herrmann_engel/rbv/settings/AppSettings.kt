package de.herrmann_engel.rbv.settings

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import de.herrmann_engel.rbv.Globals
import de.herrmann_engel.rbv.cardlist.CardListSide
import de.herrmann_engel.rbv.cardlist.CardSort

/**
 * The settings the user can change in the settings screen. They can be included in backups.
 */
class AppSettings(context: Context) : Preferences(context, Globals.SETTINGS_NAME) {

    // Default sort order of card lists
    var defaultSort: CardSort
        get() = CardSort.fromValue(defaultSortValue)
        set(value) {
            defaultSortValue = value.value
        }

    // Side(s) of the cards shown in card lists
    var flashcardListSide: CardListSide
        get() = CardListSide.fromValue(flashcardListSideValue)
        set(value) {
            flashcardListSideValue = value.value
        }

    // Simple formatting markup on cards (see StringTools)
    var formatCards by FORMAT_CARDS

    // Markdown in card notes
    var formatCardNotes by FORMAT_CARD_NOTES

    // If set, an unknown card in query mode resets its progress to 0 instead of decreasing it by 1
    var queryModeResetProgress by QUERY_MODE_RESET_PROGRESS

    // Background images on the screens
    var uiBackgroundImages by UI_BACKGROUND_IMAGES

    // Larger font size in lists and details
    var uiFontSizeBig by UI_FONT_SIZE_BIG

    // Light, dark or following the system
    var uiMode: UiMode
        get() = UiMode.fromValue(uiModeValue)
        set(value) {
            uiModeValue = value.value
        }

    // Show the media folder in gallery apps (otherwise it contains a .nomedia file)
    var mediaInGallery by MEDIA_IN_GALLERY

    // defaultSort, flashcardListSide and uiMode as stored: numbers, as in backups
    private var defaultSortValue by DEFAULT_SORT
    private var flashcardListSideValue by FLASHCARD_LIST_SIDE
    private var uiModeValue by UI_MODE

    // Applies the uiMode setting to the app
    fun applyUiMode() {
        AppCompatDelegate.setDefaultNightMode(
            when (uiMode) {
                UiMode.NIGHT -> AppCompatDelegate.MODE_NIGHT_YES
                UiMode.DAY -> AppCompatDelegate.MODE_NIGHT_NO
                UiMode.AUTO -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        )
    }

    companion object {
        private val DEFAULT_SORT = IntValue("default_sort", CardSort.DEFAULT.value)
        private val FLASHCARD_LIST_SIDE =
            IntValue("flashcard_list_side", CardListSide.FRONT.value)
        private val FORMAT_CARDS = BooleanValue("format_cards", false)
        private val FORMAT_CARD_NOTES = BooleanValue("format_card_notes", false)
        private val QUERY_MODE_RESET_PROGRESS = BooleanValue("query_mode_reset_progress", false)
        private val UI_BACKGROUND_IMAGES = BooleanValue("ui_bg_images", true)
        private val UI_FONT_SIZE_BIG = BooleanValue("ui_font_size", false)
        private val UI_MODE = IntValue("ui_mode", UiMode.DAY.value)
        private val MEDIA_IN_GALLERY = BooleanValue("media_in_gallery", true)

        // All settings, in the order they are written to backups
        val ALL: List<Value<*>> = listOf(
            DEFAULT_SORT,
            FLASHCARD_LIST_SIDE,
            FORMAT_CARDS,
            FORMAT_CARD_NOTES,
            QUERY_MODE_RESET_PROGRESS,
            UI_BACKGROUND_IMAGES,
            UI_FONT_SIZE_BIG,
            UI_MODE,
            MEDIA_IN_GALLERY
        )
    }
}
