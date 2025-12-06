package com.certak.kafka.seedkit.schemas;

import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.schemaregistry.json.JsonSchema;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Manages schema registration in Schema Registry.
 * Creates Avro, Protobuf, and JSON schemas with multiple versions to simulate evolution.
 */
public class SchemaManager {
    private static final Logger log = LoggerFactory.getLogger(SchemaManager.class);
    
    private final SchemaRegistryClient client;
    private final String registryUrl;
    
    public SchemaManager(String registryUrl) {
        this.registryUrl = registryUrl;
        this.client = new CachedSchemaRegistryClient(registryUrl, 100);
    }
    
    /**
     * Register all schemas including multiple versions for evolution simulation.
     */
    public void registerAllSchemas() {
        log.info("Registering schemas to {}", registryUrl);
        
        // Register Avro schemas
        registerAvroSchemas();
        
        // Register Protobuf schemas
        registerProtobufSchemas();
        
        // Register JSON schemas
        registerJsonSchemas();
        
        log.info("Schema registration completed");
    }
    
    private void registerAvroSchemas() {
        // Order schema with multiple versions
        registerAvroWithVersions("ecommerce.orders-value", List.of(
            AvroSchemas.ORDER_V1,
            AvroSchemas.ORDER_V2,
            AvroSchemas.ORDER_V3,
            AvroSchemas.ORDER_V4
        ));
        
        registerAvro("ecommerce.orders-key", AvroSchemas.ORDER_KEY);
        registerAvro("ecommerce.order-items-value", AvroSchemas.ORDER_ITEM);
        
        // Page views with versions
        registerAvroWithVersions("ecommerce.page-views-value", List.of(
            AvroSchemas.PAGE_VIEW_V1,
            AvroSchemas.PAGE_VIEW_V2,
            AvroSchemas.PAGE_VIEW_V3
        ));
        
        registerAvro("ecommerce.user-sessions-value", AvroSchemas.USER_SESSION);
        registerAvro("ecommerce.cart-events-value", AvroSchemas.CART_EVENT);
        registerAvro("ecommerce.product-catalog-value", AvroSchemas.PRODUCT);
        registerAvro("ecommerce.recommendations-value", AvroSchemas.RECOMMENDATION);
        
        // Payments schemas
        registerAvroWithVersions("payments.transactions-value", List.of(
            AvroSchemas.TRANSACTION_V1,
            AvroSchemas.TRANSACTION_V2,
            AvroSchemas.TRANSACTION_V3
        ));
        registerAvro("payments.card-events-value", AvroSchemas.CARD_EVENT);
        registerAvro("payments.fraud-alerts-value", AvroSchemas.FRAUD_ALERT);
        registerAvro("payments.settlements-value", AvroSchemas.SETTLEMENT);
        
        // Inventory schemas
        registerAvroWithVersions("inventory.stock-updates-value", List.of(
            AvroSchemas.STOCK_UPDATE_V1,
            AvroSchemas.STOCK_UPDATE_V2
        ));
        registerAvro("inventory.warehouse-events-value", AvroSchemas.WAREHOUSE_EVENT);
        registerAvro("inventory.transfers-value", AvroSchemas.TRANSFER);
        
        // Customer schemas
        registerAvroWithVersions("customers.profiles-value", List.of(
            AvroSchemas.CUSTOMER_PROFILE_V1,
            AvroSchemas.CUSTOMER_PROFILE_V2,
            AvroSchemas.CUSTOMER_PROFILE_V3
        ));
        registerAvro("customers.preferences-value", AvroSchemas.CUSTOMER_PREFERENCES);
        registerAvro("customers.loyalty-events-value", AvroSchemas.LOYALTY_EVENT);
        
        // Notification schemas
        registerAvro("notifications.email-outbound-value", AvroSchemas.EMAIL_NOTIFICATION);
        registerAvro("notifications.email-events-value", AvroSchemas.EMAIL_EVENT);
        registerAvro("notifications.push-events-value", AvroSchemas.PUSH_NOTIFICATION);
        registerAvro("notifications.sms-queue-value", AvroSchemas.SMS_NOTIFICATION);
        
        // IoT schemas
        registerAvroWithVersions("iot.sensor-readings-value", List.of(
            AvroSchemas.SENSOR_READING_V1,
            AvroSchemas.SENSOR_READING_V2,
            AvroSchemas.SENSOR_READING_V3
        ));
        registerAvro("iot.device-status-value", AvroSchemas.DEVICE_STATUS);
        registerAvro("iot.device-registry-value", AvroSchemas.DEVICE_REGISTRY);
        registerAvro("iot.alerts-value", AvroSchemas.IOT_ALERT);
        
        // Logs schemas
        registerAvro("logs.security-value", AvroSchemas.SECURITY_LOG);
        
        // Audit schemas
        registerAvro("audit.system-events-value", AvroSchemas.AUDIT_EVENT);
        registerAvro("audit.user-actions-value", AvroSchemas.USER_ACTION);
        registerAvro("audit.data-access-log-value", AvroSchemas.DATA_ACCESS_LOG);
        registerAvro("audit.config-changes-value", AvroSchemas.CONFIG_CHANGE);
        
        // ML schemas
        registerAvro("ml.feature-updates-value", AvroSchemas.FEATURE_UPDATE);
        registerAvro("ml.model-predictions-value", AvroSchemas.MODEL_PREDICTION);
        registerAvro("ml.training-data-value", AvroSchemas.TRAINING_DATA);
        
        // Shipping schemas
        registerAvro("shipping.tracking-updates-value", AvroSchemas.TRACKING_UPDATE);
        registerAvro("shipping.label-requests-value", AvroSchemas.LABEL_REQUEST);
        
        // Metrics schemas
        registerAvro("metrics.application-value", AvroSchemas.APP_METRIC);
        registerAvro("metrics.infrastructure-value", AvroSchemas.INFRA_METRIC);
        registerAvro("metrics.business-kpis-value", AvroSchemas.BUSINESS_KPI);
        
        // Trading schemas
        registerAvroWithVersions("trading.market-data-value", List.of(
            AvroSchemas.MARKET_DATA_V1,
            AvroSchemas.MARKET_DATA_V2
        ));
        registerAvro("trading.positions-value", AvroSchemas.POSITION);
        registerAvro("trading.risk-alerts-value", AvroSchemas.RISK_ALERT);
        
        // Healthcare schemas
        registerAvro("healthcare.appointments-value", AvroSchemas.APPOINTMENT);
        registerAvro("healthcare.lab-results-value", AvroSchemas.LAB_RESULT);
        
        // Connect schemas
        registerAvro("connect.datagen-users-value", AvroSchemas.DATAGEN_USER);
        registerAvro("connect.datagen-pageviews-value", AvroSchemas.DATAGEN_PAGEVIEW);
        
        // KSQL derived schemas
        registerAvro("ksql.order-totals-value", AvroSchemas.ORDER_TOTAL);
        registerAvro("ksql.pageview-regions-value", AvroSchemas.PAGEVIEW_REGION);
        registerAvro("ksql.enriched-orders-value", AvroSchemas.ENRICHED_ORDER);
        registerAvro("ksql.fraud-candidates-value", AvroSchemas.FRAUD_CANDIDATE);
    }
    
