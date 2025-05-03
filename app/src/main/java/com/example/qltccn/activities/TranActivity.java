package com.example.qltccn.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qltccn.R;
import com.example.qltccn.models.Transaction;
import com.example.qltccn.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;

public class TranActivity extends AppCompatActivity {

    // UI components
    private ImageView backButton, iconHome, iconChart, iconTrans, iconCategory, iconUser;
    private TextView titleText, totalBalanceText, incomeText, expenseText;

    // Firebase components
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String userId;

    // Data
    private List<Transaction> transactionList;

    // Khai báo biến class TAG để dễ dàng log
    private static final String TAG = "TranActivity";
    
    // Biến để lưu dữ liệu user
    private User currentUser;

    // Thêm biến để theo dõi trạng thái lọc
    private String currentFilter = "all"; // "all", "income", "expense"
    private List<Transaction> filteredTransactionList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transation);

        // Initialize Firebase components
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            // Redirect to login if not logged in
            startActivity(new Intent(TranActivity.this, LoginActivity.class));
            finish();
            return;
        }

        // Initialize UI components
        initializeUI();

        // Initialize transaction list
        transactionList = new ArrayList<>();

        // Set up listeners
        setupListeners();
        
        // Xử lý intent nhận từ màn hình Cate
        handleIntent();
        
        // Load user data first, then load transactions
        loadUserData();
    }

    private void initializeUI() {
        // Header elements
        backButton = findViewById(R.id.backButton);
        titleText = findViewById(R.id.titleText);
        
        // Thêm xử lý nút notification
        View notiContainer = findViewById(R.id.notiContainer);
        if (notiContainer != null) {
            notiContainer.setOnClickListener(v -> navigateToNotification());
        }
        
        // Summary elements - thêm các ID tương ứng trong layout
        totalBalanceText = findViewById(R.id.totalBalanceText);
        incomeText = findViewById(R.id.incomeText);
        expenseText = findViewById(R.id.expenseText);

        // Footer navigation
        iconHome = findViewById(R.id.iconHome);
        iconChart = findViewById(R.id.iconChart);
        iconTrans = findViewById(R.id.iconTrans);
        iconCategory = findViewById(R.id.iconCategory);
        iconUser = findViewById(R.id.iconUser);
        
        // Thiết lập sự kiện click cho ô Income và Expense
        View incomeLayout = findViewById(R.id.incomeLayout);
        View expenseLayout = findViewById(R.id.expenseLayout);
        
        if (incomeLayout != null) {
            incomeLayout.setOnClickListener(v -> {
                toggleFilter("income");
            });
        }
        
        if (expenseLayout != null) {
            expenseLayout.setOnClickListener(v -> {
                toggleFilter("expense");
            });
        }

        // Đặt biểu tượng Transaction là đã được chọn
        if (iconTrans != null) {
            iconTrans.setImageResource(R.drawable.ic_transaction1);
        }
    }

    private void setupListeners() {
        // Back button
        backButton.setOnClickListener(v -> finish());

        // Bottom navigation
        iconHome.setOnClickListener(v -> navigateToHome());
        iconChart.setOnClickListener(v -> navigateToChart());
        iconTrans.setOnClickListener(v -> {
            // Already in TranActivity, do nothing
        });
        iconCategory.setOnClickListener(v -> navigateToCategory());
        iconUser.setOnClickListener(v -> navigateToProfile());
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // Tải lại thông tin người dùng khi quay lại màn hình này
        // đảm bảo dữ liệu được cập nhật sau khi sửa đổi ở EditProfileActivity
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            userId = firebaseUser.getUid();
            loadUserData();
            loadTransactions();
        } else {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }
    
    private void loadUserData() {
        Log.d("TranActivity", "Đang tải thông tin người dùng...");
        if (userId == null || userId.isEmpty()) {
            Log.e("TranActivity", "userId là null hoặc rỗng trong loadUserData");
            return;
        }

        // Tải từ Firestore trước
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                Log.d("TranActivity", "Tải thông tin người dùng từ Firestore thành công");
                User user = documentSnapshot.toObject(User.class);
                if (user != null) {
                    updateUIWithUserData(user);
                } else {
                    Log.w("TranActivity", "Không tìm thấy user trong Firestore, thử tải từ Realtime DB");
                    loadUserFromRealtimeDB();
                }
            })
            .addOnFailureListener(e -> {
                Log.e("TranActivity", "Lỗi tải từ Firestore: " + e.getMessage());
                // Thử tải từ Realtime Database nếu Firestore thất bại
                loadUserFromRealtimeDB();
            });
    }
    
    private void loadUserFromRealtimeDB() {
        if (userId == null || userId.isEmpty()) {
            Log.e("TranActivity", "userId là null hoặc rỗng trong loadUserFromRealtimeDB");
            return;
        }
        
        FirebaseDatabase.getInstance().getReference()
            .child("users")
            .child(userId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Log.d("TranActivity", "Tải thông tin người dùng từ Realtime DB thành công");
                        try {
                            User user = snapshot.getValue(User.class);
                            if (user != null) {
                                updateUIWithUserData(user);
                            } else {
                                Log.e("TranActivity", "Không thể chuyển đổi dữ liệu từ Realtime DB thành User");
                            }
                        } catch (Exception e) {
                            Log.e("TranActivity", "Lỗi xử lý dữ liệu từ Realtime DB: " + e.getMessage());
                        }
                    } else {
                        Log.e("TranActivity", "Không tìm thấy user trong Realtime DB");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("TranActivity", "Lỗi tải từ Realtime DB: " + error.getMessage());
                }
            });
    }
    
    private void updateUIWithUserData(User user) {
        try {
            if (user == null) {
                Log.e(TAG, "User là null trong updateUIWithUserData");
                return;
            }
            
            // Lưu user hiện tại để sử dụng khi cần
            this.currentUser = user;
            
            Log.d(TAG, "Cập nhật UI với dữ liệu người dùng: " + user.getName() + ", balance: " + user.getBalance());
            
            if (totalBalanceText != null) {
                totalBalanceText.setText(com.example.qltccn.utils.CurrencyUtils.formatVND(user.getBalance()));
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi cập nhật UI với dữ liệu người dùng: " + e.getMessage());
        }
    }
    
    private void updateUserBalance(double newBalance) {
        try {
            // Chỉ cập nhật nếu userId hợp lệ
            if (userId == null || userId.isEmpty()) return;
            
            // Cập nhật số dư trong database
            mDatabase.child("users").child(userId).child("balance").setValue(newBalance)
                .addOnSuccessListener(aVoid -> {
                    // Cập nhật thành công
                    if (totalBalanceText != null) {
                        totalBalanceText.setText(com.example.qltccn.utils.CurrencyUtils.formatVND(newBalance));
                    }
                    Log.d("TranActivity", "Cập nhật số dư thành công: " + newBalance);
                })
                .addOnFailureListener(e -> {
                    Log.e("TranActivity", "Lỗi cập nhật số dư: " + e.getMessage());
                });
        } catch (Exception e) {
            Log.e("TranActivity", "Lỗi cập nhật số dư: " + e.getMessage());
        }
    }
    
    private void loadTransactions() {
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng để tải giao dịch", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Bắt đầu tải giao dịch từ Firestore cho userId: " + userId);
        
        // Xóa danh sách cũ
        transactionList.clear();
        
        // Sử dụng mảng để lưu trữ tổng thu nhập và chi tiêu, cho phép sửa đổi trong lambda
        final double[] totalValues = {0.0, 0.0}; // [0]: thu nhập, [1]: chi tiêu
        
        // Đầu tiên, thử lấy từ Firestore subcollection
        FirebaseFirestore.getInstance().collection("users")
            .document(userId)
            .collection("transactions")
            .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                Log.d(TAG, "Đã tìm thấy " + queryDocumentSnapshots.size() + " giao dịch trong Firestore");
                
                boolean hasTransactions = false;
                
                for (com.google.firebase.firestore.DocumentSnapshot document : queryDocumentSnapshots) {
                    try {
                        // Thay vì dùng document.toObject(), xử lý thủ công để tránh lỗi chuyển đổi
                        Transaction transaction = new Transaction();
                        transaction.setId(document.getId());
                        
                        // Lấy các trường dữ liệu thông thường
                        if (document.contains("userId")) transaction.setUserId(document.getString("userId"));
                        if (document.contains("category")) transaction.setCategory(document.getString("category"));
                        if (document.contains("amount") && document.get("amount") instanceof Number) {
                            transaction.setAmount(((Number) document.get("amount")).doubleValue());
                        }
                        if (document.contains("description")) transaction.setDescription(document.getString("description"));
                        if (document.contains("note")) transaction.setNote(document.getString("note"));
                        if (document.contains("type")) transaction.setType(document.getString("type"));
                        
                        // Xử lý riêng các trường thời gian
                        // date field
                        if (document.contains("date")) {
                            Object dateObj = document.get("date");
                            if (dateObj instanceof com.google.firebase.Timestamp) {
                                com.google.firebase.Timestamp timestamp = (com.google.firebase.Timestamp) dateObj;
                                transaction.setDate(timestamp.toDate().getTime());
                            } else if (dateObj instanceof Number) {
                                transaction.setDate(((Number) dateObj).longValue());
                            }
                        }
                        
                        // createdAt field
                        if (document.contains("createdAt")) {
                            Object createdAtObj = document.get("createdAt");
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
                        
                        // updatedAt field
                        if (document.contains("updatedAt")) {
                            Object updatedAtObj = document.get("updatedAt");
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
                        
                        // Kiểm tra dữ liệu hợp lệ trước khi thêm vào danh sách
                        if (transaction.getUserId() != null && transaction.getCategory() != null) {
                            hasTransactions = true;
                            transactionList.add(transaction);
                            
                            // Cập nhật tổng thu/chi
                            if ("income".equals(transaction.getType())) {
                                totalValues[0] += transaction.getAmount(); // Cập nhật tổng thu nhập
                            } else if ("expense".equals(transaction.getType())) {
                                totalValues[1] += transaction.getAmount(); // Cập nhật tổng chi tiêu
                            }
                            
                            Log.d(TAG, "Đã tải giao dịch: " + transaction.getCategory() + 
                                    ", " + transaction.getAmount() + ", date: " + new Date(transaction.getDate()));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Lỗi khi chuyển đổi document thành Transaction: " + e.getMessage(), e);
                    }
                }
                
                if (hasTransactions) {
                    // Cập nhật UI với dữ liệu từ Firestore
                    updateIncomesAndExpenses(totalValues[0], totalValues[1]);
                    displayTransactions();
                } else {
                    // Thử tải từ Realtime Database nếu không có dữ liệu trong Firestore
                    loadTransactionsFromRealtimeDB();
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Lỗi tải giao dịch từ Firestore: " + e.getMessage());
                // Thử tải từ Realtime Database
                loadTransactionsFromRealtimeDB();
            });
    }
    
    private void loadTransactionsFromRealtimeDB() {
        Log.d(TAG, "Tải giao dịch từ Realtime Database");
        
        mDatabase.child("transactions")
            .orderByChild("userId")
            .equalTo(userId)
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    try {
                        transactionList.clear();
                        
                        // Sử dụng mảng để lưu trữ tổng thu nhập và chi tiêu
                        final double[] totalValues = {0.0, 0.0}; // [0]: thu nhập, [1]: chi tiêu
                        
                        boolean hasTransactions = false;
                        
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            try {
                                Transaction transaction = snapshot.getValue(Transaction.class);
                                if (transaction != null) {
                                    hasTransactions = true;
                                    transactionList.add(transaction);
                                    
                                    // Cập nhật tổng thu/chi
                                    if ("income".equals(transaction.getType())) {
                                        totalValues[0] += transaction.getAmount(); // Cập nhật tổng thu nhập
                                    } else if ("expense".equals(transaction.getType())) {
                                        totalValues[1] += transaction.getAmount(); // Cập nhật tổng chi tiêu
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Lỗi xử lý snapshot: " + e.getMessage());
                            }
                        }
                        
                        if (hasTransactions) {
                            // Cập nhật UI với tổng thu/chi
                            updateIncomesAndExpenses(totalValues[0], totalValues[1]);
                            displayTransactions();
                        } else {
                            // Hiển thị trạng thái không có dữ liệu
                            showEmptyState();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Lỗi xử lý dữ liệu giao dịch: " + e.getMessage());
                        showEmptyState();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Lỗi tải giao dịch: " + databaseError.getMessage());
                    showEmptyState();
                }
            });
    }
    
    // Hiển thị trạng thái khi không có dữ liệu
    private void showEmptyState() {
        Log.d(TAG, "Hiển thị trạng thái không có dữ liệu");
        
        // Ẩn recyclerview và hiển thị giao diện trống
        RecyclerView recyclerView = findViewById(R.id.transactionsRecyclerView);
        TextView emptyText = findViewById(R.id.emptyTransactionsText);
        
        if (recyclerView != null) {
            recyclerView.setVisibility(View.GONE);
        }
        
        if (emptyText != null) {
            emptyText.setVisibility(View.VISIBLE);
        }
        
        // Cập nhật tổng thu/chi về 0
        updateIncomesAndExpenses(0, 0);
    }
    
    // Cập nhật UI với tổng thu và chi
    private void updateIncomesAndExpenses(double income, double expense) {
        Log.d(TAG, "Cập nhật tổng thu: " + income + ", tổng chi: " + expense);
        
        // Cập nhật vào TextView tương ứng
        if (incomeText != null) {
            incomeText.setText(com.example.qltccn.utils.CurrencyUtils.formatVND(income));
        }
        
        if (expenseText != null) {
            expenseText.setText("-" + com.example.qltccn.utils.CurrencyUtils.formatVND(expense));
        }
    }
    
    private void displayTransactions() {
        // Áp dụng bộ lọc hiện tại
        applyFilter();
    }
    
    /**
     * Chuyển đổi bộ lọc giao dịch
     * @param filter Loại filter cần áp dụng ("all", "income", "expense")
     */
    private void toggleFilter(String filter) {
        // Nếu đang chọn filter hiện tại, chuyển về "all", ngược lại sử dụng filter được chọn
        if (filter.equals(currentFilter)) {
            currentFilter = "all";
        } else {
            currentFilter = filter;
        }
        
        Log.d(TAG, "Đã chọn filter: " + currentFilter);
        
        // Cập nhật giao diện và áp dụng filter
        updateFilterUI();
        applyFilter();
    }
    
    /**
     * Cập nhật giao diện dựa trên filter đang được chọn
     */
    private void updateFilterUI() {
        View incomeLayout = findViewById(R.id.incomeLayout);
        View expenseLayout = findViewById(R.id.expenseLayout);
        
        // Lấy các TextView để thay đổi màu chữ
        TextView incomeTitleText = incomeLayout.findViewById(R.id.incomeTitleText);
        TextView incomeAmountText = findViewById(R.id.incomeText);
        
        TextView expenseTitleText = expenseLayout.findViewById(R.id.expenseTitleText);
        TextView expenseAmountText = findViewById(R.id.expenseText);
        
        // Reset trạng thái highlight và màu chữ
        if (incomeLayout != null) {
            incomeLayout.setBackgroundResource(R.drawable.shape_rounded_white);
            
            // Reset màu chữ về màu mặc định
            if (incomeTitleText != null) {
                incomeTitleText.setTextColor(getResources().getColor(R.color.black));
            }
            if (incomeAmountText != null) {
                incomeAmountText.setTextColor(getResources().getColor(R.color.black));
            }
        }
        
        if (expenseLayout != null) {
            expenseLayout.setBackgroundResource(R.drawable.shape_rounded_white);
            
            // Reset màu chữ về màu mặc định
            if (expenseTitleText != null) {
                expenseTitleText.setTextColor(getResources().getColor(R.color.black));
            }
            if (expenseAmountText != null) {
                expenseAmountText.setTextColor(getResources().getColor(R.color.blue_500));
            }
        }
        
        // Highlight filter được chọn
        if ("income".equals(currentFilter) && incomeLayout != null) {
            incomeLayout.setBackgroundResource(R.drawable.selected_filter_background);
            
            // Đổi màu chữ thành trắng
            if (incomeTitleText != null) {
                incomeTitleText.setTextColor(getResources().getColor(android.R.color.white));
            }
            if (incomeAmountText != null) {
                incomeAmountText.setTextColor(getResources().getColor(android.R.color.white));
            }
        } else if ("expense".equals(currentFilter) && expenseLayout != null) {
            expenseLayout.setBackgroundResource(R.drawable.selected_filter_background);
            
            // Đổi màu chữ thành trắng
            if (expenseTitleText != null) {
                expenseTitleText.setTextColor(getResources().getColor(android.R.color.white));
            }
            if (expenseAmountText != null) {
                expenseAmountText.setTextColor(getResources().getColor(android.R.color.white));
            }
        }
        
        // Cập nhật tiêu đề
        if ("income".equals(currentFilter)) {
            titleText.setText("Khoản thu");
        } else if ("expense".equals(currentFilter)) {
            titleText.setText("Khoản chi");
        } else {
            titleText.setText("Tất cả giao dịch");
        }
    }
    
    /**
     * Áp dụng bộ lọc hiện tại vào danh sách giao dịch
     */
    private void applyFilter() {
        if (transactionList == null) {
            return;
        }
        
        filteredTransactionList.clear();
        
        // Lọc danh sách giao dịch dựa vào currentFilter
        if ("all".equals(currentFilter)) {
            filteredTransactionList.addAll(transactionList);
        } else {
            for (Transaction transaction : transactionList) {
                if (currentFilter.equals(transaction.getType())) {
                    filteredTransactionList.add(transaction);
                }
            }
        }
        
        // Cập nhật hiển thị
        displayFilteredTransactions();
    }
    
    /**
     * Hiển thị danh sách giao dịch đã được lọc
     */
    private void displayFilteredTransactions() {
        try {
            Log.d(TAG, "Hiển thị " + filteredTransactionList.size() + 
                  " giao dịch đã lọc (filter: " + currentFilter + ")");
            
            // Sắp xếp danh sách giao dịch theo thời gian gần đây nhất trước
            Collections.sort(filteredTransactionList, (t1, t2) -> 
                Long.compare(t2.getDate(), t1.getDate()));
            
            // Lấy tham chiếu đến RecyclerView
            RecyclerView recyclerView = findViewById(R.id.transactionsRecyclerView);
            TextView emptyText = findViewById(R.id.emptyTransactionsText);
            
            if (recyclerView == null) {
                Log.e(TAG, "Không tìm thấy transactionsRecyclerView");
                return;
            }
            
            if (filteredTransactionList.isEmpty()) {
                // Hiển thị trạng thái trống
                if (emptyText != null) {
                    recyclerView.setVisibility(View.GONE);
                    emptyText.setVisibility(View.VISIBLE);
                    
                    // Cập nhật nội dung thông báo trống dựa trên bộ lọc
                    if ("income".equals(currentFilter)) {
                        emptyText.setText("Không có khoản thu nào");
                    } else if ("expense".equals(currentFilter)) {
                        emptyText.setText("Không có khoản chi nào");
                    } else {
                        emptyText.setText("Không có giao dịch nào");
                    }
                }
                return;
            } else {
                // Hiển thị danh sách giao dịch
                if (emptyText != null) {
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyText.setVisibility(View.GONE);
                }
            }
            
            // Tạo hoặc cập nhật adapter
            com.example.qltccn.adapters.TransactionAdapter adapter;
            if (recyclerView.getAdapter() == null) {
                adapter = new com.example.qltccn.adapters.TransactionAdapter(this, filteredTransactionList);
                recyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
                recyclerView.setAdapter(adapter);
                
                // Thêm bộ lắng nghe sự kiện click vào mỗi giao dịch
                adapter.setOnTransactionClickListener((transaction, position) -> {
                    openTransactionDetail(transaction);
                });
            } else {
                adapter = (com.example.qltccn.adapters.TransactionAdapter) recyclerView.getAdapter();
                adapter.updateData(filteredTransactionList);
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi hiển thị giao dịch: " + e.getMessage(), e);
        }
    }
    
    // Mở chi tiết giao dịch
    private void openTransactionDetail(Transaction transaction) {
        try {
            // Sử dụng dialog tùy chỉnh thay vì AlertDialog
            android.app.Dialog dialog = new android.app.Dialog(this);
            dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_transaction_detail);
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            
            // Lấy tham chiếu đến các view trong dialog
            TextView tvCategory = dialog.findViewById(R.id.tvDetailCategory);
            TextView tvAmount = dialog.findViewById(R.id.tvDetailAmount);
            TextView tvDate = dialog.findViewById(R.id.tvDetailDate);
            TextView tvDescription = dialog.findViewById(R.id.tvDetailDescription);
            TextView tvType = dialog.findViewById(R.id.tvDetailType);
            Button btnClose = dialog.findViewById(R.id.btnClose);
            Button btnEdit = dialog.findViewById(R.id.btnEdit);
            
            // Thiết lập dữ liệu
            if (tvCategory != null) tvCategory.setText("Danh mục: " + transaction.getCategory());
            
            if (tvAmount != null) {
                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
                tvAmount.setText("Số tiền: " + currencyFormat.format(transaction.getAmount()));
            }
            
            if (tvDate != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                tvDate.setText("Ngày: " + dateFormat.format(new Date(transaction.getDate())));
            }
            
            if (tvDescription != null) {
                String description = transaction.getDescription();
                if (description != null && !description.isEmpty()) {
                    tvDescription.setText("Mô tả: " + description);
                    tvDescription.setVisibility(View.VISIBLE);
                } else {
                    tvDescription.setVisibility(View.GONE);
                }
            }
            
            if (tvType != null) {
                String typeText = "income".equals(transaction.getType()) ? "Thu nhập" : "Chi tiêu";
                tvType.setText("Loại: " + typeText);
            }
            
            // Xử lý nút Đóng
            if (btnClose != null) {
                btnClose.setOnClickListener(v -> dialog.dismiss());
            }
            
            // Xử lý nút Chỉnh sửa
            if (btnEdit != null) {
                btnEdit.setOnClickListener(v -> {
                   /* // Mở activity chỉnh sửa giao dịch
                    Intent intent = new Intent(TranActivity.this, TransactionEditActivity.class);
                    intent.putExtra("TRANSACTION_ID", transaction.getId());
                    startActivity(intent);
                    dialog.dismiss();*/
                });
            }

            // Hiển thị dialog
            dialog.show();
        } catch (Exception e) {
            Log.e(TAG, "Lỗi mở chi tiết giao dịch: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi mở chi tiết giao dịch", Toast.LENGTH_SHORT).show();
        }
    }

    // Thêm phương thức chuyển hướng tới CategoryListActivity
    private void navigateToCategory() {
        try {
            Log.d(TAG, "Chuyển hướng tới màn hình danh mục");
            Intent intent = new Intent(this, CategoryActivity.class);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi chuyển đến màn hình danh mục: " + e.getMessage(), e);
            Toast.makeText(this, "Không thể mở màn hình danh mục", Toast.LENGTH_SHORT).show();
        }
    }

    // Phương thức điều hướng
    private void navigateToHome() {
        try {
            Log.d(TAG, "Chuyển hướng tới màn hình chính");
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi chuyển đến màn hình chính: " + e.getMessage(), e);
            Toast.makeText(this, "Không thể mở màn hình chính", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void navigateToChart() {
        try {
            Log.d(TAG, "Chuyển hướng tới màn hình phân tích");
            Intent intent = new Intent(this, AnalysisActivity.class);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi chuyển đến màn hình phân tích: " + e.getMessage(), e);
            Toast.makeText(this, "Không thể mở màn hình phân tích", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void navigateToProfile() {
        try {
            Log.d(TAG, "Chuyển hướng tới màn hình cá nhân");
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi chuyển đến màn hình cá nhân: " + e.getMessage(), e);
            Toast.makeText(this, "Không thể mở màn hình cá nhân", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Xử lý intent nhận được
     */
    private void handleIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            // Xử lý intent từ màn hình danh mục
            boolean fromCategoryScreen = intent.getBooleanExtra("FROM_CATEGORY_SCREEN", false);
            String action = intent.getStringExtra("ACTION");
            
            if (fromCategoryScreen && "ADD_EXPENSE".equals(action)) {
                // Lấy thông tin danh mục
                String categoryId = intent.getStringExtra("categoryId");
                String categoryName = intent.getStringExtra("categoryName");
                String iconName = intent.getStringExtra("iconName");
                String categoryColor = intent.getStringExtra("categoryColor");
                
                Log.d(TAG, "Nhận intent thêm chi tiêu cho danh mục: " + categoryName + ", ID: " + categoryId);
                
                // Chuyển đến màn hình thêm chi tiêu
                openAddExpenseActivity(categoryId, categoryName, iconName, categoryColor);
            }
        }
    }
    
    /**
     * Mở màn hình thêm chi tiêu
     */
    private void openAddExpenseActivity(String categoryId, String categoryName, String iconName, String categoryColor) {
        try {
            // Mở CategoryAddActivity để thêm chi tiêu
            Intent intent = new Intent(this, CategoryAddActivity.class);
            
            // Truyền thông tin danh mục
            intent.putExtra("CATEGORY_ID", categoryId);
            intent.putExtra("CATEGORY_NAME", categoryName);
            intent.putExtra("categoryId", categoryId);
            intent.putExtra("categoryName", categoryName);
            intent.putExtra("categoryType", "expense");
            intent.putExtra("iconName", iconName);
            intent.putExtra("categoryColor", categoryColor);
            
            // Chỉ định hành động là thêm chi tiêu
            intent.putExtra("ACTION", "ADD_EXPENSE");
            
            // Khởi chạy Activity
            startActivity(intent);
            
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi mở CategoryAddActivity: " + e.getMessage());
            Toast.makeText(this, "Không thể mở màn hình thêm chi tiêu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Thêm phương thức chuyển đến màn hình thông báo
    private void navigateToNotification() {
        try {
            Intent intent = new Intent(this, NotiActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi mở màn hình thông báo: " + e.getMessage(), e);
            Toast.makeText(this, "Không thể mở thông báo", Toast.LENGTH_SHORT).show();
        }
    }
}
