package de.herrmann_engel.rbv.export_import

import android.content.Context
import android.net.Uri
import com.opencsv.CSVReader
import de.herrmann_engel.rbv.Globals
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.db.AppDatabase
import de.herrmann_engel.rbv.db.utils.DB_Helper_Create
import de.herrmann_engel.rbv.db.utils.DB_Helper_Delete
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get
import de.herrmann_engel.rbv.settings.AppSettings
import de.herrmann_engel.rbv.utils.StringTools
import java.io.InputStreamReader
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Imports a CSV file, either in the format written by [AsyncExportWorker] or as a simple list
 * of cards (front, back, optional notes per row).
 *
 * The uids in the file are translated to new uids via [AsyncImportUidConvert], so packs, cards
 * and links end up assigned to the right (newly created or existing) entries. How existing
 * entries are handled depends on mode (see the IMPORT_MODE_* constants in Globals).
 *
 * Columns are looked up by name using the "<type>_schema" rows written by the export. For files
 * without schema rows, the column positions of the current format are used.
 *
 * Rows that cannot be imported are skipped (result "warn"). The rows are written in chunks of
 * ROWS_PER_TRANSACTION rows, each in its own transaction: this is much faster than writing every
 * row on its own, while other database writes (e.g. rating a card while the import is running)
 * only have to wait for the current chunk. If the import fails as a whole, the rows imported
 * until then are kept. If the file cannot be read to the end (e.g. a cut-off file), the rows
 * before the unreadable part are imported (result "warn").
 *
 * A worker imports a single file; its fields hold the state of that import.
 */
