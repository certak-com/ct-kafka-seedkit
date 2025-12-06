package com.certak.kafka.seedkit.producers;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import com.certak.kafka.seedkit.config.SeedKitConfig;
import com.certak.kafka.seedkit.data.DataGenerator;
import com.certak.kafka.seedkit.topics.TopicDefinitions;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Produces messages to Kafka topics - both initial seeding and continuous production.
 * Uses a mix of compression types: lz4, snappy, gzip, and none.
 */
public class MessageProducer {
    private static final Logger log = LoggerFactory.getLogger(MessageProducer.class);
    
    // Compression types used across different producers
    private static final String COMPRESSION_LZ4 = "lz4";
    private static final String COMPRESSION_SNAPPY = "snappy";
    private static final String COMPRESSION_GZIP = "gzip";
    private static final String COMPRESSION_NONE = "none";
    
    private final SeedKitConfig config;
    private final DataGenerator dataGenerator;
    // Multiple producers with different compression types
    private final KafkaProducer<String, String> stringProducerLz4;
    private final KafkaProducer<String, String> stringProducerSnappy;
    private final KafkaProducer<String, String> stringProducerGzip;
    private final KafkaProducer<String, String> stringProducerNone;
    private final KafkaProducer<String, GenericRecord> avroProducerLz4;
    private final KafkaProducer<String, GenericRecord> avroProducerSnappy;
    private final KafkaProducer<String, GenericRecord> avroProducerNone;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicLong messageCount = new AtomicLong(0);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(8);
    private final List<ScheduledFuture<?>> scheduledTasks = new ArrayList<>();
    private final Random random = new Random();
    
    public MessageProducer(SeedKitConfig config) {
        this.config = config;
        this.dataGenerator = new DataGenerator();
        
        // Create string producers with different compression types
        this.stringProducerLz4 = createStringProducer("seedkit-string-producer-lz4", COMPRESSION_LZ4);
        this.stringProducerSnappy = createStringProducer("seedkit-string-producer-snappy", COMPRESSION_SNAPPY);
        this.stringProducerGzip = createStringProducer("seedkit-string-producer-gzip", COMPRESSION_GZIP);
        this.stringProducerNone = createStringProducer("seedkit-string-producer-none", COMPRESSION_NONE);
        
        // Create avro producers with different compression types
        this.avroProducerLz4 = createAvroProducer("seedkit-avro-producer-lz4", COMPRESSION_LZ4);
        this.avroProducerSnappy = createAvroProducer("seedkit-avro-producer-snappy", COMPRESSION_SNAPPY);
        this.avroProducerNone = createAvroProducer("seedkit-avro-producer-none", COMPRESSION_NONE);
        
        log.info("Created producers with compression types: lz4, snappy, gzip, none");
    }
    
