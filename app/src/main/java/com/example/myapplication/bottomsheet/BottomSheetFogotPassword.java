package com.example.myapplication.bottomsheet;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.myapplication.R;
import com.example.myapplication.activity.ForgotPass1Activity;
import com.example.myapplication.activity.ForgotPass2Activity;
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


public class BottomSheetFogotPassword extends BottomSheetDialogFragment {
    private final Context context;
    private final Activity activity;
    private int type;
    private BottomSheetDialog bottomSheetDialog;

    public BottomSheetFogotPassword(Activity activity, Context context, int type) {
        this.activity = activity;
        this.context = context;
        this.type = type;
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        bottomSheetDialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_fogotpass, null);
        bottomSheetDialog.setContentView(view);

        LinearLayout linear_ok = view.findViewById(R.id.linear_ok);
        LinearLayout linear_cancel = view.findViewById(R.id.linear_cancel);
        TextView txt_Title = view.findViewById(R.id.txt_Title);

        if (type == 1) {
            txt_Title.setText("Email không tồn tại. Hãy kiểm tra lại email?");
        } else {
            txt_Title.setText("Email đã tồn tại. Bạn quên mật khẩu?");
        }

        linear_ok.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            if (type == 0) {
                context.startActivity(new Intent(context, ForgotPass2Activity.class));
                activity.overridePendingTransition(R.anim.anim_in_left, R.anim.anim_out_left);
                activity.finish();
            }
        });
        linear_cancel.setOnClickListener(v -> bottomSheetDialog.dismiss());

        return bottomSheetDialog;
    }
}
