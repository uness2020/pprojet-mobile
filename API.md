# API REST - Secure Storage Inspector

## Base URL
```
http://localhost:8080/api
```

## Authentication
Actuellement sans authentification (à ajouter en Phase 4)

## Headers requis
```
Content-Type: application/json
Accept: application/json
```

---

## Endpoints

### 1. Lister tous les scans
```http
GET /scans HTTP/1.1
Host: localhost:8080
Accept: application/json
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "name": "Scan 1",
    "targetPath": "/path/to/app",
    "scanDate": "2024-05-14T10:30:00",
    "totalFiles": 150,
    "criticalCount": 2,
    "highCount": 5,
    "mediumCount": 10,
    "lowCount": 15,
    "infoCount": 20,
    "totalIssues": 52,
    "status": "COMPLETED"
  }
]
```

---

### 2. Récupérer un scan spécifique
```http
GET /scans/{id} HTTP/1.1
Host: localhost:8080
Accept: application/json
```

**Paramètres:**
- `id` (path) - ID du scan

**Response (200 OK):**
```json
{
  "id": 1,
  "name": "Scan 1",
  "targetPath": "/path/to/app",
  "scanDate": "2024-05-14T10:30:00",
  "totalFiles": 150,
  "criticalCount": 2,
  "highCount": 5,
  "status": "COMPLETED",
  "findings": [
    {
      "id": 1,
      "scanId": 1,
      "findingType": "Google API Key",
      "severity": "CRITICAL",
      "filePath": "/path/to/config.json",
      "lineNumber": 42,
      "matchedText": "AIza[...masked...]"
    }
  ]
}
```

**Response (404 Not Found):**
```json
{
  "error": "Scan not found"
}
```

---

### 3. Créer un nouveau scan
```http
POST /scans HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "name": "Mon Scan",
  "targetPath": "/home/user/myapp"
}
```

**Body:**
```json
{
  "name": "string - Nom descriptif du scan",
  "targetPath": "string - Chemin absolu à scanner"
}
```

**Response (201 Created):**
```json
{
  "id": 5,
  "message": "Scan created successfully"
}
```

---

### 4. Supprimer un scan
```http
DELETE /scans/{id} HTTP/1.1
Host: localhost:8080
```

**Paramètres:**
- `id` (path) - ID du scan à supprimer

**Response (200 OK):**
```json
{
  "message": "Scan deleted successfully"
}
```

**Response (404 Not Found):**
```json
{
  "error": "Scan not found"
}
```

---

### 5. Exporter un scan en CSV
```http
GET /scans/{id}/export/csv HTTP/1.1
Host: localhost:8080
```

**Paramètres:**
- `id` (path) - ID du scan

**Response (200 OK):**
```json
{
  "filepath": "/home/user/Downloads/scan_5_1715772600000.csv",
  "message": "CSV export completed"
}
```

**Contenu du fichier CSV:**
```csv
Finding ID,Finding Type,Severity,File Path,Line Number,Matched Text
1,Google API Key,CRITICAL,/path/config.json,42,AIza[...]
2,Firebase URL,HIGH,/path/settings.xml,15,firebaseio.com
```

---

### 6. Exporter un scan en JSON
```http
GET /scans/{id}/export/json HTTP/1.1
Host: localhost:8080
```

**Paramètres:**
- `id` (path) - ID du scan

**Response (200 OK):**
```json
{
  "filepath": "/home/user/Downloads/scan_5_1715772600000.json",
  "message": "JSON export completed"
}
```

**Contenu du fichier JSON:**
```json
{
  "id": 5,
  "name": "Mon Scan",
  "targetPath": "/path/to/app",
  "scanDate": "2024-05-14 10:30:00",
  "totalFiles": 150,
  "totalIssues": 52,
  "criticalCount": 2,
  "highCount": 5,
  "mediumCount": 10,
  "lowCount": 15,
  "infoCount": 20,
  "status": "COMPLETED",
  "findings": [
    {
      "id": 1,
      "scanId": 5,
      "findingType": "Google API Key",
      "severity": "CRITICAL",
      "filePath": "/path/config.json",
      "lineNumber": 42,
      "matchedText": "AIza[...masked...]",
      "context": null,
      "remediation": null
    }
  ]
}
```

