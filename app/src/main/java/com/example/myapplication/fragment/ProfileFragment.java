package com.example.myapplication.fragment;

;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.activity.EditProfileActivity;
import com.example.myapplication.activity.FriendListActivity;
import com.example.myapplication.activity.MessageListActivity;
import com.example.myapplication.activity.MyProfileActivity;
import com.example.myapplication.activity.PostSaveActivity;
import com.example.myapplication.activity.SplashScreen;
import com.example.myapplication.activity.ViewProfileActivity;
import com.example.myapplication.bottomsheet.BottomSheetLogOut;
import com.example.myapplication.model.FriendShipModel;
import com.example.myapplication.server.Url_Api;
import com.example.myapplication.sharedpreferencesmanager.SharedPreferencesManager;
import com.makeramen.roundedimageview.RoundedImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ProfileFragment extends Fragment {

    private TextView txt_name;
    private RoundedImageView img_avatar;
    private LinearLayout btn_edit_profile;
    private RelativeLayout btn_messages, btn_friend, btn_post_save, btn_LogOut;
    private int users_id;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        txt_name = view.findViewById(R.id.txt_name);
        img_avatar = view.findViewById(R.id.img_avatar);
        btn_edit_profile = view.findViewById(R.id.btn_edit_profile);


        btn_messages = view.findViewById(R.id.btn_messages);
        btn_friend = view.findViewById(R.id.btn_friend);
        btn_post_save = view.findViewById(R.id.btn_post_save);
        btn_LogOut = view.findViewById(R.id.btn_LogOut);

        SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(getContext());
        users_id = sharedPreferencesManager.getUserId();

        btn_edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), ViewProfileActivity.class));
            }
        });
        btn_messages.setOnClickListener(view1 -> startActivity(new Intent(requireContext(), MessageListActivity.class)));
        btn_friend.setOnClickListener(view1 -> startActivity(new Intent(requireContext(), FriendListActivity.class)));
        btn_post_save.setOnClickListener(view1 -> startActivity(new Intent(requireContext(), PostSaveActivity.class)));

        btn_LogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BottomSheetLogOut bottomSheetLogOut = new BottomSheetLogOut(requireContext());
                bottomSheetLogOut.show(requireActivity().getSupportFragmentManager(), bottomSheetLogOut.getTag());
            }
        });
    }


    private void getProfile(int user_id) {
        OkHttpClient client = new OkHttpClient();

        String url = Url_Api.getInstance().getProfile(user_id);
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    ResponseBody responseBody = response.body();
                    if (responseBody != null) {
                        String jsonString = responseBody.string();
                        try {
                            JSONObject jsonObject = new JSONObject(jsonString);
                            boolean status = jsonObject.getBoolean("status");
                            if (status) {
                                JSONObject dataObject = jsonObject.getJSONObject("data");
                                String full_name = dataObject.getString("full_name");
                                String profile_image = dataObject.getString("profile_image");

                                requireActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        txt_name.setText(full_name);
                                        Glide.with(requireActivity()).load(profile_image).into(img_avatar);
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    private void sendDataToActivity(boolean finish) {
        Intent intent = new Intent("finish");
        Bundle bundle = new Bundle();
        bundle.putSerializable("finish", finish);
        intent.putExtras(bundle);

        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        getProfile(users_id);
    }
}