@echo off
REM Script de lancement simple avec les JARs pre-compiles
REM Secure Storage Inspector - Mode Debug

echo.
echo ============================================================
echo Secure Storage Inspector - Lancement Simple (Compile d'abord)
echo ============================================================
echo.

REM D'abord compiler
echo Step 1: Compilation du projet...
mvn clean package -DskipTests

if errorlevel 1 (
    echo.
    echo [ERREUR] La compilation a echouee
    echo.
    pause
    exit /b 1
)

echo.
echo Step 2: JARs crees avec succes!
echo.
echo Fichiers disponibles:
echo - target\secure-storage-inspector-1.0-SNAPSHOT-fat.jar (JavaFX + API)
echo.

echo Maintenant, ouvrez 2 terminaux et executez:
echo.
echo Terminal 1 - API REST:
echo   cd target
echo   java -cp secure-storage-inspector-1.0-SNAPSHOT-fat.jar com.mobapp.inspector.api.RestApiApplication
echo.
echo Terminal 2 - Interface JavaFX:
echo   cd target
echo   java -Djavafx.graphics.forceRenderingPipeline=sw -cp secure-storage-inspector-1.0-SNAPSHOT-fat.jar com.mobapp.inspector.StorageInspectorLauncher
echo.

pause
