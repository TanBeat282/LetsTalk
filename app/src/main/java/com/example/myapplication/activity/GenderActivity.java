package com.example.myapplication.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.example.myapplication.R;
import com.example.myapplication.helper.Helper;

public class GenderActivity extends AppCompatActivity {
    private EditText edtGender;
    private RadioGroup radioGroup;
    private RadioButton radioButtonMale, radioButtonFemale, radioButtonHide;
    private LinearLayout btnContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gender);
        edtGender = findViewById(R.id.edtGender);
        radioGroup = findViewById(R.id.radioGroup);
        radioButtonMale = findViewById(R.id.radioButtonMale);
        radioButtonFemale = findViewById(R.id.radioButtonFemale);
        radioButtonHide = findViewById(R.id.radioButtonHide);
        btnContinue = findViewById(R.id.btnContinue);
        btnContinue.setEnabled(false);
        btnContinue.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.gray)));

        Helper.changeStatusBarColor(this, R.color.white);
        Helper.changeNavigationColor(this, R.color.white, true);

        SharedPreferences sharedPreferences = getSharedPreferences("myinfo", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();


        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radioButtonMale) {
                    edtGender.setText("Nam");
                    btnContinue.setEnabled(true);
                    btnContinue.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.text_color)));
                } else if (checkedId == R.id.radioButtonFemale) {
                    edtGender.setText("Nữ");
                    btnContinue.setEnabled(true);
                    btnContinue.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.text_color)));
                } else if (checkedId == R.id.radioButtonHide) {
                    edtGender.setText("Không tiết lộ");
                    btnContinue.setEnabled(true);
                    btnContinue.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.text_color)));
                }
            }
        });
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences("user_register", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                String gender = edtGender.getText().toString();
                if (gender.equals("Nam")) {
                    editor.putInt("sex", 0);
                } else if (gender.equals("Nữ")) {
                    editor.putInt("sex", 1);
                } else {
                    editor.putInt("sex", -1);
                }
                editor.apply();
                navigateToAddressActivity();
            }
        });
    }

    private void navigateToAddressActivity() {
        Intent intent = new Intent(GenderActivity.this, AddressActivity.class);
        startActivity(intent);
    }
}