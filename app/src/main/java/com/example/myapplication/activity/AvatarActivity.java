package com.example.myapplication.activity;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.helper.Helper;
import com.example.myapplication.server.Url_Api;
import com.example.myapplication.sharedpreferencesmanager.SharedPreferencesManager;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.makeramen.roundedimageview.RoundedImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class AvatarActivity extends AppCompatActivity {
    private String urlAvatar;
    private Bitmap bitmapAvatar = null;
    private CircleImageView circleImageView;
    private Button btnAddAvatar, btnSkip, btnComplete, btnChange;

    private String avatarDefault = "https://firebasestorage.googleapis.com/v0/b/letstalk-3d1c5.appspot.com/o/avatar%2Favatar.jpg?alt=media&token=12ef218e-39d9-4257-88c5-2c359e60998e";

    private int user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avatar);
        circleImageView = findViewById(R.id.circleImageView);
        btnAddAvatar = findViewById(R.id.btnAddAvatar);
        btnSkip = findViewById(R.id.btnSkip);
        btnComplete = findViewById(R.id.btnComplete);
        btnChange = findViewById(R.id.btnChange);

        // Thay đổi màu sắc thanh trạng thái (status bar)
        Helper.changeStatusBarColor(this, R.color.white);
        Helper.changeNavigationColor(this, R.color.white, true);

        SharedPreferencesManager sharedPreferences = new SharedPreferencesManager(getApplicationContext());
        user_id = sharedPreferences.getUserId();

        btnAddAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAvatar(user_id, avatarDefault);
                navigateToPeopleListActivity();
            }
        });

        btnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        btnComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bitmapAvatar != null) {
                    upLoadImage(bitmapAvatar);
                }
            }
        });
    }

    private void navigateToPeopleListActivity() {
        Intent intent = new Intent(AvatarActivity.this, MainActivity.class);
        startActivity(intent);
    }

    private void selectImage() {
        Boolean isAllowed = ActivityCompat.checkSelfPermission(AvatarActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        if (isAllowed) {
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            launcher.launch(intent);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 100: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    launcher.launch(intent);
                }
                break;
            }
            default:
                break;
        }
    }

    // Tra ve hinh anh
    ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Intent intent = result.getData();
                    if (intent != null) {
                        Uri selectedImage = intent.getData();
                        try {
                            bitmapAvatar = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        circleImageView.setImageBitmap(bitmapAvatar);
                        btnAddAvatar.setVisibility(View.GONE);
                        btnComplete.setVisibility(View.VISIBLE);
                        btnSkip.setVisibility(View.GONE);
                        btnChange.setVisibility(View.VISIBLE);
                    }
                }
            }
    );

    private void upLoadImage(Bitmap bitmap) {
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference();
        StorageReference imageReference = storageReference
                .child("avatar/" + user_id + ".jpg");
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
        byte[] bytes = stream.toByteArray();
        UploadTask uploadTask = imageReference.putBytes(bytes);
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (task.isSuccessful()) return imageReference.getDownloadUrl();
                return null;
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    urlAvatar = String.valueOf(downloadUri);
                    setAvatar(user_id, urlAvatar);
                }
            }
        });
    }

    private String enCodeQrCode(String qr_code) {
        return Base64.encodeToString(qr_code.getBytes(), Base64.DEFAULT);
    }


    private void setAvatar(int user_id, String uri) {
        String avatar = enCodeQrCode(uri);

        OkHttpClient client = new OkHttpClient();

        String url = Url_Api.getInstance().editImageProfile(user_id, avatar);
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
                                runOnUiThread(() -> {
                                    startActivity(new Intent(AvatarActivity.this, MainActivity.class));
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

}