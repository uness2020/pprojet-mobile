package com.mobapp.inspector.core;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.regex.*;
import java.util.function.Consumer;

/**
 * MobileSecurityScanner - Detects security issues in mobile app storage files.
 * Specialized for mobile app analysis including API keys, certificates, and sensitive data.
 */
public class MobileSecurityScanner {

    // Progress callback for UI updates
    private Consumer<ScanProgress> progressCallback;
    
    // Mobile-specific file patterns to skip
    private static final Set<String> SKIP_PATTERNS = Set.of(
        "node_modules", ".git", ".svn", "__pycache__", "target", "build",
        "dist", ".gradle", ".idea", ".vscode", "vendor", ".npm", ".m2",
        ".xcworkspace", ".xcodeproj", "Pods", "DerivedData"
    );
    
    // Mobile-specific sensitive file patterns
    private static final Set<String> MOBILE_SENSITIVE_FILES = Set.of(
        ".p12", ".pfx", ".jks", ".keystore", ".pem", ".crt", ".cer",
        "google-services.json", "GoogleService-Info.plist", "config.xml",
        "AndroidManifest.xml", "Info.plist", "entitlements.plist",
        ".env", "secrets.properties", "api_keys.txt", "credentials.json"
    );
    
    // Mobile-specific security patterns
    private static final List<SecurityPattern> MOBILE_PATTERNS = Arrays.asList(
        // === MOBILE API KEYS ===
        new SecurityPattern("Google API Key", "AIza[0-9A-Za-z\\-_]{35}", Severity.CRITICAL),
        new SecurityPattern("Google OAuth Token", "[0-9]+-[0-9A-Za-z_]{32}\\.apps\\.googleusercontent\\.com", Severity.CRITICAL),
        new SecurityPattern("Google Service Account", "[a-z0-9\\-]+@[a-z0-9\\-]+\\.iam\\.gserviceaccount\\.com", Severity.CRITICAL),
        new SecurityPattern("Firebase API Key", "(?i)AIza[0-9A-Za-z\\-_]{35}", Severity.CRITICAL),
        new SecurityPattern("Firebase Database URL", "(?i)firebaseio\\.com", Severity.HIGH),
        new SecurityPattern("Firebase Project ID", "(?i)project_id[\"']?\\s*[:=]\\s*[\"']([a-z0-9\\-]+)[\"']", Severity.HIGH),
        
        // === APPLE SERVICES ===
        new SecurityPattern("Apple API Key", "[A-Z0-9]{8}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{12}", Severity.CRITICAL),
        new SecurityPattern("Apple Team ID", "[A-Z0-9]{10}", Severity.MEDIUM),
        new SecurityPattern("Apple Bundle ID", "[a-zA-Z0-9\\-]+\\.[a-zA-Z0-9\\-]+", Severity.INFO),
        new SecurityPattern("iTunes Connect ID", "[0-9]{9}", Severity.MEDIUM),
        
        // === ANDROID SERVICES ===
        new SecurityPattern("Android API Key", "(?i)android_api_key\\s*[=:]\\s*['\"][a-zA-Z0-9]{32,}['\"]", Severity.CRITICAL),
        new SecurityPattern("Google Play Service Key", "(?i)google_play_services_key\\s*[=:]\\s*['\"][a-zA-Z0-9]{32,}['\"]", Severity.CRITICAL),
        new SecurityPattern("Android Keystore Password", "(?i)keystore_password\\s*[=:]\\s*['\"][^'\"]{4,}['\"]", Severity.CRITICAL),
        new SecurityPattern("Android Signing Key", "(?i)store_password\\s*[=:]\\s*['\"][^'\"]{4,}['\"]", Severity.CRITICAL),
        new SecurityPattern("Android Key Alias", "(?i)key_alias\\s*[=:]\\s*['\"][^'\"]{3,}['\"]", Severity.MEDIUM),
        
        // === SOCIAL MEDIA PLATFORMS ===
        new SecurityPattern("Facebook App ID", "(?i)facebook_app_id\\s*[=:]\\s*[0-9]{15,}", Severity.HIGH),
        new SecurityPattern("Facebook Client Token", "(?i)facebook_client_token\\s*[=:]\\s*[a-zA-Z0-9]{32}", Severity.HIGH),
        new SecurityPattern("Twitter API Key", "(?i)twitter_api_key\\s*[=:]\\s*[a-zA-Z0-9]{25}", Severity.HIGH),
        new SecurityPattern("Twitter Secret", "(?i)twitter_api_secret\\s*[=:]\\s*[a-zA-Z0-9]{45}", Severity.CRITICAL),
        new SecurityPattern("Instagram Access Token", "(?i)instagram_access_token\\s*[=:]\\s*[a-zA-Z0-9\\.\\-_]{50,}", Severity.CRITICAL),
        
        // === PAYMENT SERVICES ===
        new SecurityPattern("Stripe Publishable Key", "pk_live_[0-9a-zA-Z]{24}", Severity.HIGH),
        new SecurityPattern("Stripe Secret Key", "sk_live_[0-9a-zA-Z]{24}", Severity.CRITICAL),
        new SecurityPattern("PayPal Client ID", "A[a-zA-Z0-9\\-_]{80}", Severity.HIGH),
        new SecurityPattern("PayPal Client Secret", "[a-zA-Z0-9\\-_]{80}", Severity.CRITICAL),
        new SecurityPattern("Square Application ID", "sq0idp-[a-zA-Z0-9\\-_]{22}", Severity.HIGH),
        new SecurityPattern("Square Access Token", "sq0atp-[a-zA-Z0-9\\-_]{22}", Severity.CRITICAL),
        
        // === ANALYTICS SERVICES ===
        new SecurityPattern("Google Analytics ID", "(?i)ga_tracking_id\\s*[=:]\\s*[\"']UA-[0-9]{9}-[0-9]{1,3}[\"']", Severity.MEDIUM),
        new SecurityPattern("Google Analytics 4 ID", "(?i)gtag\\s*[=:]\\s*[\"']G-[A-Z0-9]{10}[\"']", Severity.MEDIUM),
        new SecurityPattern("Mixpanel Token", "(?i)mixpanel_token\\s*[=:]\\s*[a-zA-Z0-9]{32}", Severity.MEDIUM),
        new SecurityPattern("Amplitude API Key", "(?i)amplitude_api_key\\s*[=:]\\s*[a-zA-Z0-9]{32}", Severity.MEDIUM),
        new SecurityPattern("Segment Write Key", "(?i)segment_write_key\\s*[=:]\\s*[a-zA-Z0-9]{40}", Severity.MEDIUM),
        
        // === CLOUD STORAGE ===
        new SecurityPattern("AWS Access Key", "AKIA[0-9A-Z]{16}", Severity.CRITICAL),
        new SecurityPattern("AWS Secret Key", "(?i)aws_secret_key\\s*[=:]\\s*[\"'][a-zA-Z0-9/+=]{40}[\"']", Severity.CRITICAL),
        new SecurityPattern("AWS S3 Bucket", "(?i)s3://[a-zA-Z0-9\\-\\.]+", Severity.HIGH),
        new SecurityPattern("Azure Storage Key", "(?i)azure_storage_key\\s*[=:]\\s*[\"'][a-zA-Z0-9/+=]{88}[\"']", Severity.CRITICAL),
        new SecurityPattern("Azure Container", "(?i)azure_container\\s*[=:]\\s*[\"'][a-zA-Z0-9\\-]{3,63}[\"']", Severity.MEDIUM),
        
        // === PUSH NOTIFICATIONS ===
        new SecurityPattern("FCM Server Key", "(?i)fcm_server_key\\s*[=:]\\s*[\"'][a-zA-Z0-9\\-_]{170}[\"']", Severity.CRITICAL),
        new SecurityPattern("FCM Sender ID", "(?i)fcm_sender_id\\s*[=:]\\s*[0-9]{12}", Severity.MEDIUM),
        new SecurityPattern("APNs Auth Key", "(?i)apns_auth_key\\s*[=:]\\s*[\"'][A-Z0-9]{10}[\"']", Severity.CRITICAL),
        new SecurityPattern("APNs Key ID", "(?i)apns_key_id\\s*[=:]\\s*[\"'][A-Z0-9]{10}[\"']", Severity.HIGH),
        new SecurityPattern("APNs Team ID", "(?i)apns_team_id\\s*[=:]\\s*[\"'][A-Z0-9]{10}[\"']", Severity.HIGH),
        
        // === DATABASE CONNECTIONS ===
        new SecurityPattern("MongoDB Connection", "mongodb(\\+srv)?://[^\\s]+:[^\\s]+@[^\"]+", Severity.CRITICAL),
        new SecurityPattern("PostgreSQL Connection", "postgres(ql)?://[^\\s]+:[^\\s]+@[^\"]+", Severity.CRITICAL),
        new SecurityPattern("MySQL Connection", "mysql://[^\\s]+:[^\\s]+@[^\"]+", Severity.CRITICAL),
        new SecurityPattern("Redis Connection", "redis://[^\\s]*:[^\\s]+@[^\"]+", Severity.CRITICAL),
        
        // === AUTHENTICATION TOKENS ===
        new SecurityPattern("JWT Token", "eyJ[a-zA-Z0-9_-]*\\.eyJ[a-zA-Z0-9_-]*\\.[a-zA-Z0-9_-]*", Severity.HIGH),
        new SecurityPattern("Bearer Token", "(?i)bearer\\s+[a-zA-Z0-9\\-._~+/]+=*", Severity.HIGH),
        new SecurityPattern("OAuth Token", "(?i)oauth(.{0,10})?[a-zA-Z0-9]{20,}", Severity.MEDIUM),
        
        // === CERTIFICATES AND KEYS ===
        new SecurityPattern("RSA Private Key", "-----BEGIN RSA PRIVATE KEY-----", Severity.CRITICAL),
        new SecurityPattern("Private Key", "-----BEGIN PRIVATE KEY-----", Severity.CRITICAL),
        new SecurityPattern("Certificate", "-----BEGIN CERTIFICATE-----", Severity.HIGH),
        new SecurityPattern("PKCS12 Certificate", "-----BEGIN PKCS12-----", Severity.HIGH),
        
        // === MOBILE SPECIFIC SECRETS ===
        new SecurityPattern("App Secret", "(?i)(app_secret|appsecret|client_secret|clientsecret)\\s*[=:]\\s*['\"][^'\"]{8,}['\"]", Severity.CRITICAL),
        new SecurityPattern("API Secret", "(?i)(api_secret|apisecret|secret_key|secretkey)\\s*[=:]\\s*['\"][^'\"]{8,}['\"]", Severity.CRITICAL),
        new SecurityPattern("Session Secret", "(?i)(session_secret|session_key)\\s*[=:]\\s*['\"][^'\"]{16,}['\"]", Severity.CRITICAL),
        new SecurityPattern("Encryption Key", "(?i)(encryption_key|encrypt_key|cipher_key)\\s*[=:]\\s*['\"][^'\"]{16,}['\"]", Severity.CRITICAL),
        
        // === URL WITH CREDENTIALS ===
        new SecurityPattern("URL with Credentials", "(?i)[a-z]+://[^\\s]+:[^\\s]+@[^\"]+", Severity.CRITICAL),
        
        // === PERSONAL DATA ===
        new SecurityPattern("Email Address", "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}", Severity.INFO),
        new SecurityPattern("Phone Number", "\\b(?:\\+?1[-.]?)?\\(?[0-9]{3}\\)?[-.]?[0-9]{3}[-.]?[0-9]{4}\\b", Severity.INFO),
        new SecurityPattern("Credit Card", "\\b(?:4[0-9]{12}(?:[0-9]{3})?|5[1-5][0-9]{14}|3[47][0-9]{13}|6(?:011|5[0-9]{2})[0-9]{12})\\b", Severity.CRITICAL),
        
        // === HIGH ENTROPY STRINGS ===
        new SecurityPattern("Base64 Secret", "(?i)(secret|key|token|password|auth).{0,20}[a-zA-Z0-9+/]{40,}={0,2}", Severity.MEDIUM),
        new SecurityPattern("Hex Encoded Key", "(?i)(key|secret|token).{0,20}[a-fA-F0-9]{32,}", Severity.MEDIUM),
        
        // === DEBUG/DEVELOPMENT ===
        new SecurityPattern("Debug Flag", "(?i)debug\\s*[=:]\\s*true", Severity.LOW),
        new SecurityPattern("Test API Endpoint", "(?i)(test|dev|staging)\\.(api|endpoint|url)", Severity.LOW),
        new SecurityPattern("Hardcoded IP", "\\b(?:10|172\\.(?:1[6-9]|2[0-9]|3[01])|192\\.168)\\.[0-9]{1,3}\\.[0-9]{1,3}\\b", Severity.MEDIUM)
    );

