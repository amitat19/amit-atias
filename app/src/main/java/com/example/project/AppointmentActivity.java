package com.example.project;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.*;

public class AppointmentActivity extends AppCompatActivity {

    private RadioGroup radioGroupStaff;
    private Spinner spinnerTreatment;
    private Button btnPickDate, btnConfirm;
    private LinearLayout timeSlotsContainer;
    private TextView txtSelectedDate, txtSelectedTime;
    private String selectedDate, selectedTime, selectedStaff, selectedTreatment;
    private SharedPreferences sharedPreferences;
    private Set<String> bookedAppointments;
    private final List<String> timeSlots = Arrays.asList(
            "10:00", "10:30", "11:00", "11:30", "12:00", "12:30",
            "13:00", "13:30", "14:00", "14:30", "15:00", "15:30",
            "16:00", "16:30", "17:00", "17:30"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment);

        // אתחול רכיבי הממשק
        radioGroupStaff = findViewById(R.id.radio_group_staff);
        spinnerTreatment = findViewById(R.id.spinner_treatment);
        btnPickDate = findViewById(R.id.btn_pick_date);
        btnConfirm = findViewById(R.id.btn_confirm);
        //btnBack = findViewById(R.id.btn_back);
        txtSelectedDate = findViewById(R.id.txt_selected_date);
        txtSelectedTime = findViewById(R.id.txt_selected_time);
        timeSlotsContainer = findViewById(R.id.grid_time_slots);

        sharedPreferences = getSharedPreferences("Appointments", Context.MODE_PRIVATE);
        bookedAppointments = new HashSet<>(sharedPreferences.getStringSet("booked", new HashSet<>()));

        // תחילה - מנע גישה לתאריך ולשעה עד לבחירת צוות וטיפול
        spinnerTreatment.setEnabled(false);
        btnPickDate.setEnabled(false);

        // מאזין לבחירת איש צוות
        radioGroupStaff.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId != -1) {
                RadioButton selectedStaffButton = findViewById(checkedId);
                selectedStaff = selectedStaffButton.getText().toString();
                spinnerTreatment.setEnabled(true);
            }
        });

        // אתחול ה-Spinner (סוגי טיפולים)
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.treatment_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTreatment.setAdapter(adapter);

        // מאזין לבחירת טיפול
        spinnerTreatment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTreatment = parent.getItemAtPosition(position).toString();
                btnPickDate.setEnabled(true); // אפשר לבחור תאריך רק לאחר בחירת טיפול
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // אירועים
        btnPickDate.setOnClickListener(view -> showDatePicker());
        btnConfirm.setOnClickListener(view -> confirmAppointment());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, day1) -> {
            Calendar selectedCal = Calendar.getInstance();
            selectedCal.set(year1, month1, day1);

            int dayOfWeek = selectedCal.get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek == Calendar.MONDAY || dayOfWeek == Calendar.FRIDAY) {
                Toast.makeText(this, "לא ניתן לבחור יום שני או שישי", Toast.LENGTH_SHORT).show();
                return;
            }

            selectedDate = day1 + "/" + (month1 + 1) + "/" + year1;
            txtSelectedDate.setText("תאריך שנבחר: " + selectedDate);
            updateAvailableTimes();
        }, year, month, day);

        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        calendar.add(Calendar.MONTH, 1);
        datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
        datePickerDialog.show();
    }

    private void updateAvailableTimes() {
        timeSlotsContainer.removeAllViews();
        for (String time : timeSlots) {
            String appointmentKey = selectedDate + " - " + time;
            if (!bookedAppointments.contains(appointmentKey)) {
                Button timeButton = new Button(this);
                timeButton.setText(time);
                timeButton.setPadding(16, 8, 16, 8);

                // עיצוב הכפתור
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(10, 10, 10, 10);
                timeButton.setLayoutParams(params);

                timeButton.setOnClickListener(v -> {
                    selectedTime = time;
                    txtSelectedTime.setText("שעה שנבחרה: " + selectedTime);
                });

                timeSlotsContainer.addView(timeButton);
            }
        }
    }

    private void confirmAppointment() {
        int selectedStaffId = radioGroupStaff.getCheckedRadioButtonId();
        if (selectedStaffId == -1 || selectedDate == null || selectedTime == null) {
            Toast.makeText(this, "אנא בחר איש צוות, תאריך ושעה", Toast.LENGTH_SHORT).show();
            return;
        }

        String appointment = selectedDate + " - " + selectedTime;
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
}
