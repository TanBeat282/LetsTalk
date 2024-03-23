package com.example.myapplication.model;

import java.io.Serializable;

public class ImagePostModel implements Serializable {
    private int image_id;
    private int post_id;
    private String image;

    public ImagePostModel() {
    }

    public ImagePostModel(int image_id, int post_id, String image) {
        this.image_id = image_id;
        this.post_id = post_id;
        this.image = image;
    }

    public int getImage_id() {
        return image_id;
    }

    public void setImage_id(int image_id) {
        this.image_id = image_id;
    }

    public int getPost_id() {
        return post_id;
    }

    public void setPost_id(int post_id) {
        this.post_id = post_id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
