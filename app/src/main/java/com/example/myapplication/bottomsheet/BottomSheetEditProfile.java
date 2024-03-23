package com.example.myapplication.bottomsheet;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.activity.AddressActivity;
import com.example.myapplication.activity.AvatarActivity;
import com.example.myapplication.activity.DobActivity;
import com.example.myapplication.activity.ViewProfileActivity;
import com.example.myapplication.adapter.MediaAdapter;
import com.example.myapplication.model.MediaItem;
import com.example.myapplication.server.Url_Api;
import com.example.myapplication.sharedpreferencesmanager.SharedPreferencesManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class BottomSheetEditProfile extends BottomSheetDialogFragment {
    private final Context context;
    private final Activity activity;
    private final String data;
    private final int user_id;
    private final int action;
    private BottomSheetDialog bottomSheetDialog;

    // dob
    private int dateTime, checkBirthday;
    int selectedYear, selectedMonth, selectedDay;

    //sex
    private int sex = -2;


    //address
    private String tinh, huyen, xa;


    private EditText edt_name;
    private LinearLayout linear_note_name, linear_ok;
    private TextView txt_note_1;
    private TextView txt_note_2;

    public BottomSheetEditProfile(Context context, Activity activity, int user_id, int action, String data) {
        this.context = context;
        this.activity = activity;
        this.user_id = user_id;
        this.action = action;
        this.data = data;
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        bottomSheetDialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_edit_profile, null);
        bottomSheetDialog.setContentView(view);

        EditText edt_description = bottomSheetDialog.findViewById(R.id.edt_description);
        edt_name = bottomSheetDialog.findViewById(R.id.edt_name);
        linear_note_name = bottomSheetDialog.findViewById(R.id.linear_note_name);
        txt_note_1 = bottomSheetDialog.findViewById(R.id.txt_note_1);
        txt_note_2 = bottomSheetDialog.findViewById(R.id.txt_note_2);

        LinearLayout linear_dob = bottomSheetDialog.findViewById(R.id.linear_dob);
        EditText edt_dob = bottomSheetDialog.findViewById(R.id.edt_dob);
        ImageView btn_calender = bottomSheetDialog.findViewById(R.id.btn_calender);

        LinearLayout linear_sex = bottomSheetDialog.findViewById(R.id.linear_sex);
        RadioGroup radioGroup = bottomSheetDialog.findViewById(R.id.radioGroup);
        RadioButton radioButtonMale = bottomSheetDialog.findViewById(R.id.radioButtonMale);
        RadioButton radioButtonFemale = bottomSheetDialog.findViewById(R.id.radioButtonFemale);
        RadioButton radioButtonHide = bottomSheetDialog.findViewById(R.id.radioButtonHide);


        LinearLayout linear_address = bottomSheetDialog.findViewById(R.id.linear_address);

        linear_ok = bottomSheetDialog.findViewById(R.id.linear_ok);
        LinearLayout linear_cancel = bottomSheetDialog.findViewById(R.id.linear_cancel);

//        note
//                anh bia =0
//                        avt = 1
//                                tieu su = 2
//                                        ho ten = 3
//                                                ngay sinh = 4
//                                                        gioi tinh = 5
//                                                                dia chi = 6


        if (action == 2) {
            edt_description.setVisibility(View.VISIBLE);
            edt_description.setText(data);

            linear_ok.setOnClickListener(view1 -> {
                String description = edt_description.getText().toString().trim();
                description = description.replaceAll("\n", "[NEWLINE]");
                changeInfo(null, null, description, null, null, -2, null, user_id);
            });
        } else if (action == 3) {
            checkUpdateName(user_id);
        } else if (action == 4) {
            linear_dob.setVisibility(View.VISIBLE);
            edt_dob.setText(data);
            edt_dob.setEnabled(false);
            edt_dob.setText(convertTime2(data));

            // lay ngay hien tai
            Calendar calendar = Calendar.getInstance();
            int year_now = calendar.get(Calendar.YEAR);
            btn_calender.setOnClickListener(v -> {
                DatePickerDialog datePickerDialog = new DatePickerDialog(context, AlertDialog.THEME_HOLO_LIGHT, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view12, int yearSelect, int monthOfYear, int dayOfMonth) {
                        // Lưu trữ ngày tháng năm được chọn
                        selectedYear = yearSelect;
                        selectedMonth = monthOfYear;
                        selectedDay = dayOfMonth;

                        // Kiểm tra tuổi để xác định xem có kích hoạt nút Tiếp tục hay không
                        checkBirthday = (year_now - selectedYear);
                        if (checkBirthday < 13) {
                            linear_ok.setEnabled(false);
                            linear_ok.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.gray)));
                        } else {
                            linear_ok.setEnabled(true);
                            linear_ok.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.text_color)));
                        }

                        // Đặt lại EditText với ngày tháng năm được chọn
                        edt_dob.setText(selectedDay + "-" + (selectedMonth + 1) + "-" + selectedYear);
                    }
                }, convertStringToTime(data, 0), convertStringToTime(data, 1), convertStringToTime(data, 2));

                datePickerDialog.getDatePicker().setCalendarViewShown(false);
                datePickerDialog.getDatePicker().setSpinnersShown(true);

                // Đặt lại ngày tháng năm trên DatePickerDialog nếu đã chọn trước đó
                if (selectedYear != 0 && selectedMonth != 0 && selectedDay != 0) {
                    datePickerDialog.getDatePicker().updateDate(selectedYear, selectedMonth, selectedDay);
                }

                datePickerDialog.show();
            });

            linear_ok.setOnClickListener(view1 -> {
                String dob = convertTime(edt_dob.getText().toString().trim());
                changeInfo(null, null, null, null, dob, -2, null, user_id);
            });
        } else if (action == 5) {
            linear_sex.setVisibility(View.VISIBLE);

            if (data.equals("-1")) {
                radioButtonHide.setChecked(true);
            } else if (data.equals("1")) {
                radioButtonFemale.setChecked(true);
            } else {
                radioButtonMale.setChecked(true);
            }


            radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == R.id.radioButtonMale) {
                    sex = 0;
                } else if (checkedId == R.id.radioButtonFemale) {
                    sex = 1;
                } else if (checkedId == R.id.radioButtonHide) {
                    sex = -1;
                }
            });

            linear_ok.setOnClickListener(view1 -> {
                changeInfo(null, null, null, null, null, sex, null, user_id);
            });
        } else if (action == 6) {
            linear_address.setVisibility(View.VISIBLE);

            city("https://provinces.open-api.vn/api/");

            linear_ok.setOnClickListener(view1 -> {
                String address = xa + ", " + huyen + ", " + tinh;
                changeInfo(null, null, null, null, null, -2, address, user_id);
            });
        }


        linear_cancel.setOnClickListener(view1 -> bottomSheetDialog.dismiss());

        return bottomSheetDialog;
    }

    private void changeInfo(String profile_image, String cover_avatar, String description, String full_name, String dob, int sex, String address, int user_id) {

        OkHttpClient client = new OkHttpClient();

        String url = Url_Api.getInstance().editProfile(cover_avatar, profile_image, description, full_name, dob, sex, address, user_id);
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
                        Log.d(">>>>>>>>>>>>>>>>>>", "onResponsessssssssssssssss: " + jsonString);
                        try {
                            JSONObject jsonObject = new JSONObject(jsonString);
                            boolean status = jsonObject.getBoolean("status");
                            if (status) {
                                activity.runOnUiThread(new Runnable() {
                                    @SuppressLint("SetTextI18n")
                                    @Override
                                    public void run() {
                                        sendDataToActivity(true);
                                        Toast.makeText(context, "Chỉnh sửa thành công", Toast.LENGTH_SHORT).show();
                                        bottomSheetDialog.dismiss();

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

    private void checkUpdateName(int user_id) {

        OkHttpClient client = new OkHttpClient();

        String url = Url_Api.getInstance().checkUpdateName(user_id);
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
                                activity.runOnUiThread(new Runnable() {
                                    @SuppressLint("SetTextI18n")
                                    @Override
                                    public void run() {

                                        edt_name.setVisibility(View.VISIBLE);
                                        linear_note_name.setVisibility(View.VISIBLE);
                                        edt_name.setText(data);

                                        linear_ok.setOnClickListener(view1 -> {
                                            String name = edt_name.getText().toString().trim();
                                            // Sử dụng biểu thức chính quy để kiểm tra
                                            String regex = "^[a-zA-Z\\s]+$";
                                            Pattern pattern = Pattern.compile(regex);
                                            Matcher matcher = pattern.matcher(name);

                                            if (matcher.matches()) {
                                                changeInfo(null, null, null, name, null, -2, null, user_id);
                                            } else {
                                                Toast.makeText(context, "Họ tên không hợp lệ", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                });
                            } else {
                                String day = jsonObject.getString("message");
                                activity.runOnUiThread(new Runnable() {
                                    @SuppressLint("SetTextI18n")
                                    @Override
                                    public void run() {
                                        edt_name.setVisibility(View.VISIBLE);
                                        edt_name.setEnabled(false);
                                        linear_note_name.setVisibility(View.VISIBLE);
                                        txt_note_1.setVisibility(View.GONE);
                                        txt_note_2.setVisibility(View.VISIBLE);
                                        txt_note_2.setText("Bạn đã dạt giới hạn đổi tên trong tháng này. Bạn sẽ được đổi tên sau " + day + " nữa.");
                                        edt_name.setText(data);
                                        linear_ok.setEnabled(false);
                                        linear_ok.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.gray_text)));
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

    private String getTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return dateFormat.format(new Date());
    }

    private String convertTime(String inputDate) {
        // Define input and output date formats
        @SuppressLint("SimpleDateFormat") SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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

    private String convertTime2(String inputDate) {
        // Define input and output date formats
        @SuppressLint("SimpleDateFormat") SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy");

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

    private int convertStringToTime(String time, int action) {

        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            Date date = dateFormat.parse(time);

            if (action == 1) {
                dateTime = date.getMonth() + 1;
            } else if (action == 2) {
                dateTime = date.getDate();
            } else {
                dateTime = date.getYear() + 1900;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateTime;
    }

    private void sendDataToActivity(boolean is_load) {
        Intent intent = new Intent("is_load_info");
        Bundle bundle = new Bundle();
        bundle.putSerializable("is_load", is_load);
        intent.putExtras(bundle);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
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
                    activity.runOnUiThread(new Runnable() {
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
                Province province = new Province(provinceName, provinceId);
                provinces.add(province);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ArrayAdapter<Province> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, provinces);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinner = bottomSheetDialog.findViewById(R.id.city_spinner);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Province selectedProvince = (Province) parent.getItemAtPosition(position);
                tinh = selectedProvince.getName();
                district(selectedProvince.getId());
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
                    activity.runOnUiThread(new Runnable() {
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
        ArrayList<District> districts = new ArrayList<>(); // Assuming District is not part of AddressActivity
        try {
            JSONObject jsonObject = new JSONObject(responseData);
            JSONArray jsonArray = jsonObject.getJSONArray("districts");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject districtObject = jsonArray.getJSONObject(i);
                String districtName = districtObject.getString("name");
                int districtId = districtObject.getInt("code");
                District district = new District(districtName, districtId);
                districts.add(district);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ArrayAdapter<District> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, districts);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinner = bottomSheetDialog.findViewById(R.id.district_spinner);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                District selectedDistrict = (District) parent.getItemAtPosition(position);
                huyen = selectedDistrict.getName();
                ward(selectedDistrict.getId());
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
                    activity.runOnUiThread(new Runnable() {
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
        ArrayList<Ward> wards = new ArrayList<>(); // Assuming Ward is not part of AddressActivity
        try {
            JSONObject jsonObject = new JSONObject(responseData);
            JSONArray jsonArray = jsonObject.getJSONArray("wards");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject wardObject = jsonArray.getJSONObject(i);
                String wardName = wardObject.getString("name");
                int wardId = wardObject.getInt("code");
                Ward ward = new Ward(wardName, wardId);
                wards.add(ward);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ArrayAdapter<Ward> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, wards);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinner = bottomSheetDialog.findViewById(R.id.ward_spinner);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Ward selectedWard = (Ward) parent.getItemAtPosition(position);
                xa = selectedWard.getName();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });
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
