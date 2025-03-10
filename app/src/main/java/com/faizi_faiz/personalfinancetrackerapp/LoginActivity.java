package com.faizi_faiz.personalfinancetrackerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.faizi_faiz.personalfinancetrackerapp.db.DatabaseHelper;
import com.google.android.material.button.MaterialButton;

import java.util.concurrent.Executor;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private MaterialButton btnLogin, btnBiometric;
    private TextView btnRegister;
    private DatabaseHelper databaseHelper;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Use a single SharedPreferences instance
        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);

        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        if (isLoggedIn) {
            String savedUsername = sharedPreferences.getString("username", "");
            String savedIncome = sharedPreferences.getString("income", "");
            navigateToMainActivity(savedUsername, savedIncome);
        }

        authenticateBiometric(); // Trigger biometric authentication on entry

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.tvSignUp);
        databaseHelper = new DatabaseHelper(this);

        btnLogin.setOnClickListener(v -> login());
        btnRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void login() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (databaseHelper.checkUser(username, password)) {
            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();

            // Retrieve income and save login state
            String userIncome = databaseHelper.getUserIncome(username);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isLoggedIn", true);
            editor.putString("username", username);
            editor.putString("income", userIncome);
            editor.apply();

            navigateToMainActivity(username, userIncome);
        } else {
            Toast.makeText(this, "Invalid Username or Password", Toast.LENGTH_SHORT).show();
        }
    }

    private void authenticateBiometric() {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                Toast.makeText(LoginActivity.this, "Authentication Successful", Toast.LENGTH_SHORT).show();
                autoLogin();
            }

            @Override
            public void onAuthenticationFailed() {
                Toast.makeText(LoginActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(LoginActivity.this, "Authentication Error: " + errString, Toast.LENGTH_SHORT).show();
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Login with Biometrics or Password")
                .setSubtitle("Use fingerprint, face recognition, or device password to continue")
                .setDeviceCredentialAllowed(true) // Enables device password/PIN authentication
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    private void autoLogin() {
        String savedUsername = sharedPreferences.getString("username", "");
        String savedIncome = sharedPreferences.getString("income", "");
        if (!savedUsername.isEmpty()) {
            navigateToMainActivity(savedUsername, savedIncome);
        }
    }

    private void navigateToMainActivity(String username, String income) {
        // Ensure databaseHelper is initialized
        if (databaseHelper == null) {
            databaseHelper = new DatabaseHelper(this);
        }

        int userId = databaseHelper.getUserIdByUsername(username);

        SharedPreferences sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", username);
        editor.putInt("user_id", userId);
        editor.apply();

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

}
