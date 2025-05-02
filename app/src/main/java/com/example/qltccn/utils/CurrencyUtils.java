package com.example.qltccn.utils;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

/**
 * Lớp tiện ích để định dạng tiền tệ Việt Nam
 */
public class CurrencyUtils {
    private static final Locale DEFAULT_LOCALE = new Locale("vi", "VN");
    private static final Currency DEFAULT_CURRENCY = Currency.getInstance("VND");
    private static NumberFormat currencyFormatter;

    static {
        currencyFormatter = NumberFormat.getCurrencyInstance(DEFAULT_LOCALE);
        currencyFormatter.setCurrency(DEFAULT_CURRENCY);
    }

    // Format amount to currency string
    public static String formatCurrency(double amount) {
        return currencyFormatter.format(amount);
    }

    // Format amount to currency string without symbol
    public static String formatAmount(double amount) {
        NumberFormat formatter = NumberFormat.getNumberInstance(DEFAULT_LOCALE);
        formatter.setGroupingUsed(true);
        return formatter.format(amount);
    }

    // Parse currency string to double
    public static double parseCurrency(String currencyString) {
        try {
            // Remove currency symbol and non-numeric characters
            String cleanString = currencyString.replaceAll("[^\\d.]ư", "");
            return Double.parseDouble(cleanString);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // Get currency symbol
    public static String getCurrencySymbol() {
        return DEFAULT_CURRENCY.getSymbol(DEFAULT_LOCALE);
    }

    // Format with custom locale and currency
    public static String formatWithCustomCurrency(double amount, String currencyCode, String languageCode, String countryCode) {
        try {
            Locale locale = new Locale(languageCode, countryCode);
            Currency currency = Currency.getInstance(currencyCode);
            NumberFormat formatter = NumberFormat.getCurrencyInstance(locale);
            formatter.setCurrency(currency);
            return formatter.format(amount);
        } catch (Exception e) {
            e.printStackTrace();
            return formatCurrency(amount);
        }
    }

    // Locale Việt Nam
    private static final Locale LOCALE_VN = new Locale("vi", "VN");
    
    /**
     * Định dạng số tiền theo định dạng VND
     * @param amount Số tiền cần định dạng
     * @return Chuỗi đã định dạng (ví dụ: 120.000đ)
     */
    public static String formatVND(double amount) {
        NumberFormat formatter = NumberFormat.getInstance(LOCALE_VN);
        return formatter.format(amount) + "đ";
    }
    
    /**
     * Định dạng số tiền theo định dạng VND với số lẻ 
     * @param amount Số tiền cần định dạng
     * @return Chuỗi đã định dạng (ví dụ: 120.000,50đ)
     */
    public static String formatVNDWithDecimals(double amount) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);
        return formatter.format(amount) + "đ";
    }
    
    /**
     * Định dạng số tiền theo định dạng triệu VND
     * @param amount Số tiền cần định dạng (đơn vị đồng)
     * @return Chuỗi đã định dạng (ví dụ: 1,2 triệu)
     */
    public static String formatToMillionVND(double amount) {
        double millions = amount / 1000000.0;
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        formatter.setMaximumFractionDigits(1);
        return formatter.format(millions) + " triệu";
    }
    
    /**
     * Định dạng số thành chuỗi tiền tệ Việt Nam (không có đơn vị đ)
     * @param amount Số tiền cần định dạng
     * @return Chuỗi đã định dạng không có đơn vị
     */
    public static String formatNumber(double amount) {
        NumberFormat currencyFormatter = NumberFormat.getNumberInstance(LOCALE_VN);
        return currencyFormatter.format(amount);
    }
    
    /**
     * Chuyển đổi chuỗi tiền tệ thành số
     * @param amountStr Chuỗi tiền tệ cần chuyển đổi
     * @return Số tiền
     * @throws NumberFormatException nếu chuỗi không thể chuyển đổi thành số
     */
    public static double parseVND(String amountStr) throws NumberFormatException {
        // Loại bỏ tất cả đơn vị tiền tệ và dấu định dạng
        String cleanString = amountStr.replace("đồng", "")
                .replace("đ", "")
                .replace("₫", "")
                .replace("VND", "")
                .replace(".", "")
                .replace(",", "")
                .trim();
        
        return Double.parseDouble(cleanString);
    }
} 