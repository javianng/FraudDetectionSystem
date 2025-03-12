package com.frauddetection.service;

import com.frauddetection.model.Transaction;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;
import java.util.logging.Level;

public class TransactionGenerator implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(TransactionGenerator.class.getName());
    private final BlockingQueue<Transaction> transactionQueue;
    private final Random random = new Random();
    private volatile boolean running = true;

    // Sample data for simulation
    private static final String[] TYPES = {"Credit Card", "Wire Transfer", "Cash Deposit"};
    private static final String[] LOCATIONS = {
        "New York", "London", "Tokyo", "Singapore", "Hong Kong",
        "Dubai", "Paris", "Sydney", "Mumbai", "Shanghai"
    };

    public TransactionGenerator() {
        this.transactionQueue = new LinkedBlockingQueue<>(1000);
    }

    @Override
    public void run() {
        while (running) {
            try {
                Transaction transaction = generateRandomTransaction();
                transactionQueue.put(transaction);
                LOGGER.info("Generated transaction: " + transaction);
                Thread.sleep(random.nextInt(1000)); // Random delay between 0-1000ms
            } catch (InterruptedException e) {
                LOGGER.log(Level.WARNING, "Transaction generation interrupted", e);
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private Transaction generateRandomTransaction() {
        double amount = 10 + random.nextDouble() * 990; // Random amount between 10 and 1000
        String type = TYPES[random.nextInt(TYPES.length)];
        String location = LOCATIONS[random.nextInt(LOCATIONS.length)];
        double fraudProb = random.nextDouble();

        return new Transaction(
            "TX" + System.currentTimeMillis(),
            amount,
            type,
            fraudProb,
            location
        );
    }

    public Transaction getNextTransaction() throws InterruptedException {
        return transactionQueue.take();
    }

    public void stop() {
        running = false;
    }
} 