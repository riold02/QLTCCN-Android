package com.example.qltccn.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.qltccn.R;
import com.example.qltccn.models.Transaction;
import com.example.qltccn.utils.CurrencyUtils;
import com.example.qltccn.utils.FirebaseUtils;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CategoryAddActivity extends AppCompatActivity {
    private static final String TAG = "CategoryAddActivity";

    // UI components
    private ImageView btnBack;
    private TextView txtDate;
    private TextView txtCategory;
    private EditText edtAmount;
    private EditText edtTitle;
    private EditText edtMessage;
    private Button btnSave;
    private RelativeLayout dateLayout;
    private RelativeLayout categoryLayout;

    // Data
    private String categoryName;
    private Date selectedDate;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_add);

        // Lấy tên danh mục từ intent
        categoryName = getIntent().getStringExtra("CATEGORY_NAME");

        initViews();
        setupInitialData();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        txtDate = findViewById(R.id.txtDate);
        txtCategory = findViewById(R.id.txtCategory);
        edtAmount = findViewById(R.id.edtAmount);
        edtTitle = findViewById(R.id.edtTitle);
        edtMessage = findViewById(R.id.edtMessage);
        btnSave = findViewById(R.id.btnSave);
        dateLayout = findViewById(R.id.dateLayout);
        categoryLayout = findViewById(R.id.categoryLayout);

        dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    }

    private void setupInitialData() {
        // Thiết lập ngày hiện tại
        selectedDate = new Date();
        txtDate.setText(dateFormat.format(selectedDate));

        // Thêm gợi ý cho người dùng khi chọn thời gian
        dateLayout.setOnLongClickListener(v -> {
            Toast.makeText(this, "Nhấn để chọn thời gian chi tiết", Toast.LENGTH_SHORT).show();
            return true;
        });

        // Thiết lập danh mục
        if (categoryName != null && !categoryName.isEmpty()) {
            txtCategory.setText(categoryName);
            txtCategory.setTextColor(getResources().getColor(android.R.color.black));
        }
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

    private void saveTransaction() {
        // Kiểm tra người dùng đã đăng nhập chưa thay vì kiểm tra kết nối Firebase
        if (!FirebaseUtils.isUserLoggedIn()) {
            Toast.makeText(CategoryAddActivity.this, "Bạn chưa đăng nhập. Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Tiếp tục lưu giao dịch
        proceedWithSavingTransaction();
    }

    private void proceedWithSavingTransaction() {
        // Lấy userId hiện tại
        String userId = FirebaseUtils.getCurrentUser() != null ? FirebaseUtils.getCurrentUser().getUid() : null;
        if (userId == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập. Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate danh mục
        if (categoryName == null || categoryName.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn danh mục", Toast.LENGTH_SHORT).show();
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

        // Validate tiêu đề
        String title = edtTitle.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tiêu đề chi tiêu", Toast.LENGTH_SHORT).show();
            edtTitle.requestFocus();
            return;
        }

        // Lấy mô tả (không bắt buộc)
        String message = edtMessage.getText().toString().trim();

        // Hiển thị loader
        showLoader(true);
        
        // Kiểm tra số dư trước khi thêm giao dịch
        checkBalanceBeforeTransaction(userId, amount, () -> {
            // Tạo đối tượng giao dịch sau khi kiểm tra số dư
            Transaction transaction = new Transaction();
            transaction.setUserId(userId);
            transaction.setCategory(categoryName);
            transaction.setAmount(amount);
            transaction.setDescription(title);
            transaction.setNote(message);

            transaction.setType("expense"); // Loại giao dịch là chi tiêu
            transaction.setDate(selectedDate.getTime());
            transaction.setCreatedAt(new Date().getTime());

            // Lưu vào Firestore
            saveTransactionToFirestore(transaction);
        });
    }
    
    /**
     * Kiểm tra số dư trước khi thêm giao dịch chi tiêu
     * @param userId ID người dùng
     * @param transactionAmount Số tiền giao dịch
     * @param onSuccess Callback khi số dư đủ
     */
    private void checkBalanceBeforeTransaction(String userId, double transactionAmount, Runnable onSuccess) {
        FirebaseFirestore.getInstance().collection("users").document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Double currentBalance = documentSnapshot.getDouble("balance");
                    Log.d(TAG, "Số dư hiện tại: " + currentBalance + ", Số tiền giao dịch: " + transactionAmount);
                    
                    if (currentBalance != null) {
                        if (currentBalance >= transactionAmount) {
                            // Số dư đủ, tiếp tục thêm giao dịch
                            Log.d(TAG, "Số dư đủ, tiếp tục tạo giao dịch");
                            onSuccess.run();
                        } else {
                            // Số dư không đủ, hiển thị cảnh báo
                            showLoader(false);
                            showInsufficientBalanceWarning(currentBalance, transactionAmount);
                        }
                    } else {
                        // Không có thông tin số dư, có thể là tài khoản mới
                        Log.w(TAG, "Không tìm thấy thông tin số dư");
                        showLoader(false);
                        showInsufficientBalanceWarning(0, transactionAmount);
                    }
                } else {
                    // Không tìm thấy document người dùng
                    Log.e(TAG, "Không tìm thấy thông tin người dùng");
                    showLoader(false);
                    Toast.makeText(CategoryAddActivity.this, "Không thể lấy thông tin số dư", Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(e -> {
                showLoader(false);
                Log.e(TAG, "Lỗi khi kiểm tra số dư: " + e.getMessage());
                Toast.makeText(CategoryAddActivity.this, "Lỗi khi kiểm tra số dư: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
    
    /**
     * Hiển thị cảnh báo khi số dư không đủ
     * @param currentBalance Số dư hiện tại
     * @param transactionAmount Số tiền giao dịch
     */
    private void showInsufficientBalanceWarning(double currentBalance, double transactionAmount) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Số dư không đủ");
        builder.setMessage("Số dư hiện tại của bạn là " + CurrencyUtils.formatVND(currentBalance) + 
                          ", không đủ để chi tiêu " + CurrencyUtils.formatVND(transactionAmount) + 
                          ".\n\nBạn có muốn tiếp tục tạo giao dịch này không?");
        
        builder.setPositiveButton("Tiếp tục", (dialog, which) -> {
            // Tạo đối tượng giao dịch bất chấp số dư không đủ
            Transaction transaction = new Transaction();
            transaction.setUserId(FirebaseUtils.getCurrentUser().getUid());
            transaction.setCategory(categoryName);
            transaction.setAmount(Double.parseDouble(edtAmount.getText().toString().replaceAll("[^\\d.]", "")));
            transaction.setDescription(edtTitle.getText().toString().trim());
            transaction.setNote(edtMessage.getText().toString().trim());
            transaction.setType("expense");
            transaction.setDate(selectedDate.getTime());
            transaction.setCreatedAt(new Date().getTime());
            
            // Hiển thị loader và lưu
            showLoader(true);
            saveTransactionToFirestore(transaction);
        });
        
        builder.setNegativeButton("Hủy bỏ", (dialog, which) -> {
            dialog.dismiss();
        });
        
        builder.setCancelable(false);
        builder.show();
    }

    private void saveTransactionToFirestore(Transaction transaction) {
        try {
            // Log để kiểm tra dữ liệu trước khi lưu
            Log.d(TAG, "Bắt đầu lưu giao dịch: " + transaction.getCategory() + ", " + transaction.getAmount() + 
                    ", thời gian: " + dateFormat.format(new Date(transaction.getDate())));
            
            // Tạo HashMap trực tiếp thay vì dùng phương thức toMap() có thể gây lỗi
            Map<String, Object> transactionMap = new HashMap<>();
            transactionMap.put("userId", transaction.getUserId());
            transactionMap.put("category", transaction.getCategory());
            transactionMap.put("amount", transaction.getAmount());
            transactionMap.put("description", transaction.getDescription());
            transactionMap.put("note", transaction.getNote());
            transactionMap.put("type", transaction.getType());
            
            // Lưu thời gian chính xác với giờ phút
            Date transactionDate = new Date(transaction.getDate());
            transactionMap.put("date", new Timestamp(transactionDate));
            transactionMap.put("createdAt", new Timestamp(new Date(transaction.getCreatedAt())));
            transactionMap.put("updatedAt", new Timestamp(new Date()));
            
            // Log dữ liệu sẽ lưu để debug
            Log.d(TAG, "Dữ liệu chi tiêu: userId=" + transactionMap.get("userId") + 
                  ", category=" + transactionMap.get("category") + 
                  ", amount=" + transactionMap.get("amount") + 
                  ", thời gian=" + dateFormat.format(transactionDate));
            
            // Kiểm tra collection transactions đã được khởi tạo đúng
            CollectionReference transactionsRef = FirebaseUtils.getUserTransactionsCollection();
            if (transactionsRef == null) {
                Log.e(TAG, "Không thể lấy tham chiếu đến collection transactions");
            
                showLoader(false);
                return;
            }
            
            // Ghi log đường dẫn để kiểm tra
            Log.d(TAG, "Đường dẫn lưu giao dịch: " + transactionsRef.getPath());
            
            // Thực hiện thêm dữ liệu vào subcollection transactions của người dùng
            transactionsRef
                .add(transactionMap)
                .addOnSuccessListener(documentReference -> {
                    showLoader(false);
                    String docId = documentReference.getId();
                    Log.d(TAG, "Giao dịch đã được lưu thành công với ID: " + docId);
                    
                    // Thêm ID vào transaction và cập nhật lại để đảm bảo id được lưu
                    documentReference.update("id", docId)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Đã cập nhật ID giao dịch"))
                        .addOnFailureListener(e -> Log.e(TAG, "Lỗi cập nhật ID giao dịch: " + e.getMessage()));
                    
                    Toast.makeText(CategoryAddActivity.this, "Đã lưu chi tiêu thành công", Toast.LENGTH_SHORT).show();
                    
                    // Cập nhật số dư người dùng
                    updateUserBalance(transaction.getUserId(), transaction.getAmount());
                    
                    // Đóng màn hình thêm chi phí
                    finish();
                })
                .addOnFailureListener(e -> {
                    showLoader(false);
                    Log.e(TAG, "Lỗi khi lưu giao dịch lên Firestore: " + e.getMessage(), e);
                    Toast.makeText(CategoryAddActivity.this, "Không thể lưu chi tiêu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        } catch (Exception e) {
            showLoader(false);
            Log.e(TAG, "Lỗi ngoại lệ khi lưu giao dịch: " + e.getMessage(), e);
            Toast.makeText(this, "Đã xảy ra lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUserBalance(String userId, double expenseAmount) {
        if (userId == null) {
            Log.e(TAG, "updateUserBalance: userId là null");
            return;
        }
        
        Log.d(TAG, "Bắt đầu cập nhật số dư cho user: " + userId + ", giảm: " + expenseAmount);
        
        // Tham chiếu đến document người dùng
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Double currentBalance = documentSnapshot.getDouble("balance");
                    Log.d(TAG, "Số dư hiện tại: " + currentBalance);
                    
                    if (currentBalance != null) {
                        // Giảm số dư đi khoản chi phí
                        double newBalance = currentBalance - expenseAmount;
                        Log.d(TAG, "Số dư mới: " + newBalance);
                        
                        // Cập nhật số dư mới và thời gian cập nhật
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("balance", newBalance);
                        updates.put("updatedAt", new Timestamp(new Date()));
                        
                        db.collection("users").document(userId)
                            .update(updates)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Đã cập nhật số dư thành công: " + newBalance);
                                
                                // Lưu vào SharedPreferences để cập nhật UI nhanh hơn khi quay lại màn hình trước
                                try {
                                    android.content.SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
                                    android.content.SharedPreferences.Editor editor = prefs.edit();
                                    editor.putFloat("current_balance", (float) newBalance);
                                    editor.apply();
                                } catch (Exception e) {
                                    Log.e(TAG, "Lỗi khi lưu SharedPreferences: " + e.getMessage());
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Lỗi cập nhật số dư: " + e.getMessage());
                            });
                    } else {
                        Log.e(TAG, "Số dư hiện tại là null");
                    }
                } else {
                    Log.e(TAG, "Document người dùng không tồn tại");
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Lỗi khi lấy thông tin người dùng: " + e.getMessage());
            });
    }

    private void showLoader(boolean show) {
        // Hiển thị loader (có thể triển khai ProgressBar nếu cần)
        if (show) {
            Toast.makeText(this, "Đang lưu...", Toast.LENGTH_SHORT).show();
            // Không cần kiểm tra kết nối Firebase ở đây nữa
        }
    }
} 