    // Maximum file size to scan (50MB)
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024;
    
    /**
     * Scans a directory for mobile security issues with parallel processing.
     */
    public List<SecurityFinding> scanDirectory(File directory) {
        List<SecurityFinding> findings = Collections.synchronizedList(new ArrayList<>());
        
        if (directory == null || !directory.exists()) {
            return findings;
        }
        
        reportProgress(0, 0, "Starting mobile security scan...");
        
        try {
            // Collect files first for parallel processing
            List<Path> filesToScan = new ArrayList<>();
            Files.walk(directory.toPath())
                .filter(Files::isRegularFile)
                .filter(p -> !shouldSkip(p))
                .filter(p -> {
                    try {
                        return Files.size(p) <= MAX_FILE_SIZE;
                    } catch (IOException e) {
                        return false;
                    }
                })
                .forEach(filesToScan::add);
            
            long totalFiles = filesToScan.size();
            final long[] scanned = {0};
            
            reportProgress(0, totalFiles, "Found " + totalFiles + " files to scan...");
            
            // Use parallel stream for faster scanning on multi-core systems
            filesToScan.parallelStream().forEach(path -> {
                scanFile(path.toFile(), findings);
                synchronized (scanned) {
                    scanned[0]++;
                    if (scanned[0] % 50 == 0) {
                        reportProgress(scanned[0], totalFiles, "Scanned " + scanned[0] + " files...");
                    }
                }
            });
            
            reportProgress(totalFiles, totalFiles, "Mobile security scan complete. Found " + findings.size() + " issues.");
            
        } catch (IOException e) {
            findings.add(new SecurityFinding(
                "Scan Error", 
                "Failed to scan directory: " + e.getMessage(),
                Severity.LOW,
                directory.getAbsolutePath(),
                0
            ));
        }
        
        return new ArrayList<>(findings);
    }
    
