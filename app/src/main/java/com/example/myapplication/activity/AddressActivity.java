package com.example.myapplication.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.bottomsheet.BottomSheetFogotPassword;
import com.example.myapplication.helper.Helper;
import com.example.myapplication.server.Url_Api;
import com.example.myapplication.sharedpreferencesmanager.SharedPreferencesManager;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class AddressActivity extends AppCompatActivity {
    private String tinh, huyen, xa, emailUser;
    private EditText edtAdress;
    private LinearLayout btnContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);
        edtAdress = findViewById(R.id.edtAdress);
        btnContinue = findViewById(R.id.btnContinue);
        Helper.changeStatusBarColor(this, R.color.white);
        Helper.changeNavigationColor(this, R.color.white, true);

        SharedPreferences sharedPreferences = getSharedPreferences("emailUser", MODE_PRIVATE);
        emailUser = sharedPreferences.getString("email", "");

        city("https://provinces.open-api.vn/api/");

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAccount();
            }
        });

    }

    private void city(String api) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(api)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String responseData = response.body().string();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            renderDataCity(responseData, "city");
                        }
                    });
                }
            }
        });
    }

    private void renderDataCity(String responseData, String select) {
        ArrayList<Province> provinces = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(responseData);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String provinceName = jsonObject.getString("name");
                int provinceId = jsonObject.getInt("code");
                AddressActivity.Province province = new AddressActivity.Province(provinceName, provinceId);
                provinces.add(province);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ArrayAdapter<Province> adapter = new ArrayAdapter<AddressActivity.Province>(AddressActivity.this, android.R.layout.simple_spinner_item, provinces);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinner = findViewById(R.id.city_spinner);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                AddressActivity.Province selectedProvince = (AddressActivity.Province) parent.getItemAtPosition(position);
                tinh = selectedProvince.getName();
                district(selectedProvince.getId());
                edtAdress.setText(xa + ", " + huyen + ", " + tinh);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });
    }

    private void district(int provinceId) {
        String url = "https://provinces.open-api.vn/api/p/" + provinceId + "?depth=2";
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String responseData = response.body().string();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            renderDataDistrict(responseData, "district");
                        }
                    });
                }
            }
        });
    }

    private void renderDataDistrict(String responseData, String select) {
        ArrayList<AddressActivity.District> districts = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(responseData);
            JSONArray jsonArray = jsonObject.getJSONArray("districts");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject districtObject = jsonArray.getJSONObject(i);
                String districtName = districtObject.getString("name");
                int districtId = districtObject.getInt("code");
                AddressActivity.District district = new AddressActivity.District(districtName, districtId);
                districts.add(district);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ArrayAdapter<AddressActivity.District> adapter = new ArrayAdapter<AddressActivity.District>(AddressActivity.this, android.R.layout.simple_spinner_item, districts);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinner = findViewById(R.id.district_spinner);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                AddressActivity.District selectedDistrict = (AddressActivity.District) parent.getItemAtPosition(position);
                huyen = selectedDistrict.getName();
                ward(selectedDistrict.getId());
                edtAdress.setText(xa + ", " + huyen + ", " + tinh);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });
    }

    private void ward(int provinceId) {
        String url = "https://provinces.open-api.vn/api/d/" + provinceId + "?depth=2";
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String responseData = response.body().string();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            renderDataWard(responseData, "wards");
                        }
                    });
                }
            }
        });
    }

    private void renderDataWard(String responseData, String select) {
        ArrayList<AddressActivity.Ward> wards = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(responseData);
            JSONArray jsonArray = jsonObject.getJSONArray("wards");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject districtObject = jsonArray.getJSONObject(i);
                String districtName = districtObject.getString("name");
                int districtId = districtObject.getInt("code");
                AddressActivity.Ward ward = new AddressActivity.Ward(districtName, districtId);
                wards.add(ward);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ArrayAdapter<AddressActivity.Ward> adapter = new ArrayAdapter<AddressActivity.Ward>(AddressActivity.this, android.R.layout.simple_spinner_item, wards);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinner = findViewById(R.id.ward_spinner);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                AddressActivity.Ward selectedDistrict = (AddressActivity.Ward) parent.getItemAtPosition(position);
                xa = selectedDistrict.getName();
                edtAdress.setText(xa + ", " + huyen + ", " + tinh);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });
    }

    private void navigateToAddressActivity() {
        Intent intent = new Intent(AddressActivity.this, AvatarActivity.class);
        startActivity(intent);
    }

    private void setAccount() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_register", Context.MODE_PRIVATE);

        String email = sharedPreferences.getString("email", "");
        String password = sharedPreferences.getString("password", "");
        String full_name = sharedPreferences.getString("full_name", "");
        String dob = sharedPreferences.getString("dob", "");
        String address = edtAdress.getText().toString();
        String joined_in = getTime();
        String profile_image = enCodeQrCode("https://firebasestorage.googleapis.com/v0/b/letstalk-3d1c5.appspot.com/o/avatar%2Favatar.jpg?alt=media&token=12ef218e-39d9-4257-88c5-2c359e60998e");


        int sex = sharedPreferences.getInt("sex", -1);

        registerUser(email, password, full_name, dob, profile_image, address, joined_in, sex);

    }
    private String enCodeQrCode(String qr_code) {
        return Base64.encodeToString(qr_code.getBytes(), Base64.DEFAULT);
    }
    private void registerUser(String email, String password, String full_name, String dob, String profile_image, String address, String join, int sex) {
        OkHttpClient client = new OkHttpClient();

        String url = Url_Api.getInstance().signUp(email, password, full_name);
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
                                int user_id = jsonObject.getInt("data");
                                runOnUiThread(() -> {
                                    SharedPreferences sharedPreferences = getSharedPreferences("user_register", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.clear();
                                    editor.apply();

                                    SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(AddressActivity.this);
                                    sharedPreferencesManager.saveUserId(user_id);
                                    sharedPreferencesManager.saveIsLogin(true);

                                    navigateToAddressActivity();
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
        String currentTime = dateFormat.format(new Date());
        return currentTime;
    }


    public class Province {
        private String name;
        private int id;

        public Province(String name, int id) {
            this.name = name;
            this.id = id;
        }

        @Override
        public String toString() {
            return name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public class District {
        private String name;
        private int id;

        public District(String name, int id) {
            this.name = name;
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public int getId() {
            return id;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public class Ward {
        private String name;
        private int code;

        public Ward(String name, int code) {
            this.name = name;
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public int getCode() {
            return code;
        }

        @Override
        public String toString() {
            return name;
        }
    }

}