package com.frauddetection.service;

import com.frauddetection.model.Transaction;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class FraudDetector {
    private static final Logger LOGGER = Logger.getLogger(FraudDetector.class.getName());
    private final Map<String, Integer> locationRiskScores;

    // Thresholds for fraud detection
    private static final double HIGH_AMOUNT_THRESHOLD = 8000.0;
    private static final double SUSPICIOUS_AMOUNT_THRESHOLD = 5000.0;

    public FraudDetector() {
        LOGGER.info("Initializing Fraud Detector with risk-based approach");
        locationRiskScores = new HashMap<>();
        initializeLocationRiskScores();
    }

    private void initializeLocationRiskScores() {
        // Lower score is better (1-5 scale)
        locationRiskScores.put("New York", 2);
        locationRiskScores.put("London", 2);
        locationRiskScores.put("Tokyo", 2);
        locationRiskScores.put("Singapore", 1);
        locationRiskScores.put("Hong Kong", 3);
        locationRiskScores.put("Dubai", 4);
        locationRiskScores.put("Paris", 2);
        locationRiskScores.put("Sydney", 1);
        locationRiskScores.put("Mumbai", 4);
        locationRiskScores.put("Shanghai", 3);
    }

    public boolean isFraudulent(Transaction transaction) {
        int riskScore = 0;

        // Rule 1: Location-based risk
        int locationRisk = locationRiskScores.getOrDefault(transaction.getLocation(), 5);
        riskScore += locationRisk;

        // Rule 2: Amount-based risk
        if (transaction.getAmount() > HIGH_AMOUNT_THRESHOLD) {
            riskScore += 3;
            LOGGER.warning("High amount transaction detected: " + transaction.getAmount());
        } else if (transaction.getAmount() > SUSPICIOUS_AMOUNT_THRESHOLD) {
            riskScore += 2;
            LOGGER.info("Suspicious amount detected: " + transaction.getAmount());
        }

        // Rule 3: Transaction type risk
        if (transaction.getType().equals("Wire Transfer") && transaction.getAmount() > SUSPICIOUS_AMOUNT_THRESHOLD) {
            riskScore += 2;
            LOGGER.info("High-risk wire transfer detected");
        }

        // Calculate final fraud probability (scale of 0-1)
        double fraudProbability = riskScore / 10.0; // Max score is 10
        LOGGER.info(String.format("Transaction risk score: %d/10 (%.2f probability)", riskScore, fraudProbability));

        return fraudProbability >= 0.7; // 70% threshold for fraud
    }
}