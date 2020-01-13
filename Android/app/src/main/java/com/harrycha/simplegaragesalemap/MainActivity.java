package com.harrycha.simplegaragesalemap;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    final int ACCESS_FINE_LOCATION_REQUEST_NUM = 1;

    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();

        ((App) getApplication()).prefs = getSharedPreferences("Settings", MODE_PRIVATE);;
        ((App) getApplication()).editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();

        App.myDeviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        App.salesHelper = new SalesHelper(MainActivity.this);
        App.notifiedSalesHelper = new NotifiedSalesHelper(MainActivity.this);

        final Intent startServiceIntent = new Intent(MainActivity.this, MyBackgroundService.class);
        startService(startServiceIntent);

        App.notifyNearbySales = ((App) getApplication()).prefs.getBoolean("Notify Nearby Sales", true);
        App.nearbySalesProximity = ((App) getApplication()).prefs.getInt("Nearby Sales Proximity", 10000);
        long launchCount = ((App) getApplication()).prefs.getLong("Launch Count", 0); // 2nd value is default value

        if (launchCount == 0) {
            ((App) getApplication()).editor.putLong("Launch Count", 1);
            ((App) getApplication()).editor.apply();

            checkLocationAccessible();

            Runnable r = new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                        }

                        if (isLocationPermissionGranted() && isLocationSettingOn()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setVisibility(View.VISIBLE);
                                }
                            });

                            if (!App.isUserAdded) {
                                ((App) getApplication()).checkNewUser();
                                return;
                            }
                            if (App.hasLocation) {
                                startActivity(new Intent(MainActivity.this, MapActivity.class));
                                finish();
                            }
                        }
                    }
                }
            };
            Thread serviceThread = new Thread(r);
            serviceThread.start();
        } else {
            ((App) getApplication()).editor.putLong("Launch Count", launchCount + 1);
            ((App) getApplication()).editor.apply();

            checkLocationAccessible();

            Runnable r = new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                        }

                        if (isLocationPermissionGranted() && isLocationSettingOn()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setVisibility(View.VISIBLE);
                                    startActivity(new Intent(MainActivity.this, MapActivity.class));
                                    return;
                                }
                            });
                            return;
                        }
                    }
                }
            };
            Thread serviceThread = new Thread(r);
            serviceThread.start();
        }
    }

    void initViews() {
        progressBar = findViewById(R.id.progressBar);

        progressBar.setVisibility(View.GONE);
    }

    void checkLocationAccessible() {
        if (!isLocationPermissionGranted()) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    ACCESS_FINE_LOCATION_REQUEST_NUM); // request Location permission
        }

        if (isLocationPermissionGranted() && !isLocationSettingOn()) {
            requestLocationSettingOn(); // show a text to direct the user to Location setting
        }
    }

    boolean isLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    boolean isLocationSettingOn() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER) || manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void requestLocationSettingOn() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your Location setting seems to be disabled. Would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                        explainLocationSettingOn();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void explainLocationSettingOn() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Location setting needs to be enabled to locate your sales.")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                        requestLocationSettingOn();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case ACCESS_FINE_LOCATION_REQUEST_NUM: {
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) { // if Location permission is denied
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("This app requires permission to access location in order to locate your sales.")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(final DialogInterface dialog, final int id) {
                                    ActivityCompat.requestPermissions(MainActivity.this,
                                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                            ACCESS_FINE_LOCATION_REQUEST_NUM); // request Location permission
                                    dialog.cancel();
                                }
                            });
                    final AlertDialog alert = builder.create();
                    alert.show();
                }
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) { // if Location permission is granted
                    if (isLocationPermissionGranted() && !isLocationSettingOn()) {
                        requestLocationSettingOn(); // show a text to direct the user to Location setting
                    }
                }
                return;
            }
            // other 'case' lines to check for other permissions this app might request.
        }
    }

    @Override
    public void onBackPressed() {
        // if API level > 15, exit app
        if (Build.VERSION.SDK_INT > 15) {
            this.finishAffinity();
            System.exit(0);
        }
        // if API level is 15, since finishAffinity() cannot be used, do nothing.
    }
}
