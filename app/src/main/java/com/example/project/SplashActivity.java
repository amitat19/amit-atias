package com.example.project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    private TextView countdownText;
    private static final long SPLASH_DURATION = 2000; // 2 שניות
    private static final long COUNTDOWN_INTERVAL = 1000; // כל שנייה
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        countdownText = findViewById(R.id.countdown_text);
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        new CountDownTimer(SPLASH_DURATION, COUNTDOWN_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                // עדכון הטקסט עם הזמן הנותר
                long seconds = millisUntilFinished / 1000;
                countdownText.setText(String.valueOf(seconds));
            }

            @Override
            public void onFinish() {
                // מעבר ל-MainActivity
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                // העברת שם המשתמש אם קיים
                String username = sharedPreferences.getString("username", "");
                if (!username.isEmpty()) {
                    intent.putExtra("uname", username);
                }
                startActivity(intent);
                finish(); // סגירת ה-SplashActivity
            }
        }.start();
    }
} 