package com.example.myapplication.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.activity.LoginActivity;
import com.example.myapplication.activity.MessageListActivity;
import com.example.myapplication.activity.NotificationActivity;
import com.example.myapplication.activity.SearchActivity;
import com.example.myapplication.activity.ViewMediaActivity;
import com.example.myapplication.activity.ViewProfileActivity;
import com.example.myapplication.adapter.PostAdapter;
import com.example.myapplication.model.ImagePostModel;
import com.example.myapplication.model.PostModel;
import com.example.myapplication.server.Url_Api;
import com.example.myapplication.sharedpreferencesmanager.SharedPreferencesManager;
import com.makeramen.roundedimageview.RoundedImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class HomeFragment extends Fragment {

    private int user_id;
    private SharedPreferencesManager sharedPreferencesManager;
    private TextView txtName, btn_add_friend;
    private RoundedImageView img_avatar;
    private SwipeRefreshLayout swipe_refesh;
    private ArrayList<PostModel> postModelArrayList = new ArrayList<>();
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private LinearLayout linear_no_post;

    private final BroadcastReceiver broadcastReceiverDeletePost = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle == null) {
                return;
            }
            boolean refesh = bundle.getBoolean("refesh");
            int position = bundle.getInt("position");
            if (refesh) {
                postModelArrayList.remove(position);
                postAdapter.notifyItemRemoved(position);
                postAdapter.notifyItemRangeChanged(position, postModelArrayList.size());
                Toast.makeText(requireContext(), "Đã xóa bài viết thành công.", Toast.LENGTH_SHORT).show();
            }

        }
    };

    private final BroadcastReceiver broadcastReceiverNewPost = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle == null) {
                return;
            }
            PostModel postModel = (PostModel) bundle.getSerializable("postModel");
            if (postModel != null) {
                if (postAdapter == null) {
                    // Nếu postAdapter chưa được khởi tạo, hãy khởi tạo nó ở đây
                    postAdapter = new PostAdapter(requireContext(), postModelArrayList, user_id, requireActivity());
                    recyclerView.setAdapter(postAdapter);
                }

                postModelArrayList.add(0, postModel); // Add at the beginning
                postAdapter.notifyItemInserted(0); // Notify adapter about the insertion
                recyclerView.scrollToPosition(0); // Scroll to the top
                Toast.makeText(context, "Đăng bài viết thành công", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        swipe_refesh = view.findViewById(R.id.swipe_refesh);
        ImageView linear_message = view.findViewById(R.id.linear_message);
        ImageView btn_notification = view.findViewById(R.id.btn_notification);
        ImageView btn_search = view.findViewById(R.id.btn_search);
        txtName = view.findViewById(R.id.txtName);
        img_avatar = view.findViewById(R.id.img_avatar);
        recyclerView = view.findViewById(R.id.recycler_view);
        linear_no_post = view.findViewById(R.id.linear_no_post);
        btn_add_friend = view.findViewById(R.id.btn_add_friend);

        sharedPreferencesManager = new SharedPreferencesManager(requireContext());
        user_id = sharedPreferencesManager.getUserId();


        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(layoutManager);

        linear_message.setOnClickListener(view1 -> startActivity(new Intent(requireContext(), MessageListActivity.class)));
        btn_notification.setOnClickListener(view12 -> startActivity(new Intent(requireContext(), NotificationActivity.class)));
        img_avatar.setOnClickListener(view13 -> startActivity(new Intent(requireContext(), ViewProfileActivity.class)));
        btn_search.setOnClickListener(view14 -> startActivity(new Intent(requireContext(), SearchActivity.class)));

        if (user_id != 0) {
            getInfo(user_id);
            getPost(user_id);
        }

        swipe_refesh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
            postModelArrayList.clear();
            getPost(user_id);
        }, 1000));
        btn_add_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(requireContext(), SearchActivity.class));
            }
        });

    }

    private void getPost(int user_id) {
        OkHttpClient client = new OkHttpClient();

        // Tạo request để gửi lời gọi HTTP GET đến URL
        String url = Url_Api.getInstance().getPostNewFeed(user_id);
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @SuppressLint("NotifyDataSetChanged")
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
                                JSONArray dataArray = jsonObject.getJSONArray("data");
                                for (int i = 0; i < dataArray.length(); i++) {
                                    JSONObject messageObject = dataArray.getJSONObject(i);

                                    PostModel postModel = new PostModel();

                                    postModel.setPost_id(messageObject.getInt("post_id"));
                                    postModel.setUser_id(messageObject.getInt("user_id"));
                                    postModel.setContent(messageObject.getString("content"));
                                    postModel.setTime(messageObject.getString("time"));
                                    postModel.setHeart_count(messageObject.getString("heart_count"));
                                    postModel.setComment_count(messageObject.getInt("comment_count"));
                                    postModel.setIs_save(messageObject.getInt("is_save_post") == 1);

                                    // Thay đổi cách lấy dữ liệu từ JSON cho imagePostModelList
                                    JSONArray imageArray = messageObject.getJSONArray("image");
                                    List<ImagePostModel> imagePostModelList = new ArrayList<>();
                                    for (int j = 0; j < imageArray.length(); j++) {
                                        JSONObject imageObject = imageArray.getJSONObject(j);
                                        ImagePostModel imagePostModel = new ImagePostModel();
                                        imagePostModel.setImage_id(imageObject.getInt("image_id"));
                                        imagePostModel.setPost_id(imageObject.getInt("post_id"));
                                        imagePostModel.setImage(imageObject.getString("image"));
                                        imagePostModelList.add(imagePostModel);
                                    }
                                    postModel.setImagePostModelList(imagePostModelList);
                                    postModel.setFull_name(messageObject.getString("full_name"));
                                    postModel.setProfile_image(messageObject.getString("profile_image"));
                                    postModelArrayList.add(postModel);
                                }
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(() -> {
                                        postAdapter = new PostAdapter(requireContext(), postModelArrayList, user_id, requireActivity());
                                        recyclerView.setAdapter(postAdapter);
                                        postAdapter.notifyDataSetChanged();
                                        swipe_refesh.setRefreshing(false);
                                        if (postModelArrayList.size() > 0) {
                                            linear_no_post.setVisibility(View.GONE);
                                            recyclerView.setVisibility(View.VISIBLE);
                                        } else {
                                            linear_no_post.setVisibility(View.VISIBLE);
                                            recyclerView.setVisibility(View.GONE);
                                        }
                                    });
                                }


                            } else {
                                if (postModelArrayList.size() > 0) {
                                    linear_no_post.setVisibility(View.GONE);
                                    recyclerView.setVisibility(View.VISIBLE);
                                } else {
                                    linear_no_post.setVisibility(View.VISIBLE);
                                    recyclerView.setVisibility(View.GONE);
                                }
                                swipe_refesh.setRefreshing(false);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }


    private void getInfo(int user_id) {
        OkHttpClient client = new OkHttpClient();

        String url = Url_Api.getInstance().getInfo(user_id);
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
                if (!isAdded()) {
                    // Fragment đã bị detach khỏi activity, không thực hiện các hoạt động liên quan đến giao diện người dùng
                    return;
                }

                if (response.isSuccessful()) {
                    ResponseBody responseBody = response.body();
                    if (responseBody != null) {
                        String jsonString = responseBody.string();
                        try {
                            JSONObject jsonObject = new JSONObject(jsonString);
                            boolean status = jsonObject.getBoolean("status");
                            if (status) {
                                JSONObject dataObject = jsonObject.getJSONObject("data");

                                requireActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        // Thực hiện các hoạt động liên quan đến giao diện người dùng trên luồng chính
                                        if (isAdded()) {
                                            try {
                                                txtName.setText(dataObject.getString("full_name"));
                                            } catch (JSONException e) {
                                                throw new RuntimeException(e);
                                            }
                                            try {
                                                Glide.with(requireContext()).load(dataObject.getString("profile_image")).into(img_avatar);
                                            } catch (JSONException e) {
                                                throw new RuntimeException(e);
                                            }
                                        }
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


    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(broadcastReceiverDeletePost, new IntentFilter("refesh_post"));
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(broadcastReceiverNewPost, new IntentFilter("new_post_model"));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(broadcastReceiverDeletePost);
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(broadcastReceiverNewPost);
    }
}