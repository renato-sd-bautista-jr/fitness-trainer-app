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
        DatabaseReference appointmentsRef = FirebaseDatabase.getInstance().getReference("Appointments");

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        trainersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot trainersSnapshot) {
                trainerListContainer.removeAllViews();

                for (DataSnapshot trainerSnapshot : trainersSnapshot.getChildren()) {
                    String userId = trainerSnapshot.getKey();
                    if (userId != null && userId.equals(currentUserId)) continue;

                    String fullName = trainerSnapshot.child("fullName").getValue(String.class);
                    String proficiency = trainerSnapshot.child("proficiency").getValue(String.class);
                    String contactInfo = trainerSnapshot.child("contactInfo").getValue(String.class);

                    Boolean isAvailable = trainerSnapshot.child("isAvailable").getValue(Boolean.class);
                    if (isAvailable == null) isAvailable = true;

                    if (workoutType.equals("All") || workoutType.equals(proficiency)) {

                        View trainerItem = getLayoutInflater().inflate(R.layout.trainer_item, trainerListContainer, false);
                        TextView tvTrainerName = trainerItem.findViewById(R.id.tvTrainerName);
                        TextView tvTrainerProficiency = trainerItem.findViewById(R.id.tvTrainerProficiency);
                        TextView tvTrainerAvailability = trainerItem.findViewById(R.id.tvTrainerAvailability);
                        TextView tvTrainerRating = trainerItem.findViewById(R.id.tvTrainerRating); // Make sure this exists in your layout

                        tvTrainerName.setText(fullName);
                        tvTrainerProficiency.setText("Specialties: " + proficiency);

                        if (isAvailable) {
                            tvTrainerAvailability.setText("Available");
                            tvTrainerAvailability.setTextColor(getResources().getColor(R.color.green));
                        } else {
                            tvTrainerAvailability.setText("Unavailable");
                            tvTrainerAvailability.setTextColor(getResources().getColor(R.color.red));
                        }

                        // Load ratings from Appointments node
                        appointmentsRef.orderByChild("trainerId").equalTo(userId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        float totalRating = 0;
                                        int count = 0;
                                        for (DataSnapshot appointment : snapshot.getChildren()) {
                                            String status = appointment.child("status").getValue(String.class);
                                            String ratingStr = appointment.child("ratings").getValue(String.class);

                                            if ("Completed".equalsIgnoreCase(status) && ratingStr != null) {
                                                try {
                                                    float rating = Float.parseFloat(ratingStr);
                                                    totalRating += rating;
                                                    count++;
                                                } catch (NumberFormatException ignored) {}
                                            }
                                        }

                                        if (count > 0) {
                                            float avgRating = totalRating / count;
                                            tvTrainerRating.setText(String.format("Rating: %.1f ★", avgRating));
                                        } else {
                                            tvTrainerRating.setText("No ratings yet");
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        tvTrainerRating.setText("Rating unavailable");
                                    }
                                });

                        trainerItem.setOnClickListener(v -> {
                            Intent intent = new Intent(TrainerListActivity.this, TrainerProfileActivity.class);
                            intent.putExtra("trainerId", userId);
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
