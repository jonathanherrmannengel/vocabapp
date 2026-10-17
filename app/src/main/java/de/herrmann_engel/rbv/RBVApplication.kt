package de.herrmann_engel.rbv

import android.app.Application
import com.vanniktech.emoji.EmojiManager.install
import com.vanniktech.emoji.twitter.TwitterEmojiProvider
import de.herrmann_engel.rbv.settings.AppSettings
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
        AppSettings(this).applyUiMode()
    }
}
