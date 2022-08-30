package de.herrmann_engel.rbv;

public class OSS_Licenses {
    public final String licenseLink;
    public final String licenseFilePath;
    public final OSS_Project project;

    public OSS_Licenses(String licenseIdentifier, OSS_Project project) {
        this.project = project;
        switch (licenseIdentifier) {
            case "GPLv3":
                licenseLink = "https://www.gnu.org/licenses/gpl-3.0.html";
                licenseFilePath = "oss/GPLv3.txt";
                break;
            case "Apache-2.0":
                licenseLink = "https://www.apache.org/licenses/LICENSE-2.0.txt";
                licenseFilePath = "oss/LICENSE-Apache-2.0.txt";
                break;
            case "CC-BY-SA-4.0":
                licenseLink = "https://creativecommons.org/licenses/by-sa/4.0/legalcode";
                licenseFilePath = "oss/LICENSE-CC-BY-SA-4.0.txt";
                break;
            case "public-domain":
                licenseLink = null;
                licenseFilePath = "oss/public-domain.txt";
                break;
            default:
                licenseLink = null;
                licenseFilePath = null;
        }
    }
}
