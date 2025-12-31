@echo off
REM Reset Kafka Docker environment - removes all containers, volumes, and data
REM WARNING: This will delete all Kafka topics, messages, schemas, and connector state!
setlocal

cd /d "%~dp0compose"

echo WARNING: This will remove all Kafka data including:
echo   - All topics and messages
echo   - All schemas in Schema Registry
echo   - All Kafka Connect connector configurations
echo   - All ksqlDB streams and tables
echo.

REM Check for --force flag
if "%1"=="--force" goto :confirmed
if "%1"=="-f" goto :confirmed

set /p CONFIRM="Are you sure you want to continue? (y/N) "
if /i not "%CONFIRM%"=="y" (
    echo Aborted.
    exit /b 0
)

:confirmed
echo Stopping and removing all containers and volumes...
docker compose -f kafka-ssl-compose.yml down -v --remove-orphans
if errorlevel 1 (
    echo Failed to stop Docker environment!
    exit /b 1
)

echo.
echo Starting fresh Kafka environment...
docker compose -f kafka-ssl-compose.yml up -d
if errorlevel 1 (
    echo Failed to start Docker environment!
    exit /b 1
)

echo.
echo Kafka environment reset and started fresh!
echo.
echo Services:
echo   Kafka 0:         localhost:9092 (plaintext), localhost:19092 (SSL)
echo                    localhost:9094 (SASL_PLAINTEXT), localhost:19094 (SASL_SSL)
echo   Kafka 1:         localhost:9192 (plaintext), localhost:19192 (SSL)
echo                    localhost:9194 (SASL_PLAINTEXT), localhost:19194 (SASL_SSL)
echo   Schema Registry: localhost:8281 (HTTP), localhost:8285 (HTTPS)
echo   Kafka Connect 0: localhost:8082 (HTTP), localhost:8083 (HTTPS)
echo   Kafka Connect 1: localhost:8084 (HTTP), localhost:8085 (HTTPS)
echo   ksqlDB:          localhost:8088 (HTTP), localhost:8089 (HTTPS)
echo.
echo SASL Users: admin/admin-secret, client/client-secret
echo.
echo To view logs: docker compose -f compose/kafka-ssl-compose.yml logs -f
echo To stop:      compose-down.cmd
