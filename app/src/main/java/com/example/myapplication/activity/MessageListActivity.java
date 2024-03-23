package com.example.myapplication.activity;

import static com.example.myapplication.helper.SortMessagesByTime.sortMessagesByTime;
import static com.example.myapplication.helper.SortMessagesByTime.sortMessagesListByTime;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.myapplication.R;
import com.example.myapplication.adapter.MessageListAdapter;
import com.example.myapplication.helper.DateTimeUtils;
import com.example.myapplication.helper.Helper;
import com.example.myapplication.model.MessagesListModel;
import com.example.myapplication.server.Url_Api;
import com.example.myapplication.sharedpreferencesmanager.SharedPreferencesManager;
import com.zegocloud.uikit.components.audiovideo.ZegoAvatarViewProvider;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoCallInvitationData;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallConfigProvider;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationService;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MessageListActivity extends AppCompatActivity {
    private MessageListAdapter messageListAdapter;
    private final ArrayList<MessagesListModel> messageListModels = new ArrayList<>();
    private RecyclerView rv_list_message;
    private int user_id;
    private ImageView imgBack;
    private SwipeRefreshLayout swipe_refesh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_list);

        Helper.changeStatusBarColor(this, R.color.white);
        Helper.changeNavigationColor(this, R.color.white, true);

        SharedPreferencesManager sharedPreferences = new SharedPreferencesManager(getApplicationContext());
        user_id = sharedPreferences.getUserId();

        rv_list_message = findViewById(R.id.rv_list_message);
        imgBack = findViewById(R.id.imgBack);
        swipe_refesh = findViewById(R.id.swipe_refesh_deltail_post);


        rv_list_message.setLayoutManager(new LinearLayoutManager(this));

        getInfo(user_id);
        imgBack.setOnClickListener(view -> finish());

        swipe_refesh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
            getMessageList(user_id);
        }, 1000));
    }

    private void getMessageList(int user_id) {
        OkHttpClient client = new OkHttpClient();

        // Tạo request để gửi lời gọi HTTP GET đến URL
        String url = Url_Api.getInstance().getMessagesList(user_id);
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
                                messageListModels.clear();
                                for (int i = 0; i < dataArray.length(); i++) {
                                    JSONObject messageObject = dataArray.getJSONObject(i);

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
                                    messageListModels.add(message);
                                }
                                runOnUiThread(() -> {
                                    sortMessagesListByTime(messageListModels);
                                    messageListAdapter = new MessageListAdapter(user_id, messageListModels, MessageListActivity.this, MessageListActivity.this);
                                    rv_list_message.setAdapter(messageListAdapter);
                                    messageListAdapter.notifyDataSetChanged();
                                    swipe_refesh.setRefreshing(false);
                                });

                            } else {
                                swipe_refesh.setRefreshing(false);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    private void getInfo(int user_id) {
        OkHttpClient client = new OkHttpClient();

        String url = Url_Api.getInstance().getInfo(user_id);
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

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        // Thực hiện các hoạt động liên quan đến giao diện người dùng trên luồng chính
                                        try {
                                            startServer(String.valueOf(user_id), dataObject.getString("full_name").toString(), dataObject.getString("profile_image"));
                                        } catch (JSONException e) {
                                            throw new RuntimeException(e);
                                        }
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

    private void startServer(String user_id, String full_name, String img_avatar) {
        long appID = 684104476;   // yourAppID
        String appSign = "714e6a72dbd897b9329827489af03f2443d0bc787058fbf38c7767f8c7eeaed7";  // yourAppSign
        String userID = user_id; // yourUserID, userID should only contain numbers, English characters, and '_'.
        String userName = full_name;   // yourUserName

        ZegoUIKitPrebuiltCallInvitationConfig callInvitationConfig = new ZegoUIKitPrebuiltCallInvitationConfig();
        callInvitationConfig.provider = new ZegoUIKitPrebuiltCallConfigProvider() {
            @Override
            public ZegoUIKitPrebuiltCallConfig requireConfig(ZegoCallInvitationData invitationData) {
                ZegoUIKitPrebuiltCallConfig config = ZegoUIKitPrebuiltCallConfig.oneOnOneVideoCall();
                config.avatarViewProvider = new ZegoAvatarViewProvider() {
                    @Override
                    public View onUserIDUpdated(ViewGroup parent, ZegoUIKitUser uiKitUser) {
                        ImageView imageView = new ImageView(parent.getContext());
                        String avatarUrl;
                        if (uiKitUser.userID.equals(user_id)) {
                            avatarUrl = img_avatar;
                        } else {
                            avatarUrl = null;
                        }
                        if (!TextUtils.isEmpty(avatarUrl)) {
                            RequestOptions requestOptions = new RequestOptions().circleCrop();
                            Glide.with(parent.getContext()).load(avatarUrl).apply(requestOptions).into(imageView);
                        }
                        return imageView;
                    }
                };
                return config;
            }
        };
        ZegoUIKitPrebuiltCallInvitationService.init(getApplication(), appID, appSign, userID, userName, callInvitationConfig);
    }

    @Override
    protected void onResume() {
        super.onResume();
        messageListModels.clear();
        getMessageList(user_id);
    }
}