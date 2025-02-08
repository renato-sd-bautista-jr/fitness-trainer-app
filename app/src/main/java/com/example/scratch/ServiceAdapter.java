package com.example.scratch;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {

    private List<Service> serviceList;

    public ServiceAdapter(List<Service> serviceList) {
        this.serviceList = serviceList;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.service_item, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        Service service = serviceList.get(position);
        holder.exerciseImage.setImageResource(service.getImageRes());
        holder.exerciseName.setText(service.getName());
        holder.exerciseDuration.setText("Duration: " + service.getDuration());
        holder.exercisePrice.setText(service.getPrice());
    }

    @Override
    public int getItemCount() {
        return serviceList.size();
    }

    public static class ServiceViewHolder extends RecyclerView.ViewHolder {
        ImageView exerciseImage;
        TextView exerciseName, exerciseDuration, exercisePrice;
        Button btnLearnMore, btnBookNow;

        public ServiceViewHolder(View itemView) {
            super(itemView);
            exerciseImage = itemView.findViewById(R.id.exerciseImage);
            exerciseName = itemView.findViewById(R.id.exerciseName);
            exerciseDuration = itemView.findViewById(R.id.exerciseDuration);
            exercisePrice = itemView.findViewById(R.id.exercisePrice);
            btnLearnMore = itemView.findViewById(R.id.btnLearnMore);
            btnBookNow = itemView.findViewById(R.id.btnBookNow);
        }
    }
}
