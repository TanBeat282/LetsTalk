package com.example.myapplication.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.bottomsheet.BottomSheetFogotPassword;
import com.example.myapplication.helper.Helper;
import com.example.myapplication.server.Url_Api;

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

public class ForgotPass2Activity extends AppCompatActivity {

    private EditText edt_email;
    private LinearLayout btn_send;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_pass2);
        Helper.changeStatusBarColor(this, R.color.background);
        Helper.changeNavigationColor(this, R.color.white, true);

        edt_email = findViewById(R.id.edt_email);
        btn_send = findViewById(R.id.btn_send);

        edt_email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Lưu giá trị của email sau mỗi lần thay đổi
                email = charSequence.toString();
                // Thực hiện các xử lý khác nếu cần thiết
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Có thể thực hiện các xử lý sau khi dữ liệu đã thay đổi
            }
        });

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (email != null) {
                    if (isValidEmail(email)) {
                        checkEmail(email);
                    } else {
                        Toast.makeText(ForgotPass2Activity.this, "Email không đúng định dạng", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ForgotPass2Activity.this, "Email không được để trống", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void checkEmail(String email) {
        OkHttpClient client = new OkHttpClient();

        String url = Url_Api.getInstance().check_Email(email);
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
                                int user_id = dataObject.getInt("user_id");
                                sendEmail(email, user_id);
                            } else {
                                runOnUiThread(() -> {
                                    BottomSheetFogotPassword bottomSheetFogotPassword = new BottomSheetFogotPassword(ForgotPass2Activity.this, ForgotPass2Activity.this, 1);
                                    bottomSheetFogotPassword.show(getSupportFragmentManager(), bottomSheetFogotPassword.getTag());
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

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Transport.send(mimeMessage);
                        addOtp(user_id, email, otp, getTime());
                    } catch (MessagingException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();

        } catch (AddressException e) {
            e.printStackTrace();
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
                                    Toast.makeText(ForgotPass2Activity.this, "Gửi mã xác nhận thành công", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(ForgotPass2Activity.this, ForgotPass1Activity.class);
                                    intent.putExtra("user_id", user_id);
                                    intent.putExtra("email", email);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.anim_in_right, R.anim.anim_out_right);
                                    finish();
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

    public boolean isValidEmail(String email) {
        String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        return email.matches(regex);
    }

    private String getTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 5);
        Date newDate = calendar.getTime();

        return dateFormat.format(newDate);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ForgotPass2Activity.this, LoginActivity.class));
        overridePendingTransition(R.anim.anim_in_left, R.anim.anim_out_left);
        finish();
    }
}