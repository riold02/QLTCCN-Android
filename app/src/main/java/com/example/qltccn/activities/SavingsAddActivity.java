package com.example.qltccn.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.qltccn.R;
import com.example.qltccn.models.SavingsGoal;
import com.example.qltccn.models.SavingsTransaction;
import com.example.qltccn.utils.CurrencyUtils;
import com.example.qltccn.utils.FirebaseUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SavingsAddActivity extends AppCompatActivity {
    private static final String TAG = "SavingsAddActivity";

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String currentUserId;

    // UI components
    private ImageView btnBack;
    private TextView txtDate;
    private TextView tvGoalTitle, tvGoalDescription, tvGoalAmount, tvCurrentAmount, tvProgress;
    private ImageView imgGoalIcon;
    private EditText edtAmount, edtTitle, edtNote;
    private RadioGroup transactionTypeGroup;
    private RadioButton radioDeposit, radioWithdraw;
    private Button btnSave;
    private RelativeLayout dateLayout;

    // Data
    private String goalId;
    private SavingsGoal savingsGoal;
    private Date selectedDate;
    private SimpleDateFormat dateFormat;

    private android.app.ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_savings_add);

        // Khởi tạo Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        } else {
            Toast.makeText(this, "Bạn cần đăng nhập để sử dụng tính năng này", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Lấy thông tin từ intent
        goalId = getIntent().getStringExtra("GOAL_ID");
        if (goalId == null) {
            Toast.makeText(this, "Không tìm thấy mục tiêu tiết kiệm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupListeners();
        
        // Tải thông tin mục tiêu tiết kiệm
        loadSavingsGoal();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        
        // Goal Info
        tvGoalTitle = findViewById(R.id.tvGoalTitle);
        tvGoalDescription = findViewById(R.id.tvGoalDescription);
        tvGoalAmount = findViewById(R.id.tvGoalAmount);
        tvCurrentAmount = findViewById(R.id.tvCurrentAmount);
        tvProgress = findViewById(R.id.tvProgress);
        imgGoalIcon = findViewById(R.id.imgGoalIcon);
        
        // Form fields
        txtDate = findViewById(R.id.txtDate);
        edtAmount = findViewById(R.id.edtAmount);
        edtTitle = findViewById(R.id.edtTitle);
        edtNote = findViewById(R.id.edtNote);
        dateLayout = findViewById(R.id.dateLayout);
        transactionTypeGroup = findViewById(R.id.transactionTypeGroup);
        radioDeposit = findViewById(R.id.radioDeposit);
        radioWithdraw = findViewById(R.id.radioWithdraw);
        btnSave = findViewById(R.id.btnSave);
        
        // Thiết lập ngày hiện tại
        selectedDate = new Date();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        txtDate.setText(dateFormat.format(selectedDate));
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        // Date picker
        dateLayout.setOnClickListener(v -> showDatePicker());
        
        // Save button
        btnSave.setOnClickListener(v -> saveTransaction());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(selectedDate);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar newDate = Calendar.getInstance();
                    newDate.set(year, month, dayOfMonth);
                    
                    newDate.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY));
                    newDate.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE));
                    
                    showTimePicker(newDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        
        datePickerDialog.show();
    }
    
    private void showTimePicker(Calendar dateCalendar) {
        Calendar calendar = Calendar.getInstance();
        
        android.app.TimePickerDialog timePickerDialog = new android.app.TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    dateCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    dateCalendar.set(Calendar.MINUTE, minute);
                    
                    selectedDate = dateCalendar.getTime();
                    txtDate.setText(dateFormat.format(selectedDate));
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );
        
        timePickerDialog.show();
    }

    private void loadSavingsGoal() {
        showLoader(true);
        
        db.collection("savingsGoals").document(goalId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                showLoader(false);
                
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
                showLoader(false);
                Log.e(TAG, "Lỗi khi tải dữ liệu mục tiêu: " + e.getMessage());
                
                if (e.getMessage() != null && e.getMessage().contains("PERMISSION_DENIED")) {
                    Toast.makeText(this, "Không có quyền truy cập dữ liệu. Vui lòng cập nhật quy tắc bảo mật.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Lỗi khi tải dữ liệu mục tiêu tiết kiệm", Toast.LENGTH_SHORT).show();
                }
                finish();
            });
    }

    private void updateUIWithSavingsGoal() {
        if (savingsGoal == null) return;
        
        // Cập nhật tiêu đề và thông tin mục tiêu
        tvGoalTitle.setText(savingsGoal.getTitle());
        tvGoalDescription.setText(savingsGoal.getDescription());
        
        // Cập nhật số tiền
        tvGoalAmount.setText(CurrencyUtils.formatAmount(savingsGoal.getTargetAmount()));
        tvCurrentAmount.setText(CurrencyUtils.formatAmount(savingsGoal.getCurrentAmount()));
        
        // Cập nhật tiến trình
        int progress = (int) savingsGoal.getProgressPercentage();
        tvProgress.setText(progress + "%");
        
        // Thiết lập icon theo loại mục tiêu
        setGoalIcon(imgGoalIcon, savingsGoal.getCategoryType());
        
        // Gợi ý tiêu đề
        edtTitle.setText("Gửi tiết kiệm - " + savingsGoal.getTitle());
    }

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

    private void saveTransaction() {
        if (savingsGoal == null) {
            Toast.makeText(this, "Chưa tải được thông tin mục tiêu", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate số tiền
        String amountStr = edtAmount.getText().toString().trim();
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
            edtAmount.requestFocus();
            return;
        }

        double amount;
        try {
            // Xử lý chuỗi số tiền (loại bỏ dấu phẩy, đơn vị tiền)
            amountStr = amountStr.replaceAll("[^\\d.]", "");
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
            edtAmount.requestFocus();
            return;
        }

        if (amount <= 0) {
            Toast.makeText(this, "Số tiền phải lớn hơn 0", Toast.LENGTH_SHORT).show();
            edtAmount.requestFocus();
            return;
        }

        // Lấy tiêu đề và ghi chú
        String title = edtTitle.getText().toString().trim();
        if (title.isEmpty()) {
            title = "Giao dịch tiết kiệm";
        }

        String note = edtNote.getText().toString().trim();
        
        // Lấy loại giao dịch
        String transactionType = radioDeposit.isChecked() ? "deposit" : "withdraw";
        
        // Kiểm tra nếu rút tiền, số tiền không được vượt quá số đã tiết kiệm
        if ("withdraw".equals(transactionType) && amount > savingsGoal.getCurrentAmount()) {
            Toast.makeText(this, "Số tiền rút không được vượt quá số tiền đã tiết kiệm", Toast.LENGTH_SHORT).show();
            edtAmount.requestFocus();
            return;
        }
        
        // Hiển thị loader
        showLoader(true);
        
        // Tạo giao dịch tiết kiệm
        SavingsTransaction transaction = new SavingsTransaction();
        transaction.setUserId(currentUserId);
        transaction.setGoalId(goalId);
        transaction.setAmount(amount);
        transaction.setDescription(title);
        transaction.setNote(note);
        transaction.setTransactionType(transactionType);
        transaction.setDate(selectedDate.getTime());
        transaction.setCreatedAt(System.currentTimeMillis());
        transaction.setUpdatedAt(System.currentTimeMillis());
        
        // Lưu giao dịch vào Firestore
        saveSavingsTransaction(transaction);
    }

    private void saveSavingsTransaction(SavingsTransaction transaction) {
        try {
            CollectionReference savingsTransactionsRef = db.collection("savingsTransactions");
            
            // Lưu giao dịch
            savingsTransactionsRef.add(transaction.toMap())
                .addOnSuccessListener(documentReference -> {
                    // Cập nhật ID
                    String transactionId = documentReference.getId();
                    documentReference.update("id", transactionId);
                    
                    // Cập nhật số tiền cho mục tiêu tiết kiệm
                    updateSavingsGoalAmount(transaction);
                })
                .addOnFailureListener(e -> {
                    showLoader(false);
                    Log.e(TAG, "Lỗi khi lưu giao dịch: " + e.getMessage());
                    
                    if (e.getMessage() != null && e.getMessage().contains("PERMISSION_DENIED")) {
                        Toast.makeText(this, "Không có quyền lưu giao dịch. Vui lòng cập nhật quy tắc bảo mật.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Lỗi khi lưu giao dịch: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        } catch (Exception e) {
            showLoader(false);
            Log.e(TAG, "Lỗi xử lý: " + e.getMessage());
            Toast.makeText(this, "Đã xảy ra lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void updateSavingsGoalAmount(SavingsTransaction transaction) {
        DocumentReference goalRef = db.collection("savingsGoals").document(goalId);
        
        double amountChange = transaction.getAmount();
        if ("withdraw".equals(transaction.getTransactionType())) {
            amountChange = -amountChange; // Rút tiền sẽ giảm số dư
        }
        
        // Cập nhật số tiền mục tiêu
        double newAmount = savingsGoal.getCurrentAmount() + amountChange;
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("currentAmount", newAmount);
        updates.put("updatedAt", System.currentTimeMillis());
        
        goalRef.update(updates)
            .addOnSuccessListener(aVoid -> {
                // Cập nhật số dư người dùng
                String message = "withdraw".equals(transaction.getTransactionType()) 
                        ? "Đã rút tiền khỏi mục tiêu tiết kiệm"
                        : "Đã gửi tiền vào mục tiêu tiết kiệm";
                
                // Trừ tiền từ số dư nếu là gửi tiền vào
                // Hoặc cộng tiền vào số dư nếu là rút tiền
                if ("deposit".equals(transaction.getTransactionType())) {
                    updateUserBalance(transaction.getAmount(), message);
                } else {
                    updateUserBalance(-transaction.getAmount(), message);
                }
            })
            .addOnFailureListener(e -> {
                showLoader(false);
                Log.e(TAG, "Lỗi khi cập nhật số tiền mục tiêu: " + e.getMessage());
                Toast.makeText(this, "Lỗi khi cập nhật số tiền mục tiêu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
    
    private void updateUserBalance(double amount, String message) {
        DocumentReference userRef = db.collection("users").document(currentUserId);
        
        // Truy vấn số dư hiện tại
        userRef.get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Double currentBalance = documentSnapshot.getDouble("balance");
                    double balance = (currentBalance != null) ? currentBalance : 0.0;
                    
                    // Trừ tiền nếu gửi tiết kiệm (amount > 0)
                    // Cộng tiền nếu rút tiết kiệm (amount < 0)
                    double newBalance = balance - amount;
                    
                    // Cập nhật số dư
                    userRef.update("balance", newBalance)
                        .addOnSuccessListener(aVoid -> {
                            showLoader(false);
                            Toast.makeText(SavingsAddActivity.this, message, Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            showLoader(false);
                            Log.e(TAG, "Lỗi khi cập nhật số dư: " + e.getMessage());
                            Toast.makeText(SavingsAddActivity.this, "Lỗi khi cập nhật số dư: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                } else {
                    showLoader(false);
                    Log.e(TAG, "Không tìm thấy thông tin người dùng");
                    Toast.makeText(SavingsAddActivity.this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(e -> {
                showLoader(false);
                Log.e(TAG, "Lỗi khi đọc thông tin người dùng: " + e.getMessage());
                Toast.makeText(SavingsAddActivity.this, "Lỗi khi đọc thông tin người dùng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void showLoader(boolean show) {
        if (progressDialog == null) {
            progressDialog = new android.app.ProgressDialog(this);
            progressDialog.setMessage("Đang xử lý...");
            progressDialog.setCancelable(false);
        }
        
        if (show) {
            if (!progressDialog.isShowing()) {
                progressDialog.show();
            }
        } else {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }
} 