package com.example.scratch;

public class User {
    public String userId, firstName, middleName, lastName, email, mobileNumber, username;
    public double height, weight, bmi;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String userId, String firstName, String middleName, String lastName, String email, String username, String mobileNumber, double height, double weight, double bmi) {
        this.userId = userId;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
        this.mobileNumber = mobileNumber;
        this.height = height;
        this.weight = weight;
        this.bmi = bmi;
    }
}
