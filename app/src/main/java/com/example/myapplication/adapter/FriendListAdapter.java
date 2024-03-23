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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.activity.LoiMoiDaGuiKetBanActivity;
import com.example.myapplication.activity.MessagesActivity;
import com.example.myapplication.activity.ViewProfileActivity;
import com.example.myapplication.bottomsheet.BottomSheetUnFriend;
import com.example.myapplication.model.FriendShipModel;
import com.example.myapplication.model.MessagesListModel;
import com.example.myapplication.server.Url_Api;
import com.example.myapplication.sharedpreferencesmanager.SharedPreferencesManager;
import com.makeramen.roundedimageview.RoundedImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
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

public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.ViewHolder> {

    private final ArrayList<FriendShipModel> friendShipModelArrayList;
    private final Context context;
    private final Activity activity;
    private final int user_id;

    //get user_id


    public FriendListAdapter(int user_id, ArrayList<FriendShipModel> friendShipModelArrayList, Context context, Activity activity) {
        this.user_id = user_id;
        this.friendShipModelArrayList = friendShipModelArrayList;
        this.context = context;
        this.activity = activity;
    }

    @NonNull
    @Override
    public FriendListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_friend_list, parent, false);
        return new FriendListAdapter.ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull FriendListAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        FriendShipModel friendShipModel = friendShipModelArrayList.get(position);

        holder.btn_accept.setVisibility(View.GONE);
        holder.btn_messages.setVisibility(View.VISIBLE);
        holder.btn_send_friend_ship.setVisibility(View.GONE);
        holder.btn_tuchoi.setVisibility(View.VISIBLE);
        holder.txt_tuchoi.setText("Hủy kết bạn");


        Glide.with(context).load(friendShipModel.getProfie_image()).into(holder.imgAvatar);
        holder.txtName.setText(friendShipModel.getFull_name());


        holder.btn_tuchoi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BottomSheetUnFriend bottomSheetUnFriend = new BottomSheetUnFriend(activity, context, user_id, friendShipModel, position);
                bottomSheetUnFriend.show(((AppCompatActivity) context).getSupportFragmentManager(), bottomSheetUnFriend.getTag());
            }
        });
        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, ViewProfileActivity.class);
            intent.putExtra("userId", user_id == friendShipModel.getSender_id() ? friendShipModel.getReceiver_id() : friendShipModel.getSender_id());
            context.startActivity(intent);
        });
        holder.btn_messages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkMessagesList(user_id, user_id, user_id == friendShipModel.getSender_id() ? friendShipModel.getReceiver_id() : friendShipModel.getSender_id());
            }
        });

    }

    @Override
    public int getItemCount() {
        return friendShipModelArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        RoundedImageView imgAvatar;
        LinearLayout btn_accept, btn_tuchoi, btn_messages, btn_send_friend_ship;
        TextView txtName, txt_tuchoi, txt_accept;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);

            btn_accept = itemView.findViewById(R.id.btn_accept);
            btn_messages = itemView.findViewById(R.id.btn_messages);
            btn_send_friend_ship = itemView.findViewById(R.id.btn_send_friend_ship);


            btn_tuchoi = itemView.findViewById(R.id.btn_tuchoi);
            txt_tuchoi = itemView.findViewById(R.id.txt_tuchoi);

            txtName = itemView.findViewById(R.id.txtName);
        }
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


                                activity.runOnUiThread(() -> {
                                    Intent intent = new Intent(context, MessagesActivity.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putSerializable("messages_list", message);
                                    intent.putExtras(bundle);

                                    context.startActivity(intent);
                                    activity.overridePendingTransition(R.anim.anim_in_left, R.anim.anim_out_left);
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
