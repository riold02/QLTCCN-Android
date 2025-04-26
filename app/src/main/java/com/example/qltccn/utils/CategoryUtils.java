package com.example.qltccn.utils;

import com.example.qltccn.models.Category;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.util.Log;

public class CategoryUtils {
    private static final String COLLECTION_CATEGORIES = "categories";
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final FirebaseAuth auth = FirebaseAuth.getInstance();
    
    // Interface for category callbacks
    public interface CategoryCallback {
        void onSuccess();
        void onError(String errorMessage);
    }
    
    // Interface for fetching categories
    public interface FetchCategoriesCallback {
        void onSuccess(List<Category> categories);
        void onError(String errorMessage);
    }
    
    // Interface for adding a category
    public interface OnCategoryAddedListener {
        void onSuccess(Category category);
        void onFailure(Exception e);
    }
    
    // Interface for updating a category
    public interface OnCategoryUpdatedListener {
        void onSuccess();
        void onFailure(Exception e);
    }
    
    // Interface for deleting a category
    public interface OnCategoryDeletedListener {
        void onSuccess();
        void onFailure(Exception e);
    }
    
    // Interface for fetching categories by type
    public interface OnCategoriesFetchedListener {
        void onSuccess(List<Category> categories);
        void onFailure(Exception e);
    }
    
    // Interface for fetching category hierarchy
    public interface OnCategoryHierarchyFetchedListener {
        void onSuccess(List<Category> rootCategories, Map<String, List<Category>> childrenMap);
        void onFailure(Exception e);
    }
    
    // Add a new category
    public static void addCategory(Category category, OnCategoryAddedListener listener) {
        String userId = auth.getCurrentUser().getUid();
        category.setUserId(userId);
        
        db.collection(COLLECTION_CATEGORIES)
            .add(category)
            .addOnSuccessListener(documentReference -> {
                category.setId(documentReference.getId());
                listener.onSuccess(category);
            })
            .addOnFailureListener(listener::onFailure);
    }
    
    // Update an existing category
    public static void updateCategory(Category category, OnCategoryUpdatedListener listener) {
        category.setUpdatedAt(System.currentTimeMillis());
        
        db.collection(COLLECTION_CATEGORIES)
            .document(category.getId())
            .set(category)
            .addOnSuccessListener(aVoid -> listener.onSuccess())
            .addOnFailureListener(listener::onFailure);
    }
    
    // Delete a category
    public static void deleteCategory(String categoryId, OnCategoryDeletedListener listener) {
        db.collection(COLLECTION_CATEGORIES)
            .document(categoryId)
            .delete()
            .addOnSuccessListener(aVoid -> listener.onSuccess())
            .addOnFailureListener(listener::onFailure);
    }
    
    // Get a single category by ID
    public static void getCategory(String categoryId, OnCompleteListener<DocumentSnapshot> listener) {
        db.collection(COLLECTION_CATEGORIES)
            .document(categoryId)
            .get()
            .addOnCompleteListener(listener);
    }
    
