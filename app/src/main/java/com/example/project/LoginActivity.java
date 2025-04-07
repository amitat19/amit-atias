package com.example.project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private EditText etName, etPassword;
    private Button btnLogin, btnRegister;
    private CustomerDataBase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // אתחול רכיבי הממשק
        etName = findViewById(R.id.etName);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        // אתחול מסד הנתונים
        db = CustomerDataBase.getInstance(this);

        // טיפול בלחיצה על כפתור ההתחברות
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etName.getText().toString();
                String password = etPassword.getText().toString();

                if (name.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (db.isValidCustomer(name, password)) {
                    // שמירת שם המשתמש ב-SharedPreferences
                    SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("username", name);
                    editor.apply();

                    Toast.makeText(LoginActivity.this, "התחברת בהצלחה", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("uname", name);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "שם משתמש או סיסמה שגויים", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // טיפול בלחיצה על כפתור ההרשמה
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
}
