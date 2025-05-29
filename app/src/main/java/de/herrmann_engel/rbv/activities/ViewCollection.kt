package de.herrmann_engel.rbv.activities

import android.app.Dialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toDrawable
import de.herrmann_engel.rbv.Globals
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.databinding.ActivityViewCollectionOrPackBinding
import de.herrmann_engel.rbv.databinding.DiaConfirmBinding
import de.herrmann_engel.rbv.db.DB_Collection
import de.herrmann_engel.rbv.db.utils.DB_Helper_Delete
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Date
import java.util.Locale

class ViewCollection : AppCompatActivity() {
    private lateinit var binding: ActivityViewCollectionOrPackBinding
    private lateinit var dbHelperGet: DB_Helper_Get
    private lateinit var collection: DB_Collection
    private var collectionNo = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewCollectionOrPackBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dbHelperGet = DB_Helper_Get(this)
        collectionNo = intent.extras!!.getInt("collection")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_view_collection, menu)
        menu.findItem(R.id.menu_view_collection_edit).setOnMenuItemClickListener {
            val intent = Intent(this, EditCollection::class.java)
            intent.putExtra("collection", collectionNo)
            startActivity(intent)
            return@setOnMenuItemClickListener true
        }
        menu.findItem(R.id.menu_view_collection_delete).setOnMenuItemClickListener {
            deleteCollection(false)
            return@setOnMenuItemClickListener true
        }
        return true
    }

    public override fun onResume() {
        super.onResume()
        val settings = getSharedPreferences(Globals.SETTINGS_NAME, MODE_PRIVATE)
        val increaseFontSize = settings.getBoolean("ui_font_size", false)
        collection = dbHelperGet.getSingleCollection(collectionNo)
        title = collection.name
        binding.collectionOrPackName.text = collection.name
        if (collection.desc.isNullOrEmpty()) {
            binding.collectionOrPackDesc.visibility = View.GONE
        } else {
            binding.collectionOrPackDesc.visibility = View.VISIBLE
            binding.collectionOrPackDesc.text = collection.desc
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
            val instant = Instant.ofEpochSecond(collection.date)
            val dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                .withLocale(Locale.ROOT)
                .withZone(ZoneId.systemDefault())
            binding.collectionOrPackDate.text = dateTimeFormatter.format(instant)
        } else {
            binding.collectionOrPackDate.text = Date(collection.date * 1000).toString()
        }
        val colorsStatusBar = resources.obtainTypedArray(R.array.pack_color_statusbar)
        val colorsBackground = resources.obtainTypedArray(R.array.pack_color_background)
        val minimalLength = colorsStatusBar.length().coerceAtMost(colorsBackground.length())
        val collectionColors = collection.colors
        if (collectionColors in 0..<minimalLength) {
            val colorStatusBar = colorsStatusBar.getColor(collectionColors, 0)
            val colorBackground = colorsBackground.getColor(collectionColors, 0)
            supportActionBar?.setBackgroundDrawable(colorStatusBar.toDrawable())
            binding.root.setBackgroundColor(colorBackground)
        }
        colorsStatusBar.recycle()
        colorsBackground.recycle()
    }

    private fun deleteCollection(forceDelete: Boolean) {
        val confirmDeleteDialog = Dialog(this, R.style.dia_view)
        val bindingConfirmDeleteDialog = DiaConfirmBinding.inflate(layoutInflater)
        confirmDeleteDialog.setContentView(bindingConfirmDeleteDialog.root)
        confirmDeleteDialog.setTitle(resources.getString(R.string.delete))
        confirmDeleteDialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        if (dbHelperGet.getAllPacksByCollection(collection.uid).isNotEmpty() && !forceDelete) {
            bindingConfirmDeleteDialog.diaConfirmDesc.setText(R.string.delete_collection_with_packs)
            bindingConfirmDeleteDialog.diaConfirmDesc.visibility = View.VISIBLE
        }
        bindingConfirmDeleteDialog.diaConfirmYes.setOnClickListener {
            if (dbHelperGet.getAllPacksByCollection(collection.uid).isEmpty() || forceDelete) {
                val dbHelperDelete = DB_Helper_Delete(this)
                dbHelperDelete.deleteCollection(collection, forceDelete)
                val intent = Intent(this, ListCollections::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
            } else {
                deleteCollection(true)
            }
            confirmDeleteDialog.dismiss()
        }
        bindingConfirmDeleteDialog.diaConfirmNo.setOnClickListener { confirmDeleteDialog.dismiss() }
        confirmDeleteDialog.show()
    }
}
