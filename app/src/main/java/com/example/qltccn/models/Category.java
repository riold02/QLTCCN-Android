package com.example.qltccn.models;

public class Category {
    private String id;
    private String name;
    private String type; // "income" or "expense"
    private String iconName;
    private boolean isDefault;
    private String userId; // Thêm trường userId để xác định người sở hữu danh mục
    private String parentId; // ID của danh mục cha, null nếu là danh mục gốc
    private long createdAt; // Thêm trường thời gian tạo
    private long updatedAt; // Thêm trường thời gian cập nhật

    // Empty constructor for Firebase
    public Category() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Constructor with required fields
    public Category(String name, String type) {
        this.name = name;
        this.type = type;
        this.isDefault = false;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Constructor with 6 parameters (without id)
    public Category(String name, String type, String iconName, boolean isDefault, String userId, String parentId) {
        this.name = name;
        this.type = type;
        this.iconName = iconName;
        this.isDefault = isDefault;
        this.userId = userId;
        this.parentId = parentId;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Full constructor
    public Category(String id, String name, String type, String iconName, boolean isDefault, String userId, String parentId) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.iconName = iconName;
        this.isDefault = isDefault;
        this.userId = userId;
        this.parentId = parentId;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
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
} 