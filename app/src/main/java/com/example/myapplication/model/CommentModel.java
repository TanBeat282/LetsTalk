package com.example.myapplication.model;

import java.io.Serializable;
import java.util.List;

public class CommentModel implements Serializable {
    private int comment_id;
    private int user_id;
    private int post_id;
    private String content;
    private String time;
    private String full_name;
    private String profile_image;

    public CommentModel() {
    }

    public CommentModel(int comment_id, int user_id, int post_id, String content, String time, String full_name, String profile_image) {
        this.comment_id = comment_id;
        this.user_id = user_id;
        this.post_id = post_id;
        this.content = content;
        this.time = time;
        this.full_name = full_name;
        this.profile_image = profile_image;
    }

    public int getComment_id() {
        return comment_id;
    }

    public void setComment_id(int comment_id) {
        this.comment_id = comment_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public int getPost_id() {
        return post_id;
    }

    public void setPost_id(int post_id) {
        this.post_id = post_id;
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
