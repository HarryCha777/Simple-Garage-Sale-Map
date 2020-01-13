package com.harrycha.simplegaragesalemap;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class DateTimeActivity extends AppCompatActivity {

    TextView startEndTextView, cancelTextView, nextSaveTextView;
    CalendarView calendarView;
    TimePicker timePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_time);
        initViews();

        Calendar currCal = Calendar.getInstance();

        if (App.isStartDateTime) {
            startEndTextView.setText("Start Date and Time");
            nextSaveTextView.setText("Next");

            App.currStartDate = App.startDate;
            calendarView.setDate(App.startDate);
            calendarView.setMinDate(currCal.getTimeInMillis());
            calendarView.setMaxDate(currCal.getTimeInMillis() + ((long) 1000 * 60 * 60 * 24 * 31));

            timePicker.setCurrentHour(App.startHour);
            timePicker.setCurrentMinute(App.startMin);
        } else {
            startEndTextView.setText("End Date and Time");
            nextSaveTextView.setText("Save");

            App.currEndDate = App.currStartDate;
            calendarView.setDate(App.currStartDate);
            calendarView.setMinDate(App.currStartDate);
            calendarView.setMaxDate(App.currStartDate + ((long) 1000 * 60 * 60 * 24 * 7));

            timePicker.setCurrentHour(App.endHour);
            timePicker.setCurrentMinute(App.endMin);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(startEndTextView.getText());

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                Calendar setCal = Calendar.getInstance();
                setCal.set(year, month, dayOfMonth);

                if (App.isStartDateTime) {
                    App.currStartDate = setCal.getTimeInMillis();
                } else {
                    App.currEndDate = setCal.getTimeInMillis();
                }
            }
        });
        cancelTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App.startDate = App.currStartDate = App.beforeChangeStartDate;
                App.startHour = App.currStartHour = App.beforeChangeStartHour;
                App.startMin = App.currStartMin = App.beforeChangeStartMin;
                App.endDate = App.currEndDate = App.beforeChangeEndDate;
                App.endHour = App.currEndHour = App.beforeChangeEndHour;
                App.endMin = App.currEndMin = App.beforeChangeEndMin;
                finish();
            }
        });
        nextSaveTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (App.isStartDateTime) {
                    App.currStartHour = timePicker.getCurrentHour();
                    App.currStartMin = timePicker.getCurrentMinute();

                    if (App.currStartDate + App.currStartHour * ((long) 1000 * 60 * 60) + App.currStartMin * ((long) 1000 * 60) <
                            currCal.getTimeInMillis() + currCal.get(Calendar.HOUR) * ((long) 1000 * 60 * 60) + (currCal.get(Calendar.MINUTE) - 1) * ((long) 1000 * 60)) {
                        Toast.makeText(getApplicationContext(), "Set time cannot be in the past.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    App.isStartDateTime = false;
                    startActivity(new Intent(getApplicationContext(), DateTimeActivity.class));
                    finish();
                } else {
                    App.currEndHour = timePicker.getCurrentHour();
                    App.currEndMin = timePicker.getCurrentMinute();

                    if (App.currEndDate + App.currEndHour * ((long) 1000 * 60 * 60) + App.currEndMin * ((long) 1000 * 60) <
                            App.currStartDate + App.currStartHour * ((long) 1000 * 60 * 60) + App.currStartMin * ((long) 1000 * 60)) {
                        Toast.makeText(getApplicationContext(), "End time cannot be before start time.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    App.isStartEndDateTimeSet = true;
                    App.startDate = App.currStartDate;
                    App.startHour = App.currStartHour;
                    App.startMin = App.currStartMin;
                    App.endDate = App.currEndDate;
                    App.endHour = App.currEndHour;
                    App.endMin = App.currEndMin;
                    finish();
                }
            }
        });
    }

    void initViews() {
        startEndTextView = findViewById(R.id.startEndTextView);
        calendarView = findViewById(R.id.calendarView);
        timePicker = findViewById(R.id.timePicker);
        cancelTextView = findViewById(R.id.cancelTextView);
        nextSaveTextView = findViewById(R.id.nextSaveTextView);
    }

    // on action bar's back button pressed
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        cancelTextView.performClick();
    }
}

