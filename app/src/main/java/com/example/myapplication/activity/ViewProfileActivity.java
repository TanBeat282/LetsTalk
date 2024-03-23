package com.example.myapplication.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.adapter.PostAdapter;
import com.example.myapplication.bottomsheet.BottomSheetOptionPost;
import com.example.myapplication.bottomsheet.BottomSheetOptionProfile;
import com.example.myapplication.helper.Helper;
import com.example.myapplication.model.ImagePostModel;
import com.example.myapplication.model.MessagesListModel;
import com.example.myapplication.model.PostModel;
import com.example.myapplication.server.Url_Api;
import com.example.myapplication.sharedpreferencesmanager.SharedPreferencesManager;
import com.makeramen.roundedimageview.RoundedImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
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

public class ViewProfileActivity extends AppCompatActivity {
    private ImageView btn_back;
    private ImageView img_cover;
    private RoundedImageView img_avatar;
    private TextView txt_name, txt_note;
    private LinearLayout btn_more_edit, btn_edit, btn_send_friend_ship, linear_messages, btn_accept_friend_ship, btn_un_friend_ship, btn_messgaes, btn_remove_friend_ship, linear_edit_profile;
    private int user_id, userId;

    private ArrayList<PostModel> postModelArrayList = new ArrayList<>();
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private SwipeRefreshLayout swipe_refesh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        btn_back = findViewById(R.id.btn_back);
        img_cover = findViewById(R.id.img_cover);
        img_avatar = findViewById(R.id.img_avatar);
        txt_name = findViewById(R.id.txt_name);
        txt_note = findViewById(R.id.txt_note);

        btn_send_friend_ship = findViewById(R.id.btn_send_friend_ship);
        btn_un_friend_ship = findViewById(R.id.btn_un_friend_ship);
        btn_accept_friend_ship = findViewById(R.id.btn_accept_friend_ship);
        linear_messages = findViewById(R.id.linear_messages);
        linear_edit_profile = findViewById(R.id.linear_edit_profile);
        btn_messgaes = findViewById(R.id.btn_messgaes);
        btn_edit = findViewById(R.id.btn_edit);
        btn_more_edit = findViewById(R.id.btn_more_edit);
        btn_remove_friend_ship = findViewById(R.id.btn_remove_friend_ship);
        swipe_refesh = findViewById(R.id.swipe_refesh);

        //change color changeStatusBarColor and changeNavigationColor
        Helper.changeStatusBarColor(this, R.color.white);
        Helper.changeNavigationColor(this, R.color.white, true);

        SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(this);
        user_id = sharedPreferencesManager.getUserId();

        userId = getIntent().getIntExtra("userId", 0);

