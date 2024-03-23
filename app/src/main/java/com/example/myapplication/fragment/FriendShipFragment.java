package com.example.myapplication.fragment;

import static com.example.myapplication.helper.SortMessagesByTime.sortMessagesListByTime;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.activity.FriendListActivity;
import com.example.myapplication.activity.LoiMoiDaGuiKetBanActivity;
import com.example.myapplication.activity.MessageListActivity;
import com.example.myapplication.activity.MessagesActivity;
import com.example.myapplication.activity.QrCodeActivity;
import com.example.myapplication.adapter.FriendShipAdapter;
import com.example.myapplication.adapter.MessageListAdapter;
import com.example.myapplication.adapter.MessagesAdapter;
import com.example.myapplication.helper.Helper;
import com.example.myapplication.model.FriendShipModel;
import com.example.myapplication.model.MessagesListModel;
import com.example.myapplication.model.MessagesModel;
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

public class FriendShipFragment extends Fragment {
    private ImageView btn_search_friend, btn_friend, btn_qr_code;
    private TextView txt_friend_request, txt_count_friend_request;
    private RecyclerView rv_friend_request;
    private SwipeRefreshLayout swipe_refesh;
    private int user_id;
    private FriendShipAdapter friendShipAdapter;
    private final ArrayList<FriendShipModel> friendShipModelArrayList = new ArrayList<>();


    private final BroadcastReceiver broadcastReceiverAdapter = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle == null) {
                return;
            }
            int count_friend_ship = bundle.getInt("count_friend_ship");
            txt_count_friend_request.setText(String.valueOf(count_friend_ship));

        }
    };

    public FriendShipFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_friend_ship, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btn_search_friend = view.findViewById(R.id.btn_search_friend);
        btn_friend = view.findViewById(R.id.btn_friend);
        btn_qr_code = view.findViewById(R.id.btn_qr_code);
        txt_friend_request = view.findViewById(R.id.txt_friend_request);
        txt_count_friend_request = view.findViewById(R.id.txt_count_friend_request);
        rv_friend_request = view.findViewById(R.id.rv_friend_request);
        swipe_refesh = view.findViewById(R.id.swipe_refesh);

        //change color changeStatusBarColor and changeNavigationColor
        Helper.changeStatusBarColor(requireActivity(), R.color.white);
        Helper.changeNavigationColor(requireActivity(), R.color.white, true);

        //get user_id
        SharedPreferencesManager sharedPreferences = new SharedPreferencesManager(requireContext());
        user_id = sharedPreferences.getUserId();

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        rv_friend_request.setLayoutManager(layoutManager);

        friendShipAdapter = new FriendShipAdapter(user_id, friendShipModelArrayList, requireContext(), requireActivity());
        rv_friend_request.setAdapter(friendShipAdapter);

        swipe_refesh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
            friendShipModelArrayList.clear();
            getFriendRequest(user_id);
        }, 1000));


        btn_qr_code.setOnClickListener(view12 -> requireContext().startActivity(new Intent(requireContext(), QrCodeActivity.class)));
        btn_friend.setOnClickListener(view1 -> requireContext().startActivity(new Intent(requireContext(), FriendListActivity.class)));
        txt_friend_request.setOnClickListener(view1 -> requireContext().startActivity(new Intent(requireContext(), LoiMoiDaGuiKetBanActivity.class)));
    }

    private void getFriendRequest(int user_id) {

        // 0 chua ban be 1 bạn be
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(Url_Api.getInstance().getFriendShip(user_id, 0))
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
                                    friendShipModel.setFull_name(messageObject.getString("receiver_name"));
                                    friendShipModel.setProfie_image(messageObject.getString("receiver_avatar"));

                                    friendShipModels.add(friendShipModel);
                                }

                                requireActivity().runOnUiThread(() -> {
                                    friendShipModelArrayList.clear();
                                    friendShipModelArrayList.addAll(friendShipModels);
                                    txt_count_friend_request.setText(String.valueOf(friendShipModelArrayList.size()));
                                    friendShipAdapter.notifyDataSetChanged();
                                    swipe_refesh.setRefreshing(false);

                                });
                            } else {
                                requireActivity().runOnUiThread(() -> {
                                    friendShipModelArrayList.clear();
                                    txt_count_friend_request.setText(String.valueOf(0));
                                    friendShipAdapter.notifyDataSetChanged();
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

    @Override
    public void onResume() {
        super.onResume();
        getFriendRequest(user_id);
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(broadcastReceiverAdapter, new IntentFilter("friend_ship_adapter"));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(broadcastReceiverAdapter);
    }
}