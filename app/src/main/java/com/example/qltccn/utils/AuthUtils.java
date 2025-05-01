package com.example.qltccn.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.qltccn.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AuthUtils {
    private static final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private static final String TAG = "AuthUtils";

    public static FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public static String getCurrentUserId() {
        FirebaseUser user = getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public static boolean isUserLoggedIn() {
        return getCurrentUser() != null;
    }

    public static void signOut() {
        mAuth.signOut();
    }

    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onError(String error);
    }

    // Sign up with email and password with full user data
    public static void signUp(Context context, String email, String password, String fullName,
                              String mobileNumber, String dateOfBirth, AuthCallback callback) {
        if (email.isEmpty() || password.isEmpty()) {
            callback.onError("Email và mật khẩu không được để trống");
            return;
        }

        Log.d(TAG, "Bắt đầu đăng ký tài khoản với email: " + email);

        try {
            // Tiến hành đăng ký ngay mà không kiểm tra kết nối trước
            performSignUp(context, email, password, fullName, mobileNumber, dateOfBirth, callback);
        } catch (Exception e) {
            // Xử lý ngoại lệ
            Log.e(TAG, "Lỗi khi đăng ký: " + e.getMessage());
            callback.onError("Lỗi khi đăng ký: " + e.getMessage());
        }
    }

    private static void performSignUp(Context context, String email, String password, String fullName,
                                      String mobileNumber, String dateOfBirth, AuthCallback callback) {
        try {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                Log.d(TAG, "Tạo tài khoản Firebase Auth thành công: " + user.getUid());

                                // Create user object
                                User newUser = new User();
                                newUser.setId(user.getUid());
                                newUser.setEmail(email);
                                newUser.setName(fullName);
                                newUser.setPhone(mobileNumber);
                                newUser.setBalance(0.0); // Số dư ban đầu là 0
                                newUser.updateTimestamp();

                                // Parse date of birth if provided
                                if (dateOfBirth != null && !dateOfBirth.isEmpty()) {
                                    try {
                                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                        Date dob = sdf.parse(dateOfBirth);
                                        newUser.setDateOfBirth(dob);
                                    } catch (Exception e) {
                                        Log.w(TAG, "Lỗi chuyển đổi ngày sinh: " + e.getMessage());
                                    }
                                }

                                // Lưu dữ liệu người dùng đồng thời vào cả Firestore và Realtime Database
                                saveUserToFirestore(user, newUser, new AuthCallback() {
                                    @Override
                                    public void onSuccess(FirebaseUser firebaseUser) {
                                        // Sau khi lưu thông tin người dùng, tạo các danh mục mặc định
                                        createDefaultCategories(user.getUid(), new AuthCallback() {
                                            @Override
                                            public void onSuccess(FirebaseUser firebaseUser) {
                                                Log.d(TAG, "Tạo danh mục mặc định thành công");
                                                callback.onSuccess(user);
                                            }

                                            @Override
                                            public void onError(String error) {
                                                Log.w(TAG, "Lỗi tạo danh mục mặc định: " + error);
                                                // Vẫn trả về thành công vì đã lưu được user
                                                callback.onSuccess(user);
                                            }
                                        });
                                    }

                                    @Override
                                    public void onError(String error) {
                                        callback.onError(error);
                                    }
                                });
                            } else {
                                Log.e(TAG, "Tạo tài khoản thành công nhưng user là null");
                                callback.onError("Lỗi tạo tài khoản: Không thể lấy thông tin người dùng");
                            }
                        } else {
                            String errorMessage = task.getException() != null ?
                                    task.getException().getMessage() : "Đăng ký thất bại";
                            Log.e(TAG, "Lỗi tạo tài khoản: " + errorMessage);
                            callback.onError(errorMessage);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Lỗi đăng ký: " + e.getMessage(), e);
                        callback.onError("Lỗi đăng ký: " + e.getMessage());
                    });
        } catch (Exception e) {
            Log.e(TAG, "Lỗi nghiêm trọng khi đăng ký: " + e.getMessage(), e);
            callback.onError("Lỗi không xác định: " + e.getMessage());
        }
    }

    // Lưu dữ liệu người dùng vào Firestore
    private static void saveUserToFirestore(FirebaseUser firebaseUser, User user, final AuthCallback callback) {
        if (firebaseUser == null) {
            Log.e(TAG, "Không thể lưu dữ liệu người dùng: user là null");
            callback.onError("Không thể lưu dữ liệu người dùng");
            return;
        }

        Log.d(TAG, "Bắt đầu lưu dữ liệu người dùng với ID: " + user.getId());

        try {
            // Chuẩn bị dữ liệu cho Firestore
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("email", user.getEmail());
            userData.put("name", user.getName());
            userData.put("phone", user.getPhone());
            userData.put("balance", user.getBalance());

            // Xử lý ngày sinh đúng cách
            if (user.getDateOfBirth() != null) {
                // Lưu timestamp cho Firestore
                userData.put("dateOfBirth", user.getDateOfBirth().getTime());

                // Đảm bảo format ngày hiển thị đúng
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    userData.put("dateOfBirthFormatted", sdf.format(user.getDateOfBirth()));
                } catch (Exception e) {
                    Log.w(TAG, "Lỗi định dạng ngày sinh: " + e.getMessage());
                }
            } else {
                userData.put("dateOfBirth", null);
            }

            // Thêm timestamp
            long currentTime = new Date().getTime();
            userData.put("createdAt", currentTime);
            userData.put("updatedAt", currentTime);

            // Thiết lập timeout để đảm bảo callback được gọi nếu có vấn đề
            final boolean[] callbackFired = {false};

            // Đặt timeout 5 giây
            new android.os.Handler().postDelayed(() -> {
                if (!callbackFired[0]) {
                    callbackFired[0] = true;
                    Log.w(TAG, "Timeout khi lưu dữ liệu người dùng - vẫn tiếp tục với quá trình đăng ký");
                    // Vẫn tiếp tục với callback thành công dù có timeout
                    callback.onSuccess(firebaseUser);
                }
            }, 5000);

            // Lưu dữ liệu vào Firestore
            Log.d(TAG, "Bắt đầu lưu vào Firestore...");
            FirebaseUtils.getUsersCollection()
                    .document(firebaseUser.getUid())
                    .set(userData)
                    .addOnSuccessListener(aVoid -> {
                        if (!callbackFired[0]) {
                            callbackFired[0] = true;
                            Log.d(TAG, "Lưu dữ liệu người dùng vào Firestore thành công");
                            callback.onSuccess(firebaseUser);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Lỗi lưu dữ liệu vào Firestore: " + e.getMessage(), e);
                        if (!callbackFired[0]) {
                            callbackFired[0] = true;
                            // Nếu lỗi liên quan đến quyền, vẫn cho phép đăng ký thành công
                            if (e.getMessage() != null && e.getMessage().contains("PERMISSION_DENIED")) {
                                Log.w(TAG, "Lỗi quyền truy cập khi lưu dữ liệu, nhưng vẫn tiếp tục");
                                callback.onSuccess(firebaseUser);
                            } else {
                                callback.onError("Lỗi lưu dữ liệu người dùng: " + e.getMessage());
                            }
                        }
                    });

        } catch (Exception e) {
            Log.e(TAG, "Lỗi nghiêm trọng khi lưu dữ liệu người dùng: " + e.getMessage(), e);
            callback.onError("Lỗi không xác định khi lưu dữ liệu: " + e.getMessage());
        }
    }

    // Phương thức tạo danh mục mặc định cho người dùng mới
    private static void createDefaultCategories(String userId, AuthCallback callback) {
        try {
            // Không cần đợi tạo danh mục xong, trả về thành công ngay
            callback.onSuccess(null);

            // Sử dụng Firestore thay vì Realtime Database
            CollectionReference categoriesRef = FirebaseUtils.getCategoriesCollection();

            // Danh sách danh mục mặc định
            List<Map<String, Object>> defaultCategories = new ArrayList<>();

            // Tạo các danh mục chi tiêu mặc định
            Map<String, Object> foodCategory = new HashMap<>();
            foodCategory.put("name", "Ăn uống");
            foodCategory.put("type", "expense");
            foodCategory.put("iconName", "food");
            foodCategory.put("color", "#FF5722");
            foodCategory.put("default", true);
            foodCategory.put("userId", userId); // Thêm userId
            defaultCategories.add(foodCategory);

            Map<String, Object> transportCategory = new HashMap<>();
            transportCategory.put("name", "Di chuyển");
            transportCategory.put("type", "expense");
            transportCategory.put("iconName", "transport");
            transportCategory.put("color", "#2196F3");
            transportCategory.put("default", true);
            transportCategory.put("userId", userId); // Thêm userId
            defaultCategories.add(transportCategory);

            Map<String, Object> rentCategory = new HashMap<>();
            rentCategory.put("name", "Nhà ở");
            rentCategory.put("type", "expense");
            rentCategory.put("iconName", "rent");
            rentCategory.put("color", "#9C27B0");
            rentCategory.put("default", true);
            rentCategory.put("userId", userId); // Thêm userId
            defaultCategories.add(rentCategory);

            Map<String, Object> entertainmentCategory = new HashMap<>();
            entertainmentCategory.put("name", "Giải trí");
            entertainmentCategory.put("type", "expense");
            entertainmentCategory.put("iconName", "entertainment");
            entertainmentCategory.put("color", "#FFC107");
            entertainmentCategory.put("default", true);
            entertainmentCategory.put("userId", userId); // Thêm userId
            defaultCategories.add(entertainmentCategory);

            // Tạo danh mục thu nhập mặc định
            Map<String, Object> salaryCategory = new HashMap<>();
            salaryCategory.put("name", "Lương");
            salaryCategory.put("type", "income");
            salaryCategory.put("iconName", "savings");
            salaryCategory.put("color", "#4CAF50");
            salaryCategory.put("default", true);
            salaryCategory.put("userId", userId); // Thêm userId
            defaultCategories.add(salaryCategory);

            Map<String, Object> bonusCategory = new HashMap<>();
            bonusCategory.put("name", "Thưởng");
            bonusCategory.put("type", "income");
            bonusCategory.put("iconName", "savings");
            bonusCategory.put("color", "#FFC107");
            bonusCategory.put("default", true);
            bonusCategory.put("userId", userId); // Thêm userId
            defaultCategories.add(bonusCategory);

            // Thêm từng danh mục vào Firestore trong thread riêng
            new Thread(() -> {
                for (Map<String, Object> category : defaultCategories) {
                    try {
                        categoriesRef.add(category)
                                .addOnSuccessListener(documentReference -> {
                                    // Cập nhật ID tài liệu vào danh mục
                                    documentReference.update("id", documentReference.getId());
                                    Log.d(TAG, "Đã tạo danh mục: " + category.get("name"));
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Lỗi tạo danh mục " + category.get("name") + ": " + e.getMessage());
                                    // Không ảnh hưởng đến quá trình đăng ký
                                });
                        // Ngủ 0.5 giây giữa mỗi lần tạo để tránh quá tải
                        Thread.sleep(500);
                    } catch (Exception e) {
                        Log.e(TAG, "Lỗi khi tạo danh mục " + category.get("name") + ": " + e.getMessage());
                    }
                }
            }).start();

        } catch (Exception e) {
            Log.e(TAG, "Lỗi tạo danh mục mặc định: " + e.getMessage());
            // Vẫn trả về thành công vì danh mục không quan trọng với quá trình đăng ký
            callback.onSuccess(null);
        }
    }

    // Sign in with email and password
    public static void signIn(Context context, String email, String password, AuthCallback callback) {
        if (email.isEmpty() || password.isEmpty()) {
            callback.onError("Email hoặc mật khẩu không được để trống");
            return;
        }

        try {
            // Kiểm tra kết nối Firebase trước
            FirebaseDatabase.getInstance().getReference(".info/connected").addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Boolean connected = snapshot.getValue(Boolean.class);
                            if (connected == null || !connected) {
                                callback.onError("Không thể kết nối đến máy chủ. Vui lòng kiểm tra kết nối mạng.");
                                return;
                            }

                            // Tiếp tục đăng nhập khi đã có kết nối
                            performSignIn(context, email, password, callback);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Vẫn tiếp tục thử đăng nhập nếu không thể kiểm tra kết nối
                            performSignIn(context, email, password, callback);
                        }
                    });
        } catch (Exception e) {
            // Xử lý ngoại lệ và vẫn tiếp tục thử đăng nhập
            Log.e(TAG, "Lỗi khi kiểm tra kết nối: " + e.getMessage());
            performSignIn(context, email, password, callback);
        }
    }

    private static void performSignIn(Context context, String email, String password, AuthCallback callback) {
        try {
            // Kiểm tra kết nối Firebase trước
            try {
                boolean[] timeoutFired = {false};

                // Đặt một timeout để tránh đợi quá lâu
                android.os.Handler timeoutHandler = new android.os.Handler();
                Runnable timeoutRunnable = () -> {
                    timeoutFired[0] = true;
                    Log.w(TAG, "Đăng nhập bị timeout sau 15 giây");
                    callback.onError("Hết thời gian chờ phản hồi từ máy chủ. Vui lòng thử lại.");
                };

                // Đặt timeout 15 giây
                timeoutHandler.postDelayed(timeoutRunnable, 15000);

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            // Hủy timeout vì đã nhận được phản hồi
                            timeoutHandler.removeCallbacks(timeoutRunnable);

                            // Chỉ xử lý nếu timeout chưa xảy ra
                            if (!timeoutFired[0]) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if (user != null) {
                                        Log.d(TAG, "Đăng nhập thành công với Firebase Auth: " + user.getUid());
                                        // Đảm bảo dữ liệu người dùng tồn tại trong cả hai cơ sở dữ liệu
                                        syncUserData(user, callback);
                                    } else {
                                        Log.w(TAG, "Đăng nhập thành công nhưng user là null");
                                        callback.onError("Không tìm thấy thông tin người dùng");
                                    }
                                } else {
                                    // Đăng nhập thất bại
                                    String errorMessage = task.getException() != null ?
                                            task.getException().getMessage() : "Đăng nhập thất bại";
                                    Log.e(TAG, "Đăng nhập thất bại: " + errorMessage);
                                    callback.onError(errorMessage);
                                }
                            }
                        })
                        .addOnFailureListener(e -> {
                            // Hủy timeout vì đã nhận được phản hồi
                            timeoutHandler.removeCallbacks(timeoutRunnable);

                            // Chỉ xử lý nếu timeout chưa xảy ra
                            if (!timeoutFired[0]) {
                                Log.e(TAG, "Lỗi đăng nhập: " + e.getMessage(), e);
                                callback.onError(e.getMessage());
                            }
                        });
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi gọi Firebase Auth: " + e.getMessage(), e);
                callback.onError("Lỗi hệ thống đăng nhập: " + e.getMessage());
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi trong quá trình đăng nhập: " + e.getMessage(), e);
            callback.onError("Lỗi hệ thống: " + e.getMessage());
        }
    }

    // Phương thức public để đồng bộ dữ liệu người dùng
    public static void syncUserData(FirebaseUser firebaseUser, AuthCallback callback) {
        try {
            Log.d(TAG, "Bắt đầu đồng bộ dữ liệu người dùng...");

            // Kiểm tra trong Firestore trước
            FirebaseUtils.getUsersCollection()
                    .document(firebaseUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Log.d(TAG, "Tìm thấy dữ liệu người dùng trong Firestore");

                            // Lấy dữ liệu từ Firestore
                            Map<String, Object> userData = documentSnapshot.getData();
                            if (userData == null) userData = new HashMap<>();

                            // Đảm bảo các trường cần thiết
                            ensureUserDataFields(userData, firebaseUser);

                            // Cập nhật trong Realtime Database
                            syncToRealtimeDB(firebaseUser.getUid(), userData, callback, firebaseUser);
                        } else {
                            Log.w(TAG, "Không tìm thấy dữ liệu người dùng trong Firestore, kiểm tra Realtime DB");
                            checkAndCreateUserInRealtimeDB(firebaseUser, callback);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Lỗi truy vấn Firestore: " + e.getMessage());
                        checkAndCreateUserInRealtimeDB(firebaseUser, callback);
                    });
        } catch (Exception e) {
            Log.e(TAG, "Lỗi đồng bộ dữ liệu: " + e.getMessage());
            callback.onSuccess(firebaseUser); // Vẫn cho phép đăng nhập
        }
    }

    // Kiểm tra và tạo dữ liệu người dùng trong Realtime Database nếu cần
    private static void checkAndCreateUserInRealtimeDB(FirebaseUser firebaseUser, AuthCallback callback) {
        try {
            com.google.firebase.database.DatabaseReference dbRef =
                    com.google.firebase.database.FirebaseDatabase.getInstance().getReference()
                            .child("users").child(firebaseUser.getUid());

            dbRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                @Override
                public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Log.d(TAG, "Tìm thấy dữ liệu người dùng trong Realtime DB");

                        try {
                            // Chuyển đổi dữ liệu từ RTDB
                            Map<String, Object> userData = new HashMap<>();
                            for (com.google.firebase.database.DataSnapshot child : snapshot.getChildren()) {
                                userData.put(child.getKey(), child.getValue());
                            }

                            // Đảm bảo các trường cần thiết
                            ensureUserDataFields(userData, firebaseUser);

                            // Đồng bộ ngược lên Firestore
                            syncToFirestore(firebaseUser.getUid(), userData, callback, firebaseUser);
                        } catch (Exception e) {
                            Log.e(TAG, "Lỗi xử lý dữ liệu từ RTDB: " + e.getMessage());
                            callback.onSuccess(firebaseUser); // Vẫn cho phép đăng nhập
                        }
                    } else {
                        Log.w(TAG, "Không tìm thấy dữ liệu người dùng trong cả hai DB, tạo mới");
                        createNewUserData(firebaseUser, callback);
                    }
                }

                @Override
                public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                    Log.e(TAG, "Lỗi truy vấn RTDB: " + error.getMessage());
                    createNewUserData(firebaseUser, callback);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Lỗi kiểm tra RTDB: " + e.getMessage());
            callback.onSuccess(firebaseUser); // Vẫn cho phép đăng nhập
        }
    }

    // Tạo dữ liệu người dùng mới cho cả hai database
    private static void createNewUserData(FirebaseUser firebaseUser, AuthCallback callback) {
        try {
            // Tạo dữ liệu cơ bản
            Map<String, Object> basicUserData = new HashMap<>();
            basicUserData.put("email", firebaseUser.getEmail());
            basicUserData.put("name", firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "Người dùng");
            basicUserData.put("balance", 0.0);
            basicUserData.put("id", firebaseUser.getUid());
            basicUserData.put("uid", firebaseUser.getUid());

            // Thêm timestamp
            long currentTime = new Date().getTime();
            basicUserData.put("createdAt", currentTime);
            basicUserData.put("updatedAt", currentTime);

            Log.d(TAG, "Tạo dữ liệu người dùng mới cho: " + firebaseUser.getUid());

            // Lưu vào cả hai cơ sở dữ liệu
            syncToFirestore(firebaseUser.getUid(), basicUserData, null, null);
            syncToRealtimeDB(firebaseUser.getUid(), basicUserData, callback, firebaseUser);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi tạo dữ liệu người dùng mới: " + e.getMessage());
            callback.onSuccess(firebaseUser); // Vẫn cho phép đăng nhập
        }
    }

    // Đảm bảo các trường dữ liệu cần thiết
    private static void ensureUserDataFields(Map<String, Object> userData, FirebaseUser firebaseUser) {
        if (!userData.containsKey("email")) userData.put("email", firebaseUser.getEmail());
        if (!userData.containsKey("name")) {
            userData.put("name", firebaseUser.getDisplayName() != null ?
                    firebaseUser.getDisplayName() : "Người dùng");
        }
        if (!userData.containsKey("balance")) userData.put("balance", 0.0);
        if (!userData.containsKey("id")) userData.put("id", firebaseUser.getUid());
        if (!userData.containsKey("uid")) userData.put("uid", firebaseUser.getUid());

        // Cập nhật timestamp
        long currentTime = new Date().getTime();
        if (!userData.containsKey("createdAt")) userData.put("createdAt", currentTime);
        userData.put("updatedAt", currentTime);
    }

    // Đồng bộ dữ liệu lên Firestore
    private static void syncToFirestore(String userId, Map<String, Object> userData,
                                        AuthCallback callback, FirebaseUser firebaseUser) {
        try {
            FirebaseUtils.getUsersCollection()
                    .document(userId)
                    .set(userData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Đồng bộ lên Firestore thành công");
                        if (callback != null && firebaseUser != null) {
                            callback.onSuccess(firebaseUser);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Lỗi đồng bộ lên Firestore: " + e.getMessage());
                        if (callback != null && firebaseUser != null) {
                            callback.onSuccess(firebaseUser); // Vẫn cho phép đăng nhập
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Lỗi đồng bộ lên Firestore: " + e.getMessage());
            if (callback != null && firebaseUser != null) {
                callback.onSuccess(firebaseUser); // Vẫn cho phép đăng nhập
            }
        }
    }

    // Đồng bộ dữ liệu lên Realtime Database
    private static void syncToRealtimeDB(String userId, Map<String, Object> userData,
                                         AuthCallback callback, FirebaseUser firebaseUser) {
        try {
            com.google.firebase.database.DatabaseReference dbRef =
                    com.google.firebase.database.FirebaseDatabase.getInstance().getReference()
                            .child("users").child(userId);

            dbRef.updateChildren(userData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Đồng bộ lên Realtime DB thành công");
                        if (callback != null) {
                            callback.onSuccess(firebaseUser);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Lỗi đồng bộ lên Realtime DB: " + e.getMessage());
                        if (callback != null) {
                            callback.onSuccess(firebaseUser); // Vẫn cho phép đăng nhập
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Lỗi đồng bộ lên Realtime DB: " + e.getMessage());
            if (callback != null) {
                callback.onSuccess(firebaseUser); // Vẫn cho phép đăng nhập
            }
        }
    }

    // Reset password
    public static void resetPassword(Context context, String email, OnCompleteListener<Void> listener) {
        if (email.isEmpty()) {
            Toast.makeText(context, "Email cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(listener);
    }

    // Update user profile with date of birth
    public static void updateUserProfileWithDOB(String fullName, String mobileNumber, String dateOfBirth, OnCompleteListener<Void> listener) {
        FirebaseUser user = getCurrentUser();
        if (user != null) {
            FirebaseFirestore db = FirebaseUtils.getFirestore();
            Map<String, Object> updates = new HashMap<>();
            updates.put("name", fullName);
            updates.put("phone", mobileNumber);

            // Add date of birth if provided
            if (dateOfBirth != null && !dateOfBirth.isEmpty()) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    Date dob = sdf.parse(dateOfBirth);
                    updates.put("dateOfBirth", dob);
                } catch (Exception e) {
                    // Ignore date parse errors
                }
            }

            updates.put("updatedAt", new java.util.Date());

            db.collection("users").document(user.getUid())
                    .update(updates)
                    .addOnCompleteListener(listener);
        } else if (listener != null) {
            // Đảm bảo callback luôn được gọi ngay cả khi không có user
            // Không thể sử dụng Task.forException
            listener.onComplete(null);
        }
    }

    // Phương thức tương thích ngược cho phương thức updateUserProfile đã xóa
    public static void updateUserProfile(String fullName, String mobileNumber, OnCompleteListener<Void> listener) {
        updateUserProfileWithDOB(fullName, mobileNumber, null, listener);
    }

    // Phương thức cũ để giữ tương thích ngược
    public static void signUp(Context context, String email, String password, AuthCallback callback) {
        signUp(context, email, password, "", "", "", callback);
    }
} 