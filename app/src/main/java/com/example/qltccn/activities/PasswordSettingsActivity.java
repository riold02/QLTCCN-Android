package com.example.qltccn.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.qltccn.R;
import com.example.qltccn.utils.AuthUtils;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class PasswordSettingsActivity extends AppCompatActivity {

    // Constants
    private static final String TAG = "PasswordSettings";

    // UI elements
    private ImageView toolbarBackBtn;
    private ImageView idNoti;
    private EditText etCurrentPassword, etNewPassword, etConfirmPassword;
    private ImageView toggleCurrentPassword, toggleNewPassword, toggleConfirmPassword;
    private Button changePasswordBtn;
    private ProgressBar progressBar;
    
    // Footer navigation
    private ImageView iconHome, iconChart, iconTrans, iconCategory, iconUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_settings);

        initUI();
        setClickListeners();
    }

    private void initUI() {
        // Toolbar
        toolbarBackBtn = findViewById(R.id.toolbarBackBtn);
        idNoti = findViewById(R.id.idNoti);

        // Password fields
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        
        // Toggle password visibility
        toggleCurrentPassword = findViewById(R.id.toggleCurrentPassword);
        toggleNewPassword = findViewById(R.id.toggleNewPassword);
        toggleConfirmPassword = findViewById(R.id.toggleConfirmPassword);

        // Change password button & progress bar
        changePasswordBtn = findViewById(R.id.changePasswordBtn);
        progressBar = findViewById(R.id.progressBar);

        // Footer icons
        iconHome = findViewById(R.id.iconHome);
        iconChart = findViewById(R.id.iconChart);
        iconTrans = findViewById(R.id.iconTrans);
        iconCategory = findViewById(R.id.iconCategory);
        iconUser = findViewById(R.id.iconUser);
        
        // Đặt biểu tượng User là đã được chọn vì đây là phần của màn hình hồ sơ
        if (iconUser != null) {
            iconUser.setImageResource(R.drawable.ic_profile_back1);
        }
    }

    private void setClickListeners() {
        // Toolbar actions
        toolbarBackBtn.setOnClickListener(v -> finish());
        
        // Xử lý thông báo
        idNoti.setOnClickListener(v -> {
            startActivity(new Intent(this, NotiActivity.class));
        });
        
        // Toggle password visibility
        setupPasswordToggle(toggleCurrentPassword, etCurrentPassword);
        setupPasswordToggle(toggleNewPassword, etNewPassword);
        setupPasswordToggle(toggleConfirmPassword, etConfirmPassword);
        
        // Change password button
        changePasswordBtn.setOnClickListener(v -> validateAndChangePassword());

        // Footer navigation
        iconHome.setOnClickListener(v -> navigateToAndFinish(HomeActivity.class));
        iconChart.setOnClickListener(v -> navigateToAndFinish(AnalysisActivity.class));
        iconTrans.setOnClickListener(v -> navigateToAndFinish(TranActivity.class));
        iconCategory.setOnClickListener(v -> navigateToAndFinish(CategoryActivity.class));
        iconUser.setOnClickListener(v -> navigateToAndFinish(ProfileActivity.class));
    }
    
    private void setupPasswordToggle(ImageView toggleView, EditText passwordField) {
        toggleView.setOnClickListener(v -> {
            // Kiểm tra xem mật khẩu có đang hiển thị hay không
            if (passwordField.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)) {
                // Ẩn mật khẩu
                passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                toggleView.setImageResource(R.drawable.ic_visibility);
            } else {
                // Hiển thị mật khẩu
                passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                toggleView.setImageResource(R.drawable.ic_visibility_off);
            }
            // Đặt con trỏ về cuối văn bản
            passwordField.setSelection(passwordField.getText().length());
        });
    }

    private void validateAndChangePassword() {
        // Lấy giá trị từ các trường
        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        
        // Kiểm tra trường rỗng
        if (TextUtils.isEmpty(currentPassword)) {
            etCurrentPassword.setError("Vui lòng nhập mật khẩu hiện tại");
            etCurrentPassword.requestFocus();
            return;
        }
        
        if (TextUtils.isEmpty(newPassword)) {
            etNewPassword.setError("Vui lòng nhập mật khẩu mới");
            etNewPassword.requestFocus();
            return;
        }
        
        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Vui lòng xác nhận mật khẩu mới");
            etConfirmPassword.requestFocus();
            return;
        }
        
        // Kiểm tra độ dài mật khẩu
        if (newPassword.length() < 6) {
            etNewPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            etNewPassword.requestFocus();
            return;
        }
        
        // Kiểm tra mật khẩu xác nhận trùng khớp
        if (!newPassword.equals(confirmPassword)) {
            etConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            etConfirmPassword.requestFocus();
            return;
        }
        
        // Thực hiện thay đổi mật khẩu
        changePassword(currentPassword, newPassword);
    }

    private void changePassword(String currentPassword, String newPassword) {
        setLoading(true);
        
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Bạn cần đăng nhập lại để thực hiện thao tác này", Toast.LENGTH_SHORT).show();
            setLoading(false);
            navigateToAndFinish(LoginActivity.class);
            return;
        }
        
        // Lấy email người dùng
        String email = user.getEmail();
        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "Không thể xác định email người dùng", Toast.LENGTH_SHORT).show();
            setLoading(false);
            return;
        }
        
        // Tạo credential để xác thực mật khẩu hiện tại
        AuthCredential credential = EmailAuthProvider.getCredential(email, currentPassword);
        
        // Xác thực lại người dùng
        user.reauthenticate(credential)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Xác thực thành công, tiến hành đổi mật khẩu
                    user.updatePassword(newPassword)
                        .addOnCompleteListener(updateTask -> {
                            setLoading(false);
                            if (updateTask.isSuccessful()) {
                                // Đổi mật khẩu thành công
                                Toast.makeText(PasswordSettingsActivity.this, 
                                    "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                                
                                // Xóa các trường nhập liệu
                                clearFields();
                                
                                // Quay lại màn hình trước
                                finish();
                            } else {
                                // Đổi mật khẩu thất bại
                                String errorMessage = updateTask.getException() != null 
                                    ? updateTask.getException().getMessage() 
                                    : "Đổi mật khẩu thất bại";
                                Toast.makeText(PasswordSettingsActivity.this, 
                                    "Lỗi: " + errorMessage, Toast.LENGTH_LONG).show();
                                Log.e(TAG, "Lỗi đổi mật khẩu: " + errorMessage);
                            }
                        });
                } else {
                    // Xác thực thất bại
                    setLoading(false);
                    etCurrentPassword.setError("Mật khẩu hiện tại không đúng");
                    etCurrentPassword.requestFocus();
                    
                    String errorMessage = task.getException() != null 
                        ? task.getException().getMessage() 
                        : "Xác thực mật khẩu hiện tại thất bại";
                    Log.e(TAG, "Lỗi xác thực: " + errorMessage);
                }
            });
    }
    
    private void clearFields() {
        etCurrentPassword.setText("");
        etNewPassword.setText("");
        etConfirmPassword.setText("");
    }
    
    private void setLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            changePasswordBtn.setEnabled(false);
            etCurrentPassword.setEnabled(false);
            etNewPassword.setEnabled(false);
            etConfirmPassword.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            changePasswordBtn.setEnabled(true);
            etCurrentPassword.setEnabled(true);
            etNewPassword.setEnabled(true);
            etConfirmPassword.setEnabled(true);
        }
    }
    
    private void navigateToAndFinish(Class<?> destinationClass) {
        Intent intent = new Intent(this, destinationClass);
        startActivity(intent);
        finish();
    }
} 