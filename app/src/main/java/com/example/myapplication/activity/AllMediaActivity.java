package com.example.myapplication.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.example.myapplication.R;
import com.example.myapplication.adapter.AllMediaFragmentAdpater;
import com.example.myapplication.fragment.FileFragment;
import com.example.myapplication.fragment.ImageOrVideoFragment;
import com.example.myapplication.fragment.LinkFragment;
import com.example.myapplication.helper.Helper;
import com.example.myapplication.model.MessagesListModel;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class AllMediaActivity extends AppCompatActivity {
    private TabLayout tabLayout;
    private MessagesListModel messageListModel;
    private ImageView imgBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_media);


        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            return;
        }
        messageListModel = (MessagesListModel) bundle.get("messages_list");
        if (messageListModel != null) {
            SharedPreferences sharedPreferences = getSharedPreferences("messages_list", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("messages_list_id", messageListModel.getMessage_list_id());
            editor.apply();
        }

        tabLayout = findViewById(R.id.tablayout);
        imgBack = findViewById(R.id.imgBack);
        ViewPager2 viewPager = findViewById(R.id.viewPage);

        //change color changeStatusBarColor and changeNavigationColor
        Helper.changeStatusBarColor(this, R.color.white);
        Helper.changeNavigationColor(this, R.color.white, true);

        // Sử dụng ContextCompat.getColor() để lấy màu từ tài nguyên màu sắc
        int selectedColor = ContextCompat.getColor(this, R.color.text_color);
        int normalColor = ContextCompat.getColor(this, R.color.gray_text);

        // Đặt màu cho selectedTabIndicatorColor
        tabLayout.setSelectedTabIndicatorColor(selectedColor);

        // Đặt màu cho văn bản của các tab
        tabLayout.setTabTextColors(normalColor, selectedColor);

        AllMediaFragmentAdpater adapter = new AllMediaFragmentAdpater(getSupportFragmentManager(), getLifecycle());
        adapter.addFragment(new ImageOrVideoFragment(), "Ảnh và video");
        adapter.addFragment(new FileFragment(), "Tệp tin");
        adapter.addFragment(new LinkFragment(), "Liên kết");
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            // Đặt tiêu đề cho từng tab
            tab.setText(adapter.getFragmentTitle(position));
        }).attach();

        // Lắng nghe sự kiện chọn tab
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Thay đổi màu văn bản của tab được chọn
                tabLayout.setTabTextColors(normalColor, selectedColor);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Không cần xử lý ở đây
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Không cần xử lý ở đây
            }
        });

        imgBack.setOnClickListener(view -> finish());
    }

}
