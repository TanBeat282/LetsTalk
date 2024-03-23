
package com.example.myapplication.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.helper.Helper;
import com.example.myapplication.server.Url_Api;
import com.example.myapplication.sharedpreferencesmanager.SharedPreferencesManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class LoginActivity extends AppCompatActivity {
    EditText edtEmail, edtPassword;
    TextView txtForgotPassword, txtSignUp;
    LinearLayout btnLogin;
    private SharedPreferencesManager sharedPreferences;

    private ImageView btn_hidePass;
    private boolean is_hide = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Helper.changeStatusBarColor(this, R.color.background);
        Helper.changeNavigationColor(this, R.color.white, true);

        sharedPreferences = new SharedPreferencesManager(getApplicationContext());

        edtEmail = findViewById(R.id.edtemail);
        edtPassword = findViewById(R.id.edtpassword);
        txtForgotPassword = findViewById(R.id.txtForgotPass);
        txtSignUp = findViewById(R.id.txtSignUp);
        btnLogin = findViewById(R.id.btnLogin);
        btn_hidePass = findViewById(R.id.btn_hidePass);

        edtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());

        btn_hidePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (is_hide) {
                    edtPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    btn_hidePass.setImageResource(R.drawable.outline_visibility_off_24);
                    is_hide = false;
                } else {
                    edtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    btn_hidePass.setImageResource(R.drawable.outline_visibility_24);
                    is_hide = true;
                }
            }
        });

        btnLogin.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please fill all field", Toast.LENGTH_SHORT).show();
            } else if (!isValidEmail(email)) {
                Toast.makeText(LoginActivity.this, "Wrong format email", Toast.LENGTH_SHORT).show();
            } else {
                login(email, password);
            }

        });

        txtForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPass2Activity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.anim_in_right, R.anim.anim_out_right);
            finish();
        });

        txtSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.anim_in_left, R.anim.anim_out_left);
            finish();
        });
    }


    private void login(String email, String password) {
        OkHttpClient client = new OkHttpClient();

        String url = Url_Api.getInstance().getLogin(email, password);
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
                                sharedPreferences.saveUserId(dataObject.getInt("user_id"));
                                sharedPreferences.saveIsLogin(true);
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                runOnUiThread(() -> {
                                    Toast.makeText(LoginActivity.this, "Đăng nhập thất bại", Toast.LENGTH_SHORT).show();
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
}