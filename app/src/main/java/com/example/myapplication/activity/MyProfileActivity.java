package com.example.myapplication.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.adapter.PostAdapter;
import com.example.myapplication.model.ImagePostModel;
import com.example.myapplication.model.PostModel;
import com.example.myapplication.server.Url_Api;
import com.example.myapplication.sharedpreferencesmanager.SharedPreferencesManager;
import com.makeramen.roundedimageview.RoundedImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MyProfileActivity extends AppCompatActivity {
    private ImageView btn_back;
    private ImageView img_cover;
    private RoundedImageView img_avatar;
    private TextView txt_name, txt_note,txtEdit;
    private int user_id, userId;

    private ArrayList<PostModel> postModelArrayList = new ArrayList<>();
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        btn_back = findViewById(R.id.btn_back);
        img_cover = findViewById(R.id.img_cover);
        img_avatar = findViewById(R.id.img_avatar);
        txt_name = findViewById(R.id.txt_name);
        txt_note = findViewById(R.id.txt_note);
        txtEdit = findViewById(R.id.txtEdit);
        recyclerView = findViewById(R.id.rv_post);

        SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(this);
        user_id = sharedPreferencesManager.getUserId();

        getProfileUser(user_id);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        btn_back.setOnClickListener(view -> finish());
        txtEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MyProfileActivity.this,EditMyProfileActivity.class));
            }
        });

    }
    private void getProfileUser(int user_id) {

        OkHttpClient client = new OkHttpClient();

        String url = Url_Api.getInstance().getProfile(user_id);
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
                                JSONObject dataObject = jsonObject.getJSONObject("data");

                                String full_name = dataObject.getString("full_name");
                                String profile_image = dataObject.getString("profile_image");
                                String cover_avatar = dataObject.getString("cover_avatar");
                                String description = dataObject.getString("description");
                                String location = dataObject.getString("location");
//                                String sex = dataObject.getString("sex");
                                String combinedText ="Giới thiệu: "+ description + "\nĐịa chỉ: " + location;
                                runOnUiThread(new Runnable() {
                                    @SuppressLint("SetTextI18n")
                                    @Override
                                    public void run() {
                                        txt_name.setText(full_name);
                                        txt_note.setText(combinedText);
                                        Glide.with(MyProfileActivity.this).load(cover_avatar).into(img_cover);
                                        Glide.with(MyProfileActivity.this).load(profile_image).into(img_avatar);
                                    }
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
    private void getPost(int user_id) {
        OkHttpClient client = new OkHttpClient();

        // Tạo request để gửi lời gọi HTTP GET đến URL
        String url = Url_Api.getInstance().getMyPost(user_id);
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
                                JSONArray dataArray = jsonObject.getJSONArray("data");
                                for (int i = 0; i < dataArray.length(); i++) {
                                    JSONObject messageObject = dataArray.getJSONObject(i);

                                    PostModel postModel = new PostModel();

                                    postModel.setPost_id(messageObject.getInt("post_id"));
                                    postModel.setUser_id(messageObject.getInt("user_id"));
                                    postModel.setContent(messageObject.getString("content"));
                                    postModel.setTime(messageObject.getString("time"));
                                    postModel.setHeart_count(messageObject.getString("heart_count"));
                                    postModel.setComment_count(messageObject.getInt("comment_count"));

                                    // Thay đổi cách lấy dữ liệu từ JSON cho imagePostModelList
                                    JSONArray imageArray = messageObject.getJSONArray("image");
                                    List<ImagePostModel> imagePostModelList = new ArrayList<>();
                                    for (int j = 0; j < imageArray.length(); j++) {
                                        JSONObject imageObject = imageArray.getJSONObject(j);
                                        ImagePostModel imagePostModel = new ImagePostModel();
                                        imagePostModel.setImage_id(imageObject.getInt("image_id"));
                                        imagePostModel.setPost_id(imageObject.getInt("post_id"));
                                        imagePostModel.setImage(imageObject.getString("image"));
                                        imagePostModelList.add(imagePostModel);
                                    }
                                    postModel.setImagePostModelList(imagePostModelList);

                                    postModel.setFull_name(messageObject.getString("full_name"));
                                    postModel.setProfile_image(messageObject.getString("profile_image"));
                                    Log.d(">>>>>>>>>>>", "onResponse: " + messageObject.getString("content"));
                                    postModelArrayList.add(postModel);
                                }
                                runOnUiThread(() -> {
                                    postAdapter = new PostAdapter(MyProfileActivity.this, postModelArrayList, user_id, MyProfileActivity.this);
                                    recyclerView.setAdapter(postAdapter);
                                    postAdapter.notifyDataSetChanged();
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

    @Override
    protected void onResume() {
        super.onResume();
        getPost(userId);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}