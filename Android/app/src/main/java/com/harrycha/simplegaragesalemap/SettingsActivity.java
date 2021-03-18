package com.harrycha.simplegaragesalemap;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

public class SettingsActivity extends AppCompatActivity implements OnMapReadyCallback {

    ConstraintLayout parentConstraintLayout;
    ProgressBar progressBar;
    Switch notifyNearbySalesSwitch;
    TextView currentProximityTextView;
    SeekBar nearbySalesProximitySeekBar;
    GoogleMap settingsGoogleMap;
    Circle circle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        SupportMapFragment settingsMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.settingsMapFragment);
        settingsMapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        initViews(googleMap);

        settingsGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(App.myLatitude, App.myLongitude), 8.0f));

        notifyNearbySalesSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                App.notifyNearbySales = isChecked;
                ((App) getApplication()).editor.putBoolean("Notify Nearby Sales", App.notifyNearbySales);
                ((App) getApplication()).editor.apply();
            }
        });
        nearbySalesProximitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                App.nearbySalesProximity = (progress + 1) * 1000;
                currentProximityTextView.setText(App.nearbySalesProximity / 1000 + " km");
                circle.setRadius(App.nearbySalesProximity);

                ((App) getApplication()).editor.putInt("Nearby Sales Proximity", App.nearbySalesProximity);
                ((App) getApplication()).editor.apply();
            }
        });
    }

    void initViews(GoogleMap googleMap) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Settings");

        parentConstraintLayout = findViewById(R.id.parentConstraintLayout);
        progressBar = findViewById(R.id.progressBar);
        notifyNearbySalesSwitch = findViewById(R.id.notifyNearbySalesSwitch);
        currentProximityTextView = findViewById(R.id.currentProximityTextView);
        nearbySalesProximitySeekBar = findViewById(R.id.nearbySalesProximitySeekBar);

        progressBar.bringToFront();
        progressBar.setVisibility(View.GONE);

        notifyNearbySalesSwitch.setChecked(App.notifyNearbySales);

        currentProximityTextView.setText((App.nearbySalesProximity / 1000) + " km");
        nearbySalesProximitySeekBar.setProgress(App.nearbySalesProximity / 1000);

        settingsGoogleMap = googleMap;
        settingsGoogleMap.setMyLocationEnabled(true);
        settingsGoogleMap.getUiSettings().setZoomControlsEnabled(true);

        circle = settingsGoogleMap.addCircle(new CircleOptions()
                .center(new LatLng(App.myLatitude, App.myLongitude))
                .radius(App.nearbySalesProximity)
                .strokeColor(Color.BLACK)
                .fillColor(0x30ff0000)
                .strokeWidth(2));
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
