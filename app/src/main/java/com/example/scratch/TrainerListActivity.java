package com.example.scratch;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TrainerListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainers_list);

        // Display Workout Type
        TextView workoutType = findViewById(R.id.tvWorkoutType);
        Intent intent = getIntent();
        String workoutTypeName = intent.getStringExtra("workout_type");
        workoutType.setText("Workout Type: " + workoutTypeName);

        // Trainer 1 Click
        LinearLayout trainer1 = findViewById(R.id.rlbbgjqqb6zg);
        trainer1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TrainerListActivity.this, TrainerProfileActivity.class);
                startActivity(intent);
            }
        });

        // Trainer 2 Click
        LinearLayout trainer2 = findViewById(R.id.rtsgvd3l2jtm);
        trainer2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TrainerListActivity.this, TrainerProfileActivity.class);
                startActivity(intent);
            }
        });

        // Trainer 3 Click
        LinearLayout trainer3 = findViewById(R.id.rxbcqteej5ae);
        trainer3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TrainerListActivity.this, TrainerProfileActivity.class);
                startActivity(intent);
            }
        });
    }
}