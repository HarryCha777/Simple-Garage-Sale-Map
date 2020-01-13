package com.harrycha.simplegaragesalemap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.navigation.NavigationView;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    ConstraintLayout parentConstraintLayout;
    ProgressBar progressBar;

    EditText zipCodeEditText;
    Button zipCodeCancelButton, saleButton;

    ConstraintLayout rateAskPopUpConstraintLayout, rateYesPopUpConstraintLayout, rateNoPopUpConstraintLayout;
    TextView rateAskPopUpYesTextView, rateAskPopUpNoTextView,
            rateYesPopUpYesTextView, rateYesPopUpNoTextView,
            rateNoPopUpYesTextView, rateNoPopUpNoTextView;

    @Override
    protected void onResume() {
        super.onResume();

        ((App) getApplication()).checkVersionCode(this);

        if (parentConstraintLayout != null) {
            App.enableDisableViewGroup(parentConstraintLayout, true);
            progressBar.setVisibility(View.GONE);
        }

        if (rateAskPopUpConstraintLayout != null) {
            checkRatePopUp();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

        App.finishedSetUp = true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        initViews(googleMap);
        setUpNavDrawerMenu();

        App.myLatitude = ((App) getApplication()).prefs.getFloat("Last Latitude", 0);
        App.myLongitude = ((App) getApplication()).prefs.getFloat("Last Longitude", 0);
        App.mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(App.myLatitude, App.myLongitude), 15.0f));

        App.areSalesUpdated = true;
        Runnable r = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (App.areSalesUpdated) {
                        showSalesHistory();
                        App.areSalesUpdated = false;
                    }

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                    }
                }
            }
        };
        Thread serviceThread = new Thread(r);
        serviceThread.start();

        zipCodeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Geocoder geocoder = new Geocoder(getApplicationContext());
                final String zip = zipCodeEditText.getText().toString();
                try {
                    List<Address> addresses = geocoder.getFromLocationName(zip, 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        App.mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(address.getLatitude(), address.getLongitude()), 15.0f));
                    } else {
                        Toast.makeText(getApplicationContext(), "No such zip code found.", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                }
            }
        });
        zipCodeCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zipCodeEditText.setText("");
            }
        });
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App.enableDisableViewGroup(parentConstraintLayout, false);
                progressBar.setVisibility(View.VISIBLE);
                ((App) getApplication()).checkSale(parentConstraintLayout, progressBar);
            }
        });

        if (App.isFirstTimeSeller) {
            App.isFirstTimeSeller = false;
            saleButton.performClick();
        }

        App.mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                LinearLayout info = new LinearLayout(getApplicationContext());
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(getApplicationContext());
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(getApplicationContext());
                snippet.setTextColor(Color.GRAY);
                snippet.setText(marker.getSnippet());

                info.addView(title);
                info.addView(snippet);

                return info;
            }
        });

        App.mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                App.enableDisableViewGroup(parentConstraintLayout, false);
                progressBar.setVisibility(View.VISIBLE);

                ArrayList<String> markerInfo = (ArrayList<String>) marker.getTag();
                App.viewSaleDeviceID = markerInfo.get(0);
                App.viewSalePostedDateTime = markerInfo.get(3);
                App.viewSaleMarker = marker;
                ((App) getApplication()).viewSale(App.viewSalePostedDateTime, parentConstraintLayout, progressBar);
            }
        });
    }

    void initViews(GoogleMap googleMap) {
        parentConstraintLayout = findViewById(R.id.parentConstraintLayout);
        progressBar = findViewById(R.id.progressBar);
        zipCodeEditText = findViewById(R.id.zipCodeEditText);
        zipCodeCancelButton = findViewById(R.id.zipCodeCancelButton);
        saleButton = findViewById(R.id.saleButton);

        rateAskPopUpConstraintLayout = findViewById(R.id.rateAskPopUpConstraintLayout);
        rateAskPopUpYesTextView = findViewById(R.id.rateAskPopUpYesTextView);
        rateAskPopUpNoTextView = findViewById(R.id.rateAskPopUpNoTextView);

        rateYesPopUpConstraintLayout = findViewById(R.id.rateYesPopUpConstraintLayout);
        rateYesPopUpYesTextView = findViewById(R.id.rateYesPopUpYesTextView);
        rateYesPopUpNoTextView = findViewById(R.id.rateYesPopUpNoTextView);

        rateNoPopUpConstraintLayout = findViewById(R.id.rateNoPopUpConstraintLayout);
        rateNoPopUpYesTextView = findViewById(R.id.rateNoPopUpYesTextView);
        rateNoPopUpNoTextView = findViewById(R.id.rateNoPopUpNoTextView);

        progressBar.bringToFront();
        progressBar.setVisibility(View.GONE);

        rateAskPopUpConstraintLayout.setVisibility(View.GONE);
        rateYesPopUpConstraintLayout.setVisibility(View.GONE);
        rateNoPopUpConstraintLayout.setVisibility(View.GONE);

        App.mMap = googleMap;
        App.mMap.setMyLocationEnabled(true);
    }

    // navigation drawer menu
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navView;

    void setUpNavDrawerMenu() {
        drawerLayout = findViewById(R.id.drawerLayout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.Open, R.string.Close);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navView = findViewById(R.id.navView);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.settings:
                        startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                        break;
                    case R.id.privacy_policy:
                        String url = "https://simplegaragesalemap.pythonanywhere.com";
                        Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(url));
                        startActivity(intent);
                        break;
                    case R.id.about:
                        startActivity(new Intent(getApplicationContext(), AboutActivity.class));
                        break;
                    default:
                        return true;
                }
                return true;
            }
        });
    }

    // when navigation drawer menu is clicked
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void checkRatePopUp() {
        // check to show rate pop up or not
        // see Raghav Sood's answer in https://stackoverflow.com/questions/14514579/how-to-implement-rate-it-feature-in-android-app
        SharedPreferences prefs = getApplication().getSharedPreferences("App Rater", 0);
        if (prefs.getBoolean("Don't Show Again", false)) {
            return;
        }

        SharedPreferences.Editor editor = prefs.edit();

        // Increment launch counter
        long launch_count = prefs.getLong("Launch Count", 0) + 1;
        editor.putLong("Launch Count", launch_count);

        // Get date of first launch
        Long date_firstLaunch = prefs.getLong("First Launch Date", 0);
        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong("First Launch Date", date_firstLaunch);
        }

        final int DAYS_UNTIL_PROMPT = 2; // Min number of days
        final int LAUNCHES_UNTIL_PROMPT = 10; // Min number of launches

        // Wait for at least n days and n launches before opening
        if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
            if (System.currentTimeMillis() >= date_firstLaunch +
                    (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
                showRatePopUp(editor);
            }
        }

        editor.commit();
    }

    void showRatePopUp(final SharedPreferences.Editor editor) {
        App.enableDisableViewGroup(parentConstraintLayout, false);
        rateAskPopUpConstraintLayout.setVisibility(View.VISIBLE);
        App.enableDisableViewGroup(rateAskPopUpConstraintLayout, true);
        App.enableDisableViewGroup(rateYesPopUpConstraintLayout, true);
        App.enableDisableViewGroup(rateNoPopUpConstraintLayout, true);

        String packageName = "";
        try {
            PackageInfo pInfo = getApplication().getPackageManager().getPackageInfo(getPackageName(), 0);
            packageName = pInfo.packageName;
        } catch (Exception e) {
        }
        final String finalPackageName = packageName;

        rateAskPopUpYesTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rateAskPopUpConstraintLayout.setVisibility(View.GONE);
                rateYesPopUpConstraintLayout.setVisibility(View.VISIBLE);
            }
        });
        rateAskPopUpNoTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rateAskPopUpConstraintLayout.setVisibility(View.GONE);
                rateNoPopUpConstraintLayout.setVisibility(View.VISIBLE);
            }
        });

        rateYesPopUpYesTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("market://details?id=" + finalPackageName);
                Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    startActivity(myAppLinkToMarket);
                } catch (Exception e) {
                }

                rateYesPopUpConstraintLayout.setVisibility(View.GONE);
                App.enableDisableViewGroup(parentConstraintLayout, true);
                if (editor != null) {
                    editor.putBoolean("Don't Show Again", true);
                    editor.commit();
                }
            }
        });
        rateYesPopUpNoTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rateYesPopUpConstraintLayout.setVisibility(View.GONE);
                App.enableDisableViewGroup(parentConstraintLayout, true);
                if (editor != null) {
                    editor.putBoolean("Don't Show Again", true);
                    editor.commit();
                }
            }
        });

        rateNoPopUpYesTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("market://details?id=" + finalPackageName);
                Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    startActivity(myAppLinkToMarket);
                } catch (Exception e) {
                }

                rateNoPopUpConstraintLayout.setVisibility(View.GONE);
                App.enableDisableViewGroup(parentConstraintLayout, true);
                if (editor != null) {
                    editor.putBoolean("Don't Show Again", true);
                    editor.commit();
                }
            }
        });
        rateNoPopUpNoTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rateNoPopUpConstraintLayout.setVisibility(View.GONE);
                App.enableDisableViewGroup(parentConstraintLayout, true);
                if (editor != null) {
                    editor.putBoolean("Don't Show Again", true);
                    editor.commit();
                }
            }
        });
    }

    Handler showSalesHistoryHandler = new Handler();

    public void showSalesHistory() {
        Timestamp currDateTime = Timestamp.valueOf(App.getCurrentTimestamp());

        Runnable doDisplayError = new Runnable() {
            public void run() {
                App.mMap.clear(); // clear history

                Cursor data = App.salesHelper.get();
                while (data.moveToNext()) {
                    String deviceID = data.getString(1);
                    double latitude = data.getDouble(2);
                    double longitude = data.getDouble(3);
                    String postDateTime = data.getString(4);
                    String endDateTime = data.getString(5);
                    String title = data.getString(6);
                    String address = data.getString(7);

                    if (currDateTime.before(Timestamp.valueOf(endDateTime))) { // if sale is not over yet
                        showSale(deviceID, latitude, longitude, postDateTime, endDateTime, title, address);
                    }
                }
            }
        };
        showSalesHistoryHandler.post(doDisplayError);
    }

    public void showSale(final String deviceID, double latitude, double longitude,
                         String postedDateTime, String endDateTime, String title, String address) {
        MarkerOptions marker = new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .title(title)
                .snippet(address)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        if (deviceID.equals(App.myDeviceID)) {
            marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));
        }

        Marker addedMarker = App.mMap.addMarker(marker);
        ArrayList<String> markerInfo = new ArrayList<String>();
        markerInfo.add(deviceID); // 0
        markerInfo.add(Double.toString(latitude)); // 1
        markerInfo.add(Double.toString(longitude)); // 2
        markerInfo.add(postedDateTime); // 3
        markerInfo.add(endDateTime); // 4
        addedMarker.setTag(markerInfo);
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
