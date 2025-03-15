package com.example.scratch;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ArrayAdapter;
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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
    }

    private void loadAppointments(CalendarView calendarView) {
        appointmentsRef.orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        eventList.clear();
                        for (DataSnapshot appointment : snapshot.getChildren()) {
                            String date = appointment.child("date").getValue(String.class);
                            String time = appointment.child("time").getValue(String.class);
                            if (date != null) {
                                if (selectedDate == null || selectedDate.equals(date)) {
                                    eventList.add("Appointment at " + time);
                                }
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
