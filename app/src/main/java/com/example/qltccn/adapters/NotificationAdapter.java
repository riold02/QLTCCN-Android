package com.example.qltccn.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qltccn.R;
import com.example.qltccn.models.Notification;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private Context context;
    private List<Notification> notificationList;
    private NotificationListener listener;

    public interface NotificationListener {
        void onNotificationClick(Notification notification, int position);
        void onNotificationDismiss(Notification notification, int position);
    }

    public NotificationAdapter(Context context, List<Notification> notificationList, NotificationListener listener) {
        this.context = context;
        this.notificationList = notificationList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notificationList.get(position);
        
        // Thiết lập các dữ liệu cho notification
        holder.titleTextView.setText(notification.getTitle());
        holder.messageTextView.setText(notification.getMessage());
        holder.timeTextView.setText(notification.getTime());
        
        // Thiết lập icon và màu sắc dựa trên loại thông báo
        switch (notification.getType()) {
            case "transaction":
                // Giao dịch mới
                holder.iconImageView.setImageResource(R.drawable.ic_trans);
               
                break;
            case "warning":
                // Cảnh báo
                holder.iconImageView.setImageResource(R.drawable.ic_reminder);
               
                break;    
            case "reminder":
                // Nhắc nhở
                holder.iconImageView.setImageResource(R.drawable.ic_reminder);
         
                break;
            case "savings":
                // Tiết kiệm
                holder.iconImageView.setImageResource(R.drawable.ic_savings);
              
                break;

            default:
                // Mặc định
                holder.iconImageView.setImageResource(R.drawable.ic_money);
                
                break;
        }
        
        // Thiết lập màu khác cho thông báo đã đọc và chưa đọc
        if (notification.isRead()) {
            holder.itemView.setAlpha(0.7f);
        } else {
            holder.itemView.setAlpha(1.0f);
        }
        
        // Thiết lập sự kiện click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationClick(notification, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationList != null ? notificationList.size() : 0;
    }
    
    // Xóa thông báo
    public void removeItem(int position) {
        if (position >= 0 && position < notificationList.size()) {
            Notification removedItem = notificationList.get(position);
            notificationList.remove(position);
            notifyItemRemoved(position);
            
            if (listener != null) {
                listener.onNotificationDismiss(removedItem, position);
            }
        }
    }
    
    // Thêm thông báo
    public void addItem(Notification notification) {
        notificationList.add(notification);
        notifyItemInserted(notificationList.size() - 1);
    }
    
    // Cập nhật danh sách
    public void updateList(List<Notification> newList) {
        this.notificationList = newList;
        notifyDataSetChanged();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        ImageView iconImageView;
        TextView titleTextView, messageTextView, timeTextView;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            iconImageView = itemView.findViewById(R.id.notificationIcon);
            titleTextView = itemView.findViewById(R.id.notificationTitle);
            messageTextView = itemView.findViewById(R.id.notificationMessage);
            timeTextView = itemView.findViewById(R.id.notificationTime);
        }
    }
} 