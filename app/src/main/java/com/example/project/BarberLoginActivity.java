package com.example.project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class BarberLoginActivity extends AppCompatActivity {
    private EditText etUsername, etPassword;
    private Button btnLogin;
    private CustomerDataBase db;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barber_login);

        etUsername = findViewById(R.id.et_barber_username);
        etPassword = findViewById(R.id.et_barber_password);
        btnLogin = findViewById(R.id.btn_barber_login);
        db = CustomerDataBase.getInstance(this);
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etUsername.getText().toString();
                String password = etPassword.getText().toString();

                if (name.isEmpty() || password.isEmpty()) {
                    Toast.makeText(BarberLoginActivity.this, "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (isValidBarber(name, password)) {
                    // שמירת פרטי ההתחברות
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("username", name);
                    editor.putBoolean("isBarber", true);
                    editor.apply();

                    Toast.makeText(BarberLoginActivity.this, "התחברת בהצלחה", Toast.LENGTH_SHORT).show();

                    // מעבר למסך הראשי
                    Intent intent = new Intent(BarberLoginActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(BarberLoginActivity.this, "שם משתמש או סיסמה שגויים", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean isValidBarber(String username, String password) {
        // בדיקה מול רשימת הספרים הקבועה
        return (username.equals("doron") && password.equals("1234")) ||
                (username.equals("shoval") && password.equals("1234")) ||
                (username.equals("osher") && password.equals("1234"));
    }
} 