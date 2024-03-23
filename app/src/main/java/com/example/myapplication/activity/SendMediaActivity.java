package com.example.myapplication.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.model.MediaItem;

import java.io.File;

public class SendMediaActivity extends AppCompatActivity {
    private ImageView imageView, btn_send_image;
    private VideoView videoView;
    private MediaItem mediaItem;
    private boolean camera = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_media);

        imageView = findViewById(R.id.image_view);
        videoView = findViewById(R.id.video_view);
        btn_send_image = findViewById(R.id.btn_send_image);

        // Nhận đường dẫn từ Intent
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            return;
        }
        mediaItem = (MediaItem) bundle.get("mediaItem");
        camera = (boolean) bundle.get("camera");
        if (mediaItem != null) {
            checkImageOrVideo(mediaItem);
        }


        btn_send_image.setOnClickListener(view -> {

            Intent resultIntent = new Intent();
            Bundle bundle1 = new Bundle();
            bundle1.putSerializable("mediaItem", mediaItem);
            resultIntent.putExtras(bundle1);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();

        });

    }

    private void checkImageOrVideo(MediaItem mediaItem) {
        if (mediaItem.isVideo()) {
            imageView.setVisibility(View.GONE);
            videoView.setVisibility(View.VISIBLE);
            displayVideo(mediaItem.getPath());
        } else {
            imageView.setVisibility(View.VISIBLE);
            videoView.setVisibility(View.GONE);
            displayImage(mediaItem.getPath());
        }
    }

    private void displayImage(String imagePath) {
        // Sử dụng Glide để hiển thị ảnh từ đường dẫn
        Glide.with(this)
                .load(new File(imagePath))
                .into(imageView);
    }

    private void displayVideo(String videoPath) {
        Uri videoUri = Uri.parse(videoPath);

        // Thiết lập MediaController để quản lý phương tiện
        MediaController mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);

        // Thiết lập đường dẫn video
        videoView.setVideoURI(videoUri);
    }

    private void sendDataToActivity(MediaItem mediaItem, boolean close) {
        Intent intent = new Intent("send_data_camreaX");
        Bundle bundle = new Bundle();
        bundle.putSerializable("mediaItem", mediaItem);
        bundle.putSerializable("close", close);
        intent.putExtras(bundle);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}