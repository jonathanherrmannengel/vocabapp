package de.herrmann_engel.rbv.export_import

import android.content.Context
import android.database.Cursor
import android.net.Uri
import com.opencsv.CSVWriter
import de.herrmann_engel.rbv.Globals
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.db.utils.DB_Helper_Export
import java.io.*

class AsyncExportWorker(
    val context: Context,
    private val listener: AsyncExportFinish,
    private val listenerProgress: AsyncExportProgress,
    private val singleCollection: Boolean,
    private val collectionNo: Int,
    private val includeSettings: Boolean,
    private val includeMedia: Boolean,
    private val exportFileUri: Uri?
) {
    private var progress = 0;
    fun execute() {
        listener.exportCardsResult(exportFile())
    }

    private fun exportSetting(name: String, filename: String, type: String, value: String) {
        val file = File(context.cacheDir, filename)
        val csvWrite = CSVWriter(FileWriter(file, true))
        val row = arrayOf("app_setting", name, type, value)
        csvWrite.writeNext(row)
        csvWrite.close()
    }

    private fun exportCSV(
        name: String,
        filename: String,
        cursor: Cursor,
        isFirst: Boolean,
        append: Boolean = true
    ): Boolean {
        try {
            val file = File(context.cacheDir, filename)
            val csvWrite = CSVWriter(FileWriter(file, append))
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
                        row[i + 1] = cursor.getString(cursor.getColumnIndex(columns[i]))
                    }
                    csvWrite.writeNext(row)
                    cursor.moveToNext()
                    progress++;
                    if (progress % 1000 == 0) {
                        listenerProgress.exportCardsProgress(
                            String.format(
                                context.getString(R.string.import_export_lines_progress),
                                progress
                            )
                        )
                    }
                }
            }
            csvWrite.close()
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    fun exportFile(): File? {
        try {
            val dbHelperExport = DB_Helper_Export(context)
            val index: String
            var collectionNos: MutableList<Int> = ArrayList()
            if (singleCollection) {
                index = collectionNo.toString()
                collectionNos.add(collectionNo)
            } else {
                collectionNos = dbHelperExport.allCollectionIDs
                index = "all"
            }
            val file = File(
                context.cacheDir,
                String.format(
                    "%s_%s.%s",
                    Globals.EXPORT_FILE_NAME,
                    index,
                    Globals.EXPORT_FILE_EXTENSION
                )
            )
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
                        dbHelperExport.allMedia,
                        true
                    )
                    || !exportCSV(
                        "media_link_card",
                        file.name,
                        dbHelperExport.allMediaLinks,
                        true
                    )
                ) {
                    return null
                }
            }
            if (includeSettings) {
                val settings =
                    context.getSharedPreferences(Globals.SETTINGS_NAME, Context.MODE_PRIVATE)
                if (settings.contains("default_sort")) {
                    exportSetting(
                        "default_sort",
                        file.name,
                        "int",
                        settings.getInt("default_sort", Globals.SORT_DEFAULT).toString()
                    )
                }
                if (settings.contains("format_cards")) {
                    exportSetting(
                        "format_cards",
                        file.name,
                        "bool",
                        settings.getBoolean("format_cards", false).toString()
                    )
                }
                if (settings.contains("format_card_notes")) {
                    exportSetting(
                        "format_card_notes",
                        file.name,
                        "bool",
                        settings.getBoolean("format_card_notes", false).toString()
                    )
                }
                if (settings.contains("ui_bg_images")) {
                    exportSetting(
                        "ui_bg_images",
                        file.name,
                        "bool",
                        settings.getBoolean("ui_bg_images", true).toString()
                    )
                }
                if (settings.contains("ui_font_size")) {
                    exportSetting(
                        "ui_font_size",
                        file.name,
                        "bool",
                        settings.getBoolean("ui_font_size", false).toString()
                    )
                }
                if (settings.contains("list_no_update")) {
                    exportSetting(
                        "list_no_update",
                        file.name,
                        "bool",
                        settings.getBoolean("list_no_update", true).toString()
                    )
                }
            }
            if (exportFileUri != null) {
                try {
                    val inputStream: InputStream = FileInputStream(file)
                    val outputStream: OutputStream? =
                        context.applicationContext.contentResolver.openOutputStream(exportFileUri)
                    val buffer = ByteArray(1024)
                    var length: Int
                    while (inputStream.read(buffer).also { length = it } > 0) {
                        outputStream?.write(buffer, 0, length)
                    }
                    inputStream.close()
                    outputStream?.close()
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
            return file
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}
