package com.example.qltccn.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.qltccn.R;
import com.example.qltccn.utils.AuthUtils;

public class ForgotPasswordActivity extends AppCompatActivity {
    private static final String TAG = "ForgotPasswordActivity";

    private EditText edtEmail;
    private Button btnNextStep, btnSignUp;
    private TextView tvLogin, tvSignUp, tvDescription;
    private ProgressBar progressBar;
    private ImageView ivFacebook, ivGoogle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Initialize views
        initViews();
        
        // Set click listeners
        setClickListeners();
        
        // Cập nhật nội dung mô tả
        updateDescription();
    }

    private void initViews() {
        try {
            edtEmail = findViewById(R.id.etEmail);
            btnNextStep = findViewById(R.id.btnNextStep);
            btnSignUp = findViewById(R.id.btnSignUp);
            tvLogin = findViewById(R.id.tvLogin);
            tvSignUp = findViewById(R.id.tvSignUp);
            progressBar = findViewById(R.id.progressBar);
            ivFacebook = findViewById(R.id.ivFacebook);
            ivGoogle = findViewById(R.id.ivGoogle);
            tvDescription = findViewById(R.id.tvDescription);
            
            // Kiểm tra các view quan trọng
            if (edtEmail == null || btnNextStep == null) {
                Log.e(TAG, "Không thể khởi tạo các view cần thiết. edtEmail: " + 
                      (edtEmail == null ? "null" : "ok") + ", btnNextStep: " + 
                      (btnNextStep == null ? "null" : "ok"));
                
                Toast.makeText(this, "Lỗi khởi tạo giao diện. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi khởi tạo giao diện: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi khởi tạo giao diện: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void updateDescription() {
        if (tvDescription != null) {
            tvDescription.setText("Nhập địa chỉ email của bạn và chúng tôi sẽ gửi hướng dẫn để đặt lại mật khẩu. Đảm bảo nhập chính xác email bạn đã dùng để đăng ký.");
        }
    }

    private void setClickListeners() {
        try {
            btnNextStep.setOnClickListener(v -> {
                if (validateEmail()) {
                    resetPassword();
                }
            });

            btnSignUp.setOnClickListener(v -> {
                navigateToSignUp();
            });

            tvLogin.setOnClickListener(v -> {
                navigateToLogin();
            });
            
            tvSignUp.setOnClickListener(v -> {
                navigateToSignUp();
            });
            
            // Thêm listener cho các nút mạng xã hội
            ivFacebook.setOnClickListener(v -> {
                handleSocialLogin("facebook");
            });
            
            ivGoogle.setOnClickListener(v -> {
                handleSocialLogin("google");
            });
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi thiết lập listeners: " + e.getMessage(), e);
        }
    }
    
    private void navigateToSignUp() {
        Intent intent = new Intent(ForgotPasswordActivity.this, SignUpActivity.class);
        startActivity(intent);
        finish();
    }
    
    private void navigateToLogin() {
        Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
    
    private void handleSocialLogin(String provider) {
        // Kiểm tra kết nối mạng trước khi đăng nhập
        if (!isNetworkConnected()) {
            Toast.makeText(this, "Không có kết nối mạng. Vui lòng kiểm tra WiFi hoặc dữ liệu di động.", Toast.LENGTH_LONG).show();
            return;
        }
        
        showProgress(true);
        
        try {
            switch (provider) {
                case "facebook":
                    // Chức năng đăng nhập Facebook sẽ được triển khai sau
                    Log.d(TAG, "Bắt đầu đăng nhập bằng Facebook");
                    Toast.makeText(this, "Tính năng đăng nhập bằng Facebook đang được phát triển", Toast.LENGTH_SHORT).show();
                    showProgress(false);
                    break;
                    
                case "google":
                    // Chức năng đăng nhập Google sẽ được triển khai sau
                    Log.d(TAG, "Bắt đầu đăng nhập bằng Google");
                    Toast.makeText(this, "Tính năng đăng nhập bằng Google đang được phát triển", Toast.LENGTH_SHORT).show();
                    showProgress(false);
                    break;
                    
                default:
                    Log.e(TAG, "Nhà cung cấp không được hỗ trợ: " + provider);
                    Toast.makeText(this, "Nhà cung cấp không được hỗ trợ", Toast.LENGTH_SHORT).show();
                    showProgress(false);
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi xử lý đăng nhập mạng xã hội: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            showProgress(false);
        }
    }

    private boolean validateEmail() {
        String email = edtEmail.getText().toString().trim();

        // Validate email
        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Vui lòng nhập email của bạn");
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Vui lòng nhập email hợp lệ");
            return false;
        }

        return true;
    }

    private void resetPassword() {
        try {
            String email = edtEmail.getText().toString().trim();

            // Kiểm tra kết nối mạng trước khi gửi email
            if (!isNetworkConnected()) {
                Toast.makeText(this, "Không có kết nối mạng. Vui lòng kiểm tra WiFi hoặc dữ liệu di động.", Toast.LENGTH_LONG).show();
                return;
            }

            // Show progress
            showProgress(true);
            btnNextStep.setEnabled(false);
            
            Log.d(TAG, "Bắt đầu gửi email đặt lại mật khẩu đến: " + email);
            
            // Đặt một cờ để kiểm soát timeout
            final boolean[] hasReceivedResponse = {false};
            
            // Đảm bảo không bị treo bằng cách đặt timeout
            Handler timeoutHandler = new Handler();
            Runnable timeoutRunnable = () -> {
                if (!hasReceivedResponse[0]) {
                    showProgress(false);
                    btnNextStep.setEnabled(true);
                    hasReceivedResponse[0] = true;
                    Log.e(TAG, "Gửi email đặt lại mật khẩu bị timeout sau 20 giây");
                    Toast.makeText(ForgotPasswordActivity.this, "Yêu cầu bị hủy do quá thời gian. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
                }
            };
            
            // Đặt timeout 20 giây
            timeoutHandler.postDelayed(timeoutRunnable, 20000);
            
            // Send password reset email
            AuthUtils.resetPassword(this, email, task -> {
                try {
                    if (!hasReceivedResponse[0]) {
                        hasReceivedResponse[0] = true;
                        timeoutHandler.removeCallbacks(timeoutRunnable);
                        
                        showProgress(false);
                        btnNextStep.setEnabled(true);
                        
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Email đặt lại mật khẩu đã được gửi thành công");
                            Toast.makeText(ForgotPasswordActivity.this, 
                                    "Email đặt lại mật khẩu đã được gửi đến " + email, 
                                    Toast.LENGTH_LONG).show();
                            
                            // Hiển thị thông báo thành công và chờ một lúc trước khi quay về
                            new Handler().postDelayed(() -> {
                                // Navigate back to login screen
                                Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            }, 1500); // Chờ 1.5 giây để người dùng đọc thông báo
                        } else {
                            String errorMessage = task.getException() != null ? 
                                    task.getException().getMessage() : "Không thể gửi email đặt lại mật khẩu";
                            Log.e(TAG, "Lỗi gửi email đặt lại mật khẩu: " + errorMessage);
                            Toast.makeText(ForgotPasswordActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Lỗi xử lý phản hồi đặt lại mật khẩu: " + e.getMessage(), e);
                    Toast.makeText(ForgotPasswordActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            showProgress(false);
            btnNextStep.setEnabled(true);
            Log.e(TAG, "Lỗi không mong muốn khi đặt lại mật khẩu: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi hệ thống: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void showProgress(boolean isLoading) {
        try {
            if (progressBar != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            } else {
                Log.e(TAG, "progressBar là null khi gọi showProgress(" + isLoading + ")");
            }
            
            if (btnNextStep != null) {
                btnNextStep.setEnabled(!isLoading);
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi hiển thị/ẩn loader: " + e.getMessage(), e);
        }
    }
    
    // Kiểm tra kết nối mạng
    private boolean isNetworkConnected() {
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Lỗi kiểm tra kết nối mạng: " + e.getMessage(), e);
            return false;
        }
    }
}