package com.example.scratch;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class AddTaskActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addtask);

        databaseReference = FirebaseDatabase.getInstance().getReference("Tasks");

        CheckBox cbWeight = findViewById(R.id.cbWeight);
        EditText etWeight = findViewById(R.id.etWeight);
        CheckBox cbHeartRate = findViewById(R.id.cbHeartRate);
        EditText etHeartRate = findViewById(R.id.etHeartRate);
        CheckBox cbCalories = findViewById(R.id.cbCalories);
        EditText etCalories = findViewById(R.id.etCalories);
        CheckBox cbWorkoutDuration = findViewById(R.id.cbWorkoutDuration);
        EditText etWorkoutDuration = findViewById(R.id.etWorkoutDuration);
        CheckBox cbSleep = findViewById(R.id.cbSleep);
        EditText etSleep = findViewById(R.id.etSleep);
        CheckBox cbReview = findViewById(R.id.cbReview);
        EditText etReview = findViewById(R.id.etReview);

        Button btnSaveTask = findViewById(R.id.btnSaveTask);

        btnSaveTask.setOnClickListener(v -> {
            boolean isValid = true;
            isValid &= saveTask(cbWeight, etWeight, "Weight");
            isValid &= saveTask(cbHeartRate, etHeartRate, "Heart Rate");
            isValid &= saveTask(cbCalories, etCalories, "Calories");
            isValid &= saveTask(cbWorkoutDuration, etWorkoutDuration, "Workout Duration");
            isValid &= saveTask(cbSleep, etSleep, "Sleep");
            isValid &= saveTask(cbReview, etReview, "Review");

            if (isValid) {
                Toast.makeText(this, "All tasks saved successfully!", Toast.LENGTH_SHORT).show();
                finish();  // Go back after saving
            }
        });
    }

    private boolean saveTask(CheckBox checkBox, EditText editText, String taskType) {
        if (checkBox.isChecked()) {
            String value = editText.getText().toString().trim();
            if (TextUtils.isEmpty(value)) {
                Toast.makeText(this, "Please set a value for " + taskType, Toast.LENGTH_SHORT).show();
                return false;
            }
            Map<String, Object> task = new HashMap<>();
            task.put("type", taskType);
            task.put("value", value);
            databaseReference.push().setValue(task)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, taskType + " saved", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to save " + taskType, Toast.LENGTH_SHORT).show());
        }
        return true;
    }
}
