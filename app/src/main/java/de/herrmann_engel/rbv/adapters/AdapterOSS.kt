package de.herrmann_engel.rbv.adapters

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.databinding.DiaOssBinding
import de.herrmann_engel.rbv.databinding.RecViewBinding
import de.herrmann_engel.rbv.oss.OSSLicenses
import de.herrmann_engel.rbv.settings.AppSettings
import de.herrmann_engel.rbv.utils.createDialog
import de.herrmann_engel.rbv.utils.findActivity

/**
 * List of the open source projects used by the app. Clicking one shows its details and license.
 */
class AdapterOSS(private val licenses: List<OSSLicenses>) :
    RecyclerView.Adapter<AdapterOSS.ViewHolder>() {
    class ViewHolder(val binding: RecViewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding = RecViewBinding.inflate(
            LayoutInflater.from(viewGroup.context),
            viewGroup,
            false
        )
        val settings = AppSettings(viewGroup.context)
        // Larger font size setting
        if (settings.uiFontSizeBig) {
            binding.recName.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                viewGroup.context.resources.getDimension(R.dimen.rec_view_font_size_big)
            )
        }
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val context = viewHolder.binding.root.context
        val license = licenses[position]
        viewHolder.binding.recName.text = license.project.name
        viewHolder.binding.recName.setOnClickListener {
            val activity = context.findActivity()
            if (activity != null) {
                val bindingOssDialog: DiaOssBinding =
                    DiaOssBinding.inflate(activity.layoutInflater)
                val ossDialog = createDialog(context, bindingOssDialog.root, license.project.name)
                if (license.project.id.isBlank()) {
                    bindingOssDialog.diaOssProjectId.visibility = View.GONE
                } else {
                    bindingOssDialog.diaOssProjectIdText.text = license.project.id
                }
                if (license.project.dev.isBlank()) {
                    bindingOssDialog.diaOssProjectDev.visibility = View.GONE
                } else {
                    bindingOssDialog.diaOssProjectDevText.text = license.project.dev
                }
                if (license.project.url.isBlank()) {
                    bindingOssDialog.diaOssProjectUrl.visibility = View.GONE
                } else {
                    bindingOssDialog.diaOssProjectUrlText.text = license.project.url
                }
                if (license.project.code.isBlank()) {
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
