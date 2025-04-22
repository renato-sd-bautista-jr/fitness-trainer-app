package com.example.scratch;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class LauncherActivity extends AppCompatActivity {

    private static final int SPLASH_TIME_OUT = 1000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launcher_activity);  // Ensure launcher_activity.xml exists

        // Simulate loading or splash time
        new Handler().postDelayed(this::navigateNext, SPLASH_TIME_OUT);
    }

    private void navigateNext() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Fetch user details from "Users" node
            FirebaseDatabase.getInstance().getReference("Users")
                    .child(userId)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.exists()) {
                            String firstName = snapshot.child("firstName").getValue(String.class);

                            // Show toast with first name
                            Toast.makeText(LauncherActivity.this, "Welcome " + firstName, Toast.LENGTH_SHORT).show();

                            // Pass userId and firstName to Dashboard
                            Intent intent = new Intent(LauncherActivity.this, DashboardActivity.class);
                            intent.putExtra("userId", userId);
                            intent.putExtra("firstName", firstName);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(LauncherActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                            // Still go to dashboard
                            startActivity(new Intent(LauncherActivity.this, DashboardActivity.class));
                            finish();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(LauncherActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LauncherActivity.this, DashboardActivity.class));
                        finish();
                    });

        } else {
            // No user logged in
            startActivity(new Intent(LauncherActivity.this, LoginActivity.class));
            finish();
        }
    }
}

