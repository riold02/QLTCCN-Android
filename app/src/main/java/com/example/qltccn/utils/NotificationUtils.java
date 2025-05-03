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
            // Lấy thời gian hiện tại
            long currentTimeMillis = System.currentTimeMillis();
            
            // Định dạng thời gian thành ngày giờ
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            String timeStr = sdf.format(new Date(currentTimeMillis));
            
            // Tạo thông báo mới
            Notification notification = new Notification(
                    generateId(),
                    title,
                    message,
                    timeStr,
                    type,
                    false,
                    currentTimeMillis
            );
            
            // Lấy danh sách thông báo hiện tại
            List<Notification> notifications = getNotifications(context);
            
            // Thêm thông báo mới vào đầu danh sách
            notifications.add(0, notification);
            
            // Lưu danh sách vào SharedPreferences
            saveNotifications(context, notifications);
            
            Log.d(TAG, "Đã thêm thông báo: " + title + " lúc " + timeStr);
            
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
                NotificationManager notificationManager = 
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                
                // Kiểm tra xem channel đã tồn tại chưa
                if (notificationManager != null) {
                    NotificationChannel existingChannel = notificationManager.getNotificationChannel(CHANNEL_ID);
                    if (existingChannel != null) {
                        Log.d(TAG, "Channel đã tồn tại, cập nhật thiết lập");
                        
                        // Xóa channel cũ để tạo lại với cài đặt mới (một số thiết lập không thể thay đổi sau khi đã tạo)
                        notificationManager.deleteNotificationChannel(CHANNEL_ID);
                    }
                    
                    // Tạo channel với độ ưu tiên cao nhất
                    NotificationChannel channel = new NotificationChannel(
                            CHANNEL_ID,
                            CHANNEL_NAME,
                            NotificationManager.IMPORTANCE_HIGH // Đặt mức độ quan trọng cao nhất
                    );
                    
                    // Thiết lập các tùy chọn khác
                    channel.setDescription(CHANNEL_DESC);
                    channel.enableVibration(true);
                    channel.enableLights(true);
                    channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
                    channel.setBypassDnd(true); // Vượt qua chế độ Không làm phiền
                    channel.setShowBadge(true); // Hiển thị biểu tượng thông báo
                    
                    // Cài đặt âm thanh mặc định
                    channel.setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI, 
                            new android.media.AudioAttributes.Builder()
                                .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build()
                    );
                    
                    // Đăng ký channel với hệ thống
                    notificationManager.createNotificationChannel(channel);
                    Log.d(TAG, "Đã tạo notification channel với mức độ ưu tiên cao");
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
            message += " : Nội dung: " + note ;
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
     * Gửi thông báo kiểm tra trực tiếp với độ ưu tiên cao nhất
     * @param context Context
     */
    public static void sendTestNotification(Context context) {
        try {
            Log.d(TAG, "Gửi thông báo kiểm tra trực tiếp");
            
            // Tạo notification channel với độ ưu tiên cao nhất
            createNotificationChannel(context);
            
            // Intent khi nhấn vào thông báo
            Intent intent = new Intent(context, NotiActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            
            // Tạo PendingIntent
            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flags |= PendingIntent.FLAG_IMMUTABLE;
            }
            
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context, 
                    999, // Request code
                    intent, 
                    flags
            );
            
            // Tạo builder thông báo với cấu hình đặc biệt
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_noti)
                    .setContentTitle("KIỂM TRA THÔNG BÁO")
                    .setContentText("ĐÂY LÀ THÔNG BÁO KIỂM TRA. NẾU BẠN NHẬN ĐƯỢC, HỆ THỐNG ĐANG HOẠT ĐỘNG!")
                    .setPriority(NotificationCompat.PRIORITY_MAX) // Đặt độ ưu tiên cao nhất
                    .setCategory(NotificationCompat.CATEGORY_ALARM) // Đặt danh mục báo động
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Hiển thị trên màn hình khóa
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setDefaults(NotificationCompat.DEFAULT_ALL); // Sử dụng cài đặt mặc định

            // Làm cho thông báo dạng đầy đủ khi hiển thị
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setFullScreenIntent(pendingIntent, true);
            }
            
            // Lấy NotificationManager
            NotificationManager notificationManager = 
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            
            // Gửi thông báo với ID cố định
            if (notificationManager != null) {
                // Đặt độ ưu tiên cao nhất cho kênh thông báo
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel channel = notificationManager.getNotificationChannel(CHANNEL_ID);
                    if (channel != null) {
                        channel.setImportance(NotificationManager.IMPORTANCE_HIGH);
                        notificationManager.createNotificationChannel(channel);
                    }
                }
                
                // Gửi thông báo
                notificationManager.notify(999, builder.build());
                Log.d(TAG, "Đã gửi thông báo kiểm tra trực tiếp với ID: 999");
                
                // Yêu cầu lưu ý người dùng với thiết bị chạy Android 13+
                if (Build.VERSION.SDK_INT >= 33) {
                    try {
                        // Sử dụng reflection để truy cập API REQUEST_POST_NOTIFICATIONS 
                        Class<?> activityClass = Class.forName("android.app.Activity");
                        Class<?> permClass = Class.forName("android.Manifest$permission");
                        java.lang.reflect.Field field = permClass.getField("POST_NOTIFICATIONS");
                        String permName = (String) field.get(null);
                        
                        Log.d(TAG, "Kiểm tra quyền thông báo trên Android 13+");
                    } catch (Exception e) {
                        Log.e(TAG, "Lỗi kiểm tra quyền thông báo: " + e.getMessage());
                    }
                }
            } else {
                Log.e(TAG, "NotificationManager là null trong sendTestNotification");
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi gửi thông báo kiểm tra: " + e.getMessage(), e);
        }
    }
    
    /**
     * Cập nhật định dạng thời gian cho tất cả thông báo
     * @param context Context
     */
    public static void updateNotificationsTimeFormat(Context context) {
        try {
            // Lấy danh sách thông báo hiện tại
            List<Notification> notifications = getNotifications(context);
            boolean hasChanges = false;
            
            // Duyệt qua từng thông báo và cập nhật thời gian
            for (Notification notification : notifications) {
                // Nếu thời gian là "Vừa xong" hoặc định dạng cũ, cập nhật sang định dạng mới
                if (notification.getTime().equals("Vừa xong") || !notification.getTime().contains("/")) {
                    // Lấy timestamp từ thông báo hoặc sử dụng thời gian hiện tại nếu không có
                    long timestamp = notification.getTimestamp();
                    if (timestamp == 0) {
                        timestamp = System.currentTimeMillis();
                        notification.setTimestamp(timestamp);
                    }
                    
                    // Định dạng lại thời gian
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                    String newTimeStr = sdf.format(new Date(timestamp));
                    
                    // Cập nhật thời gian
                    notification.setTime(newTimeStr);
                    hasChanges = true;
                }
            }
            
            // Lưu lại nếu có thay đổi
            if (hasChanges) {
                saveNotifications(context, notifications);
                Log.d(TAG, "Đã cập nhật định dạng thời gian cho tất cả thông báo");
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi cập nhật định dạng thời gian: " + e.getMessage());
        }
    }
} 