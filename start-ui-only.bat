@echo off
REM Lance l'interface JavaFX directement avec le JAR compile
REM Secure Storage Inspector - UI Standalone

echo.
echo ============================================================
echo Secure Storage Inspector - Interface JavaFX (Standalone)
echo ============================================================
echo.

if not exist "target\secure-storage-inspector-1.0-SNAPSHOT-fat.jar" (
    echo [ERREUR] JAR non trouvee. Veuillez d'abord lancer compile-and-prepare.bat
    echo.
    pause
    exit /b 1
)

cd target

echo Lancement de l'interface JavaFX...
echo.

java -Djavafx.graphics.forceRenderingPipeline=sw ^
    --enable-native-access=ALL-UNNAMED ^
    --add-opens=java.base/java.lang=ALL-UNNAMED ^
    --add-opens=java.base/java.util=ALL-UNNAMED ^
    -cp secure-storage-inspector-1.0-SNAPSHOT-fat.jar ^
    com.mobapp.inspector.StorageInspectorLauncher

pause
