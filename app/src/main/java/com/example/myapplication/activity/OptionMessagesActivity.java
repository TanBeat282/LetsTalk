package com.example.myapplication.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.helper.Helper;
import com.example.myapplication.model.MessagesListModel;
import com.example.myapplication.sharedpreferencesmanager.SharedPreferencesManager;
import com.makeramen.roundedimageview.RoundedImageView;

public class OptionMessagesActivity extends AppCompatActivity {
    private MessagesListModel messageListModel;
    private RoundedImageView img_avatar;
    private TextView txt_name, txt_name_cover;
    private ImageView btn_back_message;
    private LinearLayout btn_search_messages, btn_all_media, btn_view_profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option_messages);

        //get user_id
        SharedPreferencesManager sharedPreferences = new SharedPreferencesManager(getApplicationContext());
        int user_id = sharedPreferences.getUserId();

        img_avatar = findViewById(R.id.img_avatar);
        txt_name = findViewById(R.id.txt_name);
        txt_name_cover = findViewById(R.id.txt_name_cover);
        btn_back_message = findViewById(R.id.btn_back_message);
        btn_search_messages = findViewById(R.id.btn_search_messages);
        btn_view_profile = findViewById(R.id.btn_view_profile);
        btn_all_media = findViewById(R.id.btn_all_media);

        //change color changeStatusBarColor and changeNavigationColor
        Helper.changeStatusBarColor(this, R.color.white);
        Helper.changeNavigationColor(this, R.color.white, true);

        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            return;
        }
        messageListModel = (MessagesListModel) bundle.get("messages_list");


        if (messageListModel != null) {
            Glide.with(this).load(messageListModel.getReceiver_avatar()).into(img_avatar);
            txt_name.setText(messageListModel.getReceiver_name());
            txt_name_cover.setText(messageListModel.getReceiver_name());
        }

        btn_back_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(R.anim.anim_in_right, R.anim.anim_out_right);
            }
        });
        btn_view_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(OptionMessagesActivity.this, ViewProfileActivity.class);
                intent.putExtra("userId", user_id == messageListModel.getSender_id() ? messageListModel.getReceiver_id() : messageListModel.getSender_id());
                startActivity(intent);
            }
        });

        btn_search_messages.setOnClickListener(view -> {
            Intent intent = new Intent(OptionMessagesActivity.this, SearchMessagesActivity.class);
            Bundle bundle1 = new Bundle();
            bundle1.putSerializable("messages_list", messageListModel);
            intent.putExtras(bundle1);
            startActivity(intent);
        });

        btn_all_media.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(OptionMessagesActivity.this, AllMediaActivity.class);
                Bundle bundle1 = new Bundle();
                bundle1.putSerializable("messages_list", messageListModel);
                intent.putExtras(bundle1);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.anim_in_right, R.anim.anim_out_right);
    }
}