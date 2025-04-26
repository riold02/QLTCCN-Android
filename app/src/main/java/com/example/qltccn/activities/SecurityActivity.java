package com.example.qltccn.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.qltccn.R;

public class SecurityActivity extends AppCompatActivity {
    private static final String TAG = "SecurityActivity";

    // UI elements
    private ImageButton toolbarBackBtn;
    private ImageView notificationIcon;
    private ImageView iconHome, iconChart, iconTrans, iconCategory, iconUser;
    private CardView cardChangePin, cardFingerprint, cardTermsConditions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security);

        try {
            // Initialize UI elements
            initUI();

            // Set click listeners
            setClickListeners();
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khởi tạo: " + e.getMessage());
            Toast.makeText(this, "Có lỗi xảy ra khi khởi tạo màn hình", Toast.LENGTH_SHORT).show();
        }
    }

    private void initUI() {
        try {
            // Toolbar
            toolbarBackBtn = findViewById(R.id.toolbarBackBtn);
            notificationIcon = findViewById(R.id.notificationIcon);

            // Cards
            cardChangePin = findViewById(R.id.cardChangePin);
            cardFingerprint = findViewById(R.id.cardFingerprint);
            cardTermsConditions = findViewById(R.id.cardTermsConditions);

            // Footer icons
            iconHome = findViewById(R.id.iconHome);
            iconChart = findViewById(R.id.iconChart);
            iconTrans = findViewById(R.id.iconTrans);
            iconCategory = findViewById(R.id.iconCategory);
            iconUser = findViewById(R.id.iconUser);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khởi tạo UI: " + e.getMessage());
        }
    }

    private void setClickListeners() {
        // Toolbar actions
        if (toolbarBackBtn != null) {
            toolbarBackBtn.setOnClickListener(v -> finish());
        }
        
        if (notificationIcon != null) {
            notificationIcon.setOnClickListener(v -> navigateTo(NotiActivity.class));
        }

        // Card actions
        if (cardChangePin != null) {
            cardChangePin.setOnClickListener(v -> openChangePinScreen());
        }
        
        if (cardFingerprint != null) {
            cardFingerprint.setOnClickListener(v -> openFingerprintScreen());
        }
        
        if (cardTermsConditions != null) {
            cardTermsConditions.setOnClickListener(v -> openTermsConditionsScreen());
        }

        // Footer navigation
        if (iconHome != null) {
            iconHome.setOnClickListener(v -> navigateToAndFinish(HomeActivity.class));
        }
        
        if (iconChart != null) {
            iconChart.setOnClickListener(v -> navigateToAndFinish(AnalysisActivity.class));
        }
        
        if (iconTrans != null) {
            iconTrans.setOnClickListener(v -> navigateToAndFinish(TranActivity.class));
        }
        
        if (iconCategory != null) {
            iconCategory.setOnClickListener(v -> navigateToAndFinish(CategoryActivity.class));
        }
        
        if (iconUser != null) {
            iconUser.setOnClickListener(v -> navigateToAndFinish(ProfileActivity.class));
        }
    }

    private void navigateTo(Class<?> destinationClass) {
        try {
            Intent intent = new Intent(this, destinationClass);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi chuyển đến " + destinationClass.getSimpleName() + ": " + e.getMessage());
            Toast.makeText(this, "Không thể mở màn hình " + destinationClass.getSimpleName(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void navigateToAndFinish(Class<?> destinationClass) {
        try {
            Intent intent = new Intent(this, destinationClass);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi chuyển đến " + destinationClass.getSimpleName() + ": " + e.getMessage());
            Toast.makeText(this, "Không thể mở màn hình " + destinationClass.getSimpleName(), Toast.LENGTH_SHORT).show();
        }
    }

    private void openChangePinScreen() {
        // TODO: Implement change PIN functionality
        Toast.makeText(this, "Tính năng đang được phát triển", Toast.LENGTH_SHORT).show();
    }

    private void openFingerprintScreen() {
        // TODO: Implement fingerprint setup/management
        Toast.makeText(this, "Tính năng đang được phát triển", Toast.LENGTH_SHORT).show();
    }

    private void openTermsConditionsScreen() {
        // TODO: Show terms and conditions
        Toast.makeText(this, "Tính năng đang được phát triển", Toast.LENGTH_SHORT).show();
    }
}
