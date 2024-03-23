package com.example.myapplication.model;

import java.io.Serializable;

public class SearchMessagesModel implements Serializable {
    public int messages_id;
    public int sender_id;
    public String content;
    public String time;
    public int type_message;
    public String full_name;
    public String profile_image;

    public SearchMessagesModel() {
    }

    public SearchMessagesModel(int messages_id, int sender_id, String content, String time, int type_message, String full_name, String profile_image) {
        this.messages_id = messages_id;
        this.sender_id = sender_id;
        this.content = content;
        this.time = time;
        this.type_message = type_message;
        this.full_name = full_name;
        this.profile_image = profile_image;
    }

    public int getMessages_id() {
        return messages_id;
    }

    public void setMessages_id(int messages_id) {
        this.messages_id = messages_id;
    }

    public int getSender_id() {
        return sender_id;
    }

    public void setSender_id(int sender_id) {
        this.sender_id = sender_id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getType_message() {
        return type_message;
    }

    public void setType_message(int type_message) {
        this.type_message = type_message;
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
}
