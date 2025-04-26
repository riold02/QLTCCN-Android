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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.qltccn.R;
import com.example.qltccn.utils.AuthUtils;
import com.example.qltccn.utils.FirebaseUtils;
import com.example.qltccn.utils.TransactionUtils;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvSignUp, tvForgotPassword;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeUI();
        setupListeners();
    }

    private void initializeUI() {
        try {
            etEmail = findViewById(R.id.edtUsername);
            etPassword = findViewById(R.id.edtPassword);
            btnLogin = findViewById(R.id.btnLogin);
            tvSignUp = findViewById(R.id.tvSignUp);
            tvForgotPassword = findViewById(R.id.tvForgotPassword);
            progressBar = findViewById(R.id.progressBar);
            
            // Kiểm tra các view quan trọng
            if (etEmail == null || etPassword == null || btnLogin == null) {
                Log.e(TAG, "Không thể khởi tạo các view cần thiết. etEmail: " + 
                      (etEmail == null ? "null" : "ok") + ", etPassword: " + 
                      (etPassword == null ? "null" : "ok") + ", btnLogin: " + 
                      (btnLogin == null ? "null" : "ok"));
                
                Toast.makeText(this, "Lỗi khởi tạo giao diện. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi khởi tạo giao diện: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi khởi tạo giao diện: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupListeners() {
        try {
            if (btnLogin != null) {
                btnLogin.setOnClickListener(v -> attemptLogin());
            } else {
                Log.e(TAG, "btnLogin là null khi thiết lập listener");
            }
            
            if (tvSignUp != null) {
                tvSignUp.setOnClickListener(v -> {
                    Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                    startActivity(intent);
                });
            } else {
                Log.e(TAG, "tvSignUp là null khi thiết lập listener");
            }
            
            // Thêm xử lý sự kiện cho btnSignUp
            Button btnSignUp = findViewById(R.id.btnSignUp);
            if (btnSignUp != null) {
                btnSignUp.setOnClickListener(v -> {
                    Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                    startActivity(intent);
                });
            } else {
                Log.e(TAG, "btnSignUp là null khi thiết lập listener");
            }
            
            if (tvForgotPassword != null) {
                tvForgotPassword.setOnClickListener(v -> {
                    Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                    startActivity(intent);
                });
            } else {
                Log.e(TAG, "tvForgotPassword là null khi thiết lập listener");
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi thiết lập listeners: " + e.getMessage(), e);
        }
    }

    private void attemptLogin() {
        try {
            // Kiểm tra EditText trước khi sử dụng
            if (etEmail == null || etPassword == null) {
                Log.e(TAG, "Các trường nhập liệu không được khởi tạo đúng. etEmail: " + 
                     (etEmail == null ? "null" : "ok") + ", etPassword: " + 
                     (etPassword == null ? "null" : "ok"));
                
                Toast.makeText(this, "Lỗi hệ thống: Vui lòng khởi động lại ứng dụng", Toast.LENGTH_LONG).show();
                return;
            }
            
            // Reset errors
            etEmail.setError(null);
            etPassword.setError(null);
            
            // Get values
            String emailText = "";
            String passwordText = "";

            try {
                if (etEmail != null && etEmail.getText() != null) {
                    emailText = etEmail.getText().toString().trim();
                } else {
                    Log.e(TAG, "etEmail hoặc etEmail.getText() là null");
                }
                
                if (etPassword != null && etPassword.getText() != null) {
                    passwordText = etPassword.getText().toString().trim();
                } else {
                    Log.e(TAG, "etPassword hoặc etPassword.getText() là null");
                }
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi lấy giá trị từ EditText: " + e.getMessage(), e);
                Toast.makeText(this, "Lỗi hệ thống: " + e.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
            
            // Validate
            boolean cancel = false;
            View focusView = null;
            
            // Check for a valid password
            if (TextUtils.isEmpty(passwordText)) {
                etPassword.setError("Vui lòng nhập mật khẩu");
                focusView = etPassword;
                cancel = true;
            }
            
            // Check for a valid email address
            if (TextUtils.isEmpty(emailText)) {
                etEmail.setError("Vui lòng nhập email");
                focusView = etEmail;
                cancel = true;
            }
            
            if (cancel) {
                // Form field with error gets focus
                focusView.requestFocus();
            } else {
                // Kiểm tra kết nối mạng trước khi đăng nhập
                if (!isNetworkConnected()) {
                    Toast.makeText(this, "Không có kết nối mạng. Vui lòng kiểm tra WiFi hoặc dữ liệu di động.", Toast.LENGTH_LONG).show();
                    return;
                }
                
                // Show a progress spinner, and perform the login attempt
                showLoading(true);
                
                Log.d(TAG, "Bắt đầu đăng nhập với email: " + emailText);
                
                // Đặt một cờ để kiểm soát timeout
                final boolean[] hasReceivedResponse = {false};
                
                // Đảm bảo không bị treo bằng cách đặt timeout
                Handler timeoutHandler = new Handler();
                Runnable timeoutRunnable = () -> {
                    if (!hasReceivedResponse[0]) {
                        showLoading(false);
                        hasReceivedResponse[0] = true;
                        Log.e(TAG, "Đăng nhập bị timeout sau 20 giây");
                        Toast.makeText(LoginActivity.this, "Đăng nhập bị hủy do quá thời gian. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
                    }
                };
                
                // Đặt timeout 20 giây
                timeoutHandler.postDelayed(timeoutRunnable, 20000);
                
                try {
                    AuthUtils.signIn(this, emailText, passwordText, new AuthUtils.AuthCallback() {
                        @Override
                        public void onSuccess(FirebaseUser user) {
                            if (!hasReceivedResponse[0]) {
                                hasReceivedResponse[0] = true;
                                timeoutHandler.removeCallbacks(timeoutRunnable);
                                handleLoginSuccess(user);
                            }
                        }
                        
                        @Override
                        public void onError(String errorMessage) {
                            if (!hasReceivedResponse[0]) {
                                hasReceivedResponse[0] = true;
                                timeoutHandler.removeCallbacks(timeoutRunnable);
                                showLoading(false);
                                Log.e(TAG, "Lỗi đăng nhập: " + errorMessage);
                                Toast.makeText(LoginActivity.this, "Đăng nhập thất bại: " + errorMessage, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } catch (Exception e) {
                    if (!hasReceivedResponse[0]) {
                        hasReceivedResponse[0] = true;
                        timeoutHandler.removeCallbacks(timeoutRunnable);
                        showLoading(false);
                        Log.e(TAG, "Lỗi khi gọi đăng nhập: " + e.getMessage(), e);
                        Toast.makeText(this, "Lỗi hệ thống: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi không mong muốn khi đăng nhập: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi hệ thống: " + e.getMessage(), Toast.LENGTH_LONG).show();
            showLoading(false);
        }
    }
    
    private void showLoading(boolean isLoading) {
        try {
            if (progressBar != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            } else {
                Log.e(TAG, "progressBar là null khi gọi showLoading(" + isLoading + ")");
            }
            
            if (btnLogin != null) {
                btnLogin.setEnabled(!isLoading);
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
            Log.e("LoginActivity", "Lỗi kiểm tra kết nối mạng: " + e.getMessage(), e);
            return false;
        }
    }

    private void handleLoginSuccess(FirebaseUser user) {
        try {
            // Đăng nhập thành công, cập nhật UI
            hideProgressDialog();
            
            // Sau khi đăng nhập thành công, thực hiện di chuyển dữ liệu từ collection cũ sang subcollection mới
            migrateUserDataIfNeeded(user);
            
            // Khởi tạo dữ liệu mẫu cho tài khoản mới
            initializeDataForNewAccount();
            
            // Chuyển đến trang chính
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi xử lý đăng nhập thành công: " + e.getMessage(), e);
            Toast.makeText(this, "Đăng nhập thành công nhưng có lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
            // Vẫn chuyển màn hình dù có lỗi
            try {
                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            } catch (Exception ex) {
                Log.e(TAG, "Không thể chuyển màn hình: " + ex.getMessage(), ex);
                // Khởi động lại Activity nếu không thể chuyển màn hình
                recreate();
            }
        }
    }

    // Phương thức kiểm tra và di chuyển dữ liệu sang cấu trúc mới
    private void migrateUserDataIfNeeded(FirebaseUser user) {
        try {
            // Sử dụng phương thức di chuyển dữ liệu với tham số skipForNewAccounts = true
            // để tự động bỏ qua việc di chuyển cho tài khoản mới
            Log.d(TAG, "Kiểm tra và di chuyển dữ liệu nếu cần thiết");
            TransactionUtils.migrateTransactionsToUserSubcollection(new TransactionUtils.TransactionCallback() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "Di chuyển dữ liệu giao dịch thành công");
                }

                @Override
                public void onError(String errorMessage) {
                    Log.e(TAG, "Lỗi khi di chuyển dữ liệu giao dịch: " + errorMessage);
                    // Không gây ảnh hưởng đến trải nghiệm người dùng, vẫn cho phép tiếp tục
                }
            }, true); // Thêm tham số true để bỏ qua tài khoản mới
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi di chuyển dữ liệu: " + e.getMessage(), e);
        }
    }

    // Phương thức khởi tạo dữ liệu mẫu cho tài khoản mới
    private void initializeDataForNewAccount() {
        try {
            Log.d(TAG, "Kiểm tra số dư ban đầu cho tài khoản mới");
            // Chỉ đảm bảo số dư bằng 0, không tạo dữ liệu mẫu
            FirebaseUtils.getCurrentUserDocument().get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && !documentSnapshot.contains("balance")) {
                        FirebaseUtils.getCurrentUserDocument().update("balance", 0.0)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Đã thiết lập số dư ban đầu = 0");
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Lỗi khi thiết lập số dư ban đầu: " + e.getMessage());
                            });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi kiểm tra dữ liệu người dùng: " + e.getMessage());
                });
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi khởi tạo số dư ban đầu: " + e.getMessage(), e);
        }
    }

    private void hideProgressDialog() {
        try {
            // Ẩn giao diện loading
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            } else {
                Log.e(TAG, "progressBar là null khi gọi hideProgressDialog()");
            }
            
            // Kích hoạt lại nút đăng nhập
            if (btnLogin != null) {
                btnLogin.setEnabled(true);
            } else {
                Log.e(TAG, "btnLogin là null khi gọi hideProgressDialog()");
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi ẩn progress dialog: " + e.getMessage(), e);
        }
    }
} 