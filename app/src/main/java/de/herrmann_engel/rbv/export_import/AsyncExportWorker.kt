package de.herrmann_engel.rbv.export_import

import android.content.Context
import android.database.Cursor
import android.net.Uri
import com.opencsv.CSVWriter
import de.herrmann_engel.rbv.Globals
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.db.utils.DB_Helper_Export
import de.herrmann_engel.rbv.settings.AppSettings
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter
import java.util.Locale

/**
 * Exports the database (or a single collection) into a CSV file in the cache directory and,
 * if an export URI is given, copies it there.
 *
 * Every row starts with the type of the entry (e.g. "collection", "packs", "cards", "tags",
 * "app_setting"), followed by the database columns. Before the first row of a type, a
 * "<type>_schema" row lists the column names. Only database entries are exported, not the
 * media files themselves.
 */
class AsyncExportWorker(
    val context: Context,
    private val singleCollection: Boolean,
    private val collectionNo: Int,
    private val includeSettings: Boolean,
    private val includeMedia: Boolean,
    private val exportFileUri: Uri?
) {
    private var progress = 0
    private var lastProgressSentTime = 0L
    fun execute(): ImportExportEvents.Result {
        return ImportExportEvents.Result.ExportFinished(exportFile())
    }

    // Appends a single setting as an "app_setting" row (name, type, value)
    private fun exportSetting(name: String, filename: String, type: String, value: String) {
        val file = File(context.cacheDir, filename)
        CSVWriter(FileWriter(file, true)).use { csvWrite ->
            val row = arrayOf("app_setting", name, type, value)
            csvWrite.writeNext(row)
        }
    }

    /**
     * Writes all rows of the cursor to the file, prefixed by name. Writes the schema row first
     * if isFirst is set. Closes the cursor. Returns false on errors.
     */
    private fun exportCSV(
        name: String,
        filename: String,
        cursor: Cursor,
        isFirst: Boolean,
        append: Boolean = true
    ): Boolean {
        try {
            val file = File(context.cacheDir, filename)
            cursor.use {
                CSVWriter(FileWriter(file, append)).use { csvWrite ->
                    val columns = cursor.columnNames
                    if (isFirst) {
                        val schema = arrayOfNulls<String>(columns.size + 1)
                        schema[0] = name + "_schema"
                        System.arraycopy(columns, 0, schema, 1, columns.size)
                        csvWrite.writeNext(schema)
                    }
                    if (cursor.moveToFirst()) {
                        while (!cursor.isAfterLast) {
                            val row = arrayOfNulls<String>(columns.size + 1)
                            row[0] = name
                            for (i in columns.indices) {
                                row[i + 1] = cursor.getString(cursor.getColumnIndexOrThrow(columns[i]))
                            }
                            csvWrite.writeNext(row)
                            cursor.moveToNext()
                            progress++
                            val currentTime = System.currentTimeMillis()
                            // Report progress every 1000 rows, at most once per three seconds
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
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    // Returns the exported file or null on errors
    private fun exportFile(): File? {
        try {
            val dbHelperExport = DB_Helper_Export(context)
            val index: String
            val collectionNos: List<Int>
            if (singleCollection) {
                index = collectionNo.toString()
                collectionNos = listOf(collectionNo)
            } else {
                collectionNos = dbHelperExport.allCollectionIDs
                index = "all"
            }
            val file = File(
                context.cacheDir,
                String.format(
                    Locale.ROOT,
                    "%s_%s.%s",
                    Globals.EXPORT_FILE_NAME,
                    index,
                    Globals.EXPORT_FILE_EXTENSION
                )
            )
            // Collection, packs and cards are exported collection by collection; the first call
            // overwrites an existing file from a previous export, later calls append to it
            var isFirst = true
            for (i in collectionNos.indices) {
                val currentCollectionNo = collectionNos[i]
                if (!exportCSV(
                        "collection",
                        file.name,
                        dbHelperExport.getSingleCollection(currentCollectionNo),
                        isFirst,
                        !isFirst
                    )
                    || !exportCSV(
                        "packs", file.name,
                        dbHelperExport.getAllPacksByCollection(currentCollectionNo), isFirst
                    )
                    || !exportCSV(
                        "cards", file.name,
                        dbHelperExport.getAllCardsByCollection(currentCollectionNo), isFirst
                    )
                ) {
                    return null
                }
                isFirst = false
            }
            if (includeMedia) {
                if (!exportCSV(
                        "media",
                        file.name,
                        if (singleCollection) {
                            dbHelperExport.getAllMediaByCollection(collectionNo)
                        } else dbHelperExport.allMedia,
                        true
                    )
                    || !exportCSV(
                        "media_link_card",
                        file.name,
                        if (singleCollection) {
                            dbHelperExport.getAllMediaLinksByCollection(collectionNo)
                        } else dbHelperExport.allMediaLinks,
                        true
                    )
                ) {
                    return null
                }
            }
            if (!exportCSV(
                    "tags",
                    file.name,
                    if (singleCollection) {
                        dbHelperExport.getAllTagsByCollection(collectionNo)
                    } else dbHelperExport.allTags,
                    true
                )
                || !exportCSV(
                    "tags_link_card",
                    file.name,
                    if (singleCollection) {
                        dbHelperExport.getAllTagLinksByCollection(collectionNo)
                    } else dbHelperExport.allTagLinks,
                    true
                )
            ) {
                return null
            }
            if (includeSettings) {
                // Only stored settings; settings never set keep their default on import
                val settings = AppSettings(context)
                AppSettings.ALL.filter { it.isSet(settings) }.forEach {
                    exportSetting(it.key, file.name, it.typeName, it.toText(settings))
                }
            }
            // Copy the file to the location chosen by the user; the export fails if this fails
            if (exportFileUri != null) {
                val outputStream = context.contentResolver.openOutputStream(exportFileUri)
                    ?: return null
                outputStream.use { output ->
                    FileInputStream(file).use { input ->
                        input.copyTo(output)
                    }
                }
            }
            return file
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}
