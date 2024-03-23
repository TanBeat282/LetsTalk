package com.example.myapplication.model;

import java.io.Serializable;

public class MediaItem implements Serializable {
    private String path;
    private boolean isVideo;

    public MediaItem(String path, boolean isVideo) {
        this.path = path;
        this.isVideo = isVideo;
    }

    public String getPath() {
        return path;
    }

    public boolean isVideo() {
        return isVideo;
    }
}
