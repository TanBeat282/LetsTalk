package com.example.myapplication.bottomsheet;


import static androidx.core.content.ContextCompat.getSystemService;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.myapplication.R;
import com.example.myapplication.model.MessagesModel;
import com.example.myapplication.sharedpreferencesmanager.SharedPreferencesManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;


public class BottomSheetSelectedMessage extends BottomSheetDialogFragment {
    private final Context context;
    private BottomSheetDialog bottomSheetDialog;
    private final MessagesModel messagesModel;
    private int user_id;

    public BottomSheetSelectedMessage(Context context, MessagesModel messagesModel) {
        this.context = context;
        this.messagesModel = messagesModel;
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        bottomSheetDialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_selected_message, null);
        bottomSheetDialog.setContentView(view);

        LinearLayout linear_bottom = view.findViewById(R.id.linear_bottom);
        if (linear_bottom != null) {
            linear_bottom.setBackground(ContextCompat.getDrawable(context, R.drawable.transparent_background));
        }

        SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(context);

        user_id = sharedPreferencesManager.getUserId();

        LinearLayout btn_reply = view.findViewById(R.id.btn_reply);
        LinearLayout btn_copy = view.findViewById(R.id.btn_copy);
        LinearLayout btn_repeat = view.findViewById(R.id.btn_repeat);
        LinearLayout btn_remove = view.findViewById(R.id.btn_remove);
        LinearLayout btn_delete = view.findViewById(R.id.btn_delete);
        LinearLayout btn_restore = view.findViewById(R.id.btn_restore);
        LinearLayout btn_download = view.findViewById(R.id.btn_download);

        if (messagesModel.getType_message() == 9) {
            if (messagesModel.getSender_id() == user_id) {
                btn_remove.setVisibility(View.GONE);
                btn_restore.setVisibility(View.VISIBLE);
                btn_delete.setVisibility(View.VISIBLE);
            } else {
                btn_remove.setVisibility(View.GONE);
                btn_restore.setVisibility(View.GONE);
                btn_delete.setVisibility(View.VISIBLE);
            }
            btn_reply.setVisibility(View.GONE);
            btn_copy.setVisibility(View.GONE);
            btn_repeat.setVisibility(View.GONE);
            btn_download.setVisibility(View.GONE);
        } else if (messagesModel.getType_message() == 1 || messagesModel.getType_message() == 2 || messagesModel.getType_message() == 3 || messagesModel.getType_message() == 4) {
            if (messagesModel.getSender_id() == user_id) {
                btn_remove.setVisibility(View.VISIBLE);
                btn_restore.setVisibility(View.GONE);
                btn_delete.setVisibility(View.VISIBLE);
            } else {
                btn_remove.setVisibility(View.GONE);
                btn_restore.setVisibility(View.GONE);
                btn_delete.setVisibility(View.VISIBLE);
            }
            btn_copy.setVisibility(View.GONE);
            btn_download.setVisibility(View.VISIBLE);

        } else {
            if (messagesModel.getSender_id() == user_id) {
                btn_remove.setVisibility(View.VISIBLE);
                btn_restore.setVisibility(View.GONE);
                btn_delete.setVisibility(View.VISIBLE);
            } else {
                btn_remove.setVisibility(View.GONE);
                btn_restore.setVisibility(View.GONE);
                btn_delete.setVisibility(View.VISIBLE);
            }
            btn_download.setVisibility(View.GONE);
        }


        btn_reply.setOnClickListener(view1 -> {
            sendDataToActivity(messagesModel, 1);
            bottomSheetDialog.dismiss();
        });

        btn_copy.setOnClickListener(view12 -> {
            if (messagesModel.getType_message() == 7) {
                try {
                    JSONObject jsonObject = new JSONObject(messagesModel.getContent());
                    String replyContentValue = jsonObject.getString("reply_content");

                    ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    if (clipboardManager != null) {
                        ClipData clipData = ClipData.newPlainText("text_label", replyContentValue);
                        clipboardManager.setPrimaryClip(clipData);
                        Toast.makeText(context, "Đã sao chép văn bản", Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboardManager != null) {
                    ClipData clipData = ClipData.newPlainText("text_label", messagesModel.getContent());
                    clipboardManager.setPrimaryClip(clipData);
                    Toast.makeText(context, "Đã sao chép văn bản", Toast.LENGTH_SHORT).show();
                }
            }
            bottomSheetDialog.dismiss();
        });

        btn_repeat.setOnClickListener(view13 -> {
            sendDataToActivity(messagesModel, 3);
            bottomSheetDialog.dismiss();
        });

        btn_remove.setOnClickListener(view14 -> {
            if (messagesModel.getSender_id() == user_id) {
                sendDataToActivity(messagesModel, 4);
                bottomSheetDialog.dismiss();
            }
            bottomSheetDialog.dismiss();
        });

        btn_restore.setOnClickListener(view14 -> {
            if (messagesModel.getSender_id() == user_id) {
                sendDataToActivity(messagesModel, 5);
                bottomSheetDialog.dismiss();
            }
            bottomSheetDialog.dismiss();
        });
        btn_delete.setOnClickListener(view14 -> {
            sendDataToActivity(messagesModel, 6);
            bottomSheetDialog.dismiss();
        });

        btn_download.setOnClickListener(view15 -> {
            if (messagesModel.getType_message() == 1 || messagesModel.getType_message() == 2 || messagesModel.getType_message() == 3 || messagesModel.getType_message() == 4) {
                sendDataToActivity(messagesModel, 7);
                bottomSheetDialog.dismiss();
            }
        });

        return bottomSheetDialog;
    }


    private void sendDataToActivity(MessagesModel messagesModel, int action) {
        Intent intent = new Intent("send_data_bottomsheet");
        Bundle bundle = new Bundle();
        bundle.putSerializable("object_messages", messagesModel);
        bundle.putSerializable("action", action);
        intent.putExtras(bundle);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