    // Get all categories
    public static void getAllCategories(FetchCategoriesCallback callback) {
        String userId = auth.getCurrentUser().getUid();
        
        db.collection(COLLECTION_CATEGORIES)
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Category> categories = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Category category = document.toObject(Category.class);
                    category.setId(document.getId());
                    categories.add(category);
                }
                callback.onSuccess(categories);
            })
            .addOnFailureListener(e -> {
                callback.onError(e.getMessage());
            });
    }
    
    // Get categories by type (income or expense)
    public static void getCategories(String type, OnCategoriesFetchedListener listener) {
        if (type == null) {
            listener.onFailure(new IllegalArgumentException("Loại danh mục không được null"));
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (userId == null) {
            listener.onFailure(new IllegalStateException("Người dùng chưa đăng nhập"));
            return;
        }

        db.collection("categories")
                .whereEqualTo("userId", userId)
                .whereEqualTo("type", type)
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Category> categories = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Category category = document.toObject(Category.class);
                        category.setId(document.getId());
                        categories.add(category);
                    }
                    listener.onSuccess(categories);
                })
                .addOnFailureListener(e -> {
                    Log.e("CategoryUtils", "Lỗi khi lấy danh mục: " + e.getMessage());
                    listener.onFailure(e);
                });
    }
    
    // Get child categories for a parent category
    public static void getChildCategories(String parentId, OnCategoriesFetchedListener listener) {
        if (parentId == null) {
            listener.onFailure(new IllegalArgumentException("ID danh mục cha không được null"));
            return;
        }

        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId == null) {
            listener.onFailure(new IllegalStateException("Người dùng chưa đăng nhập"));
            return;
        }

        db.collection(COLLECTION_CATEGORIES)
                .whereEqualTo("userId", userId)
                .whereEqualTo("parentId", parentId)
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Category> childCategories = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Category category = document.toObject(Category.class);
                        category.setId(document.getId());
                        childCategories.add(category);
                    }
                    listener.onSuccess(childCategories);
                })
                .addOnFailureListener(e -> {
                    Log.e("CategoryUtils", "Lỗi khi lấy danh mục con: " + e.getMessage());
                    listener.onFailure(e);
                });
    }

    // Get root categories (categories without parent)
    public static void getRootCategories(String type, OnCategoriesFetchedListener listener) {
        if (type == null) {
            listener.onFailure(new IllegalArgumentException("Loại danh mục không được null"));
            return;
        }

        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId == null) {
            listener.onFailure(new IllegalStateException("Người dùng chưa đăng nhập"));
            return;
        }

        db.collection(COLLECTION_CATEGORIES)
                .whereEqualTo("userId", userId)
                .whereEqualTo("type", type)
                .whereEqualTo("parentId", null)
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Category> rootCategories = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Category category = document.toObject(Category.class);
                        category.setId(document.getId());
                        rootCategories.add(category);
                    }
                    listener.onSuccess(rootCategories);
                })
                .addOnFailureListener(e -> {
                    Log.e("CategoryUtils", "Lỗi khi lấy danh mục gốc: " + e.getMessage());
                    listener.onFailure(e);
                });
    }

    // Get all categories with their hierarchical structure
    public static void getCategoryHierarchy(String type, OnCategoryHierarchyFetchedListener listener) {
        if (type == null) {
            listener.onFailure(new IllegalArgumentException("Loại danh mục không được null"));
            return;
        }

        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId == null) {
            listener.onFailure(new IllegalStateException("Người dùng chưa đăng nhập"));
            return;
        }

        // Get all categories of the specified type
        db.collection(COLLECTION_CATEGORIES)
                .whereEqualTo("userId", userId)
                .whereEqualTo("type", type)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Category> allCategories = new ArrayList<>();
                    Map<String, List<Category>> childrenMap = new HashMap<>();
                    List<Category> rootCategories = new ArrayList<>();

                    // First pass: collect all categories and initialize childrenMap
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Category category = document.toObject(Category.class);
                        category.setId(document.getId());
                        allCategories.add(category);
                        childrenMap.put(category.getId(), new ArrayList<>());
                    }

                    // Second pass: organize categories into hierarchy
                    for (Category category : allCategories) {
                        String parentId = category.getParentId();
                        if (parentId == null || parentId.isEmpty()) {
                            // This is a root category
                            rootCategories.add(category);
                        } else {
                            // Add to parent's children list
                            List<Category> children = childrenMap.get(parentId);
                            if (children != null) {
                                children.add(category);
                            }
                        }
                    }

                    // Sort all children lists alphabetically
                    for (List<Category> children : childrenMap.values()) {
                        children.sort((c1, c2) -> c1.getName().compareToIgnoreCase(c2.getName()));
                    }

                    // Sort root categories alphabetically
                    rootCategories.sort((c1, c2) -> c1.getName().compareToIgnoreCase(c2.getName()));

                    // Return the hierarchy
                    listener.onSuccess(rootCategories, childrenMap);
                })
                .addOnFailureListener(e -> {
                    Log.e("CategoryUtils", "Lỗi khi lấy cấu trúc phân cấp danh mục: " + e.getMessage());
                    listener.onFailure(e);
                });
    }
    
    // Convert Category object to Map for Firestore
    private static Map<String, Object> categoryToMap(Category category) {
        Map<String, Object> map = new HashMap<>();
        
        if (category.getId() != null) {
            map.put("id", category.getId());
        }
        map.put("name", category.getName());
        map.put("type", category.getType());
        map.put("iconName", category.getIconName());

        map.put("default", category.isDefault());
        
        // Thêm userId để xác định người sở hữu danh mục
        String userId = FirebaseUtils.getCurrentUserId();
        if (userId != null) {
            map.put("userId", userId);
        }
        
        // Thêm parentId nếu có
        if (category.getParentId() != null) {
            map.put("parentId", category.getParentId());
        }
        
        // Thêm thời gian tạo và cập nhật
        map.put("createdAt", category.getCreatedAt());
        map.put("updatedAt", category.getUpdatedAt());
        
        return map;
    }
} 