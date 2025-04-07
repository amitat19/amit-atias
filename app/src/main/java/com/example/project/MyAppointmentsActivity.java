package com.example.project;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class MyAppointmentsActivity extends AppCompatActivity {

    private ListView appointmentsListView;
    private Button btnBack;
    private SharedPreferences sharedPreferences;
    private ArrayAdapter<String> adapter;
    private CustomerDataBase db;
    private List<Appointment> userAppointments;
    private List<String> formattedAppointments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_appointments);

        appointmentsListView = findViewById(R.id.appointments_list);
        btnBack = findViewById(R.id.btn_back);
        sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        db = CustomerDataBase.getInstance(this);

        // קבלת שם המשתמש המחובר
        String username = sharedPreferences.getString("username", "");
        Toast.makeText(this, "משתמש מחובר: " + username, Toast.LENGTH_SHORT).show();

        if (username.isEmpty()) {
            Toast.makeText(this, "אינך מחובר למערכת", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // קבלת כל התורים של המשתמש
        userAppointments = db.getCustomerAppointments(username);
        
        // המרת התורים לפורמט קריא
        formattedAppointments = new ArrayList<>();
        for (Appointment appointment : userAppointments) {
            String formattedAppointment = "תור עם " + appointment.getBarberName() + 
                                        " בתאריך " + appointment.getDate() + 
                                        " בשעה " + appointment.getTime();
            formattedAppointments.add(formattedAppointment);
        }
        
        if (formattedAppointments.isEmpty()) {
            Toast.makeText(this, "אין לך תורים פעילים", Toast.LENGTH_SHORT).show();
        }

        // הצגת התורים ב-ListView
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, formattedAppointments);
        appointmentsListView.setAdapter(adapter);

        appointmentsListView.setOnItemClickListener((parent, view, position, id) -> {
            // מחיקת התור מהמסד הנתונים
            Appointment appointment = userAppointments.get(position);
            db.deleteAppointment(appointment.getCustomerName(), appointment.getDate(), appointment.getTime());
            
            // עדכון הרשימות
            userAppointments.remove(position);
            formattedAppointments.remove(position);
            adapter.notifyDataSetChanged();
            
            Toast.makeText(this, "התור נמחק!", Toast.LENGTH_SHORT).show();
        });

        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(MyAppointmentsActivity.this, MainActivity.class));
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
