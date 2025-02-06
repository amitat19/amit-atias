package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etPassword;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etName = findViewById(R.id.etName);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (name.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "אנא מלא את כל השדות", Toast.LENGTH_SHORT).show();
            return;
        }

        CustomerDataBase db = CustomerDataBase.getInstance(this);

        // בדיקה אם שם המשתמש קיים במערכת
        if (db.getCustomerByName(name) != null) {
            Toast.makeText(this, "שם משתמש כבר קיים במערכת", Toast.LENGTH_SHORT).show();
            return;
        }

        // הוספת לקוח חדש למאגר
        Customer newCustomer = new Customer(name, password);
        db.addCustomer(newCustomer);

        Toast.makeText(this, "ההרשמה בוצעה בהצלחה!", Toast.LENGTH_SHORT).show();

        // מעבר למסך הראשי
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
