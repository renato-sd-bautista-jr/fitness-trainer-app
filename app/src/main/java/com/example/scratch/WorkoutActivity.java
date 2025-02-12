package com.example.scratch;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class WorkoutActivity extends AppCompatActivity {

    LinearLayout hypertrophyLayout, strengthLayout, cardioLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout);

        hypertrophyLayout = findViewById(R.id.rh5g8is0bbmw);
        strengthLayout = findViewById(R.id.rr0hf84q8jfp);
        cardioLayout = findViewById(R.id.r1gqbxct37cu);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        hypertrophyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToTrainers("Hypertrophy");
            }
        });

        strengthLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToTrainers("Strength");
            }
        });

        cardioLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToTrainers("Cardio");
            }
        });

        // Bottom Navigation Click Handling
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_dashboard) {
                    startActivity(new Intent(WorkoutActivity.this, DashboardActivity.class));
                    finish();
                    return true;

                } else if (itemId == R.id.nav_schedule) {
                    startActivity(new Intent(WorkoutActivity.this, ScheduleActivity.class));
                    finish();
                    return true;

                } else if (itemId == R.id.nav_workouts) {
                    return true; // Already in Workout Activity
                }

                return false;
            }
        });

        // Set default selected item
        bottomNavigationView.setSelectedItemId(R.id.nav_workouts);
    }

    private void navigateToTrainers(String workoutType) {
        Intent intent = new Intent(WorkoutActivity.this, TrainerListActivity.class);
        intent.putExtra("WorkoutType", workoutType);
        startActivity(intent);
    }
}
