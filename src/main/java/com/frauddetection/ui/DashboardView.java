package com.frauddetection.ui;

import javafx.geometry.Insets;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import java.io.File;
import java.time.LocalDate;
import com.frauddetection.model.Transaction;
import com.frauddetection.service.TransactionService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class DashboardView extends BorderPane {
    private final TransactionService transactionService;
    private TableView<Transaction> transactionTable;
    private LineChart<Number, Number> transactionChart;
    private PieChart fraudDistributionChart;
    private Spinner<Double> thresholdSpinner;
    private DatePicker startDate;
    private DatePicker endDate;
    private ComboBox<String> transactionTypeFilter;
    private Timeline simulationTimeline;
    private Button importButton;
    private Button generateButton;
    private ToggleButton simulationToggle;

    public DashboardView(TransactionService transactionService) {
        this.transactionService = transactionService;
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
        setupSimulationTimeline();
    }

    private void setupSimulationTimeline() {
        simulationTimeline = new Timeline(
                new KeyFrame(Duration.seconds(2), e -> {
                    transactionService.generateTransaction();
                    updateDashboard();
                }));
        simulationTimeline.setCycleCount(Animation.INDEFINITE);
    }

    private void initializeComponents() {
        // Transaction Table
        transactionTable = new TableView<>();
        TableColumn<Transaction, String> idCol = new TableColumn<>("ID");
        TableColumn<Transaction, Double> amountCol = new TableColumn<>("Amount");
        TableColumn<Transaction, String> typeCol = new TableColumn<>("Type");
        TableColumn<Transaction, String> locationCol = new TableColumn<>("Location");
        TableColumn<Transaction, Boolean> fraudCol = new TableColumn<>("Fraud");
        TableColumn<Transaction, String> timestampCol = new TableColumn<>("Date/Time");

        idCol.setCellValueFactory(data -> javafx.beans.binding.Bindings.createStringBinding(
                () -> data.getValue().getId()));
        amountCol.setCellValueFactory(data -> javafx.beans.binding.Bindings.createObjectBinding(
                () -> data.getValue().getAmount()));
        typeCol.setCellValueFactory(data -> javafx.beans.binding.Bindings.createStringBinding(
                () -> data.getValue().getType()));
        locationCol.setCellValueFactory(data -> javafx.beans.binding.Bindings.createStringBinding(
                () -> data.getValue().getLocation()));
        fraudCol.setCellValueFactory(data -> javafx.beans.binding.Bindings.createObjectBinding(
                () -> data.getValue().isFraudulent()));
        timestampCol.setCellValueFactory(data -> javafx.beans.binding.Bindings.createStringBinding(
                () -> data.getValue().getTimestamp()
                        .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));

        transactionTable.getColumns().addAll(idCol, timestampCol, amountCol, typeCol, locationCol, fraudCol);

        // Charts
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        transactionChart = new LineChart<>(xAxis, yAxis);
        transactionChart.setTitle("Transaction Patterns");

        fraudDistributionChart = new PieChart();
        fraudDistributionChart.setTitle("Fraud Distribution");

        // Filters and Controls
        startDate = new DatePicker(LocalDate.now().minusMonths(1));
        endDate = new DatePicker(LocalDate.now());

        transactionTypeFilter = new ComboBox<>();
        transactionTypeFilter.setItems(FXCollections.observableArrayList(
                "All", "Credit Card", "Wire Transfer", "Cash Deposit"));
        transactionTypeFilter.setValue("All");

        thresholdSpinner = new Spinner<>(0.0, 1.0, 0.5, 0.1);
        thresholdSpinner.setEditable(true);

        // Buttons
        importButton = new Button("Import Data");
        generateButton = new Button("Generate Transaction");
        simulationToggle = new ToggleButton("Enable Simulation");
    }

    private void layoutComponents() {
        // Top Controls
        HBox filterControls = new HBox(10);
        filterControls.setPadding(new Insets(10));
        filterControls.getChildren().addAll(
                new Label("Start Date:"), startDate,
                new Label("End Date:"), endDate,
                new Label("Type:"), transactionTypeFilter,
                new Label("Fraud Threshold:"), thresholdSpinner);

        // Center Content
        GridPane centerContent = new GridPane();
        centerContent.setHgap(10);
        centerContent.setVgap(10);
        centerContent.setPadding(new Insets(10));

        // Add transaction table
        VBox tableBox = new VBox(5);
        tableBox.getChildren().addAll(new Label("Transactions"), transactionTable);
        GridPane.setConstraints(tableBox, 0, 0);

        // Add charts
        VBox chartsBox = new VBox(10);
        chartsBox.getChildren().addAll(transactionChart, fraudDistributionChart);
        GridPane.setConstraints(chartsBox, 1, 0);

        centerContent.getChildren().addAll(tableBox, chartsBox);

        // Bottom Controls - Simulation
        HBox simulationControls = new HBox(10);
        simulationControls.setPadding(new Insets(10));
        simulationControls.getChildren().addAll(
                importButton, generateButton, simulationToggle);

        // Layout assembly
        setTop(filterControls);
        setCenter(centerContent);
        setBottom(simulationControls);
    }

    private void setupEventHandlers() {
        // Filter change handlers
        startDate.setOnAction(e -> updateDashboard());
        endDate.setOnAction(e -> updateDashboard());
        transactionTypeFilter.setOnAction(e -> updateDashboard());
        thresholdSpinner.valueProperty().addListener((obs, oldVal, newVal) -> updateDashboard());

        // Button handlers
        importButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Import Transaction Data");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

            File file = fileChooser.showOpenDialog(getScene().getWindow());
            if (file != null) {
                try {
                    transactionService.importTransactions(file);
                    updateDashboard();
                } catch (Exception ex) {
                    showError("Error importing file", ex.getMessage());
                }
            }
        });

        generateButton.setOnAction(e -> {
            transactionService.generateTransaction();
            updateDashboard();
        });

        simulationToggle.setOnAction(e -> {
            boolean isSelected = simulationToggle.isSelected();
            transactionService.setSimulationEnabled(isSelected);
            if (isSelected) {
                simulationTimeline.play();
            } else {
                simulationTimeline.stop();
            }
        });
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void updateDashboard() {
        // Update transaction table
        ObservableList<Transaction> filteredTransactions = FXCollections.observableArrayList(
                transactionService.getFilteredTransactions(
                        startDate.getValue(),
                        endDate.getValue(),
                        transactionTypeFilter.getValue(),
                        thresholdSpinner.getValue()));
        transactionTable.setItems(filteredTransactions);

        // Update charts
        updateTransactionChart(filteredTransactions);
        updateFraudDistributionChart(filteredTransactions);
    }

    private void updateTransactionChart(ObservableList<Transaction> transactions) {
        transactionChart.getData().clear();
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Transaction Amount Over Time");

        // Add data points
        for (int i = 0; i < transactions.size(); i++) {
            series.getData().add(new XYChart.Data<>(i, transactions.get(i).getAmount()));
        }

        transactionChart.getData().add(series);
    }

    private void updateFraudDistributionChart(ObservableList<Transaction> transactions) {
        fraudDistributionChart.getData().clear();

        long fraudCount = transactions.stream().filter(Transaction::isFraudulent).count();
        long legitimateCount = transactions.size() - fraudCount;

        PieChart.Data fraudData = new PieChart.Data("Fraudulent", fraudCount);
        PieChart.Data legitimateData = new PieChart.Data("Legitimate", legitimateCount);

        fraudDistributionChart.getData().addAll(fraudData, legitimateData);
    }

    public void refreshData() {
        updateDashboard();
    }
}