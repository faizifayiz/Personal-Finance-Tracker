package com.faizi_faiz.personalfinancetrackerapp.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.faizi_faiz.personalfinancetrackerapp.Expense;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "FinanceTracker.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_USERS = "users";
    private static final String TABLE_EXPENSES = "expenses";
    // Column names
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_INCOME = "income";

    private static final String COLUMN_EXPENSE_ID = "id";
    private static final String COLUMN_EXPENSE_USER_ID = "user_id"; // Foreign Key
    private static final String COLUMN_EXPENSE_CATEGORY = "category";
    private static final String COLUMN_EXPENSE_AMOUNT = "amount";
    private static final String COLUMN_EXPENSE_DATE = "date";
    private static final String COLUMN_EXPENSE_NOTES = "notes";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_NAME + " TEXT, "
                + COLUMN_USERNAME + " TEXT UNIQUE, "
                + COLUMN_EMAIL + " TEXT, "
                + COLUMN_PASSWORD + " TEXT, "
                + COLUMN_INCOME + " TEXT)";

        // Create Expenses Table with Foreign Key
        String createExpensesTable = "CREATE TABLE " + TABLE_EXPENSES + " (" +
                COLUMN_EXPENSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_EXPENSE_USER_ID + " INTEGER, " +
                COLUMN_EXPENSE_CATEGORY + " TEXT, " +
                COLUMN_EXPENSE_AMOUNT + " TEXT, " +
                COLUMN_EXPENSE_DATE + " TEXT, " +
                COLUMN_EXPENSE_NOTES + " TEXT, " +
                "FOREIGN KEY(" + COLUMN_EXPENSE_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + "))";

        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(createExpensesTable);
    }



    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // Insert user data (used during registration)
    public boolean insertUser(String name, String username, String email, String password, String income) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_INCOME, income);

        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result != -1; // Return true if insert successful
    }

    // Check if user exists (used in LoginActivity)
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " +
                COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD + " = ?", new String[]{username, password});

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }
    public int getUserIdByUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        int userId = -1;

        Cursor cursor = db.rawQuery("SELECT id FROM users WHERE username = ?", new String[]{username});
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(0);
        }
        cursor.close();
        db.close();

        return userId;
    }

    // Get user's income
    // Get Username by User ID
    public String getUsernameById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String username = "Guest"; // Default value

        Cursor cursor = db.rawQuery("SELECT username FROM users WHERE id = ?", new String[]{String.valueOf(userId)});
        if (cursor.moveToFirst()) {
            username = cursor.getString(0);
        }
        cursor.close();
        db.close();

        return username;
    }

    // Get Income by Username
    public String getUserIncome(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String income = "0"; // Default value

        Cursor cursor = db.rawQuery("SELECT income FROM users WHERE username = ?", new String[]{username});
        if (cursor.moveToFirst()) {
            income = cursor.getString(0);
        }
        cursor.close();
        db.close();

        return income;
    }


    public boolean insertExpense(int userId, String category, String amount, String date, String notes) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("category", category);
        values.put("amount", amount);
        values.put("date", date);
        values.put("notes", notes);

        long result = db.insert("expenses", null, values);
        db.close();
        return result != -1;  // Returns true if insert was successful
    }
    public ArrayList<Expense> getExpensesByUserId(int userId) {
        ArrayList<Expense> expenses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM expenses WHERE user_id = ?", new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                Expense expense = new Expense(
                        cursor.getString(cursor.getColumnIndexOrThrow("category")),
                        cursor.getString(cursor.getColumnIndexOrThrow("amount")),
                        cursor.getString(cursor.getColumnIndexOrThrow("date")),
                        cursor.getString(cursor.getColumnIndexOrThrow("notes"))
                );
                expenses.add(expense);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return expenses;
    }

    public int getTotalExpenses(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(amount) FROM expenses WHERE user_id=?", new String[]{String.valueOf(userId)});

        int total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return total;
    }



}
