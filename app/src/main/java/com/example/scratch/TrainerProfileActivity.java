package com.example.scratch;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
public class TrainerProfileActivity extends AppCompatActivity {

    private TextView trainerName, trainerProficiency, trainerContact, selectedDate, trainerSchedule;
    private Button btnBookNow, btnSetDate;
    private ListView listViewAvailableDates;
    private Spinner spinnerAvailableTime;
    private ArrayAdapter<String> datesAdapter;
    private List<String> availableDates = new ArrayList<>();
    private String trainerId;
    private DatabaseReference trainersRef, appointmentsRef;
    private List<String> formattedDates = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainer_profile);

        // Initialize views
        trainerName = findViewById(R.id.tvTrainerName);
        trainerProficiency = findViewById(R.id.tvTrainerProficiency);
        trainerContact = findViewById(R.id.tvTrainerContact);
        selectedDate = findViewById(R.id.tvSelectedDate);
        btnBookNow = findViewById(R.id.btnBookNow);
        btnSetDate = findViewById(R.id.btnSetDate);
        listViewAvailableDates = findViewById(R.id.listViewAvailableDates);
        spinnerAvailableTime = findViewById(R.id.spinnerAvailableTime);
        trainerSchedule = findViewById(R.id.trainerSchedule);

        appointmentsRef = FirebaseDatabase.getInstance().getReference("Appointments");
        trainersRef = FirebaseDatabase.getInstance().getReference("Trainers");

        Intent intent = getIntent();
        trainerId = intent.getStringExtra("trainerId");

        if (trainerId == null || trainerId.isEmpty()) {
            Toast.makeText(this, "Error: Trainer ID is missing.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 👉 Load trainer info
        loadTrainerInfo();

        // 👉 Fetch schedules
        fetchAvailableSchedules();

        // 👉 Set listener for date selection
        listViewAvailableDates.setOnItemClickListener((parent, view, position, id) -> {
            if (formattedDates != null && !formattedDates.isEmpty() && position < formattedDates.size()) {
                String clickedDate = formattedDates.get(position);
                String selectedDay = extractDayOfWeekFromDate(clickedDate);
                showAvailableTimesPopup(selectedDay);
            }
        });

        // Set date button
        btnSetDate.setOnClickListener(v -> fetchAvailableSchedules());

        // Book now button
        btnBookNow.setOnClickListener(v -> {
            if (spinnerAvailableTime.getAdapter() == null || spinnerAvailableTime.getAdapter().getCount() == 0) {
                Toast.makeText(this, "No available time slots", Toast.LENGTH_SHORT).show();
                return;
            }

            Object selectedItem = spinnerAvailableTime.getSelectedItem();
            if (selectedItem == null) {
                Toast.makeText(this, "Please select a time slot", Toast.LENGTH_SHORT).show();
                return;
            }

            String selectedTimeSlot = selectedItem.toString();
            bookAppointment(selectedTimeSlot);
        });
    }

    private void loadTrainerInfo() {
        trainersRef.child(trainerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                String proficiency = snapshot.child("proficiency").getValue(String.class);
                String contact = snapshot.child("contact").getValue(String.class);

                trainerName.setText(name != null ? name : "No name");
                trainerProficiency.setText(proficiency != null ? proficiency : "No proficiency");
                trainerContact.setText(contact != null ? contact : "No contact");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TrainerProfileActivity.this, "Error loading trainer info", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private String extractDayOfWeekFromDate(String formattedDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMM dd yyyy", Locale.getDefault());
            Date date = sdf.parse(formattedDate);
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
            return dayFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }


    private void fetchAvailableSchedules() {

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        String today = sdf.format(calendar.getTime());
        calendar.add(Calendar.MONTH, 3); // Go 3 months forward
        String threeMonthsLater = sdf.format(calendar.getTime());
        appointmentsRef.orderByChild("date").startAt(today).endAt(threeMonthsLater)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<String> bookedDates = new ArrayList<>();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        String currentDate = sdf.format(new Date());

                        // Get the booked dates
                        for (DataSnapshot appointmentSnapshot : snapshot.getChildren()) {
                            String appointmentDate = appointmentSnapshot.child("date").getValue(String.class);
                            if (appointmentDate != null && appointmentDate.compareTo(currentDate) >= 0) {
                                bookedDates.add(appointmentDate);
                            }
                        }

                        // Fetch available schedule for the trainer
                        trainersRef.child(trainerId).child("availableSchedule")
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        // Check if the availableSchedule is stored as a Map of days with time slots
                                        Map<String, Object> availableSchedule = (Map<String, Object>) snapshot.getValue();
                                        List<String> availableDates = new ArrayList<>();  // To store the available dates

                                        if (availableSchedule != null && !availableSchedule.isEmpty()) {
                                            StringBuilder scheduleText = new StringBuilder();

                                            // Get current date
                                            Calendar calendar = Calendar.getInstance();
                                            int currentYear = calendar.get(Calendar.YEAR);
                                            int currentMonth = calendar.get(Calendar.MONTH);
                                            int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

                                            // Loop through the available schedule
                                            for (Map.Entry<String, Object> entry : availableSchedule.entrySet()) {
                                                String dayOfWeek = entry.getKey(); // Day of the week (e.g., "Friday")
                                                List<String> timeSlots = (List<String>) entry.getValue(); // List of time slots for that day

                                                if (timeSlots != null && !timeSlots.isEmpty()) {
                                                    for (String timeSlot : timeSlots) {
                                                        // Build the schedule text
                                                        scheduleText.append(dayOfWeek).append(": ").append(timeSlot).append("\n");

                                                        // Calculate the actual date for the given day of the week
                                                        String date = getNextDateForDayOfWeek(dayOfWeek, currentYear, currentMonth, currentDay);

                                                        // Add the calculated date to the available dates list
                                                        if (date != null && !bookedDates.contains(date)) {
                                                            availableDates.add(date); // Only add if not already booked
                                                        }
                                                    }
                                                }
                                            }

                                            // Display the available schedule text
                                            trainerSchedule.setText(scheduleText.toString());
                                        } else {
                                            trainerSchedule.setText("No schedule available");
                                        }

                                        // Now update the available dates list in the ListView
                                        if (!availableDates.isEmpty()) {
                                            // Format available dates and days
                                            List<String> formattedDates = new ArrayList<>();  // Declare formattedDates here
                                            formattedDates.clear();
                                            for (String date : availableDates) {
                                                String formattedDate = formatDateWithDay(date);
                                                formattedDates.add(formattedDate);
                                            }

                                            // Create and set the adapter
                                            datesAdapter = new ArrayAdapter<>(TrainerProfileActivity.this, android.R.layout.simple_list_item_1, formattedDates);
                                            listViewAvailableDates.setAdapter(datesAdapter);

                                            // Set click listener for item clicks
                                            listViewAvailableDates.setOnItemClickListener((parent, view, position, id) -> {
                                                if (!formattedDates.isEmpty() && position < formattedDates.size()) {
                                                    String clickedDate = formattedDates.get(position);
                                                    // Handle the click event (e.g., navigate to the trainer's profile or schedule booking)
                                                    Toast.makeText(TrainerProfileActivity.this, "Selected: " + clickedDate, Toast.LENGTH_SHORT).show();
                                                }
                                            });

                                        } else {
                                            // Handle case where there are no available dates
                                            List<String> noDates = new ArrayList<>();
                                            noDates.add("No available dates");
                                            datesAdapter = new ArrayAdapter<>(TrainerProfileActivity.this, android.R.layout.simple_list_item_1, noDates);
                                            listViewAvailableDates.setAdapter(datesAdapter);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(TrainerProfileActivity.this, "Error fetching available dates.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(TrainerProfileActivity.this, "Error fetching appointments.", Toast.LENGTH_SHORT).show();
                    }
                });
    }



    // Helper function to calculate the next date for a given day of the week
    private String getNextDateForDayOfWeek(String dayOfWeek, int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth);

        // Get the integer value for the day of the week (e.g., "Monday" = 2, "Friday" = 6)
        int targetDayOfWeek = -1;
        switch (dayOfWeek) {
            case "Monday":
                targetDayOfWeek = Calendar.MONDAY;
                break;
            case "Tuesday":
                targetDayOfWeek = Calendar.TUESDAY;
                break;
            case "Wednesday":
                targetDayOfWeek = Calendar.WEDNESDAY;
                break;
            case "Thursday":
                targetDayOfWeek = Calendar.THURSDAY;
                break;
            case "Friday":
                targetDayOfWeek = Calendar.FRIDAY;
                break;
            case "Saturday":
                targetDayOfWeek = Calendar.SATURDAY;
                break;
            case "Sunday":
                targetDayOfWeek = Calendar.SUNDAY;
                break;
        }

        if (targetDayOfWeek != -1) {
            // Find the next date for the target day of the week
            int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            int daysToAdd = (targetDayOfWeek - currentDayOfWeek + 7) % 7; // To ensure the next occurrence

            calendar.add(Calendar.DAY_OF_YEAR, daysToAdd);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String calculatedDate = sdf.format(calendar.getTime());

            // Check if the calculated date is within 3 months
            Calendar now = Calendar.getInstance();
            now.add(Calendar.MONTH, 3); // Add 3 months
            if (calendar.before(now)) {
                return calculatedDate;
            }
        }

        return null;
    }


    // Helper function to format the date as "April 9, 2025" and display the day
    private String formatDateWithDay(String date) {
        try {
            SimpleDateFormat sdfInput = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date parsedDate = sdfInput.parse(date);

            SimpleDateFormat sdfOutput = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
            String formattedDate = sdfOutput.format(parsedDate);

            // Get the day of the week
            SimpleDateFormat dayOfWeekFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
            String dayOfWeek = dayOfWeekFormat.format(parsedDate);

            return formattedDate + " (" + dayOfWeek + ")";
        } catch (ParseException e) {
            e.printStackTrace();
            return date;
        }
    }
    // Load available times based on the day of the week
    private void loadAvailableTimes(String dayOfWeek) {
        // Assuming available times are set in the database for each day of the week
        trainersRef.child(trainerId).child("availableSchedule")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Get the available schedule as a List (not Map)
                        List<String> availableSchedule = (List<String>) snapshot.child("availableSchedule").getValue();
                        if (availableSchedule != null && !availableSchedule.isEmpty()) {
                            StringBuilder scheduleText = new StringBuilder();
                            for (String schedule : availableSchedule) {
                                scheduleText.append(schedule).append("\n");
                            }
                            // Display the schedule
                            trainerSchedule.setText(scheduleText.toString());
                        } else {
                            trainerSchedule.setText("No schedule available");
                        }

                        // Update available dates list
                        datesAdapter = new ArrayAdapter<>(TrainerProfileActivity.this, android.R.layout.simple_list_item_1, availableDates);
                        listViewAvailableDates.setAdapter(datesAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(TrainerProfileActivity.this, "Error fetching available dates.", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void loadAvailableTimeSlots(String selectedDate) {
        // Reference to the Firebase node where time slots are stored
        DatabaseReference timeSlotsRef = FirebaseDatabase.getInstance().getReference("timeSlots");

        // Query to get the available time slots for the selected date
        timeSlotsRef.orderByChild("date").equalTo(selectedDate)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<String> availableTimeSlots = new ArrayList<>();
                        if (snapshot.exists()) {
                            // Iterate through the results and add time slots
                            for (DataSnapshot timeSlotSnapshot : snapshot.getChildren()) {
                                String timeSlot = timeSlotSnapshot.child("timeSlot").getValue(String.class);
                                if (timeSlot != null) {
                                    availableTimeSlots.add(timeSlot);
                                }
                            }
                        } else {
                            availableTimeSlots.add("No available slots");
                        }

                        // Update the spinner with available time slots
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(TrainerProfileActivity.this,
                                android.R.layout.simple_spinner_item, availableTimeSlots);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerAvailableTime.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(TrainerProfileActivity.this, "Failed to load time slots.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String getDayOfWeek(int dayOfWeek) {
        String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        return days[dayOfWeek - 1];
    }

    private void bookAppointment(String selectedTimeSlot) {
        String date = selectedDate.getText().toString().replace("Selected Date: ", "");
        if (selectedTimeSlot == null || "No available slots".equals(selectedTimeSlot)) {
            Toast.makeText(this, "No available time slot selected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the current user ID from FirebaseAuth
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Check for duplicate appointment
        appointmentsRef.orderByChild("trainerId").equalTo(trainerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("FirebaseDebug", "Available schedule data: " + snapshot.child("availableSchedule").getValue());

                boolean duplicate = false;
                for (DataSnapshot appointment : snapshot.getChildren()) {
                    String existingDate = appointment.child("date").getValue(String.class);
                    String existingTimeSlot = appointment.child("timeSlot").getValue(String.class);
                    if (date.equals(existingDate) && selectedTimeSlot.equals(existingTimeSlot)) {
                        duplicate = true;
                        break;
                    }
                }

                if (duplicate) {
                    Toast.makeText(TrainerProfileActivity.this, "Appointment already exists for this slot!", Toast.LENGTH_SHORT).show();
                } else {
                    // Book appointment
                    String appointmentId = UUID.randomUUID().toString();
                    Map<String, Object> appointment = new HashMap<>();
                    appointment.put("trainerId", trainerId);
                    appointment.put("userId", userId); // Correctly setting userId here
                    appointment.put("date", date);
                    appointment.put("timeSlot", selectedTimeSlot);

                    appointmentsRef.child(appointmentId).setValue(appointment)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(TrainerProfileActivity.this, "Appointment booked!", Toast.LENGTH_SHORT).show();
                                // Remove booked slot from spinner or list
                                ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerAvailableTime.getAdapter();
                                adapter.remove(selectedTimeSlot);
                                adapter.notifyDataSetChanged();
                            })
                            .addOnFailureListener(e -> Toast.makeText(TrainerProfileActivity.this, "Failed to book appointment.", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TrainerProfileActivity.this, "Failed to check appointments", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAvailableTimesPopup(String selectedDay) {
        // Inflate the dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_available_times, null);

        // Create the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        Button btnTime1 = dialogView.findViewById(R.id.btnTime1);
        Button btnTime2 = dialogView.findViewById(R.id.btnTime2);
        Button btnTime3 = dialogView.findViewById(R.id.btnTime3);

        // Load available times for the selected day
        trainersRef.child(trainerId).child("availableSchedule").child(selectedDay)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<String> availableTimes = new ArrayList<>();
                        for (DataSnapshot timeSnapshot : snapshot.getChildren()) {
                            String time = timeSnapshot.getValue(String.class);
                            if (time != null) {
                                availableTimes.add(time);
                            }
                        }

                        // Enable/Disable buttons based on availability
                        btnTime1.setEnabled(availableTimes.contains("Time Slot 1"));
                        btnTime2.setEnabled(availableTimes.contains("Time Slot 2"));
                        btnTime3.setEnabled(availableTimes.contains("Time Slot 3"));

                        // Set button background color based on availability
                        setButtonState(btnTime1, availableTimes.contains("Time Slot 1"));
                        setButtonState(btnTime2, availableTimes.contains("Time Slot 2"));
                        setButtonState(btnTime3, availableTimes.contains("Time Slot 3"));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(TrainerProfileActivity.this, "Failed to load available times", Toast.LENGTH_SHORT).show();
                    }
                });

        // Add ClickListeners for the time buttons
        btnTime1.setOnClickListener(v -> {
            if (btnTime1.isEnabled()) {
                bookAppointment("Time Slot 1");
            }
        });

        btnTime2.setOnClickListener(v -> {
            if (btnTime2.isEnabled()) {
                bookAppointment("Time Slot 2");
            }
        });

        btnTime3.setOnClickListener(v -> {
            if (btnTime3.isEnabled()) {
                bookAppointment("Time Slot 3");
            }
        });

        // Show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();


    }
    private void setButtonState(Button button, boolean isAvailable) {
        if (isAvailable) {
            button.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_green_light));
        } else {
            button.setBackgroundColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        }
    }

}


