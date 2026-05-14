package com.mobapp.inspector.ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mobapp.inspector.database.DatabaseService;
import com.mobapp.inspector.database.Scan;
import com.mobapp.inspector.service.ExportService;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javafx.stage.FileChooser;

/**
 * UI Panel for displaying scan history and managing past scans.
 */
public class ScanHistoryPanel extends VBox {
    
    private static final Logger logger = LoggerFactory.getLogger(ScanHistoryPanel.class);
    private static final DateTimeFormatter DATE_FORMAT = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private TableView<Scan> scansTable;
    private ObservableList<Scan> scansList;
    private DatabaseService dbService;
    private Button deleteButton;
    private Button viewDetailsButton;
    private Button exportButton;
    private Label statusLabel;
    
    public ScanHistoryPanel() {
        dbService = new DatabaseService();
        initializeUI();
        loadScans();
    }

    /** Refreshes the table from SQLite (e.g. after a new scan is saved). */
    public void refresh() {
        loadScans();
    }
    
    private void initializeUI() {
        setPadding(new Insets(20));
        setSpacing(15);
        setStyle("-fx-background-color: #f8f9fa;");
        
        // Header
        Label headerLabel = new Label("Scan History");
        headerLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        headerLabel.setTextFill(Color.web("#2c3e50"));
        
        // Toolbar
        HBox toolbar = createToolbar();
        
        // Table
        scansTable = createScansTable();
        
        // Status bar
        statusLabel = new Label("Loading scans...");
        statusLabel.setFont(Font.font("System", 11));
        statusLabel.setTextFill(Color.web("#7f8c8d"));
        
        getChildren().addAll(headerLabel, toolbar, scansTable, statusLabel);
        VBox.setVgrow(scansTable, javafx.scene.layout.Priority.ALWAYS);
    }
    
