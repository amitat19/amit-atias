package com.example.project;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;

public class CustomerDataBase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "customers.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_CUSTOMERS = "customers";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_PASSWORD = "password";

    private static CustomerDataBase instance;

    private CustomerDataBase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized CustomerDataBase getInstance(Context context) {
        if (instance == null) {
            instance = new CustomerDataBase(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TABLE_CUSTOMERS + " ("
                + COLUMN_NAME + " TEXT PRIMARY KEY, "
                + COLUMN_PASSWORD + " TEXT)";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CUSTOMERS);
        onCreate(db);
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
}
