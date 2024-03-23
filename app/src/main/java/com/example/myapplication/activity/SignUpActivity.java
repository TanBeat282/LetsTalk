package com.example.myapplication.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.bottomsheet.BottomSheetFogotPassword;
import com.example.myapplication.helper.Helper;
import com.example.myapplication.server.Url_Api;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class SignUpActivity extends AppCompatActivity {
    private EditText edtEmail, edtPassword, edtConfirmPassword, edtName;
    private LinearLayout btnConfirm;
    private RadioButton rbAgree;
    private String email, name, pass, confirmPass;
    private TextView txtSignUp;
    private ImageView btn_hidePass, btn_hidePass2;
    private boolean is_hide = true, is_hide2 = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        initializeViews();
        setStatusBarAndNavigationColor();
        clearSharedPreferences();

        edtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
        edtConfirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());

        addTextWatcher(edtEmail, "email");
        addTextWatcher(edtName, "name");
        addTextWatcher(edtPassword, "password");
        addTextWatcher(edtConfirmPassword, "confirmPassword");

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
        btn_hidePass2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (is_hide2) {
                    edtConfirmPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    btn_hidePass2.setImageResource(R.drawable.outline_visibility_off_24);
                    is_hide2 = false;
                } else {
                    edtConfirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    btn_hidePass2.setImageResource(R.drawable.outline_visibility_24);
                    is_hide2 = true;
                }
            }
        });

        btnConfirm.setOnClickListener(v -> {
            if (checkForm(email, name, pass, confirmPass)) {
                checkEmail(email, pass, name);
            }
        });
        txtSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                overridePendingTransition(R.anim.anim_in_right, R.anim.anim_out_right);
                finish();
            }
        });
    }

    private void initializeViews() {
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtName = findViewById(R.id.edtName);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnConfirm = findViewById(R.id.btnConfirm);
        rbAgree = findViewById(R.id.rbAgree);
        txtSignUp = findViewById(R.id.txtSignUp);
        btn_hidePass = findViewById(R.id.btn_hidePass);
        btn_hidePass2 = findViewById(R.id.btn_hidePass2);


    }

    private void setStatusBarAndNavigationColor() {
        Helper.changeStatusBarColor(this, R.color.background);
        Helper.changeNavigationColor(this, R.color.white, true);
    }

    private void clearSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_register", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    private void addTextWatcher(EditText editText, String valueReference) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                switch (valueReference) {
                    case "email":
                        email = charSequence.toString();
                        break;
                    case "name":
                        name = charSequence.toString().trim();
                        break;
                    case "password":
                        pass = charSequence.toString().trim();
                        break;
                    case "confirmPassword":
                        confirmPass = charSequence.toString().trim();
                        break;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    private void checkEmail(String email, String full_name, String password) {
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
                try {
                    if (response.isSuccessful()) {
                        handleSuccessfulResponse(response);
                    } else {
                        handleUnsuccessfulResponse();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("SignUpActivity", "Exception during network call: " + e.getMessage());
                }
            }
        });
    }

    private void handleSuccessfulResponse(Response response) throws IOException, JSONException {
        ResponseBody responseBody = response.body();
        if (responseBody != null) {
            String jsonString = responseBody.string();
            JSONObject jsonObject = new JSONObject(jsonString);
            boolean status = jsonObject.getBoolean("status");
            if (status) {
                runOnUiThread(() -> {
                    BottomSheetFogotPassword bottomSheetFogotPassword = new BottomSheetFogotPassword(SignUpActivity.this, SignUpActivity.this, 0);
                    bottomSheetFogotPassword.show(getSupportFragmentManager(), bottomSheetFogotPassword.getTag());
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        signUp(email, name, pass);
                    }
                });
            }
        }
    }

    private void handleUnsuccessfulResponse() {
        Log.e("SignUpActivity", "Unsuccessful response");
        Toast.makeText(SignUpActivity.this, "Thất bại", Toast.LENGTH_SHORT).show();
    }

    private boolean checkForm(String email, String name, String password, String confirmPassword) {
        if (email == null || email.isEmpty()) {
            showToast("Vui lòng nhập email");
            return false;
        }
        if (!isValidEmail(email)) {
            showToast("Email không đúng định dạng. Vui lòng kiểm tra lại.");
            return false;
        }
        if (name == null || name.isEmpty()) {
            showToast("Vui lòng nhập Tên");
            return false;
        }
        if (password == null || password.isEmpty()) {
            showToast("Vui lòng nhập mật khẩu");
            return false;
        }
        if (confirmPassword == null || confirmPassword.isEmpty()) {
            showToast("Vui lòng nhập xác nhận mật khẩu");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            showToast("Mật khẩu không giống nhau");
            return false;
        }
        if (!isValidPassword(password)) {
            showToast("Mật khẩu không đúng định dạng");
        }
        if (!rbAgree.isChecked()) {
            showToast("Bạn chưa chấp nhận điều khoản");
            return false;
        }

        return true;
    }


    private void showToast(String message) {
        Toast.makeText(SignUpActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void signUp(String email, String full_name, String password) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(Url_Api.getInstance().signUp(email, enCodeQrCode(password), full_name))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        handleSignUpResponse(response);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showToast("Thất bại");
                        }
                    });
                }
            }
        });
    }

    private void handleSignUpResponse(Response response) throws IOException, JSONException {
        ResponseBody responseBody = response.body();
        if (responseBody != null) {
            String jsonString = responseBody.string();
            Log.d(">>>>>>>>>>>>>>>", "handleSignUpResponse: " + jsonString);
            JSONObject jsonObject = new JSONObject(jsonString);
            boolean status = jsonObject.getBoolean("status");
            if (status) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showToast("Đăng kí thành công");
                        startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                        overridePendingTransition(R.anim.anim_in_right, R.anim.anim_out_right);
                        finish();
                    }
                });
            }
        }
    }

    public boolean isValidEmail(String email) {
        String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        return email.matches(regex);
    }

    private boolean isValidPassword(String password) {
        // Biểu thức chính quy kiểm tra mật khẩu
        String passwordPattern = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$";

        // So sánh mật khẩu với biểu thức chính quy
        return password.matches(passwordPattern);
    }

    private String enCodeQrCode(String qr_code) {
        return Base64.encodeToString(qr_code.getBytes(), Base64.DEFAULT);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
        overridePendingTransition(R.anim.anim_in_right, R.anim.anim_out_right);
        finish();
    }
}
