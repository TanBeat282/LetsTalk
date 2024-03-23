package com.example.myapplication.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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

import java.util.ArrayList;
import java.util.List;

public class MediaLibraryAdapter extends RecyclerView.Adapter<MediaLibraryAdapter.ViewHolder> {
    private ArrayList<Uri> uriArrayList;
    private Context context;
    private Activity mActivity;

    public MediaLibraryAdapter(Context context, Activity mActivity, ArrayList<Uri> uriArrayList) {
        this.context = context;
        this.mActivity = mActivity;
        this.uriArrayList = uriArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_media_lib, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Uri uri = uriArrayList.get(position);

        Glide.with(context)
                .load(uri)
                .into(holder.imageView);

        Glide.with(context)
                .load(uri.toString())
                .into(holder.imageView);

        holder.txtTimeVideo.setVisibility(View.GONE);

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ViewMediaActivity.class);
                intent.putExtra("url_media", uri.toString());
                intent.putExtra("sender_id", 0);
                intent.putExtra("is_video", false);
                context.startActivity(intent);
            }
        });
        holder.btn_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Xóa phần tử khỏi danh sách
                uriArrayList.remove(position);
                // Thông báo cho Adapter biết rằng dữ liệu đã thay đổi
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return uriArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView,btn_remove;
        TextView txtTimeVideo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            btn_remove = itemView.findViewById(R.id.btn_remove);
            imageView = itemView.findViewById(R.id.imageView);
            txtTimeVideo = itemView.findViewById(R.id.txtTimeVideo);
        }
    }
}
