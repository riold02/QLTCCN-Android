package com.example.qltccn.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RelativeLayout;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qltccn.R;
import com.example.qltccn.adapters.NotificationAdapter;
import com.example.qltccn.models.Notification;
import com.example.qltccn.utils.SwipeToDeleteCallback;
import com.example.qltccn.utils.NotificationUtils;

import java.util.ArrayList;
import java.util.List;

public class NotiActivity extends AppCompatActivity {

    // Khai báo các thành phần UI
    private ImageView backButton;
    private TextView titleText;
    private TextView emptyNotificationsText;
    private ImageView notiIcon;
    private RelativeLayout notiContainer;
    private Button clearAllButton;
    
    // RecyclerView
    private RecyclerView notificationsRecyclerView;
    
    // Adapter và danh sách
    private NotificationAdapter notificationAdapter;
    private List<Notification> notificationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noti);

        // Khởi tạo các thành phần UI
        initializeUI();
        
        // Thiết lập sự kiện click cho các nút
        setupClickListeners();
        
        // Tải thông báo từ SharedPreferences
        loadNotifications();
        
        // Cập nhật UI
        updateUI();
    }

    private void initializeUI() {
        // Header
        backButton = findViewById(R.id.backButton);
        titleText = findViewById(R.id.titleText);
        notiContainer = findViewById(R.id.notiContainer);
        notiIcon = findViewById(R.id.notiIcon);
        
        // Ẩn nút thông báo vì đang ở trong trang thông báo
        if (notiContainer != null) {
            notiContainer.setVisibility(View.GONE);
        }
        
        // Nút xóa tất cả thông báo
        clearAllButton = findViewById(R.id.clearAllButton);
        
        // Thông báo trống
        emptyNotificationsText = findViewById(R.id.emptyNotificationsText);
        
        // Notifications RecyclerView
        notificationsRecyclerView = findViewById(R.id.notificationsRecyclerView);
        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        notificationList = new ArrayList<>();
        
        // Thiết lập adapter cho thông báo
        notificationAdapter = new NotificationAdapter(this, notificationList, new NotificationAdapter.NotificationListener() {
            @Override
            public void onNotificationClick(Notification notification, int position) {
                // Xử lý khi click vào thông báo
                handleNotificationClick(notification);
            }

            @Override
            public void onNotificationDismiss(Notification notification, int position) {
                // Xử lý khi thông báo bị xóa
                deleteNotification(notification);
                // Kiểm tra và cập nhật UI nếu không còn thông báo
                updateUI();
            }
        });
        notificationsRecyclerView.setAdapter(notificationAdapter);
        
        // Thiết lập vuốt để xóa
        setupSwipeToDelete();
    }
    
    private void setupSwipeToDelete() {
        // Thiết lập swipe để xóa cho notifications
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this, new SwipeToDeleteCallback.SwipeToDeleteListener() {
            @Override
            public void onItemDelete(int position) {
                // Xóa thông báo khi vuốt
                notificationAdapter.removeItem(position);
                // Kiểm tra và cập nhật UI nếu không còn thông báo
                updateUI();
            }
        });
        
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchHelper.attachToRecyclerView(notificationsRecyclerView);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Tải lại thông báo khi trở lại màn hình
        loadNotifications();
        
        // Đánh dấu đã thấy thông báo mới
        markRecentAsSeen();
    }
    
    private void loadNotifications() {
        // Tải danh sách thông báo từ SharedPreferences
        List<Notification> notifications = NotificationUtils.getNotifications(this);
        
        // Cập nhật danh sách
        notificationList.clear();
        
        if (notifications != null && !notifications.isEmpty()) {
            notificationList.addAll(notifications);
        } else {
            // Nếu không có thông báo trong SharedPreferences, tạo mẫu

        }
        
        // Cập nhật adapter
        notificationAdapter.notifyDataSetChanged();
        
        // Cập nhật UI
        updateUI();
    }
    
    /**
     * Đánh dấu các thông báo mới là đã xem
     */
    private void markRecentAsSeen() {
        // Lưu lại trạng thái đã xem trong SharedPreferences
        SharedPreferences prefs = getSharedPreferences("notification_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("last_seen_timestamp", System.currentTimeMillis());
        editor.apply();
    }
    
    /**
     * Phương thức được gọi khi có giao dịch chi tiêu mới
     * @param amount Số tiền chi tiêu
     * @param category Danh mục chi tiêu
     */
    public void addTransactionNotification(double amount, String category) {
        // Tạo thông báo trong NotificationUtils
        NotificationUtils.addNotification(
            this,
            "Chi tiêu mới",
            "Bạn vừa chi tiêu " + String.format("%,.0fđ", amount) + " cho " + category,
            "transaction"
        );
                
        // Tải lại thông báo để cập nhật danh sách
        loadNotifications();
    }
    
    private void updateUI() {
        // Hiển thị thông báo trống nếu không có thông báo
        if (notificationList.isEmpty()) {
            emptyNotificationsText.setVisibility(View.VISIBLE);
            notificationsRecyclerView.setVisibility(View.GONE);
            clearAllButton.setVisibility(View.GONE);
        } else {
            emptyNotificationsText.setVisibility(View.GONE);
            notificationsRecyclerView.setVisibility(View.VISIBLE);
            clearAllButton.setVisibility(View.VISIBLE);
        }
    }
    
    private void handleNotificationClick(Notification notification) {
        // Đánh dấu thông báo đã đọc
        NotificationUtils.markAsRead(this, notification.getId());
        notification.setRead(true);
        notificationAdapter.notifyDataSetChanged();
        
        // Xử lý khi người dùng click vào thông báo
        String type = notification.getType();
        
        switch (type) {
            case "transaction":
                // Đi đến màn hình giao dịch
                Intent transIntent = new Intent(this, TranActivity.class);
                startActivity(transIntent);
                break;
            case "savings":
                // Đi đến màn hình tiết kiệm
                Intent savingsIntent = new Intent(this, SavingsActivity.class);
                startActivity(savingsIntent);
                break;
            default:
                // Hiển thị thông báo
                Toast.makeText(this, notification.getMessage(), Toast.LENGTH_SHORT).show();
                break;
        }
    }
    
    private void deleteNotification(Notification notification) {
        // Xóa thông báo khỏi SharedPreferences
        NotificationUtils.deleteNotification(this, notification.getId());
        
        // Hiển thị Toast thông báo
        Toast.makeText(this, "Đã xóa thông báo: " + notification.getTitle(), Toast.LENGTH_SHORT).show();
    }

    private void setupClickListeners() {
        // Sự kiện click nút quay lại
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
        // Sự kiện click nút xóa tất cả thông báo
        clearAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showClearAllConfirmDialog();
            }
        });
    }
    
    /**
     * Hiển thị dialog xác nhận xóa tất cả thông báo
     */
    private void showClearAllConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xóa tất cả thông báo");
        builder.setMessage("Bạn có chắc chắn muốn xóa tất cả thông báo không?");
        
        // Nút xác nhận xóa
        builder.setPositiveButton("Xóa tất cả", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                clearAllNotifications();
            }
        });
        
        // Nút hủy
        builder.setNegativeButton("Hủy", null);
        
        builder.show();
    }
    
    /**
     * Xóa tất cả thông báo
     */
    private void clearAllNotifications() {
        // Xóa tất cả thông báo
        NotificationUtils.clearAllNotifications(this);
        
        // Cập nhật danh sách
        notificationList.clear();
        notificationAdapter.notifyDataSetChanged();
        
        // Cập nhật UI
        updateUI();
        
        // Thông báo cho người dùng
        Toast.makeText(this, "Đã xóa tất cả thông báo", Toast.LENGTH_SHORT).show();
    }
} 