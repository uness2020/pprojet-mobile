#!/bin/bash
# Script de lancement de l'API REST
# Secure Storage Inspector REST API

echo ""
echo "============================================================"
echo "Secure Storage Inspector - REST API"
echo "============================================================"
echo ""
echo "API disponible sur: http://localhost:8080/api"
echo ""

echo "Compilation et lancement de l'API REST..."
echo ""

mvn clean compile exec:java -Dexec.mainClass="com.mobapp.inspector.api.RestApiApplication"

if [ $? -eq 0 ]; then
    echo ""
    echo "[SUCCESS] API REST fermée"
    echo ""
else
    echo ""
    echo "[ERREUR] Échec du lancement de l'API"
    echo ""
fi
