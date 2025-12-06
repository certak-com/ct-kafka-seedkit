#!/bin/bash
# Start Kafka Docker environment (preserves existing data/volumes)
# Use compose-reset.sh to start fresh with no persisted data

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/compose"

echo "Starting Kafka Docker environment..."
echo "(Use compose-reset.sh to start fresh with no persisted data)"
echo ""

docker compose -f kafka-ssl-compose.yml up -d

echo ""
echo "Kafka environment started!"
echo ""
echo "Services:"
echo "  Kafka:           localhost:9092 (plaintext), localhost:19092 (SSL)"
echo "  Schema Registry: localhost:8281 (HTTP), localhost:8285 (HTTPS)"
echo "  Kafka Connect 0: localhost:8082 (HTTP), localhost:8083 (HTTPS)"
echo "  Kafka Connect 1: localhost:8084 (HTTP), localhost:8085 (HTTPS)"
echo "  ksqlDB:          localhost:8089 (HTTP), localhost:8088 (HTTPS)"
echo ""
echo "To view logs: docker compose -f compose/kafka-ssl-compose.yml logs -f"
echo "To stop:      ./compose-down.sh"
