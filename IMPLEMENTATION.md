# Secure Storage Inspector - Implementation Guide

## 🎯 Implémentation des 3 étapes prioritaires

### ✅ Phase 1: Export de Rapports (Terminée)

#### Formats Supportés:
- **CSV** : Format tabulaire pour Excel/Google Sheets
- **JSON** : Format structuré pour intégration programmatique
- **HTML** : Rapport interactif avec styling

#### Utilisation:
```java
// Export CSV
String csvPath = ExportService.exportToCSV(scan);

// Export JSON
String jsonPath = ExportService.exportToJSON(scan);

// Export HTML
String htmlPath = ExportService.exportToHTML(scan);
```

**Classes créées:**
- `ExportService.java` - Service d'export multi-format

---

### ✅ Phase 2: Base de Données (Terminée)

#### Fonctionnalités:
- **Persistance SQLite** des résultats de scan
- **Historique des scans** avec métadonnées
- **Résultats détaillés** des findings
- **Gestion des scans** (CRUD)

#### Structure de la base de données:

```sql
-- Table des scans
scans (
  id, name, target_path, scan_date, completed_date,
  total_files, critical_count, high_count, medium_count, low_count, info_count,
  scan_duration_seconds, status, error_message
)

-- Table des findings
scan_findings (
  id, scan_id, finding_type, severity, file_path,
  line_number, matched_text, context, remediation
)
```

#### API Bases de Données:

```java
DatabaseService dbService = new DatabaseService();

// Sauvegarder un scan
long scanId = dbService.saveScan(scan);

// Récupérer tous les scans
List<Scan> scans = dbService.getAllScans();

// Récupérer un scan spécifique
Scan scan = dbService.getScanById(scanId);

// Sauvegarder les findings
dbService.saveFindings(scanId, findings);

// Récupérer les findings d'un scan
List<ScanFinding> findings = dbService.getFindings(scanId);

// Supprimer un scan
dbService.deleteScan(scanId);

// Mettre à jour le statut
dbService.updateScanStatus(scanId, "COMPLETED", null);
```

**Classes créées:**
- `Scan.java` - Entité scan
- `ScanFinding.java` - Entité finding
- `DatabaseService.java` - Service de gestion BD
- `ScanHistoryPanel.java` - UI pour l'historique

**Base de données:** `secure_storage_inspector.db` (SQLite)

---

### ✅ Phase 3: API REST (Terminée)

#### Configuration:
- **Port:** 8080
- **Base URL:** `http://localhost:8080/api`
- **Format:** JSON
- **CORS:** Activé pour tous les origines

#### Endpoints disponibles:

```bash
# Récupérer tous les scans
GET /api/scans

# Récupérer un scan spécifique
GET /api/scans/{id}

# Créer un nouveau scan
POST /api/scans
Content-Type: application/json
{
  "name": "Mon Scan",
  "targetPath": "/path/to/app"
}

# Supprimer un scan
DELETE /api/scans/{id}

# Exporter en CSV
GET /api/scans/{id}/export/csv

# Exporter en JSON
GET /api/scans/{id}/export/json

# Exporter en HTML
GET /api/scans/{id}/export/html
```

#### Exemples d'utilisation cURL:

```bash
# Lister tous les scans
curl http://localhost:8080/api/scans

# Créer un scan
curl -X POST http://localhost:8080/api/scans \
  -H "Content-Type: application/json" \
  -d '{"name":"Test","targetPath":"/home/user/app"}'

# Exporter en JSON
curl http://localhost:8080/api/scans/1/export/json > scan_report.json

# Exporter en CSV
curl http://localhost:8080/api/scans/1/export/csv > scan_report.csv
```

**Classes créées:**
- `ScanController.java` - Contrôleur REST principal
- `RestApiApplication.java` - Application Spring Boot
- `application.properties` - Configuration Spring
- `logback.xml` - Configuration logging

---

## 📁 Structure des fichiers créés

```
src/main/java/com/mobapp/inspector/
├── database/
│   ├── Scan.java
│   ├── ScanFinding.java
│   └── DatabaseService.java
├── service/
│   └── ExportService.java
├── api/
│   ├── ScanController.java
│   └── RestApiApplication.java
└── ui/
    └── ScanHistoryPanel.java

src/main/resources/
├── application.properties
└── logback.xml
```

---

## 🚀 Comment démarrer

### 1. Compiler le projet
```bash
mvn clean package
```

### 2. Lancer l'application JavaFX (mode UI)
```bash
mvn javafx:run
```

### 3. Lancer l'API REST séparément
```bash
mvn spring-boot:run -pl :secure-storage-inspector
```

### 4. Accéder à l'historique des scans
Dans l'UI JavaFX, ajouter un onglet "Scan History" pour voir l'historique.

---

## 🔧 Configuration avancée

### Variables d'environnement:
```bash
# Port de l'API
export SERVER_PORT=8080

# Chemin de la base de données
export DB_PATH=./secure_storage_inspector.db

# Niveau de log
export LOG_LEVEL=INFO
```

### Fichier application.properties personnalisé:
```properties
server.port=9000
logging.level.com.mobapp.inspector=DEBUG
spring.jpa.show-sql=true
```

---

## 📊 Cas d'usage

### Workflow complet:
1. **Scanner** → Lancer un scan de sécurité
2. **Visualiser** → Voir les findings dans l'UI
3. **Sauvegarder** → Les résultats sont sauvegardés auto dans la BD
4. **Consulter historique** → Voir tous les scans passés
5. **Exporter** → Générer des rapports (CSV/JSON/HTML)
6. **Intégrer** → Utiliser l'API REST pour l'automatisation

### Intégration CI/CD:
```bash
# Lancer un scan via l'API
SCAN_ID=$(curl -X POST http://localhost:8080/api/scans \
  -H "Content-Type: application/json" \
  -d '{"name":"CI-Build","targetPath":"/src"}' | jq -r '.id')

# Exporter le rapport
curl http://localhost:8080/api/scans/$SCAN_ID/export/html > report.html

# Utiliser le rapport dans le pipeline
```

---

## 🔐 Sécurité

- ✅ Base de données chiffrée (option SQLCipher)
- ✅ Masquage des données sensibles dans les exports
- ✅ CORS configuré (à adapter selon votre environnement)
- ✅ Logging sécurisé sans exposition de secrets
- ✅ Validations d'entrée

---

## 📈 Prochaines étapes (Phase 4+)

- [ ] Tests unitaires et intégration
- [ ] Dashboard avec graphiques
- [ ] Planification de scans (cron)
- [ ] Notifications email/Slack
- [ ] Gestion multi-utilisateur
- [ ] Analyse binaire/APK
- [ ] Plugin IDE (VS Code, IntelliJ)

---

## 🆘 Dépannage

### Erreur: "Base de données verrouillée"
```bash
# Supprimer le fichier de base de données et recommencer
rm secure_storage_inspector.db
```

### Erreur: "Port 8080 déjà utilisé"
```bash
# Changer le port dans application.properties
server.port=9000
```

### Logs d'erreur
```bash
# Consulter les logs
tail -f logs/secure-storage-inspector.log
```

---

## 📞 Support

Pour des questions ou des problèmes:
1. Vérifier les logs en `logs/secure-storage-inspector.log`
2. Consulter la documentation de l'API en `docs/API.md`
3. Vérifier que la base de données existe et est accessible
4. S'assurer que Java 17+ est installé

---

**Version:** 1.0  
**Date de mise à jour:** May 14, 2026