---

### 7. Exporter un scan en HTML
```http
GET /scans/{id}/export/html HTTP/1.1
Host: localhost:8080
```

**Paramètres:**
- `id` (path) - ID du scan

**Response (200 OK):**
```json
{
  "filepath": "/home/user/Downloads/scan_5_1715772600000.html",
  "message": "HTML export completed"
}
```

**Résultat:** Un fichier HTML interactif avec:
- Métadonnées du scan
- Résumé des trouvailles par sévérité
- Tableau détaillé avec tri/recherche
- Styling professionnel

---

## Codes de réponse HTTP

| Code | Signification |
|------|---------------|
| 200  | OK - Requête réussie |
| 201  | Created - Ressource créée |
| 400  | Bad Request - Données invalides |
| 404  | Not Found - Ressource non trouvée |
| 500  | Internal Server Error - Erreur serveur |

---

## Exemples cURL

### Lister tous les scans
```bash
curl -X GET http://localhost:8080/api/scans \
  -H "Accept: application/json"
```

### Créer un scan
```bash
curl -X POST http://localhost:8080/api/scans \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Scan",
    "targetPath": "/home/user/project"
  }'
```

### Récupérer un scan
```bash
curl -X GET http://localhost:8080/api/scans/1 \
  -H "Accept: application/json"
```

### Exporter en JSON
```bash
curl -X GET http://localhost:8080/api/scans/1/export/json \
  -o report.json
```

### Exporter en CSV
```bash
curl -X GET http://localhost:8080/api/scans/1/export/csv \
  -o report.csv
```

### Supprimer un scan
```bash
curl -X DELETE http://localhost:8080/api/scans/1
```

---

## Exemples avec Python

```python
import requests
import json

BASE_URL = "http://localhost:8080/api"

# Lister les scans
response = requests.get(f"{BASE_URL}/scans")
scans = response.json()
print(json.dumps(scans, indent=2))

# Créer un scan
new_scan = {
    "name": "Python Test",
    "targetPath": "/home/user/app"
}
response = requests.post(f"{BASE_URL}/scans", json=new_scan)
scan_id = response.json()["id"]

# Exporter en JSON
response = requests.get(f"{BASE_URL}/scans/{scan_id}/export/json")
print(response.json())

# Supprimer un scan
response = requests.delete(f"{BASE_URL}/scans/{scan_id}")
print(response.json())
```

---

## Exemples avec JavaScript/Fetch

```javascript
const BASE_URL = 'http://localhost:8080/api';

// Lister les scans
fetch(`${BASE_URL}/scans`)
  .then(r => r.json())
  .then(scans => console.log(scans));

// Créer un scan
fetch(`${BASE_URL}/scans`, {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    name: 'JS Test',
    targetPath: '/path/to/app'
  })
})
  .then(r => r.json())
  .then(data => console.log(data));

// Exporter en HTML
fetch(`${BASE_URL}/scans/1/export/html`)
  .then(r => r.json())
  .then(data => console.log(data));
```

---

## Erreurs courantes

### 404 Not Found
```json
{
  "timestamp": "2024-05-14T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Scan with id 999 not found"
}
```

### 400 Bad Request
```json
{
  "timestamp": "2024-05-14T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid targetPath: path does not exist"
}
```

### 500 Internal Server Error
```json
{
  "timestamp": "2024-05-14T10:30:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Database connection failed"
}
```

---

## Performance

- **Max items par page:** 1000
- **Timeout requête:** 30 secondes
- **Taille max payload:** 50 MB
- **Rate limit:** Non activé (à implémenter)

---

## Sécurité

- ✅ CORS activé pour développement
- ⚠️ À ajouter: Authentification JWT
- ⚠️ À ajouter: Rate limiting
- ⚠️ À ajouter: Validation des entrées
- ⚠️ À ajouter: HTTPS/TLS

---

**API Version:** 1.0  
**Dernière mise à jour:** May 14, 2026
