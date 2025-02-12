package com.example.scratch;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize Bottom Navigation View
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Set listener for navigation item selection
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    startActivity(new Intent(DashboardActivity.this, HomeDashboardActivity.class));
                    return true;
                } else if (itemId == R.id.nav_dashboard) {
                    // Already in Dashboard, no need to switch
                    return true;
                } else if (itemId == R.id.nav_schedule) {
                    startActivity(new Intent(DashboardActivity.this, ScheduleActivity.class));
                    return true;
                } else if (itemId == R.id.nav_workouts) {
                    startActivity(new Intent(DashboardActivity.this, WorkoutActivity.class));
                    return true;
                } else if (itemId == R.id.nav_trainer) {
                    startActivity(new Intent(DashboardActivity.this, TrainerActivity.class));
                    return true;
                }

                return false;
            }
        });

        // Set default selected item (e.g., Dashboard)
        bottomNavigationView.setSelectedItemId(R.id.nav_dashboard);
    }
}
