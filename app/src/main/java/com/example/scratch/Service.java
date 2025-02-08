package com.example.scratch;

public class Service {
    private String name;
    private String duration;
    private String price;
    private int imageRes;

    public Service(String name, String duration, String price, int imageRes) {
        this.name = name;
        this.duration = duration;
        this.price = price;
        this.imageRes = imageRes;
    }

    public String getName() { return name; }
    public String getDuration() { return duration; }
    public String getPrice() { return price; }
    public int getImageRes() { return imageRes; }
}