    /**
     * Sets a progress callback for UI updates.
     */
    public void setProgressCallback(Consumer<ScanProgress> callback) {
        this.progressCallback = callback;
    }
    
    /**
     * Reports progress to callback if set.
     */
    private void reportProgress(long current, long total, String message) {
        if (progressCallback != null) {
            progressCallback.accept(new ScanProgress(current, total, message));
        }
    }
    
    /**
     * Counts files in directory (used for progress estimation).
     */
    @SuppressWarnings("unused")
    private long[] countFiles(Path root) {
        long[] counts = {0, 0};
        try {
            Files.walk(root)
                .filter(Files::isRegularFile)
                .filter(p -> !shouldSkip(p))
                .forEach(p -> counts[0]++);
        } catch (IOException e) {
            // Ignore
        }
        return counts;
    }
    
    /**
     * Checks if path should be skipped.
     */
    private boolean shouldSkip(Path path) {
        String pathStr = path.toString().toLowerCase();
        for (String skip : SKIP_PATTERNS) {
            if (pathStr.contains(skip.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Scans a single file for security issues.
     */
    public void scanFile(File file, List<SecurityFinding> findings) {
        String fileName = file.getName().toLowerCase();
        
        // Check for mobile-specific sensitive files
        for (String pattern : MOBILE_SENSITIVE_FILES) {
            if (fileName.contains(pattern.toLowerCase())) {
                findings.add(new SecurityFinding(
                    "Mobile Sensitive File",
                    "Mobile app sensitive file detected: " + file.getName(),
                    Severity.HIGH,
                    file.getAbsolutePath(),
                    0
                ));
            }
        }
        
        // Skip binary files for text-based scanning
        if (isBinaryFile(file)) {
            scanBinaryFile(file, findings);
            return;
        }
        
        // Scan SQLite databases
        if (fileName.endsWith(".db") || fileName.endsWith(".sqlite") || fileName.endsWith(".sqlite3")) {
            scanDatabase(file, findings);
            return;
        }
        
        // Scan text-based files
        scanTextFile(file, findings);
    }
    
    /**
     * Scans a text file for sensitive patterns.
     */
    private void scanTextFile(File file, List<SecurityFinding> findings) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;
            Set<String> foundPatterns = new HashSet<>(); // Avoid duplicates
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                // Skip comment lines
                String trimmed = line.trim();
                if (trimmed.startsWith("#") || trimmed.startsWith("//") || trimmed.startsWith("/*") || trimmed.startsWith("*")) {
                    continue;
                }
                
                for (SecurityPattern pattern : MOBILE_PATTERNS) {
                    Matcher matcher = pattern.getPattern().matcher(line);
                    while (matcher.find()) {
                        String match = matcher.group();
                        String key = pattern.getName() + ":" + lineNumber;
                        
                        if (!foundPatterns.contains(key)) {
                            foundPatterns.add(key);
                            
                            // Check entropy for high-confidence detection
                            double entropy = calculateEntropy(match);
                            Severity severity = pattern.getSeverity();
                            
                            // Boost severity for high-entropy strings
                            if (entropy > 4.0 && severity != Severity.CRITICAL) {
                                severity = Severity.values()[Math.max(0, severity.ordinal() - 1)];
                            }
                            
                            findings.add(new SecurityFinding(
                                pattern.getName(),
                                "Found potential " + pattern.getName() + " (entropy: " + String.format("%.1f", entropy) + "): " + truncate(match, 50),
                                severity,
                                file.getAbsolutePath(),
                                lineNumber
                            ));
                        }
                    }
                }
                
                // Detect high-entropy strings that might be secrets
                if (line.length() > 20) {
                    detectHighEntropyStrings(line, file.getAbsolutePath(), lineNumber, findings);
                }
            }
        } catch (IOException e) {
            // Skip files that can't be read
        }
    }
    
    /**
     * Calculates Shannon entropy of a string.
     */
    private double calculateEntropy(String s) {
        if (s == null || s.isEmpty()) return 0;
        
        Map<Character, Integer> freq = new HashMap<>();
        for (char c : s.toCharArray()) {
            freq.merge(c, 1, Integer::sum);
        }
        
        double entropy = 0;
        for (int count : freq.values()) {
            double p = (double) count / s.length();
            entropy -= p * (Math.log(p) / Math.log(2));
        }
        return entropy;
    }
    
    /**
     * Detects high-entropy strings that might be secrets.
     */
    private void detectHighEntropyStrings(String line, String filePath, int lineNumber, List<SecurityFinding> findings) {
        // Find quoted strings
        Pattern quotedPattern = Pattern.compile("['\"]([a-zA-Z0-9+/=_-]{20,})['\"]");
        Matcher matcher = quotedPattern.matcher(line);
        
        while (matcher.find()) {
            String match = matcher.group(1);
            double entropy = calculateEntropy(match);
            
            // High entropy threshold (> 4.5 bits/char indicates likely secret)
            if (entropy > 4.5) {
                // Check it's not already found
                boolean alreadyFound = findings.stream()
                    .anyMatch(f -> f.getFilePath().equals(filePath) && f.getLineNumber() == lineNumber);
                
                if (!alreadyFound) {
                    findings.add(new SecurityFinding(
                        "High Entropy String",
                        "Potential secret (entropy: " + String.format("%.1f", entropy) + "): " + truncate(match, 30),
                        Severity.MEDIUM,
                        filePath,
                        lineNumber
                    ));
                }
            }
        }
    }
    
    /**
     * Scans a SQLite database for sensitive data.
     */
    private void scanDatabase(File file, List<SecurityFinding> findings) {
        String url = "jdbc:sqlite:" + file.getAbsolutePath();
        
        try (Connection conn = DriverManager.getConnection(url)) {
            // Get all tables
            List<String> tables = getTables(conn);
            
            for (String table : tables) {
                scanTable(conn, table, file.getAbsolutePath(), findings);
            }
        } catch (SQLException e) {
            // Database might be encrypted or corrupted
            findings.add(new SecurityFinding(
                "Database Access Issue",
                "Could not access database: " + e.getMessage(),
                Severity.LOW,
                file.getAbsolutePath(),
                0
            ));
        }
    }
    
    /**
     * Gets list of tables in a SQLite database.
     */
    private List<String> getTables(Connection conn) throws SQLException {
        List<String> tables = new ArrayList<>();
        String query = "SELECT name FROM sqlite_master WHERE type='table'";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                tables.add(rs.getString("name"));
            }
        }
        return tables;
    }
    
    /**
     * Scans a database table for sensitive data.
     */
    private void scanTable(Connection conn, String table, String filePath, List<SecurityFinding> findings) {
        try {
            String query = "SELECT * FROM \"" + table + "\" LIMIT 1000";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                int columnCount = rs.getMetaData().getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = rs.getMetaData().getColumnName(i);
                    String value = rs.getString(i);
                    
                    if (value != null) {
                        // Check column name for sensitive indicators
                        String colLower = columnName.toLowerCase();
                        if (colLower.contains("password") || colLower.contains("secret") || 
                            colLower.contains("token") || colLower.contains("key") ||
                            colLower.contains("api") || colLower.contains("auth")) {
                            
                            findings.add(new SecurityFinding(
                                "Sensitive Column",
                                "Table '" + table + "' contains potentially sensitive column: " + columnName,
                                Severity.MEDIUM,
                                filePath,
                                0
                            ));
                        }
                        
                        // Check value against patterns
                        for (SecurityPattern pattern : MOBILE_PATTERNS) {
                            if (pattern.getPattern().matcher(value).find()) {
                                findings.add(new SecurityFinding(
                                    pattern.getName(),
                                    "Found in table '" + table + "', column '" + columnName + "'",
                                    pattern.getSeverity(),
                                    filePath,
                                    0
                                ));
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            // Skip tables that can't be read
        }
    }
    
    /**
     * Scans binary files for embedded strings.
     */
    private void scanBinaryFile(File file, List<SecurityFinding> findings) {
        try (InputStream is = new BufferedInputStream(new FileInputStream(file), 65536)) {
            byte[] buffer = new byte[65536]; // 64KB buffer for better performance
            StringBuilder sb = new StringBuilder();
            
            while (is.read(buffer) != -1) {
                for (byte b : buffer) {
                    if (b >= 32 && b < 127) {
                        sb.append((char) b);
                    } else if (sb.length() > 0) {
                        String str = sb.toString();
                        checkStringForPatterns(str, file.getAbsolutePath(), findings);
                        sb.setLength(0);
                    }
                }
            }
            
            // Check remaining string
            if (sb.length() > 0) {
                checkStringForPatterns(sb.toString(), file.getAbsolutePath(), findings);
            }
        } catch (IOException e) {
            // Skip files that can't be read
        }
    }
    
    /**
     * Checks a string against security patterns with early termination.
     */
    private void checkStringForPatterns(String str, String filePath, List<SecurityFinding> findings) {
        if (str.length() < 8) return;
        
        // Only check high-severity patterns for performance
        for (SecurityPattern pattern : MOBILE_PATTERNS) {
            if (pattern.getSeverity().ordinal() >= Severity.MEDIUM.ordinal()) {
                Matcher matcher = pattern.getPattern().matcher(str);
                if (matcher.find()) {
                    findings.add(new SecurityFinding(
                        pattern.getName(),
                        "Found embedded in binary: " + truncate(matcher.group(), 50),
                        pattern.getSeverity(),
                        filePath,
                        0
                    ));
                    // Early termination - found a match, no need to check more patterns for this string
                    return;
                }
            }
        }
    }
    
    /**
     * Checks if a file is binary.
     */
    private boolean isBinaryFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") ||
               name.endsWith(".gif") || name.endsWith(".ico") || name.endsWith(".zip") ||
               name.endsWith(".apk") || name.endsWith(".dex") || name.endsWith(".so") ||
               name.endsWith(".class") || name.endsWith(".jar") || name.endsWith(".ipa") ||
               name.endsWith(".xcarchive") || name.endsWith(".app") || name.endsWith(".framework");
    }
    
    /**
     * Truncates a string for display.
     */
    private String truncate(String s, int maxLength) {
        if (s.length() <= maxLength) return s;
        return s.substring(0, maxLength) + "...";
    }
    
    // Inner classes
    
    public enum Severity {
        CRITICAL, HIGH, MEDIUM, LOW, INFO
    }
    
    /**
     * Progress information for scanning.
     */
    public static class ScanProgress {
        private final long filesScanned;
        private final long totalFiles;
        private final String message;
        
        public ScanProgress(long filesScanned, long totalFiles, String message) {
            this.filesScanned = filesScanned;
            this.totalFiles = totalFiles;
            this.message = message;
        }
        
        public long getFilesScanned() { return filesScanned; }
        public long getTotalFiles() { return totalFiles; }
        public double getProgress() { 
            return totalFiles > 0 ? (double) filesScanned / totalFiles : 0; 
        }
        public String getMessage() { return message; }
    }
    
    public static class SecurityPattern {
        private final String name;
        private final Pattern pattern;
        private final Severity severity;
        
        public SecurityPattern(String name, String regex, Severity severity) {
            this.name = name;
            this.pattern = Pattern.compile(regex);
            this.severity = severity;
        }
        
        public String getName() { return name; }
        public Pattern getPattern() { return pattern; }
        public Severity getSeverity() { return severity; }
    }
    
    public static class SecurityFinding {
        private final String type;
        private final String description;
        private final Severity severity;
        private final String filePath;
        private final int lineNumber;
        
        public SecurityFinding(String type, String description, Severity severity, String filePath, int lineNumber) {
            this.type = type;
            this.description = description;
            this.severity = severity;
            this.filePath = filePath;
            this.lineNumber = lineNumber;
        }
        
        public String getType() { return type; }
        public String getDescription() { return description; }
        public Severity getSeverity() { return severity; }
        public String getFilePath() { return filePath; }
        public int getLineNumber() { return lineNumber; }
    }
}
