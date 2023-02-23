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
            var line: Array<String?>?
            while (csvReader.readNext().also { line = it } != null) {
                try {
                    when {
                        line?.get(0) == "collection" -> {
                            if (errorLevel == Globals.IMPORT_ERROR_LEVEL_ERROR) {
                                errorLevel = Globals.IMPORT_ERROR_LEVEL_OKAY
                            }
                            val collectionUidOld = Integer.parseInt((line?.get(1) ?: "0"))
                            val name = line?.get(2) ?: ""
                            val desc = line?.get(3) ?: ""
                            val date = Integer.parseInt((line?.get(4) ?: "0")).toLong()
                            var colors = 0
                            if (line!!.size >= 6) {
                                colors = Integer.parseInt((line?.get(5) ?: "0"))
                            }
                            var emoji: String? = null
                            if (line!!.size >= 7) {
                                emoji = line?.get(6) ?: ""
                                emoji = StringTools().firstEmoji(emoji)
                            }
                            val sameNamed = helperGet.getAllCollectionsByName(name)
                            if (sameNamed.size == 0 || mode == Globals.IMPORT_MODE_DUPLICATES) {
                                val collectionUiNew =
                                    helperCreate
                                        .createCollection(name, desc, colors, emoji, date)
                                        .toInt()
                                collectionUidConverter.insertPair(collectionUidOld, collectionUiNew)
                            } else if (mode == Globals.IMPORT_MODE_INTEGRATE) {
                                collectionUidConverter.insertPair(
                                    collectionUidOld,
                                    sameNamed[0].uid
                                )
                            }
                        }
                        line?.get(0) == "packs" -> {
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
                                if (sameNamed.size == 0 || mode == Globals.IMPORT_MODE_DUPLICATES) {
                                    val collectionUiNew =
                                        helperCreate
                                            .createCollection(
                                                name,
                                                desc,
                                                colors,
                                                emoji,
                                                date
                                            )
                                            .toInt()
                                    collectionUidConverter.insertPair(0, collectionUiNew)
                                } else {
                                    collectionUidConverter.insertPair(0, sameNamed[0].uid)
                                }
                            }
                            var currentCollection = collectionUidConverter.getNewValue(0)
                            if (line!!.size >= 7) {
                                currentCollection =
                                    collectionUidConverter.getNewValue(
                                        Integer.parseInt((line?.get(6) ?: "0"))
                                    )
                            }
                            if (currentCollection > 0) {
                                val packUidOld = Integer.parseInt((line?.get(1) ?: "0"))
                                val name = line?.get(2) ?: ""
                                val desc = line?.get(3) ?: ""
                                val date = Integer.parseInt((line?.get(4) ?: "0")).toLong()
                                val colors = Integer.parseInt((line?.get(5) ?: "0"))
                                var emoji: String? = null
                                if (line!!.size >= 8) {
                                    emoji = line?.get(7) ?: ""
                                    emoji = StringTools().firstEmoji(emoji)
                                }
                                val sameNamed =
                                    helperGet.getAllPacksByCollectionAndNameAndDesc(
                                        currentCollection,
                                        name,
                                        desc
                                    )
                                if (sameNamed.size == 0 || mode == Globals.IMPORT_MODE_DUPLICATES) {
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
                        line?.get(0) == "cards" -> {
                            val currentPack =
                                packUidConverter.getNewValue(
                                    Integer.parseInt((line?.get(4) ?: "0"))
                                )
                            if (currentPack > 0) {
                                try {
                                    helperGet.getSinglePack(currentPack)
                                    val cardUidOld = Integer.parseInt((line?.get(1) ?: "0"))
                                    val front = line?.get(2) ?: ""
                                    val back = line?.get(3) ?: ""
                                    val known = Integer.parseInt((line?.get(5) ?: "0"))
                                    val date = Integer.parseInt((line?.get(6) ?: "0")).toLong()
                                    val notes = line?.get(7) ?: ""
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
                                            date
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
                        line?.get(0) == "media" && includeMedia -> {
                            try {
                                val mediaUidOld = Integer.parseInt((line?.get(1) ?: "0"))
                                val file = line?.get(2) ?: ""
                                val mime = line?.get(3) ?: ""
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
                        line?.get(0) == "media_link_card" && includeMedia -> {
                            try {
                                val currentMedia =
                                    mediaUidConverter.getNewValue(
                                        Integer.parseInt((line?.get(2) ?: "0"))
                                    )
                                val currentCard =
                                    cardUidConverter.getNewValue(
                                        Integer.parseInt((line?.get(3) ?: "0"))
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
                        line?.get(0) == "app_setting" && includeSettings -> {
                            val settings =
                                context.getSharedPreferences(
                                    Globals.SETTINGS_NAME,
                                    Context.MODE_PRIVATE
                                )
                            val editor = settings.edit()
                            val name = line?.get(1)
                            when {
                                line?.get(2) == "int" -> {
                                    val value = line?.get(3)!!.toInt()
                                    editor.putInt(name, value)
                                }
                                line?.get(2) == "bool" -> {
                                    val value = line?.get(3)!!.toBoolean()
                                    editor.putBoolean(name, value)
                                }
                            }
                            editor.apply()
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
