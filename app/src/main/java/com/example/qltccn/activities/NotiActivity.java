package com.example.qltccn.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.qltccn.R;

public class NotiActivity extends AppCompatActivity {

    // Khai báo các thành phần UI
    private ImageView backButton;
    private TextView titleText;
    
    // Footer navigation
    private LinearLayout homeNav, analysisNav, transactionNav, profileNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noti);

        // Khởi tạo các thành phần UI
        initializeUI();
        
        // Thiết lập sự kiện click cho các nút
        setupClickListeners();
    }

    private void initializeUI() {
        // Header
        backButton = findViewById(R.id.backButton);
        titleText = findViewById(R.id.titleText);

        // Footer navigation
        homeNav = findViewById(R.id.iconHome);
        analysisNav = findViewById(R.id.iconAnalysis);
        transactionNav = findViewById(R.id.iconTrans);
        profileNav = findViewById(R.id.iconUser);
    }

    private void setupClickListeners() {
        // Sự kiện click nút quay lại
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Footer navigation listeners
        homeNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(NotiActivity.this, HomeActivity.class));
                finish();
            }
        });

        analysisNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(NotiActivity.this, AnalysisActivity.class));
                finish();
            }
        });

        transactionNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(NotiActivity.this, TranActivity.class));
                finish();
            }
        });

        profileNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(NotiActivity.this, ProfileActivity.class));
                finish();
            }
        });
    }
} 