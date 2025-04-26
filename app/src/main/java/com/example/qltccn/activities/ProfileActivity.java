package com.example.qltccn.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.qltccn.R;
import com.example.qltccn.models.User;
import com.example.qltccn.utils.AuthUtils;
import com.example.qltccn.utils.UserUtils;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {
    // Khai báo biến TAG để sử dụng trong Log
    private static final String TAG = "ProfileActivity";
    
    // Toolbar
    private ImageButton toolbarBackBtn;
    
    // Profile section
    private ImageView profileImage;
    private ImageView cameraIcon;
    private TextView ev_user_name;
    private TextView ev_user_email;

    // Menu items
    private CardView cardEditProfile;
    private CardView cardSecurity;
    private CardView cardSetting;
    private CardView cardHelp;
    private CardView cardLogout;
    
    // Footer navigation
    private ImageView iconHome;
    private ImageView iconChart;
    private ImageView iconTrans;
    private ImageView iconCategory;
    private ImageView iconUser;

    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        setupListeners();
        loadUserProfile();
    }

    private void initViews() {
        // Toolbar
        toolbarBackBtn = findViewById(R.id.toolbarBackBtn);
        
        // Profile section
        profileImage = findViewById(R.id.profileImage);
        cameraIcon = findViewById(R.id.cameraIcon);
        ev_user_name = findViewById(R.id.ev_user_name);
        ev_user_email = findViewById(R.id.ev_user_email);

        
        // Menu items
        cardEditProfile = findViewById(R.id.cardEditProfile);
        cardSecurity = findViewById(R.id.cardSecurity);
        cardSetting = findViewById(R.id.cardSetting);
        cardHelp = findViewById(R.id.cardHelp);
        cardLogout = findViewById(R.id.cardLogout);
        
        // Footer navigation
        iconHome = findViewById(R.id.iconHome);
        iconChart = findViewById(R.id.iconChart);
        iconTrans = findViewById(R.id.iconTrans);
        iconCategory = findViewById(R.id.iconCategory);
        iconUser = findViewById(R.id.iconUser);
        
        // Đặt biểu tượng Profile là đã được chọn
        if (iconUser != null) {
            iconUser.setImageResource(R.drawable.ic_profile_back1);
        }
    }

    private void setupListeners() {
        // Toolbar
        toolbarBackBtn.setOnClickListener(v -> finish());
        
        // Menu items
        cardEditProfile.setOnClickListener(v -> navigateTo(EditProfileActivity.class));
        
        cardSecurity.setOnClickListener(v -> navigateTo(SecurityActivity.class));
        
        cardSetting.setOnClickListener(v -> navigateTo(SettingsActivity.class));
        
        cardHelp.setOnClickListener(v -> showHelpDialog());
        
        cardLogout.setOnClickListener(v -> showLogoutConfirmation());
        
        // Footer navigation
        iconHome.setOnClickListener(v -> navigateToAndFinish(HomeActivity.class));
        
        iconChart.setOnClickListener(v -> navigateToAndFinish(AnalysisActivity.class));
        
        iconTrans.setOnClickListener(v -> navigateToAndFinish(TranActivity.class));
        
        iconCategory.setOnClickListener(v -> navigateToAndFinish(CategoryActivity.class));
        
        // Đã đang ở màn hình Profile, không cần listener
    }

    // Phương thức điều hướng không đóng activity hiện tại
    private void navigateTo(Class<?> destinationClass) {
        try {
            Intent intent = new Intent(this, destinationClass);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi chuyển đến " + destinationClass.getSimpleName() + ": " + e.getMessage());
            Toast.makeText(this, "Không thể mở màn hình " + destinationClass.getSimpleName(), Toast.LENGTH_SHORT).show();
        }
    }
    
    // Phương thức điều hướng có đóng activity hiện tại
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

    private void loadUserProfile() {
        showProgress(true);
        
        try {
            // Kiểm tra xem người dùng đã đăng nhập chưa
            FirebaseUser firebaseUser = AuthUtils.getCurrentUser();
            if (firebaseUser == null) {
                Toast.makeText(this, "Vui lòng đăng nhập để tiếp tục", Toast.LENGTH_SHORT).show();
                redirectToLogin();
                return;
            }
            
            // Lấy thông tin người dùng từ Firestore/Realtime DB thông qua UserUtils
            UserUtils.getCurrentUser(new UserUtils.FetchUserCallback() {
                @Override
                public void onSuccess(User user) {
                    // Nếu dữ liệu người dùng được tìm thấy
                    if (user != null) {
                        currentUser = user;
                        
                        // Đảm bảo tên người dùng được lấy từ Firebase Auth nếu không có trong Firestore
                        if (user.getName() == null || user.getName().isEmpty()) {
                            if (firebaseUser.getDisplayName() != null && !firebaseUser.getDisplayName().isEmpty()) {
                                currentUser.setName(firebaseUser.getDisplayName());
                                // Cập nhật lại tên người dùng vào database
                                UserUtils.updateUserProfile(currentUser, new UserUtils.UserCallback() {
                                    @Override
                                    public void onSuccess(FirebaseUser user) {
                                        Log.d(TAG, "Đã đồng bộ tên người dùng với Firebase Auth");
                                    }
                                    
                                    @Override
                                    public void onError(String errorMessage) {
                                        Log.e(TAG, "Lỗi đồng bộ tên người dùng: " + errorMessage);
                                    }
                                });
                            }
                        }
                        
                        showProgress(false);
                        populateUI();
                    } else {
                        // Tạo đối tượng User từ thông tin Firebase Auth
                        currentUser = new User(
                            firebaseUser.getUid(),
                            firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "Người dùng",
                            firebaseUser.getEmail() != null ? firebaseUser.getEmail() : "Không có email"
                        );
                        
                        showProgress(false);
                        populateUI();
                    }
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "Lỗi khi tải thông tin người dùng: " + error);
                    
                    // Tạo đối tượng User từ thông tin Firebase Auth nếu lỗi
                    currentUser = new User(
                        firebaseUser.getUid(),
                        firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "Người dùng",
                        firebaseUser.getEmail() != null ? firebaseUser.getEmail() : "Không có email"
                    );
                    
                    showProgress(false);
                    populateUI();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Lỗi không xác định khi tải thông tin người dùng: " + e.getMessage());
            showProgress(false);
            
            // Hiển thị dữ liệu mặc định
            populateUIWithDefaultData();
        }
    }
    
    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    private void populateUIWithDefaultData() {
        ev_user_name.setText("Người dùng");
        ev_user_email.setText("Chưa có email");
        profileImage.setImageResource(R.drawable.profile);
    }

    private void populateUI() {
        try {
            if (currentUser == null) {
                currentUser = new User(); // Tạo user trống để tránh lỗi null
            }
            
            // Hiển thị tên người dùng
            if (currentUser.getName() != null && !currentUser.getName().isEmpty()) {
                ev_user_name.setText(currentUser.getName());
                Log.d(TAG, "Hiển thị tên người dùng: " + currentUser.getName());
            } else {
                FirebaseUser firebaseUser = AuthUtils.getCurrentUser();
                if (firebaseUser != null && firebaseUser.getDisplayName() != null && !firebaseUser.getDisplayName().isEmpty()) {
                    ev_user_name.setText(firebaseUser.getDisplayName());
                    Log.d(TAG, "Sử dụng tên từ Firebase Auth: " + firebaseUser.getDisplayName());
                    
                    // Cập nhật lại currentUser
                    currentUser.setName(firebaseUser.getDisplayName());
                } else {
                    ev_user_name.setText("Người dùng");
                    Log.d(TAG, "Sử dụng tên mặc định: Người dùng");
                }
            }
            
            // Hiển thị email
            if (currentUser.getEmail() != null && !currentUser.getEmail().isEmpty()) {
                ev_user_email.setText(currentUser.getEmail());
            } else {
                FirebaseUser firebaseUser = AuthUtils.getCurrentUser();
                if (firebaseUser != null && firebaseUser.getEmail() != null) {
                    ev_user_email.setText(firebaseUser.getEmail());
                    currentUser.setEmail(firebaseUser.getEmail());
                } else {
                    ev_user_email.setText("Chưa có email");
                }
            }
            
            // Thiết lập ảnh mặc định trước khi tải ảnh từ URL
            profileImage.setImageResource(R.drawable.profile);
            
            // Load avatar nếu có
            if (currentUser.getProfileImageUrl() != null && !currentUser.getProfileImageUrl().isEmpty()) {
                try {
                    // Sử dụng Glide để tải ảnh đại diện
                    com.bumptech.glide.Glide.with(this)
                        .load(currentUser.getProfileImageUrl())
                        .placeholder(R.drawable.profile)
                        .error(R.drawable.profile)
                        .circleCrop()
                        .into(profileImage);
                    Log.d(TAG, "Đang tải ảnh đại diện từ: " + currentUser.getProfileImageUrl());
                } catch (Exception e) {
                    Log.e(TAG, "Lỗi khi tải ảnh đại diện: " + e.getMessage());
                    profileImage.setImageResource(R.drawable.profile);
                }
            } else {
                // Kiểm tra xem Firebase Auth có URL ảnh không
                FirebaseUser firebaseUser = AuthUtils.getCurrentUser();
                if (firebaseUser != null && firebaseUser.getPhotoUrl() != null) {
                    try {
                        // Sử dụng Glide để tải ảnh đại diện
                        com.bumptech.glide.Glide.with(this)
                            .load(firebaseUser.getPhotoUrl())
                            .placeholder(R.drawable.profile)
                            .error(R.drawable.profile)
                            .circleCrop()
                            .into(profileImage);
                        Log.d(TAG, "Đang tải ảnh đại diện từ Firebase Auth: " + firebaseUser.getPhotoUrl());
                    } catch (Exception e) {
                        Log.e(TAG, "Lỗi khi tải ảnh đại diện từ Firebase Auth: " + e.getMessage());
                        profileImage.setImageResource(R.drawable.profile);
                    }
                } else {
                    // Sử dụng avatar mặc định
                    profileImage.setImageResource(R.drawable.profile);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi hiển thị thông tin người dùng: " + e.getMessage());
            populateUIWithDefaultData();
        }
    }
    
    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> logout())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void logout() {
        AuthUtils.signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showProgress(boolean show) {
        try {
            // Tìm ProgressBar trong layout
            View progressBar = findViewById(R.id.progressBar);
            if (progressBar != null) {
                progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
            
            // Đảm bảo kiểm tra null trước khi truy cập các View
            if (ev_user_name != null) {
                ev_user_name.setEnabled(!show);
                ev_user_name.setAlpha(show ? 0.5f : 1f);
            }
            
            if (ev_user_email != null) {
                ev_user_email.setEnabled(!show);
                ev_user_email.setAlpha(show ? 0.5f : 1f);
            }
            
            // Kiểm tra null trước khi truy cập vào các CardView
            if (cardEditProfile != null) cardEditProfile.setEnabled(!show);
            if (cardSecurity != null) cardSecurity.setEnabled(!show);
            if (cardSetting != null) cardSetting.setEnabled(!show);
            if (cardHelp != null) cardHelp.setEnabled(!show);
            if (cardLogout != null) cardLogout.setEnabled(!show);
            
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi hiển thị/ẩn trạng thái loading: " + e.getMessage());
        }
    }

    private void showHelpDialog() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Trợ giúp");
            
            // Không cần tải layout dialog_help, chỉ hiển thị thông báo văn bản
            String helpMessage = "Quản lý tài chính cá nhân là ứng dụng giúp bạn theo dõi thu chi cá nhân.\n\n" +
                    "• Truy cập màn hình Thêm giao dịch để ghi lại khoản thu chi\n" +
                    "• Xem báo cáo thống kê tại màn hình Phân tích\n" +
                    "• Tùy chỉnh danh mục tại màn hình Danh mục\n" +
                    "• Quản lý thông tin cá nhân tại màn hình Hồ sơ\n\n" +
                    "Phiên bản: 1.0.0\n" +
                    "Liên hệ hỗ trợ: support@qltccn.com";
            
            builder.setMessage(helpMessage);
            
            builder.setPositiveButton("Đóng", (dialog, which) -> dialog.dismiss());
            
            // Thêm nút liên hệ hỗ trợ nếu cần
            builder.setNeutralButton("Liên hệ hỗ trợ", (dialog, which) -> {
                // Gửi email đến địa chỉ hỗ trợ
                try {
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(android.net.Uri.parse("mailto:support@qltccn.com"));
                    
                    // Chỉ đính kèm thông tin người dùng nếu có
                    if (currentUser != null && currentUser.getName() != null) {
                        intent.putExtra(Intent.EXTRA_SUBJECT, "Hỗ trợ QLTCCN - " + currentUser.getName());
                    } else {
                        intent.putExtra(Intent.EXTRA_SUBJECT, "Hỗ trợ QLTCCN");
                    }
                    
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(Intent.createChooser(intent, "Gửi email..."));
                    } else {
                        Toast.makeText(this, "Không tìm thấy ứng dụng email", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Lỗi khi mở email: " + e.getMessage());
                    Toast.makeText(this, "Không thể gửi email", Toast.LENGTH_SHORT).show();
                }
            });
            
            AlertDialog dialog = builder.create();
            dialog.show();
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi hiển thị dialog trợ giúp: " + e.getMessage());
            Toast.makeText(this, "Không thể hiển thị thông tin trợ giúp", Toast.LENGTH_SHORT).show();
        }
    }
} 