package com.harrycha.simplegaragesalemap;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class SettingsActivity extends AppCompatActivity {

    ConstraintLayout parentConstraintLayout;
    ProgressBar progressBar;
    Switch notifyNearbySalesSwitch;
    SeekBar nearbySalesProximitySeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initViews();

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
                switch (progress) {
                    case 0:
                        App.nearbySalesProximity = 100;
                        break;
                    case 1:
                        App.nearbySalesProximity = 1000;
                        break;
                    case 2:
                        App.nearbySalesProximity = 10000;
                        break;
                    default:
                        //Toast.makeText(getApplicationContext(), "Proximity change error.", Toast.LENGTH_LONG).show();
                        break;
                }
                ((App) getApplication()).editor.putInt("Nearby Sales Proximity", App.nearbySalesProximity);
                ((App) getApplication()).editor.apply();
            }
        });
    }

    void initViews() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Settings");

        parentConstraintLayout = findViewById(R.id.parentConstraintLayout);
        progressBar = findViewById(R.id.progressBar);
        notifyNearbySalesSwitch = findViewById(R.id.notifyNearbySalesSwitch);
        nearbySalesProximitySeekBar = findViewById(R.id.nearbySalesProximitySeekBar);

        progressBar.bringToFront();
        progressBar.setVisibility(View.GONE);

        notifyNearbySalesSwitch.setChecked(App.notifyNearbySales);
        int progress = 0;
        if (App.nearbySalesProximity == 1000) progress = 1;
        if (App.nearbySalesProximity == 10000) progress = 2;
        nearbySalesProximitySeekBar.setProgress(progress);
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
