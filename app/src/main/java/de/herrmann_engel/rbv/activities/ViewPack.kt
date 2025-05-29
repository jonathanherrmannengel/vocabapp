package de.herrmann_engel.rbv.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.core.graphics.drawable.toDrawable
import de.herrmann_engel.rbv.Globals
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.actions.PackActions
import de.herrmann_engel.rbv.databinding.ActivityViewCollectionOrPackBinding
import de.herrmann_engel.rbv.db.DB_Pack
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Date
import java.util.Locale

class ViewPack : PackActionsActivity() {
    private lateinit var binding: ActivityViewCollectionOrPackBinding
    private lateinit var dbHelperGet: DB_Helper_Get
    private lateinit var pack: DB_Pack
    private var packNo = 0
    private var collectionNo = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewCollectionOrPackBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dbHelperGet = DB_Helper_Get(this)
        collectionNo = intent.extras!!.getInt("collection")
        packNo = intent.extras!!.getInt("pack")
    }

    public override fun onResume() {
        super.onResume()
        val settings = getSharedPreferences(Globals.SETTINGS_NAME, MODE_PRIVATE)
        val increaseFontSize = settings.getBoolean("ui_font_size", false)
        pack = dbHelperGet.getSinglePack(packNo)
        title = pack.name
        binding.collectionOrPackName.text = pack.name
        if (pack.desc.isNullOrEmpty()) {
            binding.collectionOrPackDesc.visibility = View.GONE
        } else {
            binding.collectionOrPackDesc.visibility = View.VISIBLE
            binding.collectionOrPackDesc.text = pack.desc
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
            val instant = Instant.ofEpochSecond(pack.date)
            val dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                .withLocale(Locale.ROOT)
                .withZone(ZoneId.systemDefault())
            binding.collectionOrPackDate.text = dateTimeFormatter.format(instant)
        } else {
            binding.collectionOrPackDate.text = Date(pack.date * 1000).toString()
        }
        val colorsStatusBar = resources.obtainTypedArray(R.array.pack_color_statusbar)
        val colorsBackground = resources.obtainTypedArray(R.array.pack_color_background)
        val minimalLength = colorsStatusBar.length().coerceAtMost(colorsBackground.length())
        val packColors = pack.colors
        if (packColors in 0..<minimalLength) {
            val colorStatusBar = colorsStatusBar.getColor(packColors, 0)
            val colorBackground = colorsBackground.getColor(packColors, 0)
            supportActionBar?.setBackgroundDrawable(colorStatusBar.toDrawable())
            binding.root.setBackgroundColor(colorBackground)
        }
        colorsStatusBar.recycle()
        colorsBackground.recycle()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_view_pack, menu)
        menu.findItem(R.id.menu_view_pack_edit).setOnMenuItemClickListener {
            val intent = Intent(this, EditPack::class.java)
            intent.putExtra("pack", packNo)
            startActivity(intent)
            return@setOnMenuItemClickListener true
        }
        menu.findItem(R.id.menu_view_pack_delete).setOnMenuItemClickListener {
            PackActions(this).delete(pack)
            return@setOnMenuItemClickListener true
        }
        if (collectionNo == Globals.LIST_CARDS_GET_DB_COLLECTIONS_ALL) {
            menu.findItem(R.id.menu_view_pack_move).isVisible = false
        } else {
            menu.findItem(R.id.menu_view_pack_move).setOnMenuItemClickListener {
                PackActions(this).move(pack)
                return@setOnMenuItemClickListener true
            }
        }
        return true
    }

    override fun deletedPacks(packIds: ArrayList<Int>) {
        val intent = Intent(this, ListPacks::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
    }

    override fun movedPacks(packIds: ArrayList<Int>) {
        try {
            pack = dbHelperGet.getSinglePack(packNo)
            collectionNo = pack.collection
            val intent = Intent(this, ListPacks::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra("collection", collectionNo)
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
        }
    }
}
