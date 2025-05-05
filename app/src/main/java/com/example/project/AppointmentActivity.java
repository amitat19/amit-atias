package com.example.project;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
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
    private CustomerDataBase customerDataBase;
    private String customerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment);

        // אתחול רכיבי הממשק
        radioGroupStaff = findViewById(R.id.radio_group_staff);
        spinnerTreatment = findViewById(R.id.spinner_treatment);
        btnPickDate = findViewById(R.id.btn_pick_date);
        btnConfirm = findViewById(R.id.btn_confirm);
        txtSelectedDate = findViewById(R.id.txt_selected_date);
        txtSelectedTime = findViewById(R.id.txt_selected_time);
        timeSlotsContainer = findViewById(R.id.grid_time_slots);

        sharedPreferences = getSharedPreferences("Appointments", Context.MODE_PRIVATE);
        bookedAppointments = new HashSet<>(sharedPreferences.getStringSet("booked", new HashSet<>()));

        customerDataBase = CustomerDataBase.getInstance(this);
        customerName = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("username", "");

        // תחילה - מנע גישה לתאריך ולשעה עד לבחירת צוות וטיפול
        spinnerTreatment.setEnabled(false);
        btnPickDate.setEnabled(false);

        // מאזין לבחירת ספר
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
            String appointmentKey = selectedDate + " - " + time + " - " + selectedStaff;
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
        if (selectedStaff == null || selectedDate == null || selectedTime == null) {
            Toast.makeText(this, "אנא בחר ספר, תאריך ושעה", Toast.LENGTH_SHORT).show();
            return;
        }

        // בדיקה אם התור כבר תפוס
        if (!customerDataBase.isAppointmentAvailable(selectedStaff, selectedDate, selectedTime)) {
            Toast.makeText(this, "התור שבחרת תפוס, אנא בחר תור אחר", Toast.LENGTH_SHORT).show();
            return;
        }

        // יצירת מחרוזת התור
        String appointmentString = customerName + "_" + selectedStaff + "_" + selectedTreatment + "_" + selectedDate + "_" + selectedTime;
        
        // שמירת התור במסד הנתונים
        customerDataBase.saveAppointment(appointmentString);
        
        // תזמון תזכורת 24 שעות לפני התור
        scheduleReminder(selectedDate, selectedTime, selectedStaff, selectedTreatment);
        
        Toast.makeText(this, "התור נשמר בהצלחה!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void scheduleReminder(String date, String time, String barber, String treatment) {
        try {
            // פיצול התאריך והשעה
            String[] dateParts = date.split("/");
            String[] timeParts = time.split(":");
            
            int day = Integer.parseInt(dateParts[0]);
            int month = Integer.parseInt(dateParts[1]) - 1; // חודשים מתחילים מ-0
            int year = Integer.parseInt(dateParts[2]);
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            // יצירת תאריך התור
            Calendar appointmentTime = Calendar.getInstance();
            appointmentTime.set(year, month, day, hour, minute, 0);

            // חישוב זמן התזכורת (24 שעות לפני התור)
            Calendar reminderTime = (Calendar) appointmentTime.clone();
            reminderTime.add(Calendar.HOUR_OF_DAY, -24);

            // בדיקה שהתזכורת לא מתוזמנת לעבר
            if (reminderTime.before(Calendar.getInstance())) {
                Log.d("AppointmentActivity", "Cannot schedule reminder in the past");
                return;
            }

            // יצירת Intent לתזכורת
            Intent reminderIntent = new Intent(this, AppointmentReminderReceiver.class);
            reminderIntent.putExtra("title", treatment + " עם " + barber);
            reminderIntent.putExtra("time", time);

            // יצירת PendingIntent
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                (int) System.currentTimeMillis(),
                reminderIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // קבלת AlarmManager והגדרת התזכורת
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime.getTimeInMillis(),
                    pendingIntent
                );
                Log.d("AppointmentActivity", "Reminder scheduled for: " + reminderTime.getTime());
            }

        } catch (Exception e) {
            Log.e("AppointmentActivity", "Error scheduling reminder: " + e.getMessage());
        }
    }

    private String getSelectedBarber() {
        int selectedId = radioGroupStaff.getCheckedRadioButtonId();
        if (selectedId == -1) {
            return "";
        }
        RadioButton selectedRadioButton = findViewById(selectedId);
        return selectedRadioButton.getText().toString();
    }

    private String getSelectedTreatment() {
        int selectedId = spinnerTreatment.getSelectedItemPosition();
        if (selectedId == 0) {
            return "";
        }
        return spinnerTreatment.getItemAtPosition(selectedId).toString();
    }

    private void saveAppointment() {
        String selectedBarber = getSelectedBarber();
        String selectedTreatment = getSelectedTreatment();
        String selectedDate = txtSelectedDate.getText().toString();
        String selectedTime = txtSelectedTime.getText().toString();

        if (selectedBarber.isEmpty() || selectedTreatment.isEmpty() || selectedDate.isEmpty() || selectedTime.isEmpty()) {
            Toast.makeText(this, "אנא מלא את כל השדות", Toast.LENGTH_SHORT).show();
            return;
        }

        // קבלת שם המשתמש המחובר
        SharedPreferences userPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String username = userPrefs.getString("username", "");

        if (username.isEmpty()) {
            Toast.makeText(this, "אינך מחובר למערכת", Toast.LENGTH_SHORT).show();
            return;
        }

        // בדיקה אם התאריך והשעה כבר תפוסים
        CustomerDataBase db = CustomerDataBase.getInstance(this);
        if (db.isAppointmentTaken(selectedBarber, selectedDate, selectedTime)) {
            Toast.makeText(this, "התור תפוס, אנא בחר זמן אחר", Toast.LENGTH_SHORT).show();
            return;
        }

        // שמירת התור
        String appointmentString = username + "_" + selectedBarber + "_" + selectedTreatment + "_" + selectedDate + "_" + selectedTime;
        
        // שמירה במסד הנתונים
        db.saveAppointment(appointmentString);
        
        Toast.makeText(this, "התור נקבע בהצלחה!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
