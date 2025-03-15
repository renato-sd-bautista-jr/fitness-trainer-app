package com.example.scratch;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TrainerProfileActivity extends AppCompatActivity {

    TextView trainerName, trainerProficiency, trainerContact, selectedDate;
    Button btnBookNow, btnSetDate;
    ImageButton btnBack;
    Spinner spinnerAvailableTime;
    DatabaseReference appointmentsRef, trainersRef;
    String trainerId, userId = "User123";  // Example userId, replace as needed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainer_profile);

        // Initialize views
        trainerName = findViewById(R.id.tvTrainerName);
        trainerProficiency = findViewById(R.id.tvTrainerProficiency);
        trainerContact = findViewById(R.id.tvTrainerContact);
        selectedDate = findViewById(R.id.tvSelectedDate);
        btnBookNow = findViewById(R.id.btnBookNow);
        btnSetDate = findViewById(R.id.btnSetDate);
        btnBack = findViewById(R.id.btnBack);
        spinnerAvailableTime = findViewById(R.id.spinnerAvailableTime);

        // Firebase reference
        appointmentsRef = FirebaseDatabase.getInstance().getReference("Appointments");
        trainersRef = FirebaseDatabase.getInstance().getReference("Trainers");

// Fetch passed data
        Intent intent = getIntent();
        trainerId = intent.getStringExtra("UserId");  // Use UserId as trainerId

// Check for null trainerId
        if (trainerId == null || trainerId.isEmpty()) {
            Toast.makeText(this, "Error: Trainer ID is missing.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

// Access trainer data with userId
        DatabaseReference trainerRef = trainersRef.child(trainerId);


// Add the code here
        trainerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("fullName").getValue(String.class);
                    String proficiency = snapshot.child("proficiency").getValue(String.class);
                    String contactInfo = snapshot.child("contactInfo").getValue(String.class);

                    // Set data to views
                    trainerName.setText(name != null ? name : "N/A");
                    trainerProficiency.setText("Specialities: " + (proficiency != null ? proficiency : "N/A"));
                    trainerContact.setText("Contact: " + (contactInfo != null ? contactInfo : "N/A"));
                } else {
                    Toast.makeText(TrainerProfileActivity.this, "Trainer not found.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TrainerProfileActivity.this, "Failed to load trainer data.", Toast.LENGTH_SHORT).show();
            }
        });
        btnSetDate.setOnClickListener(v -> {
            trainersRef.child(trainerId).child("availableSchedule").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<String> availableDays = new ArrayList<>();
                    Calendar calendar = Calendar.getInstance();
                    long minDate = Long.MAX_VALUE;

                    for (DataSnapshot daySnapshot : snapshot.getChildren()) {
                        String day = daySnapshot.getKey();
                        availableDays.add(day);
                        Calendar tempCalendar = Calendar.getInstance();
                        while (!getDayOfWeek(tempCalendar.get(Calendar.DAY_OF_WEEK)).equals(day)) {
                            tempCalendar.add(Calendar.DAY_OF_MONTH, 1);
                        }
                        if (tempCalendar.getTimeInMillis() < minDate) {
                            minDate = tempCalendar.getTimeInMillis();
                        }
                    }

                    // Preselect closest available date only once
                    if (selectedDate.getText().toString().equals("Selected Date:")) {
                        calendar.setTimeInMillis(minDate);
                        String closestDate = calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH);
                        selectedDate.setText("Selected Date: " + closestDate);
                        loadAvailableTimes(getDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK)));
                    }

                    DatePickerDialog datePickerDialog = new DatePickerDialog(TrainerProfileActivity.this, (view, year, month, dayOfMonth) -> {
                        Calendar selectedCalendar = Calendar.getInstance();
                        selectedCalendar.set(year, month, dayOfMonth);
                        String date = year + "-" + (month + 1) + "-" + dayOfMonth;

                        if (availableDays.contains(getDayOfWeek(selectedCalendar.get(Calendar.DAY_OF_WEEK)))) {
                            selectedDate.setText("Selected Date: " + date);
                            loadAvailableTimes(getDayOfWeek(selectedCalendar.get(Calendar.DAY_OF_WEEK)));
                        } else {
                            Toast.makeText(TrainerProfileActivity.this, "Selected date is unavailable", Toast.LENGTH_SHORT).show();
                        }
                    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

                    // Disable past dates and only enable available dates
                    datePickerDialog.getDatePicker().setMinDate(minDate);

                    datePickerDialog.show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(TrainerProfileActivity.this, "Failed to load available dates", Toast.LENGTH_SHORT).show();
                }
            });
        });





        // Set current date
        Calendar calendar = Calendar.getInstance();
        String currentDate = calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH);
        selectedDate.setText("Selected Date: " + currentDate);

        // Load times for the current day of the week
        loadAvailableTimes(getDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK)));



        // Book Now Button Click
        btnBookNow.setOnClickListener(v -> bookAppointment());
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadAvailableTimes(String dayOfWeek) {
        if (trainerId == null) {
            Toast.makeText(this, "Error: Trainer ID is missing.", Toast.LENGTH_SHORT).show();
            return;
        }

        trainersRef.child(trainerId).child("availableSchedule").child(dayOfWeek)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<String> timeSlots = new ArrayList<>();
                        for (DataSnapshot slotSnapshot : snapshot.getChildren()) {
                            String time = slotSnapshot.getValue(String.class);
                            if (time != null) {
                                timeSlots.add(time);
                            }
                        }
                        if (timeSlots.isEmpty()) {
                            timeSlots.add("No available slots");
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(TrainerProfileActivity.this, android.R.layout.simple_spinner_item, timeSlots);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerAvailableTime.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(TrainerProfileActivity.this, "Failed to load available times", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private String getDayOfWeek(int day) {
        String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        return days[day - 1];
    }

    private void bookAppointment() {
        String date = selectedDate.getText().toString().replace("Selected Date: ", "");
        if (spinnerAvailableTime.getSelectedItem() == null || "No available slots".equals(spinnerAvailableTime.getSelectedItem().toString())) {
            Toast.makeText(this, "No available time slot selected", Toast.LENGTH_SHORT).show();
            return;
        }
        String timeSlot = spinnerAvailableTime.getSelectedItem().toString();

        // Get the current user ID from FirebaseAuth
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Check for duplicate appointment
        appointmentsRef.orderByChild("trainerId").equalTo(trainerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean duplicate = false;
                for (DataSnapshot appointment : snapshot.getChildren()) {
                    String existingDate = appointment.child("date").getValue(String.class);
                    String existingTimeSlot = appointment.child("timeSlot").getValue(String.class);
                    if (date.equals(existingDate) && timeSlot.equals(existingTimeSlot)) {
                        duplicate = true;
                        break;
                    }
                }

                if (duplicate) {
                    Toast.makeText(TrainerProfileActivity.this, "Appointment already exists for this slot!", Toast.LENGTH_SHORT).show();
                } else {
                    // Book appointment
                    String appointmentId = UUID.randomUUID().toString();
                    Map<String, Object> appointment = new HashMap<>();
                    appointment.put("trainerId", trainerId);
                    appointment.put("userId", userId); // Correctly setting userId here
                    appointment.put("date", date);
                    appointment.put("timeSlot", timeSlot);

                    appointmentsRef.child(appointmentId).setValue(appointment)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(TrainerProfileActivity.this, "Appointment booked!", Toast.LENGTH_SHORT).show();
                                // Remove booked slot from spinner
                                ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerAvailableTime.getAdapter();
                                adapter.remove(timeSlot);
                                adapter.notifyDataSetChanged();
                            })
                            .addOnFailureListener(e -> Toast.makeText(TrainerProfileActivity.this, "Failed to book appointment.", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TrainerProfileActivity.this, "Failed to check appointments", Toast.LENGTH_SHORT).show();
            }
        });
    }


}
