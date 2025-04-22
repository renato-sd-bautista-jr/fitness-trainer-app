package com.example.scratch;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

public class ScheduleActivity extends AppCompatActivity {

    private TextView tvSelectedDate;
    private ListView listViewSchedule;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> eventList;
    private DatabaseReference appointmentsRef;
    private FirebaseAuth mAuth;
    private String userId;
    private Calendar selectedCalendar;
    private String filterStartDate = null;
    private String filterTimeSlot = null;
    private RadioGroup radioGroupFilter;
    private RadioButton radioUpcoming, radioCompleted;
    DatabaseReference trainersRef = FirebaseDatabase.getInstance().getReference("Trainers");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        listViewSchedule = findViewById(R.id.listViewSchedule);
        Button btnPickDateRange = findViewById(R.id.btnPickDateRange);
        Button btnClearFilter = findViewById(R.id.btnClearFilter);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        radioGroupFilter = findViewById(R.id.radioGroupFilter);
        radioUpcoming = findViewById(R.id.radioUpcoming);
        radioCompleted = findViewById(R.id.radioCompleted);


        NotificationHelper.createNotificationChannel(this);

        PeriodicWorkRequest scheduleCheckRequest =
                new PeriodicWorkRequest.Builder(ScheduleWorker.class, 15, TimeUnit.MINUTES)
                        .build();
        WorkManager.getInstance(this).enqueue(scheduleCheckRequest);

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        appointmentsRef = FirebaseDatabase.getInstance().getReference("Appointments");

        eventList = new ArrayList<>();
        adapter = new ScheduleAdapter(this, eventList);
        listViewSchedule.setAdapter(adapter);

