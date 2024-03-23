package com.example.myapplication.helper;

import com.example.myapplication.model.MessagesListModel;
import com.example.myapplication.model.MessagesModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class SortMessagesByTime {
    public static void sortMessagesListByTime(ArrayList<MessagesListModel> messages) {
        messages.sort((message1, message2) -> {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            try {
                Date time1 = dateFormat.parse(message1.getTime());
                Date time2 = dateFormat.parse(message2.getTime());
                if (time1 != null && time2 != null) {
                    return time2.compareTo(time1);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return 0;
        });
    }
    public static void sortMessagesByTime(ArrayList<MessagesModel> messages) {
        messages.sort((message1, message2) -> {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            try {
                Date time1 = dateFormat.parse(message1.getTime());
                Date time2 = dateFormat.parse(message2.getTime());
                if (time1 != null && time2 != null) {
                    return time2.compareTo(time1);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return 0;
        });
    }
}
