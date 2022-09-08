package de.herrmann_engel.rbv

import android.app.Dialog
import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class AdapterOSS(private val licenses: List<OSS_Licenses>, private val c: Context) :
        RecyclerView.Adapter<AdapterOSS.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.rec_name)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view =
                LayoutInflater.from(viewGroup.context).inflate(R.layout.rec_view, viewGroup, false)
        val settings = c.getSharedPreferences(Globals.SETTINGS_NAME, Context.MODE_PRIVATE)
        if (settings.getBoolean("ui_font_size", false)) {
            view.findViewById<TextView>(R.id.rec_name)
                    .setTextSize(
                            TypedValue.COMPLEX_UNIT_PX,
                            c.resources.getDimension(R.dimen.rec_view_font_size_big)
                    )
            view.findViewById<TextView>(R.id.rec_desc)
                    .setTextSize(
                            TypedValue.COMPLEX_UNIT_PX,
                            c.resources.getDimension(R.dimen.rec_view_font_size_below_big)
                    )
        }
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.textView.text = licenses[position].project.name
        val license = licenses[position]
        viewHolder.textView.setOnClickListener {
            val ossDialog = Dialog(c, R.style.dia_view)
            ossDialog.setContentView(R.layout.dia_oss)
            ossDialog.setTitle(license.project.name)
            ossDialog.window!!.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT
            )
            val ossDialogProjectDev = ossDialog.findViewById<TextView>(R.id.dia_oss_project_dev)
            if (license.project.dev.equals("")) {
                ossDialogProjectDev.visibility = View.GONE
            } else {
                ossDialogProjectDev.text = license.project.dev
            }
            val ossDialogProjectUrl = ossDialog.findViewById<TextView>(R.id.dia_oss_project_url)
            if (license.project.url.equals("")) {
                ossDialogProjectUrl.visibility = View.GONE
            } else {
                ossDialogProjectUrl.text = license.project.url
            }
            val ossDialogLicenseShort = ossDialog.findViewById<TextView>(R.id.dia_oss_license_short)
            if (license.licenseLink == null) {
                ossDialogLicenseShort.visibility = View.GONE
            } else {
                ossDialogLicenseShort.text = license.licenseLink
            }
            val ossDialogLicense = ossDialog.findViewById<TextView>(R.id.dia_oss_license)
            try {
                ossDialogLicense.text =
                        c.assets.open(license.licenseFilePath).bufferedReader().use { reader ->
                            reader.readText()
                        }
            } catch (e: Exception) {
                Toast.makeText(c, R.string.error, Toast.LENGTH_LONG).show()
            }
            ossDialog.show()
        }
    }

    override fun getItemCount(): Int {
        return licenses.size
    }
}
