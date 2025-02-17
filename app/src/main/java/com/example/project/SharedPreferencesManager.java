package com.example.project;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class SharedPreferencesManager {

    private SharedPreferences sharedPreferences;

    public SharedPreferencesManager(Context context) {
        sharedPreferences = context.getSharedPreferences("Appointments", Context.MODE_PRIVATE);
    }

    public boolean saveAppointment(String appointment) {
        Set<String> appointmentsSet = sharedPreferences.getStringSet("booked", new HashSet<>());

        if (appointmentsSet.contains(appointment)) {
            return false;
        }

        appointmentsSet.add(appointment);
        sharedPreferences.edit().putStringSet("booked", appointmentsSet).apply();
        return true;
    }

    public ArrayList<String> getAppointments() {
        return new ArrayList<>(sharedPreferences.getStringSet("booked", new HashSet<>()));
    }
}
