@echo off
REM Script de lancement de l'API REST
REM Secure Storage Inspector REST API

echo.
echo ============================================================
echo Secure Storage Inspector - REST API
echo ============================================================
echo.
echo API disponible sur: http://localhost:8080/api
echo.

echo Compilation et lancement de l'API REST...
echo.

mvn clean compile exec:java -Dexec.mainClass="com.mobapp.inspector.api.RestApiApplication"

if errorlevel 1 (
    echo.
    echo [ERREUR] Echec du lancement de l'API
    echo.
    pause
) else (
    echo.
    echo [SUCCESS] API REST fermee
    echo.
)
