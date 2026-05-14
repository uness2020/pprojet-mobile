package com.mobapp.inspector.database;

import java.io.File;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for managing SQLite database operations for scan history and results.
 */
public class DatabaseService {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseService.class);
    private static final String DB_URL = "jdbc:sqlite:secure_storage_inspector.db";
    
    private Connection connection;
    
    public DatabaseService() {
        initializeDatabase();
    }
    
    /**
     * Initialize the database and create tables if they don't exist.
     */
    public void initializeDatabase() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            try (Statement pragma = connection.createStatement()) {
                pragma.execute("PRAGMA foreign_keys = ON");
            }
            createTables();
            logger.info("Database initialized successfully");
        } catch (SQLException e) {
            logger.error("Failed to initialize database", e);
        }
    }
    
    /**
     * Create necessary tables in the database.
     */
    private void createTables() throws SQLException {
        String createScansTable = "CREATE TABLE IF NOT EXISTS scans (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name VARCHAR(255) NOT NULL," +
                "target_path VARCHAR(1000) NOT NULL," +
                "scan_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "completed_date TIMESTAMP," +
                "total_files INTEGER DEFAULT 0," +
                "critical_count INTEGER DEFAULT 0," +
                "high_count INTEGER DEFAULT 0," +
                "medium_count INTEGER DEFAULT 0," +
                "low_count INTEGER DEFAULT 0," +
                "info_count INTEGER DEFAULT 0," +
                "scan_duration_seconds REAL," +
                "status VARCHAR(50) DEFAULT 'PENDING'," +
                "error_message TEXT" +
                ")";
        
        String createFindingsTable = "CREATE TABLE IF NOT EXISTS scan_findings (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "scan_id INTEGER NOT NULL," +
                "finding_type VARCHAR(255) NOT NULL," +
                "severity VARCHAR(50) NOT NULL," +
                "file_path VARCHAR(1000) NOT NULL," +
                "line_number INTEGER," +
                "matched_text TEXT," +
                "context TEXT," +
                "remediation TEXT," +
                "FOREIGN KEY(scan_id) REFERENCES scans(id) ON DELETE CASCADE" +
                ")";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createScansTable);
            stmt.execute(createFindingsTable);
            logger.info("Database tables created successfully");
        }
    }
    
    /**
     * Save a scan to the database.
     */
    public long saveScan(Scan scan) throws SQLException {
        String sql = "INSERT INTO scans (name, target_path, scan_date, total_files, " +
                "critical_count, high_count, medium_count, low_count, info_count, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, scan.getName());
            pstmt.setString(2, scan.getTargetPath());
            pstmt.setTimestamp(3, Timestamp.valueOf(scan.getScanDate()));
            pstmt.setLong(4, scan.getTotalFiles() != null ? scan.getTotalFiles() : 0);
            pstmt.setLong(5, scan.getCriticalCount() != null ? scan.getCriticalCount() : 0);
            pstmt.setLong(6, scan.getHighCount() != null ? scan.getHighCount() : 0);
            pstmt.setLong(7, scan.getMediumCount() != null ? scan.getMediumCount() : 0);
            pstmt.setLong(8, scan.getLowCount() != null ? scan.getLowCount() : 0);
            pstmt.setLong(9, scan.getInfoCount() != null ? scan.getInfoCount() : 0);
            pstmt.setString(10, scan.getStatus());
            
            pstmt.executeUpdate();
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    scan.setId(id);
                    logger.info("Scan saved with ID: {}", id);
                    return id;
                }
            }
        }
        return -1;
    }
    
    /**
     * Save findings for a scan.
     */
    public void saveFindings(long scanId, List<ScanFinding> findings) throws SQLException {
        String sql = "INSERT INTO scan_findings (scan_id, finding_type, severity, file_path, " +
                "line_number, matched_text, remediation) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (ScanFinding finding : findings) {
                pstmt.setLong(1, scanId);
                pstmt.setString(2, finding.getFindingType());
                pstmt.setString(3, finding.getSeverity());
                pstmt.setString(4, finding.getFilePath());
                pstmt.setInt(5, finding.getLineNumber() != null ? finding.getLineNumber() : 0);
                pstmt.setString(6, finding.getMatchedText());
                pstmt.setString(7, finding.getRemediation());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            logger.info("Saved {} findings for scan {}", findings.size(), scanId);
        }
    }
    
    /**
     * Get all scans from the database.
     */
    public List<Scan> getAllScans() throws SQLException {
        List<Scan> scans = new ArrayList<>();
        String sql = "SELECT * FROM scans ORDER BY scan_date DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Scan scan = new Scan();
                scan.setId(rs.getLong("id"));
                scan.setName(rs.getString("name"));
                scan.setTargetPath(rs.getString("target_path"));
                scan.setScanDate(rs.getTimestamp("scan_date").toLocalDateTime());
                
                Timestamp completedDate = rs.getTimestamp("completed_date");
                if (completedDate != null) {
                    scan.setCompletedDate(completedDate.toLocalDateTime());
                }
                
                scan.setTotalFiles(rs.getLong("total_files"));
                scan.setCriticalCount(rs.getLong("critical_count"));
                scan.setHighCount(rs.getLong("high_count"));
                scan.setMediumCount(rs.getLong("medium_count"));
                scan.setLowCount(rs.getLong("low_count"));
                scan.setInfoCount(rs.getLong("info_count"));
                scan.setStatus(rs.getString("status"));
                
                scans.add(scan);
            }
        }
        return scans;
    }
    
    /**
     * Get findings for a specific scan.
     */
    public List<ScanFinding> getFindings(long scanId) throws SQLException {
        List<ScanFinding> findings = new ArrayList<>();
        String sql = "SELECT * FROM scan_findings WHERE scan_id = ? ORDER BY severity DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, scanId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ScanFinding finding = new ScanFinding();
                    finding.setId(rs.getLong("id"));
                    finding.setScanId(rs.getLong("scan_id"));
                    finding.setFindingType(rs.getString("finding_type"));
                    finding.setSeverity(rs.getString("severity"));
                    finding.setFilePath(rs.getString("file_path"));
                    finding.setLineNumber(rs.getInt("line_number"));
                    finding.setMatchedText(rs.getString("matched_text"));
                    finding.setRemediation(rs.getString("remediation"));
                    
                    findings.add(finding);
                }
            }
        }
        return findings;
    }
    
    /**
     * Get a specific scan by ID.
     */
    public Scan getScanById(long scanId) throws SQLException {
        String sql = "SELECT * FROM scans WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, scanId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Scan scan = new Scan();
                    scan.setId(rs.getLong("id"));
                    scan.setName(rs.getString("name"));
                    scan.setTargetPath(rs.getString("target_path"));
                    scan.setScanDate(rs.getTimestamp("scan_date").toLocalDateTime());
                    scan.setTotalFiles(rs.getLong("total_files"));
                    scan.setCriticalCount(rs.getLong("critical_count"));
                    scan.setHighCount(rs.getLong("high_count"));
                    scan.setMediumCount(rs.getLong("medium_count"));
                    scan.setLowCount(rs.getLong("low_count"));
                    scan.setInfoCount(rs.getLong("info_count"));
                    scan.setStatus(rs.getString("status"));
                    scan.setFindings(getFindings(scanId));
                    return scan;
                }
            }
        }
        return null;
    }
    
    /**
     * Update scan status.
     */
    public void updateScanStatus(long scanId, String status, String errorMessage) throws SQLException {
        String sql = "UPDATE scans SET status = ?, error_message = ?, completed_date = CURRENT_TIMESTAMP WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setString(2, errorMessage);
            pstmt.setLong(3, scanId);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Delete a scan and its findings.
     */
    public void deleteScan(long scanId) throws SQLException {
        try (PreparedStatement delFindings = connection.prepareStatement(
                "DELETE FROM scan_findings WHERE scan_id = ?")) {
            delFindings.setLong(1, scanId);
            delFindings.executeUpdate();
        }
        try (PreparedStatement pstmt = connection.prepareStatement("DELETE FROM scans WHERE id = ?")) {
            pstmt.setLong(1, scanId);
            pstmt.executeUpdate();
            logger.info("Scan {} deleted", scanId);
        }
    }
    
    /**
     * Close database connection.
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.info("Database connection closed");
            }
        } catch (SQLException e) {
            logger.error("Error closing database connection", e);
        }
    }
}
