package com.example.project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {
    private TextInputEditText etUsername, etPhone, etPassword;
    private MaterialButton btnRegister;
    private CustomerDataBase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // אתחול רכיבי הממשק
        etUsername = findViewById(R.id.et_username);
        etPhone = findViewById(R.id.et_phone);
        etPassword = findViewById(R.id.et_password);
        btnRegister = findViewById(R.id.btn_register);

        // אתחול מסד הנתונים
        db = CustomerDataBase.getInstance(this);

        // טיפול בלחיצה על כפתור ההרשמה
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString().trim();
                String phone = etPhone.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                // בדיקת תקינות השדות
                if (username.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
                    return;
                }

                // בדיקה אם שם המשתמש כבר קיים
                if (db.isUsernameTaken(username)) {
                    Toast.makeText(RegisterActivity.this, "שם המשתמש כבר קיים במערכת", Toast.LENGTH_SHORT).show();
                    return;
                }

                // שמירת המשתמש במסד הנתונים
                if (db.registerUser(username, phone, password)) {
                    // שמירת פרטי המשתמש ב-SharedPreferences
                    SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("username", username);
                    editor.putString("phone", phone);
                    editor.apply();

                    Toast.makeText(RegisterActivity.this, "ההרשמה בוצעה בהצלחה", Toast.LENGTH_SHORT).show();
                    
                    // מעבר למסך הראשי
                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    intent.putExtra("uname", username);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this, "שגיאה בהרשמה", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
