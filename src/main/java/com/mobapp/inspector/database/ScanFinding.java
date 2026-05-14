package com.mobapp.inspector.database;

import java.io.Serializable;

/**
 * JPA Entity for storing individual security findings from scans.
 */
public class ScanFinding implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private Long scanId;
    private String findingType;
    private String severity; // CRITICAL, HIGH, MEDIUM, LOW, INFO
    private String filePath;
    private Integer lineNumber;
    private String matchedText;
    private String context;
    private String remediation;
    
    public ScanFinding() {
    }
    
    public ScanFinding(Long scanId, String findingType, String severity, 
                      String filePath, Integer lineNumber, String matchedText) {
        this.scanId = scanId;
        this.findingType = findingType;
        this.severity = severity;
        this.filePath = filePath;
        this.lineNumber = lineNumber;
        this.matchedText = matchedText;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getScanId() {
        return scanId;
    }
    
    public void setScanId(Long scanId) {
        this.scanId = scanId;
    }
    
    public String getFindingType() {
        return findingType;
    }
    
    public void setFindingType(String findingType) {
        this.findingType = findingType;
    }
    
    public String getSeverity() {
        return severity;
    }
    
    public void setSeverity(String severity) {
        this.severity = severity;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public Integer getLineNumber() {
        return lineNumber;
    }
    
    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }
    
    public String getMatchedText() {
        return matchedText;
    }
    
    public void setMatchedText(String matchedText) {
        this.matchedText = matchedText;
    }
    
    public String getContext() {
        return context;
    }
    
    public void setContext(String context) {
        this.context = context;
    }
    
    public String getRemediation() {
        return remediation;
    }
    
    public void setRemediation(String remediation) {
        this.remediation = remediation;
    }
    
    @Override
    public String toString() {
        return "ScanFinding{" +
                "id=" + id +
                ", findingType='" + findingType + '\'' +
                ", severity='" + severity + '\'' +
                ", filePath='" + filePath + '\'' +
                ", lineNumber=" + lineNumber +
                '}';
    }
}
