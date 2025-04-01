package com.example.scratch;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

public class CreateGoalActivity extends AppCompatActivity {

    private EditText etCalories, etWeight, etHeartRate, etWorkoutDuration, etSleep;
    private DatabaseReference databaseReference;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_goal);

        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("Goals");
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Initialize fields
        etCalories = findViewById(R.id.etCalories);
        etWeight = findViewById(R.id.etWeight);
        etHeartRate = findViewById(R.id.etHeartRate);
        etWorkoutDuration = findViewById(R.id.etWorkoutDuration);
        etSleep = findViewById(R.id.etSleep);
        Button btnSaveGoal = findViewById(R.id.btnSaveGoal);

        btnSaveGoal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveGoal();
            }
        });
    }

    private void saveGoal() {
        String calories = etCalories.getText().toString().trim();
        String weight = etWeight.getText().toString().trim();
        String heartRate = etHeartRate.getText().toString().trim();
        String workoutDuration = etWorkoutDuration.getText().toString().trim();
        String sleep = etSleep.getText().toString().trim();

        if (calories.isEmpty() && weight.isEmpty() && heartRate.isEmpty() && workoutDuration.isEmpty() && sleep.isEmpty()) {
            Toast.makeText(this, "Please set at least one goal", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> goalMap = new HashMap<>();
        if (!calories.isEmpty()) goalMap.put("Calories", calories);
        if (!weight.isEmpty()) goalMap.put("Weight", weight);
        if (!heartRate.isEmpty()) goalMap.put("Heart Rate", heartRate);
        if (!workoutDuration.isEmpty()) goalMap.put("Workout Duration", workoutDuration);
        if (!sleep.isEmpty()) goalMap.put("Sleep", sleep);

        // Save goals under the user's ID
        databaseReference.child(userId).setValue(goalMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(CreateGoalActivity.this, "Goal Saved", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(CreateGoalActivity.this, "Failed to Save Goal", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
