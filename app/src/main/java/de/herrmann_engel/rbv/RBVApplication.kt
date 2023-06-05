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

class RBVApplication : Application() {
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
