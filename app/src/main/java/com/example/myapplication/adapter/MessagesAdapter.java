package com.example.myapplication.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.CountDownTimer;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.activity.ViewMediaActivity;
import com.example.myapplication.bottomsheet.BottomSheetSelectedMessage;
import com.example.myapplication.model.MessagesModel;
import com.example.myapplication.R;
import com.example.myapplication.server.Url_Api;
import com.google.gson.Gson;
import com.makeramen.roundedimageview.RoundedImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;
    private final Activity activity;
    private final ArrayList<MessagesModel> list;
    private final int sender_id;
    private final String url_avatar;

    private boolean is_play_record = false;
    private boolean is_paused = false;
    private long remainingTimeMillis = 0;
    private CountDownTimer countDownTimer;


    private final MediaPlayer mediaPlayer = new MediaPlayer();

    private int previousScrolledPosition = RecyclerView.NO_POSITION;
    private int currentScrolledPosition = RecyclerView.NO_POSITION;

    //TYPE_SEND
    private static final int TYPE_SEND_TEXT = 1;
    private static final int TYPE_SEND_IMAGE = 2;
    private static final int TYPE_SEND_VIDEO = 3;
    private static final int TYPE_SEND_RECORD = 4;
    private static final int TYPE_SEND_FILE = 5;
    private static final int TYPE_SEND_LINK = 6;
    private static final int TYPE_SEND_EMOTION = 7;
    private static final int TYPE_SEND_REPLY = 8;
    private static final int TYPE_SEND_REMOVE_ONLY = 9;
    private static final int TYPE_SEND_REMOVE_ALL = 10;

    //TYPE_RECEIVE
    private static final int TYPE_RECEIVE_TEXT = 11;
    private static final int TYPE_RECEIVE_IMAGE = 12;
    private static final int TYPE_RECEIVE_VIDEO = 13;
    private static final int TYPE_RECEIVE_RECORD = 14;
    private static final int TYPE_RECEIVE_FILE = 15;
    private static final int TYPE_RECEIVE_LINK = 16;
    private static final int TYPE_RECEIVE_EMOTION = 17;
    private static final int TYPE_RECEIVE_REPLY = 18;
    private static final int TYPE_RECEIVE_REMOVE_ONLY = 19;
    private static final int TYPE_RECEIVE_REMOVE_ALL = 20;


    //TYPE_MESS
    private static final int TYPE_IMAGE = 1;
    private static final int TYPE_VIDEO = 2;
    private static final int TYPE_RECORD = 3;
    private static final int TYPE_FILE = 4;
    private static final int TYPE_LINK = 5;
    private static final int TYPE_EMOTION = 6;
    private static final int TYPE_REPLY = 7;
    private static final int TYPE_REMOVE_ONLY = 8;
    private static final int TYPE_REMOVE_ALL = 9;


    public MessagesAdapter(Context context, Activity activity, ArrayList<MessagesModel> list, int sender_id, String url_avatar) {
        this.context = context;
        this.activity = activity;
        this.list = list;
        this.sender_id = sender_id;
        this.url_avatar = url_avatar;
    }

    public void markScrolledPosition(int position) {
        // Lưu vị trí cuộn trước đó
        previousScrolledPosition = currentScrolledPosition;

        // Lưu vị trí cuộn hiện tại
        currentScrolledPosition = position;

        // Cập nhật giao diện người dùng
        notifyDataSetChanged();
    }

    public interface FullNameCallback {
        void onFullNameComplete(String full_name);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        switch (viewType) {
            case TYPE_SEND_TEXT:
                view = LayoutInflater.from(context).inflate(R.layout.item_senmess_text, parent, false);
                return new SendMessTextViewHolder(view);
            case TYPE_SEND_IMAGE:
                view = LayoutInflater.from(context).inflate(R.layout.item_senmess_image, parent, false);
                return new SendMessImageViewHolder(view);
            case TYPE_SEND_VIDEO:
                view = LayoutInflater.from(context).inflate(R.layout.item_senmess_video, parent, false);
                return new SendMessVideoViewHolder(view);
            case TYPE_SEND_RECORD:
                view = LayoutInflater.from(context).inflate(R.layout.item_senmess_record, parent, false);
                return new SendMessRecordViewHolder(view);
            case TYPE_SEND_FILE:
                view = LayoutInflater.from(context).inflate(R.layout.item_senmess_file, parent, false);
                return new SendMessFileViewHolder(view);
            case TYPE_SEND_LINK:
                view = LayoutInflater.from(context).inflate(R.layout.item_senmess_link, parent, false);
                return new SendMessTextViewHolder(view);
            case TYPE_SEND_EMOTION:
                view = LayoutInflater.from(context).inflate(R.layout.item_senmess_repeat, parent, false);
                return new SendMessEmotionViewHolder(view);
            case TYPE_SEND_REPLY:
                view = LayoutInflater.from(context).inflate(R.layout.item_senmess_reply, parent, false);
                return new SendMessReplyViewHolder(view);
            case TYPE_SEND_REMOVE_ONLY:
                view = LayoutInflater.from(context).inflate(R.layout.item_senmess_remove_only, parent, false);
                return new SendMessRemoveOnlyViewHolder(view);
            case TYPE_SEND_REMOVE_ALL:
                view = LayoutInflater.from(context).inflate(R.layout.item_senmess_remove_all, parent, false);
                return new SendMessRemoveAllViewHolder(view);

            //TYPE_RECEIVE
            case TYPE_RECEIVE_IMAGE:
                view = LayoutInflater.from(context).inflate(R.layout.item_receivemess_image, parent, false);
                return new ReceivedMessImageViewHolder(view);
            case TYPE_RECEIVE_VIDEO:
                view = LayoutInflater.from(context).inflate(R.layout.item_receivemess_video, parent, false);
                return new ReceivedMessVideoViewHolder(view);
            case TYPE_RECEIVE_RECORD:
                view = LayoutInflater.from(context).inflate(R.layout.item_receivemess_record, parent, false);
                return new ReceivedMessRecordViewHolder(view);
            case TYPE_RECEIVE_FILE:
                view = LayoutInflater.from(context).inflate(R.layout.item_receivemess_file, parent, false);
                return new ReceivedMessFileViewHolder(view);
            case TYPE_RECEIVE_EMOTION:
                view = LayoutInflater.from(context).inflate(R.layout.item_receivemess_repeat, parent, false);
                return new ReceivedMessEmotionViewHolder(view);
            case TYPE_RECEIVE_REPLY:
                view = LayoutInflater.from(context).inflate(R.layout.item_receivemess_reply, parent, false);
                return new ReceivedMessReplyViewHolder(view);
            case TYPE_RECEIVE_REMOVE_ONLY:
                view = LayoutInflater.from(context).inflate(R.layout.item_receivemess_remove_only, parent, false);
                return new ReceivedMessRemoveOnlyViewHolder(view);
            case TYPE_RECEIVE_REMOVE_ALL:
                view = LayoutInflater.from(context).inflate(R.layout.item_receivemess_remove_all, parent, false);
                return new ReceivedMessRemoveAllViewHolder(view);
            default:
                view = LayoutInflater.from(context).inflate(R.layout.item_receivemess_text, parent, false);
                return new ReceivedMessTextViewHolder(view);
        }

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessagesModel messagesModel = list.get(position);
        int viewType = getItemViewType(position);

        if (viewType == TYPE_SEND_IMAGE || viewType == TYPE_RECEIVE_IMAGE) {
            if (viewType == TYPE_SEND_IMAGE) {
                Glide.with(context).load(messagesModel.getContent()).into(((SendMessImageViewHolder) holder).image_view);
                ((SendMessImageViewHolder) holder).txtTime.setText(formatTime(messagesModel.getTime()));

                ((SendMessImageViewHolder) holder).itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context, ViewMediaActivity.class);
                        intent.putExtra("url_media", messagesModel.getContent());
                        intent.putExtra("sender_id", messagesModel.getSender_id());
                        intent.putExtra("is_video", false);
                        context.startActivity(intent);
                    }
                });
            } else {
                Glide.with(context).load(messagesModel.getContent()).into(((ReceivedMessImageViewHolder) holder).image_view);
                ((ReceivedMessImageViewHolder) holder).txtTime.setText(formatTime(messagesModel.getTime()));
                Glide.with(context).load(url_avatar).into(((ReceivedMessImageViewHolder) holder).roundedImageView);
                ((ReceivedMessImageViewHolder) holder).itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context, ViewMediaActivity.class);
                        intent.putExtra("url_media", messagesModel.getContent());
                        intent.putExtra("sender_id", messagesModel.getSender_id());
                        intent.putExtra("is_video", false);
                        context.startActivity(intent);
                    }
                });
            }
        } else if (viewType == TYPE_SEND_VIDEO || viewType == TYPE_RECEIVE_VIDEO) {
            if (viewType == TYPE_SEND_VIDEO) {
                Uri videoUri = Uri.parse(messagesModel.getContent());
                SendMessVideoViewHolder viewHolder = (SendMessVideoViewHolder) holder;

                viewHolder.videoView.setVideoURI(videoUri);

                // Thêm MediaController
                MediaController mediaController = new MediaController(context);
                viewHolder.videoView.setMediaController(mediaController);

                viewHolder.txtTime.setText(formatTime(messagesModel.getTime()));
                ((SendMessVideoViewHolder) holder).itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context, ViewMediaActivity.class);
                        intent.putExtra("url_media", messagesModel.getContent());
                        intent.putExtra("sender_id", messagesModel.getSender_id());
                        intent.putExtra("is_video", true);
                        context.startActivity(intent);
                    }
                });

            } else {
                ((ReceivedMessVideoViewHolder) holder).txtTime.setText(formatTime(messagesModel.getTime()));
                Glide.with(context).load(url_avatar).into(((ReceivedMessVideoViewHolder) holder).imgAvatar);

                Uri videoUri = Uri.parse(messagesModel.getContent());
                ReceivedMessVideoViewHolder viewHolder = (ReceivedMessVideoViewHolder) holder;

                viewHolder.videoView.setVideoURI(videoUri);

                // Thêm MediaController
                MediaController mediaController = new MediaController(context);
                viewHolder.videoView.setMediaController(mediaController);
                viewHolder.txtTime.setText(formatTime(messagesModel.getTime()));

                ((ReceivedMessVideoViewHolder) holder).itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context, ViewMediaActivity.class);
                        intent.putExtra("url_media", messagesModel.getContent());
                        intent.putExtra("sender_id", messagesModel.getSender_id());
                        intent.putExtra("is_video", true);
                        context.startActivity(intent);
                    }
                });
            }
        } else if (viewType == TYPE_SEND_RECORD || viewType == TYPE_RECEIVE_RECORD) {
            if (viewType == TYPE_SEND_RECORD) {

                try {
                    JSONObject contentJson = new JSONObject(messagesModel.getContent());

                    // Retrieve values from the JSONObject
                    String url_record = deCodeQrCode(contentJson.getString("url_record"));
                    String time_record = contentJson.getString("time_record");


                    ((SendMessRecordViewHolder) holder).txtTime.setText(formatTime(messagesModel.getTime()));
                    ((SendMessRecordViewHolder) holder).txtMess.setText(time_record);
                    ((SendMessRecordViewHolder) holder).btn_play.setOnClickListener(view -> {
                        if (!is_play_record) {
                            // Bắt đầu phát âm thanh
                            is_play_record = true;
                            ((SendMessRecordViewHolder) holder).btn_play.setImageResource(R.drawable.ic_pause_2); // Thay đổi hình ảnh sang pause

                            try {
                                mediaPlayer.reset();
                                mediaPlayer.setDataSource(url_record);
                                mediaPlayer.prepare();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            if (remainingTimeMillis == 0) {
                                // Lần đầu tiên chạy
                                long duration = mediaPlayer.getDuration(); // Thời gian tối đa của âm thanh
                                remainingTimeMillis = duration;

                                mediaPlayer.setOnCompletionListener(mp -> {
                                    is_play_record = false;
                                    ((SendMessRecordViewHolder) holder).btn_play.setImageResource(R.drawable.baseline_play_circle_24);
                                    remainingTimeMillis = 0;
                                    if (countDownTimer != null) {
                                        countDownTimer.cancel();
                                    }
                                });

                                mediaPlayer.start();

                                countDownTimer = new CountDownTimer(duration, 1000) {
                                    @Override
                                    public void onTick(long millisUntilFinished) {
                                        if (!is_paused) {
                                            remainingTimeMillis = millisUntilFinished;
                                            long totalSeconds = millisUntilFinished / 1000;
                                            long minutes = totalSeconds / 60;
                                            long seconds = totalSeconds % 60;

                                            String time = String.format("%02d:%02d", minutes, seconds);
                                            // Update TextView với thời gian giảm dần hoặc thời gian đoạn ghi âm
                                            ((SendMessRecordViewHolder) holder).txtMess.setText(time);
                                        }
                                    }

                                    @Override
                                    public void onFinish() {
                                        // Xử lý khi kết thúc phát âm thanh
                                        remainingTimeMillis = 0;
                                        is_play_record = false;
                                        ((SendMessRecordViewHolder) holder).btn_play.setImageResource(R.drawable.baseline_play_circle_24);
                                    }
                                }.start();
                            } else {
                                // Tiếp tục phát khi đã tạm dừng
                                if (!is_paused) {
                                    mediaPlayer.seekTo((int) (mediaPlayer.getDuration() - remainingTimeMillis));
                                    mediaPlayer.start();
                                    countDownTimer = new CountDownTimer(remainingTimeMillis, 1000) {
                                        @Override
                                        public void onTick(long l) {
                                            if (!is_paused) {
                                                remainingTimeMillis = l;
                                            }
                                        }

                                        @Override
                                        public void onFinish() {
                                            // Xử lý khi kết thúc phát âm thanh
                                            remainingTimeMillis = 0;
                                            is_play_record = false;
                                            ((SendMessRecordViewHolder) holder).btn_play.setImageResource(R.drawable.baseline_play_circle_24);
                                        }
                                        // ... (tương tự như trong onTick và onFinish của CountDownTimer trước đó)
                                    }.start();
                                } else {
                                    // Tiếp tục từ tạm dừng
                                    mediaPlayer.start();
                                    is_paused = false;
                                    if (countDownTimer != null) {
                                        countDownTimer.cancel();
                                    }
                                }
                            }
                        } else {
                            // Dừng hoặc tạm dừng phát âm thanh
                            if (mediaPlayer.isPlaying()) {
                                mediaPlayer.pause();
                                ((SendMessRecordViewHolder) holder).btn_play.setImageResource(R.drawable.baseline_play_circle_24);
                                is_paused = true;
                            } else {
                                // Bắt đầu lại từ tạm dừng
                                mediaPlayer.start();
                                ((SendMessRecordViewHolder) holder).btn_play.setImageResource(R.drawable.ic_pause_2);
                                is_paused = false;
                                if (countDownTimer != null) {
                                    countDownTimer.cancel();
                                }
                            }
                        }
                    });
                    holder.itemView.setOnClickListener(view -> {
                        Intent outIntent = new Intent("action_download");
                        outIntent.putExtra("url_File", url_record);
                        LocalBroadcastManager.getInstance(context).sendBroadcast(outIntent);
                    });

                } catch (JSONException e) {
                    e.printStackTrace(); // Handle the exception according to your needs
                }

            } else {
                try {
                    JSONObject contentJson = new JSONObject(messagesModel.getContent());

                    // Retrieve values from the JSONObject
                    String url_record = deCodeQrCode(contentJson.getString("url_record"));
                    String time_record = contentJson.getString("time_record");

                    ((ReceivedMessRecordViewHolder) holder).txtMess.setText(time_record);
                    Glide.with(context)
                            .load(url_avatar)
                            .into(((ReceivedMessRecordViewHolder) holder).roundedImageView);
                    ((ReceivedMessRecordViewHolder) holder).txtTime.setText(formatTime(messagesModel.getTime()));
                    ((ReceivedMessRecordViewHolder) holder).btn_play.setOnClickListener(view -> {
                        if (!is_play_record) {
                            // Bắt đầu phát âm thanh
                            is_play_record = true;
                            ((ReceivedMessRecordViewHolder) holder).btn_play.setImageResource(R.drawable.ic_pause_2); // Thay đổi hình ảnh sang pause

                            try {
                                mediaPlayer.reset();
                                mediaPlayer.setDataSource(url_record);
                                mediaPlayer.prepare();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            if (remainingTimeMillis == 0) {
                                // Lần đầu tiên chạy
                                long duration = mediaPlayer.getDuration(); // Thời gian tối đa của âm thanh
                                remainingTimeMillis = duration;

                                mediaPlayer.setOnCompletionListener(mp -> {
                                    is_play_record = false;
                                    ((ReceivedMessRecordViewHolder) holder).btn_play.setImageResource(R.drawable.baseline_play_circle_24);
                                    remainingTimeMillis = 0;
                                    if (countDownTimer != null) {
                                        countDownTimer.cancel();
                                    }
                                });

                                mediaPlayer.start();

                                countDownTimer = new CountDownTimer(duration, 1000) {
                                    @Override
                                    public void onTick(long millisUntilFinished) {
                                        if (!is_paused) {
                                            remainingTimeMillis = millisUntilFinished;
                                            long totalSeconds = millisUntilFinished / 1000;
                                            long minutes = totalSeconds / 60;
                                            long seconds = totalSeconds % 60;

                                            String time = String.format("%02d:%02d", minutes, seconds);
                                            // Update TextView với thời gian giảm dần hoặc thời gian đoạn ghi âm
                                            ((ReceivedMessRecordViewHolder) holder).txtMess.setText(time);
                                        }
                                    }

                                    @Override
                                    public void onFinish() {
                                        // Xử lý khi kết thúc phát âm thanh
                                        remainingTimeMillis = 0;
                                        is_play_record = false;
                                        ((ReceivedMessRecordViewHolder) holder).btn_play.setImageResource(R.drawable.baseline_play_circle_24);
                                    }
                                }.start();
                            } else {
                                // Tiếp tục phát khi đã tạm dừng
                                if (!is_paused) {
                                    mediaPlayer.seekTo((int) (mediaPlayer.getDuration() - remainingTimeMillis));
                                    mediaPlayer.start();
                                    countDownTimer = new CountDownTimer(remainingTimeMillis, 1000) {
                                        @Override
                                        public void onTick(long l) {
                                            if (!is_paused) {
                                                remainingTimeMillis = l;
                                            }
                                        }

                                        @Override
                                        public void onFinish() {
                                            // Xử lý khi kết thúc phát âm thanh
                                            remainingTimeMillis = 0;
                                            is_play_record = false;
                                            ((ReceivedMessRecordViewHolder) holder).btn_play.setImageResource(R.drawable.baseline_play_circle_24);
                                        }
                                        // ... (tương tự như trong onTick và onFinish của CountDownTimer trước đó)
                                    }.start();
                                } else {
                                    // Tiếp tục từ tạm dừng
                                    mediaPlayer.start();
                                    is_paused = false;
                                    if (countDownTimer != null) {
                                        countDownTimer.cancel();
                                    }
                                }
                            }
                        } else {
                            // Dừng hoặc tạm dừng phát âm thanh
                            if (mediaPlayer.isPlaying()) {
                                mediaPlayer.pause();
                                ((ReceivedMessRecordViewHolder) holder).btn_play.setImageResource(R.drawable.baseline_play_circle_24);
                                is_paused = true;
                            } else {
                                // Bắt đầu lại từ tạm dừng
                                mediaPlayer.start();
                                ((ReceivedMessRecordViewHolder) holder).btn_play.setImageResource(R.drawable.ic_pause_2);
                                is_paused = false;
                                if (countDownTimer != null) {
                                    countDownTimer.cancel();
                                }
                            }
                        }
                    });
                    holder.itemView.setOnClickListener(view -> {
                        Intent outIntent = new Intent("action_download");
                        outIntent.putExtra("url_File", url_record);
                        LocalBroadcastManager.getInstance(context).sendBroadcast(outIntent);
                    });
                } catch (JSONException e) {
                    e.printStackTrace(); // Handle the exception according to your needs
                }
            }
        } else if (viewType == TYPE_SEND_FILE || viewType == TYPE_RECEIVE_FILE) {
            if (viewType == TYPE_SEND_FILE) {
                ((SendMessFileViewHolder) holder).txtLink.setText(subStringFileName(URLUtil.guessFileName(messagesModel.getContent(), null, null)));
                ((SendMessFileViewHolder) holder).txtTime.setText(formatTime(messagesModel.getTime()));
                holder.itemView.setOnClickListener(view -> {
                    Intent outIntent = new Intent("action_download");
                    outIntent.putExtra("url_File", messagesModel.getContent());
                    LocalBroadcastManager.getInstance(context).sendBroadcast(outIntent);
                });
            } else {
                ((ReceivedMessFileViewHolder) holder).txtLink.setText(subStringFileName(URLUtil.guessFileName(messagesModel.getContent(), null, null)));
                ((ReceivedMessFileViewHolder) holder).txtDate.setText(formatTime(messagesModel.getTime()));
                Glide.with(context)
                        .load(url_avatar)
                        .into(((ReceivedMessFileViewHolder) holder).roundedImageView);
                holder.itemView.setOnClickListener(view -> {
                    Intent outIntent = new Intent("action_download");
                    outIntent.putExtra("url_File", messagesModel.getContent());
                    LocalBroadcastManager.getInstance(context).sendBroadcast(outIntent);
                });
            }
        } else if (viewType == TYPE_SEND_LINK || viewType == TYPE_RECEIVE_LINK) {
            if (viewType == TYPE_SEND_LINK) {
                if (isValidURLL(messagesModel.getContent())) {
                    ((SendMessTextViewHolder) holder).txtMess.setText(messagesModel.getContent());
                    ((SendMessTextViewHolder) holder).txtMess.setPaintFlags(((SendMessTextViewHolder) holder).txtMess.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                }
                ((SendMessTextViewHolder) holder).txtTime.setText(formatTime(messagesModel.getTime()));
                holder.itemView.setOnClickListener(view -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(messagesModel.getContent()));
                    context.startActivity(intent);
                });
            } else {
                if (isValidURLL(messagesModel.getContent())) {
                    ((ReceivedMessTextViewHolder) holder).txtMess.setText(messagesModel.getContent());
                    ((ReceivedMessTextViewHolder) holder).txtMess.setPaintFlags(((ReceivedMessTextViewHolder) holder).txtMess.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                }
                ((ReceivedMessTextViewHolder) holder).txtTime.setText(formatTime(messagesModel.getTime()));
                Glide.with(context)
                        .load(url_avatar)
                        .into(((ReceivedMessTextViewHolder) holder).roundedImageView);
                holder.itemView.setOnClickListener(view -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(messagesModel.getContent()));
                    context.startActivity(intent);
                });
            }
        } else if (viewType == TYPE_SEND_EMOTION || viewType == TYPE_RECEIVE_EMOTION) {
            if (viewType == TYPE_SEND_EMOTION) {
                try {
                    JSONObject contentJson = new JSONObject(messagesModel.getContent());

                    // Retrieve values from the JSONObject
                    int messages_id = contentJson.getInt("messages_id");
                    int user_id_repeat = contentJson.getInt("user_id_repeat");
                    int user_id_receiver = contentJson.getInt("user_id_receiver");
                    int sender_id_messages = contentJson.getInt("sender_id");
                    String content = contentJson.getString("content");

                    if (user_id_repeat == sender_id) {
                        if (sender_id_messages == sender_id) {
                            ((SendMessEmotionViewHolder) holder).txt_content_repeat.setText("Bạn đã nhắc lại tin nhắn của bạn");
                        } else {
                            getInfo(sender_id_messages, new FullNameCallback() {
                                @Override
                                public void onFullNameComplete(String full_name) {
                                    ((SendMessEmotionViewHolder) holder).txt_content_repeat.setText(" Bạn đã nhắc lại tin nhắn của " + full_name);
                                }
                            });
                        }
                    }
                    ((SendMessEmotionViewHolder) holder).txtNoiDung.setText(content);
                    ((SendMessEmotionViewHolder) holder).txtDate.setText(formatTime(messagesModel.getTime()));

                } catch (JSONException e) {
                    e.printStackTrace(); // Handle the exception according to your needs
                }
            } else {
                try {
                    JSONObject contentJson = new JSONObject(messagesModel.getContent());

                    // Retrieve values from the JSONObject
                    int messages_id = contentJson.getInt("messages_id");
                    int user_id_repeat = contentJson.getInt("user_id_repeat");
                    int user_id_receiver = contentJson.getInt("user_id_receiver");
                    int sender_id_messages = contentJson.getInt("sender_id");
                    String content = contentJson.getString("content");


                    Glide.with(context).load(url_avatar).into(((ReceivedMessEmotionViewHolder) holder).roundedImageView);
                    if (user_id_repeat != sender_id) {
                        if (sender_id_messages != sender_id) {
                            getInfo(sender_id_messages, new FullNameCallback() {
                                @Override
                                public void onFullNameComplete(String full_name) {
                                    ((ReceivedMessEmotionViewHolder) holder).txt_content_repeat.setText(full_name + " đã nhắc lại tin nhắn của " + full_name);
                                }
                            });
                        } else {
                            getInfo(user_id_repeat, new FullNameCallback() {
                                @Override
                                public void onFullNameComplete(String full_name) {
                                    ((ReceivedMessEmotionViewHolder) holder).txt_content_repeat.setText(full_name + " đã nhắc lại tin nhắn của bạn");
                                }
                            });

                        }
                    }
                    ((ReceivedMessEmotionViewHolder) holder).txtNoiDung.setText(content);
                    ((ReceivedMessEmotionViewHolder) holder).txtDate.setText(formatTime(messagesModel.getTime()));

                } catch (JSONException e) {
                    e.printStackTrace(); // Handle the exception according to your needs
                }

            }
        } else if (viewType == TYPE_SEND_REPLY || viewType == TYPE_RECEIVE_REPLY) {
            if (viewType == TYPE_SEND_REPLY) {
                ((SendMessReplyViewHolder) holder).txtDate.setText(formatTime(messagesModel.getTime()));
                int messages_id = 0;
                try {
                    JSONObject jsonObject = new JSONObject(messagesModel.getContent());
                    messages_id = jsonObject.getInt("messages_id");
                    String name_reply = jsonObject.getString("name_reply");
                    String contentValue = jsonObject.getString("content");
                    String replyContentValue = jsonObject.getString("reply_content");

                    ((SendMessReplyViewHolder) holder).txtMess.setText(contentValue);
                    ((SendMessReplyViewHolder) holder).txtMessReply.setText(replyContentValue);
                    ((SendMessReplyViewHolder) holder).txt_name.setText(name_reply);
                } catch (JSONException e) {
                    e.printStackTrace();
                    ((SendMessReplyViewHolder) holder).txtMess.setText("Error parsing JSON");
                }

                int finalMessages_id = messages_id;
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent("scrolling_messages_id");
                        intent.putExtra("messages_id", finalMessages_id);
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                    }
                });
            } else {
                ((ReceivedMessReplyViewHolder) holder).txtTime.setText(formatTime(messagesModel.getTime()));
                Glide.with(context).load(url_avatar).into(((ReceivedMessReplyViewHolder) holder).roundedImageView);

                try {
                    JSONObject jsonObject = new JSONObject(messagesModel.getContent());
                    String messages_id = jsonObject.getString("messages_id");
                    String name_reply = jsonObject.getString("name_reply");
                    String contentValue = jsonObject.getString("content");
                    String replyContentValue = jsonObject.getString("reply_content");


                    ((ReceivedMessReplyViewHolder) holder).txtMess.setText(contentValue);
                    ((ReceivedMessReplyViewHolder) holder).txtMessReply.setText(replyContentValue);
                    ((ReceivedMessReplyViewHolder) holder).txt_name.setText(name_reply);
                } catch (JSONException e) {
                    e.printStackTrace();
                    ((ReceivedMessReplyViewHolder) holder).txtMess.setText("Error parsing JSON");
                }
            }
        } else if (viewType == TYPE_SEND_REMOVE_ONLY || viewType == TYPE_RECEIVE_REMOVE_ONLY) {
            if (viewType == TYPE_SEND_REMOVE_ONLY) {
                ((SendMessRemoveOnlyViewHolder) holder).itemView.setVisibility(View.GONE);
            } else {
                ((ReceivedMessRemoveOnlyViewHolder) holder).itemView.setVisibility(View.GONE);
            }
        } else if (viewType == TYPE_SEND_REMOVE_ALL || viewType == TYPE_RECEIVE_REMOVE_ALL) {
            if (viewType == TYPE_SEND_REMOVE_ALL) {
                ((SendMessRemoveAllViewHolder) holder).txtDate.setText(formatTime(messagesModel.getTime()));
            } else {
                ((ReceivedMessRemoveAllViewHolder) holder).txtDate.setText(formatTime(messagesModel.getTime()));
                Glide.with(context).load(url_avatar).into(((ReceivedMessRemoveAllViewHolder) holder).roundedImageView);
            }
        } else {
            if (viewType == TYPE_SEND_TEXT) {
                ((SendMessTextViewHolder) holder).txtMess.setText(messagesModel.getContent());
                ((SendMessTextViewHolder) holder).txtTime.setText(formatTime(messagesModel.getTime()));
            } else {
                ((ReceivedMessTextViewHolder) holder).txtMess.setText(messagesModel.getContent());
                ((ReceivedMessTextViewHolder) holder).txtTime.setText(formatTime(messagesModel.getTime()));
                Glide.with(context)
                        .load(url_avatar)
                        .into(((ReceivedMessTextViewHolder) holder).roundedImageView);
            }
        }

        holder.itemView.setOnLongClickListener(view -> {
            BottomSheetSelectedMessage bottomSheetSelectedMessage = new BottomSheetSelectedMessage(context, messagesModel);
            bottomSheetSelectedMessage.show(((AppCompatActivity) context).getSupportFragmentManager(), bottomSheetSelectedMessage.getTag());
            return false;
        });
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        MessagesModel messagesModel = list.get(position);

        if (messagesModel.getSender_id() == sender_id) {
            if (messagesModel.getType_message() == TYPE_IMAGE) {
                return TYPE_SEND_IMAGE;
            } else if (messagesModel.getType_message() == TYPE_VIDEO) {
                return TYPE_SEND_VIDEO;
            } else if (messagesModel.getType_message() == TYPE_RECORD) {
                return TYPE_SEND_RECORD;
            } else if (messagesModel.getType_message() == TYPE_FILE) {
                return TYPE_SEND_FILE;
            } else if (messagesModel.getType_message() == TYPE_LINK) {
                return TYPE_SEND_LINK;
            } else if (messagesModel.getType_message() == TYPE_EMOTION) {
                return TYPE_SEND_EMOTION;
            } else if (messagesModel.getType_message() == TYPE_REPLY) {
                return TYPE_SEND_REPLY;
            } else if (messagesModel.getType_message() == TYPE_REMOVE_ONLY) {
                return TYPE_SEND_REMOVE_ONLY;
            } else if (messagesModel.getType_message() == TYPE_REMOVE_ALL) {
                return TYPE_SEND_REMOVE_ALL;
            } else {
                return TYPE_SEND_TEXT;
            }
        } else {
            if (messagesModel.getType_message() == TYPE_IMAGE) {
                return TYPE_RECEIVE_IMAGE;
            } else if (messagesModel.getType_message() == TYPE_VIDEO) {
                return TYPE_RECEIVE_VIDEO;
            } else if (messagesModel.getType_message() == TYPE_RECORD) {
                return TYPE_RECEIVE_RECORD;
            } else if (messagesModel.getType_message() == TYPE_FILE) {
                return TYPE_RECEIVE_FILE;
            } else if (messagesModel.getType_message() == TYPE_LINK) {
                return TYPE_RECEIVE_LINK;
            } else if (messagesModel.getType_message() == TYPE_EMOTION) {
                return TYPE_RECEIVE_EMOTION;
            } else if (messagesModel.getType_message() == TYPE_REPLY) {
                return TYPE_RECEIVE_REPLY;
            } else if (messagesModel.getType_message() == TYPE_REMOVE_ONLY) {
                return TYPE_RECEIVE_REMOVE_ONLY;
            } else if (messagesModel.getType_message() == TYPE_REMOVE_ALL) {
                return TYPE_RECEIVE_REMOVE_ALL;
            } else {
                return TYPE_RECEIVE_TEXT;
            }
        }
    }

    // SendMess
    static class SendMessTextViewHolder extends RecyclerView.ViewHolder {
        TextView txtMess, txtTime;

        public SendMessTextViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMess = itemView.findViewById(R.id.txtMess);
            txtTime = itemView.findViewById(R.id.txtTime);
        }
    }

    static class SendMessImageViewHolder extends RecyclerView.ViewHolder {
        TextView txtTime;
        ImageView image_view;

        public SendMessImageViewHolder(@NonNull View itemView) {
            super(itemView);
            image_view = itemView.findViewById(R.id.imageView_message_image);
            txtTime = itemView.findViewById(R.id.textView_message_time);
        }
    }

    static class SendMessVideoViewHolder extends RecyclerView.ViewHolder {
        TextView txtTime;
        VideoView videoView;

        public SendMessVideoViewHolder(@NonNull View itemView) {
            super(itemView);
            videoView = itemView.findViewById(R.id.videoView);
            txtTime = itemView.findViewById(R.id.txtTime);
        }
    }

    static class SendMessRecordViewHolder extends RecyclerView.ViewHolder {
        TextView txtMess, txtTime;
        ImageView btn_play;

        public SendMessRecordViewHolder(@NonNull View itemView) {
            super(itemView);
            btn_play = itemView.findViewById(R.id.btn_play);
            txtMess = itemView.findViewById(R.id.txtMess);
            txtTime = itemView.findViewById(R.id.txtTime);
        }
    }

    static class SendMessFileViewHolder extends RecyclerView.ViewHolder {
        TextView txtLink, txtTime;
        ImageView image_icon_download;

        public SendMessFileViewHolder(@NonNull View itemView) {
            super(itemView);
            image_icon_download = itemView.findViewById(R.id.image_icon_download);
            txtLink = itemView.findViewById(R.id.txtLink);
            txtTime = itemView.findViewById(R.id.txtTime);
        }
    }


    //chua fix emotion
    static class SendMessEmotionViewHolder extends RecyclerView.ViewHolder {
        TextView txtNoiDung, txtDate, txt_content_repeat;


        public SendMessEmotionViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNoiDung = itemView.findViewById(R.id.txtMess);
            txtDate = itemView.findViewById(R.id.txtTime);
            txt_content_repeat = itemView.findViewById(R.id.txt_content_repeat);
        }
    }

    static class SendMessReplyViewHolder extends RecyclerView.ViewHolder {
        TextView txtMess, txtMessReply, txtDate, txt_name;

        public SendMessReplyViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMess = itemView.findViewById(R.id.txtMess);
            txtMessReply = itemView.findViewById(R.id.txtMessReply);
            txt_name = itemView.findViewById(R.id.txt_name);
            txtDate = itemView.findViewById(R.id.txtTime);
        }
    }


    //chua fix remove only
    static class SendMessRemoveOnlyViewHolder extends RecyclerView.ViewHolder {
        TextView txtDate, txtMess;

        public SendMessRemoveOnlyViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMess = itemView.findViewById(R.id.txtMess);
            txtDate = itemView.findViewById(R.id.txtTime);
        }
    }

    static class SendMessRemoveAllViewHolder extends RecyclerView.ViewHolder {
        TextView txtDate;
        TextView txtMess;

        public SendMessRemoveAllViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMess = itemView.findViewById(R.id.txtMess);
            txtDate = itemView.findViewById(R.id.txtTime);
        }
    }


    // ReceivedMess
    static class ReceivedMessTextViewHolder extends RecyclerView.ViewHolder {
        TextView txtMess, txtTime;
        RoundedImageView roundedImageView;

        public ReceivedMessTextViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMess = itemView.findViewById(R.id.txtMess);
            txtTime = itemView.findViewById(R.id.txtTime);
            roundedImageView = itemView.findViewById(R.id.roundedImageView);
        }
    }

    static class ReceivedMessImageViewHolder extends RecyclerView.ViewHolder {
        TextView txtTime;
        ImageView image_view;
        RoundedImageView roundedImageView;

        public ReceivedMessImageViewHolder(@NonNull View itemView) {
            super(itemView);
            image_view = itemView.findViewById(R.id.imageView_message_image);
            txtTime = itemView.findViewById(R.id.textView_message_time);
            roundedImageView = itemView.findViewById(R.id.roundedImageView);
        }
    }

    static class ReceivedMessVideoViewHolder extends RecyclerView.ViewHolder {
        TextView txtTime;
        VideoView videoView;
        RoundedImageView imgAvatar;

        public ReceivedMessVideoViewHolder(@NonNull View itemView) {
            super(itemView);
            videoView = itemView.findViewById(R.id.videoView);
            txtTime = itemView.findViewById(R.id.txtTime);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
        }
    }

    static class ReceivedMessRecordViewHolder extends RecyclerView.ViewHolder {
        TextView txtMess, txtTime;
        ImageView btn_play;
        RoundedImageView roundedImageView;

        public ReceivedMessRecordViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMess = itemView.findViewById(R.id.txtMess);
            btn_play = itemView.findViewById(R.id.btn_play);
            txtTime = itemView.findViewById(R.id.txtTime);
            roundedImageView = itemView.findViewById(R.id.roundedImageView);
        }
    }

    static class ReceivedMessFileViewHolder extends RecyclerView.ViewHolder {
        TextView txtLink, txtDate;
        RoundedImageView roundedImageView;

        public ReceivedMessFileViewHolder(@NonNull View itemView) {
            super(itemView);
            txtLink = itemView.findViewById(R.id.txtLink);
            txtDate = itemView.findViewById(R.id.txtTime);
            roundedImageView = itemView.findViewById(R.id.imgAvatar);
        }
    }


    // chu fix
    static class ReceivedMessEmotionViewHolder extends RecyclerView.ViewHolder {
        TextView txtNoiDung, txtDate, txt_content_repeat;
        RoundedImageView roundedImageView;

        public ReceivedMessEmotionViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNoiDung = itemView.findViewById(R.id.txtMess);
            txtDate = itemView.findViewById(R.id.txtTime);
            txt_content_repeat = itemView.findViewById(R.id.txt_content_repeat);
            roundedImageView = itemView.findViewById(R.id.roundedImageView);
        }
    }

    static class ReceivedMessReplyViewHolder extends RecyclerView.ViewHolder {
        TextView txtMess, txtMessReply, txtTime, txt_name;
        RoundedImageView roundedImageView;

        public ReceivedMessReplyViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMess = itemView.findViewById(R.id.txtMess);
            txt_name = itemView.findViewById(R.id.txt_name);
            txtMessReply = itemView.findViewById(R.id.txtMessReply);
            txtTime = itemView.findViewById(R.id.txtTime);
            roundedImageView = itemView.findViewById(R.id.roundedImageView);
        }
    }

    //chua fix
    static class ReceivedMessRemoveOnlyViewHolder extends RecyclerView.ViewHolder {
        TextView txtNoiDung, txtDate;
        RoundedImageView roundedImageView;

        public ReceivedMessRemoveOnlyViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNoiDung = itemView.findViewById(R.id.txtMess);
            txtDate = itemView.findViewById(R.id.txtTime);
            roundedImageView = itemView.findViewById(R.id.roundedImageView);
        }
    }

    static class ReceivedMessRemoveAllViewHolder extends RecyclerView.ViewHolder {
        TextView txtNoiDung, txtDate;
        RoundedImageView roundedImageView;

        public ReceivedMessRemoveAllViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNoiDung = itemView.findViewById(R.id.txtMess);
            txtDate = itemView.findViewById(R.id.txtTime);
            roundedImageView = itemView.findViewById(R.id.roundedImageView);
        }
    }

    private void getInfo(int user_id, FullNameCallback callback) {
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
                                activity.runOnUiThread(() -> callback.onFullNameComplete(full_name));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        });
    }


    private boolean isValidURLL(String text) {
        return Patterns.WEB_URL.matcher(text).matches();
    }

    private String formatTime(String originalTime) {
        try {
            SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat newFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

            Date date = originalFormat.parse(originalTime);
            if (date != null) {
                return newFormat.format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
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

    private String deCodeQrCode(String encodedQrCode) {
        byte[] decodedBytes = Base64.decode(encodedQrCode, Base64.DEFAULT);
        return new String(decodedBytes);
    }

}

