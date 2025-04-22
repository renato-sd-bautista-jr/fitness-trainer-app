package com.example.scratch;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.text.SimpleDateFormat;

public class TrainerProfileActivity extends AppCompatActivity {

    private TextView trainerName, trainerProficiency, trainerContact, selectedDate;
    private Button btnBookNow;
    private ListView listViewAvailableDates;
    private Spinner spinnerAvailableTime;
    private ArrayAdapter<String> timeAdapter;
    private ArrayList<String> formattedDates = new ArrayList<>();
    private Map<String, List<String>> dateToTimeSlotsMap = new HashMap<>();
    private String selectedDateForBooking = null;

    private DatabaseReference trainersRef, appointmentsRef;
    private String trainerId;
    private String selectedTimeSlot = "";
    private String selectedDateString = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainer_profile);

        // Init views
        trainerName = findViewById(R.id.tvTrainerName);
        trainerProficiency = findViewById(R.id.tvTrainerProficiency);
        trainerContact = findViewById(R.id.tvTrainerContact);
        selectedDate = findViewById(R.id.tvSelectedDate);
        btnBookNow = findViewById(R.id.btnBookNow);
        listViewAvailableDates = findViewById(R.id.listViewAvailableDates);
        spinnerAvailableTime = findViewById(R.id.spinnerAvailableTime);
        TextView trainerRating = findViewById(R.id.tvTrainerRating);

        // Firebase
        trainersRef = FirebaseDatabase.getInstance().getReference("Trainers");
        appointmentsRef = FirebaseDatabase.getInstance().getReference("Appointments");

        // Get intent extras
        trainerId = getIntent().getStringExtra("trainerId");
        String name = getIntent().getStringExtra("TrainerName");
        String proficiency = getIntent().getStringExtra("Proficiency");
        String contact = getIntent().getStringExtra("ContactInfo");
        fetchAndDisplayTrainerRating(trainerRating);
        // Set trainer info
        trainerName.setText(name);
        trainerProficiency.setText(proficiency);
        trainerContact.setText(contact);

        fetchAvailableSchedules();

        listViewAvailableDates.setOnItemClickListener((parent, view, position, id) -> {
            if (position < formattedDates.size()) {
                String fullPreview = formattedDates.get(position); // format: 2025-4-25 - 8 AM, 10 AM
                String[] parts = fullPreview.split(" - ");
                if (parts.length >= 2) {
                    selectedDateString = parts[0]; // e.g., 2025-4-25
                    selectedDate.setText("Selected Date: " + selectedDateString);

                    // Fetch appointments for selected date and update the popup
                    appointmentsRef.orderByChild("date").equalTo(selectedDateString)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    List<String> originalSlots = dateToTimeSlotsMap.get(selectedDateString);
                                    List<String> availableSlots = new ArrayList<>(originalSlots);

                                    for (DataSnapshot appointment : snapshot.getChildren()) {
                                        String bookedSlot = appointment.child("timeSlot").getValue(String.class);
                                        String trainer = appointment.child("trainerId").getValue(String.class);

                                        if (trainerId.equals(trainer)) {
                                            availableSlots.remove(bookedSlot);
                                        }
                                    }

                                    showAvailableTimesPopup(selectedDateString, availableSlots);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(TrainerProfileActivity.this, "Error loading time slots", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });



        btnBookNow.setOnClickListener(v -> {
            if (selectedDateString.isEmpty()) {
                Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedTimeSlot == null || selectedTimeSlot.isEmpty()) {
                Toast.makeText(this, "Please select a time", Toast.LENGTH_SHORT).show();
                return;
            }

            bookAppointment(selectedTimeSlot);
        });
    }
    private void showAvailableTimesPopup(String selectedDate, List<String> availableTimes) {
        if (availableTimes == null || availableTimes.isEmpty()) {
            Toast.makeText(this, "No available time slots", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Available Times on " + selectedDate);

        String[] timeSlotArray = availableTimes.toArray(new String[0]);
        builder.setItems(timeSlotArray, (dialog, which) -> {
            selectedTimeSlot = timeSlotArray[which];
            this.selectedDate.setText("Selected Date: " + selectedDate);
            spinnerAvailableTime.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, availableTimes));
        });

        builder.show();
    }


    private String extractDayOfWeekFromDate(String dateString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d", Locale.getDefault());
            Date date = sdf.parse(dateString);
            if (date != null) {
                return new SimpleDateFormat("EEEE", Locale.getDefault()).format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }
    private void fetchAvailableSchedules() {
        // Declare dayToTimeSlots map at the method level
        final Map<String, List<String>> dayToTimeSlots = new HashMap<>();

        // Fetch available schedule
        trainersRef.child(trainerId).child("availableSchedule")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot scheduleSnapshot) {
                        for (DataSnapshot daySnap : scheduleSnapshot.getChildren()) {
                            String day = daySnap.getKey();
                            List<String> slots = new ArrayList<>();
                            for (DataSnapshot timeSnap : daySnap.getChildren()) {
                                slots.add(timeSnap.getValue(String.class));
                            }
                            dayToTimeSlots.put(day, slots);
                        }

                        // Now fetch booked appointments
                        appointmentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot appointmentsSnapshot) {
                                dateToTimeSlotsMap.clear();
                                formattedDates.clear();

                                Calendar calendar = Calendar.getInstance();
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d", Locale.getDefault());
                                SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());

                                // Build a map of booked slots by date
                                Map<String, List<String>> bookedSlotsByDate = new HashMap<>();
                                for (DataSnapshot appointment : appointmentsSnapshot.getChildren()) {
                                    String date = appointment.child("date").getValue(String.class);
                                    String time = appointment.child("timeSlot").getValue(String.class);
                                    String trainer = appointment.child("trainerId").getValue(String.class);

                                    if (trainerId.equals(trainer)) {
                                        if (!bookedSlotsByDate.containsKey(date)) {
                                            bookedSlotsByDate.put(date, new ArrayList<>());
                                        }
                                        bookedSlotsByDate.get(date).add(time);
                                    }
                                }

                                // Loop through next 90 days and prepare available slots per date
                                for (int i = 0; i < 90; i++) {
                                    Date date = calendar.getTime();
                                    String formattedDate = sdf.format(date);
                                    String dayOfWeek = dayFormat.format(date);

                                    if (dayToTimeSlots.containsKey(dayOfWeek)) {
                                        List<String> allSlots = new ArrayList<>(dayToTimeSlots.get(dayOfWeek));
                                        List<String> bookedSlots = bookedSlotsByDate.getOrDefault(formattedDate, new ArrayList<>());

                                        allSlots.removeAll(bookedSlots); // Remove only booked slots for that date

                                        if (!allSlots.isEmpty()) {
                                            dateToTimeSlotsMap.put(formattedDate, allSlots);
                                            String preview = formattedDate + " - " + String.join(", ", allSlots);
                                            formattedDates.add(preview);
                                        }
                                    }

                                    calendar.add(Calendar.DATE, 1);
                                }

                                // Update the list view
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(TrainerProfileActivity.this, android.R.layout.simple_list_item_1, formattedDates);
                                listViewAvailableDates.setAdapter(adapter);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(TrainerProfileActivity.this, "Failed to fetch appointments", Toast.LENGTH_SHORT).show();
                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(TrainerProfileActivity.this, "Failed to fetch schedule", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void showTimeSelectionPopup(List<String> availableTimes) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Time Slot");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, availableTimes);
        spinnerAvailableTime.setAdapter(adapter);

        builder.setView(spinnerAvailableTime);
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void fetchAndDisplayTrainerRating(TextView trainerRatingView) {
        DatabaseReference appointmentsRef = FirebaseDatabase.getInstance().getReference("Appointments");

        appointmentsRef.orderByChild("trainerId").equalTo(trainerId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int total = 0;
                        int count = 0;

                        for (DataSnapshot appointment : snapshot.getChildren()) {
                            if (appointment.hasChild("rating")) {
                                Long ratingValue = appointment.child("rating").getValue(Long.class);
                                if (ratingValue != null) {
                                    total += ratingValue;
                                    count++;
                                }
                            }
                        }

                        if (count > 0) {
                            double average = (double) total / count;
                            // Display both average rating and the total number of ratings
                            trainerRatingView.setText(String.format(Locale.getDefault(), "Rating: %.1f ★ (%d rates)", average, count));
                        } else {
                            trainerRatingView.setText("Rating: Not yet rated");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        trainerRatingView.setText("Rating: Error");
                    }
                });
    }


    private void bookAppointment(String timeSlot) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        appointmentsRef.orderByChild("date").equalTo(selectedDateString)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean isAlreadyBooked = false;

                        for (DataSnapshot appointment : snapshot.getChildren()) {
                            String bookedTime = appointment.child("timeSlot").getValue(String.class);
                            String trainer = appointment.child("trainerId").getValue(String.class);

                            if (trainerId.equals(trainer) && timeSlot.equals(bookedTime)) {
                                isAlreadyBooked = true;
                                break;
                            }
                        }

                        if (isAlreadyBooked) {
                            Toast.makeText(TrainerProfileActivity.this, "Selected time is already booked.", Toast.LENGTH_SHORT).show();
                        } else {
                            String appointmentId = UUID.randomUUID().toString();
                            Map<String, Object> appointmentData = new HashMap<>();
                            appointmentData.put("trainerId", trainerId);
                            appointmentData.put("userId", userId);
                            appointmentData.put("date", selectedDateString);
                            appointmentData.put("timeSlot", timeSlot);
                            appointmentData.put("status", "To be confirmed");  // ✅ Status field added

                            appointmentsRef.child(appointmentId).setValue(appointmentData)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(TrainerProfileActivity.this, "Appointment booked!", Toast.LENGTH_SHORT).show();
                                        fetchAvailableSchedules(); // refresh the schedule
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(TrainerProfileActivity.this, "Failed to book appointment", Toast.LENGTH_SHORT).show();
                                        fetchAvailableSchedules();
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(TrainerProfileActivity.this, "Error checking slot availability", Toast.LENGTH_SHORT).show();
                    }
                });
    }



}
