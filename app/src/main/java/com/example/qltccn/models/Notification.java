package com.example.qltccn.models;

/**
 * Lớp đại diện cho một thông báo trong ứng dụng
 */
public class Notification {
    private String id;
    private String title;
    private String message;
    private String time;
    private String type; // "reminder", "transaction", "update", etc.
    private boolean read;
    private long timestamp;
    
    /**
     * Constructor không tham số cho Gson
     */
    public Notification() {
    }
    
    /**
     * Constructor đầy đủ
     * @param id ID thông báo
     * @param title Tiêu đề
     * @param message Nội dung
     * @param time Thời gian hiển thị
     * @param type Loại thông báo
     * @param read Trạng thái đã đọc
     * @param timestamp Thời gian tạo (milliseconds)
     */
    public Notification(String id, String title, String message, String time, String type, boolean read, long timestamp) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.time = time;
        this.type = type;
        this.read = read;
        this.timestamp = timestamp;
    }
    
    /**
     * Constructor phù hợp với mã cũ
     */
    public Notification(String title, String message, String time, String type) {
        this.id = "notification_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 10000);
        this.title = title;
        this.message = message;
        this.time = time;
        this.type = type;
        this.read = false;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getters và Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getTime() {
        return time;
    }
    
    public void setTime(String time) {
        this.time = time;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public boolean isRead() {
        return read;
    }
    
    public void setRead(boolean read) {
        this.read = read;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
} 