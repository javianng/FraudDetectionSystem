package com.frauddetection;

import com.frauddetection.service.TransactionService;
import com.frauddetection.ui.DashboardView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class FraudDetectionApp extends Application {
    private TransactionService transactionService;
    private Timeline simulationTimeline;

    @Override
    public void start(Stage primaryStage) {
        transactionService = new TransactionService();
        DashboardView dashboard = new DashboardView(transactionService);

        // Set up simulation timeline
        simulationTimeline = new Timeline(
            new KeyFrame(Duration.seconds(2), e -> transactionService.generateTransaction())
        );
        simulationTimeline.setCycleCount(Animation.INDEFINITE);

        Scene scene = new Scene(dashboard, 1200, 800);
        primaryStage.setTitle("Fraud Detection System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() {
        if (simulationTimeline != null) {
            simulationTimeline.stop();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
} 