package com.example.myapplication.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import android.Manifest;

import com.example.myapplication.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.EmojiTextView;

import java.io.File;
import java.io.IOException;

public class TestActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION_CODE = 1;
    private long recordingStartTime;
    private MediaRecorder mediaRecorder;
    private String audioFilePath;
    private boolean isRecording = false;

    private Button recordButton;
    private Button stopButton;
    private TextView timerTextView;

    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        recordButton = findViewById(R.id.recordButton);
        stopButton = findViewById(R.id.stopButton);
        timerTextView = findViewById(R.id.timerTextView);

        File externalFilesDir = getExternalFilesDir(null);
        if (externalFilesDir != null) {
            audioFilePath = externalFilesDir.getAbsolutePath() + "/audio.3gp";
        }


        // Check and request permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, REQUEST_PERMISSION_CODE);
        } else {
            // Kiểm tra đường dẫn
            Log.d("TestActivity", "External storage directory: " + Environment.getExternalStorageDirectory());
        }


        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isRecording) {
                    startRecording();
                }
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRecording) {
                    stopRecording();
                }
            }
        });
    }

    private void startRecording() {
        if (mediaRecorder == null) {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setOutputFile(audioFilePath);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recordingStartTime = SystemClock.elapsedRealtime();

            try {
                mediaRecorder.prepare();
                mediaRecorder.start();

                isRecording = true;
                startTimer();

                Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void stopRecording() {
        if (isRecording) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;

            isRecording = false;
            stopTimer();

            uploadAudio();
            Toast.makeText(this, "Recording stopped", Toast.LENGTH_SHORT).show();
        }
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(Long.MAX_VALUE, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long elapsedTime = SystemClock.elapsedRealtime() - recordingStartTime;
                long totalSeconds = elapsedTime / 1000;
                long minutes = totalSeconds / 60;
                long seconds = totalSeconds % 60;

                String time = String.format("Recording time: %02d:%02d", minutes, seconds);
                timerTextView.setText(time);
            }

            @Override
            public void onFinish() {
            }
        }.start();
    }

    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
            timerTextView.setText("Recording stopped");
        }
    }

    private void uploadAudio() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference audioRef = storageRef.child("record/" + System.currentTimeMillis() + ".3gp");

        Uri audioUri = Uri.fromFile(new File(audioFilePath));

        audioRef.putFile(audioUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(TestActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(TestActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}