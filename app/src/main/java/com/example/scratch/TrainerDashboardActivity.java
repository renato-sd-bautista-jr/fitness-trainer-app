package com.example.scratch;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.scratch.BottomNavTrainerHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashSet;
import java.util.Set;

public class TrainerDashboardActivity extends AppCompatActivity {

    private TextView tvWelcomeTrainer;
    FirebaseAuth mAuth;
    DatabaseReference appointmentsRef;
    String currentTrainerId;
    Switch switchAvailability;
    DatabaseReference trainerRef;

    TextView tvClientsCount, tvHoursWeek, tvSessionsCount, tvRequestBadge;
    LinearLayout upcomingAppointmentsContainer, notificationsContainer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainer_dashboard);


        mAuth = FirebaseAuth.getInstance();
        currentTrainerId = mAuth.getCurrentUser().getUid(); // Logged-in trainer

        appointmentsRef = FirebaseDatabase.getInstance().getReference("Appointments");

        tvClientsCount = findViewById(R.id.tvClientsCount);
        tvSessionsCount = findViewById(R.id.tvSessionsCount);
        tvRequestBadge = findViewById(R.id.tvRequestBadge);
        tvHoursWeek = findViewById(R.id.tvHoursWeek);
        upcomingAppointmentsContainer = findViewById(R.id.upcomingAppointmentsContainer);
        notificationsContainer = findViewById(R.id.notificationsContainer);
        switchAvailability = findViewById(R.id.switchAvailability);
        trainerRef = FirebaseDatabase.getInstance().getReference("Trainers").child(currentTrainerId);

        loadTrainerDashboardStats();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        BottomNavTrainerHelper.setup(this, bottomNavigationView, R.id.nav_trainerdashboard);

        trainerRef.child("isAvailable").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean isAvailable = snapshot.getValue(Boolean.class);
                if (isAvailable != null) {
                    switchAvailability.setChecked(isAvailable);
                } else {
                    // Default to available if not set
                    switchAvailability.setChecked(true);
                    trainerRef.child("isAvailable").setValue(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TrainerDashboardActivity.this, "Failed to load availability", Toast.LENGTH_SHORT).show();
            }
        });
        switchAvailability.setOnCheckedChangeListener((buttonView, isChecked) -> {
            trainerRef.child("isAvailable").setValue(isChecked);
            String message = isChecked ? "You are now available for appointments" : "You are now unavailable";
            Toast.makeText(TrainerDashboardActivity.this, message, Toast.LENGTH_SHORT).show();
        });
        tvRequestBadge.setOnClickListener(v -> {
            Intent intent = new Intent(TrainerDashboardActivity.this, TrainerAppointmentsActivity.class);
            startActivity(intent);
        });
    }

    private void loadTrainerDashboardStats() {
        appointmentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalAppointments = 0;
                int totalClients = 0;
                int totalSessions = 0;
                int hoursThisWeek = 0;
                int totalRatings = 0;
                int countRatings = 0;

                Set<String> uniqueClientIds = new HashSet<>();

                upcomingAppointmentsContainer.removeAllViews();
                notificationsContainer.removeAllViews();

                for (DataSnapshot data : snapshot.getChildren()) {
                    String trainerId = data.child("trainerId").getValue(String.class);
                    if (trainerId != null && trainerId.equals(currentTrainerId)) {
                        totalAppointments++;
                        totalSessions++;

                        // Count unique clients
                        String userId = data.child("userId").getValue(String.class);
                        if (userId != null) uniqueClientIds.add(userId);

                        // Parse timeSlot duration (optional: customize better)
                        String timeSlot = data.child("timeSlot").getValue(String.class);
                        if (timeSlot != null && timeSlot.contains("-")) {
                            hoursThisWeek += 2; // You can refine this later
                        }

                        float appointmentRating = -1; // Default: no rating

                        String ratingStr = data.child("ratings").getValue(String.class);
                        if (ratingStr != null) {
                            try {
                                appointmentRating = Float.parseFloat(ratingStr);
                                totalRatings += appointmentRating;
                                countRatings++;
                            } catch (NumberFormatException e) {
                                // Use default -1 to indicate no rating
                            }
                        }


                        // Upcoming appointment card
                        String date = data.child("date").getValue(String.class);
                        String status = data.child("status").getValue(String.class);
                        if (status == null || status.trim().isEmpty()) {
                            status = "Not Confirmed";
                        }

                        addUpcomingAppointmentView(date, timeSlot, status, appointmentRating);
                        addNotificationView("Upcoming Appointment on " + date + " at " + timeSlot + " [" + status + "]");
                    }
                }

                // Update statistics
                tvClientsCount.setText(String.valueOf(uniqueClientIds.size()));
                tvSessionsCount.setText(String.valueOf(totalSessions));
                tvHoursWeek.setText(String.valueOf(hoursThisWeek));
                tvRequestBadge.setText(String.valueOf(totalAppointments)); // Shows appointment count as badge

                // Display average rating and total ratings on the LinearLayout
                if (countRatings > 0) {
                    double averageRating = (double) totalRatings / countRatings;

                    // Set the rating on the RatingBar
                    RatingBar ratingBar = findViewById(R.id.ratingBarTrainer);
                    ratingBar.setRating((float) averageRating);

                    // Set the total ratings count text
                    TextView tvTotalRatings = findViewById(R.id.tvTotalRatings);
                    tvTotalRatings.setText("(" + countRatings + " ratings)");
                } else {
                    // If no ratings available, set default text
                    RatingBar ratingBar = findViewById(R.id.ratingBarTrainer);
                    ratingBar.setRating(0);

                    TextView tvTotalRatings = findViewById(R.id.tvTotalRatings);
                    tvTotalRatings.setText("(No ratings yet)");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TrainerDashboardActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addUpcomingAppointmentView(String date, String timeSlot, String status, float rating) {
        LinearLayout cardLayout = new LinearLayout(this);
        cardLayout.setOrientation(LinearLayout.VERTICAL);
        cardLayout.setPadding(24, 16, 24, 16);
        cardLayout.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);
        cardLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        ((LinearLayout.LayoutParams) cardLayout.getLayoutParams()).setMargins(16, 0, 16, 0);

        TextView appointmentText = new TextView(this);
        appointmentText.setText(date + "\n" + timeSlot);
        appointmentText.setTextColor(Color.BLACK);
        appointmentText.setTextSize(14);
        appointmentText.setGravity(Gravity.CENTER);
        appointmentText.setPadding(0, 0, 0, 8);
        cardLayout.addView(appointmentText);

        TextView statusText = new TextView(this);
        statusText.setText("Status: " + status);
        statusText.setTextColor(Color.parseColor("#880500"));
        statusText.setTextSize(13);
        cardLayout.addView(statusText);

        TextView ratingText = new TextView(this);
        if (rating >= 0) {
            ratingText.setText("Rating: " + rating + " ★");
            ratingText.setTextColor(Color.parseColor("#FFA000")); // Amber
        } else {
            ratingText.setText("Rating: No rating yet");
            ratingText.setTextColor(Color.GRAY);
        }
        ratingText.setTextSize(13);
        cardLayout.addView(ratingText);

        upcomingAppointmentsContainer.addView(cardLayout);
    }


    private void addNotificationView(String message) {
        TextView notificationView = new TextView(this);
        notificationView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        notificationView.setText("• " + message);
        notificationView.setTextSize(15);
        notificationView.setTextColor(Color.DKGRAY);
        notificationView.setPadding(4, 8, 4, 8);

        notificationsContainer.addView(notificationView);
    }

}
