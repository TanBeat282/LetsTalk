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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.myapplication.R;
import com.example.myapplication.activity.ChangePassword1Activity;
import com.example.myapplication.model.MessagesListModel;
import com.example.myapplication.sharedpreferencesmanager.SharedPreferencesManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;


public class BottomSheetOptionProfile extends BottomSheetDialogFragment {
    private final Context context;
    private final Activity activity;
    private BottomSheetDialog bottomSheetDialog;
    public BottomSheetOptionProfile(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        bottomSheetDialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_option_profile, null);
        bottomSheetDialog.setContentView(view);

        LinearLayout btn_change_pass = view.findViewById(R.id.btn_change_pass);
        LinearLayout btn_block_account = view.findViewById(R.id.btn_block_account);
        LinearLayout btn_delete_account = view.findViewById(R.id.btn_delete_account);

        btn_change_pass.setOnClickListener(view1 -> {
            startActivity(new Intent(context, ChangePassword1Activity.class));
            bottomSheetDialog.dismiss();
        });

        return bottomSheetDialog;
    }
}
