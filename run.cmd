@echo off
REM Run script for Certak Kafka SeedKit (Windows)
setlocal

REM Build if target doesn't exist or if --build flag is passed
if "%1"=="--build" goto build
if not exist "target\ct-kafka-seedkit-1.0.0-SNAPSHOT.jar" goto build
goto run

:build
echo Building Certak Kafka SeedKit...
call mvnw.cmd package -DskipTests -q
if errorlevel 1 (
    echo Build failed!
    exit /b 1
)

:run
echo Starting Certak Kafka SeedKit...
java --enable-preview -jar target\ct-kafka-seedkit-1.0.0-SNAPSHOT.jar %*
