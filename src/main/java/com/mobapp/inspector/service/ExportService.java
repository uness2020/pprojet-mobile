package com.mobapp.inspector.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mobapp.inspector.core.MobileSecurityScanner.SecurityFinding;
import com.mobapp.inspector.database.Scan;
import com.mobapp.inspector.database.ScanFinding;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for exporting scan results (CSV, JSON, HTML, PDF).
 */
public class ExportService {

    private static final Logger logger = LoggerFactory.getLogger(ExportService.class);
    private static final DateTimeFormatter DATE_FORMAT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final float PDF_MARGIN = 50;
    private static final float PDF_LINE = 14;
    private static final float PDF_BOTTOM = 56;

    private static Path defaultExportPath(String filename) throws IOException {
        Path dir = Paths.get(System.getProperty("user.home"), "Downloads");
        Files.createDirectories(dir);
        return dir.resolve(filename);
    }

    private static String asciiSafe(String s) {
        if (s == null) {
            return "";
        }
        return s.replaceAll("[^\\x20-\\x7E]", "?");
    }

    private static List<ScanFinding> findingsOrEmpty(Scan scan) {
        return scan.getFindings() != null ? scan.getFindings() : List.of();
    }

    public static String exportToCSV(Scan scan) throws IOException {
        String filename = "scan_" + scan.getId() + "_" + System.currentTimeMillis() + ".csv";
        Path filepath = defaultExportPath(filename);
        exportToCSV(scan, filepath);
        return filepath.toString();
    }

    public static void exportToCSV(Scan scan, Path filepath) throws IOException {
        Files.createDirectories(filepath.getParent());
        try (FileWriter out = new FileWriter(filepath.toFile());
             CSVPrinter csvPrinter = new CSVPrinter(out, CSVFormat.DEFAULT
                     .withHeader("Finding ID", "Finding Type", "Severity", "File Path",
                             "Line Number", "Matched Text"))) {

            for (ScanFinding finding : findingsOrEmpty(scan)) {
                csvPrinter.printRecord(
                    finding.getId(),
                    finding.getFindingType(),
                    finding.getSeverity(),
                    finding.getFilePath(),
                    finding.getLineNumber(),
                    finding.getMatchedText()
                );
            }
            csvPrinter.flush();
            logger.info("CSV export completed: {}", filepath);
        }
    }

    public static String exportToJSON(Scan scan) throws IOException {
        String filename = "scan_" + scan.getId() + "_" + System.currentTimeMillis() + ".json";
        Path filepath = defaultExportPath(filename);
        exportToJSON(scan, filepath);
        return filepath.toString();
    }

    public static void exportToJSON(Scan scan, Path filepath) throws IOException {
        Files.createDirectories(filepath.getParent());
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        ScanExportData exportData = new ScanExportData(scan);
        try (FileWriter writer = new FileWriter(filepath.toFile())) {
            gson.toJson(exportData, writer);
            logger.info("JSON export completed: {}", filepath);
        }
    }

    public static String exportToHTML(Scan scan) throws IOException {
        String filename = "scan_" + scan.getId() + "_" + System.currentTimeMillis() + ".html";
        Path filepath = defaultExportPath(filename);
        exportToHTML(scan, filepath);
        return filepath.toString();
    }

