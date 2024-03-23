package com.example.myapplication.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.adapter.MessagesAdapter;
import com.example.myapplication.server.Url_Api;
import com.github.chrisbanes.photoview.PhotoView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ViewMediaActivity extends AppCompatActivity {
    private long downloadID;
    private String url_media;
    private boolean is_video;

    private int sender_id;
    private TextView txtTitle;
    private static final int REQUEST_WRITE_STORAGE_PERMISSION = 101;

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_media);

        ImageView imgDown = findViewById(R.id.imgDown);
        ImageView imgBack = findViewById(R.id.imgBack);
        txtTitle = findViewById(R.id.txtTitle);
        PhotoView photoView = findViewById(R.id.photo_view);
        VideoView video_view = findViewById(R.id.video_view);

        url_media = getIntent().getStringExtra("url_media");
        sender_id = getIntent().getIntExtra("sender_id", 0);
        is_video = getIntent().getBooleanExtra("is_video", false);

        if (sender_id != 0) {
            getInfo(sender_id);
        }

        if (is_video) {
            photoView.setVisibility(View.GONE);
            video_view.setVisibility(View.VISIBLE);

            Uri videoUri = Uri.parse(url_media);
            video_view.setVideoURI(videoUri);

            // Thêm MediaController
            MediaController mediaController = new MediaController(this);
            video_view.setMediaController(mediaController);
        } else {
            photoView.setVisibility(View.VISIBLE);
            video_view.setVisibility(View.GONE);

            Glide.with(this)
                    .load(url_media)
                    .into(photoView);

            Uri uri = Uri.parse(url_media);
            Glide.with(this)
                    .load(uri)
                    .into(photoView);
        }

        imgDown.setOnClickListener(v -> xinquyen());
        imgBack.setOnClickListener(view -> {
            finish();
        });

        registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                downloadImage(url_media);
            } else {
                Toast.makeText(this, "Ứng dụng cần quyền ghi vào bộ nhớ để tải tệp về.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void xinquyen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE_PERMISSION);
        } else {
            downloadImage(url_media);
        }
    }

    private void downloadImage(String url) {
        File myFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "LetsTalk");
        if (!myFolder.exists()) {
            myFolder.mkdirs();
        }

        String fileName = URLUtil.guessFileName(url, null, null);
        File destinationFile = new File(myFolder, subStringFileName(fileName));
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setTitle(subStringFileName(fileName))
                .setDescription("Đang lưu...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationUri(Uri.fromFile(destinationFile));

        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        downloadID = downloadManager.enqueue(request);
    }

    private final BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (id == downloadID) {
                Toast.makeText(ViewMediaActivity.this, "Đã lưu thành công", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void getInfo(int user_id) {
        OkHttpClient client = new OkHttpClient();

        String url = Url_Api.getInstance().getInfo(user_id);
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
                                JSONObject dataObject = jsonObject.getJSONObject("data");

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        // Thực hiện các hoạt động liên quan đến giao diện người dùng trên luồng chín
                                        try {
                                            txtTitle.setText(dataObject.getString("full_name"));
                                        } catch (JSONException e) {
                                            throw new RuntimeException(e);
                                        }

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

    private String subStringFileName(String file_name) {
        int underscoreIndex = file_name.indexOf("_");

        if (underscoreIndex != -1) {
            // Kiểm tra xem phần trước dấu "_" là một số hay không
            String potentialTimestamp = file_name.substring(0, underscoreIndex);

            try {
                Long.parseLong(potentialTimestamp); // Thử chuyển đổi thành số
                // Nếu không có exception, đây là một số và có thể cắt
                return file_name.substring(underscoreIndex + 1);
            } catch (NumberFormatException e) {
                // Nếu có exception, phần trước dấu "_" không phải là một số
                return file_name;
            }
        } else {
            return file_name;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(onDownloadComplete); // Hủy đăng ký BroadcastReceiver khi không cần nữa
    }
}