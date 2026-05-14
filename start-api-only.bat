@echo off
REM Lance l'API REST directement avec le JAR compile
REM Secure Storage Inspector - REST API Standalone

echo.
echo ============================================================
echo Secure Storage Inspector - API REST (Standalone)
echo ============================================================
echo.
echo API disponible sur: http://localhost:8080/api
echo.

if not exist "target\secure-storage-inspector-1.0-SNAPSHOT-fat.jar" (
    echo [ERREUR] JAR non trouvee. Veuillez d'abord lancer compile-and-prepare.bat
    echo.
    pause
    exit /b 1
)

cd target

echo Lancement de l'API REST...
echo.

java -cp secure-storage-inspector-1.0-SNAPSHOT-fat.jar ^
    org.springframework.boot.loader.JarLauncher

pause
