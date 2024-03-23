package com.example.myapplication.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.activity.ViewProfileActivity;
import com.example.myapplication.bottomsheet.BottomSheetOptionComment;
import com.example.myapplication.model.CommentModel;
import com.example.myapplication.model.MessagesModel;
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
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class FileMessagesAdapter extends RecyclerView.Adapter<FileMessagesAdapter.ViewHolder> {

    private final Context context;
    private final Activity activity;
    private final ArrayList<MessagesModel> messagesModels;

    public FileMessagesAdapter(Context context, Activity activity, ArrayList<MessagesModel> messagesModels) {
        this.context = context;
        this.activity = activity;
        this.messagesModels = messagesModels;
    }

    public interface AddHeartCallback {
        void onAddHeartComplete(String url );
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_file_messages, parent, false);
        return new ViewHolder(view); // Chỗ này trả về một đối tượng ViewHolder mới
    }


    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MessagesModel messagesModel = messagesModels.get(position);
        holder.txtLink.setText(subStringFileName(URLUtil.guessFileName(messagesModel.getContent(), null, null)));
        holder.txtDate.setText(formatTime(messagesModel.getTime()));

        getInfo(messagesModel.getSender_id(), new AddHeartCallback() {
            @Override
            public void onAddHeartComplete(String url) {
                Glide.with(context)
                        .load(url)
                        .into(holder.roundedImageView);
            }
        });
        holder.itemView.setOnClickListener(view -> {
            Intent outIntent = new Intent("action_download");
            outIntent.putExtra("url_File", messagesModel.getContent());
            LocalBroadcastManager.getInstance(context).sendBroadcast(outIntent);
        });
    }

    @Override
    public int getItemCount() {

        return messagesModels.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtLink, txtDate;
        RoundedImageView roundedImageView;

        public ViewHolder(View view) {
            super(view);
            txtLink = itemView.findViewById(R.id.txtLink);
            txtDate = itemView.findViewById(R.id.txtTime);
            roundedImageView = itemView.findViewById(R.id.imgAvatar);
        }
    }

    private void getInfo(int user_id, AddHeartCallback callback) {
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
                                String profile_image = dataObject.getString("profile_image");
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        // Thực hiện các hoạt động liên quan đến giao diện người dùng trên luồng chính
                                        callback.onAddHeartComplete(profile_image);
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

    private String subStringFileName(String file_name) {
        int underscoreIndex = file_name.indexOf("_");

        if (underscoreIndex != -1) {
            // Kiểm tra xem phần trước dấu "_" là một số hay không
            String potentialTimestamp = file_name.substring(0, underscoreIndex);

            try {
                Long.parseLong(potentialTimestamp); // Thử chuyển đổi thành số
                // Nếu không có exception, đây là một số và có thể cắt
                return file_name.substring(underscoreIndex + 1);
            } catch (NumberFormatException e) {
                // Nếu có exception, phần trước dấu "_" không phải là một số
                return file_name;
            }
        } else {
            return file_name;
        }
    }

    private String formatTime(String originalTime) {
        try {
            SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat newFormat = new SimpleDateFormat("HH:mm dd-MM-yyyy", Locale.getDefault());

            Date date = originalFormat.parse(originalTime);
            if (date != null) {
                return newFormat.format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

}
