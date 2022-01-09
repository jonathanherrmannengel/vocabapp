package de.herrmann_engel.rbv;

public class OSS_Licenses {
    public String licenseLink;
    public String licenseFilePath;
    public OSS_Project project;
    public OSS_Licenses(String licenseIdentifier, OSS_Project project) {
        this.project = project;
        switch (licenseIdentifier) {
            case "Apache-2.0":
                licenseLink = "https://www.apache.org/licenses/LICENSE-2.0.txt";
                licenseFilePath = "oss/LICENSE-Apache-2.0.txt";
                break;
            default:
                licenseLink = null;
                licenseFilePath = null;
        }
    }
}
