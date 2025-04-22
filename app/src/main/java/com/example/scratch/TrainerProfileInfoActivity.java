package com.example.scratch;

import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.scratch.BottomNavTrainerHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrainerProfileInfoActivity extends AppCompatActivity {

    private TextView tvFullName, tvProficiency, tvContact;
    private LinearLayout scheduleContainer;
    private Button btnEditProfile;

    private DatabaseReference trainerRef;
    private String currentTrainerId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainer_profile_info);

        // Initialize Views
        tvFullName = findViewById(R.id.tvFullName);
        tvProficiency = findViewById(R.id.tvProficiency);
        tvContact = findViewById(R.id.tvContact);
        scheduleContainer = findViewById(R.id.scheduleContainer);
        btnEditProfile = findViewById(R.id.btnEditProfile);

        // Get current trainer UID from Firebase Auth
        currentTrainerId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Firebase reference to this trainer's node
        trainerRef = FirebaseDatabase.getInstance().getReference("Trainers").child(currentTrainerId);

        loadTrainerInfo();

        btnEditProfile.setOnClickListener(view -> {
            Toast.makeText(this, "Edit Profile Clicked (future function)", Toast.LENGTH_SHORT).show();
            // Intent to edit profile can be added here


        });

        // Bottom Nav Setup
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        BottomNavTrainerHelper.setup(this, bottomNavigationView, R.id.nav_trainerprofile);

    }

    private void loadTrainerInfo() {
        trainerRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                if (snapshot.exists()) {
                    String name = snapshot.child("fullName").getValue(String.class);
                    String proficiency = snapshot.child("proficiency").getValue(String.class);
                    String contactInfo = snapshot.child("contactInfo").getValue(String.class);

                    // Set TextViews
                    tvFullName.setText(name != null ? name : "N/A");
                    tvProficiency.setText("Proficiency: " + (proficiency != null ? proficiency : "N/A"));
                    tvContact.setText("Contact: " + (contactInfo != null ? contactInfo : "N/A"));

                    // Parse and display schedule
                    DataSnapshot scheduleSnapshot = snapshot.child("availableSchedule");
                    Map<String, List<String>> scheduleMap = new HashMap<>();

                    for (DataSnapshot daySnapshot : scheduleSnapshot.getChildren()) {
                        List<String> timeSlots = new ArrayList<>();
                        for (DataSnapshot timeSlot : daySnapshot.getChildren()) {
                            timeSlots.add(timeSlot.getValue(String.class));
                        }
                        scheduleMap.put(daySnapshot.getKey(), timeSlots);
                    }

                    displaySchedule(scheduleMap);
                } else {
                    Toast.makeText(this, "Trainer data not found.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Failed to load profile info.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displaySchedule(Map<String, List<String>> scheduleMap) {
        scheduleContainer.removeAllViews();
        for (String day : scheduleMap.keySet()) {
            TextView dayView = new TextView(this);
            dayView.setText(day + ": " + String.join(", ", scheduleMap.get(day)));
            dayView.setTextSize(16f);
            dayView.setTextColor(getResources().getColor(android.R.color.darker_gray));
            dayView.setPadding(0, 8, 0, 8);
            scheduleContainer.addView(dayView);
        }
    }
}
