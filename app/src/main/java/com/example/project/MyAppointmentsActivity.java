package com.example.project;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MyAppointmentsActivity extends AppCompatActivity {

    private ListView listViewAppointments;
    private Button btnBack;
    private SharedPreferences sharedPreferences;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> appointmentsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_appointments);

        listViewAppointments = findViewById(R.id.list_view_appointments);
        btnBack = findViewById(R.id.btn_back);
        sharedPreferences = getSharedPreferences("Appointments", Context.MODE_PRIVATE);

        Set<String> appointmentsSet = sharedPreferences.getStringSet("booked", new HashSet<>());
        appointmentsList = new ArrayList<>(appointmentsSet);

        if (appointmentsList.isEmpty()) {
            Toast.makeText(this, "אין לך תורים", Toast.LENGTH_LONG).show();
            btnBack.setVisibility(View.VISIBLE);
        } else {
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, appointmentsList);
            listViewAppointments.setAdapter(adapter);
        }

        listViewAppointments.setOnItemClickListener((parent, view, position, id) -> {
            removeAppointment(position);
        });

        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(MyAppointmentsActivity.this, MainActivity.class));
            finish();  // לסיים את הפעולה ולחזור לדף הראשי
        });
    }

    private void removeAppointment(int position) {
        Set<String> appointmentsSet = sharedPreferences.getStringSet("booked", new HashSet<>());
        ArrayList<String> tempList = new ArrayList<>(appointmentsSet);

        if (position >= 0 && position < tempList.size()) {
            tempList.remove(position);
            appointmentsSet = new HashSet<>(tempList);
            sharedPreferences.edit().putStringSet("booked", appointmentsSet).apply();

            appointmentsList.remove(position);
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "התור נמחק!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();  // לסיים את הפעולה ולחזור לעמוד הקודם
    }
}
