package de.herrmann_engel.rbv.language

import android.content.Context
import android.content.res.Resources
import java.util.Locale

/**
 * The language tag in its standard form (e.g. "iw" becomes "he"), or null if it is empty or names
 * no language; used for tags from backups and stored choices.
 */
fun normalizeLanguageTag(tag: String?): String? {
    if (tag.isNullOrBlank()) {
        return null
    }
    val locale = Locale.forLanguageTag(tag.trim())
    return if (locale.language.isEmpty()) null else locale.toLanguageTag()
}

// The language of a stored tag, null if there is none
fun languageOf(tag: String?): Locale? = normalizeLanguageTag(tag)?.let { Locale.forLanguageTag(it) }

// The language the app is shown in; may differ from the device languages
fun appLocale(context: Context): Locale = context.resources.configuration.locales[0]

// The languages of the device (ignoring the app's own language setting), most preferred first
fun deviceLocales(): List<Locale> = Resources.getSystem().configuration.locales.let { locales ->
    List(locales.size()) { locales[it] }
}

// Name of the language in the app's language (e.g. "French" or "Französisch")
fun languageName(context: Context, locale: Locale): String =
    locale.getDisplayName(appLocale(context))

// Name of the language in the language itself (e.g. "français")
fun ownLanguageName(locale: Locale): String = locale.getDisplayName(locale)
