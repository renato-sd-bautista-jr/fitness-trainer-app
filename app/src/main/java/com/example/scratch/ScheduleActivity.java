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
        showAppointmentsForDate(selectedDate);

        // Handle calendar date selection
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
            tvSelectedDate.setText("Selected Date: " + selectedDate);
            showAppointmentsForDate(selectedDate);
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
            String selectedAppointment = eventList.get(position);
            if (!selectedAppointment.equals("No appointments for this date")) {
                showAppointmentOptions(selectedAppointment);
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
                                        showAppointmentsForDate(selectedDate);
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

    private void showAppointmentsForDate(String selectedDate) {
        appointmentsRef.orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        eventList.clear();
                        for (DataSnapshot appointment : snapshot.getChildren()) {
                            String date = appointment.child("date").getValue(String.class);
                            String timeSlot = appointment.child("timeSlot").getValue(String.class);
                            if (selectedDate.equals(date)) {
                                eventList.add("Appointment at " + timeSlot);
                            }
                        }
                        if (eventList.isEmpty()) {
                            eventList.add("No appointments for this date");
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
