package com.example.qltccn.utils;

import com.example.qltccn.models.Transaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticsUtils {
    
    // Interface for statistics callbacks
    public interface StatisticsCallback {
        void onSuccess(Map<String, Object> statistics);
        void onError(String errorMessage);
    }
    
    // Get total income, expense, and balance for a date range
    public static void getTotalStatistics(Date startDate, Date endDate, StatisticsCallback callback) {
        TransactionUtils.getTransactionsByDateRange(startDate, endDate, new TransactionUtils.FetchTransactionsCallback() {
            @Override
            public void onSuccess(List<Transaction> transactions) {
                double totalIncome = 0;
                double totalExpense = 0;
                
                for (Transaction transaction : transactions) {
                    if ("income".equals(transaction.getType())) {
                        totalIncome += transaction.getAmount();
                    } else if ("expense".equals(transaction.getType())) {
                        totalExpense += transaction.getAmount();
                    }
                }
                
                double balance = totalIncome - totalExpense;
                
                Map<String, Object> statistics = new HashMap<>();
                statistics.put("totalIncome", totalIncome);
                statistics.put("totalExpense", totalExpense);
                statistics.put("balance", balance);
                statistics.put("transactionCount", transactions.size());
                
                callback.onSuccess(statistics);
            }
            
            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }
    
    // Get statistics by category for a date range
    public static void getCategoryStatistics(Date startDate, Date endDate, StatisticsCallback callback) {
        TransactionUtils.getTransactionsByDateRange(startDate, endDate, new TransactionUtils.FetchTransactionsCallback() {
            @Override
            public void onSuccess(List<Transaction> transactions) {
                Map<String, Double> expenseByCategory = new HashMap<>();
                Map<String, Double> incomeByCategory = new HashMap<>();
                
                for (Transaction transaction : transactions) {
                    String category = transaction.getCategory();
                    double amount = transaction.getAmount();
                    
                    if ("expense".equals(transaction.getType())) {
                        // Update expense by category
                        if (expenseByCategory.containsKey(category)) {
                            expenseByCategory.put(category, expenseByCategory.get(category) + amount);
                        } else {
                            expenseByCategory.put(category, amount);
                        }
                    } else if ("income".equals(transaction.getType())) {
                        // Update income by category
                        if (incomeByCategory.containsKey(category)) {
                            incomeByCategory.put(category, incomeByCategory.get(category) + amount);
                        } else {
                            incomeByCategory.put(category, amount);
                        }
                    }
                }
                
                Map<String, Object> statistics = new HashMap<>();
                statistics.put("expenseByCategory", expenseByCategory);
                statistics.put("incomeByCategory", incomeByCategory);
                
                callback.onSuccess(statistics);
            }
            
            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }
    
    // Get monthly statistics for a year
    public static void getMonthlyStatistics(int year, StatisticsCallback callback) {
        // Get start and end date for the year
        Date startDate = DateUtils.getStartOfYear(new Date(year - 1900, 0, 1));
        Date endDate = DateUtils.getEndOfYear(new Date(year - 1900, 0, 1));
        
        TransactionUtils.getTransactionsByDateRange(startDate, endDate, new TransactionUtils.FetchTransactionsCallback() {
            @Override
            public void onSuccess(List<Transaction> transactions) {
                Map<Integer, Double> monthlyIncome = new HashMap<>();
                Map<Integer, Double> monthlyExpense = new HashMap<>();
                
                // Initialize maps with zero values for all months
                for (int i = 1; i <= 12; i++) {
                    monthlyIncome.put(i, 0.0);
                    monthlyExpense.put(i, 0.0);
                }
                
                for (Transaction transaction : transactions) {
                    Date date = transaction.    getDateObject();
                    int month = date.getMonth() + 1; // Month is 0-based in Java Date
                    double amount = transaction.getAmount();
                    
                    if ("income".equals(transaction.getType())) {
                        // Update monthly income
                        monthlyIncome.put(month, monthlyIncome.get(month) + amount);
                    } else if ("expense".equals(transaction.getType())) {
                        // Update monthly expense
                        monthlyExpense.put(month, monthlyExpense.get(month) + amount);
                    }
                }
                
                Map<String, Object> statistics = new HashMap<>();
                statistics.put("monthlyIncome", monthlyIncome);
                statistics.put("monthlyExpense", monthlyExpense);
                
                callback.onSuccess(statistics);
            }
            
            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }
    
    // Get daily statistics for a month
    public static void getDailyStatistics(int year, int month, StatisticsCallback callback) {
        // Get start and end date for the month
        Date startDate = DateUtils.getStartOfMonth(new Date(year - 1900, month - 1, 1));
        Date endDate = DateUtils.getEndOfMonth(new Date(year - 1900, month - 1, 1));
        
        TransactionUtils.getTransactionsByDateRange(startDate, endDate, new TransactionUtils.FetchTransactionsCallback() {
            @Override
            public void onSuccess(List<Transaction> transactions) {
                Map<Integer, Double> dailyIncome = new HashMap<>();
                Map<Integer, Double> dailyExpense = new HashMap<>();
                
                // Get number of days in the month
                int daysInMonth = DateUtils.getEndOfMonth(new Date(year - 1900, month - 1, 1)).getDate();
                
                // Initialize maps with zero values for all days
                for (int i = 1; i <= daysInMonth; i++) {
                    dailyIncome.put(i, 0.0);
                    dailyExpense.put(i, 0.0);
                }
                
                for (Transaction transaction : transactions) {
                    Date date = transaction.getDateObject();
                    int day = date.getDate(); // Day of month
                    double amount = transaction.getAmount();
                    
                    if ("income".equals(transaction.getType())) {
                        // Update daily income
                        dailyIncome.put(day, dailyIncome.get(day) + amount);
                    } else if ("expense".equals(transaction.getType())) {
                        // Update daily expense
                        dailyExpense.put(day, dailyExpense.get(day) + amount);
                    }
                }
                
                Map<String, Object> statistics = new HashMap<>();
                statistics.put("dailyIncome", dailyIncome);
                statistics.put("dailyExpense", dailyExpense);
                
                callback.onSuccess(statistics);
            }
            
            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }
    
    // Get top expense categories for a date range
    public static void getTopExpenseCategories(Date startDate, Date endDate, int limit, StatisticsCallback callback) {
        getCategoryStatistics(startDate, endDate, new StatisticsCallback() {
            @Override
            public void onSuccess(Map<String, Object> statistics) {
                Map<String, Double> expenseByCategory = (Map<String, Double>) statistics.get("expenseByCategory");
                
                // Convert map to list for sorting
                List<Map.Entry<String, Double>> entries = new ArrayList<>(expenseByCategory.entrySet());
                
                // Sort by amount in descending order
                entries.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));
                
                // Take top N categories
                int topN = Math.min(limit, entries.size());
                List<Map.Entry<String, Double>> topEntries = entries.subList(0, topN);
                
                // Convert back to map
                Map<String, Double> topCategories = new HashMap<>();
                for (Map.Entry<String, Double> entry : topEntries) {
                    topCategories.put(entry.getKey(), entry.getValue());
                }
                
                Map<String, Object> result = new HashMap<>();
                result.put("topExpenseCategories", topCategories);
                
                callback.onSuccess(result);
            }
            
            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }
} 