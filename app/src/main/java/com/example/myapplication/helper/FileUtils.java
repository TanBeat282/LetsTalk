package com.example.myapplication.helper;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;

public class FileUtils {

    public static String getFileNameFromUri(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            ContentResolver contentResolver = context.getContentResolver();
            Cursor cursor = null;
            try {
                cursor = contentResolver.query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int displayNameIndex = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
                    if (displayNameIndex != -1) {
                        result = cursor.getString(displayNameIndex);
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else if (uri.getScheme().equals("file")) {
            result = new java.io.File(uri.getPath()).getName();
        }
        return result;
    }

    public static String getFileExtensionFromUri(Context context, Uri uri) {
        ContentResolver contentResolver = context.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        // Lấy đuôi file từ ContentResolver
        String extension = mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));

        // Nếu ContentResolver không cung cấp đuôi file, thử lấy từ tên file
        if (extension == null || extension.isEmpty()) {
            String fileName = getFileNameFromUri(context, uri);
            int lastDotIndex = fileName.lastIndexOf(".");
            if (lastDotIndex != -1) {
                extension = fileName.substring(lastDotIndex + 1);
            }
        }

        return extension;
    }
}
