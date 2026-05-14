#!/bin/bash
# Script de lancement de l'UI et de l'API REST en parallèle
# Secure Storage Inspector Complete

echo ""
echo "============================================================"
echo "Secure Storage Inspector - UI + API"
echo "============================================================"
echo ""

# Compiler le projet une fois
echo "Compilation du projet..."
mvn clean compile
if [ $? -ne 0 ]; then
    echo "[ERREUR] La compilation a échoué"
    exit 1
fi

echo ""
echo "============================================================"
echo "Démarrage de l'API REST en arrière-plan..."
echo "API disponible sur: http://localhost:8080/api"
echo "============================================================"
echo ""

# Lancer l'API dans un processus en arrière-plan
mvn exec:java -Dexec.mainClass="com.mobapp.inspector.api.RestApiApplication" &
API_PID=$!

sleep 3

echo ""
echo "============================================================"
echo "Démarrage de l'interface JavaFX..."
echo "============================================================"
echo ""

# Lancer l'UI
mvn javafx:run

echo ""
echo "[INFO] Application fermée"
echo ""

# Arrêter l'API si elle est toujours en cours
if kill -0 $API_PID 2>/dev/null; then
    kill $API_PID
    echo "[INFO] API REST arrêtée"
fi
