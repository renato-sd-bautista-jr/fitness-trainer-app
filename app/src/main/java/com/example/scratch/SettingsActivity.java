package com.example.scratch;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

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
import java.util.concurrent.TimeUnit;

public class SettingsActivity extends AppCompatActivity {

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


        // Handle calendar date selection
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
            tvSelectedDate.setText("Selected Date: " + selectedDate);
        });



        // Set default selected item (Schedule)
        bottomNavigationView.setSelectedItemId(R.id.nav_schedule);


    }






    private void showRescheduleDialog(String appointmentDetails) {
        // Implement date and time picker dialog here to select new date and time
        Toast.makeText(this, "Reschedule feature not implemented yet", Toast.LENGTH_SHORT).show();
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
