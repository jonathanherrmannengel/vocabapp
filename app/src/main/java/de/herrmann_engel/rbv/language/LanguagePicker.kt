package de.herrmann_engel.rbv.language

import android.app.Activity
import android.text.Editable
import android.text.TextWatcher
import androidx.recyclerview.widget.LinearLayoutManager
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.adapters.AdapterLanguages
import de.herrmann_engel.rbv.databinding.DiaLanguagesBinding
import de.herrmann_engel.rbv.db.utils.DbHelperGet
import de.herrmann_engel.rbv.utils.createDialog
import java.text.Collator
import java.util.Locale

/**
 * Dialog to choose the language of the front or back sides of a pack's or collection's cards. The
 * list starts with the entry for no chosen language, followed by the device languages, the
 * languages already chosen for other packs or collections, and all other languages; each group is
 * sorted by name. A search field filters the languages by name, own name or code.
 */
class LanguagePicker(private val activity: Activity) {
    private val appLocale = appLocale(activity)

    /**
     * Shows the dialog; onPick gets the chosen language tag, or null for the first entry (named
     * noneName, described by noneDesc).
     */
    fun show(
        title: String,
        noneName: String,
        noneDesc: String,
        selectedTag: String?,
        onPick: (String?) -> Unit
    ) {
        val binding = DiaLanguagesBinding.inflate(activity.layoutInflater)
        val dialog = createDialog(activity, binding.root, title)
        val adapter = AdapterLanguages(entries(noneName, noneDesc), selectedTag) {
            dialog.dismiss()
            onPick(it)
        }
        binding.diaLanguagesList.adapter = adapter
        binding.diaLanguagesList.layoutManager = LinearLayoutManager(activity)
        binding.diaLanguagesSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                adapter.filter(s.toString().lowercase(appLocale))
                // Each search (and its end) shows the list from the start
                binding.diaLanguagesList.scrollToPosition(0)
            }
        })
        dialog.show()
    }

    private fun entries(noneName: String, noneDesc: String): List<AdapterLanguages.Entry> {
        val deviceTags = deviceLocales().mapNotNullTo(LinkedHashSet()) {
            normalizeLanguageTag(it.language)
        }
        val usedTags = DbHelperGet(activity).getUsedLanguageTags()
            .mapNotNullTo(HashSet()) { normalizeLanguageTag(it) } - deviceTags
        // Only the two-letter codes (ISO 639-1) of the common languages; newer Android versions
        // also list several hundred rare languages with three-letter codes. Old codes (e.g. "iw")
        // become the current ones (e.g. "he"), which removes duplicates.
        val otherTags = Locale.getISOLanguages()
            .filter { it.length == 2 }
            .mapNotNullTo(HashSet()) { normalizeLanguageTag(it) } - deviceTags - usedTags
        val groups = listOf(deviceTags, usedTags, otherTags)
            .filter { it.isNotEmpty() }
            .map { group(it) }
        val none = AdapterLanguages.Entry(null, noneName, noneDesc, "", "", groups.isNotEmpty())
        return listOf(none) + groups.flatMapIndexed { index, group ->
            // A separator below each group except the last
            if (index < groups.size - 1) group.dropLast(1) + group.last().copy(endsGroup = true)
            else group
        }
    }

    // Entries of the languages, sorted by name in the app's language
    private fun group(tags: Collection<String>): List<AdapterLanguages.Entry> {
        val collator = Collator.getInstance(appLocale)
        return tags.map { tag ->
            val locale = Locale.forLanguageTag(tag)
            val name = languageName(activity, locale)
            val ownName = ownLanguageName(locale)
            // Without a name in the language itself, Android returns the name in the app's
            // language; the own name is marked with its language, so that screen readers
            // pronounce it
            val (desc, code) = if (ownName == name) {
                "" to tag
            } else {
                ownName.inLanguage(locale) to
                    activity.getString(R.string.language_code_after_own_name, tag)
            }
            val search = listOf(name, ownName, tag).joinToString(" ").lowercase(appLocale)
            AdapterLanguages.Entry(tag, name, desc, code, search, false)
        }.sortedWith(compareBy(collator) { it.name })
    }
}
