# Certak Kafka SeedKit

A Maven-based Java 25 application that sets up and populates a Kafka ecosystem with realistic data, for demo and testing purposes.

Primarily used a testing bed for [KafkIO](https://kafkio.com) (Certak's Apache Kafka™ GUI, for Engineers and Administrators)

## Overview

Certak Kafka SeedKit creates a realistic Kafka environment with:
- **80+ topics** across multiple domains (e-commerce, payments, IoT, trading, healthcare, etc.)
- **Multiple schema types**: Avro (primary), Protobuf, and JSON Schema
- **Schema evolution**: Multiple versions of key schemas to simulate real-world evolution
- **Various message formats**: JSON, XML, plain text, binary, CSV batches
- **Large messages**: Some topics contain messages of 100-300KB
- **Mixed compression types**: Producers use lz4, snappy, gzip, and none
- **Kafka Connectors**: Datagen, FileStream, and an intentionally failing JDBC connector
- **KSQL streams and tables**: Pre-configured for common analytics patterns
- **Consumer groups**: Multiple realistic consumer groups actively consuming data
- **Dynamic consumers**: Temporary consumers that stop after a period, and intermittent consumers that come and go
- **Continuous producers**: Background threads continuously producing messages
- **High-frequency producer**: At least one producer generating 10+ messages per second

## Prerequisites

- Java 25 (JDK)
- Docker and Docker Compose (for running the included Kafka environment)
- Running Kafka ecosystem with:
  - Kafka broker
  - Schema Registry
  - Kafka Connect (2 clusters)
  - KSQL DB

## Kafka Docker Environment

The project includes a complete Kafka environment with SSL support via Docker Compose. This is the easiest way to get started.

### Starting the Kafka Environment

```bash
# Linux/macOS
./compose-up.sh

# Windows
compose-up.cmd
```

This starts all Kafka services while preserving any existing data from previous runs.

### Starting Fresh (Reset All Data)

To remove all existing data (topics, messages, schemas, connectors) and start with a clean environment:

```bash
# Linux/macOS
./compose-reset.sh           # Interactive - prompts for confirmation
./compose-reset.sh --force   # Non-interactive - skips confirmation

# Windows
compose-reset.cmd            # Interactive - prompts for confirmation
compose-reset.cmd --force    # Non-interactive - skips confirmation
```

### Stopping the Environment

```bash
# Linux/macOS
./compose-down.sh

# Windows
compose-down.cmd
```

This stops all containers but preserves volumes/data for the next startup.

### Viewing Logs
```bash
# Linux/macOS
./compose-logs.sh              # All services
./compose-logs.sh kafka0       # Specific service
./compose-logs.sh --tail 100   # Last 100 lines

# Windows
compose-logs.cmd               # All services
compose-logs.cmd kafka0        # Specific service
compose-logs.cmd --tail 100    # Last 100 lines
```

### Services and Ports

| Service | HTTP Port | HTTPS Port | Description |
|---------|-----------|------------|-------------|
| Kafka | 9092 | 19092 | Kafka broker (plaintext and SSL) |
| Schema Registry | 8281 | 8285 | Confluent Schema Registry |
| Kafka Connect 0 | 8082 | 8083 | Primary Connect cluster |
| Kafka Connect 0-2 | 8182 | 8183 | Second node in primary cluster |
| Kafka Connect 1 | 8084 | 8085 | Secondary Connect cluster |
| ksqlDB | 8089 | 8088 | ksqlDB server |

### Hosts File Configuration

Add the following entries to your hosts file (`/etc/hosts` on Linux/macOS, `C:\Windows\System32\drivers\etc\hosts` on Windows):

```
127.0.0.1            kafka
127.0.0.1            ksqldb0
127.0.0.1            connect0
127.0.0.1            connect1
127.0.0.1            schemareg0
127.0.0.1            schemareg1
```

### Waiting for Services

After starting, wait for all services to be healthy before running the seedkit:

```bash
# Check service health
docker compose -f compose/kafka-ssl-compose.yml ps
```

All services should show `healthy` status (this may take 1-2 minutes on first start).

## Configuration

Edit `src/main/resources/application.yaml` to configure your Kafka ecosystem:

```yaml
kafka:
  bootstrap-servers: kafka:9092

schema-registry:
  primary-url: http://schemareg0:8281
  secondary-url: http://schemareg1:8281

kafka-connect:
  clusters:
    - name: connect-cluster-primary
      url: http://connect0:8182
    - name: connect-cluster-secondary
      url: http://connect1:8182

ksqldb:
  url: http://ksqldb0:8088
```

## Building

```bash
# Using build script (recommended)
./build.sh        # Linux/macOS
build.cmd         # Windows

# Or using Maven wrapper
./mvnw clean package -DskipTests

# Or with system Maven
mvn clean package -DskipTests
```

## Running

```bash
# Using run script (recommended - auto-builds if needed)
./run.sh          # Linux/macOS
run.cmd           # Windows

# Force rebuild then run
./run.sh --build  # Linux/macOS
run.cmd --build   # Windows

# Or using Maven wrapper
./mvnw exec:java

# Or run the JAR directly
java --enable-preview -jar target/ct-kafka-seedkit-1.0.0-SNAPSHOT.jar
```

## Topics Created

The application creates topics across these domains:

### E-Commerce
- `ecommerce.orders` - Order lifecycle events (Avro, 4 schema versions)
- `ecommerce.page-views` - User page views (Avro, 3 schema versions)
- `ecommerce.cart-events` - Shopping cart events
- `ecommerce.product-catalog` - Product catalog (compacted)
- `ecommerce.product-reviews` - Reviews (JSON Schema)
- And more...

### Payments
- `payments.transactions` - Payment transactions (Avro, 3 schema versions)
- `payments.card-events` - Card authorization events
- `payments.fraud-alerts` - Fraud detection alerts
- `payments.chargebacks` - Chargebacks (Protobuf)

### IoT
- `iot.sensor-readings` - High-volume sensor telemetry (Avro, 3 versions)
- `iot.device-status` - Device status updates
- `iot.firmware-updates` - Firmware commands (Protobuf)
- `iot.raw-telemetry` - Raw binary telemetry
- `iot.high-frequency-telemetry` - High-frequency data (~10 msgs/sec)

### Trading
- `trading.market-data` - Real-time market data
- `trading.orders` - Trade orders (Protobuf)
- `trading.executions` - Trade executions (Protobuf)
- `trading.positions` - Portfolio positions (compacted)

### Logs & Audit
- `logs.application` - Application logs (JSON, no schema)
- `logs.access` - HTTP access logs (plain text)
- `audit.system-events` - System audit events
- `audit.user-actions` - User action audit trail

### Integration
- `integration.erp-sync` - ERP integration (XML/SOAP)
- `integration.crm-events` - CRM events (XML)
- `integration.webhook-inbound` - Webhook payloads (JSON)

### Large Messages
- `data.bulk-imports` - Large JSON batches (~150KB)
- `data.xml-transforms` - Large XML documents (~150KB)
- `healthcare.fhir-resources` - FHIR bundles (~100KB)

## Producer Compression

The application uses multiple producers with different compression types to simulate real-world diversity:

| Compression | Use Cases | Producers |
|-------------|-----------|-----------|
| **lz4** | High-frequency telemetry, binary data, sensor readings | Fast compression, low latency |
| **snappy** | Orders, transactions, CSV batches, app logs | Balanced speed/ratio |
| **gzip** | Large JSON/XML messages, infrastructure logs | Best compression ratio |
| **none** | Cart events, device status, notifications | Low overhead, small messages |

During seeding and continuous production, messages are distributed across different compression-enabled producers to create realistic variety in the Kafka cluster.

## Kafka Connectors

### Primary Connect Cluster
- **datagen-users** - Generates synthetic user data
- **datagen-pageviews** - Generates pageview events
- **file-source-logs** - FileStreamSource connector
- **file-sink-orders** - FileStreamSink connector
- **jdbc-source-failing** - Intentionally failing JDBC connector (for demo)

### Secondary Connect Cluster
- **datagen-stock-trades** - Generates stock trade data
- **datagen-clickstream** - Generates clickstream events
- **file-source-events** - FileStreamSource for events

## Consumer Groups

The application starts multiple types of consumer groups to simulate realistic Kafka usage patterns.

### Permanent Consumer Groups

These run continuously until the application is stopped:

| Group Name | Topics | Consumers |
|------------|--------|-----------|
| analytics-pipeline | orders, page-views, user-sessions | 3 |
| fraud-detection-service | transactions, card-events | 2 |
| inventory-sync | stock-updates, warehouse-events | 1 |
| notification-service | email-outbound, push-events, sms-queue | 2 |
| audit-logger | system-events, user-actions, data-access-log | 1 |
| ml-feature-store | feature-updates, model-predictions | 2 |
| iot-telemetry-processor | sensor-readings, device-status | 4 |
| log-aggregator | logs.application, infrastructure, security | 2 |

Plus standalone consumers:
- realtime-dashboard
- log-shipper-elasticsearch
- prometheus-metrics-collector
- compliance-audit-archiver
- data-lake-ingestion
- cdc-processor

### Temporary Consumers (Stop After Duration)

These consumers run for a fixed period then stop permanently, simulating batch jobs and one-time processing:

| Consumer ID | Group | Topics | Duration |
|-------------|-------|--------|----------|
| batch-etl-job-001 | batch-etl-processor | orders, transactions | 2 minutes |
| data-migration-consumer-001 | data-migration | page-views, logs.application | 3 minutes |
| backfill-job-001 | historical-backfill | sensor-readings, metrics.application | 90 seconds |
| one-time-analysis-001 | adhoc-analysis | market-data, orders | 60 seconds |
| debug-consumer-001 | debug-session | logs.application, logs.infrastructure | 45 seconds |

### Intermittent Consumers (Come and Go)

These consumers cycle between online and offline states, simulating auto-scaling, spot instances, and unstable services:

| Consumer ID | Group | Topics | Online | Offline |
|-------------|-------|--------|--------|---------|
| auto-scaler-001 | auto-scaling-group | orders, transactions | 30-120s | 20-60s |
| spot-instance-consumer-001 | spot-processing | sensor-readings, device-status | 45-180s | 30-90s |
| dev-test-consumer-001 | dev-testing | logs.application, cart-events | 15-60s | 45-120s |
| periodic-reporter-001 | periodic-reports | metrics.application, market-data | 60-90s | 120-180s |
| flaky-consumer-001 | unstable-service | page-views, email-outbound | 10-45s | 15-45s |

## KSQL Streams and Tables

### Streams
- `ORDERS_STREAM` - E-commerce orders
- `PAGEVIEWS_STREAM` - Page view events
- `TRANSACTIONS_STREAM` - Payment transactions
- `SENSOR_READINGS_STREAM` - IoT sensor data
- `APP_LOGS_STREAM` - Application logs
- `MARKET_DATA_STREAM` - Stock market data

### Tables
- `CUSTOMERS_TABLE` - Customer profiles

### Derived Streams/Tables
- `HIGH_VALUE_ORDERS` - Orders > $500
- `SUSPICIOUS_TRANSACTIONS` - Transactions with risk score > 70
- `CUSTOMER_ORDER_TOTALS` - Windowed order aggregations
- `PAGEVIEWS_BY_REGION` - Regional pageview counts
- `FRAUD_CANDIDATES` - Potential fraud (multiple transactions in window)

## Sample KSQL Queries

```sql
-- Real-time order monitoring
SELECT * FROM ORDERS_STREAM EMIT CHANGES LIMIT 10;

-- High-value orders only
SELECT * FROM HIGH_VALUE_ORDERS EMIT CHANGES;

-- Customer order totals (windowed)
SELECT * FROM CUSTOMER_ORDER_TOTALS EMIT CHANGES;

-- Suspicious transactions for fraud detection
SELECT * FROM SUSPICIOUS_TRANSACTIONS EMIT CHANGES;

-- Pageviews by region
SELECT * FROM PAGEVIEWS_BY_REGION EMIT CHANGES;

-- Sensor readings filtering
SELECT * FROM SENSOR_READINGS_STREAM 
WHERE sensorType = 'TEMPERATURE' AND value > 30 
EMIT CHANGES;

-- Market data for specific symbols
SELECT symbol, price, bid, ask, volume, timestamp 
FROM MARKET_DATA_STREAM 
WHERE symbol IN ('AAPL', 'GOOGL', 'MSFT') 
EMIT CHANGES;

-- Application error logs
SELECT timestamp, service, message 
FROM APP_LOGS_STREAM 
WHERE level = 'ERROR' 
EMIT CHANGES;

-- Windowed aggregation: Orders per minute
SELECT 
    WINDOWSTART AS window_start,
    COUNT(*) AS order_count,
    SUM(totalAmount) AS total_revenue
FROM ORDERS_STREAM
WINDOW TUMBLING (SIZE 1 MINUTE)
GROUP BY 1
EMIT CHANGES;

-- Transaction velocity monitoring
SELECT 
    customerId,
    COUNT(*) AS txn_count,
    SUM(amount) AS total_amount,
    AVG(riskScore) AS avg_risk
FROM TRANSACTIONS_STREAM
WINDOW HOPPING (SIZE 5 MINUTES, ADVANCE BY 1 MINUTE)
GROUP BY customerId
HAVING COUNT(*) > 3
EMIT CHANGES;
```

## Schema Evolution

Several topics have multiple schema versions registered to simulate real-world evolution:

- `ecommerce.orders-value`: 4 versions (added fields, enum types, nested objects)
- `ecommerce.page-views-value`: 3 versions (added tracking fields)
- `payments.transactions-value`: 3 versions (added risk, fee fields)
- `customers.profiles-value`: 3 versions (added addresses, loyalty)
- `iot.sensor-readings-value`: 3 versions (added location, quality)
- `trading.market-data-value`: 2 versions (added bid/ask spread)
- `inventory.stock-updates-value`: 2 versions (added reason tracking)

## Continuous Producers

The application runs continuous producers with varying intervals:

| Topic | Interval | Compression |
|-------|----------|-------------|
| ecommerce.orders | 500-5000ms | snappy |
| ecommerce.page-views | 250-2500ms | lz4 |
| ecommerce.cart-events | 500-10000ms | none |
| payments.transactions | 500-5000ms | snappy |
| iot.sensor-readings | 125-2500ms | lz4 |
| iot.device-status | 1000-10000ms | none |
| iot.high-frequency-telemetry | **100ms fixed** (~10/sec) | lz4 |
| logs.application | 250-2500ms | snappy |
| logs.infrastructure | 500-5000ms | gzip |
| notifications.email-outbound | 1000-10000ms | none |
| trading.market-data | 250-2500ms | lz4 |
| metrics.application | 500-5000ms | snappy |

The **high-frequency telemetry producer** guarantees at least 10 messages per second, useful for testing high-throughput scenarios.

## Stopping

Press `Ctrl+C` to gracefully shut down the application. This will:
1. Stop all continuous producers (including high-frequency)
2. Stop all permanent consumer threads
3. Stop all intermittent consumers
4. Close all Kafka connections