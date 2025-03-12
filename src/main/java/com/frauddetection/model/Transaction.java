package com.frauddetection.model;

import java.time.LocalDateTime;

public class Transaction {
    private String id;
    private double amount;
    private String type;
    private LocalDateTime timestamp;
    private double fraudProbability;
    private String location;

    public Transaction(String id, double amount, String type, double fraudProbability, String location) {
        this.id = id;
        this.amount = amount;
        this.type = type;
        this.timestamp = LocalDateTime.now();
        this.fraudProbability = fraudProbability;
        this.location = location;
    }

    public String getId() {
        return id;
    }

    public double getAmount() {
        return amount;
    }

    public String getType() {
        return type;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public double getFraudProbability() {
        return fraudProbability;
    }

    public String getLocation() {
        return location;
    }

    public boolean isFraudulent() {
        return fraudProbability >= 0.7; // Default threshold
    }

    @Override
    public String toString() {
        return String.format("Transaction{id='%s', amount=%.2f, type='%s', location='%s', fraudProbability=%.2f}",
                id, amount, type, location, fraudProbability);
    }
}