package com.frauddetection.service;

import com.frauddetection.model.Transaction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Random;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class TransactionService {
    private ObservableList<Transaction> transactions;
    private Random random;
    private boolean simulationEnabled;
    private double fraudProbability;
    private double maxTransactionAmount;
    private final String[] LOCATIONS = {
            "New York", "London", "Tokyo", "Singapore", "Hong Kong",
            "Dubai", "Paris", "Sydney", "Mumbai", "Shanghai"
    };

    public TransactionService() {
        this.transactions = FXCollections.observableArrayList();
        this.random = new Random();
        this.simulationEnabled = false;
        this.fraudProbability = 0.1;
        this.maxTransactionAmount = 10000.0;
    }

    public List<Transaction> getFilteredTransactions(LocalDate startDate, LocalDate endDate,
            String type, double fraudThreshold) {
        return transactions.stream()
                .filter(t -> {
                    boolean dateMatch = true;
                    if (startDate != null && endDate != null) {
                        dateMatch = !t.getTimestamp().toLocalDate().isBefore(startDate) &&
                                !t.getTimestamp().toLocalDate().isAfter(endDate);
                    }
                    boolean typeMatch = type.equals("All") || t.getType().equals(type);
                    boolean fraudMatch = t.getFraudProbability() >= fraudThreshold;
                    return dateMatch && typeMatch && fraudMatch;
                })
                .collect(Collectors.toList());
    }

    public void importTransactions(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    Transaction transaction = new Transaction(
                            parts[0], // id
                            Double.parseDouble(parts[1]), // amount
                            parts[2], // type
                            Double.parseDouble(parts[3]), // fraudProbability
                            parts.length > 4 ? parts[4] : LOCATIONS[random.nextInt(LOCATIONS.length)] // location
                    );
                    transactions.add(transaction);
                }
            }
        }
    }

    public void generateTransaction() {
        if (!simulationEnabled) {
            return;
        }

        String[] types = { "Credit Card", "Wire Transfer", "Cash Deposit" };
        String type = types[random.nextInt(types.length)];
        double amount = random.nextDouble() * maxTransactionAmount;
        double fraudProb = random.nextDouble();
        String location = LOCATIONS[random.nextInt(LOCATIONS.length)];

        Transaction transaction = new Transaction(
                "TX" + System.currentTimeMillis(),
                amount,
                type,
                fraudProb,
                location);

        transactions.add(transaction);
    }

    public void setSimulationEnabled(boolean enabled) {
        this.simulationEnabled = enabled;
    }

    public void setFraudProbability(double probability) {
        this.fraudProbability = Math.max(0.0, Math.min(1.0, probability));
    }

    public void setMaxTransactionAmount(double amount) {
        this.maxTransactionAmount = Math.max(0.0, amount);
    }

    public ObservableList<Transaction> getTransactions() {
        return transactions;
    }

    public void clearTransactions() {
        transactions.clear();
    }
}