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
import java.util.Locale;
import java.util.Map;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        listViewSchedule = findViewById(R.id.listViewSchedule);
        Button btnPickDateRange = findViewById(R.id.btnPickDateRange);
        Button btnClearFilter = findViewById(R.id.btnClearFilter);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        NotificationHelper.createNotificationChannel(this);

        PeriodicWorkRequest scheduleCheckRequest =
                new PeriodicWorkRequest.Builder(ScheduleWorker.class, 15, TimeUnit.MINUTES)
                        .build();
        WorkManager.getInstance(this).enqueue(scheduleCheckRequest);

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        appointmentsRef = FirebaseDatabase.getInstance().getReference("Appointments");

        eventList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, eventList);
        listViewSchedule.setAdapter(adapter);

        // ✅ Optional fix: store and reuse selected date
        selectedCalendar = Calendar.getInstance(); // Start with the current date
        updateSelectedDateText(); // Update the displayed selected date
        loadAppointments(selectedCalendar,3); // ✅ Load appointments starting from today

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
                showAppointmentOptions(selectedItem);
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

    private void updateSelectedDateText() {
        String text = "Selected Range: ";
        if (filterStartDate != null) {
            text += filterStartDate;
        } else {
            text += "All upcoming appointments";
        }
        tvSelectedDate.setText(text);
    }

    private void showAppointmentOptions(String appointmentDetails) {
        new AlertDialog.Builder(this)
                .setTitle("Appointment Options")
                .setMessage(appointmentDetails)
                .setPositiveButton("Reschedule", (dialog, which) -> showRescheduleDialog(appointmentDetails))
                .setNegativeButton("Cancel Appointment", (dialog, which) -> confirmCancellation(appointmentDetails))
                .setNeutralButton("Close", null)
                .show();
    }

    private void confirmCancellation(String appointmentDetails) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Cancellation")
                .setMessage("Are you sure you want to cancel this appointment? This may incur a fee.")
                .setPositiveButton("Yes", (dialog, which) -> cancelAppointment(appointmentDetails))
                .setNegativeButton("No", null)
                .show();
    }

    private void cancelAppointment(String appointmentDetails) {
        appointmentsRef.orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot appointment : snapshot.getChildren()) {
                            String timeSlot = appointment.child("timeSlot").getValue(String.class);
                            if (appointmentDetails.contains(timeSlot)) {
                                appointment.getRef().removeValue().addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ScheduleActivity.this, "Appointment cancelled", Toast.LENGTH_SHORT).show();
                                        loadAppointments(selectedCalendar, 1); // Load appointments for the 1-week range
                                    } else {
                                        Toast.makeText(ScheduleActivity.this, "Failed to cancel appointment", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ScheduleActivity.this, "Failed to access appointments", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showRescheduleDialog(String appointmentDetails) {
        Toast.makeText(this, "Reschedule feature not implemented yet", Toast.LENGTH_SHORT).show();
    }

    private void loadAppointments(Calendar selectedDate, int weeksAhead) {
        Calendar endDate = (Calendar) selectedDate.clone();
        endDate.add(Calendar.DAY_OF_YEAR, weeksAhead * 7); // Get the next weeksAhead weeks

        appointmentsRef.orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Map<String, ArrayList<String>> scheduleMap = new HashMap<>();
                        eventList.clear();

                        SimpleDateFormat sdfInput = new SimpleDateFormat("yyyy-M-d", Locale.getDefault()); // Date format from Firebase
                        SimpleDateFormat sdfOutput = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()); // Output format (e.g., April 4, 2025)

                        for (DataSnapshot appointment : snapshot.getChildren()) {
                            String dateStr = appointment.child("date").getValue(String.class);
                            String timeSlot = appointment.child("timeSlot").getValue(String.class);

                            if (dateStr != null && timeSlot != null) {
                                try {
                                    // Parse the date from Firebase (keep in yyyy-M-d format for comparison)
                                    Date appointmentDate = sdfInput.parse(dateStr);
                                    if (appointmentDate != null &&
                                            !appointmentDate.before(selectedDate.getTime()) &&
                                            !appointmentDate.after(endDate.getTime())) {

                                        // Format the date for display
                                        String formattedDate = sdfOutput.format(appointmentDate);

                                        // Add the time slot under the formatted date
                                        scheduleMap.putIfAbsent(formattedDate, new ArrayList<>());
                                        scheduleMap.get(formattedDate).add("• " + timeSlot);
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        if (scheduleMap.isEmpty()) {
                            eventList.add("No appointments found for selected range.");
                        } else {
                            TreeMap<String, ArrayList<String>> sortedMap = new TreeMap<>(scheduleMap);
                            for (Map.Entry<String, ArrayList<String>> entry : sortedMap.entrySet()) {
                                eventList.add("📅 " + entry.getKey());
                                eventList.addAll(entry.getValue());
                            }
                        }

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ScheduleActivity.this, "Failed to load appointments", Toast.LENGTH_SHORT).show();
                    }
                });

    }
}