    private HBox createToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(10));
        toolbar.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-width: 0 0 1 0;");
        
        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> loadScans());
        refreshButton.getStyleClass().add("btn-primary");
        
        viewDetailsButton = new Button("View Details");
        viewDetailsButton.setDisable(true);
        viewDetailsButton.setOnAction(e -> viewDetails());
        viewDetailsButton.getStyleClass().add("btn-info");
        
        exportButton = new Button("Export");
        exportButton.setDisable(true);
        exportButton.setOnAction(e -> showExportMenu());
        exportButton.getStyleClass().add("btn-success");
        
        deleteButton = new Button("Delete");
        deleteButton.setDisable(true);
        deleteButton.setStyle("-fx-text-fill: white; -fx-background-color: #e74c3c;");
        deleteButton.setOnAction(e -> deleteScan());
        
        toolbar.getChildren().addAll(refreshButton, new Separator(), viewDetailsButton, 
                                     exportButton, deleteButton);
        
        return toolbar;
    }
    
    private TableView<Scan> createScansTable() {
        scansList = FXCollections.observableArrayList();
        scansTable = new TableView<>(scansList);
        scansTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        
        // ID Column
        TableColumn<Scan, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);
        
        // Name Column
        TableColumn<Scan, String> nameCol = new TableColumn<>("Scan Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(150);
        
        // Path Column
        TableColumn<Scan, String> pathCol = new TableColumn<>("Target Path");
        pathCol.setCellValueFactory(new PropertyValueFactory<>("targetPath"));
        pathCol.setPrefWidth(300);
        
        // Date Column
        TableColumn<Scan, String> dateCol = new TableColumn<>("Scan Date");
        dateCol.setCellValueFactory(cellData -> {
            if (cellData.getValue().getScanDate() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getScanDate().format(DATE_FORMAT));
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });
        dateCol.setPrefWidth(150);
        
        // Issues Column
        TableColumn<Scan, Long> issuesCol = new TableColumn<>("Total Issues");
        issuesCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getTotalIssues()));
        issuesCol.setPrefWidth(100);
        
        // Status Column
        TableColumn<Scan, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);
        
        scansTable.getColumns().addAll(idCol, nameCol, pathCol, dateCol, issuesCol, statusCol);
        
        // Selection listener
        scansTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean hasSelection = newVal != null;
            viewDetailsButton.setDisable(!hasSelection);
            exportButton.setDisable(!hasSelection);
            deleteButton.setDisable(!hasSelection);
        });
        
        return scansTable;
    }
    
    private void loadScans() {
        Thread thread = new Thread(() -> {
            try {
                List<Scan> scans = dbService.getAllScans();
                Platform.runLater(() -> {
                    scansList.clear();
                    scansList.addAll(scans);
                    statusLabel.setText("Loaded " + scans.size() + " scans");
                    logger.info("Loaded {} scans from database", scans.size());
                });
            } catch (SQLException e) {
                logger.error("Error loading scans", e);
                Platform.runLater(() -> 
                    statusLabel.setText("Error loading scans: " + e.getMessage()));
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
    
    private void viewDetails() {
        Scan selectedScan = scansTable.getSelectionModel().getSelectedItem();
        if (selectedScan == null) return;
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Scan Details - " + selectedScan.getName());
        alert.setHeaderText("Scan Information");
        
        String details = String.format(
            "ID: %d\nName: %s\nPath: %s\nDate: %s\nStatus: %s\n\n" +
            "Critical: %d\nHigh: %d\nMedium: %d\nLow: %d\nInfo: %d\n" +
            "Total Issues: %d\nTotal Files: %d",
            selectedScan.getId(),
            selectedScan.getName(),
            selectedScan.getTargetPath(),
            selectedScan.getScanDate().format(DATE_FORMAT),
            selectedScan.getStatus(),
            selectedScan.getCriticalCount(),
            selectedScan.getHighCount(),
            selectedScan.getMediumCount(),
            selectedScan.getLowCount(),
            selectedScan.getInfoCount(),
            selectedScan.getTotalIssues(),
            selectedScan.getTotalFiles()
        );
        
        alert.setContentText(details);
        alert.showAndWait();
    }
    
    private void showExportMenu() {
        if (scansTable.getSelectionModel().getSelectedItem() == null) {
            return;
        }
        
        ContextMenu contextMenu = new ContextMenu();
        
        MenuItem csvItem = new MenuItem("Export as CSV");
        csvItem.setOnAction(e -> exportSelectedToFormat("CSV"));

        MenuItem jsonItem = new MenuItem("Export as JSON");
        jsonItem.setOnAction(e -> exportSelectedToFormat("JSON"));
        
        MenuItem htmlItem = new MenuItem("Export as HTML");
        htmlItem.setOnAction(e -> exportSelectedToFormat("HTML"));

        MenuItem pdfItem = new MenuItem("Export as PDF");
        pdfItem.setOnAction(e -> exportSelectedToFormat("PDF"));

        contextMenu.getItems().addAll(csvItem, jsonItem, htmlItem, pdfItem);
        contextMenu.show(exportButton, 
            exportButton.localToScreen(exportButton.getBoundsInLocal()).getCenterX(),
            exportButton.localToScreen(exportButton.getBoundsInLocal()).getCenterY());
    }
    
    private void exportSelectedToFormat(String format) {
        Scan selectedScan = scansTable.getSelectionModel().getSelectedItem();
        if (selectedScan == null) {
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export scan as " + format);
        String ext = format.toLowerCase();
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter(format + " files", "*." + ext));
        fileChooser.setInitialFileName("scan_" + selectedScan.getId() + "." + ext);

        File file = fileChooser.showSaveDialog(getScene().getWindow());
        if (file == null) {
            return;
        }

        Thread thread = new Thread(() -> {
            try {
                Scan full = dbService.getScanById(selectedScan.getId());
                if (full == null) {
                    Platform.runLater(() ->
                        new Alert(Alert.AlertType.ERROR, "Scan not found.", ButtonType.OK).showAndWait());
                    return;
                }
                switch (format) {
                    case "CSV" -> ExportService.exportToCSV(full, file.toPath());
                    case "JSON" -> ExportService.exportToJSON(full, file.toPath());
                    case "HTML" -> ExportService.exportToHTML(full, file.toPath());
                    case "PDF" -> ExportService.exportToPDF(full, file.toPath());
                    default -> throw new IllegalStateException("Unknown format: " + format);
                }
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Export");
                    alert.setContentText("Scan exported as " + format + " to:\n" + file.getAbsolutePath());
                    alert.showAndWait();
                    logger.info("Exported scan {} as {} to {}", selectedScan.getId(), format, file);
                });
            } catch (SQLException | IOException e) {
                logger.error("Export failed", e);
                Platform.runLater(() ->
                    new Alert(Alert.AlertType.ERROR, "Export failed: " + e.getMessage(), ButtonType.OK).showAndWait());
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void deleteScan() {
        Scan selectedScan = scansTable.getSelectionModel().getSelectedItem();
        if (selectedScan == null) return;
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Scan");
        confirm.setHeaderText("Confirm Deletion");
        confirm.setContentText("Are you sure you want to delete this scan?\nThis action cannot be undone.");
        
        if (confirm.showAndWait().get() == ButtonType.OK) {
            Thread thread = new Thread(() -> {
                try {
                    dbService.deleteScan(selectedScan.getId());
                    Platform.runLater(this::loadScans);
                    logger.info("Deleted scan {}", selectedScan.getId());
                } catch (SQLException e) {
                    logger.error("Error deleting scan", e);
                }
            });
            thread.setDaemon(true);
            thread.start();
        }
    }
    
    public void dispose() {
        dbService.close();
    }
}
