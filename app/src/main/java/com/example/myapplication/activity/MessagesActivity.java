package com.example.myapplication.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;

import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.adapter.MessagesAdapter;
import com.example.myapplication.bottomsheet.BottomSheetMedia;
import com.example.myapplication.helper.FirebaseUploaderMessages;
import com.example.myapplication.helper.Helper;
import com.example.myapplication.model.MediaItem;
import com.example.myapplication.model.MessagesListModel;
import com.example.myapplication.model.MessagesModel;
import com.example.myapplication.server.Url_Api;
import com.example.myapplication.server.WebSocketService;
import com.example.myapplication.sharedpreferencesmanager.SharedPreferencesManager;
import com.google.gson.Gson;
import com.makeramen.roundedimageview.RoundedImageView;
import com.vanniktech.emoji.EmojiPopup;
import com.zegocloud.uikit.prebuilt.call.invite.widget.ZegoSendCallInvitationButton;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class MessagesActivity extends AppCompatActivity {

    private int user_id;
    private String name_user;

    //chat
    private MessagesListModel messageListModel;
    private MessagesAdapter messagesAdapter;
    private final ArrayList<MessagesModel> messagesModelArrayList = new ArrayList<>();
    private RecyclerView rv_message;
    private LinearLayout liner_more_voice_image, linear_micro, linear_input, linear_no_messages, liner_text_is_friend, linear_input_mess;
    private EditText edt_input_message;
    private View viewHoatDong;

    //call
    private ZegoSendCallInvitationButton btn_goi_thoai, btn_goi_video;

    //realtime
    private WebSocketService webSocketService;
    private boolean is_RuningWebSocketService = false;

    // popup
    private boolean isPopupVisible = false;

    private static final int YOUR_REQUEST_CODE = 123;


    private static final int REQUEST_PERMISSION_CODE = 1;
    private long recordingStartTime;
    private MediaRecorder mediaRecorder;
    private String audioFilePath;
    private boolean isRecording = false;

    private CountDownTimer countDownTimer;
    private TextView txt_time_record;
    private String time_record;


    // reply
    private LinearLayout linear_reply;
    private TextView txt_reply_name;
    private TextView txt_content_reply;
    private ImageView img_media;
    private int action_messages = 0;
    private MessagesModel messagesReply;


    //download
    private long downloadID;
    private static final int REQUEST_WRITE_STORAGE_PERMISSION = 101;


    private int currentPage = 1;
    private boolean isLoading = false;

    //get data form wesocket servicer
    private final BroadcastReceiver broadcastReceiverWebsocket = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle == null) {
                return;
            }
            MessagesModel newMessage = (MessagesModel) bundle.get("object_messages");
            if (newMessage != null) {
                addNewMessages(newMessage);
            }
        }
    };

    //get data form bottomsheet
    private final BroadcastReceiver broadcastReceiverBottomSheet = new BroadcastReceiver() {
        @SuppressLint("NotifyDataSetChanged")
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle == null) {
                return;
            }

            if (messagesReply != null && action_messages != 0) {
                messagesReply = null;
                action_messages = 0;
            }
            messagesReply = (MessagesModel) bundle.get("object_messages");
            action_messages = (int) bundle.get("action");

            if (action_messages == 1) {
                openReply(messagesReply, action_messages);
            } else if (action_messages == 3) {
                repeatMessages(messagesReply);
            } else if (action_messages == 4) {
                removeMessageToWebSocket(messagesReply);
            } else if ((action_messages == 5)) {
                restoreMessageToWebSocket(messagesReply);
            } else if ((action_messages == 6)) {
                deleteMessageToWebSocket(messagesReply);
            } else if ((action_messages == 7)) {
                requestPermissions_WRITE_EXTERNAL_STORAGE(messagesReply);
            }
        }
    };

    private final BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (id == downloadID) {
                Toast.makeText(MessagesActivity.this, "Đã tải xong. Lưu tại /Download/LetsTalk", Toast.LENGTH_SHORT).show();
            }
        }
    };


    private final BroadcastReceiver broadcastReceiverScrolling = new BroadcastReceiver() {
        @SuppressLint("NotifyDataSetChanged")
        @Override
        public void onReceive(Context context, Intent intent) {
            int messagesId = intent.getIntExtra("messages_id", 0);
            // Tìm vị trí của item với messagesId trong danh sách
            for (int i = 0; i < messagesModelArrayList.size(); i++) {
                MessagesModel messagesModel = messagesModelArrayList.get(i);
                if (messagesModel.getMessages_id() == messagesId) {
                    // Cuộn đến vị trí của item nếu tìm thấy
                    rv_message.scrollToPosition(i);
                    // Đánh dấu vị trí này là được cuộn
                    messagesAdapter.markScrolledPosition(i);
                    return;
                }
            }
        }
    };

    private final BroadcastReceiver broadcastReceiverUrlMedia = new BroadcastReceiver() {
        @SuppressLint("NotifyDataSetChanged")
        @Override
        public void onReceive(Context context, Intent intent) {
            String url = intent.getStringExtra("url");
            int type = intent.getIntExtra("type", -1);
            sendMessageToWebSocket(user_id, url, getTime(), type);
        }
    };

    private final BroadcastReceiver broadcastReceiverCamera = new BroadcastReceiver() {
        @SuppressLint("NotifyDataSetChanged")
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle == null) {
                return;
            }
            MediaItem mediaItem = (MediaItem) bundle.get("mediaItem");
            if (mediaItem != null) {
                MediaItem receivedMediaItem = (MediaItem) bundle.getSerializable("mediaItem");
                if (receivedMediaItem != null) {
                    FirebaseUploaderMessages.uploadFile(MessagesActivity.this, messageListModel.getMessage_list_id(), Uri.parse("file://" + receivedMediaItem.getPath()), receivedMediaItem.isVideo() ? 2 : 1);
                }
            }
        }
    };


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        //connect servicer
        Intent serviceIntent = new Intent(this, WebSocketService.class);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);


        // findViewById
        TextView name_receive_message = findViewById(R.id.name_receive_message);
        RoundedImageView img_avatar = findViewById(R.id.imgAvatar);
        viewHoatDong = findViewById(R.id.viewHoatDong);
        ImageView btn_back_message = findViewById(R.id.btn_back_message);
        ImageView btn_image = findViewById(R.id.btn_image);
        ImageView btn_more_input_message = findViewById(R.id.btn_more_input_message);
        ImageView btn_voice = findViewById(R.id.btn_voice);
        ImageView btn_send = findViewById(R.id.btn_send);
        ImageView btn_sticker = findViewById(R.id.btn_sticker);
        ImageView btn_more_message = findViewById(R.id.btn_more_message);

        edt_input_message = findViewById(R.id.edt_input_message);
        liner_more_voice_image = findViewById(R.id.liner_more_voice_image);
        btn_goi_thoai = findViewById(R.id.btn_goi_thoai);
        btn_goi_video = findViewById(R.id.btn_goi_video);

        linear_reply = findViewById(R.id.linear_reply);
        txt_reply_name = findViewById(R.id.txt_reply_name);
        txt_content_reply = findViewById(R.id.txt_content_reply);
        LinearLayout btn_close_reply = findViewById(R.id.btn_close_reply);
        img_media = findViewById(R.id.img_media);
        linear_micro = findViewById(R.id.linear_micro);
        linear_input = findViewById(R.id.linear_input);
        linear_no_messages = findViewById(R.id.linear_no_messages);

        linear_input_mess = findViewById(R.id.linear_input_mess);
        liner_text_is_friend = findViewById(R.id.liner_text_is_friend);
        TextView txt_view_profile = findViewById(R.id.txt_view_profile);

        //record
        ImageView btn_delete_record = findViewById(R.id.btn_delete_record);
        txt_time_record = findViewById(R.id.txt_time_record);
        ImageView btn_send_record = findViewById(R.id.btn_send_record);

        //change color changeStatusBarColor and changeNavigationColor
        Helper.changeStatusBarColor(this, R.color.white);
        Helper.changeNavigationColor(this, R.color.white, true);

        //get user_id
        //get user_id
        SharedPreferencesManager sharedPreferences = new SharedPreferencesManager(getApplicationContext());
        user_id = sharedPreferences.getUserId();

        //get intent MessagesListModel
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            return;
        }
        messageListModel = (MessagesListModel) bundle.get("messages_list");


        //set Receiver_avatar
        Glide.with(this)
                .load(messageListModel.getReceiver_avatar())
                .into(img_avatar);

        //set active
        if (messageListModel.isReceiver_is_online()) {
            viewHoatDong.setBackgroundResource(R.drawable.background_round_green);
        } else {
            viewHoatDong.setBackgroundResource(R.drawable.background_round_red);
        }

        // set name
        name_receive_message.setText(messageListModel.getReceiver_name());

        //get namw user
        getProfile(user_id);

        //check is_friend
        checkIsFriend(user_id, user_id == messageListModel.getSender_id() ? messageListModel.getReceiver_id() : messageListModel.getSender_id());

        txt_view_profile.setOnClickListener(view -> {
            Intent intent = new Intent(MessagesActivity.this, ViewProfileActivity.class);
            intent.putExtra("userId", user_id == messageListModel.getSender_id() ? messageListModel.getReceiver_id() : messageListModel.getSender_id());
            startActivity(intent);
        });

        //chat
        rv_message = findViewById(R.id.rv_message);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        rv_message.setLayoutManager(layoutManager);

        messagesAdapter = new MessagesAdapter(MessagesActivity.this, MessagesActivity.this, messagesModelArrayList, user_id, messageListModel.getReceiver_avatar());
        rv_message.setAdapter(messagesAdapter);

        rv_message.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            // Nếu số lượng item trong adapter khác 0 và layout của RecyclerView đã thay đổi
            if (bottom < oldBottom && messagesAdapter.getItemCount() > 0) {
                // Cuộn RecyclerView đến vị trí đầu tiên
                rv_message.postDelayed(() -> layoutManager.scrollToPosition(0), 100);
            }
        });
        rv_message.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                // Kiểm tra xem RecyclerView có đang cuộn hay không
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && dy > 0 && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 1) {
                    // Cuộn đến vị trí cuối cùng, gọi hàm để lấy thêm dữ liệu
                    getLoadMoreMessages(messageListModel.getMessage_list_id());
                }
            }

        });


        //btn back
        btn_back_message.setOnClickListener(view -> {
            finish();
            overridePendingTransition(R.anim.anim_in_right, R.anim.anim_out_right);
        });


        //get messages
        if (messageListModel.getMessage_list_id() == -1) {
            rv_message.setVisibility(View.GONE);
            linear_no_messages.setVisibility(View.VISIBLE);
        } else {
            rv_message.setVisibility(View.VISIBLE);
            getMessageList(messageListModel.getMessage_list_id());
        }


        //call
        goiThoai(String.valueOf(messageListModel.getSender_id() == user_id ? messageListModel.getReceiver_id() : messageListModel.getSender_id()));
        goiVideo(String.valueOf(messageListModel.getSender_id() == user_id ? messageListModel.getReceiver_id() : messageListModel.getSender_id()));


        edt_input_message.setOnClickListener(view -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(edt_input_message, InputMethodManager.SHOW_IMPLICIT);
            rv_message.smoothScrollToPosition(0);
            btn_more_input_message.setVisibility(View.VISIBLE);
            btn_voice.setVisibility(View.VISIBLE);
        });

        btn_send.setOnClickListener(view -> sendMessages(edt_input_message.getText().toString()));

        edt_input_message.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Không cần xử lý
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Không cần xử lý
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String text = editable.toString();
                if (!text.isEmpty()) {

                    btn_send.setVisibility(View.VISIBLE);
                    liner_more_voice_image.setVisibility(View.GONE);

                    btn_send.setOnClickListener(view -> sendMessages(text));
                } else {
                    btn_send.setVisibility(View.GONE);
                    liner_more_voice_image.setVisibility(View.VISIBLE);

                    btn_image.setOnClickListener(view -> {
                        if (!isPopupVisible) {
                            // Ẩn bàn phím
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                            BottomSheetMedia bottomSheetMedia = new BottomSheetMedia(MessagesActivity.this, MessagesActivity.this);
                            bottomSheetMedia.show((MessagesActivity.this).getSupportFragmentManager(), bottomSheetMedia.getTag());
                        } else {
                            // Hiển thị bàn phím
                            edt_input_message.requestFocus();
                            InputMethodManager immm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            immm.showSoftInput(edt_input_message, InputMethodManager.SHOW_IMPLICIT);

                            isPopupVisible = false;
                        }
                    });
                    btn_voice.setOnClickListener(view -> {

                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        linear_input.setVisibility(View.GONE);
                        linear_micro.setVisibility(View.VISIBLE);


                    });
                }
            }
        });

        btn_image.setOnClickListener(view -> {
            // Ẩn bàn phím
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            BottomSheetMedia bottomSheetMedia = new BottomSheetMedia(this, this);
            bottomSheetMedia.show((this).getSupportFragmentManager(), bottomSheetMedia.getTag());
        });

        btn_voice.setOnClickListener(view -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            requestPermissions_RECORD_AUDIO();

        });
        btn_delete_record.setOnClickListener(view -> stopRecording());
        btn_send_record.setOnClickListener(view -> {
            sendRecording();
            linear_input.setVisibility(View.VISIBLE);
            linear_micro.setVisibility(View.GONE);
        });

        btn_more_input_message.setOnClickListener(view -> activityResultLauncher.launch("*/*"));

        btn_more_message.setOnClickListener(view -> {
            Intent intent = new Intent(MessagesActivity.this, OptionMessagesActivity.class);
            Bundle bundle1 = new Bundle();
            bundle1.putSerializable("messages_list", messageListModel);
            intent.putExtras(bundle1);
            startActivity(intent);
            overridePendingTransition(R.anim.anim_in_left, R.anim.anim_out_left);
        });


        //Initialize emoji popup
        EmojiPopup popup = EmojiPopup.Builder.fromRootView(
                findViewById(R.id.root_view)
        ).build(edt_input_message);

        btn_sticker.setOnClickListener(view -> popup.toggle());

        btn_close_reply.setOnClickListener(view -> {
            linear_reply.setVisibility(View.GONE);
            txt_reply_name.setText("");
            txt_content_reply.setText("");
        });

    }


    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WebSocketService.LocalBinder binder = (WebSocketService.LocalBinder) service;
            webSocketService = binder.getService();
            is_RuningWebSocketService = true;
            Log.d(">>>>>>>>>>>>>>", "Service connected successfully");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            is_RuningWebSocketService = false;
            Log.d(">>>>>>>>>>>>>>", "Service disconnected");
        }
    };


    private void checkIsFriend(int sender_id, int reciever_id) {

        OkHttpClient client = new OkHttpClient();

        String url = Url_Api.getInstance().checkIsFriend(sender_id, reciever_id);
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
                                int is_friend = dataObject.getInt("is_friend");

                                runOnUiThread(() -> {
                                    if (is_friend == 0 || is_friend == -1) {
                                        linear_input_mess.setVisibility(View.GONE);
                                        liner_text_is_friend.setVisibility(View.VISIBLE);
                                        btn_goi_thoai.setVisibility(View.GONE);
                                        btn_goi_video.setVisibility(View.GONE);
                                        viewHoatDong.setVisibility(View.GONE);
                                    } else {
                                        linear_input_mess.setVisibility(View.VISIBLE);
                                        liner_text_is_friend.setVisibility(View.GONE);
                                        btn_goi_thoai.setVisibility(View.VISIBLE);
                                        btn_goi_video.setVisibility(View.VISIBLE);
                                        viewHoatDong.setVisibility(View.VISIBLE);
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
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        name_user = full_name;
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

    private String getTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return dateFormat.format(new Date());
    }

    private void getLoadMoreMessages(int messages_list_id) {
        if (!isLoading) {
            isLoading = true;
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(Url_Api.getInstance().getMessages(messages_list_id, currentPage))
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                    isLoading = false;
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

                                    ArrayList<MessagesModel> newMessages = new ArrayList<>();
                                    for (int i = 0; i < dataArray.length(); i++) {
                                        JSONObject messageObject = dataArray.getJSONObject(i);
                                        int messageId = messageObject.getInt("messages_id");
                                        int messagesListId = messageObject.getInt("messages_list_id");
                                        int senderId = messageObject.getInt("sender_id");
                                        String content = messageObject.getString("content");
                                        String time = messageObject.getString("time");
                                        int typeMessage = messageObject.getInt("type_message");

                                        MessagesModel message = new MessagesModel(messagesListId, senderId, messageId, typeMessage, time, content);
                                        newMessages.add(message);
                                    }

                                    runOnUiThread(() -> {

                                        messagesModelArrayList.addAll(newMessages);
                                        messagesAdapter.notifyItemRangeInserted(
                                                messagesModelArrayList.size() - newMessages.size(),
                                                newMessages.size()
                                        );
                                        currentPage++;
                                        isLoading = false;
                                    });

                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                                isLoading = false;
                            }
                        }
                    }
                }
            });
        }
    }

    private void getMessageList(int messages_list_id) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(Url_Api.getInstance().getMessages(messages_list_id, currentPage))
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

                                ArrayList<MessagesModel> newMessages = new ArrayList<>();
                                for (int i = 0; i < dataArray.length(); i++) {
                                    JSONObject messageObject = dataArray.getJSONObject(i);
                                    int messageId = messageObject.getInt("messages_id");
                                    int messagesListId = messageObject.getInt("messages_list_id");
                                    int senderId = messageObject.getInt("sender_id");
                                    String content = messageObject.getString("content");
                                    String time = messageObject.getString("time");
                                    int typeMessage = messageObject.getInt("type_message");

                                    if (typeMessage != 8) {
                                        MessagesModel message = new MessagesModel(messagesListId, senderId, messageId, typeMessage, time, content);
                                        newMessages.add(message);
                                    }
                                }

                                runOnUiThread(() -> {
                                    rv_message.setVisibility(View.VISIBLE);
                                    linear_no_messages.setVisibility(View.GONE);

                                    if (currentPage == 1) {
                                        messagesModelArrayList.clear();
                                    }
                                    messagesModelArrayList.addAll(newMessages);
                                    messagesAdapter.notifyItemRangeInserted(
                                            messagesModelArrayList.size() - newMessages.size(),
                                            newMessages.size()
                                    );
                                    rv_message.smoothScrollToPosition(0);
                                    currentPage++;
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

    private void addMessagesList(int user_id, int sender_id, int receiver_id, String content, String time, int typeMess) {

        OkHttpClient client = new OkHttpClient();

        String url = Url_Api.getInstance().addMessagesList(user_id, sender_id, receiver_id);
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
                                JSONObject messageObject = jsonObject.getJSONObject("message");

                                int messagesListId = messageObject.getInt("messages_list_id");
                                int senderId = messageObject.getInt("sender_id");
                                int receiverId = messageObject.getInt("receiver_id");
                                String lastContent = messageObject.getString("last_content");
                                int typeMessage = messageObject.getInt("type_message");
                                String timeData = messageObject.getString("time");
                                boolean isSeen = messageObject.getInt("is_seen") == 1;
                                String receiver_avatar = messageObject.getString("profile_image");
                                String receiver_name = messageObject.getString("full_name");
                                boolean receiver_is_online = messageObject.getInt("is_online") == 1;

                                MessagesListModel message = new MessagesListModel();
                                message.setMessage_list_id(messagesListId);
                                message.setSender_id(senderId);
                                message.setReceiver_id(receiverId);
                                message.setLast_content(lastContent);
                                message.setType_message(typeMessage);
                                message.setTime(timeData);
                                message.setReceiver_avatar(receiver_avatar);
                                message.setReceiver_name(receiver_name);
                                message.setReceiver_is_online(receiver_is_online);
                                message.setIs_seen(isSeen);
                                runOnUiThread(() -> {
                                    messageListModel = message;

                                    if (webSocketService != null) {

                                        if (typeMess != -1) {
                                            MessagesModel messagesModel = new MessagesModel(messageListModel.getMessage_list_id(), sender_id, 0, typeMess, time, content);
                                            Gson gson = new Gson();
                                            String jsonMessage = gson.toJson(messagesModel);
                                            webSocketService.sendMessage(jsonMessage);
                                        } else {
                                            Log.d(">>>>>>>>>>>>>", "sendMessageToWebSocket: " + "0");
                                        }

                                    } else {
                                        Log.d(">>>>>>>>>>>>>", "sendMessageToWebSocket: " + "AAAAAAAAAAAAAAAAAAAAAAAA");
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

    private void sendMessages(String content) {
        if (!content.isEmpty()) {
            if (action_messages == 1) {
                replyMessages(content);

                linear_reply.setVisibility(View.GONE);
                txt_reply_name.setText("");
                txt_content_reply.setText("");
            } else {
                sendMessageToWebSocket(user_id, content, getTime(), isValidURLL(content) ? 5 : 0);
                edt_input_message.setText("");
            }
        }

    }

    private void sendMessageToWebSocket(int sender_id, String content, String time, int typeMess) {
        if (webSocketService != null) {
            if (messageListModel.getMessage_list_id() == -1) {
                addMessagesList(user_id, messageListModel.getSender_id(), messageListModel.getReceiver_id(), content, time, typeMess);
            } else {
                if (typeMess != -1) {
                    if (typeMess == 3) {
                        JSONObject contentJson = new JSONObject();
                        try {

                            contentJson.put("url_record", enCodeQrCode(content));
                            contentJson.put("time_record", time_record);
                        } catch (JSONException e) {
                            e.printStackTrace(); // Handle the exception according to your needs
                        }

                        // Convert the JSONObject to a JSON string
                        String contentJsonString = contentJson.toString();
                        MessagesModel messagesModel = new MessagesModel(messageListModel.getMessage_list_id(), sender_id, 0, typeMess, time, contentJsonString);
                        Gson gson = new Gson();
                        String jsonMessage = gson.toJson(messagesModel);
                        webSocketService.sendMessage(jsonMessage);
                    } else {
                        MessagesModel messagesModel = new MessagesModel(messageListModel.getMessage_list_id(), sender_id, 0, typeMess, time, content);
                        Gson gson = new Gson();
                        String jsonMessage = gson.toJson(messagesModel);
                        webSocketService.sendMessage(jsonMessage);
                    }

                } else {
                    Log.d(">>>>>>>>>>>>>", "sendMessageToWebSocket: " + "0");
                }
            }

        } else {
            Log.d(">>>>>>>>>>>>>", "sendMessageToWebSocket: " + "AAAAAAAAAAAAAAAAAAAAAAAA");
        }
    }

    private String enCodeQrCode(String qr_code) {
        return Base64.encodeToString(qr_code.getBytes(), Base64.DEFAULT);
    }

    private void removeMessageToWebSocket(MessagesModel messagesModel) {
        if (webSocketService != null) {
            JSONObject contentJson = new JSONObject();
            try {
                contentJson.put("messages_content", messagesModel.getContent());
                contentJson.put("type_messages", messagesModel.getType_message());
            } catch (JSONException e) {
                e.printStackTrace(); // Handle the exception according to your needs
            }

            // Convert the JSONObject to a JSON string
            String contentJsonString = contentJson.toString();

            MessagesModel newMessagesModel = new MessagesModel(messagesModel.getMessages_list_id(), messagesModel.getSender_id(), messagesModel.getMessages_id(), 9, messagesModel.getTime(), contentJsonString);
            Gson gson = new Gson();
            String jsonMessage = gson.toJson(newMessagesModel);
            webSocketService.sendMessage(jsonMessage);
        }
    }

    private void restoreMessageToWebSocket(MessagesModel messagesModel) {
        if (webSocketService != null) {
            try {
                JSONObject contentJson = new JSONObject(messagesModel.getContent());

                // Retrieve values from the JSONObject
                String messages_content = contentJson.getString("messages_content");
                int type_messages = contentJson.getInt("type_messages");

                MessagesModel newMessagesModel = new MessagesModel(messagesModel.getMessages_list_id(), messagesModel.getSender_id(), messagesModel.getMessages_id(), type_messages, messagesModel.getTime(), messages_content);
                Gson gson = new Gson();
                String jsonMessage = gson.toJson(newMessagesModel);

                webSocketService.sendMessage(jsonMessage);
            } catch (JSONException e) {
                e.printStackTrace(); // Handle the exception according to your needs
            }
        }
    }

    private void deleteMessageToWebSocket(MessagesModel messagesModel) {
        if (webSocketService != null) {
            MessagesModel newMessagesModel = new MessagesModel(messagesModel.getMessages_list_id(), messagesModel.getSender_id(), messagesModel.getMessages_id(), 8, messagesModel.getTime(), messagesModel.getContent());
            Gson gson = new Gson();
            String jsonMessage = gson.toJson(newMessagesModel);
            webSocketService.sendMessage(jsonMessage);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void addNewMessages(MessagesModel messagesModel) {
        if (messagesModel != null) {
            rv_message.setVisibility(View.VISIBLE);
            linear_no_messages.setVisibility(View.GONE);

            int messageId = messagesModel.getMessages_id();
            boolean messageExists = false;

            // Duyệt qua danh sách tin nhắn hiện tại để tìm tin nhắn với messageId
            for (int i = 0; i < messagesModelArrayList.size(); i++) {
                MessagesModel existingMessage = messagesModelArrayList.get(i);
                if (existingMessage.getMessages_id() == messageId) {
                    if (existingMessage.getType_message() == 8) {
                        messagesModelArrayList.remove(i);
                        messagesAdapter.notifyItemChanged(i);
                    } else {
                        messagesModelArrayList.set(i, messagesModel);
                        messagesAdapter.notifyItemChanged(i);
                        messageExists = true;
                        break;
                    }
                }
            }

            if (!messageExists) {
                Log.d(">>>>>>>>>>>", "chen: " + messagesModel.getMessages_id());
                // Nếu tin nhắn không tồn tại, chèn nó vào đầu danh sách
                messagesModelArrayList.add(0, messagesModel);
                if (messagesAdapter != null) {
                    messagesAdapter.notifyItemInserted(0);
                    rv_message.smoothScrollToPosition(0);
                }
            } else {
                // Nếu tin nhắn tồn tại và đã được cập nhật, thông báo cho adapter về sự thay đổi tại vị trí cụ thể
                if (messagesAdapter != null) {
                    messagesAdapter.notifyItemChanged(0);
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void openReply(MessagesModel messagesModel, int index) {
        if (index == 1) {
            linear_reply.setVisibility(View.VISIBLE);
            if (messagesModel.getSender_id() == user_id) {
                txt_reply_name.setText("Đang trả lời chính mình");
            } else {
                txt_reply_name.setText(messageListModel.getReceiver_name());
            }

            switch (messagesModel.getType_message()) {
                case 0:
                    img_media.setVisibility(View.GONE);
                    txt_content_reply.setText(messagesModel.getContent());
                    break;
                case 1:
                    img_media.setVisibility(View.VISIBLE);
                    txt_content_reply.setText("Hình ảnh");
                    Glide.with(this)
                            .load(messagesModel.getContent())
                            .into(img_media);
                    break;

            }
        }
    }

    private void replyMessages(String content) {
        if (!content.isEmpty()) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String currentTime = dateFormat.format(new Date());

            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("messages_id", messagesReply.getMessages_id());

                if (messagesReply.getSender_id() == user_id) {
                    jsonObject.put("name_reply", "Trả lời chính mình");
                } else {
                    jsonObject.put("name_reply", messageListModel.getReceiver_name());
                }
                jsonObject.put("content", messagesReply.getContent());
                jsonObject.put("reply_content", content);

                String jsonString = jsonObject.toString();

                sendMessageToWebSocket(user_id, jsonString, currentTime, 7);

                edt_input_message.setText("");
                action_messages = 0;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void repeatMessages(MessagesModel messagesModel) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentTime = dateFormat.format(new Date());

        if (messagesModel.getType_message() == 7) {
            try {
                JSONObject jsonObject = new JSONObject(messagesModel.getContent());
                String replyContentValue = jsonObject.getString("reply_content");
                try {
                    JSONObject jsonObject2 = new JSONObject();
                    jsonObject2.put("messages_id", messagesModel.getMessages_id());
                    jsonObject2.put("user_id_repeat", user_id);
                    jsonObject2.put("user_id_receiver", user_id == messageListModel.getSender_id() ? messageListModel.getReceiver_id() : messageListModel.getSender_id());
                    jsonObject2.put("sender_id", messagesModel.getSender_id());
                    jsonObject2.put("content", replyContentValue);

                    String jsonString = jsonObject2.toString();

                    sendMessageToWebSocket(user_id, jsonString, currentTime, 6);

                    edt_input_message.setText("");
                    action_messages = 0;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            try {
                JSONObject jsonObject2 = new JSONObject();
                jsonObject2.put("messages_id", messagesModel.getMessages_id());
                jsonObject2.put("user_id_repeat", user_id);
                jsonObject2.put("user_id_receiver", user_id == messageListModel.getSender_id() ? messageListModel.getReceiver_id() : messageListModel.getSender_id());
                jsonObject2.put("sender_id", messagesModel.getSender_id());

                jsonObject2.put("content", messagesModel.getContent());

                String jsonString = jsonObject2.toString();

                sendMessageToWebSocket(user_id, jsonString, currentTime, 6);

                edt_input_message.setText("");
                action_messages = 0;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void requestPermissions_RECORD_AUDIO() {
        // Kiểm tra xem quyền ghi âm đã được cấp phép chưa
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Quyền ghi âm chưa được cấp phép, yêu cầu người dùng cấp phép
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, REQUEST_PERMISSION_CODE);
        } else {
            linear_input.setVisibility(View.GONE);
            linear_micro.setVisibility(View.VISIBLE);
            // Quyền ghi âm đã được cấp phép, bắt đầu ghi âm
            startRecording();
        }
    }

    private void startRecording() {
        if (mediaRecorder == null) {

            File externalFilesDir = getExternalFilesDir(null);
            if (externalFilesDir != null) {
                audioFilePath = externalFilesDir.getAbsolutePath() + "/audio.3gp";
            }


            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setOutputFile(audioFilePath);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recordingStartTime = SystemClock.elapsedRealtime();

            try {
                mediaRecorder.prepare();
                mediaRecorder.start();

                isRecording = true;
                startTimer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void sendRecording() {
        if (isRecording) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;

            isRecording = false;
            stopTimer();

            FirebaseUploaderMessages.uploadFile(this, messageListModel.getMessage_list_id(), Uri.parse("file://" + audioFilePath), 3);
        }
    }

    private void stopRecording() {
        if (isRecording) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;

            isRecording = false;
            stopTimer();

            linear_input.setVisibility(View.VISIBLE);
            linear_micro.setVisibility(View.GONE);
        }
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(Long.MAX_VALUE, 1000) {
            @SuppressLint("DefaultLocale")
            @Override
            public void onTick(long millisUntilFinished) {
                long elapsedTime = SystemClock.elapsedRealtime() - recordingStartTime;
                long totalSeconds = elapsedTime / 1000;
                long minutes = totalSeconds / 60;
                long seconds = totalSeconds % 60;

                time_record = String.format("%02d:%02d", minutes, seconds);
                txt_time_record.setText(time_record);
            }

            @Override
            public void onFinish() {
            }

        }.start();
    }

    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    private void requestPermissions_WRITE_EXTERNAL_STORAGE(MessagesModel messagesModel) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE_PERMISSION);
        } else {
            downloadImage(messagesModel);
        }
    }

    private void downloadImage(MessagesModel messagesModel) {

        File myFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "LetsTalk");
        if (!myFolder.exists()) {
            myFolder.mkdirs();
        }

        String fileName = URLUtil.guessFileName(messagesModel.getContent(), null, null);
        File destinationFile = new File(myFolder, subStringFileName(fileName));
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(messagesModel.getContent()));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setTitle(subStringFileName(fileName))
                .setDescription("Đang lưu...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationUri(Uri.fromFile(destinationFile));

        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        downloadID = downloadManager.enqueue(request);
    }

    private String subStringFileName(String file_name) {
        int underscoreIndex = file_name.indexOf("_");

        if (underscoreIndex != -1) {
            // Kiểm tra xem phần trước dấu "_" là một số hay không
            String potentialTimestamp = file_name.substring(0, underscoreIndex);

            try {
                Long.parseLong(potentialTimestamp); // Thử chuyển đổi thành số
                // Nếu không có exception, đây là một số và có thể cắt
                return file_name.substring(underscoreIndex + 1);
            } catch (NumberFormatException e) {
                // Nếu có exception, phần trước dấu "_" không phải là một số
                return file_name;
            }
        } else {
            return file_name;
        }
    }

    private void goiThoai(String userIdCall) {
        btn_goi_thoai.setIsVideoCall(false);
        btn_goi_thoai.setResourceID("zego_uikit_call");
        btn_goi_thoai.setInvitees(Collections.singletonList(new ZegoUIKitUser(userIdCall)));
    }

    private void goiVideo(String userIdCall) {
        btn_goi_video.setIsVideoCall(true);
        btn_goi_video.setResourceID("zego_uikit_call");
        btn_goi_video.setInvitees(Collections.singletonList(new ZegoUIKitUser(userIdCall)));
    }

    private boolean isValidURLL(String text) {
        return Patterns.WEB_URL.matcher(text).matches();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == YOUR_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Bundle bundle = data.getExtras();
                if (bundle != null) {
                    MediaItem receivedMediaItem = (MediaItem) bundle.getSerializable("mediaItem");
                    if (receivedMediaItem != null) {
                        FirebaseUploaderMessages.uploadFile(this, messageListModel.getMessage_list_id(), Uri.parse("file://" + receivedMediaItem.getPath()), receivedMediaItem.isVideo() ? 2 : 1);
                    }
                }
            }
        }
    }

    private final ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            result -> {
                if (result != null) {
                    FirebaseUploaderMessages.uploadFile(this, messageListModel.getMessage_list_id(), result, 4);
                }
            }
    );

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onResume() {
        super.onResume();

        Intent serviceIntent = new Intent(this, WebSocketService.class);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiverWebsocket, new IntentFilter("send_data_to_activity"));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiverBottomSheet, new IntentFilter("send_data_bottomsheet"));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiverScrolling, new IntentFilter("scrolling_messages_id"));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiverUrlMedia, new IntentFilter("url_media"));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiverCamera, new IntentFilter("send_data_camreaX"));

        registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiverWebsocket);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiverBottomSheet);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiverScrolling);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiverUrlMedia);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiverCamera);

        unregisterReceiver(onDownloadComplete);

        if (isRecording) {
            stopRecording();
        }
        // Hủy bỏ đối tượng CountDownTimer nếu đang hoạt động
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Ngắt kết nối từ dịch vụ ở đây
        if (is_RuningWebSocketService) {
            unbindService(serviceConnection);
            is_RuningWebSocketService = false;
            webSocketService = null;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.anim_in_right, R.anim.anim_out_right);
    }
}