package com.example.myapplication.activity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.helper.Helper;
import com.example.myapplication.server.WebSocketService;
import com.example.myapplication.sharedpreferencesmanager.SharedPreferencesManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

@SuppressLint("CustomSplashScreen")
public class SplashScreen extends AppCompatActivity {
    private SharedPreferencesManager sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        //change color changeStatusBarColor and changeNavigationColor
        Helper.changeStatusBarColor(this, R.color.white);
        Helper.changeNavigationColor(this, R.color.white, true);

        sharedPreferences = new SharedPreferencesManager(getApplicationContext());


        int SP_TIME_OUT = 3000;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent splashIntent;

                if (isConnectedToInternet()) {
                    if (sharedPreferences.getIsLogin()) {

                        splashIntent = new Intent(SplashScreen.this, MainActivity.class);
                    } else {
                        splashIntent = new Intent(SplashScreen.this, LoginActivity.class);
                    }
                } else {
                    showNoInternetDialog();
                    return;
                }

                startActivity(splashIntent);
                finish();
            }
        }, SP_TIME_OUT);
    }


    private boolean isConnectedToInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        return false;
    }

    private void showNoInternetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Không có kết nối internet. Vui lòng kiểm tra kết nối và thử lại.")
                .setPositiveButton("OK", (dialog, id) -> finishAffinity());
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
