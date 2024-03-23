package com.example.myapplication.bottomsheet;


import android.annotation.SuppressLint;
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
import com.example.myapplication.model.MessagesListModel;
import com.example.myapplication.model.MessagesModel;
import com.example.myapplication.sharedpreferencesmanager.SharedPreferencesManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.json.JSONException;
import org.json.JSONObject;


public class BottomSheetOptionMessagesList extends BottomSheetDialogFragment {
    private final Context context;
    private BottomSheetDialog bottomSheetDialog;
    private final MessagesListModel messagesListModel;
    private int user_id;

    public BottomSheetOptionMessagesList(Context context, MessagesListModel messagesListModel) {
        this.context = context;
        this.messagesListModel = messagesListModel;
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        bottomSheetDialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_option_messages_list, null);
        bottomSheetDialog.setContentView(view);

        SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(context);

        user_id = sharedPreferencesManager.getUserId();

        LinearLayout btn_delete_messages_list = view.findViewById(R.id.btn_delete_messages_list);
        LinearLayout btn_notification_messages = view.findViewById(R.id.btn_notification_messages);
        LinearLayout btn_block_messages_list = view.findViewById(R.id.btn_block_messages_list);

        return bottomSheetDialog;
    }
}
