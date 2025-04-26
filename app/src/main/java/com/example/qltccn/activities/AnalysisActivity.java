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
        updateTabSelection(tabWeekly);
        
        // Lấy ngày bắt đầu và kết thúc của tuần hiện tại
        Calendar calendar = Calendar.getInstance();
        
        // Đặt về đầu ngày hiện tại
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        
        // Tìm ngày đầu tuần (thứ 2)
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        }
        long startOfWeek = calendar.getTimeInMillis();
        
        // Tìm ngày cuối tuần (Chủ nhật)
        calendar.add(Calendar.DAY_OF_WEEK, 6);
        long endOfWeek = calendar.getTimeInMillis();
        
        // Lấy ID người dùng hiện tại
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? 
                         FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
                         
        if (userId == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }
        
        updateChartForWeeklyView(userId, startOfWeek, endOfWeek);
    }

    private void updateChartForWeeklyView(String userId, long startTime, long endTime) {
        // Thiết lập các khoảng thời gian cho biểu đồ theo tuần (7 ngày)
        ArrayList<Entry> incomeEntries = new ArrayList<>();
        ArrayList<Entry> expenseEntries = new ArrayList<>();
        ArrayList<String> xAxisLabels = new ArrayList<>();
        
        // Danh sách các ngày trong tuần
        String[] dayNames = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
        
        // Khởi tạo dữ liệu rỗng cho 7 ngày trong tuần
        for (int i = 0; i < 7; i++) {
            xAxisLabels.add(dayNames[i]);
            incomeEntries.add(new Entry(i, 0));
            expenseEntries.add(new Entry(i, 0));
        }
        
        Log.d(TAG, "Bắt đầu truy vấn giao dịch từ " + new Date(startTime) + " đến " + new Date(endTime));
        
        // Truy cập subcollection transactions của người dùng
        FirebaseFirestore.getInstance().collection("users").document(userId).collection("transactions")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                Log.d(TAG, "Đã tìm thấy " + queryDocumentSnapshots.size() + " giao dịch tổng cộng");
                
                // Tạo mảng để lưu tổng thu nhập và chi tiêu theo ngày trong tuần
                float[] dailyIncome = new float[7];
                float[] dailyExpense = new float[7];
                
                // Đếm số lượng giao dịch chi tiêu và thu nhập
                int expenseCount = 0;
                int incomeCount = 0;
                float totalWeeklyIncome = 0;
                float totalWeeklyExpense = 0;
                
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    try {
                        String type = doc.getString("type");
                        Double amount = doc.getDouble("amount");
                        
                        // Xử lý trường date có thể ở nhiều định dạng khác nhau
                        Long date = null;
                        
                        // Kiểm tra nếu date là kiểu Timestamp của Firestore
                        if (doc.get("date") instanceof com.google.firebase.Timestamp) {
                            com.google.firebase.Timestamp timestamp = doc.getTimestamp("date");
                            if (timestamp != null) {
                                date = timestamp.toDate().getTime();
                            }
                        } 
                        // Kiểm tra nếu date là String (định dạng ngày)
                        else if (doc.get("date") instanceof String) {
                            String dateStr = doc.getString("date");
                            try {
                                SimpleDateFormat sdfParse = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                Date parsedDate = sdfParse.parse(dateStr);
                                if (parsedDate != null) {
                                    date = parsedDate.getTime();
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Lỗi khi chuyển đổi chuỗi ngày: " + e.getMessage());
                            }
                        }
                        // Thử lấy trực tiếp từ Long
                        else {
                            try {
                                date = doc.getLong("date");
                            } catch (Exception e) {
                                Log.e(TAG, "Không thể lấy date dưới dạng Long: " + e.getMessage());
                            }
                        }
                        
                        if (type == null || amount == null || date == null) {
                            Log.d(TAG, "Bỏ qua giao dịch không hợp lệ: ID=" + doc.getId() + 
                                 ", loại=" + type + ", số tiền=" + amount + 
                                 ", date type=" + (doc.get("date") != null ? doc.get("date").getClass().getSimpleName() : "null"));
                            continue; // Bỏ qua giao dịch không hợp lệ
                        }
                        
                        // Kiểm tra xem giao dịch có nằm trong khoảng thời gian tuần này không
                        if (date < startTime || date > endTime) {
                            continue; // Bỏ qua nếu không thuộc khoảng thời gian
                        }
                        
                        // Xác định ngày trong tuần (thứ 2: index 0, thứ 3: index 1, ...)
                        Calendar transactionCal = Calendar.getInstance();
                        transactionCal.setTimeInMillis(date);
                        
                        // Chuyển đổi từ Calendar.DAY_OF_WEEK sang index của mảng
                        // Lưu ý: Calendar.MONDAY = 2, TUESDAY = 3, ..., SUNDAY = 1
                        int dayOfWeek = transactionCal.get(Calendar.DAY_OF_WEEK);
                        int dayIndex;
                        
                        // Chuyển đổi sang index 0-6, với 0 là thứ 2 và 6 là chủ nhật
                        if (dayOfWeek == Calendar.SUNDAY) {
                            dayIndex = 6; // Chủ nhật là ngày cuối tuần (index 6)
                        } else {
                            dayIndex = dayOfWeek - Calendar.MONDAY; // Thứ 2 (index 0) đến thứ 7 (index 5)
                        }
                        
                        if (dayIndex >= 0 && dayIndex < 7) {
                            if ("income".equals(type)) {
                                dailyIncome[dayIndex] += amount.floatValue();
                                totalWeeklyIncome += amount.floatValue();
                                incomeCount++;
                                Log.d(TAG, "Giao dịch thu nhập vào " + dayNames[dayIndex] + ": " + amount);
                            } else if ("expense".equals(type) || "savings".equals(type)) {
                                dailyExpense[dayIndex] += amount.floatValue();
                                totalWeeklyExpense += amount.floatValue();
                                expenseCount++;
                                Log.d(TAG, "Giao dịch " + type + " vào " + dayNames[dayIndex] + ": " + amount);
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Lỗi khi xử lý giao dịch: " + e.getMessage() + ", ID=" + doc.getId());
                    }
                }
                
                // Cập nhật entries từ dữ liệu đã tính toán
                for (int i = 0; i < 7; i++) {
                    incomeEntries.set(i, new Entry(i, dailyIncome[i]));
                    expenseEntries.set(i, new Entry(i, dailyExpense[i]));
                    Log.d(TAG, "Dữ liệu " + dayNames[i] + ": Thu nhập=" + dailyIncome[i] + ", Chi tiêu=" + dailyExpense[i]);
                }
                
                // Log thông tin tổng hợp
                Log.d(TAG, "Tổng số giao dịch thu nhập tuần này: " + incomeCount + ", tổng: " + totalWeeklyIncome);
                Log.d(TAG, "Tổng số giao dịch chi tiêu tuần này: " + expenseCount + ", tổng: " + totalWeeklyExpense);
                
                // Cập nhật biểu đồ
                updateLineChart(incomeEntries, expenseEntries, xAxisLabels);
                
                // Cập nhật giao diện tổng thu nhập/chi tiêu
                if (tvIncome != null) {
                    tvIncome.setText(CurrencyUtils.formatVND(totalWeeklyIncome));
                    Log.d(TAG, "Cập nhật UI tổng thu nhập tuần: " + CurrencyUtils.formatVND(totalWeeklyIncome));
                }
                
                if (tvExpense != null) {
                    tvExpense.setText("-" + CurrencyUtils.formatVND(totalWeeklyExpense));
                    Log.d(TAG, "Cập nhật UI tổng chi tiêu và tiết kiệm tuần: " + CurrencyUtils.formatVND(totalWeeklyExpense));
                }
                
                if (tvTotalBalance != null) {
                    tvTotalBalance.setText(CurrencyUtils.formatVND(totalWeeklyIncome - totalWeeklyExpense));
                    Log.d(TAG, "Cập nhật UI số dư tuần: " + CurrencyUtils.formatVND(totalWeeklyIncome - totalWeeklyExpense));
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Lỗi khi truy vấn giao dịch cho biểu đồ hàng tuần: " + e.getMessage(), e);
                Toast.makeText(AnalysisActivity.this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                
                // Set giá trị mặc định để tránh hiển thị rỗng
                if (tvIncome != null) tvIncome.setText(CurrencyUtils.formatVND(0));
                if (tvExpense != null) tvExpense.setText("-" + CurrencyUtils.formatVND(0));
                if (tvTotalBalance != null) tvTotalBalance.setText(CurrencyUtils.formatVND(0));
                
                // Hiển thị biểu đồ trống
                ArrayList<Entry> emptyIncomeEntries = new ArrayList<>();
                ArrayList<Entry> emptyExpenseEntries = new ArrayList<>();
                for (int i = 0; i < 7; i++) {
                    emptyIncomeEntries.add(new Entry(i, 0));
                    emptyExpenseEntries.add(new Entry(i, 0));
                }
                updateLineChart(emptyIncomeEntries, emptyExpenseEntries, xAxisLabels);
            });
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
                double totalSavings = 0;
                
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    try {
                        String type = doc.getString("type");
                        Double amount = doc.getDouble("amount");
                        
                        // Ghi log từng giao dịch để debug
                        Log.d(TAG, "Giao dịch ID=" + doc.getId() + 
                              ", loại=" + type + 
                              ", số tiền=" + amount);
                        
                        if (type != null && amount != null) {
                            if ("income".equals(type)) {
                                totalIncome += amount;
                                Log.d(TAG, "Cộng vào thu nhập: " + amount + ", total = " + totalIncome);
                            } else if ("expense".equals(type)) {
                                totalExpense += amount;
                                Log.d(TAG, "Cộng vào chi tiêu: " + amount + ", total = " + totalExpense);
                            } else if ("savings".equals(type)) {
                                totalSavings += amount;
                                Log.d(TAG, "Cộng vào tiết kiệm: " + amount + ", total = " + totalSavings);
                            } else {
                                Log.w(TAG, "Loại giao dịch không xác định: " + type);
                            }
                        } else {
                            Log.w(TAG, "Giao dịch không có thông tin đầy đủ: " + doc.getId());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Lỗi khi xử lý giao dịch: " + doc.getId() + ", lỗi: " + e.getMessage());
                    }
                }
                
                // Tính tổng chi tiêu (bao gồm cả chi tiêu và tiết kiệm)
                double totalExpensesAndSavings = totalExpense + totalSavings;
                
                // Ghi log tổng thu/chi để kiểm tra
                Log.d(TAG, "Tổng thu nhập: " + totalIncome);
                Log.d(TAG, "Tổng chi tiêu: " + totalExpense);
                Log.d(TAG, "Tổng tiết kiệm: " + totalSavings);
                Log.d(TAG, "Tổng chi tiêu và tiết kiệm: " + totalExpensesAndSavings);
                
                // Cập nhật UI
                if (tvIncome != null) {
                    tvIncome.setText(CurrencyUtils.formatVND(totalIncome));
                    Log.d(TAG, "Cập nhật UI thu nhập: " + CurrencyUtils.formatVND(totalIncome));
                }
                
                if (tvExpense != null) {
                    tvExpense.setText("-" + CurrencyUtils.formatVND(totalExpensesAndSavings));
                    Log.d(TAG, "Cập nhật UI chi tiêu: " + CurrencyUtils.formatVND(totalExpensesAndSavings));
                }
                
                // Cập nhật số dư nếu cần
                if (tvTotalBalance != null) {
                    double balance = totalIncome - totalExpensesAndSavings;
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
        
        // Khởi tạo danh sách ngày và entries trống
        for (int i = 0; i < numberOfDays; i++) {
            long currentDayTime = startTime + i * 24 * 60 * 60 * 1000;
            xAxisLabels.add(sdf.format(new Date(currentDayTime)));
            incomeEntries.add(new Entry(i, 0));
            expenseEntries.add(new Entry(i, 0));
        }
        
        // Truy cập subcollection transactions của người dùng
        FirebaseFirestore.getInstance().collection("users").document(userId).collection("transactions")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                Log.d(TAG, "Đã tìm thấy " + queryDocumentSnapshots.size() + " giao dịch tổng cộng");
                
                // Khởi tạo biến đếm và tổng
                int expenseCount = 0;
                int incomeCount = 0;
                float totalDailyIncome = 0;
                float totalDailyExpense = 0;
                
                // Mảng tạm để lưu trữ giá trị thu chi theo ngày
                float[] dailyIncome = new float[numberOfDays];
                float[] dailyExpense = new float[numberOfDays];
                
                // Xử lý từng giao dịch
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    try {
                        String type = doc.getString("type");
                        Double amount = doc.getDouble("amount");
                        
                        // Xử lý trường date có thể ở nhiều định dạng khác nhau
                        Long date = null;
                        
                        // Kiểm tra nếu date là kiểu Timestamp của Firestore
                        if (doc.get("date") instanceof com.google.firebase.Timestamp) {
                            com.google.firebase.Timestamp timestamp = doc.getTimestamp("date");
                            if (timestamp != null) {
                                date = timestamp.toDate().getTime();
                            }
                        } 
                        // Kiểm tra nếu date là String (định dạng ngày)
                        else if (doc.get("date") instanceof String) {
                            String dateStr = doc.getString("date");
                            try {
                                SimpleDateFormat sdfParse = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                Date parsedDate = sdfParse.parse(dateStr);
                                if (parsedDate != null) {
                                    date = parsedDate.getTime();
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Lỗi khi chuyển đổi chuỗi ngày: " + e.getMessage());
                            }
                        }
                        // Thử lấy trực tiếp từ Long
                        else {
                            try {
                                date = doc.getLong("date");
                            } catch (Exception e) {
                                Log.e(TAG, "Không thể lấy date dưới dạng Long: " + e.getMessage());
                            }
                        }
                        
                        if (type == null || amount == null || date == null) {
                            Log.d(TAG, "Bỏ qua giao dịch không hợp lệ: ID=" + doc.getId() + 
                                 ", loại=" + type + ", số tiền=" + amount + 
                                 ", date type=" + (doc.get("date") != null ? doc.get("date").getClass().getSimpleName() : "null"));
                            continue; // Bỏ qua giao dịch không hợp lệ
                        }
                        
                        // Kiểm tra xem giao dịch có nằm trong khoảng thời gian không
                        if (date < startTime || date > endTime) {
                            continue; // Bỏ qua nếu không thuộc khoảng thời gian
                        }
                        
                        // Tính toán vị trí của ngày trong mảng
                        int dayIndex = (int) ((date - startTime) / (24 * 60 * 60 * 1000));
                            if (dayIndex >= 0 && dayIndex < numberOfDays) {
                                if ("income".equals(type)) {
                                dailyIncome[dayIndex] += amount.floatValue();
                                    totalDailyIncome += amount.floatValue();
                                    incomeCount++;
                                Log.d(TAG, "Giao dịch thu nhập vào ngày " + xAxisLabels.get(dayIndex) + ": " + amount);
                            } else if ("expense".equals(type) || "savings".equals(type)) {
                                dailyExpense[dayIndex] += amount.floatValue();
                                    totalDailyExpense += amount.floatValue();
                                    expenseCount++;
                                Log.d(TAG, "Giao dịch " + type + " vào ngày " + xAxisLabels.get(dayIndex) + ": " + amount);
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Lỗi khi xử lý giao dịch: " + e.getMessage() + ", ID=" + doc.getId());
                    }
                }
                
                // Cập nhật entries từ dữ liệu đã tính toán
                for (int i = 0; i < numberOfDays; i++) {
                    incomeEntries.set(i, new Entry(i, dailyIncome[i]));
                    expenseEntries.set(i, new Entry(i, dailyExpense[i]));
                    Log.d(TAG, "Dữ liệu ngày " + xAxisLabels.get(i) + ": Thu nhập=" + dailyIncome[i] + ", Chi tiêu=" + dailyExpense[i]);
                }
                
                Log.d(TAG, "Tổng số giao dịch thu nhập hàng ngày: " + incomeCount + ", tổng: " + totalDailyIncome);
                Log.d(TAG, "Tổng số giao dịch chi tiêu/tiết kiệm hàng ngày: " + expenseCount + ", tổng: " + totalDailyExpense);
                
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
                
                // Hiển thị biểu đồ trống
                ArrayList<Entry> emptyIncomeEntries = new ArrayList<>();
                ArrayList<Entry> emptyExpenseEntries = new ArrayList<>();
                for (int i = 0; i < numberOfDays; i++) {
                    emptyIncomeEntries.add(new Entry(i, 0));
                    emptyExpenseEntries.add(new Entry(i, 0));
                }
                updateLineChart(emptyIncomeEntries, emptyExpenseEntries, xAxisLabels);
            });
    }

    private void updateChartForMonthlyView(String userId, long startTime, long endTime) {
        // Thiết lập các khoảng thời gian cho biểu đồ hàng tháng
        ArrayList<Entry> incomeEntries = new ArrayList<>();
        ArrayList<Entry> expenseEntries = new ArrayList<>();
        ArrayList<String> xAxisLabels = new ArrayList<>();
        
        // Danh sách tên tháng
        String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        
        // Khởi tạo dữ liệu rỗng cho 12 tháng
        for (int i = 0; i < 12; i++) {
            xAxisLabels.add(monthNames[i]);
            incomeEntries.add(new Entry(i, 0));
            expenseEntries.add(new Entry(i, 0));
        }
        
        Log.d(TAG, "Bắt đầu truy vấn giao dịch từ " + new Date(startTime) + " đến " + new Date(endTime));
        
        // Truy cập subcollection transactions của người dùng
        FirebaseFirestore.getInstance().collection("users").document(userId).collection("transactions")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                Log.d(TAG, "Đã tìm thấy " + queryDocumentSnapshots.size() + " giao dịch tổng cộng");
                
                // Tạo mảng để lưu tổng thu nhập và chi tiêu theo tháng
                float[] monthlyIncome = new float[12];
                float[] monthlyExpense = new float[12];
                
                // Đếm số lượng giao dịch chi tiêu và thu nhập
                int expenseCount = 0;
                int incomeCount = 0;
                float totalMonthlyIncome = 0;
                float totalMonthlyExpense = 0;
                
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    try {
                        String type = doc.getString("type");
                        Double amount = doc.getDouble("amount");
                        
                        // Xử lý trường date có thể ở nhiều định dạng khác nhau
                        Long date = null;
                        
                        // Kiểm tra nếu date là kiểu Timestamp của Firestore
                        if (doc.get("date") instanceof com.google.firebase.Timestamp) {
                            com.google.firebase.Timestamp timestamp = doc.getTimestamp("date");
                            if (timestamp != null) {
                                date = timestamp.toDate().getTime();
                            }
                        } 
                        // Kiểm tra nếu date là String (định dạng ngày)
                        else if (doc.get("date") instanceof String) {
                            String dateStr = doc.getString("date");
                            try {
                                SimpleDateFormat sdfParse = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                Date parsedDate = sdfParse.parse(dateStr);
                                if (parsedDate != null) {
                                    date = parsedDate.getTime();
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Lỗi khi chuyển đổi chuỗi ngày: " + e.getMessage());
                            }
                        }
                        // Thử lấy trực tiếp từ Long
                        else {
                            try {
                                date = doc.getLong("date");
                            } catch (Exception e) {
                                Log.e(TAG, "Không thể lấy date dưới dạng Long: " + e.getMessage());
                            }
                        }
                        
                        if (type == null || amount == null || date == null) {
                            Log.d(TAG, "Bỏ qua giao dịch không hợp lệ: ID=" + doc.getId() + 
                                 ", loại=" + type + ", số tiền=" + amount + 
                                 ", date type=" + (doc.get("date") != null ? doc.get("date").getClass().getSimpleName() : "null"));
                            continue; // Bỏ qua giao dịch không hợp lệ
                        }
                        
                        // Kiểm tra xem giao dịch có nằm trong khoảng thời gian không
                        if (date < startTime || date > endTime) {
                            continue; // Bỏ qua nếu không thuộc khoảng thời gian
                        }
                        
                            // Xác định tháng
                            Calendar transactionCal = Calendar.getInstance();
                        transactionCal.setTimeInMillis(date);
                            int monthIndex = transactionCal.get(Calendar.MONTH);
                            
                            if (monthIndex >= 0 && monthIndex < 12) {
                                if ("income".equals(type)) {
                                    monthlyIncome[monthIndex] += amount.floatValue();
                                totalMonthlyIncome += amount.floatValue();
                                    incomeCount++;
                                Log.d(TAG, "Giao dịch thu nhập vào tháng " + monthNames[monthIndex] + ": " + amount);
                            } else if ("expense".equals(type) || "savings".equals(type)) {
                                    monthlyExpense[monthIndex] += amount.floatValue();
                                totalMonthlyExpense += amount.floatValue();
                                    expenseCount++;
                                Log.d(TAG, "Giao dịch " + type + " vào tháng " + monthNames[monthIndex] + ": " + amount);
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Lỗi khi xử lý giao dịch: " + e.getMessage() + ", ID=" + doc.getId());
                    }
                }
                
                // Cập nhật entries từ dữ liệu đã tính toán
                for (int i = 0; i < 12; i++) {
                    incomeEntries.set(i, new Entry(i, monthlyIncome[i]));
                    expenseEntries.set(i, new Entry(i, monthlyExpense[i]));
                    Log.d(TAG, "Dữ liệu tháng " + monthNames[i] + ": Thu nhập=" + monthlyIncome[i] + ", Chi tiêu=" + monthlyExpense[i]);
                }
                
                // Log thông tin tổng hợp
                Log.d(TAG, "Tổng số giao dịch thu nhập: " + incomeCount + ", tổng: " + totalMonthlyIncome);
                Log.d(TAG, "Tổng số giao dịch chi tiêu và tiết kiệm: " + expenseCount + ", tổng: " + totalMonthlyExpense);
                
                // Cập nhật biểu đồ
                updateLineChart(incomeEntries, expenseEntries, xAxisLabels);
                
                // Cập nhật giao diện tổng thu nhập/chi tiêu
                if (tvIncome != null) {
                    tvIncome.setText(CurrencyUtils.formatVND(totalMonthlyIncome));
                    Log.d(TAG, "Cập nhật UI tổng thu nhập: " + CurrencyUtils.formatVND(totalMonthlyIncome));
                }
                
                if (tvExpense != null) {
                    tvExpense.setText("-" + CurrencyUtils.formatVND(totalMonthlyExpense));
                    Log.d(TAG, "Cập nhật UI tổng chi tiêu và tiết kiệm: " + CurrencyUtils.formatVND(totalMonthlyExpense));
                }
                
                if (tvTotalBalance != null) {
                    tvTotalBalance.setText(CurrencyUtils.formatVND(totalMonthlyIncome - totalMonthlyExpense));
                    Log.d(TAG, "Cập nhật UI số dư: " + CurrencyUtils.formatVND(totalMonthlyIncome - totalMonthlyExpense));
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Lỗi khi truy vấn giao dịch cho biểu đồ hàng tháng: " + e.getMessage(), e);
                Toast.makeText(AnalysisActivity.this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                
                // Set giá trị mặc định để tránh hiển thị rỗng
                if (tvIncome != null) tvIncome.setText(CurrencyUtils.formatVND(0));
                if (tvExpense != null) tvExpense.setText("-" + CurrencyUtils.formatVND(0));
                if (tvTotalBalance != null) tvTotalBalance.setText(CurrencyUtils.formatVND(0));
                
                // Hiển thị biểu đồ trống
                ArrayList<Entry> emptyIncomeEntries = new ArrayList<>();
                ArrayList<Entry> emptyExpenseEntries = new ArrayList<>();
                for (int i = 0; i < 12; i++) {
                    emptyIncomeEntries.add(new Entry(i, 0));
                    emptyExpenseEntries.add(new Entry(i, 0));
                }
                updateLineChart(emptyIncomeEntries, emptyExpenseEntries, xAxisLabels);
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
            
            // Kiểm tra xem người dùng đã đăng nhập chưa
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser == null) {
                Log.e(TAG, "Không thể cập nhật biểu đồ: Người dùng chưa đăng nhập");
                Toast.makeText(this, "Vui lòng đăng nhập để xem biểu đồ", Toast.LENGTH_SHORT).show();
                
                // Hiển thị biểu đồ trống
                showEmptyChart(xAxisLabels);
                return;
            }
            
            String userId = firebaseUser.getUid();
            if (userId == null || userId.isEmpty()) {
                Log.e(TAG, "Không thể cập nhật biểu đồ: ID người dùng không hợp lệ");
                Toast.makeText(this, "Không thể xác định người dùng", Toast.LENGTH_SHORT).show();
                
                // Hiển thị biểu đồ trống
                showEmptyChart(xAxisLabels);
                return;
            }
            
            // Tạo các danh sách entries cho biểu đồ
            ArrayList<BarEntry> incomeBarEntries = new ArrayList<>();
            ArrayList<BarEntry> expenseBarEntries = new ArrayList<>();
            ArrayList<BarEntry> savingsBarEntries = new ArrayList<>();
            
            // Điền dữ liệu mặc định vào các entries từ dữ liệu hiện có
            for (int i = 0; i < xAxisLabels.size(); i++) {
                // Đưa dữ liệu thu nhập vào
                float incomeValue = (i < incomeEntries.size()) ? incomeEntries.get(i).getY() : 0f;
                incomeBarEntries.add(new BarEntry(i, incomeValue));
                
                // Giá trị mặc định cho chi tiêu và tiết kiệm, sẽ được cập nhật sau
                expenseBarEntries.add(new BarEntry(i, 0f));
                savingsBarEntries.add(new BarEntry(i, 0f));
            }
            
            // Truy cập vào Firestore để lấy thông tin từng giao dịch
            FirebaseFirestore.getInstance().collection("users").document(userId).collection("transactions")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    try {
                        Log.d(TAG, "Tổng số giao dịch: " + queryDocumentSnapshots.size());
                        
                        if (queryDocumentSnapshots.isEmpty()) {
                            Log.d(TAG, "Không có giao dịch nào, hiển thị biểu đồ trống");
                            completeChartSetup(incomeBarEntries, expenseBarEntries, savingsBarEntries, xAxisLabels);
                            return;
                        }
                        
                        // Tạo mảng tạm để lưu trữ giá trị chi tiêu và tiết kiệm 
                        float[] expenseValues = new float[xAxisLabels.size()];
                        float[] savingsValues = new float[xAxisLabels.size()];
                        
                        // Duyệt qua các giao dịch để tính toán
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            try {
                                String type = doc.getString("type");
                                Double amount = doc.getDouble("amount");
                                
                                // Xử lý trường date có thể ở nhiều định dạng khác nhau
                                Long date = null;
                                
                                // Kiểm tra nếu date là kiểu Timestamp của Firestore
                                if (doc.get("date") instanceof com.google.firebase.Timestamp) {
                                    com.google.firebase.Timestamp timestamp = doc.getTimestamp("date");
                                    if (timestamp != null) {
                                        date = timestamp.toDate().getTime();
                                    }
                                } 
                                // Kiểm tra nếu date là String (định dạng ngày)
                                else if (doc.get("date") instanceof String) {
                                    String dateStr = doc.getString("date");
                                    try {
                                        // Giả sử định dạng ngày là dd/MM/yyyy
                                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                        Date parsedDate = sdf.parse(dateStr);
                                        if (parsedDate != null) {
                                            date = parsedDate.getTime();
                                        }
                                    } catch (Exception e) {
                                        Log.e(TAG, "Lỗi khi chuyển đổi chuỗi ngày: " + e.getMessage());
                                    }
                                }
                                // Thử lấy trực tiếp từ Long
                                else {
                                    try {
                                        date = doc.getLong("date");
                                    } catch (Exception e) {
                                        Log.e(TAG, "Không thể lấy date dưới dạng Long: " + e.getMessage());
                                    }
                                }
                                
                                if (type == null || amount == null || date == null) {
                                    Log.d(TAG, "Bỏ qua giao dịch không hợp lệ: ID=" + doc.getId() + 
                                         ", loại=" + type + ", số tiền=" + amount + 
                                         ", date type=" + (doc.get("date") != null ? doc.get("date").getClass().getSimpleName() : "null"));
                                    continue; // Bỏ qua giao dịch không hợp lệ
                                }
                                
                                // Xử lý cho từng mục thời gian trên biểu đồ
                                for (int i = 0; i < xAxisLabels.size(); i++) {
                                    boolean isInPeriod = isTransactionInPeriod(date, i, xAxisLabels);
                                    if (isInPeriod) {
                                        if ("expense".equals(type)) {
                                            expenseValues[i] += amount.floatValue();
                                        } else if ("savings".equals(type)) {
                                            savingsValues[i] += amount.floatValue();
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Lỗi khi xử lý giao dịch: " + e.getMessage() + ", ID=" + doc.getId());
                            }
                        }
                        
                        // Cập nhật các entries dựa trên dữ liệu đã tính toán
                        for (int i = 0; i < xAxisLabels.size(); i++) {
                            expenseBarEntries.set(i, new BarEntry(i, expenseValues[i]));
                            savingsBarEntries.set(i, new BarEntry(i, savingsValues[i]));
                            
                            Log.d(TAG, "Mục " + i + " - " + xAxisLabels.get(i) + ": Thu nhập=" + 
                                   incomeBarEntries.get(i).getY() + ", Chi tiêu=" + expenseValues[i] + 
                                   ", Tiết kiệm=" + savingsValues[i]);
                        }
                        
                        // Hoàn thành thiết lập biểu đồ
                        completeChartSetup(incomeBarEntries, expenseBarEntries, savingsBarEntries, xAxisLabels);
                        
                    } catch (Exception e) {
                        Log.e(TAG, "Lỗi khi xử lý dữ liệu giao dịch: " + e.getMessage());
                        Toast.makeText(AnalysisActivity.this, "Lỗi khi xử lý dữ liệu", Toast.LENGTH_SHORT).show();
                        showEmptyChart(xAxisLabels);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi truy vấn giao dịch: " + e.getMessage());
                    Toast.makeText(AnalysisActivity.this, "Lỗi khi tải dữ liệu giao dịch", Toast.LENGTH_SHORT).show();
                    showEmptyChart(xAxisLabels);
                });
            
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi cập nhật biểu đồ: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi khi cập nhật biểu đồ", Toast.LENGTH_SHORT).show();
            try {
                // Hiển thị biểu đồ trống trong trường hợp có lỗi
                showEmptyChart(xAxisLabels);
            } catch (Exception ex) {
                Log.e(TAG, "Lỗi khi hiển thị biểu đồ trống: " + ex.getMessage());
            }
        }
    }
    
    /**
     * Hiển thị biểu đồ trống khi không có dữ liệu hoặc có lỗi
     */
    private void showEmptyChart(ArrayList<String> xAxisLabels) {
        try {
            ArrayList<BarEntry> emptyEntries = new ArrayList<>();
            for (int i = 0; i < xAxisLabels.size(); i++) {
                emptyEntries.add(new BarEntry(i, 0f));
            }
            
            BarDataSet emptyDataSet = new BarDataSet(emptyEntries, "Không có dữ liệu");
            emptyDataSet.setColor(getResources().getColor(R.color.gray_400));
            emptyDataSet.setDrawValues(false);
            
            BarData barData = new BarData(emptyDataSet);
            barData.setBarWidth(0.5f);
            
            barChart.setData(barData);
            barChart.invalidate();
            
            // Hiển thị thông báo "Không có dữ liệu" trên biểu đồ
            barChart.setNoDataText("Không có dữ liệu");
            barChart.setNoDataTextColor(getResources().getColor(R.color.gray_600));
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi hiển thị biểu đồ trống: " + e.getMessage());
        }
    }
    
    /**
     * Hoàn thành thiết lập biểu đồ với dữ liệu đã chuẩn bị
     */
    private void completeChartSetup(ArrayList<BarEntry> incomeBarEntries, 
                                   ArrayList<BarEntry> expenseBarEntries, 
                                   ArrayList<BarEntry> savingsBarEntries,
                                   ArrayList<String> xAxisLabels) {
        try {
            // In log các giá trị để kiểm tra
            for (int i = 0; i < Math.min(incomeBarEntries.size(), xAxisLabels.size()); i++) {
                Log.d(TAG, "Giá trị ban đầu - " + xAxisLabels.get(i) + 
                      ": Thu nhập=" + incomeBarEntries.get(i).getY() + 
                      ", Chi tiêu=" + expenseBarEntries.get(i).getY() + 
                      ", Tiết kiệm=" + savingsBarEntries.get(i).getY());
            }
            
            // Tìm giá trị lớn nhất để thiết lập trục Y phù hợp
            float maxValue = 0.1f; // Giá trị tối thiểu để tránh chia cho 0
            for (int i = 0; i < incomeBarEntries.size(); i++) {
                float income = incomeBarEntries.get(i).getY();
                float expense = expenseBarEntries.get(i).getY();
                float savings = savingsBarEntries.get(i).getY();
                float currentMax = Math.max(income, Math.max(expense, savings));
                if (currentMax > maxValue) {
                    maxValue = currentMax;
                }
            }
            
            // Đảm bảo có khoảng cách tối thiểu trên trục Y
            maxValue = Math.max(maxValue, 10000); // Tối thiểu 10.000 VND
            
            Log.d(TAG, "Giá trị lớn nhất trên biểu đồ: " + maxValue);
            
            // Điều chỉnh trục Y
            YAxis leftAxis = barChart.getAxisLeft();
            leftAxis.setAxisMaximum(maxValue * 1.2f); // Thêm 20% không gian trên cùng
            leftAxis.setAxisMinimum(0f);
            
            // Định dạng giá trị trục Y để hiển thị đơn vị M, K
            leftAxis.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    if (value >= 1000000) {
                        return String.format("%.0fM", value / 1000000);
                    } else if (value >= 1000) {
                        return String.format("%.0fK", value / 1000);
                    } else {
                        return String.format("%.0f", value);
                    }
                }
            });
            
            // Thêm đường lưới ngang để dễ đọc giá trị
            leftAxis.setDrawGridLines(true);
            leftAxis.enableGridDashedLine(10f, 10f, 0f);
            
            // Thêm grid line ở mức giá trị 0
            leftAxis.setDrawZeroLine(true);
            leftAxis.setZeroLineWidth(1.5f);
            leftAxis.setZeroLineColor(getResources().getColor(android.R.color.darker_gray));
            
            // Tạo dataset thu nhập với màu xanh lá cây
            BarDataSet incomeBarDataSet = new BarDataSet(incomeBarEntries, "Thu nhập");
            incomeBarDataSet.setColor(getResources().getColor(R.color.income_green));
            incomeBarDataSet.setValueTextSize(10f);
            incomeBarDataSet.setValueTextColor(getResources().getColor(R.color.income_green));
            
            // Tạo dataset chi tiêu với màu đỏ
            BarDataSet expenseBarDataSet = new BarDataSet(expenseBarEntries, "Chi tiêu");
            expenseBarDataSet.setColor(getResources().getColor(R.color.expense_red));
            expenseBarDataSet.setValueTextSize(10f);
            expenseBarDataSet.setValueTextColor(getResources().getColor(R.color.expense_red));
            
            // Tạo dataset tiết kiệm với màu xanh dương
            BarDataSet savingsBarDataSet = new BarDataSet(savingsBarEntries, "Tiết kiệm");
            savingsBarDataSet.setColor(getResources().getColor(R.color.savings_blue));
            savingsBarDataSet.setValueTextSize(10f);
            savingsBarDataSet.setValueTextColor(getResources().getColor(R.color.savings_blue));
            
            // Hiển thị giá trị trên cột khi giá trị đáng kể
            for (int i = 0; i < incomeBarEntries.size(); i++) {
                boolean hasSignificantValue = false;
                if (incomeBarEntries.get(i).getY() >= maxValue * 0.1f) hasSignificantValue = true;
                if (expenseBarEntries.get(i).getY() >= maxValue * 0.1f) hasSignificantValue = true; 
                if (savingsBarEntries.get(i).getY() >= maxValue * 0.1f) hasSignificantValue = true;
                
                // Hiển thị giá trị nếu có ít nhất một cột có giá trị đáng kể
                if (hasSignificantValue) {
                    incomeBarDataSet.setDrawValues(true);
                    expenseBarDataSet.setDrawValues(true);
                    savingsBarDataSet.setDrawValues(true);
                    break;
                }
            }
            
            // Cải thiện hiển thị bằng cách vẽ viền cho các cột
            float borderWidth = 0.5f;
            incomeBarDataSet.setBarBorderWidth(borderWidth);
            expenseBarDataSet.setBarBorderWidth(borderWidth);
            savingsBarDataSet.setBarBorderWidth(borderWidth);
            
            int borderColor = getResources().getColor(android.R.color.darker_gray);
            incomeBarDataSet.setBarBorderColor(borderColor);
            expenseBarDataSet.setBarBorderColor(borderColor);
            savingsBarDataSet.setBarBorderColor(borderColor);
            
            // Đảm bảo hiển thị cả ba bộ dữ liệu
            BarData barData = new BarData(incomeBarDataSet, expenseBarDataSet, savingsBarDataSet);
            
            // Thiết lập các thuộc tính của barData
            float groupSpace = 0.3f; // Khoảng cách giữa các nhóm
            float barSpace = 0.03f;  // Khoảng cách giữa các cột trong nhóm
            float barWidth = 0.2f;  // Độ rộng của cột
            
            // Đặt độ rộng cột cho tất cả các dataset
            barData.setBarWidth(barWidth);
            
            // Định dạng giá trị hiển thị trên cột
            barData.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    if (value == 0) return "";
                    if (value >= 1000000) {
                        return String.format("%.1fM", value / 1000000);
                    } else if (value >= 1000) {
                        return String.format("%.0fK", value / 1000);
                    } else if (value > 0) {
                        return String.format("%.0f", value);
                    }
                    return "";
                }
            });
            
            // Nhóm các cột lại với nhau
            barData.groupBars(0f, groupSpace, barSpace);
            
            // Đảm bảo tất cả các cột có cùng kích thước
            incomeBarDataSet.setBarShadowColor(android.graphics.Color.TRANSPARENT);
            expenseBarDataSet.setBarShadowColor(android.graphics.Color.TRANSPARENT);
            savingsBarDataSet.setBarShadowColor(android.graphics.Color.TRANSPARENT);
            
            // Cập nhật dữ liệu cho biểu đồ
            barChart.setData(barData);
            barChart.setFitBars(true); // Đảm bảo các cột vừa khít
            
            // Điều chỉnh hiển thị số lượng dòng lưới trên trục Y
            int gridCount = Math.min(6, Math.max(3, (int)(maxValue / 10000) + 1));
            leftAxis.setLabelCount(gridCount, true);
            
            // Cập nhật legend (chú thích)
            Legend legend = barChart.getLegend();
            legend.setEnabled(true);
            legend.setTextSize(12f);
            legend.setForm(Legend.LegendForm.CIRCLE);
            legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
            legend.setWordWrapEnabled(true);
            legend.setFormSize(12f);
            legend.setFormLineWidth(2f);
            legend.setXEntrySpace(10f);
            
            // Tùy chỉnh trục X
            XAxis xAxis = barChart.getXAxis();
            xAxis.setGranularity(1f);
            xAxis.setGranularityEnabled(true);
            xAxis.setAxisMinimum(-0.5f); // Thêm khoảng trống ở hai bên trục X
            xAxis.setAxisMaximum(xAxisLabels.size() - 0.5f);
            xAxis.setLabelCount(xAxisLabels.size());
            xAxis.setLabelRotationAngle(0); // Không xoay nhãn
            
            // Cải thiện khoảng cách từ nhãn trục X đến biểu đồ
            barChart.setExtraBottomOffset(15f);
            
            // Hiệu ứng animation khi hiển thị biểu đồ
            barChart.animateY(1000);
            
            // Cập nhật biểu đồ
            barChart.invalidate();
            
            // Thêm tương tác với biểu đồ (hiển thị giá trị chi tiết khi nhấn)
            barChart.setOnChartValueSelectedListener(new com.github.mikephil.charting.listener.OnChartValueSelectedListener() {
                @Override
                public void onValueSelected(com.github.mikephil.charting.data.Entry e, com.github.mikephil.charting.highlight.Highlight h) {
                    try {
                        int index = (int) e.getX();
                        if (index >= 0 && index < xAxisLabels.size()) {
                            String label = xAxisLabels.get(index);
                            float incomeValue = incomeBarEntries.get(index).getY();
                            float expenseValue = expenseBarEntries.get(index).getY();
                            float savingsValue = savingsBarEntries.get(index).getY();
                            
                            // Hiển thị giá trị chi tiết tại đây
                            String message = label + ": " + 
                                    "\nThu nhập: " + CurrencyUtils.formatVND(incomeValue) + 
                                    "\nChi tiêu: " + CurrencyUtils.formatVND(expenseValue) +
                                    "\nTiết kiệm: " + CurrencyUtils.formatVND(savingsValue);
                            
                            Toast.makeText(
                                AnalysisActivity.this, 
                                message,
                                Toast.LENGTH_LONG
                            ).show();
                        }
                    } catch (Exception ex) {
                        Log.e(TAG, "Lỗi khi hiển thị chi tiết: " + ex.getMessage());
                    }
                }
                
                @Override
                public void onNothingSelected() {
                    // Không làm gì
                }
            });
            
            Log.d(TAG, "Đã cập nhật biểu đồ thành công với " + incomeBarEntries.size() + " mục dữ liệu");
            Log.d(TAG, "Số lượng datasets trong biểu đồ: " + barData.getDataSetCount());
            
            // In ra giá trị các cột để kiểm tra
            for (int i = 0; i < incomeBarEntries.size(); i++) {
                if (i < xAxisLabels.size()) {
                    Log.d(TAG, "Cột cuối cùng " + xAxisLabels.get(i) + 
                           ": Thu nhập=" + incomeBarEntries.get(i).getY() + 
                           ", Chi tiêu=" + expenseBarEntries.get(i).getY() + 
                           ", Tiết kiệm=" + savingsBarEntries.get(i).getY());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi hoàn thành thiết lập biểu đồ: " + e.getMessage(), e);
            showEmptyChart(xAxisLabels);
        }
    }
    
    /**
     * Kiểm tra xem một giao dịch có thuộc vào khoảng thời gian tương ứng với nhãn thời gian không
     * 
     * @param transactionDate Ngày của giao dịch (timestamp)
     * @param index Chỉ số của nhãn thời gian
     * @param labels Danh sách các nhãn thời gian
     * @return true nếu giao dịch thuộc vào khoảng thời gian đó
     */
    private boolean isTransactionInPeriod(long transactionDate, int index, ArrayList<String> labels) {
        // Kiểm tra xem đây là biểu đồ ngày, tuần, tháng hay năm
        String label = labels.get(index);
        Calendar transactionCal = Calendar.getInstance();
        transactionCal.setTimeInMillis(transactionDate);
        
        if (label.matches("\\d{2}/\\d{2}")) { // Định dạng dd/MM
            // Đây là biểu đồ theo ngày
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
            String transactionDay = sdf.format(new Date(transactionDate));
            return label.equals(transactionDay);
        } else if (label.matches("\\w{3}")) { // Định dạng ba chữ cái (Jan, Feb, ...)
            // Đây là biểu đồ theo tháng
            String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
            int transactionMonth = transactionCal.get(Calendar.MONTH);
            return label.equals(monthNames[transactionMonth]);
        }
        
        return false; // Mặc định không thuộc khoảng thời gian này
    }
} 