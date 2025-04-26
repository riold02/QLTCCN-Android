package com.example.qltccn.models;

import com.google.firebase.firestore.Exclude;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class User {
    private String id;
    private String uid;
    private String name;
    private String email;
    private String phone;
    private String profileImageUrl; 
    private Date dateOfBirth;
    private double balance;
    private Object createdAt;
    private Object updatedAt;

    // Empty constructor for Firebase
    public User() {
        this.balance = 0.0;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    // Constructor with required fields
    public User(String id, String name, String email) {
        this.id = id;
        this.uid = id; // uid và id đồng nhất
        this.name = name;
        this.email = email;
        this.balance = 0.0;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    // Full constructor
    public User(String id, String name, String email, String phone, String profileImageUrl, 
                Date dateOfBirth, double balance) {
        this.id = id;
        this.uid = id; // uid và id đồng nhất
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.profileImageUrl = profileImageUrl;
        this.dateOfBirth = dateOfBirth;
        this.balance = balance;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        this.uid = id; // Đảm bảo uid và id luôn đồng nhất
    }
    
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
        this.id = uid; // Đảm bảo uid và id luôn đồng nhất
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    // Getter/Setter cho avatar - Phương thức tương thích ngược
    @Exclude
    public String getAvatarUrl() {
        return profileImageUrl;
    }

    @Exclude
    public void setAvatarUrl(String avatarUrl) {
        this.profileImageUrl = avatarUrl;
    }
    
    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    @Exclude
    public Date getCreatedAt() {
        if (createdAt instanceof Date) {
            return (Date) createdAt;
        } else if (createdAt instanceof Long) {
            return new Date((Long) createdAt);
        }
        return new Date();
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    @Exclude
    public Date getUpdatedAt() {
        if (updatedAt instanceof Date) {
            return (Date) updatedAt;
        } else if (updatedAt instanceof Long) {
            return new Date((Long) updatedAt);
        }
        return new Date();
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Update the updatedAt timestamp
    public void updateTimestamp() {
        this.updatedAt = new Date();
    }
    
    // Chuyển đổi thành Map cho Firebase
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("uid", uid);
        result.put("name", name);
        result.put("email", email);
        result.put("phone", phone);
        result.put("profileImageUrl", profileImageUrl);
        
        // Đối với Date, lưu dưới dạng timestamp (long)
        if (dateOfBirth != null) {
            result.put("dateOfBirth", dateOfBirth.getTime());
            
            // Thêm dạng đã format để tiện sử dụng
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
                result.put("dateOfBirthFormatted", sdf.format(dateOfBirth));
            } catch (Exception e) {
                android.util.Log.w("User", "Lỗi định dạng ngày sinh: " + e.getMessage());
            }
        } else {
            result.put("dateOfBirth", null);
        }
        
        result.put("balance", balance);
        
        // Lưu thời gian tạo và cập nhật dưới dạng timestamp
        if (createdAt != null) {
            if (createdAt instanceof Date) {
                result.put("createdAt", ((Date) createdAt).getTime());
            } else if (createdAt instanceof Long) {
                result.put("createdAt", createdAt);
            } else {
                result.put("createdAt", System.currentTimeMillis());
            }
        } else {
            result.put("createdAt", System.currentTimeMillis());
        }
        
        if (updatedAt != null) {
            if (updatedAt instanceof Date) {
                result.put("updatedAt", ((Date) updatedAt).getTime());
            } else if (updatedAt instanceof Long) {
                result.put("updatedAt", updatedAt);
            } else {
                result.put("updatedAt", System.currentTimeMillis());
            }
        } else {
            result.put("updatedAt", System.currentTimeMillis());
        }
        
        return result;
    }
} 