package de.herrmann_engel.rbv.export_import

import android.content.Context
import android.content.Intent
import android.database.Cursor
import androidx.core.content.FileProvider
import com.opencsv.CSVWriter
import de.herrmann_engel.rbv.Globals
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.db.utils.DB_Helper_Export
import java.io.File
import java.io.FileWriter

class Export {
    private val context: Context
    private val singleCollection: Boolean
    private var collectionNo = 0
    private var includeSettings = false
    private var includeMedia: Boolean

    constructor(context: Context, includeSettings: Boolean, includeMedia: Boolean) {
        singleCollection = false
        this.context = context
        this.includeSettings = includeSettings
        this.includeMedia = includeMedia
    }

    constructor(context: Context, collectionNo: Int, includeMedia: Boolean) {
        singleCollection = true
        this.context = context
        this.collectionNo = collectionNo
        this.includeMedia = includeMedia
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
                }
            }
            csvWrite.close()
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    fun exportFile(): Boolean {
        try {
            val share = Intent(Intent.ACTION_SEND)
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            share.type = "text/csv"
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
                    return false
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
                    return false
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
            share.putExtra(
                Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                    context.applicationContext,
                    context.packageName + ".fileprovider", file
                )
            )
            context.startActivity(
                Intent.createChooser(
                    share,
                    context.resources.getString(R.string.export_cards)
                )
            )
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }
}
