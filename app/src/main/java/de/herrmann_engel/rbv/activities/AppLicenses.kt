package de.herrmann_engel.rbv.activities

import android.content.res.XmlResourceParser
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.LinearLayoutManager
import de.herrmann_engel.rbv.Globals
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.adapters.AdapterOSS
import de.herrmann_engel.rbv.databinding.ActivityDefaultRecBinding
import de.herrmann_engel.rbv.oss.OSSLicenses
import de.herrmann_engel.rbv.oss.OSSProject

class AppLicenses : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityDefaultRecBinding.inflate(
            layoutInflater
        )
        setContentView(binding.root)
        try {
            val parser = resources.getXml(R.xml.licenses)
            var eventType = parser.eventType
            val licenses: MutableList<OSSLicenses> = ArrayList()
            var projectRunning = false
            var currentTag: String? = ""
            var project: OSSProject
            var licenseIdentifierTmp = ""
            var projectNameTmp = ""
            var projectDevTmp = ""
            var projectUrlTmp = ""
            var projectCodeTmp = ""
            while (eventType != XmlResourceParser.END_DOCUMENT) {
                if (eventType == XmlResourceParser.START_TAG) {
                    if (parser.name == "item" && !projectRunning) {
                        projectRunning = true
                    } else if (parser.name != "item" && projectRunning) {
                        currentTag = parser.name
                    }
                } else if (eventType == XmlResourceParser.END_TAG) {
                    currentTag = ""
                    if (parser.name == "item" && projectRunning) {
                        projectRunning = false
                        project = OSSProject(
                            projectNameTmp,
                            projectDevTmp,
                            projectUrlTmp,
                            projectCodeTmp
                        )
                        licenses.add(OSSLicenses(licenseIdentifierTmp, project))
                        licenseIdentifierTmp = ""
                        projectNameTmp = ""
                        projectDevTmp = ""
                        projectUrlTmp = ""
                        projectCodeTmp = ""
                    }
                } else if (eventType == XmlResourceParser.TEXT) {
                    when (currentTag) {
                        "identifier" -> licenseIdentifierTmp = parser.text
                        "projectName" -> projectNameTmp = parser.text
                        "projectDev" -> projectDevTmp = parser.text
                        "projectUrl" -> projectUrlTmp = parser.text
                        "projectCode" -> projectCodeTmp = parser.text
                    }
                }
                eventType = parser.next()
            }
            val adapter = AdapterOSS(licenses)
            binding.recDefault.adapter = adapter
            binding.recDefault.layoutManager = LinearLayoutManager(this)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show()
        }
        val settings = getSharedPreferences(Globals.SETTINGS_NAME, MODE_PRIVATE)
        if (settings.getBoolean("ui_bg_images", true)) {
            binding.backgroundImage.visibility = View.VISIBLE
            binding.backgroundImage.setImageDrawable(
                AppCompatResources.getDrawable(
                    this,
                    R.drawable.bg_licenses
                )
            )
        }
    }
}
