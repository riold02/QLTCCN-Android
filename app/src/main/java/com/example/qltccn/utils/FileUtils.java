package com.example.qltccn.utils;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class FileUtils {
    private static final String TAG = "FileUtils";

    /**
     * Lấy đường dẫn thực tế của file từ URI
     */
    @SuppressLint("NewApi")
    public static String getPath(final Context context, final Uri uri) {
        // Kiểm tra URI có null không
        if (uri == null) {
            return null;
        }

        Log.d(TAG, "URI: " + uri.toString());

        // Kiểm tra xem là file URI hay không (file://...)
        if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        // API cấp cao hơn KitKat
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // DocumentProvider
            if (DocumentsContract.isDocumentUri(context, uri)) {
                Log.d(TAG, "DocumentsContract URI");
                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }
                }
                // DownloadsProvider
                else if (isDownloadsDocument(uri)) {
                    final String id = DocumentsContract.getDocumentId(uri);
                    try {
                        final Uri contentUri = ContentUris.withAppendedId(
                                Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                        return getDataColumn(context, contentUri, null, null);
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "Error getting path from Downloads URI: " + e.getMessage());
                        return getFilePathFromUri(context, uri);
                    }
                }
                // MediaProvider
                else if (isMediaDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }

                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[] {
                            split[1]
                    };

                    return getDataColumn(context, contentUri, selection, selectionArgs);
                }
            }
            // MediaStore (và các providers chung khác)
            else if ("content".equalsIgnoreCase(uri.getScheme())) {
                Log.d(TAG, "Content URI");
                return getDataColumn(context, uri, null, null);
            }
        }

        // Fallback cho tất cả các trường hợp còn lại
        return getFilePathFromUri(context, uri);
    }

    /**
     * Lấy giá trị của cột "_data" trong database, tương ứng với đường dẫn file
     */
    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting data column: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return getFilePathFromUri(context, uri);
    }

    /**
     * Phương pháp dự phòng để lấy đường dẫn file: tạo một bản sao tạm thời
     */
    public static String getFilePathFromUri(Context context, Uri uri) {
        try {
            String fileName = getFileName(context, uri);
            File cacheDir = context.getCacheDir();
            File file = new File(cacheDir, fileName);

            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;
            
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[4 * 1024]; // 4k buffer
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
            
            inputStream.close();
            outputStream.close();
            
            return file.getAbsolutePath();
        } catch (Exception e) {
            Log.e(TAG, "Error getting file path from URI: " + e.getMessage());
            return null;
        }
    }

    /**
     * Lấy tên file từ uri
     */
    public static String getFileName(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }
        
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        
        // Tạo tên file ngẫu nhiên nếu không tìm thấy
        if (result == null || result.isEmpty()) {
            result = "file_" + System.currentTimeMillis() + ".jpg";
        }
        
        return result;
    }

    /**
     * Kiểm tra xem uri có phải là ExternalStorageProvider hay không
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * Kiểm tra xem uri có phải là DownloadsProvider hay không
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * Kiểm tra xem uri có phải là MediaProvider hay không
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
} 