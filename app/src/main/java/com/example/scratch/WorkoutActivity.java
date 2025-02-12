package com.example.scratch;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;

public class WorkoutActivity extends AppCompatActivity {

    private WorkoutAdapter workoutAdapter;
    private List<String> workoutList, filteredList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout);

        RecyclerView recyclerViewWorkouts = findViewById(R.id.recyclerViewWorkouts);
        TextInputEditText etSearchWorkout = findViewById(R.id.etSearchWorkout);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Sample workouts
        workoutList = new ArrayList<>();
        workoutList.add("Push-ups");
        workoutList.add("Squats");
        workoutList.add("Burpees");
        workoutList.add("Plank");
        workoutList.add("Jump Rope");

        // Set up RecyclerView
        filteredList = new ArrayList<>(workoutList);
        workoutAdapter = new WorkoutAdapter(filteredList);
        recyclerViewWorkouts.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewWorkouts.setAdapter(workoutAdapter);

        // Search functionality
        etSearchWorkout.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filterWorkouts(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        // Handle Bottom Navigation Clicks
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    startActivity(new Intent(WorkoutActivity.this, HomeDashboardActivity.class));
                    return true;
                } else if (itemId == R.id.nav_dashboard) {
                    startActivity(new Intent(WorkoutActivity.this, DashboardActivity.class));
                    return true;
                } else if (itemId == R.id.nav_schedule) {
                    startActivity(new Intent(WorkoutActivity.this, ScheduleActivity.class));
                    return true;
                } else if (itemId == R.id.nav_workouts) {
                    return true; // Already in Workouts
                } else if (itemId == R.id.nav_trainers) {
                    startActivity(new Intent(WorkoutActivity.this, TrainerActivity.class));
                    return true;
                }

                return false;
            }
        });

        // Set default selected item
        bottomNavigationView.setSelectedItemId(R.id.nav_workouts);
    }

    private void filterWorkouts(String query) {
        filteredList.clear();
        for (String workout : workoutList) {
            if (workout.toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(workout);
            }
        }
        workoutAdapter.notifyDataSetChanged();
    }
}
