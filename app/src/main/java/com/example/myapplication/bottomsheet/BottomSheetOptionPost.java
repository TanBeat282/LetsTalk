package com.example.myapplication.bottomsheet;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.myapplication.R;
import com.example.myapplication.activity.EditPostActivity;
import com.example.myapplication.activity.PostActivity;
import com.example.myapplication.adapter.PostAdapter;
import com.example.myapplication.model.CommentModel;
import com.example.myapplication.model.PostModel;
import com.example.myapplication.server.Url_Api;
import com.example.myapplication.sharedpreferencesmanager.SharedPreferencesManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class BottomSheetOptionPost extends BottomSheetDialogFragment {
    private final Context context;
    private final Activity activity;
    private BottomSheetDialog bottomSheetDialog;
    private int user_id;
    private int position;
    private PostModel postModel;
    private FirebaseStorage storage;

    public BottomSheetOptionPost(Context context, Activity activity, PostModel postModel, int position) {
        this.context = context;
        this.activity = activity;
        this.postModel = postModel;
        this.position = position;
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        bottomSheetDialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_option_post, null);
        bottomSheetDialog.setContentView(view);

        SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(context);
        user_id = sharedPreferencesManager.getUserId();
        storage = FirebaseStorage.getInstance();

        LinearLayout btn_save_post = view.findViewById(R.id.btn_save_post);
        ImageView img_save_post = view.findViewById(R.id.img_save_post);
        TextView txt_save_post = view.findViewById(R.id.txt_save_post);

        LinearLayout btn_edit_post = view.findViewById(R.id.btn_edit_post);
        LinearLayout btn_delete_post = view.findViewById(R.id.btn_delete_post);
        LinearLayout btn_report_post = view.findViewById(R.id.btn_report_post);

        if (postModel.getUser_id() == user_id) {
            btn_edit_post.setVisibility(View.VISIBLE);
            btn_delete_post.setVisibility(View.VISIBLE);
        } else {
            btn_report_post.setVisibility(View.VISIBLE);
        }
        if (postModel.isIs_save()) {
            img_save_post.setImageResource(R.drawable.outline_bookmark_24);
            txt_save_post.setText("Bỏ lưu bài viết");
        } else {
            img_save_post.setImageResource(R.drawable.outline_bookmark_border_24);
            txt_save_post.setText("Lưu bài viết");
        }
        btn_save_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savePost(user_id, postModel.getPost_id());
            }
        });

        btn_edit_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, EditPostActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("post_model", postModel);
                intent.putExtras(bundle);
                startActivity(intent);
                bottomSheetDialog.dismiss();
            }
        });
        btn_delete_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deletePost(postModel.getPost_id());
            }
        });

        return bottomSheetDialog;
    }

    private void savePost(int user_id, int post_id) {
        OkHttpClient client = new OkHttpClient();

        // Tạo request để gửi lời gọi HTTP GET đến URL
        String url = Url_Api.getInstance().savePost(user_id, post_id, getTime());
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
                                int is_save_post = jsonObject.getInt("is_save_post");

                                activity.runOnUiThread(() -> {
                                    if (is_save_post == 1) {
                                        Toast.makeText(context, "Lưu bài viết thành công", Toast.LENGTH_SHORT).show();
                                        bottomSheetDialog.dismiss();
                                    } else {
                                        Toast.makeText(context, "Bỏ lưu bài viết thành công", Toast.LENGTH_SHORT).show();
                                        bottomSheetDialog.dismiss();
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

    private void deletePost(int post_id) {
        OkHttpClient client = new OkHttpClient();

        // Tạo request để gửi lời gọi HTTP GET đến URL
        String url = Url_Api.getInstance().deletePost(post_id);
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
                                activity.runOnUiThread(() -> {
                                    if (!postModel.getImagePostModelList().isEmpty()) {
                                        deleteAllFilesInFolder(post_id);
                                    } else {
                                        sendDataToActivity(true, position);
                                        bottomSheetDialog.dismiss();
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

    private void deleteAllFilesInFolder(int post_id) {
        // Tạo một đối tượng StorageReference đại diện cho thư mục cần xóa
        StorageReference storageRef = storage.getReference().child("post/" + post_id);

        // Lấy danh sách tất cả các tệp trong thư mục
        storageRef.listAll()
                .addOnSuccessListener(listResult -> {
                    // Duyệt qua danh sách và xóa từng tệp
                    for (StorageReference item : listResult.getItems()) {
                        item.delete()
                                .addOnSuccessListener(aVoid -> {
                                    // Xóa tệp thành công
                                    sendDataToActivity(true, position);
                                    bottomSheetDialog.dismiss();
                                })
                                .addOnFailureListener(exception -> {
                                    // Xóa tệp thất bại
                                    Log.e("FirebaseStorage", "Lỗi khi xóa tệp: " + exception.getMessage());
                                });
                    }
                })
                .addOnFailureListener(exception -> {
                    // Lấy danh sách tệp thất bại
                    Log.e("FirebaseStorage", "Lỗi khi lấy danh sách tệp: " + exception.getMessage());
                });
    }


    private void sendDataToActivity(boolean refesh, int position) {
        Intent intent = new Intent("refesh_post");
        Bundle bundle = new Bundle();
        bundle.putSerializable("refesh", refesh);
        bundle.putSerializable("position", position);
        intent.putExtras(bundle);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private String getTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return dateFormat.format(new Date());
    }

}
