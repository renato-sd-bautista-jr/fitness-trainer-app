package com.example.scratch;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;

public class TrainerActivity extends AppCompatActivity {

    private TrainerAdapter trainerAdapter;
    private List<TrainerModel> trainerList, filteredList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainer);

        RecyclerView recyclerViewTrainers = findViewById(R.id.recyclerViewTrainers);
        TextInputEditText etSearchTrainer = findViewById(R.id.etSearchTrainer);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Sample trainer data
        trainerList = new ArrayList<>();
        trainerList.add(new TrainerModel("John Doe", "Strength Coach", R.drawable.ic_home));
        trainerList.add(new TrainerModel("Jane Smith", "Yoga Instructor", R.drawable.ic_home));
        trainerList.add(new TrainerModel("Mark Johnson", "Cardio Specialist", R.drawable.ic_home));

        // Set up RecyclerView
        filteredList = new ArrayList<>(trainerList);
        trainerAdapter = new TrainerAdapter(filteredList);
        recyclerViewTrainers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTrainers.setAdapter(trainerAdapter);

        // Search functionality
        etSearchTrainer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filterTrainers(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        // Bottom Navigation Click Handling
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_dashboard) {
                    startActivity(new Intent(TrainerActivity.this, DashboardActivity.class));
                    return true;

                    } else if (itemId == R.id.nav_schedule) {
                        startActivity(new Intent(TrainerActivity.this, ScheduleActivity.class));
                        return true;

                } else if (itemId == R.id.nav_trainer) {
                    return true; // Already in Trainer Activity
                }

                return false;
            }
        });

        // Set default selected item
        bottomNavigationView.setSelectedItemId(R.id.nav_trainer);
    }

    private void filterTrainers(String query) {
        filteredList.clear();
        for (TrainerModel trainer : trainerList) {
            if (trainer.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(trainer);
            }
        }
        trainerAdapter.notifyDataSetChanged();
    }
}
