package com.mobapp.inspector.ui;

import com.mobapp.inspector.core.MobileSecurityScanner;
import com.mobapp.inspector.core.MobileSecurityScanner.SecurityFinding;
import com.mobapp.inspector.core.MobileSecurityScanner.Severity;
import com.mobapp.inspector.core.MobileSecurityScanner.ScanProgress;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.chart.PieChart;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;

/**
 * SecurityScanPanel - UI component for displaying mobile security scan results.
 */
public class SecurityScanPanel extends VBox {
    
    private MobileSecurityScanner scanner;
    private TableView<SecurityFinding> findingsTable;
    private ObservableList<SecurityFinding> findingsList;
    private Label statusLabel;
    private ProgressBar progressBar;
    private Label summaryLabel;
    private ComboBox<Severity> severityFilter;
    private TextField searchField;
    private Button scanButton;
    private Button exportButton;
    private Button clearButton;
    private File currentDirectory;
    private VBox progressPanel;
    private PieChart findingsChart;
    
    public SecurityScanPanel() {
        scanner = new MobileSecurityScanner();
        scanner.setProgressCallback(this::onScanProgress);
        initializeUI();
    }
    
    /**
     * Sets the current directory for the security scanner.
     * This is called when a file is selected in the main file browser.
     */
    public void setCurrentDirectory(File directory) {
        this.currentDirectory = directory;
    }
    
    private void initializeUI() {
        setPadding(new Insets(20));
        setSpacing(15);
        setStyle("-fx-background-color: #f8f9fa;");
        
        // Header
        Label headerLabel = new Label("Secure Storage Inspector");
        headerLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        headerLabel.setTextFill(Color.web("#2c3e50"));
        
        Label descriptionLabel = new Label("Scan files for security vulnerabilities, API keys, and sensitive data");
        descriptionLabel.setFont(Font.font("System", 14));
        descriptionLabel.setTextFill(Color.web("#7f8c8d"));
        
        // Control Panel
        HBox controlPanel = createControlPanel();
        
        // Progress Panel
        progressPanel = createProgressPanel();
        
        // Summary Panel
        HBox summaryPanel = createSummaryPanel();
        
        // Filter Panel
        HBox filterPanel = createFilterPanel();
        
        // Findings Table
        findingsTable = createFindingsTable();
        findingsList = FXCollections.observableArrayList();
        findingsTable.setItems(findingsList);
        
        // Create Pie Chart
        findingsChart = createFindingsChart();
        
        // Create split pane with chart and table
        SplitPane resultsSplitPane = new SplitPane();
        resultsSplitPane.setDividerPosition(0, 0.3);
        
        // Chart container
        VBox chartContainer = new VBox(10);
        chartContainer.setPadding(new Insets(10));
        chartContainer.getChildren().add(findingsChart);
        VBox.setVgrow(findingsChart, Priority.ALWAYS);
        
        // Table container
        VBox tableContainer = new VBox(10);
        tableContainer.getChildren().add(findingsTable);
        VBox.setVgrow(findingsTable, Priority.ALWAYS);
        
        resultsSplitPane.getItems().addAll(chartContainer, tableContainer);
        
        // Add all components
        getChildren().addAll(
            headerLabel,
            descriptionLabel,
            new Separator(),
            controlPanel,
            progressPanel,
            summaryPanel,
            new Separator(),
            filterPanel,
            resultsSplitPane
        );
        
        VBox.setVgrow(resultsSplitPane, Priority.ALWAYS);
    }
    
    private HBox createControlPanel() {
        HBox panel = new HBox(10);
        panel.setAlignment(Pos.CENTER_LEFT);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 5;");
        
        scanButton = new Button("Scan Directory");
        scanButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");
        scanButton.setOnAction(e -> scanDirectory());
        
        Button scanCurrentFileButton = new Button("Scan Current File");
        scanCurrentFileButton.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");
        scanCurrentFileButton.setOnAction(e -> scanCurrentFile());
        
        exportButton = new Button("Export Results");
        exportButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");
        exportButton.setOnAction(e -> exportResults());
        exportButton.setDisable(true);
        
        clearButton = new Button("Clear Results");
        clearButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");
        clearButton.setOnAction(e -> clearResults());
        clearButton.setDisable(true);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        panel.getChildren().addAll(scanButton, scanCurrentFileButton, spacer, exportButton, clearButton);
        
        return panel;
    }
    
