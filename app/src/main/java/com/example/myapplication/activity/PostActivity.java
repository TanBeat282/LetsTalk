package com.example.myapplication.activity;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.helper.Helper;
import com.example.myapplication.server.Url_Api;
import com.example.myapplication.sharedpreferencesmanager.SharedPreferencesManager;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class PostActivity extends AppCompatActivity {

    private int user_id;
    private LinearLayout btn_post;
    private ImageView btn_image;
    private EditText edt_input_message;
    private ArrayList<Uri> arrayList;


    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult o) {
            if (o.getResultCode() == RESULT_OK) {
                arrayList.clear();
                if (o.getData() != null && o.getData().getClipData() != null) {
                    int count = o.getData().getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        Uri imageUri = o.getData().getClipData().getItemAt(i).getUri();
                        arrayList.add(imageUri);
                    }
                }
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_post);

        SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(this);
        user_id = sharedPreferencesManager.getUserId();

        Helper.changeStatusBarColor(this, R.color.white);
        Helper.changeNavigationColor(this, R.color.white, true);


        btn_post = findViewById(R.id.btn_post);
        btn_image = findViewById(R.id.btn_image);
        edt_input_message = findViewById(R.id.edt_input_message);

        FirebaseApp.initializeApp(PostActivity.this);

        arrayList = new ArrayList<>();

        btn_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                activityResultLauncher.launch(intent);
            }
        });

        btn_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String content = edt_input_message.getText().toString().trim();
                // Thay thế ký tự xuống hàng bằng ký tự đặc biệt
                addPost(user_id, content);
                btn_post.setEnabled(false);
            }
        });

    }

    private void addPost(int user_id, String content) {
        OkHttpClient client = new OkHttpClient();

        String url = Url_Api.getInstance().addPost(user_id, content, getTime());
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
                                int post_id = jsonObject.getInt("post_id");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (arrayList != null || arrayList.size() != 0) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    uploadImages(new ArrayList<>(), arrayList, post_id);
                                                }
                                            });
                                        } else {
                                            runOnUiThread(() -> {
                                                btn_post.setEnabled(true);
                                                Toast.makeText(PostActivity.this, "Đăng bài viết thành công", Toast.LENGTH_SHORT).show();
                                                finish();
                                            });
                                        }
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(PostActivity.this, "Đăng bài viết thất bại", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void uploadImages(@NonNull ArrayList<String> imagesUrl, ArrayList<Uri> imageUriList, int post_id) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("post/" + post_id).child(UUID.randomUUID().toString());
        Uri uri = imageUriList.get(imagesUrl.size());
        storageReference.putFile(uri).addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl().addOnCompleteListener(task -> {
            String url = Objects.requireNonNull(task.getResult()).toString();
            imagesUrl.add(url);

            if (imagesUrl.size() == imageUriList.size()) {
                addImagePost(user_id, post_id, enCodeQrCode(String.valueOf(imagesUrl)));
            } else {
                uploadImages(imagesUrl, arrayList, post_id);
            }
        })).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to upload images", Toast.LENGTH_SHORT).show();
        });
    }

    private void addImagePost(int user_id, int post_id, String arrayListUrl) {
        OkHttpClient client = new OkHttpClient();

        String url = Url_Api.getInstance().addImagesPost(user_id, post_id, arrayListUrl);
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
                                    btn_post.setEnabled(true);
                                    Toast.makeText(PostActivity.this, "Đăng bài viết thành công", Toast.LENGTH_SHORT).show();
                                    finish();
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(PostActivity.this, "Đăng bài viết thất bại", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private String enCodeQrCode(String qr_code) {
        return Base64.encodeToString(qr_code.getBytes(), Base64.DEFAULT);
    }

    private String getTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentTime = dateFormat.format(new Date());
        return currentTime;
    }

}