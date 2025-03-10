package com.faizi_faiz.personalfinancetrackerapp;


import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class BudgetActivity extends Activity {

    private EditText foodBudget, transportBudget, entertainmentBudget, billsBudget, miscellaneousBudget;
    private Button saveBudgetButton;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);

        // Initialize views
        foodBudget = findViewById(R.id.foodBudget);
        transportBudget = findViewById(R.id.transportBudget);
        entertainmentBudget = findViewById(R.id.entertainmentBudget);
        billsBudget = findViewById(R.id.billsBudget);
        miscellaneousBudget = findViewById(R.id.miscellaneousBudget);
        saveBudgetButton = findViewById(R.id.saveBudgetButton);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("FinanceTracker", MODE_PRIVATE);

        // Load saved budgets
        loadBudgets();

        // Save budget button click listener
        saveBudgetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveBudgets();
            }
        });
    }

    private void loadBudgets() {
        foodBudget.setText(sharedPreferences.getString("foodBudget", ""));
        transportBudget.setText(sharedPreferences.getString("transportBudget", ""));
        entertainmentBudget.setText(sharedPreferences.getString("entertainmentBudget", ""));
        billsBudget.setText(sharedPreferences.getString("billsBudget", ""));
        miscellaneousBudget.setText(sharedPreferences.getString("miscellaneousBudget", ""));
    }

    private void saveBudgets() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("foodBudget", foodBudget.getText().toString());
        editor.putString("transportBudget", transportBudget.getText().toString());
        editor.putString("entertainmentBudget", entertainmentBudget.getText().toString());
        editor.putString("billsBudget", billsBudget.getText().toString());
        editor.putString("miscellaneousBudget", miscellaneousBudget.getText().toString());
        editor.apply();
        Toast.makeText(this, "Budgets saved", Toast.LENGTH_SHORT).show();
    }
}