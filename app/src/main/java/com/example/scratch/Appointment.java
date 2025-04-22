package com.example.scratch;

public class Appointment {
    private String appointmentId; // ✅ Add this
    private String date;
    private String timeSlot;
    private String status;
    private String trainerId;
    private String userId;
    private String userFullName;

    // Default constructor required for Firebase
    public Appointment() {}

    // Constructor
    public Appointment(String date, String timeSlot, String status, String trainerId, String userId) {
        this.date = date;
        this.timeSlot = timeSlot;
        this.status = status;
        this.trainerId = trainerId;
        this.userId = userId;
    }

    // Getters
    public String getAppointmentId() {
        return appointmentId;
    }

    public String getDate() {
        return date;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public String getStatus() {
        return status;
    }

    public String getTrainerId() {
        return trainerId;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserFullName() {
        return userFullName;
    }

    // Setters
    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTrainerId(String trainerId) {
        this.trainerId = trainerId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }
}
