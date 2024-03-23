package com.example.myapplication.activity;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.MediaStoreOutputOptions;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.myapplication.model.MediaItem;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.myapplication.R;

public class PictureAndVideoActivity extends AppCompatActivity {
    ImageButton capture, recordVideo, toggleFlash, flipCamera, btn_video, btn_picture;
    private PreviewView previewView;
    int cameraFacing = CameraSelector.LENS_FACING_BACK;
    private int YOUR_REQUEST_CODE = 123;

    ExecutorService service;
    Recording recording = null;
    VideoCapture<Recorder> videoCapture = null;

    private final ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean result) {
            if (result) {
                startCameraPicture(cameraFacing);
            }
        }
    });
    private final ActivityResultLauncher<String> activityResultLauncherVideo = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
        if (ActivityCompat.checkSelfPermission(PictureAndVideoActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera(cameraFacing);
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_and_video);
        previewView = findViewById(R.id.cameraPreview);
        capture = findViewById(R.id.capture);
        toggleFlash = findViewById(R.id.toggleFlash);
        flipCamera = findViewById(R.id.flipCamera);
        btn_video = findViewById(R.id.btn_video);
        btn_picture = findViewById(R.id.btn_picture);
        recordVideo = findViewById(R.id.recordVideo);

        btn_picture.setVisibility(View.GONE);
        btn_video.setVisibility(View.VISIBLE);
        recordVideo.setVisibility(View.GONE);
        capture.setVisibility(View.VISIBLE);


        if (ContextCompat.checkSelfPermission(PictureAndVideoActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            activityResultLauncher.launch(android.Manifest.permission.CAMERA);
        } else {
            startCameraPicture(cameraFacing);
        }

        if (ActivityCompat.checkSelfPermission(PictureAndVideoActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            activityResultLauncherVideo.launch(Manifest.permission.CAMERA);
        } else {
            startCamera(cameraFacing);
        }


        flipCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cameraFacing == CameraSelector.LENS_FACING_BACK) {
                    cameraFacing = CameraSelector.LENS_FACING_FRONT;
                } else {
                    cameraFacing = CameraSelector.LENS_FACING_BACK;
                }
                startCameraPicture(cameraFacing);
            }
        });
        btn_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_picture.setVisibility(View.VISIBLE);
                btn_video.setVisibility(View.GONE);
                recordVideo.setVisibility(View.VISIBLE);
                capture.setVisibility(View.GONE);
            }
        });
        btn_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_picture.setVisibility(View.GONE);
                btn_video.setVisibility(View.VISIBLE);
                recordVideo.setVisibility(View.GONE);
                capture.setVisibility(View.VISIBLE);
            }
        });

        recordVideo.setOnClickListener(view -> {
            if (ActivityCompat.checkSelfPermission(PictureAndVideoActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                activityResultLauncherVideo.launch(Manifest.permission.CAMERA);
            } else if (ActivityCompat.checkSelfPermission(PictureAndVideoActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                activityResultLauncherVideo.launch(Manifest.permission.RECORD_AUDIO);
            } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P && ActivityCompat.checkSelfPermission(PictureAndVideoActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                activityResultLauncherVideo.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            } else {
                captureVideo();
            }
        });
        service = Executors.newSingleThreadExecutor();
    }

    public void startCameraPicture(int cameraFacing) {
        int aspectRatio = aspectRatio(previewView.getWidth(), previewView.getHeight());
        ListenableFuture<ProcessCameraProvider> listenableFuture = ProcessCameraProvider.getInstance(this);

        listenableFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = (ProcessCameraProvider) listenableFuture.get();

                Preview preview = new Preview.Builder().setTargetAspectRatio(aspectRatio).build();

                ImageCapture imageCapture = new ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation()).build();

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(cameraFacing).build();

                cameraProvider.unbindAll();

                Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

                capture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (ContextCompat.checkSelfPermission(PictureAndVideoActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            activityResultLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        }
                        takePicture(imageCapture);
                    }
                });

                toggleFlash.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setFlashIcon(camera);
                    }
                });

                preview.setSurfaceProvider(previewView.getSurfaceProvider());
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    public void startCamera(int cameraFacing) {
        ListenableFuture<ProcessCameraProvider> processCameraProvider = ProcessCameraProvider.getInstance(PictureAndVideoActivity.this);

        processCameraProvider.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = processCameraProvider.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                Recorder recorder = new Recorder.Builder()
                        .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                        .build();
                videoCapture = VideoCapture.withOutput(recorder);

                cameraProvider.unbindAll();

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(cameraFacing).build();

                Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, videoCapture);

                toggleFlash.setOnClickListener(view -> toggleFlash(camera));
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(PictureAndVideoActivity.this));
    }

    private void toggleFlash(Camera camera) {
        if (camera.getCameraInfo().hasFlashUnit()) {
            if (camera.getCameraInfo().getTorchState().getValue() == 0) {
                camera.getCameraControl().enableTorch(true);
                toggleFlash.setImageResource(R.drawable.flash_off);
            } else {
                camera.getCameraControl().enableTorch(false);
                toggleFlash.setImageResource(R.drawable.flash_onl);
            }
        } else {
            runOnUiThread(() -> Toast.makeText(PictureAndVideoActivity.this, "Flash is not available currently", Toast.LENGTH_SHORT).show());
        }
    }


    public void takePicture(ImageCapture imageCapture) {
        final File file = new File(getExternalFilesDir(null), System.currentTimeMillis() + ".jpg");
        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).build();
        imageCapture.takePicture(outputFileOptions, Executors.newCachedThreadPool(), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                runOnUiThread(() -> {
                    MediaItem mediaItem = new MediaItem(file.getPath(), false);
                    Intent intent = new Intent(PictureAndVideoActivity.this, SendMediaActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("mediaItem", mediaItem);
                    bundle.putSerializable("camera", true);
                    intent.putExtras(bundle);
                    startActivityForResult(intent, YOUR_REQUEST_CODE);
                    // Không cần finish() ở đây để màn hình `SendMediaActivity` có thể nhận được kết quả trả về
                });
                startCameraPicture(cameraFacing);
            }


            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(PictureAndVideoActivity.this, "Failed to save: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                startCameraPicture(cameraFacing);
            }
        });
    }

    public void captureVideo() {
        recordVideo.setImageResource(R.drawable.round_stop_circle_24);
        Recording recording1 = recording;
        if (recording1 != null) {
            recording1.stop();
            recording = null;
            return;
        }
        String name = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.getDefault()).format(System.currentTimeMillis());
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
        contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Video");

        MediaStoreOutputOptions options = new MediaStoreOutputOptions.Builder(getContentResolver(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                .setContentValues(contentValues).build();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        recording = videoCapture.getOutput().prepareRecording(PictureAndVideoActivity.this, options).withAudioEnabled().start(ContextCompat.getMainExecutor(PictureAndVideoActivity.this), videoRecordEvent -> {
            if (videoRecordEvent instanceof VideoRecordEvent.Start) {
                capture.setEnabled(true);
            } else if (videoRecordEvent instanceof VideoRecordEvent.Finalize) {
                if (!((VideoRecordEvent.Finalize) videoRecordEvent).hasError()) {
                    Uri videoUri = ((VideoRecordEvent.Finalize) videoRecordEvent).getOutputResults().getOutputUri();
                    String filePath = getPathFromUri(PictureAndVideoActivity.this, videoUri);

                    if (filePath != null) {
                        MediaItem mediaItem = new MediaItem(filePath, true);
                        Intent intent = new Intent(PictureAndVideoActivity.this, SendMediaActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("mediaItem", mediaItem);
                        bundle.putSerializable("camera", true);
                        intent.putExtras(bundle);
                        startActivityForResult(intent, YOUR_REQUEST_CODE);
                    } else {
                        // Xử lý lỗi không thể lấy đường dẫn từ Uri
                        Toast.makeText(PictureAndVideoActivity.this, "Failed to retrieve file path", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    recording.close();
                    recording = null;
                    String msg = "Error: " + ((VideoRecordEvent.Finalize) videoRecordEvent).getError();
                    Toast.makeText(PictureAndVideoActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
                capture.setImageResource(R.drawable.round_fiber_manual_record_24);
            }
        });
    }

    public static String getPathFromUri(Context context, Uri uri) {
        if (uri == null) return null;

        String path = null;
        String[] projection = {MediaStore.Video.Media.DATA};

        try (Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                path = cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return path;
    }


    private void setFlashIcon(Camera camera) {
        if (camera.getCameraInfo().hasFlashUnit()) {
            if (camera.getCameraInfo().getTorchState().getValue() == 0) {
                camera.getCameraControl().enableTorch(true);
                toggleFlash.setImageResource(R.drawable.flash_off);
            } else {
                camera.getCameraControl().enableTorch(false);
                toggleFlash.setImageResource(R.drawable.flash_onl);
            }
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(PictureAndVideoActivity.this, "Flash is not available currently", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private int aspectRatio(int width, int height) {
        double previewRatio = (double) Math.max(width, height) / Math.min(width, height);
        if (Math.abs(previewRatio - 4.0 / 3.0) <= Math.abs(previewRatio - 16.0 / 9.0)) {
            return AspectRatio.RATIO_4_3;
        }
        return AspectRatio.RATIO_16_9;
    }

    // Xử lý kết quả trả về từ SendMediaActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == YOUR_REQUEST_CODE && resultCode == RESULT_OK) {
            setResult(Activity.RESULT_OK, data);
            finish();

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        service.shutdown();
    }
}