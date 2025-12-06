@echo off
REM Stop Kafka Docker environment (preserves volumes/data)
REM Use compose-reset.cmd to remove all data and start fresh
setlocal

cd /d "%~dp0compose"

echo Stopping Kafka Docker environment...
docker compose -f kafka-ssl-compose.yml down
if errorlevel 1 (
    echo Failed to stop Docker environment!
    exit /b 1
)

echo.
echo Kafka environment stopped.
echo Data volumes are preserved. Use compose-reset.cmd to remove all data.