    public static void exportToHTML(Scan scan, Path filepath) throws IOException {
        Files.createDirectories(filepath.getParent());
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("<meta charset='UTF-8'>\n");
        html.append("<title>Secure Storage Inspector - Scan Report</title>\n");
        html.append("<style>\n");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }\n");
        html.append("h1 { color: #2c3e50; border-bottom: 3px solid #3498db; padding-bottom: 10px; }\n");
        html.append("h2 { color: #34495e; margin-top: 30px; }\n");
        html.append(".summary { background: white; padding: 20px; border-radius: 5px; margin: 20px 0; }\n");
        html.append(".critical { background-color: #e74c3c; color: white; padding: 5px 10px; border-radius: 3px; }\n");
        html.append(".high { background-color: #e67e22; color: white; padding: 5px 10px; border-radius: 3px; }\n");
        html.append(".medium { background-color: #f39c12; color: white; padding: 5px 10px; border-radius: 3px; }\n");
        html.append(".low { background-color: #3498db; color: white; padding: 5px 10px; border-radius: 3px; }\n");
        html.append(".info { background-color: #95a5a6; color: white; padding: 5px 10px; border-radius: 3px; }\n");
        html.append("table { width: 100%; border-collapse: collapse; background: white; }\n");
        html.append("th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }\n");
        html.append("th { background-color: #3498db; color: white; }\n");
        html.append("tr:hover { background-color: #ecf0f1; }\n");
        html.append(".footer { text-align: center; color: #7f8c8d; margin-top: 30px; }\n");
        html.append("</style>\n");
        html.append("</head>\n");
        html.append("<body>\n");

        html.append("<h1>Secure Storage Inspector - Scan Report</h1>\n");

        html.append("<div class='summary'>\n");
        html.append("<h2>Scan Information</h2>\n");
        html.append("<p><strong>Scan Name:</strong> ").append(escapeHtml(scan.getName())).append("</p>\n");
        html.append("<p><strong>Target Path:</strong> ").append(escapeHtml(scan.getTargetPath())).append("</p>\n");
        html.append("<p><strong>Scan Date:</strong> ").append(scan.getScanDate().format(DATE_FORMAT)).append("</p>\n");
        html.append("<p><strong>Total Files Scanned:</strong> ").append(scan.getTotalFiles()).append("</p>\n");
        html.append("<p><strong>Status:</strong> ").append(escapeHtml(scan.getStatus())).append("</p>\n");
        html.append("</div>\n");

        html.append("<div class='summary'>\n");
        html.append("<h2>Security Summary</h2>\n");
        html.append("<p><span class='critical'>CRITICAL:</span> ").append(scan.getCriticalCount()).append("</p>\n");
        html.append("<p><span class='high'>HIGH:</span> ").append(scan.getHighCount()).append("</p>\n");
        html.append("<p><span class='medium'>MEDIUM:</span> ").append(scan.getMediumCount()).append("</p>\n");
        html.append("<p><span class='low'>LOW:</span> ").append(scan.getLowCount()).append("</p>\n");
        html.append("<p><span class='info'>INFO:</span> ").append(scan.getInfoCount()).append("</p>\n");
        html.append("<p><strong>Total Issues:</strong> ").append(scan.getTotalIssues()).append("</p>\n");
        html.append("</div>\n");

        List<ScanFinding> findings = findingsOrEmpty(scan);
        if (!findings.isEmpty()) {
            html.append("<h2>Detailed Findings</h2>\n");
            html.append("<table>\n");
            html.append("<tr><th>Finding Type</th><th>Severity</th><th>File Path</th><th>Line</th><th>Matched Text</th></tr>\n");

            for (ScanFinding finding : findings) {
                String severityClass = finding.getSeverity() != null
                    ? finding.getSeverity().toLowerCase() : "info";
                html.append("<tr>\n");
                html.append("<td>").append(escapeHtml(finding.getFindingType())).append("</td>\n");
                html.append("<td><span class='").append(severityClass).append("'>")
                    .append(escapeHtml(finding.getSeverity())).append("</span></td>\n");
                html.append("<td>").append(escapeHtml(finding.getFilePath())).append("</td>\n");
                html.append("<td>").append(finding.getLineNumber()).append("</td>\n");
                html.append("<td><code>").append(escapeHtml(maskSensitiveData(finding.getMatchedText())))
                    .append("</code></td>\n");
                html.append("</tr>\n");
            }

            html.append("</table>\n");
        }

        html.append("<div class='footer'>\n");
        html.append("<p>Generated by Secure Storage Inspector</p>\n");
        html.append("</div>\n");
        html.append("</body>\n");
        html.append("</html>\n");

