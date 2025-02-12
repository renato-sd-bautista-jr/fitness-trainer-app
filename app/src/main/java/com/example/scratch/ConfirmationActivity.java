package com.example.scratch;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ConfirmationActivity extends AppCompatActivity {

    TextView tvTrainerNameConfirm, tvDateConfirm, tvTimeConfirm;
    Button btnConfirmBooking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);

        tvTrainerNameConfirm = findViewById(R.id.tvTrainerNameConfirm);
        tvDateConfirm = findViewById(R.id.tvDateConfirm);
        tvTimeConfirm = findViewById(R.id.tvTimeConfirm);
        btnConfirmBooking = findViewById(R.id.btnConfirmBooking);

        Intent intent = getIntent();
        String trainerName = intent.getStringExtra("TrainerName");
        String date = intent.getStringExtra("SelectedDate");
        String timeSlot = intent.getStringExtra("SelectedTimeSlot");

        tvTrainerNameConfirm.setText("Trainer Name: " + trainerName);
        tvDateConfirm.setText("Date: " + date);
        tvTimeConfirm.setText("Time Slot: " + timeSlot);

        btnConfirmBooking.setOnClickListener(v -> {
            // Add booking confirmation logic here
            finish(); // Close the activity after confirmation
        });
    }
}
