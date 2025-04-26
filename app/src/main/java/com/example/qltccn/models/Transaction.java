package com.example.qltccn.models;

import com.google.firebase.firestore.Exclude;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Transaction {
    private String id;
    private String userId;
    private String category;
    private double amount;
    private String description;
    private String note;
    private String type; // "income" hoặc "expense"
    private long date;
    private long createdAt;
    private long updatedAt;
    private String goalId; // ID của mục tiêu tiết kiệm (nếu có)

    // Constructor rỗng cho Firebase
    public Transaction() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.date = System.currentTimeMillis();
    }

    // Constructor với các trường cơ bản
    public Transaction(String userId, String category, double amount, String description, String type) {
        this.userId = userId;
        this.category = category;
        this.amount = amount;
        this.description = description;
        this.type = type;
        this.date = System.currentTimeMillis();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Constructor đầy đủ
    public Transaction(String id, String userId, String category, double amount, String description, 
                      String note, String type, long date, String goalId) {
        this.id = id;
        this.userId = userId;
        this.category = category;
        this.amount = amount;
        this.description = description;
        this.note = note;
        this.type = type;
        this.date = date;
        this.goalId = goalId;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters và Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getGoalId() {
        return goalId;
    }

    public void setGoalId(String goalId) {
        this.goalId = goalId;
    }

    // Phương thức tiện ích
    @Exclude
    public Date getDateObject() {
        return new Date(date);
    }

    @Exclude
    public void setDateObject(Date date) {
        this.date = date.getTime();
    }

    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        
        // Đảm bảo tất cả các trường đều được đưa vào map
        // Ngay cả khi id là null, vẫn thêm vào để tránh lỗi với Firestore
        map.put("id", id);
        map.put("userId", userId);
        map.put("category", category);
        map.put("amount", amount);
        map.put("description", description);
        map.put("note", note); // Không cần kiểm tra null, Firestore chấp nhận giá trị null
        map.put("type", type);
        map.put("date", date);
        map.put("createdAt", createdAt);
        map.put("updatedAt", updatedAt);
        if (goalId != null) map.put("goalId", goalId);
        
        return map;
    }

    // Phương thức để cập nhật timestamp
    public void updateTimestamp() {
        this.updatedAt = System.currentTimeMillis();
    }
} 