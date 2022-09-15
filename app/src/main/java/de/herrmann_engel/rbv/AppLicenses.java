package de.herrmann_engel.rbv;

import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AppLicenses extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default_rec);

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

            RecyclerView recyclerView = this.findViewById(R.id.rec_default);
            AdapterOSS adapter = new AdapterOSS(licenses, this);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), ListCollections.class);
        startActivity(intent);
        this.finish();
    }
}
