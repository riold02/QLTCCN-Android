package com.example.qltccn.utils;

import android.net.Uri;
import android.util.Log;

import com.example.qltccn.models.Transaction;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FirebaseUtils {
    private static final String TAG = "FirebaseUtils";
    
    private static FirebaseAuth mAuth;
    private static FirebaseFirestore mFirestore;
    private static FirebaseStorage mStorage;

    // Interface callback cho các hoạt động Firestore
    public interface FirestoreCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    // Khởi tạo Firebase
    static {
        try {
            mAuth = FirebaseAuth.getInstance();
            mFirestore = FirebaseFirestore.getInstance();
            mStorage = FirebaseStorage.getInstance();
            
            android.util.Log.d(TAG, "Firebase đã được khởi tạo thành công");
        } catch (Exception e) {
            android.util.Log.e(TAG, "Lỗi khởi tạo Firebase: " + e.getMessage(), e);
        }
    }

    // Get Firebase Auth instance
    public static FirebaseAuth getAuth() {
        if (mAuth == null) {
            mAuth = FirebaseAuth.getInstance();
        }
        return mAuth;
    }

    // Get current user
    public static FirebaseUser getCurrentUser() {
        return getAuth().getCurrentUser();
    }

    // Get current user ID
    public static String getCurrentUserId() {
        FirebaseUser user = getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    // Check if user is logged in
    public static boolean isUserLoggedIn() {
        return getCurrentUser() != null;
    }

    // Get Firebase Firestore instance
    public static FirebaseFirestore getFirestore() {
        if (mFirestore == null) {
            mFirestore = FirebaseFirestore.getInstance();
        }
        return mFirestore;
    }

    // Get users collection
    public static CollectionReference getUsersCollection() {
        return getFirestore().collection("users");
    }

    // Get current user document reference
    public static DocumentReference getCurrentUserDocument() {
        String userId = getCurrentUserId();
        return userId != null ? getUsersCollection().document(userId) : null;
    }

    /**
     * Lấy tham chiếu đến collection giao dịch của người dùng
     */
    public static CollectionReference getUserTransactionsCollection() {
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            
            // Log để xác định có kết nối đến Firestore không
            Log.d(TAG, "Lấy tham chiếu đến collection transactions trong Firestore");
            
            // Kiểm tra quyền truy cập và kết nối
            FirebaseUser currentUser = getCurrentUser();
            if (currentUser == null) {
                Log.e(TAG, "Không thể lấy collection transactions - người dùng chưa đăng nhập");
                return null;
            }
            
            String userId = currentUser.getUid();
            
            // Thay đổi đường dẫn transactions để trở thành subcollection của user
            // Cấu trúc mới: /users/{userId}/transactions/
            CollectionReference transactionsRef = db.collection("users").document(userId).collection("transactions");
            Log.d(TAG, "Đường dẫn collection: " + transactionsRef.getPath());
            
            return transactionsRef;
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi lấy tham chiếu transactions collection: " + e.getMessage(), e);
            return null;
        }
    }

    // Get categories collection
    public static CollectionReference getCategoriesCollection() {
        return getFirestore().collection("categories");
    }
    
    // Truy vấn categories của người dùng hiện tại hoặc danh mục mặc định
    // Phương thức này trả về một truy vấn đã được lọc
    public static com.google.firebase.firestore.Query getUserCategoriesQuery() {
        String userId = getCurrentUserId();
        
        // Nếu người dùng đã đăng nhập, lấy danh mục của họ hoặc danh mục mặc định (system)
        if (userId != null) {
            return getCategoriesCollection()
                   .whereIn("userId", java.util.Arrays.asList(userId, "system", null));
        } else {
            // Nếu không đăng nhập, chỉ trả về danh mục mặc định
            return getCategoriesCollection()
                   .whereIn("userId", java.util.Arrays.asList("system", null));
        }
    }

    // Get Firebase Storage instance
    public static FirebaseStorage getStorage() {
        if (mStorage == null) {
            mStorage = FirebaseStorage.getInstance();
        }
        return mStorage;
    }

    // Get Storage reference
    public static StorageReference getStorageReference() {
        return getStorage().getReference();
    }

    // Get user receipts storage folder reference
    public static StorageReference getUserReceiptsReference() {
        String userId = getCurrentUserId();
        return userId != null ? getStorageReference().child("receipts").child(userId) : null;
    }

    // Get receipt reference with filename
    public static StorageReference getReceiptReference(String filename) {
        StorageReference userReceiptsRef = getUserReceiptsReference();
        return userReceiptsRef != null ? userReceiptsRef.child(filename) : null;
    }

    // Thêm phương thức để lấy tham chiếu đến ảnh đại diện của người dùng
    public static StorageReference getUserProfileImageRef(String userId) {
        if (userId == null) {
            Log.e(TAG, "getUserProfileImageRef: userId is null");
            return null;
        }
        try {
            // Đảm bảo đường dẫn đúng cú pháp
            return getStorageReference().child("profile_images").child(userId + ".jpg");
        } catch (Exception e) {
            Log.e(TAG, "Lỗi tạo tham chiếu ảnh đại diện: " + e.getMessage(), e);
            return null;
        }
    }

    // Thêm phương thức để lấy tham chiếu đến ảnh đại diện của người dùng hiện tại
    public static StorageReference getCurrentUserProfileImageRef() {
        String userId = getCurrentUserId();
        if (userId == null) {
            Log.e(TAG, "getCurrentUserProfileImageRef: không có người dùng đăng nhập");
        }
        return getUserProfileImageRef(userId);
    }
    
    // Interface để callback trạng thái kết nối Firebase
    public interface FirebaseConnectionCallback {
        void onConnected();
        void onDisconnected(String message);
        void onError(String error);
    }
    
    /**
     * Kiểm tra xem tài khoản hiện tại có phải là tài khoản mới không
     * Tài khoản được coi là mới nếu được tạo trong vòng 5 phút
     * @return true nếu tài khoản mới được tạo
     */
    public static boolean isNewAccount() {
        FirebaseUser currentUser = getAuth().getCurrentUser();
        if (currentUser == null || currentUser.getMetadata() == null) {
            return false;
        }
        
        long creationTimestamp = currentUser.getMetadata().getCreationTimestamp();
        long currentTime = System.currentTimeMillis();
        
        // Tài khoản mới nếu được tạo trong vòng 5 phút
        return (currentTime - creationTimestamp) < 5 * 60 * 1000;
    }

    /**
     * Khởi tạo dữ liệu giao dịch mẫu cho người dùng mới
     * @param callback Callback để thông báo kết quả
     */
    public static void initializeTransactionsForNewUser(Runnable onComplete) {
        try {
            String userId = getCurrentUserId();
            if (userId == null) {
                Log.e("FirebaseUtils", "Không thể khởi tạo dữ liệu: người dùng chưa đăng nhập");
                if (onComplete != null) onComplete.run();
                return;
            }
            
            // Chỉ khởi tạo nếu là tài khoản mới
            if (!isNewAccount()) {
                Log.d("FirebaseUtils", "Không phải tài khoản mới, bỏ qua khởi tạo dữ liệu mẫu");
                if (onComplete != null) onComplete.run();
                return;
            }
            
            // Không tự động tạo dữ liệu mẫu nữa, chỉ đảm bảo số dư ban đầu là 0
            DocumentReference userRef = getCurrentUserDocument();
            if (userRef != null) {
                userRef.get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && !documentSnapshot.contains("balance")) {
                        // Nếu người dùng tồn tại nhưng chưa có trường balance
                        userRef.update("balance", 0.0)
                               .addOnSuccessListener(aVoid -> {
                                   Log.d("FirebaseUtils", "Đã thiết lập số dư ban đầu = 0");
                                   if (onComplete != null) onComplete.run();
                               })
                               .addOnFailureListener(e -> {
                                   Log.e("FirebaseUtils", "Lỗi khi thiết lập số dư ban đầu: " + e.getMessage());
                                   if (onComplete != null) onComplete.run();
                               });
                    } else {
                        Log.d("FirebaseUtils", "Người dùng đã có thông tin số dư");
                        if (onComplete != null) onComplete.run();
                    }
                }).addOnFailureListener(e -> {
                    Log.e("FirebaseUtils", "Lỗi khi kiểm tra dữ liệu người dùng: " + e.getMessage());
                    if (onComplete != null) onComplete.run();
                });
            } else {
                Log.e("FirebaseUtils", "Không tìm thấy tham chiếu người dùng");
                if (onComplete != null) onComplete.run();
            }
        } catch (Exception e) {
            Log.e("FirebaseUtils", "Lỗi ngoại lệ khi khởi tạo dữ liệu mẫu: " + e.getMessage(), e);
            if (onComplete != null) onComplete.run();
        }
    }
    
    /**
     * Cập nhật số dư cho người dùng hiện tại
     * @param newBalance Số dư mới
     */
    private static void updateUserBalance(double newBalance) {
        DocumentReference userRef = getCurrentUserDocument();
        if (userRef == null) {
            Log.e("FirebaseUtils", "Không thể cập nhật số dư: không tìm thấy người dùng");
            return;
        }
        
        userRef.update("balance", newBalance)
            .addOnSuccessListener(aVoid -> {
                Log.d("FirebaseUtils", "Cập nhật số dư thành công: " + newBalance);
            })
            .addOnFailureListener(e -> {
                Log.e("FirebaseUtils", "Lỗi khi cập nhật số dư: " + e.getMessage());
            });
    }
    
    /**
     * Thêm tiền vào tài khoản người dùng sử dụng Firestore
     * @param amount Số tiền cần thêm
     * @param note Ghi chú cho giao dịch
     * @param date Thời gian giao dịch (milliseconds)
     * @param callback Callback để thông báo kết quả
     */
    public static void addFundsToAccount(double amount, String note, long date, FirebaseConnectionCallback callback) {
        try {
            if (!isUserLoggedIn()) {
                if (callback != null) {
                    callback.onError("Người dùng chưa đăng nhập");
                }
                return;
            }
            
            String userId = getCurrentUserId();
            if (userId == null) {
                if (callback != null) {
                    callback.onError("Không xác định được ID người dùng");
                }
                return;
            }
            
            // Lấy số dư hiện tại
            getCurrentUserDocument().get()
                .addOnSuccessListener(documentSnapshot -> {
                    try {
                        double currentBalance = 0.0;
                        
                        // Kiểm tra xem tài liệu người dùng có tồn tại không
                        if (documentSnapshot.exists()) {
                            // Lấy số dư hiện tại (mặc định là 0 nếu không tồn tại)
                            if (documentSnapshot.contains("balance")) {
                                Object balanceObj = documentSnapshot.get("balance");
                                if (balanceObj instanceof Number) {
                                    currentBalance = ((Number) balanceObj).doubleValue();
                                }
                            }
                        }
                        
                        // Tính toán số dư mới
                        final double newBalance = currentBalance + amount;
                        
                        // Cập nhật số dư trong Firestore
                        updateUserBalance(newBalance);
                        
                        // Chuẩn bị nội dung mô tả và ghi chú
                        String description = note != null && !note.isEmpty() ? note : "Nạp tiền vào tài khoản";
                        
                        // Tạo giao dịch mới
                        Transaction transaction = new Transaction();
                        transaction.setAmount(amount);
                        transaction.setType("income");
                        transaction.setCategory("deposit");
                        transaction.setDescription(description);
                        transaction.setNote(description);
                        transaction.setDate(date);
                        transaction.setUserId(userId);
                        
                        // Lưu giao dịch vào collection transactions của người dùng
                        getUserTransactionsCollection().add(transaction.toMap())
                            .addOnSuccessListener(documentReference -> {
                                Log.d("FirebaseUtils", "Giao dịch được lưu với ID: " + documentReference.getId());
                                if (callback != null) {
                                    callback.onConnected();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e("FirebaseUtils", "Lỗi khi lưu giao dịch: " + e.getMessage());
                                if (callback != null) {
                                    callback.onError("Lỗi khi lưu giao dịch: " + e.getMessage());
                                }
                            });
                    } catch (Exception e) {
                        Log.e("FirebaseUtils", "Lỗi xử lý dữ liệu: " + e.getMessage());
                        if (callback != null) {
                            callback.onError("Lỗi xử lý dữ liệu: " + e.getMessage());
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseUtils", "Lỗi khi tải dữ liệu người dùng: " + e.getMessage());
                    if (callback != null) {
                        callback.onError("Lỗi khi tải dữ liệu người dùng: " + e.getMessage());
                    }
                });
        } catch (Exception e) {
            Log.e("FirebaseUtils", "Lỗi chung: " + e.getMessage());
            if (callback != null) {
                callback.onError("Lỗi chung: " + e.getMessage());
            }
        }
    }

    /**
     * Phương thức cũ để tương thích ngược với mã hiện có
     */
    public static void addFundsToAccount(double amount, String note, FirebaseConnectionCallback callback) {
        // Gọi phương thức mới với thời gian hiện tại
        addFundsToAccount(amount, note, System.currentTimeMillis(), callback);
    }
} 