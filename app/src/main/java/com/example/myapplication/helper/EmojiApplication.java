package com.example.myapplication.helper;

import android.app.Application;

import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.facebook.FacebookEmojiProvider;

public class EmojiApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //Install emoji manager
        EmojiManager.install(new FacebookEmojiProvider());
    }
}