package com.frauddetection.service;

import com.frauddetection.model.Transaction;
import java.util.logging.Logger;

public class FraudDetector {
    private static final Logger LOGGER = Logger.getLogger(FraudDetector.class.getName());
    private final WekaFraudDetector wekaDetector;

    public FraudDetector() {
        LOGGER.info("Initializing Fraud Detector with Weka-based machine learning model");
        wekaDetector = new WekaFraudDetector();
    }

    public boolean isFraudulent(Transaction transaction) {
        double fraudProbability = wekaDetector.predictFraudProbability(transaction);
        boolean isFraud = fraudProbability >= 0.7; // Default threshold

        if (isFraud) {
            LOGGER.warning(String.format("Potential fraud detected: %s (probability: %.2f)",
                    transaction.toString(), fraudProbability));
        }

        return isFraud;
    }

    public void updateModel(Transaction transaction, boolean isActuallyFraudulent) {
        wekaDetector.updateModel(transaction, isActuallyFraudulent);
    }
}