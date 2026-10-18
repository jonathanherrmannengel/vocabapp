package de.herrmann_engel.rbv.activities

import android.content.Intent
import android.content.res.XmlResourceParser
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import de.herrmann_engel.rbv.Globals
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.adapters.AdapterOSS
import de.herrmann_engel.rbv.databinding.ActivityInformationBinding
import de.herrmann_engel.rbv.oss.OSSLicenses
import de.herrmann_engel.rbv.oss.OSSProject
import de.herrmann_engel.rbv.settings.AppSettings

/**
 * App information: link to the online help and the list of open source licenses.
 */
class AppInformation : RBVActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityInformationBinding.inflate(
            layoutInflater
        )
        setContentView(binding.root)
        try {
            // Read the used open source projects from res/xml/licenses.xml. Each <item> describes a
            // project; its child elements are collected until the closing </item>.
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
            var projectIdTmp = ""
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
                            projectCodeTmp,
                            projectIdTmp
                        )
                        licenses.add(OSSLicenses(licenseIdentifierTmp, project))
                        licenseIdentifierTmp = ""
                        projectNameTmp = ""
                        projectDevTmp = ""
                        projectUrlTmp = ""
                        projectCodeTmp = ""
                        projectIdTmp = ""
                    }
                } else if (eventType == XmlResourceParser.TEXT) {
                    when (currentTag) {
                        "identifier" -> licenseIdentifierTmp = parser.text
                        "projectName" -> projectNameTmp = parser.text
                        "projectDev" -> projectDevTmp = parser.text
                        "projectUrl" -> projectUrlTmp = parser.text
                        "projectCode" -> projectCodeTmp = parser.text
                        "projectId" -> projectIdTmp = parser.text
                    }
                }
                eventType = parser.next()
            }
            val adapter = AdapterOSS(licenses)
            binding.recLicenses.adapter = adapter
            binding.recLicenses.layoutManager = LinearLayoutManager(this)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show()
        }
        val settings = AppSettings(this)
        if (settings.uiBackgroundImages) {
            binding.informationBackgroundImage.visibility = View.VISIBLE
            binding.informationBackgroundImage.setImageDrawable(
                AppCompatResources.getDrawable(
                    this,
                    R.drawable.bg_information
                )
            )
            binding.licensesBackgroundImage.visibility = View.VISIBLE
            binding.licensesBackgroundImage.setImageDrawable(
                AppCompatResources.getDrawable(
                    this,
                    R.drawable.bg_licenses
                )
            )
        }
        if (settings.uiFontSizeBig) {
            binding.titleLicenses.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                resources.getDimension(R.dimen.title_size_big)
            )
        }
        // Open the online help in the language of the device
        binding.onlineHelpButton.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            intent.data = String.format(Globals.ONLINE_HELP, resources.configuration.locales.get(0).language).toUri()
            this.startActivity(intent)
        }
        setUpEdgeToEdge(binding.informationContainer)
    }
}
