package com.example.myapplication.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.adapter.FriendListAdapter;
import com.example.myapplication.adapter.FriendShipAdapter;
import com.example.myapplication.helper.Helper;
import com.example.myapplication.model.FriendShipModel;
import com.example.myapplication.server.Url_Api;
import com.example.myapplication.sharedpreferencesmanager.SharedPreferencesManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class FriendListActivity extends AppCompatActivity {
    private RecyclerView rv_friend_request;

    private int user_id;
    private LinearLayout txt_no_friend;
    private FriendListAdapter friendListAdapter;
    private ImageView btn_search;
    private TextView txtTitle;
    private SearchView search_view_friend_ship;
    private SwipeRefreshLayout swipe_refesh;
    private boolean is_search = false;
    private final ArrayList<FriendShipModel> friendShipModelArrayList = new ArrayList<>();


    private final BroadcastReceiver broadcastReceiverAdapter = new BroadcastReceiver() {
        @SuppressLint("NotifyDataSetChanged")
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle == null) {
                return;
            }
            boolean refesh = bundle.getBoolean("refesh");
            int position = bundle.getInt("position");
            if (refesh) {
                friendShipModelArrayList.remove(position);
                friendListAdapter.notifyDataSetChanged();
                if (friendShipModelArrayList.size() == 0) {
                    rv_friend_request.setVisibility(View.GONE);
                    txt_no_friend.setVisibility(View.VISIBLE);
                } else {
                    rv_friend_request.setVisibility(View.VISIBLE);
                    txt_no_friend.setVisibility(View.GONE);

                }

            }

        }
    };

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);


        //change color changeStatusBarColor and changeNavigationColor
        Helper.changeStatusBarColor(FriendListActivity.this, R.color.white);
        Helper.changeNavigationColor(FriendListActivity.this, R.color.white, true);

        //get user_id
        SharedPreferencesManager sharedPreferences = new SharedPreferencesManager(FriendListActivity.this);
        user_id = sharedPreferences.getUserId();

        rv_friend_request = findViewById(R.id.rv_friend_request);
        ImageView imgBack = findViewById(R.id.imgBack);
        txt_no_friend = findViewById(R.id.txt_no_friend);
        txtTitle = findViewById(R.id.txtTitle);
        btn_search = findViewById(R.id.btn_search);
        search_view_friend_ship = findViewById(R.id.search_view_friend_ship);
        swipe_refesh = findViewById(R.id.swipe_refesh);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rv_friend_request.setLayoutManager(layoutManager);

        friendListAdapter = new FriendListAdapter(user_id, friendShipModelArrayList, this, this);
        rv_friend_request.setAdapter(friendListAdapter);

        swipe_refesh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
            friendShipModelArrayList.clear();
            getFriend(user_id);
        }, 1000));


        imgBack.setOnClickListener(view -> finish());
        btn_search.setOnClickListener(view -> {
            if (is_search) {
                txtTitle.setVisibility(View.VISIBLE);
                btn_search.setVisibility(View.VISIBLE);
                search_view_friend_ship.setVisibility(View.GONE);
                is_search = false;
            } else {
                txtTitle.setVisibility(View.GONE);
                btn_search.setVisibility(View.GONE);
                search_view_friend_ship.setVisibility(View.VISIBLE);
                is_search = true;
            }
        });
    }

    private void getFriend(int user_id) {
        // 0 chua ban be 1 bạn be
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(Url_Api.getInstance().getFriend(user_id, 1))
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

                                ArrayList<FriendShipModel> friendShipModels = new ArrayList<>();
                                for (int i = 0; i < dataArray.length(); i++) {
                                    JSONObject messageObject = dataArray.getJSONObject(i);

                                    FriendShipModel friendShipModel = new FriendShipModel();

                                    friendShipModel.setFriend_ship_id(messageObject.getInt("friend_ship_id"));
                                    friendShipModel.setSender_id(messageObject.getInt("sender_id"));
                                    friendShipModel.setReceiver_id(messageObject.getInt("receiver_id"));
                                    friendShipModel.setTime(messageObject.getString("time"));
                                    friendShipModel.setIs_friend(messageObject.getInt("is_friend") == 0);
                                    friendShipModel.setFull_name(messageObject.getString("full_name"));
                                    friendShipModel.setProfie_image(messageObject.getString("profile_image"));

                                    friendShipModels.add(friendShipModel);
                                }

                                runOnUiThread(() -> {
                                    friendShipModelArrayList.clear();
                                    friendShipModelArrayList.addAll(friendShipModels);
//                                    txt_count_friend_request.setText(String.valueOf(friendShipModelArrayList.size()));
                                    if (friendShipModelArrayList.size() == 0) {
                                        rv_friend_request.setVisibility(View.GONE);
                                        txt_no_friend.setVisibility(View.VISIBLE);

                                    } else {
                                        rv_friend_request.setVisibility(View.VISIBLE);
                                        txt_no_friend.setVisibility(View.GONE);
                                        friendListAdapter.notifyDataSetChanged(); // Thay đổi dòng này
                                    }
                                    swipe_refesh.setRefreshing(false);
                                });
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        rv_friend_request.setVisibility(View.GONE);
                                        txt_no_friend.setVisibility(View.VISIBLE);
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

    @Override
    protected void onResume() {
        super.onResume();
        getFriend(user_id);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiverAdapter, new IntentFilter("refesh"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiverAdapter);
    }
}