package com.example.myapplication.helper;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class FirebaseUploaderMessages {
    private static String file_name;
    private static String path;
    private static int type_messages = 0;

    // Phương thức tải lên tệp lên Firebase Storage
    public static void uploadFile(Context context, int messages_list, Uri imagePath, int type) {
        type_messages = type;


        // Tạo tham chiếu đến Firebase Storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        if (type == 2) {
            file_name = System.currentTimeMillis() + "_" + FileUtils.getFileNameFromUri(context, imagePath);
            path = "messages_list/" + messages_list + "/video/";
        } else if (type == 3) {
            file_name = System.currentTimeMillis() + "_" + FileUtils.getFileNameFromUri(context, imagePath);
            path = "messages_list/" + messages_list + "/record/";
        } else if (type == 4) {
            file_name = System.currentTimeMillis() + "_" + FileUtils.getFileNameFromUri(context, imagePath);
            path = "messages_list/" + messages_list + "/file/";
        } else {
            file_name = System.currentTimeMillis() + "_" + FileUtils.getFileNameFromUri(context, imagePath);
            path = "messages_list/" + messages_list + "/image/";
        }


        // Tạo tham chiếu đến nơi lưu trữ trên Firebase Storage
        StorageReference imageRef = storageRef.child(path + file_name);


        // Tạo ProgressDialog
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Đang gửi...");
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
                    sendBroadcast(context, imageUrl);
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

    // Phương thức để gửi broadcast
    private static void sendBroadcast(Context context, String imageUrl) {
        Intent intent = new Intent("url_media");
        intent.putExtra("url", imageUrl);
        intent.putExtra("type", type_messages);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
