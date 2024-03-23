package com.example.myapplication.bottomsheet;


import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.activity.PostActivity;
import com.example.myapplication.activity.ViewPostActivity;
import com.example.myapplication.adapter.MediaLibraryAdapter;
import com.example.myapplication.adapter.MediaPostAdapter;
import com.example.myapplication.model.ImagePostModel;
import com.example.myapplication.model.MessagesModel;
import com.example.myapplication.model.PostModel;
import com.example.myapplication.server.Url_Api;
import com.example.myapplication.sharedpreferencesmanager.SharedPreferencesManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class BottomSheetCreatePost extends BottomSheetDialogFragment {
    private final Context context;
    private final Activity activity;
    private BottomSheetDialog bottomSheetDialog;
    private int user_id;
    private ArrayList<Uri> arrayList;
    private LinearLayout btn_post;
    private RecyclerView rv_image;

    public BottomSheetCreatePost(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult o) {
            if (o.getResultCode() == RESULT_OK) {
                arrayList.clear();

                // Check if multiple images are selected
                if (o.getData() != null && o.getData().getClipData() != null) {
                    int count = o.getData().getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        Uri imageUri = o.getData().getClipData().getItemAt(i).getUri();
                        arrayList.add(imageUri);
                    }
                } else if (o.getData() != null && o.getData().getData() != null) {
                    // Check if a single image is selected
                    Uri singleImageUri = o.getData().getData();
                    arrayList.add(singleImageUri);
                }

                if (arrayList.size() == 0) {
                    rv_image.setVisibility(View.GONE);
                } else {
                    rv_image.setVisibility(View.VISIBLE);
                    MediaLibraryAdapter mediaLibraryAdapter = new MediaLibraryAdapter(context, activity, arrayList);
                    if (arrayList.size() >= 7) {
                        Uri imageUri = Uri.parse("https://firebasestorage.googleapis.com/v0/b/letstalk-3d1c5.appspot.com/o/file_default%2Fba_cham.jpg?alt=media&token=c1da26ab-25eb-4fc8-bc5e-532d7d4497b1");
                        arrayList.add(5, imageUri);
                    }
                    if (arrayList.size() >= 6) {
                        arrayList.subList(6, arrayList.size()).clear();
                    }
                    activity.runOnUiThread(() -> {
                        // Đặt LayoutManager dựa trên kích thước của imagePostModels
                        if (arrayList.size() == 2) {
                            rv_image.setLayoutManager(new GridLayoutManager(context, 2));
                        } else if (arrayList.size() == 3) {
                            rv_image.setLayoutManager(new GridLayoutManager(context, 3));
                        } else if (arrayList.size() == 4) {
                            rv_image.setLayoutManager(new GridLayoutManager(context, 2));
                        } else if (arrayList.size() >= 5) {
                            rv_image.setLayoutManager(new GridLayoutManager(context, 3));
                        } else {
                            rv_image.setLayoutManager(new GridLayoutManager(context, 1));
                        }
                        rv_image.setAdapter(mediaLibraryAdapter);
                        mediaLibraryAdapter.notifyDataSetChanged();
                    });
                }
            }
        }
    });


    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        bottomSheetDialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_create_post, null);
        bottomSheetDialog.setContentView(view);

        SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(context);
        user_id = sharedPreferencesManager.getUserId();

        ImageView btn_close = view.findViewById(R.id.btn_close);
        btn_post = view.findViewById(R.id.btn_post);
        EditText edt_input_message = view.findViewById(R.id.edt_input_message);
        rv_image = view.findViewById(R.id.rv_image);
        ImageView btn_image = view.findViewById(R.id.btn_image);

        FirebaseApp.initializeApp(context);
        arrayList = new ArrayList<>();

        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
            }
        });
        btn_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                activityResultLauncher.launch(intent);
            }
        });


        btn_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String content = edt_input_message.getText().toString().trim();
                content = content.replaceAll("\n", "[NEWLINE]");
                addPost(user_id, content);
                btn_post.setEnabled(false);
                btn_post.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.gray_text)));
            }
        });


        return bottomSheetDialog;

    }

    private void addPost(int user_id, String content) {
        OkHttpClient client = new OkHttpClient();

        String url = Url_Api.getInstance().addPost(user_id, content, getTime());
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
                                activity.runOnUiThread(() -> {
                                    try {
                                        PostModel postModel = new PostModel();

                                        postModel.setPost_id(dataObject.getInt("post_id"));
                                        postModel.setUser_id(dataObject.getInt("user_id"));
                                        postModel.setContent(dataObject.getString("content"));
                                        postModel.setTime(dataObject.getString("time"));
                                        postModel.setHeart_count(dataObject.getString("heart_count"));
                                        postModel.setComment_count(dataObject.getInt("comment_count"));
                                        postModel.setIs_save(dataObject.getInt("is_save_post") == 1);

                                        // Thay đổi cách lấy dữ liệu từ JSON cho imagePostModelList
                                        JSONArray imageArray = dataObject.getJSONArray("image");
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
                                        postModel.setFull_name(dataObject.getString("full_name"));
                                        postModel.setProfile_image(dataObject.getString("profile_image"));

                                        if (arrayList != null && arrayList.size() >= 1) {
                                            uploadImages(new ArrayList<>(), arrayList, postModel.getPost_id());
                                        } else {
                                            sendDataToActivity(postModel);
                                            bottomSheetDialog.dismiss();
                                        }

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "Đăng bài viết thất bại", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void uploadImages(@NonNull ArrayList<String> imagesUrl, ArrayList<Uri> imageUriList, int post_id) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("post/" + post_id).child(UUID.randomUUID().toString());
        Uri uri = imageUriList.get(imagesUrl.size());
        storageReference.putFile(uri).addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl().addOnCompleteListener(task -> {
            String url = Objects.requireNonNull(task.getResult()).toString();
            imagesUrl.add(url);

            if (imagesUrl.size() == imageUriList.size()) {
                addImagePost(user_id, post_id, enCodeQrCode(String.valueOf(imagesUrl)));
            } else {
                uploadImages(imagesUrl, arrayList, post_id);
            }
        })).addOnFailureListener(e -> {
            Toast.makeText(context, "Failed to upload images", Toast.LENGTH_SHORT).show();
        });
    }

    private void addImagePost(int user_id, int post_id, String arrayListUrl) {
        OkHttpClient client = new OkHttpClient();

        String url = Url_Api.getInstance().addImagesPost(user_id, post_id, arrayListUrl);
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

                                activity.runOnUiThread(() -> {
                                    try {
                                        PostModel postModel = new PostModel();

                                        postModel.setPost_id(dataObject.getInt("post_id"));
                                        postModel.setUser_id(dataObject.getInt("user_id"));
                                        postModel.setContent(dataObject.getString("content"));
                                        postModel.setTime(dataObject.getString("time"));
                                        postModel.setHeart_count(dataObject.getString("heart_count"));
                                        postModel.setComment_count(dataObject.getInt("comment_count"));
                                        postModel.setIs_save(dataObject.getInt("is_save_post") == 1);

                                        // Thay đổi cách lấy dữ liệu từ JSON cho imagePostModelList
                                        JSONArray imageArray = dataObject.getJSONArray("image");
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
                                        postModel.setFull_name(dataObject.getString("full_name"));
                                        postModel.setProfile_image(dataObject.getString("profile_image"));


                                        sendDataToActivity(postModel);
                                        bottomSheetDialog.dismiss();

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    activity.runOnUiThread(() -> {
                        Toast.makeText(context, "Đăng bài viết thất bại", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private String enCodeQrCode(String qr_code) {
        return Base64.encodeToString(qr_code.getBytes(), Base64.DEFAULT);
    }

    private String getTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentTime = dateFormat.format(new Date());
        return currentTime;
    }

    private void sendDataToActivity(PostModel postModel) {
        Intent intent = new Intent("new_post_model");
        Bundle bundle = new Bundle();
        bundle.putSerializable("postModel", postModel);
        intent.putExtras(bundle);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Chặn thoát khỏi BottomSheetDialog khi click bên ngoài
        getDialog().setCancelable(false);
    }

}
