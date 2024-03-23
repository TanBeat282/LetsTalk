package com.example.myapplication.activity;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
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

import com.bumptech.glide.Glide;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.adapter.MediaPostAdapter;
import com.example.myapplication.helper.Helper;
import com.example.myapplication.model.ImagePostModel;
import com.example.myapplication.model.PostModel;
import com.example.myapplication.server.Url_Api;
import com.example.myapplication.sharedpreferencesmanager.SharedPreferencesManager;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

public class EditPostActivity extends AppCompatActivity {

    private int user_id;
    private LinearLayout btn_post;
    private PostModel postModel;
    private ImageView btn_image;
    private EditText edt_input_message;
    private ArrayList<Uri> arrayList;
    private RecyclerView rv_image;


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
        rv_image = findViewById(R.id.rv_image);

        FirebaseApp.initializeApp(EditPostActivity.this);
        arrayList = new ArrayList<>();

        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            return;
        }

        postModel = (PostModel) bundle.get("post_model");
        if (postModel != null) {
            getPostDetail(postModel.getPost_id(), user_id);
        }

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

    private void getPostDetail(int post_id, int user_id) {
        OkHttpClient client = new OkHttpClient();

        // Tạo request để gửi lời gọi HTTP GET đến URL
        String url = Url_Api.getInstance().getPostDetail(post_id, user_id);
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    ResponseBody responseBody = response.body();
                    if (responseBody != null) {
                        try {
                            String jsonString = responseBody.string();
                            JSONObject jsonObject = new JSONObject(jsonString);

                            // Kiểm tra status trước khi tiếp tục
                            boolean status = jsonObject.getBoolean("status");
                            if (status) {
                                JSONObject dataObject = jsonObject.getJSONObject("data");
                                // Lấy các giá trị từ đối tượng data
                                int postId = dataObject.getInt("post_id");
                                int userId = dataObject.getInt("user_id");
                                String content = dataObject.getString("content");
                                String time = dataObject.getString("time");

                                // Lấy giá trị từ đối tượng heart_count
                                JSONObject heartCountObject = dataObject.getJSONObject("heart_count");
                                int heartCountValue = heartCountObject.getInt("heart_count");
                                int isHeart = heartCountObject.getInt("is_heart");

                                int commentCount = dataObject.getInt("comment_count");

                                // Lấy mảng image
                                // Thay đổi cách lấy dữ liệu từ JSON cho imagePostModelList
                                JSONArray imageArray = dataObject.getJSONArray("image");
                                List<ImagePostModel> imagePostModelList = new ArrayList<>();
                                for (int j = 0; j < imageArray.length(); j++) {
                                    JSONObject imageObject = imageArray.getJSONObject(j);
                                    ImagePostModel imagePostModel = new ImagePostModel();
                                    imagePostModel.setImage_id(imageObject.getInt("image_id"));
                                    imagePostModel.setPost_id(imageObject.getInt("post_id"));
                                    imagePostModel.setImage(imageObject.getString("image"));
                                    imagePostModelList.add(imagePostModel);
                                }
                                if (imagePostModelList.size() == 0) {
                                    rv_image.setVisibility(View.GONE);
                                } else {
                                    rv_image.setVisibility(View.VISIBLE);
                                    MediaPostAdapter mediaPostAdapter = new MediaPostAdapter(EditPostActivity.this, EditPostActivity.this, imagePostModelList, false);
                                    if (imagePostModelList.size() >= 7) {
                                        ImagePostModel newItem = new ImagePostModel(0, post_id, "https://firebasestorage.googleapis.com/v0/b/letstalk-3d1c5.appspot.com/o/file_default%2Fba_cham.jpg?alt=media&token=c1da26ab-25eb-4fc8-bc5e-532d7d4497b1");
                                        imagePostModelList.add(5, newItem);
                                    }
                                    if (imagePostModelList.size() >= 6) {
                                        imagePostModelList.subList(6, imagePostModelList.size()).clear();
                                    }
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (imagePostModelList.size() == 2) {
                                                rv_image.setLayoutManager(new GridLayoutManager(EditPostActivity.this, 2));
                                            } else if (imagePostModelList.size() == 3) {
                                                rv_image.setLayoutManager(new GridLayoutManager(EditPostActivity.this, 3));
                                            } else if (imagePostModelList.size() == 4) {
                                                rv_image.setLayoutManager(new GridLayoutManager(EditPostActivity.this, 2));
                                            } else if (imagePostModelList.size() >= 5) {
                                                rv_image.setLayoutManager(new GridLayoutManager(EditPostActivity.this, 3));
                                            } else {
                                                rv_image.setLayoutManager(new GridLayoutManager(EditPostActivity.this, 1));
                                            }
                                            rv_image.setAdapter(mediaPostAdapter);
                                        }
                                    });
                                }
                                // Có thể sử dụng các giá trị ở đây
                                runOnUiThread(() -> {
                                    edt_input_message.setText(content);
                                });
                            } else {
                                // In ra thông báo lỗi nếu status không phải là true
                                String errorMessage = "Status is not true. JSON: " + jsonString;
                                Log.e("PostDetailError", errorMessage);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
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
                                                Toast.makeText(EditPostActivity.this, "Đăng bài viết thành công", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(EditPostActivity.this, "Đăng bài viết thất bại", Toast.LENGTH_SHORT).show();
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
                                    Toast.makeText(EditPostActivity.this, "Đăng bài viết thành công", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(EditPostActivity.this, "Đăng bài viết thất bại", Toast.LENGTH_SHORT).show();
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