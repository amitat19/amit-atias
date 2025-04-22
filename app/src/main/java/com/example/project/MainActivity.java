package com.example.project;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // אתחול ה-ActivityResultLaunchers
        cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        Bundle extras = data.getExtras();
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        if (imageBitmap != null) {
                            saveImageToGallery(imageBitmap);
                            Toast.makeText(this, "התמונה נשמרה בהצלחה", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        );

        galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        try {
                            Uri imageUri = data.getData();
                            Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                            if (imageBitmap != null) {
                                saveImageToGallery(imageBitmap);
                                Toast.makeText(this, "התמונה נשמרה בהצלחה", Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        );



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

        // הגדרת Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.swisserslogoremovebg);
        }

        // קבלת שם משתמש מתוך Intent או SharedPreferences
        Intent intent = getIntent();
        user = intent.getStringExtra("uname");
        if (user == null || user.isEmpty()) {
            user = sharedPreferences.getString("username", "");
        }

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
            Intent intent = new Intent(MainActivity.this, AppointmentActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_my_appointments) {
            Intent intent = new Intent(MainActivity.this, MyAppointmentsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_barber_schedule) {
            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            String barberName = sharedPreferences.getString("username", "");
            Intent intent = new Intent(MainActivity.this, BarberScheduleActivity.class);
            intent.putExtra("barber_name", barberName);
            startActivity(intent);
        } else if (id == R.id.nav_upload_photo) {
            checkAndRequestPermissions();
        } else if (id == R.id.nav_view_gallery) {
            Intent intent = new Intent(MainActivity.this, GalleryActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_logout) {
            SharedPreferences.Editor editor = getSharedPreferences("UserPrefs", MODE_PRIVATE).edit();
            editor.clear();
            editor.apply();
            recreate();
        }
        
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void checkAndRequestPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();

        // Add camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.CAMERA);
        }

        // For SDK 33 and above, use READ_MEDIA_IMAGES instead of READ_EXTERNAL_STORAGE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API 33+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    1
            );
        } else {
            showImagePickerDialog();
        }
    }

    private void showImagePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("בחר פעולה");
        builder.setItems(new String[]{"צלם תמונה", "בחר מהגלריה"}, (dialog, which) -> {
            if (which == 0) {
                // צילום תמונה
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraLauncher.launch(takePictureIntent);
            } else {
                // בחירה מהגלריה
                Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                galleryLauncher.launch(pickPhotoIntent);
            }
        });
        builder.show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                showImagePickerDialog();
            } else {
                Toast.makeText(this, "נדרשות הרשאות מצלמה וגלריה להעלאת תמונות", Toast.LENGTH_LONG).show();
            }
        }
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

    private void saveImageToGallery(Bitmap bitmap) {
        // המרת התמונה ל-Base64
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT);

        // שמירת התמונה ב-SharedPreferences
        SharedPreferences prefs = getSharedPreferences("GalleryImages", MODE_PRIVATE);
        int imageCount = prefs.getInt("image_count", 0);
        
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("image_" + imageCount, imageString);
        editor.putInt("image_count", imageCount + 1);
        editor.apply();
    }
}
