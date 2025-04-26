package com.example.qltccn.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.qltccn.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UserUtils {
    
    private static final String TAG = "UserUtils";
    
    // Interface for user callbacks
    public interface UserCallback {
        void onSuccess(FirebaseUser user);
        void onError(String errorMessage);
    }
    
    // Interface for fetching user
    public interface FetchUserCallback {
        void onSuccess(User user);
        void onError(String errorMessage);
    }
    
    // Interface for login callback
    public interface LoginCallback {
        void onSuccess(FirebaseUser user);
        void onError(String errorMessage, String errorCode);
    }
    
    // Login with email and password
    public static void loginWithEmailAndPassword(String email, String password, LoginCallback callback) {
        Log.d(TAG, "Attempting login with email: " + email);
        
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        Log.d(TAG, "Login successful: " + user.getUid());
                        callback.onSuccess(user);
                    } else {
                        String errorCode = "";
                        if (task.getException() instanceof FirebaseAuthException) {
                            errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                        }
                        
                        String errorMessage = task.getException() != null ? 
                                task.getException().getMessage() : "Login failed";
                        
                        Log.e(TAG, "Login failed: " + errorMessage + " (Code: " + errorCode + ")");
                        callback.onError(errorMessage, errorCode);
                    }
                });
    }
    
    // Get current user from Firestore
    public static void getCurrentUser(FetchUserCallback callback) {
        FirebaseUser firebaseUser = FirebaseUtils.getCurrentUser();
        if (firebaseUser == null) {
            Log.e(TAG, "getCurrentUser: User not logged in");
            callback.onError("User not logged in");
            return;
        }
        
        Log.d(TAG, "Getting user data for: " + firebaseUser.getUid());
        
        FirebaseUtils.getUsersCollection()
                .document(firebaseUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    try {
                        if (documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            if (user != null) {
                                // Đảm bảo dữ liệu cơ bản
                                if (user.getId() == null) {
                                    user.setId(firebaseUser.getUid());
                                }
                                if (user.getName() == null || user.getName().isEmpty()) {
                                    user.setName(firebaseUser.getDisplayName() != null ? 
                                            firebaseUser.getDisplayName() : "");
                                }
                                if (user.getEmail() == null || user.getEmail().isEmpty()) {
                                    user.setEmail(firebaseUser.getEmail());
                                }
                                
                                // Đảm bảo số dư không bị null
                                if (Double.isNaN(user.getBalance())) {
                                    user.setBalance(0.0);
                                }
                                
                                Log.d(TAG, "User data retrieved successfully: " + user.getName() + ", Balance: " + user.getBalance());
                                callback.onSuccess(user);
                            } else {
                                // Tạo user mới nếu toObject trả về null (lỗi convert)
                                Log.w(TAG, "User data exists but conversion failed, creating default user");
                                createAndReturnDefaultUser(firebaseUser, callback);
                            }
                        } else {
                            // Nếu không tìm thấy dữ liệu, tạo user mới
                            Log.w(TAG, "User data not found in Firestore, creating default user");
                            createAndReturnDefaultUser(firebaseUser, callback);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing user data: " + e.getMessage(), e);
                        createAndReturnDefaultUser(firebaseUser, callback);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get user data: " + e.getMessage());
                    createAndReturnDefaultUser(firebaseUser, callback);
                });
    }
    
    // Tạo người dùng mặc định và trả về ngay lập tức
    private static void createAndReturnDefaultUser(FirebaseUser firebaseUser, FetchUserCallback callback) {
        User defaultUser = new User(
            firebaseUser.getUid(),
            firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "",
            firebaseUser.getEmail() != null ? firebaseUser.getEmail() : ""
        );
        
        // Trả về user mặc định ngay lập tức
        callback.onSuccess(defaultUser);
        
        // Lưu user mặc định vào Firestore trong background
        FirebaseUtils.getUsersCollection()
            .document(firebaseUser.getUid())
            .set(defaultUser)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Default user saved to Firestore");
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to save default user: " + e.getMessage());
            });
    }
    
    // Update user profile
    public static void updateUserProfile(User user, UserCallback callback) {
        FirebaseUser firebaseUser = FirebaseUtils.getCurrentUser();
        if (firebaseUser == null) {
            callback.onError("User not logged in");
            return;
        }
        
        // Update timestamp
        user.updateTimestamp();
        
        // Convert user to map
        Map<String, Object> userMap = userToMap(user);
        
        // Cập nhật tên hiển thị trong Firebase Auth
        firebaseUser.updateProfile(new com.google.firebase.auth.UserProfileChangeRequest.Builder()
            .setDisplayName(user.getName())
            .build())
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Đã cập nhật tên hiển thị trong Firebase Auth");
                
                // Chỉ cập nhật vào Firestore
                FirebaseUtils.getUsersCollection()
                    .document(firebaseUser.getUid())
                    .update(userMap)
                    .addOnSuccessListener(aVoid3 -> {
                        Log.d(TAG, "Đã cập nhật thông tin người dùng vào Firestore");
                        callback.onSuccess(firebaseUser);
                    })
                    .addOnFailureListener(e2 -> {
                        callback.onError("Lỗi cập nhật dữ liệu: " + e2.getMessage());
                    });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Lỗi cập nhật tên hiển thị: " + e.getMessage());
                callback.onError("Lỗi cập nhật tên hiển thị: " + e.getMessage());
            });
    }
    
    // Update user profile image
    public static void updateUserProfileImage(Uri imageUri, OnCompleteListener<UploadTask.TaskSnapshot> listener) {
        try {
            String userId = FirebaseUtils.getCurrentUserId();
            if (userId == null) {
                // Đảm bảo callback luôn được gọi
                listener.onComplete(Tasks.forException(new Exception("Không có người dùng đăng nhập")));
                return;
            }

            // Lấy tham chiếu đến vị trí lưu trữ ảnh đại diện
            StorageReference profileImageRef = FirebaseUtils.getCurrentUserProfileImageRef();
            if (profileImageRef == null) {
                listener.onComplete(Tasks.forException(new Exception("Không thể tạo tham chiếu lưu trữ")));
                return;
            }

            // Thiết lập metadata để đảm bảo ảnh được xử lý đúng
            com.google.firebase.storage.StorageMetadata metadata = new com.google.firebase.storage.StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .build();
                
            Log.d(TAG, "Bắt đầu tải ảnh lên cho user ID: " + userId);
            Log.d(TAG, "Đường dẫn lưu trữ: " + profileImageRef.getPath());

            // Tải ảnh lên Firebase Storage với metadata
            UploadTask uploadTask = profileImageRef.putFile(imageUri, metadata);
            
            // Theo dõi tiến độ tải lên
            uploadTask.addOnProgressListener(taskSnapshot -> {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                Log.d(TAG, "Tiến độ tải lên: " + progress + "%");
            });
            
            // Thêm các listener để xử lý lỗi và hoàn thành
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                Log.d(TAG, "Tải ảnh lên thành công, kích thước: " + 
                    (taskSnapshot.getTotalByteCount() / 1024) + "KB");
            });
            
            uploadTask.addOnFailureListener(e -> {
                Log.e(TAG, "Lỗi tải ảnh lên: " + e.getMessage());
                // Phân tích chi tiết lỗi
                if (e instanceof com.google.firebase.storage.StorageException) {
                    com.google.firebase.storage.StorageException storageException = (com.google.firebase.storage.StorageException) e;
                    int errorCode = storageException.getErrorCode();
                    String errorMessage = "Mã lỗi Storage: " + errorCode;
                    
                    switch (errorCode) {
                        case com.google.firebase.storage.StorageException.ERROR_NOT_AUTHENTICATED:
                            errorMessage += " - Người dùng chưa đăng nhập";
                            break;
                        case com.google.firebase.storage.StorageException.ERROR_NOT_AUTHORIZED:
                            errorMessage += " - Không đủ quyền truy cập";
                            break;
                        case com.google.firebase.storage.StorageException.ERROR_QUOTA_EXCEEDED:
                            errorMessage += " - Vượt quá giới hạn lưu trữ";
                            break;
                        case com.google.firebase.storage.StorageException.ERROR_RETRY_LIMIT_EXCEEDED:
                            errorMessage += " - Vượt quá số lần thử lại";
                            break;
                    }
                    
                    Log.e(TAG, errorMessage);
                }
            });
            
            // Gọi listener khi hoàn thành
            uploadTask.addOnCompleteListener(listener);
            
        } catch (Exception e) {
            Log.e(TAG, "Lỗi cập nhật ảnh đại diện: " + e.getMessage(), e);
            listener.onComplete(Tasks.forException(e));
        }
    }
    
    // Update profile image with context
    public static void updateProfileImageWithContext(android.content.Context context, Uri imageUri, UserCallback callback) {
        try {
            String userId = FirebaseUtils.getCurrentUserId();
            if (userId == null) {
                callback.onError("Không có người dùng đăng nhập");
                return;
            }

            // Kiểm tra xem có thể đọc dữ liệu từ URI không
            try {
                android.database.Cursor cursor = context.getContentResolver().query(imageUri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE);
                    int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    String fileName = nameIndex > -1 ? cursor.getString(nameIndex) : "unknown";
                    long size = cursor.getLong(sizeIndex);
                    cursor.close();
                    
                    Log.d(TAG, "Tệp sẽ tải lên: " + fileName + ", Kích thước: " + (size / 1024) + "KB");
                    
                    // Nếu kích thước ảnh quá lớn (>5MB), cảnh báo
                    if (size > 5 * 1024 * 1024) {
                        Log.w(TAG, "Ảnh có kích thước lớn, có thể gặp vấn đề khi tải lên");
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Không thể đọc thông tin tệp: " + e.getMessage());
            }

            // Tạo một bản sao tạm thời của ảnh trong thư mục cache
            try {
                java.io.File cacheDir = context.getCacheDir();
                java.io.File tempFile = new java.io.File(cacheDir, "profile_" + userId + ".jpg");
                
                java.io.InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
                if (inputStream == null) {
                    callback.onError("Không thể đọc dữ liệu từ ảnh đã chọn");
                    return;
                }
                
                java.io.FileOutputStream outputStream = new java.io.FileOutputStream(tempFile);
                
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                
                inputStream.close();
                outputStream.flush();
                outputStream.close();
                
                // Sử dụng URI của tệp tạm thời
                Uri tempUri = Uri.fromFile(tempFile);
                Log.d(TAG, "Đã tạo tệp tạm thời: " + tempFile.getAbsolutePath());
                
                // Tiến hành tải lên với tempUri
                updateUserProfileImage(tempUri, task -> {
                    // Xóa tệp tạm thời sau khi hoàn thành
                    tempFile.delete();
                    
                    if (task.isSuccessful()) {
                        // Tiếp tục xử lý như phương thức gốc
                        StorageReference profileRef = FirebaseUtils.getCurrentUserProfileImageRef();
                        if (profileRef != null) {
                            profileRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                                // Cập nhật URL ảnh vào profile người dùng
                                FirebaseUser firebaseUser = FirebaseUtils.getCurrentUser();
                                if (firebaseUser != null) {
                                    final String imageUrl = downloadUri.toString();
                                    Log.d(TAG, "Đã lấy được URL ảnh: " + imageUrl);
                                    
                                    // Cập nhật photoURL trong Firebase Auth trước
                                    firebaseUser.updateProfile(new com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                        .setPhotoUri(downloadUri)
                                        .build())
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d(TAG, "Đã cập nhật photoURL trong Firebase Auth");
                                            
                                            // Sau đó chỉ cập nhật vào Firestore, bỏ qua Realtime DB
                                            Map<String, Object> updates = new HashMap<>();
                                            updates.put("profileImageUrl", imageUrl);
                                            updates.put("updatedAt", new Date().getTime());
                                            
                                            // Cập nhật vào Firestore
                                            FirebaseUtils.getUsersCollection()
                                                .document(firebaseUser.getUid())
                                                .update(updates)
                                                .addOnSuccessListener(aVoid2 -> {
                                                    Log.d(TAG, "Đã cập nhật profileImageUrl vào Firestore");
                                                    callback.onSuccess(firebaseUser);
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e(TAG, "Lỗi cập nhật Firestore: " + e.getMessage());
                                                    // Vẫn xem như thành công vì đã cập nhật được vào Firebase Auth
                                                    callback.onSuccess(firebaseUser);
                                                });
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Lỗi cập nhật photoURL trong Firebase Auth: " + e.getMessage());
                                            callback.onError("Lỗi cập nhật ảnh đại diện: " + e.getMessage());
                                        });
                                } else {
                                    callback.onError("Không tìm thấy thông tin người dùng"); 
                                }
                            }).addOnFailureListener(e -> {
                                Log.e(TAG, "Lỗi lấy URL ảnh: " + e.getMessage());
                                callback.onError("Lỗi lấy URL ảnh: " + e.getMessage());
                            });
                        } else {
                            callback.onError("Không thể tạo tham chiếu Storage");
                        }
                    } else {
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Lỗi không xác định";
                        Log.e(TAG, "Lỗi tải ảnh lên: " + errorMsg);
                        callback.onError("Lỗi tải ảnh lên: " + errorMsg);
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi xử lý ảnh: " + e.getMessage(), e);
                callback.onError("Lỗi khi xử lý ảnh: " + e.getMessage());
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi cập nhật ảnh đại diện: " + e.getMessage(), e);
            callback.onError("Lỗi cập nhật ảnh đại diện: " + e.getMessage());
        }
    }
    
    // Update user profile image (wrapper cho updateUserProfileImage để tương thích với code cũ)
    public static void updateProfileImage(Uri imageUri, UserCallback callback) {
        updateUserProfileImage(imageUri, task -> {
            if (task.isSuccessful()) {
                // Lấy URL của ảnh sau khi tải lên
                StorageReference profileRef = FirebaseUtils.getCurrentUserProfileImageRef();
                if (profileRef != null) {
                    profileRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                        // Cập nhật URL ảnh vào profile người dùng
                        FirebaseUser firebaseUser = FirebaseUtils.getCurrentUser();
                        if (firebaseUser != null) {
                            final String imageUrl = downloadUri.toString();
                            Log.d(TAG, "Đã lấy được URL ảnh: " + imageUrl);
                            
                            // Cập nhật photoURL trong Firebase Auth trước
                            firebaseUser.updateProfile(new com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                .setPhotoUri(downloadUri)
                                .build())
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Đã cập nhật photoURL trong Firebase Auth");
                                    
                                    // Bỏ qua cập nhật vào Realtime Database
                                    Map<String, Object> updates = new HashMap<>();
                                    updates.put("profileImageUrl", imageUrl);
                                    updates.put("updatedAt", new Date().getTime());
                                    
                                    // Chỉ cập nhật vào Firestore
                                    FirebaseUtils.getUsersCollection()
                                        .document(firebaseUser.getUid())
                                        .update(updates)
                                        .addOnSuccessListener(aVoid2 -> {
                                            Log.d(TAG, "Đã cập nhật profileImageUrl vào Firestore");
                                            callback.onSuccess(firebaseUser);
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Lỗi cập nhật Firestore: " + e.getMessage());
                                            // Vẫn xem như thành công vì đã cập nhật được vào Firebase Auth
                                            callback.onSuccess(firebaseUser);
                                        });
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Lỗi cập nhật photoURL trong Firebase Auth: " + e.getMessage());
                                    callback.onError("Lỗi cập nhật ảnh đại diện: " + e.getMessage());
                                });
                        } else {
                            callback.onError("Không tìm thấy thông tin người dùng"); 
                        }
                    }).addOnFailureListener(e -> {
                        Log.e(TAG, "Lỗi lấy URL ảnh: " + e.getMessage());
                        callback.onError("Lỗi lấy URL ảnh: " + e.getMessage());
                    });
                } else {
                    callback.onError("Không thể tạo tham chiếu Storage");
                }
            } else {
                String errorMsg = task.getException() != null ? task.getException().getMessage() : "Lỗi không xác định";
                Log.e(TAG, "Lỗi tải ảnh lên: " + errorMsg);
                callback.onError("Lỗi tải ảnh lên: " + errorMsg);
            }
        });
    }
    
    // Change password
    public static void changePassword(String newPassword, UserCallback callback) {
        FirebaseUser firebaseUser = FirebaseUtils.getCurrentUser();
        if (firebaseUser == null) {
            callback.onError("User not logged in");
            return;
        }
        
        firebaseUser.updatePassword(newPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(firebaseUser);
                    } else {
                        callback.onError(task.getException() != null ? 
                                task.getException().getMessage() : "Failed to update password");
                    }
                });
    }
    
    // Convert User object to Map for Firestore
    private static Map<String, Object> userToMap(User user) {
        Map<String, Object> map = new HashMap<>();
        
        if (user.getId() != null) {
            map.put("uid", user.getId());
        }
        if (user.getEmail() != null) {
            map.put("email", user.getEmail());
        }
        if (user.getName() != null) {
            map.put("name", user.getName());
        }
        if (user.getPhone() != null) {
            map.put("phone", user.getPhone());
        }
        if (user.getDateOfBirth() != null) {
            map.put("dateOfBirth", user.getDateOfBirth());
        }
        if (user.getProfileImageUrl() != null) {
            map.put("profileImageUrl", user.getProfileImageUrl());
        }
        map.put("updatedAt", new Date());
        
        return map;
    }
} 