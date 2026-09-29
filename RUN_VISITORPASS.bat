@echo off
title VisitorPass - Gated Community System
color 0A

echo ========================================================
echo       STARTING VISITORPASS APPLICATION (PORT 8080)
echo ========================================================
echo.

cd /d "%~dp0"

echo [1/3] Checking MySQL Service...
sc query MySQL80 | find "RUNNING" >nul
if %ERRORLEVEL% NEQ 0 (
    echo Starting MySQL service...
    net start MySQL80 >nul 2>&1
)
echo MySQL Service is Ready.
echo.

echo [2/3] Launching Web Browser at http://localhost:8080 ...
start "" "http://localhost:8080"
echo.

echo [3/3] Starting Spring Boot Application...
echo.
java -jar target\VisitorPass-0.0.1-SNAPSHOT.jar

pause
