package com.example.project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BarberScheduleActivity extends AppCompatActivity {
    private TextView barberNameText;
    private ListView appointmentsList;
    private CustomerDataBase db;
    private String barberName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barber_schedule);

        // אתחול רכיבי ה-UI
        barberNameText = findViewById(R.id.tv_barber_name);
        appointmentsList = findViewById(R.id.lv_appointments);
        db = CustomerDataBase.getInstance(this);

        // קבלת שם הספר מהאינטנט
        barberName = getIntent().getStringExtra("barber_name");
        if (barberName == null || barberName.isEmpty()) {
            // אם לא קיבלנו את השם מהאינטנט, ננסה לקבל אותו מ-SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            barberName = sharedPreferences.getString("username", "");
        }

        // הצגת שם הספר
        barberNameText.setText("שלום " + barberName);

        // טעינת התורים
        loadAppointments();
    }

    private void loadAppointments() {
        CustomerDataBase db = CustomerDataBase.getInstance(this);
        List<String> appointments = db.getBarberAppointments(barberName);
        
        Log.d("BarberScheduleActivity", "Loaded " + appointments.size() + " appointments");
        
        // עדכון ה-ListView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_list_item_1, appointments);
        appointmentsList.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // טעינת התורים של הספר
        loadAppointments();
    }

    private void showImagePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("בחר פעולה");
        builder.setItems(new String[]{"צלם תמונה", "בחר מהגלריה"}, (dialog, which) -> {
            if (which == 0) {
                // צילום תמונה
                Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePicture.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePicture, 2);
                }
            } else {
                // בחירה מהגלריה
                Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto, 3);
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bitmap imageBitmap = null;
            if (requestCode == 2) { // צילום תמונה
                Bundle extras = data.getExtras();
                imageBitmap = (Bitmap) extras.get("data");
            } else if (requestCode == 3) { // בחירה מהגלריה
                Uri selectedImage = data.getData();
                try {
                    imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (imageBitmap != null) {
                saveImageToGallery(imageBitmap);
            }
        }
    }

    private void saveImageToGallery(Bitmap imageBitmap) {
        // המרת התמונה ל-Base64
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String imageString = Base64.encodeToString(byteArray, Base64.DEFAULT);

        // שמירת התמונה ב-SharedPreferences
        SharedPreferences prefs = getSharedPreferences("GalleryImages", MODE_PRIVATE);
        int imageCount = prefs.getInt("image_count", 0);
        
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("image_" + imageCount, imageString);
        editor.putInt("image_count", imageCount + 1);
        editor.apply();

        Toast.makeText(this, "התמונה נוספה לגלריה", Toast.LENGTH_SHORT).show();
    }
} 