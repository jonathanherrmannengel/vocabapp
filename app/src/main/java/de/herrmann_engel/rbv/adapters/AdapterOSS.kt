package de.herrmann_engel.rbv.adapters

import android.app.Dialog
import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import de.herrmann_engel.rbv.Globals
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.databinding.DiaOssBinding
import de.herrmann_engel.rbv.databinding.RecViewBinding
import de.herrmann_engel.rbv.oss.OSSLicenses
import de.herrmann_engel.rbv.utils.ContextTools

class AdapterOSS(private val licenses: List<OSSLicenses>) :
    RecyclerView.Adapter<AdapterOSS.ViewHolder>() {
    class ViewHolder(val binding: RecViewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding = RecViewBinding.inflate(
            LayoutInflater.from(viewGroup.context),
            viewGroup,
            false
        )
        val settings =
            viewGroup.context.getSharedPreferences(Globals.SETTINGS_NAME, Context.MODE_PRIVATE)
        if (settings.getBoolean("ui_font_size", false)) {
            binding.recName.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                viewGroup.context.resources.getDimension(R.dimen.rec_view_font_size_big)
            )
            binding.recDesc.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                viewGroup.context.resources.getDimension(R.dimen.rec_view_font_size_below_big)
            )
        }
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val context = viewHolder.binding.root.context
        val license = licenses[position]
        viewHolder.binding.recName.text = license.project.name
        viewHolder.binding.recName.setOnClickListener {
            val activity = ContextTools().getActivity(context)
            if (activity != null) {
                val ossDialog = Dialog(context, R.style.dia_view)
                val bindingOssDialog: DiaOssBinding =
                    DiaOssBinding.inflate(activity.layoutInflater)
                ossDialog.setContentView(bindingOssDialog.root)
                ossDialog.setTitle(license.project.name)
                ossDialog.window!!.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT
                )
                if (license.project.dev.isEmpty()) {
                    bindingOssDialog.diaOssProjectDev.visibility = View.GONE
                } else {
                    bindingOssDialog.diaOssProjectDevText.text = license.project.dev
                }
                if (license.project.url.isEmpty()) {
                    bindingOssDialog.diaOssProjectUrl.visibility = View.GONE
                } else {
                    bindingOssDialog.diaOssProjectUrlText.text = license.project.url
                }
                if (license.project.code.isEmpty()) {
                    bindingOssDialog.diaOssProjectCode.visibility = View.GONE
                } else {
                    bindingOssDialog.diaOssProjectCodeText.text = license.project.code
                }
                if (license.licenseLink == null) {
                    bindingOssDialog.diaOssLicenseShort.visibility = View.GONE
                } else {
                    bindingOssDialog.diaOssLicenseShortText.text = license.licenseLink
                }
                try {
                    bindingOssDialog.diaOssLicense.text =
                        license.licenseFilePath?.let { it1 ->
                            context.assets.open(it1).bufferedReader().use { reader ->
                                reader.readText()
                            }
                        }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, R.string.error, Toast.LENGTH_LONG).show()
                }
                ossDialog.show()
            } else {
                Toast.makeText(context, R.string.error, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun getItemCount(): Int {
        return licenses.size
    }
}
