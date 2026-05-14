@echo off
REM Script de lancement de l'UI et de l'API REST en parallele
REM Secure Storage Inspector Complete

echo.
echo ============================================================
echo Secure Storage Inspector - UI + API
echo ============================================================
echo.

REM Compiler le projet une fois
echo Compilation du projet...
mvn clean compile
if errorlevel 1 (
    echo [ERREUR] La compilation a echouee
    pause
    exit /b 1
)

echo.
echo ============================================================
echo Demarrage de l'API REST en arriere-plan...
echo API disponible sur: http://localhost:8080/api
echo ============================================================
echo.

REM Lancer l'API dans une nouvelle fenetre
start "Secure Storage Inspector - API REST" cmd /k "cd /d %CD% && mvn exec:java -Dexec.mainClass=com.mobapp.inspector.api.RestApiApplication"

timeout /t 3 /nobreak

echo.
echo ============================================================
echo Demarrage de l'interface JavaFX...
echo ============================================================
echo.

REM Lancer l'UI dans la fenetre courante
mvn javafx:run

echo.
echo [INFO] Application fermee
echo.
pause
