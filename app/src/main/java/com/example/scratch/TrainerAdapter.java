package com.example.scratch;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TrainerAdapter extends RecyclerView.Adapter<TrainerAdapter.TrainerViewHolder> {

    private List<TrainerModel> trainerList;

    public TrainerAdapter(List<TrainerModel> trainerList) {
        this.trainerList = trainerList;
    }

    @NonNull
    @Override
    public TrainerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trainer, parent, false);
        return new TrainerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrainerViewHolder holder, int position) {
        TrainerModel trainer = trainerList.get(position);
        holder.tvName.setText(trainer.getName());
        holder.tvSpecialization.setText(trainer.getSpecialization());
        holder.imgTrainer.setImageResource(trainer.getImageResId());
    }

    @Override
    public int getItemCount() {
        return trainerList.size();
    }

    public static class TrainerViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvSpecialization;
        ImageView imgTrainer;

        public TrainerViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvSpecialization = itemView.findViewById(R.id.tvSpecialization);
            imgTrainer = itemView.findViewById(R.id.imgTrainer);
        }
    }
}
