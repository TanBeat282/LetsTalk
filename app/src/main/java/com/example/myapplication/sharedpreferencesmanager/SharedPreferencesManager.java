package com.example.myapplication.sharedpreferencesmanager;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesManager {
    private final Context context;

    public SharedPreferencesManager(Context context) {
        this.context = context;
    }

    public void saveUserId(int user_id) {
        if (user_id != 0) {
            SharedPreferences sharedPreferences = context.getSharedPreferences("user_info", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("user_id", user_id);
            editor.apply();
        }
    }

    public int getUserId() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("user_info", Context.MODE_PRIVATE);
        return sharedPreferences.getInt("user_id", 0);
    }

    public void saveIsLogin(boolean is_login) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("user_info", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("is_login", is_login);
        editor.apply();
    }

    public boolean getIsLogin() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("user_info", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("is_login", false);
    }
    public void clearUserInfo() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("user_info", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

}
