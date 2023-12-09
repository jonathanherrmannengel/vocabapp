package de.herrmann_engel.rbv.oss

class OSSLicenses(licenseIdentifier: String?, val project: OSSProject) {
    var licenseLink: String? = null
    var licenseFilePath: String? = null

    init {
        when (licenseIdentifier) {
            "GPLv3" -> {
                licenseLink = "https://www.gnu.org/licenses/gpl-3.0.html"
                licenseFilePath = "oss/GPLv3.txt"
            }

            "Apache-2.0" -> {
                licenseLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                licenseFilePath = "oss/LICENSE-Apache-2.0.txt"
            }

            "CC-BY-SA-4.0" -> {
                licenseLink = "https://creativecommons.org/licenses/by-sa/4.0/legalcode"
                licenseFilePath = "oss/LICENSE-CC-BY-SA-4.0.txt"
            }

            "BSD-2-clause-commonmark" -> {
                licenseLink =
                    "https://raw.githubusercontent.com/commonmark/commonmark-java/main/LICENSE.txt"
                licenseFilePath = "oss/BSD-2-clause-commonmark.txt"
            }

            "WTFPL-colorseekbar" -> {
                licenseLink = "http://www.wtfpl.net/about/"
                licenseFilePath = "oss/WTFPL-colorseekbar.txt"
            }

            "public-domain" -> {
                licenseLink = null
                licenseFilePath = "oss/public-domain.txt"
            }

            else -> {
                licenseLink = null
                licenseFilePath = null
            }
        }
    }
}
