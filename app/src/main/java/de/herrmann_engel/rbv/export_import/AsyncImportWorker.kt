package de.herrmann_engel.rbv.export_import

import android.content.Context
import android.net.Uri
import com.opencsv.CSVReader
import de.herrmann_engel.rbv.Globals
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.db.utils.DB_Helper_Create
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get
import de.herrmann_engel.rbv.utils.StringTools
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AsyncImportWorker(
    val context: Context,
    private val listener: AsyncImportFinish,
    private val listenerProgress: AsyncImportProgress,
    private val uri: Uri,
    private val mode: Int,
    private val includeSettings: Boolean,
    private val includeMedia: Boolean
) {

    private var progress = 0
    private var lastProgressSentTime = 0L

    fun execute() {
        listener.importCardsResult(execute2())
    }

    private fun execute2(): Int {
        try {
            var errorLevel = Globals.IMPORT_ERROR_LEVEL_ERROR
            val csvReader =
                CSVReader(InputStreamReader(context.contentResolver.openInputStream(uri)))
            val helperCreate =
                DB_Helper_Create(context)
            val helperGet =
                DB_Helper_Get(context)
            val collectionUidConverter = AsyncImportUidConvert()
            val packUidConverter = AsyncImportUidConvert()
            val cardUidConverter = AsyncImportUidConvert()
            val mediaUidConverter = AsyncImportUidConvert()
            val tagsUidConverter = AsyncImportUidConvert()
            var line: Array<String?>?
            if (mode == Globals.IMPORT_MODE_SIMPLE_LIST) {
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
            while (csvReader.readNext().also { line = it } != null) {
                try {
                    line!!
                    if (mode == Globals.IMPORT_MODE_SIMPLE_LIST) {
                        try {
                            if (line.size == 2 || line.size == 3) {
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
                                if (errorLevel == Globals.IMPORT_ERROR_LEVEL_ERROR) {
                                    errorLevel = Globals.IMPORT_ERROR_LEVEL_OKAY
                                }
                            } else {
                                errorLevel = Globals.IMPORT_ERROR_LEVEL_WARN
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            errorLevel = Globals.IMPORT_ERROR_LEVEL_WARN
                        }
                    } else {
                        when {
                            line[0] == "collection" -> {
                                if (errorLevel == Globals.IMPORT_ERROR_LEVEL_ERROR) {
                                    errorLevel = Globals.IMPORT_ERROR_LEVEL_OKAY
                                }
                                val collectionUidOld = Integer.parseInt((line[1] ?: "0"))
                                val name = line[2] ?: ""
                                val desc = line[3] ?: ""
                                val date = Integer.parseInt((line[4] ?: "0")).toLong()
                                var colors = 0
                                if (line.size >= 6) {
                                    colors = Integer.parseInt((line[5] ?: "0"))
                                }
                                var emoji: String? = null
                                if (line.size >= 7) {
                                    emoji = line[6] ?: ""
                                    emoji = StringTools().firstEmoji(emoji)
                                }
                                val sameNamed = helperGet.getAllCollectionsByName(name)
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

                            line[0] == "packs" -> {
                                if (collectionUidConverter.getSize() == 0 &&
                                    mode != Globals.IMPORT_MODE_SKIP
                                ) {
                                    if (errorLevel == Globals.IMPORT_ERROR_LEVEL_ERROR) {
                                        errorLevel = Globals.IMPORT_ERROR_LEVEL_OKAY
                                    }
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
                                var currentCollection = collectionUidConverter.getNewValue(0)
                                if (line.size >= 7) {
                                    currentCollection =
                                        collectionUidConverter.getNewValue(
                                            Integer.parseInt((line[6] ?: "0"))
                                        )
                                }
                                if (currentCollection > 0) {
                                    val packUidOld = Integer.parseInt((line[1] ?: "0"))
                                    val name = line[2] ?: ""
                                    val desc = line[3] ?: ""
                                    val date = Integer.parseInt((line[4] ?: "0")).toLong()
                                    val colors = Integer.parseInt((line[5] ?: "0"))
                                    var emoji: String? = null
                                    if (line.size >= 8) {
                                        emoji = line[7] ?: ""
                                        emoji = StringTools().firstEmoji(emoji)
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
                            }

                            line[0] == "cards" -> {
                                val currentPack =
                                    packUidConverter.getNewValue(
                                        Integer.parseInt((line[4] ?: "0"))
                                    )
                                if (currentPack > 0) {
                                    try {
                                        helperGet.getSinglePack(currentPack)
                                        val cardUidOld = Integer.parseInt((line[1] ?: "0"))
                                        val front = line[2] ?: ""
                                        val back = line[3] ?: ""
                                        val known = Integer.parseInt((line[5] ?: "0"))
                                        val date = Integer.parseInt((line[6] ?: "0")).toLong()
                                        val notes = line[7] ?: ""
                                        val lastRepetition = if (line.size >= 9) {
                                            Integer.parseInt((line[8] ?: "0")).toLong()
                                        } else {
                                            0L
                                        }
                                        val sameNamed =
                                            helperGet.getSingleCardIdByPackAndFrontAndBackAndNotes(
                                                currentPack,
                                                front,
                                                back,
                                                notes
                                            )
                                        if (sameNamed == 0 || mode == Globals.IMPORT_MODE_DUPLICATES) {
                                            val cardUidNew = helperCreate.createCard(
                                                front,
                                                back,
                                                notes,
                                                currentPack,
                                                known,
                                                date,
                                                lastRepetition
                                            ).toInt()
                                            cardUidConverter.insertPair(cardUidOld, cardUidNew)
                                        } else if (mode == Globals.IMPORT_MODE_INTEGRATE) {
                                            cardUidConverter.insertPair(cardUidOld, sameNamed)
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        errorLevel = Globals.IMPORT_ERROR_LEVEL_WARN
                                    }
                                }
                            }

                            line[0] == "media" && includeMedia -> {
                                try {
                                    val mediaUidOld = Integer.parseInt((line[1] ?: "0"))
                                    val file = line[2] ?: ""
                                    val mime = line[3] ?: ""
                                    if (file.isNotEmpty() && mime.isNotEmpty()) {
                                        if (helperGet.existsMedia(file)) {
                                            val mediaUidNew = helperGet.getSingleMedia(file).uid
                                            mediaUidConverter.insertPair(mediaUidOld, mediaUidNew)
                                        } else {
                                            val mediaUidNew =
                                                helperCreate.createMedia(file, mime).toInt()
                                            mediaUidConverter.insertPair(mediaUidOld, mediaUidNew)

                                        }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    errorLevel = Globals.IMPORT_ERROR_LEVEL_WARN
                                }
                            }

                            line[0] == "media_link_card" && includeMedia -> {
                                try {
                                    val currentMedia =
                                        mediaUidConverter.getNewValue(
                                            Integer.parseInt((line[2] ?: "0"))
                                        )
                                    val currentCard =
                                        cardUidConverter.getNewValue(
                                            Integer.parseInt((line[3] ?: "0"))
                                        )
                                    if (currentMedia != 0 && currentCard != 0 && !helperGet.existsMediaLinkCard(
                                            currentMedia,
                                            currentCard
                                        )
                                    ) {
                                        helperCreate.createMediaLink(currentMedia, currentCard)
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    errorLevel = Globals.IMPORT_ERROR_LEVEL_WARN
                                }
                            }

                            line[0] == "tags" -> {
                                try {
                                    val tagUidOld = Integer.parseInt((line[1] ?: "0"))
                                    val name = line[2] ?: ""
                                    val emoji = line[3] ?: ""
                                    val color = line[4] ?: ""
                                    if (name.isNotBlank()) {
                                        if (helperGet.existsTag(name)) {
                                            val tagUidNew = helperGet.getSingleTag(name).uid
                                            tagsUidConverter.insertPair(tagUidOld, tagUidNew)
                                        } else {
                                            val tagUidNew =
                                                helperCreate.createTag(name, emoji, color).toInt()
                                            tagsUidConverter.insertPair(tagUidOld, tagUidNew)
                                        }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    errorLevel = Globals.IMPORT_ERROR_LEVEL_WARN
                                }
                            }

                            line[0] == "tags_link_card" -> {
                                try {
                                    val currentTag =
                                        tagsUidConverter.getNewValue(
                                            Integer.parseInt((line[2] ?: "0"))
                                        )
                                    val currentCard =
                                        cardUidConverter.getNewValue(
                                            Integer.parseInt((line[3] ?: "0"))
                                        )
                                    if (currentTag != 0 && currentCard != 0 && !helperGet.existsTagLinkCard(
                                            currentTag,
                                            currentCard
                                        )
                                    ) {
                                        helperCreate.createTagLink(currentTag, currentCard)
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    errorLevel = Globals.IMPORT_ERROR_LEVEL_WARN
                                }
                            }

                            line[0] == "app_setting" && includeSettings -> {
                                val settings =
                                    context.getSharedPreferences(
                                        Globals.SETTINGS_NAME,
                                        Context.MODE_PRIVATE
                                    )
                                val editor = settings.edit()
                                val name = line[1]
                                when {
                                    line[2] == "int" -> {
                                        line[3]?.toInt()?.let {
                                            editor.putInt(name, it)
                                        }
                                    }

                                    line[2] == "bool" -> {
                                        line[3]?.toBoolean()?.let {
                                            editor.putBoolean(name, it)
                                        }
                                    }
                                }
                                editor.apply()
                            }
                        }
                    }
                    progress++
                    val currentTime = System.currentTimeMillis()
                    if (progress % 1000 == 0 && currentTime - lastProgressSentTime > 1000) {
                        lastProgressSentTime = currentTime
                        listenerProgress.importCardsProgress(
                            String.format(
                                context.getString(R.string.import_export_lines_progress),
                                progress
                            )
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    errorLevel = Globals.IMPORT_ERROR_LEVEL_WARN
                }
            }
            csvReader.close()
            return errorLevel
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Globals.IMPORT_ERROR_LEVEL_ERROR
    }
}