        // ✅ Optional fix: store and reuse selected date
        selectedCalendar = Calendar.getInstance(); // Start with the current date
        updateSelectedDateText(); // Update the displayed selected date
        loadAppointments(selectedCalendar,3); // ✅ Load appointments starting from today
        radioGroupFilter.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioUpcoming) {
                loadAppointments(selectedCalendar, 3); // Load upcoming
            } else if (checkedId == R.id.radioCompleted) {
                loadCompletedAppointments(); // Load completed
            }
        });
        // Bottom nav
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_dashboard) {
                startActivity(new Intent(ScheduleActivity.this, DashboardActivity.class));
                return true;
            } else if (itemId == R.id.nav_schedule) {
                return true;
            } else if (itemId == R.id.nav_workouts) {
                startActivity(new Intent(ScheduleActivity.this, TrainerListActivity.class));
                return true;
            }
            return false;
        });
        bottomNavigationView.setSelectedItemId(R.id.nav_schedule);

        listViewSchedule.setOnItemClickListener((parent, view, position, id) -> {
            String selectedItem = eventList.get(position);
            if (!selectedItem.startsWith("📅") && !selectedItem.equals("No appointments found.")) {
                String appointmentId = extractAppointmentId(selectedItem);
                if (appointmentId != null) {
                    appointmentsRef.child(appointmentId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String status = snapshot.child("status").getValue(String.class);
                            if ("Completed".equalsIgnoreCase(status)) {
                                showRatingDialog(appointmentId);
                            } else {
                                showAppointmentOptions(appointmentId, selectedItem);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(ScheduleActivity.this, "Failed to check appointment status", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });



        // 🗓 Pick Date Range (Proximity)
        btnPickDateRange.setOnClickListener(v -> {
            // Create a dialog to pick the number of weeks (1, 2, 3, etc.)
            String[] weekOptions = {"1 Week", "2 Weeks", "3 Weeks"};
            new AlertDialog.Builder(ScheduleActivity.this)
                    .setTitle("Select Time Range")
                    .setItems(weekOptions, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int weeksAhead = which + 1; // Convert position to weeks (1, 2, 3)
                            selectedCalendar = Calendar.getInstance(); // Start with the current date
                            filterStartDate = weeksAhead + " Week(s)";
                            updateSelectedDateText(); // Update the displayed selected date
                            loadAppointments(selectedCalendar, weeksAhead); // Load appointments for the selected range
                        }
                    })
                    .show();
        });

        btnClearFilter.setOnClickListener(v -> {
            filterStartDate = null;
            filterTimeSlot = null;
            selectedCalendar = Calendar.getInstance(); // Reset to today
            updateSelectedDateText(); // Update the displayed selected date
            loadAppointments(selectedCalendar, 3); // Load appointments for the default 1-week range
        });
    }
    private void showRatingDialog(String appointmentId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rate Trainer");

        String[] ratingOptions = {"⭐ 1", "⭐⭐ 2", "⭐⭐⭐ 3", "⭐⭐⭐⭐ 4", "⭐⭐⭐⭐⭐ 5"};

        builder.setItems(ratingOptions, (dialog, which) -> {
            int rating = which + 1;
            appointmentsRef.child(appointmentId).child("rating").setValue(rating)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Thanks for rating!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Failed to submit rating", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        builder.setNegativeButton("Close", null);
        builder.show();
    }


    private String extractAppointmentId(String selectedItem) {
        // Assuming the appointment ID is included as part of the string
        // e.g., "• 8 AM - 10 AM (Completed) [ID: appointmentId]"
        String idPrefix = "[ID:";
        int startIndex = selectedItem.indexOf(idPrefix);
        if (startIndex != -1) {
            int endIndex = selectedItem.indexOf("]", startIndex);
            if (endIndex != -1) {
                return selectedItem.substring(startIndex + idPrefix.length(), endIndex);
            }
        }
        return null;
    }
    private void loadCompletedAppointments() {
        appointmentsRef.orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        eventList.clear();
                        SimpleDateFormat sdfInput = new SimpleDateFormat("yyyy-M-d", Locale.getDefault());
                        SimpleDateFormat sdfOutput = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());

                        Date now = new Date();

                        Map<String, ArrayList<String>> completedMap = new HashMap<>();

                        // Use a holder object to accumulate the rating and number of ratings
                        final int[] totalRating = {0};
                        final int[] numberOfRatings = {0};

                        // Store the trainer names for quick access
                        Map<String, String> trainerNameMap = new HashMap<>();

                        // Fetch trainer data from Firebase (to get trainer's name)
                        trainersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot trainerSnapshot) {
                                // Populate the trainerNameMap with trainerId -> fullName
                                for (DataSnapshot trainer : trainerSnapshot.getChildren()) {
                                    String trainerId = trainer.getKey();
                                    String fullName = trainer.child("fullName").getValue(String.class);
                                    if (trainerId != null && fullName != null) {
                                        trainerNameMap.put(trainerId, fullName);
                                    }
                                }

                                // Now process the appointments
                                for (DataSnapshot appointment : snapshot.getChildren()) {
                                    String dateStr = appointment.child("date").getValue(String.class);
                                    String timeSlot = appointment.child("timeSlot").getValue(String.class);
                                    String status = appointment.child("status").getValue(String.class);
                                    Long rating = appointment.child("rating").getValue(Long.class);
                                    String trainerId = appointment.child("trainerId").getValue(String.class);

                                    if (dateStr != null && timeSlot != null && status != null) {
                                        try {
                                            Date appointmentDate = sdfInput.parse(dateStr);
                                            if (appointmentDate != null && appointmentDate.before(now) && status.equalsIgnoreCase("Completed")) {
                                                String formattedDate = sdfOutput.format(appointmentDate);

                                                // Get the trainer's name from the map
                                                String trainerName = trainerNameMap.getOrDefault(trainerId, "Unknown Trainer");

                                                // Add the appointment info along with the trainer's name
                                                completedMap.putIfAbsent(formattedDate, new ArrayList<>());
                                                String appointmentId = appointment.getKey();
                                                completedMap.get(formattedDate).add("• " + timeSlot + " with " + trainerName + " (Completed) [ID:" + appointmentId + "]");

                                                // Calculate the total rating
                                                if (rating != null && rating > 0) {
                                                    totalRating[0] += rating;
                                                    numberOfRatings[0]++;
                                                }
                                            }
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }

                                // After processing, update the UI
                                if (completedMap.isEmpty()) {
                                    eventList.add("No completed appointments found.");
                                } else {
                                    TreeMap<String, ArrayList<String>> sortedMap = new TreeMap<>(completedMap);
                                    for (Map.Entry<String, ArrayList<String>> entry : sortedMap.entrySet()) {
                                        eventList.add("📅 " + entry.getKey());
                                        eventList.addAll(entry.getValue());
                                    }
                                }

                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(ScheduleActivity.this, "Failed to load trainer data", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ScheduleActivity.this, "Failed to load completed appointments", Toast.LENGTH_SHORT).show();
                    }
                });
    }




    private void updateSelectedDateText() {
        String text = "Selected Range: ";
        if (filterStartDate != null) {
            text += filterStartDate;
        } else {
            text += "All upcoming appointments";
        }
        tvSelectedDate.setText(text);
    }

    private void showAppointmentOptions(String appointmentId, String appointmentDetails) {
        new AlertDialog.Builder(this)
                .setTitle("Appointment Options")
                .setMessage(appointmentDetails)
                .setPositiveButton("Cancel Appointment", (dialog, which) -> confirmCancellation(appointmentId, appointmentDetails))
                .setNegativeButton("Close", null)
                .show();
    }


    private void confirmCancellation(String appointmentId, String appointmentDetails) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Cancellation")
                .setMessage("Are you sure you want to cancel this appointment?\n\n" + appointmentDetails)
                .setPositiveButton("Yes", (dialog, which) -> {
                    appointmentsRef.child(appointmentId).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(ScheduleActivity.this, "Appointment cancelled", Toast.LENGTH_SHORT).show();
                                loadAppointments(Calendar.getInstance(), 1); // refresh the list
                            })
                            .addOnFailureListener(e -> Toast.makeText(ScheduleActivity.this, "Failed to cancel", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void cancelAppointmentIfPending(String appointmentDetails) {
        appointmentsRef.orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot appointment : snapshot.getChildren()) {
                            String timeSlot = appointment.child("timeSlot").getValue(String.class);
                            String status = appointment.child("status").getValue(String.class);

                            if (appointmentDetails.contains(timeSlot)) {
                                if ("To be confirmed".equals(status)) {
                                    appointment.getRef().removeValue().addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(ScheduleActivity.this, "Appointment cancelled", Toast.LENGTH_SHORT).show();
                                            loadAppointments(selectedCalendar, 1);
                                        } else {
                                            Toast.makeText(ScheduleActivity.this, "Failed to cancel", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    Toast.makeText(ScheduleActivity.this, "Only pending appointments can be cancelled", Toast.LENGTH_SHORT).show();
                                }
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ScheduleActivity.this, "Error checking appointment status", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void showRescheduleDialog(String appointmentDetails) {
        Toast.makeText(this, "Reschedule feature not implemented yet", Toast.LENGTH_SHORT).show();
    }

    private void loadAppointments(Calendar selectedDate, int weeksAhead) {
        Calendar endDate = (Calendar) selectedDate.clone();
        endDate.add(Calendar.DAY_OF_YEAR, weeksAhead * 7);

        appointmentsRef.orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Map<String, ArrayList<String>> scheduleMap = new HashMap<>();
                        eventList.clear();

                        SimpleDateFormat sdfInput = new SimpleDateFormat("yyyy-M-d", Locale.getDefault());
                        SimpleDateFormat sdfOutput = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());

                        List<DataSnapshot> appointmentsList = new ArrayList<>();
                        Set<String> trainerIds = new HashSet<>();

                        for (DataSnapshot appointment : snapshot.getChildren()) {
                            String trainerId = appointment.child("trainerId").getValue(String.class);
                            if (trainerId != null) {
                                trainerIds.add(trainerId);
                                appointmentsList.add(appointment);
                            }
                        }

                        // Now fetch all trainers in one call
                        trainersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot trainerSnapshot) {
                                Map<String, String> trainerNameMap = new HashMap<>();
                                for (DataSnapshot trainer : trainerSnapshot.getChildren()) {
                                    String id = trainer.getKey();
                                    String fullName = trainer.child("fullName").getValue(String.class);
                                    if (id != null && fullName != null) {
                                        trainerNameMap.put(id, fullName);
                                    }
                                }

                                for (DataSnapshot appointment : appointmentsList) {
                                    String dateStr = appointment.child("date").getValue(String.class);
                                    String timeSlot = appointment.child("timeSlot").getValue(String.class);
                                    String status = appointment.child("status").getValue(String.class);
                                    String trainerId = appointment.child("trainerId").getValue(String.class);
                                    String appointmentId = appointment.getKey();

                                    if (dateStr != null && timeSlot != null && status != null && trainerId != null) {
                                        try {
                                            Date appointmentDate = sdfInput.parse(dateStr);
                                            if (appointmentDate != null &&
                                                    !appointmentDate.before(selectedDate.getTime()) &&
                                                    !appointmentDate.after(endDate.getTime())) {

                                                String formattedDate = sdfOutput.format(appointmentDate);
                                                String trainerName = trainerNameMap.getOrDefault(trainerId, "Unknown Trainer");
                                                String displayText = "• " + timeSlot + " with " + trainerName + " (" + status + ") [ID:" + appointmentId + "]";

                                                scheduleMap.putIfAbsent(formattedDate, new ArrayList<>());
                                                scheduleMap.get(formattedDate).add(displayText);
                                            }
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }

                                if (scheduleMap.isEmpty()) {
                                    eventList.add("No appointments found for selected range.");
                                    adapter.notifyDataSetChanged();
                                } else {
                                    refreshScheduleList(scheduleMap); // ✅ Now using the reusable method
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(ScheduleActivity.this, "Failed to load trainers", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ScheduleActivity.this, "Failed to load appointments", Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private void refreshScheduleList(Map<String, ArrayList<String>> scheduleMap) {
        eventList.clear();
        TreeMap<String, ArrayList<String>> sortedMap = new TreeMap<>(scheduleMap);
        for (Map.Entry<String, ArrayList<String>> entry : sortedMap.entrySet()) {
            eventList.add("📅 " + entry.getKey());
            eventList.addAll(entry.getValue());
        }
        adapter.notifyDataSetChanged();
    }

}
