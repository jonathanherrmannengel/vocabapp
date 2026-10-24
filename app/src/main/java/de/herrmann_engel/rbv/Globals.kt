package de.herrmann_engel.rbv

/**
 * App-wide constants.
 */
object Globals {
    const val DB_NAME = BuildConfig.DB_NAME
    const val EXPORT_FILE_NAME = BuildConfig.EXPORT_FILE_NAME
    const val EXPORT_FILE_EXTENSION = "csv"
    const val EXPORT_FILE_TYPE = "text/csv"
    // Names of the shared preferences files: user settings and internal configuration
    const val SETTINGS_NAME = "rbv_settings"
    const val CONFIG_NAME = "rbv_config"
    // Size limits; e.g. above MAX_SIZE_CARDS_LIST_ACCURATE cards, faster but less accurate formatting and search is used
    const val MAX_SIZE_CARD_IMAGE_PREVIEW = 9
    const val MAX_SIZE_CARD_IMAGE_PRINT = 12
    const val MAX_SIZE_CARDS_LIST_ACCURATE = 15000
    const val MAX_SIZE_CARDS_CONTEXTUAL_MENU_PRINT = 100
    const val MAX_SIZE_CARDS_CONTEXTUAL_MENU_SELECT = 1000
    const val MAX_SIZE_PACKS_CONTEXTUAL_MENU_SELECT = 75
    const val ONLINE_HELP = "https://herrmann-engel.de/projekte/rbv/info/?lang=%s"
}
