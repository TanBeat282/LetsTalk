package com.example.myapplication.bottomsheet;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.myapplication.R;
import com.example.myapplication.activity.QrCodeActivity;
import com.example.myapplication.model.FriendShipModel;
import com.example.myapplication.server.Url_Api;
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


public class BottomSheetUnFriend extends BottomSheetDialogFragment {
    private final Context context;
    private final Activity activity;
    private FriendShipModel friendShipModel;
    private int user_id, position;
    private BottomSheetDialog bottomSheetDialog;

    public BottomSheetUnFriend(Activity activity, Context context, int user_id, FriendShipModel friendShipModel, int position) {
        this.activity = activity;
        this.context = context;
        this.user_id = user_id;
        this.friendShipModel = friendShipModel;
        this.position = position;
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        bottomSheetDialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_unfriend, null);
        bottomSheetDialog.setContentView(view);

        LinearLayout linear_ok = view.findViewById(R.id.linear_ok);
        LinearLayout linear_cancel = view.findViewById(R.id.linear_cancel);
        TextView txt_Title = view.findViewById(R.id.txt_Title);
        TextView txt_name = view.findViewById(R.id.txt_name);

        txt_Title.setText("Bạn có muốn hủy kết bạn với");
        txt_name.setText(friendShipModel.getFull_name());

        linear_ok.setOnClickListener(v -> {
            actionFriendRequest();

        });
        linear_cancel.setOnClickListener(v -> bottomSheetDialog.dismiss());

        return bottomSheetDialog;
    }

    private void actionFriendRequest() {
        // 0 chua ban be 1 bạn be
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(Url_Api.getInstance().deleteFriend(friendShipModel.getFriend_ship_id()))
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
                                    Toast.makeText(context, "Hủy kết bạn thành công!", Toast.LENGTH_SHORT).show();
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
        Intent intent = new Intent("refesh");
        Bundle bundle = new Bundle();
        bundle.putSerializable("refesh", refesh);
        bundle.putSerializable("position", position);
        intent.putExtras(bundle);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
