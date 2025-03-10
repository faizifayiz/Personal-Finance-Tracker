package com.faizi_faiz.personalfinancetrackerapp;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.faizi_faiz.personalfinancetrackerapp.Adapter.ExpenseAdapter;
import com.faizi_faiz.personalfinancetrackerapp.db.DatabaseHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private RecyclerView expenseRecyclerView;
    private ExpenseAdapter expenseAdapter;
    private ArrayList<Expense> expenseList;
    private SharedPreferences sharedPreferences;
    private DatabaseHelper databaseHelper;

    private EditText expenseAmount, expenseDate, expenseNotes;
    private Spinner expenseCategory;
    private TextView nameTV, incomeTV;
    private ImageView Inlogout;
    private CardView addExpenseButton, setbudget, chartcard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        // Initialize Views
        initViews();

        // Load user data
        loadUserData();

        // Initialize RecyclerView
        setupRecyclerView();

        // Set up button actions
        setButtonListeners();
    }

    private void initViews() {
        expenseRecyclerView = findViewById(R.id.expenseRecyclerView);
        addExpenseButton = findViewById(R.id.addExpenseCard);
        setbudget = findViewById(R.id.budgetCard);
        chartcard = findViewById(R.id.chartCard);
        expenseAmount = findViewById(R.id.expenseAmount);
        expenseDate = findViewById(R.id.expenseDate);
        expenseNotes = findViewById(R.id.expenseNotes);
        expenseCategory = findViewById(R.id.expenseCategory);
        incomeTV = findViewById(R.id.userIncome);
        nameTV = findViewById(R.id.userName);
        Inlogout = findViewById(R.id.logout);

        databaseHelper = new DatabaseHelper(this);
    }

    private void loadUserData() {
        sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "Guest");
        int userId = sharedPreferences.getInt("user_id", -1);

        if (userId == -1) {
            Toast.makeText(this, "User not found!", Toast.LENGTH_SHORT).show();
        } else {
            String savedIncome = databaseHelper.getUserIncome(username);
            nameTV.setText("Welcome, " + username);
            incomeTV.setText("Income: $" + (savedIncome != null ? savedIncome : "0"));
        }
    }

    private void setupRecyclerView() {
        expenseRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        expenseList = loadExpenses();
        expenseAdapter = new ExpenseAdapter(expenseList);
        expenseRecyclerView.setAdapter(expenseAdapter);
    }

    private void setButtonListeners() {
        addExpenseButton.setOnClickListener(v -> addExpense());
        Inlogout.setOnClickListener(v -> logout());
        setbudget.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, BudgetActivity.class)));
        chartcard.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ChartActivity.class)));
    }

    private void addExpense() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_expense, null);
        builder.setView(dialogView);

        EditText expenseAmount = dialogView.findViewById(R.id.expenseAmount);
        EditText expenseDate = dialogView.findViewById(R.id.expenseDate);
        EditText expenseNotes = dialogView.findViewById(R.id.expenseNotes);
        Spinner expenseCategory = dialogView.findViewById(R.id.expenseCategory);
        Button btnSaveExpense = dialogView.findViewById(R.id.btnSaveExpense);

        AlertDialog dialog = builder.create();
        dialog.show();

        expenseDate.setOnClickListener(v -> showDatePicker(expenseDate));

        btnSaveExpense.setOnClickListener(v -> {
            String amount = expenseAmount.getText().toString();
            String date = expenseDate.getText().toString();
            String notes = expenseNotes.getText().toString();
            String category = expenseCategory.getSelectedItem().toString();

            if (amount.isEmpty() || date.isEmpty()) {
                Toast.makeText(this, "Please enter all details", Toast.LENGTH_SHORT).show();
                return;
            }

            Expense expense = new Expense(category, amount, date, notes);
            expenseList.add(expense);
            expenseAdapter.notifyDataSetChanged();

            saveExpenses(expenseList, getUserId());

            dialog.dismiss();
        });
    }

    private void showDatePicker(EditText expenseDate) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            expenseDate.setText(selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear);
        }, year, month, day);
        datePickerDialog.show();
    }

    private int getUserId() {
        return sharedPreferences.getInt("user_id", -1);
    }

    private void saveExpenses(ArrayList<Expense> expenses, int userId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        JSONArray jsonArray = new JSONArray();
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        for (Expense expense : expenses) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("category", expense.getCategory());
                jsonObject.put("amount", expense.getAmount());
                jsonObject.put("date", expense.getDate());
                jsonObject.put("notes", expense.getNotes());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            jsonArray.put(jsonObject);

            ContentValues values = new ContentValues();
            values.put("user_id", userId);
            values.put("category", expense.getCategory());
            values.put("amount", expense.getAmount());
            values.put("date", expense.getDate());
            values.put("notes", expense.getNotes());
            db.insert("expenses", null, values);
        }

        editor.putString("expenses", jsonArray.toString());
        editor.apply();
        db.close();

        checkExpenseLimit(userId);
    }

    private void checkExpenseLimit(int userId) {
        String savedIncome = databaseHelper.getUserIncome(nameTV.getText().toString());
        double income = savedIncome != null ? Double.parseDouble(savedIncome) : 0;
        double totalExpenses = databaseHelper.getTotalExpenses(userId);

        if (totalExpenses > income) {
            showExpenseNotification();
        }
    }

    private void showExpenseNotification() {
        String channelId = "expense_alert_channel";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Expense Alerts", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notifies when expenses exceed income");
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_baseline_notification_important_24)
                .setContentTitle("Budget Alert!")
                .setContentText("Your expenses have exceeded your income!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        notificationManager.notify(1, builder.build());
    }

    private ArrayList<Expense> loadExpenses() {
        return databaseHelper.getExpensesByUserId(getUserId());
    }

    private void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


}
