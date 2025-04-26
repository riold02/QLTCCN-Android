package com.example.qltccn.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.qltccn.models.Transaction;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionUtils {
    
    // Interface for transaction callbacks
    public interface TransactionCallback {
        void onSuccess();
        void onError(String errorMessage);
    }
    
    // Interface for fetching transactions
    public interface FetchTransactionsCallback {
        void onSuccess(List<Transaction> transactions);
        void onError(String errorMessage);
    }
    
    // Add a new transaction - phiên bản mới với Context và TransactionCallback
    public static void addTransaction(Context context, Transaction transaction, TransactionCallback callback) {
        String userId = FirebaseUtils.getCurrentUserId();
        if (userId == null) {
            callback.onError("Người dùng chưa đăng nhập");
            return;
        }
        
        // Set user ID
        transaction.setUserId(userId);
        
        // Convert transaction to map
        Map<String, Object> transactionMap = transactionToMap(transaction);
        
        // Add to Firestore in user's transactions subcollection
        CollectionReference transactionsRef = FirebaseUtils.getUserTransactionsCollection();
        if (transactionsRef == null) {
            callback.onError("Không thể truy cập giao dịch của người dùng");
            return;
        }
        
        transactionsRef.add(transactionMap)
                .addOnSuccessListener(documentReference -> {
                    // Update transaction ID with Firestore document ID
                    String transactionId = documentReference.getId();
                    documentReference.update("id", transactionId)
                            .addOnSuccessListener(aVoid -> {
                                transaction.setId(transactionId);
                                callback.onSuccess();
                            })
                            .addOnFailureListener(e -> {
                                callback.onError("Lỗi khi cập nhật ID giao dịch: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    callback.onError("Lỗi khi thêm giao dịch: " + e.getMessage());
                });
    }
    
    // Add a new transaction
    public static void addTransaction(Transaction transaction, TransactionCallback callback) {
        String userId = FirebaseUtils.getCurrentUserId();
        if (userId == null) {
            callback.onError("Người dùng chưa đăng nhập");
            return;
        }
        
        // Set user ID
        transaction.setUserId(userId);
        
        // Convert transaction to map
        Map<String, Object> transactionMap = transactionToMap(transaction);
        
        // Add to Firestore in user's transactions subcollection
        CollectionReference transactionsRef = FirebaseUtils.getUserTransactionsCollection();
        if (transactionsRef == null) {
            callback.onError("Không thể truy cập giao dịch của người dùng");
            return;
        }
        
        transactionsRef.add(transactionMap)
                .addOnSuccessListener(documentReference -> {
                    // Update transaction ID with Firestore document ID
                    String transactionId = documentReference.getId();
                    documentReference.update("id", transactionId)
                            .addOnSuccessListener(aVoid -> {
                                transaction.setId(transactionId);
                                callback.onSuccess();
                            })
                            .addOnFailureListener(e -> {
                                callback.onError("Lỗi khi cập nhật ID giao dịch: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    callback.onError("Lỗi khi thêm giao dịch: " + e.getMessage());
                });
    }
    
    // Update an existing transaction
    public static void updateTransaction(Transaction transaction, TransactionCallback callback) {
        if (transaction.getId() == null) {
            callback.onError("Transaction ID is required for update");
            return;
        }
        
        // Update timestamp
        transaction.updateTimestamp();
        
        // Convert transaction to map
        Map<String, Object> transactionMap = transactionToMap(transaction);
        
        // Update in Firestore
        CollectionReference transactionsRef = FirebaseUtils.getUserTransactionsCollection();
        if (transactionsRef == null) {
            callback.onError("Không thể truy cập giao dịch của người dùng");
            return;
        }
        
        transactionsRef.document(transaction.getId())
                .update(transactionMap)
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }
    
    // Delete a transaction
    public static void deleteTransaction(String transactionId, TransactionCallback callback) {
        if (transactionId == null) {
            callback.onError("Transaction ID is required for deletion");
            return;
        }
        
        // Delete from Firestore
        CollectionReference transactionsRef = FirebaseUtils.getUserTransactionsCollection();
        if (transactionsRef == null) {
            callback.onError("Không thể truy cập giao dịch của người dùng");
            return;
        }
        
        transactionsRef.document(transactionId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }
    
    // Get a single transaction by ID
    public static void getTransaction(String transactionId, OnCompleteListener<DocumentSnapshot> listener) {
        try {
            CollectionReference transactionsRef = FirebaseUtils.getUserTransactionsCollection();
            if (transactionsRef == null) {
                Log.e("TransactionUtils", "Không thể truy cập giao dịch của người dùng");
                listener.onComplete(Tasks.forException(new Exception("Không thể truy cập giao dịch của người dùng")));
                return;
            }
            
            transactionsRef.document(transactionId)
                    .get()
                    .addOnCompleteListener(listener);
        } catch (Exception e) {
            Log.e("TransactionUtils", "Lỗi lấy giao dịch: " + e.getMessage());
            listener.onComplete(Tasks.forException(e));
        }
    }
    
    // Get all transactions for current user
    public static void getAllTransactions(FetchTransactionsCallback callback) {
        CollectionReference transactionsRef = FirebaseUtils.getUserTransactionsCollection();
        if (transactionsRef == null) {
            callback.onError("Không thể truy cập giao dịch của người dùng");
            return;
        }
        
        transactionsRef.orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Transaction> transactions = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Transaction transaction = document.toObject(Transaction.class);
                        transactions.add(transaction);
                    }
                    callback.onSuccess(transactions);
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }
    
    // Get transactions by type (income or expense)
    public static void getTransactionsByType(String type, FetchTransactionsCallback callback) {
        CollectionReference transactionsRef = FirebaseUtils.getUserTransactionsCollection();
        if (transactionsRef == null) {
            callback.onError("Không thể truy cập giao dịch của người dùng");
            return;
        }
        
        transactionsRef.whereEqualTo("type", type)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Transaction> transactions = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Transaction transaction = document.toObject(Transaction.class);
                        transactions.add(transaction);
                    }
                    callback.onSuccess(transactions);
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }
    
    // Get transactions by date range
    public static void getTransactionsByDateRange(Date startDate, Date endDate, FetchTransactionsCallback callback) {
        CollectionReference transactionsRef = FirebaseUtils.getUserTransactionsCollection();
        if (transactionsRef == null) {
            callback.onError("Không thể truy cập giao dịch của người dùng");
            return;
        }
        
        long startTimestamp = startDate.getTime();
        long endTimestamp = endDate.getTime();
        
        transactionsRef.whereGreaterThanOrEqualTo("date", startTimestamp)
                .whereLessThanOrEqualTo("date", endTimestamp)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Transaction> transactions = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Transaction transaction = document.toObject(Transaction.class);
                        transactions.add(transaction);
                    }
                    callback.onSuccess(transactions);
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }
    
    public static void getTransactionsByCategory(String category, FetchTransactionsCallback callback) {
        CollectionReference transactionsRef = FirebaseUtils.getUserTransactionsCollection();
        if (transactionsRef == null) {
            callback.onError("Không thể truy cập giao dịch của người dùng");
            return;
        }
        
        transactionsRef.whereEqualTo("category", category)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Transaction> transactions = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Transaction transaction = document.toObject(Transaction.class);
                        transactions.add(transaction);
                    }
                    callback.onSuccess(transactions);
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }

    /**
     * Di chuyển dữ liệu giao dịch từ collection cũ sang cấu trúc mới
     * @param callback Callback để thông báo kết quả
     */
    public static void migrateTransactionsToUserSubcollection(TransactionCallback callback) {
        migrateTransactionsToUserSubcollection(callback, false);
    }
    
    /**
     * Di chuyển dữ liệu giao dịch từ collection cũ sang cấu trúc mới, với tùy chọn bỏ qua cho tài khoản mới
     * @param callback Callback để thông báo kết quả
     * @param skipForNewAccounts True nếu muốn bỏ qua việc di chuyển cho tài khoản mới
     */
    public static void migrateTransactionsToUserSubcollection(TransactionCallback callback, boolean skipForNewAccounts) {
        try {
            String userId = FirebaseUtils.getCurrentUserId();
            if (userId == null) {
                Log.e("TransactionUtils", "Không thể di chuyển dữ liệu: người dùng chưa đăng nhập");
                callback.onError("User not logged in");
                return;
            }
            
            // Nếu skipForNewAccounts = true thì kiểm tra xem tài khoản có phải là mới không
            if (skipForNewAccounts && FirebaseUtils.isNewAccount()) {
                Log.d("TransactionUtils", "Bỏ qua việc di chuyển dữ liệu cho tài khoản mới");
                callback.onSuccess();
                return;
            }
            
            Log.d("TransactionUtils", "Bắt đầu di chuyển dữ liệu của người dùng: " + userId);
            
            // Lấy tất cả giao dịch của người dùng từ subcollection mới để kiểm tra
            FirebaseUtils.getUserTransactionsCollection()
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Nếu đã có dữ liệu trong subcollection, có thể bỏ qua quá trình di chuyển
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Log.d("TransactionUtils", "Đã có " + queryDocumentSnapshots.size() + " giao dịch trong subcollection. Không cần di chuyển.");
                        callback.onSuccess();
                        return;
                    }
                    
                    Log.d("TransactionUtils", "Không tìm thấy giao dịch trong subcollection. Tiến hành lấy từ collection cũ...");
                    
                    // Truy cập trực tiếp vào collection "transactions" thay vì sử dụng phương thức getTransactionsCollection()
                    FirebaseFirestore.getInstance().collection("transactions")
                        .whereEqualTo("userId", userId)
                        .get()
                        .addOnSuccessListener(oldTransactions -> {
                            Log.d("TransactionUtils", "Đã tìm thấy " + oldTransactions.size() + " giao dịch trong collection cũ");
                            
                            if (oldTransactions.isEmpty()) {
                                Log.d("TransactionUtils", "Không có giao dịch nào trong collection cũ. Không cần di chuyển.");
                                callback.onSuccess();
                                return;
                            }
                            
                            CollectionReference newTransactionsRef = FirebaseUtils.getUserTransactionsCollection();
                            if (newTransactionsRef == null) {
                                Log.e("TransactionUtils", "Không thể truy cập subcollection của người dùng");
                                callback.onError("Không thể truy cập subcollection của người dùng");
                                return;
                            }
                            
                            // Tạo danh sách các giao dịch để di chuyển
                            List<Transaction> transactions = new ArrayList<>();
                            for (DocumentSnapshot document : oldTransactions) {
                                Transaction transaction = document.toObject(Transaction.class);
                                if (transaction != null) {  
                                    transactions.add(transaction);
                                    Log.d("TransactionUtils", "Đã thêm giao dịch " + transaction.getId() + " vào danh sách di chuyển");
                                }
                            }
                            
                            Log.d("TransactionUtils", "Chuẩn bị di chuyển " + transactions.size() + " giao dịch sang subcollection mới");
                            
                            // Thêm từng giao dịch vào subcollection mới
                            final int[] successCount = {0};
                            final int totalCount = transactions.size();
                            
                            if (totalCount == 0) {
                                Log.d("TransactionUtils", "Không có giao dịch hợp lệ để di chuyển");
                                callback.onSuccess();
                                return;
                            }
                            
                            // Sử dụng batch để cập nhật nhiều giao dịch cùng lúc
                            FirebaseFirestore db = FirebaseUtils.getFirestore();
                            WriteBatch batch = db.batch();
                            
                            for (Transaction transaction : transactions) {
                                if (transaction.getId() != null) {
                                    DocumentReference docRef = newTransactionsRef.document(transaction.getId());
                                    Map<String, Object> transactionMap = transactionToMap(transaction);
                                    batch.set(docRef, transactionMap);
                                }
                            }
                            
                            // Commit batch
                            batch.commit()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("TransactionUtils", "Di chuyển thành công " + totalCount + " giao dịch");
                                    callback.onSuccess();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("TransactionUtils", "Lỗi khi di chuyển giao dịch: " + e.getMessage());
                                    callback.onError("Lỗi khi di chuyển giao dịch: " + e.getMessage());
                                });
                        })
                        .addOnFailureListener(e -> {
                            String errorMessage = e.getMessage();
                            Log.e("TransactionUtils", "Lỗi khi tải giao dịch từ collection cũ: " + errorMessage);
                            
                            if (errorMessage != null && errorMessage.contains("PERMISSION_DENIED")) {
                                Log.w("TransactionUtils", "Không đủ quyền truy cập collection cũ. Bỏ qua việc di chuyển dữ liệu.");
                                callback.onSuccess();
                            } else {
                                callback.onError("Lỗi khi tải giao dịch từ collection cũ: " + errorMessage);
                            }
                        });
                })
                .addOnFailureListener(e -> {
                    Log.e("TransactionUtils", "Lỗi khi kiểm tra subcollection: " + e.getMessage());
                    callback.onError("Lỗi khi kiểm tra subcollection: " + e.getMessage());
                });
        } catch (Exception e) {
            Log.e("TransactionUtils", "Lỗi ngoại lệ khi di chuyển dữ liệu: " + e.getMessage(), e);
            callback.onError("Lỗi ngoại lệ: " + e.getMessage());
        }
    }
    
    // Convert Transaction object to Map for Firestore
    private static Map<String, Object> transactionToMap(Transaction transaction) {
        Map<String, Object> map = new HashMap<>();
        
        if (transaction.getId() != null) {
            map.put("id", transaction.getId());
        }
        map.put("userId", transaction.getUserId());
        map.put("amount", transaction.getAmount());
        map.put("category", transaction.getCategory());
        map.put("description", transaction.getDescription());
        map.put("date", transaction.getDate());
        map.put("type", transaction.getType());
        map.put("createdAt", transaction.getCreatedAt());
        map.put("updatedAt", transaction.getUpdatedAt());
        
        return map;
    }
} 