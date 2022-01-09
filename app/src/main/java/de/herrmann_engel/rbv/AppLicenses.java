package de.herrmann_engel.rbv;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class AppLicenses extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default_rec);

        try {
            InputStream inputStream =  getResources().getAssets().open("oss/licenses.xml");

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(false);
            XmlPullParser xmlPullParser = factory.newPullParser();

            xmlPullParser.setInput( new InputStreamReader( inputStream) );
            int eventType = xmlPullParser.getEventType();


            List<OSS_Licenses> licenses = new ArrayList<>();

            boolean projectRunning = false;
            String currentTag = "";
            OSS_Project project;
            String licenseIdentifierTmp = "";
            String projectNameTmp = "";
            String projectDevTmp = "";
            String porjectUrlTmp = "";

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if(eventType == XmlPullParser.START_TAG) {
                    if(xmlPullParser.getName().equals("item") && !projectRunning){
                        projectRunning = true;
                    } else if(!xmlPullParser.getName().equals("item") && projectRunning){
                        currentTag = xmlPullParser.getName();
                    }
                } else if(eventType == XmlPullParser.END_TAG) {
                    currentTag = "";
                    if(xmlPullParser.getName().equals("item") && projectRunning){
                        projectRunning = false;
                        project = new OSS_Project(projectNameTmp,projectDevTmp,porjectUrlTmp);
                        licenses.add(new OSS_Licenses(licenseIdentifierTmp,project));
                        licenseIdentifierTmp = "";
                        projectNameTmp = "";
                        projectDevTmp = "";
                        porjectUrlTmp = "";
                    }
                } else if(eventType == XmlPullParser.TEXT) {
                    switch (currentTag) {
                        case "identifier":
                            licenseIdentifierTmp = xmlPullParser.getText();
                            break;
                        case "projectName":
                            projectNameTmp = xmlPullParser.getText();
                            break;
                        case "projectDev":
                            projectDevTmp = xmlPullParser.getText();
                            break;
                        case "projectUrl":
                            porjectUrlTmp = xmlPullParser.getText();
                            break;
                    }
                }
                eventType = xmlPullParser.next();
            }

            RecyclerView recyclerView = this.findViewById(R.id.rec_default);
            AdapterOSS adapter = new AdapterOSS(licenses,this);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), ListCollections.class);
        startActivity(intent);
        this.finish();
    }
}