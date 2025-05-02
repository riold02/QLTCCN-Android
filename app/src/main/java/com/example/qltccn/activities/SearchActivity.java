package com.example.qltccn.activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qltccn.R;
import com.example.qltccn.adapters.TransactionAdapter;
import com.example.qltccn.models.Category;
import com.example.qltccn.models.Transaction;
import com.example.qltccn.utils.CurrencyUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

public class SearchActivity extends AppCompatActivity {
    private static final String TAG = "SearchActivity";

    // UI elements
    private ImageButton toolbarBackBtn;
    private ImageView idNoti;
    private ImageView iconHome, iconAnalysis, iconTrans, iconCategory, iconUser;
    
    private EditText searchEt;
    private SwitchCompat switchIncome, switchExpense, switchSavings;
    private Button btnSearch;
    private TextView searchResultsTitle, noResultsTextView;
    private RecyclerView searchResultsRecyclerView;
    private TextView dateTextView;
    private RelativeLayout datePickerLayout;
    private Spinner categorySpinner;

    // Search parameters
    private Calendar selectedDate;
    private String selectedCategory = "";
    private boolean searchIncome = false;
    private boolean searchExpense = true;
    private boolean searchSavings = false;
    
    // RecyclerView adapter
    private TransactionAdapter transactionAdapter;
    private List<Transaction> transactionList;
    
    // Danh sách danh mục
    private List<Category> incomeCategories = new ArrayList<>();
    private List<Category> expenseCategories = new ArrayList<>();
    private List<Category> savingsCategories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        
        try {
            // Initialize calendar
            selectedDate = Calendar.getInstance();
            
            // Initialize UI elements
            initUI();
            
            // Thiết lập RecyclerView trước để sẵn sàng hiển thị kết quả
            setupRecyclerView();
            
            // Thiết lập sự kiện cho các nút
            setClickListeners();
            
            // Tải danh mục - cuối cùng để sử dụng UI đã khởi tạo ở trên
            loadCategories();
            
            Log.d(TAG, "Ứng dụng tìm kiếm đã khởi tạo thành công");
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi khởi tạo SearchActivity: " + e.getMessage(), e);
            Toast.makeText(this, "Có lỗi khi khởi tạo. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
        }
    }

    private void initUI() {
        // Toolbar
        toolbarBackBtn = findViewById(R.id.toolbarBackBtn);
        idNoti = findViewById(R.id.idNoti);

        // Search components
        searchEt = findViewById(R.id.searchEt);
        switchIncome = findViewById(R.id.switchIncome);
        switchExpense = findViewById(R.id.switchExpense);
        switchSavings = findViewById(R.id.switchSavings);
        btnSearch = findViewById(R.id.btnSearch);
        
        // Tìm TextView hiển thị ngày và layout chọn ngày
        dateTextView = findViewById(R.id.dateTextView);
        datePickerLayout = findViewById(R.id.datePickerLayout);
        
        // Tìm TextView hiển thị danh mục và layout chọn danh mục
        categorySpinner = findViewById(R.id.categorySpinner);
        
        // Cập nhật ngày hiển thị
        updateDisplayDate();

        // Result views
        searchResultsTitle = findViewById(R.id.searchResultsTitle);
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView);
        noResultsTextView = findViewById(R.id.noResultsTextView);
        
        // Đảm bảo RecyclerView không bị null
        if (searchResultsRecyclerView == null) {
            Log.e(TAG, "SearchResultsRecyclerView không tìm thấy trong layout!");
        } else {
            Log.d(TAG, "SearchResultsRecyclerView đã được tìm thấy");
        }

