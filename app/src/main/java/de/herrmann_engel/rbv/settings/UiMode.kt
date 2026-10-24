package de.herrmann_engel.rbv.settings

/**
 * Theme setting: follow the system, always day or always night. value is stored in the settings and
 * must not change.
 */
enum class UiMode(val value: Int) {
    AUTO(0),
    DAY(1),
    NIGHT(2);

    companion object {
        // Unknown values (e.g. from a newer app version) count as AUTO
        fun fromValue(value: Int): UiMode =
            entries.firstOrNull { it.value == value } ?: AUTO
    }
}
