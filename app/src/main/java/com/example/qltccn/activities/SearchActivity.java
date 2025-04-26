package com.example.qltccn.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.qltccn.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity {

    // UI elements
    private ImageButton toolbarBackBtn;
    private ImageView idNoti;
    private ImageView iconHome, iconAnalysis, iconTrans, iconCategory, iconUser;
    
    private EditText searchEt;
    private SwitchCompat switchIncome, switchExpense;
    private Button btnSearch;

    // Search parameters
    private Calendar selectedDate;
    private String selectedCategory = "";
    private boolean searchIncome = false;
    private boolean searchExpense = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Initialize calendar
        selectedDate = Calendar.getInstance();

        // Initialize UI elements
        initUI();

        // Set click listeners
        setClickListeners();
    }

    private void initUI() {
        // Toolbar
        toolbarBackBtn = findViewById(R.id.toolbarBackBtn);
        idNoti = findViewById(R.id.idNoti);

        // Search components
        searchEt = findViewById(R.id.searchEt);
        switchIncome = findViewById(R.id.switchIncome);
        switchExpense = findViewById(R.id.switchExpense);
        btnSearch = findViewById(R.id.btnSearch);

        // Footer icons
        iconHome = findViewById(R.id.iconHome);
        iconAnalysis = findViewById(R.id.iconAnalysis);
        iconTrans = findViewById(R.id.iconTrans);
        iconCategory = findViewById(R.id.iconCategory);
        iconUser = findViewById(R.id.iconUser);
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
        
        // Toggle switches
        switchIncome.setOnCheckedChangeListener((buttonView, isChecked) -> searchIncome = isChecked);
        switchExpense.setOnCheckedChangeListener((buttonView, isChecked) -> searchExpense = isChecked);
        
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

    private void performSearch() {
        String searchQuery = searchEt.getText().toString().trim();
        
        if (!searchIncome && !searchExpense) {
            Toast.makeText(this, "Please select at least one type: Income or Expense", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // TODO: Implement the search logic
        // This could query a database and display results in a new activity or fragment
        
        Toast.makeText(this, "Searching for: " + searchQuery, Toast.LENGTH_SHORT).show();
    }
} 