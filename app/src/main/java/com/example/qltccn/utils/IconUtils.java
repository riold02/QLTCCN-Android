package com.example.qltccn.utils;

import android.content.Context;

import com.example.qltccn.R;

public class IconUtils {

    /**
     * Chuyển đổi tên icon thành resource ID
     */
    public static int getIconResourceId(Context context, String iconName) {
        if (iconName == null || iconName.isEmpty()) {
            return R.drawable.ic_general; // Icon mặc định
        }

        // Tìm kiếm icon dựa trên tên
        switch (iconName.toLowerCase()) {
            case "ic_salary":
                return R.drawable.ic_salary;
            case "ic_investment":
                return R.drawable.ic_entertainment;
            case "ic_gift":
                return R.drawable.ic_gifts;
            case "ic_food":
                return R.drawable.ic_food;
            case "ic_transport":
                return R.drawable.ic_transport;
            case "ic_entertainment":
                return R.drawable.ic_entertainment;
            case "ic_shopping":
                return R.drawable.ic_groceries;
            case "ic_health":
                return R.drawable.ic_medicine;
            case "ic_education":
                return R.drawable.ic_gifts;
            default:
                return R.drawable.ic_general;
        }
    }

    /**
     * Lấy danh sách tên các icon có sẵn
     */
    public static String[] getAvailableIconNames() {
        return new String[] {
                "ic_salary",
                "ic_investment",
                "ic_gift",
                "ic_food",
                "ic_transport",
                "ic_entertainment",
                "ic_shopping",
                "ic_health",
                "ic_education",
                "ic_general"
        };
    }
} 