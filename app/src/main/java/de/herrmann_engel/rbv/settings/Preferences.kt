package de.herrmann_engel.rbv.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Typed access to a preferences file. Subclasses declare each value once as a property delegated
 * to a [Value], which holds the key and the default value.
 */
abstract class Preferences(context: Context, name: String) {
    private val prefs: SharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE)

    /**
     * A single value. Besides reading and writing it as a property, it can be converted to and
     * from text with its type name, as used for the settings rows of backups.
     */
    sealed class Value<T>(val key: String, val default: T, val typeName: String) :
        ReadWriteProperty<Preferences, T> {

        protected abstract fun read(prefs: SharedPreferences): T
        protected abstract fun write(editor: SharedPreferences.Editor, value: T)

        // Returns null if the text is not a valid value of this type
        protected abstract fun parse(text: String): T?

        override fun getValue(thisRef: Preferences, property: KProperty<*>): T =
            readOrDefault(thisRef.prefs)

        override fun setValue(thisRef: Preferences, property: KProperty<*>, value: T) {
            thisRef.prefs.edit { write(this, value) }
        }

        // True if a value is stored; otherwise reading returns the default
        fun isSet(preferences: Preferences): Boolean = preferences.prefs.contains(key)

        fun toText(preferences: Preferences): String = readOrDefault(preferences.prefs).toString()

        // A value stored with another type (e.g. imported from a broken backup by an old version)
        // is treated as unset
        private fun readOrDefault(prefs: SharedPreferences): T =
            try {
                read(prefs)
            } catch (_: ClassCastException) {
                default
            }

        // Stores the value given as text; text that is not valid for this type is ignored
        fun setFromText(preferences: Preferences, text: String) {
            val value = parse(text) ?: return
            preferences.prefs.edit { write(this, value) }
        }
    }

    class IntValue(key: String, default: Int) : Value<Int>(key, default, "int") {
        override fun read(prefs: SharedPreferences) = prefs.getInt(key, default)
        override fun write(editor: SharedPreferences.Editor, value: Int) {
            editor.putInt(key, value)
        }

        override fun parse(text: String) = text.toIntOrNull()
    }

    class BooleanValue(key: String, default: Boolean) : Value<Boolean>(key, default, "bool") {
        override fun read(prefs: SharedPreferences) = prefs.getBoolean(key, default)
        override fun write(editor: SharedPreferences.Editor, value: Boolean) {
            editor.putBoolean(key, value)
        }

        override fun parse(text: String) = text.toBooleanStrictOrNull()
    }

    class StringValue(key: String, default: String?) : Value<String?>(key, default, "string") {
        override fun read(prefs: SharedPreferences): String? = prefs.getString(key, default)
        override fun write(editor: SharedPreferences.Editor, value: String?) {
            editor.putString(key, value)
        }

        override fun parse(text: String) = text
    }
}
