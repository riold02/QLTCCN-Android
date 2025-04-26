package com.example.qltccn.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.qltccn.R;

public class PasswordSettingsActivity extends AppCompatActivity {

    // UI elements
    private ImageButton toolbarBackBtn;
    private ImageView idNoti;
    private ImageView iconHome, iconChart, iconTrans, iconCategory, iconUser;
    
    // Phần mật khẩu
    private Button changePasswordBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_settings);

        // Initialize UI elements
        initUI();

        // Set click listeners
        setClickListeners();
    }

    private void initUI() {
        // Toolbar
        toolbarBackBtn = findViewById(R.id.toolbarBackBtn);
        idNoti = findViewById(R.id.idNoti);

        // Password button
        changePasswordBtn = findViewById(R.id.changePasswordBtn);

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
        
        // Cập nhật xử lý khi nhấp vào nút thông báo
        idNoti.setOnClickListener(v -> {
            // Hiển thị thông báo tính năng đang phát triển thay vì mở NotiActivity
            Toast.makeText(PasswordSettingsActivity.this, "Tính năng đang phát triển", Toast.LENGTH_SHORT).show();
            
            // Tạo rung nhẹ để tăng trải nghiệm người dùng
            try {
                android.os.Vibrator vibrator = (android.os.Vibrator) getSystemService(android.content.Context.VIBRATOR_SERVICE);
                if (vibrator != null && vibrator.hasVibrator()) {
                    // Rung nhẹ 100ms
                    vibrator.vibrate(100);
                }
            } catch (Exception e) {
                Log.e("PasswordSettings", "Lỗi khi tạo rung: " + e.getMessage());
            }
        });
        
        // Change password button
        changePasswordBtn.setOnClickListener(v -> changePassword());

        // Footer navigation
        iconHome.setOnClickListener(v -> {
            startActivity(new Intent(PasswordSettingsActivity.this, HomeActivity.class));
            finish();
        });
        
        iconChart.setOnClickListener(v -> {
            startActivity(new Intent(PasswordSettingsActivity.this, AnalysisActivity.class));
            finish();
        });
        
        iconTrans.setOnClickListener(v -> {
            startActivity(new Intent(PasswordSettingsActivity.this, TranActivity.class));
            finish();
        });
        
        iconCategory.setOnClickListener(v -> {
            startActivity(new Intent(PasswordSettingsActivity.this, CategoryActivity.class));
            finish();
        });
        
        iconUser.setOnClickListener(v -> {
            startActivity(new Intent(PasswordSettingsActivity.this, ProfileActivity.class));
            finish();
        });
    }

    private void changePassword() {
        // TODO: Implement password change logic
        // 1. Validate inputs
        // 2. Verify current password
        // 3. Check if new password meets requirements
        // 4. Check if new password and confirm password match
        // 5. Update password in the database
        // 6. Show success message
        
        Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show();
    }
} 