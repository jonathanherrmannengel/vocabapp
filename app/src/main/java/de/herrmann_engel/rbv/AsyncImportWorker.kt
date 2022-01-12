package de.herrmann_engel.rbv

import android.content.Context
import android.net.Uri
import java.lang.Exception
import com.opencsv.CSVReader
import java.io.*


class AsyncImportWorker(val context: Context, private val listener: AsyncImportFinish, private val uri : Uri, private val mode : Int)  {
    fun execute() {
        listener.importCardsResult(execute2())
    }
    private fun  execute2() : Int {
        try {
            var errorLevel = Globals.IMPORT_ERROR_LEVEL_ERROR
            val csvReader = CSVReader(InputStreamReader(context.contentResolver.openInputStream(uri)))
            val helperCreate = DB_Helper_Create(context)
            val helperGet = DB_Helper_Get(context)
            val collectionUidConverter = AsyncImportUidConvert()
            val packUidConverter = AsyncImportUidConvert()
            var line: Array<String?>?
            while (csvReader.readNext().also { line = it } != null) {
                try {
                    when {
                        line?.get(0) == "collection" -> {
                            if(errorLevel == Globals.IMPORT_ERROR_LEVEL_ERROR) {
                                errorLevel = Globals.IMPORT_ERROR_LEVEL_OKAY
                            }
                            val collectionUidOld = Integer.parseInt((line?.get(1) ?: "0"))
                            val name = line?.get(2) ?: ""
                            val desc = line?.get(3) ?: ""
                            val date = Integer.parseInt((line?.get(4) ?: "0")).toLong()
                            val sameNamed = helperGet.getAllCollectionsByName(name)
                            if(sameNamed.size == 0 || mode == Globals.IMPORT_MODE_DUPLICATES) {
                                val collectionUiNew = helperCreate.createCollection(name, desc, date).toInt()
                                collectionUidConverter.insertPair(collectionUidOld, collectionUiNew)
                            } else if (mode == Globals.IMPORT_MODE_INTEGRATE) {
                                collectionUidConverter.insertPair(collectionUidOld, sameNamed[0].uid)
                            }
                        }
                        line?.get(0) == "packs" -> {
                            if(collectionUidConverter.getSize() == 0 && mode != Globals.IMPORT_MODE_SKIP) {
                                if(errorLevel == Globals.IMPORT_ERROR_LEVEL_ERROR) {
                                    errorLevel = Globals.IMPORT_ERROR_LEVEL_OKAY
                                }
                                val name = "_default"
                                val desc = "_default"
                                val date = System.currentTimeMillis() / 1000L
                                val sameNamed = helperGet.getAllCollectionsByName(name)
                                if(sameNamed.size == 0 || mode == Globals.IMPORT_MODE_DUPLICATES) {
                                    val collectionUiNew = helperCreate.createCollection(name, desc, date).toInt()
                                    collectionUidConverter.insertPair(0, collectionUiNew)
                                } else {
                                    collectionUidConverter.insertPair(0, sameNamed[0].uid)
                                }
                            }
                            var currentCollection = collectionUidConverter.getNewValue(0)
                            if(line!!.size >= 7) {
                                currentCollection = collectionUidConverter.getNewValue(Integer.parseInt((line?.get(6) ?: "0")))
                            }
                            if(currentCollection > 0) {
                                val packUidOld = Integer.parseInt((line?.get(1) ?: "0"))
                                val name = line?.get(2) ?: ""
                                val desc = line?.get(3) ?: ""
                                val date = Integer.parseInt((line?.get(4) ?: "0")).toLong()
                                val colors = Integer.parseInt((line?.get(5) ?: "0"))
                                val sameNamed = helperGet.getAllPacksByCollectionAndNameAndDesc(currentCollection, name, desc)
                                if(sameNamed.size == 0  || mode == Globals.IMPORT_MODE_DUPLICATES) {
                                    val packUidNew = helperCreate.createPack(
                                        name,
                                        desc,
                                        currentCollection,
                                        colors,
                                        date
                                    ).toInt()
                                    packUidConverter.insertPair(packUidOld, packUidNew)
                                } else if (mode == Globals.IMPORT_MODE_INTEGRATE) {
                                    packUidConverter.insertPair(packUidOld, sameNamed[0].uid)
                                }
                            }
                        }
                        line?.get(0) == "cards" -> {
                            val currentPack = packUidConverter.getNewValue(Integer.parseInt((line?.get(4) ?: "0")))
                            if(currentPack > 0) {
                                try {
                                    helperGet.getSinglePack(currentPack)
                                    val front = line?.get(2) ?: ""
                                    val back = line?.get(3) ?: ""
                                    val known = Integer.parseInt((line?.get(5) ?: "0"))
                                    val date = Integer.parseInt((line?.get(6) ?: "0")).toLong()
                                    val notes = line?.get(7) ?: ""
                                    val sameNamed = helperGet.getAllCardsByPackAndFrontAndBackAndNotes(currentPack,front,back,notes)
                                    if(sameNamed.size == 0  || mode == Globals.IMPORT_MODE_DUPLICATES) {
                                        helperCreate.createCard(
                                            front,
                                            back,
                                            notes,
                                            currentPack,
                                            known,
                                            date
                                        )
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    errorLevel = Globals.IMPORT_ERROR_LEVEL_WARN
                                }
                            }
                        }
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