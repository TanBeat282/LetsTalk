package com.example.myapplication.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.example.myapplication.R;
import com.example.myapplication.adapter.FriendListAdapter;
import com.example.myapplication.adapter.SearchFriendAdapter;
import com.example.myapplication.adapter.SearchMessagesAdapter;
import com.example.myapplication.helper.Helper;
import com.example.myapplication.model.FriendShipModel;
import com.example.myapplication.model.SearchFriendModel;
import com.example.myapplication.model.SearchMessagesModel;
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

public class SearchActivity extends AppCompatActivity {
    private final ArrayList<SearchFriendModel> searchFriendModelArrayList = new ArrayList<>();
    private SearchFriendAdapter searchFriendAdapter;
    private int user_id;
    private RecyclerView rv_search_friend;
    private SearchView search_view_friend;
    private ImageView imgBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Helper.changeStatusBarColor(this, R.color.white);
        Helper.changeNavigationColor(this, R.color.white, true);

        //get user_id
        SharedPreferencesManager sharedPreferences = new SharedPreferencesManager(SearchActivity.this);
        user_id = sharedPreferences.getUserId();

        rv_search_friend = findViewById(R.id.rv_search_friend);
        search_view_friend = findViewById(R.id.search_view_friend);
        imgBack = findViewById(R.id.imgBack);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rv_search_friend.setLayoutManager(layoutManager);

        searchFriendAdapter = new SearchFriendAdapter(user_id, searchFriendModelArrayList, this, this);
        rv_search_friend.setAdapter(searchFriendAdapter);

        search_view_friend.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                new Handler().postDelayed(() -> {
                    searchFriend(s, user_id);
                }, 1000);

                return false;
            }
        });
        search_view_friend.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                // Log when the "X" button is pressed
                searchFriendModelArrayList.clear();
                searchFriendAdapter.notifyDataSetChanged();
                return false;
            }
        });
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void searchFriend(String full_name, int user_id) {
        OkHttpClient client = new OkHttpClient();

        // Tạo request để gửi lời gọi HTTP GET đến URL
        String url = Url_Api.getInstance().searchFriend(full_name, user_id);
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
                                searchFriendModelArrayList.clear();
                                JSONArray dataArray = jsonObject.getJSONArray("users");

                                ArrayList<SearchFriendModel> searchFriendModels = new ArrayList<>();
                                for (int i = 0; i < dataArray.length(); i++) {
                                    JSONObject messageObject = dataArray.getJSONObject(i);

                                    SearchFriendModel searchFriendModel = new SearchFriendModel();

                                    searchFriendModel.setFriend_ship_id(messageObject.getInt("friend_ship_id"));
                                    searchFriendModel.setSender_id(messageObject.getInt("sender_id"));
                                    searchFriendModel.setReceiver_id(messageObject.getInt("receiver_id"));
                                    searchFriendModel.setTime(messageObject.getString("time"));
                                    searchFriendModel.setIs_friend(messageObject.getInt("is_friend"));
                                    searchFriendModel.setUser_id(messageObject.getInt("user_id"));
                                    searchFriendModel.setFull_name(messageObject.getString("full_name"));
                                    searchFriendModel.setProfile_image(messageObject.getString("profile_image"));

                                    searchFriendModels.add(searchFriendModel);
                                }

                                runOnUiThread(() -> {
                                    searchFriendModelArrayList.addAll(searchFriendModels);
                                    searchFriendAdapter.notifyDataSetChanged(); // Thay đổi dòng này
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