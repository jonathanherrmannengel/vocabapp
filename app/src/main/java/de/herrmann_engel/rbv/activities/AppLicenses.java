package de.herrmann_engel.rbv.activities;

import android.content.SharedPreferences;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import de.herrmann_engel.rbv.Globals;
import de.herrmann_engel.rbv.R;
import de.herrmann_engel.rbv.adapters.AdapterOSS;
import de.herrmann_engel.rbv.databinding.ActivityDefaultRecBinding;
import de.herrmann_engel.rbv.oss.OSSLicenses;
import de.herrmann_engel.rbv.oss.OSSProject;

public class AppLicenses extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityDefaultRecBinding binding = ActivityDefaultRecBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        try {
            XmlResourceParser parser = getResources().getXml(R.xml.licenses);
            int eventType = parser.getEventType();

            List<OSSLicenses> licenses = new ArrayList<>();

            boolean projectRunning = false;
            String currentTag = "";
            OSSProject project;
            String licenseIdentifierTmp = "";
            String projectNameTmp = "";
            String projectDevTmp = "";
            String projectUrlTmp = "";

            while (eventType != XmlResourceParser.END_DOCUMENT) {
                if (eventType == XmlResourceParser.START_TAG) {
                    if (parser.getName().equals("item") && !projectRunning) {
                        projectRunning = true;
                    } else if (!parser.getName().equals("item") && projectRunning) {
                        currentTag = parser.getName();
                    }
                } else if (eventType == XmlResourceParser.END_TAG) {
                    currentTag = "";
                    if (parser.getName().equals("item") && projectRunning) {
                        projectRunning = false;
                        project = new OSSProject(projectNameTmp, projectDevTmp, projectUrlTmp);
                        licenses.add(new OSSLicenses(licenseIdentifierTmp, project));
                        licenseIdentifierTmp = "";
                        projectNameTmp = "";
                        projectDevTmp = "";
                        projectUrlTmp = "";
                    }
                } else if (eventType == XmlResourceParser.TEXT) {
                    switch (currentTag) {
                        case "identifier":
                            licenseIdentifierTmp = parser.getText();
                            break;
                        case "projectName":
                            projectNameTmp = parser.getText();
                            break;
                        case "projectDev":
                            projectDevTmp = parser.getText();
                            break;
                        case "projectUrl":
                            projectUrlTmp = parser.getText();
                            break;
                    }
                }
                eventType = parser.next();
            }

            AdapterOSS adapter = new AdapterOSS(licenses);
            binding.recDefault.setAdapter(adapter);
            binding.recDefault.setLayoutManager(new LinearLayoutManager(this));

        } catch (Exception e) {
            Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show();
        }

        SharedPreferences settings = getSharedPreferences(Globals.SETTINGS_NAME, MODE_PRIVATE);
        if (settings.getBoolean("ui_bg_images", true)) {
            binding.backgroundImage.setVisibility(View.VISIBLE);
            binding.backgroundImage.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.bg_licenses));
        }
    }

}