class AsyncImportWorker(
    val context: Context,
    private val uri: Uri,
    private val mode: Int,
    private val includeSettings: Boolean,
    private val includeMedia: Boolean
) {

    private var progress = 0
    private var lastProgressSentTime = 0L

    // One of the IMPORT_ERROR_LEVEL_* constants; stays at "error" unless at least one entry was imported
    private var errorLevel = Globals.IMPORT_ERROR_LEVEL_ERROR

    // Column name -> position per row type, from the "<type>_schema" rows
    private val schemas = HashMap<String, Map<String, Int>>()

    private val helperCreate = DB_Helper_Create(context)
    private val helperGet = DB_Helper_Get(context)
    private val helperDelete = DB_Helper_Delete(context)

    // Translation of the uids in the file to the uids in the database, per entry type
    private val collectionUidConverter = AsyncImportUidConvert()
    private val packUidConverter = AsyncImportUidConvert()
    private val cardUidConverter = AsyncImportUidConvert()
    private val mediaUidConverter = AsyncImportUidConvert()
    private val tagsUidConverter = AsyncImportUidConvert()

    // Tags created by this import; those without card in the end are deleted again
    private val createdTags = mutableListOf<Int>()

    // Collection created for a simple list (null if an existing one is used)
    private var simpleListCreatedCollection: Int? = null

    // True if rows of a simple list were skipped because they have the wrong number of columns
    private var simpleListRowsSkipped = false

    // Settings rows of the file; they are only applied once the import has succeeded
    private val importedSettings = ArrayList<Array<String?>>()

    /**
     * Returns the value of the named column of this row, or null if the row has no such column.
     * Without a schema row for this row type, defaultIndex is used.
     */
    private fun Array<String?>.column(name: String, defaultIndex: Int): String? {
        val schema = schemas[this[0]]
        val index = if (schema != null) schema[name] ?: return null else defaultIndex
        return getOrNull(index)
    }

    // Text format of SQLite's DATETIME() (UTC)
    private val sqliteDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT).apply {
        timeZone = TimeZone.getTimeZone("UTC")
        isLenient = false
    }

    /**
     * Parses a date column (Unix time in seconds). Backups of old versions may contain the date of
     * the default collection in the text format of SQLite's DATETIME() instead (written by an old
     * migration), so this format is accepted too. Missing or unknown values become 0, so the row
     * is still imported.
     */
    private fun parseDate(value: String?): Long {
        if (value.isNullOrBlank()) {
            return 0
        }
        value.trim().toLongOrNull()?.let { return it }
        return try {
            (sqliteDateFormat.parse(value.trim())?.time ?: 0L) / 1000L
        } catch (_: ParseException) {
            0
        }
    }

    fun execute(): ImportExportEvents.Result {
        return ImportExportEvents.Result.ImportFinished(execute2())
    }

    // Returns one of the IMPORT_ERROR_LEVEL_* constants
    private fun execute2(): Int {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return Globals.IMPORT_ERROR_LEVEL_ERROR
            CSVReader(InputStreamReader(inputStream)).use { csvReader ->
                importRows(csvReader)
            }
            applySettings()
            return errorLevel
        } catch (e: Exception) {
            e.printStackTrace()
        }
        // The import failed as a whole: rows imported until then are kept, so this is only an
        // error if nothing was imported
        return if (errorLevel == Globals.IMPORT_ERROR_LEVEL_ERROR) {
            Globals.IMPORT_ERROR_LEVEL_ERROR
        } else {
            Globals.IMPORT_ERROR_LEVEL_WARN
        }
    }

    private fun importRows(csvReader: CSVReader) {
        if (mode == Globals.IMPORT_MODE_SIMPLE_LIST) {
            createSimpleListPack()
        }
        try {
            var endOfFile = false
            // True if the rest of the file could not be read (e.g. a cut-off file)
            var readFailed = false
            while (!endOfFile) {
                AppDatabase.getInstance(context).runInTransaction {
                    var rows = 0
                    while (rows < ROWS_PER_TRANSACTION) {
                        val line = try {
                            csvReader.readNext()
                        } catch (e: Exception) {
                            // A read error (e.g. a cut-off file) ends the import. It is caught
                            // here, inside the transaction, so the rows read so far are committed.
                            e.printStackTrace()
                            readFailed = true
                            null
                        }
                        if (line == null) {
                            endOfFile = true
                            break
                        }
                        rows++
                        try {
                            importRow(line)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            errorLevel = Globals.IMPORT_ERROR_LEVEL_WARN
                        }
                        reportProgress()
                    }
                }
            }
            // Only an error if nothing could be imported
            if ((readFailed || simpleListRowsSkipped) && errorLevel == Globals.IMPORT_ERROR_LEVEL_OKAY) {
                errorLevel = Globals.IMPORT_ERROR_LEVEL_WARN
            }
        } finally {
            // Tags can stay unused, e.g. if the file contains unused tags, the cards of a tag were
            // skipped or the file is incomplete
            helperDelete.deleteUnusedTags(createdTags)
            // Nothing could be imported from a simple list: remove the pack (and the collection)
            // created for it
            if (mode == Globals.IMPORT_MODE_SIMPLE_LIST && errorLevel == Globals.IMPORT_ERROR_LEVEL_ERROR) {
                helperGet.getSinglePack(packUidConverter.getNewValue(0))?.let {
                    helperDelete.deletePack(it, true)
                }
                simpleListCreatedCollection?.let { uid ->
                    helperGet.getSingleCollection(uid)?.let { helperDelete.deleteCollection(it, true) }
                }
            }
        }
    }

    // Imports one row; throws if the row cannot be imported
    private fun importRow(line: Array<String?>) {
        if (mode == Globals.IMPORT_MODE_SIMPLE_LIST) {
            importSimpleListCard(line)
            return
        }
        val type = line[0]
        when {
            type?.endsWith("_schema") == true -> {
                schemas[type.removeSuffix("_schema")] =
                    line.withIndex().drop(1).associate { (i, name) -> (name ?: "") to i }
            }

            type == "collection" -> importCollection(line)
            type == "packs" -> importPack(line)
            type == "cards" -> importCard(line)
            type == "media" && includeMedia -> importMedia(line)
            type == "media_link_card" && includeMedia -> importMediaLink(line)
            type == "tags" -> importTag(line)
            type == "tags_link_card" -> importTagLink(line)
            type == "app_setting" && includeSettings -> importedSettings.add(line)
        }
    }

    // Reports the number of processed rows, at most every 1000 rows and 3 seconds
    private fun reportProgress() {
        progress++
        val currentTime = System.currentTimeMillis()
        if (progress % 1000 == 0 && currentTime - lastProgressSentTime > 3000) {
            lastProgressSentTime = currentTime
            ImportExportEvents.reportProgress(
                String.format(
                    context.getString(R.string.import_export_lines_progress),
                    progress
                )
            )
        }
    }

    // At least one entry was imported, so the import is no longer a complete failure
    private fun markImported() {
        if (errorLevel == Globals.IMPORT_ERROR_LEVEL_ERROR) {
            errorLevel = Globals.IMPORT_ERROR_LEVEL_OKAY
        }
    }

    /**
     * Simple list: all cards go into a new pack (named after the current date) inside a dedicated
     * collection, which is reused if it already exists. Both are stored under the old uid 0.
     */
    private fun createSimpleListPack() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT)
        val date = Date()
        val collectionName =
            context.resources.getString(R.string.import_simple_list_coll_name)
        val collectionDesc =
            context.resources.getString(R.string.import_simple_list_coll_desc)
        val packName = context.resources.getString(
            R.string.import_simple_list_pack_name,
            dateFormat.format(date)
        )
        val packDesc = context.resources.getString(
            R.string.import_simple_list_pack_desc,
            context.resources.getString(R.string.all_packs)
        )
        val sameNamed = helperGet.getAllCollectionsByName(collectionName)
        if (sameNamed.isEmpty()) {
            val collectionUidNew =
                helperCreate
                    .createCollection(collectionName, collectionDesc)
                    .toInt()
            simpleListCreatedCollection = collectionUidNew
            collectionUidConverter.insertPair(
                0,
                collectionUidNew
            )
        } else {
            collectionUidConverter.insertPair(
                0,
                sameNamed[0].uid
            )
        }
        packUidConverter.insertPair(
            0, helperCreate
                .createPack(
                    packName,
                    packDesc,
                    collectionUidConverter.getNewValue(0)
                )
                .toInt()
        )
    }

    // Columns: front, back, notes (optional)
    private fun importSimpleListCard(line: Array<String?>) {
        if (line.size != 2 && line.size != 3) {
            simpleListRowsSkipped = true
            return
        }
        val front = line[0] ?: ""
        val back = line[1] ?: ""
        val notes =
            if (line.size == 3) {
                line[2] ?: ""
            } else ""
        helperCreate.createCard(
            front,
            back,
            notes,
            packUidConverter.getNewValue(0)
        )
        markImported()
    }

    // Columns: uid, name, desc, date, colors (optional), emoji (optional)
    private fun importCollection(line: Array<String?>) {
        markImported()
        val collectionUidOld = (line.column("uid", 1) ?: "0").toInt()
        val name = line.column("name", 2) ?: ""
        val desc = line.column("desc", 3) ?: ""
        val date = parseDate(line.column("date", 4))
        val colors = (line.column("colors", 5) ?: "0").toInt()
        val emoji = line.column("emoji", 6)?.let {
            StringTools().firstEmoji(it)
        }
        val sameNamed = helperGet.getAllCollectionsByName(name)
        // Create a new collection unless one with the same name exists; in integrate mode the
        // existing one is used instead, in skip mode its content is skipped
        if (sameNamed.isEmpty() || mode == Globals.IMPORT_MODE_DUPLICATES) {
            val collectionUidNew =
                helperCreate
                    .createCollection(name, desc, colors, emoji, date)
                    .toInt()
            collectionUidConverter.insertPair(
                collectionUidOld,
                collectionUidNew
            )
        } else if (mode == Globals.IMPORT_MODE_INTEGRATE) {
            collectionUidConverter.insertPair(
                collectionUidOld,
                sameNamed[0].uid
            )
        }
    }

    // Columns: uid, name, desc, date, colors, collection (optional), emoji (optional)
    private fun importPack(line: Array<String?>) {
        // Files from old versions without collections: put the packs into a "_default" collection
        if (collectionUidConverter.getSize() == 0 &&
            mode != Globals.IMPORT_MODE_SKIP
        ) {
            markImported()
            val name = "_default"
            val desc = "_default"
            val date = System.currentTimeMillis() / 1000L
            val colors = 0
            val emoji = null
            val sameNamed = helperGet.getAllCollectionsByName(name)
            if (sameNamed.isEmpty() || mode == Globals.IMPORT_MODE_DUPLICATES) {
                val collectionUidNew =
                    helperCreate
                        .createCollection(
                            name,
                            desc,
                            colors,
                            emoji,
                            date
                        )
                        .toInt()
                collectionUidConverter.insertPair(0, collectionUidNew)
            } else {
                collectionUidConverter.insertPair(0, sameNamed[0].uid)
            }
        }
        val currentCollection = line.column("collection", 6)?.let {
            collectionUidConverter.getNewValue(it.toInt())
        } ?: collectionUidConverter.getNewValue(0)
        if (currentCollection <= 0) {
            return
        }
        val packUidOld = (line.column("uid", 1) ?: "0").toInt()
        val name = line.column("name", 2) ?: ""
        val desc = line.column("desc", 3) ?: ""
        val date = parseDate(line.column("date", 4))
        val colors = (line.column("colors", 5) ?: "0").toInt()
        val emoji = line.column("emoji", 7)?.let {
            StringTools().firstEmoji(it)
        }
        val sameNamed =
            helperGet.getAllPacksByCollectionAndNameAndDesc(
                currentCollection,
                name,
                desc
            )
        if (sameNamed.isEmpty() || mode == Globals.IMPORT_MODE_DUPLICATES) {
            val packUidNew =
                helperCreate
                    .createPack(
                        name,
                        desc,
                        currentCollection,
                        colors,
                        emoji,
                        date
                    )
                    .toInt()
            packUidConverter.insertPair(packUidOld, packUidNew)
        } else if (mode == Globals.IMPORT_MODE_INTEGRATE) {
            packUidConverter.insertPair(packUidOld, sameNamed[0].uid)
        }
    }

    // Columns: uid, front, back, pack, known, date, notes, last repetition (optional), repetition count (optional)
    private fun importCard(line: Array<String?>) {
        val currentPack =
            packUidConverter.getNewValue(
                (line.column("pack", 4) ?: "0").toInt()
            )
        if (currentPack <= 0) {
            return
        }
        val cardUidOld = (line.column("uid", 1) ?: "0").toInt()
        val front = line.column("front", 2) ?: ""
        val back = line.column("back", 3) ?: ""
        val known = (line.column("known", 5) ?: "0").toInt()
        val date = parseDate(line.column("date", 6))
        val notes = line.column("notes", 7) ?: ""
        val lastRepetition =
            parseDate(line.column("last_repetition", 8))
        val countRepetitions =
            (line.column("count_repetitions", 9) ?: "0").toInt()
        val sameNamed =
            helperGet.getSingleCardIdByPackAndFrontAndBackAndNotes(
                currentPack,
                front,
                back,
                notes
            )
        // Existing identical cards are skipped, or reused in integrate mode
        if (sameNamed == 0 || mode == Globals.IMPORT_MODE_DUPLICATES) {
            val cardUidNew = helperCreate.createCard(
                front,
                back,
                notes,
                currentPack,
                known,
                date,
                lastRepetition,
                countRepetitions
            ).toInt()
            cardUidConverter.insertPair(cardUidOld, cardUidNew)
        } else if (mode == Globals.IMPORT_MODE_INTEGRATE) {
            cardUidConverter.insertPair(cardUidOld, sameNamed)
        }
    }

    // Columns: uid, file, mime. Only the database entries are imported; the files themselves are
    // expected in the media folder. Existing entries for the same file name are reused.
    private fun importMedia(line: Array<String?>) {
        val mediaUidOld = (line.column("uid", 1) ?: "0").toInt()
        val file = line.column("file", 2) ?: ""
        val mime = line.column("mime", 3) ?: ""
        if (file.isEmpty() || mime.isEmpty()) {
            return
        }
        val mediaUidNew =
            helperGet.getSingleMedia(file)?.uid ?: helperCreate.createMedia(file, mime).toInt()
        mediaUidConverter.insertPair(mediaUidOld, mediaUidNew)
    }

    // Columns: uid, fileId, cardId
    private fun importMediaLink(line: Array<String?>) {
        val currentMedia =
            mediaUidConverter.getNewValue(
                (line.column("fileId", 2) ?: "0").toInt()
            )
        val currentCard =
            cardUidConverter.getNewValue(
                (line.column("cardId", 3) ?: "0").toInt()
            )
        if (currentMedia != 0 && currentCard != 0 && !helperGet.existsMediaLinkCard(
                currentMedia,
                currentCard
            )
        ) {
            helperCreate.createMediaLink(currentMedia, currentCard)
        }
    }

    // Columns: uid, tag_name, emoji, color. Tags are identified by name; existing tags are reused.
    private fun importTag(line: Array<String?>) {
        val tagUidOld = (line.column("uid", 1) ?: "0").toInt()
        val name = line.column("tag_name", 2) ?: ""
        val emoji = line.column("emoji", 3) ?: ""
        val color = line.column("color", 4) ?: ""
        if (name.isBlank()) {
            return
        }
        val existingTag = helperGet.getSingleTag(name)
        if (existingTag != null) {
            tagsUidConverter.insertPair(tagUidOld, existingTag.uid)
        } else {
            val tagUidNew = helperCreate.createTag(name, emoji, color).toInt()
            tagsUidConverter.insertPair(tagUidOld, tagUidNew)
            createdTags.add(tagUidNew)
        }
    }

    // Columns: uid, tagId, cardId
    private fun importTagLink(line: Array<String?>) {
        val currentTag =
            tagsUidConverter.getNewValue(
                (line.column("tagId", 2) ?: "0").toInt()
            )
        val currentCard =
            cardUidConverter.getNewValue(
                (line.column("cardId", 3) ?: "0").toInt()
            )
        if (currentTag != 0 && currentCard != 0 && !helperGet.existsTagLinkCard(
                currentTag,
                currentCard
            )
        ) {
            helperCreate.createTagLink(currentTag, currentCard)
        }
    }

    companion object {
        // Number of rows written per transaction
        private const val ROWS_PER_TRANSACTION = 500
    }

    /**
     * Columns: name, type ("int" or "bool"), value. Only known settings with the expected type
     * and a valid value are applied; other rows are skipped.
     */
    private fun applySettings() {
        val settings = AppSettings(context)
        for (line in importedSettings) {
            val setting = AppSettings.ALL.find { it.key == line.getOrNull(1) } ?: continue
            val value = line.getOrNull(3) ?: continue
            if (line.getOrNull(2) == setting.typeName) {
                setting.setFromText(settings, value)
            }
        }
    }
}
