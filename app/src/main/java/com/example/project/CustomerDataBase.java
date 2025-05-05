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
    private static final int DATABASE_VERSION = 3;
    private static final String TABLE_CUSTOMERS = "customers";
    private static final String TABLE_APPOINTMENTS = "appointments";
    private static final String TABLE_USERS = "users";
    
    // עמודות טבלת לקוחות
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PHONE = "phone";
    private static final String COLUMN_PASSWORD = "password";
    
    // עמודות טבלת תורים
    private static final String COLUMN_APPOINTMENT_ID = "id";
    private static final String COLUMN_CUSTOMER_NAME = "customer_name";
    private static final String COLUMN_BARBER_NAME = "barber_name";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_TIME = "time";
    private static final String COLUMN_TREATMENT = "treatment";

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
        String createCustomersTable = "CREATE TABLE " + TABLE_CUSTOMERS + " (" +
                COLUMN_USERNAME + " TEXT PRIMARY KEY, " +
                COLUMN_PHONE + " TEXT NOT NULL, " +
                COLUMN_PASSWORD + " TEXT NOT NULL)";
        db.execSQL(createCustomersTable);

        // יצירת טבלת תורים
        String createAppointmentsTable = "CREATE TABLE " + TABLE_APPOINTMENTS + " (" +
                COLUMN_APPOINTMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_CUSTOMER_NAME + " TEXT NOT NULL, " +
                COLUMN_BARBER_NAME + " TEXT NOT NULL, " +
                COLUMN_TREATMENT + " TEXT NOT NULL, " +
                COLUMN_DATE + " TEXT NOT NULL, " +
                COLUMN_TIME + " TEXT NOT NULL)";
        db.execSQL(createAppointmentsTable);

        // יצירת טבלת משתמשים
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_USERNAME + " TEXT PRIMARY KEY,"
                + COLUMN_PHONE + " TEXT,"
                + COLUMN_PASSWORD + " TEXT"
                + ")";
        db.execSQL(createUsersTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            // מחיקת הטבלה הישנה
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_APPOINTMENTS);
            // יצירת הטבלה מחדש עם העמודה החדשה
            String createAppointmentsTable = "CREATE TABLE " + TABLE_APPOINTMENTS + " ("
                    + COLUMN_APPOINTMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_CUSTOMER_NAME + " TEXT, "
                    + COLUMN_BARBER_NAME + " TEXT, "
                    + COLUMN_TREATMENT + " TEXT, "
                    + COLUMN_DATE + " TEXT, "
                    + COLUMN_TIME + " TEXT)";
            db.execSQL(createAppointmentsTable);
        }
    }

    public void addCustomer(Customer customer) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, customer.getUsername());
        values.put(COLUMN_PHONE, customer.getPhone());
        values.put(COLUMN_PASSWORD, customer.getPassword());

        db.insert(TABLE_USERS, null, values);
        db.close();
    }

    public Customer getCustomerByUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_USERNAME, COLUMN_PHONE, COLUMN_PASSWORD},
                COLUMN_USERNAME + "=?",
                new String[]{username}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String customerUsername = cursor.getString(0);
            String phone = cursor.getString(1);
            String password = cursor.getString(2);
            cursor.close();
            return new Customer(customerUsername, phone, password);
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
        
        try {
            Cursor cursor = db.query(TABLE_APPOINTMENTS,
                    new String[]{COLUMN_CUSTOMER_NAME, COLUMN_BARBER_NAME, COLUMN_DATE, COLUMN_TIME},
                    COLUMN_CUSTOMER_NAME + "=?",
                    new String[]{customerName}, null, null, COLUMN_DATE + ", " + COLUMN_TIME);

            if (cursor.moveToFirst()) {
                do {
                    String barberName = cursor.getString(1);
                    String date = cursor.getString(2);
                    String time = cursor.getString(3);
                    appointments.add(new Appointment(customerName, barberName, date, time));
                } while (cursor.moveToNext());
            }
            
            cursor.close();
        } catch (Exception e) {
            Log.e("CustomerDataBase", "Error getting customer appointments: " + e.getMessage());
        } finally {
            db.close();
        }
        
        return appointments;
    }

    public List<String> getBarberAppointments(String barberName) {
        List<String> appointments = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Log.d("CustomerDataBase", "Getting appointments for barber: " + barberName);
        
        try {
            String query = "SELECT " + COLUMN_CUSTOMER_NAME + ", " + COLUMN_DATE + ", " + COLUMN_TIME + 
                         " FROM " + TABLE_APPOINTMENTS +
                         " WHERE " + COLUMN_BARBER_NAME + " = ?" +
                         " ORDER BY " + COLUMN_DATE + ", " + COLUMN_TIME;

            Cursor cursor = db.rawQuery(query, new String[]{barberName});
            
            if (cursor.moveToFirst()) {
                do {
                    String customerName = cursor.getString(0);
                    String date = cursor.getString(1);
                    String time = cursor.getString(2);
                    String appointment = customerName + " - " + date + " - " + time;
                    appointments.add(appointment);
                } while (cursor.moveToNext());
            }
            
            cursor.close();
        } catch (Exception e) {
            Log.e("CustomerDataBase", "Error getting barber appointments: " + e.getMessage());
        } finally {
            db.close();
        }
        
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

    public void saveAppointment(String appointmentString) {
        // פיצול המחרוזת לחלקים
        String[] parts = appointmentString.split("_");
        if (parts.length != 5) {
            Log.e("CustomerDataBase", "Invalid appointment format: " + appointmentString);
            return;
        }

        String customerName = parts[0];
        String barberName = parts[1];
        String treatment = parts[2];
        String date = parts[3];
        String time = parts[4];

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CUSTOMER_NAME, customerName);
        values.put(COLUMN_BARBER_NAME, barberName);
        values.put(COLUMN_TREATMENT, treatment);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_TIME, time);

        long result = db.insert(TABLE_APPOINTMENTS, null, values);
        db.close();

        if (result == -1) {
            Log.e("CustomerDataBase", "Failed to save appointment");
        } else {
            Log.d("CustomerDataBase", "Appointment saved successfully");
        }
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

    public boolean registerUser(String username, String phone, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PHONE, phone);
        values.put(COLUMN_PASSWORD, password);
        
        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public boolean isUsernameTaken(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CUSTOMERS,
                new String[]{COLUMN_USERNAME},
                COLUMN_USERNAME + "=?",
                new String[]{username},
                null, null, null);

        boolean isTaken = cursor != null && cursor.getCount() > 0;
        if (cursor != null) {
            cursor.close();
        }
        return isTaken;
    }

    public boolean isValidCustomer(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_USERNAME},
                COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{username, password},
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

    public boolean isAppointmentAvailable(String barberName, String date, String time) {
        SQLiteDatabase db = this.getReadableDatabase();
        boolean isAvailable = true;
        
        try {
            String query = "SELECT COUNT(*) FROM " + TABLE_APPOINTMENTS +
                         " WHERE " + COLUMN_BARBER_NAME + " = ?" +
                         " AND " + COLUMN_DATE + " = ?" +
                         " AND " + COLUMN_TIME + " = ?";
            
            Cursor cursor = db.rawQuery(query, new String[]{barberName, date, time});
            
            if (cursor.moveToFirst()) {
                int count = cursor.getInt(0);
                isAvailable = count == 0;
            }
            
            cursor.close();
        } catch (Exception e) {
            Log.e("CustomerDataBase", "Error checking appointment availability: " + e.getMessage());
        } finally {
            db.close();
        }
        
        return isAvailable;
    }
}
