package com.example.qltccn.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qltccn.R;
import com.example.qltccn.adapters.TransactionAdapter;
import com.example.qltccn.models.Transaction;
import com.example.qltccn.utils.CurrencyUtils;
import com.example.qltccn.utils.FirebaseUtils;
import com.example.qltccn.utils.UserUtils;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.CollectionReference;

import java.util.ArrayList;
import java.util.List;

public class CategoryDetailActivity extends AppCompatActivity {
    private static final String TAG = "CategoryDetailActivity";
    private static final int REFRESH_REQUEST_CODE = 1001;

    // UI components
    private ImageView btnBack;
    private TextView txtCategoryTitle;
    private TextView txtTotalBalance;
    private TextView txtTotalExpenses;
    private ProgressBar progressBar;
    private TextView lblPercentage;
    private TextView lblExpenseStatus;
    private TextView lblTotalAmount;
    private RecyclerView recyclerViewTransactions;
    private Button btnAddExpense;

    // Data
    private String categoryName;
    private double totalBalance = 0;
    private double categoryExpenses = 0;
    private List<Transaction> categoryTransactions = new ArrayList<>();
    private TransactionAdapter transactionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_detail);

        // Lấy tên danh mục từ intent
        categoryName = getIntent().getStringExtra("CATEGORY_NAME");
        if (categoryName == null || categoryName.isEmpty()) {
            Toast.makeText(this, "Không có thông tin danh mục", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupListeners();
        loadData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        txtCategoryTitle = findViewById(R.id.txtCategoryTitle);
        txtTotalBalance = findViewById(R.id.txtTotalBalance);
        txtTotalExpenses = findViewById(R.id.txtTotalExpenses);
        progressBar = findViewById(R.id.progressBar);
        lblPercentage = findViewById(R.id.lblPercentage);
        lblExpenseStatus = findViewById(R.id.lblExpenseStatus);
        lblTotalAmount = findViewById(R.id.lblTotalAmount);
        recyclerViewTransactions = findViewById(R.id.recyclerViewTransactions);
        btnAddExpense = findViewById(R.id.btnAddExpense);

        // Thiết lập tiêu đề
        txtCategoryTitle.setText(categoryName);

        // Thiết lập RecyclerView
        recyclerViewTransactions.setLayoutManager(new LinearLayoutManager(this));
        transactionAdapter = new TransactionAdapter(categoryTransactions, new TransactionAdapter.OnTransactionClickListener() {
            @Override
            public void onTransactionClick(Transaction transaction, int position) {
                showTransactionDetails(transaction);
            }
        });
        recyclerViewTransactions.setAdapter(transactionAdapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> {
            // Thiết lập kết quả trả về để màn hình trước biết cần cập nhật dữ liệu
            Intent resultIntent = new Intent();
            resultIntent.putExtra("REFRESH_CATEGORIES", true);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
        
        btnAddExpense.setOnClickListener(v -> {
            Intent intent = new Intent(CategoryDetailActivity.this, CategoryAddActivity.class);
            intent.putExtra("CATEGORY_NAME", categoryName);
            intent.putExtra("categoryName", categoryName);
            intent.putExtra("categoryType", "expense");
            
            startActivityForResult(intent, REFRESH_REQUEST_CODE);
        });
        
        if (transactionAdapter != null) {
            transactionAdapter.setOnTransactionClickListener((transaction, position) -> {
                showTransactionDetails(transaction);
            });
        }
    }

    private void loadData() {
        // Tải thông tin người dùng và số dư
        UserUtils.getCurrentUser(new UserUtils.FetchUserCallback() {
            @Override
            public void onSuccess(com.example.qltccn.models.User user) {
                if (user != null) {
                    totalBalance = user.getBalance();
                    txtTotalBalance.setText(CurrencyUtils.formatVND(totalBalance));
                    
                    // Sau khi có thông tin cơ bản, tải giao dịch của danh mục
                    loadCategoryTransactions(user.getId());
                }
            }
            
            @Override
            public void onError(String errorMessage) {
                Toast.makeText(CategoryDetailActivity.this, "Không thể tải thông tin người dùng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCategoryTransactions(String userId) {
        if (userId == null) {
            Log.e(TAG, "userId không được cung cấp khi tải giao dịch");
            updateUIWithEmptyData();
            return;
        }

        // Kiểm tra collection transactions
        try {
            // Lấy tham chiếu collection và kiểm tra null
            CollectionReference transactionsRef = FirebaseUtils.getUserTransactionsCollection();
            if (transactionsRef == null) {
                Log.e(TAG, "Không thể lấy tham chiếu đến collection transactions");
                updateUIWithEmptyData();
                return;
            }
            
            // Log để debug
            Log.d(TAG, "Đường dẫn collection giao dịch: " + transactionsRef.getPath());
            
            // Query các giao dịch thuộc danh mục này
            transactionsRef
                .whereEqualTo("userId", userId)
                .whereEqualTo("category", categoryName)
                .whereEqualTo("type", "expense") // Chỉ lấy các giao dịch chi tiêu
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    try {
                        categoryTransactions.clear();
                        categoryExpenses = 0;

                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            try {
                                // Lấy dữ liệu từ document và tạo transaction thủ công
                                String id = doc.getId();
                                String docUserId = doc.getString("userId");
                                String category = doc.getString("category");
                                double amount = 0;
                                if (doc.get("amount") instanceof Number) {
                                    amount = ((Number) doc.get("amount")).doubleValue();
                                }
                                String description = doc.getString("description");
                                String type = doc.getString("type");
                                
                                // Xử lý trường date - chuyển từ Timestamp sang long
                                long dateValue = 0;
                                Object dateObj = doc.get("date");
                                if (dateObj instanceof com.google.firebase.Timestamp) {
                                    dateValue = ((com.google.firebase.Timestamp) dateObj).toDate().getTime();
                                } else if (dateObj instanceof Number) {
                                    dateValue = ((Number) dateObj).longValue();
                                }
                                
                                // Tạo đối tượng transaction mới
                                Transaction transaction = new Transaction();
                                transaction.setId(id);
                                transaction.setUserId(docUserId);
                                transaction.setCategory(category);
                                transaction.setAmount(amount);
                                transaction.setDescription(description);
                                transaction.setType(type);
                                transaction.setDate(dateValue);
                                transaction.setNote(doc.getString("note"));
                                
                                // Chỉ thêm vào danh sách nếu dữ liệu hợp lệ
                                if (docUserId != null && category != null) {
                                    categoryTransactions.add(transaction);
                                    categoryExpenses += amount;
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Lỗi khi xử lý document: " + e.getMessage(), e);
                            }
                        }

                        // Cập nhật UI dù có giao dịch hay không
                        updateUI();
                        
                        // Cập nhật danh sách giao dịch
                        transactionAdapter.updateData(categoryTransactions);
                        
                        // Hiển thị thông báo nếu không có giao dịch
                        if (categoryTransactions.isEmpty()) {
                            Log.d(TAG, "Không có giao dịch nào cho danh mục: " + categoryName);
                        } else {
                            Log.d(TAG, "Đã tải " + categoryTransactions.size() + " giao dịch cho danh mục " + categoryName);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Lỗi khi xử lý dữ liệu giao dịch: " + e.getMessage(), e);
                        updateUIWithEmptyData();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi tải giao dịch: " + e.getMessage(), e);
                    updateUIWithEmptyData();
                });
        } catch (Exception e) {
            Log.e(TAG, "Lỗi ngoại lệ khi tải giao dịch: " + e.getMessage(), e);
            updateUIWithEmptyData();
        }
    }
    
    // Phương thức cập nhật UI khi không có dữ liệu
    private void updateUIWithEmptyData() {
        categoryTransactions.clear();
        categoryExpenses = 0;
        updateUI();
        transactionAdapter.updateData(categoryTransactions);
    }

    private void updateUI() {
        // Hiển thị tổng chi tiêu
        txtTotalExpenses.setText("-" + CurrencyUtils.formatVND(categoryExpenses));
        lblTotalAmount.setText(CurrencyUtils.formatVND(categoryExpenses));

        // Tính phần trăm chi tiêu
        int percentage = 0;
        if (totalBalance > 0) {
            percentage = (int) (categoryExpenses / totalBalance * 100);
            if (percentage > 100) percentage = 100;
        }

        // Cập nhật thanh tiến trình
        progressBar.setProgress(percentage);
        lblPercentage.setText(percentage + "%");

        // Cập nhật trạng thái chi tiêu
        String status;
        if (percentage < 30) {
            status = percentage + "% của chi tiêu, tuyệt vời.";
        } else if (percentage < 70) {
            status = percentage + "% của chi tiêu, hãy kiểm soát.";
        } else {
            status = percentage + "% của chi tiêu, cảnh báo.";
        }
        lblExpenseStatus.setText(status);
    }

    private void showTransactionDetails(Transaction transaction) {
        try {
            // Sử dụng dialog tùy chỉnh thay vì AlertDialog
            android.app.Dialog dialog = new android.app.Dialog(this);
            dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_transaction_detail);
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            
            // Gán dữ liệu cho dialog
            TextView tvCategory = dialog.findViewById(R.id.tvDetailCategory);
            TextView tvAmount = dialog.findViewById(R.id.tvDetailAmount);
            TextView tvDate = dialog.findViewById(R.id.tvDetailDate);
            TextView tvDescription = dialog.findViewById(R.id.tvDetailDescription);
            TextView tvType = dialog.findViewById(R.id.tvDetailType);
            Button btnClose = dialog.findViewById(R.id.btnClose);
            Button btnEdit = dialog.findViewById(R.id.btnEdit);
            
            if (tvCategory != null) tvCategory.setText("Danh mục: " + transaction.getCategory());
            
            if (tvAmount != null) {
                String formattedAmount = "-" + CurrencyUtils.formatVND(transaction.getAmount());
                tvAmount.setText("Số tiền: " + formattedAmount);
                tvAmount.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }
            
            if (tvDate != null) {
                java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault());
                tvDate.setText("Ngày: " + dateFormat.format(new java.util.Date(transaction.getDate())));
            }
            
            if (tvType != null) {
                tvType.setText("Loại: Chi tiêu");
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
                    // Mở activity chỉnh sửa giao dịch
                    /*
                    Intent intent = new Intent(CategoryDetailActivity.this, TransactionEditActivity.class);
                    intent.putExtra("TRANSACTION_ID", transaction.getId());
                    startActivity(intent);
                    dialog.dismiss();*/
                });
            }

            // Hiển thị dialog
            dialog.show();
        } catch (Exception e) {
            Log.e("CategoryDetailActivity", "Lỗi hiển thị chi tiết giao dịch: " + e.getMessage(), e);
            Toast.makeText(this, "Không thể hiển thị chi tiết giao dịch", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // Tải lại dữ liệu khi quay lại màn hình này
        if (FirebaseUtils.getCurrentUser() != null) {
            loadData();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REFRESH_REQUEST_CODE) {
            // Luôn đặt kết quả là cần làm mới cho activity cha (CategoryActivity)
            Intent resultIntent = new Intent();
            resultIntent.putExtra("REFRESH_CATEGORIES", true);
            setResult(RESULT_OK, resultIntent);
            
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "Nhận yêu cầu làm mới danh mục từ activity con");
                
                // Làm mới dữ liệu giao dịch và thống kê
                loadData();
                
                // Hiển thị thông báo
                Toast.makeText(this, "Dữ liệu đã được cập nhật", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    @Override
    public void onBackPressed() {
        // Thiết lập kết quả trả về để màn hình trước biết cần cập nhật dữ liệu
        Intent resultIntent = new Intent();
        resultIntent.putExtra("REFRESH_CATEGORIES", true);
        setResult(RESULT_OK, resultIntent);
        super.onBackPressed();
    }
} 