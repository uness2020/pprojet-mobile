#!/bin/bash
# Script de test de l'API REST - Secure Storage Inspector
# Teste les endpoints principaux

BASE_URL="http://localhost:8080/api"

print_section() {
    echo ""
    echo "============================================================"
    echo "  $1"
    echo "============================================================"
    echo ""
}

# Test 1: Connection
print_section "1. Test de Connexion"
if curl -s "$BASE_URL/scans" -o /dev/null -w "%{http_code}" | grep -q "200"; then
    echo "✅ API est accessible!"
    echo "   URL: $BASE_URL/scans"
else
    echo "❌ Impossible de se connecter à l'API"
    echo "   Assurez-vous que l'API est en cours d'exécution"
    echo "   Commande: mvn exec:java -Dexec.mainClass='com.mobapp.inspector.api.RestApiApplication'"
    exit 1
fi

# Test 2: Récupérer les scans
print_section "2. Récupérer tous les scans"
curl -s "$BASE_URL/scans" \
    -H "Content-Type: application/json" | jq '.'

# Test 3: Créer un scan
print_section "3. Créer un nouveau scan"
SCAN_ID=$(curl -s -X POST "$BASE_URL/scans" \
    -H "Content-Type: application/json" \
    -d '{"name":"Test Bash","targetPath":"/home/user/app"}' | jq -r '.id')

if [ "$SCAN_ID" != "null" ] && [ ! -z "$SCAN_ID" ]; then
    echo "✅ Scan créé avec ID: $SCAN_ID"
    
    # Test 4: Récupérer le scan
    print_section "4. Récupérer le scan #$SCAN_ID"
    curl -s "$BASE_URL/scans/$SCAN_ID" \
        -H "Content-Type: application/json" | jq '.'
    
    # Test 5: Exporter en JSON
    print_section "5. Exporter le scan en JSON"
    curl -s "$BASE_URL/scans/$SCAN_ID/export/json" \
        -H "Content-Type: application/json" | jq '.'
else
    echo "❌ Impossible de créer un scan"
fi

print_section "✅ TESTS COMPLETES"
