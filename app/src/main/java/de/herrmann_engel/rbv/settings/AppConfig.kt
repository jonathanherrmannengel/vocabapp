package de.herrmann_engel.rbv.settings

import android.content.Context
import de.herrmann_engel.rbv.Globals

/**
 * Values the app stores for itself, not changeable in the settings screen and not included in
 * backups.
 */
class AppConfig(context: Context) : Preferences(context, Globals.CONFIG_NAME) {

    // URI of the media folder chosen by the user, null if none is chosen yet
    var mediaFolder by MEDIA_FOLDER

    // Whether the warning about inaccurate search and formatting in large card lists still has
    // to be shown, separately for lists with and without formatting
    var showInaccurateWarningFormat by SHOW_INACCURATE_WARNING_FORMAT
    var showInaccurateWarningNoFormat by SHOW_INACCURATE_WARNING_NO_FORMAT

    companion object {
        private val MEDIA_FOLDER = StringValue("card_media_folder", null)
        private val SHOW_INACCURATE_WARNING_FORMAT = BooleanValue("inaccurate_warning_format", true)
        private val SHOW_INACCURATE_WARNING_NO_FORMAT =
            BooleanValue("inaccurate_warning_no_format", true)
    }
}
