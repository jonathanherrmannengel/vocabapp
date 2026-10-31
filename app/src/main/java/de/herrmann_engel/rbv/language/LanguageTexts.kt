package de.herrmann_engel.rbv.language

import android.content.Context
import android.os.Build
import de.herrmann_engel.rbv.R

// Text for a side without any chosen language: detected on Android 10+, else the device language
private fun noLanguageText(context: Context): String = context.getString(
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        R.string.language_automatic
    } else {
        R.string.language_device
    }
)

// Text for the language chosen for a collection's side (see noLanguageText)
fun collectionLanguageText(context: Context, tag: String?): String =
    languageOf(tag)?.let { languageName(context, it) } ?: noLanguageText(context)

/**
 * Text for the language chosen for a pack's side; without one, the collection's chosen language is
 * used (collectionTag); without that either, the same as for a collection (see noLanguageText).
 */
fun packLanguageText(context: Context, tag: String?, collectionTag: String?): String =
    languageOf(tag)?.let { languageName(context, it) }
        ?: languageOf(collectionTag)?.let {
            context.getString(R.string.language_like_collection, languageName(context, it))
        }
        ?: noLanguageText(context)

// Text for the language actually used for a pack's side (see CardLanguages.resolve), with a note
// where it comes from (e.g. "French (detected)")
fun resolvedLanguageText(context: Context, language: ResolvedLanguage): String {
    // Only without any language (Source.NONE) there is no locale
    val locale = language.locale ?: return context.getString(R.string.language_device)
    val name = languageName(context, locale)
    return when (language.source) {
        ResolvedLanguage.Source.COLLECTION ->
            context.getString(R.string.language_like_collection, name)
        ResolvedLanguage.Source.DETECTED -> context.getString(R.string.language_detected, name)
        // Chosen for the pack itself
        else -> name
    }
}
