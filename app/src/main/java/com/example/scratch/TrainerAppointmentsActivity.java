package com.example.scratch;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.scratch.BottomNavTrainerHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrainerAppointmentsActivity extends AppCompatActivity {
    private ListView listViewAppointments;
    private DatabaseReference appointmentsRef;
    private String trainerId;
    private ArrayList<String> appointmentList;
    private ArrayAdapter<String> adapter;
    private RecyclerView recyclerViewAppointments;
    private AppointmentAdapter appointmentAdapter;
    private List<Appointment> fullAppointmentList = new ArrayList<>();
    private List<Appointment> filteredList = new ArrayList<>();

    private Spinner statusFilterSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainer_appointments);
        trainerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        appointmentsRef = FirebaseDatabase.getInstance().getReference("Appointments");

        appointmentList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, appointmentList);

        loadAppointments();

        BottomNavigationView nav = findViewById(R.id.bottomNavigationView);
        BottomNavTrainerHelper.setup(this, nav, R.id.nav_appointments); // Replace with your actual menu ID
        recyclerViewAppointments = findViewById(R.id.recyclerViewAppointments);
        recyclerViewAppointments.setLayoutManager(new LinearLayoutManager(this));



        recyclerViewAppointments.setAdapter(appointmentAdapter);

        statusFilterSpinner = findViewById(R.id.spinnerFilter);
        setupStatusFilter();

        loadAppointments();

        RecyclerView recyclerView = findViewById(R.id.recyclerViewAppointments);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        List<Appointment> appointmentList = new ArrayList<>();
        AppointmentAdapter adapter = new AppointmentAdapter(appointmentList, appointment -> {

        });
        recyclerView.setAdapter(adapter);
        appointmentAdapter = new AppointmentAdapter(filteredList, this::showAppointmentPopup);

        recyclerViewAppointments.setAdapter(appointmentAdapter);


    }
    private void showAppointmentPopup(Appointment appointment) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Appointment Options");

        String[] options = {"✅ Confirm", "❌ Cancel", "Close"};
        builder.setItems(options, (dialog, which) -> {
            DatabaseReference apptRef = FirebaseDatabase.getInstance()
                    .getReference("Appointments")
                    .child(appointment.getAppointmentId());

            switch (which) {
                case 0: // Confirm
                    apptRef.child("status").setValue("Confirmed");
                    Toast.makeText(this, "Appointment confirmed", Toast.LENGTH_SHORT).show();
                    break;
                case 1: // Cancel
                    apptRef.child("status").setValue("Cancelled");
                    Toast.makeText(this, "Appointment cancelled", Toast.LENGTH_SHORT).show();
                    break;
                case 2: // Close
                    dialog.dismiss();
                    break;
            }
        });

        builder.show();
    }
    private void setupStatusFilter() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"All", "Not Confirmed", "Confirmed", "Completed"});
        statusFilterSpinner.setAdapter(adapter);
        statusFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                filterAppointments(parent.getItemAtPosition(pos).toString());
            }
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
    private void filterAppointments(String status) {
        filteredList.clear();
        if (status.equals("All")) {
            filteredList.addAll(fullAppointmentList);
        } else {
            for (Appointment appt : fullAppointmentList) {
                if (appt.getStatus().equals(status)) {
                    filteredList.add(appt);
                }
            }
        }
        appointmentAdapter.notifyDataSetChanged();
    }


    private void loadAppointments() {
        appointmentsRef.orderByChild("trainerId").equalTo(trainerId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        fullAppointmentList.clear();
                        filteredList.clear();

                        List<DataSnapshot> appointmentSnapshots = new ArrayList<>();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            appointmentSnapshots.add(data);
                        }

                        if (appointmentSnapshots.isEmpty()) {
                            filterAppointments(statusFilterSpinner.getSelectedItem().toString());
                            return;
                        }

                        final int[] counter = {0};
                        for (DataSnapshot data : appointmentSnapshots) {
                            Appointment appt = data.getValue(Appointment.class);
                            if (appt == null) continue;

                            // Set the appointment ID
                            appt.setAppointmentId(data.getKey());

                            // Fetch user details and add appointment to list
                            String userId = appt.getUserId();
                            FirebaseDatabase.getInstance().getReference("Users").child(userId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot userSnap) {
                                            String firstName = userSnap.child("firstName").getValue(String.class);
                                            String lastName = userSnap.child("lastName").getValue(String.class);
                                            appt.setUserFullName(firstName + " " + lastName);

                                            fullAppointmentList.add(appt);
                                            counter[0]++;
                                            if (counter[0] == appointmentSnapshots.size()) {
                                                filterAppointments(statusFilterSpinner.getSelectedItem().toString());
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            appt.setUserFullName("Unknown User");
                                            fullAppointmentList.add(appt);
                                            counter[0]++;
                                            if (counter[0] == appointmentSnapshots.size()) {
                                                filterAppointments(statusFilterSpinner.getSelectedItem().toString());
                                            }
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(TrainerAppointmentsActivity.this, "Failed to load appointments", Toast.LENGTH_SHORT).show();
                    }
                });
    }





}

