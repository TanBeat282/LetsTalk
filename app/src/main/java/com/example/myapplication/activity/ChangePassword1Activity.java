package com.example.myapplication.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.helper.Helper;
import com.example.myapplication.server.Url_Api;
import com.example.myapplication.sharedpreferencesmanager.SharedPreferencesManager;
import com.google.android.exoplayer2.C;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.Random;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ChangePassword1Activity extends AppCompatActivity {

    private TextView txt_content, txtSignUp;
    private EditText edt_otp;
    private LinearLayout btn_verify;
    private int user_id;
    private String otp, email;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password1);

        txt_content = findViewById(R.id.txt_content);
        edt_otp = findViewById(R.id.edt_otp);
        btn_verify = findViewById(R.id.btn_verify);
        txtSignUp = findViewById(R.id.txtSignUp);

        Helper.changeStatusBarColor(this, R.color.white);
        Helper.changeNavigationColor(this, R.color.white, true);

        SharedPreferencesManager sharedPreferences = new SharedPreferencesManager(getApplicationContext());
        user_id = sharedPreferences.getUserId();
        getInfo(user_id);

        btn_verify.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.gray)));


        edt_otp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                otp = charSequence.toString();
                if (otp.length() < 6) {
                    btn_verify.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.gray)));
                } else {
                    btn_verify.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.text_color)));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });


        txtSignUp.setOnClickListener(view -> {
            getInfo(user_id);
            txtSignUp.setTextColor(R.color.gray_text);
            txtSignUp.setEnabled(false);
            edt_otp.setText("");
        });
        btn_verify.setOnClickListener(view -> {
            if (otp != null) {
                if (otp.length() == 6) {
                    checkOtp(user_id, email, otp);
                } else {
                    Toast.makeText(ChangePassword1Activity.this, "OTP phải đủ 6 kí tự", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ChangePassword1Activity.this, "OTP không được bỏ trống", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void checkOtp(int user_id, String email, String otp) {
        Log.d(">>>>>>>>>>>>>>", "checkOtp: " + user_id + email + otp + getTime());

        OkHttpClient client = new OkHttpClient();

        String url = Url_Api.getInstance().checkOtp(user_id, email, otp, getTime());
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
                        Log.d(">>>>>>>>>>>>>>", "checkOtpsfdsf: " + jsonString);
                        try {
                            JSONObject jsonObject = new JSONObject(jsonString);
                            boolean status = jsonObject.getBoolean("status");
                            int type = jsonObject.getInt("type");
                            if (status) {
                                runOnUiThread(() -> {
                                    Intent intent = new Intent(ChangePassword1Activity.this, ChangePassword2Activity.class);
                                    intent.putExtra("user_id", user_id);
                                    intent.putExtra("email", email);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.anim_in_left, R.anim.anim_out_left);
                                    finish();
                                });
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (type == -1) {
                                            Toast.makeText(ChangePassword1Activity.this, "OTP hết hiệu lực", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(ChangePassword1Activity.this, "OTP không hợp lệ", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }


        });
    }

    private void getInfo(int user_id) {
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
                                email = dataObject.getString("email");
                                runOnUiThread(() -> sendEmail(email, user_id));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        });
    }

    private void sendEmail(String email, int user_id) {
        try {
            String stringSenderEmail = "letstalks.community@gmail.com";
            String stringPasswordSenderEmail = "dspv womm lqfk gotl";
            String stringReceiverEmail = email;
            String otp = generateRandomFourDigitNumber();

            String stringHost = "smtp.gmail.com";

            Properties properties = System.getProperties();

            properties.put("mail.smtp.host", stringHost);
            properties.put("mail.smtp.port", "465");
            properties.put("mail.smtp.ssl.enable", "true");
            properties.put("mail.smtp.auth", "true");

            javax.mail.Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(stringSenderEmail, stringPasswordSenderEmail);
                }
            });

            MimeMessage mimeMessage = new MimeMessage(session);
            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(stringReceiverEmail));

            mimeMessage.setSubject("LetsTalk");
            mimeMessage.setText("Mã xác minh đặt lại mật khẩu của bạn là: " + otp + "\nMã xác nhận có hiệu lực trong 5 phút. Vui lòng không cung cấp mã xác minh cho người khác\nLetsTalk.");

            Thread thread = new Thread(() -> {
                try {
                    Transport.send(mimeMessage);
                    addOtp(user_id, email, otp, getTime2());
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            });
            thread.start();

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private String generateRandomFourDigitNumber() {
        Random random = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            int randomNumber = random.nextInt(10);
            randomStringBuilder.append(randomNumber);
        }
        return randomStringBuilder.toString();
    }

    private void addOtp(int user_id, String email, String otp, String time) {

        OkHttpClient client = new OkHttpClient();

        String url = Url_Api.getInstance().addOtp(user_id, email, otp, time);
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
                                runOnUiThread(() -> {
                                    edt_otp.setEnabled(true);
                                    txt_content.setVisibility(View.VISIBLE);
                                    txt_content.setText("Mã cũa bạn đã được gửi đến " + email);
                                    Toast.makeText(ChangePassword1Activity.this, "Gửi mã xác nhận thành công", Toast.LENGTH_SHORT).show();
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }


        });
    }

    private String getTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return dateFormat.format(new Date());
    }

    private String getTime2() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 5);
        Date newDate = calendar.getTime();

        return dateFormat.format(newDate);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.anim_in_right, R.anim.anim_out_right);
        finish();
    }
}