    private VBox createProgressPanel() {
        VBox panel = new VBox(5);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 5;");
        panel.setVisible(false);
        
        progressBar = new ProgressBar();
        progressBar.setPrefWidth(Double.MAX_VALUE);
        
        statusLabel = new Label("Ready to scan");
        statusLabel.setFont(Font.font("System", 12));
        
        panel.getChildren().addAll(progressBar, statusLabel);
        
        return panel;
    }
    
    private HBox createSummaryPanel() {
        HBox panel = new HBox(15);
        panel.setAlignment(Pos.CENTER_LEFT);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #ecf0f1; -fx-border-color: #bdc3c7; -fx-border-radius: 5;");
        
        summaryLabel = new Label("No scan performed yet");
        summaryLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        panel.getChildren().addAll(summaryLabel, spacer);
        
        return panel;
    }
    
    private HBox createFilterPanel() {
        HBox panel = new HBox(10);
        panel.setAlignment(Pos.CENTER_LEFT);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 5;");
        
        Label filterLabel = new Label("Filters:");
        filterLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        
        severityFilter = new ComboBox<>();
        severityFilter.getItems().addAll(null, Severity.CRITICAL, Severity.HIGH, Severity.MEDIUM, Severity.LOW, Severity.INFO);
        severityFilter.setPromptText("All Severities");
        severityFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterFindings());
        
