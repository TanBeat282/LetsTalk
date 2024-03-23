package com.example.myapplication.activity;

import static com.example.myapplication.helper.SortMessagesByTime.sortMessagesListByTime;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.SearchView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.adapter.MessageListAdapter;
import com.example.myapplication.adapter.SearchMessagesAdapter;
import com.example.myapplication.helper.Helper;
import com.example.myapplication.model.MessagesListModel;
import com.example.myapplication.model.SearchMessagesModel;
import com.example.myapplication.server.Url_Api;

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

public class SearchMessagesActivity extends AppCompatActivity {
    private MessagesListModel messageListModel;
    private ImageView imgBack;
    private SearchView search_view_message;
    private SearchMessagesAdapter searchMessagesAdapter;
    private final ArrayList<SearchMessagesModel> searchMessagesModelArrayList = new ArrayList<>();
    private RecyclerView rv_search_messages;
    private TextView txt_count_messages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_messages);

        imgBack = findViewById(R.id.imgBack);
        search_view_message = findViewById(R.id.search_view_message);
        imgBack = findViewById(R.id.imgBack);
        txt_count_messages = findViewById(R.id.txt_count_messages);

        rv_search_messages = findViewById(R.id.rv_search_messages);
        rv_search_messages.setLayoutManager(new LinearLayoutManager(this));

        //change color changeStatusBarColor and changeNavigationColor
        Helper.changeStatusBarColor(this, R.color.white);
        Helper.changeNavigationColor(this, R.color.white, true);


        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            return;
        }
        messageListModel = (MessagesListModel) bundle.get("messages_list");

        search_view_message.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                searchMessages(messageListModel.getMessage_list_id(), s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        imgBack.setOnClickListener(view -> finish());
    }

    private void searchMessages(int message_list_id, String content) {
        OkHttpClient client = new OkHttpClient();

        // Tạo request để gửi lời gọi HTTP GET đến URL
        String url = Url_Api.getInstance().searchMessages(message_list_id, content);
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
                                searchMessagesModelArrayList.clear();
                                for (int i = 0; i < dataArray.length(); i++) {
                                    JSONObject messageObject = dataArray.getJSONObject(i);

                                    SearchMessagesModel searchMessagesModel = new SearchMessagesModel();

                                    searchMessagesModel.setMessages_id(messageObject.getInt("messages_id"));
                                    searchMessagesModel.setSender_id(messageObject.getInt("sender_id"));
                                    searchMessagesModel.setContent(messageObject.getString("content"));
                                    searchMessagesModel.setTime(messageObject.getString("time"));
                                    searchMessagesModel.setType_message(messageObject.getInt("type_message"));
                                    searchMessagesModel.setFull_name(messageObject.getString("full_name"));
                                    searchMessagesModel.setProfile_image(messageObject.getString("profile_image"));
                                    searchMessagesModelArrayList.add(searchMessagesModel);
                                }
                                runOnUiThread(() -> {
                                    txt_count_messages.setText("Có " + searchMessagesModelArrayList.size() + " tin nhắn trùng khớp");
                                    searchMessagesAdapter = new SearchMessagesAdapter(searchMessagesModelArrayList, SearchMessagesActivity.this, SearchMessagesActivity.this);
                                    rv_search_messages.setAdapter(searchMessagesAdapter);
                                    searchMessagesAdapter.notifyDataSetChanged();
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