    private void registerProtobufSchemas() {
        registerProtobuf("payments.chargebacks-value", ProtobufSchemas.CHARGEBACK);
        registerProtobuf("iot.firmware-updates-value", ProtobufSchemas.FIRMWARE_UPDATE);
        registerProtobuf("shipping.carrier-events-value", ProtobufSchemas.CARRIER_EVENT);
        registerProtobuf("trading.orders-value", ProtobufSchemas.TRADE_ORDER);
        registerProtobuf("trading.executions-value", ProtobufSchemas.TRADE_EXECUTION);
    }
    
    private void registerJsonSchemas() {
        registerJson("ecommerce.product-reviews-value", JsonSchemas.PRODUCT_REVIEW);
        registerJson("inventory.reorder-alerts-value", JsonSchemas.REORDER_ALERT);
        registerJson("customers.feedback-value", JsonSchemas.CUSTOMER_FEEDBACK);
        registerJson("notifications.in-app-value", JsonSchemas.IN_APP_NOTIFICATION);
        registerJson("shipping.route-optimization-value", JsonSchemas.ROUTE_OPTIMIZATION);
        registerJson("ml.model-metrics-value", JsonSchemas.MODEL_METRICS);
    }
    
    private void registerAvro(String subject, String schema) {
        try {
            AvroSchema avroSchema = new AvroSchema(schema);
            int id = client.register(subject, avroSchema);
            log.debug("Registered Avro schema for {} with id {}", subject, id);
        } catch (IOException | RestClientException e) {
            log.error("Failed to register Avro schema for {}: {}", subject, e.getMessage());
        }
    }
    
    private void registerAvroWithVersions(String subject, List<String> schemas) {
        for (String schema : schemas) {
            registerAvro(subject, schema);
        }
        log.info("Registered {} versions for {}", schemas.size(), subject);
    }
    
    private void registerProtobuf(String subject, String schema) {
        try {
            ProtobufSchema protobufSchema = new ProtobufSchema(schema);
            int id = client.register(subject, protobufSchema);
            log.debug("Registered Protobuf schema for {} with id {}", subject, id);
        } catch (IOException | RestClientException e) {
            log.error("Failed to register Protobuf schema for {}: {}", subject, e.getMessage());
        }
    }
    
    private void registerJson(String subject, String schema) {
        try {
            JsonSchema jsonSchema = new JsonSchema(schema);
            int id = client.register(subject, jsonSchema);
            log.debug("Registered JSON schema for {} with id {}", subject, id);
        } catch (IOException | RestClientException e) {
            log.error("Failed to register JSON schema for {}: {}", subject, e.getMessage());
        }
    }
}
