package com.example.qltccn.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.qltccn.utils.NotificationUtils;

/**
 * BroadcastReceiver để nhận thông báo từ AlarmManager và hiển thị thông báo
 */
public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationReceiver";
    
    // Các action constants
    public static final String ACTION_EXPENSE_REMINDER = "com.example.qltccn.EXPENSE_REMINDER"; 
    public static final String ACTION_LOW_BALANCE_CHECK = "com.example.qltccn.LOW_BALANCE_CHECK";
    public static final String ACTION_BUDGET_CHECK = "com.example.qltccn.BUDGET_CHECK";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "Nhận action: " + action);
        
        if (ACTION_EXPENSE_REMINDER.equals(action)) {
            // Gửi thông báo nhắc nhở chi tiêu
            NotificationUtils.addNotification(
                context,
                "Nhắc nhở chi tiêu",
                "Đừng quên cập nhật các khoản chi tiêu trong ngày hôm nay!",
                "reminder"
            );
            Log.d(TAG, "Đã gửi thông báo nhắc nhở chi tiêu");
        } 
        else if (ACTION_LOW_BALANCE_CHECK.equals(action)) {
            // Kiểm tra số dư thấp (giá trị ngưỡng 100,000 VND)
            double balance = intent.getDoubleExtra("balance", 0);
            double threshold = intent.getDoubleExtra("threshold", 100000);
            NotificationUtils.checkLowBalanceAndNotify(context, balance, threshold);
        }
        else if (ACTION_BUDGET_CHECK.equals(action)) {
            // Kiểm tra ngân sách
            String category = intent.getStringExtra("category");
            double currentAmount = intent.getDoubleExtra("currentAmount", 0);
            double budgetAmount = intent.getDoubleExtra("budgetAmount", 0);
            
            if (category != null) {
                NotificationUtils.checkBudgetAndNotify(context, category, currentAmount, budgetAmount);
            }
        }
        else if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            // Khởi động lại các alarm sau khi thiết bị khởi động
            Log.d(TAG, "Khởi động lại các thông báo sau khi thiết bị khởi động");
            restoreNotificationSettings(context);
        }
    }
    
    /**
     * Khôi phục lại các cài đặt thông báo sau khi thiết bị khởi động lại
     */
    private void restoreNotificationSettings(Context context) {
        try {
            // Khôi phục nhắc nhở chi tiêu hàng ngày
            SharedPreferences prefs = context.getSharedPreferences("notification_settings", Context.MODE_PRIVATE);
            boolean expenseReminder = prefs.getBoolean("expense_reminder", false);
            
            if (expenseReminder) {
                int hour = prefs.getInt("reminder_hour", 20);
                int minute = prefs.getInt("reminder_minute", 0);
                NotificationUtils.setDailyExpenseReminder(context, hour, minute, true);
            }
            
            // Thêm logic khôi phục các thông báo khác nếu cần
            
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khôi phục cài đặt thông báo: " + e.getMessage());
        }
    }
} 