    private KafkaProducer<String, String> createStringProducer(String clientId, String compression) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
        props.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, 2097152); // 2MB for large messages
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, compression);
        return new KafkaProducer<>(props);
    }
    
    private KafkaProducer<String, GenericRecord> createAvroProducer(String clientId, String compression) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
        props.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        props.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, config.getSchemaRegistryUrl());
        props.put(KafkaAvroSerializerConfig.AUTO_REGISTER_SCHEMAS, true);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, compression);
        return new KafkaProducer<>(props);
    }
    
    // Helper to get a random string producer
    private KafkaProducer<String, String> getRandomStringProducer() {
        return switch (random.nextInt(4)) {
            case 0 -> stringProducerLz4;
            case 1 -> stringProducerSnappy;
            case 2 -> stringProducerGzip;
            default -> stringProducerNone;
        };
    }
    
    // Helper to get a random avro producer
    private KafkaProducer<String, GenericRecord> getRandomAvroProducer() {
        return switch (random.nextInt(3)) {
            case 0 -> avroProducerLz4;
            case 1 -> avroProducerSnappy;
            default -> avroProducerNone;
        };
    }
    
    /**
     * Seed all topics with initial data.
     */
    public void seedInitialData() {
        log.info("Seeding initial data to all topics...");
        
        int msgCount = config.getInitialMessageCount();
        int largeMsgCount = config.getLargeMessageCount();
        
        // Seed Avro topics
        seedAvroTopic("ecommerce.orders", msgCount, this::generateOrderRecord);
        seedAvroTopic("ecommerce.page-views", msgCount, this::generatePageViewRecord);
        seedAvroTopic("ecommerce.cart-events", msgCount / 2, this::generateCartEventRecord);
        seedAvroTopic("payments.transactions", msgCount, this::generateTransactionRecord);
        seedAvroTopic("iot.sensor-readings", msgCount * 2, this::generateSensorReadingRecord);
        seedAvroTopic("iot.device-status", msgCount / 2, this::generateDeviceStatusRecord);
        seedAvroTopic("notifications.email-outbound", msgCount / 2, this::generateEmailNotificationRecord);
        seedAvroTopic("trading.market-data", msgCount, this::generateMarketDataRecord);
        seedAvroTopic("metrics.application", msgCount, this::generateAppMetricRecord);
        seedAvroTopic("audit.system-events", msgCount / 2, this::generateAuditEventRecord);
        
        // Seed JSON topics (no schema)
        seedJsonTopic("logs.application", msgCount * 2, () -> dataGenerator.generateApplicationLog());
        seedJsonTopic("logs.infrastructure", msgCount, () -> dataGenerator.generateApplicationLog());
        seedJsonTopic("ecommerce.search-queries", msgCount, () -> generateSearchQueryJson());
        seedJsonTopic("integration.webhook-inbound", msgCount / 2, () -> generateWebhookJson());
        seedJsonTopic("metrics.custom-events", msgCount / 2, () -> generateCustomEventJson());
        
        // Seed plain text topics
        seedPlainTextTopic("logs.access", msgCount * 2, () -> dataGenerator.generateAccessLog());
        seedPlainTextTopic("integration.legacy-mainframe", msgCount / 4, () -> generateMainframeRecord());
        seedPlainTextTopic("healthcare.hl7-messages", msgCount / 4, () -> generateHl7Message());
        
        // Seed XML topics
        seedXmlTopic("integration.erp-sync", msgCount / 2, () -> dataGenerator.generateErpSyncXml());
        seedXmlTopic("integration.crm-events", msgCount / 2, () -> dataGenerator.generateCrmXml());
        
        // Seed large message topics
        seedLargeJsonTopic("data.bulk-imports", largeMsgCount, 150);
        seedLargeJsonTopic("ml.experiment-results", largeMsgCount, 200);
        seedLargeJsonTopic("healthcare.fhir-resources", largeMsgCount, 100);
        seedLargeXmlTopic("data.xml-transforms", largeMsgCount, 150);
        
        // Seed binary topics
        seedBinaryTopic("data.binary-blobs", largeMsgCount / 2);
        seedBinaryTopic("iot.raw-telemetry", msgCount);
        
        // Seed CSV batches
        seedCsvTopic("data.csv-batches", largeMsgCount);
        
        log.info("Initial seeding completed. Total messages: {}", messageCount.get());
    }
    
    /**
     * Start continuous message production.
     */
    public void startContinuousProduction() {
        if (!config.isContinuousProducerEnabled()) {
            log.info("Continuous production is disabled");
            return;
        }
        
        running.set(true);
        log.info("Starting continuous message production...");
        
        int minInterval = config.getMinIntervalMs();
        int maxInterval = config.getMaxIntervalMs();
        
        // Schedule continuous producers for different topics with varying intervals
        scheduleContinuousProducer("ecommerce.orders", minInterval, maxInterval, this::produceOrderMessage);
        scheduleContinuousProducer("ecommerce.page-views", minInterval / 2, maxInterval / 2, this::producePageViewMessage);
        scheduleContinuousProducer("ecommerce.cart-events", minInterval, maxInterval * 2, this::produceCartEventMessage);
        scheduleContinuousProducer("payments.transactions", minInterval, maxInterval, this::produceTransactionMessage);
        scheduleContinuousProducer("iot.sensor-readings", minInterval / 4, maxInterval / 2, this::produceSensorReadingMessage);
        scheduleContinuousProducer("iot.device-status", minInterval * 2, maxInterval * 2, this::produceDeviceStatusMessage);
        scheduleContinuousProducer("logs.application", minInterval / 2, maxInterval / 2, this::produceApplicationLogMessage);
        scheduleContinuousProducer("logs.infrastructure", minInterval, maxInterval, this::produceInfraLogMessage);
        scheduleContinuousProducer("notifications.email-outbound", minInterval * 2, maxInterval * 2, this::produceEmailNotificationMessage);
        scheduleContinuousProducer("trading.market-data", minInterval / 2, maxInterval / 2, this::produceMarketDataMessage);
        scheduleContinuousProducer("metrics.application", minInterval, maxInterval, this::produceAppMetricMessage);
        
        // High-frequency producer - produces at least once per second (targeting ~10 messages/sec)
        scheduleHighFrequencyProducer("iot.high-frequency-telemetry", 100, this::produceHighFrequencyTelemetry);
        
        log.info("Continuous production started for {} topics (including high-frequency producer)", scheduledTasks.size());
    }
    
    /**
     * Schedules a high-frequency producer that produces at a fixed rate (at least 1 msg/sec).
     */
    private void scheduleHighFrequencyProducer(String topic, int intervalMs, Runnable producer) {
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            if (running.get()) {
                try {
                    producer.run();
                } catch (Exception e) {
                    log.error("Error in high-frequency producer for {}: {}", topic, e.getMessage());
                }
            }
        }, 0, intervalMs, TimeUnit.MILLISECONDS);
        
        scheduledTasks.add(future);
        log.info("Started high-frequency producer for {} at {}ms interval (~{} msg/sec)", 
            topic, intervalMs, 1000 / intervalMs);
    }
    
    /**
     * Produces high-frequency telemetry messages (at least 10/sec).
     */
    private void produceHighFrequencyTelemetry() {
        try {
            String key = "device-hf-" + random.nextInt(100);
            String value = String.format(
                "{\"deviceId\":\"%s\",\"timestamp\":%d,\"readings\":{\"temp\":%.2f,\"humidity\":%.2f,\"pressure\":%.2f}}",
                key,
                Instant.now().toEpochMilli(),
                20 + random.nextDouble() * 15,
                30 + random.nextDouble() * 50,
                980 + random.nextDouble() * 40
            );
            ProducerRecord<String, String> record = new ProducerRecord<>("iot.high-frequency-telemetry", key, value);
            record.headers().add(new RecordHeader("frequency", "high".getBytes()));
            stringProducerLz4.send(record); // Use lz4 for high-frequency data
            messageCount.incrementAndGet();
        } catch (Exception e) {
            log.error("Error producing high-frequency telemetry: {}", e.getMessage());
        }
    }
    
    private void scheduleContinuousProducer(String topic, int minIntervalMs, int maxIntervalMs, Runnable producer) {
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            if (running.get()) {
                try {
                    producer.run();
                    // Add some randomness to the interval
                    int jitter = random.nextInt(Math.max(1, (maxIntervalMs - minIntervalMs) / 2));
                    Thread.sleep(jitter);
                } catch (Exception e) {
                    log.error("Error in continuous producer for {}: {}", topic, e.getMessage());
                }
            }
        }, random.nextInt(1000), (minIntervalMs + maxIntervalMs) / 2, TimeUnit.MILLISECONDS);
        
        scheduledTasks.add(future);
    }
    
    // ============================================================
    // AVRO MESSAGE PRODUCERS
    // ============================================================
    
    private void seedAvroTopic(String topic, int count, RecordSupplier supplier) {
        log.debug("Seeding {} messages to {}", count, topic);
        for (int i = 0; i < count; i++) {
            try {
                AvroMessage msg = supplier.get();
                ProducerRecord<String, GenericRecord> record = new ProducerRecord<>(topic, msg.key(), msg.value());
                if (msg.headers() != null) {
                    msg.headers().forEach((k, v) -> record.headers().add(new RecordHeader(k, v.getBytes(StandardCharsets.UTF_8))));
                }
                getRandomAvroProducer().send(record);
                messageCount.incrementAndGet();
            } catch (Exception e) {
                log.error("Error seeding message to {}: {}", topic, e.getMessage());
            }
        }
        flushAllAvroProducers();
    }
    
    private void seedJsonTopic(String topic, int count, java.util.function.Supplier<String> supplier) {
        log.debug("Seeding {} JSON messages to {}", count, topic);
        TopicDefinitions.TopicDef def = getTopicDef(topic);
        
        for (int i = 0; i < count; i++) {
            try {
                String key = def != null && def.hasKeys() ? UUID.randomUUID().toString() : null;
                String value = supplier.get();
                ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);
                if (def != null && def.hasHeaders()) {
                    record.headers().add(new RecordHeader("content-type", "application/json".getBytes()));
                    record.headers().add(new RecordHeader("source", "seedkit".getBytes()));
                }
                getRandomStringProducer().send(record);
                messageCount.incrementAndGet();
            } catch (Exception e) {
                log.error("Error seeding JSON message to {}: {}", topic, e.getMessage());
            }
        }
        flushAllStringProducers();
    }
    
    private void seedPlainTextTopic(String topic, int count, java.util.function.Supplier<String> supplier) {
        log.debug("Seeding {} plain text messages to {}", count, topic);
        TopicDefinitions.TopicDef def = getTopicDef(topic);
        
        for (int i = 0; i < count; i++) {
            try {
                String key = def != null && def.hasKeys() ? UUID.randomUUID().toString() : null;
                getRandomStringProducer().send(new ProducerRecord<>(topic, key, supplier.get()));
                messageCount.incrementAndGet();
            } catch (Exception e) {
                log.error("Error seeding plain text message to {}: {}", topic, e.getMessage());
            }
        }
        flushAllStringProducers();
    }
    
    private void seedXmlTopic(String topic, int count, java.util.function.Supplier<String> supplier) {
        log.debug("Seeding {} XML messages to {}", count, topic);
        TopicDefinitions.TopicDef def = getTopicDef(topic);
        
        for (int i = 0; i < count; i++) {
            try {
                String key = def != null && def.hasKeys() ? UUID.randomUUID().toString() : null;
                ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, supplier.get());
                record.headers().add(new RecordHeader("content-type", "application/xml".getBytes()));
                getRandomStringProducer().send(record);
                messageCount.incrementAndGet();
            } catch (Exception e) {
                log.error("Error seeding XML message to {}: {}", topic, e.getMessage());
            }
        }
        flushAllStringProducers();
    }
    
    private void seedLargeJsonTopic(String topic, int count, int targetSizeKb) {
        log.debug("Seeding {} large JSON messages (~{}KB) to {}", count, targetSizeKb, topic);
        for (int i = 0; i < count; i++) {
            try {
                String key = UUID.randomUUID().toString();
                String value = dataGenerator.generateLargeJson(targetSizeKb);
                ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);
                record.headers().add(new RecordHeader("content-type", "application/json".getBytes()));
                record.headers().add(new RecordHeader("size-kb", String.valueOf(value.length() / 1024).getBytes()));
                // Use gzip for large messages
                stringProducerGzip.send(record);
                messageCount.incrementAndGet();
            } catch (Exception e) {
                log.error("Error seeding large JSON message to {}: {}", topic, e.getMessage());
            }
        }
        stringProducerGzip.flush();
    }
    
    private void seedLargeXmlTopic(String topic, int count, int targetSizeKb) {
        log.debug("Seeding {} large XML messages (~{}KB) to {}", count, targetSizeKb, topic);
        for (int i = 0; i < count; i++) {
            try {
                String key = UUID.randomUUID().toString();
                String value = dataGenerator.generateLargeXml(targetSizeKb);
                ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);
                record.headers().add(new RecordHeader("content-type", "application/xml".getBytes()));
                // Use gzip for large messages
                stringProducerGzip.send(record);
                messageCount.incrementAndGet();
            } catch (Exception e) {
                log.error("Error seeding large XML message to {}: {}", topic, e.getMessage());
            }
        }
        stringProducerGzip.flush();
    }
    
    private void seedBinaryTopic(String topic, int count) {
        log.debug("Seeding {} binary messages to {}", count, topic);
        for (int i = 0; i < count; i++) {
            try {
                String key = UUID.randomUUID().toString();
                byte[] data = new byte[1024 + random.nextInt(50000)];
                random.nextBytes(data);
                ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, Base64.getEncoder().encodeToString(data));
                record.headers().add(new RecordHeader("content-type", "application/octet-stream".getBytes()));
                // Use lz4 for binary data (fast compression)
                stringProducerLz4.send(record);
                messageCount.incrementAndGet();
            } catch (Exception e) {
                log.error("Error seeding binary message to {}: {}", topic, e.getMessage());
            }
        }
        stringProducerLz4.flush();
    }
    
    private void seedCsvTopic(String topic, int count) {
        log.debug("Seeding {} CSV batch messages to {}", count, topic);
        for (int i = 0; i < count; i++) {
            try {
                String key = UUID.randomUUID().toString();
                String value = generateCsvBatch(100 + random.nextInt(400));
                ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);
                record.headers().add(new RecordHeader("content-type", "text/csv".getBytes()));
                record.headers().add(new RecordHeader("batch-id", key.getBytes()));
                // Use snappy for CSV (good balance)
                stringProducerSnappy.send(record);
                messageCount.incrementAndGet();
            } catch (Exception e) {
                log.error("Error seeding CSV message to {}: {}", topic, e.getMessage());
            }
        }
        stringProducerSnappy.flush();
    }
    
    // Flush helpers
    private void flushAllStringProducers() {
        stringProducerLz4.flush();
        stringProducerSnappy.flush();
        stringProducerGzip.flush();
        stringProducerNone.flush();
    }
    
    private void flushAllAvroProducers() {
        avroProducerLz4.flush();
        avroProducerSnappy.flush();
        avroProducerNone.flush();
    }
    
    // ============================================================
    // CONTINUOUS PRODUCTION METHODS
    // ============================================================
    
    private void produceOrderMessage() {
        try {
            AvroMessage msg = generateOrderRecord();
            ProducerRecord<String, GenericRecord> record = new ProducerRecord<>("ecommerce.orders", msg.key(), msg.value());
            if (msg.headers() != null) {
                msg.headers().forEach((k, v) -> record.headers().add(new RecordHeader(k, v.getBytes(StandardCharsets.UTF_8))));
            }
            avroProducerSnappy.send(record);
            messageCount.incrementAndGet();
        } catch (Exception e) {
            log.error("Error producing order message: {}", e.getMessage());
        }
    }
    
    private void producePageViewMessage() {
        try {
            AvroMessage msg = generatePageViewRecord();
            avroProducerLz4.send(new ProducerRecord<>("ecommerce.page-views", msg.key(), msg.value()));
            messageCount.incrementAndGet();
        } catch (Exception e) {
            log.error("Error producing page view message: {}", e.getMessage());
        }
    }
    
    private void produceCartEventMessage() {
        try {
            AvroMessage msg = generateCartEventRecord();
            avroProducerNone.send(new ProducerRecord<>("ecommerce.cart-events", msg.key(), msg.value()));
            messageCount.incrementAndGet();
        } catch (Exception e) {
            log.error("Error producing cart event message: {}", e.getMessage());
        }
    }
    
    private void produceTransactionMessage() {
        try {
            AvroMessage msg = generateTransactionRecord();
            ProducerRecord<String, GenericRecord> record = new ProducerRecord<>("payments.transactions", msg.key(), msg.value());
            if (msg.headers() != null) {
                msg.headers().forEach((k, v) -> record.headers().add(new RecordHeader(k, v.getBytes(StandardCharsets.UTF_8))));
            }
            avroProducerSnappy.send(record);
            messageCount.incrementAndGet();
        } catch (Exception e) {
            log.error("Error producing transaction message: {}", e.getMessage());
        }
    }
    
    private void produceSensorReadingMessage() {
        try {
            AvroMessage msg = generateSensorReadingRecord();
            ProducerRecord<String, GenericRecord> record = new ProducerRecord<>("iot.sensor-readings", msg.key(), msg.value());
            if (msg.headers() != null) {
                msg.headers().forEach((k, v) -> record.headers().add(new RecordHeader(k, v.getBytes(StandardCharsets.UTF_8))));
            }
            avroProducerLz4.send(record);
            messageCount.incrementAndGet();
        } catch (Exception e) {
            log.error("Error producing sensor reading message: {}", e.getMessage());
        }
    }
    
    private void produceDeviceStatusMessage() {
        try {
            AvroMessage msg = generateDeviceStatusRecord();
            avroProducerNone.send(new ProducerRecord<>("iot.device-status", msg.key(), msg.value()));
            messageCount.incrementAndGet();
        } catch (Exception e) {
            log.error("Error producing device status message: {}", e.getMessage());
        }
    }
    
    private void produceApplicationLogMessage() {
        try {
            String logMsg = dataGenerator.generateApplicationLog();
            stringProducerSnappy.send(new ProducerRecord<>("logs.application", null, logMsg));
            messageCount.incrementAndGet();
        } catch (Exception e) {
            log.error("Error producing application log message: {}", e.getMessage());
        }
    }
    
    private void produceInfraLogMessage() {
        try {
            String logMsg = dataGenerator.generateApplicationLog();
            stringProducerGzip.send(new ProducerRecord<>("logs.infrastructure", null, logMsg));
            messageCount.incrementAndGet();
        } catch (Exception e) {
            log.error("Error producing infra log message: {}", e.getMessage());
        }
    }
    
    private void produceEmailNotificationMessage() {
        try {
            AvroMessage msg = generateEmailNotificationRecord();
            ProducerRecord<String, GenericRecord> record = new ProducerRecord<>("notifications.email-outbound", msg.key(), msg.value());
            if (msg.headers() != null) {
                msg.headers().forEach((k, v) -> record.headers().add(new RecordHeader(k, v.getBytes(StandardCharsets.UTF_8))));
            }
            avroProducerNone.send(record);
            messageCount.incrementAndGet();
        } catch (Exception e) {
            log.error("Error producing email notification message: {}", e.getMessage());
        }
    }
    
    private void produceMarketDataMessage() {
        try {
            AvroMessage msg = generateMarketDataRecord();
            avroProducerLz4.send(new ProducerRecord<>("trading.market-data", msg.key(), msg.value()));
            messageCount.incrementAndGet();
        } catch (Exception e) {
            log.error("Error producing market data message: {}", e.getMessage());
        }
    }
    
    private void produceAppMetricMessage() {
        try {
            AvroMessage msg = generateAppMetricRecord();
            avroProducerSnappy.send(new ProducerRecord<>("metrics.application", msg.key(), msg.value()));
            messageCount.incrementAndGet();
        } catch (Exception e) {
            log.error("Error producing app metric message: {}", e.getMessage());
        }
    }
    
    // ============================================================
    // RECORD GENERATION METHODS
    // ============================================================
    
    private AvroMessage generateOrderRecord() {
        Map<String, Object> data = dataGenerator.generateOrder();
        String schemaJson = """
            {"type":"record","name":"Order","namespace":"com.certak.kafka.ecommerce","fields":[
                {"name":"orderId","type":"string"},
                {"name":"customerId","type":"string"},
                {"name":"customerEmail","type":["null","string"],"default":null},
                {"name":"totalAmount","type":"double"},
                {"name":"taxAmount","type":["null","double"],"default":null},
                {"name":"discountAmount","type":["null","double"],"default":null},
                {"name":"currency","type":"string","default":"USD"},
                {"name":"status","type":"string"},
                {"name":"paymentMethod","type":["null","string"],"default":null},
                {"name":"notes","type":["null","string"],"default":null},
                {"name":"createdAt","type":"long"},
                {"name":"updatedAt","type":["null","long"],"default":null}
            ]}
            """;
        
        Schema schema = new Schema.Parser().parse(schemaJson);
        GenericRecord record = new GenericData.Record(schema);
        record.put("orderId", data.get("orderId"));
        record.put("customerId", data.get("customerId"));
        record.put("customerEmail", data.get("customerEmail"));
        record.put("totalAmount", data.get("totalAmount"));
        record.put("taxAmount", data.get("taxAmount"));
        record.put("discountAmount", data.get("discountAmount"));
        record.put("currency", data.get("currency"));
        record.put("status", data.get("status"));
        record.put("paymentMethod", data.get("paymentMethod"));
        record.put("notes", data.get("notes"));
        record.put("createdAt", data.get("createdAt"));
        record.put("updatedAt", data.get("updatedAt"));
        
        Map<String, String> headers = Map.of(
            "event-type", "order.created",
            "correlation-id", UUID.randomUUID().toString()
        );
        
        return new AvroMessage((String) data.get("orderId"), record, headers);
    }
    
    private AvroMessage generatePageViewRecord() {
        Map<String, Object> data = dataGenerator.generatePageView();
        String schemaJson = """
            {"type":"record","name":"PageView","namespace":"com.certak.kafka.analytics","fields":[
                {"name":"viewId","type":"string"},
                {"name":"userId","type":"string"},
                {"name":"sessionId","type":["null","string"],"default":null},
                {"name":"pageUrl","type":"string"},
                {"name":"pageTitle","type":["null","string"],"default":null},
                {"name":"referrer","type":["null","string"],"default":null},
                {"name":"userAgent","type":["null","string"],"default":null},
                {"name":"ipAddress","type":["null","string"],"default":null},
                {"name":"country","type":["null","string"],"default":null},
                {"name":"region","type":["null","string"],"default":null},
                {"name":"deviceType","type":["null","string"],"default":null},
                {"name":"browser","type":["null","string"],"default":null},
                {"name":"timestamp","type":"long"},
                {"name":"durationMs","type":["null","long"],"default":null}
            ]}
            """;
        
        Schema schema = new Schema.Parser().parse(schemaJson);
        GenericRecord record = new GenericData.Record(schema);
        record.put("viewId", data.get("viewId"));
        record.put("userId", data.get("userId"));
        record.put("sessionId", data.get("sessionId"));
        record.put("pageUrl", data.get("pageUrl"));
        record.put("pageTitle", data.get("pageTitle"));
        record.put("referrer", data.get("referrer"));
        record.put("userAgent", data.get("userAgent"));
        record.put("ipAddress", data.get("ipAddress"));
        record.put("country", data.get("country"));
        record.put("region", data.get("region"));
        record.put("deviceType", data.get("deviceType"));
        record.put("browser", data.get("browser"));
        record.put("timestamp", data.get("timestamp"));
        record.put("durationMs", ((Number) data.get("durationMs")).longValue());
        
        return new AvroMessage((String) data.get("userId"), record, null);
    }
    
    private AvroMessage generateCartEventRecord() {
        Map<String, Object> data = dataGenerator.generateCartEvent();
        String schemaJson = """
            {"type":"record","name":"CartEvent","namespace":"com.certak.kafka.ecommerce","fields":[
                {"name":"eventId","type":"string"},
                {"name":"cartId","type":"string"},
                {"name":"userId","type":"string"},
                {"name":"eventType","type":"string"},
                {"name":"productId","type":["null","string"],"default":null},
                {"name":"quantity","type":["null","int"],"default":null},
                {"name":"timestamp","type":"long"}
            ]}
            """;
        
        Schema schema = new Schema.Parser().parse(schemaJson);
        GenericRecord record = new GenericData.Record(schema);
        record.put("eventId", data.get("eventId"));
        record.put("cartId", data.get("cartId"));
        record.put("userId", data.get("userId"));
        record.put("eventType", data.get("eventType"));
        record.put("productId", data.get("productId"));
        record.put("quantity", data.get("quantity"));
        record.put("timestamp", data.get("timestamp"));
        
        return new AvroMessage((String) data.get("userId"), record, null);
    }
    
    private AvroMessage generateTransactionRecord() {
        Map<String, Object> data = dataGenerator.generateTransaction();
        String schemaJson = """
            {"type":"record","name":"Transaction","namespace":"com.certak.kafka.payments","fields":[
                {"name":"transactionId","type":"string"},
                {"name":"orderId","type":"string"},
                {"name":"customerId","type":["null","string"],"default":null},
                {"name":"amount","type":"double"},
                {"name":"fee","type":["null","double"],"default":null},
                {"name":"netAmount","type":["null","double"],"default":null},
                {"name":"currency","type":"string","default":"USD"},
                {"name":"paymentMethod","type":"string"},
                {"name":"cardLast4","type":["null","string"],"default":null},
                {"name":"cardBrand","type":["null","string"],"default":null},
                {"name":"status","type":"string"},
                {"name":"gatewayResponse","type":["null","string"],"default":null},
                {"name":"riskScore","type":["null","double"],"default":null},
                {"name":"timestamp","type":"long"},
                {"name":"processedAt","type":["null","long"],"default":null}
            ]}
            """;
        
        Schema schema = new Schema.Parser().parse(schemaJson);
        GenericRecord record = new GenericData.Record(schema);
        record.put("transactionId", data.get("transactionId"));
        record.put("orderId", data.get("orderId"));
        record.put("customerId", data.get("customerId"));
        record.put("amount", data.get("amount"));
        record.put("fee", data.get("fee"));
        record.put("netAmount", data.get("netAmount"));
        record.put("currency", data.get("currency"));
        record.put("paymentMethod", data.get("paymentMethod"));
        record.put("cardLast4", data.get("cardLast4"));
        record.put("cardBrand", data.get("cardBrand"));
        record.put("status", data.get("status"));
        record.put("gatewayResponse", data.get("gatewayResponse"));
        record.put("riskScore", data.get("riskScore"));
        record.put("timestamp", data.get("timestamp"));
        record.put("processedAt", data.get("processedAt"));
        
        Map<String, String> headers = Map.of(
            "event-type", "payment.processed",
            "idempotency-key", UUID.randomUUID().toString()
        );
        
        return new AvroMessage((String) data.get("transactionId"), record, headers);
    }
    
    private AvroMessage generateSensorReadingRecord() {
        Map<String, Object> data = dataGenerator.generateSensorReading();
        String schemaJson = """
            {"type":"record","name":"SensorReading","namespace":"com.certak.kafka.iot","fields":[
                {"name":"readingId","type":"string"},
                {"name":"deviceId","type":"string"},
                {"name":"sensorId","type":["null","string"],"default":null},
                {"name":"sensorType","type":"string"},
                {"name":"value","type":"double"},
                {"name":"unit","type":"string"},
                {"name":"quality","type":["null","double"],"default":null},
                {"name":"timestamp","type":"long"},
                {"name":"receivedAt","type":["null","long"],"default":null}
            ]}
            """;
        
        Schema schema = new Schema.Parser().parse(schemaJson);
        GenericRecord record = new GenericData.Record(schema);
        record.put("readingId", data.get("readingId"));
        record.put("deviceId", data.get("deviceId"));
        record.put("sensorId", data.get("sensorId"));
        record.put("sensorType", data.get("sensorType"));
        record.put("value", data.get("value"));
        record.put("unit", data.get("unit"));
        record.put("quality", data.get("quality"));
        record.put("timestamp", data.get("timestamp"));
        record.put("receivedAt", data.get("receivedAt"));
        
        Map<String, String> headers = Map.of(
            "device-id", (String) data.get("deviceId"),
            "sensor-type", (String) data.get("sensorType")
        );
        
        return new AvroMessage((String) data.get("deviceId"), record, headers);
    }
    
    private AvroMessage generateDeviceStatusRecord() {
        Map<String, Object> data = dataGenerator.generateDeviceStatus();
        String schemaJson = """
            {"type":"record","name":"DeviceStatus","namespace":"com.certak.kafka.iot","fields":[
                {"name":"deviceId","type":"string"},
                {"name":"status","type":"string"},
                {"name":"batteryLevel","type":["null","int"],"default":null},
                {"name":"signalStrength","type":["null","int"],"default":null},
                {"name":"firmwareVersion","type":["null","string"],"default":null},
                {"name":"lastSeen","type":"long"},
                {"name":"errorMessage","type":["null","string"],"default":null}
            ]}
            """;
        
        Schema schema = new Schema.Parser().parse(schemaJson);
        GenericRecord record = new GenericData.Record(schema);
        record.put("deviceId", data.get("deviceId"));
        record.put("status", data.get("status"));
        record.put("batteryLevel", data.get("batteryLevel"));
        record.put("signalStrength", data.get("signalStrength"));
        record.put("firmwareVersion", data.get("firmwareVersion"));
        record.put("lastSeen", data.get("lastSeen"));
        record.put("errorMessage", data.get("errorMessage"));
        
        return new AvroMessage((String) data.get("deviceId"), record, null);
    }
    
    private AvroMessage generateEmailNotificationRecord() {
        Map<String, Object> data = dataGenerator.generateEmailNotification();
        String schemaJson = """
            {"type":"record","name":"EmailNotification","namespace":"com.certak.kafka.notifications","fields":[
                {"name":"notificationId","type":"string"},
                {"name":"recipientEmail","type":"string"},
                {"name":"recipientName","type":["null","string"],"default":null},
                {"name":"subject","type":"string"},
                {"name":"templateId","type":"string"},
                {"name":"priority","type":"string"},
                {"name":"scheduledAt","type":["null","long"],"default":null},
                {"name":"createdAt","type":"long"}
            ]}
            """;
        
        Schema schema = new Schema.Parser().parse(schemaJson);
        GenericRecord record = new GenericData.Record(schema);
        record.put("notificationId", data.get("notificationId"));
        record.put("recipientEmail", data.get("recipientEmail"));
        record.put("recipientName", data.get("recipientName"));
        record.put("subject", data.get("subject"));
        record.put("templateId", data.get("templateId"));
        record.put("priority", data.get("priority"));
        record.put("scheduledAt", data.get("scheduledAt"));
        record.put("createdAt", data.get("createdAt"));
        
        Map<String, String> headers = Map.of(
            "template-id", (String) data.get("templateId"),
            "priority", (String) data.get("priority")
        );
        
        return new AvroMessage((String) data.get("notificationId"), record, headers);
    }
    
    private AvroMessage generateMarketDataRecord() {
        Map<String, Object> data = dataGenerator.generateMarketData();
        String schemaJson = """
            {"type":"record","name":"MarketData","namespace":"com.certak.kafka.trading","fields":[
                {"name":"symbol","type":"string"},
                {"name":"exchange","type":"string"},
                {"name":"price","type":"double"},
                {"name":"bid","type":["null","double"],"default":null},
                {"name":"ask","type":["null","double"],"default":null},
                {"name":"bidSize","type":["null","long"],"default":null},
                {"name":"askSize","type":["null","long"],"default":null},
                {"name":"volume","type":"long"},
                {"name":"vwap","type":["null","double"],"default":null},
                {"name":"open","type":["null","double"],"default":null},
                {"name":"high","type":["null","double"],"default":null},
                {"name":"low","type":["null","double"],"default":null},
                {"name":"previousClose","type":["null","double"],"default":null},
                {"name":"timestamp","type":"long"}
            ]}
            """;
        
        Schema schema = new Schema.Parser().parse(schemaJson);
        GenericRecord record = new GenericData.Record(schema);
        record.put("symbol", data.get("symbol"));
        record.put("exchange", data.get("exchange"));
        record.put("price", data.get("price"));
        record.put("bid", data.get("bid"));
        record.put("ask", data.get("ask"));
        record.put("bidSize", data.get("bidSize"));
        record.put("askSize", data.get("askSize"));
        record.put("volume", ((Number) data.get("volume")).longValue());
        record.put("vwap", data.get("vwap"));
        record.put("open", data.get("open"));
        record.put("high", data.get("high"));
        record.put("low", data.get("low"));
        record.put("previousClose", data.get("previousClose"));
        record.put("timestamp", data.get("timestamp"));
        
        return new AvroMessage((String) data.get("symbol"), record, null);
    }
    
    private AvroMessage generateAppMetricRecord() {
        Map<String, Object> data = dataGenerator.generateAppMetric();
        String schemaJson = """
            {"type":"record","name":"ApplicationMetric","namespace":"com.certak.kafka.metrics","fields":[
                {"name":"metricId","type":"string"},
                {"name":"serviceName","type":"string"},
                {"name":"instanceId","type":"string"},
                {"name":"metricName","type":"string"},
                {"name":"metricType","type":"string"},
                {"name":"value","type":"double"},
                {"name":"timestamp","type":"long"}
            ]}
            """;
        
        Schema schema = new Schema.Parser().parse(schemaJson);
        GenericRecord record = new GenericData.Record(schema);
        record.put("metricId", data.get("metricId"));
        record.put("serviceName", data.get("serviceName"));
        record.put("instanceId", data.get("instanceId"));
        record.put("metricName", data.get("metricName"));
        record.put("metricType", data.get("metricType"));
        record.put("value", data.get("value"));
        record.put("timestamp", data.get("timestamp"));
        
        return new AvroMessage((String) data.get("serviceName") + "-" + data.get("instanceId"), record, null);
    }
    
    private AvroMessage generateAuditEventRecord() {
        Map<String, Object> data = dataGenerator.generateAuditEvent();
        String schemaJson = """
            {"type":"record","name":"AuditEvent","namespace":"com.certak.kafka.audit","fields":[
                {"name":"eventId","type":"string"},
                {"name":"eventType","type":"string"},
                {"name":"entityType","type":"string"},
                {"name":"entityId","type":"string"},
                {"name":"action","type":"string"},
                {"name":"actorId","type":"string"},
                {"name":"actorType","type":"string"},
                {"name":"previousState","type":["null","string"],"default":null},
                {"name":"newState","type":["null","string"],"default":null},
                {"name":"timestamp","type":"long"}
            ]}
            """;
        
        Schema schema = new Schema.Parser().parse(schemaJson);
        GenericRecord record = new GenericData.Record(schema);
        record.put("eventId", data.get("eventId"));
        record.put("eventType", data.get("eventType"));
        record.put("entityType", data.get("entityType"));
        record.put("entityId", data.get("entityId"));
        record.put("action", data.get("action"));
        record.put("actorId", data.get("actorId"));
        record.put("actorType", data.get("actorType"));
        record.put("previousState", data.get("previousState"));
        record.put("newState", data.get("newState"));
        record.put("timestamp", data.get("timestamp"));
        
        Map<String, String> headers = Map.of(
            "entity-type", (String) data.get("entityType"),
            "action", (String) data.get("action")
        );
        
        return new AvroMessage((String) data.get("eventId"), record, headers);
    }
    
    // ============================================================
    // HELPER METHODS FOR JSON/TEXT GENERATION
    // ============================================================
    
    private String generateSearchQueryJson() {
        return String.format("""
            {"queryId":"%s","userId":"%s","query":"%s","filters":%s,"results":%d,"timestamp":%d}
            """,
            UUID.randomUUID().toString(),
            dataGenerator.getRandomCustomerId(),
            randomSearchTerm(),
            "{\"category\":\"" + randomCategory() + "\"}",
            random.nextInt(500),
            Instant.now().toEpochMilli()
        ).trim();
    }
    
    private String generateWebhookJson() {
        return String.format("""
            {"webhookId":"%s","source":"%s","eventType":"%s","payload":%s,"receivedAt":%d}
            """,
            UUID.randomUUID().toString(),
            randomFrom(List.of("stripe", "shopify", "sendgrid", "twilio", "github")),
            randomFrom(List.of("payment.completed", "order.created", "email.delivered", "message.sent")),
            "{\"id\":\"" + UUID.randomUUID().toString().substring(0, 8) + "\"}",
            Instant.now().toEpochMilli()
        ).trim();
    }
    
    private String generateCustomEventJson() {
        return String.format("""
            {"eventId":"%s","eventName":"%s","properties":%s,"timestamp":%d}
            """,
            UUID.randomUUID().toString(),
            randomFrom(List.of("button_click", "form_submit", "video_play", "scroll_depth", "feature_used")),
            "{\"element\":\"button-" + random.nextInt(100) + "\",\"value\":" + random.nextInt(1000) + "}",
            Instant.now().toEpochMilli()
        ).trim();
    }
    
    private String generateMainframeRecord() {
        // Fixed-width mainframe-style record
        return String.format("%-10s%-30s%-20s%010d%015.2f%-8s%n",
            "TXN" + String.format("%07d", random.nextInt(10000000)),
            padRight(randomName(), 30),
            padRight(randomCity(), 20),
            random.nextInt(1000000000),
            random.nextDouble() * 10000,
            Instant.now().toString().substring(0, 8).replace("-", "")
        );
    }
    
    private String generateHl7Message() {
        String timestamp = Instant.now().toString().replace("-", "").replace(":", "").substring(0, 14);
        return String.format("""
            MSH|^~\\&|SENDING_APP|SENDING_FACILITY|RECEIVING_APP|RECEIVING_FACILITY|%s||ADT^A01|MSG%s|P|2.5
            EVN|A01|%s
            PID|1||PAT%s^^^HOSPITAL||%s^%s||%s|%s|||%s^^%s^%s^%s
            PV1|1|I|WARD^ROOM^BED|||||||ATT^%s^%s
            """,
            timestamp,
            UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
            timestamp,
            String.format("%08d", random.nextInt(100000000)),
            randomLastName().toUpperCase(),
            randomFirstName().toUpperCase(),
            randomDate(),
            random.nextBoolean() ? "M" : "F",
            randomStreet(),
            randomCity(),
            randomState(),
            randomZip(),
            randomLastName().toUpperCase(),
            randomFirstName().toUpperCase()
        );
    }
    
    private String generateCsvBatch(int rows) {
        StringBuilder sb = new StringBuilder();
        sb.append("id,name,email,company,amount,currency,status,created_at\n");
        for (int i = 0; i < rows; i++) {
            sb.append(String.format("%s,%s,%s,%s,%.2f,%s,%s,%s\n",
                UUID.randomUUID().toString().substring(0, 8),
                randomName(),
                randomEmail(),
                randomCompany().replace(",", ""),
                random.nextDouble() * 10000,
                "USD",
                randomFrom(List.of("PENDING", "COMPLETED", "FAILED")),
                Instant.now().minusSeconds(random.nextInt(86400 * 30)).toString()
            ));
        }
        return sb.toString();
    }
    
    // Simple random data helpers
    private String randomSearchTerm() {
        return randomFrom(List.of("laptop", "phone", "headphones", "camera", "watch", "tablet", "speaker", "keyboard"));
    }
    
    private String randomCategory() {
        return randomFrom(List.of("Electronics", "Clothing", "Home", "Sports", "Books"));
    }
    
    private String randomName() {
        return randomFirstName() + " " + randomLastName();
    }
    
    private String randomFirstName() {
        return randomFrom(List.of("John", "Jane", "Bob", "Alice", "Charlie", "Diana", "Edward", "Fiona"));
    }
    
    private String randomLastName() {
        return randomFrom(List.of("Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis"));
    }
    
    private String randomEmail() {
        return randomFirstName().toLowerCase() + "." + randomLastName().toLowerCase() + "@example.com";
    }
    
    private String randomCompany() {
        return randomFrom(List.of("Acme Inc", "Global Corp", "Tech Solutions", "Data Systems", "Cloud Services"));
    }
    
    private String randomStreet() {
        return random.nextInt(9999) + " " + randomFrom(List.of("Main", "Oak", "Maple", "Cedar", "Pine")) + " " + randomFrom(List.of("St", "Ave", "Blvd", "Dr"));
    }
    
    private String randomCity() {
        return randomFrom(List.of("New York", "Los Angeles", "Chicago", "Houston", "Phoenix", "Seattle", "Denver", "Boston"));
    }
    
    private String randomState() {
        return randomFrom(List.of("NY", "CA", "IL", "TX", "AZ", "WA", "CO", "MA"));
    }
    
    private String randomZip() {
        return String.format("%05d", random.nextInt(100000));
    }
    
    private String randomDate() {
        int year = 1950 + random.nextInt(50);
        int month = 1 + random.nextInt(12);
        int day = 1 + random.nextInt(28);
        return String.format("%04d%02d%02d", year, month, day);
    }
    
    private String padRight(String s, int n) {
        return String.format("%-" + n + "s", s).substring(0, n);
    }
    
    private <T> T randomFrom(List<T> list) {
        return list.get(random.nextInt(list.size()));
    }
    
    private TopicDefinitions.TopicDef getTopicDef(String topicName) {
        return TopicDefinitions.getAllTopics().stream()
            .filter(t -> t.name().equals(topicName))
            .findFirst()
            .orElse(null);
    }
    
    public void stop() {
        running.set(false);
        scheduledTasks.forEach(f -> f.cancel(false));
        scheduler.shutdown();
        try {
            scheduler.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        // Close all string producers
        stringProducerLz4.close();
        stringProducerSnappy.close();
        stringProducerGzip.close();
        stringProducerNone.close();
        // Close all avro producers
        avroProducerLz4.close();
        avroProducerSnappy.close();
        avroProducerNone.close();
        log.info("Message producer stopped. Total messages produced: {}", messageCount.get());
    }
    
    public long getMessageCount() {
        return messageCount.get();
    }
    
    @FunctionalInterface
    private interface RecordSupplier {
        AvroMessage get();
    }
    
    private record AvroMessage(String key, GenericRecord value, Map<String, String> headers) {}
}
