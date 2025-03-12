package com.frauddetection.service;

import com.frauddetection.model.Transaction;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;

public class WekaFraudDetector {
    private static final Logger LOGGER = Logger.getLogger(WekaFraudDetector.class.getName());
    private RandomForest classifier;
    private Instances dataStructure;

    public WekaFraudDetector() {
        initializeModel();
    }

    private void initializeModel() {
        try {
            // Create attributes for the model
            ArrayList<Attribute> attributes = new ArrayList<>();

            // Numeric attributes
            attributes.add(new Attribute("amount"));

            // Nominal attribute for transaction type
            ArrayList<String> typeValues = new ArrayList<>();
            typeValues.add("Credit Card");
            typeValues.add("Wire Transfer");
            typeValues.add("Cash Deposit");
            attributes.add(new Attribute("type", typeValues));

            // Nominal attribute for location
            ArrayList<String> locationValues = new ArrayList<>();
            locationValues.add("New York");
            locationValues.add("London");
            locationValues.add("Tokyo");
            locationValues.add("Singapore");
            locationValues.add("Hong Kong");
            locationValues.add("Dubai");
            locationValues.add("Paris");
            locationValues.add("Sydney");
            locationValues.add("Mumbai");
            locationValues.add("Shanghai");
            attributes.add(new Attribute("location", locationValues));

            // Class attribute (fraudulent or legitimate)
            ArrayList<String> classValues = new ArrayList<>();
            classValues.add("legitimate");
            classValues.add("fraudulent");
            attributes.add(new Attribute("class", classValues));

            // Create dataset structure
            dataStructure = new Instances("FraudDetection", attributes, 0);
            dataStructure.setClassIndex(dataStructure.numAttributes() - 1);

            // Initialize and configure the Random Forest classifier
            classifier = new RandomForest();
            String[] options = new String[] {
                    "-I", "100", // number of trees
                    "-K", "3", // number of features
                    "-S", "42" // random seed
            };
            classifier.setOptions(options);

            // Train the model with some initial data
            trainInitialModel();

            LOGGER.info("Weka Random Forest model initialized successfully");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing Weka model", e);
        }
    }

    private void trainInitialModel() throws Exception {
        // Create some sample training data
        Instances trainingData = new Instances(dataStructure);

        // Add some legitimate transactions
        addTrainingInstance(trainingData, 100.0, "Credit Card", "New York", "legitimate");
        addTrainingInstance(trainingData, 500.0, "Wire Transfer", "London", "legitimate");
        addTrainingInstance(trainingData, 1000.0, "Cash Deposit", "Tokyo", "legitimate");

        // Add some fraudulent transactions
        addTrainingInstance(trainingData, 9000.0, "Wire Transfer", "Dubai", "fraudulent");
        addTrainingInstance(trainingData, 8500.0, "Credit Card", "Mumbai", "fraudulent");
        addTrainingInstance(trainingData, 7500.0, "Wire Transfer", "Shanghai", "fraudulent");

        // Train the classifier
        classifier.buildClassifier(trainingData);
    }

    private void addTrainingInstance(Instances data, double amount, String type, String location, String classValue) {
        double[] values = new double[data.numAttributes()];
        values[0] = amount;
        values[1] = data.attribute(1).indexOfValue(type);
        values[2] = data.attribute(2).indexOfValue(location);
        values[3] = data.attribute(3).indexOfValue(classValue);
        data.add(new DenseInstance(1.0, values));
    }

    public double predictFraudProbability(Transaction transaction) {
        try {
            // Create instance for prediction
            double[] values = new double[dataStructure.numAttributes()];
            values[0] = transaction.getAmount();
            values[1] = dataStructure.attribute(1).indexOfValue(transaction.getType());
            values[2] = dataStructure.attribute(2).indexOfValue(transaction.getLocation());

            DenseInstance instance = new DenseInstance(1.0, values);
            instance.setDataset(dataStructure);

            // Get probability distribution
            double[] distribution = classifier.distributionForInstance(instance);

            // Return probability of fraud (index 1 is "fraudulent" class)
            return distribution[1];
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error predicting fraud probability", e);
            // Fallback to a conservative estimate
            return 0.5;
        }
    }

    public void updateModel(Transaction transaction, boolean isActuallyFraudulent) {
        try {
            // Create instance for updating the model
            double[] values = new double[dataStructure.numAttributes()];
            values[0] = transaction.getAmount();
            values[1] = dataStructure.attribute(1).indexOfValue(transaction.getType());
            values[2] = dataStructure.attribute(2).indexOfValue(transaction.getLocation());
            values[3] = dataStructure.attribute(3).indexOfValue(isActuallyFraudulent ? "fraudulent" : "legitimate");

            Instances updateData = new Instances(dataStructure);
            updateData.add(new DenseInstance(1.0, values));

            // Update the classifier with the new instance
            classifier.buildClassifier(updateData);

            LOGGER.info("Model updated with new transaction data");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error updating model", e);
        }
    }
}