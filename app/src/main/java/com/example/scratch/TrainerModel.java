package com.example.scratch;

public class TrainerModel {
    private String name, specialization;
    private int imageResId;

    public TrainerModel(String name, String specialization, int imageResId) {
        this.name = name;
        this.specialization = specialization;
        this.imageResId = imageResId;
    }

    public String getName() { return name; }
    public String getSpecialization() { return specialization; }
    public int getImageResId() { return imageResId; }
}
