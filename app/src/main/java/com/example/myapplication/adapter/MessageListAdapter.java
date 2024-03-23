package com.example.myapplication.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.activity.MessagesActivity;
import com.example.myapplication.bottomsheet.BottomSheetOptionMessagesList;
import com.example.myapplication.bottomsheet.BottomSheetSelectedMessage;
import com.example.myapplication.model.MessagesListModel;
import com.example.myapplication.server.Url_Api;
import com.makeramen.roundedimageview.RoundedImageView;

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

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.ViewHolder> {

    private final ArrayList<MessagesListModel> messageListModels;
    private final Context context;
    private final Activity activity;
    private final int user_id;

    public MessageListAdapter(int user_id, ArrayList<MessagesListModel> messageListModels, Context context, Activity activity) {
        this.user_id = user_id;
        this.messageListModels = messageListModels;
        this.context = context;
        this.activity = activity;
    }

    public interface FriendCheckCallback {
        void onFriendCheckResult(boolean isFriend);
    }


    @NonNull
    @Override
    public MessageListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_message_list, parent, false);
        return new MessageListAdapter.ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MessageListAdapter.ViewHolder holder, int position) {
        MessagesListModel messageListModel = messageListModels.get(position);

        Glide.with(context).load(messageListModel.getReceiver_avatar()).into(holder.avatarImageView);


        if (user_id == messageListModel.getSender_id()) {
            if (messageListModel.isIs_seen()) {
                Glide.with(context).load(messageListModel.getReceiver_avatar()).into(holder.imgAvatarSeen);
                holder.txtTime.setText(convertTime(messageListModel.getTime()));
                holder.txtTime.setVisibility(View.VISIBLE);
            } else {
                holder.txtTime.setText(convertTime(messageListModel.getTime()));
                holder.txtTime.setVisibility(View.VISIBLE);
            }
        } else {
            holder.imgAvatarSeen.setVisibility(View.GONE);
        }

//
        int textColor = messageListModel.isIs_seen() ? R.color.gray_text : R.color.black;
///
        holder.nameTextView.setText(messageListModel.getReceiver_name());
        if (user_id == messageListModel.getReceiver_id()) {
            holder.nameTextView.setTextColor(ContextCompat.getColor(context, textColor));
            holder.nameTextView.setTypeface(null, Typeface.BOLD);
        }
//
        if (user_id == messageListModel.getSender_id()) {
            holder.txtNameSend.setText("Tôi: ");
        } else if (user_id == messageListModel.getReceiver_id()) {
            holder.txtNameSend.setText(messageListModel.getReceiver_name() + ": ");
        }
        if (user_id == messageListModel.getReceiver_id()) {
            holder.txtNameSend.setTextColor(ContextCompat.getColor(context, textColor));
            holder.nameTextView.setTypeface(null, Typeface.BOLD);
        }

        //
        if (messageListModel.getType_message() == 1) {
            holder.txtMess.setText("Đã gửi hình ảnh");
        } else if (messageListModel.getType_message() == 2) {
            holder.txtMess.setText("Đã gửi video");
        } else if (messageListModel.getType_message() == 3) {
            holder.txtMess.setText("Đã gửi ghi âm");
        } else if (messageListModel.getType_message() == 4) {
            holder.txtMess.setText("Đã gửi tệp tin");
        } else if (messageListModel.getType_message() == 6) {
            holder.txtMess.setText("Đã bày tỏ cảm xúc tin nhắn");
        } else if (messageListModel.getType_message() == 7) {
            try {
                JSONObject contentJson = new JSONObject(messageListModel.getLast_content());
                holder.txtMess.setText("Đã trả lời tin nhắn: " + contentJson.getString("content"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (messageListModel.getType_message() == 9) {
            holder.txtMess.setText("Đã thu hồi tin nhắn");
        } else {
            holder.txtMess.setText(messageListModel.getLast_content());
        }

        if (user_id == messageListModel.getReceiver_id()) {
            holder.txtMess.setTextColor(ContextCompat.getColor(context, textColor));
        }

        //
        checkIsFriend(user_id, user_id == messageListModel.getSender_id() ? messageListModel.getReceiver_id() : messageListModel.getSender_id(), new FriendCheckCallback() {
            @Override
            public void onFriendCheckResult(boolean isFriend) {
                if (isFriend) {
                    holder.viewHoatDong.setVisibility(View.VISIBLE);
                    if (messageListModel.isReceiver_is_online()) {
                        holder.viewHoatDong.setBackgroundResource(R.drawable.background_round_green);
                    } else {
                        holder.viewHoatDong.setBackgroundResource(R.drawable.background_round_red);
                    }
                } else {
                    holder.viewHoatDong.setVisibility(View.GONE);
                }
            }
        });


        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MessagesActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("messages_list", messageListModel);
            intent.putExtras(bundle);

            context.startActivity(intent);
            activity.overridePendingTransition(R.anim.anim_in_left, R.anim.anim_out_left);
        });

        holder.itemView.setOnLongClickListener(view -> {
            BottomSheetOptionMessagesList bottomSheetOptionMessagesList = new BottomSheetOptionMessagesList(context, messageListModel);
            bottomSheetOptionMessagesList.show(((AppCompatActivity) context).getSupportFragmentManager(), bottomSheetOptionMessagesList.getTag());
            return false;
        });

    }

    @Override
    public int getItemCount() {
        return messageListModels.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        RoundedImageView avatarImageView, imgAvatarSeen;
        TextView nameTextView, txtTime;
        TextView txtNameSend, txtMess;
        View viewHoatDong;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarImageView = itemView.findViewById(R.id.imgAvatar);
            nameTextView = itemView.findViewById(R.id.txtName);
            txtNameSend = itemView.findViewById(R.id.txtNameSend);
            txtMess = itemView.findViewById(R.id.txtMess);
            viewHoatDong = itemView.findViewById(R.id.viewHoatDong);
            imgAvatarSeen = itemView.findViewById(R.id.imgAvatarSeen);
            txtTime = itemView.findViewById(R.id.txtTime);
        }
    }

    private String convertTime(String targetDateTime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentTime = dateFormat.format(new Date());

        try {
            Date currentDate = dateFormat.parse(currentTime);
            Date targetDate = dateFormat.parse(targetDateTime);

            assert targetDate != null;
            assert currentDate != null;
            long durationInMillis = Math.abs(targetDate.getTime() - currentDate.getTime()); // Lấy giá trị tuyệt đối
            long days = durationInMillis / (24 * 60 * 60 * 1000); // Tính số ngày
            long hours = (durationInMillis % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000);
            long minutes = (durationInMillis % (60 * 60 * 1000)) / (60 * 1000);

            if (days > 0) {
                return days + " ngày trước";
            } else if (hours >= 1) {
                return hours + " giờ trước";
            } else {
                return minutes + " phút trước";
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return "00:00";
        }
    }

    private void checkIsFriend(int sender_id, int reciever_id, FriendCheckCallback callback) {
        OkHttpClient client = new OkHttpClient();

        String url = Url_Api.getInstance().checkIsFriend(sender_id, reciever_id);
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                callback.onFriendCheckResult(false); // Gọi callback với giá trị mặc định
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
                                int check_is_friend = dataObject.getInt("is_friend");
                                boolean isFriend = (check_is_friend != 0 && check_is_friend != -1);

                                activity.runOnUiThread(() -> callback.onFriendCheckResult(isFriend));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            callback.onFriendCheckResult(false); // Gọi callback với giá trị mặc định
                        }
                    }
                }
            }
        });
    }


}
