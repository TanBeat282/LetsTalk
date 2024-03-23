package com.example.myapplication.notification;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.myapplication.R;
import com.example.myapplication.activity.MessagesActivity;
import com.example.myapplication.model.MessagesListModel;
import com.example.myapplication.model.MessagesModel;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @SuppressLint("LogNotTimber")
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        // Kiểm tra xem thông báo có dữ liệu nội dung không
        if (remoteMessage.getData().size() > 0) {
            // Lấy các giá trị từ dữ liệu
            String title = remoteMessage.getNotification().getTitle(); // Lấy title
            String content = remoteMessage.getNotification().getBody(); // Lấy body
            String profileImageUrl = remoteMessage.getData().get("profile_image");

            if (title != null && content != null && profileImageUrl != null) {
                // Tạo và hiển thị thông báo
                sendNotification(title, content, profileImageUrl, null);
            }
        }
    }

    private void sendNotification(String title, String content, String profileImageUrl, MessagesListModel messagesListModel) {
        // Tạo một Intent để mở ứng dụng của bạn khi thông báo được nhấn
        Intent intent = new Intent(this, MessagesActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("messages_list", messagesListModel);
        intent.putExtras(bundle);


        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        // Xây dựng thông báo
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        // Tải hình ảnh từ URL và đặt nó làm hình ảnh thông báo
        try {
            URL url = new URL(profileImageUrl);
            Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            notificationBuilder.setLargeIcon(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Hiển thị thông báo
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1 /* ID of notification */, notificationBuilder.build());
    }

}