        searchField = new TextField();
        searchField.setPromptText("Search findings...");
        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterFindings());
        
        Button resetFilterButton = new Button("Reset");
        resetFilterButton.setOnAction(e -> {
            severityFilter.setValue(null);
            searchField.clear();
        });
        
        panel.getChildren().addAll(filterLabel, severityFilter, searchField, resetFilterButton);
        
        return panel;
    }
    
    @SuppressWarnings("unchecked")
    private TableView<SecurityFinding> createFindingsTable() {
        TableView<SecurityFinding> table = new TableView<>();
        table.setPlaceholder(new Label("No security findings to display"));
        table.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 5;");
        
        // Severity Column
        TableColumn<SecurityFinding, String> severityCol = new TableColumn<>("Severity");
        severityCol.setPrefWidth(100);
        severityCol.setCellValueFactory(param -> {
            Severity severity = param.getValue().getSeverity();
            String color = getSeverityColor(severity);
            return new SimpleStringProperty(color + severity.name());
        });
        
        // Type Column
        TableColumn<SecurityFinding, String> typeCol = new TableColumn<>("Type");
        typeCol.setPrefWidth(200);
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        
        // Description Column
        TableColumn<SecurityFinding, String> descCol = new TableColumn<>("Description");
        descCol.setPrefWidth(400);
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        
        // File Column
        TableColumn<SecurityFinding, String> fileCol = new TableColumn<>("File");
        fileCol.setPrefWidth(300);
        fileCol.setCellValueFactory(param -> {
            String path = param.getValue().getFilePath();
            return new SimpleStringProperty(new File(path).getName());
        });
        
        // Line Column
        TableColumn<SecurityFinding, Integer> lineCol = new TableColumn<>("Line");
        lineCol.setPrefWidth(60);
        lineCol.setCellValueFactory(new PropertyValueFactory<>("lineNumber"));
        
        table.getColumns().addAll(severityCol, typeCol, descCol, fileCol, lineCol);
        
        // Double-click to open file
        table.setRowFactory(tv -> {
            TableRow<SecurityFinding> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    SecurityFinding finding = row.getItem();
                    openFileInSystem(finding.getFilePath());
                }
            });
            return row;
        });
        
        return table;
    }
    
    private String getSeverityColor(Severity severity) {
        switch (severity) {
            case CRITICAL: return "🔴 ";
            case HIGH: return "🟠 ";
            case MEDIUM: return "🟡 ";
            case LOW: return "🔵 ";
            case INFO: return "⚪ ";
            default: return "⚪ ";
        }
    }
    
    private PieChart createFindingsChart() {
        PieChart chart = new PieChart();
        chart.setTitle("Security Findings by Severity");
        chart.setLegendVisible(true);
        chart.setLabelsVisible(true);
        chart.setStartAngle(90);
        chart.setMinSize(200, 200);
        chart.setPrefSize(300, 300);
        
        // Add placeholder data
        chart.getData().add(new PieChart.Data("No Data", 1));
        
        return chart;
    }
    
    private void updateChart(List<SecurityFinding> findings) {
        findingsChart.getData().clear();
        
        if (findings.isEmpty()) {
            findingsChart.getData().add(new PieChart.Data("No Findings", 1));
            return;
        }
        
        // Count findings by severity
        Map<Severity, Long> counts = new HashMap<>();
        for (Severity severity : Severity.values()) {
            counts.put(severity, findings.stream().filter(f -> f.getSeverity() == severity).count());
        }
        
        // Add pie chart data for non-zero counts
        if (counts.get(Severity.CRITICAL) > 0) {
            PieChart.Data slice = new PieChart.Data("Critical (" + counts.get(Severity.CRITICAL) + ")", counts.get(Severity.CRITICAL));
            findingsChart.getData().add(slice);
        }
        if (counts.get(Severity.HIGH) > 0) {
            PieChart.Data slice = new PieChart.Data("High (" + counts.get(Severity.HIGH) + ")", counts.get(Severity.HIGH));
            findingsChart.getData().add(slice);
        }
        if (counts.get(Severity.MEDIUM) > 0) {
            PieChart.Data slice = new PieChart.Data("Medium (" + counts.get(Severity.MEDIUM) + ")", counts.get(Severity.MEDIUM));
            findingsChart.getData().add(slice);
        }
        if (counts.get(Severity.LOW) > 0) {
            PieChart.Data slice = new PieChart.Data("Low (" + counts.get(Severity.LOW) + ")", counts.get(Severity.LOW));
            findingsChart.getData().add(slice);
        }
        if (counts.get(Severity.INFO) > 0) {
            PieChart.Data slice = new PieChart.Data("Info (" + counts.get(Severity.INFO) + ")", counts.get(Severity.INFO));
            findingsChart.getData().add(slice);
        }
    }
    
    private void scanDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Directory to Scan");
        
        if (currentDirectory != null && currentDirectory.exists()) {
            directoryChooser.setInitialDirectory(currentDirectory);
        }
        
        File selectedDirectory = directoryChooser.showDialog(getScene().getWindow());
        if (selectedDirectory != null) {
            currentDirectory = selectedDirectory;
            performScan(selectedDirectory);
        }
    }
    
    private void scanCurrentFile() {
        // This would need to be connected to the currently selected file in the main app
        // For now, we'll show a file chooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File to Scan");
        
        if (currentDirectory != null && currentDirectory.exists()) {
            fileChooser.setInitialDirectory(currentDirectory);
        }
        
        File selectedFile = fileChooser.showOpenDialog(getScene().getWindow());
        if (selectedFile != null) {
            currentDirectory = selectedFile.getParentFile();
            performFileScan(selectedFile);
        }
    }
    
    private void performScan(File directory) {
        clearResults();
        showProgress(true);
        
        Thread scanThread = new Thread(() -> {
            List<SecurityFinding> findings = scanner.scanDirectory(directory);
            Platform.runLater(() -> {
                findingsList.addAll(findings);
                updateSummary(findings);
                showProgress(false);
                exportButton.setDisable(false);
                clearButton.setDisable(false);
            });
        });
        
        scanThread.setDaemon(true);
        scanThread.start();
    }
    
    private void performFileScan(File file) {
        clearResults();
        showProgress(true);
        
        Thread scanThread = new Thread(() -> {
            List<SecurityFinding> findings = new ArrayList<>();
            scanner.scanFile(file, findings);
            Platform.runLater(() -> {
                findingsList.addAll(findings);
                updateSummary(findings);
                showProgress(false);
                exportButton.setDisable(false);
                clearButton.setDisable(false);
            });
        });
        
        scanThread.setDaemon(true);
        scanThread.start();
    }
    
    private void onScanProgress(ScanProgress progress) {
        Platform.runLater(() -> {
            progressBar.setProgress(progress.getProgress());
            statusLabel.setText(progress.getMessage());
        });
    }
    
    private void showProgress(boolean show) {
        progressPanel.setVisible(show);
        scanButton.setDisable(show);
        
        if (show) {
            progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        }
    }
    
    private void updateSummary(List<SecurityFinding> findings) {
        Map<Severity, Long> counts = new HashMap<>();
        for (Severity severity : Severity.values()) {
            counts.put(severity, findings.stream().filter(f -> f.getSeverity() == severity).count());
        }
        
        // Update the pie chart
        updateChart(findings);
        
        StringBuilder summary = new StringBuilder();
        summary.append("Scan Complete: ").append(findings.size()).append(" findings - ");
        
        if (counts.get(Severity.CRITICAL) > 0) {
            summary.append(counts.get(Severity.CRITICAL)).append(" critical, ");
        }
        if (counts.get(Severity.HIGH) > 0) {
            summary.append(counts.get(Severity.HIGH)).append(" high, ");
        }
        if (counts.get(Severity.MEDIUM) > 0) {
            summary.append(counts.get(Severity.MEDIUM)).append(" medium, ");
        }
        if (counts.get(Severity.LOW) > 0) {
            summary.append(counts.get(Severity.LOW)).append(" low, ");
        }
        if (counts.get(Severity.INFO) > 0) {
            summary.append(counts.get(Severity.INFO)).append(" info");
        }
        
        summaryLabel.setText(summary.toString());
    }
    
    private void filterFindings() {
        Severity selectedSeverity = severityFilter.getValue();
        String searchText = searchField.getText().toLowerCase();
        
        ObservableList<SecurityFinding> filteredList = FXCollections.observableArrayList();
        
        for (SecurityFinding finding : findingsList) {
            boolean matchesSeverity = selectedSeverity == null || finding.getSeverity() == selectedSeverity;
            boolean matchesSearch = searchText.isEmpty() || 
                finding.getType().toLowerCase().contains(searchText) ||
                finding.getDescription().toLowerCase().contains(searchText) ||
                finding.getFilePath().toLowerCase().contains(searchText);
            
            if (matchesSeverity && matchesSearch) {
                filteredList.add(finding);
            }
        }
        
        findingsTable.setItems(filteredList);
    }
    
    private void clearResults() {
        findingsList.clear();
        summaryLabel.setText("No scan performed yet");
        exportButton.setDisable(true);
        clearButton.setDisable(true);
        severityFilter.setValue(null);
        searchField.clear();
    }
    
    private void exportResults() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Scan Results");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
            new FileChooser.ExtensionFilter("JSON Files", "*.json"),
            new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );
        
        File selectedFile = fileChooser.showSaveDialog(getScene().getWindow());
        if (selectedFile != null) {
            exportToFile(selectedFile);
        }
    }
    
    private void exportToFile(File file) {
        try {
            String extension = file.getName().substring(file.getName().lastIndexOf('.') + 1).toLowerCase();
            
            switch (extension) {
                case "csv":
                    exportToCSV(file);
                    break;
                case "json":
                    exportToJSON(file);
                    break;
                default:
                    exportToText(file);
                    break;
            }
            
            showSuccessMessage("Results exported successfully to: " + file.getName());
        } catch (IOException e) {
            showErrorMessage("Failed to export results: " + e.getMessage());
        }
    }
    
    private void exportToCSV(File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("Severity,Type,Description,File,Line\n");
            
            for (SecurityFinding finding : findingsList) {
                writer.write(String.format("%s,%s,\"%s\",%s,%d\n",
                    finding.getSeverity(),
                    finding.getType(),
                    finding.getDescription().replace("\"", "\"\""),
                    finding.getFilePath(),
                    finding.getLineNumber()
                ));
            }
        }
    }
    
    private void exportToJSON(File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("[\n");
            
            for (int i = 0; i < findingsList.size(); i++) {
                SecurityFinding finding = findingsList.get(i);
                writer.write(String.format("  {\n" +
                    "    \"severity\": \"%s\",\n" +
                    "    \"type\": \"%s\",\n" +
                    "    \"description\": \"%s\",\n" +
                    "    \"filePath\": \"%s\",\n" +
                    "    \"lineNumber\": %d\n" +
                    "  }%s\n",
                    finding.getSeverity(),
                    finding.getType(),
                    finding.getDescription().replace("\"", "\\\""),
                    finding.getFilePath(),
                    finding.getLineNumber(),
                    i < findingsList.size() - 1 ? "," : ""
                ));
            }
            
            writer.write("]\n");
        }
    }
    
    private void exportToText(File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            writer.write("Mobile Security Scan Report\n");
            writer.write("Generated: " + sdf.format(new Date()) + "\n");
            writer.write("Total Findings: " + findingsList.size() + "\n");
            writer.write("=" + "=".repeat(50) + "\n\n");
            
            for (SecurityFinding finding : findingsList) {
                writer.write(String.format("[%s] %s\n", finding.getSeverity(), finding.getType()));
                writer.write("Description: " + finding.getDescription() + "\n");
                writer.write("File: " + finding.getFilePath() + "\n");
                if (finding.getLineNumber() > 0) {
                    writer.write("Line: " + finding.getLineNumber() + "\n");
                }
                writer.write("-".repeat(50) + "\n\n");
            }
        }
    }
    
    private void openFileInSystem(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                java.awt.Desktop.getDesktop().open(file);
            }
        } catch (Exception e) {
            showErrorMessage("Could not open file: " + e.getMessage());
        }
    }
    
    private void showSuccessMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showErrorMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
