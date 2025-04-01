package com.example.scratch;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import java.util.concurrent.TimeUnit;

public class ScheduleActivity extends AppCompatActivity {

    private TextView tvSelectedDate;
    private ListView listViewSchedule;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> eventList;
    private DatabaseReference appointmentsRef;
    private FirebaseAuth mAuth;
    private String userId;
    private String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        CalendarView calendarView = findViewById(R.id.calendarView);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        listViewSchedule = findViewById(R.id.listViewSchedule);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        // Inside onCreate()
        NotificationHelper.createNotificationChannel(this);

        PeriodicWorkRequest scheduleCheckRequest =
                new PeriodicWorkRequest.Builder(ScheduleWorker.class, 15, TimeUnit.MINUTES)
                        .build();

        WorkManager.getInstance(this).enqueue(scheduleCheckRequest);
        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        appointmentsRef = FirebaseDatabase.getInstance().getReference("Appointments");

        // Set up ListView
        eventList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, eventList);
        listViewSchedule.setAdapter(adapter);

        // Load Appointments for today by default
        selectedDate = new SimpleDateFormat("yyyy-M-d", Locale.getDefault()).format(new Date());
        tvSelectedDate.setText("Selected Date: " + selectedDate);
        loadAppointments();

        // Handle calendar date selection
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
            tvSelectedDate.setText("Selected Date: " + selectedDate);
        });

        // Bottom Navigation setup
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

        // Set default selected item (Schedule)
        bottomNavigationView.setSelectedItemId(R.id.nav_schedule);

        // Handle appointment clicks
        listViewSchedule.setOnItemClickListener((parent, view, position, id) -> {
            String selectedItem = eventList.get(position);

            if (!selectedItem.startsWith("📅") && !selectedItem.equals("No appointments found.")) {
                showAppointmentOptions(selectedItem);
            }
        });
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
        // Implement date and time picker dialog here to select new date and time
        Toast.makeText(this, "Reschedule feature not implemented yet", Toast.LENGTH_SHORT).show();
    }

    private void loadAppointments() {
        appointmentsRef.orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Map<String, ArrayList<String>> scheduleMap = new HashMap<>();
                        eventList.clear();

                        Calendar currentTime = Calendar.getInstance();

                        for (DataSnapshot appointment : snapshot.getChildren()) {
                            String date = appointment.child("date").getValue(String.class);
                            String timeSlot = appointment.child("timeSlot").getValue(String.class);

                            if (date != null && timeSlot != null) {
                                scheduleMap.putIfAbsent(date, new ArrayList<>());
                                scheduleMap.get(date).add("• " + timeSlot);

                                // Check for today's appointments
                                String today = new SimpleDateFormat("yyyy-M-d", Locale.getDefault()).format(new Date());
                                if (today.equals(date)) {
                                    sendNotificationIfUpcoming(timeSlot);
                                }
                            }
                        }

                        if (scheduleMap.isEmpty()) {
                            eventList.add("No appointments found.");
                        } else {
                            for (Map.Entry<String, ArrayList<String>> entry : scheduleMap.entrySet()) {
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

    // Check if appointment is within the next hour
    private void sendNotificationIfUpcoming(String timeSlot) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        try {
            Date appointmentTime = timeFormat.parse(timeSlot);
            Calendar appointmentCal = Calendar.getInstance();
            appointmentCal.setTime(appointmentTime);

            Calendar now = Calendar.getInstance();
            if (appointmentCal.get(Calendar.HOUR_OF_DAY) == now.get(Calendar.HOUR_OF_DAY) + 1) {
                NotificationHelper.sendNotification(this, "Upcoming Appointment", "You have an appointment at " + timeSlot + ". Get ready!");

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
