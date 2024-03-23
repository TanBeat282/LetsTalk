package com.example.myapplication.helper;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionUtils {

    // Hằng số để sử dụng trong onRequestPermissionsResult
    public static final int PERMISSION_REQUEST_CODE = 100;

    // Hàm kiểm tra xem quyền đã được cấp hay chưa
    public static boolean isPermissionGranted(Activity activity, String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int result = ContextCompat.checkSelfPermission(activity, permission);
            return result == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    // Hàm xin quyền
    public static void requestPermission(Activity activity, String[] permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Kiểm tra xem quyền đã được cấp hay chưa
            boolean allPermissionsGranted = true;
            for (String permission : permissions) {
                if (!isPermissionGranted(activity, permission)) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            // Xin quyền từ người dùng nếu có ít nhất một quyền chưa được cấp
            if (!allPermissionsGranted) {
                ActivityCompat.requestPermissions(activity, permissions, PERMISSION_REQUEST_CODE);
            }
        }
    }
}

