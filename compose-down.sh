#!/bin/bash
# Stop Kafka Docker environment (preserves volumes/data)
# Use compose-reset.sh to remove all data and start fresh

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/compose"

echo "Stopping Kafka Docker environment..."
docker compose -f kafka-ssl-compose.yml down

echo ""
echo "Kafka environment stopped."
echo "Data volumes are preserved. Use compose-reset.sh to remove all data."
