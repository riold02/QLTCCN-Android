package com.example.qltccn.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.qltccn.R;
import com.example.qltccn.utils.AuthUtils;
import com.example.qltccn.utils.DateUtils;
import com.example.qltccn.utils.FirebaseUtils;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SignUpActivity extends AppCompatActivity {
    private static final String TAG = "SignUpActivity";

    private EditText edtFullName, edtEmail, edtMobileNumber, edtDateOfBirth, edtPassword, edtConfirmPassword;
    private Button btnSignUp;
    private TextView tvAlreadyAccount;
    private CheckBox cbTerms;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize views
        initViews();

        // Set click listeners
        setClickListeners();
    }

    private void initViews() {
        edtFullName = findViewById(R.id.edtFullName);
        edtEmail = findViewById(R.id.edtEmail);
        edtMobileNumber = findViewById(R.id.edtMobileNumber);
        edtDateOfBirth = findViewById(R.id.edtDateOfBirth);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvAlreadyAccount = findViewById(R.id.tvAlreadyAccount);
        cbTerms = findViewById(R.id.cbTerms);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setClickListeners() {
        btnSignUp.setOnClickListener(v -> {
            if (validateInputs()) {
                registerUser();
            }
        });

        tvAlreadyAccount.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private boolean validateInputs() {
        String fullName = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String mobileNumber = edtMobileNumber.getText().toString().trim();
        String dateOfBirth = edtDateOfBirth.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        // Validate full name
        if (TextUtils.isEmpty(fullName)) {
            edtFullName.setError("Please enter your full name");
            return false;
        }

        // Validate email
        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Please enter your email");
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Please enter a valid email");
            return false;
        }

        // Validate mobile number
        if (TextUtils.isEmpty(mobileNumber)) {
            edtMobileNumber.setError("Please enter your mobile number");
            return false;
        }

        // Validate date of birth
        if (TextUtils.isEmpty(dateOfBirth)) {
            edtDateOfBirth.setError("Please enter your date of birth");
            return false;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date dob = sdf.parse(dateOfBirth);
            if (dob.after(new Date())) {
                edtDateOfBirth.setError("Date of birth cannot be in the future");
                return false;
            }
        } catch (ParseException e) {
            edtDateOfBirth.setError("Please enter a valid date (DD/MM/YYYY)");
            return false;
        }

        // Validate password
        if (TextUtils.isEmpty(password)) {
            edtPassword.setError("Please enter a password");
            return false;
        }
        if (password.length() < 6) {
            edtPassword.setError("Password must be at least 6 characters");
            return false;
        }

        // Validate confirm password
        if (TextUtils.isEmpty(confirmPassword)) {
            edtConfirmPassword.setError("Please confirm your password");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            edtConfirmPassword.setError("Passwords do not match");
            return false;
        }

        // Validate terms and conditions
        if (!cbTerms.isChecked()) {
            Toast.makeText(this, "Please accept the terms and conditions", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void registerUser() {
        try {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            String fullName = edtFullName.getText().toString().trim();
            String mobileNumber = edtMobileNumber.getText().toString().trim();
            String dateOfBirth = edtDateOfBirth.getText().toString().trim();

            // Kiểm tra kết nối mạng
            if (!isNetworkConnected()) {
                Toast.makeText(this, "Không có kết nối mạng. Vui lòng kiểm tra WiFi hoặc dữ liệu di động.", Toast.LENGTH_LONG).show();
                return;
            }

            // Show progress
            btnSignUp.setEnabled(false);
            showProgress(true);

            // Ghi log thông tin đăng ký
            Log.d(TAG, "Bắt đầu đăng ký với email: " + email);

            // Thực hiện đăng ký trực tiếp
            performSignUp(email, password, fullName, mobileNumber, dateOfBirth);
        } catch (Exception e) {
            // Xử lý mọi ngoại lệ có thể xảy ra
            showProgress(false);
            if (btnSignUp != null) {
                btnSignUp.setEnabled(true);
            }
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "Lỗi không xác định: " + e.getMessage(), e);
        }
    }

    private void performSignUp(String email, String password, String fullName, String mobileNumber, String dateOfBirth) {
        try {
            // Register user with Firebase with all information
            AuthUtils.signUp(this, email, password, fullName, mobileNumber, dateOfBirth, new AuthUtils.AuthCallback() {
                @Override
                public void onSuccess(FirebaseUser user) {
                    try {
                        showProgress(false);
                        btnSignUp.setEnabled(true);

                        if (user != null) {
                            Log.d(TAG, "Đăng ký thành công với UID: " + user.getUid());
                            completeRegistrationAndNavigate(user);
                        } else {
                            Log.w(TAG, "Đăng ký thành công nhưng user là null");
                            Toast.makeText(SignUpActivity.this, "Đăng ký thành công nhưng không thể tải dữ liệu người dùng", Toast.LENGTH_LONG).show();
                            // Chuyển về màn hình đăng nhập
                            Intent loginIntent = new Intent(SignUpActivity.this, LoginActivity.class);
                            startActivity(loginIntent);
                            finish();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Lỗi xử lý thành công: " + e.getMessage(), e);
                        showProgress(false);
                        btnSignUp.setEnabled(true);
                        // Vẫn chuyển về màn hình đăng nhập
                        Intent loginIntent = new Intent(SignUpActivity.this, LoginActivity.class);
                        startActivity(loginIntent);
                        finish();
                    }
                }

                @Override
                public void onError(String error) {
                    try {
                        showProgress(false);
                        btnSignUp.setEnabled(true);
                        Toast.makeText(SignUpActivity.this, "Lỗi đăng ký: " + error, Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Lỗi đăng ký: " + error);
                    } catch (Exception e) {
                        Log.e(TAG, "Lỗi xử lý lỗi: " + e.getMessage(), e);
                    }
                }
            });
        } catch (Exception e) {
            showProgress(false);
            btnSignUp.setEnabled(true);
            Log.e(TAG, "Lỗi nghiêm trọng khi đăng ký: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi nghiêm trọng: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // Hoàn tất quá trình đăng ký và chuyển đến màn hình chính
    private void completeRegistrationAndNavigate(FirebaseUser user) {
        try {
            hideProgressDialog();

            // Thông báo đăng ký thành công
            Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();

            // Chuyển đến màn hình chính
            Intent mainIntent = new Intent(SignUpActivity.this, HomeActivity.class);
            mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(mainIntent);
            finish();

        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi hoàn tất đăng ký: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            // Vẫn chuyển về màn hình đăng nhập
            Intent loginIntent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
        }
    }

    // Phương thức ẩn dialog tiến trình
    private void hideProgressDialog() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    // Phương thức hiển thị hoặc ẩn ProgressBar
    private void showProgress(boolean show) {
        try {
            if (progressBar != null) {
                progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi hiển thị progress: " + e.getMessage());
        }
    }

    // Kiểm tra kết nối mạng
    private boolean isNetworkConnected() {
        try {
            android.net.ConnectivityManager connectivityManager =
                    (android.net.ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    android.net.Network activeNetwork = connectivityManager.getActiveNetwork();
                    if (activeNetwork != null) {
                        android.net.NetworkCapabilities networkCapabilities =
                                connectivityManager.getNetworkCapabilities(activeNetwork);
                        return networkCapabilities != null &&
                                (networkCapabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) ||
                                        networkCapabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR));
                    }
                } else {
                    android.net.NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi kiểm tra kết nối mạng: " + e.getMessage(), e);
        }
        return false;
    }
}