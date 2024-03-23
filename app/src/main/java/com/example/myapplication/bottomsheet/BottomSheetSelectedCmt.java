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
import com.example.myapplication.model.CommentModel;
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


public class BottomSheetSelectedCmt extends BottomSheetDialogFragment {
    private final Context context;
    private final Activity activity;
    private BottomSheetDialog bottomSheetDialog;

    public BottomSheetSelectedCmt(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        bottomSheetDialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_selected_comment, null);
        bottomSheetDialog.setContentView(view);


        LinearLayout btn_latest = view.findViewById(R.id.btn_latest);
        LinearLayout btn_oldest = view.findViewById(R.id.btn_oldest);

        btn_oldest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendDataToActivity(false);
                bottomSheetDialog.dismiss();
            }
        });
        btn_latest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendDataToActivity(true);
                bottomSheetDialog.dismiss();

            }
        });


        return bottomSheetDialog;
    }


    private void sendDataToActivity(boolean is_latest) {
        Intent intent = new Intent("selected_comment");
        Bundle bundle = new Bundle();
        bundle.putSerializable("is_latest", is_latest);
        intent.putExtras(bundle);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
