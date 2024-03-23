package com.example.myapplication.activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.helper.Helper;
import com.example.myapplication.sharedpreferencesmanager.SharedPreferencesManager;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public class QrCodeActivity extends AppCompatActivity {

    private LinearLayout btn_scan_qr_code;
    private ImageView img_qr_code, imgBack;
    private int user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code);

        btn_scan_qr_code = findViewById(R.id.btn_scan_qr_code);
        img_qr_code = findViewById(R.id.img_qr_code);
        imgBack = findViewById(R.id.imgBack);

        //change color changeStatusBarColor and changeNavigationColor
        Helper.changeStatusBarColor(this, R.color.white);
        Helper.changeNavigationColor(this, R.color.white, true);

        SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(this);
        user_id = sharedPreferencesManager.getUserId();

        createQrCodeToDeCode(String.valueOf(user_id));

        btn_scan_qr_code.setOnClickListener(view -> {
            ScanOptions scanOptions = new ScanOptions();
            scanOptions.setPrompt("Lets Talk");
            scanOptions.setBeepEnabled(false);
            scanOptions.setOrientationLocked(true);
            scanOptions.setCaptureActivity(ScanerQrCode.class);
            barLauncher.launch(scanOptions);
        });

        img_qr_code.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                // Bước 1: Chuyển đổi ImageView thành Bitmap
                BitmapDrawable drawable = (BitmapDrawable) img_qr_code.getDrawable();
                Bitmap bitmap = drawable.getBitmap();

                // Bước 2: Lưu Bitmap thành tệp PNG
                saveBitmapToGallery(bitmap);

                return true; // Trả về true để ngăn chặn sự kiện long click được xử lý tiếp
            }
        });
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


    }

    private String enCodeQrCode(String qr_code) {
        return Base64.encodeToString(qr_code.getBytes(), Base64.DEFAULT);
    }

    private void createQrCodeToDeCode(String qr_code) {

        JSONObject contentJson = new JSONObject();
        try {
            contentJson.put("user_id", qr_code);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String contentJsonString = contentJson.toString();

        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();

        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(enCodeQrCode(contentJsonString), BarcodeFormat.QR_CODE, 400, 400);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            img_qr_code.setImageBitmap(bitmap);
        } catch (WriterException e) {
            throw new RuntimeException(e);
        }
    }

    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() != null) {
            String content = result.getContents();
            if (isBase64(content)) {
                byte[] decodedBytes = Base64.decode(content, Base64.DEFAULT);
                String decodedString = new String(decodedBytes, StandardCharsets.UTF_8);
                try {
                    JSONObject contentJson = new JSONObject(decodedString);
                    int userId = contentJson.getInt("user_id");
                    Intent intent = new Intent(QrCodeActivity.this, ViewProfileActivity.class);
                    intent.putExtra("userId", userId);
                    startActivity(intent);
                } catch (JSONException e) {
                    Toast.makeText(QrCodeActivity.this, "Không có người dùng", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(QrCodeActivity.this, "Không phải mã kết bạn!", Toast.LENGTH_SHORT).show();
            }
        }
    });

    private boolean isBase64(String str) {
        try {
            byte[] decodedBytes = Base64.decode(str, Base64.DEFAULT);
            new String(decodedBytes, StandardCharsets.UTF_8);
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    // Phương thức để lưu Bitmap thành tệp PNG trong thư viện
    private void saveBitmapToGallery(Bitmap bitmap) {
        String fileName = "image_" + System.currentTimeMillis() + ".png";

        try {
            // Tạo đường dẫn tới thư mục Pictures trong thư viện
            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();

            // Tạo và mở FileOutputStream
            FileOutputStream out = new FileOutputStream(new File(path, fileName));

            // Lưu Bitmap với định dạng PNG
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

            // Đóng FileOutputStream
            out.flush();
            out.close();

            // Thông báo rằng ảnh đã được lưu
            Toast.makeText(getApplicationContext(), "Ảnh đã được lưu vào thư viện", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            // Xử lý lỗi nếu có
        }
    }
}