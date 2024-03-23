package com.example.myapplication.model;

import java.io.Serializable;

public class MessagesListModel implements Serializable {
    private int message_list_id, sender_id, receiver_id, type_message;
    private String last_content, time, receiver_avatar, receiver_name;
    private boolean receiver_is_online, is_seen;

    public MessagesListModel() {
    }

    public MessagesListModel(int message_list_id, int sender_id, int receiver_id, int type_message, String last_content, String time, String receiver_avatar, String receiver_name, boolean receiver_is_online, boolean is_seen) {
        this.message_list_id = message_list_id;
        this.sender_id = sender_id;
        this.receiver_id = receiver_id;
        this.type_message = type_message;
        this.last_content = last_content;
        this.time = time;
        this.receiver_avatar = receiver_avatar;
        this.receiver_name = receiver_name;
        this.receiver_is_online = receiver_is_online;
        this.is_seen = is_seen;
    }

    public int getMessage_list_id() {
        return message_list_id;
    }

    public void setMessage_list_id(int message_list_id) {
        this.message_list_id = message_list_id;
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

    public int getType_message() {
        return type_message;
    }

    public void setType_message(int type_message) {
        this.type_message = type_message;
    }

    public String getLast_content() {
        return last_content;
    }

    public void setLast_content(String last_content) {
        this.last_content = last_content;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getReceiver_avatar() {
        return receiver_avatar;
    }

    public void setReceiver_avatar(String receiver_avatar) {
        this.receiver_avatar = receiver_avatar;
    }

    public String getReceiver_name() {
        return receiver_name;
    }

    public void setReceiver_name(String receiver_name) {
        this.receiver_name = receiver_name;
    }

    public boolean isReceiver_is_online() {
        return receiver_is_online;
    }

    public void setReceiver_is_online(boolean receiver_is_online) {
        this.receiver_is_online = receiver_is_online;
    }

    public boolean isIs_seen() {
        return is_seen;
    }

    public void setIs_seen(boolean is_seen) {
        this.is_seen = is_seen;
    }
}
