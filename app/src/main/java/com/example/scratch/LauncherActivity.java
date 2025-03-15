package com.example.scratch;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LauncherActivity extends AppCompatActivity {

    private static final int SPLASH_TIME_OUT = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launcher_activity);  // Make sure launcher_activity.xml exists

        // Simulate loading or splash time
        new Handler().postDelayed(this::navigateNext, SPLASH_TIME_OUT);
    }

    private void navigateNext() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Intent intent;

        if (currentUser != null) {
            // User is logged in, go to Dashboard
            intent = new Intent(LauncherActivity.this, DashboardActivity.class);
        } else {
            // No user logged in, go to Login
            intent = new Intent(LauncherActivity.this, LoginActivity.class);
        }

        startActivity(intent);
        finish();  // Close LauncherActivity
    }
}
