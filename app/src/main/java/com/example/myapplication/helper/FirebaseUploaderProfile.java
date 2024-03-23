package com.example.myapplication.helper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.myapplication.server.Url_Api;
import com.example.myapplication.sharedpreferencesmanager.SharedPreferencesManager;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class FirebaseUploaderProfile {
    private static String file_name;
    private static String path;
    private static Context context;
    private static Activity activity;

    // Phương thức tải lên tệp lên Firebase Storage
    public static void uploadFile(Context mcontext, Activity mactivity, Uri imagePath, int type) {
        context = mcontext;
        activity = mactivity;

        SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(context);
        int user_id = sharedPreferencesManager.getUserId();

        // Tạo tham chiếu đến Firebase Storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        if (type == 1) {
            file_name = System.currentTimeMillis() + "_" + FileUtils.getFileNameFromUri(context, imagePath);
            path = "users/" + user_id + "/profile_image/";
        } else {
            file_name = System.currentTimeMillis() + "_" + FileUtils.getFileNameFromUri(context, imagePath);
            path = "users/" + user_id + "/cover_avatar/";
        }

        // Tạo tham chiếu đến nơi lưu trữ trên Firebase Storage
        StorageReference imageRef = storageRef.child(path + file_name);


        // Tạo ProgressDialog
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Đang áp dụng...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Tải ảnh lên Firebase Storage
        UploadTask uploadTask = imageRef.putFile(imagePath);

        // Xử lý sự kiện khi quá trình tải hoàn thành
        uploadTask.addOnCompleteListener(task -> {
            progressDialog.dismiss(); // Đóng ProgressDialog khi quá trình hoàn thành
            if (task.isSuccessful()) {
                // Lấy URL của ảnh trên Firebase Storage sau khi tải lên thành công
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    // Gửi broadcast từ lớp FileUploader
                    changeInfo(type == 0 ? enCodeQrCode(imageUrl) : null, type == 1 ? enCodeQrCode(imageUrl) : null, null, null, null, -2, null, user_id);
                });
            } else {
                // Xử lý khi có lỗi xảy ra
                Toast.makeText(context, "Upload failed", Toast.LENGTH_SHORT).show();
            }
        });

        // Xử lý sự kiện cập nhật tiến trình
        uploadTask.addOnProgressListener(snapshot -> {
            // Lấy phần trăm tiến trình tải lên
            double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
            // Cập nhật ProgressDialog
            progressDialog.setProgress((int) progress);
        });
    }

    private static String enCodeQrCode(String qr_code) {
        return Base64.encodeToString(qr_code.getBytes(), Base64.DEFAULT);
    }

    private static void changeInfo(String profile_image, String cover_avatar, String description, String full_name, String dob, int sex, String address, int user_id) {

        OkHttpClient client = new OkHttpClient();

        String url = Url_Api.getInstance().editProfile(profile_image, cover_avatar, description, full_name, dob, sex, address, user_id);
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    ResponseBody responseBody = response.body();
                    if (responseBody != null) {
                        String jsonString = responseBody.string();
                        try {
                            JSONObject jsonObject = new JSONObject(jsonString);
                            boolean status = jsonObject.getBoolean("status");
                            if (status) {
                                activity.runOnUiThread(new Runnable() {
                                    @SuppressLint("SetTextI18n")
                                    @Override
                                    public void run() {
                                        sendDataToActivity(true);
                                        Toast.makeText(context, "Chỉnh sửa thành công", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }


        });
    }

    private static void sendDataToActivity(boolean is_load) {
        Intent intent = new Intent("is_load_info");
        Bundle bundle = new Bundle();
        bundle.putSerializable("is_load", is_load);
        intent.putExtras(bundle);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
