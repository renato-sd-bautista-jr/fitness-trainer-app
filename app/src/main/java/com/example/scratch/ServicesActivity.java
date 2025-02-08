package com.example.scratch;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ServicesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ServiceAdapter serviceAdapter;
    private List<Service> serviceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services);

        recyclerView = findViewById(R.id.recyclerServices);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        serviceList = new ArrayList<>();
        serviceAdapter = new ServiceAdapter(serviceList);
        recyclerView.setAdapter(serviceAdapter);

        loadServices();
    }

    private void loadServices() {
        serviceList.add(new Service("Cardio Workout", "30 min", "$15", R.drawable.musikanoir1));
        serviceList.add(new Service("CrossFit", "45 min", "$20", R.drawable.musikanoir1));
        serviceList.add(new Service("Yoga", "60 min", "$12", R.drawable.musikanoir1));
        serviceList.add(new Service("HIIT", "40 min", "$18", R.drawable.musikanoir1));

        serviceAdapter.notifyDataSetChanged();
    }
}
