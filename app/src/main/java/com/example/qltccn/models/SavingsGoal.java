package com.example.qltccn.models;

import com.google.firebase.firestore.Exclude;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SavingsGoal {
    private String id;
    private String userId;
    private String title;
    private String description;
    private double targetAmount;
    private double currentAmount;
    private String iconName;
    private long startDate;
    private long endDate;
    private long createdAt;
    private long updatedAt;
    private String categoryType; // Loại danh mục (travel, house, car, wedding, v.v.)

    // Constructor rỗng cho Firebase
    public SavingsGoal() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.currentAmount = 0.0;
    }

    // Constructor với các trường cơ bản
    public SavingsGoal(String userId, String title, String description, double targetAmount, String iconName, String categoryType) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.targetAmount = targetAmount;
        this.iconName = iconName;
        this.categoryType = categoryType;
        this.currentAmount = 0.0;
        this.startDate = System.currentTimeMillis();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Constructor đầy đủ
    public SavingsGoal(String id, String userId, String title, String description, double targetAmount, 
                       double currentAmount, String iconName, long startDate, long endDate, String categoryType) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.iconName = iconName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.categoryType = categoryType;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(double targetAmount) {
        this.targetAmount = targetAmount;
    }

    public double getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(double currentAmount) {
        this.currentAmount = currentAmount;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
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

    public String getCategoryType() {
        return categoryType;
    }

    public void setCategoryType(String categoryType) {
        this.categoryType = categoryType;
    }

    // Phương thức tiện ích
    @Exclude
    public double getProgressPercentage() {
        if (targetAmount <= 0) return 0;
        return (currentAmount / targetAmount) * 100;
    }

    @Exclude
    public boolean isCompleted() {
        return currentAmount >= targetAmount;
    }

    @Exclude
    public Date getStartDateObject() {
        return new Date(startDate);
    }

    @Exclude
    public Date getEndDateObject() {
        return new Date(endDate);
    }

    @Exclude
    public void setStartDateObject(Date date) {
        this.startDate = date.getTime();
    }

    @Exclude
    public void setEndDateObject(Date date) {
        this.endDate = date.getTime();
    }

    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        if (id != null) map.put("id", id);
        map.put("userId", userId);
        map.put("title", title);
        map.put("description", description);
        map.put("targetAmount", targetAmount);
        map.put("currentAmount", currentAmount);
        map.put("iconName", iconName);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        map.put("createdAt", createdAt);
        map.put("updatedAt", updatedAt);
        map.put("categoryType", categoryType);
        return map;
    }

    // Phương thức để cập nhật số tiền hiện tại
    public void addAmount(double amount) {
        this.currentAmount += amount;
        this.updatedAt = System.currentTimeMillis();
    }
} 