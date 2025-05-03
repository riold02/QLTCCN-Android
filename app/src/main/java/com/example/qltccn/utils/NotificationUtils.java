package com.example.qltccn.utils;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.qltccn.R;
import com.example.qltccn.activities.NotiActivity;
import com.example.qltccn.models.Notification;
import com.example.qltccn.models.Transaction;
import com.example.qltccn.receivers.NotificationReceiver;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Lớp tiện ích để quản lý thông báo cục bộ
 */
public class NotificationUtils {
    
    private static final String TAG = "NotificationUtils";
    private static final String PREFS_NAME = "notification_preferences";
    private static final String NOTIFICATION_SETTINGS = "notification_settings";
    private static final String KEY_NOTIFICATIONS = "notifications";
    private static final String CHANNEL_ID = "qltccn_notification_channel";
    private static final String CHANNEL_NAME = "QLTCCN Notifications";
    private static final String CHANNEL_DESC = "Thông báo từ ứng dụng Quản lý thu chi cá nhân";
    
    // Notification ID counter
    private static int notificationIdCounter = 1;
    
    /**
     * Thêm thông báo mới
     * @param context Context
     * @param title Tiêu đề thông báo
     * @param message Nội dung thông báo
     * @param type Loại thông báo (transaction, warning, reminder, savings, tip)
     */
    public static void addNotification(Context context, String title, String message, String type) {
        try {
            // Định dạng thời gian
            String timeStr = "Vừa xong";
            
            // Tạo thông báo mới
            Notification notification = new Notification(
                    generateId(),
                    title,
                    message,
                    timeStr,
                    type,
                    false,
                    System.currentTimeMillis()
            );
            
            // Lấy danh sách thông báo hiện tại
            List<Notification> notifications = getNotifications(context);
            
            // Thêm thông báo mới vào đầu danh sách
            notifications.add(0, notification);
            
            // Lưu danh sách vào SharedPreferences
            saveNotifications(context, notifications);
            
            Log.d(TAG, "Đã thêm thông báo: " + title);
            
            // Hiển thị thông báo dạng push
            showPushNotification(context, title, message, type);
            
        } catch (Exception e) {
            Log.e(TAG, "Lỗi thêm thông báo: " + e.getMessage());
        }
    }
    
    /**
     * Hiển thị thông báo dạng push notification
     * @param context Context
     * @param title Tiêu đề thông báo
     * @param message Nội dung thông báo
     * @param type Loại thông báo
     */
    public static void showPushNotification(Context context, String title, String message, String type) {
        try {
            Log.d(TAG, "Bắt đầu hiển thị thông báo: " + title);
            
            // Tạo notification channel cho Android 8.0+
            createNotificationChannel(context);
            
            // Intent khi nhấn vào thông báo
            Intent intent = new Intent(context, NotiActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            
            // Phải dùng PendingIntent.FLAG_IMMUTABLE hoặc PendingIntent.FLAG_MUTABLE từ Android 12+
            int pendingIntentFlag = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S 
                    ? PendingIntent.FLAG_IMMUTABLE 
                    : PendingIntent.FLAG_UPDATE_CURRENT;
            
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context, 
                    (int) System.currentTimeMillis(), // Tạo request code khác nhau mỗi lần
                    intent, 
                    pendingIntentFlag
            );
            
            // Tạo thông báo với cấu hình cơ bản
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_noti)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH) // Ưu tiên cao hơn
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setDefaults(NotificationCompat.DEFAULT_ALL); // Sử dụng cài đặt mặc định
            
