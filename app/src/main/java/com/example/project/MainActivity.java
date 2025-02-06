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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // אתחול רכיבי ה-UI
        videoView = findViewById(R.id.video_view);
        welcomeText = findViewById(R.id.txt_welcome);
        btnLogin = findViewById(R.id.btn_login);

        // הפעלת הסרטון
        playIntroVideo();

        // עדכון טקסט "שלום אורח" אם אין משתמש מחובר
        String username = getLoggedInUser();
        if (username != null) {
            welcomeText.setText("שלום " + username + "!");
        } else {
            welcomeText.setText("שלום אורח!");
        }

        // לחצן מעבר למסך התחברות
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
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

        // טיפול באירועי לחיצה בתפריט הניווט
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    welcomeText.setText("בחרת בדף הבית!");
                } else if (id == R.id.nav_appointment) {
                    welcomeText.setText("בחרת לקבוע תור");
                }

                // סגירת ה-Drawer לאחר לחיצה
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
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

        // הגדרת לולאה על הווידאו
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.setLooping(true);
            }
        });

        videoView.start();
    }

    private String getLoggedInUser() {
        // בדיקה האם יש משתמש מחובר (כרגע מחזיר null כדוגמה)
        return null;
    }
}
