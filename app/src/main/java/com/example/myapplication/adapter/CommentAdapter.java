package com.example.myapplication.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.activity.ViewPostActivity;
import com.example.myapplication.activity.ViewProfileActivity;
import com.example.myapplication.bottomsheet.BottomSheetOptionComment;
import com.example.myapplication.bottomsheet.BottomSheetUnFriend;
import com.example.myapplication.model.CommentModel;
import com.example.myapplication.model.ImagePostModel;
import com.example.myapplication.model.PostModel;
import com.makeramen.roundedimageview.RoundedImageView;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private final Context context;
    private final int user_id;
    private final Activity activity;
    private final ArrayList<CommentModel> commentModelArrayList;

    public CommentAdapter(Context context, ArrayList<CommentModel> commentModelArrayList, int user_id, Activity activity) {
        this.context = context;
        this.commentModelArrayList = commentModelArrayList;
        this.user_id = user_id;
        this.activity = activity;
    }
    public void removeItem(int position) {
        commentModelArrayList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cmt, parent, false);
        return new ViewHolder(view); // Chỗ này trả về một đối tượng ViewHolder mới
    }


    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CommentModel commentModel = commentModelArrayList.get(position);

        Glide.with(context).load(commentModel.getProfile_image()).into(holder.roundedImageView);
        holder.txtName.setText(commentModel.getFull_name());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            holder.txtTime.setText(calculateTime(commentModel.getTime()));
        }
        holder.txtMess.setText(commentModel.getContent());

        holder.btn_repy_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        holder.itemView.setOnLongClickListener(view -> {
            BottomSheetOptionComment bottomSheetOptionComment = new BottomSheetOptionComment(context, activity, commentModel, position);
            bottomSheetOptionComment.show(((AppCompatActivity) context).getSupportFragmentManager(), bottomSheetOptionComment.getTag());
            return false;
        });

        holder.roundedImageView.setOnClickListener(view -> {
            Intent intent = new Intent(context, ViewProfileActivity.class);
            intent.putExtra("userId", commentModel.getUser_id());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {

        return commentModelArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        RoundedImageView roundedImageView;
        TextView txtName, txtTime, txtMess, btn_repy_comment;

        public ViewHolder(View view) {
            super(view);
            roundedImageView = view.findViewById(R.id.roundedImageView);
            txtName = view.findViewById(R.id.txtName);
            txtMess = view.findViewById(R.id.txtMess);
            txtTime = view.findViewById(R.id.txtTime);
            btn_repy_comment = view.findViewById(R.id.btn_repy_comment);


        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static String calculateTime(String time) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try {
            LocalDateTime specifiedTime = LocalDateTime.parse(time, formatter);
            Duration duration = Duration.between(specifiedTime, now);
            long seconds = Math.abs(duration.getSeconds());
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;

            StringBuilder result = new StringBuilder();

            if (days > 0) {
                result.append(days).append(" ngày trước");
            } else if (hours > 0) {
                result.append(hours).append(" giờ trước");
            } else if (minutes > 0) {
                result.append(minutes).append(" phút trước");
            } else {
                result.append("Vừa xong");
            }

            return result.toString();
        } catch (DateTimeParseException e) {
            // Handle the case when the input is not a valid date format
            return time; // Return the original string
        }
    }

}
