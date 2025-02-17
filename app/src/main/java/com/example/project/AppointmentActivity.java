package com.example.project;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class AppointmentActivity extends AppCompatActivity {

    private RadioGroup radioGroupStaff;
    private Spinner spinnerTreatment;
    private Button btnPickDate, btnPickTime, btnConfirm, btnBack;
    private TextView txtSelectedDate, txtSelectedTime;
    private String selectedDate, selectedTime, selectedStaff, selectedTreatment;
    private SharedPreferences sharedPreferences;
    private Set<String> bookedAppointments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment);

        radioGroupStaff = findViewById(R.id.radio_group_staff);
        spinnerTreatment = findViewById(R.id.spinner_treatment);
        btnPickDate = findViewById(R.id.btn_pick_date);
        btnPickTime = findViewById(R.id.btn_pick_time);
        btnConfirm = findViewById(R.id.btn_confirm);
        btnBack = findViewById(R.id.btn_back);  // כפתור חזרה
        txtSelectedDate = findViewById(R.id.txt_selected_date);
        txtSelectedTime = findViewById(R.id.txt_selected_time);

        sharedPreferences = getSharedPreferences("Appointments", Context.MODE_PRIVATE);
        bookedAppointments = sharedPreferences.getStringSet("booked", new HashSet<>());

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.treatment_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTreatment.setAdapter(adapter);

        btnPickDate.setOnClickListener(view -> showDatePicker());
        btnPickTime.setOnClickListener(view -> showTimePicker());
        btnConfirm.setOnClickListener(view -> confirmAppointment());
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(AppointmentActivity.this, MainActivity.class));
            finish();  // לסיים את הפעולה ולחזור לדף הראשי
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, day1) -> {
            selectedDate = day1 + "/" + (month1 + 1) + "/" + year1;
            txtSelectedDate.setText("תאריך שנבחר: " + selectedDate);
        }, year, month, day);

        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        calendar.add(Calendar.MONTH, 1);
        datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
        datePickerDialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            if (hourOfDay < 10 || (hourOfDay == 17 && minute > 30) || hourOfDay > 17) {
                Toast.makeText(this, "בחר שעה בין 10:30 ל-17:30", Toast.LENGTH_SHORT).show();
                return;
            }
            selectedTime = hourOfDay + ":" + (minute < 10 ? "0" + minute : minute);
            txtSelectedTime.setText("שעה שנבחרה: " + selectedTime);
        }, 10, 30, true);

        timePickerDialog.show();
    }

    private void confirmAppointment() {
        int selectedStaffId = radioGroupStaff.getCheckedRadioButtonId();
        if (selectedStaffId == -1 || selectedDate == null || selectedTime == null) {
            Toast.makeText(this, "אנא בחר איש צוות, תאריך ושעה", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedStaffButton = findViewById(selectedStaffId);
        selectedStaff = selectedStaffButton.getText().toString();
        selectedTreatment = spinnerTreatment.getSelectedItem().toString();

        String appointment = selectedStaff + " - " + selectedTreatment + " - " + selectedDate + " - " + selectedTime;

        if (bookedAppointments.contains(appointment)) {
            Toast.makeText(this, "התור הזה כבר תפוס!", Toast.LENGTH_SHORT).show();
            return;
        }

        bookedAppointments.add(appointment);
        sharedPreferences.edit().putStringSet("booked", bookedAppointments).apply();

        Toast.makeText(this, "התור נשמר בהצלחה!", Toast.LENGTH_SHORT).show();

        startActivity(new Intent(this, MyAppointmentsActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();  // לסיים את הפעולה ולחזור לעמוד הקודם
    }
}
