package com.example.qltccn.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.qltccn.R;
import com.example.qltccn.models.Transaction;
import com.example.qltccn.utils.CurrencyUtils;
import com.example.qltccn.utils.FirebaseUtils;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Date;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AnalysisActivity extends AppCompatActivity {

    private static final String TAG = "AnalysisActivity";
    
    // Khai báo các thành phần UI
    private ImageView backButton;
    private TextView titleText, tvTotalBalance, tvIncome, tvExpense;
    private Button tabDaily, tabWeekly;
    private BarChart barChart;
    
    // Footer navigation
    private ImageView homeNav, analysisNav, transactionNav, categoryNav, profileNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

        // Khởi tạo các thành phần UI
        initializeUI();
        
        // Thiết lập sự kiện click cho các nút
        setupClickListeners();
        
        // Khởi tạo dữ liệu biểu đồ (trong thực tế sẽ lấy từ cơ sở dữ liệu)
        setupChartData();
        
        // Tải dữ liệu người dùng
        loadUserData();
    }

    private void initializeUI() {
        // Header
        backButton = findViewById(R.id.backButton);
        titleText = findViewById(R.id.titleText);
        
        // Thêm xử lý nút notification
        View notiContainer = findViewById(R.id.notiContainer);
        if (notiContainer != null) {
            notiContainer.setOnClickListener(v -> navigateToNotification());
        }
        
        // Content
        tvTotalBalance = findViewById(R.id.tv_total_balance);
        tvIncome = findViewById(R.id.tv_income_summary);
        tvExpense = findViewById(R.id.tv_expense_summary);
        tabDaily = findViewById(R.id.tabDaily);
        tabWeekly = findViewById(R.id.tabWeekly);
        barChart = findViewById(R.id.barChart);

        // Footer navigation
        homeNav = findViewById(R.id.iconHome);
        analysisNav = findViewById(R.id.iconAnalysis);
        transactionNav = findViewById(R.id.iconTrans);
        categoryNav = findViewById(R.id.iconCategory);
        profileNav = findViewById(R.id.iconUser);
        
        // Đặt biểu tượng Analysis là đã được chọn
        if (analysisNav != null) {
            analysisNav.setImageResource(R.drawable.ic_analysis1);
        }
    }

    private void setupClickListeners() {
        // Sự kiện click nút quay lại
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
        // Sự kiện click tab
        tabDaily.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Cập nhật UI và dữ liệu khi chọn Daily
                updateTabSelection(tabDaily);
                updateChartForDailyView();
            }
        });
        
        tabWeekly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Cập nhật UI và dữ liệu khi chọn Weekly
                updateTabSelection(tabWeekly);
                updateChartForWeeklyView();
            }
        });
        
        // Nút Monthly
        Button tabMonthly = findViewById(R.id.tabMonthly);
        if (tabMonthly != null) {
            tabMonthly.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Chọn tab Monthly
                    updateTabSelection(tabMonthly);
                    
                    // Lấy thời gian bắt đầu và kết thúc của năm
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.MONTH, 0); // Tháng 1
                    calendar.set(Calendar.DAY_OF_MONTH, 1);
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    
                    long startOfYear = calendar.getTimeInMillis();
                    
                    calendar.set(Calendar.MONTH, 11); // Tháng 12
                    calendar.set(Calendar.DAY_OF_MONTH, 31);
                    calendar.set(Calendar.HOUR_OF_DAY, 23);
                    calendar.set(Calendar.MINUTE, 59);
                    calendar.set(Calendar.SECOND, 59);
                    
                    long endOfYear = calendar.getTimeInMillis();
                    
                    // Lấy ID người dùng
                    String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? 
                                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
                    
                    if (userId != null) {
                        updateChartForMonthlyView(userId, startOfYear, endOfYear);
                    }
                }
            });
        }
        
        // Nút Year
        Button tabYear = findViewById(R.id.tabYear);
        if (tabYear != null) {
            tabYear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Chọn tab Year
                    updateTabSelection(tabYear);
                    
                    // Hiển thị Toast (có thể triển khai biểu đồ năm sau)
                    Toast.makeText(AnalysisActivity.this, "Chức năng đang được phát triển", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Footer navigation listeners
        homeNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AnalysisActivity.this, HomeActivity.class));
                finish();
            }
        });

        analysisNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Đã đang ở màn hình Analysis nên không cần làm gì
            }
        });

        transactionNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AnalysisActivity.this, TranActivity.class));
                finish();
            }
        });

        categoryNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AnalysisActivity.this, CategoryActivity.class));
                finish();
            }
        });

        profileNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AnalysisActivity.this, ProfileActivity.class));
                finish();
            }
        });
    }
    
    // Phương thức để cập nhật giao diện khi chọn tab
    private void updateTabSelection(Button selectedTab) {
        // Reset tất cả tab về trạng thái không được chọn
        tabDaily.setSelected(false);
        tabDaily.setBackgroundResource(android.R.color.transparent);
        tabDaily.setTextColor(getResources().getColor(android.R.color.black));
        
        tabWeekly.setSelected(false);
        tabWeekly.setBackgroundResource(android.R.color.transparent);
        tabWeekly.setTextColor(getResources().getColor(android.R.color.black));
        
        Button tabMonthly = findViewById(R.id.tabMonthly);
        if (tabMonthly != null) {
            tabMonthly.setSelected(false);
            tabMonthly.setBackgroundResource(android.R.color.transparent);
            tabMonthly.setTextColor(getResources().getColor(android.R.color.black));
        }
        
        Button tabYear = findViewById(R.id.tabYear);
        if (tabYear != null) {
            tabYear.setSelected(false);
            tabYear.setBackgroundResource(android.R.color.transparent);
            tabYear.setTextColor(getResources().getColor(android.R.color.black));
        }
        
        // Đặt tab được chọn
        selectedTab.setSelected(true);
        selectedTab.setBackgroundResource(R.drawable.selected_tab_background);
        selectedTab.setTextColor(getResources().getColor(android.R.color.white));
    }
    
    private void setupChartData() {
        // Cấu hình biểu đồ
        configureBarChart();
        
        // Mặc định chọn tab Monthly
        Button tabMonthly = findViewById(R.id.tabMonthly);
        if (tabMonthly != null) {
            updateTabSelection(tabMonthly);
            
            // Lấy thời gian bắt đầu và kết thúc của năm
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.MONTH, 0); // Tháng 1
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            
            long startOfYear = calendar.getTimeInMillis();
            
            calendar.set(Calendar.MONTH, 11); // Tháng 12
            calendar.set(Calendar.DAY_OF_MONTH, 31);
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            
            long endOfYear = calendar.getTimeInMillis();
            
            // Lấy ID người dùng
            String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? 
                        FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
            
            if (userId != null) {
                // Cập nhật biểu đồ
                updateChartForMonthlyView(userId, startOfYear, endOfYear);
            } else {
                // Nếu không có người dùng, chọn tab Daily làm mặc định
                updateTabSelection(tabDaily);
                updateChartForDailyView();
            }
        } else {
            // Nếu không có tab Monthly, chọn tab Daily làm mặc định
            updateTabSelection(tabDaily);
            updateChartForDailyView();
        }
    }
    
    private void configureBarChart() {
        // Cấu hình chung cho biểu đồ
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        barChart.setHighlightFullBarEnabled(false);
        
        // Thiết lập màu nền là DFF7E2
        int colorDFF7E2 = android.graphics.Color.parseColor("#DFF7E2");
        barChart.setBackgroundColor(colorDFF7E2);
        
        // Cấu hình trục X
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextSize(12f);
        
        // Cấu hình trục Y bên trái
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setSpaceTop(35f);
        leftAxis.setAxisMinimum(0f);
        
        // Vô hiệu hóa trục Y bên phải
        barChart.getAxisRight().setEnabled(false);
        
        // Vô hiệu hóa zoom
        barChart.setScaleEnabled(false);
        
        // Hiển thị legend
        barChart.getLegend().setEnabled(true);
    }
    
    private void updateChartForDailyView() {
        // Đánh dấu nút Daily được chọn
        tabDaily.setSelected(true);
        tabWeekly.setSelected(false);
        
        // Lấy ngày bắt đầu và kết thúc của tuần hiện tại
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        long startOfWeek = calendar.getTimeInMillis();
        
        calendar.add(Calendar.DAY_OF_WEEK, 6);
        long endOfWeek = calendar.getTimeInMillis();
        
        // Lấy ID người dùng hiện tại
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? 
                        FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
                        
        if (userId == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Gọi phương thức mới với tham số userId, startOfWeek, endOfWeek
        updateChartForDailyView(userId, startOfWeek, endOfWeek);
    }
    
    private void updateChartForWeeklyView() {
        // Đánh dấu nút Weekly được chọn
        tabDaily.setSelected(false);
        tabWeekly.setSelected(true);
        
        // Lấy ngày bắt đầu và kết thúc của tháng hiện tại
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfMonth = calendar.getTimeInMillis();
        
        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        long endOfMonth = calendar.getTimeInMillis();
        
        // Lấy ID người dùng hiện tại
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? 
                         FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
                         
        if (userId == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Gọi phương thức để cập nhật biểu đồ theo tháng
        updateChartForMonthlyView(userId, startOfMonth, endOfMonth);
    }

    private void loadUserData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            
            // Tải thông tin từ Firestore
            FirebaseFirestore.getInstance().collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Double balance = documentSnapshot.getDouble("balance");
                        if (balance != null && tvTotalBalance != null) {
                            tvTotalBalance.setText(CurrencyUtils.formatVND(balance));
                            Log.d(TAG, "Đã tải số dư từ document user: " + balance);
                        } else {
                            Log.d(TAG, "Số dư không tồn tại hoặc là null, sẽ tính từ giao dịch");
                        }
                    } else {
                        Log.d(TAG, "Document user không tồn tại, sẽ tính số dư từ giao dịch");
                    }
                    
                    // Luôn tính toán thu chi từ giao dịch thực tế
                    calculateIncomeAndExpense(userId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi tải dữ liệu người dùng: " + e.getMessage());
                    
                    // Vẫn cố gắng tải dữ liệu giao dịch
                    calculateIncomeAndExpense(userId);
                });
        } else {
            Log.w(TAG, "Không có người dùng đăng nhập");
            
            // Hiển thị thông báo và đặt giá trị mặc định
            Toast.makeText(this, "Vui lòng đăng nhập để xem phân tích", Toast.LENGTH_SHORT).show();
            
            if (tvIncome != null) {
                tvIncome.setText(CurrencyUtils.formatVND(0));
            }
            if (tvExpense != null) {
                tvExpense.setText("-" + CurrencyUtils.formatVND(0));
            }
            if (tvTotalBalance != null) {
                tvTotalBalance.setText(CurrencyUtils.formatVND(0));
            }
        }
    }
    
    private void calculateIncomeAndExpense(String userId) {
        Log.d(TAG, "Tính toán tổng thu/chi cho người dùng: " + userId);
        
        // Truy cập vào subcollection transactions của người dùng
        FirebaseFirestore.getInstance().collection("users").document(userId).collection("transactions")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                Log.d(TAG, "Đã tìm thấy " + queryDocumentSnapshots.size() + " giao dịch của người dùng");
                
                double totalIncome = 0;
                double totalExpense = 0;
                
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    try {
                        String type = doc.getString("type");
                        Double amount = doc.getDouble("amount");
                        
                        // Ghi log từng giao dịch để debug
                        Log.d(TAG, "Giao dịch ID=" + doc.getId() + 
                              ", loại=" + type + 
                              ", số tiền=" + amount);
                        
                        if (amount != null) {
                            if ("income".equals(type)) {
                                totalIncome += amount;
                                Log.d(TAG, "Cộng vào thu nhập: " + amount + ", total = " + totalIncome);
                            } else if ("expense".equals(type)) {
                                totalExpense += amount;
                                Log.d(TAG, "Cộng vào chi tiêu: " + amount + ", total = " + totalExpense);
                            } else {
                                Log.w(TAG, "Loại giao dịch không xác định: " + type);
                            }
                        } else {
                            Log.w(TAG, "Giao dịch không có số tiền: " + doc.getId());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Lỗi khi xử lý giao dịch: " + doc.getId() + ", lỗi: " + e.getMessage());
                    }
                }
                
                // Ghi log tổng thu/chi để kiểm tra
                Log.d(TAG, "Tổng thu nhập: " + totalIncome);
                Log.d(TAG, "Tổng chi tiêu: " + totalExpense);
                
                // Cập nhật UI
                if (tvIncome != null) {
                    tvIncome.setText(CurrencyUtils.formatVND(totalIncome));
                    Log.d(TAG, "Cập nhật UI thu nhập: " + CurrencyUtils.formatVND(totalIncome));
                }
                
                if (tvExpense != null) {
                    tvExpense.setText("-" + CurrencyUtils.formatVND(totalExpense));
                    Log.d(TAG, "Cập nhật UI chi tiêu: " + CurrencyUtils.formatVND(totalExpense));
                }
                
                // Cập nhật số dư nếu cần
                if (tvTotalBalance != null) {
                    double balance = totalIncome - totalExpense;
                    tvTotalBalance.setText(CurrencyUtils.formatVND(balance));
                    Log.d(TAG, "Cập nhật UI số dư: " + CurrencyUtils.formatVND(balance));
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Lỗi khi tính toán thu chi: " + e.getMessage(), e);
                
                // Hiển thị thông báo lỗi cho người dùng
                Toast.makeText(AnalysisActivity.this, 
                    "Không thể tải dữ liệu giao dịch: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
                
                // Set giá trị mặc định để tránh hiển thị rỗng
                if (tvIncome != null) tvIncome.setText(CurrencyUtils.formatVND(0));
                if (tvExpense != null) tvExpense.setText("-" + CurrencyUtils.formatVND(0));
                if (tvTotalBalance != null) tvTotalBalance.setText(CurrencyUtils.formatVND(0));
            });
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

    private void updateChartForDailyView(String userId, long startTime, long endTime) {
        // Thiết lập các khoảng thời gian cho biểu đồ hàng ngày
        long rangeInMillis = endTime - startTime;
        int numberOfDays = (int) (rangeInMillis / (24 * 60 * 60 * 1000)) + 1;
        
        // Danh sách các mục nhập (entries) cho thu nhập và chi tiêu
        ArrayList<Entry> incomeEntries = new ArrayList<>();
        ArrayList<Entry> expenseEntries = new ArrayList<>();
        
        // Danh sách ngày
        ArrayList<String> xAxisLabels = new ArrayList<>();
        
        // Định dạng ngày
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
        
        // Khởi tạo dữ liệu
        for (int i = 0; i < numberOfDays; i++) {
            xAxisLabels.add(sdf.format(new Date(startTime + i * 24 * 60 * 60 * 1000)));
            incomeEntries.add(new Entry(i, 0));
            expenseEntries.add(new Entry(i, 0));
        }
        
        // Truy cập subcollection transactions của người dùng
        FirebaseFirestore.getInstance().collection("users").document(userId).collection("transactions")
            .whereGreaterThanOrEqualTo("date", startTime)
            .whereLessThanOrEqualTo("date", endTime)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                Log.d(TAG, "Đã tìm thấy " + queryDocumentSnapshots.size() + " giao dịch trong khoảng thời gian");
                
                // Đếm số lượng giao dịch chi tiêu và thu nhập
                int expenseCount = 0;
                int incomeCount = 0;
                float totalDailyIncome = 0;
                float totalDailyExpense = 0;
                
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    try {
                        String type = doc.getString("type");
                        Double amount = doc.getDouble("amount");
                        Long dateValue = doc.getLong("date");
                        
                        // Log để debug dữ liệu giao dịch
                        Log.d(TAG, "Giao dịch: ID=" + doc.getId() + 
                              ", loại=" + type + 
                              ", số tiền=" + amount + 
                              ", ngày=" + (dateValue != null ? new Date(dateValue) : "null"));
                        
                        if (type != null && amount != null && dateValue != null) {
                            // Xác định chỉ số của ngày trong mảng
                            int dayIndex = (int) ((dateValue - startTime) / (24 * 60 * 60 * 1000));
                            
                            if (dayIndex >= 0 && dayIndex < numberOfDays) {
                                if ("income".equals(type)) {
                                    // Cập nhật giá trị thu nhập
                                    float currentValue = incomeEntries.get(dayIndex).getY();
                                    incomeEntries.set(dayIndex, new Entry(dayIndex, currentValue + amount.floatValue()));
                                    totalDailyIncome += amount.floatValue();
                                    incomeCount++;
                                    Log.d(TAG, "Thêm thu nhập: " + amount + " vào ngày " + dayIndex);
                                } else if ("expense".equals(type)) {
                                    // Cập nhật giá trị chi tiêu
                                    float currentValue = expenseEntries.get(dayIndex).getY();
                                    expenseEntries.set(dayIndex, new Entry(dayIndex, currentValue + amount.floatValue()));
                                    totalDailyExpense += amount.floatValue();
                                    expenseCount++;
                                    Log.d(TAG, "Thêm chi tiêu: " + amount + " vào ngày " + dayIndex);
                                } else {
                                    Log.d(TAG, "Loại giao dịch không xác định: " + type);
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Lỗi khi xử lý giao dịch: " + e.getMessage());
                    }
                }
                
                Log.d(TAG, "Tổng số giao dịch thu nhập hàng ngày: " + incomeCount + ", tổng: " + totalDailyIncome);
                Log.d(TAG, "Tổng số giao dịch chi tiêu hàng ngày: " + expenseCount + ", tổng: " + totalDailyExpense);
                
                // Cập nhật biểu đồ
                updateLineChart(incomeEntries, expenseEntries, xAxisLabels);
                
                // Cập nhật tổng thu nhập/chi tiêu UI
                if (tvIncome != null) {
                    tvIncome.setText(CurrencyUtils.formatVND(totalDailyIncome));
                }
                
                if (tvExpense != null) {
                    tvExpense.setText("-" + CurrencyUtils.formatVND(totalDailyExpense));
                }
                
                if (tvTotalBalance != null) {
                    tvTotalBalance.setText(CurrencyUtils.formatVND(totalDailyIncome - totalDailyExpense));
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Lỗi khi truy vấn giao dịch cho biểu đồ hàng ngày: " + e.getMessage());
                
                // Set giá trị mặc định để tránh hiển thị rỗng
                if (tvIncome != null) tvIncome.setText(CurrencyUtils.formatVND(0));
                if (tvExpense != null) tvExpense.setText("-" + CurrencyUtils.formatVND(0));
                if (tvTotalBalance != null) tvTotalBalance.setText(CurrencyUtils.formatVND(0));
            });
    }

    private void updateChartForMonthlyView(String userId, long startTime, long endTime) {
        // Thiết lập các khoảng thời gian cho biểu đồ hàng tháng
        ArrayList<Entry> incomeEntries = new ArrayList<>();
        ArrayList<Entry> expenseEntries = new ArrayList<>();
        ArrayList<String> xAxisLabels = new ArrayList<>();
        
        // Danh sách tên tháng
        String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        
        // Khởi tạo dữ liệu
        for (int i = 0; i < 12; i++) {
            xAxisLabels.add(monthNames[i]);
            incomeEntries.add(new Entry(i, 0));
            expenseEntries.add(new Entry(i, 0));
        }
        
        Log.d(TAG, "Bắt đầu truy vấn giao dịch từ " + new Date(startTime) + " đến " + new Date(endTime));
        
        // Truy cập subcollection transactions của người dùng
        FirebaseFirestore.getInstance().collection("users").document(userId).collection("transactions")
            .whereGreaterThanOrEqualTo("date", startTime)
            .whereLessThanOrEqualTo("date", endTime)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                Log.d(TAG, "Đã tìm thấy " + queryDocumentSnapshots.size() + " giao dịch trong năm");
                
                // Tạo mảng để lưu tổng thu nhập và chi tiêu theo tháng
                float[] monthlyIncome = new float[12];
                float[] monthlyExpense = new float[12];
                
                // Đếm số lượng giao dịch chi tiêu và thu nhập
                int expenseCount = 0;
                int incomeCount = 0;
                
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    try {
                        String type = doc.getString("type");
                        Double amount = doc.getDouble("amount");
                        Long dateValue = doc.getLong("date");
                        
                        // Log để debug dữ liệu giao dịch
                        Log.d(TAG, "Giao dịch ID=" + doc.getId() + 
                              ", loại=" + type + 
                              ", số tiền=" + amount + 
                              ", ngày=" + (dateValue != null ? new Date(dateValue) : "null"));
                        
                        if (type != null && amount != null && dateValue != null) {
                            // Xác định tháng
                            Calendar transactionCal = Calendar.getInstance();
                            transactionCal.setTimeInMillis(dateValue);
                            
                            int monthIndex = transactionCal.get(Calendar.MONTH);
                            
                            if (monthIndex >= 0 && monthIndex < 12) {
                                if ("income".equals(type)) {
                                    // Cập nhật giá trị thu nhập
                                    monthlyIncome[monthIndex] += amount.floatValue();
                                    Log.d(TAG, "Thêm thu nhập: " + amount + " vào tháng " + monthNames[monthIndex] + 
                                          ", Mới = " + monthlyIncome[monthIndex]);
                                    incomeCount++;
                                } else if ("expense".equals(type)) {
                                    // Cập nhật giá trị chi tiêu
                                    monthlyExpense[monthIndex] += amount.floatValue();
                                    Log.d(TAG, "Thêm chi tiêu: " + amount + " vào tháng " + monthNames[monthIndex] + 
                                          ", Mới = " + monthlyExpense[monthIndex]);
                                    expenseCount++;
                                } else {
                                    Log.d(TAG, "Loại giao dịch không xác định: " + type);
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Lỗi khi xử lý giao dịch: " + e.getMessage(), e);
                    }
                }
                
                // Cập nhật entries từ dữ liệu đã tính toán
                for (int i = 0; i < 12; i++) {
                    incomeEntries.set(i, new Entry(i, monthlyIncome[i]));
                    expenseEntries.set(i, new Entry(i, monthlyExpense[i]));
                }
                
                // Log thông tin tổng hợp
                Log.d(TAG, "Tổng số giao dịch thu nhập: " + incomeCount);
                Log.d(TAG, "Tổng số giao dịch chi tiêu: " + expenseCount);
                
                // Ghi log chi tiết các mục dữ liệu thu nhập/chi tiêu
                for (int i = 0; i < 12; i++) {
                    Log.d(TAG, "Tháng " + monthNames[i] + ": Thu nhập = " + monthlyIncome[i] + ", Chi tiêu = " + monthlyExpense[i]);
                }
                
                // Cập nhật biểu đồ
                updateLineChart(incomeEntries, expenseEntries, xAxisLabels);
                
                // Tính và cập nhật tổng thu nhập/chi tiêu
                float totalIncome = 0;
                float totalExpense = 0;
                
                for (int i = 0; i < 12; i++) {
                    totalIncome += monthlyIncome[i];
                    totalExpense += monthlyExpense[i];
                }
                
                // Cập nhật giao diện tổng thu nhập/chi tiêu
                if (tvIncome != null) {
                    tvIncome.setText(CurrencyUtils.formatVND(totalIncome));
                    Log.d(TAG, "Cập nhật UI tổng thu nhập: " + CurrencyUtils.formatVND(totalIncome));
                }
                
                if (tvExpense != null) {
                    tvExpense.setText("-" + CurrencyUtils.formatVND(totalExpense));
                    Log.d(TAG, "Cập nhật UI tổng chi tiêu: " + CurrencyUtils.formatVND(totalExpense));
                }
                
                if (tvTotalBalance != null) {
                    tvTotalBalance.setText(CurrencyUtils.formatVND(totalIncome - totalExpense));
                    Log.d(TAG, "Cập nhật UI số dư: " + CurrencyUtils.formatVND(totalIncome - totalExpense));
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Lỗi khi truy vấn giao dịch cho biểu đồ hàng tháng: " + e.getMessage(), e);
                Toast.makeText(AnalysisActivity.this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                
                // Set giá trị mặc định để tránh hiển thị rỗng
                if (tvIncome != null) tvIncome.setText(CurrencyUtils.formatVND(0));
                if (tvExpense != null) tvExpense.setText("-" + CurrencyUtils.formatVND(0));
                if (tvTotalBalance != null) tvTotalBalance.setText(CurrencyUtils.formatVND(0));
            });
    }

    /**
     * Cập nhật biểu đồ với dữ liệu thu nhập và chi tiêu
     * 
     * @param incomeEntries Danh sách các mục nhập thu nhập
     * @param expenseEntries Danh sách các mục nhập chi tiêu
     * @param xAxisLabels Danh sách nhãn trục x
     */
    private void updateLineChart(ArrayList<Entry> incomeEntries, ArrayList<Entry> expenseEntries, ArrayList<String> xAxisLabels) {
        try {
            // Kiểm tra và ghi log dữ liệu chi tiêu
            Log.d(TAG, "====== DEBUG DỮ LIỆU CHI TIÊU ======");
            boolean hasExpenseData = false;
            float maxExpense = 0;
            float totalExpense = 0;
            for (int i = 0; i < expenseEntries.size(); i++) {
                float value = expenseEntries.get(i).getY();
                Log.d(TAG, "Chi tiêu tại " + i + ": " + value);
                totalExpense += value;
                if (value > 0) {
                    hasExpenseData = true;
                    if (value > maxExpense) maxExpense = value;
                }
            }
            Log.d(TAG, "Có dữ liệu chi tiêu: " + hasExpenseData + ", Giá trị cao nhất: " + maxExpense + ", Tổng: " + totalExpense);
            
            // Kiểm tra và ghi log dữ liệu thu nhập
            float totalIncome = 0;
            for (int i = 0; i < incomeEntries.size(); i++) {
                float value = incomeEntries.get(i).getY();
                totalIncome += value;
                Log.d(TAG, "Thu nhập tại " + i + ": " + value);
            }
            Log.d(TAG, "Tổng thu nhập biểu đồ: " + totalIncome);
            
            // Thiết lập nhãn cho trục X
            XAxis xAxis = barChart.getXAxis();
            xAxis.setValueFormatter(new IndexAxisValueFormatter(xAxisLabels));
            xAxis.setGranularity(1f);
            xAxis.setLabelRotationAngle(0); // Không xoay nhãn
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setDrawGridLines(false);
            xAxis.setTextSize(10f);
            xAxis.setTextColor(getResources().getColor(android.R.color.darker_gray));
            
            // Cấu hình trục Y bên trái
            YAxis leftAxis = barChart.getAxisLeft();
            leftAxis.setDrawGridLines(true);
            leftAxis.setAxisMinimum(0f);
            leftAxis.setTextColor(getResources().getColor(android.R.color.darker_gray));
            leftAxis.setTextSize(10f);
            
            // Vô hiệu hóa trục Y bên phải
            barChart.getAxisRight().setEnabled(false);
            
            // Tùy chỉnh khác cho biểu đồ
            barChart.setDrawGridBackground(false);
            barChart.getDescription().setEnabled(false);
            barChart.setDrawBorders(false);
            barChart.setExtraBottomOffset(10f);
            barChart.setExtraTopOffset(10f);
            
            // Thay đổi màu nền của biểu đồ thành DFF7E2
            int colorDFF7E2 = android.graphics.Color.parseColor("#DFF7E2");
            barChart.setBackgroundColor(colorDFF7E2);
            
            // Tạo BarData từ Entry
            ArrayList<BarEntry> incomeBarEntries = new ArrayList<>();
            ArrayList<BarEntry> expenseBarEntries = new ArrayList<>();
            
            for (int i = 0; i < incomeEntries.size(); i++) {
                incomeBarEntries.add(new BarEntry(i, incomeEntries.get(i).getY()));
                expenseBarEntries.add(new BarEntry(i, expenseEntries.get(i).getY()));
                Log.d(TAG, "Dữ liệu biểu đồ " + i + " - Thu nhập: " + incomeEntries.get(i).getY() + ", Chi tiêu: " + expenseEntries.get(i).getY());
            }
            
            // Tạo dataset thu nhập với màu xanh lá cây
            BarDataSet incomeBarDataSet = new BarDataSet(incomeBarEntries, "Thu nhập");
            incomeBarDataSet.setColor(getResources().getColor(R.color.green_500));
            incomeBarDataSet.setValueTextSize(0f); // Không hiển thị giá trị trên cột
            incomeBarDataSet.setDrawValues(false);
            
            // Tạo dataset chi tiêu với màu đỏ để dễ phân biệt
            BarDataSet expenseBarDataSet = new BarDataSet(expenseBarEntries, "Chi tiêu");
            expenseBarDataSet.setColor(getResources().getColor(R.color.red));
            expenseBarDataSet.setValueTextSize(0f); // Không hiển thị giá trị trên cột
            expenseBarDataSet.setDrawValues(false);
            
            // Đảm bảo hiển thị cả hai bộ dữ liệu
            BarData barData = new BarData(incomeBarDataSet, expenseBarDataSet);
            
            // Kiểm tra xem BarData có cả hai DataSet không
            Log.d(TAG, "Số bộ dữ liệu trong BarData: " + barData.getDataSetCount());
            for (int i = 0; i < barData.getDataSetCount(); i++) {
                IBarDataSet dataSet = barData.getDataSetByIndex(i);
                Log.d(TAG, "DataSet " + i + ": " + dataSet.getLabel() + ", Số lượng entry: " + dataSet.getEntryCount());
            }
            
            // Thiết lập các thuộc tính của barData
            float groupSpace = 0.3f; // Khoảng cách giữa các nhóm
            float barSpace = 0.05f; // Khoảng cách giữa các cột trong nhóm
            float barWidth = 0.3f; // Độ rộng của cột
            
            barData.setBarWidth(barWidth);
            barData.groupBars(0f, groupSpace, barSpace);
            
            // Cập nhật dữ liệu cho biểu đồ
            barChart.setData(barData);
            
            // Cập nhật legend (chú thích)
            barChart.getLegend().setEnabled(true);
            barChart.getLegend().setTextSize(12f);
            barChart.getLegend().setForm(Legend.LegendForm.CIRCLE);
            barChart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
            
            // Hiệu ứng animation khi hiển thị biểu đồ
            barChart.animateY(1000);
            
            // Cập nhật biểu đồ
            barChart.invalidate();
            
            Log.d(TAG, "Đã cập nhật biểu đồ thành công với " + incomeEntries.size() + " mục dữ liệu");
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi cập nhật biểu đồ: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi khi cập nhật biểu đồ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
} 