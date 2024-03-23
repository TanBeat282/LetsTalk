package com.example.myapplication.bottomsheet;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapter.MediaAdapter;
import com.example.myapplication.model.MediaItem;
import com.example.myapplication.model.MessagesModel;
import com.example.myapplication.server.WebSocketService;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class BottomSheetMedia extends BottomSheetDialogFragment {
    private final Context context;
    private final Activity activity;
    private BottomSheetDialog bottomSheetDialog;
    //request
    private static final int REQUEST_PERMISSION_CODE = 1;

    private RecyclerView rv_media;
    private MediaAdapter mediaAdapter;

    //get data form wesocket servicer
    private final BroadcastReceiver broadcastReceiverWebsocket = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle == null) {
                return;
            }
            boolean close = bundle.getBoolean("close", false);
            if (close) {
                bottomSheetDialog.dismiss();
            }
        }
    };


    public BottomSheetMedia(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        bottomSheetDialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_media, null);
        bottomSheetDialog.setContentView(view);

        rv_media = view.findViewById(R.id.rv_media);
        rv_media.setLayoutManager(new GridLayoutManager(context, 3));

        checkAndRequestPermissions();
        return bottomSheetDialog;
    }

    private List<MediaItem> getAllMedia() {
        List<MediaItem> mediaList = new ArrayList<>();

        // Thêm một item mới vào đầu danh sách (icon camera từ drawable)
        Drawable cameraIconDrawable = getResources().getDrawable(R.drawable.baseline_camera);
        Bitmap cameraIconBitmap = drawableToBitmap(cameraIconDrawable);
        String cameraIconPath = saveBitmapToCache(cameraIconBitmap, "camera_icon");
        mediaList.add(0, new MediaItem(cameraIconPath, false));

        String[] projection = {MediaStore.Files.FileColumns.DATA, MediaStore.Files.FileColumns.MEDIA_TYPE};
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Files.getContentUri("external"),
                projection,
                null,
                null,
                null
        );

        if (cursor != null) {
            int columnIndexData = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);
            int columnIndexMediaType = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE);

            while (cursor.moveToNext()) {
                String path = cursor.getString(columnIndexData);
                int mediaType = cursor.getInt(columnIndexMediaType);

                // Kiểm tra xem mục có phải là ảnh hay video
                boolean isVideo = mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

                // Chỉ thêm vào danh sách nếu là ảnh hoặc video
                if (isImageOrVideo(path)) {
                    mediaList.add(new MediaItem(path, isVideo));
                }
            }

            cursor.close();
        }

        // Sắp xếp danh sách theo thời gian giảm dần
        mediaList.sort((item1, item2) -> {
            File file1 = new File(item1.getPath());
            File file2 = new File(item2.getPath());

            // So sánh thời gian giảm dần
            return Long.compare(file2.lastModified(), file1.lastModified());
        });

        return mediaList;
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private String saveBitmapToCache(Bitmap bitmap, String fileName) {
        File cachePath = new File(context.getCacheDir(), "images");
        cachePath.mkdirs();
        File imagePath = new File(cachePath, fileName + ".png");

        try (FileOutputStream stream = new FileOutputStream(imagePath)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return imagePath.getAbsolutePath();
    }

    private void checkAndRequestPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();

        // Kiểm tra quyền đọc ảnh từ thư viện
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        // Kiểm tra quyền đọc video từ thư viện
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        // Xin quyền nếu cần thiết
        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(
                    activity,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSION_CODE
            );
        } else {
            List<MediaItem> mediaList = getAllMedia();
            mediaAdapter = new MediaAdapter(context, activity, mediaList);
            rv_media.setAdapter(mediaAdapter);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    List<MediaItem> mediaList = getAllMedia();
                    mediaAdapter = new MediaAdapter(context, activity, mediaList);
                    rv_media.setAdapter(mediaAdapter);
                } else {
                    // Quyền không được cấp, thông báo cho người dùng
                    Toast.makeText(context, "Quyền truy cập bị từ chối", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private boolean isImageOrVideo(String path) {
        if (path == null) {
            return false;
        }

        String lowerCasePath = path.toLowerCase();

        // Kiểm tra phần mở rộng của tên tệp
        return lowerCasePath.endsWith(".jpg") || lowerCasePath.endsWith(".jpeg") ||
                lowerCasePath.endsWith(".png") || lowerCasePath.endsWith(".gif") ||
                lowerCasePath.endsWith(".bmp") || lowerCasePath.endsWith(".webp") ||
                lowerCasePath.endsWith(".mp4") || lowerCasePath.endsWith(".3gp") ||
                lowerCasePath.endsWith(".mkv") || lowerCasePath.endsWith(".avi") ||
                lowerCasePath.endsWith(".mov") || lowerCasePath.endsWith(".flv");

        // Các tiêu chí khác có thể được thêm vào tùy thuộc vào yêu cầu cụ thể của bạn
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiverWebsocket, new IntentFilter("send_data_media_adapter"));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiverWebsocket);
    }
}
