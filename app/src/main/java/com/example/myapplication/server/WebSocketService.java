package com.example.myapplication.server;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.myapplication.R;
import com.example.myapplication.activity.SplashScreen;
import com.example.myapplication.activity.TestActivity;
import com.example.myapplication.model.MessagesModel;
import com.google.gson.Gson;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class WebSocketService extends Service {

    private static final String TAG = ">>>>>>>>>>>>>>>>";
    private static final String IP = "192.168.172.170";
    private static WebSocket webSocket;
    private final IBinder binder = new LocalBinder();
    private static final String CHANNEL_ID = "WebSocketServiceChannel";
    private int user_id;
    private String token;

    public class LocalBinder extends Binder {
        public WebSocketService getService() {
            return WebSocketService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            user_id = intent.getIntExtra("user_id", 0);
            token = intent.getStringExtra("token");
        }

        if (webSocket == null) {
            startWebSocket();
        }
//        createNotificationChannel();
//        Notification notification = buildNotification();
//        startForeground(1, notification);

        return START_STICKY;
    }

    private void startWebSocket() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("ws://" + IP + ":8081?token=" + token + "&user_id=" + user_id)
                .build();

        webSocket = client.newWebSocket(request, createSocketListener());
    }

    private WebSocketListener createSocketListener() {
        return new WebSocketListener() {
            @Override
            public void onOpen(@NonNull WebSocket webSocket, @NonNull okhttp3.Response response) {
                Log.d(TAG, "WebSocket Connection Open");
            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
                Gson gson = new Gson();
                MessagesModel response = gson.fromJson(text, MessagesModel.class);
                sendActionToActivity(response);
            }

            @Override
            public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                Log.d(TAG, "WebSocket Connection Closed. Code: " + code + ", Reason: " + reason);
            }

            @Override
            public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, okhttp3.Response response) {
                Log.e(TAG, "WebSocket Connection Failure: " + t.getMessage());
            }
        };
    }


    public void sendMessage(String message) {
        if (webSocket != null) {
            webSocket.send(message);
        }
    }

    public void closeWebSocket() {
        if (webSocket != null) {
            webSocket.close(1000, "Connection closed by client");
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "WebSocket Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private Notification buildNotification() {
        Intent notificationIntent = new Intent(this, SplashScreen.class); // Replace with your main activity
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("WebSocket Service")
                .setContentText("WebSocket connection is active")
                .setSmallIcon(R.drawable.logo) // Replace with your notification icon
                .setContentIntent(pendingIntent)
                .build();
    }


    private void sendActionToActivity(MessagesModel messagesModel) {
        Intent intent = new Intent("send_data_to_activity");
        Bundle bundle = new Bundle();
        bundle.putSerializable("object_messages", messagesModel);
        intent.putExtras(bundle);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
