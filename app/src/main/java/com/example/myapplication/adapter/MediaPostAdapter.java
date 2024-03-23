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

import androidx.annotation.NonNull;;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.myapplication.R;
import com.example.myapplication.activity.ViewMediaActivity;
import com.example.myapplication.activity.ViewPostImageActivity;
import com.example.myapplication.model.ImagePostModel;

import java.util.ArrayList;
import java.util.List;;

public class MediaPostAdapter extends RecyclerView.Adapter<MediaPostAdapter.ViewHolder> {
    private List<ImagePostModel> imagePostModels;
    private Context context;
    private Activity mActivity;
    private boolean is_finish;
    private final int YOUR_REQUEST_CODE = 12345;

    public MediaPostAdapter(Context context, Activity mActivity, List<ImagePostModel> imagePostModels, boolean is_finish) {
        this.context = context;
        this.mActivity = mActivity;
        this.imagePostModels = imagePostModels;
        this.is_finish = is_finish;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_media_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        ImagePostModel imagePostModel = imagePostModels.get(position);

        Glide.with(context)
                .load(imagePostModel.getImage())
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL)) // Áp dụng chiến lược cache
                .into(holder.imageView);


        holder.txtTimeVideo.setVisibility(View.GONE);
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ViewPostImageActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("post_id", imagePostModel.getPost_id());
                bundle.putSerializable("is_finish", is_finish);
                intent.putExtras(bundle);
                mActivity.startActivityForResult(intent, YOUR_REQUEST_CODE);
                mActivity.overridePendingTransition(R.anim.anim_in_left, R.anim.anim_out_left);
            }
        });

    }

    @Override
    public int getItemCount() {
        return imagePostModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView txtTimeVideo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            txtTimeVideo = itemView.findViewById(R.id.txtTimeVideo);
        }
    }
}
