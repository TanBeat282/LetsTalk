package com.example.myapplication.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.myapplication.R;
import com.example.myapplication.adapter.MediaMessagesAdapter;
import com.example.myapplication.model.MessagesModel;
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

public class ImageOrVideoFragment extends Fragment {
    private int messages_list_id;
    private LinearLayout linear_no_media;
    private RecyclerView rv_media;

    private final ArrayList<MessagesModel> messagesModelArrayList = new ArrayList<>();
    private MediaMessagesAdapter messagesAdapter;

    public ImageOrVideoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_image_or_video, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rv_media = view.findViewById(R.id.rv_media);
        linear_no_media = view.findViewById(R.id.linear_no_media);

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("messages_list", Context.MODE_PRIVATE);
        messages_list_id = sharedPreferences.getInt("messages_list_id", 0);

        rv_media.setLayoutManager(new GridLayoutManager(requireContext(), 3));

        messagesAdapter = new MediaMessagesAdapter(requireContext(), requireActivity(), messagesModelArrayList);
        rv_media.setAdapter(messagesAdapter);
    }

    private void getMessageList(int messages_list_id, int type_message1) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(Url_Api.getInstance().getMediaMessages(messages_list_id, type_message1))
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

                                ArrayList<MessagesModel> newMessages = new ArrayList<>();
                                for (int i = 0; i < dataArray.length(); i++) {
                                    JSONObject messageObject = dataArray.getJSONObject(i);
                                    int messageId = messageObject.getInt("messages_id");
                                    int messagesListId = messageObject.getInt("messages_list_id");
                                    int senderId = messageObject.getInt("sender_id");
                                    String content = messageObject.getString("content");
                                    String time = messageObject.getString("time");
                                    int typeMessage = messageObject.getInt("type_message");

                                    if (typeMessage != 8) {
                                        MessagesModel message = new MessagesModel(messagesListId, senderId, messageId, typeMessage, time, content);
                                        newMessages.add(message);
                                    }
                                }

                                requireActivity().runOnUiThread(() -> {
                                    rv_media.setVisibility(View.VISIBLE);
                                    linear_no_media.setVisibility(View.GONE);
                                    messagesModelArrayList.clear();
                                    messagesModelArrayList.addAll(newMessages);
                                    messagesAdapter.notifyDataSetChanged();
                                });
                            } else {
                                requireActivity().runOnUiThread(() -> {
                                    rv_media.setVisibility(View.GONE);
                                    linear_no_media.setVisibility(View.VISIBLE);
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
    public void onResume() {
        super.onResume();
        if (messages_list_id != 0) {
            getMessageList(messages_list_id, 1);
        }
    }
}
