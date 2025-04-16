package com.example.project;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;
import java.util.ArrayList;
import java.util.List;
import android.content.SharedPreferences;
import java.util.Map;
import android.util.Log;

public class CustomerDataBase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "customers.db";
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_CUSTOMERS = "customers";
    private static final String TABLE_APPOINTMENTS = "appointments";
    
    // עמודות טבלת לקוחות
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_PASSWORD = "password";
    
    // עמודות טבלת תורים
    private static final String COLUMN_APPOINTMENT_ID = "id";
    private static final String COLUMN_CUSTOMER_NAME = "customer_name";
    private static final String COLUMN_BARBER_NAME = "barber_name";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_TIME = "time";

    private static CustomerDataBase instance;
    private SharedPreferences sharedPreferences;

    private CustomerDataBase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        sharedPreferences = context.getSharedPreferences("Appointments", Context.MODE_PRIVATE);
    }

    public static synchronized CustomerDataBase getInstance(Context context) {
        if (instance == null) {
            instance = new CustomerDataBase(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // יצירת טבלת לקוחות
        String createCustomersTable = "CREATE TABLE " + TABLE_CUSTOMERS + " ("
                + COLUMN_NAME + " TEXT PRIMARY KEY, "
                + COLUMN_PASSWORD + " TEXT)";
        db.execSQL(createCustomersTable);

        // יצירת טבלת תורים
        String createAppointmentsTable = "CREATE TABLE " + TABLE_APPOINTMENTS + " ("
                + COLUMN_APPOINTMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_CUSTOMER_NAME + " TEXT, "
                + COLUMN_BARBER_NAME + " TEXT, "
                + COLUMN_DATE + " TEXT, "
                + COLUMN_TIME + " TEXT)";
        db.execSQL(createAppointmentsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // הוספת טבלת התורים בגרסה 2
            String createAppointmentsTable = "CREATE TABLE " + TABLE_APPOINTMENTS + " ("
                    + COLUMN_APPOINTMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_CUSTOMER_NAME + " TEXT, "
                    + COLUMN_BARBER_NAME + " TEXT, "
                    + COLUMN_DATE + " TEXT, "
                    + COLUMN_TIME + " TEXT)";
            db.execSQL(createAppointmentsTable);
        }
    }

    public void addCustomer(Customer customer) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, customer.getName());
        values.put(COLUMN_PASSWORD, customer.getPassword());

        db.insert(TABLE_CUSTOMERS, null, values);
        db.close();
    }

    public Customer getCustomerByName(String name) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_CUSTOMERS,
                new String[]{COLUMN_NAME, COLUMN_PASSWORD},
                COLUMN_NAME + "=?",
                new String[]{name}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String customerName = cursor.getString(0);
            String password = cursor.getString(1);
            cursor.close();
            return new Customer(customerName, password);
        }

        if (cursor != null) {
            cursor.close();
        }

        return null;
    }

    // פונקציות לניהול תורים
    public void addAppointment(Appointment appointment) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CUSTOMER_NAME, appointment.getCustomerName());
        values.put(COLUMN_BARBER_NAME, appointment.getBarberName());
        values.put(COLUMN_DATE, appointment.getDate());
        values.put(COLUMN_TIME, appointment.getTime());
        
        db.insert(TABLE_APPOINTMENTS, null, values);
        db.close();
    }

    public List<Appointment> getCustomerAppointments(String customerName) {
        List<Appointment> appointments = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_APPOINTMENTS,
                new String[]{COLUMN_CUSTOMER_NAME, COLUMN_BARBER_NAME, COLUMN_DATE, COLUMN_TIME},
                COLUMN_CUSTOMER_NAME + "=?",
                new String[]{customerName}, null, null, COLUMN_DATE + ", " + COLUMN_TIME);
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Appointment appointment = new Appointment(
                    cursor.getString(0), // customerName
                    cursor.getString(1), // barberName
                    cursor.getString(2), // date
                    cursor.getString(3)  // time
                );
                appointments.add(appointment);
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        return appointments;
    }

    public List<String> getBarberAppointments(String barberName) {
        List<String> appointments = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Log.d("CustomerDataBase", "Getting appointments for barber: " + barberName);
        
        try {
            // שליפת כל התורים של הספר
            String query = "SELECT " + COLUMN_CUSTOMER_NAME + ", " + COLUMN_DATE + ", " + COLUMN_TIME + 
                         " FROM " + TABLE_APPOINTMENTS + 
                         " WHERE " + COLUMN_BARBER_NAME + " = ?";
            Cursor cursor = db.rawQuery(query, new String[]{barberName});

            Log.d("CustomerDataBase", "Query executed: " + query);
            Log.d("CustomerDataBase", "Number of appointments found: " + (cursor != null ? cursor.getCount() : 0));

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String customerName = cursor.getString(0);
                    String date = cursor.getString(1);
                    String time = cursor.getString(2);
                    
                    Log.d("CustomerDataBase", "Found appointment - Customer: " + customerName + 
                          ", Date: " + date + ", Time: " + time);
                    
                    // יצירת מחרוזת מפורמטת של התור
                    String formattedAppointment = String.format("לקוח: %s\nתאריך: %s\nשעה: %s",
                        customerName, date, time);
                    appointments.add(formattedAppointment);
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e("CustomerDataBase", "Error getting barber appointments", e);
        } finally {
            db.close();
        }
        
        Log.d("CustomerDataBase", "Returning " + appointments.size() + " appointments");
        return appointments;
    }

    public void deleteAppointment(String customerName, String date, String time) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_APPOINTMENTS,
                COLUMN_CUSTOMER_NAME + "=? AND " + COLUMN_DATE + "=? AND " + COLUMN_TIME + "=?",
                new String[]{customerName, date, time});
        db.close();
    }

    public boolean isAppointmentTaken(String barber, String date, String time) {
        String key = barber + "_" + date + "_" + time;
        return sharedPreferences.contains(key);
    }

    public void saveAppointment(String key, String appointment) {
        String[] parts = appointment.split("_");
        if (parts.length != 5) {
            Log.e("CustomerDataBase", "Invalid appointment format: " + appointment);
            return;
        }
        
        String customerName = parts[0];
        String barberName = parts[1];
        String date = parts[3];
        String time = parts[4];
        
        Log.d("CustomerDataBase", "Saving appointment - Customer: " + customerName + 
              ", Barber: " + barberName + ", Date: " + date + ", Time: " + time);
        
        // שמירה במסד הנתונים
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CUSTOMER_NAME, customerName);
        values.put(COLUMN_BARBER_NAME, barberName);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_TIME, time);
        
        long result = db.insert(TABLE_APPOINTMENTS, null, values);
        if (result == -1) {
            Log.e("CustomerDataBase", "Failed to save appointment to database");
        } else {
            Log.d("CustomerDataBase", "Successfully saved appointment to database with ID: " + result);
        }
        db.close();
        
        // שמירה ב-SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, appointment);
        editor.apply();
        Log.d("CustomerDataBase", "Saved appointment to SharedPreferences with key: " + key);
    }

    public String getAppointment(String key) {
        return sharedPreferences.getString(key, "");
    }

    public void deleteAppointment(String key) {
        String[] parts = key.split("_");
        if (parts.length != 3) return;
        
        String barber = parts[0];
        String date = parts[1];
        String time = parts[2];
        
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_APPOINTMENTS,
                COLUMN_BARBER_NAME + "=? AND " + COLUMN_DATE + "=? AND " + COLUMN_TIME + "=?",
                new String[]{barber, date, time});
        db.close();
    }

    public List<String> getUserAppointments(String username) {
        List<String> appointments = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_APPOINTMENTS,
                new String[]{COLUMN_CUSTOMER_NAME, COLUMN_BARBER_NAME, COLUMN_DATE, COLUMN_TIME},
                COLUMN_CUSTOMER_NAME + "=?",
                new String[]{username}, null, null, COLUMN_DATE + ", " + COLUMN_TIME);
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String appointment = cursor.getString(0) + "_" + cursor.getString(1) + "_" + 
                                   cursor.getString(2) + "_" + cursor.getString(3);
                appointments.add(appointment);
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        return appointments;
    }

    public List<String> getAllAppointments() {
        List<String> appointments = new ArrayList<>();
        Map<String, ?> allAppointments = sharedPreferences.getAll();
        
        for (Map.Entry<String, ?> entry : allAppointments.entrySet()) {
            appointments.add(entry.getValue().toString());
        }
        
        return appointments;
    }

    public boolean isValidCustomer(String name, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CUSTOMERS,
                new String[]{COLUMN_NAME, COLUMN_PASSWORD},
                COLUMN_NAME + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{name, password},
                null, null, null);

        boolean isValid = cursor != null && cursor.getCount() > 0;
        if (cursor != null) {
            cursor.close();
        }
        return isValid;
    }

    public boolean isAppointmentBooked(String key) {
        String[] parts = key.split("_");
        if (parts.length != 3) return false;
        
        String barber = parts[0];
        String date = parts[1];
        String time = parts[2];
        
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_APPOINTMENTS,
                new String[]{COLUMN_APPOINTMENT_ID},
                COLUMN_BARBER_NAME + "=? AND " + COLUMN_DATE + "=? AND " + COLUMN_TIME + "=?",
                new String[]{barber, date, time}, null, null, null);
        
        boolean isBooked = cursor != null && cursor.getCount() > 0;
        if (cursor != null) cursor.close();
        return isBooked;
    }
}
