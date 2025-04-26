package com.example.qltccn.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.qltccn.R;
import com.example.qltccn.utils.FirebaseUtils;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private static final int SPLASH_TIMEOUT = 2500; // Thời gian hiển thị màn hình chào (mili giây)
    
    private ImageView logoImage;
    private TextView appName;
    private View loadingDots;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Thiết lập toàn màn hình
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        
        setContentView(R.layout.activity_splash);
        
        // Ánh xạ các view
        logoImage = findViewById(R.id.logoImage);
        appName = findViewById(R.id.appName);
        loadingDots = findViewById(R.id.loadingDots);
        
        // Tạo hiệu ứng
        Animation topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        Animation bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);
        
        // Áp dụng hiệu ứng
        logoImage.setAnimation(topAnim);
        appName.setAnimation(bottomAnim);
        
        // Kiểm tra người dùng đã đăng nhập chưa và chuyển màn hình tương ứng
        checkUserLoginStatus();
    }
    
    private void checkUserLoginStatus() {
        // Hiển thị loading
        if (loadingDots != null) {
            loadingDots.setVisibility(View.VISIBLE);
        }
        
        try {
            FirebaseUser currentUser = FirebaseUtils.getCurrentUser();
            
            if (currentUser != null) {
                // Đã đăng nhập, kiểm tra dữ liệu
                checkUserData(currentUser.getUid());
            } else {
                // Chưa đăng nhập, chờ một chút rồi chuyển đến màn hình Login
                new Handler().postDelayed(() -> navigateToLoginScreen(), SPLASH_TIMEOUT);
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi kiểm tra trạng thái đăng nhập: " + e.getMessage(), e);
            // Chuyển hướng đến màn hình đăng nhập nếu có lỗi
            new Handler().postDelayed(() -> navigateToLoginScreen(), SPLASH_TIMEOUT);
        }
    }
    
    private void checkUserData(String userId) {
        // Kiểm tra dữ liệu người dùng trực tiếp
        try {
            FirebaseUtils.getUsersCollection()
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Đã tìm thấy dữ liệu người dùng, điều hướng đến màn hình chính
                        Log.d(TAG, "Tìm thấy dữ liệu người dùng trong Firestore");
                        new Handler().postDelayed(() -> navigateToMainScreen(), SPLASH_TIMEOUT);
                    } else {
                        // Không tìm thấy dữ liệu người dùng, đăng xuất và điều hướng đến màn hình đăng nhập
                        Log.w(TAG, "Không tìm thấy dữ liệu người dùng");
                        FirebaseUtils.getAuth().signOut();
                        new Handler().postDelayed(() -> navigateToLoginScreen(), SPLASH_TIMEOUT);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi truy vấn dữ liệu người dùng: " + e.getMessage());
                    // Nếu có lỗi thì vẫn đăng xuất và điều hướng đến màn hình đăng nhập
                    FirebaseUtils.getAuth().signOut();
                    new Handler().postDelayed(() -> {
                        navigateToLoginScreen();
                        showErrorToast("Không thể kết nối đến máy chủ: " + e.getMessage());
                    }, SPLASH_TIMEOUT);
                });
        } catch (Exception e) {
            Log.e(TAG, "Lỗi kiểm tra dữ liệu người dùng: " + e.getMessage(), e);
            FirebaseUtils.getAuth().signOut();
            new Handler().postDelayed(() -> navigateToLoginScreen(), SPLASH_TIMEOUT);
        }
    }
    
    private void showErrorToast(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_LONG).show();
    }
    
    private void navigateToMainScreen() {
        Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
    
    private void navigateToLoginScreen() {
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}