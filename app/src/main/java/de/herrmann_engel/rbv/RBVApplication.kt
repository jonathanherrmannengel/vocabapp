package de.herrmann_engel.rbv

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import com.vanniktech.emoji.EmojiManager.install
import com.vanniktech.emoji.twitter.TwitterEmojiProvider

class RBVApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        install(TwitterEmojiProvider())
        val settings = getSharedPreferences(Globals.SETTINGS_NAME, MODE_PRIVATE)
        val uiDarkMode = settings.getBoolean("ui_dark_mode", false)
        if (uiDarkMode) {
            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
        }
    }
}
