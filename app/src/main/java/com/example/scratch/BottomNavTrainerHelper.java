package com.example.scratch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BottomNavTrainerHelper {

    public static void setup(final Activity currentActivity, BottomNavigationView bottomNavigationView, int currentItemId) {
        bottomNavigationView.setSelectedItemId(currentItemId);

        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                Context context = currentActivity.getApplicationContext();

                if (itemId == R.id.nav_appointments) {
                    if (currentItemId != R.id.nav_appointments) {
                        currentActivity.startActivity(new Intent(currentActivity, TrainerDashboardActivity.class));
                    } else {
                        Toast.makeText(context, "Already on Appointments", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    if (currentItemId != R.id.nav_profile) {
                        currentActivity.startActivity(new Intent(currentActivity, TrainerProfileInfoActivity.class));
                    } else {
                        Toast.makeText(context, "Already on Profile", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }

                return false;
            }
        });
    }
}
