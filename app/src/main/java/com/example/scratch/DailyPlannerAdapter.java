package com.example.scratch;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DailyPlannerAdapter extends RecyclerView.Adapter<DailyPlannerAdapter.ViewHolder> {

    private final List<String> dailyPlannerItems;

    public DailyPlannerAdapter(List<String> dailyPlannerItems) {
        this.dailyPlannerItems = dailyPlannerItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_daily_planner, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String task = dailyPlannerItems.get(position);
        holder.taskTextView.setText(task);
    }

    @Override
    public int getItemCount() {
        return dailyPlannerItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView taskTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            taskTextView = itemView.findViewById(R.id.tvTask);
        }
    }
}
