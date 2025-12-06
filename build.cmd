@echo off
REM Build script for Certak Kafka SeedKit (Windows)
setlocal

echo Building Certak Kafka SeedKit...
call mvnw.cmd clean package -DskipTests
if errorlevel 1 (
    echo Build failed!
    exit /b 1
)
echo.
echo Build successful! Run with: run.cmd
