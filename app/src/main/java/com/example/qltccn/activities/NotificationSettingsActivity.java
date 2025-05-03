package com.example.qltccn.activities;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.widget.TimePicker;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.qltccn.R;
import com.example.qltccn.utils.NotificationUtils;

import java.util.Calendar;

public class NotificationSettingsActivity extends AppCompatActivity {

    // UI elements
    private ImageButton toolbarBackBtn;
    private ImageView idNoti;
    private ImageView iconHome, iconChart, iconTrans, iconCategory, iconUser;
    private Button testNotificationBtn;
    
    private SwitchCompat switchGeneralNotification, switchSound, switchSilentCall, switchVibrate,
            switchTransactionUpdate, switchExpenseReminder, switchBudgetNotifications, switchLowBalanceAlerts;

    // Preference keys
    private static final String PREF_NAME = "notification_settings";
    private static final String KEY_GENERAL = "general_notification";
    private static final String KEY_SOUND = "sound";
    private static final String KEY_SILENT_CALL = "silent_call";
    private static final String KEY_VIBRATE = "vibrate";
    private static final String KEY_TRANSACTION = "transaction_update";
    private static final String KEY_EXPENSE = "expense_reminder";
    private static final String KEY_BUDGET = "budget_notifications";
    private static final String KEY_LOW_BALANCE = "low_balance_alerts";
    private static final String KEY_REMINDER_HOUR = "reminder_hour";
    private static final String KEY_REMINDER_MINUTE = "reminder_minute";
    
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);

        // Initialize SharedPreferences
        preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Initialize UI elements
        initUI();

        // Load saved preferences
        loadSavedPreferences();

        // Set click listeners
        setClickListeners();
        
        // Đăng ký channel thông báo
        NotificationUtils.createNotificationChannel(this);
        
        // Cập nhật định dạng thời gian cho tất cả thông báo
        NotificationUtils.updateNotificationsTimeFormat(this);
        
        // Thêm Toast thông báo đã mở màn hình cài đặt
        Toast.makeText(this, "Đã mở màn hình cài đặt thông báo", Toast.LENGTH_SHORT).show();
    }

    private void initUI() {
        // Toolbar
        toolbarBackBtn = findViewById(R.id.toolbarBackBtn);
        idNoti = findViewById(R.id.idNoti);

        // Nút kiểm tra thông báo
        testNotificationBtn = findViewById(R.id.testNotificationBtn);

        // Switches
        switchGeneralNotification = findViewById(R.id.switchGeneralNotification);
        switchSound = findViewById(R.id.switchSound);
        switchSilentCall = findViewById(R.id.switchSilentCall);
        switchVibrate = findViewById(R.id.switchVibrate);
        switchTransactionUpdate = findViewById(R.id.switchTransactionUpdate);
        switchExpenseReminder = findViewById(R.id.switchExpenseReminder);
        switchBudgetNotifications = findViewById(R.id.switchBudgetNotifications);
        switchLowBalanceAlerts = findViewById(R.id.switchLowBalanceAlerts);

        // Footer icons
        iconHome = findViewById(R.id.iconHome);
        iconChart = findViewById(R.id.iconChart);
        iconTrans = findViewById(R.id.iconTrans);
        iconCategory = findViewById(R.id.iconCategory);
        iconUser = findViewById(R.id.iconUser);
    }

    private void loadSavedPreferences() {
        // Load saved switch states from SharedPreferences
        switchGeneralNotification.setChecked(preferences.getBoolean(KEY_GENERAL, true));
        switchSound.setChecked(preferences.getBoolean(KEY_SOUND, true));
        switchSilentCall.setChecked(preferences.getBoolean(KEY_SILENT_CALL, true));
        switchVibrate.setChecked(preferences.getBoolean(KEY_VIBRATE, true));
        switchTransactionUpdate.setChecked(preferences.getBoolean(KEY_TRANSACTION, true));
        switchExpenseReminder.setChecked(preferences.getBoolean(KEY_EXPENSE, false));
        switchBudgetNotifications.setChecked(preferences.getBoolean(KEY_BUDGET, true));
        switchLowBalanceAlerts.setChecked(preferences.getBoolean(KEY_LOW_BALANCE, false));
    }

    private void setClickListeners() {
        // Toolbar actions
        toolbarBackBtn.setOnClickListener(v -> {
            Toast.makeText(this, "Quay lại", Toast.LENGTH_SHORT).show();
            finish();
        });
        
        idNoti.setOnClickListener(v -> {
            // Mở màn hình danh sách thông báo
            startActivity(new Intent(NotificationSettingsActivity.this, NotiActivity.class));
        });
        
        // Nút kiểm tra thông báo
        testNotificationBtn.setOnClickListener(v -> {
            Toast.makeText(this, "Đang gửi thông báo kiểm tra...", Toast.LENGTH_SHORT).show();
            sendTestNotification();
        });

        // Switch change listeners
        switchGeneralNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePreference(KEY_GENERAL, isChecked);
            updateDependentSwitches(isChecked);
            
            // Gửi thông báo thử nghiệm nếu bật
            if (isChecked) {
                Toast.makeText(this, "Đã bật thông báo", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Đã tắt thông báo", Toast.LENGTH_SHORT).show();
            }
        });
        
        switchSound.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePreference(KEY_SOUND, isChecked);
            if (isChecked) {
                Toast.makeText(this, "Đã bật âm thanh thông báo", Toast.LENGTH_SHORT).show();
            }
        });
        
        switchSilentCall.setOnCheckedChangeListener((buttonView, isChecked) -> 
                savePreference(KEY_SILENT_CALL, isChecked));
        
        switchVibrate.setOnCheckedChangeListener((buttonView, isChecked) -> 
                savePreference(KEY_VIBRATE, isChecked));
        
        switchTransactionUpdate.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePreference(KEY_TRANSACTION, isChecked);
            
            // Tạo thông báo mẫu về cập nhật giao dịch
            if (isChecked && preferences.getBoolean(KEY_GENERAL, true)) {
                // Tạo một thông báo mẫu về giao dịch
                NotificationUtils.addNotification(
                    this, 
                    "Thông báo giao dịch",
                    "Bạn sẽ nhận được thông báo khi có giao dịch mới",
                    "transaction"
                );
            }
        });
        
        switchExpenseReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePreference(KEY_EXPENSE, isChecked);
            
            if (isChecked) {
                // Hiển thị dialog chọn thời gian nhắc nhở
                showTimePickerDialog();
            } else {
                // Tắt thông báo nhắc nhở
                NotificationUtils.setDailyExpenseReminder(this, 20, 0, false);
            }
        });
        
        switchBudgetNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePreference(KEY_BUDGET, isChecked);
            NotificationUtils.setBudgetNotification(this, isChecked);
            
            // Tạo thông báo mẫu về ngân sách
            if (isChecked && preferences.getBoolean(KEY_GENERAL, true)) {
                NotificationUtils.addNotification(
                    this, 
                    "Thông báo ngân sách", 
                    "Bạn sẽ nhận được thông báo khi gần hết ngân sách", 
                    "warning"
                );
            }
        });
        
        switchLowBalanceAlerts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePreference(KEY_LOW_BALANCE, isChecked);
            
            // Tạo thông báo mẫu về số dư thấp
            if (isChecked && preferences.getBoolean(KEY_GENERAL, true)) {
                NotificationUtils.addNotification(
                    this, 
                    "Cảnh báo số dư thấp", 
                    "Bạn sẽ nhận được thông báo khi số dư tài khoản thấp", 
                    "warning"
                );
                
                // Thiết lập ngưỡng cảnh báo (ví dụ: 100,000 VND)
                double threshold = 100000.0;
                
                // Kiểm tra số dư hiện tại (đây chỉ là ví dụ, thực tế cần lấy từ dữ liệu tài khoản)
                double currentBalance = 120000.0; // Giả sử có sẵn
                
                // Kiểm tra và thông báo nếu số dư thấp
                NotificationUtils.checkLowBalanceAndNotify(this, currentBalance, threshold);
            }
        });

        // Footer navigation
        iconHome.setOnClickListener(v -> {
            startActivity(new Intent(NotificationSettingsActivity.this, HomeActivity.class));
            finish();
        });
        
        iconChart.setOnClickListener(v -> {
            startActivity(new Intent(NotificationSettingsActivity.this, AnalysisActivity.class));
            finish();
        });
        
        iconTrans.setOnClickListener(v -> {
            startActivity(new Intent(NotificationSettingsActivity.this, TranActivity.class));
            finish();
        });
        
        iconCategory.setOnClickListener(v -> {
            startActivity(new Intent(NotificationSettingsActivity.this, CategoryActivity.class));
            finish();
        });
        
        iconUser.setOnClickListener(v -> {
            startActivity(new Intent(NotificationSettingsActivity.this, ProfileActivity.class));
            finish();
        });
    }

    /**
     * Hiển thị dialog chọn thời gian nhắc nhở
     */
    private void showTimePickerDialog() {
        // Lấy thời gian đã lưu hoặc mặc định (20:00)
        int hour = preferences.getInt(KEY_REMINDER_HOUR, 20);
        int minute = preferences.getInt(KEY_REMINDER_MINUTE, 0);
        
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minutes) -> {
                    // Lưu thời gian đã chọn
                    saveReminderTime(hourOfDay, minutes);
                    
                    // Thiết lập thông báo
                    NotificationUtils.setDailyExpenseReminder(this, hourOfDay, minutes, true);
                    
                    // Hiển thị thông báo
                    Toast.makeText(this, 
                            "Đã đặt nhắc nhở lúc " + String.format("%02d:%02d", hourOfDay, minutes), 
                            Toast.LENGTH_SHORT).show();
                },
                hour,
                minute,
                true
        );
        
        timePickerDialog.setTitle("Chọn thời gian nhắc nhở");
        timePickerDialog.show();
    }
    
    /**
     * Lưu thời gian nhắc nhở đã chọn
     */
    private void saveReminderTime(int hour, int minute) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(KEY_REMINDER_HOUR, hour);
        editor.putInt(KEY_REMINDER_MINUTE, minute);
        editor.apply();
    }

    private void savePreference(String key, boolean value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    private void updateDependentSwitches(boolean enabled) {
        // When general notifications are disabled, disable all other notification settings
        switchSound.setEnabled(enabled);
        switchSilentCall.setEnabled(enabled);
        switchVibrate.setEnabled(enabled);
        switchTransactionUpdate.setEnabled(enabled);
        switchExpenseReminder.setEnabled(enabled);
        switchBudgetNotifications.setEnabled(enabled);
        switchLowBalanceAlerts.setEnabled(enabled);
    }

    private void showNotificationInfo() {
        // Hiển thị thông tin về cài đặt thông báo
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thông tin về thông báo");
        builder.setMessage(
            "Cài đặt thông báo cho phép bạn kiểm soát các loại thông báo bạn muốn nhận:\n\n" +
            "- Thông báo chung: Bật/tắt tất cả thông báo\n" +
            "- Âm thanh: Phát âm thanh khi có thông báo\n" +
            "- Rung: Thiết bị sẽ rung khi có thông báo\n" +
            "- Cập nhật giao dịch: Thông báo khi có giao dịch mới\n" +
            "- Nhắc nhở chi tiêu: Thông báo nhắc nhở chi tiêu định kỳ\n" +
            "- Thông báo ngân sách: Thông báo khi sắp vượt quá ngân sách\n" +
            "- Cảnh báo số dư thấp: Thông báo khi tài khoản có số dư thấp"
        );
        builder.setPositiveButton("Đóng", null);
        builder.show();
    }

    /**
     * Gửi thông báo kiểm tra
     */
    private void sendTestNotification() {
        try {
            // Hiển thị Toast thông báo chi tiết
            Toast.makeText(this, "Đang gửi thông báo kiểm tra...", Toast.LENGTH_SHORT).show();
            
            // Đảm bảo kênh thông báo đã được tạo
            NotificationUtils.createNotificationChannel(this);
            
            // Log trước khi gửi
            Log.d("NotificationSettingsActivity", "Bắt đầu gửi thông báo kiểm tra");
            
            // Gọi phương thức gửi thông báo kiểm tra trực tiếp
            NotificationUtils.sendTestNotification(this);
            
            // Lưu thông báo vào danh sách thông báo
            NotificationUtils.addNotification(
                this,
                "Thông báo kiểm tra",
                "Đây là thông báo kiểm tra. Nếu bạn thấy thông báo này, hệ thống thông báo đang hoạt động.",
                "reminder"
            );
            
            // Toast thành công
            Toast.makeText(this, "Đã gửi thông báo kiểm tra!", Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            Log.e("NotificationSettingsActivity", "Lỗi gửi thông báo: " + e.getMessage(), e);
            
            // Hiển thị thông báo lỗi đầy đủ
            new AlertDialog.Builder(this)
                .setTitle("Lỗi gửi thông báo")
                .setMessage("Chi tiết lỗi: " + e.getMessage() + "\n\nNếu bạn không nhận được thông báo, hãy kiểm tra quyền thông báo trong cài đặt của thiết bị.")
                .setPositiveButton("Đóng", null)
                .show();
            
            Toast.makeText(this, "Không thể gửi thông báo: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
} 