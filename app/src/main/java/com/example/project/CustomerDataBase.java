package com.example.project;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class CustomerDataBase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "my_app_database.db";
    private static final String TABLE_NAME_CUSTOMERS = "tbl_customers";
    private static final int DATABASE_VERSION = 1;

    private static final String COLUMN_ID = "Id";
    private static final String COLUMN_NAME = "Name";
    private static final String COLUMN_SCORE = "Score";

    private static final String[] ALL_COLUMNS = {COLUMN_ID, COLUMN_NAME, COLUMN_SCORE};

    private static final String CREATE_TABLE_CUSTOMERS = "CREATE TABLE IF NOT EXISTS " +
            TABLE_NAME_CUSTOMERS + "(" +
            COLUMN_ID + " INTEGER PRIMARY KEY," +
            COLUMN_NAME + " TEXT," +
            COLUMN_SCORE + " INTEGER);";

    private static ArrayList<Customer> customerList;

    // Singleton instance
    private static CustomerDataBase instance;

    private CustomerDataBase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        customerList = new ArrayList<>();
        loadCustomersFromDatabase();
    }

    public static synchronized CustomerDataBase getInstance(Context context) {
        if (instance == null) {
            instance = new CustomerDataBase(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_CUSTOMERS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_CUSTOMERS);
        onCreate(db);
    }

    private Customer insertCustomerInDatabase(Customer customer) {
        try (SQLiteDatabase db = getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME, customer.getName());
            values.put(COLUMN_SCORE, customer.getScore());

            long id = db.insert(TABLE_NAME_CUSTOMERS, null, values);
            customer.setId(id);
            return customer;
        }
    }

    private void updateCustomerInDatabase(Customer customer) {
        try (SQLiteDatabase db = getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME, customer.getName());
            values.put(COLUMN_SCORE, customer.getScore());

            db.update(TABLE_NAME_CUSTOMERS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(customer.getId())});
        }
    }

    public void deleteCustomerFromDatabase(long id) {
        try (SQLiteDatabase db = getWritableDatabase()) {
            db.delete(TABLE_NAME_CUSTOMERS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        }
    }

    @SuppressLint("Range")
    private void loadCustomersFromDatabase() {
        customerList.clear();
        try (SQLiteDatabase db = getReadableDatabase();
             Cursor cursor = db.query(TABLE_NAME_CUSTOMERS, ALL_COLUMNS, null, null, null, null, COLUMN_SCORE + " DESC")) {

            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                int score = cursor.getInt(cursor.getColumnIndex(COLUMN_SCORE));
                long id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));

                Customer customer = new Customer(id, name, score);
                customerList.add(customer);
            }
        }
    }

    public ArrayList<Customer> getAllCustomers() {
        return new ArrayList<>(customerList);
    }

    public void insertOrUpdateCustomer(Customer customer) {
        int customerIndex = findCustomerIndexInList(customer);

        if (customerIndex != -1) {
            updateCustomerInDatabase(customer);
            customerList.set(customerIndex, customer);
        } else {
            Customer newCustomerWithId = insertCustomerInDatabase(customer);
            customerList.add(newCustomerWithId);
        }
    }

    private int findCustomerIndexInList(Customer customer) {
        for (int i = 0; i < customerList.size(); i++) {
            if (customerList.get(i).getId() == customer.getId()) {
                return i;
            }
        }
        return -1;
    }

    @SuppressLint("Range")
    public Customer getCustomerByName(String name) {
        try (SQLiteDatabase db = getReadableDatabase();
             Cursor cursor = db.query(TABLE_NAME_CUSTOMERS, ALL_COLUMNS, COLUMN_NAME + " = ?", new String[]{name}, null, null, null)) {

            if (cursor != null && cursor.moveToFirst()) {
                String customerName = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                int score = cursor.getInt(cursor.getColumnIndex(COLUMN_SCORE));
                long id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));

                return new Customer(id, customerName, score);
            }
        }
        return null;
    }

    public void insertOrUpdateCustomerByName(String name) {
        Customer customer = getCustomerByName(name);

        if (customer == null) {
            customer = new Customer(name);  // Default constructor with name
        }

        customer.addScore();  // Increment the score
        insertOrUpdateCustomer(customer);
    }
}
