package com.harrycha.simplegaragesalemap;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class InterestsActivity extends AppCompatActivity {

    ConstraintLayout parentConstraintLayout;
    ProgressBar progressBar;

    TextView titleTextView, othersCounterTextView, backTextView, confirmTextView;
    EditText othersEditText;

    CheckBox[] checkBoxes = new CheckBox[10];
    CheckBox othersCheckBox;
    int checkCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interests);
        initViews();

        if (App.isBuyer)
            titleTextView.setText("Please select all items you are interested in buying.");
        else
            titleTextView.setText("Please select all items you are selling.");

        for (int i = 0; i < 10; i++) {
            checkBoxes[i].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                                         @Override
                                                         public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                             if (isChecked)
                                                                 checkCounter++;
                                                             else
                                                                 checkCounter--;
                                                         }
                                                     }
            );
        }
        othersCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                                     @Override
                                                     public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                         if (isChecked) {
                                                             checkCounter++;
                                                             othersEditText.setVisibility(View.VISIBLE);
                                                             othersCounterTextView.setVisibility(View.VISIBLE);
                                                         }
                                                         else {
                                                             checkCounter--;
                                                             othersEditText.setVisibility(View.GONE);
                                                             othersCounterTextView.setVisibility(View.GONE);
                                                         }
                                                     }
                                                 }
        );

        backTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        confirmTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkCounter == 0) {
                    Toast.makeText(InterestsActivity.this, "Please select at least one item.", Toast.LENGTH_SHORT).show();
                    return;
                }

                App.enableDisableViewGroup(parentConstraintLayout, false);
                progressBar.setVisibility(View.VISIBLE);
                ((App) getApplication()).updateInterests(checkBoxes[0].isChecked(), checkBoxes[1].isChecked(),
                        checkBoxes[2].isChecked(), checkBoxes[3].isChecked(), checkBoxes[4].isChecked(), checkBoxes[5].isChecked(),
                        checkBoxes[6].isChecked(), checkBoxes[7].isChecked(), checkBoxes[8].isChecked(), checkBoxes[9].isChecked(),
                        othersEditText.getText().toString());
            }
        });
    }

    void initViews() {
        parentConstraintLayout = findViewById(R.id.parentConstraintLayout);
        progressBar = findViewById(R.id.progressBar);
        titleTextView = findViewById(R.id.titleTextView);
        othersEditText = findViewById(R.id.othersEditText);
        othersCounterTextView = findViewById(R.id.othersCounterTextView);
        backTextView = findViewById(R.id.backTextView);
        confirmTextView = findViewById(R.id.confirmTextView);

        checkBoxes[0] = findViewById(R.id.checkBox1);
        checkBoxes[1] = findViewById(R.id.checkBox2);
        checkBoxes[2] = findViewById(R.id.checkBox3);
        checkBoxes[3] = findViewById(R.id.checkBox4);
        checkBoxes[4] = findViewById(R.id.checkBox5);
        checkBoxes[5] = findViewById(R.id.checkBox6);
        checkBoxes[6] = findViewById(R.id.checkBox7);
        checkBoxes[7] = findViewById(R.id.checkBox8);
        checkBoxes[8] = findViewById(R.id.checkBox9);
        checkBoxes[9] = findViewById(R.id.checkBox10);
        othersCheckBox = findViewById(R.id.otherCheckBox);

        progressBar.bringToFront();
        progressBar.setVisibility(View.GONE);
        othersEditText.setVisibility(View.GONE);
        othersCounterTextView.setVisibility(View.GONE);

        App.countChar(othersEditText, othersCounterTextView, 100);
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}

