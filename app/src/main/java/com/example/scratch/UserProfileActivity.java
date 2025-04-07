package com.example.scratch;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

public class UserProfileActivity extends AppCompatActivity {

    private Button btnEdit, btnSave, btnReset, btnCalculate;
    private EditText etFirstName, etLastName, etEmail, etHeight, etWeight, etBmi;
    private DatabaseReference userRef;
    private FirebaseAuth mAuth;
    private boolean isEditing = true;
    private String initialFirstName, initialLastName, initialEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userprofile);

        btnEdit = findViewById(R.id.btnEdit);
        btnSave = findViewById(R.id.btnSave);
        btnReset = findViewById(R.id.btnReset);
        btnCalculate = findViewById(R.id.btnCalculate);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);
        etHeight = findViewById(R.id.etHeight);
        etWeight = findViewById(R.id.etWeight);
        etBmi = findViewById(R.id.etBmi);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String userId = currentUser.getUid();
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        loadUserProfile();

        btnEdit.setOnClickListener(v -> enableEditing(true));
        btnSave.setOnClickListener(v -> saveUserProfile());
        btnReset.setOnClickListener(v -> resetFields());
        btnCalculate.setOnClickListener(v -> calculateBmi());
    }

    private void loadUserProfile() {
        userRef.get().addOnSuccessListener(snapshot -> {
            Log.d("UserProfile", "Snapshot: " + snapshot.toString());
            if (snapshot.exists()) {
                initialFirstName = snapshot.child("firstName").getValue(String.class);
                initialLastName = snapshot.child("lastName").getValue(String.class);
                initialEmail = snapshot.child("email").getValue(String.class);

                etFirstName.setText(initialFirstName != null ? initialFirstName : "");
                etLastName.setText(initialLastName != null ? initialLastName : "");
                etEmail.setText(initialEmail != null ? initialEmail : "");
            } else {
                Toast.makeText(this, "User profile not found", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show();
        });
    }

    private void enableEditing(boolean enable) {
        isEditing = enable;
        etFirstName.setEnabled(enable);
        etLastName.setEnabled(enable);
        etEmail.setEnabled(enable);
        etHeight.setEnabled(enable);
        etWeight.setEnabled(enable);
        etBmi.setEnabled(enable);
        btnEdit.setVisibility(enable ? View.GONE : View.VISIBLE);
        btnSave.setVisibility(enable ? View.VISIBLE : View.GONE);
        btnReset.setVisibility(enable ? View.VISIBLE : View.GONE);
        btnCalculate.setEnabled(enable);
    }

    private void saveUserProfile() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("firstName", etFirstName.getText().toString().trim());
        updates.put("lastName", etLastName.getText().toString().trim());
        updates.put("email", etEmail.getText().toString().trim());

        userRef.updateChildren(updates).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
            enableEditing(false);
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
        });
    }

    private void resetFields() {
        etFirstName.setText(initialFirstName);
        etLastName.setText(initialLastName);
        etEmail.setText(initialEmail);
    }

    private void calculateBmi() {
        String heightStr = etHeight.getText().toString().trim();
        String weightStr = etWeight.getText().toString().trim();
        String bmiStr = etBmi.getText().toString().trim();

        if (!heightStr.isEmpty() && !weightStr.isEmpty()) {
            double height = Double.parseDouble(heightStr) / 100.0;
            double weight = Double.parseDouble(weightStr);
            double bmi = weight / (height * height);
            etBmi.setText(String.format("%.2f", bmi));
        } else if (!weightStr.isEmpty() && !bmiStr.isEmpty()) {
            double weight = Double.parseDouble(weightStr);
            double bmi = Double.parseDouble(bmiStr);
            double height = Math.sqrt(weight / bmi) * 100;
            etHeight.setText(String.format("%.2f", height));
        } else {
            Toast.makeText(this, "Provide height and weight or weight and BMI", Toast.LENGTH_SHORT).show();
        }
    }
}
