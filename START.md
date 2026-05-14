# 🚀 DÉMARRAGE EN 3 ÉTAPES

## Prérequis
- ✅ Java 17+ installé (`java -version`)
- ✅ Maven 3.6+ installé (`mvn --version`)
- ✅ Projet compilé (voir ci-dessous)

---

## ÉTAPE 1️⃣: Compiler (Une fois seulement)

### Windows (PowerShell ou CMD)
```bash
cd C:\Users\youne\Downloads\mobapp-storage-inspector-1.0.0
mvn clean package -DskipTests
```

### Mac/Linux
```bash
cd ~/Downloads/mobapp-storage-inspector-1.0.0
mvn clean package -DskipTests
```

**Résultat:** Fichier `target/secure-storage-inspector-1.0-SNAPSHOT-fat.jar` créé ✅

---

## ÉTAPE 2️⃣: Lancer l'API REST

### Windows PowerShell (Terminal 1)
```powershell
mvn exec:java -Dexec.mainClass="com.mobapp.inspector.api.RestApiApplication"
```

Ou double-cliquez: `start-api-only.bat`

### Mac/Linux (Terminal 1)
```bash
mvn exec:java -Dexec.mainClass="com.mobapp.inspector.api.RestApiApplication"
```

✅ **L'API est maintenant active sur:** `http://localhost:8080/api`

---

## ÉTAPE 3️⃣: Lancer l'Interface (Terminal 2)

### Windows PowerShell (Terminal 2)
```powershell
mvn javafx:run
```

Ou double-cliquez: `start-ui-only.bat`

### Mac/Linux (Terminal 2)
```bash
mvn javafx:run
```

✅ **L'interface JavaFX devrait s'afficher!**

---

## ✨ C'est tout! Vous pouvez maintenant:

1. **Sélectionner un dossier** à scanner dans l'interface
2. **Cliquer sur "Scan"** pour analyser les fichiers
3. **Voir les résultats** dans le Security Scanner tab
4. **Consulter l'historique** dans Scan History tab
5. **Exporter les rapports** en CSV/JSON/HTML

---

## 🧪 Tester l'API (Optionnel)

### Avec cURL (Terminal 3)
```bash
# Lister les scans
curl http://localhost:8080/api/scans

# Créer un scan
curl -X POST http://localhost:8080/api/scans \
  -H "Content-Type: application/json" \
  -d '{"name":"Mon Scan","targetPath":"C:\\Users\\youne\\Downloads"}'

# Exporter en JSON
curl http://localhost:8080/api/scans/1/export/json > rapport.json
```

### Avec Python
```bash
python test-api.py
```

---

## ❌ Si ça ne marche pas...

### Erreur: "Toolkit not found" (JavaFX)
→ Normal en environnement sans interface graphique (VM, SSH, etc.)
→ Utilisez une machine avec affichage graphique

### Erreur: "Port 8080 déjà utilisé"
```bash
# Utiliser un autre port
mvn exec:java -Dexec.mainClass="com.mobapp.inspector.api.RestApiApplication" -Dserver.port=9000
```

### Erreur: "mvn command not found"
→ Maven n'est pas installé
→ Installez Maven: https://maven.apache.org/download.cgi

### Les JARs n'existent pas
```bash
mvn clean package -DskipTests
```

---

## 📚 Documentation complète

- **QUICKSTART.md** - Guide complet avec tous les détails
- **IMPLEMENTATION.md** - Architecture et implémentation
- **API.md** - Documentation API REST
- **RÉSUMÉ_IMPLÉMENTATION.md** - Fonctionnalités ajoutées

---

## 📊 Architecture

```
Terminal 1: API REST (8080)
Terminal 2: UI JavaFX
BD Locale: secure_storage_inspector.db
```

---

**Vous êtes prêt! 🎉**

**Commencez par l'ÉTAPE 1 ci-dessus!**
