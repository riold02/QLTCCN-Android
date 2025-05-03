package com.example.qltccn.activities;

import androidx.appcompat.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.DatePicker;
import android.app.DatePickerDialog;
import java.util.Calendar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.qltccn.R;
import com.example.qltccn.utils.CurrencyUtils;
import com.example.qltccn.utils.UserUtils;
import com.example.qltccn.utils.FirebaseUtils;
import android.util.Log;
import com.example.qltccn.utils.NotificationUtils;

public class CategoryActivity extends AppCompatActivity {

    // Biến UI
    private ImageView backButton;
    private TextView txtTotalBalance;
    private TextView txtTotalExpense;
    private Button btnAddMoney;
    private View expenseLoadingIndicator; // Indicator khi đang tải tổng chi tiêu
    
    // Request code cho CategoryDetailActivity
    private static final int REQUEST_CATEGORY_DETAIL = 200;
    
    // Footer navigation
    private ImageView iconHome;
    private ImageView iconChart;
    private ImageView iconTrans;
    private ImageView iconCategory;
    private ImageView iconUser;
    
    // Category icons
    private LinearLayout foodCategory;
    private LinearLayout transportCategory;
    private LinearLayout medicineCategory;
    private LinearLayout groceriesCategory;
    private LinearLayout rentCategory;
    private LinearLayout giftsCategory;
    private LinearLayout savingsCategory;
    private LinearLayout entertainmentCategory;
    private LinearLayout moreCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        
        initViews();
        setupListeners();
        loadUserData();
    }
    
    private void initViews() {
        // Header
        backButton = findViewById(R.id.backButton);
        txtTotalBalance = findViewById(R.id.txtTotalBalance);
        txtTotalExpense = findViewById(R.id.txtTotalExpense);
        btnAddMoney = findViewById(R.id.btnAddMoney);
        
        // Thêm xử lý nút notification
        View notiContainer = findViewById(R.id.notiContainer);
        if (notiContainer != null) {
            notiContainer.setOnClickListener(v -> navigateToNotification());
        }
        
        // Footer
        iconHome = findViewById(R.id.iconHome);
        iconChart = findViewById(R.id.iconChart);
        iconTrans = findViewById(R.id.iconTrans);
        iconCategory = findViewById(R.id.iconCategory);
        iconUser = findViewById(R.id.iconUser);
        
        // Đặt biểu tượng Category là đã được chọn
        if (iconCategory != null) {
            iconCategory.setImageResource(R.drawable.ic_category1);
        }
        
        // Categories
        foodCategory = findViewById(R.id.foodCategory);
        transportCategory = findViewById(R.id.transportCategory);
        medicineCategory = findViewById(R.id.medicineCategory);
        groceriesCategory = findViewById(R.id.groceriesCategory);
        rentCategory = findViewById(R.id.rentCategory);
        giftsCategory = findViewById(R.id.giftsCategory);
        savingsCategory = findViewById(R.id.savingsCategory);
        entertainmentCategory = findViewById(R.id.entertainmentCategory);
        moreCategory = findViewById(R.id.moreCategory);
        
        // Indicator khi đang tải tổng chi tiêu
    }
    
    private void setupListeners() {
        // Back button
        backButton.setOnClickListener(v -> finish());
        
        // Nạp tiền button
        btnAddMoney.setOnClickListener(v -> showAddMoneyDialog());
        
        // Footer navigation
        iconHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            finish();
        });
        
        iconChart.setOnClickListener(v -> {
            Intent intent = new Intent(this, AnalysisActivity.class);
            startActivity(intent);
        });
        
        iconTrans.setOnClickListener(v -> {
            Intent intent = new Intent(this, TranActivity.class);
            startActivity(intent);
        });
        
        // Không cần listener cho iconCategory vì đang ở màn hình Category
        
        iconUser.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        });
        
        // Category clicks
        setupCategoryClickListeners();
    }
    
    private void showAddMoneyDialog() {
        // Sử dụng dialog có sẵn
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_funds, null);
        builder.setView(dialogView);
        
        // Tham chiếu đến các view trong dialog
        EditText edtAmount = dialogView.findViewById(R.id.editAmount);
        EditText edtNote = dialogView.findViewById(R.id.editNote);
        TextView textDate = dialogView.findViewById(R.id.textDate);
        View layoutDate = dialogView.findViewById(R.id.layoutDate);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnAddFunds = dialogView.findViewById(R.id.btnAddFunds);
        
        // Biến để lưu thời gian được chọn
        final Calendar[] selectedDateTime = {Calendar.getInstance()};
        final java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
        
        // Thiết lập ngày hiện tại
        textDate.setText(dateFormat.format(selectedDateTime[0].getTime()));
        
        // Thiết lập sự kiện cho view chọn ngày
        layoutDate.setOnClickListener(v -> {
            // Hiển thị DatePickerDialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                CategoryActivity.this,
                (view, year, monthOfYear, dayOfMonth) -> {
                    // Cập nhật ngày được chọn
                    selectedDateTime[0].set(Calendar.YEAR, year);
                    selectedDateTime[0].set(Calendar.MONTH, monthOfYear);
                    selectedDateTime[0].set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    
                    // Cập nhật hiển thị
                    textDate.setText(dateFormat.format(selectedDateTime[0].getTime()));
                },
                selectedDateTime[0].get(Calendar.YEAR),
                selectedDateTime[0].get(Calendar.MONTH),
                selectedDateTime[0].get(Calendar.DAY_OF_MONTH)
            );
            
            // Giới hạn không cho chọn quá ngày hiện tại và hiển thị dialog
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            datePickerDialog.show();
        });
        
        // Tạo dialog
        AlertDialog dialog = builder.create();
        
        // Xử lý sự kiện nút Hủy
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        // Xử lý sự kiện nút Thêm tiền
        btnAddFunds.setOnClickListener(v -> {
            String amountStr = edtAmount.getText().toString().trim();
            String note = edtNote.getText().toString().trim();
            
            if (TextUtils.isEmpty(amountStr)) {
                Toast.makeText(CategoryActivity.this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
                return;
            }
            
            try {
                // Xử lý chuỗi trước khi chuyển đổi, loại bỏ dấu phẩy, chấm
                amountStr = amountStr.replaceAll("[^\\d]", "");
                
                // Chuyển đổi sang số
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    Toast.makeText(CategoryActivity.this, "Số tiền phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Đặt thời gian đã chọn
                long selectedTime = selectedDateTime[0].getTimeInMillis();
                
                // Gọi API để nạp tiền và truyền ngày đã chọn
                addMoneyToAccount(amount, note, selectedTime);
                dialog.dismiss();
                
            } catch (NumberFormatException e) {
                Toast.makeText(CategoryActivity.this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Hiển thị dialog
        dialog.show();
    }
    
    private void addMoneyToAccount(double amount, String note, long selectedTime) {
        // Hiển thị thông báo đang xử lý
        Toast.makeText(this, "Đang nạp tiền...", Toast.LENGTH_SHORT).show();
        
        try {
            // Gọi hàm nạp tiền từ Firebase Utils với context của activity
            FirebaseUtils.addFundsToAccount(amount, note, selectedTime, new FirebaseUtils.FirebaseConnectionCallback() {
                @Override
                public void onConnected() {
                    runOnUiThread(() -> {
                        // Nạp tiền thành công
                        Toast.makeText(CategoryActivity.this, 
                                "Đã nạp " + CurrencyUtils.formatVND(amount) + " thành công", 
                                Toast.LENGTH_SHORT).show();
                        
                        // Tạo thông báo nạp tiền thành công
                        NotificationUtils.addDepositNotification(CategoryActivity.this, amount, note);
                        
                        // Tải lại dữ liệu người dùng
                        loadUserData();
                    });
                }
                
                @Override
                public void onDisconnected(String message) {
                    runOnUiThread(() -> {
                        // Mất kết nối
                        Toast.makeText(CategoryActivity.this, 
                                "Mất kết nối: " + message, 
                                Toast.LENGTH_SHORT).show();
                    });
                }
                
                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        // Có lỗi xảy ra
                        Toast.makeText(CategoryActivity.this, 
                                "Lỗi: " + error, 
                                Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khi nạp tiền: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void setupCategoryClickListeners() {
        // Xử lý click cho từng danh mục
        View.OnClickListener categoryClickListener = v -> {
            String categoryName = "";
            
            if (v.getId() == R.id.foodCategory) {
                categoryName = "Food";
            } else if (v.getId() == R.id.transportCategory) {
                categoryName = "Transport";
            } else if (v.getId() == R.id.medicineCategory) {
                categoryName = "Medicine";
            } else if (v.getId() == R.id.groceriesCategory) {
                categoryName = "Groceries";
            } else if (v.getId() == R.id.rentCategory) {
                categoryName = "Rent";
            } else if (v.getId() == R.id.giftsCategory) {
                categoryName = "Gifts";
            } else if (v.getId() == R.id.savingsCategory) {
                // Chuyển đến SavingsActivity thay vì CategoryDetailActivity
                Intent intent = new Intent(this, SavingsActivity.class);
                startActivity(intent);
                return;
            } else if (v.getId() == R.id.entertainmentCategory) {
                categoryName = "Entertainment";
            } else if (v.getId() == R.id.moreCategory) {
                // Xử lý riêng cho "More" - có thể mở CategoryManagementActivity
             //   Intent intent = new Intent(this, CategoryManagementActivity.class);
              //  startActivity(intent);
                return;
            }
            
            if (!categoryName.isEmpty()) {
                // Mở màn hình chi tiết danh mục
                Intent intent = new Intent(this, CategoryDetailActivity.class);
                intent.putExtra("CATEGORY_NAME", categoryName);
                startActivityForResult(intent, REQUEST_CATEGORY_DETAIL);
            }
        };
        
        // Gán listener cho từng danh mục
        foodCategory.setOnClickListener(categoryClickListener);
        transportCategory.setOnClickListener(categoryClickListener);
        medicineCategory.setOnClickListener(categoryClickListener);
        groceriesCategory.setOnClickListener(categoryClickListener);
        rentCategory.setOnClickListener(categoryClickListener);
        giftsCategory.setOnClickListener(categoryClickListener);
        savingsCategory.setOnClickListener(categoryClickListener);
        entertainmentCategory.setOnClickListener(categoryClickListener);
        moreCategory.setOnClickListener(categoryClickListener);
    }
    
    private void loadUserData() {
        // Tải thông tin số dư và chi tiêu
        UserUtils.getCurrentUser(new UserUtils.FetchUserCallback() {
            @Override
            public void onSuccess(com.example.qltccn.models.User user) {
                if (user != null) {
                    txtTotalBalance.setText(CurrencyUtils.formatVND(user.getBalance()));
                    
                    // Tính toán lại tổng chi tiêu từ Firestore thay vì chỉ đọc từ SharedPreferences
                    calculateAndUpdateExpense(user.getId());
                }
            }
            
            @Override
            public void onError(String errorMessage) {
                Toast.makeText(CategoryActivity.this, "Không thể tải thông tin người dùng", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Tính toán lại tổng chi tiêu từ Firestore và cập nhật lên UI
     * @param userId ID của người dùng
     */
    private void calculateAndUpdateExpense(String userId) {
        // Hiển thị giá trị từ SharedPreferences trước để người dùng không phải đợi
        android.content.SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        float cachedTotalExpense = prefs.getFloat("total_expense", 0f);
        txtTotalExpense.setText("-" + CurrencyUtils.formatVND(cachedTotalExpense));
        
        // Lấy tham chiếu đến collection giao dịch
        com.google.firebase.firestore.CollectionReference transactionsRef = FirebaseUtils.getUserTransactionsCollection();
        if (transactionsRef == null) {
            Log.e("CategoryActivity", "Không thể lấy tham chiếu đến collection giao dịch");
            return;
        }
        
        // Truy vấn tất cả các giao dịch chi tiêu
        transactionsRef
            .whereEqualTo("userId", userId)
            .whereEqualTo("type", "expense")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                double totalExpense = 0;
                
                // Tính tổng chi tiêu
                for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                    if (doc.contains("amount") && doc.get("amount") instanceof Number) {
                        totalExpense += ((Number) doc.get("amount")).doubleValue();
                    }
                }
                
                // Cập nhật UI
                txtTotalExpense.setText("-" + CurrencyUtils.formatVND(totalExpense));
                
                // Lưu vào SharedPreferences để dùng cho các lần sau
                android.content.SharedPreferences.Editor editor = prefs.edit();
                editor.putFloat("total_expense", (float) totalExpense);
                editor.apply();
                
                Log.d("CategoryActivity", "Đã cập nhật tổng chi tiêu mới: " + totalExpense);
            })
            .addOnFailureListener(e -> {
                Log.e("CategoryActivity", "Lỗi khi truy vấn giao dịch: " + e.getMessage());
            });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        // Tải lại dữ liệu khi quay lại màn hình
        loadUserData();
        
        // Ghi log để debug
        Log.d("CategoryActivity", "onResume: Đã tải lại dữ liệu người dùng");
    }

    // Thêm phương thức chuyển đến màn hình thông báo
    private void navigateToNotification() {
        try {
            Intent intent = new Intent(this, NotiActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e("CategoryActivity", "Lỗi khi mở màn hình thông báo: " + e.getMessage(), e);
            Toast.makeText(this, "Không thể mở thông báo", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_CATEGORY_DETAIL && resultCode == RESULT_OK) {
            // Kiểm tra xem có cần làm mới dữ liệu không
            boolean refreshCategories = data != null && data.getBooleanExtra("REFRESH_CATEGORIES", false);
            
            if (refreshCategories) {
                Log.d("CategoryActivity", "Nhận yêu cầu làm mới từ CategoryDetailActivity");
                
                // Tải lại thông tin người dùng (bao gồm số dư)
                UserUtils.getCurrentUser(new UserUtils.FetchUserCallback() {
                    @Override
                    public void onSuccess(com.example.qltccn.models.User user) {
                        if (user != null) {
                            // Cập nhật số dư
                            txtTotalBalance.setText(CurrencyUtils.formatVND(user.getBalance()));
                            
                            // Tính toán lại tổng chi tiêu trực tiếp từ Firestore
                            calculateAndUpdateExpense(user.getId());
                            
                            // Hiển thị thông báo
                            Toast.makeText(CategoryActivity.this, "Dữ liệu đã được cập nhật", Toast.LENGTH_SHORT).show();
                        }
                    }
                    
                    @Override
                    public void onError(String errorMessage) {
                        // Nếu không lấy được thông tin người dùng, chỉ cập nhật lại dữ liệu cũ
                        loadUserData();
                        Toast.makeText(CategoryActivity.this, 
                            "Không thể tải thông tin mới nhất: " + errorMessage, 
                            Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }
} 