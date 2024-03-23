package com.example.myapplication.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.example.myapplication.R;
import com.example.myapplication.helper.Helper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DobActivity extends AppCompatActivity {
    private EditText edtBirthday;
    private LinearLayout btnContinue;
    private int year, month, day, checkBirthday;
    private Calendar calendar;

    // Khai báo biến để lưu trữ ngày tháng năm được chọn
    int selectedYear, selectedMonth, selectedDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dob);

        edtBirthday = findViewById(R.id.edtBirthday);
        btnContinue = findViewById(R.id.btnContinue);

        Helper.changeStatusBarColor(this, R.color.white);
        Helper.changeNavigationColor(this, R.color.white, true);

        // lay ngay hien tai
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        edtBirthday.setText(day + "-" + (month + 1) + "-" + year);

        btnContinue.setEnabled(false);
        btnContinue.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.gray)));

        edtBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(DobActivity.this, AlertDialog.THEME_HOLO_LIGHT, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int yearSelect, int monthOfYear, int dayOfMonth) {
                        // Lưu trữ ngày tháng năm được chọn
                        selectedYear = yearSelect;
                        selectedMonth = monthOfYear;
                        selectedDay = dayOfMonth;

                        // Kiểm tra tuổi để xác định xem có kích hoạt nút Tiếp tục hay không
                        checkBirthday = (year - selectedYear);
                        if (checkBirthday < 13) {
                            btnContinue.setEnabled(false);
                            btnContinue.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.gray)));
                        } else {
                            btnContinue.setEnabled(true);
                            btnContinue.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.text_color)));
                        }

                        // Đặt lại EditText với ngày tháng năm được chọn
                        edtBirthday.setText(selectedDay + "-" + (selectedMonth + 1) + "-" + selectedYear);
                    }
                }, year, month, day);

                datePickerDialog.getDatePicker().setCalendarViewShown(false);
                datePickerDialog.getDatePicker().setSpinnersShown(true);

                // Đặt lại ngày tháng năm trên DatePickerDialog nếu đã chọn trước đó
                if (selectedYear != 0 && selectedMonth != 0 && selectedDay != 0) {
                    datePickerDialog.getDatePicker().updateDate(selectedYear, selectedMonth, selectedDay);
                }

                datePickerDialog.show();
            }
        });
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String birthday = edtBirthday.getText().toString();
                SharedPreferences sharedPreferences = getSharedPreferences("user_register", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("dob", convertTime(birthday));
                editor.apply();
                navigateToGenderActivity();
            }
        });
    }

    private String convertTime(String inputDate) {
        // Define input and output date formats
        SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            // Parse the input date
            Date date = inputFormat.parse(inputDate);

            // Format the date to the desired format
            String formattedDate = outputFormat.format(date);

            // Output the result
            return formattedDate;
        } catch (ParseException e) {
            e.printStackTrace();
            // Handle the exception, you can return a default value or rethrow the exception
            return "Invalid Date";
        }
    }

    private void navigateToGenderActivity() {
        Intent intent = new Intent(DobActivity.this, GenderActivity.class);
        startActivity(intent);
    }
}