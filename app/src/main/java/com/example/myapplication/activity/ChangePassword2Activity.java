package com.example.myapplication.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.helper.Helper;
import com.example.myapplication.server.Url_Api;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ChangePassword2Activity extends AppCompatActivity {
    private LinearLayout btn_xacnhan;
    private ImageView btn_hidePass, btn_hidePass2;
    private EditText edt_pass, edt_pass2;
    private String email, pass, pass2;
    private int user_id;
    private boolean is_hide = true, is_hide2 = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password2);

        Helper.changeStatusBarColor(this, R.color.white);
        Helper.changeNavigationColor(this, R.color.white, true);

        edt_pass = findViewById(R.id.edt_pass);
        edt_pass2 = findViewById(R.id.edt_pass2);
        btn_xacnhan = findViewById(R.id.btn_verify);
        btn_hidePass = findViewById(R.id.btn_hidePass);
        btn_hidePass2 = findViewById(R.id.btn_hidePass2);

        email = getIntent().getStringExtra("email");
        user_id = getIntent().getIntExtra("user_id", 0);

        edt_pass.setTransformationMethod(PasswordTransformationMethod.getInstance());
        edt_pass2.setTransformationMethod(PasswordTransformationMethod.getInstance());


        btn_hidePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (is_hide) {
                    edt_pass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    btn_hidePass.setImageResource(R.drawable.outline_visibility_off_24);
                    is_hide = false;
                } else {
                    edt_pass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    btn_hidePass.setImageResource(R.drawable.outline_visibility_24);
                    is_hide = true;
                }
            }
        });
        btn_hidePass2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (is_hide2) {
                    edt_pass2.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    btn_hidePass2.setImageResource(R.drawable.outline_visibility_off_24);
                    is_hide2 = false;
                } else {
                    edt_pass2.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    btn_hidePass2.setImageResource(R.drawable.outline_visibility_24);
                    is_hide2 = true;
                }
            }
        });


        edt_pass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Lưu giá trị của email sau mỗi lần thay đổi
                pass = charSequence.toString();
                // Thực hiện các xử lý khác nếu cần thiết
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Có thể thực hiện các xử lý sau khi dữ liệu đã thay đổi
            }
        });
        edt_pass2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Lưu giá trị của email sau mỗi lần thay đổi
                pass2 = charSequence.toString();
                // Thực hiện các xử lý khác nếu cần thiết
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Có thể thực hiện các xử lý sau khi dữ liệu đã thay đổi
            }
        });
        btn_xacnhan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pass == null || pass.isEmpty() || pass2 == null || pass2.isEmpty()) {
                    Toast.makeText(ChangePassword2Activity.this, "Vui lòng nhập đầy đủ mật khẩu", Toast.LENGTH_SHORT).show();
                } else {
                    if (pass.equals(pass2)) {
                        if (isValidPassword(pass)) {
                            // Mật khẩu hợp lệ, thực hiện thay đổi mật khẩu
                            changePass(user_id, pass);
                        } else {
                            Toast.makeText(ChangePassword2Activity.this, "Mật khẩu không đúng định dạng", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ChangePassword2Activity.this, "Vui lòng nhập mật khẩu giống nhau", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }

    private String enCodeQrCode(String qr_code) {
        return Base64.encodeToString(qr_code.getBytes(), Base64.DEFAULT);
    }


    private void changePass(int user_id, String pass) {

        OkHttpClient client = new OkHttpClient();

        String url = Url_Api.getInstance().changePass(user_id, enCodeQrCode(pass));
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
                                    Toast.makeText(ChangePassword2Activity.this, "Thay đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                                    overridePendingTransition(R.anim.anim_in_right, R.anim.anim_out_right);
                                    finish();
                                });
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(ChangePassword2Activity.this, "Lỗi khi đặt mật khẩu", Toast.LENGTH_SHORT).show();
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

    // Hàm kiểm tra mật khẩu
    private boolean isValidPassword(String password) {
        // Biểu thức chính quy kiểm tra mật khẩu
        String passwordPattern = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$";

        // So sánh mật khẩu với biểu thức chính quy
        return password.matches(passwordPattern);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.anim_in_right, R.anim.anim_out_right);
        finish();
    }
}