package com.example.scratch;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Spinner;
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

public class TrainerListActivity extends AppCompatActivity {

    private LinearLayout trainerListContainer;
    private Spinner spinnerWorkoutType;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainers_list);

        trainerListContainer = findViewById(R.id.trainerListContainer);
        spinnerWorkoutType = findViewById(R.id.spinnerWorkoutType);

        setupWorkoutTypeFilter();

        // Initialize Bottom Navigation View
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_dashboard) {
                    startActivity(new Intent(TrainerListActivity.this, DashboardActivity.class));
                    return true;
                } else if (itemId == R.id.nav_schedule) {
                    startActivity(new Intent(TrainerListActivity.this, ScheduleActivity.class));
                    return true;
                } else if (itemId == R.id.nav_workouts) {
                    return true;
                }
                return false;
            }
        });

        // Set default selected item (Workouts)
        bottomNavigationView.setSelectedItemId(R.id.nav_workouts);
    }

    private void setupWorkoutTypeFilter() {
        String[] workoutTypes = {"All", "Strength Training", "Cardio", "Yoga"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, workoutTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWorkoutType.setAdapter(adapter);

        spinnerWorkoutType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedType = parent.getItemAtPosition(position).toString();
                loadTrainers(selectedType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadTrainers(String workoutType) {
        DatabaseReference trainersRef = FirebaseDatabase.getInstance().getReference("Trainers");
        trainersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                trainerListContainer.removeAllViews();

                for (DataSnapshot trainerSnapshot : dataSnapshot.getChildren()) {
                    String fullName = trainerSnapshot.child("fullName").getValue(String.class);
                    String proficiency = trainerSnapshot.child("proficiency").getValue(String.class);
                    String contactInfo = trainerSnapshot.child("contactInfo").getValue(String.class);
                    String userId = trainerSnapshot.getKey();  // Get the UserId (trainer's unique ID)

                    if (workoutType.equals("All") || workoutType.equals(proficiency)) {
                        View trainerItem = getLayoutInflater().inflate(R.layout.trainer_item, trainerListContainer, false);
                        TextView tvTrainerName = trainerItem.findViewById(R.id.tvTrainerName);
                        tvTrainerName.setText(fullName);

                        trainerItem.setOnClickListener(v -> {
                            Intent intent = new Intent(TrainerListActivity.this, TrainerProfileActivity.class);
                            intent.putExtra("UserId", userId);  // Pass the UserId
                            intent.putExtra("TrainerName", fullName);
                            intent.putExtra("Proficiency", proficiency);
                            intent.putExtra("ContactInfo", contactInfo);
                            startActivity(intent);
                        });
                        trainerListContainer.addView(trainerItem);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }


}
