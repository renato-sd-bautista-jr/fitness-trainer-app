package com.example.scratch;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvGreeting, tvBmiValue;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser().getUid(); // Get logged-in user ID


        userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        // Initialize UI elements
        tvGreeting = findViewById(R.id.tvGreeting);
        tvBmiValue = findViewById(R.id.tvBmiValue);

        // Fetch and display user data
        loadUserData();

        // Initialize Bottom Navigation View
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_dashboard) {
                    return true;
                } else if (itemId == R.id.nav_schedule) {
                    startActivity(new Intent(DashboardActivity.this, ScheduleActivity.class));
                    return true;
                } else if (itemId == R.id.nav_workouts) {
                    startActivity(new Intent(DashboardActivity.this, WorkoutActivity.class));
                    return true;
                }
                return false;
            }
        });

        // Set default selected item (Dashboard)
        bottomNavigationView.setSelectedItemId(R.id.nav_dashboard);

        // Handle settings click
        ImageView ivSettings = findViewById(R.id.ivSettings);
        ivSettings.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(DashboardActivity.this, v);
            popupMenu.getMenuInflater().inflate(R.menu.menu_settings, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.menu_profile) {
                    startActivity(new Intent(DashboardActivity.this, ProfileActivity.class));
                } else if (itemId == R.id.menu_settings) {
                    startActivity(new Intent(DashboardActivity.this, SettingsActivity.class));
                } else if (itemId == R.id.menu_register_trainer) {
                    startActivity(new Intent(DashboardActivity.this, RegisterTrainerActivity.class));
                } else if (itemId == R.id.menu_logout) {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();  // Close the current activity
                }
                return true;
            });
            popupMenu.show();
        });
    }

    private void loadUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("UserData", "Snapshot: " + snapshot.getValue());

                if (snapshot.exists()) {
                    String firstName = snapshot.child("firstName").getValue(String.class);
                    if (firstName != null) {
                        tvGreeting.setText("Hello, " + firstName);
                    } else {
                        tvGreeting.setText("Hello, User");
                    }

                    Double bmi = snapshot.child("bmi").getValue(Double.class);
                    if (bmi != null) {
                        tvBmiValue.setText(String.format("%.2f", bmi));
                    } else {
                        tvBmiValue.setText("N/A");
                    }
                } else {
                    tvGreeting.setText("Hello, User");
                    tvBmiValue.setText("N/A");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors
                tvGreeting.setText("Error loading data");
                tvBmiValue.setText("N/A");
            }
        });
    }
}
