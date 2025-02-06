package com.example.project;

public class Customer {
    private long id;
    private String name;
    private int score;

    public Customer(long id, String name, int score) {
        this.id = id;
        this.name = name;
        this.score = score;
    }

    // Constructor for new customers without an ID
    public Customer(String name) {
        this.name = name;
        this.score = 0;  // Default initial score
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void addScore() {
        this.score++;
    }
}
