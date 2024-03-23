package com.example.myapplication.Models;

public class UserModel {
    private int id;
    private String name;
    private String email;
    private String password;
    private String phone;
    private String date;
    private String address;
    private String imageUrl;

    public UserModel(int id, String name, String email, String password, String phone, String date, String address, String imageUrl) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.date = date;
        this.address = address;
        this.imageUrl = imageUrl;
        this.id = id;
    }

    public UserModel() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String location) {
        this.address = location;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String toString() {
        return "UserModel{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", mail='" + email + '\'' +
                ", password='" + password + '\'' +
                ", phoneNum='" + phone + '\'' +
                ", location=" + address +
                ", imgUrl='" + imageUrl + '\'' +
                '}';
    }
}
