package de.herrmann_engel.rbv.activities

import android.app.Dialog
import android.os.Bundle
import android.view.Menu
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.databinding.ActivityNewCollectionOrPackBinding
import de.herrmann_engel.rbv.databinding.DiaConfirmBinding
import de.herrmann_engel.rbv.db.utils.DB_Helper_Create

class NewCollection : AppCompatActivity() {
    private lateinit var binding: ActivityNewCollectionOrPackBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewCollectionOrPackBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.newCollectionOrPackNameLayout.hint = String.format(
            getString(R.string.collection_or_pack_name_format),
            getString(R.string.collection_name), getString(R.string.collection_or_pack_name)
        )
        binding.newCollectionOrPackDescLayout.hint = String.format(
            getString(R.string.optional),
            getString(R.string.collection_or_pack_desc)
        )

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val name = binding.newCollectionOrPackName.text.toString()
                val desc = binding.newCollectionOrPackDesc.text.toString()
                if (name.isBlank() && desc.isBlank()) {
                    finish()
                } else {
                    val confirmCancelDialog = Dialog(this@NewCollection, R.style.dia_view)
                    val bindingConfirmCancelDialog = DiaConfirmBinding.inflate(layoutInflater)
                    confirmCancelDialog.setContentView(bindingConfirmCancelDialog.root)
                    confirmCancelDialog.setTitle(resources.getString(R.string.discard_changes))
                    confirmCancelDialog.window!!.setLayout(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT
                    )
                    bindingConfirmCancelDialog.diaConfirmYes.setOnClickListener { finish() }
                    bindingConfirmCancelDialog.diaConfirmNo.setOnClickListener { confirmCancelDialog.dismiss() }
                    confirmCancelDialog.show()
                }
            }
        })

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_add, menu)
        menu.findItem(R.id.menu_add_insert).setOnMenuItemClickListener {
            val name = binding.newCollectionOrPackName.text.toString()
            val desc = binding.newCollectionOrPackDesc.text.toString()
            try {
                val dbHelperCreate = DB_Helper_Create(this)
                dbHelperCreate.createCollection(name, desc)
                finish()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, R.string.error_values, Toast.LENGTH_SHORT).show()
            }
            return@setOnMenuItemClickListener true
        }
        return true
    }
}