        // Footer icons
        iconHome = findViewById(R.id.iconHome);
        iconAnalysis = findViewById(R.id.iconChart);
        iconTrans = findViewById(R.id.iconTrans);
        iconCategory = findViewById(R.id.iconCategory);
        iconUser = findViewById(R.id.iconUser);
    }
    
    private void updateDisplayDate() {
        if (dateTextView != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            dateTextView.setText(sdf.format(selectedDate.getTime()));
        }
    }
    
    private void setupRecyclerView() {
        try {
            if (searchResultsRecyclerView == null) {
                Log.e(TAG, "RecyclerView không tìm thấy trong setupRecyclerView()!");
                return;
            }
            
            // Thiết lập layout manager với giới hạn cao tối đa
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            searchResultsRecyclerView.setLayoutManager(layoutManager);
            
            // Chỉ tạo mới adapter nếu chưa tồn tại
            if (transactionList == null) {
                transactionList = new ArrayList<>();
            }
            
            if (transactionAdapter == null) {
                transactionAdapter = new TransactionAdapter(this, transactionList);
                
                // Xử lý sự kiện khi nhấp vào một giao dịch
                transactionAdapter.setOnTransactionClickListener((transaction, position) -> {
                    // Hiển thị chi tiết giao dịch
                    String typeText = "";
                    if ("income".equals(transaction.getType())) {
                        typeText = "Thu nhập";
                    } else if ("expense".equals(transaction.getType())) {
                        typeText = "Chi tiêu";
                    } else if ("savings".equals(transaction.getType())) {
                        typeText = "Tiết kiệm";
                    }
                    
                    Toast.makeText(SearchActivity.this, 
                        "Chi tiết: " + typeText + " - " + CurrencyUtils.formatVND(transaction.getAmount()) + " - " + transaction.getCategory(), 
                        Toast.LENGTH_SHORT).show();
                });
                
                searchResultsRecyclerView.setAdapter(transactionAdapter);
            }
            
            // Ban đầu, ẩn RecyclerView và các view liên quan đến kết quả
            searchResultsTitle.setVisibility(View.GONE);
            searchResultsRecyclerView.setVisibility(View.GONE);
            noResultsTextView.setVisibility(View.GONE);
            
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi thiết lập RecyclerView: " + e.getMessage(), e);
        }
    }

    private void setClickListeners() {
        // Toolbar actions
        toolbarBackBtn.setOnClickListener(v -> finish());
        
        // Cập nhật xử lý khi nhấp vào nút thông báo
        idNoti.setOnClickListener(v -> {
            // Hiển thị thông báo tính năng đang phát triển thay vì mở NotiActivity
            Toast.makeText(SearchActivity.this, "Tính năng đang phát triển", Toast.LENGTH_SHORT).show();
            
            // Tạo rung nhẹ để tăng trải nghiệm người dùng
            try {
                android.os.Vibrator vibrator = (android.os.Vibrator) getSystemService(android.content.Context.VIBRATOR_SERVICE);
                if (vibrator != null && vibrator.hasVibrator()) {
                    // Rung nhẹ 100ms
                    vibrator.vibrate(100);
                }
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi tạo rung: " + e.getMessage());
            }
        });

        // Search text change listener
        searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // Could implement search suggestions as user types
            }
        });
        
        // Toggle switches with listener để cập nhật danh sách danh mục
        switchIncome.setOnCheckedChangeListener((buttonView, isChecked) -> {
            searchIncome = isChecked;
            setupCategorySpinner();
        });
        
        switchExpense.setOnCheckedChangeListener((buttonView, isChecked) -> {
            searchExpense = isChecked;
            setupCategorySpinner();
        });
        
        switchSavings.setOnCheckedChangeListener((buttonView, isChecked) -> {
            searchSavings = isChecked;
            setupCategorySpinner();
        });
        
        // Date picker
        if (datePickerLayout != null) {
            datePickerLayout.setOnClickListener(v -> showDatePickerDialog());
        }
        
        // Search button
        btnSearch.setOnClickListener(v -> performSearch());

        // Footer navigation
        iconHome.setOnClickListener(v -> {
            startActivity(new Intent(SearchActivity.this, HomeActivity.class));
            finish();
        });
        
        iconAnalysis.setOnClickListener(v -> {
            startActivity(new Intent(SearchActivity.this, AnalysisActivity.class));
            finish();
        });
        
        iconTrans.setOnClickListener(v -> {
            startActivity(new Intent(SearchActivity.this, TranActivity.class));
            finish();
        });
        
        iconCategory.setOnClickListener(v -> {
            startActivity(new Intent(SearchActivity.this, CategoryActivity.class));
            finish();
        });
        
        iconUser.setOnClickListener(v -> {
            startActivity(new Intent(SearchActivity.this, ProfileActivity.class));
            finish();
        });
    }
    
    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                selectedDate.set(Calendar.YEAR, year);
                selectedDate.set(Calendar.MONTH, month);
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDisplayDate();
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }
    
    private void loadCategories() {
        // Xóa danh sách cũ
        incomeCategories.clear();
        expenseCategories.clear();
        savingsCategories.clear();
        
        // Thêm các danh mục mặc định
        // Danh mục thu nhập
        incomeCategories.add(new Category("1", "Lương", "income", "ic_salary"));
        incomeCategories.add(new Category("2", "Đầu tư", "income", "ic_investment"));
        incomeCategories.add(new Category("3", "Tiền thưởng", "income", "ic_bonus"));
        incomeCategories.add(new Category("4", "Quà tặng", "income", "ic_gift"));
     
        // Danh mục chi tiêu
        expenseCategories.add(new Category("5", "Ăn uống", "expense", "ic_food"));
        expenseCategories.add(new Category("6", "Di chuyển", "expense", "ic_transport"));
        expenseCategories.add(new Category("7", "Mua sắm", "expense", "ic_shopping"));
        expenseCategories.add(new Category("8", "Giải trí", "expense", "ic_entertainment"));
        expenseCategories.add(new Category("9", "Sức khỏe", "expense", "ic_health"));
        expenseCategories.add(new Category("10", "Giáo dục", "expense", "ic_education"));
        expenseCategories.add(new Category("11", "Nhà cửa", "expense", "ic_housing"));
        expenseCategories.add(new Category("12", "Hóa đơn", "expense", "ic_bills"));
        
        // Danh mục tiết kiệm
        savingsCategories.add(new Category("13", "Tiết kiệm", "savings", "ic_general"));
        savingsCategories.add(new Category("14", "Du lịch", "savings", "ic_travel"));
        savingsCategories.add(new Category("15", "Mua nhà", "savings", "ic_house"));
        savingsCategories.add(new Category("16", "Mua xe", "savings", "ic_car"));
        savingsCategories.add(new Category("17", "Đám cưới", "savings", "ic_wedding"));
        // Tạo danh sách cho Spinner
        setupCategorySpinner();
        
        // Cập nhật từ Firestore (nếu có)
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            
            FirebaseFirestore.getInstance().collection("users")
                    .document(userId)
                    .collection("categories")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            String id = document.getId();
                            String name = document.getString("name");
                            String type = document.getString("type");
                            String icon = document.getString("icon");
                            
                            if (name != null && type != null) {
                                Category category = new Category(id, name, type, icon);
                                
                                if ("income".equals(type)) {
                                    incomeCategories.add(category);
                                } else if ("expense".equals(type)) {
                                    expenseCategories.add(category);
                                } else if ("savings".equals(type)) {
                                    savingsCategories.add(category);
                                }
                            }
                        }
                        
                        // Cập nhật lại Spinner với danh mục mới
                        setupCategorySpinner();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Lỗi khi tải danh mục từ Firestore", e);
                    });
        }
    }

    private void setupCategorySpinner() {
        // Tạo danh sách danh mục hiển thị dựa trên loại giao dịch đã chọn
        List<Category> categoriesToShow = new ArrayList<>();
        
        if (searchIncome) {
            categoriesToShow.addAll(incomeCategories);
        }
        
        if (searchExpense) {
            categoriesToShow.addAll(expenseCategories);
        }
        
        if (searchSavings) {
            categoriesToShow.addAll(savingsCategories);
        }
        
        // Nếu không có danh mục nào được chọn, hiển thị tất cả
        if (categoriesToShow.isEmpty()) {
            categoriesToShow.addAll(incomeCategories);
            categoriesToShow.addAll(expenseCategories);
            categoriesToShow.addAll(savingsCategories);
        }
        
        // Tạo danh sách tên hiển thị và giá trị tương ứng
        List<String> displayNames = new ArrayList<>();
        Map<String, String> displayToValue = new HashMap<>();
        
        // Thêm tùy chọn "Tất cả danh mục" ở đầu
        displayNames.add("Tất cả danh mục");
        displayToValue.put("Tất cả danh mục", "");
        
        // Thêm các danh mục vào danh sách với định dạng "Tên [Loại]"
        for (Category category : categoriesToShow) {
            String type = "";
            if ("income".equals(category.getType())) {
                type = "[Thu nhập]";
            } else if ("expense".equals(category.getType())) {
                type = "[Chi tiêu]";
            } else if ("savings".equals(category.getType())) {
                type = "[Tiết kiệm]";
            }
            
            String displayName = category.getName() + " " + type;
            displayNames.add(displayName);
            displayToValue.put(displayName, category.getName());
        }
        
        // Tạo adapter cho Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, 
                android.R.layout.simple_spinner_dropdown_item,
                displayNames);
        
        // Thiết lập adapter cho Spinner
        categorySpinner.setAdapter(adapter);
        
        // Nếu đã chọn danh mục trước đó, chọn mục tương ứng trong spinner
        if (!selectedCategory.isEmpty()) {
            for (int i = 0; i < displayNames.size(); i++) {
                String displayName = displayNames.get(i);
                if (displayToValue.get(displayName) != null && 
                    displayToValue.get(displayName).equals(selectedCategory)) {
                    categorySpinner.setSelection(i);
                    break;
                }
            }
        }
        
        // Thiết lập sự kiện khi chọn danh mục
        categorySpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selectedDisplay = displayNames.get(position);
                selectedCategory = displayToValue.get(selectedDisplay);
                
                Log.d(TAG, "Đã chọn danh mục: " + selectedCategory + " (hiển thị: " + selectedDisplay + ")");
            }
            
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                // Không làm gì
            }
        });
    }

    private void performSearch() {
        String searchQuery = searchEt.getText().toString().trim().toLowerCase();
        
        if (!searchIncome && !searchExpense && !searchSavings) {
            Toast.makeText(this, "Vui lòng chọn ít nhất một loại giao dịch", Toast.LENGTH_SHORT).show();
            return;
        }
        
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để tìm kiếm", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String userId = currentUser.getUid();
        
        // Hiển thị loading
        Toast.makeText(this, "Đang tìm kiếm...", Toast.LENGTH_SHORT).show();
        
        // Xóa danh sách kết quả cũ
        if (transactionList != null) {
            transactionList.clear();
            if (transactionAdapter != null) {
                transactionAdapter.notifyDataSetChanged();
            }
        }
        
        // Thiết lập ngày bắt đầu và kết thúc để tìm kiếm
        Calendar startDate = (Calendar) selectedDate.clone();
        startDate.set(Calendar.HOUR_OF_DAY, 0);
        startDate.set(Calendar.MINUTE, 0);
        startDate.set(Calendar.SECOND, 0);
        
        Calendar endDate = (Calendar) selectedDate.clone();
        endDate.set(Calendar.HOUR_OF_DAY, 23);
        endDate.set(Calendar.MINUTE, 59);
        endDate.set(Calendar.SECOND, 59);
        
        // Debug - ghi lại thông tin tìm kiếm
        Log.d(TAG, "Tìm kiếm: Query='" + searchQuery + "', Category='" + selectedCategory + "'");
        Log.d(TAG, "Ngày: " + new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate.getTime()));
        Log.d(TAG, "Loại giao dịch: " + 
              (searchIncome ? "Thu nhập " : "") +
              (searchExpense ? "Chi tiêu " : "") +
              (searchSavings ? "Tiết kiệm" : ""));
        
        // Truy vấn Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        db.collection("users")
            .document(userId)
            .collection("transactions")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Transaction> results = new ArrayList<>();
                
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    try {
                        String type = document.getString("type");
                        Double amount = document.getDouble("amount");
                        String category = document.getString("category");
                        String note = document.getString("note");
                        String description = document.getString("description");
                        String transactionType = document.getString("transactionType");
                        
                        // Nếu là giao dịch tiết kiệm hoặc deposit và có mô tả, thêm vào danh mục
                        if ((category == null || category.isEmpty()) && 
                            (type != null && type.equals("savings") || "deposit".equals(transactionType))) {
                            
                            // Kiểm tra mô tả để xác định danh mục tiết kiệm
                            if (description != null) {
                                if (description.toLowerCase().contains("du lịch") || 
                                    description.toLowerCase().contains("travel")) {
                                    category = "travel";
                                } else if (description.toLowerCase().contains("nhà") || 
                                          description.toLowerCase().contains("house")) {
                                    category = "house";
                                } else if (description.toLowerCase().contains("xe") || 
                                          description.toLowerCase().contains("car")) {
                                    category = "car";
                                } else if (description.toLowerCase().contains("cưới") || 
                                          description.toLowerCase().contains("wedding")) {
                                    category = "wedding";
                                } else {
                                    category = "savings";
                                }
                            } else if (note != null) {
                                if (note.toLowerCase().contains("travel") || 
                                    note.toLowerCase().contains("du lịch")) {
                                    category = "travel";
                                } else if (note.toLowerCase().contains("house") || 
                                          note.toLowerCase().contains("nhà")) {
                                    category = "house";
                                } else if (note.toLowerCase().contains("car") || 
                                          note.toLowerCase().contains("xe")) {
                                    category = "car";
                                } else if (note.toLowerCase().contains("wedding") || 
                                          note.toLowerCase().contains("cưới")) {
                                    category = "wedding";
                                } else {
                                    category = "savings";
                                }
                            } else {
                                category = "savings";
                            }
                        }
                        
                        // Chuyển đổi sang chữ thường để so sánh không phân biệt hoa thường
                        String categoryLower = category != null ? category.toLowerCase() : "";
                        String noteLower = note != null ? note.toLowerCase() : "";
                        String descriptionLower = description != null ? description.toLowerCase() : "";
                        
                        Log.d(TAG, "Giao dịch: " + document.getId() + " - " + type + " - " + category + 
                              (description != null ? " - Desc: " + description : "") + 
                              (transactionType != null ? " - TransType: " + transactionType : ""));
                        
                        // Xử lý ngày từ nhiều định dạng
                        Long dateValue = null;
                        Date transactionDate = null;
                        
                        if (document.get("date") instanceof com.google.firebase.Timestamp) {
                            com.google.firebase.Timestamp timestamp = document.getTimestamp("date");
                            if (timestamp != null) {
                                transactionDate = timestamp.toDate();
                                dateValue = transactionDate.getTime();
                            }
                        } else if (document.get("date") instanceof String) {
                            String dateString = document.getString("date");
                            try {
                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                transactionDate = sdf.parse(dateString);
                                if (transactionDate != null) {
                                    dateValue = transactionDate.getTime();
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Lỗi khi parse ngày: " + e.getMessage());
                            }
                        } else {
                            dateValue = document.getLong("date");
                            if (dateValue != null) {
                                transactionDate = new Date(dateValue);
                            }
                        }
                        
                        // Kiểm tra xem giao dịch có thỏa mãn điều kiện tìm kiếm không
                        boolean matchesType = (searchIncome && "income".equals(type)) || 
                                            (searchExpense && "expense".equals(type)) ||
                                            (searchSavings && ("savings".equals(type) || "deposit".equals(transactionType)));
                        
                        boolean matchesDate = true;
                        if (dateValue != null) {
                            matchesDate = dateValue >= startDate.getTimeInMillis() && 
                                         dateValue <= endDate.getTimeInMillis();
                        }
                        
                        boolean matchesQuery = true;
                        if (!searchQuery.isEmpty()) {
                            matchesQuery = (categoryLower.contains(searchQuery)) ||
                                          (noteLower.contains(searchQuery)) ||
                                          (descriptionLower.contains(searchQuery));
                        }
                        
                        boolean matchesCategory = true;
                        if (!selectedCategory.isEmpty()) {
                            matchesCategory = isCategoryMatch(selectedCategory, category);
                            
                            // Kiểm tra thêm cho Du lịch trong mô tả
                            if (!matchesCategory && "Du lịch".equalsIgnoreCase(selectedCategory) || "travel".equalsIgnoreCase(selectedCategory)) {
                                matchesCategory = (descriptionLower != null && descriptionLower.contains("du lịch")) || 
                                                 (descriptionLower != null && descriptionLower.contains("travel")) ||
                                                 (noteLower != null && noteLower.contains("du lịch")) ||
                                                 (noteLower != null && noteLower.contains("travel"));
                            }
                        }
                        
                        // Debug
                        if (matchesType && matchesDate && matchesQuery && matchesCategory) {
                            Log.d(TAG, "Phù hợp: YES - " + document.getId());
                        } else {
                            Log.d(TAG, "Phù hợp: NO - " + document.getId() + 
                                   " (Type=" + matchesType + 
                                   ", Date=" + matchesDate + 
                                   ", Query=" + matchesQuery + 
                                   ", Category=" + matchesCategory + ")");
                        }
                        
                        // Thêm vào kết quả nếu thỏa mãn các điều kiện
                        if (matchesType && matchesDate && matchesQuery && matchesCategory) {
                            Transaction transaction = new Transaction();
                            transaction.setId(document.getId());
                            transaction.setType(type);
                            transaction.setAmount(amount != null ? amount : 0);
                            transaction.setCategory(category);
                            transaction.setNote(note);
                            transaction.setDate(dateValue);
                            
                            results.add(transaction);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Lỗi khi xử lý giao dịch: " + e.getMessage(), e);
                    }
                }
                
                // Debug
                Log.d(TAG, "Kết quả tìm kiếm: " + results.size() + " giao dịch");
                
                // Cập nhật UI dựa trên kết quả
                updateUIWithResults(results);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Lỗi khi tìm kiếm: " + e.getMessage(), e);
                Toast.makeText(SearchActivity.this, "Lỗi khi tìm kiếm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                showNoResults();
            });
    }
    
    private void updateUIWithResults(List<Transaction> results) {
        try {
            if (searchResultsTitle == null || searchResultsRecyclerView == null || noResultsTextView == null) {
                Log.e(TAG, "Các view hiển thị kết quả null: " +
                        "searchResultsTitle=" + (searchResultsTitle == null) +
                        ", searchResultsRecyclerView=" + (searchResultsRecyclerView == null) +
                        ", noResultsTextView=" + (noResultsTextView == null));
                Toast.makeText(this, "Lỗi hiển thị kết quả", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (results.isEmpty()) {
                showNoResults();
                return;
            }
            
            // Hiển thị kết quả
            if (transactionList == null) {
                transactionList = new ArrayList<>();
            }
            
            transactionList.clear();
            transactionList.addAll(results);
            
            if (transactionAdapter == null) {
                transactionAdapter = new TransactionAdapter(this, transactionList);
                searchResultsRecyclerView.setAdapter(transactionAdapter);
                searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            } else {
                transactionAdapter.notifyDataSetChanged();
            }
            
            // Hiển thị kết quả
            searchResultsTitle.setVisibility(View.VISIBLE);
            searchResultsRecyclerView.setVisibility(View.VISIBLE);
            noResultsTextView.setVisibility(View.GONE);
            
            searchResultsTitle.setText("Kết quả tìm kiếm (" + results.size() + ")");
            
            Log.d(TAG, "Đã hiển thị " + results.size() + " kết quả tìm kiếm");
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi cập nhật UI với kết quả: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi hiển thị kết quả: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showNoResults() {
        try {
            if (searchResultsTitle == null || searchResultsRecyclerView == null || noResultsTextView == null) {
                Log.e(TAG, "Các view hiển thị kết quả null khi hiển thị không có kết quả");
                return;
            }
            
            searchResultsTitle.setVisibility(View.VISIBLE);
            searchResultsRecyclerView.setVisibility(View.GONE);
            noResultsTextView.setVisibility(View.VISIBLE);
            
            searchResultsTitle.setText("Kết quả tìm kiếm (0)");
            
            Log.d(TAG, "Đã hiển thị trạng thái không có kết quả");
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi hiển thị trạng thái không có kết quả: " + e.getMessage());
        }
    }

    /**
     * Kiểm tra xem danh mục đã chọn có khớp với danh mục của giao dịch không
     * Xử lý cả trường hợp tên hiển thị (Ăn uống) và mã nội bộ (food)
     */
    private boolean isCategoryMatch(String selectedCategory, String categoryValue) {
        if (selectedCategory == null || categoryValue == null) {
            return false;
        }
        
        // Kiểm tra trùng khớp trực tiếp (không phân biệt hoa thường)
        if (selectedCategory.equalsIgnoreCase(categoryValue)) {
            return true;
        }
        
        // Kiểm tra các cặp tên hiển thị/mã đặc biệt
        switch (selectedCategory.toLowerCase()) {
            case "ăn uống":
                return categoryValue.equalsIgnoreCase("food");
            case "food":
                return categoryValue.equalsIgnoreCase("Ăn uống");
            
            case "di chuyển":
                return categoryValue.equalsIgnoreCase("transport");
            case "transport":
                return categoryValue.equalsIgnoreCase("Di chuyển");
            
            case "mua sắm":
                return categoryValue.equalsIgnoreCase("shopping");
            case "shopping":
                return categoryValue.equalsIgnoreCase("Mua sắm");
            
            case "nhà cửa":
                return categoryValue.equalsIgnoreCase("housing");
            case "housing":
                return categoryValue.equalsIgnoreCase("Nhà cửa");
            
            case "giải trí":
                return categoryValue.equalsIgnoreCase("entertainment");
            case "entertainment":
                return categoryValue.equalsIgnoreCase("Giải trí");
            
            case "giáo dục":
                return categoryValue.equalsIgnoreCase("education");
            case "education":
                return categoryValue.equalsIgnoreCase("Giáo dục");
            
            case "sức khỏe":
                return categoryValue.equalsIgnoreCase("health");
            case "health":
                return categoryValue.equalsIgnoreCase("Sức khỏe");
            
            case "lương":
                return categoryValue.equalsIgnoreCase("salary") || 
                       categoryValue.equalsIgnoreCase("deposit");
            case "salary":
                return categoryValue.equalsIgnoreCase("Lương") || 
                       categoryValue.equalsIgnoreCase("deposit");
            case "deposit":
                return categoryValue.equalsIgnoreCase("Lương") || 
                       categoryValue.equalsIgnoreCase("salary");
            
            case "đầu tư":
                return categoryValue.equalsIgnoreCase("investment");
            case "investment":
                return categoryValue.equalsIgnoreCase("Đầu tư");
            
            case "tiết kiệm":
                return categoryValue.equalsIgnoreCase("savings") || 
                       categoryValue.equalsIgnoreCase("travel") ||
                       categoryValue.equalsIgnoreCase("house") ||
                       categoryValue.equalsIgnoreCase("car") ||
                       categoryValue.equalsIgnoreCase("wedding") ||
                       categoryValue.equalsIgnoreCase("deposit");
            case "savings":
                return categoryValue.equalsIgnoreCase("Tiết kiệm") ||
                       categoryValue.equalsIgnoreCase("travel") ||
                       categoryValue.equalsIgnoreCase("house") ||
                       categoryValue.equalsIgnoreCase("car") ||
                       categoryValue.equalsIgnoreCase("wedding") ||
                       categoryValue.equalsIgnoreCase("deposit");
            case "travel":
            case "du lịch":
                return categoryValue.equalsIgnoreCase("Tiết kiệm") ||
                       categoryValue.equalsIgnoreCase("savings") ||
                       categoryValue.equalsIgnoreCase("travel") ||
                       categoryValue.equalsIgnoreCase("deposit");
            case "house":
            case "mua nhà":
                return categoryValue.equalsIgnoreCase("Tiết kiệm") ||
                       categoryValue.equalsIgnoreCase("savings") ||
                       categoryValue.equalsIgnoreCase("house");
            case "car":
            case "mua xe":
                return categoryValue.equalsIgnoreCase("Tiết kiệm") ||
                       categoryValue.equalsIgnoreCase("savings") ||
                       categoryValue.equalsIgnoreCase("car");
            case "wedding":
            case "đám cưới":
                return categoryValue.equalsIgnoreCase("Tiết kiệm") ||
                       categoryValue.equalsIgnoreCase("savings") ||
                       categoryValue.equalsIgnoreCase("wedding");
        }
        
        return false;
    }
} 