#!/bin/bash
# Script de lancement de l'interface JavaFX
# Secure Storage Inspector UI

echo ""
echo "============================================================"
echo "Secure Storage Inspector - UI (JavaFX)"
echo "============================================================"
echo ""

export JAVAFX_OPTIONS="-Djavafx.graphics.forceRenderingPipeline=sw --enable-native-access=ALL-UNNAMED"

echo "Compilation et lancement de l'UI JavaFX..."
echo ""

mvn clean compile javafx:run $JAVAFX_OPTIONS

if [ $? -eq 0 ]; then
    echo ""
    echo "[SUCCESS] Interface JavaFX fermée"
    echo ""
else
    echo ""
    echo "[ERREUR] Échec du lancement de l'UI"
    echo ""
fi
