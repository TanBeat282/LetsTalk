package com.example.myapplication.bottomsheet;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.myapplication.R;
import com.example.myapplication.adapter.PostAdapter;
import com.example.myapplication.model.CommentModel;
import com.example.myapplication.model.MessagesListModel;
import com.example.myapplication.server.Url_Api;
import com.example.myapplication.sharedpreferencesmanager.SharedPreferencesManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class BottomSheetOptionComment extends BottomSheetDialogFragment {
    private final Context context;
    private final Activity activity;
    private BottomSheetDialog bottomSheetDialog;
    private int user_id;
    private int position;
    private CommentModel commentModel;

    public BottomSheetOptionComment(Context context, Activity activity, CommentModel commentModel, int position) {
        this.context = context;
        this.activity = activity;
        this.commentModel = commentModel;
        this.position = position;
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        bottomSheetDialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_option_comment, null);
        bottomSheetDialog.setContentView(view);

        SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(context);
        user_id = sharedPreferencesManager.getUserId();

        LinearLayout btn_repy_comment = view.findViewById(R.id.btn_repy_comment);
        LinearLayout btn_copy_comment = view.findViewById(R.id.btn_copy_comment);
        LinearLayout btn_delete_comment = view.findViewById(R.id.btn_delete_comment);

        btn_copy_comment.setOnClickListener(view1 -> {
            ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboardManager != null) {
                ClipData clipData = ClipData.newPlainText("text_label", commentModel.getContent());
                clipboardManager.setPrimaryClip(clipData);
                Toast.makeText(context, "Đã sao chép văn bản", Toast.LENGTH_SHORT).show();
                bottomSheetDialog.dismiss();
            }
        });
        btn_delete_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteComment(commentModel.getComment_id(), position);
            }
        });

        return bottomSheetDialog;
    }

    private void deleteComment(int comment_id, int position) {
        OkHttpClient client = new OkHttpClient();

        // Tạo request để gửi lời gọi HTTP GET đến URL
        String url = Url_Api.getInstance().deleteCmt(comment_id);
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
                                    sendDataToActivity(true, position);
                                    bottomSheetDialog.dismiss();
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

    private void sendDataToActivity(boolean refesh, int position) {
        Intent intent = new Intent("refesh_comment");
        Bundle bundle = new Bundle();
        bundle.putSerializable("refesh", refesh);
        bundle.putSerializable("position", position);
        intent.putExtras(bundle);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
