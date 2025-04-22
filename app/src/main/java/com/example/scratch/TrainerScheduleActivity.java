package com.example.scratch;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.scratch.BottomNavTrainerHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class TrainerScheduleActivity extends AppCompatActivity {

    private ListView listViewSchedule;
    private ArrayAdapter<String> scheduleAdapter;
    private ArrayList<String> scheduleList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainer_schedule);

        listViewSchedule = findViewById(R.id.listViewSchedule);
        scheduleList = new ArrayList<>();
        scheduleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, scheduleList);
        listViewSchedule.setAdapter(scheduleAdapter);

        loadTrainerAppointments();

        // Bottom Nav Setup
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        BottomNavTrainerHelper.setup(this, bottomNavigationView, R.id.nav_trainerschedule);
    }

    private void loadTrainerAppointments() {
        // Get the current logged-in trainer's ID
        String trainerId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Get reference to the appointments in the Firebase Database
        DatabaseReference appointmentsRef = FirebaseDatabase.getInstance().getReference("Appointments");

        // Query the database to filter by trainerId
        appointmentsRef.orderByChild("trainerId")
                .equalTo(trainerId)  // Only fetch appointments that match the current trainer's ID
                .get()
                .addOnSuccessListener(snapshot -> {
                    scheduleList.clear();  // Clear the list before populating it

                    if (snapshot.exists()) {
                        for (DataSnapshot appointmentSnapshot : snapshot.getChildren()) {
                            String date = appointmentSnapshot.child("date").getValue(String.class);
                            String timeSlot = appointmentSnapshot.child("timeSlot").getValue(String.class);
                            String userId = appointmentSnapshot.child("userId").getValue(String.class);

                            // Optionally, you can also check for userId if you want appointments for a specific user.
                            // if(userId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) { ... }

                            String appointmentDetails = "Date: " + date + "\nTime Slot: " + timeSlot;
                            scheduleList.add(appointmentDetails);  // Add to the list to display in the ListView
                        }

                        // Notify the adapter that data has changed
                        scheduleAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "No appointments found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading appointments: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
