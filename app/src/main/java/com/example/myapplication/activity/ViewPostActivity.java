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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.adapter.CommentAdapter;
import com.example.myapplication.adapter.MediaPostAdapter;
import com.example.myapplication.adapter.PostAdapter;
import com.example.myapplication.bottomsheet.BottomSheetOptionPost;
import com.example.myapplication.bottomsheet.BottomSheetSelectedCmt;
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

public class ViewPostActivity extends AppCompatActivity {
    private int user_id, post_id, position, userId;
    private boolean is_finish = false;
    private RecyclerView rv_commnent, rv_image;
    private RoundedImageView imgAvatar;
    private SwipeRefreshLayout swipe_refesh;
    private NestedScrollView nestedScrollView;
    private EditText edt_input_message;
    private ImageView imgBack, btn_heart, btn_commnet, btn_share, btn_send, btn_sticker;
    private TextView txtName, txtTime, txtContent, txt_count_heart, txt_count_cmt, txtTitle, txt_count_share, txt_all_cmt;
    private ArrayList<CommentModel> commentModelArrayList = new ArrayList<>();
    private CommentAdapter commentAdapter;
    private ImageView btn_more_post;
    private PostModel postModel;
    private int commentCount = 0, heartCountValue = 0;

    private final BroadcastReceiver broadcastReceiverRefeshComment = new BroadcastReceiver() {
        @SuppressLint("NotifyDataSetChanged")
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle == null) {
                return;
            }
            boolean isRefesh = bundle.getBoolean("refesh");
            int position = bundle.getInt("position", -1);
            if (isRefesh && position != -1) {
                commentAdapter.removeItem(position);
                commentCount = commentCount - 1;
                txt_count_cmt.setText(String.valueOf(commentCount));
                Toast.makeText(context, "Xóa bình luận thành công.", Toast.LENGTH_SHORT).show();
            }
        }
    };
    private final BroadcastReceiver broadcastReceiverSelectedComment = new BroadcastReceiver() {
        @SuppressLint("NotifyDataSetChanged")
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle == null) {
                return;
            }
            boolean is_latest = bundle.getBoolean("is_latest");
            if (is_latest) {
                txt_all_cmt.setText("Mới nhất");
                commentModelArrayList.clear();
                getCmtPost(post_id, 1);
            } else {
                txt_all_cmt.setText("Cũ nhất");
                commentModelArrayList.clear();
                getCmtPost(post_id, 0);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_post);

        swipe_refesh = findViewById(R.id.swipe_refesh_deltail_post);
        nestedScrollView = findViewById(R.id.nestedScrollView);
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
        btn_share = findViewById(R.id.btn_share);
        rv_commnent = findViewById(R.id.rv_commnent);

        edt_input_message = findViewById(R.id.edt_input_message);
        btn_send = findViewById(R.id.btn_send);
        btn_sticker = findViewById(R.id.btn_sticker);
        btn_more_post = findViewById(R.id.btn_more_post);
        txt_all_cmt = findViewById(R.id.txt_all_cmt);

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
        position = (int) bundle.get("position");
        is_finish = (boolean) bundle.getBoolean("is_finish");
        if (post_id != 0) {
            getPostDetail(post_id, user_id);
        }


        LinearLayoutManager layoutManager = new LinearLayoutManager(ViewPostActivity.this);
        rv_commnent.setLayoutManager(layoutManager);

        //Initialize emoji popup
        EmojiPopup popup = EmojiPopup.Builder.fromRootView(
                findViewById(R.id.root_view)
        ).build(edt_input_message);

        btn_sticker.setOnClickListener(view -> popup.toggle());

        btn_send.setOnClickListener(view -> {
            String content = edt_input_message.getText().toString().trim();
            // Thay thế ký tự xuống hàng bằng ký tự đặc biệt
            content = content.replaceAll("\n", "[NEWLINE]");
            if (content != null || !content.isEmpty()) {
                sendCmt(post_id, user_id, content);
            }
        });

        btn_heart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addHeart(post_id, user_id, new AddHeartCallback() {
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
                finish();
                overridePendingTransition(R.anim.anim_in_right, R.anim.anim_out_right);
            }
        });
        swipe_refesh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
            getPostDetail(post_id, user_id);
            commentModelArrayList.clear();
            getCmtPost(post_id, 1);
        }, 1000));
        imgAvatar.setOnClickListener(view -> {
            Intent intent = new Intent(ViewPostActivity.this, ViewProfileActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });
        txt_all_cmt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BottomSheetSelectedCmt bottomSheetSelectedCmt = new BottomSheetSelectedCmt(ViewPostActivity.this, ViewPostActivity.this);
                bottomSheetSelectedCmt.show(getSupportFragmentManager(), bottomSheetSelectedCmt.getTag());
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
                                    MediaPostAdapter mediaPostAdapter = new MediaPostAdapter(ViewPostActivity.this, ViewPostActivity.this, imagePostModelList, is_finish);
                                    if (imagePostModelList.size() >= 7) {
                                        ImagePostModel newItem = new ImagePostModel(0, post_id, "https://firebasestorage.googleapis.com/v0/b/letstalk-3d1c5.appspot.com/o/file_default%2Fba_cham.jpg?alt=media&token=c1da26ab-25eb-4fc8-bc5e-532d7d4497b1");
                                        imagePostModelList.add(5, newItem);
                                    }
                                    if (imagePostModelList.size() >= 6) {
                                        imagePostModelList.subList(6, imagePostModelList.size()).clear();
                                    }
                                    runOnUiThread(() -> {
                                        // Đặt LayoutManager dựa trên kích thước của imagePostModels
                                        if (imagePostModelList.size() == 2) {
                                            rv_image.setLayoutManager(new GridLayoutManager(ViewPostActivity.this, 2));
                                        } else if (imagePostModelList.size() == 3) {
                                            rv_image.setLayoutManager(new GridLayoutManager(ViewPostActivity.this, 3));
                                        } else if (imagePostModelList.size() == 4) {
                                            rv_image.setLayoutManager(new GridLayoutManager(ViewPostActivity.this, 2));
                                        } else if (imagePostModelList.size() >= 5) {
                                            rv_image.setLayoutManager(new GridLayoutManager(ViewPostActivity.this, 3));
                                        } else {
                                            rv_image.setLayoutManager(new GridLayoutManager(ViewPostActivity.this, 1));
                                        }
                                        rv_image.setAdapter(mediaPostAdapter);
                                    });
                                }

                                String fullName = dataObject.getString("full_name");
                                String profileImage = dataObject.getString("profile_image");

                                // Có thể sử dụng các giá trị ở đây
                                runOnUiThread(() -> {
                                    Glide.with(ViewPostActivity.this).load(profileImage).into(imgAvatar);
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

    private void addHeart(int post_id, int user_id, AddHeartCallback callback) {
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

    private void getCmtPost(int post_id, int is_latest) {
        OkHttpClient client = new OkHttpClient();

        // Tạo request để gửi lời gọi HTTP GET đến URL
        String url = Url_Api.getInstance().getCmt(post_id, is_latest);
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
                                commentModelArrayList.clear();
                                JSONArray dataArray = jsonObject.getJSONArray("data");
                                for (int i = 0; i < dataArray.length(); i++) {
                                    JSONObject messageObject = dataArray.getJSONObject(i);

                                    CommentModel commentModel = new CommentModel();

                                    commentModel.setComment_id(messageObject.getInt("comment_id"));
                                    commentModel.setUser_id(messageObject.getInt("user_id"));
                                    commentModel.setPost_id(messageObject.getInt("post_id"));
                                    commentModel.setContent(messageObject.getString("content"));
                                    commentModel.setTime(calculateTime(messageObject.getString("time")));
                                    commentModel.setFull_name(messageObject.getString("full_name"));
                                    commentModel.setProfile_image(messageObject.getString("profile_image"));
                                    commentModelArrayList.add(commentModel);
                                }
                                runOnUiThread(() -> {
                                    commentAdapter = new CommentAdapter(ViewPostActivity.this, commentModelArrayList, user_id, ViewPostActivity.this);
                                    rv_commnent.setAdapter(commentAdapter);
                                    commentAdapter.notifyDataSetChanged();
                                    swipe_refesh.setRefreshing(false);
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

    private void sendCmt(int post_id, int user_id, String content) {
        OkHttpClient client = new OkHttpClient();

        // Tạo request để gửi lời gọi HTTP GET đến URL
        String url = Url_Api.getInstance().addCmt(post_id, user_id, content, getTime());
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

                                runOnUiThread(() -> {
                                    edt_input_message.setText("");
                                    commentModelArrayList.clear();
                                    getCmtPost(post_id, 1);
                                    commentCount = commentCount + 1;
                                    txt_count_cmt.setText(String.valueOf(commentCount));
                                    rv_commnent.scrollToPosition(commentModelArrayList.size() - 1);
                                    nestedScrollView.fullScroll(View.FOCUS_DOWN);
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

    public interface AddHeartCallback {
        void onAddHeartComplete(int isHeart, int heart_count);
    }


    @Override
    protected void onResume() {
        super.onResume();
        commentModelArrayList.clear();
        getCmtPost(post_id, 1);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiverRefeshComment, new IntentFilter("refesh_comment"));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiverSelectedComment, new IntentFilter("selected_comment"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiverRefeshComment);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiverSelectedComment);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.anim_in_right, R.anim.anim_out_right);
    }
}