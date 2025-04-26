package com.example.qltccn.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qltccn.R;
import com.example.qltccn.adapters.TransactionAdapter;
import com.example.qltccn.models.Transaction;
import com.example.qltccn.models.User;
import com.example.qltccn.utils.FirebaseUtils;
import com.example.qltccn.utils.UserUtils;
import com.example.qltccn.utils.CurrencyUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";

    // Header
    private TextView welcomeText, goodMorningText;
    private ImageView userIcon;
    
    // Footer navigation
    private ImageView iconHome;
    private ImageView iconAnalysis;
    private ImageView iconTrans;
    private ImageView iconCategory;
    private ImageView iconUser;

    // User Info
    private User currentUser;
    private TextView tvBalance;
    private View progressBar;
    private TextView tvTotalExpense;
    private TextView tvLastIncome; // TextView cho số tiền nạp gần nhất
    private TextView tvLastExpense; // TextView cho chi tiêu gần nhất
    
    // Transactions section
    private LinearLayoutManager layoutManager;
    private RecyclerView recyclerViewTransactions;
    private TransactionAdapter transactionAdapter;
    private List<Transaction> recentTransactions = new ArrayList<>();
    private List<Transaction> allTransactions = new ArrayList<>();
    private View transactionsContainer;
    
    // Filter
    private Button tabDaily, tabWeekly, tabMonthly;
    private String currentTimeFilter = "monthly"; // Mặc định là monthly view
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initViews();
        setupListeners();
        loadUserBalance();
        loadRecentTransactions();
    }

    private void initViews() {
        try {
            // Header
            welcomeText = findViewById(R.id.welcomeText);
            goodMorningText = findViewById(R.id.goodMorningText);

            // Thêm xử lý nút notification
            View notiContainer = findViewById(R.id.notiContainer);
            ImageView notiIcon = findViewById(R.id.notiIcon);
            if (notiContainer != null) {
                notiContainer.setOnClickListener(v -> navigateToNotification());
            }

            // Footer navigation
            iconHome = findViewById(R.id.iconHome);
            iconAnalysis = findViewById(R.id.iconAnalysis);
            iconTrans = findViewById(R.id.iconTrans);
            iconCategory = findViewById(R.id.iconCategory);
            iconUser = findViewById(R.id.iconUser);
            
            // Thông tin số dư và chi tiêu
            tvBalance = findViewById(R.id.tv_total_balance);
            tvTotalExpense = findViewById(R.id.tv_total_expense);
            
            // TextView cho giao dịch gần nhất
            tvLastIncome = findViewById(R.id.tvLastIncome);
            tvLastExpense = findViewById(R.id.tvLastExpenseValue);

            progressBar = findViewById(R.id.progressBar);
            
            // Khởi tạo thông tin mục tiêu tiết kiệm
            ProgressBar savingsProgressBar = findViewById(R.id.savingsProgressBar);
            TextView savingsProgressText = findViewById(R.id.savingsProgressText);
            TextView savingsGoalText = findViewById(R.id.savingsGoalText);
            
            // Thêm sự kiện click cho khu vực Savings để mở màn hình Savings
            View savingsSection = findViewById(R.id.goalsCard);
            if (savingsSection != null) {
                savingsSection.setOnClickListener(v -> navigateToSavings());
            }
            
            // Tải dữ liệu mục tiêu tiết kiệm từ cơ sở dữ liệu
            String userId = FirebaseUtils.getCurrentUser() != null ? 
                            FirebaseUtils.getCurrentUser().getUid() : null;
                            
            if (userId != null) {
                loadSavingsGoalData(userId, savingsProgressBar, savingsProgressText, savingsGoalText);
            } else {
                // Giá trị mặc định nếu không có người dùng
                updateSavingsGoalUI(savingsProgressBar, savingsProgressText, savingsGoalText, 0, 1);
            }
            
            if (iconHome != null) {
                iconHome.setImageResource(R.drawable.ic_home1);
            }
            
            // Filter buttons
            tabDaily = findViewById(R.id.tabDaily);
            tabWeekly = findViewById(R.id.tabWeekly);
            tabMonthly = findViewById(R.id.tabMonthly);
            
            // Transactions
            transactionsContainer = findViewById(R.id.transactionsContainer);
            // If we have a RecyclerView for transactions, initialize it
            recyclerViewTransactions = findViewById(R.id.transactionsRecyclerView);
            if (recyclerViewTransactions != null) {
                layoutManager = new LinearLayoutManager(this);
                recyclerViewTransactions.setLayoutManager(layoutManager);
                transactionAdapter = new TransactionAdapter(recentTransactions, new TransactionAdapter.OnTransactionClickListener() {
                    @Override
                    public void onTransactionClick(Transaction transaction, int position) {
                        // Show transaction details dialog
                        showTransactionDetail(transaction);
                    }
                });
                recyclerViewTransactions.setAdapter(transactionAdapter);
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi khởi tạo views: " + e.getMessage(), e);
        }
    }

    private void setupListeners() {
        try {
            // Footer navigation
            // No need to add listener for iconHome as we're already in the Home screen
            if (iconAnalysis != null) {
                iconAnalysis.setOnClickListener(v -> {
                    // Navigate to Analysis Activity
                    Intent intent = new Intent(this, AnalysisActivity.class);
                    startActivity(intent);
                });
            }
            
            if (iconTrans != null) {
                iconTrans.setOnClickListener(v -> {
                    // Navigate to Transactions Activity
                    Intent intent = new Intent(this, TranActivity.class);
                    startActivity(intent);
                });
            }
            
            if (iconCategory != null) {
                iconCategory.setOnClickListener(v -> navigateToCategory());
            }
            
            if (iconUser != null) {
                iconUser.setOnClickListener(v -> {
                    Intent intent = new Intent(this, ProfileActivity.class);
                    startActivity(intent);
                });
            }
            
            // User icon click to go to profile
            if (userIcon != null) {
                userIcon.setOnClickListener(v -> {
                    Intent intent = new Intent(this, ProfileActivity.class);
                    startActivity(intent);
                });
            }
            
            // Filter button listeners
            if (tabDaily != null) {
                tabDaily.setOnClickListener(v -> {
                    updateTabSelection(tabDaily);
                    currentTimeFilter = "daily";
                    filterTransactions();
                });
            }
            
            if (tabWeekly != null) {
                tabWeekly.setOnClickListener(v -> {
                    updateTabSelection(tabWeekly);
                    currentTimeFilter = "weekly";
                    filterTransactions();
                });
            }
            
            if (tabMonthly != null) {
                tabMonthly.setOnClickListener(v -> {
                    updateTabSelection(tabMonthly);
                    currentTimeFilter = "monthly";
                    filterTransactions();
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi thiết lập listeners: " + e.getMessage(), e);
        }
    }

    private void loadUserBalance() {
        showLoader(true);
        try {
            UserUtils.getCurrentUser(new UserUtils.FetchUserCallback() {
                @Override
                public void onSuccess(User user) {
                    try {
                        showLoader(false);
                        if (user != null) {
                            currentUser = user;
                            
                            // Đảm bảo dữ liệu người dùng đầy đủ
                            ensureUserDataComplete(user);
                            
                            // Hiển thị số dư, xử lý nếu balance là null
                            if (tvBalance != null) {
                                tvBalance.setText(com.example.qltccn.utils.CurrencyUtils.formatVND(user.getBalance()));
                            }
                            
                            // Hiển thị tên người dùng, xử lý nếu name là null
                            if (welcomeText != null) {
                                welcomeText.setText(user.getName() != null ? user.getName() : "");
                            }
                            
                            // Cập nhật thông tin mục tiêu tiết kiệm
                            loadSavingsGoalData(user.getId(), null, null, null);
                            
                            // Đối với tài khoản mới, không hiển thị chi tiêu, vì chưa có giao dịch nào
                            if (FirebaseUtils.isNewAccount()) {
                                if (tvTotalExpense != null) {
                                    tvTotalExpense.setText("-0 đồng");
                                }
                                
                                // Lưu tổng chi tiêu 0 vào SharedPreferences
                                android.content.SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
                                android.content.SharedPreferences.Editor editor = prefs.edit();
                                editor.putFloat("total_expense", 0f);
                                editor.apply();
                                
                                Log.d(TAG, "Tài khoản mới tạo, thiết lập chi tiêu = 0");
                            } else {
                                // Đồng bộ với chi tiêu thực tế sau khi dữ liệu cơ bản đã hiển thị
                                new Handler().postDelayed(() -> {
                                    syncWithActualTransactions(user);
                                }, 500);
                            }
                        } else {
                            // Xử lý trường hợp user null
                            if (tvBalance != null) {
                                tvBalance.setText("0 đồng");
                            }
                            if (welcomeText != null) {
                                welcomeText.setText("");
                            }
                            if (tvTotalExpense != null) {
                                tvTotalExpense.setText("-0 đồng");
                            }
                            
                            Toast.makeText(HomeActivity.this, "Không thể tải thông tin người dùng", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Lỗi xử lý dữ liệu người dùng: " + e.getMessage(), e);
                        showDefaultUI();
                    }
                }
                
                @Override
                public void onError(String errorMessage) {
                    try {
                        showLoader(false);
                        // Hiển thị giá trị mặc định khi có lỗi
                        if (tvBalance != null) {
                            tvBalance.setText("0 đồng");
                        }
                        if (tvTotalExpense != null) {
                            tvTotalExpense.setText("-0 đồng");
                        }
                        
                        Log.e(TAG, "Lỗi tải thông tin: " + errorMessage);


                    } catch (Exception e) {
                        Log.e(TAG, "Lỗi xử lý lỗi: " + e.getMessage(), e);
                        showDefaultUI();
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi tải thông tin người dùng: " + e.getMessage(), e);
            showLoader(false);
            showDefaultUI();
        }
    }
    
    /**
     * Tải dữ liệu mục tiêu tiết kiệm từ Firestore
     */
    private void loadSavingsGoalData(String userId, ProgressBar progressBar, TextView progressText, TextView goalText) {
        FirebaseFirestore.getInstance().collection("users")
            .document(userId)
            .collection("savingsGoals")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                double totalCurrentAmount = 0;
                double totalTargetAmount = 0;
                
                if (!queryDocumentSnapshots.isEmpty()) {
                    // Tính tổng số tiền hiện tại và mục tiêu từ tất cả các mục tiêu tiết kiệm
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        // Lấy số tiền hiện tại
                        Double currentAmount = document.getDouble("currentAmount");
                        if (currentAmount != null) {
                            totalCurrentAmount += currentAmount;
                        }
                        
                        // Lấy số tiền mục tiêu
                        Double targetAmount = document.getDouble("targetAmount");
                        if (targetAmount != null) {
                            totalTargetAmount += targetAmount;
                        }
                    }
                    
                    // Cập nhật UI với dữ liệu từ cơ sở dữ liệu
                    updateSavingsGoalUI(progressBar, progressText, goalText, totalCurrentAmount, totalTargetAmount);
                } else {
                    // Không có dữ liệu mục tiêu tiết kiệm nào
                    updateSavingsGoalUI(progressBar, progressText, goalText, 0, 1);
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Lỗi khi tải dữ liệu mục tiêu tiết kiệm: " + e.getMessage());
                // Sử dụng giá trị mặc định nếu có lỗi
                updateSavingsGoalUI(progressBar, progressText, goalText, 0, 1);
            });
    }
    
    // Hiển thị/ẩn loader
    private void showLoader(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserBalance(); // Reload user data when returning to this screen
        loadRecentTransactions(); // Reload recent transactions
    }

    // Đảm bảo thông tin người dùng đầy đủ
    private void ensureUserDataComplete(User user) {
        boolean needsUpdate = false;
        FirebaseUser authUser = FirebaseUtils.getCurrentUser();
        
        if (authUser != null) {
            // Kiểm tra và cập nhật tên nếu cần
            if (user.getName() == null || user.getName().isEmpty()) {
                if (authUser.getDisplayName() != null && !authUser.getDisplayName().isEmpty()) {
                    user.setName(authUser.getDisplayName());
                    needsUpdate = true;
                } else {
                    user.setName("");
                    needsUpdate = true;
                }
            }
            
            // Kiểm tra và cập nhật email nếu cần
            if (user.getEmail() == null || user.getEmail().isEmpty()) {
                if (authUser.getEmail() != null && !authUser.getEmail().isEmpty()) {
                    user.setEmail(authUser.getEmail());
                    needsUpdate = true;
                }
            }
            
            // Lưu lại nếu có thay đổi
            if (needsUpdate) {
                updateUserData(user);
            }
        }
    }
    
    // Đồng bộ với số dư thực tế từ các giao dịch
    private void syncWithActualTransactions(User user) {
        if (user == null || user.getId() == null) return;

        try {
            // Tránh crash bằng cách đặt timeout và xử lý lỗi
            boolean[] timeoutTriggered = {false};

            // Timeout handler để đảm bảo không bao giờ bị treo
            new Handler().postDelayed(() -> {
                if (!timeoutTriggered[0]) {
                    timeoutTriggered[0] = true;
                    Log.w(TAG, "Timeout khi đồng bộ giao dịch");
                }
            }, 5000); // 5 giây timeout

            // Lấy tất cả giao dịch của người dùng
            FirebaseUtils.getUserTransactionsCollection()
                .whereEqualTo("userId", user.getId())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (timeoutTriggered[0]) return; // Nếu đã timeout thì bỏ qua
                    timeoutTriggered[0] = true;

                    try {
                        double totalIncome = 0;
                        double totalExpense = 0;

                        if (!querySnapshot.isEmpty()) {
                            for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                if (doc.contains("amount") && doc.contains("type")) {
                                    double amount = doc.getDouble("amount") != null ? doc.getDouble("amount") : 0;
                                    String type = doc.getString("type");

                                    if ("income".equals(type)) {
                                        totalIncome += amount;
                                    } else if ("expense".equals(type)) {
                                        totalExpense += amount;
                                    }
                                }
                            }
                        }
                        
                        // Chỉ cập nhật thông tin hiển thị chi tiêu, không tự động thay đổi số dư
                        updateTotalExpense(totalExpense);
                        
                        // Ghi log thông tin chi tiêu và thu nhập để debug
                        Log.d(TAG, "Thu nhập: " + totalIncome + ", Chi tiêu: " + totalExpense);
                    } catch (Exception e) {
                        Log.e(TAG, "Lỗi khi tính toán chi tiêu: " + e.getMessage(), e);
                    }
                })
                .addOnFailureListener(e -> {
                    if (!timeoutTriggered[0]) {
                        timeoutTriggered[0] = true;
                        Log.e(TAG, "Lỗi khi truy vấn giao dịch: " + e.getMessage(), e);
                    }
                });
        } catch (Exception e) {
            Log.e(TAG, "Lỗi trong quá trình đồng bộ chi tiêu: " + e.getMessage(), e);
        }
    }
    
    // Cập nhật tổng chi tiêu
    private void updateTotalExpense(double totalExpense) {
        try {
            // Hiển thị tổng chi tiêu trên UI
            if (tvTotalExpense != null) {
                tvTotalExpense.setText("-" + com.example.qltccn.utils.CurrencyUtils.formatVND(totalExpense));
                Log.d(TAG, "Đã cập nhật tổng chi tiêu: " + totalExpense);
            } else {
                Log.e(TAG, "tvTotalExpense là null, không thể cập nhật UI");
                // Thử tìm lại view
                tvTotalExpense = findViewById(R.id.tv_total_expense);
                if (tvTotalExpense != null) {
                    tvTotalExpense.setText("-" + com.example.qltccn.utils.CurrencyUtils.formatVND(totalExpense));
                }
            }
            
            // Lưu tổng chi tiêu vào SharedPreferences để các màn hình khác có thể truy cập
            android.content.SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
            android.content.SharedPreferences.Editor editor = prefs.edit();
            editor.putFloat("total_expense", (float) totalExpense);
            editor.apply();
            
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi cập nhật tổng chi tiêu: " + e.getMessage(), e);
        }
    }
    
    // Cập nhật số dư người dùng
    private void updateUserBalance(User user, double newBalance) {
        if (user == null || user.getId() == null) return;
        
        try {
            // Cập nhật dữ liệu người dùng cục bộ
            user.setBalance(newBalance);
            
            // Cập nhật UI trước để có UX tốt
            if (tvBalance != null) {
                tvBalance.setText(com.example.qltccn.utils.CurrencyUtils.formatVND(newBalance));
            }
            
            // Cập nhật lên Firestore
            FirebaseUtils.getUsersCollection()
                .document(user.getId())
                .update("balance", newBalance, "updatedAt", new Date())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Cập nhật số dư thành công: " + newBalance);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi cập nhật số dư: " + e.getMessage(), e);
                    // Nếu cập nhật thất bại, hiển thị lại giá trị cũ
                    if (tvBalance != null) {
                        tvBalance.setText(com.example.qltccn.utils.CurrencyUtils.formatVND(user.getBalance()));
                    }
                });
        } catch (Exception e) {
            Log.e(TAG, "Lỗi trong quá trình cập nhật số dư: " + e.getMessage(), e);
        }
    }
    
    // Cập nhật toàn bộ thông tin người dùng
    private void updateUserData(User user) {
        FirebaseUtils.getUsersCollection()
            .document(user.getId())
            .set(user)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Cập nhật thông tin người dùng thành công");
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Lỗi cập nhật thông tin người dùng: " + e.getMessage());
            });
    }

    // Hiển thị UI mặc định khi có lỗi
    private void showDefaultUI() {
        try {
            // Hiển thị giao diện mặc định khi có lỗi
            if (tvBalance != null) {
                tvBalance.setText("0 đồng");
            }
            
            if (welcomeText != null) {
                welcomeText.setText("");
            }
            
            if (tvTotalExpense != null) {
                tvTotalExpense.setText("-0 đồng");
            }
            
            // Đặt giá trị mặc định cho giao dịch gần nhất
            if (tvLastIncome != null) {
                tvLastIncome.setText("0 đồng");
            }
            
            if (tvLastExpense != null) {
                tvLastExpense.setText("0 đồng");
            }
            
            showLoader(false);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi hiển thị UI mặc định: " + e.getMessage(), e);
        }
    }

    // Thêm phương thức chuyển tới màn hình danh mục
    private void navigateToCategory() {
        try {
            Log.d(TAG, "Chuyển tới màn hình danh mục");
            Intent intent = new Intent(this, CategoryActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi chuyển tới màn hình danh mục: " + e.getMessage());
            Toast.makeText(this, "Không thể mở màn hình danh mục", Toast.LENGTH_SHORT).show();
        }
    }

    // Load recent transactions from Firestore
    private void loadRecentTransactions() {
        if (FirebaseUtils.getCurrentUser() == null) {
            Log.e(TAG, "loadRecentTransactions: Người dùng chưa đăng nhập");
            return;
        }
        
        String userId = FirebaseUtils.getCurrentUser().getUid();
        Log.d(TAG, "loadRecentTransactions: Đang tải giao dịch cho userId: " + userId);
        
        // Kiểm tra tham chiếu transactions collection
        CollectionReference transactionsRef = FirebaseUtils.getUserTransactionsCollection();
        if (transactionsRef == null) {
            Log.e(TAG, "loadRecentTransactions: Không thể lấy tham chiếu đến collection giao dịch");
            return;
        }
        
        Log.d(TAG, "loadRecentTransactions: Đường dẫn collection: " + transactionsRef.getPath());
        
        // Lấy tất cả giao dịch
        transactionsRef
            .whereEqualTo("userId", userId)
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                Log.d(TAG, "loadRecentTransactions: Đã tìm thấy " + queryDocumentSnapshots.size() + " giao dịch");
                allTransactions.clear();
                
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    try {
                        // Xử lý thủ công thay vì sử dụng toObject() để xử lý vấn đề với Timestamp
                        Transaction transaction = new Transaction();
                        transaction.setId(doc.getId());
                        
                        // Lấy các trường dữ liệu thông thường
                        if (doc.contains("userId")) transaction.setUserId(doc.getString("userId"));
                        if (doc.contains("category")) transaction.setCategory(doc.getString("category"));
                        if (doc.contains("amount") && doc.get("amount") instanceof Number) {
                            transaction.setAmount(((Number) doc.get("amount")).doubleValue());
                        }
                        if (doc.contains("description")) transaction.setDescription(doc.getString("description"));
                        if (doc.contains("note")) transaction.setNote(doc.getString("note"));
                        if (doc.contains("type")) transaction.setType(doc.getString("type"));
                        
                        // Xử lý trường date - chuyển từ Timestamp sang long
                        if (doc.contains("date")) {
                            Object dateObj = doc.get("date");
                            if (dateObj instanceof com.google.firebase.Timestamp) {
                                com.google.firebase.Timestamp timestamp = (com.google.firebase.Timestamp) dateObj;
                                transaction.setDate(timestamp.toDate().getTime());
                            } else if (dateObj instanceof Number) {
                                transaction.setDate(((Number) dateObj).longValue());
                            }
                        }
                        
                        // Xử lý các trường thời gian khác nếu cần
                        if (doc.contains("createdAt")) {
                            Object createdAtObj = doc.get("createdAt");
                            if (createdAtObj instanceof com.google.firebase.Timestamp) {
                                com.google.firebase.Timestamp timestamp = (com.google.firebase.Timestamp) createdAtObj;
                                transaction.setCreatedAt(timestamp.toDate().getTime());
                            } else if (createdAtObj instanceof Number) {
                                transaction.setCreatedAt(((Number) createdAtObj).longValue());
                            } else {
                                transaction.setCreatedAt(System.currentTimeMillis());
                            }
                        } else {
                            transaction.setCreatedAt(System.currentTimeMillis());
                        }
                        
                        if (doc.contains("updatedAt")) {
                            Object updatedAtObj = doc.get("updatedAt");
                            if (updatedAtObj instanceof com.google.firebase.Timestamp) {
                                com.google.firebase.Timestamp timestamp = (com.google.firebase.Timestamp) updatedAtObj;
                                transaction.setUpdatedAt(timestamp.toDate().getTime());
                            } else if (updatedAtObj instanceof Number) {
                                transaction.setUpdatedAt(((Number) updatedAtObj).longValue());
                            } else {
                                transaction.setUpdatedAt(System.currentTimeMillis());
                            }
                        } else {
                            transaction.setUpdatedAt(System.currentTimeMillis());
                        }
                        
                        allTransactions.add(transaction);
                        Log.d(TAG, "loadRecentTransactions: Đã thêm giao dịch: " + transaction.getCategory() + ", " + transaction.getAmount());
                    } catch (Exception e) {
                        Log.e(TAG, "loadRecentTransactions: Lỗi khi chuyển đổi dữ liệu giao dịch: " + e.getMessage() + ", DocumentID: " + doc.getId());
                    }
                }
                
                Log.d(TAG, "loadRecentTransactions: Tổng số giao dịch đã tải: " + allTransactions.size());
                
                // Cập nhật thông tin giao dịch gần nhất
                updateLatestTransactionsInfo();
                
                // Lọc và hiển thị giao dịch gần đây
                filterTransactions();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "loadRecentTransactions: Lỗi khi tải giao dịch gần đây: " + e.getMessage());
            });
    }
    
    /**
     * Cập nhật tab được chọn
     */
    private void updateTabSelection(Button selectedTab) {
        // Reset tất cả tab về trạng thái không được chọn
        if (tabDaily != null) {
            tabDaily.setSelected(false);
            tabDaily.setBackgroundResource(android.R.color.transparent);
            tabDaily.setTextColor(getResources().getColor(android.R.color.black));
        }
        
        if (tabWeekly != null) {
            tabWeekly.setSelected(false);
            tabWeekly.setBackgroundResource(android.R.color.transparent);
            tabWeekly.setTextColor(getResources().getColor(android.R.color.black));
        }
        
        if (tabMonthly != null) {
            tabMonthly.setSelected(false);
            tabMonthly.setBackgroundResource(android.R.color.transparent);
            tabMonthly.setTextColor(getResources().getColor(android.R.color.black));
        }
        
        // Đặt tab được chọn
        if (selectedTab != null) {
            selectedTab.setSelected(true);
            selectedTab.setBackgroundResource(R.drawable.selected_tab_background);
            selectedTab.setTextColor(getResources().getColor(android.R.color.white));
        }
    }
    
    /**
     * Lọc giao dịch dựa trên chế độ đã chọn (daily, weekly, monthly)
     */
    private void filterTransactions() {
        recentTransactions.clear();
        
        if (allTransactions.isEmpty()) {
            updateTransactionsUI();
            return;
        }
        
        Calendar calendar = Calendar.getInstance();
        long currentTime = calendar.getTimeInMillis();
        long startTime;
        
        // Biến để tính tổng chi tiêu theo thời kỳ đã chọn
        double periodTotalExpense = 0;
        
        switch (currentTimeFilter) {
            case "daily":
                // Lấy giao dịch trong ngày
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                startTime = calendar.getTimeInMillis();
                
                Log.d(TAG, "filterTransactions: Lọc theo ngày từ " + new Date(startTime) + " đến " + new Date(currentTime));
                
                for (Transaction transaction : allTransactions) {
                    if (transaction.getDate() >= startTime && transaction.getDate() <= currentTime) {
                        // Thêm tất cả giao dịch vào danh sách hiển thị (bao gồm cả tiết kiệm)
                        recentTransactions.add(transaction);
                        
                        // Tính tổng chi tiêu chỉ cho loại "expense" (không bao gồm "savings")
                        if ("expense".equals(transaction.getType())) {
                            periodTotalExpense += transaction.getAmount();
                        }
                    }
                }
                break;
                
            case "weekly":
                // Lấy giao dịch trong tuần (7 ngày qua)
                calendar.add(Calendar.DAY_OF_MONTH, -7);
                startTime = calendar.getTimeInMillis();
                
                Log.d(TAG, "filterTransactions: Lọc theo tuần từ " + new Date(startTime) + " đến " + new Date(currentTime));
                
                for (Transaction transaction : allTransactions) {
                    if (transaction.getDate() >= startTime && transaction.getDate() <= currentTime) {
                        // Thêm tất cả giao dịch vào danh sách hiển thị (bao gồm cả tiết kiệm)
                        recentTransactions.add(transaction);
                        
                        // Tính tổng chi tiêu chỉ cho loại "expense" (không bao gồm "savings")
                        if ("expense".equals(transaction.getType())) {
                            periodTotalExpense += transaction.getAmount();
                        }
                    }
                }
                break;
                
            case "monthly":
            default:
                // Lấy giao dịch trong tháng
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                startTime = calendar.getTimeInMillis();
                
                Log.d(TAG, "filterTransactions: Lọc theo tháng từ " + new Date(startTime) + " đến " + new Date(currentTime));
                
                for (Transaction transaction : allTransactions) {
                    if (transaction.getDate() >= startTime && transaction.getDate() <= currentTime) {
                        // Thêm tất cả giao dịch vào danh sách hiển thị (bao gồm cả tiết kiệm)
                        recentTransactions.add(transaction);
                        
                        // Tính tổng chi tiêu chỉ cho loại "expense" (không bao gồm "savings")
                        if ("expense".equals(transaction.getType())) {
                            periodTotalExpense += transaction.getAmount();
                        }
                    }
                }
                break;
        }
        
        // Cập nhật tổng chi tiêu theo thời kỳ đã chọn
        updateTotalExpense(periodTotalExpense);
        Log.d(TAG, "filterTransactions: Tổng chi tiêu theo " + currentTimeFilter + ": " + periodTotalExpense);
        
        // Giới hạn số lượng giao dịch hiển thị (lấy 5 giao dịch gần nhất)
        if (recentTransactions.size() > 5) {
            recentTransactions = recentTransactions.subList(0, 5);
        }
        
        Log.d(TAG, "filterTransactions: Hiển thị " + recentTransactions.size() + " giao dịch sau khi lọc theo " + currentTimeFilter);
        
        // Cập nhật UI
        updateTransactionsUI();
    }
    
    // Update the UI with recent transactions
    private void updateTransactionsUI() {
        try {
            Log.d(TAG, "updateTransactionsUI: Cập nhật UI với " + recentTransactions.size() + " giao dịch");
            
            // If we have a RecyclerView, update it
            if (recyclerViewTransactions != null && transactionAdapter != null) {
                Log.d(TAG, "updateTransactionsUI: Sử dụng RecyclerView để hiển thị");
                transactionAdapter.updateData(recentTransactions);
                
                // Show/hide empty state text
                TextView emptyText = findViewById(R.id.emptyTransactionsText);
                if (emptyText != null) {
                    if (recentTransactions.isEmpty()) {
                        Log.d(TAG, "updateTransactionsUI: Hiển thị trạng thái trống");
                        recyclerViewTransactions.setVisibility(View.GONE);
                        emptyText.setVisibility(View.VISIBLE);
                    } else {
                        Log.d(TAG, "updateTransactionsUI: Hiển thị danh sách " + recentTransactions.size() + " giao dịch");
                        recyclerViewTransactions.setVisibility(View.VISIBLE);
                        emptyText.setVisibility(View.GONE);
                    }
                } else {
                    Log.e(TAG, "updateTransactionsUI: emptyText là null");
                }
                return;
            } else {
                Log.w(TAG, "updateTransactionsUI: RecyclerView hoặc adapter là null, sử dụng cách hiển thị thay thế");
            }
            
            // Otherwise, update the static transaction views in layout
            try {
                // Clear existing transaction views if container exists
                if (transactionsContainer != null && transactionsContainer instanceof android.view.ViewGroup) {
                    android.view.ViewGroup container = (android.view.ViewGroup) transactionsContainer;
                    container.removeAllViews();
                    
                    // Show message if no transactions
                    if (recentTransactions.isEmpty()) {
                        Log.d(TAG, "updateTransactionsUI: Hiển thị thông báo trống bằng TextView");
                        TextView emptyText = new TextView(this);
                        emptyText.setText("Chưa có giao dịch nào");
                        emptyText.setTextSize(16);
                        emptyText.setGravity(android.view.Gravity.CENTER);
                        emptyText.setPadding(32, 32, 32, 32);
                        container.addView(emptyText);
                    } else {
                        Log.d(TAG, "updateTransactionsUI: Hiển thị " + recentTransactions.size() + " giao dịch theo cách thủ công");
                        // Add transaction items manually
                        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm - dd MMM", Locale.getDefault());
                        
                        for (Transaction transaction : recentTransactions) {
                            Log.d(TAG, "updateTransactionsUI: Thêm giao dịch: " + transaction.getCategory() + " - " + transaction.getAmount());
                            View transactionView = getLayoutInflater().inflate(R.layout.item_transaction, null);
                            
                            ImageView iconView = transactionView.findViewById(R.id.ivCategoryIcon);
                            TextView categoryView = transactionView.findViewById(R.id.tvCategory);
                            TextView dateView = transactionView.findViewById(R.id.tvDate);
                            TextView amountView = transactionView.findViewById(R.id.tvAmount);
                            TextView descView = transactionView.findViewById(R.id.tvDescription);
                            
                            // Set data
                            if (iconView != null) {
                                iconView.setImageResource(getCategoryIcon(transaction.getCategory()));
                            }
                            
                            if (categoryView != null) {
                                categoryView.setText(transaction.getCategory());
                            }
                            
                            if (descView != null) {
                                descView.setText(transaction.getDescription() != null && !transaction.getDescription().isEmpty() 
                                    ? transaction.getDescription() : "");
                            }
                            
                            if (dateView != null) {
                                dateView.setText(dateFormat.format(new Date(transaction.getDate())));
                            }
                            
                            if (amountView != null) {
                                String prefix;
                                int color;
                                
                                // Xác định prefix và màu sắc dựa vào loại giao dịch
                                switch (transaction.getType()) {
                                    case "income":
                                        prefix = "+";
                                        color = getResources().getColor(android.R.color.holo_green_dark);
                                        break;
                                    case "expense":
                                        prefix = "-";
                                        color = getResources().getColor(android.R.color.holo_red_dark);
                                        break;
                                    case "savings":
                                        prefix = "-";
                                        color = getResources().getColor(android.R.color.holo_blue_dark);
                                        break;
                                    default:
                                        prefix = "";
                                        color = getResources().getColor(android.R.color.black);
                                        break;
                                }
                                
                                String formattedAmount = String.format(Locale.getDefault(), "%s%,.0f đồng", 
                                        prefix, transaction.getAmount());
                                amountView.setText(formattedAmount);
                                amountView.setTextColor(color);
                            }

                            // Add click listener
                            transactionView.setOnClickListener(v -> {
                                showTransactionDetail(transaction);
                            });

                            // Add to container
                            container.addView(transactionView);
                        }
                    }
                } else {
                    Log.e(TAG, "updateTransactionsUI: transactionsContainer là null hoặc không phải ViewGroup");
                }
            } catch (Exception e) {
                Log.e(TAG, "updateTransactionsUI: Lỗi khi cập nhật UI giao dịch: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            Log.e(TAG, "updateTransactionsUI: Lỗi khi cập nhật UI giao dịch: " + e.getMessage(), e);
        }
    }

    // Helper method to get icon resource for a category
    private int getCategoryIcon(String category) {
        if (category == null) return R.drawable.ic_category;
        
        switch (category.toLowerCase()) {
            case "ăn uống":
            case "food":
                return R.drawable.ic_food;
                
            case "di chuyển":
            case "transport":
                return R.drawable.ic_transport;
                
            case "mua sắm":
            case "shopping":
                return R.drawable.ic_shopping;
                
            case "nhà cửa":
            case "housing":
                return R.drawable.ic_home;
                
            case "giải trí":
            case "entertainment":
                return R.drawable.ic_entertainment;
                
            case "giáo dục":
            case "education":
                return R.drawable.ic_savings;
                
            case "sức khỏe":
            case "health":
                return R.drawable.ic_health;
                
            case "lương":
            case "salary":
                return R.drawable.ic_salary;
                
            case "đầu tư":
            case "investment":
                return R.drawable.ic_stock;
                
            case "tiết kiệm":
            case "savings":
                return R.drawable.ic_savings;
                
            default:
                return R.drawable.ic_category;
        }
    }

    // Show transaction detail in a dialog
    private void showTransactionDetail(Transaction transaction) {
        try {
            // Sử dụng dialog tùy chỉnh thay vì AlertDialog
            android.app.Dialog dialog = new android.app.Dialog(this);
            dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_transaction_detail);
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            
            // Get references to views in the dialog
            TextView tvCategory = dialog.findViewById(R.id.tvDetailCategory);
            TextView tvAmount = dialog.findViewById(R.id.tvDetailAmount);
            TextView tvDate = dialog.findViewById(R.id.tvDetailDate);
            TextView tvDescription = dialog.findViewById(R.id.tvDetailDescription);
            TextView tvType = dialog.findViewById(R.id.tvDetailType);
            Button btnClose = dialog.findViewById(R.id.btnClose);
            Button btnEdit = dialog.findViewById(R.id.btnEdit);

            // Set data
            if (tvCategory != null) tvCategory.setText("Danh mục: " + transaction.getCategory());

            if (tvAmount != null) {
                String prefix;
                int color;
                
                // Xác định prefix và màu sắc dựa vào loại giao dịch
                switch (transaction.getType()) {
                    case "income":
                        prefix = "+";
                        color = getResources().getColor(android.R.color.holo_green_dark);
                        break;
                    case "expense":
                        prefix = "-";
                        color = getResources().getColor(android.R.color.holo_red_dark);
                        break;
                    case "savings":
                        prefix = "-";
                        color = getResources().getColor(android.R.color.holo_blue_dark);
                        break;
                    default:
                        prefix = "";
                        color = getResources().getColor(android.R.color.black);
                        break;
                }
                
                String formattedAmount = String.format(Locale.getDefault(), "%s%,.0f đồng", 
                        prefix, transaction.getAmount());
                tvAmount.setText("Số tiền: " + formattedAmount);
                tvAmount.setTextColor(color);
            }
            
            if (tvDate != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                tvDate.setText("Ngày: " + dateFormat.format(new Date(transaction.getDate())));
            }
            
            if (tvType != null) {
                String typeText;
                switch (transaction.getType()) {
                    case "income":
                        typeText = "Thu nhập";
                        break;
                    case "expense":
                        typeText = "Chi tiêu";
                        break;
                    case "savings":
                        typeText = "Tiết kiệm";
                        break;
                    default:
                        typeText = transaction.getType();
                }
                tvType.setText("Loại: " + typeText);
            }
            
            if (tvDescription != null) {
                String description = transaction.getDescription();
                if (description == null || description.isEmpty()) {
                    description = "Không có mô tả";
                }
                tvDescription.setText("Mô tả: " + description);
            }
            
            // Xử lý nút Đóng
            if (btnClose != null) {
                btnClose.setOnClickListener(v -> dialog.dismiss());
            }
            
            // Xử lý nút Chỉnh sửa
            if (btnEdit != null) {
                btnEdit.setOnClickListener(v -> {
                    // Navigate to TranActivity to edit
                    Intent intent = new Intent(HomeActivity.this, TranActivity.class);
                    intent.putExtra("TRANSACTION_ID", transaction.getId());
                    intent.putExtra("ACTION", "EDIT_TRANSACTION");
                    startActivity(intent);
                    dialog.dismiss();
                });
            }

            // Hiển thị dialog
            dialog.show();
            
        } catch (Exception e) {
            Log.e(TAG, "Lỗi hiển thị chi tiết giao dịch: " + e.getMessage(), e);
            Toast.makeText(this, "Không thể hiển thị chi tiết giao dịch", Toast.LENGTH_SHORT).show();
        }
    }

    // Thêm phương thức chuyển đến màn hình thông báo
    private void navigateToNotification() {
        try {
            Log.d(TAG, "Hiển thị thông báo chức năng đang phát triển");
            // Thay vì mở NotiActivity, hiển thị thông báo tính năng đang phát triển
            Toast.makeText(this, "Tính năng đang phát triển", Toast.LENGTH_SHORT).show();
            
            // Có thể bổ sung thêm rung nhẹ để tăng trải nghiệm người dùng
            android.os.Vibrator vibrator = (android.os.Vibrator) getSystemService(android.content.Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                // Rung nhẹ 100ms
                vibrator.vibrate(100);
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi hiển thị thông báo: " + e.getMessage(), e);
        }
    }

    /**
     * Cập nhật giao diện cho mục tiêu tiết kiệm
     * 
     * @param progressBar Thanh tiến trình
     * @param progressText Text hiển thị phần trăm
     * @param goalText Text hiển thị số tiền hiện tại/mục tiêu
     * @param currentAmount Số tiền hiện tại
     * @param targetAmount Mục tiêu
     */
    private void updateSavingsGoalUI(ProgressBar progressBar, TextView progressText, TextView goalText, 
                                     double currentAmount, double targetAmount) {
        try {
            if (progressBar == null || progressText == null || goalText == null) {
                return;
            }
            
            // Đảm bảo targetAmount không thể là 0 để tránh chia cho 0
            if (targetAmount <= 0) {
                targetAmount = 1;
            }
            
            // Tính phần trăm hoàn thành
            int progress = (int) ((currentAmount / targetAmount) * 100);
            
            // Giới hạn tiến độ từ 0-100%
            progress = Math.min(100, Math.max(0, progress));
            
            // Cập nhật UI
            progressBar.setProgress(progress);
            progressText.setText(progress + "%");
            
            // Định dạng và hiển thị số tiền mục tiêu
            goalText.setText(CurrencyUtils.formatVND(targetAmount));
            
            // Log để debug
            Log.d(TAG, "Cập nhật UI mục tiêu tiết kiệm: " + progress + "%, " + 
                  "Hiện tại: " + CurrencyUtils.formatVND(currentAmount) + ", " + 
                  "Mục tiêu: " + CurrencyUtils.formatVND(targetAmount));
            
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi cập nhật UI mục tiêu tiết kiệm: " + e.getMessage());
        }
    }

    /**
     * Cập nhật thông tin về giao dịch gần nhất
     * Sẽ được gọi sau khi tải dữ liệu giao dịch
     */
    private void updateLatestTransactionsInfo() {
        try {
            Transaction lastIncome = null;
            Transaction lastExpense = null;
            
            // Tìm giao dịch nạp tiền và chi tiêu gần nhất
            for (Transaction transaction : allTransactions) {
                if ("income".equals(transaction.getType()) && (lastIncome == null || transaction.getDate() > lastIncome.getDate())) {
                    lastIncome = transaction;
                } else if ("expense".equals(transaction.getType()) && (lastExpense == null || transaction.getDate() > lastExpense.getDate())) {
                    lastExpense = transaction;
                }
            }
            
            // Cập nhật UI cho giao dịch nạp tiền gần nhất
            if (tvLastIncome != null) {
                if (lastIncome != null) {
                    tvLastIncome.setText(com.example.qltccn.utils.CurrencyUtils.formatVND(lastIncome.getAmount()));
                    Log.d(TAG, "Giao dịch nạp tiền gần nhất: " + lastIncome.getAmount() + " đồng (" + lastIncome.getCategory() + ")");
                } else {
                    tvLastIncome.setText("0 đồng");
                    Log.d(TAG, "Không có giao dịch nạp tiền nào");
                }
            }
            
            // Cập nhật UI cho giao dịch chi tiêu gần nhất
            if (tvLastExpense != null) {
                if (lastExpense != null) {
                    tvLastExpense.setText("-" + com.example.qltccn.utils.CurrencyUtils.formatVND(lastExpense.getAmount()));
                    Log.d(TAG, "Giao dịch chi tiêu gần nhất: " + lastExpense.getAmount() + " đồng (" + lastExpense.getCategory() + ")");
                } else {
                    tvLastExpense.setText("0 đồng");
                    Log.d(TAG, "Không có giao dịch chi tiêu nào");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi cập nhật thông tin giao dịch gần nhất: " + e.getMessage(), e);
        }
    }

    // Phương thức để mở màn hình Savings
    private void navigateToSavings() {
        try {
            Intent intent = new Intent(this, SavingsActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi chuyển đến màn hình Savings: " + e.getMessage());
            Toast.makeText(this, "Không thể mở màn hình Savings", Toast.LENGTH_SHORT).show();
        }
    }
}