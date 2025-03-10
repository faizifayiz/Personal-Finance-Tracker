package com.faizi_faiz.personalfinancetrackerapp;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.faizi_faiz.personalfinancetrackerapp.db.DatabaseHelper;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class ChartActivity extends AppCompatActivity {

    private PieChart pieChartOverall, pieChartCategories;
    private SharedPreferences sharedPreferences;
    private ArrayList<Expense> expenseList;
    private Button btnWeekly, btnMonthly;
    private String selectedView = "weekly"; // Default view
    private float totalIncome;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        pieChartOverall = findViewById(R.id.pieChartOverall);
        pieChartCategories = findViewById(R.id.pieChartCategories);
        btnWeekly = findViewById(R.id.btnWeekly);
        btnMonthly = findViewById(R.id.btnMonthly);

        databaseHelper = new DatabaseHelper(this);

        // Load user details from SharedPreferences
        sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "Guest");
        int userId = sharedPreferences.getInt("user_id", -1);

        if (userId == -1) {
            Toast.makeText(this, "User not found!", Toast.LENGTH_SHORT).show();
        } else {
            // Fetch income from database
            String savedIncome = databaseHelper.getUserIncome(username);

            try {
                totalIncome = Float.parseFloat(savedIncome);
            } catch (NumberFormatException e) {
                totalIncome = 0;
            }
        }



        sharedPreferences = getSharedPreferences("ExpenseData", MODE_PRIVATE);
        expenseList = loadExpenses();


        // Show initial chart
        updateCharts();

        // Handle button clicks
        btnWeekly.setOnClickListener(v -> {
            selectedView = "weekly";
            updateCharts();
        });

        btnMonthly.setOnClickListener(v -> {
            selectedView = "monthly";
            updateCharts();
        });
    }

    private void updateCharts() {
        showOverallChart();
        showCategoryChart();
    }

    private void showOverallChart() {
        float totalExpense = getTotalExpense();
        float remainingBalance = totalIncome - totalExpense;

        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(totalIncome, "Income"));
        entries.add(new PieEntry(totalExpense, "Expense"));
        entries.add(new PieEntry(remainingBalance, "Remaining"));

        PieDataSet dataSet = new PieDataSet(entries, "Overall Finance");
        dataSet.setColors(Color.GREEN, Color.RED, Color.BLUE);
        dataSet.setValueTextSize(12f);

        PieData pieData = new PieData(dataSet);
        pieChartOverall.setData(pieData);
        pieChartOverall.invalidate(); // Refresh chart
    }


    private void showCategoryChart() {
        HashMap<String, Float> categoryExpenses = getCategoryExpenses();
        ArrayList<PieEntry> entries = new ArrayList<>();

        for (String category : categoryExpenses.keySet()) {
            entries.add(new PieEntry(categoryExpenses.get(category), category));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Expenses by Category");
        dataSet.setColors(Color.YELLOW, Color.CYAN, Color.MAGENTA, Color.LTGRAY, Color.DKGRAY);
        dataSet.setValueTextSize(12f);

        PieData pieData = new PieData(dataSet);
        pieChartCategories.setData(pieData);
        pieChartCategories.invalidate();
    }

    private float getTotalExpense() {
        float total = 0;
        for (Expense expense : filterExpensesByDate()) {
            total += Float.parseFloat(expense.getAmount());
        }
        return total;
    }

    private HashMap<String, Float> getCategoryExpenses() {
        HashMap<String, Float> categoryMap = new HashMap<>();
        for (Expense expense : filterExpensesByDate()) {
            String category = expense.getCategory();
            float amount = Float.parseFloat(expense.getAmount());
            categoryMap.put(category, categoryMap.getOrDefault(category, 0f) + amount);
        }
        return categoryMap;
    }

    private ArrayList<Expense> filterExpensesByDate() {
        ArrayList<Expense> filteredExpenses = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        Date now = new Date();
        cal.setTime(now);

        if (selectedView.equals("weekly")) {
            // Get start of the week (Monday)
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        } else {
            // Get start of the month
            cal.set(Calendar.DAY_OF_MONTH, 1);
        }

        Date startDate = cal.getTime();

        for (Expense expense : expenseList) {
            try {
                Log.d("ChartActivity", "Checking expense date: " + expense.getDate());
                Date expenseDate = sdf.parse(expense.getDate());

                if (expenseDate != null && !expenseDate.before(startDate)) {
                    filteredExpenses.add(expense);
                } else {
                    Log.d("ChartActivity", "Excluded: " + expense.getDate());
                }
            } catch (Exception e) {
                Log.e("ChartActivity", "Error parsing date: " + expense.getDate(), e);
            }
        }

        return filteredExpenses;
    }

    private ArrayList<Expense> loadExpenses() {
        ArrayList<Expense> expenses = new ArrayList<>();
        String json = sharedPreferences.getString("expenses", "[]");
        SimpleDateFormat inputFormat = new SimpleDateFormat("M/d/yyyy", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String date = obj.getString("date");

                // Convert date format
                Date parsedDate = inputFormat.parse(date);
                String formattedDate = parsedDate != null ? outputFormat.format(parsedDate) : date;

                expenses.add(new Expense(
                        obj.getString("category"),
                        obj.getString("amount"),
                        formattedDate, // Save converted date
                        obj.getString("notes")
                ));
            }
        } catch (JSONException | ParseException e) {
            Log.e("ChartActivity", "Error parsing expenses JSON", e);
        }
        return expenses;
    }
}