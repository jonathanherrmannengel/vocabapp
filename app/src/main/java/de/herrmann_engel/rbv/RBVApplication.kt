package de.herrmann_engel.rbv

import android.app.Application
import com.vanniktech.emoji.EmojiManager.install
import com.vanniktech.emoji.twitter.TwitterEmojiProvider

class RBVApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        install(TwitterEmojiProvider())
    }
}
