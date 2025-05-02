package com.example.qltccn.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.qltccn.activities.NotiActivity;
import com.example.qltccn.models.Notification;
import com.example.qltccn.models.Transaction;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Lớp tiện ích để quản lý thông báo cục bộ
 */
public class NotificationUtils {
    
    private static final String TAG = "NotificationUtils";
    private static final String PREFS_NAME = "notification_preferences";
    private static final String KEY_NOTIFICATIONS = "notifications";
    
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
            
        } catch (Exception e) {
            Log.e(TAG, "Lỗi thêm thông báo: " + e.getMessage());
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
} 