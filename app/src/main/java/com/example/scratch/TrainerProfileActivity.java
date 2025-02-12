package com.example.scratch;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class TrainerProfileActivity extends AppCompatActivity {

    TextView trainerName, selectedDate;
    Button btnBookNow, btnSetDate;
    ImageButton btnBack;
    ListView listAvailableTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainer_profile);

        trainerName = findViewById(R.id.tvTrainerName);
        selectedDate = findViewById(R.id.tvSelectedDate);
        btnBookNow = findViewById(R.id.btnBookNow);
        btnSetDate = findViewById(R.id.btnSetDate);
        btnBack = findViewById(R.id.btnBack);
        listAvailableTime = findViewById(R.id.listAvailableTime);

        String name = getIntent().getStringExtra("TrainerName");
        trainerName.setText(name);

        Calendar calendar = Calendar.getInstance();
        String currentDate = calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH);
        selectedDate.setText("Selected Date: " + currentDate);

        btnSetDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(TrainerProfileActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String date = year + "-" + (month + 1) + "-" + dayOfMonth;
                        selectedDate.setText("Selected Date: " + date);
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnBookNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TrainerProfileActivity.this, ConfirmationActivity.class);
                intent.putExtra("TrainerName", name);
                intent.putExtra("BookingDate", selectedDate.getText().toString());
                startActivity(intent);
            }
        });
    }
}
