package de.herrmann_engel.rbv

import android.app.Application
import com.vanniktech.emoji.EmojiManager.install
import com.vanniktech.emoji.twitter.TwitterEmojiProvider
import de.herrmann_engel.rbv.settings.AppSettings

/**
 * Application entry point: sets up the emoji provider and applies the theme (day/night) setting.
 */
class RBVApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        install(TwitterEmojiProvider())
        AppSettings(this).applyUiMode()
    }
}
