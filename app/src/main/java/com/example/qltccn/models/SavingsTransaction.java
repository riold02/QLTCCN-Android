package com.example.qltccn.models;

import com.google.firebase.firestore.Exclude;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SavingsTransaction {
    private String id;
    private String userId;
    private String goalId;
    private double amount;
    private String description;
    private String note;
    private String transactionType; // "deposit" hoặc "withdraw"
    private long date;
    private long createdAt;
    private long updatedAt;

    // Constructor rỗng cho Firebase
    public SavingsTransaction() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.date = System.currentTimeMillis();
    }

    // Constructor với các trường cơ bản
    public SavingsTransaction(String userId, String goalId, double amount, String description, String transactionType) {
        this.userId = userId;
        this.goalId = goalId;
        this.amount = amount;
        this.description = description;
        this.transactionType = transactionType;
        this.date = System.currentTimeMillis();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Constructor đầy đủ
    public SavingsTransaction(String id, String userId, String goalId, double amount, 
                              String description, String note, String transactionType, long date) {
        this.id = id;
        this.userId = userId;
        this.goalId = goalId;
        this.amount = amount;
        this.description = description;
        this.note = note;
        this.transactionType = transactionType;
        this.date = date;
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

    public String getGoalId() {
        return goalId;
    }

    public void setGoalId(String goalId) {
        this.goalId = goalId;
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

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
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
        if (id != null) map.put("id", id);
        map.put("userId", userId);
        map.put("goalId", goalId);
        map.put("amount", amount);
        map.put("description", description);
        if (note != null) map.put("note", note);
        map.put("transactionType", transactionType);
        map.put("date", date);
        map.put("createdAt", createdAt);
        map.put("updatedAt", updatedAt);
        return map;
    }

    // Phương thức để cập nhật timestamp
    public void updateTimestamp() {
        this.updatedAt = System.currentTimeMillis();
    }
} 