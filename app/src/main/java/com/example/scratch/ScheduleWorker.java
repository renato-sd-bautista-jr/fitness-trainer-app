package com.example.scratch;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ScheduleWorker extends Worker {

    private static final String TAG = "ScheduleWorker";
    private DatabaseReference appointmentsRef;
    private String userId;

    public ScheduleWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        appointmentsRef = FirebaseDatabase.getInstance().getReference("Appointments");
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public Result doWork() {
        checkUpcomingAppointments();
        return Result.success();
    }

    private void checkUpcomingAppointments() {
        String todayDate = new SimpleDateFormat("yyyy-M-d", Locale.getDefault()).format(Calendar.getInstance().getTime());

        appointmentsRef.orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot appointment : snapshot.getChildren()) {
                            String date = appointment.child("date").getValue(String.class);
                            String timeSlot = appointment.child("timeSlot").getValue(String.class);

                            if (date != null && timeSlot != null) {
                                if (date.equals(todayDate)) {
                                    NotificationHelper.sendNotification(getApplicationContext(),
                                            "Today's Schedule", "You have an appointment at " + timeSlot);
                                }

                                // Check if the appointment is 1 hour away
                                checkOneHourBefore(timeSlot);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Failed to check appointments: " + error.getMessage());
                    }
                });
    }

    private void checkOneHourBefore(String timeSlot) {
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        String[] timeParts = timeSlot.split(":");
        if (timeParts.length == 2) {
            int appointmentHour = Integer.parseInt(timeParts[0]);
            int appointmentMinute = Integer.parseInt(timeParts[1]);

            if (appointmentHour == currentHour + 1 && appointmentMinute <= currentMinute) {
                NotificationHelper.sendNotification(getApplicationContext(),
                        "Upcoming Appointment", "Your appointment is in 1 hour at " + timeSlot);
            }
        }
    }

}
