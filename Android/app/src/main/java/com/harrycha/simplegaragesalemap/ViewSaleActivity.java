package com.harrycha.simplegaragesalemap;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.sql.Timestamp;
import java.util.Calendar;

public class ViewSaleActivity extends AppCompatActivity {

    ConstraintLayout parentConstraintLayout;
    ProgressBar progressBar;

    TextView titleTextView, descriptionTextView, addressTextView, startDateTimeTextView, endDateTimeTextView, emailTextView, phoneNumberTextView;
    ImageView[] images = new ImageView[3];
    Button editButton, deleteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_sale);
        initViews();

        titleTextView.setText(App.viewSaleDisplayValues.get(1));
        descriptionTextView.setText(App.viewSaleDisplayValues.get(2));
        addressTextView.setText(App.viewSaleDisplayValues.get(0));
        startDateTimeTextView.setText(getAmericanDateTime(App.viewSaleDisplayValues.get(8)));
        endDateTimeTextView.setText(getAmericanDateTime(App.viewSaleDisplayValues.get(9)));
        emailTextView.setText(App.viewSaleDisplayValues.get(6));
        phoneNumberTextView.setText(App.viewSaleDisplayValues.get(7));
        if (emailTextView.getText().length() == 0) emailTextView.setVisibility(View.GONE);
        if (phoneNumberTextView.getText().length() == 0) phoneNumberTextView.setVisibility(View.GONE);

        images[0].setImageBitmap(bitmapToImage(App.viewSaleDisplayValues.get(3)));
        images[1].setImageBitmap(bitmapToImage(App.viewSaleDisplayValues.get(4)));
        images[2].setImageBitmap(bitmapToImage(App.viewSaleDisplayValues.get(5)));
        if (App.viewSaleDisplayValues.get(3).length() == 0) images[0].setVisibility(View.GONE);
        if (App.viewSaleDisplayValues.get(4).length() == 0) images[1].setVisibility(View.GONE);
        if (App.viewSaleDisplayValues.get(5).length() == 0) images[2].setVisibility(View.GONE);

        if (!App.viewSaleDeviceID.equals(App.myDeviceID)) {
            editButton.setVisibility(View.GONE);
            deleteButton.setVisibility(View.GONE);
        }

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App.isEditing = true;
                finish();
                startActivity(new Intent(getApplicationContext(), PostSaleActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App.enableDisableViewGroup(parentConstraintLayout, false);
                progressBar.setVisibility(View.VISIBLE);
                ((App) getApplication()).deleteSale();
            }
        });
    }

    void initViews() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("View Sale");

        parentConstraintLayout = findViewById(R.id.parentConstraintLayout);
        progressBar = findViewById(R.id.progressBar);
        titleTextView = findViewById(R.id.titleTextView);
        images[0] = findViewById(R.id.image1);
        images[1] = findViewById(R.id.image2);
        images[2] = findViewById(R.id.image3);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        addressTextView = findViewById(R.id.addressTextView);
        startDateTimeTextView = findViewById(R.id.startDateTimeTextView);
        endDateTimeTextView = findViewById(R.id.endDateTimeTextView);
        emailTextView = findViewById(R.id.emailTextView);
        phoneNumberTextView = findViewById(R.id.phoneNumberTextView);
        editButton = findViewById(R.id.editButton);
        deleteButton = findViewById(R.id.deleteButton);

        progressBar.bringToFront();
        progressBar.setVisibility(View.GONE);
    }

    String getAmericanDateTime(String dateTime) {
        Timestamp timestamp = Timestamp.valueOf(dateTime);
        Calendar setCal = Calendar.getInstance();
        setCal.setTimeInMillis(timestamp.getTime());
        int hour = timestamp.getHours();
        int min = timestamp.getMinutes();

        String amPm = "AM";
        if (hour >= 12) {
            amPm = "PM";
            hour -= 12;
        }
        if (hour == 0) hour = 12;
        String minString = Integer.toString(min);
        if (min < 10) minString = "0" + minString;
        return (setCal.get(Calendar.MONTH) + 1) + "/" + setCal.get(Calendar.DAY_OF_MONTH) + "/" + (setCal.get(Calendar.YEAR) % 100) + "    " +
                hour + ":" + minString + " " + amPm;
    }

    Bitmap bitmapToImage(String string) {
        byte[] imageBytes = Base64.decode(string, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        return bitmap;
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