        Files.writeString(filepath, html.toString());
        logger.info("HTML export completed: {}", filepath);
    }

    private static String escapeHtml(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;");
    }

    public static String exportToPDF(Scan scan) throws IOException {
        String filename = "scan_" + scan.getId() + "_" + System.currentTimeMillis() + ".pdf";
        Path filepath = defaultExportPath(filename);
        exportToPDF(scan, filepath);
        return filepath.toString();
    }

    public static void exportToPDF(Scan scan, Path filepath) throws IOException {
        Files.createDirectories(filepath.getParent());
        try (PDDocument doc = new PDDocument()) {
            writeScanSummaryPdf(doc, scan, findingsOrEmpty(scan));
            doc.save(filepath.toFile());
            logger.info("PDF export completed: {}", filepath);
        }
    }

    /**
     * PDF report for the current (not yet saved) scan results in the Security Scanner tab.
     */
    public static void exportSecurityFindingsToPdf(
        List<SecurityFinding> findings,
        Path filepath,
        String reportTitle,
        String scannedPath
    ) throws IOException {
        Files.createDirectories(filepath.getParent());
        Scan pseudo = new Scan();
        pseudo.setName(reportTitle != null ? reportTitle : "Live scan");
        pseudo.setTargetPath(scannedPath != null ? scannedPath : "");
        pseudo.setScanDate(java.time.LocalDateTime.now());
        pseudo.setStatus("COMPLETED");
        pseudo.setTotalFiles(Math.max(1, findings.stream().map(SecurityFinding::getFilePath).distinct().count()));
        long c = 0, h = 0, m = 0, l = 0, i = 0;
        for (SecurityFinding f : findings) {
            if (f.getSeverity() == null) {
                continue;
            }
            switch (f.getSeverity()) {
                case CRITICAL -> c++;
                case HIGH -> h++;
                case MEDIUM -> m++;
                case LOW -> l++;
                case INFO -> i++;
            }
        }
        pseudo.setCriticalCount(c);
        pseudo.setHighCount(h);
        pseudo.setMediumCount(m);
        pseudo.setLowCount(l);
        pseudo.setInfoCount(i);
        List<ScanFinding> rows = new ArrayList<>();
        for (SecurityFinding f : findings) {
            ScanFinding sf = new ScanFinding();
            sf.setFindingType(f.getType());
            sf.setSeverity(f.getSeverity() != null ? f.getSeverity().name() : "");
            sf.setFilePath(f.getFilePath());
            sf.setLineNumber(f.getLineNumber());
            sf.setMatchedText(truncate(f.getDescription(), 2000));
            rows.add(sf);
        }
        pseudo.setFindings(rows);
        try (PDDocument doc = new PDDocument()) {
            writeScanSummaryPdf(doc, pseudo, rows);
            doc.save(filepath.toFile());
            logger.info("PDF export (live findings) completed: {}", filepath);
        }
    }

    private static void writeScanSummaryPdf(PDDocument doc, Scan scan, List<ScanFinding> findings) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);
        float height = page.getMediaBox().getHeight();
        float y = height - PDF_MARGIN;
        PDPageContentStream stream = new PDPageContentStream(doc, page);
        try {
            y = pdfWriteHeader(stream, scan, y);
            stream.setFont(PDType1Font.HELVETICA_BOLD, 10);
            stream.beginText();
            stream.newLineAtOffset(PDF_MARGIN, y);
            stream.showText(asciiSafe("Findings (type | severity | file | line)"));
            stream.endText();
            y -= PDF_LINE * 2;

            stream.setFont(PDType1Font.HELVETICA, 8);
            for (ScanFinding f : findings) {
                List<String> lines = pdfWrapFinding(f);
                for (String line : lines) {
                    if (y < PDF_BOTTOM) {
                        stream.close();
                        page = new PDPage(PDRectangle.A4);
                        doc.addPage(page);
                        height = page.getMediaBox().getHeight();
                        y = height - PDF_MARGIN;
                        stream = new PDPageContentStream(doc, page);
                        stream.setFont(PDType1Font.HELVETICA, 8);
                    }
                    stream.beginText();
                    stream.newLineAtOffset(PDF_MARGIN, y);
                    stream.showText(asciiSafe(truncate(line, 120)));
                    stream.endText();
                    y -= PDF_LINE;
                }
                y -= 4;
            }
        } finally {
            stream.close();
        }
    }

    private static float pdfWriteHeader(PDPageContentStream stream, Scan scan, float y) throws IOException {
        stream.setFont(PDType1Font.HELVETICA_BOLD, 16);
        stream.beginText();
        stream.newLineAtOffset(PDF_MARGIN, y);
        stream.showText(asciiSafe("Secure Storage Inspector"));
        stream.endText();
        y -= PDF_LINE * 2;

        stream.setFont(PDType1Font.HELVETICA, 11);
        String[] lines = {
            "Scan: " + truncate(scan.getName(), 80),
            "Path: " + truncate(scan.getTargetPath(), 100),
            "Date: " + scan.getScanDate().format(DATE_FORMAT),
            "Status: " + scan.getStatus(),
            String.format("Files: %s  Issues: %s (C:%s H:%s M:%s L:%s I:%s)",
                scan.getTotalFiles(), scan.getTotalIssues(),
                scan.getCriticalCount(), scan.getHighCount(), scan.getMediumCount(),
                scan.getLowCount(), scan.getInfoCount())
        };
        for (String line : lines) {
            stream.beginText();
            stream.newLineAtOffset(PDF_MARGIN, y);
            stream.showText(asciiSafe(line));
            stream.endText();
            y -= PDF_LINE;
        }
        return y - PDF_LINE;
    }

    private static List<String> pdfWrapFinding(ScanFinding f) {
        List<String> out = new ArrayList<>();
        String head = String.format("- %s | %s | %s | %s",
            nullToEmpty(f.getFindingType()),
            nullToEmpty(f.getSeverity()),
            truncate(nullToEmpty(f.getFilePath()), 60),
            f.getLineNumber() != null ? f.getLineNumber() : 0);
        out.add(head);
        String text = nullToEmpty(f.getMatchedText());
        if (!text.isEmpty()) {
            for (int i = 0; i < text.length(); i += 110) {
                out.add("  " + truncate(text.substring(i, Math.min(text.length(), i + 110)), 110));
            }
        }
        return out;
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max - 3) + "...";
    }

    private static String maskSensitiveData(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        if (text.length() > 50) {
            return text.substring(0, 25) + "..." + text.substring(text.length() - 25);
        }
        return text;
    }

    public static class ScanExportData {
        public long id;
        public String name;
        public String targetPath;
        public String scanDate;
        public long totalFiles;
        public long totalIssues;
        public long criticalCount;
        public long highCount;
        public long mediumCount;
        public long lowCount;
        public long infoCount;
        public String status;
        public List<ScanFinding> findings;

        public ScanExportData(Scan scan) {
            this.id = scan.getId() != null ? scan.getId() : 0L;
            this.name = scan.getName();
            this.targetPath = scan.getTargetPath();
            this.scanDate = scan.getScanDate() != null ? scan.getScanDate().format(DATE_FORMAT) : "";
            this.totalFiles = scan.getTotalFiles() != null ? scan.getTotalFiles() : 0;
            this.totalIssues = scan.getTotalIssues();
            this.criticalCount = scan.getCriticalCount() != null ? scan.getCriticalCount() : 0;
            this.highCount = scan.getHighCount() != null ? scan.getHighCount() : 0;
            this.mediumCount = scan.getMediumCount() != null ? scan.getMediumCount() : 0;
            this.lowCount = scan.getLowCount() != null ? scan.getLowCount() : 0;
            this.infoCount = scan.getInfoCount() != null ? scan.getInfoCount() : 0;
            this.status = scan.getStatus();
            this.findings = findingsOrEmpty(scan);
        }
    }
}
