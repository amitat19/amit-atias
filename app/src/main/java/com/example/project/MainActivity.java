package com.example.project;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.VideoView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private Button btnLogin;
    private TextView welcomeText;
    private VideoView videoView;
    private String user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // אתחול רכיבי ה-UI
        videoView = findViewById(R.id.video_view);
        welcomeText = findViewById(R.id.txt_welcome);
        btnLogin = findViewById(R.id.btn_login);

        // קבלת שם משתמש מתוך Intent
        Intent intent = getIntent();
        user = intent.getStringExtra("uname");

        // הפעלת הסרטון בלולאה
        playIntroVideo();

        // עדכון תצוגת שם המשתמש
        updateWelcomeMessage();

        // הגדרת לחצן התחברות
        btnLogin.setOnClickListener(v -> {
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(loginIntent);
        });

        // הגדרת Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // הגדרת DrawerLayout ו-NavigationView
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        // הגדרת Drawer Toggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // טיפול בלחיצות על פריטי הניווט
        navigationView.setNavigationItemSelectedListener(item -> {
            handleNavigationItemSelected(item);
            return true;
        });
    }

    @Override
    public void onBackPressed() {
        // סגירת ה-Drawer אם הוא פתוח
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void playIntroVideo() {
        String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.intro_video;
        Uri uri = Uri.parse(videoPath);
        videoView.setVideoURI(uri);

        // הפעלת הסרטון בלולאה
        videoView.setOnPreparedListener(mediaPlayer -> {
            mediaPlayer.setLooping(true);
            videoView.start();
        });

        videoView.setOnCompletionListener(mp -> videoView.start()); // מנגנון גיבוי ללולאה
    }

    private void updateWelcomeMessage() {
        if (user != null && !user.isEmpty()) {
            welcomeText.setText("שלום " + user + "!");
        } else {
            welcomeText.setText("שלום אורח!");
        }
    }

    private void handleNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            welcomeText.setText("בחרת בדף הבית!");
        } else if (id == R.id.nav_appointment) {
            // מעבר לעמוד קביעת תור
            Intent intent = new Intent(MainActivity.this, AppointmentActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_my_appointments) {
            startActivity(new Intent(MainActivity.this, MyAppointmentsActivity.class));
        }
        drawerLayout.closeDrawer(GravityCompat.START);
    }
}
