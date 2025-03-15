package com.example.scratch;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private RecyclerView dailyPlannerRecyclerView;
    private DailyPlannerAdapter dailyPlannerAdapter;
    private List<String> dailyPlannerItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_dashboard) {
                return true;
            } else if (itemId == R.id.nav_schedule) {
                startActivity(new Intent(this, ScheduleActivity.class));
                return true;
            } else if (itemId == R.id.nav_workouts) {
                startActivity(new Intent(this, TrainerListActivity.class));
                return true;
            }
            return false;
        });

        ViewPager2 viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);

        ProgressPagerAdapter pagerAdapter = new ProgressPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        String[] tabTitles = {"Weight", "Heart Rate", "Calories", "Workout Duration", "Sleep", "Review"};
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> tab.setText(tabTitles[position])).attach();

        // Daily Planner Section
        dailyPlannerRecyclerView = findViewById(R.id.dailyPlannerRecyclerView);
        dailyPlannerRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        dailyPlannerItems = new ArrayList<>();
        dailyPlannerAdapter = new DailyPlannerAdapter(dailyPlannerItems);
        dailyPlannerRecyclerView.setAdapter(dailyPlannerAdapter);

        // Add new task button
        Button addTaskButton = findViewById(R.id.btnAddTask);
        EditText taskInput = findViewById(R.id.etTaskInput);
        addTaskButton.setOnClickListener(v -> {
            String newTask = taskInput.getText().toString().trim();
            if (!newTask.isEmpty()) {
                dailyPlannerItems.add(newTask);
                dailyPlannerAdapter.notifyItemInserted(dailyPlannerItems.size() - 1);
                taskInput.setText("");
            } else {
                Toast.makeText(this, "Please enter a task", Toast.LENGTH_SHORT).show();
            }
        });
    }
}