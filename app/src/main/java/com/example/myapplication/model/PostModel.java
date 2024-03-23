package com.example.myapplication.model;

import com.example.myapplication.model.User;
import com.google.gson.annotations.SerializedName;

import org.w3c.dom.Comment;

import java.io.Serializable;
import java.util.List;

public class PostModel implements Serializable {
    private int post_id;
    private int user_id;
    private String content;
    private String time;
    private String heart_count;
    private int comment_count;
    private boolean is_save;
    private List<ImagePostModel> imagePostModelList;  // Thay đổi kiểu dữ liệu
    private String full_name;
    private String profile_image;

    public PostModel() {
    }

    public PostModel(int post_id, int user_id, String content, String time, String heart_count, int comment_count, boolean is_save, List<ImagePostModel> imagePostModelList, String full_name, String profile_image) {
        this.post_id = post_id;
        this.user_id = user_id;
        this.content = content;
        this.time = time;
        this.heart_count = heart_count;
        this.comment_count = comment_count;
        this.is_save = is_save;
        this.imagePostModelList = imagePostModelList;
        this.full_name = full_name;
        this.profile_image = profile_image;
    }

    public int getPost_id() {
        return post_id;
    }

    public void setPost_id(int post_id) {
        this.post_id = post_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
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

    public String getHeart_count() {
        return heart_count;
    }

    public void setHeart_count(String heart_count) {
        this.heart_count = heart_count;
    }

    public int getComment_count() {
        return comment_count;
    }

    public void setComment_count(int comment_count) {
        this.comment_count = comment_count;
    }

    public boolean isIs_save() {
        return is_save;
    }

    public void setIs_save(boolean is_save) {
        this.is_save = is_save;
    }

    public List<ImagePostModel> getImagePostModelList() {
        return imagePostModelList;
    }

    public void setImagePostModelList(List<ImagePostModel> imagePostModelList) {
        this.imagePostModelList = imagePostModelList;
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
