package com.faizi_faiz.personalfinancetrackerapp;



import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.ArrayList;

public class GlobalPreferenceManager {

    private static final String PREF_NAME = "FinanceTrackerPrefs";
    private static GlobalPreferenceManager instance;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    // Private constructor to enforce singleton pattern
    private GlobalPreferenceManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // Singleton instance getter
    public static synchronized GlobalPreferenceManager getInstance(Context context) {
        if (instance == null) {
            instance = new GlobalPreferenceManager(context);
        }
        return instance;
    }

    // Save a string value
    public void saveString(String key, String value) {
        editor.putString(key, value);
        editor.apply();
    }

    // Retrieve a string value
    public String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    // Save an integer value
    public void saveInt(String key, int value) {
        editor.putInt(key, value);
        editor.apply();
    }

    // Retrieve an integer value
    public int getInt(String key, int defaultValue) {
        return sharedPreferences.getInt(key, defaultValue);
    }

    // Save a boolean value
    public void saveBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.apply();
    }

    // Retrieve a boolean value
    public boolean getBoolean(String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    // Save a list of expenses
    public void saveExpenses(String key, ArrayList<String> expenses) {
        JSONArray jsonArray = new JSONArray(expenses);
        editor.putString(key, jsonArray.toString());
        editor.apply();
    }

    // Retrieve a list of expenses
    public ArrayList<String> getExpenses(String key) {
        String json = sharedPreferences.getString(key, "[]");
        ArrayList<String> expenses = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                expenses.add(jsonArray.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return expenses;
    }

    // Clear all preferences
    public void clearAllPreferences() {
        editor.clear();
        editor.apply();
    }
}