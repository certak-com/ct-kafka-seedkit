@echo off
REM View logs from Kafka Docker environment
setlocal

cd /d "%~dp0compose"

REM Pass through any arguments (e.g., service name, --tail, etc.)
docker compose -f kafka-ssl-compose.yml logs -f %*
