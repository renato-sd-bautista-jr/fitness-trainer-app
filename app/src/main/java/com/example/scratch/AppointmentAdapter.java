package com.example.scratch;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder> {
    private List<Appointment> appointmentList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Appointment appointment);
    }

    public AppointmentAdapter(List<Appointment> appointmentList, OnItemClickListener listener) {
        this.appointmentList = appointmentList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_appointment, parent, false);
        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        Appointment appointment = appointmentList.get(position);

        holder.dateTextView.setText(appointment.getDate());
        holder.timeTextView.setText(appointment.getTimeSlot());
        holder.statusTextView.setText(appointment.getStatus());
        holder.textViewAppointmentInfo.setText(
                "👤 " + appointment.getUserFullName() + "\n📅 " + appointment.getDate()
                        + " at " + appointment.getTimeSlot() + "\nStatus: " + appointment.getStatus()
        );

        holder.itemView.setOnClickListener(v -> listener.onItemClick(appointment));
    }

    @Override
    public int getItemCount() {
        return appointmentList.size();
    }

    public static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView;
        TextView timeTextView;
        TextView statusTextView;
        TextView textViewAppointmentInfo;

        public AppointmentViewHolder(View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.textViewDate);
            timeTextView = itemView.findViewById(R.id.textViewTime);
            statusTextView = itemView.findViewById(R.id.textViewStatus);
            textViewAppointmentInfo = itemView.findViewById(R.id.textViewAppointmentInfo);
        }
    }


}
