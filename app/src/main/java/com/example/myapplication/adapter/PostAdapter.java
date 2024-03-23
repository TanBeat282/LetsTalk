package com.example.myapplication.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ViewPort;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.activity.EditPostActivity;
import com.example.myapplication.activity.SendMediaActivity;
import com.example.myapplication.activity.ViewMediaActivity;
import com.example.myapplication.activity.ViewPostActivity;
import com.example.myapplication.activity.ViewPostImageActivity;
import com.example.myapplication.activity.ViewProfileActivity;
import com.example.myapplication.bottomsheet.BottomSheetLogOut;
import com.example.myapplication.bottomsheet.BottomSheetOptionMessagesList;
import com.example.myapplication.bottomsheet.BottomSheetOptionPost;
import com.example.myapplication.model.CommentModel;
import com.example.myapplication.model.ImagePostModel;
import com.example.myapplication.model.MessagesModel;
import com.example.myapplication.model.PostModel;
import com.example.myapplication.server.Url_Api;
import com.example.myapplication.sharedpreferencesmanager.SharedPreferencesManager;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.makeramen.roundedimageview.RoundedImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private final Context context;
    private final int user_id;
    private final Activity activity;
    private final ArrayList<PostModel> postModelArrayList;
    private int isHeartValue = 0;

    public PostAdapter(Context context, ArrayList<PostModel> postModelArrayList, int user_id, Activity activity) {
        this.context = context;
        this.postModelArrayList = postModelArrayList;
        this.user_id = user_id;
        this.activity = activity;
    }

    public interface AddHeartCallback {
        void onAddHeartComplete(int isHeart, int heart_count);
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }


    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        PostModel postModel = postModelArrayList.get(position);

        Glide.with(context).load(postModel.getProfile_image()).into(holder.imgAvatar);
        holder.txtName.setText(postModel.getFull_name());
        holder.txtTime.setText(calculateTime(postModel.getTime()));

        holder.txt_count_share.setVisibility(View.GONE);

        if (postModel.getHeart_count() != null || !postModel.getHeart_count().isEmpty()) {
            // Sử dụng Gson để phân tích chuỗi JSON thành một đối tượng JsonObject
            JsonObject jsonObject = JsonParser.parseString(postModel.getHeart_count()).getAsJsonObject();

            // Lấy giá trị của thuộc tính "is_heart"
            isHeartValue = jsonObject.get("heart_count").getAsInt();
            int is_heart = jsonObject.get("is_heart").getAsInt();
            holder.txt_count_heart.setText(String.valueOf(isHeartValue));

            if (is_heart == 1) {
                holder.btn_heart.setImageResource(R.drawable.outline_favorite_24);
            } else {
                holder.btn_heart.setImageResource(R.drawable.outline_favorite_border_24);
            }


            holder.txt_count_cmt.setText(String.valueOf(postModel.getComment_count()));
            holder.txtContent.setText(postModel.getContent());
        }

        // Trong ViewHolder
        List<ImagePostModel> imagePostModels = postModel.getImagePostModelList();
        if (imagePostModels.size() == 0) {
            holder.rv_image.setVisibility(View.GONE);
        } else {
            holder.rv_image.setVisibility(View.VISIBLE);
            MediaPostAdapter mediaPostAdapter = new MediaPostAdapter(context, activity, imagePostModels, true);
            if (imagePostModels.size() >= 7) {
                ImagePostModel newItem = new ImagePostModel(0, postModel.getPost_id(), "https://firebasestorage.googleapis.com/v0/b/letstalk-3d1c5.appspot.com/o/file_default%2Fba_cham.jpg?alt=media&token=c1da26ab-25eb-4fc8-bc5e-532d7d4497b1");
                imagePostModels.add(5, newItem);
            }
            if (imagePostModels.size() >= 6) {
                imagePostModels.subList(6, imagePostModels.size()).clear();
            }

            // Đặt LayoutManager dựa trên kích thước của imagePostModels
            if (imagePostModels.size() == 2) {
                holder.rv_image.setLayoutManager(new GridLayoutManager(context, 2));
            } else if (imagePostModels.size() == 3) {
                holder.rv_image.setLayoutManager(new GridLayoutManager(context, 3));
            } else if (imagePostModels.size() == 4) {
                holder.rv_image.setLayoutManager(new GridLayoutManager(context, 2));
            } else if (imagePostModels.size() >= 5) {
                holder.rv_image.setLayoutManager(new GridLayoutManager(context, 3));
            } else {
                holder.rv_image.setLayoutManager(new GridLayoutManager(context, 1));
            }

            // Đặt Adapter cho RecyclerView
            holder.rv_image.setAdapter(mediaPostAdapter);
        }

        holder.btn_more_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BottomSheetOptionPost bottomSheetOptionPost = new BottomSheetOptionPost(context, activity, postModel, position);
                bottomSheetOptionPost.show(((AppCompatActivity) context).getSupportFragmentManager(), bottomSheetOptionPost.getTag());
            }
        });


        holder.btn_heart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addHeart(postModel.getPost_id(), user_id, new AddHeartCallback() {
                    @Override
                    public void onAddHeartComplete(int isHeart, int heart_count) {
                        if (isHeart == 1) {
                            holder.btn_heart.setImageResource(R.drawable.outline_favorite_24);
                            if (heart_count == 0) {
                                holder.txt_count_heart.setVisibility(View.GONE);
                            } else {
                                holder.txt_count_heart.setVisibility(View.VISIBLE);
                                holder.txt_count_heart.setText(String.valueOf(heart_count));
                            }
                        } else {
                            holder.btn_heart.setImageResource(R.drawable.outline_favorite_border_24);
                            if (heart_count == 0) {
                                holder.txt_count_heart.setVisibility(View.GONE);
                            } else {
                                holder.txt_count_heart.setVisibility(View.VISIBLE);
                                holder.txt_count_heart.setText(String.valueOf(heart_count));
                            }
                        }
                    }
                });

            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ViewPostActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("post_id", postModel.getPost_id());
                bundle.putSerializable("position", position);
                bundle.putSerializable("is_finish", false);
                intent.putExtras(bundle);
                activity.startActivity(intent);
                activity.overridePendingTransition(R.anim.anim_in_left, R.anim.anim_out_left);
            }
        });

        holder.imgAvatar.setOnClickListener(view -> {
            Intent intent = new Intent(context, ViewProfileActivity.class);
            intent.putExtra("userId", postModel.getUser_id());
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {

        return postModelArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        RoundedImageView imgAvatar;
        ImageView btn_more_post, btn_heart, btn_comment, btn_share;
        TextView txtName, txtTime, txtContent, txt_count_heart, txt_count_cmt, txt_count_share;
        RecyclerView rv_image;


        public ViewHolder(View view) {
            super(view);
            imgAvatar = view.findViewById(R.id.imgAvatar);
            txtName = view.findViewById(R.id.txtName);
            txtTime = view.findViewById(R.id.txtTime);
            txtContent = view.findViewById(R.id.txtContent);
            btn_more_post = view.findViewById(R.id.btn_more_post);

            txt_count_heart = view.findViewById(R.id.txt_count_heart);
            txt_count_cmt = view.findViewById(R.id.txt_count_cmt);
            txt_count_share = view.findViewById(R.id.txt_count_share);
            btn_heart = view.findViewById(R.id.btn_heart);
            btn_comment = view.findViewById(R.id.btn_comment);
            btn_share = view.findViewById(R.id.btn_share);

            rv_image = view.findViewById(R.id.rv_image);
        }
    }

    private void addHeart(int post_id, int user_id, AddHeartCallback callback) {
        OkHttpClient client = new OkHttpClient();

        // Tạo request để gửi lời gọi HTTP GET đến URL
        String url = Url_Api.getInstance().addHeart(post_id, user_id, getTime());
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
                                int is_heart = jsonObject.getInt("is_heart");
                                int heart_count = jsonObject.getInt("heart_count");

                                activity.runOnUiThread(() -> {
                                    // Gọi callback để trả về giá trị is_heart
                                    callback.onAddHeartComplete(is_heart, heart_count);
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
        return dateFormat.format(new Date());
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
}
