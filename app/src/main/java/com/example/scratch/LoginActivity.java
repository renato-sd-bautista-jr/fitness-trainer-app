package com.example.scratch; // Ensure the package is correct

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Layout for LoginActivity

        // Find the login button by ID
        Button btnLogin = findViewById(R.id.btnLogin);

        // Set OnClickListener for the login button
        btnLogin.setOnClickListener(v -> {
            // Start HomeDashboardActivity instead of trying to launch a fragment directly
            Intent intent = new Intent(LoginActivity.this, HomeDashboardActivity.class);
            startActivity(intent);
            finish(); // Close LoginActivity so the user can't go back to it
        });
    }
}
