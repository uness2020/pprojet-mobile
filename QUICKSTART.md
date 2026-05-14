# 🚀 Guide de Démarrage Rapide - Secure Storage Inspector

## ✅ Compilation Réussie!

Les fichiers JAR suivants ont été créés:
- `target/secure-storage-inspector-1.0-SNAPSHOT-fat.jar` (69 MB - Complet)
- `target/secure-storage-inspector-1.0-SNAPSHOT.jar` (127 KB - Principal)

---

## 🎯 OPTION 1: Lancer via Script Batch (Windows) ⭐ RECOMMANDÉ

### Étape 1: Première compilation
```bash
double-cliquez: compile-and-prepare.bat
```

Cela va:
- Compiler le projet
- Créer les JARs
- Afficher les commandes de lancement

### Étape 2: Lancer l'API REST
```bash
double-cliquez: start-api-only.bat
```
✅ API disponible sur `http://localhost:8080/api`

### Étape 3: Lancer l'Interface JavaFX (Nouveau terminal)
```bash
double-cliquez: start-ui-only.bat
```

---

## 🎯 OPTION 2: Via PowerShell/Terminal (Recommandé pour les devs)

### Terminal 1 - API REST
```powershell
mvn exec:java -Dexec.mainClass="com.mobapp.inspector.api.RestApiApplication"
```

### Terminal 2 - Interface JavaFX (dans un nouveau terminal)
```powershell
mvn javafx:run
```

---

## 🎯 OPTION 3: Lancer directement les JARs

### Terminal 1 - API REST
```bash
cd target
java -cp secure-storage-inspector-1.0-SNAPSHOT-fat.jar com.mobapp.inspector.api.RestApiApplication
```

### Terminal 2 - Interface JavaFX
```bash
cd target
java -Djavafx.graphics.forceRenderingPipeline=sw ^
    --enable-native-access=ALL-UNNAMED ^
    --add-opens=java.base/java.lang=ALL-UNNAMED ^
    --add-opens=java.base/java.util=ALL-UNNAMED ^
    -cp secure-storage-inspector-1.0-SNAPSHOT-fat.jar ^
    com.mobapp.inspector.StorageInspectorLauncher
```

---

## 🧪 Tester l'API REST

Une fois que l'API est en cours d'exécution:

### Avec cURL
```bash
# Lister tous les scans
curl http://localhost:8080/api/scans

# Créer un scan
curl -X POST http://localhost:8080/api/scans \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Scan","targetPath":"C:\\Users\\youne\\Downloads"}'
```

### Avec PowerShell
```powershell
# Lister les scans
Invoke-WebRequest -Uri http://localhost:8080/api/scans

# Créer un scan
$body = @{name="Test Scan"; targetPath="C:\Users\youne\Downloads"} | ConvertTo-Json
Invoke-WebRequest -Uri http://localhost:8080/api/scans -Method Post -Body $body -ContentType "application/json"
```

---

## 📊 Flux de travail complet

```
1. Démarrer l'API REST (Terminal 1)
   ↓
2. Démarrer l'UI JavaFX (Terminal 2)
   ↓
3. Sélectionner un répertoire dans l'UI
   ↓
4. Cliquer "Scan"
   ↓
5. Attendre les résultats (auto-sauvegardés en BD)
   ↓
6. Visualiser dans "Scan History"
   ↓
7. Exporter en CSV/JSON/HTML
   ↓
8. (Optionnel) Utiliser l'API REST pour les scans automatisés
```

---

## ⚠️ Dépannage

### Erreur: "Toolkit not found" (JavaFX)
✅ **Solution:** C'est normal en environnement sans affichage graphique.
   Utilisez `-Djavafx.graphics.forceRenderingPipeline=sw` (déjà présent dans les scripts)

### Erreur: "Port 8080 déjà utilisé"
✅ **Solution:** Changer le port dans `src/main/resources/application.properties`:
   ```properties
   server.port=9000
   ```

### Erreur: "Base de données verrouillée"
✅ **Solution:** Supprimer le fichier DB et relancer:
   ```bash
   rm secure_storage_inspector.db
   ```

### Les JARs ne sont pas créés
✅ **Solution:** Relancer la compilation:
   ```bash
   mvn clean package -DskipTests
   ```

---

## 📚 Documentation supplémentaire

- **IMPLEMENTATION.md** - Guide complet d'implémentation
- **API.md** - Documentation détaillée de l'API REST
- **RÉSUMÉ_IMPLÉMENTATION.md** - Résumé des fonctionnalités

---

## 🔒 Sécurité

⚠️ **IMPORTANT:** Cette application est actuellement en mode développement.

Pour la production:
- [ ] Ajouter authentification JWT à l'API
- [ ] Chiffrer la base de données
- [ ] Configurer HTTPS/TLS
- [ ] Ajouter rate limiting
- [ ] Valider toutes les entrées utilisateur
- [ ] Sauvegarder les logs de manière sécurisée

---

## 🎓 Architecture

```
Secure Storage Inspector v1.0
│
├─ Terminal 1: API REST (Spring Boot)
│  └─ Port: 8080
│  └─ Endpoints: /api/scans/*
│
├─ Terminal 2: UI JavaFX
│  └─ Application graphique
│  └─ Explorateur de fichiers
│  └─ Scanner de sécurité
│  └─ Historique des scans
│
└─ Base de données
   └─ SQLite: secure_storage_inspector.db
```

---

## ✨ Prochaines étapes

- [ ] Tests unitaires (JUnit5)
- [ ] Dashboard avec graphiques
- [ ] Authentification JWT
- [ ] Notifications email/Slack
- [ ] Plugin IDE (VS Code, IntelliJ)
- [ ] Analyse binaire/APK
- [ ] Planification de scans (Quartz)

---

**✅ Vous êtes prêt à démarrer!**

Commencez par cliquer sur `compile-and-prepare.bat` si vous êtes sous Windows.

Ou utilisez les commandes Maven si vous êtes sur Mac/Linux.

**Besoin d'aide?** Consultez la documentation ou les logs en `logs/secure-storage-inspector.log`

---

**Version:** 1.0  
**Date:** May 14, 2026  
**Statut:** ✅ Prêt pour utilisation
