package com.example.qltccn.utils;

import android.content.Context;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 * Lớp tiện ích để thực hiện các thao tác dữ liệu với Firebase Firestore
 */
public class DataUtils {

    /**
     * Interface callback cho các thao tác dữ liệu
     */
    public interface DataCallback {
        void onSuccess(String documentId);
        void onError(String errorMessage);
    }

    /**
     * Interface callback cho các thao tác truy vấn dữ liệu
     */
    public interface QueryCallback {
        void onSuccess(QuerySnapshot querySnapshot);
        void onError(String errorMessage);
    }
    
    /**
     * Interface callback cho lắng nghe thay đổi dữ liệu
     */
    public interface DocumentListener {
        void onDocumentChange(DocumentSnapshot documentSnapshot);
        void onError(String errorMessage);
    }
    
    /**
     * Interface callback cho lắng nghe thay đổi collection
     */
    public interface CollectionListener {
        void onCollectionChange(QuerySnapshot querySnapshot);
        void onError(String errorMessage);
    }

    /**
     * Thêm dữ liệu mới vào một collection
     * 
     * @param collectionName Tên collection
     * @param data Dữ liệu cần thêm (Map)
     * @param callback Callback xử lý kết quả
     */
    public static void addDocument(String collectionName, Map<String, Object> data, DataCallback callback) {
        // Thêm timestamp
        data.put("createdAt", new Date());
        data.put("updatedAt", new Date());
        
        // Thêm userId nếu người dùng đã đăng nhập
        String userId = FirebaseUtils.getCurrentUserId();
        if (userId != null) {
            data.put("userId", userId);
        }
        
        // Thêm vào Firestore
        FirebaseUtils.getFirestore()
                .collection(collectionName)
                .add(data)
                .addOnSuccessListener(documentReference -> {
                    // Cập nhật ID của document
                    String docId = documentReference.getId();
                    documentReference.update("id", docId);
                    callback.onSuccess(docId);
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }
    
    /**
     * Cập nhật dữ liệu của một document
     * 
     * @param collectionName Tên collection
     * @param documentId ID của document cần cập nhật
     * @param data Dữ liệu cần cập nhật (Map)
     * @param callback Callback xử lý kết quả
     */
    public static void updateDocument(String collectionName, String documentId, Map<String, Object> data, DataCallback callback) {
        if (documentId == null || documentId.isEmpty()) {
            callback.onError("ID của document không được để trống");
            return;
        }
        
        // Cập nhật timestamp
        data.put("updatedAt", new Date());
        
        // Cập nhật trong Firestore
        FirebaseUtils.getFirestore()
                .collection(collectionName)
                .document(documentId)
                .update(data)
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess(documentId);
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }
    
    /**
     * Xóa một document
     * 
     * @param collectionName Tên collection
     * @param documentId ID của document cần xóa
     * @param callback Callback xử lý kết quả
     */
    public static void deleteDocument(String collectionName, String documentId, DataCallback callback) {
        if (documentId == null || documentId.isEmpty()) {
            callback.onError("ID của document không được để trống");
            return;
        }
        
        FirebaseUtils.getFirestore()
                .collection(collectionName)
                .document(documentId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess(documentId);
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }
    
    /**
     * Lấy tất cả documents của một collection
     * 
     * @param collectionName Tên collection
     * @param callback Callback xử lý kết quả
     */
    public static void getAllDocuments(String collectionName, QueryCallback callback) {
        FirebaseUtils.getFirestore()
                .collection(collectionName)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    callback.onSuccess(querySnapshot);
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }
    
    /**
     * Lấy tất cả documents của người dùng hiện tại
     * 
     * @param collectionName Tên collection
     * @param callback Callback xử lý kết quả
     */
    public static void getUserDocuments(String collectionName, QueryCallback callback) {
        String userId = FirebaseUtils.getCurrentUserId();
        if (userId == null) {
            callback.onError("Người dùng chưa đăng nhập");
            return;
        }
        
        FirebaseUtils.getFirestore()
                .collection(collectionName)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    callback.onSuccess(querySnapshot);
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }
    
    /**
     * Lấy documents theo trường và giá trị
     * 
     * @param collectionName Tên collection
     * @param field Tên trường
     * @param value Giá trị
     * @param callback Callback xử lý kết quả
     */
    public static void getDocumentsByField(String collectionName, String field, Object value, QueryCallback callback) {
        FirebaseUtils.getFirestore()
                .collection(collectionName)
                .whereEqualTo(field, value)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    callback.onSuccess(querySnapshot);
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }
    
    /**
     * Lấy documents theo khoảng thời gian
     * 
     * @param collectionName Tên collection
     * @param dateField Tên trường ngày
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @param callback Callback xử lý kết quả
     */
    public static void getDocumentsByDateRange(String collectionName, String dateField, Date startDate, Date endDate, QueryCallback callback) {
        FirebaseUtils.getFirestore()
                .collection(collectionName)
                .whereGreaterThanOrEqualTo(dateField, startDate)
                .whereLessThanOrEqualTo(dateField, endDate)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    callback.onSuccess(querySnapshot);
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }
    
    /**
     * Thêm nhiều documents cùng lúc bằng batch
     * 
     * @param collectionName Tên collection
     * @param dataList Danh sách dữ liệu cần thêm
     * @param callback Callback xử lý kết quả
     */
    public static void addDocumentsBatch(String collectionName, List<Map<String, Object>> dataList, DataCallback callback) {
        if (dataList == null || dataList.isEmpty()) {
            callback.onError("Danh sách dữ liệu trống");
            return;
        }
        
        FirebaseFirestore db = FirebaseUtils.getFirestore();
        WriteBatch batch = db.batch();
        
        // Thêm userId và timestamp cho mỗi document
        String userId = FirebaseUtils.getCurrentUserId();
        Date now = new Date();
        
        for (Map<String, Object> data : dataList) {
            // Thêm timestamp
            data.put("createdAt", now);
            data.put("updatedAt", now);
            
            // Thêm userId nếu người dùng đã đăng nhập
            if (userId != null) {
                data.put("userId", userId);
            }
            
            // Tạo reference mới cho document
            DocumentReference docRef = db.collection(collectionName).document();
            String docId = docRef.getId();
            data.put("id", docId);
            
            // Thêm vào batch
            batch.set(docRef, data);
        }
        
        // Commit batch
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess("Batch completed successfully");
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }
    
    /**
     * Cập nhật nhiều documents cùng lúc bằng batch
     * 
     * @param collectionName Tên collection
     * @param updates Map chứa ID document và dữ liệu cần cập nhật
     * @param callback Callback xử lý kết quả
     */
    public static void updateDocumentsBatch(String collectionName, Map<String, Map<String, Object>> updates, DataCallback callback) {
        if (updates == null || updates.isEmpty()) {
            callback.onError("Danh sách cập nhật trống");
            return;
        }
        
        FirebaseFirestore db = FirebaseUtils.getFirestore();
        WriteBatch batch = db.batch();
        Date now = new Date();
        
        for (Map.Entry<String, Map<String, Object>> entry : updates.entrySet()) {
            String documentId = entry.getKey();
            Map<String, Object> data = entry.getValue();
            
            // Cập nhật timestamp
            data.put("updatedAt", now);
            
            // Thêm vào batch
            DocumentReference docRef = db.collection(collectionName).document(documentId);
            batch.update(docRef, data);
        }
        
        // Commit batch
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess("Batch update completed successfully");
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }
    
    /**
     * Xóa nhiều documents cùng lúc bằng batch
     * 
     * @param collectionName Tên collection
     * @param documentIds Danh sách ID document cần xóa
     * @param callback Callback xử lý kết quả
     */
    public static void deleteDocumentsBatch(String collectionName, List<String> documentIds, DataCallback callback) {
        if (documentIds == null || documentIds.isEmpty()) {
            callback.onError("Danh sách ID trống");
            return;
        }
        
        FirebaseFirestore db = FirebaseUtils.getFirestore();
        WriteBatch batch = db.batch();
        
        for (String documentId : documentIds) {
            DocumentReference docRef = db.collection(collectionName).document(documentId);
            batch.delete(docRef);
        }
        
        // Commit batch
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess("Batch delete completed successfully");
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }
    
    /**
     * Lắng nghe thay đổi của một document theo thời gian thực
     * 
     * @param collectionName Tên collection
     * @param documentId ID của document
     * @param listener Listener xử lý sự kiện
     * @return ListenerRegistration để hủy đăng ký lắng nghe khi không cần thiết
     */
    public static ListenerRegistration listenToDocument(String collectionName, String documentId, DocumentListener listener) {
        return FirebaseUtils.getFirestore()
                .collection(collectionName)
                .document(documentId)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        listener.onError(e.getMessage());
                        return;
                    }
                    
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        listener.onDocumentChange(documentSnapshot);
                    } else {
                        listener.onError("Document không tồn tại");
                    }
                });
    }
    
    /**
     * Lắng nghe thay đổi của một collection theo thời gian thực
     * 
     * @param collectionName Tên collection
     * @param listener Listener xử lý sự kiện
     * @return ListenerRegistration để hủy đăng ký lắng nghe khi không cần thiết
     */
    public static ListenerRegistration listenToCollection(String collectionName, CollectionListener listener) {
        return FirebaseUtils.getFirestore()
                .collection(collectionName)
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        listener.onError(e.getMessage());
                        return;
                    }
                    
                    if (querySnapshot != null) {
                        listener.onCollectionChange(querySnapshot);
                    }
                });
    }
    
    /**
     * Lắng nghe thay đổi của documents của người dùng hiện tại theo thời gian thực
     * 
     * @param collectionName Tên collection
     * @param listener Listener xử lý sự kiện
     * @return ListenerRegistration để hủy đăng ký lắng nghe khi không cần thiết
     */
    public static ListenerRegistration listenToUserDocuments(String collectionName, CollectionListener listener) {
        String userId = FirebaseUtils.getCurrentUserId();
        if (userId == null) {
            listener.onError("Người dùng chưa đăng nhập");
            return null;
        }
        
        return FirebaseUtils.getFirestore()
                .collection(collectionName)
                .whereEqualTo("userId", userId)
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        listener.onError(e.getMessage());
                        return;
                    }
                    
                    if (querySnapshot != null) {
                        listener.onCollectionChange(querySnapshot);
                    }
                });
    }
} 