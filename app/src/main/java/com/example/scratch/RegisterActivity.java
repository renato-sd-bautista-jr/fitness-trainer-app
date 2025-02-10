package com.example.scratch;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import android.text.SpannableString;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.graphics.Color;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etFullName, etUsername, etPassword, etConfirmPassword, etMobileNumber;
    private Button btnRegister;
    private TextView tvLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etEmail = findViewById(R.id.etEmail);
        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etMobileNumber = findViewById(R.id.etMobileNumber);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);

        // Set different colors for "Don't have an account?" and "Register"
        SpannableString spannable = new SpannableString("Already have an account? Login");
        spannable.setSpan(new ForegroundColorSpan(Color.WHITE), 0, 22, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // Black text
        spannable.setSpan(new ForegroundColorSpan(Color.BLUE), 23, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // Blue text

        tvLogin.setText(spannable);

        btnRegister.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String fullName = etFullName.getText().toString().trim();
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();
            String mobileNumber = etMobileNumber.getText().toString().trim();

            if (email.isEmpty() || fullName.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || mobileNumber.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else if (!password.equals(confirmPassword)) {
                Toast.makeText(RegisterActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(RegisterActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
            }
        });

        tvLogin.setOnClickListener(v -> startActivity(new Intent(RegisterActivity.this, LoginActivity.class)));
    }
}
