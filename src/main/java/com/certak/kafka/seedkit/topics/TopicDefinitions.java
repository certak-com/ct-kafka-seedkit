package com.certak.kafka.seedkit.topics;

import java.util.List;

/**
 * Defines all topics to be created in the Kafka ecosystem.
 * Topics are organized by domain/category with realistic naming conventions.
 */
public class TopicDefinitions {
    
    /**
     * Topic definition with all configuration options.
     */
    public record TopicDef(
        String name,
        int partitions,
        short replicationFactor,
        TopicType type,
        SchemaType schemaType,
        MessageFormat messageFormat,
        boolean hasKeys,
        boolean hasHeaders,
        boolean isLargeMessages,
        String description
    ) {
        public TopicDef(String name, TopicType type, SchemaType schemaType, MessageFormat format) {
            this(name, 3, (short) 1, type, schemaType, format, true, false, false, "");
        }
        
        public TopicDef withPartitions(int p) {
            return new TopicDef(name, p, replicationFactor, type, schemaType, messageFormat, hasKeys, hasHeaders, isLargeMessages, description);
        }
        
        public TopicDef withKeys(boolean k) {
            return new TopicDef(name, partitions, replicationFactor, type, schemaType, messageFormat, k, hasHeaders, isLargeMessages, description);
        }
        
        public TopicDef withHeaders(boolean h) {
            return new TopicDef(name, partitions, replicationFactor, type, schemaType, messageFormat, hasKeys, h, isLargeMessages, description);
        }
        
        public TopicDef withLargeMessages(boolean l) {
            return new TopicDef(name, partitions, replicationFactor, type, schemaType, messageFormat, hasKeys, hasHeaders, l, description);
        }
        
        public TopicDef withDescription(String d) {
            return new TopicDef(name, partitions, replicationFactor, type, schemaType, messageFormat, hasKeys, hasHeaders, isLargeMessages, d);
        }
    }
    
    public enum TopicType {
        EVENTS,          // Event sourcing / domain events
        COMMANDS,        // Command topics
        CHANGELOG,       // CDC / changelog topics
        LOGS,            // Log aggregation
        METRICS,         // Metrics and telemetry
        NOTIFICATIONS,   // Notification queues
        INTERNAL         // Internal Kafka topics
    }
    
    public enum SchemaType {
        AVRO,
        PROTOBUF,
        JSON_SCHEMA,
        NONE             // No schema registry
    }
    
    public enum MessageFormat {
        JSON,
        XML,
        BINARY,
        PLAIN_TEXT,
        AVRO_BINARY,
        PROTOBUF_BINARY
    }
    
