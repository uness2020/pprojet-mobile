#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Script de test de l'API REST - Secure Storage Inspector
Teste les endpoints principaux de l'API
"""

import requests
import json
import sys
import time

BASE_URL = "http://localhost:8080/api"
HEADERS = {"Content-Type": "application/json"}

def print_section(title):
    """Afficher un titre de section"""
    print(f"\n{'='*60}")
    print(f"  {title}")
    print(f"{'='*60}\n")

def test_connection():
    """Tester la connexion à l'API"""
    print_section("1. Test de Connexion")
    try:
        response = requests.get(f"{BASE_URL}/scans", timeout=5)
        if response.status_code == 200:
            print("✅ API est accessible!")
            print(f"   URL: {BASE_URL}/scans")
            print(f"   Status: {response.status_code}")
            return True
        else:
            print(f"❌ Erreur: Status {response.status_code}")
            return False
    except requests.exceptions.ConnectionError:
        print("❌ Impossible de se connecter à l'API")
        print(f"   Assurez-vous que l'API est en cours d'exécution sur {BASE_URL}")
        return False
    except Exception as e:
        print(f"❌ Erreur: {str(e)}")
        return False

def test_get_scans():
    """Récupérer tous les scans"""
    print_section("2. Récupérer tous les scans")
    try:
        response = requests.get(f"{BASE_URL}/scans", headers=HEADERS)
        print(f"Status: {response.status_code}")
        
        if response.status_code == 200:
            scans = response.json()
            print(f"✅ Nombre de scans: {len(scans)}")
            
            if scans:
                for scan in scans[:3]:  # Afficher les 3 premiers
                    print(f"\n  ID: {scan.get('id')}")
                    print(f"  Name: {scan.get('name')}")
                    print(f"  Path: {scan.get('targetPath')}")
                    print(f"  Status: {scan.get('status')}")
                    print(f"  Total Issues: {scan.get('totalIssues', 0)}")
            else:
                print("  (Aucun scan pour le moment)")
        else:
            print(f"❌ Erreur {response.status_code}")
            print(response.text)
    except Exception as e:
        print(f"❌ Erreur: {str(e)}")

def test_create_scan():
    """Créer un nouveau scan"""
    print_section("3. Créer un nouveau scan")
    
    payload = {
        "name": "Test Scan API",
        "targetPath": "C:\\Users\\youne\\Downloads"
    }
    
    print(f"Payload: {json.dumps(payload, indent=2)}")
    
    try:
        response = requests.post(
            f"{BASE_URL}/scans",
            headers=HEADERS,
            json=payload
        )
        
        print(f"Status: {response.status_code}")
        
        if response.status_code == 201:
            result = response.json()
            print(f"✅ Scan créé avec succès!")
            print(f"   ID: {result.get('id')}")
            print(f"   Message: {result.get('message')}")
            return result.get('id')
        else:
            print(f"❌ Erreur {response.status_code}")
            print(response.text)
            return None
    except Exception as e:
        print(f"❌ Erreur: {str(e)}")
        return None

def test_get_scan(scan_id):
    """Récupérer un scan spécifique"""
    print_section(f"4. Récupérer le scan #{scan_id}")
    
    try:
        response = requests.get(f"{BASE_URL}/scans/{scan_id}", headers=HEADERS)
        print(f"Status: {response.status_code}")
        
        if response.status_code == 200:
            scan = response.json()
            print(f"✅ Scan récupéré!")
            print(json.dumps(scan, indent=2, default=str))
        else:
            print(f"❌ Erreur {response.status_code}")
            if response.status_code == 404:
                print("   Scan non trouvé")
    except Exception as e:
        print(f"❌ Erreur: {str(e)}")

def test_export(scan_id, format_type):
    """Exporter un scan"""
    print_section(f"5. Exporter le scan #{scan_id} en {format_type.upper()}")
    
    try:
        url = f"{BASE_URL}/scans/{scan_id}/export/{format_type}"
        response = requests.get(url, headers=HEADERS)
        
        print(f"Status: {response.status_code}")
        
        if response.status_code == 200:
            result = response.json()
            print(f"✅ Export {format_type.upper()} créé!")
            print(f"   Fichier: {result.get('filepath')}")
            print(f"   Message: {result.get('message')}")
        else:
            print(f"❌ Erreur {response.status_code}")
            print(response.text)
    except Exception as e:
        print(f"❌ Erreur: {str(e)}")

def main():
    """Fonction principale"""
    print("\n" + "="*60)
    print("  SECURE STORAGE INSPECTOR - API TEST")
    print("="*60)
    
    # Test 1: Connexion
    if not test_connection():
        print("\n❌ Impossible de se connecter à l'API")
        print("Assurez-vous qu'elle est lancée avec:")
        print("  mvn exec:java -Dexec.mainClass=\"com.mobapp.inspector.api.RestApiApplication\"")
        sys.exit(1)
    
    time.sleep(1)
    
    # Test 2: Récupérer les scans
    test_get_scans()
    
    # Test 3: Créer un scan
    scan_id = test_create_scan()
    
    if scan_id:
        time.sleep(1)
        
        # Test 4: Récupérer le scan
        test_get_scan(scan_id)
        
        # Test 5: Exporter en JSON
        test_export(scan_id, "json")
        
        # Test 6: Exporter en CSV
        test_export(scan_id, "csv")
        
        # Test 7: Exporter en HTML
        test_export(scan_id, "html")
    
    print("\n" + "="*60)
    print("  ✅ TESTS COMPLETES")
    print("="*60 + "\n")

if __name__ == "__main__":
    main()
