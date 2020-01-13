package com.harrycha.simplegaragesalemap;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class BuyerSellerActivity extends AppCompatActivity {

    Button buyerButton, sellerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buyer_seller);
        initViews();

        buyerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App.isBuyer = true;
                startActivity(new Intent(getApplicationContext(), InterestsActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });
        sellerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App.isBuyer = false;
                startActivity(new Intent(getApplicationContext(), InterestsActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });
    }

    void initViews() {
        buyerButton = findViewById(R.id.buyerButton);
        sellerButton = findViewById(R.id.sellerButton);
    }

    @Override
    public void onBackPressed() {
    }
}

