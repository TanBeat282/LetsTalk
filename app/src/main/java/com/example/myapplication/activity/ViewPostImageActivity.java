package com.example.myapplication.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.adapter.CommentAdapter;
import com.example.myapplication.adapter.MediaPostALlAdapter;
import com.example.myapplication.adapter.MediaPostAdapter;
import com.example.myapplication.adapter.PostAdapter;
import com.example.myapplication.helper.FirebaseUploaderMessages;
import com.example.myapplication.helper.Helper;
import com.example.myapplication.model.CommentModel;
import com.example.myapplication.model.ImagePostModel;
import com.example.myapplication.model.MediaItem;
import com.example.myapplication.model.MessagesListModel;
import com.example.myapplication.model.PostModel;
import com.example.myapplication.server.Url_Api;
import com.example.myapplication.sharedpreferencesmanager.SharedPreferencesManager;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.makeramen.roundedimageview.RoundedImageView;
import com.vanniktech.emoji.EmojiPopup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ViewPostImageActivity extends AppCompatActivity {
    private int user_id, post_id, userId;
    private boolean is_finish = false;
    private RecyclerView rv_image;
    private RoundedImageView imgAvatar;
    private SwipeRefreshLayout swipe_refesh;
    private ImageView imgBack, btn_heart, btn_commnet, btn_share, btn_send;
    private TextView txtName, txtTime, txtContent, txt_count_heart, txt_count_cmt, txtTitle, txt_count_share;

    private int commentCount = 0, heartCountValue = 0;
    private final int YOUR_REQUEST_CODE = 12345;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_post_image);

        swipe_refesh = findViewById(R.id.swipe_refesh_deltail_post);
        imgBack = findViewById(R.id.imgBack);
        imgAvatar = findViewById(R.id.imgAvatar);
        txtName = findViewById(R.id.txtName);
        txtTime = findViewById(R.id.txtTime);
        txtContent = findViewById(R.id.txtContent);
        txtTitle = findViewById(R.id.txtTitle);
        rv_image = findViewById(R.id.rv_image);

        txt_count_heart = findViewById(R.id.txt_count_heart);
        txt_count_cmt = findViewById(R.id.txt_count_cmt);
        txt_count_share = findViewById(R.id.txt_count_share);
        btn_heart = findViewById(R.id.btn_heart);
        btn_commnet = findViewById(R.id.btn_commnet);

        //change color changeStatusBarColor and changeNavigationColor
        Helper.changeStatusBarColor(this, R.color.white);
        Helper.changeNavigationColor(this, R.color.white, true);

        SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(this);
        user_id = sharedPreferencesManager.getUserId();

        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            return;
        }

        post_id = (int) bundle.get("post_id");
        is_finish = (boolean) bundle.getBoolean("is_finish");
        if (post_id != 0) {
            getPostDetail(post_id, user_id);
        }

        btn_heart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addHeart(post_id, user_id, new ViewPostActivity.AddHeartCallback() {
                    @Override
                    public void onAddHeartComplete(int isHeart, int heart_count) {
                        if (isHeart == 1) {
                            btn_heart.setImageResource(R.drawable.outline_favorite_24);
                            if (heart_count == 0) {
                                txt_count_heart.setVisibility(View.GONE);
                            } else {
                                txt_count_heart.setVisibility(View.VISIBLE);
                                txt_count_heart.setText(String.valueOf(heart_count));
                            }
                        } else {
                            btn_heart.setImageResource(R.drawable.outline_favorite_border_24);
                            if (heart_count == 0) {
                                txt_count_heart.setVisibility(View.GONE);
                            } else {
                                txt_count_heart.setVisibility(View.VISIBLE);
                                txt_count_heart.setText(String.valueOf(heart_count));
                            }
                        }
                    }
                });
            }
        });
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent resultIntent = new Intent();
                Bundle bundle1 = new Bundle();
                bundle1.putSerializable("post_id", post_id);
                bundle1.putInt("heart_count", heartCountValue);
                bundle1.putInt("comnent_count", commentCount);
                resultIntent.putExtras(bundle1);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
                overridePendingTransition(R.anim.anim_in_right, R.anim.anim_out_right);
            }
        });
        btn_commnet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (is_finish) {
                    Intent intent = new Intent(ViewPostImageActivity.this, ViewPostActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("post_id", post_id);
                    intent.putExtras(bundle);
                    startActivityForResult(intent, YOUR_REQUEST_CODE);
                    finish();
                    overridePendingTransition(R.anim.anim_in_left, R.anim.anim_out_left);
                } else {
                    finish();
                    overridePendingTransition(R.anim.anim_in_left, R.anim.anim_out_left);
                }
            }
        });

        swipe_refesh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
            getPostDetail(post_id, user_id);
        }, 1000));

        imgAvatar.setOnClickListener(view -> {
            Intent intent = new Intent(ViewPostImageActivity.this, ViewProfileActivity.class);
            intent.putExtra("userId",userId);
            startActivity(intent);
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
                                userId = dataObject.getInt("user_id");
                                String content = dataObject.getString("content");
                                String time = dataObject.getString("time");

                                // Lấy giá trị từ đối tượng heart_count
                                JSONObject heartCountObject = dataObject.getJSONObject("heart_count");
                                int heartCountValue = heartCountObject.getInt("heart_count");
                                int isHeart = heartCountObject.getInt("is_heart");

                                commentCount = dataObject.getInt("comment_count");

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
                                    MediaPostALlAdapter mediaPostALlAdapter = new MediaPostALlAdapter(ViewPostImageActivity.this, ViewPostImageActivity.this, imagePostModelList);

                                    // Lời gọi cập nhật giao diện đặt trong runOnUiThread
                                    runOnUiThread(() -> {
                                        LinearLayoutManager layoutManager = new LinearLayoutManager(ViewPostImageActivity.this);
                                        rv_image.setLayoutManager(layoutManager);
                                        rv_image.setAdapter(mediaPostALlAdapter);
                                    });
                                }

                                String fullName = dataObject.getString("full_name");
                                String profileImage = dataObject.getString("profile_image");

                                // Có thể sử dụng các giá trị ở đây
                                runOnUiThread(() -> {
                                    Glide.with(ViewPostImageActivity.this).load(profileImage).into(imgAvatar);
                                    txtName.setText(fullName);
                                    txtTime.setText(calculateTime(time));

                                    if (heartCountValue == 0) {
                                        txt_count_heart.setVisibility(View.GONE);
                                    } else {
                                        txt_count_heart.setVisibility(View.VISIBLE);
                                        txt_count_heart.setText(String.valueOf(heartCountValue));
                                    }

                                    if (isHeart == 1) {
                                        btn_heart.setImageResource(R.drawable.outline_favorite_24);
                                    } else {
                                        btn_heart.setImageResource(R.drawable.outline_favorite_border_24);
                                    }


                                    if (commentCount == 0) {
                                        txt_count_cmt.setVisibility(View.GONE);
                                    } else {
                                        txt_count_cmt.setVisibility(View.VISIBLE);
                                        txt_count_cmt.setText(String.valueOf(commentCount));
                                    }
                                    txt_count_share.setVisibility(View.GONE);


                                    txtContent.setText(content);
                                    txtTitle.setText("Bài viết của " + fullName);
                                    swipe_refesh.setRefreshing(false);
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

    private void addHeart(int post_id, int user_id, ViewPostActivity.AddHeartCallback callback) {
        OkHttpClient client = new OkHttpClient();

        // Tạo request để gửi lời gọi HTTP GET đến URL
        String url = Url_Api.getInstance().addHeart(post_id, user_id, getTime());
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
                        String jsonString = responseBody.string();
                        try {
                            JSONObject jsonObject = new JSONObject(jsonString);
                            boolean status = jsonObject.getBoolean("status");
                            if (status) {
                                int is_heart = jsonObject.getInt("is_heart");
                                int heart_count = jsonObject.getInt("heart_count");

                                runOnUiThread(() -> {
                                    // Gọi callback để trả về giá trị is_heart
                                    callback.onAddHeartComplete(is_heart, heart_count);
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

    private String getTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return dateFormat.format(new Date());
    }

    private static String calculateTime(String time) {
        // Thời điểm hiện tại
        LocalDateTime now = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            now = LocalDateTime.now();
        }

        DateTimeFormatter formatter = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        }
        LocalDateTime specifiedTime = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            specifiedTime = LocalDateTime.parse(time, formatter);
        }

        Duration duration = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            duration = Duration.between(specifiedTime, now);
        }
        long seconds = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            seconds = Math.abs(duration.getSeconds());
        }
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        // Xây dựng chuỗi kết quả
        StringBuilder result = new StringBuilder();

        if (days > 0) {
            result.append(days).append(" ngày trước");
        } else if (hours > 0) {
            result.append(hours).append(" giờ trước");
        } else if (minutes > 0) {
            result.append(minutes).append(" phút trước");
        } else {
            result.append("0 phút trước");
        }

        return result.toString();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent resultIntent = new Intent();
        Bundle bundle1 = new Bundle();
        bundle1.putSerializable("heart_count", heartCountValue);
        bundle1.putSerializable("post_id", post_id);
        resultIntent.putExtras(bundle1);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
        overridePendingTransition(R.anim.anim_in_right, R.anim.anim_out_right);
    }

}