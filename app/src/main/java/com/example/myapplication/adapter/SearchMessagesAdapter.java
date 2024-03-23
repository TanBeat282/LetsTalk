package com.example.myapplication.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.activity.MessagesActivity;
import com.example.myapplication.bottomsheet.BottomSheetOptionMessagesList;
import com.example.myapplication.model.MessagesListModel;
import com.example.myapplication.model.SearchMessagesModel;
import com.makeramen.roundedimageview.RoundedImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class SearchMessagesAdapter extends RecyclerView.Adapter<SearchMessagesAdapter.ViewHolder> {

    private final ArrayList<SearchMessagesModel> searchMessagesModels;
    private final Context context;
    private final Activity activity;


    public SearchMessagesAdapter(ArrayList<SearchMessagesModel> searchMessagesModels, Context context, Activity activity) {
        this.searchMessagesModels = searchMessagesModels;
        this.context = context;
        this.activity = activity;
    }

    @NonNull
    @Override
    public SearchMessagesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_search_list, parent, false);
        return new SearchMessagesAdapter.ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull SearchMessagesAdapter.ViewHolder holder, int position) {
        SearchMessagesModel searchMessagesModel = searchMessagesModels.get(position);

        Glide.with(context).load(searchMessagesModel.getProfile_image()).into(holder.avatarImageView);

        holder.nameTextView.setText(searchMessagesModel.getFull_name());

        holder.txtMess.setText(searchMessagesModel.getContent());

        holder.txtTime.setText(formatTime(searchMessagesModel.getTime()));

    }

    @Override
    public int getItemCount() {
        return searchMessagesModels.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        RoundedImageView avatarImageView, imgAvatarSeen;
        TextView nameTextView, txtTime;
        TextView txtNameSend, txtMess;
        View viewHoatDong;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarImageView = itemView.findViewById(R.id.imgAvatar);
            nameTextView = itemView.findViewById(R.id.txtName);
            txtNameSend = itemView.findViewById(R.id.txtNameSend);
            txtMess = itemView.findViewById(R.id.txtMess);
            viewHoatDong = itemView.findViewById(R.id.viewHoatDong);
            imgAvatarSeen = itemView.findViewById(R.id.imgAvatarSeen);
            txtTime = itemView.findViewById(R.id.txtTime);
        }
    }

    private String formatTime(String originalTime) {
        try {
            SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat newFormat = new SimpleDateFormat("HH:mm dd-MM-yyyy", Locale.getDefault());

            Date date = originalFormat.parse(originalTime);
            if (date != null) {
                return newFormat.format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }


}
