package com.example.project;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {
    private RecyclerView recyclerGallery;
    private TextView txtGalleryTitle;
    private GalleryAdapter adapter;
    private List<Bitmap> galleryImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        // אתחול רכיבי ה-UI
        recyclerGallery = findViewById(R.id.recycler_gallery);
        txtGalleryTitle = findViewById(R.id.txt_gallery_title);

        // טעינת התמונות
        loadGalleryImages();

        // הגדרת ה-RecyclerView
        recyclerGallery.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new GalleryAdapter(galleryImages);
        recyclerGallery.setAdapter(adapter);
    }

    private void loadGalleryImages() {
        galleryImages = new ArrayList<>();
        SharedPreferences prefs = getSharedPreferences("GalleryImages", MODE_PRIVATE);
        int imageCount = prefs.getInt("image_count", 0);
        
        for (int i = 0; i < imageCount; i++) {
            String imageString = prefs.getString("image_" + i, null);
            if (imageString != null) {
                byte[] imageBytes = Base64.decode(imageString, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                galleryImages.add(bitmap);
            }
        }
    }
} 