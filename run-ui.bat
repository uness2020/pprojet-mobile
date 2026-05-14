@echo off
REM Script de lancement de l'interface JavaFX
REM Secure Storage Inspector UI

echo.
echo ============================================================
echo Secure Storage Inspector - UI (JavaFX)
echo ============================================================
echo.

set JAVAFX_OPTIONS=-Djavafx.graphics.forceRenderingPipeline=sw --enable-native-access=ALL-UNNAMED

echo Compilation et lancement de l'UI JavaFX...
echo.

mvn clean compile javafx:run %JAVAFX_OPTIONS%

if errorlevel 1 (
    echo.
    echo [ERREUR] Echec du lancement de l'UI
    echo.
    pause
) else (
    echo.
    echo [SUCCESS] Interface JavaFX fermee
    echo.
)
