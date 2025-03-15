package com.example.scratch;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.*;

public class RegisterTrainerActivity extends AppCompatActivity {

    private EditText etFullName, etContactInfo;
    private Spinner spinnerProficiency;
    private CheckBox cbApplyGeneralTime;
    private Button btnRegisterTrainer;
    private Map<String, List<String>> dayTimeSelections = new HashMap<>();
    private String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    private FirebaseAuth mAuth;
    private DatabaseReference trainersRef;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registertrainer);

        mAuth = FirebaseAuth.getInstance();
        trainersRef = FirebaseDatabase.getInstance().getReference("Trainers");

        etFullName = findViewById(R.id.etFullName);
        spinnerProficiency = findViewById(R.id.spinnerProficiency);
        etContactInfo = findViewById(R.id.etContactInfo);
        cbApplyGeneralTime = findViewById(R.id.cbApplyGeneralTime);
        btnRegisterTrainer = findViewById(R.id.btnRegisterTrainer);

        // Prefill full name from the authenticated user
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String displayName = user.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                etFullName.setText(displayName);
            } else {
                // Optionally fetch from Firebase Database if needed
                trainersRef.child(user.getUid()).get().addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        String fullName = snapshot.child("fullName").getValue(String.class);
                        if (fullName != null) {
                            etFullName.setText(fullName);
                        }
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch user name", Toast.LENGTH_SHORT).show();
                });
            }
        }

        // Set up the proficiency spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.proficiency_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProficiency.setAdapter(adapter);

        // Initialize day checkboxes and time text views
        for (String day : days) {
            int cbId = getResources().getIdentifier("cb" + day, "id", getPackageName());
            int tvId = getResources().getIdentifier("tv" + day + "Times", "id", getPackageName());

            CheckBox cbDay = findViewById(cbId);
            TextView tvDayTimes = findViewById(tvId);

            cbDay.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectTimeForDay(day, tvDayTimes);
                } else {
                    dayTimeSelections.remove(day);
                    tvDayTimes.setText("");
                }
            });
        }

        btnRegisterTrainer.setOnClickListener(v -> registerTrainer());
    }


    private void selectTimeForDay(String day, TextView tvDayTimes) {
        String[] times = {"8 AM - 10 AM", "11 AM - 1 PM", "2 PM - 4 PM"};
        boolean[] checkedTimes = new boolean[times.length];
        List<String> selectedTimes = dayTimeSelections.getOrDefault(day, new ArrayList<>());

        for (int i = 0; i < times.length; i++) {
            checkedTimes[i] = selectedTimes.contains(times[i]);
        }

        new android.app.AlertDialog.Builder(this)
                .setTitle("Select Time Slots for " + day)
                .setMultiChoiceItems(times, checkedTimes, (dialog, which, isChecked) -> {
                    if (isChecked) {
                        selectedTimes.add(times[which]);
                    } else {
                        selectedTimes.remove(times[which]);
                    }
                })
                .setPositiveButton("OK", (dialog, which) -> {
                    dayTimeSelections.put(day, selectedTimes);
                    tvDayTimes.setText(String.join(", ", selectedTimes));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void registerTrainer() {
        String fullName = etFullName.getText().toString().trim();
        String proficiency = spinnerProficiency.getSelectedItem().toString();
        String contactInfo = etContactInfo.getText().toString().trim();

        if (fullName.isEmpty() || proficiency.isEmpty() || contactInfo.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        Map<String, Object> trainerData = new HashMap<>();
        trainerData.put("userId", userId);
        trainerData.put("fullName", fullName);
        trainerData.put("proficiency", proficiency);
        trainerData.put("contactInfo", contactInfo);
        trainerData.put("availableSchedule", dayTimeSelections);

        trainersRef.child(userId).setValue(trainerData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Trainer Registered Successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to Register Trainer", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
