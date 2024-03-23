package com.example.myapplication.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.myapplication.R;
import com.example.myapplication.activity.ViewMediaActivity;
import com.example.myapplication.activity.ViewPostImageActivity;
import com.example.myapplication.model.ImagePostModel;
import com.example.myapplication.model.MessagesModel;

import java.util.ArrayList;
import java.util.List;

;
;

public class MediaMessagesAdapter extends RecyclerView.Adapter<MediaMessagesAdapter.ViewHolder> {
    private final ArrayList<MessagesModel> messagesModels;
    private Context context;
    private Activity mActivity;

    public MediaMessagesAdapter(Context context, Activity mActivity, ArrayList<MessagesModel> messagesModels) {
        this.context = context;
        this.mActivity = mActivity;
        this.messagesModels = messagesModels;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_media_messages, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        MessagesModel messagesModel = messagesModels.get(position);

        Glide.with(context)
                .load(messagesModel.getContent())
                .into(holder.imageView);
        if (messagesModel.getType_message() == 2) {
            holder.txtTimeVideo.setVisibility(View.VISIBLE);
            holder.btn_play.setVisibility(View.VISIBLE);
        }else {
            holder.txtTimeVideo.setVisibility(View.GONE);
            holder.btn_play.setVisibility(View.GONE);
        }
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (messagesModel.getType_message()==2){
                    Intent intent = new Intent(context, ViewMediaActivity.class);
                    intent.putExtra("url_media", messagesModel.getContent());
                    intent.putExtra("sender_id", messagesModel.getSender_id());
                    intent.putExtra("is_video", true);
                    context.startActivity(intent);
                }else {
                    Intent intent = new Intent(context, ViewMediaActivity.class);
                    intent.putExtra("url_media", messagesModel.getContent());
                    intent.putExtra("sender_id", messagesModel.getSender_id());
                    intent.putExtra("is_video", false);
                    context.startActivity(intent);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return messagesModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView, btn_play;
        TextView txtTimeVideo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            btn_play = itemView.findViewById(R.id.btn_play);
            txtTimeVideo = itemView.findViewById(R.id.txtTimeVideo);
        }
    }
}
