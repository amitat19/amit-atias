package com.example.project;

public class Appointment {
    private String customerName;
    private String barberName;
    private String date;
    private String time;

    public Appointment(String customerName, String barberName, String date, String time) {
        this.customerName = customerName;
        this.barberName = barberName;
        this.date = date;
        this.time = time;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getBarberName() {
        return barberName;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }
} 