    /**
     * Returns all topic definitions organized by domain.
     */
    public static List<TopicDef> getAllTopics() {
        return List.of(
            // ============================================================
            // E-COMMERCE DOMAIN - Core retail operations
            // ============================================================
            new TopicDef("ecommerce.orders", TopicType.EVENTS, SchemaType.AVRO, MessageFormat.AVRO_BINARY)
                .withPartitions(12).withKeys(true).withHeaders(true)
                .withDescription("Order lifecycle events - created, updated, shipped, delivered"),
            
            new TopicDef("ecommerce.orders.dlq", TopicType.EVENTS, SchemaType.AVRO, MessageFormat.AVRO_BINARY)
                .withPartitions(3).withKeys(true).withHeaders(true)
                .withDescription("Dead letter queue for failed order processing"),
            
            new TopicDef("ecommerce.order-items", TopicType.EVENTS, SchemaType.AVRO, MessageFormat.AVRO_BINARY)
                .withPartitions(6).withKeys(true).withHeaders(false)
                .withDescription("Individual line items within orders"),
            
            new TopicDef("ecommerce.page-views", TopicType.EVENTS, SchemaType.AVRO, MessageFormat.AVRO_BINARY)
                .withPartitions(12).withKeys(true).withHeaders(true)
                .withDescription("User page view events for analytics"),
            
            new TopicDef("ecommerce.user-sessions", TopicType.EVENTS, SchemaType.AVRO, MessageFormat.AVRO_BINARY)
                .withPartitions(6).withKeys(true).withHeaders(false)
                .withDescription("User session start/end events"),
            
            new TopicDef("ecommerce.cart-events", TopicType.EVENTS, SchemaType.AVRO, MessageFormat.AVRO_BINARY)
                .withPartitions(6).withKeys(true).withHeaders(false)
                .withDescription("Shopping cart add/remove/update events"),
            
            new TopicDef("ecommerce.product-catalog", TopicType.CHANGELOG, SchemaType.AVRO, MessageFormat.AVRO_BINARY)
                .withPartitions(3).withKeys(true).withHeaders(false)
                .withDescription("Product catalog changes - compacted topic"),
            
            new TopicDef("ecommerce.product-reviews", TopicType.EVENTS, SchemaType.JSON_SCHEMA, MessageFormat.JSON)
                .withPartitions(3).withKeys(true).withHeaders(false)
                .withDescription("Customer product reviews"),
            
            new TopicDef("ecommerce.search-queries", TopicType.EVENTS, SchemaType.NONE, MessageFormat.JSON)
                .withPartitions(6).withKeys(false).withHeaders(false)
                .withDescription("Search query logs for analytics"),
            
            new TopicDef("ecommerce.recommendations", TopicType.EVENTS, SchemaType.AVRO, MessageFormat.AVRO_BINARY)
                .withPartitions(3).withKeys(true).withHeaders(false)
                .withDescription("Product recommendation events"),
            
            // ============================================================
            // PAYMENTS DOMAIN - Financial transactions
            // ============================================================
            new TopicDef("payments.transactions", TopicType.EVENTS, SchemaType.AVRO, MessageFormat.AVRO_BINARY)
                .withPartitions(12).withKeys(true).withHeaders(true)
                .withDescription("Payment transaction events"),
            
            new TopicDef("payments.card-events", TopicType.EVENTS, SchemaType.AVRO, MessageFormat.AVRO_BINARY)
                .withPartitions(6).withKeys(true).withHeaders(true)
                .withDescription("Card authorization, capture, refund events"),
            
            new TopicDef("payments.fraud-alerts", TopicType.EVENTS, SchemaType.AVRO, MessageFormat.AVRO_BINARY)
                .withPartitions(3).withKeys(true).withHeaders(true)
                .withDescription("Fraud detection alerts"),
            
            new TopicDef("payments.settlements", TopicType.EVENTS, SchemaType.AVRO, MessageFormat.AVRO_BINARY)
                .withPartitions(3).withKeys(true).withHeaders(false)
                .withDescription("Settlement batch events"),
            
            new TopicDef("payments.chargebacks", TopicType.EVENTS, SchemaType.PROTOBUF, MessageFormat.PROTOBUF_BINARY)
                .withPartitions(3).withKeys(true).withHeaders(true)
                .withDescription("Chargeback dispute events"),
            
            // ============================================================
            // INVENTORY DOMAIN - Stock management
            // ============================================================
            new TopicDef("inventory.stock-updates", TopicType.EVENTS, SchemaType.AVRO, MessageFormat.AVRO_BINARY)
                .withPartitions(6).withKeys(true).withHeaders(false)
                .withDescription("Real-time stock level changes"),
            
            new TopicDef("inventory.warehouse-events", TopicType.EVENTS, SchemaType.AVRO, MessageFormat.AVRO_BINARY)
                .withPartitions(3).withKeys(true).withHeaders(false)
                .withDescription("Warehouse operations - receiving, picking, shipping"),
            
            new TopicDef("inventory.reorder-alerts", TopicType.EVENTS, SchemaType.JSON_SCHEMA, MessageFormat.JSON)
                .withPartitions(3).withKeys(true).withHeaders(false)
                .withDescription("Low stock alerts triggering reorders"),
            
            new TopicDef("inventory.transfers", TopicType.EVENTS, SchemaType.AVRO, MessageFormat.AVRO_BINARY)
                .withPartitions(3).withKeys(true).withHeaders(false)
                .withDescription("Inter-warehouse transfer events"),
            
            // ============================================================
            // CUSTOMER DOMAIN - Customer data and interactions
            // ============================================================
            new TopicDef("customers.profiles", TopicType.CHANGELOG, SchemaType.AVRO, MessageFormat.AVRO_BINARY)
                .withPartitions(6).withKeys(true).withHeaders(false)
                .withDescription("Customer profile updates - compacted"),
            
            new TopicDef("customers.preferences", TopicType.CHANGELOG, SchemaType.AVRO, MessageFormat.AVRO_BINARY)
                .withPartitions(3).withKeys(true).withHeaders(false)
                .withDescription("Customer preference settings"),
            
            new TopicDef("customers.loyalty-events", TopicType.EVENTS, SchemaType.AVRO, MessageFormat.AVRO_BINARY)
                .withPartitions(3).withKeys(true).withHeaders(false)
                .withDescription("Loyalty program point accrual and redemption"),
            
            new TopicDef("customers.feedback", TopicType.EVENTS, SchemaType.JSON_SCHEMA, MessageFormat.JSON)
                .withPartitions(3).withKeys(true).withHeaders(false)
                .withDescription("Customer feedback and surveys"),
            
            // ============================================================
            // NOTIFICATIONS DOMAIN - Multi-channel messaging
            // ============================================================
            new TopicDef("notifications.email-outbound", TopicType.NOTIFICATIONS, SchemaType.AVRO, MessageFormat.AVRO_BINARY)
                .withPartitions(6).withKeys(true).withHeaders(true)
                .withDescription("Outbound email queue"),
            
            new TopicDef("notifications.email-events", TopicType.EVENTS, SchemaType.AVRO, MessageFormat.AVRO_BINARY)
                .withPartitions(3).withKeys(true).withHeaders(false)
                .withDescription("Email delivery events - sent, opened, clicked, bounced"),
            
            new TopicDef("notifications.push-events", TopicType.NOTIFICATIONS, SchemaType.AVRO, MessageFormat.AVRO_BINARY)
                .withPartitions(6).withKeys(true).withHeaders(true)
                .withDescription("Mobile push notification queue"),
            
            new TopicDef("notifications.sms-queue", TopicType.NOTIFICATIONS, SchemaType.AVRO, MessageFormat.AVRO_BINARY)
                .withPartitions(3).withKeys(true).withHeaders(true)
                .withDescription("SMS message queue"),
            
            new TopicDef("notifications.in-app", TopicType.NOTIFICATIONS, SchemaType.JSON_SCHEMA, MessageFormat.JSON)
                .withPartitions(3).withKeys(true).withHeaders(false)
                .withDescription("In-app notification events"),
            
            new TopicDef("notifications.templates", TopicType.CHANGELOG, SchemaType.NONE, MessageFormat.JSON)
                .withPartitions(1).withKeys(true).withHeaders(false)
                .withDescription("Notification template definitions"),
            
            // ============================================================
            // IoT DOMAIN - Device telemetry
            // ============================================================
            new TopicDef("iot.sensor-readings", TopicType.METRICS, SchemaType.AVRO, MessageFormat.AVRO_BINARY)
                .withPartitions(12).withKeys(true).withHeaders(true)
                .withDescription("High-volume sensor telemetry data"),
            
            new TopicDef("iot.device-status", TopicType.EVENTS, SchemaType.AVRO, MessageFormat.AVRO_BINARY)
                .withPartitions(6).withKeys(true).withHeaders(false)
                .withDescription("Device online/offline status changes"),
            
            new TopicDef("iot.device-registry", TopicType.CHANGELOG, SchemaType.AVRO, MessageFormat.AVRO_BINARY)
                .withPartitions(3).withKeys(true).withHeaders(false)
                .withDescription("Device registration and metadata"),
            
            new TopicDef("iot.alerts", TopicType.EVENTS, SchemaType.AVRO, MessageFormat.AVRO_BINARY)
                .withPartitions(3).withKeys(true).withHeaders(true)
                .withDescription("IoT threshold breach alerts"),
            
            new TopicDef("iot.firmware-updates", TopicType.COMMANDS, SchemaType.PROTOBUF, MessageFormat.PROTOBUF_BINARY)
                .withPartitions(3).withKeys(true).withHeaders(true)
                .withDescription("Firmware update commands"),
            
            new TopicDef("iot.raw-telemetry", TopicType.METRICS, SchemaType.NONE, MessageFormat.BINARY)
                .withPartitions(12).withKeys(true).withHeaders(false)
                .withDescription("Raw binary telemetry before parsing"),
            
            new TopicDef("iot.high-frequency-telemetry", TopicType.METRICS, SchemaType.NONE, MessageFormat.JSON)
                .withPartitions(12).withKeys(true).withHeaders(true)
                .withDescription("High-frequency telemetry data - 10+ msgs/sec"),
            
            // ============================================================
            // LOGS DOMAIN - Centralized logging
            // ============================================================
            new TopicDef("logs.application", TopicType.LOGS, SchemaType.NONE, MessageFormat.JSON)
                .withPartitions(12).withKeys(false).withHeaders(true)
                .withDescription("Application logs from all services"),
            
            new TopicDef("logs.infrastructure", TopicType.LOGS, SchemaType.NONE, MessageFormat.JSON)
                .withPartitions(6).withKeys(false).withHeaders(true)
                .withDescription("Infrastructure and system logs"),
            
            new TopicDef("logs.security", TopicType.LOGS, SchemaType.AVRO, MessageFormat.AVRO_BINARY)
                .withPartitions(6).withKeys(true).withHeaders(true)
                .withDescription("Security audit logs"),
            
            new TopicDef("logs.access", TopicType.LOGS, SchemaType.NONE, MessageFormat.PLAIN_TEXT)
                .withPartitions(6).withKeys(false).withHeaders(false)
                .withDescription("HTTP access logs - Apache/Nginx format"),
            
            new TopicDef("logs.errors", TopicType.LOGS, SchemaType.NONE, MessageFormat.JSON)
                .withPartitions(6).withKeys(false).withHeaders(true)
                .withDescription("Aggregated error logs"),
            
            // ============================================================
            // AUDIT DOMAIN - Compliance and audit trails
            // ============================================================
            new TopicDef("audit.system-events", TopicType.EVENTS, SchemaType.AVRO, MessageFormat.AVRO_BINARY)
                .withPartitions(3).withKeys(true).withHeaders(true)
                .withDescription("System-level audit events"),
            
            new TopicDef("audit.user-actions", TopicType.EVENTS, SchemaType.AVRO, MessageFormat.AVRO_BINARY)
                .withPartitions(6).withKeys(true).withHeaders(true)
                .withDescription("User action audit trail"),
            
            new TopicDef("audit.data-access-log", TopicType.EVENTS, SchemaType.AVRO, MessageFormat.AVRO_BINARY)
                .withPartitions(6).withKeys(true).withHeaders(true)
                .withDescription("Data access audit for compliance"),
            
            new TopicDef("audit.config-changes", TopicType.EVENTS, SchemaType.AVRO, MessageFormat.AVRO_BINARY)
                .withPartitions(3).withKeys(true).withHeaders(true)
                .withDescription("Configuration change audit"),
            
            // ============================================================
            // ML/AI DOMAIN - Machine learning pipelines
            // ============================================================
            new TopicDef("ml.feature-updates", TopicType.EVENTS, SchemaType.AVRO, MessageFormat.AVRO_BINARY)
                .withPartitions(6).withKeys(true).withHeaders(false)
                .withDescription("Real-time feature store updates"),
            
            new TopicDef("ml.model-predictions", TopicType.EVENTS, SchemaType.AVRO, MessageFormat.AVRO_BINARY)
                .withPartitions(6).withKeys(true).withHeaders(true)
                .withDescription("Model inference results"),
            
            new TopicDef("ml.training-data", TopicType.EVENTS, SchemaType.AVRO, MessageFormat.AVRO_BINARY)
                .withPartitions(3).withKeys(true).withHeaders(false).withLargeMessages(true)
                .withDescription("Training data batches - large messages"),
            
            new TopicDef("ml.model-metrics", TopicType.METRICS, SchemaType.JSON_SCHEMA, MessageFormat.JSON)
                .withPartitions(3).withKeys(true).withHeaders(false)
                .withDescription("Model performance metrics"),
            
            new TopicDef("ml.experiment-results", TopicType.EVENTS, SchemaType.NONE, MessageFormat.JSON)
                .withPartitions(1).withKeys(true).withHeaders(false).withLargeMessages(true)
                .withDescription("A/B test and experiment results - large JSON"),
            
            // ============================================================
            // SHIPPING/LOGISTICS DOMAIN
            // ============================================================
            new TopicDef("shipping.tracking-updates", TopicType.EVENTS, SchemaType.AVRO, MessageFormat.AVRO_BINARY)
                .withPartitions(6).withKeys(true).withHeaders(true)
                .withDescription("Package tracking status updates"),
            
            new TopicDef("shipping.carrier-events", TopicType.EVENTS, SchemaType.PROTOBUF, MessageFormat.PROTOBUF_BINARY)
                .withPartitions(3).withKeys(true).withHeaders(false)
                .withDescription("Carrier integration events"),
            
            new TopicDef("shipping.label-requests", TopicType.COMMANDS, SchemaType.AVRO, MessageFormat.AVRO_BINARY)
                .withPartitions(3).withKeys(true).withHeaders(false)
                .withDescription("Shipping label generation requests"),
            
            new TopicDef("shipping.route-optimization", TopicType.EVENTS, SchemaType.JSON_SCHEMA, MessageFormat.JSON)
                .withPartitions(3).withKeys(true).withHeaders(false)
                .withDescription("Delivery route optimization events"),
            
            // ============================================================
            // INTEGRATION DOMAIN - External system integration
            // ============================================================
            new TopicDef("integration.erp-sync", TopicType.EVENTS, SchemaType.NONE, MessageFormat.XML)
                .withPartitions(3).withKeys(true).withHeaders(true)
                .withDescription("ERP system sync - XML SOAP format"),
            
            new TopicDef("integration.crm-events", TopicType.EVENTS, SchemaType.NONE, MessageFormat.XML)
                .withPartitions(3).withKeys(true).withHeaders(true)
                .withDescription("CRM integration events - XML format"),
            
            new TopicDef("integration.webhook-inbound", TopicType.EVENTS, SchemaType.NONE, MessageFormat.JSON)
                .withPartitions(6).withKeys(false).withHeaders(true)
                .withDescription("Inbound webhook payloads"),
            
            new TopicDef("integration.api-gateway-logs", TopicType.LOGS, SchemaType.NONE, MessageFormat.JSON)
                .withPartitions(6).withKeys(false).withHeaders(true)
                .withDescription("API gateway request/response logs"),
            
            new TopicDef("integration.legacy-mainframe", TopicType.EVENTS, SchemaType.NONE, MessageFormat.PLAIN_TEXT)
                .withPartitions(1).withKeys(true).withHeaders(false)
                .withDescription("Legacy mainframe fixed-width records"),
            
            // ============================================================
            // METRICS/MONITORING DOMAIN
            // ============================================================
            new TopicDef("metrics.application", TopicType.METRICS, SchemaType.AVRO, MessageFormat.AVRO_BINARY)
                .withPartitions(6).withKeys(true).withHeaders(false)
                .withDescription("Application performance metrics"),
            
            new TopicDef("metrics.infrastructure", TopicType.METRICS, SchemaType.AVRO, MessageFormat.AVRO_BINARY)
                .withPartitions(6).withKeys(true).withHeaders(false)
                .withDescription("Infrastructure metrics - CPU, memory, disk"),
            
            new TopicDef("metrics.business-kpis", TopicType.METRICS, SchemaType.AVRO, MessageFormat.AVRO_BINARY)
                .withPartitions(3).withKeys(true).withHeaders(false)
                .withDescription("Business KPI snapshots"),
            
            new TopicDef("metrics.custom-events", TopicType.METRICS, SchemaType.NONE, MessageFormat.JSON)
                .withPartitions(3).withKeys(false).withHeaders(false)
                .withDescription("Custom analytics events"),
            
            // ============================================================
            // FINANCIAL/TRADING DOMAIN
            // ============================================================
            new TopicDef("trading.market-data", TopicType.EVENTS, SchemaType.AVRO, MessageFormat.AVRO_BINARY)
                .withPartitions(12).withKeys(true).withHeaders(false)
                .withDescription("Real-time market data feed"),
            
            new TopicDef("trading.orders", TopicType.EVENTS, SchemaType.PROTOBUF, MessageFormat.PROTOBUF_BINARY)
                .withPartitions(12).withKeys(true).withHeaders(true)
                .withDescription("Trade order events"),
            
            new TopicDef("trading.executions", TopicType.EVENTS, SchemaType.PROTOBUF, MessageFormat.PROTOBUF_BINARY)
                .withPartitions(6).withKeys(true).withHeaders(true)
                .withDescription("Trade execution confirmations"),
            
            new TopicDef("trading.positions", TopicType.CHANGELOG, SchemaType.AVRO, MessageFormat.AVRO_BINARY)
                .withPartitions(6).withKeys(true).withHeaders(false)
                .withDescription("Portfolio positions - compacted"),
            
            new TopicDef("trading.risk-alerts", TopicType.EVENTS, SchemaType.AVRO, MessageFormat.AVRO_BINARY)
                .withPartitions(3).withKeys(true).withHeaders(true)
                .withDescription("Risk limit breach alerts"),
            
            // ============================================================
            // HEALTHCARE DOMAIN (Anonymized)
            // ============================================================
            new TopicDef("healthcare.appointments", TopicType.EVENTS, SchemaType.AVRO, MessageFormat.AVRO_BINARY)
                .withPartitions(6).withKeys(true).withHeaders(false)
                .withDescription("Appointment scheduling events"),
            
            new TopicDef("healthcare.lab-results", TopicType.EVENTS, SchemaType.AVRO, MessageFormat.AVRO_BINARY)
                .withPartitions(3).withKeys(true).withHeaders(true)
                .withDescription("Lab result notifications"),
            
            new TopicDef("healthcare.hl7-messages", TopicType.EVENTS, SchemaType.NONE, MessageFormat.PLAIN_TEXT)
                .withPartitions(3).withKeys(true).withHeaders(true)
                .withDescription("HL7 v2 formatted messages"),
            
            new TopicDef("healthcare.fhir-resources", TopicType.EVENTS, SchemaType.NONE, MessageFormat.JSON)
                .withPartitions(6).withKeys(true).withHeaders(true).withLargeMessages(true)
                .withDescription("FHIR R4 resource bundles - large JSON"),
            
            // ============================================================
            // INTERNAL/SYSTEM TOPICS
            // ============================================================
            new TopicDef("_internal.dead-letter-queue", TopicType.INTERNAL, SchemaType.NONE, MessageFormat.BINARY)
                .withPartitions(3).withKeys(true).withHeaders(true)
                .withDescription("Global dead letter queue"),
            
            new TopicDef("_internal.schema-changes", TopicType.INTERNAL, SchemaType.NONE, MessageFormat.JSON)
                .withPartitions(1).withKeys(true).withHeaders(false)
                .withDescription("Schema evolution notifications"),
            
            new TopicDef("_internal.connector-status", TopicType.INTERNAL, SchemaType.NONE, MessageFormat.JSON)
                .withPartitions(1).withKeys(true).withHeaders(false)
                .withDescription("Kafka Connect status updates"),
            
            new TopicDef("_internal.reprocessing-requests", TopicType.COMMANDS, SchemaType.NONE, MessageFormat.JSON)
                .withPartitions(1).withKeys(true).withHeaders(true)
                .withDescription("Data reprocessing commands"),
            
            // ============================================================
            // SPECIAL TOPICS - Various formats and sizes
            // ============================================================
            new TopicDef("data.bulk-imports", TopicType.EVENTS, SchemaType.NONE, MessageFormat.JSON)
                .withPartitions(3).withKeys(true).withHeaders(true).withLargeMessages(true)
                .withDescription("Large batch import data"),
            
            new TopicDef("data.xml-transforms", TopicType.EVENTS, SchemaType.NONE, MessageFormat.XML)
                .withPartitions(3).withKeys(true).withHeaders(false).withLargeMessages(true)
                .withDescription("Large XML document transforms"),
            
            new TopicDef("data.binary-blobs", TopicType.EVENTS, SchemaType.NONE, MessageFormat.BINARY)
                .withPartitions(3).withKeys(true).withHeaders(false).withLargeMessages(true)
                .withDescription("Binary data blobs"),
            
            new TopicDef("data.csv-batches", TopicType.EVENTS, SchemaType.NONE, MessageFormat.PLAIN_TEXT)
                .withPartitions(3).withKeys(true).withHeaders(true).withLargeMessages(true)
                .withDescription("CSV batch uploads"),
            
            // ============================================================
            // CONNECT SOURCE/SINK TOPICS
            // ============================================================
            new TopicDef("connect.datagen-users", TopicType.EVENTS, SchemaType.AVRO, MessageFormat.AVRO_BINARY)
                .withPartitions(3).withKeys(true).withHeaders(false)
                .withDescription("Datagen connector - synthetic users"),
            
            new TopicDef("connect.datagen-pageviews", TopicType.EVENTS, SchemaType.AVRO, MessageFormat.AVRO_BINARY)
                .withPartitions(6).withKeys(true).withHeaders(false)
                .withDescription("Datagen connector - synthetic pageviews"),
            
            new TopicDef("connect.file-input", TopicType.EVENTS, SchemaType.NONE, MessageFormat.PLAIN_TEXT)
                .withPartitions(1).withKeys(false).withHeaders(false)
                .withDescription("FileStreamSource input topic"),
            
            new TopicDef("connect.file-output", TopicType.EVENTS, SchemaType.NONE, MessageFormat.PLAIN_TEXT)
                .withPartitions(1).withKeys(false).withHeaders(false)
                .withDescription("FileStreamSink output topic"),
            
            // ============================================================
            // KSQL DERIVED TOPICS
            // ============================================================
            new TopicDef("ksql.order-totals", TopicType.EVENTS, SchemaType.AVRO, MessageFormat.AVRO_BINARY)
                .withPartitions(3).withKeys(true).withHeaders(false)
                .withDescription("KSQL derived - order totals by customer"),
            
            new TopicDef("ksql.pageview-regions", TopicType.EVENTS, SchemaType.AVRO, MessageFormat.AVRO_BINARY)
                .withPartitions(3).withKeys(true).withHeaders(false)
                .withDescription("KSQL derived - pageviews by region"),
            
            new TopicDef("ksql.enriched-orders", TopicType.EVENTS, SchemaType.AVRO, MessageFormat.AVRO_BINARY)
                .withPartitions(6).withKeys(true).withHeaders(false)
                .withDescription("KSQL derived - orders joined with customer data"),
            
            new TopicDef("ksql.fraud-candidates", TopicType.EVENTS, SchemaType.AVRO, MessageFormat.AVRO_BINARY)
                .withPartitions(3).withKeys(true).withHeaders(false)
                .withDescription("KSQL derived - potential fraud transactions")
        );
    }
    
    /**
     * Get topics that should have continuous data production.
     */
    public static List<String> getContinuousTopics() {
        return List.of(
            "ecommerce.orders",
            "ecommerce.page-views",
            "ecommerce.cart-events",
            "payments.transactions",
            "iot.sensor-readings",
            "iot.device-status",
            "iot.high-frequency-telemetry",
            "logs.application",
            "logs.infrastructure",
            "notifications.email-outbound",
            "trading.market-data",
            "metrics.application"
        );
    }
    
    /**
     * Get topics that should have large messages.
     */
    public static List<String> getLargeMessageTopics() {
        return getAllTopics().stream()
            .filter(TopicDef::isLargeMessages)
            .map(TopicDef::name)
            .toList();
    }
}
