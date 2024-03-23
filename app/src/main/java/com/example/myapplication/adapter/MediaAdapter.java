package com.example.myapplication.adapter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.activity.PictureAndVideoActivity;
import com.example.myapplication.activity.SendMediaActivity;
import com.example.myapplication.model.MediaItem;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.ViewHolder> {
    private List<MediaItem> mediaList;
    private Context context;
    private Activity mActivity;
    private int YOUR_REQUEST_CODE = 123;

    private static final int REQUEST_CAMERA_PERMISSION = 1;

    public MediaAdapter(Context context, Activity mActivity, List<MediaItem> mediaList) {
        this.context = context;
        this.mActivity = mActivity;
        this.mediaList = mediaList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_media, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        MediaItem mediaItem = mediaList.get(position);

        if (mediaItem.isVideo()) {
            holder.txtTimeVideo.setVisibility(View.VISIBLE);
            holder.txtTimeVideo.setText(getVideoDuration(mediaItem.getPath()));
        } else {
            holder.txtTimeVideo.setVisibility(View.GONE);
        }

        if (position == 0) {
            // Đặt background cho item đầu tiên
            holder.imageView.setBackgroundResource(R.drawable.background_camera);

            // Áp dụng padding cho item đầu tiên
            int paddingInPx = 200;
            holder.imageView.setPadding(paddingInPx, paddingInPx, paddingInPx, paddingInPx);
        }

        // Kiểm tra null trước khi load
        if (mediaItem.getPath() != null) {
            File imageFile = new File(mediaItem.getPath());
            Glide.with(context)
                    .load(imageFile)
                    .into(holder.imageView);
        } else {
            // Xử lý trường hợp không có đường dẫn
        }

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (position == 0 && isCameraIcon(position)) {
                    // Nếu là icon camera
                    if (checkCameraPermission()) {

                        sendDataToActivity(true);
                        // Nếu đã có quyền camera, mở camera
                        Intent intent = new Intent(context, PictureAndVideoActivity.class);
                        mActivity.startActivityForResult(intent, YOUR_REQUEST_CODE);
                    } else {
                        // Nếu chưa có quyền, yêu cầu quyền camera
                        requestCameraPermission();
                    }
                } else {
                    sendDataToActivity(true);
                    Intent intent = new Intent(context, SendMediaActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("mediaItem", mediaItem);
                    bundle.putSerializable("camera", false);
                    intent.putExtras(bundle);
                    mActivity.startActivityForResult(intent, YOUR_REQUEST_CODE);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mediaList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView txtTimeVideo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            txtTimeVideo = itemView.findViewById(R.id.txtTimeVideo);
        }
    }

    private String getVideoDuration(String videoPath) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(videoPath);
        String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        try {
            retriever.release();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (duration != null) {
            long durationInMillis = Long.parseLong(duration);

            // Kiểm tra nếu thời lượng nhỏ hơn 1 giờ
            if (TimeUnit.MILLISECONDS.toHours(durationInMillis) < 1) {
                // Chuyển đổi thời gian từ milliseconds sang định dạng mm:ss
                return String.format(Locale.getDefault(), "%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(durationInMillis) % TimeUnit.HOURS.toMinutes(1),
                        TimeUnit.MILLISECONDS.toSeconds(durationInMillis) % TimeUnit.MINUTES.toSeconds(1));
            } else {
                // Chuyển đổi thời gian từ milliseconds sang định dạng h:mm:ss
                return String.format(Locale.getDefault(), "%d:%02d:%02d",
                        TimeUnit.MILLISECONDS.toHours(durationInMillis),
                        TimeUnit.MILLISECONDS.toMinutes(durationInMillis) % TimeUnit.HOURS.toMinutes(1),
                        TimeUnit.MILLISECONDS.toSeconds(durationInMillis) % TimeUnit.MINUTES.toSeconds(1));
            }
        }

        return "";
    }


    private boolean isCameraIcon(int position) {
        return position == 0 && mediaList.size() > 0 && !mediaList.get(0).isVideo();
    }

    private boolean checkCameraPermission() {
        // Kiểm tra và trả về true nếu đã có quyền, ngược lại trả về false
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestCameraPermission() {
        // Yêu cầu quyền camera
        ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
    }


    private void sendDataToActivity(boolean close) {
        Intent intent = new Intent("send_data_media_adapter");
        Bundle bundle = new Bundle();
        bundle.putSerializable("close", close);
        intent.putExtras(bundle);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