            // Lấy NotificationManager
            NotificationManager notificationManager = 
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            
            // Kiểm tra không null và hiển thị thông báo
            if (notificationManager != null) {
                int notificationId = (int) System.currentTimeMillis(); // ID duy nhất
                notificationManager.notify(notificationId, builder.build());
                Log.d(TAG, "Đã hiển thị push notification với ID: " + notificationId);
            } else {
                Log.e(TAG, "NotificationManager là null");
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi hiển thị push notification: " + e.getMessage(), e);
        }
    }
    
    /**
     * Tạo notification channel cho Android 8.0+
     * @param context Context
     */
    public static void createNotificationChannel(Context context) {
        // Chỉ cần thực hiện trên Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_HIGH // Tăng mức độ ưu tiên
                );
                channel.setDescription(CHANNEL_DESC);
                channel.enableVibration(true);
                channel.enableLights(true);
                
                // Đăng ký channel với hệ thống
                NotificationManager notificationManager = 
                        context.getSystemService(NotificationManager.class);
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(channel);
                    Log.d(TAG, "Đã tạo notification channel thành công");
                } else {
                    Log.e(TAG, "NotificationManager là null khi tạo channel");
                }
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi tạo notification channel: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * Lấy danh sách tất cả thông báo
     * @param context Context
     * @return Danh sách thông báo
     */
    public static List<Notification> getNotifications(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String notificationsJson = prefs.getString(KEY_NOTIFICATIONS, null);
        
        if (notificationsJson == null) {
            return new ArrayList<>();
        } else {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Notification>>(){}.getType();
            return gson.fromJson(notificationsJson, type);
        }
    }
    
    /**
     * Lưu danh sách thông báo
     * @param context Context
     * @param notifications Danh sách thông báo
     */
    private static void saveNotifications(Context context, List<Notification> notifications) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        Gson gson = new Gson();
        String notificationsJson = gson.toJson(notifications);
        
        editor.putString(KEY_NOTIFICATIONS, notificationsJson);
        editor.apply();
    }
    
    /**
     * Tạo ID ngẫu nhiên cho thông báo
     * @return ID của thông báo
     */
    private static String generateId() {
        return "notification_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 10000);
    }
    
    /**
     * Thêm thông báo khi có giao dịch chi tiêu mới
     * @param context Context
     * @param transaction Giao dịch mới
     */
    public static void addTransactionNotification(Context context, Transaction transaction) {
        if (transaction == null) return;
        
        String title = "";
        String message = "";
        String type = "transaction";
        
        // Định dạng số tiền
        String formattedAmount = CurrencyUtils.formatVND(transaction.getAmount());
        
        // Tạo thông báo dựa trên loại giao dịch
        if ("expense".equals(transaction.getType())) {
            title = "Chi tiêu mới";
            message = "Bạn vừa chi tiêu " + formattedAmount + " cho " + transaction.getCategory();
        } else if ("income".equals(transaction.getType())) {
            title = "Thu nhập mới";
            message = "Bạn vừa nhận " + formattedAmount + " từ " + transaction.getCategory();
        }
        
        // Thêm thông báo
        addNotification(context, title, message, type);
    }
    
    /**
     * Thêm thông báo khi có giao dịch tiết kiệm
     * @param context Context
     * @param amount Số tiền
     * @param goalTitle Tên mục tiêu tiết kiệm
     * @param isDeposit true nếu là gửi tiền, false nếu là rút tiền
     */
    public static void addSavingsNotification(Context context, double amount, String goalTitle, boolean isDeposit) {
        String title = "";
        String message = "";
        String type = "savings";
        
        // Định dạng số tiền
        String formattedAmount = CurrencyUtils.formatVND(amount);
        
        // Tạo thông báo dựa trên loại giao dịch tiết kiệm
        if (isDeposit) {
            title = "Gửi tiết kiệm thành công";
            message = "Bạn vừa gửi " + formattedAmount + " vào mục tiêu " + goalTitle;
        } else {
            title = "Rút tiền tiết kiệm thành công";
            message = "Bạn vừa rút " + formattedAmount + " từ mục tiêu " + goalTitle;
        }
        
        // Thêm thông báo
        addNotification(context, title, message, type);
    }
    
    /**
     * Thêm thông báo khi nạp tiền vào tài khoản
     * @param context Context
     * @param amount Số tiền nạp
     * @param note Ghi chú nạp tiền (nếu có)
     */
    public static void addDepositNotification(Context context, double amount, String note) {
        String title = "Nạp tiền thành công";
        String message = "Bạn vừa nạp " + CurrencyUtils.formatVND(amount) + " vào tài khoản";
        
        // Thêm ghi chú vào thông báo nếu có
        if (note != null && !note.isEmpty()) {
            message += " (" + note + ")";
        }
        
        // Thêm thông báo
        addNotification(context, title, message, "income");
    }
    
    /**
     * Mở màn hình thông báo
     * @param context Context
     */
    public static void openNotificationScreen(Context context) {
        Intent intent = new Intent(context, NotiActivity.class);
        context.startActivity(intent);
    }
    
    /**
     * Đánh dấu thông báo đã đọc
     * @param context Context
     * @param notificationId ID thông báo
     */
    public static void markAsRead(Context context, String notificationId) {
        List<Notification> notifications = getNotifications(context);
        
        for (Notification notification : notifications) {
            if (notification.getId().equals(notificationId)) {
                notification.setRead(true);
                break;
            }
        }
        
        saveNotifications(context, notifications);
    }
    
    /**
     * Đánh dấu tất cả thông báo đã đọc
     * @param context Context
     */
    public static void markAllAsRead(Context context) {
        List<Notification> notifications = getNotifications(context);
        
        for (Notification notification : notifications) {
            notification.setRead(true);
        }
        
        saveNotifications(context, notifications);
        Log.d(TAG, "Đã đánh dấu tất cả thông báo là đã đọc");
    }
    
    /**
     * Xóa thông báo
     * @param context Context
     * @param notificationId ID thông báo
     */
    public static void deleteNotification(Context context, String notificationId) {
        List<Notification> notifications = getNotifications(context);
        List<Notification> updatedList = new ArrayList<>();
        
        for (Notification notification : notifications) {
            if (!notification.getId().equals(notificationId)) {
                updatedList.add(notification);
            }
        }
        
        saveNotifications(context, updatedList);
        Log.d(TAG, "Đã xóa thông báo: " + notificationId);
    }
    
    /**
     * Xóa tất cả thông báo
     * @param context Context
     */
    public static void clearAllNotifications(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_NOTIFICATIONS);
        editor.apply();
        
        Log.d(TAG, "Đã xóa tất cả thông báo");
    }
    
    /**
     * Thiết lập lịch nhắc nhở chi tiêu hàng ngày
     * @param context Context
     * @param hour Giờ nhắc nhở (0-23)
     * @param minute Phút nhắc nhở (0-59)
     * @param enabled Bật/tắt nhắc nhở
     */
    public static void setDailyExpenseReminder(Context context, int hour, int minute, boolean enabled) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction(NotificationReceiver.ACTION_EXPENSE_REMINDER);
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                100,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        if (enabled) {
            // Thiết lập thời gian
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            
            // Nếu thời gian đã qua trong ngày, thêm 1 ngày
            if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
            
            // Đặt lịch lặp lại hàng ngày
            if (alarmManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(),
                            pendingIntent
                    );
                } else {
                    alarmManager.setInexactRepeating(
                            AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(),
                            AlarmManager.INTERVAL_DAY,
                            pendingIntent
                    );
                }
                
                Log.d(TAG, "Đã đặt lịch nhắc nhở chi tiêu lúc " + hour + ":" + minute);
                
                // Gửi thông báo ngay lập tức để xác nhận việc thiết lập
                addNotification(
                    context,
                    "Nhắc nhở chi tiêu đã được thiết lập",
                    "Bạn sẽ nhận được nhắc nhở chi tiêu hàng ngày lúc " + String.format("%02d:%02d", hour, minute),
                    "reminder"
                );
                
                // Lưu cài đặt vào SharedPreferences
                SharedPreferences prefs = context.getSharedPreferences(NOTIFICATION_SETTINGS, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("expense_reminder", true);
                editor.putInt("reminder_hour", hour);
                editor.putInt("reminder_minute", minute);
                editor.apply();
            }
        } else {
            // Hủy lịch
            if (alarmManager != null) {
                alarmManager.cancel(pendingIntent);
                Log.d(TAG, "Đã hủy lịch nhắc nhở chi tiêu");
                
                // Cập nhật SharedPreferences
                SharedPreferences prefs = context.getSharedPreferences(NOTIFICATION_SETTINGS, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("expense_reminder", false);
                editor.apply();
            }
        }
    }
    
    /**
     * Thiết lập thông báo khi sắp vượt ngân sách
     * @param context Context
     * @param enabled Bật/tắt thông báo
     */
    public static void setBudgetNotification(Context context, boolean enabled) {
        // Lưu trạng thái
        SharedPreferences settings = context.getSharedPreferences(NOTIFICATION_SETTINGS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("budget_notifications", enabled);
        editor.apply();
        
        if (enabled) {
            // Lưu ý: Việc kiểm tra ngân sách nên được thực hiện khi có giao dịch mới
            Log.d(TAG, "Đã bật thông báo ngân sách");
        } else {
            Log.d(TAG, "Đã tắt thông báo ngân sách");
        }
    }
    
    /**
     * Kiểm tra và gửi thông báo nếu gần vượt ngân sách
     * @param context Context
     * @param category Danh mục
     * @param currentAmount Số tiền hiện tại đã chi
     * @param budgetAmount Ngân sách cho danh mục
     */
    public static void checkBudgetAndNotify(Context context, String category, double currentAmount, double budgetAmount) {
        // Kiểm tra cài đặt
        SharedPreferences settings = context.getSharedPreferences(NOTIFICATION_SETTINGS, Context.MODE_PRIVATE);
        boolean budgetNotificationsEnabled = settings.getBoolean("budget_notifications", true);
        boolean generalEnabled = settings.getBoolean("general_notification", true);
        
        if (!generalEnabled || !budgetNotificationsEnabled) {
            return;
        }
        
        // Tính phần trăm đã sử dụng
        double percentUsed = (currentAmount / budgetAmount) * 100;
        
        // Gửi thông báo nếu đã sử dụng hơn 80% ngân sách
        if (percentUsed >= 80 && percentUsed < 100) {
            String formattedBudget = CurrencyUtils.formatVND(budgetAmount);
            String formattedCurrent = CurrencyUtils.formatVND(currentAmount);
            
            addNotification(
                context,
                "Cảnh báo ngân sách " + category,
                "Bạn đã sử dụng " + (int)percentUsed + "% ngân sách " + category + " (" 
                    + formattedCurrent + "/" + formattedBudget + ")",
                "warning"
            );
        }
        // Gửi thông báo nếu đã vượt ngân sách
        else if (percentUsed >= 100) {
            String formattedBudget = CurrencyUtils.formatVND(budgetAmount);
            String formattedCurrent = CurrencyUtils.formatVND(currentAmount);
            
            addNotification(
                context,
                "Vượt ngân sách " + category,
                "Bạn đã vượt ngân sách " + category + ": " 
                    + formattedCurrent + "/" + formattedBudget,
                "warning"
            );
        }
    }
    
    /**
     * Kiểm tra và gửi thông báo số dư thấp
     * @param context Context
     * @param balance Số dư hiện tại
     * @param threshold Ngưỡng cảnh báo (mặc định 100,000 VND)
     */
    public static void checkLowBalanceAndNotify(Context context, double balance, double threshold) {
        // Kiểm tra cài đặt
        SharedPreferences settings = context.getSharedPreferences(NOTIFICATION_SETTINGS, Context.MODE_PRIVATE);
        boolean lowBalanceEnabled = settings.getBoolean("low_balance_alerts", false);
        boolean generalEnabled = settings.getBoolean("general_notification", true);
        
        if (!generalEnabled || !lowBalanceEnabled) {
            return;
        }
        
        // Gửi thông báo nếu số dư thấp hơn ngưỡng
        if (balance < threshold) {
            String formattedBalance = CurrencyUtils.formatVND(balance);
            
            addNotification(
                context,
                "Cảnh báo số dư thấp",
                "Số dư tài khoản của bạn hiện còn " + formattedBalance,
                "warning"
            );
        }
    }
    
    /**
     * Gửi thông báo kiểm tra trực tiếp
     * @param context Context
     */
    public static void sendTestNotification(Context context) {
        try {
            Log.d(TAG, "Gửi thông báo kiểm tra trực tiếp");
            
            // Tạo notification channel
            createNotificationChannel(context);
            
            // Intent khi nhấn vào thông báo
            Intent intent = new Intent(context, NotiActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            
            // Tạo PendingIntent
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context, 
                    123456, // Request code cố định
                    intent, 
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            
            // Tạo builder thông báo
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_noti)
                    .setContentTitle("Thông báo kiểm tra")
                    .setContentText("Nếu bạn nhìn thấy thông báo này, hệ thống thông báo đã hoạt động!")
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setDefaults(NotificationCompat.DEFAULT_ALL);
            
            // Lấy NotificationManager
            NotificationManager notificationManager = 
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            
            // Gửi thông báo
            if (notificationManager != null) {
                notificationManager.notify(999, builder.build());
                Log.d(TAG, "Đã gửi thông báo kiểm tra trực tiếp");
            } else {
                Log.e(TAG, "NotificationManager là null trong sendTestNotification");
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi gửi thông báo kiểm tra: " + e.getMessage(), e);
        }
    }
} 