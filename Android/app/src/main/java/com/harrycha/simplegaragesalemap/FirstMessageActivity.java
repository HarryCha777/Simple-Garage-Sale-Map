package com.harrycha.simplegaragesalemap;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class FirstMessageActivity extends AppCompatActivity {

    TextView firstMessagePopUpBuyerTitleTextView, firstMessagePopUpOkTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_message);
        initViews();

        firstMessagePopUpOkTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });
    }

    void initViews() {
        firstMessagePopUpBuyerTitleTextView = findViewById(R.id.firstMessagePopUpBuyerTitleTextView);
        firstMessagePopUpOkTextView = findViewById(R.id.firstMessagePopUpOkTextView);
    }

    @Override
    public void onBackPressed() {
    }
}

