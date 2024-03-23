package com.example.myapplication.activity;


import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;

import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;

import com.example.myapplication.R;
import com.example.myapplication.bottomsheet.BottomSheetEditProfile;
import com.example.myapplication.bottomsheet.BottomSheetUnFriend;
import com.example.myapplication.helper.FirebaseUploaderProfile;
import com.example.myapplication.helper.Helper;
import com.example.myapplication.server.Url_Api;
import com.example.myapplication.sharedpreferencesmanager.SharedPreferencesManager;

import com.makeramen.roundedimageview.RoundedImageView;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class EditMyProfileActivity extends AppCompatActivity {
    private int user_id;
    private SharedPreferencesManager sharedPreferencesManager;
    private TextView txt_description, txt_name, txt_dob, txt_sex, txt_address;
    private LinearLayout btn_edit_description, btn_edit_dob, btn_edit_sex, btn_edit_address;
    private ImageView btn_edit_name, img_cover, btn_back;
    private RoundedImageView img_avatar;
    private LinearLayout btn_edit_cover_avatar, btn_edit_avatar;
    private String full_name, dob, description, address, profile_image, cover_avatar;
    private int sex;
    private int type_change_image = -1;


    private final BroadcastReceiver broadcastReceiverAdapter = new BroadcastReceiver() {
        @SuppressLint("NotifyDataSetChanged")
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle == null) {
                return;
            }
            boolean is_load = bundle.getBoolean("is_load");
            if (is_load) {
                getProfileUser(user_id);
            }

        }
    };
    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK) {
            Intent data = result.getData();
            if (data != null) {
                Uri selectedImageUri = data.getData();
                FirebaseUploaderProfile.uploadFile(EditMyProfileActivity.this, EditMyProfileActivity.this, selectedImageUri, type_change_image);
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_my_profile);

        Helper.changeStatusBarColor(this, R.color.white);
        Helper.changeNavigationColor(this, R.color.white, true);

        sharedPreferencesManager = new SharedPreferencesManager(this);
        user_id = sharedPreferencesManager.getUserId();

        txt_description = findViewById(R.id.txt_description);
        btn_edit_description = findViewById(R.id.btn_edit_description);
        txt_name = findViewById(R.id.txt_name);
        btn_edit_name = findViewById(R.id.btn_edit_name);
        txt_dob = findViewById(R.id.txt_dob);
        btn_edit_dob = findViewById(R.id.btn_edit_dob);
        txt_sex = findViewById(R.id.txt_sex);
        btn_edit_sex = findViewById(R.id.btn_edit_sex);
        txt_address = findViewById(R.id.txt_address);
        btn_edit_address = findViewById(R.id.btn_edit_address);

        btn_back = findViewById(R.id.btn_back);
        img_cover = findViewById(R.id.img_cover);
        img_avatar = findViewById(R.id.img_avatar);
        btn_edit_cover_avatar = findViewById(R.id.btn_edit_cover_avatar);
        btn_edit_avatar = findViewById(R.id.btn_edit_avatar);

        btn_back.setOnClickListener(view -> finish());

        img_avatar.setOnClickListener(view -> {
            Intent intent = new Intent(EditMyProfileActivity.this, ViewMediaActivity.class);
            intent.putExtra("url_media", profile_image);
            startActivity(intent);
        });

        img_cover.setOnClickListener(view -> {
            Intent intent = new Intent(EditMyProfileActivity.this, ViewMediaActivity.class);
            intent.putExtra("url_media", cover_avatar);
            startActivity(intent);
        });


//        note
//                anh bia =0
//                        avt = 1
//                                tieu su = 2
//                                        ho ten = 3
//                                                ngay sinh = 4
//                                                        gioi tinh = 5
//                                                                dia chi = 6
        // Trong hàm onClick:
        btn_edit_cover_avatar.setOnClickListener(view -> {
            type_change_image = 0;
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            activityResultLauncher.launch(intent);
        });
        btn_edit_avatar.setOnClickListener(view -> {
            type_change_image = 1;
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            activityResultLauncher.launch(intent);
        });


        btn_edit_description.setOnClickListener(view -> openBottomSheet(2, description));
        btn_edit_name.setOnClickListener(view -> openBottomSheet(3, full_name));
        btn_edit_dob.setOnClickListener(view -> openBottomSheet(4, dob));
        btn_edit_sex.setOnClickListener(view -> openBottomSheet(5, String.valueOf(sex)));
        btn_edit_address.setOnClickListener(view -> openBottomSheet(6, address));
    }

    private void openBottomSheet(int action, String data) {
        BottomSheetEditProfile bottomSheetEditProfile = new BottomSheetEditProfile(EditMyProfileActivity.this, EditMyProfileActivity.this, user_id, action, data);
        bottomSheetEditProfile.show(getSupportFragmentManager(), bottomSheetEditProfile.getTag());
    }

    private void getProfileUser(int user_id) {

        OkHttpClient client = new OkHttpClient();

        String url = Url_Api.getInstance().getProfile(user_id);
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

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
                                JSONObject dataObject = jsonObject.getJSONObject("data");

                                full_name = dataObject.getString("full_name");
                                dob = dataObject.getString("dob");
                                description = dataObject.getString("description");
                                address = dataObject.getString("address");
                                profile_image = dataObject.getString("profile_image");
                                cover_avatar = dataObject.getString("cover_avatar");
                                sex = dataObject.getInt("sex");
                                runOnUiThread(new Runnable() {
                                    @SuppressLint("SetTextI18n")
                                    @Override
                                    public void run() {
                                        txt_name.setText(full_name);
                                        txt_dob.setText(convertTime(dob));
                                        txt_description.setText(description);
                                        txt_address.setText(address);
                                        Glide.with(EditMyProfileActivity.this).load(profile_image).into(img_avatar);
                                        Glide.with(EditMyProfileActivity.this).load(cover_avatar).into(img_cover);

                                        if (sex == -1) {
                                            txt_sex.setText("Không tiết lộ");
                                        } else if (sex == 1) {
                                            txt_sex.setText("Nữ");
                                        } else {
                                            txt_sex.setText("Nam");
                                        }
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

    private String convertTime(String time) {

        DateTimeFormatter formatter = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        }

        // Chuyển đổi thành LocalDateTime
        LocalDateTime dateTime = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            dateTime = LocalDateTime.parse(time, formatter);
        }

        // Định dạng lại thành "dd-MM-yyyy"
        DateTimeFormatter outputFormatter = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            outputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        }
        String formattedTime = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            formattedTime = dateTime.format(outputFormatter);
        }
        return formattedTime;
    }

    @Override
    protected void onResume() {
        super.onResume();
        getProfileUser(user_id);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiverAdapter, new IntentFilter("is_load_info"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiverAdapter);
    }
}