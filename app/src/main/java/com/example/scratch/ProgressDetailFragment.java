package com.example.scratch;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ProgressDetailFragment extends Fragment {

    private static final String ARG_METRIC = "metric";

    public static ProgressDetailFragment newInstance(String metric) {
        ProgressDetailFragment fragment = new ProgressDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_METRIC, metric);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_progress_detail, container, false);

        TextView tvMetricTitle = view.findViewById(R.id.tvMetricTitle);
        EditText etMetricInput = view.findViewById(R.id.etMetricInput);
        Button btnSave = view.findViewById(R.id.btnSave);

        // Get the metric type passed from ViewPager/Activity
        String metric = getArguments() != null ? getArguments().getString(ARG_METRIC) : "Metric";

        // Set title and hint dynamically
        tvMetricTitle.setText(metric + " Tracking");
        etMetricInput.setHint("Enter your " + metric.toLowerCase());

        // Save button logic (you can add Firebase or local storage here)
        btnSave.setOnClickListener(v -> {
            String input = etMetricInput.getText().toString().trim();
            if (!input.isEmpty()) {
                Toast.makeText(getContext(), metric + " Saved: " + input, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Please enter your " + metric.toLowerCase(), Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}
