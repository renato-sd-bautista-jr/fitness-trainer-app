package com.example.scratch;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.example.scratch.R;
public class DashboardActivity extends AppCompatActivity {

    private DatabaseReference db;
    private FirebaseAuth auth;
    private LinearLayout cardContainer, goalContainer;
    private ImageButton btnToggleStats, btnToggleGoals;
    private boolean isStatsVisible = true, isGoalsVisible = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        db = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();

        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "User";

        // Set greeting
        TextView tvGreeting = findViewById(R.id.greetingTextView);
        //tvGreeting.setText("Hello, " + userId + "!");

        // Initialize Views
        cardContainer = findViewById(R.id.cardContainer);
        goalContainer = findViewById(R.id.goalContainer);
        btnToggleStats = findViewById(R.id.btnToggleStats);
        btnToggleGoals = findViewById(R.id.btnToggleGoals);

        ImageButton btnSettings = findViewById(R.id.btnSettings);
        btnSettings.setVisibility(View.VISIBLE);
        // Toggle Stats Section
        btnToggleStats.setOnClickListener(v -> {
            isStatsVisible = !isStatsVisible;
            cardContainer.setVisibility(isStatsVisible ? View.VISIBLE : View.GONE);
            btnToggleStats.setRotation(isStatsVisible ? 0 : 180); // Rotate button for effect
        });

        // Toggle Goals Section
        btnToggleGoals.setOnClickListener(v -> {
            isGoalsVisible = !isGoalsVisible;
            goalContainer.setVisibility(isGoalsVisible ? View.VISIBLE : View.GONE);
            btnToggleGoals.setRotation(isGoalsVisible ? 0 : 180);
        });

        // Create Goal Button
        Button btnCreateGoal = findViewById(R.id.btnCreateGoal);
        if (btnCreateGoal != null) {
            btnCreateGoal.setOnClickListener(v -> {
                Intent intent = new Intent(DashboardActivity.this, CreateGoalActivity.class);
                startActivity(intent);
            });
        }

        // Load stats and goals
        loadUserStats(userId);
        loadUserGoals(userId);

        // Ensure bottom nav is fixed
        setupBottomNavigation();
        // Set the click listener
        btnSettings.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(DashboardActivity.this, btnSettings);
            popupMenu.getMenuInflater().inflate(R.menu.menu_settings, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                Intent intent = null; // Declare intent variable here

                if (item.getItemId() == R.id.menu_profile) {
                    intent = new Intent(DashboardActivity.this, UserProfileActivity.class);
                } else if (item.getItemId() == R.id.menu_settings) {
                    intent = new Intent(DashboardActivity.this, SettingsActivity.class);
                } else if (item.getItemId() == R.id.menu_register_trainer) {
                    intent = new Intent(DashboardActivity.this, RegisterTrainerActivity.class);
                } else if (item.getItemId() == R.id.menu_logout) {
                    FirebaseAuth.getInstance().signOut();
                    intent = new Intent(DashboardActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                } else {
                    return false;
                }

                if (intent != null) {
                    startActivity(intent);
                    return true;
                }
                return false;
            });

            popupMenu.show();
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_profile) {
            startActivity(new Intent(this, UserProfileActivity.class));
            return true;
        } else if (id == R.id.menu_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.menu_register_trainer) {
            startActivity(new Intent(this, RegisterTrainerActivity.class));
            return true;
        } else if (id == R.id.menu_logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivity.class); // Or your actual login activity
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        if (bottomNavigationView != null) {
            bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int itemId = item.getItemId();
                    if (itemId == R.id.nav_dashboard) {
                        Toast.makeText(DashboardActivity.this, "Already on Dashboard", Toast.LENGTH_SHORT).show();
                        return true;
                    } else if (itemId == R.id.nav_schedule) {
                        startActivity(new Intent(DashboardActivity.this, ScheduleActivity.class));
                        return true;
                    } else if (itemId == R.id.nav_workouts) {
                        startActivity(new Intent(DashboardActivity.this, TrainerListActivity.class));
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    private void loadUserGoals(String userId) {
        DatabaseReference goalsRef = db.child("Goals").child(userId);
        goalsRef.get().addOnSuccessListener(dataSnapshot -> {
            goalContainer.removeAllViews();
            for (DataSnapshot goalSnapshot : dataSnapshot.getChildren()) {
                String goal = goalSnapshot.getKey();
                String value = goalSnapshot.getValue(String.class);
                addEditableStatCard(goalsRef, goalContainer, goal, value != null ? value : "0");
            }
        }).addOnFailureListener(e -> Toast.makeText(DashboardActivity.this, "Failed to load goals", Toast.LENGTH_SHORT).show());
    }

    private void loadUserStats(String userId) {
        DatabaseReference statsRef = db.child("Stats").child(userId);
        cardContainer.removeAllViews();

        // Load user stats
        statsRef.get().addOnSuccessListener(dataSnapshot -> {
            String[] stats = {"Calories", "Weight", "Heart Rate", "Workout Duration", "Sleep"};
            for (String stat : stats) {
                String value = dataSnapshot.child(stat).getValue(String.class);
                addEditableStatCard(statsRef, cardContainer, stat, value != null ? value : "0");
            }
        }).addOnFailureListener(e ->
                Toast.makeText(DashboardActivity.this, "Failed to load stats", Toast.LENGTH_SHORT).show()
        );
    }

    private void addEditableStatCard(DatabaseReference ref, LinearLayout container, String title, String value) {
        StatCardView statCard = new StatCardView(this);
        statCard.setTitle(title);
        statCard.setValue(value);

        statCard.setOnValueChangeListener(newValue -> {
            ref.child(title).setValue(newValue).addOnFailureListener(e ->
                    Toast.makeText(DashboardActivity.this, "Failed to update " + title, Toast.LENGTH_SHORT).show()
            );
        });

        container.addView(statCard);
    }
}
