package com.example.myapplication.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.example.myapplication.R;
import com.example.myapplication.helper.Helper;

public class NameActivity extends AppCompatActivity {
    private EditText edtFirstName, edtLastName;
    private LinearLayout btnContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name);


        //change color changeStatusBarColor and changeNavigationColor
        Helper.changeStatusBarColor(this, R.color.white);
        Helper.changeNavigationColor(this, R.color.white, true);

        edtFirstName = findViewById(R.id.edtFirstName);
        edtLastName = findViewById(R.id.edtLastName);
        btnContinue = findViewById(R.id.btnContinue);
        btnContinue.setEnabled(false);
        btnContinue.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.gray_text)));

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // check if first name and last name are not empty
                if (!edtFirstName.getText().toString().isEmpty() && !edtLastName.getText().toString().isEmpty()) {
                    btnContinue.setEnabled(true);
                    btnContinue.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.text_color)));
                } else {
                    btnContinue.setEnabled(false);
                    btnContinue.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.gray_text)));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // do nothing
            }
        };

        edtFirstName.addTextChangedListener(textWatcher);
        edtLastName.addTextChangedListener(textWatcher);

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = edtFirstName.getText().toString() + " " + edtLastName.getText().toString();
                SharedPreferences sharedPreferences = getSharedPreferences("user_register", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("full_name", name);
                editor.apply();
                navigateToBirthdayActivity();

            }
        });
    }
    private void navigateToBirthdayActivity() {
        Intent intent = new Intent(NameActivity.this, DobActivity.class);
        startActivity(intent);
    }
}