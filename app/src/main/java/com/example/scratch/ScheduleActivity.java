package com.example.scratch;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ScheduleActivity extends AppCompatActivity {

    private TextView tvSelectedDate;
    private ListView listViewSchedule;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> eventList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        CalendarView calendarView = findViewById(R.id.calendarView);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        listViewSchedule = findViewById(R.id.listViewSchedule);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Sample event data
        eventList = new ArrayList<>();
        eventList.add("No events for today");

        // Set up ListView
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, eventList);
        listViewSchedule.setAdapter(adapter);

        // Set default date
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        tvSelectedDate.setText("Selected Date: " + currentDate);

        // Change event list on date selection
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String selectedDate = sdf.format(new Date(year - 1900, month, dayOfMonth));
            tvSelectedDate.setText("Selected Date: " + selectedDate);

            // Update schedule (For now, it's just an example)
            eventList.clear();
            eventList.add("Meeting at 10 AM");
            eventList.add("Workout session at 5 PM");
            adapter.notifyDataSetChanged();
        });

        // Set listener for Bottom Navigation
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();


               if (itemId == R.id.nav_dashboard) {
                    startActivity(new Intent(ScheduleActivity.this, DashboardActivity.class));
                    return true;

                   } else if (itemId == R.id.nav_schedule) {
                       return true;
                   } else if (itemId == R.id.nav_workouts) {
                       startActivity(new Intent(ScheduleActivity.this, WorkoutActivity.class));
                       return true;
                   }

                   return false;
            }
        });

        // Set default selected item in Bottom Navigation
        bottomNavigationView.setSelectedItemId(R.id.nav_schedule);
    }
}
