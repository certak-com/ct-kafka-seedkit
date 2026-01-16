@echo off
REM Start Kafka Docker environment (preserves existing data/volumes)
REM Use compose-reset.cmd to start fresh with no persisted data
setlocal

cd /d "%~dp0compose"

echo Starting Kafka Docker environment...
echo (Use compose-reset.cmd to start fresh with no persisted data)
echo.

docker compose -f kafka-confluent-environment-ssl.yml up -d
if errorlevel 1 (
    echo Failed to start Docker environment!
    exit /b 1
)

echo.
echo Kafka environment started!
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
echo To view logs: docker compose -f compose/kafka-confluent-environment-ssl.yml logs -f
echo To stop:      compose-down.cmd
