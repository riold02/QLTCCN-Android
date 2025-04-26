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
import com.example.qltccn.adapters.SavingsTransactionAdapter;
import com.example.qltccn.models.SavingsGoal;
import com.example.qltccn.models.SavingsTransaction;
import com.example.qltccn.utils.CurrencyUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class SavingsGoalDetailActivity extends AppCompatActivity implements View.OnClickListener {

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String currentUserId;
    private String goalId;

    // UI Elements
    private ImageView btnBack, btnNotification;
    private TextView tvTitle, tvCategoryName, tvCategoryDescription;
    private TextView tvGoalAmount, tvAmountSaved, tvProgressPercent, tvExpenseStatus, tvTotalAmount;
    private ProgressBar progressBar;
    private Button btnAddSavings, btnSetGoal;
    private RecyclerView recyclerViewTransactions;
    
    // Footer Navigation
    private ImageView iconHome, iconAnalysis, iconTrans, iconCategory, iconUser;
    
    // Data
    private SavingsGoal savingsGoal;
    private List<SavingsTransaction> transactions = new ArrayList<>();
    private SavingsTransactionAdapter transactionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_savings_goal_detail);
        
        // Lấy goalId từ intent
        goalId = getIntent().getStringExtra("GOAL_ID");
        if (goalId == null) {
            Toast.makeText(this, "Không tìm thấy mục tiêu tiết kiệm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        
        // Initialize UI Elements
        initUI();
        setClickListeners();
        
        // Set up RecyclerView
        recyclerViewTransactions.setLayoutManager(new LinearLayoutManager(this));
        transactionAdapter = new SavingsTransactionAdapter(this, transactions);
        recyclerViewTransactions.setAdapter(transactionAdapter);
        
        // Load savings goal
        loadSavingsGoal();
        
        // Load transactions
        loadTransactions();
    }

    private void initUI() {
        // Header
        btnBack = findViewById(R.id.btnBack);
        btnNotification = findViewById(R.id.btnNotification);
        tvTitle = findViewById(R.id.tvTitle);
        
        // Card Overview
        tvCategoryName = findViewById(R.id.tvCategoryName);
        tvCategoryDescription = findViewById(R.id.tvCategoryDescription);
        tvGoalAmount = findViewById(R.id.tvGoalAmount);
        tvAmountSaved = findViewById(R.id.tvAmountSaved);
        tvProgressPercent = findViewById(R.id.tvProgressPercent);
        tvExpenseStatus = findViewById(R.id.tvExpenseStatus);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        progressBar = findViewById(R.id.progressBar);
        
        // RecyclerView
        recyclerViewTransactions = findViewById(R.id.recyclerViewTransactions);
        
        // Buttons
        btnAddSavings = findViewById(R.id.btnAddSavings);
        btnSetGoal = findViewById(R.id.btnSetGoal);
        
        // Footer
        iconHome = findViewById(R.id.iconHome);
        iconAnalysis = findViewById(R.id.iconAnalysis);
        iconTrans = findViewById(R.id.iconTrans);
        iconCategory = findViewById(R.id.iconCategory);
        iconUser = findViewById(R.id.iconUser);
    }

    private void setClickListeners() {
        btnBack.setOnClickListener(this);
        btnNotification.setOnClickListener(this);
        btnAddSavings.setOnClickListener(this);
        btnSetGoal.setOnClickListener(this);
        
        // Footer navigation
        iconHome.setOnClickListener(this);
        iconAnalysis.setOnClickListener(this);
        iconTrans.setOnClickListener(this);
        iconCategory.setOnClickListener(this);
        iconUser.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        
        if (id == R.id.btnBack) {
            onBackPressed();
        } else if (id == R.id.btnNotification) {
            // Hiển thị thông báo tính năng đang phát triển thay vì mở NotiActivity
            Toast.makeText(this, "Tính năng đang phát triển", Toast.LENGTH_SHORT).show();
            
            // Có thể bổ sung thêm rung nhẹ để tăng trải nghiệm người dùng
            try {
                android.os.Vibrator vibrator = (android.os.Vibrator) getSystemService(android.content.Context.VIBRATOR_SERVICE);
                if (vibrator != null && vibrator.hasVibrator()) {
                    // Rung nhẹ 100ms
                    vibrator.vibrate(100);
                }
            } catch (Exception e) {
                Log.e("SavingsGoalDetail", "Lỗi khi tạo rung: " + e.getMessage());
            }
        } else if (id == R.id.btnAddSavings) {
            // Mở SavingsAddActivity để thêm giao dịch tiết kiệm
            Intent intent = new Intent(this, SavingsAddActivity.class);
            // Thêm thông tin về mục tiêu tiết kiệm
            intent.putExtra("GOAL_ID", goalId);
            startActivityForResult(intent, 100);
        } else if (id == R.id.btnSetGoal) {
            // Mở màn hình cập nhật mục tiêu
            // TODO: Thêm activity cập nhật mục tiêu
            Toast.makeText(this, "Tính năng đang phát triển", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.iconHome) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        } else if (id == R.id.iconAnalysis) {
            startActivity(new Intent(this, AnalysisActivity.class));
            finish();
        } else if (id == R.id.iconTrans) {
            startActivity(new Intent(this, TranActivity.class));
            finish();
        } else if (id == R.id.iconCategory) {
            startActivity(new Intent(this, CategoryActivity.class));
            finish();
        } else if (id == R.id.iconUser) {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
        }
    }

    private void loadSavingsGoal() {
        db.collection("savingsGoals").document(goalId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        savingsGoal = documentSnapshot.toObject(SavingsGoal.class);
                        if (savingsGoal != null) {
                            savingsGoal.setId(documentSnapshot.getId());
                            updateUIWithSavingsGoal();
                        }
                    } else {
                        Toast.makeText(this, "Không tìm thấy mục tiêu tiết kiệm", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("SavingsGoalDetail", "Lỗi khi tải dữ liệu mục tiêu: " + e.getMessage());
                    
                    if (e.getMessage() != null && e.getMessage().contains("PERMISSION_DENIED")) {
                        Toast.makeText(this, "Không có quyền truy cập dữ liệu. Vui lòng cập nhật quy tắc bảo mật.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Lỗi khi tải dữ liệu mục tiêu tiết kiệm", Toast.LENGTH_SHORT).show();
                    }
                    finish();
                });
    }

    private void updateUIWithSavingsGoal() {
        // Cập nhật tiêu đề và thông tin mục tiêu
        tvTitle.setText(savingsGoal.getTitle());
        tvCategoryName.setText(savingsGoal.getTitle());
        tvCategoryDescription.setText(savingsGoal.getDescription());
        
        // Cập nhật số tiền
        tvGoalAmount.setText(CurrencyUtils.formatAmount(savingsGoal.getTargetAmount()));
        tvAmountSaved.setText(CurrencyUtils.formatAmount(savingsGoal.getCurrentAmount()));
        tvTotalAmount.setText(CurrencyUtils.formatAmount(savingsGoal.getTargetAmount()));
        
        // Cập nhật tiến trình
        int progress = (int) savingsGoal.getProgressPercentage();
        tvProgressPercent.setText(progress + "%");
        progressBar.setProgress(progress);
        
        // Cập nhật thông báo trạng thái
        tvExpenseStatus.setText(progress + "% Của mục tiêu, " + 
                (progress < 50 ? "Cần cố gắng hơn." : "Còn ít nữa thôi!"));
        
        // Thiết lập icon theo loại mục tiêu
        ImageView ivCategoryIcon = findViewById(R.id.ivCategoryIcon);
        setGoalIcon(ivCategoryIcon, savingsGoal.getCategoryType());
    }

    private void loadTransactions() {
        try {
            // Truy vấn không sử dụng orderBy để tránh lỗi index
            db.collection("savingsTransactions")
                    .whereEqualTo("goalId", goalId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        transactions.clear();
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            SavingsTransaction transaction = doc.toObject(SavingsTransaction.class);
                            if (transaction != null) {
                                transaction.setId(doc.getId());
                                transactions.add(transaction);
                            }
                        }
                        
                        // Sắp xếp danh sách giao dịch theo ngày giảm dần
                        transactions.sort((t1, t2) -> Long.compare(t2.getDate(), t1.getDate()));
                        
                        transactionAdapter.notifyDataSetChanged();
                        
                        // Hiển thị thông báo nếu không có giao dịch
                        View emptyView = findViewById(R.id.emptyTransactionsView);
                        if (transactions.isEmpty() && emptyView != null) {
                            emptyView.setVisibility(View.VISIBLE);
                            recyclerViewTransactions.setVisibility(View.GONE);
                        } else {
                            if (emptyView != null) emptyView.setVisibility(View.GONE);
                            recyclerViewTransactions.setVisibility(View.VISIBLE);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("SavingsGoalDetail", "Lỗi khi tải dữ liệu giao dịch: " + e.getMessage());
                        
                        String errorMessage = e.getMessage();
                        if (errorMessage != null) {
                            if (errorMessage.contains("PERMISSION_DENIED")) {
                                Toast.makeText(this, "Không có quyền truy cập giao dịch. Vui lòng cập nhật quy tắc bảo mật.", Toast.LENGTH_LONG).show();
                            } else if (errorMessage.contains("FAILED_PRECONDITION") || errorMessage.contains("requires an index")) {
                                Toast.makeText(this, "Cần tạo chỉ mục cho truy vấn. Vui lòng kiểm tra log.", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(this, "Lỗi khi tải dữ liệu: " + errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Lỗi khi tải dữ liệu giao dịch", Toast.LENGTH_SHORT).show();
                        }
                        
                        // Hiển thị view trống khi có lỗi
                        View emptyView = findViewById(R.id.emptyTransactionsView);
                        if (emptyView != null) {
                            emptyView.setVisibility(View.VISIBLE);
                            recyclerViewTransactions.setVisibility(View.GONE);
                        }
                    });
        } catch (Exception e) {
            Log.e("SavingsGoalDetail", "Exception khi tải giao dịch: " + e.getMessage());
            Toast.makeText(this, "Lỗi ứng dụng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Phương thức thiết lập icon cho mục tiêu dựa vào loại
    private void setGoalIcon(ImageView imageView, String categoryType) {
        switch (categoryType) {
            case "travel":
                imageView.setImageResource(R.drawable.ic_travel);
                break;
            case "house":
                imageView.setImageResource(R.drawable.ic_newhome);
                break;
            case "car":
                imageView.setImageResource(R.drawable.ic_car);
                break;
            case "wedding":
                imageView.setImageResource(R.drawable.ic_wedding);
                break;
            default:
                imageView.setImageResource(R.drawable.ic_expense);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Cập nhật dữ liệu khi quay lại màn hình
        if (goalId != null) {
            loadSavingsGoal();
            loadTransactions();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            // Cập nhật dữ liệu khi quay lại sau khi thêm giao dịch
            loadSavingsGoal();
            loadTransactions();
        }
    }
} 