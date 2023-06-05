package de.herrmann_engel.rbv.activities

import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import de.herrmann_engel.rbv.Globals
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.adapters.AdapterCollectionsMovePack
import de.herrmann_engel.rbv.databinding.ActivityViewCollectionOrPackBinding
import de.herrmann_engel.rbv.databinding.DiaConfirmBinding
import de.herrmann_engel.rbv.databinding.DiaRecBinding
import de.herrmann_engel.rbv.db.DB_Pack
import de.herrmann_engel.rbv.db.utils.DB_Helper_Delete
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Date
import java.util.Locale

class ViewPack : AppCompatActivity() {
    private lateinit var binding: ActivityViewCollectionOrPackBinding
    private lateinit var dbHelperGet: DB_Helper_Get
    private var pack: DB_Pack? = null
    private var packNo = 0
    private var collectionNo = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewCollectionOrPackBinding.inflate(
            layoutInflater
        )
        setContentView(binding.root)
        dbHelperGet = DB_Helper_Get(this)
        intent.extras?.let {
            collectionNo = it.getInt("collection")
            packNo = it.getInt("pack")
        } ?: run {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
        }
    }

    public override fun onResume() {
        super.onResume()
        val settings = getSharedPreferences(Globals.SETTINGS_NAME, MODE_PRIVATE)
        val increaseFontSize = settings.getBoolean("ui_font_size", false)
        try {
            pack = dbHelperGet.getSinglePack(packNo)
            title = pack!!.name
            binding.collectionOrPackName.text = pack!!.name
            if (pack!!.desc.isNullOrEmpty()) {
                binding.collectionOrPackDesc.visibility = View.GONE
            } else {
                binding.collectionOrPackDesc.visibility = View.VISIBLE
                binding.collectionOrPackDesc.text = pack!!.desc
            }
            if (increaseFontSize) {
                binding.collectionOrPackName.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    resources.getDimension(R.dimen.details_name_size_big)
                )
                binding.collectionOrPackDesc.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    resources.getDimension(R.dimen.details_desc_size_big)
                )
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val instant = Instant.ofEpochSecond(pack!!.date)
                val dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                    .withLocale(Locale.ROOT)
                    .withZone(ZoneId.systemDefault())
                binding.collectionOrPackDate.text = dateTimeFormatter.format(instant)
            } else {
                binding.collectionOrPackDate.text = Date(pack!!.date * 1000).toString()
            }
            val colorsStatusBar = resources.obtainTypedArray(R.array.pack_color_statusbar)
            val colorsBackground = resources.obtainTypedArray(R.array.pack_color_background)
            if (pack!!.colors >= 0 && pack!!.colors < colorsStatusBar.length() && pack!!.colors < colorsBackground.length()) {
                val colorStatusBar = colorsStatusBar.getColor(pack!!.colors, 0)
                val colorBackground = colorsBackground.getColor(pack!!.colors, 0)
                supportActionBar?.setBackgroundDrawable(ColorDrawable(colorStatusBar))
                window.statusBarColor = colorStatusBar
                binding.root.setBackgroundColor(colorBackground)
            }
            colorsStatusBar.recycle()
            colorsBackground.recycle()
        } catch (e: Exception) {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_view_pack, menu)
        if (collectionNo == 0 || packNo == 0) {
            menu.findItem(R.id.menu_view_pack_edit).isVisible = false
            menu.findItem(R.id.menu_view_pack_delete).isVisible = false
            menu.findItem(R.id.menu_view_pack_move).isVisible = false
        } else {
            menu.findItem(R.id.menu_view_pack_edit).setOnMenuItemClickListener {
                val intent = Intent(this, EditPack::class.java)
                intent.putExtra("pack", packNo)
                startActivity(intent)
                return@setOnMenuItemClickListener true
            }
            menu.findItem(R.id.menu_view_pack_delete).setOnMenuItemClickListener {
                deletePack(false)
                return@setOnMenuItemClickListener true
            }
            if (collectionNo == -1) {
                menu.findItem(R.id.menu_view_pack_move).isVisible = false
            } else {
                menu.findItem(R.id.menu_view_pack_move).setOnMenuItemClickListener {
                    val moveDialog = Dialog(this, R.style.dia_view)
                    val bindingMoveDialog = DiaRecBinding.inflate(
                        layoutInflater
                    )
                    moveDialog.setContentView(bindingMoveDialog.root)
                    moveDialog.setTitle(resources.getString(R.string.move_pack))
                    moveDialog.window!!.setLayout(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT
                    )
                    val collections = dbHelperGet.allCollections
                    val adapter = AdapterCollectionsMovePack(collections, pack!!, moveDialog)
                    bindingMoveDialog.diaRec.adapter = adapter
                    bindingMoveDialog.diaRec.layoutManager = LinearLayoutManager(this)
                    moveDialog.show()
                    return@setOnMenuItemClickListener true
                }
            }
        }
        return true
    }

    private fun deletePack(forceDelete: Boolean) {
        val confirmDeleteDialog = Dialog(this, R.style.dia_view)
        val bindingConfirmDeleteDialog = DiaConfirmBinding.inflate(
            layoutInflater
        )
        confirmDeleteDialog.setContentView(bindingConfirmDeleteDialog.root)
        confirmDeleteDialog.setTitle(resources.getString(R.string.delete))
        confirmDeleteDialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        if (dbHelperGet.countCardsInPack(pack!!.uid) > 0 && !forceDelete) {
            bindingConfirmDeleteDialog.diaConfirmDesc.setText(R.string.delete_pack_with_cards)
            bindingConfirmDeleteDialog.diaConfirmDesc.visibility = View.VISIBLE
        }
        bindingConfirmDeleteDialog.diaConfirmYes.setOnClickListener {
            if (dbHelperGet.countCardsInPack(pack!!.uid) == 0 || forceDelete) {
                val dbHelperDelete = DB_Helper_Delete(this)
                dbHelperDelete.deletePack(pack, forceDelete)
                val intent = Intent(this, ListPacks::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
            } else {
                deletePack(true)
            }
            confirmDeleteDialog.dismiss()
        }
        bindingConfirmDeleteDialog.diaConfirmNo.setOnClickListener { confirmDeleteDialog.dismiss() }
        confirmDeleteDialog.show()
    }

    fun movedPack() {
        try {
            pack = dbHelperGet.getSinglePack(packNo)
            collectionNo = pack!!.collection
            val intent = Intent(this, ListPacks::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra("collection", collectionNo)
            this.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
        }
    }
}
