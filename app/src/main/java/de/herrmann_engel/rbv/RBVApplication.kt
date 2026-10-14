package de.herrmann_engel.rbv

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode
import com.vanniktech.emoji.EmojiManager.install
import com.vanniktech.emoji.twitter.TwitterEmojiProvider
import de.herrmann_engel.rbv.Globals.UI_MODE_DAY
import de.herrmann_engel.rbv.Globals.UI_MODE_NIGHT
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Application entry point: sets up the emoji provider and applies the theme (day/night) setting.
 */
class RBVApplication : Application() {
    // Scope for background work that has to outlive single activities (e.g. import and export)
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        install(TwitterEmojiProvider())
        val settings = getSharedPreferences(Globals.SETTINGS_NAME, MODE_PRIVATE)
        val uiMode = settings.getInt("ui_mode", UI_MODE_DAY)
        setDefaultNightMode(
            when {
                (uiMode == UI_MODE_NIGHT) -> MODE_NIGHT_YES
                (uiMode == UI_MODE_DAY) -> MODE_NIGHT_NO
                else -> MODE_NIGHT_FOLLOW_SYSTEM
            }
        )
    }
}
