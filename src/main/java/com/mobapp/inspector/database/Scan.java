package com.mobapp.inspector.database;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * JPA Entity for storing scan results in the database.
 */
public class Scan implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String name;
    private String targetPath;
    private LocalDateTime scanDate;
    private LocalDateTime completedDate;
    private Long totalFiles;
    private Long criticalCount;
    private Long highCount;
    private Long mediumCount;
    private Long lowCount;
    private Long infoCount;
    private Double scanDurationSeconds;
    private String status; // PENDING, IN_PROGRESS, COMPLETED, FAILED
    private String errorMessage;
    private List<ScanFinding> findings;
    
    public Scan() {
    }
    
    public Scan(String name, String targetPath) {
        this.name = name;
        this.targetPath = targetPath;
        this.scanDate = LocalDateTime.now();
        this.status = "PENDING";
        this.criticalCount = 0L;
        this.highCount = 0L;
        this.mediumCount = 0L;
        this.lowCount = 0L;
        this.infoCount = 0L;
        this.totalFiles = 0L;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getTargetPath() {
        return targetPath;
    }
    
    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }
    
    public LocalDateTime getScanDate() {
        return scanDate;
    }
    
    public void setScanDate(LocalDateTime scanDate) {
        this.scanDate = scanDate;
    }
    
    public LocalDateTime getCompletedDate() {
        return completedDate;
    }
    
    public void setCompletedDate(LocalDateTime completedDate) {
        this.completedDate = completedDate;
    }
    
    public Long getTotalFiles() {
        return totalFiles;
    }
    
    public void setTotalFiles(Long totalFiles) {
        this.totalFiles = totalFiles;
    }
    
    public Long getCriticalCount() {
        return criticalCount;
    }
    
    public void setCriticalCount(Long criticalCount) {
        this.criticalCount = criticalCount;
    }
    
    public Long getHighCount() {
        return highCount;
    }
    
    public void setHighCount(Long highCount) {
        this.highCount = highCount;
    }
    
    public Long getMediumCount() {
        return mediumCount;
    }
    
    public void setMediumCount(Long mediumCount) {
        this.mediumCount = mediumCount;
    }
    
    public Long getLowCount() {
        return lowCount;
    }
    
    public void setLowCount(Long lowCount) {
        this.lowCount = lowCount;
    }
    
    public Long getInfoCount() {
        return infoCount;
    }
    
    public void setInfoCount(Long infoCount) {
        this.infoCount = infoCount;
    }
    
    public Double getScanDurationSeconds() {
        return scanDurationSeconds;
    }
    
    public void setScanDurationSeconds(Double scanDurationSeconds) {
        this.scanDurationSeconds = scanDurationSeconds;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public List<ScanFinding> getFindings() {
        return findings;
    }
    
    public void setFindings(List<ScanFinding> findings) {
        this.findings = findings;
    }
    
    public Long getTotalIssues() {
        return criticalCount + highCount + mediumCount + lowCount + infoCount;
    }
    
    @Override
    public String toString() {
        return "Scan{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", targetPath='" + targetPath + '\'' +
                ", scanDate=" + scanDate +
                ", totalIssues=" + getTotalIssues() +
                ", status='" + status + '\'' +
                '}';
    }
}
