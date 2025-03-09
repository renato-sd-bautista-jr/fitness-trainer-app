package com.example.scratch;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.example.scratch.R;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etLogin;
    private TextInputEditText etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference("Users");

        etLogin = findViewById(R.id.etLogin);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        btnLogin.setOnClickListener(v -> loginUser());

        tvRegister.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
    }

    private void loginUser() {
        String loginInput = etLogin.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (loginInput.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Patterns.EMAIL_ADDRESS.matcher(loginInput).matches()) {
            signInWithEmail(loginInput, password);
        } else {
            databaseRef.orderByChild("username").equalTo(loginInput).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().hasChildren()) {
                    for (DataSnapshot snapshot : task.getResult().getChildren()) {
                        String email = snapshot.child("email").getValue(String.class);
                        if (email != null) {
                            signInWithEmail(email, password);
                        }
                        break;
                    }
                } else {
                    databaseRef.orderByChild("mobileNumber").equalTo(loginInput).get().addOnCompleteListener(task2 -> {
                        if (task2.isSuccessful() && task2.getResult().hasChildren()) {
                            for (DataSnapshot snapshot : task2.getResult().getChildren()) {
                                String email = snapshot.child("email").getValue(String.class);
                                if (email != null) {
                                    signInWithEmail(email, password);
                                }
                                break;
                            }
                        } else {
                            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }

    private void signInWithEmail(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
