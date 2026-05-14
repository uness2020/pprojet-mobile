package com.mobapp.inspector.api;

import com.mobapp.inspector.database.DatabaseService;
import com.mobapp.inspector.database.Scan;
import com.mobapp.inspector.service.ExportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for scan operations.
 */
@RestController
@RequestMapping("/api/scans")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ScanController {
    
    private static final Logger logger = LoggerFactory.getLogger(ScanController.class);
    private final DatabaseService dbService;
    
    public ScanController(DatabaseService dbService) {
        this.dbService = dbService;
    }
    
    /**
     * GET /api/scans - Get all scans
     */
    @GetMapping
    public ResponseEntity<List<Scan>> getAllScans() {
        try {
            List<Scan> scans = dbService.getAllScans();
            return ResponseEntity.ok(scans);
        } catch (SQLException e) {
            logger.error("Error retrieving scans", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * GET /api/scans/{id} - Get scan by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Scan> getScanById(@PathVariable long id) {
        try {
            Scan scan = dbService.getScanById(id);
            if (scan == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(scan);
        } catch (SQLException e) {
            logger.error("Error retrieving scan", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * POST /api/scans - Create a new scan
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createScan(@RequestBody ScanRequest request) {
        try {
            Scan scan = new Scan(request.name, request.targetPath);
            long scanId = dbService.saveScan(scan);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", scanId);
            response.put("message", "Scan created successfully");
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (SQLException e) {
            logger.error("Error creating scan", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * DELETE /api/scans/{id} - Delete a scan
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteScan(@PathVariable long id) {
        try {
            dbService.deleteScan(id);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Scan deleted successfully");
            
            return ResponseEntity.ok(response);
        } catch (SQLException e) {
            logger.error("Error deleting scan", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * GET /api/scans/{id}/export/csv - Export scan as CSV
     */
    @GetMapping("/{id}/export/csv")
    public ResponseEntity<Map<String, String>> exportCSV(@PathVariable long id) {
        try {
            Scan scan = dbService.getScanById(id);
            if (scan == null) {
                return ResponseEntity.notFound().build();
            }
            
            String filepath = ExportService.exportToCSV(scan);
            Map<String, String> response = new HashMap<>();
            response.put("filepath", filepath);
            response.put("message", "CSV export completed");
            
            return ResponseEntity.ok(response);
        } catch (SQLException | IOException e) {
            logger.error("Error exporting CSV", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * GET /api/scans/{id}/export/json - Export scan as JSON
     */
    @GetMapping("/{id}/export/json")
    public ResponseEntity<Map<String, String>> exportJSON(@PathVariable long id) {
        try {
            Scan scan = dbService.getScanById(id);
            if (scan == null) {
                return ResponseEntity.notFound().build();
            }
            
            String filepath = ExportService.exportToJSON(scan);
            Map<String, String> response = new HashMap<>();
            response.put("filepath", filepath);
            response.put("message", "JSON export completed");
            
            return ResponseEntity.ok(response);
        } catch (SQLException | IOException e) {
            logger.error("Error exporting JSON", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * GET /api/scans/{id}/export/html - Export scan as HTML
     */
    @GetMapping("/{id}/export/html")
    public ResponseEntity<Map<String, String>> exportHTML(@PathVariable long id) {
        try {
            Scan scan = dbService.getScanById(id);
            if (scan == null) {
                return ResponseEntity.notFound().build();
            }
            
            String filepath = ExportService.exportToHTML(scan);
            Map<String, String> response = new HashMap<>();
            response.put("filepath", filepath);
            response.put("message", "HTML export completed");
            
            return ResponseEntity.ok(response);
        } catch (SQLException | IOException e) {
            logger.error("Error exporting HTML", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/scans/{id}/export/pdf - Export scan as PDF
     */
    @GetMapping("/{id}/export/pdf")
    public ResponseEntity<Map<String, String>> exportPDF(@PathVariable long id) {
        try {
            Scan scan = dbService.getScanById(id);
            if (scan == null) {
                return ResponseEntity.notFound().build();
            }

            String filepath = ExportService.exportToPDF(scan);
            Map<String, String> response = new HashMap<>();
            response.put("filepath", filepath);
            response.put("message", "PDF export completed");

            return ResponseEntity.ok(response);
        } catch (SQLException | IOException e) {
            logger.error("Error exporting PDF", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Request object for creating scans via API
     */
    public static class ScanRequest {
        public String name;
        public String targetPath;
        
        public ScanRequest() {
        }
        
        public ScanRequest(String name, String targetPath) {
            this.name = name;
            this.targetPath = targetPath;
        }
    }
}
