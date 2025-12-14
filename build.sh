#!/bin/bash
# Build script for Certak Kafka SeedKit (Linux/macOS)

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "Building Certak Kafka SeedKit..."
./mvnw clean package -DskipTests

echo ""
echo "Build successful! Run with: ./run.sh"
