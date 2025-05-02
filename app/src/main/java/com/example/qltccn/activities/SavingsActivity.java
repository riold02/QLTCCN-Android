package com.example.qltccn.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.qltccn.R;
import com.example.qltccn.models.SavingsGoal;
import com.example.qltccn.models.Transaction;
import com.example.qltccn.models.User;
import com.example.qltccn.utils.CurrencyUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SavingsActivity extends AppCompatActivity implements View.OnClickListener {

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String currentUserId;

    // UI Elements
    private TextView tvTotalBalance, tvTotalSaving, tvProgressPercent, tvProgressTotal;
    private ProgressBar savingProgressBar;
    private ImageView btnBack, btnNotification;
    private Button btnAddMoreSavingGoal;
    
    // Savings Goal Elements
    private LinearLayout goalTravel, goalHouse, goalCar, goalWedding;
    private TextView tvTravelTitle, tvHouseTitle, tvCarTitle, tvWeddingTitle;
    private TextView tvTravelAmount, tvHouseAmount, tvCarAmount, tvWeddingAmount;
    private TextView tvTravelProgress, tvHouseProgress, tvCarProgress, tvWeddingProgress;
    
    // Footer Navigation
    private ImageView iconHome, iconAnalysis, iconTrans, iconCategory, iconUser;
    
    // Data
    private List<SavingsGoal> savingsGoals = new ArrayList<>();
    private double totalBalance = 0.0;
    private double totalSaving = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_savings);
        
        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        } else {
            // Nếu chưa đăng nhập, chuyển đến màn hình đăng nhập
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        
        // Initialize UI Elements
        initUI();
        setClickListeners();
        
        // Load user data
        loadUserData();
        
        // Load savings goals
        loadSavingsGoals();
    }

    private void initUI() {
        // Header
        btnBack = findViewById(R.id.btnBack);
        btnNotification = findViewById(R.id.btnNotification);
        
        // Balance Info
        tvTotalBalance = findViewById(R.id.tvTotalBalance);
        tvTotalSaving = findViewById(R.id.tvTotalExpense);
        
        // Progress
        tvProgressPercent = findViewById(R.id.tvProgressPercent);
        tvProgressTotal = findViewById(R.id.tvProgressTotal);
        savingProgressBar = findViewById(R.id.expenseProgressBar);
        
        // Goals Grid
        goalTravel = findViewById(R.id.goalTravel);
        goalHouse = findViewById(R.id.goalHouse);
        goalCar = findViewById(R.id.goalCar);
        goalWedding = findViewById(R.id.goalWedding);
        
        tvTravelTitle = findViewById(R.id.tvTravelTitle);
        tvHouseTitle = findViewById(R.id.tvHouseTitle);
        tvCarTitle = findViewById(R.id.tvCarTitle);
        tvWeddingTitle = findViewById(R.id.tvWeddingTitle);
        
        tvTravelAmount = findViewById(R.id.tvTravelAmount);
        tvHouseAmount = findViewById(R.id.tvHouseAmount);
        tvCarAmount = findViewById(R.id.tvCarAmount);
        tvWeddingAmount = findViewById(R.id.tvWeddingAmount);
        
        tvTravelProgress = findViewById(R.id.tvTravelProgress);
        tvHouseProgress = findViewById(R.id.tvHouseProgress);
        tvCarProgress = findViewById(R.id.tvCarProgress);
        tvWeddingProgress = findViewById(R.id.tvWeddingProgress);
        
        // Button
        btnAddMoreSavingGoal = findViewById(R.id.btnAddMoreSavingGoal);
        
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
        btnAddMoreSavingGoal.setOnClickListener(this);
        
        // Goals click listeners
        goalTravel.setOnClickListener(this);
        goalHouse.setOnClickListener(this);
        goalCar.setOnClickListener(this);
        goalWedding.setOnClickListener(this);
        
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

            } catch (Exception e) {
                Log.e("SavingsActivity", "Lỗi khi tạo rung: " + e.getMessage());
            }
        } else if (id == R.id.btnAddMoreSavingGoal) {
            // Tạo 4 mục tiêu mặc định nếu chưa có
            createDefaultSavingsGoals();
        } else if (id == R.id.goalTravel) {
            openSavingsGoalDetail("travel");
        } else if (id == R.id.goalHouse) {
            openSavingsGoalDetail("house");
        } else if (id == R.id.goalCar) {
            openSavingsGoalDetail("car");
        } else if (id == R.id.goalWedding) {
            openSavingsGoalDetail("wedding");
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

    private void openSavingsGoalDetail(String categoryType) {
        // Tìm mục tiêu tiết kiệm tương ứng
        for (SavingsGoal goal : savingsGoals) {
            if (categoryType.equals(goal.getCategoryType())) {
                Intent intent = new Intent(this, SavingsGoalDetailActivity.class);
                intent.putExtra("GOAL_ID", goal.getId());
                startActivity(intent);
                return;
            }
        }
        
        // Nếu chưa có mục tiêu, tạo mục tiêu mới tương ứng với loại đã chọn
        String title = "";
        String description = "";
        double targetAmount = 0;
        
        switch (categoryType) {
            case "travel":
                title = "Du lịch";
                description = "Tiết kiệm cho chuyến du lịch";
                targetAmount = 10000000;
                break;
            case "house":
                title = "Nhà mới";
                description = "Tiết kiệm cho căn nhà mới";
                targetAmount = 500000000;
                break;
            case "car":
                title = "Xe hơi";
                description = "Tiết kiệm để mua xe hơi";
                targetAmount = 300000000;
                break;
            case "wedding":
                title = "Đám cưới";
                description = "Tiết kiệm cho đám cưới";
                targetAmount = 100000000;
                break;
        }
        
        Toast.makeText(this, "Đang tạo mục tiêu " + title + "...", Toast.LENGTH_SHORT).show();
        
        // Tạo mục tiêu mới
        SavingsGoal newGoal = new SavingsGoal(
                currentUserId,
                title,
                description,
                targetAmount,
                getIconNameForCategory(categoryType),
                categoryType
        );
        
        // Lưu vào Firestore và mở chi tiết
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("savingsGoals").add(newGoal.toMap())
            .addOnSuccessListener(documentReference -> {
                // Cập nhật ID cho savingsGoal
                String goalId = documentReference.getId();
                Map<String, Object> update = new HashMap<>();
                update.put("id", goalId);
                documentReference.update(update);
                
                // Mở chi tiết mục tiêu
                Intent intent = new Intent(this, SavingsGoalDetailActivity.class);
                intent.putExtra("GOAL_ID", goalId);
                startActivity(intent);
                
                // Tải lại danh sách mục tiêu
                loadSavingsGoals();
            })
            .addOnFailureListener(e -> {
                if (e.getMessage() != null && e.getMessage().contains("PERMISSION_DENIED")) {
                    Toast.makeText(SavingsActivity.this, 
                            "Không thể tạo mục tiêu do thiếu quyền. Vui lòng cập nhật quy tắc bảo mật Firebase.", 
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(SavingsActivity.this, "Lỗi khi tạo mục tiêu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void loadUserData() {
        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            updateUIWithUserData(user);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SavingsActivity.this, "Lỗi khi tải dữ liệu người dùng", Toast.LENGTH_SHORT).show();
                });
                
        // Lấy tổng tiết kiệm
        calculateTotalSaving();
    }

    private void calculateTotalSaving() {
        double total = 0;
        
        for (SavingsGoal goal : savingsGoals) {
            total += goal.getCurrentAmount();
        }
        
        totalSaving = total;
        tvTotalSaving.setText(CurrencyUtils.formatAmount(totalSaving));
        updateProgressBar();
    }

    private void updateUIWithUserData(User user) {
        totalBalance = user.getBalance();
        tvTotalBalance.setText(CurrencyUtils.formatAmount(totalBalance));
        tvProgressTotal.setText(CurrencyUtils.formatAmount(totalBalance));
        updateProgressBar();
    }

    private void updateProgressBar() {
        if (totalBalance > 0) {
            int percent = (int) ((totalSaving / totalBalance) * 100);
            if (percent > 100) percent = 100;
            
            tvProgressPercent.setText(percent + "%");
            savingProgressBar.setProgress(percent);
        }
    }

    private void loadSavingsGoals() {
        // Hiển thị trạng thái đang tải
        Toast.makeText(this, "Đang tải dữ liệu mục tiêu...", Toast.LENGTH_SHORT).show();
        
        try {
            // Cố gắng tải dữ liệu không sử dụng orderBy() để tránh lỗi index
            db.collection("savingsGoals")
                    .whereEqualTo("userId", currentUserId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        savingsGoals.clear();
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            SavingsGoal goal = doc.toObject(SavingsGoal.class);
                            if (goal != null) {
                                goal.setId(doc.getId());
                                savingsGoals.add(goal);
                            }
                        }
                        
                        // Nếu không có mục tiêu nào, tạo mục tiêu mặc định
                        if (savingsGoals.isEmpty()) {
                            createDefaultSavingsGoals();
                            return;
                        }
                        
                        updateSavingsGoalsUI();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("SavingsActivity", "Lỗi khi tải dữ liệu mục tiêu tiết kiệm: " + e.getMessage());
                        
                        // Kiểm tra loại lỗi
                        String errorMessage = e.getMessage();
                        if (errorMessage != null) {
                            if (errorMessage.contains("PERMISSION_DENIED")) {
                                handlePermissionDeniedError();
                            } else if (errorMessage.contains("FAILED_PRECONDITION") || errorMessage.contains("requires an index")) {
                                // Thông báo về lỗi chỉ mục
                                Toast.makeText(SavingsActivity.this, 
                                        "Cần tạo chỉ mục cho truy vấn. Truy cập URL được ghi trong log để tạo chỉ mục.", 
                                        Toast.LENGTH_LONG).show();
                                
                                Toast.makeText(SavingsActivity.this, 
                                        "Vui lòng đăng nhập vào Firebase Console và mở URL hiển thị trong logcat.", 
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(SavingsActivity.this, "Lỗi khi tải dữ liệu: " + errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(SavingsActivity.this, "Lỗi khi tải dữ liệu mục tiêu", Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception e) {
            Log.e("SavingsActivity", "Exception khi tải dữ liệu: " + e.getMessage());
            Toast.makeText(this, "Lỗi ứng dụng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateSavingsGoalsUI() {
        // Cập nhật UI cho mỗi loại mục tiêu
        updateGoalUI("travel", tvTravelAmount, tvTravelProgress);
        updateGoalUI("house", tvHouseAmount, tvHouseProgress);
        updateGoalUI("car", tvCarAmount, tvCarProgress);
        updateGoalUI("wedding", tvWeddingAmount, tvWeddingProgress);
        
        // Cập nhật tổng tiết kiệm
        calculateTotalSaving();
    }

    private void updateGoalUI(String categoryType, TextView amountView, TextView progressView) {
        for (SavingsGoal goal : savingsGoals) {
            if (categoryType.equals(goal.getCategoryType())) {
                amountView.setText(CurrencyUtils.formatAmount(goal.getCurrentAmount()));
                int progress = (int) goal.getProgressPercentage();
                progressView.setText(progress + "%");
                return;
            }
        }
        
        // Nếu không tìm thấy mục tiêu
        amountView.setText(CurrencyUtils.formatAmount(0));
        progressView.setText("0%");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Cập nhật dữ liệu khi quay lại màn hình
        loadUserData();
        loadSavingsGoals();
    }

    // Thay thế phương thức tạo mục tiêu bằng phương thức tạo 4 mục tiêu mặc định
    private void createDefaultSavingsGoals() {
        // Hiển thị toast thông báo đang tạo mục tiêu
        Toast.makeText(this, "Đang tạo mục tiêu tiết kiệm mặc định...", Toast.LENGTH_SHORT).show();
        
        // Kiểm tra các mục tiêu đã tồn tại chưa
        checkAndCreateGoal("travel", "Du lịch", "Tiết kiệm cho chuyến du lịch", 10000000);
        checkAndCreateGoal("house", "Nhà mới", "Tiết kiệm cho căn nhà mới", 500000000);
        checkAndCreateGoal("car", "Xe hơi", "Tiết kiệm để mua xe hơi", 300000000);
        checkAndCreateGoal("wedding", "Đám cưới", "Tiết kiệm cho đám cưới", 100000000);
    }
    
    private void checkAndCreateGoal(String categoryType, String title, String description, double targetAmount) {
        // Kiểm tra mục tiêu đã tồn tại chưa
        boolean goalExists = false;
        for (SavingsGoal goal : savingsGoals) {
            if (categoryType.equals(goal.getCategoryType())) {
                goalExists = true;
                break;
            }
        }
        
        // Nếu chưa tồn tại, tạo mới
        if (!goalExists) {
            SavingsGoal newGoal = new SavingsGoal(
                    currentUserId,
                    title,
                    description,
                    targetAmount,
                    getIconNameForCategory(categoryType),
                    categoryType
            );
            
            // Lưu vào Firestore
            saveDefaultSavingsGoal(newGoal);
        }
    }
    
    private String getIconNameForCategory(String categoryType) {
        switch (categoryType) {
            case "travel": return "ic_travel";
            case "house": return "ic_newhome";
            case "car": return "ic_car";
            case "wedding": return "ic_wedding";
            default: return "ic_expense";
        }
    }
    
    private void saveDefaultSavingsGoal(SavingsGoal savingsGoal) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // Lưu vào collection savingsGoals
        db.collection("savingsGoals").add(savingsGoal.toMap())
            .addOnSuccessListener(documentReference -> {
                // Cập nhật ID cho savingsGoal
                String goalId = documentReference.getId();
                Map<String, Object> update = new HashMap<>();
                update.put("id", goalId);
                documentReference.update(update);
                
                // Tải lại danh sách mục tiêu
                loadSavingsGoals();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(SavingsActivity.this, "Lỗi khi tạo mục tiêu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    // Xử lý lỗi quyền truy cập Firestore
    private void handlePermissionDeniedError() {
        Log.e("SavingsActivity", "Lỗi Permission Denied: Không có quyền truy cập Firestore");
        
        // Hiển thị thông báo lỗi cho người dùng
        Toast.makeText(this, 
                "Không thể tải dữ liệu do thiếu quyền truy cập. Vui lòng cập nhật quy tắc bảo mật Firebase.", 
                Toast.LENGTH_LONG).show();
                
        // Hướng dẫn cách cập nhật quy tắc bảo mật
        Toast.makeText(this, 
                "Vào Firebase Console > Firestore Database > Rules để cập nhật quyền truy cập.", 
                Toast.LENGTH_LONG).show();
    }
} 