package com.example.myapplication.bottomsheet;


import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.myapplication.R;
import com.example.myapplication.activity.SplashScreen;
import com.example.myapplication.sharedpreferencesmanager.SharedPreferencesManager;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;


public class BottomSheetLogOut extends BottomSheetDialogFragment {
    private final Context context;
    private BottomSheetDialog bottomSheetDialog;

    public BottomSheetLogOut(Context context) {
        this.context = context;
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        bottomSheetDialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_selected, null);
        bottomSheetDialog.setContentView(view);

        LinearLayout linear_ok = view.findViewById(R.id.linear_ok);
        LinearLayout linear_cancel = view.findViewById(R.id.linear_cancel);
        TextView txt_Title = view.findViewById(R.id.txt_Title);
        TextView txt_TextButton = view.findViewById(R.id.txt_TextButton);

        txt_Title.setText("Bạn chắc chắn muốn đăng xuất khỏi LetsTalk không?");
        txt_TextButton.setText("Đăng xuất");

        Drawable drawable = ContextCompat.getDrawable(context, R.drawable.round_logout);
        linear_ok.setBackground(drawable);

        linear_ok.setOnClickListener(view1 -> clearInfo());
        linear_cancel.setOnClickListener(v -> bottomSheetDialog.dismiss());


        return bottomSheetDialog;
    }

    private void clearInfo() {
        SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(getContext());
        sharedPreferencesManager.clearUserInfo();
        sendDataToActivity(true);
        startActivity(new Intent(requireContext(), SplashScreen.class));

    }

    private void sendDataToActivity(boolean finish) {
        Intent intent = new Intent("finish");
        Bundle bundle = new Bundle();
        bundle.putSerializable("finish", finish);
        intent.putExtras(bundle);

        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent);
    }
}
