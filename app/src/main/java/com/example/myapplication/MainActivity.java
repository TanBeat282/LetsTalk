package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.example.myapplication.activity.PostActivity;
import com.example.myapplication.activity.SplashScreen;
import com.example.myapplication.bottomsheet.BottomSheetCreatePost;
import com.example.myapplication.bottomsheet.BottomSheetOptionPost;
import com.example.myapplication.fragment.FriendShipFragment;
import com.example.myapplication.fragment.HomeFragment;
import com.example.myapplication.fragment.MapFragment;
import com.example.myapplication.fragment.MessageFragment;
import com.example.myapplication.fragment.ProfileFragment;
import com.example.myapplication.helper.Helper;
import com.example.myapplication.server.WebSocketService;
import com.example.myapplication.sharedpreferencesmanager.SharedPreferencesManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {
    private FloatingActionButton floatingActionButton;
    private SharedPreferencesManager sharedPreferences;
    private String token;

    private final BroadcastReceiver broadcastReceiverAdapter = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle == null) {
                return;
            }
            boolean finish = bundle.getBoolean("finish");
            if (finish) {
                finish();
            }

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        floatingActionButton = findViewById(R.id.addPost);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, PostActivity.class);
//                startActivity(intent);
                BottomSheetCreatePost bottomSheetCreatePost = new BottomSheetCreatePost(MainActivity.this, MainActivity.this);
                bottomSheetCreatePost.show(getSupportFragmentManager(), bottomSheetCreatePost.getTag());
            }
        });

        Helper.changeStatusBarColor(this, R.color.white);
        Helper.changeNavigationColor(this, R.color.gray1, true);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        replaceFragment(new HomeFragment());
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.action_home) {
                replaceFragment(new HomeFragment());
            } else if (item.getItemId() == R.id.action_location) {
                replaceFragment(new MapFragment());
            } else if (item.getItemId() == R.id.action_message) {
                replaceFragment(new FriendShipFragment());
            } else {
                replaceFragment(new ProfileFragment());
            }
            item.setChecked(true);
            return false;
        });
        sharedPreferences = new SharedPreferencesManager(getApplicationContext());
        getToken();

    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    private void getToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(">>>>>>>>>>>>>>>>", "Fetching FCM registration token failed", task.getException());
                            return;
                        }
                        token = task.getResult();
                        Intent serviceIntent = new Intent(MainActivity.this, WebSocketService.class);
                        serviceIntent.putExtra("user_id", sharedPreferences.getUserId());
                        serviceIntent.putExtra("token", token);
                        startService(serviceIntent);
                    }
                });

    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiverAdapter, new IntentFilter("finish"));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiverAdapter);
    }

}