package com.harrycha.simplegaragesalemap;

import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    TextView titleTextView, contentTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        titleTextView = findViewById(R.id.titleTextView);
        contentTextView = findViewById(R.id.contentTextView);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("About");

        String version = "";
        try {
            PackageInfo pInfo = getApplication().getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
        } catch (Exception e) {}

        String title = "Welcome to\nSimple Garage Sale Map!";
        titleTextView.setText(title);

        String content =
                "This app is designed for its new sellers to be able to quickly and effortlessly post their garage sales.\n\n" +
                "The buyers can also look for garage sales around the world by navigating through the world map.\n\n" +
                "By automatically notifying the users of nearby garage sales, the app helps the buyers locate garage sales close to them.\n\n" +
                "This app also accumulates sales from EstateSales.org.\n\n" +
                "For any questions or suggestions, please don’t hesitate to reach out to the developer at sgsmapp@gmail.com.\n\n" +
                "App version: " + version;
        contentTextView.setText(content);
    }

    // on action bar's back button pressed
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
