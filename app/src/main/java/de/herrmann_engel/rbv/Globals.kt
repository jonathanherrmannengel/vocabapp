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
    // Theme setting: follow system, always day, always night
    const val UI_MODE_AUTO = 0
    const val UI_MODE_DAY = 1
    const val UI_MODE_NIGHT = 2
    // Result levels of an import
    const val IMPORT_ERROR_LEVEL_OKAY = 0
    const val IMPORT_ERROR_LEVEL_WARN = 1
    const val IMPORT_ERROR_LEVEL_ERROR = 2
    // How the import handles cards that already exist: skip them, import them as duplicates,
    // merge them into existing collections/packs, or import a simple front/back list
    const val IMPORT_MODE_SKIP = 0
    const val IMPORT_MODE_DUPLICATES = 1
    const val IMPORT_MODE_INTEGRATE = 2
    const val IMPORT_MODE_SIMPLE_LIST = 3
    // Sort orders for card lists
    const val SORT_CARDS_DEFAULT = 0
    const val SORT_CARDS_RANDOM = 1
    const val SORT_CARDS_ALPHABETICAL = 2
    const val SORT_CARDS_REPETITION = 3
    const val SORT_CARDS_MIXED = 4
    // Which side(s) of the cards are shown in card lists
    const val FLASHCARD_LIST_SIDE_FRONT = 0
    const val FLASHCARD_LIST_SIDE_BACK = 1
    const val FLASHCARD_LIST_SIDE_BOTH = 2
    // Size limits; e.g. above MAX_SIZE_CARDS_LIST_ACCURATE cards, faster but less accurate formatting and search is used
    const val MAX_SIZE_CARD_IMAGE_PREVIEW = 9
    const val MAX_SIZE_CARD_IMAGE_PRINT = 12
    const val MAX_SIZE_CARDS_LIST_ACCURATE = 15000
    const val MAX_SIZE_CARDS_CONTEXTUAL_MENU_PRINT = 100
    const val MAX_SIZE_CARDS_CONTEXTUAL_MENU_SELECT = 1000
    const val MAX_SIZE_PACKS_CONTEXTUAL_MENU_SELECT = 75
    const val MAX_SIZE_COLLECTIONS_OR_PACKS_LIST_COUNTER = 40
    const val ONLINE_HELP = "https://herrmann-engel.de/projekte/rbv/info/?lang=%s"
}
