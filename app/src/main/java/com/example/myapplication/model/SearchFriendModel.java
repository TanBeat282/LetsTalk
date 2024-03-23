package com.example.myapplication.model;

import java.io.Serializable;

public class SearchFriendModel implements Serializable {
    private int friend_ship_id, sender_id, receiver_id, user_id;
    private String time, full_name, profile_image;
    private int is_friend;

    public SearchFriendModel() {

    }

    public SearchFriendModel(int friend_ship_id, int sender_id, int receiver_id, int user_id, String time, String full_name, String profile_image, int is_friend) {
        this.friend_ship_id = friend_ship_id;
        this.sender_id = sender_id;
        this.receiver_id = receiver_id;
        this.user_id = user_id;
        this.time = time;
        this.full_name = full_name;
        this.profile_image = profile_image;
        this.is_friend = is_friend;
    }

    public int getFriend_ship_id() {
        return friend_ship_id;
    }

    public void setFriend_ship_id(int friend_ship_id) {
        this.friend_ship_id = friend_ship_id;
    }

    public int getSender_id() {
        return sender_id;
    }

    public void setSender_id(int sender_id) {
        this.sender_id = sender_id;
    }

    public int getReceiver_id() {
        return receiver_id;
    }

    public void setReceiver_id(int receiver_id) {
        this.receiver_id = receiver_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getProfile_image() {
        return profile_image;
    }

    public void setProfile_image(String profile_image) {
        this.profile_image = profile_image;
    }

    public int isIs_friend() {
        return is_friend;
    }

    public void setIs_friend(int is_friend) {
        this.is_friend = is_friend;
    }
}

