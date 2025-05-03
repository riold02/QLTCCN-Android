package com.example.qltccn.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.qltccn.R;
import com.example.qltccn.models.User;
import com.example.qltccn.utils.UserUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {
    private static final String TAG = "EditProfileActivity";
    private static final int PICK_IMAGE_REQUEST = 1;
    // Mã kết quả để xác định đã cập nhật thành công
    public static final int RESULT_PROFILE_UPDATED = 1001;

    private ImageView toolbarBackBtn;
    private ImageView profileImage;
    private ImageView changePhotoBtn;
    private TextView profileName;
    private TextView profileId;
    private EditText editUsername;
    private EditText editPhone;
    private EditText editEmail;
    private Button btnUpdateProfile;
    private ProgressBar progressBar;
    private Uri imageUri;

    // Footer navigation
    private ImageView iconHome;
    private ImageView iconChart;
    private ImageView iconTrans;
    private ImageView iconCategory;
    private ImageView iconUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initViews();
        setupListeners();
        loadUserProfile();
    }

    private void initViews() {
        toolbarBackBtn = findViewById(R.id.toolbarBackBtn);
        profileImage = findViewById(R.id.profileImage);
        changePhotoBtn = findViewById(R.id.changePhotoBtn);
        profileName = findViewById(R.id.profileName);
        profileId = findViewById(R.id.profileId);
        editUsername = findViewById(R.id.editUsername);
        editPhone = findViewById(R.id.editPhone);
        editEmail = findViewById(R.id.editEmail);
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);
        
        // Footer navigation
        iconHome = findViewById(R.id.iconHome);
        iconChart = findViewById(R.id.iconChart);
        iconTrans = findViewById(R.id.iconTrans);
        iconCategory = findViewById(R.id.iconCategory);
        iconUser = findViewById(R.id.iconUser);
    }

    private void setupListeners() {
        toolbarBackBtn.setOnClickListener(v -> finish());

        changePhotoBtn.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
        });

        btnUpdateProfile.setOnClickListener(v -> saveProfile());

        // Footer navigation listeners
        iconHome.setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });
        iconChart.setOnClickListener(v -> {
            startActivity(new Intent(this, AnalysisActivity.class));
            finish();
        });
        iconTrans.setOnClickListener(v -> {
            startActivity(new Intent(this, TranActivity.class));
            finish();
        });
        iconCategory.setOnClickListener(v -> {
            startActivity(new Intent(this, CategoryActivity.class));
            finish();
        });
        iconUser.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
        });
    }

    private void loadUserProfile() {
        showProgress(true);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Người dùng chưa đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        UserUtils.getCurrentUser(new UserUtils.FetchUserCallback() {
            @Override
            public void onSuccess(User user) {
                showProgress(false);
                if (user != null) {
                    updateUI(user);
                } else {
                    loadUserFromRealtimeDB(currentUser.getUid());
                }
            }

            @Override
            public void onError(String error) {
                showProgress(false);
                // Thử tải từ Realtime Database
                loadUserFromRealtimeDB(currentUser.getUid());
            }
        });
    }
    
    private void loadUserFromRealtimeDB(String userId) {
        com.google.firebase.database.DatabaseReference userRef = 
                FirebaseDatabase.getInstance().getReference().child("users").child(userId);
        
        userRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        updateUI(user);
                    } else {
                        Toast.makeText(EditProfileActivity.this, "Không thể tải thông tin người dùng", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(EditProfileActivity.this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                }
                showProgress(false);
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                showProgress(false);
                Toast.makeText(EditProfileActivity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void updateUI(User user) {
        profileName.setText(user.getName() != null ? user.getName() : "Người dùng");
        profileId.setText("ID: " + user.getId());
        editUsername.setText(user.getName() != null ? user.getName() : "");
        editPhone.setText(user.getPhone() != null ? user.getPhone() : "");
        editEmail.setText(user.getEmail() != null ? user.getEmail() : "");
        
        // Load avatar if exists
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            try {
                // Sử dụng Glide để tải ảnh đại diện
                com.bumptech.glide.Glide.with(this)
                    .load(user.getProfileImageUrl())
                    .placeholder(R.drawable.profile)
                    .error(R.drawable.profile)
                    .circleCrop()
                    .into(profileImage);
                Log.d(TAG, "Đang tải ảnh đại diện từ: " + user.getProfileImageUrl());
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi tải ảnh đại diện: " + e.getMessage());
                profileImage.setImageResource(R.drawable.profile);
            }
        } else {
            // Sử dụng avatar mặc định
            profileImage.setImageResource(R.drawable.profile);
        }
    }

    private void saveProfile() {
        String name = editUsername.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String email = editEmail.getText().toString().trim();

        if (name.isEmpty()) {
            editUsername.setError("Tên không được để trống");
            return;
        }

        btnUpdateProfile.setEnabled(false);
        showProgress(true);
        
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Người dùng chưa đăng nhập", Toast.LENGTH_SHORT).show();
            btnUpdateProfile.setEnabled(true);
            showProgress(false);
            return;
        }

        // Tạo đối tượng User với thông tin mới
        User user = new User();
        user.setId(currentUser.getUid());
        user.setName(name);
        user.setPhone(phone);
        user.setEmail(email);
        
        // Lưu lại URL ảnh hiện có (nếu có)
        if (currentUser.getPhotoUrl() != null) {
            user.setProfileImageUrl(currentUser.getPhotoUrl().toString());
        }

        // Kiểm tra xem người dùng đã chọn ảnh mới chưa
        if (imageUri != null) {
            // Hiển thị thông báo đang tải ảnh lên
            Toast.makeText(this, "Đang tải ảnh lên...", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Đang tải ảnh mới lên: " + imageUri.toString());
            
            // Debug thêm về file và quyền
            try {
                String filePath = com.example.qltccn.utils.FileUtils.getPath(this, imageUri);
                Log.d(TAG, "Real file path: " + filePath);
                
                // Tạo file tạm trong bộ nhớ cache
                java.io.File cacheDir = getCacheDir();
                java.io.File tempFile = new java.io.File(cacheDir, "profile_" + user.getId() + ".jpg");
                
                try {
                    // Sao chép ảnh vào file tạm
                    java.io.InputStream inputStream = getContentResolver().openInputStream(imageUri);
                    if (inputStream == null) {
                        Toast.makeText(this, "Không thể đọc dữ liệu từ ảnh đã chọn", Toast.LENGTH_SHORT).show();
                        updateUserProfile(user);
                        return;
                    }
                    
                    java.io.FileOutputStream outputStream = new java.io.FileOutputStream(tempFile);
                    byte[] buffer = new byte[4 * 1024]; // 4k buffer
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    
                    inputStream.close();
                    outputStream.flush();
                    outputStream.close();
                    
                    // Sử dụng URI của file tạm để tải lên
                    imageUri = Uri.fromFile(tempFile);
                    Log.d(TAG, "Đã tạo file tạm: " + tempFile.getAbsolutePath());
                    Log.d(TAG, "URI mới: " + imageUri.toString());
                } catch (Exception e) {
                    Log.e(TAG, "Lỗi khi tạo file tạm: " + e.getMessage(), e);
                    Toast.makeText(this, "Lỗi xử lý ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                
                // Kiểm tra quyền truy cập Storage
                boolean isLoggedIn = com.example.qltccn.utils.FirebaseUtils.isUserLoggedIn();
                String userId = com.example.qltccn.utils.FirebaseUtils.getCurrentUserId();
                com.google.firebase.storage.StorageReference storageRef = com.example.qltccn.utils.FirebaseUtils.getCurrentUserProfileImageRef();
                
                Log.d(TAG, "User logged in: " + isLoggedIn);
                Log.d(TAG, "Current user ID: " + userId);
                Log.d(TAG, "Storage reference: " + (storageRef != null ? storageRef.getPath() : "null"));
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi kiểm tra file: " + e.getMessage(), e);
            }
            
            // Sử dụng phương thức mới có truyền context
            UserUtils.updateProfileImageWithContext(this, imageUri, new UserUtils.UserCallback() {
                @Override
                public void onSuccess(FirebaseUser firebaseUser) {
                    Log.d(TAG, "Tải ảnh lên thành công");
                    Toast.makeText(EditProfileActivity.this, "Đã cập nhật ảnh đại diện", Toast.LENGTH_SHORT).show();
                    
                    // Cập nhật URL ảnh mới từ firebaseUser vào user
                    if (firebaseUser.getPhotoUrl() != null) {
                        user.setProfileImageUrl(firebaseUser.getPhotoUrl().toString());
                        Log.d(TAG, "Đã cập nhật URL ảnh mới: " + user.getProfileImageUrl());
                    }
                    
                    // Cập nhật thông tin người dùng sau khi tải ảnh lên thành công
                    updateUserProfile(user);
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "Lỗi cập nhật ảnh: " + error);
                    
                    // Hiển thị lỗi nhưng vẫn tiếp tục cập nhật thông tin khác
                    Toast.makeText(EditProfileActivity.this, "Lỗi cập nhật ảnh: " + error, Toast.LENGTH_SHORT).show();
                    
                    // Vẫn tiếp tục cập nhật các thông tin khác
                    updateUserProfile(user);
                    
                    btnUpdateProfile.setEnabled(true);
                    showProgress(false);
                }
            });
        } else {
            // Nếu không có ảnh mới, chỉ cập nhật thông tin người dùng
            updateUserProfile(user);
        }
    }

    private void updateUserProfile(User user) {
        Log.d(TAG, "Bắt đầu cập nhật thông tin người dùng: " + user.getName());
        
        // Bỏ qua việc đồng bộ vào Realtime Database vì gặp lỗi permission
        // Chỉ cập nhật trực tiếp vào Firestore
        syncWithFirestore(user);
    }
    
    private void syncWithFirestore(User user) {
        Log.d(TAG, "Đang đồng bộ thông tin với Firestore...");
        
        UserUtils.updateUserProfile(user, new UserUtils.UserCallback() {
            @Override
            public void onSuccess(FirebaseUser firebaseUser) {
                Log.d(TAG, "Đã cập nhật thông tin trong Firestore và Firebase Auth");
                
                // Cập nhật lại UI để hiển thị thông tin mới
                if (user.getName() != null) {
                    profileName.setText(user.getName());
                }
                
                btnUpdateProfile.setEnabled(true);
                showProgress(false);
                Toast.makeText(EditProfileActivity.this, "Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show();
                
                // Đặt kết quả là đã cập nhật thành công
                setResult(RESULT_PROFILE_UPDATED);
                
                // Đóng màn hình và quay lại ProfileActivity
                finish();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Lỗi cập nhật Firestore: " + error);
                btnUpdateProfile.setEnabled(true);
                showProgress(false);
                Toast.makeText(EditProfileActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showProgress(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            
            // Hiển thị chi tiết về URI để debug
            Log.d(TAG, "URI ảnh đã chọn: " + imageUri);
            Log.d(TAG, "URI Scheme: " + imageUri.getScheme());
            Log.d(TAG, "URI Authority: " + imageUri.getAuthority());
            Log.d(TAG, "URI Path: " + imageUri.getPath());
            
            // Hiển thị hình ảnh đã chọn bằng Glide để xử lý tốt hơn
            try {
                com.bumptech.glide.Glide.with(this)
                    .load(imageUri)
                    .circleCrop()
                    .into(profileImage);
                Log.d(TAG, "Đã chọn ảnh mới từ URI: " + imageUri);
                
                // Kiểm tra nếu ảnh lớn hơn 1MB thì hiển thị thông báo cảnh báo
                try {
                    android.database.Cursor cursor = getContentResolver().query(imageUri, null, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        int sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE);
                        int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                        
                        String fileName = nameIndex > -1 ? cursor.getString(nameIndex) : "unknown";
                        long size = cursor.getLong(sizeIndex);
                        cursor.close();
                        
                        Log.d(TAG, "Tên file ảnh: " + fileName + ", Kích thước: " + (size / 1024) + "KB");
                        
                        if (size > 1024 * 1024) { // Lớn hơn 1MB
                            Toast.makeText(this, "Ảnh có kích thước lớn, có thể mất nhiều thời gian để tải lên", Toast.LENGTH_LONG).show();
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Lỗi khi kiểm tra kích thước ảnh: " + e.getMessage(), e);
                }
            } catch (Exception e) {
                Log.e(TAG, "Lỗi hiển thị ảnh đã chọn: " + e.getMessage(), e);
                // Fallback để hiển thị ảnh mà không qua Glide nếu có lỗi
                profileImage.setImageURI(imageUri);
            }
        }
    }
} 