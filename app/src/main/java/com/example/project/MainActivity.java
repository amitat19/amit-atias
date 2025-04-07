package com.example.project;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private Button btnLogin, btnShowLocation, btnShowDetails, btnAboutBarbershop, btnBarberLogin;
    private TextView welcomeText, txtAddress, txtBarbershopInfo;
    private VideoView videoView;
    private ImageView imgBarbershop, imgRomNav;
    private CardView cardBarbershop, cardInfo;
    private String user;
    private boolean isShowingText = false; // משתנה למעקב אחרי מצב התצוגה
    private NavigationView navigationView;
    private boolean isInfoVisible = false;
    private boolean isAddressVisible = false;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // אתחול רכיבי ה-UI
        videoView = findViewById(R.id.video_view);
        welcomeText = findViewById(R.id.txt_welcome);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        btnLogin = findViewById(R.id.btn_login);
        btnBarberLogin = findViewById(R.id.btn_barber_login);
        imgBarbershop = findViewById(R.id.img_barbershop);
        imgRomNav = findViewById(R.id.img_romnav);
        btnShowLocation = findViewById(R.id.btn_show_location);
        btnShowDetails = findViewById(R.id.btn_show_details);
        btnAboutBarbershop = findViewById(R.id.btn_about_barbershop);
        txtAddress = findViewById(R.id.txt_address);
        txtBarbershopInfo = findViewById(R.id.txt_barbershop_info);
        cardInfo = findViewById(R.id.card_info);
        cardBarbershop = findViewById(R.id.card_barbershop);
        sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);

        // קבלת שם משתמש מתוך Intent
        Intent intent = getIntent();
        user = intent.getStringExtra("uname");

        // הפעלת הסרטון בלולאה
        playIntroVideo();

        // עדכון תצוגת שם המשתמש
        updateWelcomeMessage();

        // בדיקה אם המשתמש מחובר
        String username = sharedPreferences.getString("username", "");
        boolean isBarber = sharedPreferences.getBoolean("isBarber", false);

        if (!username.isEmpty()) {
            welcomeText.setText("שלום " + username);
            btnLogin.setVisibility(View.GONE);
            btnBarberLogin.setVisibility(View.GONE);
            
            // הגדרת התפריט המתאים
            if (isBarber) {
                navigationView.getMenu().clear();
                navigationView.inflateMenu(R.menu.barber_menu);
            } else {
                navigationView.getMenu().clear();
                navigationView.inflateMenu(R.menu.user_menu);
            }
        } else {
            welcomeText.setText("שלום אורח");
            btnLogin.setVisibility(View.VISIBLE);
            btnBarberLogin.setVisibility(View.VISIBLE);
        }

        // טיפול בלחיצות על כפתורי התחברות
        btnLogin.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        });

        btnBarberLogin.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, BarberLoginActivity.class));
        });

        // הגדרת לחצן הצגת מיקום
        btnShowLocation.setOnClickListener(v -> openGoogleMaps());

        // הגדרת לחצן הצגת פרטי מספרה
        btnShowDetails.setOnClickListener(v -> showBarbershopDetails());

        // כפתור להחלפה בין תמונה לטקסט
        btnAboutBarbershop.setOnClickListener(v -> toggleBarbershopInfo());

        // הגדרת Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // הגדרת Drawer Toggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // טיפול בלחיצות על פריטי הניווט
        navigationView.setNavigationItemSelectedListener(this);

        // יצירת ערוץ התראות
        createNotificationChannel();
    }

    private void toggleBarbershopInfo() {
        if (isShowingText) {
            txtBarbershopInfo.setVisibility(View.GONE);
            imgBarbershop.setVisibility(View.VISIBLE);
            btnAboutBarbershop.setText("קצת על המספרה");
        } else {
            imgBarbershop.setVisibility(View.GONE);
            txtBarbershopInfo.setVisibility(View.VISIBLE);
            txtBarbershopInfo.setText("המספרה שלנו ממוקמת בכפר סבא, הכרמל 20, ומספקת שירות מקצועי ברמה הגבוהה ביותר.");
            btnAboutBarbershop.setText("חזור לתמונה");
        }
        isShowingText = !isShowingText;
    }

    private void openGoogleMaps() {
        Uri gmmIntentUri = Uri.parse("geo:32.17870,34.90689?q=Swissers Barber Shop");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    private void showBarbershopDetails() {
        imgRomNav.setVisibility(View.GONE);
        txtAddress.setVisibility(View.VISIBLE);
        txtAddress.setText("הכרמל 20 כפר סבא\nטלפון: 054-2292255");
    }

    @Override
    public void onBackPressed() {
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

        videoView.setOnPreparedListener(mediaPlayer -> {
            mediaPlayer.setLooping(true);
            videoView.start();
        });

        videoView.setOnCompletionListener(mp -> videoView.start());
    }

    private void updateWelcomeMessage() {
        if (user != null && !user.isEmpty()) {
            welcomeText.setText("שלום " + user + "!");
        } else {
            welcomeText.setText("שלום אורח!");
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.nav_new_appointment) {
            startActivity(new Intent(MainActivity.this, AppointmentActivity.class));
        } else if (id == R.id.nav_my_appointments) {
            startActivity(new Intent(MainActivity.this, MyAppointmentsActivity.class));
        } else if (id == R.id.nav_barber_schedule) {
            // קבלת שם הספר מההתחברות
            String barberName = sharedPreferences.getString("username", "");
            Intent intent = new Intent(MainActivity.this, BarberScheduleActivity.class);
            intent.putExtra("barber_name", barberName);
            startActivity(intent);
        } else if (id == R.id.nav_logout) {
            // מחיקת פרטי ההתחברות
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
            
            // רענון המסך
            recreate();
        }
        
        drawerLayout.closeDrawers();
        return true;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "appointment_channel",
                    "תזכורות תור",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("ערוץ לתזכורות תור");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // רענון מצב ההתחברות
        String username = sharedPreferences.getString("username", "");
        boolean isBarber = sharedPreferences.getBoolean("isBarber", false);
        
        if (!username.isEmpty()) {
            welcomeText.setText("שלום " + username);
            btnLogin.setVisibility(View.GONE);
            btnBarberLogin.setVisibility(View.GONE);
        } else {
            welcomeText.setText("שלום אורח");
            btnLogin.setVisibility(View.VISIBLE);
            btnBarberLogin.setVisibility(View.VISIBLE);
        }
    }
}