        recyclerView = findViewById(R.id.rv_post);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        swipe_refesh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (userId != 0) {
                getProfile(user_id, userId);
            } else {
                postModelArrayList.clear();
                getProfileUser(user_id);
                getPost(user_id);
            }
        }, 1000));



        btn_send_friend_ship.setOnClickListener(view -> sendFriendShip(user_id, userId));

        btn_un_friend_ship.setOnClickListener(view -> sendUnFriendShip(user_id, userId));

        btn_accept_friend_ship.setOnClickListener(view -> acceptFriendShip(user_id, userId));

        btn_messgaes.setOnClickListener(view -> {
            checkMessagesList(user_id, user_id, userId);
        });

        btn_remove_friend_ship.setOnClickListener(view -> removeFriendShip(user_id, userId));

        btn_edit.setOnClickListener(view -> startActivity(new Intent(ViewProfileActivity.this, EditMyProfileActivity.class)));

        btn_back.setOnClickListener(view -> finish());
        btn_more_edit.setOnClickListener(view -> {
            BottomSheetOptionProfile bottomSheetOptionProfile = new BottomSheetOptionProfile(ViewProfileActivity.this, ViewProfileActivity.this);
            bottomSheetOptionProfile.show(getSupportFragmentManager(), bottomSheetOptionProfile.getTag());
        });

    }

    private void checkMessagesList(int user_id, int sender_id, int receiver_id) {

        OkHttpClient client = new OkHttpClient();

        String url = Url_Api.getInstance().checkMessagesList(user_id, sender_id, receiver_id);
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
                                JSONObject messageObject = jsonObject.getJSONObject("data");


                                MessagesListModel message = new MessagesListModel();

                                message.setMessage_list_id(messageObject.getInt("messages_list_id"));
                                message.setSender_id(messageObject.getInt("sender_id"));
                                message.setReceiver_id(messageObject.getInt("receiver_id"));
                                message.setLast_content(messageObject.getString("last_content"));
                                message.setType_message(messageObject.getInt("type_message"));
                                message.setTime(messageObject.getString("time"));
                                message.setIs_seen(messageObject.getInt("is_seen") == 1);
                                message.setReceiver_avatar(messageObject.getString("receiver_avatar"));
                                message.setReceiver_name(messageObject.getString("receiver_name"));
                                message.setReceiver_is_online(messageObject.getInt("receiver_is_online") == 1);


                                runOnUiThread(() -> {
                                    Intent intent = new Intent(ViewProfileActivity.this, MessagesActivity.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putSerializable("messages_list", message);
                                    intent.putExtras(bundle);

                                    startActivity(intent);
                                    overridePendingTransition(R.anim.anim_in_left, R.anim.anim_out_left);
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

    private void sendFriendShip(int sender_id, int reciever_id) {

        OkHttpClient client = new OkHttpClient();

        String url = Url_Api.getInstance().addFriendShip(sender_id, reciever_id, getTime());
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
                                    btn_send_friend_ship.setVisibility(View.GONE);
                                    btn_un_friend_ship.setVisibility(View.VISIBLE);
                                    btn_accept_friend_ship.setVisibility(View.GONE);
                                    linear_messages.setVisibility(View.GONE);
                                    linear_edit_profile.setVisibility(View.GONE);
                                    txt_note.setText("");
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

    private void sendUnFriendShip(int sender_id, int reciever_id) {

        OkHttpClient client = new OkHttpClient();

        String url = Url_Api.getInstance().removeFriendShip(sender_id, reciever_id);
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
                                    btn_send_friend_ship.setVisibility(View.VISIBLE);
                                    btn_un_friend_ship.setVisibility(View.GONE);
                                    btn_accept_friend_ship.setVisibility(View.GONE);
                                    linear_messages.setVisibility(View.GONE);
                                    linear_edit_profile.setVisibility(View.GONE);
                                    txt_note.setText("");
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

    private void acceptFriendShip(int sender_id, int reciever_id) {

        OkHttpClient client = new OkHttpClient();

        String url = Url_Api.getInstance().acceptFriendShip(user_id, sender_id, reciever_id, getTime());
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
                                // Extract individual variables from the "user" object
                                JSONObject userObject = jsonObject.getJSONObject("user");
                                int userId = userObject.getInt("user_id");
                                String description = userObject.getString("description");

                                runOnUiThread(() -> {
                                    // Now you can use these variables as needed
                                    btn_send_friend_ship.setVisibility(View.GONE);
                                    btn_un_friend_ship.setVisibility(View.GONE);
                                    btn_accept_friend_ship.setVisibility(View.GONE);
                                    linear_messages.setVisibility(View.VISIBLE);
                                    linear_edit_profile.setVisibility(View.GONE);
                                    txt_note.setText(description);
                                    getPost(userId);
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

    private void removeFriendShip(int sender_id, int reciever_id) {

        OkHttpClient client = new OkHttpClient();

        String url = Url_Api.getInstance().removeFriendShip(sender_id, reciever_id);
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

                                    btn_send_friend_ship.setVisibility(View.VISIBLE);
                                    btn_un_friend_ship.setVisibility(View.GONE);
                                    btn_accept_friend_ship.setVisibility(View.GONE);
                                    linear_messages.setVisibility(View.GONE);
                                    linear_edit_profile.setVisibility(View.GONE);
                                    txt_note.setText("");
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

    private void getProfile(int sender_id, int reciever_id) {

        OkHttpClient client = new OkHttpClient();

        String url = Url_Api.getInstance().checkIsFriend(sender_id, reciever_id);
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
                                int sender_id = dataObject.getInt("sender_id");
                                int receiver_id = dataObject.getInt("receiver_id");
                                int is_friend = dataObject.getInt("is_friend");

                                runOnUiThread(new Runnable() {
                                    @SuppressLint("SetTextI18n")
                                    @Override
                                    public void run() {
                                        txt_name.setText(full_name);

                                        if (description != null) {
                                            txt_note.setText(description);
                                        } else {
                                            txt_note.setVisibility(View.GONE);
                                        }

                                        if (cover_avatar != null) {
                                            Glide.with(ViewProfileActivity.this).load(cover_avatar).into(img_cover);
                                        } else {
                                            int color = ContextCompat.getColor(ViewProfileActivity.this, R.color.white); // Thay thế R.color.your_color_resource bằng màu sắc mong muốn
                                            Drawable drawable = new ColorDrawable(color);
                                            img_cover.setImageDrawable(drawable);
                                        }

                                        if (profile_image != null) {
                                            Glide.with(ViewProfileActivity.this).load(profile_image).into(img_avatar);
                                        } else {
                                            int color = ContextCompat.getColor(ViewProfileActivity.this, R.color.white); // Thay thế R.color.your_color_resource bằng màu sắc mong muốn
                                            Drawable drawable = new ColorDrawable(color);
                                            img_cover.setImageDrawable(drawable);
                                        }


                                        if (is_friend == 0) {

                                            btn_send_friend_ship.setVisibility(View.GONE);
                                            if (sender_id == user_id) {
                                                btn_un_friend_ship.setVisibility(View.VISIBLE);
                                                btn_accept_friend_ship.setVisibility(View.GONE);
                                            } else {
                                                btn_un_friend_ship.setVisibility(View.GONE);
                                                btn_accept_friend_ship.setVisibility(View.VISIBLE);
                                            }
                                            linear_messages.setVisibility(View.GONE);
                                            txt_note.setText("Bạn không thể xem thông tin " + full_name + " khi chưa là bạn bè");

                                        } else if (is_friend == 1) {
                                            btn_send_friend_ship.setVisibility(View.GONE);
                                            btn_un_friend_ship.setVisibility(View.GONE);
                                            btn_accept_friend_ship.setVisibility(View.GONE);
                                            linear_messages.setVisibility(View.VISIBLE);
                                            if (description != null) {
                                                txt_note.setText(description);
                                            } else {
                                                txt_note.setVisibility(View.GONE);
                                            }
                                            getPost(userId);
                                        } else {
                                            btn_send_friend_ship.setVisibility(View.VISIBLE);
                                            btn_un_friend_ship.setVisibility(View.GONE);
                                            btn_accept_friend_ship.setVisibility(View.GONE);
                                            linear_messages.setVisibility(View.GONE);
                                            txt_note.setText("Bạn không thể xem thông tin " + full_name + " khi chưa là bạn bè");
                                        }
                                        swipe_refesh.setRefreshing(false);
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

                                runOnUiThread(() -> {
                                    txt_name.setText(full_name);

                                    if (description != null) {
                                        txt_note.setText(description);
                                    } else {
                                        txt_note.setVisibility(View.GONE);
                                    }

                                    if (cover_avatar != null) {
                                        Glide.with(ViewProfileActivity.this).load(cover_avatar).into(img_cover);
                                    } else {
                                        int color = ContextCompat.getColor(ViewProfileActivity.this, R.color.white); // Thay thế R.color.your_color_resource bằng màu sắc mong muốn
                                        Drawable drawable = new ColorDrawable(color);
                                        img_cover.setImageDrawable(drawable);
                                    }

                                    if (profile_image != null) {
                                        Glide.with(ViewProfileActivity.this).load(profile_image).into(img_avatar);
                                    } else {
                                        int color = ContextCompat.getColor(ViewProfileActivity.this, R.color.white); // Thay thế R.color.your_color_resource bằng màu sắc mong muốn
                                        Drawable drawable = new ColorDrawable(color);
                                        img_cover.setImageDrawable(drawable);
                                    }

                                    btn_send_friend_ship.setVisibility(View.GONE);
                                    btn_un_friend_ship.setVisibility(View.GONE);
                                    btn_accept_friend_ship.setVisibility(View.GONE);
                                    linear_messages.setVisibility(View.GONE);
                                    linear_edit_profile.setVisibility(View.VISIBLE);
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
                                    postModel.setIs_save(messageObject.getInt("is_save_post") == 1);

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
                                    postModelArrayList.add(postModel);
                                }
                                runOnUiThread(() -> {
                                    postAdapter = new PostAdapter(ViewProfileActivity.this, postModelArrayList, user_id, ViewProfileActivity.this);
                                    recyclerView.setAdapter(postAdapter);
                                    postAdapter.notifyDataSetChanged();
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

    private String getTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentTime = dateFormat.format(new Date());
        return currentTime;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (userId != 0) {
            getProfile(user_id, userId);
        } else {
            getProfileUser(user_id);
            getPost(user_id);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}