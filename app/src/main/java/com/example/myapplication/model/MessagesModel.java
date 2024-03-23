package com.example.myapplication.model;

import java.io.Serializable;

public class MessagesModel implements Serializable {
    private int messages_list_id, sender_id, messages_id, type_message;
    private String time, content;
    public MessagesModel() {
    }

    public MessagesModel(int messages_list_id, int sender_id, int messages_id, int type_message, String time, String content) {
        this.messages_list_id = messages_list_id;
        this.sender_id = sender_id;
        this.messages_id = messages_id;
        this.type_message = type_message;
        this.time = time;
        this.content = content;
    }

    public int getMessages_list_id() {
        return messages_list_id;
    }

    public void setMessages_list_id(int messages_list_id) {
        this.messages_list_id = messages_list_id;
    }

    public int getSender_id() {
        return sender_id;
    }

    public void setSender_id(int sender_id) {
        this.sender_id = sender_id;
    }

    public int getMessages_id() {
        return messages_id;
    }

    public void setMessages_id(int messages_id) {
        this.messages_id = messages_id;
    }

    public int getType_message() {
        return type_message;
    }

    public void setType_message(int type_message) {
        this.type_message = type_message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
