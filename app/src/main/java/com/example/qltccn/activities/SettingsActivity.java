package com.example.qltccn.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.qltccn.R;

public class SettingsActivity extends AppCompatActivity {

    // UI elements
    private ImageButton toolbarBackBtn;
    private ImageView infoIcon;
    private ImageView iconHome, iconChart, iconTrans, iconCategory, iconUser;
    private CardView cardNotificationSettings, cardPasswordSettings, cardDeleteAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize UI elements
        initUI();

        // Set click listeners
        setClickListeners();
    }

    private void initUI() {
        // Toolbar
        toolbarBackBtn = findViewById(R.id.toolbarBackBtn);
        infoIcon = findViewById(R.id.infoIcon);

        // Cards
        cardNotificationSettings = findViewById(R.id.cardNotificationSettings);
        cardPasswordSettings = findViewById(R.id.cardPasswordSettings);
        cardDeleteAccount = findViewById(R.id.cardDeleteAccount);

        // Footer icons
        iconHome = findViewById(R.id.iconHome);
        iconChart = findViewById(R.id.iconChart);
        iconTrans = findViewById(R.id.iconTrans);
        iconCategory = findViewById(R.id.iconCategory);
        iconUser = findViewById(R.id.iconUser);
    }

    private void setClickListeners() {
        // Toolbar actions
        toolbarBackBtn.setOnClickListener(v -> finish());
        infoIcon.setOnClickListener(v -> showInfoDialog());

        // Card actions
        cardNotificationSettings.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, NotificationSettingsActivity.class);
            startActivity(intent);
        });
        
        cardPasswordSettings.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, PasswordSettingsActivity.class);
            startActivity(intent);
        });
        
        cardDeleteAccount.setOnClickListener(v -> showDeleteAccountConfirmation());

        // Footer navigation
        iconHome.setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, HomeActivity.class));
            finish();
        });
        
        iconChart.setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, AnalysisActivity.class));
            finish();
        });
        
        iconTrans.setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, TranActivity.class));
            finish();
        });
        
        iconCategory.setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, CategoryActivity.class));
            finish();
        });
        
        iconUser.setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, ProfileActivity.class));
            finish();
        });
    }

    private void showInfoDialog() {
        // TODO: Show information about settings
        // This would display a dialog with app settings information
    }

    private void showDeleteAccountConfirmation() {
        // TODO: Show confirmation dialog for account deletion
        // This would display a warning and confirmation dialog
    }
} 