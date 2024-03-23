package com.example.myapplication.server;

import com.example.myapplication.Models.UserModel;
import com.example.myapplication.model.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Call;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface Api_service {
    Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    Api_service apiService = new Retrofit.Builder()
            .baseUrl("http://192.168.1.221/duantotnghiep/src/api/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(Api_service.class);

    @FormUrlEncoded
    @POST("profile.php")
    Call<UserModel> updateProfile(@Field("name") String name,@Field("date") String date,@Field("phone") String phone,@Field("address") String address);
    @GET("get_profile.php")
    Call<UserModel> getUserProfile(@Query("users_id") Integer users_id);
}
