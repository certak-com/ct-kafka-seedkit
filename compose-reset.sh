#!/bin/bash
# Reset Kafka Docker environment - removes all containers, volumes, and data
# WARNING: This will delete all Kafka topics, messages, schemas, and connector state!

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/compose"

echo "WARNING: This will remove all Kafka data including:"
echo "  - All topics and messages"
echo "  - All schemas in Schema Registry"
echo "  - All Kafka Connect connector configurations"
echo "  - All ksqlDB streams and tables"
echo ""

# Check for --force flag
if [ "$1" != "--force" ] && [ "$1" != "-f" ]; then
    read -p "Are you sure you want to continue? (y/N) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "Aborted."
        exit 0
    fi
fi

echo "Stopping and removing all containers and volumes..."
docker compose -f kafka-confluent-environment-ssl.yml down -v --remove-orphans

echo ""
echo "Starting fresh Kafka environment..."
docker compose -f kafka-confluent-environment-ssl.yml up -d

echo ""
echo "Kafka environment reset and started fresh!"
echo ""
echo "Services:"
echo "  Kafka 0:         localhost:9092 (plaintext), localhost:19092 (SSL)"
echo "                   localhost:9094 (SASL_PLAINTEXT), localhost:19094 (SASL_SSL)"
echo "  Kafka 1:         localhost:9192 (plaintext), localhost:19192 (SSL)"
echo "                   localhost:9194 (SASL_PLAINTEXT), localhost:19194 (SASL_SSL)"
echo "  Schema Registry: localhost:8281 (HTTP), localhost:8285 (HTTPS)"
echo "  Kafka Connect 0: localhost:8082 (HTTP), localhost:8083 (HTTPS)"
echo "  Kafka Connect 1: localhost:8084 (HTTP), localhost:8085 (HTTPS)"
echo "  ksqlDB:          localhost:8088 (HTTP), localhost:8089 (HTTPS)"
echo ""
echo "SASL Users: admin/admin-secret, client/client-secret"
echo ""
echo "To view logs: docker compose -f compose/kafka-confluent-environment-ssl.yml logs -f"
echo "To stop:      ./compose-down.sh"
