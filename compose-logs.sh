#!/bin/bash
# View logs from Kafka Docker environment

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/compose"

# Pass through any arguments (e.g., service name, --tail, etc.)
docker compose -f kafka-ssl-compose.yml logs -f "$@"
