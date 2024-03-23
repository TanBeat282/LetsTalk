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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.activity.MessagesActivity;
import com.example.myapplication.activity.ViewProfileActivity;
import com.example.myapplication.bottomsheet.BottomSheetUnFriend;
import com.example.myapplication.model.FriendShipModel;
import com.example.myapplication.model.MessagesListModel;
import com.example.myapplication.model.SearchFriendModel;
import com.example.myapplication.server.Url_Api;
import com.makeramen.roundedimageview.RoundedImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class SearchFriendAdapter extends RecyclerView.Adapter<SearchFriendAdapter.ViewHolder> {

    private final ArrayList<SearchFriendModel> searchFriendModelArrayList;
    private final Context context;
    private final Activity activity;
    private final int user_id;

    public SearchFriendAdapter(int user_id, ArrayList<SearchFriendModel> searchFriendModelArrayList, Context context, Activity activity) {
        this.user_id = user_id;
        this.searchFriendModelArrayList = searchFriendModelArrayList;
        this.context = context;
        this.activity = activity;
    }

    public interface sendFriendCallback {
        void onSendFriendComplete(boolean status);
    }

    public interface acceptFriendCallback {
        void onAcceptFriendComplete(boolean status);
    }

    public interface removeFriendCallback {
        void onRemoveFriendComplete(boolean status);
    }

    @NonNull
    @Override
    public SearchFriendAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_friend_list, parent, false);
        return new SearchFriendAdapter.ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull SearchFriendAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        SearchFriendModel searchFriendModel = searchFriendModelArrayList.get(position);

        Glide.with(context).load(searchFriendModel.getProfile_image()).into(holder.imgAvatar);
        holder.txtName.setText(searchFriendModel.getFull_name());

        if (searchFriendModel.getSender_id() == user_id && searchFriendModel.isIs_friend() == 0) {
            holder.btn_accept.setVisibility(View.GONE);
            holder.btn_messages.setVisibility(View.GONE);
            holder.btn_send_friend_ship.setVisibility(View.GONE);
            holder.btn_tuchoi.setVisibility(View.VISIBLE);
            holder.txt_tuchoi.setText("Hủy yêu cầu");
        }
        if (searchFriendModel.getReceiver_id() == user_id && searchFriendModel.isIs_friend() == 0) {
            holder.btn_accept.setVisibility(View.VISIBLE);
            holder.btn_messages.setVisibility(View.GONE);
            holder.btn_send_friend_ship.setVisibility(View.GONE);
            holder.btn_tuchoi.setVisibility(View.VISIBLE);
            holder.txt_tuchoi.setText("Từ chối");
        }
        if (searchFriendModel.isIs_friend() == -1) {
            holder.btn_accept.setVisibility(View.GONE);
            holder.btn_messages.setVisibility(View.GONE);
            holder.btn_send_friend_ship.setVisibility(View.VISIBLE);
            holder.btn_tuchoi.setVisibility(View.GONE);
        }
        if (searchFriendModel.isIs_friend() == 1) {
            holder.btn_accept.setVisibility(View.GONE);
            holder.btn_messages.setVisibility(View.VISIBLE);
            holder.btn_send_friend_ship.setVisibility(View.GONE);
            holder.btn_tuchoi.setVisibility(View.VISIBLE);
            holder.txt_tuchoi.setText("Hủy kết bạn");
        }
        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, ViewProfileActivity.class);
            intent.putExtra("userId", searchFriendModel.getUser_id());
            context.startActivity(intent);
        });

        holder.btn_messages.setOnClickListener(view -> checkMessagesList(user_id, user_id, user_id == searchFriendModel.getSender_id() ? searchFriendModel.getReceiver_id() : searchFriendModel.getSender_id()));

        if (searchFriendModel.getSender_id() == user_id && searchFriendModel.isIs_friend() == 0) {
            holder.btn_tuchoi.setOnClickListener(view -> removeFriend(searchFriendModel.getFriend_ship_id(), status -> {
                if (status) {
                    holder.btn_accept.setVisibility(View.GONE);
                    holder.btn_messages.setVisibility(View.GONE);
                    holder.btn_send_friend_ship.setVisibility(View.VISIBLE);
                    holder.btn_tuchoi.setVisibility(View.GONE);
                    Toast.makeText(context, "Hủy gửi yêu cầu kết bạn thành công!", Toast.LENGTH_SHORT).show();
                }
            }));
        }
        if (searchFriendModel.getReceiver_id() == user_id && searchFriendModel.isIs_friend() == 0) {
            holder.btn_tuchoi.setOnClickListener(view -> removeFriend(searchFriendModel.getFriend_ship_id(), status -> {
                if (status) {
                    holder.btn_accept.setVisibility(View.GONE);
                    holder.btn_messages.setVisibility(View.GONE);
                    holder.btn_send_friend_ship.setVisibility(View.VISIBLE);
                    holder.btn_tuchoi.setVisibility(View.GONE);
                    Toast.makeText(context, "Từ chối kết bạn thành công!", Toast.LENGTH_SHORT).show();
                }
            }));
        }
        if (searchFriendModel.isIs_friend() == 1) {
            holder.btn_tuchoi.setOnClickListener(view -> removeFriend(searchFriendModel.getFriend_ship_id(), status -> {
                if (status) {
                    holder.btn_accept.setVisibility(View.GONE);
                    holder.btn_messages.setVisibility(View.GONE);
                    holder.btn_send_friend_ship.setVisibility(View.VISIBLE);
                    holder.btn_tuchoi.setVisibility(View.GONE);
                    Toast.makeText(context, "Hủy kết bạn thành công!", Toast.LENGTH_SHORT).show();
                }
            }));
        }

        holder.btn_send_friend_ship.setOnClickListener(view -> sendFriendShip(user_id, searchFriendModel.getUser_id(), status -> {
            if (status) {
                holder.btn_accept.setVisibility(View.GONE);
                holder.btn_messages.setVisibility(View.GONE);
                holder.btn_send_friend_ship.setVisibility(View.GONE);
                holder.btn_tuchoi.setVisibility(View.VISIBLE);
                holder.txt_tuchoi.setText("Hủy yêu cầu");
                Toast.makeText(context, "Gửi yêu kết bạn thành công!", Toast.LENGTH_SHORT).show();
            }
        }));
        holder.btn_accept.setOnClickListener(view -> acceptFriendShip(user_id, searchFriendModel.getUser_id(), status -> {
            if (status) {
                holder.btn_accept.setVisibility(View.GONE);
                holder.btn_messages.setVisibility(View.VISIBLE);
                holder.btn_send_friend_ship.setVisibility(View.GONE);
                holder.btn_tuchoi.setVisibility(View.VISIBLE);
                holder.txt_tuchoi.setText("Hủy kết bạn");
                Toast.makeText(context, "Chấp nhận kết bạn thành công!", Toast.LENGTH_SHORT).show();
            }
        }));
    }

    @Override
    public int getItemCount() {
        return searchFriendModelArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        RoundedImageView imgAvatar;
        LinearLayout btn_accept, btn_tuchoi, btn_messages, btn_send_friend_ship;
        TextView txtName, txt_tuchoi;

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

    private void removeFriend(int friend_ship_id, removeFriendCallback callback) {
        // 0 chua ban be 1 bạn be
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(Url_Api.getInstance().deleteFriend(friend_ship_id))
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
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onRemoveFriendComplete(status);
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        });
    }

    private void sendFriendShip(int sender_id, int receiver_id, sendFriendCallback callback) {

        OkHttpClient client = new OkHttpClient();

        String url = Url_Api.getInstance().addFriendShip(sender_id, receiver_id, getTime());
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
                            activity.runOnUiThread(() -> {
                                callback.onSendFriendComplete(status);
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    private void acceptFriendShip(int sender_id, int receiver_id, acceptFriendCallback callback) {

        OkHttpClient client = new OkHttpClient();

        String url = Url_Api.getInstance().acceptFriendShip(user_id, sender_id, receiver_id, getTime());
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
                            activity.runOnUiThread(() -> {
                                callback.onAcceptFriendComplete(status);
                            });
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
}
