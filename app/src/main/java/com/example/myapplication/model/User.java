package com.example.myapplication.model;

public class User {
    private Integer user_id;
    private String full_name;
    private String dob;
    private String password;
    private String email;
    private String description;
    private String location;
    private String profile_image;
    private String joined_in;
    private boolean is_online;
    private String token;
    private String sex;

    public User() {
    }

    public User(Integer user_id, String full_name, String dob, String password, String email, String description, String location, String profile_image, String joined_in, boolean is_online, String token, String sex) {
        this.user_id = user_id;
        this.full_name = full_name;
        this.dob = dob;
        this.password = password;
        this.email = email;
        this.description = description;
        this.location = location;
        this.profile_image = profile_image;
        this.joined_in = joined_in;
        this.is_online = is_online;
        this.token = token;
        this.sex = sex;
    }

    public Integer getUser_id() {
        return user_id;
    }

    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getProfile_image() {
        return profile_image;
    }

    public void setProfile_image(String profile_image) {
        this.profile_image = profile_image;
    }

    public String getJoined_in() {
        return joined_in;
    }

    public void setJoined_in(String joined_in) {
        this.joined_in = joined_in;
    }

    public boolean isIs_online() {
        return is_online;
    }

    public void setIs_online(boolean is_online) {
        this.is_online = is_online;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }
}
