package com.example.qltccn.activities;

import android.annotation.SuppressLint;
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

        // Thêm xử lý nút search
        ImageView btnSearch = findViewById(R.id.btnSearch);
        if (btnSearch != null) {
            btnSearch.setOnClickListener(v -> navigateToSearch());
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

                    // Lấy thời gian bắt đầu và kết thúc của tháng hiện tại
                    Calendar calendar = Calendar.getInstance();
                    int currentMonth = calendar.get(Calendar.MONTH);
                    int currentYear = calendar.get(Calendar.YEAR);

                    // Đặt về đầu tháng
                    calendar.set(currentYear, currentMonth, 1, 0, 0, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    long startOfMonth = calendar.getTimeInMillis();

                    // Đặt về cuối tháng
                    calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                    calendar.set(Calendar.HOUR_OF_DAY, 23);
                    calendar.set(Calendar.MINUTE, 59);
                    calendar.set(Calendar.SECOND, 59);
                    long endOfMonth = calendar.getTimeInMillis();

                    // Lấy ID người dùng
                    String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                            FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

                    if (userId != null) {
                        // Tính toán dữ liệu thu chi cho tháng hiện tại
                        calculateMonthlyIncomeAndExpense(userId, startOfMonth, endOfMonth);

                        // Cập nhật biểu đồ cả năm
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

                        // Cập nhật biểu đồ
                        updateChartForMonthlyView(userId, startOfYear, endOfYear);
                    } else {
                        Toast.makeText(AnalysisActivity.this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
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

                    // Lấy ID người dùng
                    String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                            FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

                    if (userId != null) {
                        // Lấy năm hiện tại
                        Calendar calendar = Calendar.getInstance();
                        int currentYear = calendar.get(Calendar.YEAR);

                        // Đặt thời gian về đầu năm
                        calendar.set(currentYear, Calendar.JANUARY, 1, 0, 0, 0);
                        calendar.set(Calendar.MILLISECOND, 0);
                        long startOfYear = calendar.getTimeInMillis();

                        // Đặt thời gian về cuối năm
                        calendar.set(currentYear, Calendar.DECEMBER, 31, 23, 59, 59);
                        long endOfYear = calendar.getTimeInMillis();

                        // Tính toán dữ liệu thu chi cho năm hiện tại
                        calculateYearlyIncomeAndExpense(userId, startOfYear, endOfYear);

                        // Gọi phương thức cập nhật biểu đồ dữ liệu theo năm
                        updateChartForYearView(userId);
                    } else {
                        Toast.makeText(AnalysisActivity.this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
                    }
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
        updateTabSelection(tabDaily);

        // Lấy ngày hôm nay
        Calendar calendar = Calendar.getInstance();

        // Đặt về đầu ngày hiện tại
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfToday = calendar.getTimeInMillis();

        // Đặt về cuối ngày hiện tại
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        long endOfToday = calendar.getTimeInMillis();

        // Lấy ID người dùng hiện tại
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (userId == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tính toán dữ liệu thu nhập và chi tiêu cho ngày hôm nay
        calculateDailyIncomeAndExpense(userId, startOfToday, endOfToday);

        // Lấy dữ liệu cho cả tuần để hiển thị biểu đồ
        // Đặt thời gian về đầu ngày hiện tại
        calendar.setTimeInMillis(startOfToday);

        // Tìm ngày đầu tuần (thứ 2)
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            calendar.add(Calendar.DAY_OF_MONTH, -1);
        }
        long startOfWeek = calendar.getTimeInMillis();

        // Tìm ngày cuối tuần (Chủ nhật)
        calendar.add(Calendar.DAY_OF_WEEK, 6);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        long endOfWeek = calendar.getTimeInMillis();

        // Hiển thị biểu đồ theo ngày trong tuần
        updateChartForDailyView(userId, startOfWeek, endOfWeek);
    }

    private void calculateDailyIncomeAndExpense(String userId, long startTime, long endTime) {
        FirebaseFirestore.getInstance().collection("users").document(userId).collection("transactions")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Tính toán thu chi cho ngày: " + new Date(startTime));

                    double dailyIncome = 0;
                    double dailyExpense = 0;
                    double dailySavings = 0;

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            String type = doc.getString("type");
                            Double amount = doc.getDouble("amount");

                            // Xử lý trường date có thể ở nhiều định dạng khác nhau
                            Long date = null;

                            // Kiểm tra các định dạng date có thể có
                            if (doc.get("date") instanceof com.google.firebase.Timestamp) {
                                com.google.firebase.Timestamp timestamp = doc.getTimestamp("date");
                                if (timestamp != null) {
                                    date = timestamp.toDate().getTime();
                                }
                            } else if (doc.get("date") instanceof String) {
                                try {
                                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                    Date parsedDate = sdf.parse(doc.getString("date"));
                                    if (parsedDate != null) {
                                        date = parsedDate.getTime();
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Lỗi khi phân tích chuỗi ngày: " + e.getMessage());
                                }
                            } else {
                                try {
                                    date = doc.getLong("date");
                                } catch (Exception e) {
                                    Log.e(TAG, "Không thể lấy date dưới dạng Long: " + e.getMessage());
                                }
                            }

                            if (type == null || amount == null || date == null) {
                                continue; // Bỏ qua giao dịch không hợp lệ
                            }

                            // Kiểm tra nếu giao dịch thuộc ngày hôm nay
                            if (date >= startTime && date <= endTime) {
                                if ("income".equals(type)) {
                                    dailyIncome += amount;
                                    Log.d(TAG, "Thu nhập hôm nay: " + amount);
                                } else if ("expense".equals(type)) {
                                    dailyExpense += amount;
                                    Log.d(TAG, "Chi tiêu hôm nay: " + amount);
                                } else if ("savings".equals(type)) {
                                    dailySavings += amount;
                                    Log.d(TAG, "Tiết kiệm hôm nay: " + amount);
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Lỗi khi xử lý giao dịch: " + e.getMessage());
                        }
                    }

                    // Cập nhật UI
                    if (tvIncome != null) {
                        tvIncome.setText(CurrencyUtils.formatVND(dailyIncome));
                        Log.d(TAG, "Cập nhật thu nhập ngày: " + CurrencyUtils.formatVND(dailyIncome));
                    }

                    if (tvExpense != null) {
                        tvExpense.setText("-" + CurrencyUtils.formatVND(dailyExpense + dailySavings));
                        Log.d(TAG, "Cập nhật chi tiêu ngày: " + CurrencyUtils.formatVND(dailyExpense + dailySavings));
                    }

                    if (tvTotalBalance != null) {
                        tvTotalBalance.setText(CurrencyUtils.formatVND(dailyIncome - dailyExpense - dailySavings));
                        Log.d(TAG, "Cập nhật số dư ngày: " + CurrencyUtils.formatVND(dailyIncome - dailyExpense - dailySavings));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi tính toán thu chi cho ngày: " + e.getMessage());

                    if (tvIncome != null) tvIncome.setText(CurrencyUtils.formatVND(0));
                    if (tvExpense != null) tvExpense.setText("-" + CurrencyUtils.formatVND(0));
                    if (tvTotalBalance != null) tvTotalBalance.setText(CurrencyUtils.formatVND(0));
                });
    }

    private void updateChartForWeeklyView() {
        // Đánh dấu nút Weekly được chọn
        updateTabSelection(tabWeekly);

        // Lấy ngày đầu tuần và cuối tuần hiện tại
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
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        long endOfWeek = calendar.getTimeInMillis();

        // Lấy ID người dùng hiện tại
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (userId == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tính toán dữ liệu thu nhập và chi tiêu cho tuần hiện tại
        calculateWeeklyIncomeAndExpense(userId, startOfWeek, endOfWeek);

        // Lấy dữ liệu cho biểu đồ tuần trong tháng
        // Đặt thời gian về đầu tháng
        calendar.setTimeInMillis(startOfWeek);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        long startOfMonth = calendar.getTimeInMillis();

        // Đặt về cuối tháng
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        long endOfMonth = calendar.getTimeInMillis();

        // Hiển thị biểu đồ theo tuần trong tháng
        updateChartForWeeklyView(userId, startOfMonth, endOfMonth);
    }

    private void calculateWeeklyIncomeAndExpense(String userId, long startTime, long endTime) {
        FirebaseFirestore.getInstance().collection("users").document(userId).collection("transactions")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Tính toán thu chi cho tuần từ " + new Date(startTime) + " đến " + new Date(endTime));

                    double weeklyIncome = 0;
                    double weeklyExpense = 0;
                    double weeklySavings = 0;

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            String type = doc.getString("type");
                            Double amount = doc.getDouble("amount");

                            // Xử lý trường date có thể ở nhiều định dạng khác nhau
                            Long date = null;

                            // Kiểm tra các định dạng date có thể có
                            if (doc.get("date") instanceof com.google.firebase.Timestamp) {
                                com.google.firebase.Timestamp timestamp = doc.getTimestamp("date");
                                if (timestamp != null) {
                                    date = timestamp.toDate().getTime();
                                }
                            } else if (doc.get("date") instanceof String) {
                                try {
                                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                    Date parsedDate = sdf.parse(doc.getString("date"));
                                    if (parsedDate != null) {
                                        date = parsedDate.getTime();
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Lỗi khi phân tích chuỗi ngày: " + e.getMessage());
                                }
                            } else {
                                try {
                                    date = doc.getLong("date");
                                } catch (Exception e) {
                                    Log.e(TAG, "Không thể lấy date dưới dạng Long: " + e.getMessage());
                                }
                            }

                            if (type == null || amount == null || date == null) {
                                continue; // Bỏ qua giao dịch không hợp lệ
                            }

                            // Kiểm tra nếu giao dịch thuộc tuần hiện tại
                            if (date >= startTime && date <= endTime) {
                                if ("income".equals(type)) {
                                    weeklyIncome += amount;
                                    Log.d(TAG, "Thu nhập tuần này: " + amount);
                                } else if ("expense".equals(type)) {
                                    weeklyExpense += amount;
                                    Log.d(TAG, "Chi tiêu tuần này: " + amount);
                                } else if ("savings".equals(type)) {
                                    weeklySavings += amount;
                                    Log.d(TAG, "Tiết kiệm tuần này: " + amount);
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Lỗi khi xử lý giao dịch: " + e.getMessage());
                        }
                    }

                    // Cập nhật UI
                    if (tvIncome != null) {
                        tvIncome.setText(CurrencyUtils.formatVND(weeklyIncome));
                        Log.d(TAG, "Cập nhật thu nhập tuần: " + CurrencyUtils.formatVND(weeklyIncome));
                    }

                    if (tvExpense != null) {
                        tvExpense.setText("-" + CurrencyUtils.formatVND(weeklyExpense + weeklySavings));
                        Log.d(TAG, "Cập nhật chi tiêu tuần: " + CurrencyUtils.formatVND(weeklyExpense + weeklySavings));
                    }

                    if (tvTotalBalance != null) {
                        tvTotalBalance.setText(CurrencyUtils.formatVND(weeklyIncome - weeklyExpense - weeklySavings));
                        Log.d(TAG, "Cập nhật số dư tuần: " + CurrencyUtils.formatVND(weeklyIncome - weeklyExpense - weeklySavings));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi tính toán thu chi cho tuần: " + e.getMessage());

                    if (tvIncome != null) tvIncome.setText(CurrencyUtils.formatVND(0));
                    if (tvExpense != null) tvExpense.setText("-" + CurrencyUtils.formatVND(0));
                    if (tvTotalBalance != null) tvTotalBalance.setText(CurrencyUtils.formatVND(0));
                });
    }

    private void updateChartForWeeklyView(String userId, long startTime, long endTime) {
        // Thiết lập các khoảng thời gian cho biểu đồ theo tuần trong tháng
        ArrayList<Entry> incomeEntries = new ArrayList<>();
        ArrayList<Entry> expenseEntries = new ArrayList<>();
        ArrayList<Entry> savingsEntries = new ArrayList<>();
        ArrayList<String> xAxisLabels = new ArrayList<>();

        // Danh sách các tuần trong tháng
        String[] weekNames = {"1st Week", "2nd Week", "3rd Week", "4th Week", "5th Week"};

        // Lấy số tuần tối đa trong tháng (thông thường là 4 hoặc 5)
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(startTime);
        int maxWeeksInMonth = calendar.getActualMaximum(Calendar.WEEK_OF_MONTH);

        // Khởi tạo dữ liệu rỗng cho các tuần trong tháng
        for (int i = 0; i < maxWeeksInMonth; i++) {
            xAxisLabels.add(weekNames[i]);
            incomeEntries.add(new Entry(i, 0));
            expenseEntries.add(new Entry(i, 0));
            savingsEntries.add(new Entry(i, 0));
        }

        Log.d(TAG, "Bắt đầu truy vấn giao dịch từ " + new Date(startTime) + " đến " + new Date(endTime));

        // Truy cập subcollection transactions của người dùng
        FirebaseFirestore.getInstance().collection("users").document(userId).collection("transactions")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Đã tìm thấy " + queryDocumentSnapshots.size() + " giao dịch tổng cộng");

                    // Tạo mảng để lưu tổng thu nhập, chi tiêu và tiết kiệm theo tuần trong tháng
                    float[] weeklyIncome = new float[maxWeeksInMonth];
                    float[] weeklyExpense = new float[maxWeeksInMonth];
                    float[] weeklySavings = new float[maxWeeksInMonth];

                    // Đếm số lượng giao dịch
                    int expenseCount = 0;
                    int incomeCount = 0;
                    int savingsCount = 0;
                    float totalWeeklyIncome = 0;
                    float totalWeeklyExpense = 0;
                    float totalWeeklySavings = 0;

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
                                Log.d(TAG, "Bỏ qua giao dịch không hợp lệ: ID=" + doc.getId());
                                continue; // Bỏ qua giao dịch không hợp lệ
                            }

                            // Kiểm tra xem giao dịch có nằm trong khoảng thời gian tháng này không
                            if (date < startTime || date > endTime) {
                                continue; // Bỏ qua nếu không thuộc khoảng thời gian
                            }

                            // Xác định tuần trong tháng
                            Calendar transactionCal = Calendar.getInstance();
                            transactionCal.setTimeInMillis(date);
                            int weekOfMonth = transactionCal.get(Calendar.WEEK_OF_MONTH) - 1; // 0-based index

                            if (weekOfMonth >= 0 && weekOfMonth < maxWeeksInMonth) {
                                if ("income".equals(type)) {
                                    weeklyIncome[weekOfMonth] += amount.floatValue();
                                    totalWeeklyIncome += amount.floatValue();
                                    incomeCount++;
                                    Log.d(TAG, "Giao dịch thu nhập vào " + weekNames[weekOfMonth] + ": " + amount);
                                } else if ("expense".equals(type)) {
                                    weeklyExpense[weekOfMonth] += amount.floatValue();
                                    totalWeeklyExpense += amount.floatValue();
                                    expenseCount++;
                                    Log.d(TAG, "Giao dịch chi tiêu vào " + weekNames[weekOfMonth] + ": " + amount);
                                } else if ("savings".equals(type)) {
                                    weeklySavings[weekOfMonth] += amount.floatValue();
                                    totalWeeklySavings += amount.floatValue();
                                    savingsCount++;
                                    Log.d(TAG, "Giao dịch tiết kiệm vào " + weekNames[weekOfMonth] + ": " + amount);
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Lỗi khi xử lý giao dịch: " + e.getMessage() + ", ID=" + doc.getId());
                        }
                    }

                    // Cập nhật entries từ dữ liệu đã tính toán
                    for (int i = 0; i < maxWeeksInMonth; i++) {
                        incomeEntries.set(i, new Entry(i, weeklyIncome[i]));
                        expenseEntries.set(i, new Entry(i, weeklyExpense[i]));
                        savingsEntries.set(i, new Entry(i, weeklySavings[i]));
                        Log.d(TAG, "Dữ liệu " + weekNames[i] + ": Thu nhập=" + weeklyIncome[i] +
                                ", Chi tiêu=" + weeklyExpense[i] + ", Tiết kiệm=" + weeklySavings[i]);
                    }

                    // Log thông tin tổng hợp
                    Log.d(TAG, "Tổng số giao dịch thu nhập theo tuần: " + incomeCount + ", tổng: " + totalWeeklyIncome);
                    Log.d(TAG, "Tổng số giao dịch chi tiêu theo tuần: " + expenseCount + ", tổng: " + totalWeeklyExpense);
                    Log.d(TAG, "Tổng số giao dịch tiết kiệm theo tuần: " + savingsCount + ", tổng: " + totalWeeklySavings);

                    // Tạo các danh sách entries cho biểu đồ
                    ArrayList<BarEntry> incomeBarEntries = new ArrayList<>();
                    ArrayList<BarEntry> expenseBarEntries = new ArrayList<>();
                    ArrayList<BarEntry> savingsBarEntries = new ArrayList<>();

                    // Điền dữ liệu từ entries đã tính
                    for (int i = 0; i < xAxisLabels.size(); i++) {
                        // Đưa dữ liệu thu nhập vào
                        float incomeValue = (i < incomeEntries.size()) ? incomeEntries.get(i).getY() : 0f;
                        incomeBarEntries.add(new BarEntry(i, incomeValue));

                        // Đưa dữ liệu chi tiêu vào
                        float expenseValue = (i < expenseEntries.size()) ? expenseEntries.get(i).getY() : 0f;
                        expenseBarEntries.add(new BarEntry(i, expenseValue));

                        // Đưa dữ liệu tiết kiệm vào
                        float savingsValue = (i < savingsEntries.size()) ? savingsEntries.get(i).getY() : 0f;
                        savingsBarEntries.add(new BarEntry(i, savingsValue));
                    }

                    // Hoàn thành thiết lập biểu đồ
                    completeChartSetup(incomeBarEntries, expenseBarEntries, savingsBarEntries, xAxisLabels);

                    // Cập nhật giao diện tổng thu nhập/chi tiêu
                    if (tvIncome != null) {
                        tvIncome.setText(CurrencyUtils.formatVND(totalWeeklyIncome));
                        Log.d(TAG, "Cập nhật UI tổng thu nhập theo tuần: " + CurrencyUtils.formatVND(totalWeeklyIncome));
                    }

                    if (tvExpense != null) {
                        tvExpense.setText("-" + CurrencyUtils.formatVND(totalWeeklyExpense + totalWeeklySavings));
                        Log.d(TAG, "Cập nhật UI tổng chi tiêu và tiết kiệm theo tuần: " + CurrencyUtils.formatVND(totalWeeklyExpense + totalWeeklySavings));
                    }

                    if (tvTotalBalance != null) {
                        tvTotalBalance.setText(CurrencyUtils.formatVND(totalWeeklyIncome - totalWeeklyExpense - totalWeeklySavings));
                        Log.d(TAG, "Cập nhật UI số dư theo tuần: " + CurrencyUtils.formatVND(totalWeeklyIncome - totalWeeklyExpense - totalWeeklySavings));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi truy vấn giao dịch cho biểu đồ theo tuần: " + e.getMessage(), e);
                    Toast.makeText(AnalysisActivity.this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    // Set giá trị mặc định để tránh hiển thị rỗng
                    if (tvIncome != null) tvIncome.setText(CurrencyUtils.formatVND(0));
                    if (tvExpense != null) tvExpense.setText("-" + CurrencyUtils.formatVND(0));
                    if (tvTotalBalance != null) tvTotalBalance.setText(CurrencyUtils.formatVND(0));

                    // Hiển thị biểu đồ trống
                    ArrayList<BarEntry> emptyIncomeEntries = new ArrayList<>();
                    ArrayList<BarEntry> emptyExpenseEntries = new ArrayList<>();
                    ArrayList<BarEntry> emptySavingsEntries = new ArrayList<>();

                    for (int i = 0; i < maxWeeksInMonth; i++) {
                        emptyIncomeEntries.add(new BarEntry(i, 0f));
                        emptyExpenseEntries.add(new BarEntry(i, 0f));
                        emptySavingsEntries.add(new BarEntry(i, 0f));
                    }

                    // Hiển thị biểu đồ trống
                    completeChartSetup(emptyIncomeEntries, emptyExpenseEntries, emptySavingsEntries, xAxisLabels);
                });
    }

    private void updateChartForDailyView(String userId, long startTime, long endTime) {
        // Thiết lập các khoảng thời gian cho biểu đồ theo các ngày trong tuần
        ArrayList<Entry> incomeEntries = new ArrayList<>();
        ArrayList<Entry> expenseEntries = new ArrayList<>();
        ArrayList<String> xAxisLabels = new ArrayList<>();

        // Danh sách tên các ngày trong tuần
        String[] dayNames = {"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "CN"};

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
                    float totalDailyIncome = 0;
                    float totalDailyExpense = 0;

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
                                Log.d(TAG, "Bỏ qua giao dịch không hợp lệ: ID=" + doc.getId());
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
                                    totalDailyIncome += amount.floatValue();
                                    incomeCount++;
                                    Log.d(TAG, "Giao dịch thu nhập vào " + dayNames[dayIndex] + ": " + amount);
                                } else if ("expense".equals(type) || "savings".equals(type)) {
                                    dailyExpense[dayIndex] += amount.floatValue();
                                    totalDailyExpense += amount.floatValue();
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
                    Log.d(TAG, "Tổng số giao dịch thu nhập trong tuần: " + incomeCount + ", tổng: " + totalDailyIncome);
                    Log.d(TAG, "Tổng số giao dịch chi tiêu trong tuần: " + expenseCount + ", tổng: " + totalDailyExpense);

                    // Cập nhật biểu đồ
                    updateLineChart(incomeEntries, expenseEntries, xAxisLabels);

                    // Cập nhật giao diện tổng thu nhập/chi tiêu
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
            Intent intent = new Intent(this, NotiActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi mở màn hình thông báo: " + e.getMessage(), e);
            Toast.makeText(this, "Không thể mở thông báo", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private void updateChartForMonthlyView(String userId, long startTime, long endTime) {
        // Thiết lập các khoảng thời gian cho biểu đồ hàng tháng
        ArrayList<Entry> incomeEntries = new ArrayList<>();
        ArrayList<Entry> expenseEntries = new ArrayList<>();
        ArrayList<String> xAxisLabels = new ArrayList<>();

        // Danh sách tên tháng
        String[] monthNames = {"T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "T10", "T11", "T12"};

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
     * Cập nhật biểu đồ với dữ liệu theo năm
     * @param userId ID của người dùng
     */
    private void updateChartForYearView(String userId) {
        // Thiết lập các khoảng thời gian cho biểu đồ theo năm
        ArrayList<Entry> incomeEntries = new ArrayList<>();
        ArrayList<Entry> expenseEntries = new ArrayList<>();
        ArrayList<String> xAxisLabels = new ArrayList<>();

        // Danh sách các năm (5 năm gần đây)
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int[] years = {currentYear-4, currentYear-3, currentYear-2, currentYear-1, currentYear};

        // Khởi tạo dữ liệu rỗng cho 5 năm
        for (int i = 0; i < years.length; i++) {
            xAxisLabels.add(String.valueOf(years[i]));
            incomeEntries.add(new Entry(i, 0));
            expenseEntries.add(new Entry(i, 0));
        }

        Log.d(TAG, "Bắt đầu truy vấn giao dịch cho biểu đồ năm");

        // Truy cập subcollection transactions của người dùng
        FirebaseFirestore.getInstance().collection("users").document(userId).collection("transactions")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Đã tìm thấy " + queryDocumentSnapshots.size() + " giao dịch tổng cộng");

                    // Tạo mảng để lưu tổng thu nhập và chi tiêu theo năm
                    float[] yearlyIncome = new float[years.length];
                    float[] yearlyExpense = new float[years.length];

                    // Đếm số lượng giao dịch chi tiêu và thu nhập
                    int expenseCount = 0;
                    int incomeCount = 0;
                    float totalYearlyIncome = 0;
                    float totalYearlyExpense = 0;

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
                                Log.d(TAG, "Bỏ qua giao dịch không hợp lệ: ID=" + doc.getId());
                                continue; // Bỏ qua giao dịch không hợp lệ
                            }

                            // Xác định năm của giao dịch
                            Calendar transactionCal = Calendar.getInstance();
                            transactionCal.setTimeInMillis(date);
                            int transactionYear = transactionCal.get(Calendar.YEAR);

                            // Tìm index của năm trong mảng
                            int yearIndex = -1;
                            for (int i = 0; i < years.length; i++) {
                                if (years[i] == transactionYear) {
                                    yearIndex = i;
                                    break;
                                }
                            }

                            if (yearIndex >= 0) {
                                if ("income".equals(type)) {
                                    yearlyIncome[yearIndex] += amount.floatValue();
                                    totalYearlyIncome += amount.floatValue();
                                    incomeCount++;
                                    Log.d(TAG, "Giao dịch thu nhập năm " + transactionYear + ": " + amount);
                                } else if ("expense".equals(type) || "savings".equals(type)) {
                                    yearlyExpense[yearIndex] += amount.floatValue();
                                    totalYearlyExpense += amount.floatValue();
                                    expenseCount++;
                                    Log.d(TAG, "Giao dịch " + type + " năm " + transactionYear + ": " + amount);
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Lỗi khi xử lý giao dịch: " + e.getMessage() + ", ID=" + doc.getId());
                        }
                    }

                    // Cập nhật entries từ dữ liệu đã tính toán
                    for (int i = 0; i < years.length; i++) {
                        incomeEntries.set(i, new Entry(i, yearlyIncome[i]));
                        expenseEntries.set(i, new Entry(i, yearlyExpense[i]));
                        Log.d(TAG, "Dữ liệu năm " + years[i] + ": Thu nhập=" + yearlyIncome[i] + ", Chi tiêu=" + yearlyExpense[i]);
                    }

                    // Log thông tin tổng hợp
                    Log.d(TAG, "Tổng số giao dịch thu nhập: " + incomeCount + ", tổng: " + totalYearlyIncome);
                    Log.d(TAG, "Tổng số giao dịch chi tiêu và tiết kiệm: " + expenseCount + ", tổng: " + totalYearlyExpense);

                    // Cập nhật biểu đồ
                    updateLineChart(incomeEntries, expenseEntries, xAxisLabels);

                    // Cập nhật giao diện tổng thu nhập/chi tiêu
                    if (tvIncome != null) {
                        tvIncome.setText(CurrencyUtils.formatVND(totalYearlyIncome));
                        Log.d(TAG, "Cập nhật UI tổng thu nhập: " + CurrencyUtils.formatVND(totalYearlyIncome));
                    }

                    if (tvExpense != null) {
                        tvExpense.setText("-" + CurrencyUtils.formatVND(totalYearlyExpense));
                        Log.d(TAG, "Cập nhật UI tổng chi tiêu và tiết kiệm: " + CurrencyUtils.formatVND(totalYearlyExpense));
                    }

                    if (tvTotalBalance != null) {
                        tvTotalBalance.setText(CurrencyUtils.formatVND(totalYearlyIncome - totalYearlyExpense));
                        Log.d(TAG, "Cập nhật UI số dư: " + CurrencyUtils.formatVND(totalYearlyIncome - totalYearlyExpense));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi truy vấn giao dịch cho biểu đồ năm: " + e.getMessage(), e);
                    Toast.makeText(AnalysisActivity.this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    // Set giá trị mặc định để tránh hiển thị rỗng
                    if (tvIncome != null) tvIncome.setText(CurrencyUtils.formatVND(0));
                    if (tvExpense != null) tvExpense.setText("-" + CurrencyUtils.formatVND(0));
                    if (tvTotalBalance != null) tvTotalBalance.setText(CurrencyUtils.formatVND(0));

                    // Hiển thị biểu đồ trống
                    ArrayList<Entry> emptyIncomeEntries = new ArrayList<>();
                    ArrayList<Entry> emptyExpenseEntries = new ArrayList<>();
                    for (int i = 0; i < years.length; i++) {
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
            // Tính tổng income và expense từ các entries
            float totalIncome = 0;
            float totalExpense = 0;

            for (Entry entry : incomeEntries) {
                totalIncome += entry.getY();
            }

            for (Entry entry : expenseEntries) {
                totalExpense += entry.getY();
            }

            // Cập nhật UI hiển thị tổng thu nhập và chi tiêu ở dưới cùng
            if (tvIncome != null) {
                tvIncome.setText(CurrencyUtils.formatVND(totalIncome));
                Log.d(TAG, "Cập nhật UI thu nhập trên biểu đồ: " + CurrencyUtils.formatVND(totalIncome));
            }

            if (tvExpense != null) {
                tvExpense.setText("-" + CurrencyUtils.formatVND(totalExpense));
                Log.d(TAG, "Cập nhật UI chi tiêu trên biểu đồ: " + CurrencyUtils.formatVND(totalExpense));
            }

            if (tvTotalBalance != null) {
                tvTotalBalance.setText(CurrencyUtils.formatVND(totalIncome - totalExpense));
                Log.d(TAG, "Cập nhật UI số dư trên biểu đồ: " + CurrencyUtils.formatVND(totalIncome - totalExpense));
            }

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

            // Điền dữ liệu từ incomeEntries và expenseEntries hiện có
            for (int i = 0; i < xAxisLabels.size(); i++) {
                // Đưa dữ liệu thu nhập vào
                float incomeValue = (i < incomeEntries.size()) ? incomeEntries.get(i).getY() : 0f;
                incomeBarEntries.add(new BarEntry(i, incomeValue));

                // Đưa dữ liệu chi tiêu vào
                float expenseValue = (i < expenseEntries.size()) ? expenseEntries.get(i).getY() : 0f;
                expenseBarEntries.add(new BarEntry(i, expenseValue));

                // Giá trị mặc định cho tiết kiệm
                savingsBarEntries.add(new BarEntry(i, 0f));

                // Ghi log để kiểm tra dữ liệu
                Log.d(TAG, "Dữ liệu ban đầu - " + xAxisLabels.get(i) +
                        ": Thu nhập=" + incomeValue +
                        ", Chi tiêu=" + expenseValue);
            }

            // Chỉ truy vấn thêm dữ liệu tiết kiệm từ Firestore nếu cần
            // Đối với tab Daily, chúng ta đã tính toán chi tiêu rồi, nên có thể hoàn thành thiết lập luôn
            if (xAxisLabels.size() > 0 && (xAxisLabels.get(0).equals("Thứ 2") || xAxisLabels.get(0).matches("\\d{2}/\\d{2}"))) {
                // Đối với tab Daily, không cần truy vấn thêm, dùng dữ liệu đã có
                completeChartSetup(incomeBarEntries, expenseBarEntries, savingsBarEntries, xAxisLabels);
                return;
            }

            // Truy cập vào Firestore để lấy thêm thông tin giao dịch tiết kiệm
            // Sử dụng lại firebaseUser và userId đã khai báo trước đó

            // Truy cập vào Firestore để lấy thông tin từng giao dịch
            FirebaseFirestore.getInstance().collection("users").document(userId).collection("transactions")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        try {
                            Log.d(TAG, "Tổng số giao dịch: " + queryDocumentSnapshots.size());

                            if (queryDocumentSnapshots.isEmpty()) {
                                Log.d(TAG, "Không có giao dịch nào, hiển thị biểu đồ với dữ liệu đã có");
                                completeChartSetup(incomeBarEntries, expenseBarEntries, savingsBarEntries, xAxisLabels);
                                return;
                            }

                            // Tạo mảng tạm để lưu trữ giá trị tiết kiệm và chi tiêu
                            float[] savingsValues = new float[xAxisLabels.size()];
                            float[] expenseValues = new float[xAxisLabels.size()];

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
                                        continue; // Bỏ qua giao dịch không hợp lệ
                                    }

                                    // Xử lý cho từng mục thời gian trên biểu đồ
                                    for (int i = 0; i < xAxisLabels.size(); i++) {
                                        boolean isInPeriod = isTransactionInPeriod(date, i, xAxisLabels);
                                        if (isInPeriod) {
                                            if ("savings".equals(type)) {
                                                savingsValues[i] += amount.floatValue();
                                            } else if ("expense".equals(type)) {
                                                expenseValues[i] += amount.floatValue();
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Lỗi khi xử lý giao dịch: " + e.getMessage());
                                }
                            }

                            // Cập nhật các entries dựa trên dữ liệu đã tính toán
                            for (int i = 0; i < xAxisLabels.size(); i++) {
                                // Cập nhật dữ liệu cho cột tiết kiệm
                                savingsBarEntries.set(i, new BarEntry(i, savingsValues[i]));

                                // Cập nhật lại dữ liệu cho cột chi tiêu (không bao gồm tiết kiệm)
                                expenseBarEntries.set(i, new BarEntry(i, expenseValues[i]));

                                Log.d(TAG, "Mục " + i + " - " + xAxisLabels.get(i) + ": Thu nhập=" +
                                        incomeBarEntries.get(i).getY() + ", Chi tiêu=" + expenseBarEntries.get(i).getY() +
                                        ", Tiết kiệm=" + savingsValues[i]);
                            }

                            // Hoàn thành thiết lập biểu đồ
                            completeChartSetup(incomeBarEntries, expenseBarEntries, savingsBarEntries, xAxisLabels);

                        } catch (Exception e) {
                            Log.e(TAG, "Lỗi khi xử lý dữ liệu giao dịch: " + e.getMessage());
                            Toast.makeText(AnalysisActivity.this, "Lỗi khi xử lý dữ liệu", Toast.LENGTH_SHORT).show();
                            // Vẫn hiển thị biểu đồ với dữ liệu đã có
                            completeChartSetup(incomeBarEntries, expenseBarEntries, savingsBarEntries, xAxisLabels);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Lỗi khi truy vấn giao dịch: " + e.getMessage());
                        Toast.makeText(AnalysisActivity.this, "Lỗi khi tải dữ liệu giao dịch", Toast.LENGTH_SHORT).show();
                        // Vẫn hiển thị biểu đồ với dữ liệu đã có
                        completeChartSetup(incomeBarEntries, expenseBarEntries, savingsBarEntries, xAxisLabels);
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
            // Tính tổng từ các cột trên biểu đồ
            float totalIncome = 0;
            float totalExpense = 0;
            float totalSavings = 0;

            for (BarEntry entry : incomeBarEntries) {
                totalIncome += entry.getY();
            }

            for (BarEntry entry : expenseBarEntries) {
                totalExpense += entry.getY();
            }

            for (BarEntry entry : savingsBarEntries) {
                totalSavings += entry.getY();
            }

            // Cập nhật UI với tổng hợp từ biểu đồ
            if (tvIncome != null) {
                tvIncome.setText(CurrencyUtils.formatVND(totalIncome));
                Log.d(TAG, "Cập nhật UI thu nhập từ biểu đồ: " + CurrencyUtils.formatVND(totalIncome));
            }

            if (tvExpense != null) {
                tvExpense.setText("-" + CurrencyUtils.formatVND(totalExpense + totalSavings));
                Log.d(TAG, "Cập nhật UI chi tiêu từ biểu đồ: " + CurrencyUtils.formatVND(totalExpense + totalSavings));
            }

            if (tvTotalBalance != null) {
                tvTotalBalance.setText(CurrencyUtils.formatVND(totalIncome - totalExpense - totalSavings));
                Log.d(TAG, "Cập nhật UI số dư từ biểu đồ: " + CurrencyUtils.formatVND(totalIncome - totalExpense - totalSavings));
            }

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
        } else if (label.matches("T\\d{1,2}")) { // Định dạng Tx (T1, T2, ...)
            // Đây là biểu đồ theo tháng
            int transactionMonth = transactionCal.get(Calendar.MONTH);
            String monthLabel = "T" + (transactionMonth + 1); // T1, T2, ...
            return label.equals(monthLabel);
        } else if (label.matches("\\d{4}")) { // Định dạng năm 4 chữ số
            // Đây là biểu đồ theo năm
            int transactionYear = transactionCal.get(Calendar.YEAR);
            return label.equals(String.valueOf(transactionYear));
        } else if (label.matches("\\w{3}")) { // Định dạng ba chữ cái (Jan, Feb, ...)
            // Đây là biểu đồ theo tháng (định dạng tiếng Anh)
            String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
            int transactionMonth = transactionCal.get(Calendar.MONTH);
            return label.equals(monthNames[transactionMonth]);
        }

        return false; // Mặc định không thuộc khoảng thời gian này
    }

    private void calculateMonthlyIncomeAndExpense(String userId, long startTime, long endTime) {
        FirebaseFirestore.getInstance().collection("users").document(userId).collection("transactions")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Tính toán thu chi cho tháng từ " + new Date(startTime) + " đến " + new Date(endTime));

                    double monthlyIncome = 0;
                    double monthlyExpense = 0;
                    double monthlySavings = 0;

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            String type = doc.getString("type");
                            Double amount = doc.getDouble("amount");

                            // Xử lý trường date có thể ở nhiều định dạng khác nhau
                            Long date = null;

                            // Kiểm tra các định dạng date có thể có
                            if (doc.get("date") instanceof com.google.firebase.Timestamp) {
                                com.google.firebase.Timestamp timestamp = doc.getTimestamp("date");
                                if (timestamp != null) {
                                    date = timestamp.toDate().getTime();
                                }
                            } else if (doc.get("date") instanceof String) {
                                try {
                                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                    Date parsedDate = sdf.parse(doc.getString("date"));
                                    if (parsedDate != null) {
                                        date = parsedDate.getTime();
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Lỗi khi phân tích chuỗi ngày: " + e.getMessage());
                                }
                            } else {
                                try {
                                    date = doc.getLong("date");
                                } catch (Exception e) {
                                    Log.e(TAG, "Không thể lấy date dưới dạng Long: " + e.getMessage());
                                }
                            }

                            if (type == null || amount == null || date == null) {
                                continue; // Bỏ qua giao dịch không hợp lệ
                            }

                            // Kiểm tra nếu giao dịch thuộc tháng hiện tại
                            if (date >= startTime && date <= endTime) {
                                if ("income".equals(type)) {
                                    monthlyIncome += amount;
                                    Log.d(TAG, "Thu nhập tháng này: " + amount);
                                } else if ("expense".equals(type)) {
                                    monthlyExpense += amount;
                                    Log.d(TAG, "Chi tiêu tháng này: " + amount);
                                } else if ("savings".equals(type)) {
                                    monthlySavings += amount;
                                    Log.d(TAG, "Tiết kiệm tháng này: " + amount);
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Lỗi khi xử lý giao dịch: " + e.getMessage());
                        }
                    }

                    // Cập nhật UI
                    if (tvIncome != null) {
                        tvIncome.setText(CurrencyUtils.formatVND(monthlyIncome));
                        Log.d(TAG, "Cập nhật thu nhập tháng: " + CurrencyUtils.formatVND(monthlyIncome));
                    }

                    if (tvExpense != null) {
                        tvExpense.setText("-" + CurrencyUtils.formatVND(monthlyExpense + monthlySavings));
                        Log.d(TAG, "Cập nhật chi tiêu tháng: " + CurrencyUtils.formatVND(monthlyExpense + monthlySavings));
                    }

                    if (tvTotalBalance != null) {
                        tvTotalBalance.setText(CurrencyUtils.formatVND(monthlyIncome - monthlyExpense - monthlySavings));
                        Log.d(TAG, "Cập nhật số dư tháng: " + CurrencyUtils.formatVND(monthlyIncome - monthlyExpense - monthlySavings));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi tính toán thu chi cho tháng: " + e.getMessage());

                    if (tvIncome != null) tvIncome.setText(CurrencyUtils.formatVND(0));
                    if (tvExpense != null) tvExpense.setText("-" + CurrencyUtils.formatVND(0));
                    if (tvTotalBalance != null) tvTotalBalance.setText(CurrencyUtils.formatVND(0));
                });
    }

    private void calculateYearlyIncomeAndExpense(String userId, long startTime, long endTime) {
        FirebaseFirestore.getInstance().collection("users").document(userId).collection("transactions")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Tính toán thu chi cho năm từ " + new Date(startTime) + " đến " + new Date(endTime));

                    double yearlyIncome = 0;
                    double yearlyExpense = 0;
                    double yearlySavings = 0;

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            String type = doc.getString("type");
                            Double amount = doc.getDouble("amount");

                            // Xử lý trường date có thể ở nhiều định dạng khác nhau
                            Long date = null;

                            // Kiểm tra các định dạng date có thể có
                            if (doc.get("date") instanceof com.google.firebase.Timestamp) {
                                com.google.firebase.Timestamp timestamp = doc.getTimestamp("date");
                                if (timestamp != null) {
                                    date = timestamp.toDate().getTime();
                                }
                            } else if (doc.get("date") instanceof String) {
                                try {
                                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                    Date parsedDate = sdf.parse(doc.getString("date"));
                                    if (parsedDate != null) {
                                        date = parsedDate.getTime();
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Lỗi khi phân tích chuỗi ngày: " + e.getMessage());
                                }
                            } else {
                                try {
                                    date = doc.getLong("date");
                                } catch (Exception e) {
                                    Log.e(TAG, "Không thể lấy date dưới dạng Long: " + e.getMessage());
                                }
                            }

                            if (type == null || amount == null || date == null) {
                                continue; // Bỏ qua giao dịch không hợp lệ
                            }

                            // Kiểm tra nếu giao dịch thuộc năm hiện tại
                            if (date >= startTime && date <= endTime) {
                                if ("income".equals(type)) {
                                    yearlyIncome += amount;
                                    Log.d(TAG, "Thu nhập năm nay: " + amount);
                                } else if ("expense".equals(type)) {
                                    yearlyExpense += amount;
                                    Log.d(TAG, "Chi tiêu năm nay: " + amount);
                                } else if ("savings".equals(type)) {
                                    yearlySavings += amount;
                                    Log.d(TAG, "Tiết kiệm năm nay: " + amount);
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Lỗi khi xử lý giao dịch: " + e.getMessage());
                        }
                    }

                    // Cập nhật UI
                    if (tvIncome != null) {
                        tvIncome.setText(CurrencyUtils.formatVND(yearlyIncome));
                        Log.d(TAG, "Cập nhật thu nhập năm: " + CurrencyUtils.formatVND(yearlyIncome));
                    }

                    if (tvExpense != null) {
                        tvExpense.setText("-" + CurrencyUtils.formatVND(yearlyExpense + yearlySavings));
                        Log.d(TAG, "Cập nhật chi tiêu năm: " + CurrencyUtils.formatVND(yearlyExpense + yearlySavings));
                    }

                    if (tvTotalBalance != null) {
                        tvTotalBalance.setText(CurrencyUtils.formatVND(yearlyIncome - yearlyExpense - yearlySavings));
                        Log.d(TAG, "Cập nhật số dư năm: " + CurrencyUtils.formatVND(yearlyIncome - yearlyExpense - yearlySavings));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi tính toán thu chi cho năm: " + e.getMessage());

                    if (tvIncome != null) tvIncome.setText(CurrencyUtils.formatVND(0));
                    if (tvExpense != null) tvExpense.setText("-" + CurrencyUtils.formatVND(0));
                    if (tvTotalBalance != null) tvTotalBalance.setText(CurrencyUtils.formatVND(0));
                });
    }

    // Thêm phương thức chuyển đến màn hình tìm kiếm
    private void navigateToSearch() {
        try {
            Log.d(TAG, "Chuyển đến màn hình tìm kiếm");
            Intent intent = new Intent(AnalysisActivity.this, SearchActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi chuyển đến màn hình tìm kiếm: " + e.getMessage(), e);
            Toast.makeText(this, "Không thể mở trang tìm kiếm", Toast.LENGTH_SHORT).show();
        }
    }
} 