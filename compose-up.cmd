@echo off
REM Start Kafka Docker environment (preserves existing data/volumes)
REM Use compose-reset.cmd to start fresh with no persisted data
setlocal

cd /d "%~dp0compose"

echo Starting Kafka Docker environment...
echo (Use compose-reset.cmd to start fresh with no persisted data)
echo.

docker compose -f kafka-ssl-compose.yml up -d
if errorlevel 1 (
    echo Failed to start Docker environment!
    exit /b 1
)

echo.
echo Kafka environment started!
echo.
echo Services:
echo   Kafka:           localhost:9092 (plaintext), localhost:19092 (SSL)
echo   Schema Registry: localhost:8281 (HTTP), localhost:8285 (HTTPS)
echo   Kafka Connect 0: localhost:8082 (HTTP), localhost:8083 (HTTPS)
echo   Kafka Connect 1: localhost:8084 (HTTP), localhost:8085 (HTTPS)
echo   ksqlDB:          localhost:8089 (HTTP), localhost:8088 (HTTPS)
echo.
echo To view logs: docker compose -f compose/kafka-ssl-compose.yml logs -f
echo To stop:      compose-down.cmd
