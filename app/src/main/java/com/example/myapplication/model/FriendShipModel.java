package com.example.myapplication.model;

import java.io.Serializable;

public class FriendShipModel implements Serializable {
    private int friend_ship_id, sender_id, receiver_id;
    private String time, full_name, profie_image;
    private boolean is_friend;

    public FriendShipModel() {
    }

    public FriendShipModel(int friend_ship_id, int sender_id, int receiver_id, String time, String full_name, String profie_image, boolean is_friend) {
        this.friend_ship_id = friend_ship_id;
        this.sender_id = sender_id;
        this.receiver_id = receiver_id;
        this.time = time;
        this.full_name = full_name;
        this.profie_image = profie_image;
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

    public String getProfie_image() {
        return profie_image;
    }

    public void setProfie_image(String profie_image) {
        this.profie_image = profie_image;
    }

    public boolean isIs_friend() {
        return is_friend;
    }

    public void setIs_friend(boolean is_friend) {
        this.is_friend = is_friend;
    }
}
