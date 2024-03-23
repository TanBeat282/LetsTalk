package com.example.myapplication.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.activity.MessagesActivity;
import com.example.myapplication.activity.QrCodeActivity;
import com.example.myapplication.activity.ViewProfileActivity;
import com.example.myapplication.bottomsheet.BottomSheetOptionMessagesList;
import com.example.myapplication.model.FriendShipModel;
import com.example.myapplication.model.MessagesListModel;
import com.example.myapplication.server.Url_Api;
import com.makeramen.roundedimageview.RoundedImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class FriendShipAdapter extends RecyclerView.Adapter<FriendShipAdapter.ViewHolder> {

    private final ArrayList<FriendShipModel> friendShipModelArrayList;
    private final Context context;
    private final Activity activity;
    private final int user_id;

    public FriendShipAdapter(int user_id, ArrayList<FriendShipModel> friendShipModelArrayList, Context context, Activity activity) {
        this.user_id = user_id;
        this.friendShipModelArrayList = friendShipModelArrayList;
        this.context = context;
        this.activity = activity;
    }

    @NonNull
    @Override
    public FriendShipAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_friend_request, parent, false);
        return new FriendShipAdapter.ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull FriendShipAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        FriendShipModel friendShipModel = friendShipModelArrayList.get(position);

        Glide.with(context).load(friendShipModel.getProfie_image()).into(holder.imgAvatar);
        holder.txtName.setText(friendShipModel.getFull_name());
        holder.txt_time_send.setText(calculateTime(friendShipModel.getTime()));


        holder.btn_accept.setOnClickListener(view -> actionFriendRequest(friendShipModel.getSender_id(), friendShipModel.getReceiver_id(), getTime(), true, position));
        holder.btn_tuchoi.setOnClickListener(view -> actionFriendRequest(friendShipModel.getSender_id(), friendShipModel.getReceiver_id(), getTime(), false, position));
        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, ViewProfileActivity.class);
            intent.putExtra("userId", friendShipModel.getSender_id());
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return friendShipModelArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        RoundedImageView imgAvatar;
        LinearLayout btn_accept, btn_tuchoi;
        TextView txtName, txt_time_send;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            btn_accept = itemView.findViewById(R.id.btn_accept);
            btn_tuchoi = itemView.findViewById(R.id.btn_tuchoi);
            txtName = itemView.findViewById(R.id.txtName);
            txt_time_send = itemView.findViewById(R.id.txt_time_send);
        }
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

    private void actionFriendRequest(int sender_id, int receiver_id, String time, boolean action, int position) {

        // 0 chua ban be 1 bạn be
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(action ? Url_Api.getInstance().acceptFriendShip(user_id, user_id, user_id == sender_id ? receiver_id : sender_id, getTime()) : Url_Api.getInstance().removeFriendShip(user_id, user_id == sender_id ? receiver_id : sender_id))
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

                                activity.runOnUiThread(() -> {
                                    Toast.makeText(context, action ? "Chấp nhận yêu cầu kết bạn thành công" : "Hủy yêu cầu kết bạn thành công", Toast.LENGTH_SHORT).show();
                                    friendShipModelArrayList.remove(position);
                                    notifyDataSetChanged();
                                    sendDataToActivity(friendShipModelArrayList.size());
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

    private void sendDataToActivity(int count_friend_ship) {
        Intent intent = new Intent("friend_ship_adapter");
        Bundle bundle = new Bundle();
        bundle.putSerializable("count_friend_ship", count_friend_ship);
        intent.putExtras(bundle);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

}
