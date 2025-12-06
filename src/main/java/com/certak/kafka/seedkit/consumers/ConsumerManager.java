package com.certak.kafka.seedkit.consumers;

import com.certak.kafka.seedkit.config.SeedKitConfig;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Manages multiple consumer threads that consume and discard messages.
 * This simulates realistic consumer groups for demo purposes.
 * 
 * Includes three types of consumers:
 * - Permanent consumers: Run continuously until shutdown
 * - Temporary consumers: Consume for a fixed period then stop permanently
 * - Intermittent consumers: Come and go at random intervals
 */
public class ConsumerManager {
    private static final Logger log = LoggerFactory.getLogger(ConsumerManager.class);
    
    private final SeedKitConfig config;
    private final ExecutorService executor;
    private final ScheduledExecutorService intermittentScheduler;
    private final List<DemoConsumer> consumers = new ArrayList<>();
    private final List<IntermittentConsumer> intermittentConsumers = new ArrayList<>();
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final Random random = new Random();
    
    public ConsumerManager(SeedKitConfig config) {
        this.config = config;
        this.executor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
        this.intermittentScheduler = Executors.newScheduledThreadPool(4);
    }
    
    /**
     * Start all configured consumer groups.
     */
    public void startConsumers() {
        if (!config.isConsumerGroupsEnabled()) {
            log.info("Consumer groups are disabled");
            return;
        }
        
        running.set(true);
        log.info("Starting consumer groups...");
        
        for (Map<String, Object> groupConfig : config.getConsumerGroupConfigs()) {
            String groupName = (String) groupConfig.get("name");
            @SuppressWarnings("unchecked")
            List<String> topics = (List<String>) groupConfig.get("topics");
            int consumerCount = (int) groupConfig.getOrDefault("consumers-count", 1);
            
            log.info("Starting consumer group '{}' with {} consumers for topics: {}", 
                groupName, consumerCount, topics);
            
            for (int i = 0; i < consumerCount; i++) {
                String consumerId = groupName + "-consumer-" + i;
                DemoConsumer consumer = new DemoConsumer(consumerId, groupName, topics);
                consumers.add(consumer);
                executor.submit(consumer);
            }
        }
        
        // Add some standalone consumers (not part of a group)
        addStandaloneConsumers();
        
        // Add temporary consumers that will stop after a period
        addTemporaryConsumers();
        
        // Add intermittent consumers that come and go
        addIntermittentConsumers();
        
        log.info("Started {} permanent consumers, plus temporary and intermittent consumers", consumers.size());
    }
    
    private void addStandaloneConsumers() {
        // These simulate independent consumers or monitoring tools
        
        // Real-time dashboard consumer
        DemoConsumer dashboardConsumer = new DemoConsumer(
            "realtime-dashboard-001",
            "realtime-dashboard",
            List.of("ecommerce.orders", "payments.transactions", "trading.market-data")
        );
        consumers.add(dashboardConsumer);
        executor.submit(dashboardConsumer);
        
        // Log shipper consumer
        DemoConsumer logShipper = new DemoConsumer(
            "log-shipper-elasticsearch-001",
            "log-shipper-elasticsearch",
            List.of("logs.application", "logs.infrastructure", "logs.security")
        );
        consumers.add(logShipper);
        executor.submit(logShipper);
        
        // Metrics collector
        DemoConsumer metricsCollector = new DemoConsumer(
            "prometheus-metrics-collector-001",
            "prometheus-metrics-collector",
            List.of("metrics.application", "metrics.infrastructure", "metrics.business-kpis")
        );
        consumers.add(metricsCollector);
        executor.submit(metricsCollector);
        
        // Audit compliance consumer
        DemoConsumer auditConsumer = new DemoConsumer(
            "compliance-audit-archiver-001",
            "compliance-audit-archiver",
            List.of("audit.system-events", "audit.user-actions", "audit.data-access-log")
        );
        consumers.add(auditConsumer);
        executor.submit(auditConsumer);
        
        // Data lake ingestion
        DemoConsumer dataLakeConsumer = new DemoConsumer(
            "data-lake-ingestion-001",
            "data-lake-ingestion",
            List.of("ecommerce.orders", "ecommerce.page-views", "payments.transactions", "customers.profiles")
        );
        consumers.add(dataLakeConsumer);
        executor.submit(dataLakeConsumer);
        
        // CDC processor
        DemoConsumer cdcProcessor = new DemoConsumer(
            "cdc-processor-001",
            "cdc-processor",
            List.of("ecommerce.product-catalog", "customers.profiles", "inventory.stock-updates")
        );
        consumers.add(cdcProcessor);
        executor.submit(cdcProcessor);
        
        log.info("Added 6 standalone consumers");
    }
    
    /**
     * Adds temporary consumers that consume for a fixed period then stop permanently.
     */
    private void addTemporaryConsumers() {
        // Batch processing job - runs for 2 minutes
        TemporaryConsumer batchJob = new TemporaryConsumer(
            "batch-etl-job-001",
            "batch-etl-processor",
            List.of("ecommerce.orders", "payments.transactions"),
            Duration.ofMinutes(2)
        );
        executor.submit(batchJob);
        
        // Migration consumer - runs for 3 minutes
        TemporaryConsumer migrationConsumer = new TemporaryConsumer(
            "data-migration-consumer-001",
            "data-migration",
            List.of("ecommerce.page-views", "logs.application"),
            Duration.ofMinutes(3)
        );
        executor.submit(migrationConsumer);
        
        // Backfill job - runs for 90 seconds
        TemporaryConsumer backfillJob = new TemporaryConsumer(
            "backfill-job-001",
            "historical-backfill",
            List.of("iot.sensor-readings", "metrics.application"),
            Duration.ofSeconds(90)
        );
        executor.submit(backfillJob);
        
        // One-time analysis consumer - runs for 60 seconds
        TemporaryConsumer analysisConsumer = new TemporaryConsumer(
            "one-time-analysis-001",
            "adhoc-analysis",
            List.of("trading.market-data", "ecommerce.orders"),
            Duration.ofSeconds(60)
        );
        executor.submit(analysisConsumer);
        
        // Short-lived debug consumer - runs for 45 seconds
        TemporaryConsumer debugConsumer = new TemporaryConsumer(
            "debug-consumer-001",
            "debug-session",
            List.of("logs.application", "logs.infrastructure"),
            Duration.ofSeconds(45)
        );
        executor.submit(debugConsumer);
        
        log.info("Added 5 temporary consumers that will stop after their configured duration");
    }
    
    /**
     * Adds intermittent consumers that randomly come and go.
     */
    private void addIntermittentConsumers() {
        // Scaling consumer group - simulates auto-scaling
        IntermittentConsumer scalingConsumer = new IntermittentConsumer(
            "auto-scaler-001",
            "auto-scaling-group",
            List.of("ecommerce.orders", "payments.transactions"),
            30, 120, // on for 30-120 seconds
            20, 60   // off for 20-60 seconds
        );
        intermittentConsumers.add(scalingConsumer);
        intermittentScheduler.submit(scalingConsumer);
        
        // Spot instance consumer - simulates spot instance behavior
        IntermittentConsumer spotConsumer = new IntermittentConsumer(
            "spot-instance-consumer-001",
            "spot-processing",
            List.of("iot.sensor-readings", "iot.device-status"),
            45, 180, // on for 45-180 seconds
            30, 90   // off for 30-90 seconds
        );
        intermittentConsumers.add(spotConsumer);
        intermittentScheduler.submit(spotConsumer);
        
        // Dev/test consumer - simulates developer testing
        IntermittentConsumer devConsumer = new IntermittentConsumer(
            "dev-test-consumer-001",
            "dev-testing",
            List.of("logs.application", "ecommerce.cart-events"),
            15, 60,  // on for 15-60 seconds
            45, 120  // off for 45-120 seconds
        );
        intermittentConsumers.add(devConsumer);
        intermittentScheduler.submit(devConsumer);
        
        // Periodic reporting consumer - simulates scheduled reports
        IntermittentConsumer reportingConsumer = new IntermittentConsumer(
            "periodic-reporter-001",
            "periodic-reports",
            List.of("metrics.application", "trading.market-data"),
            60, 90,   // on for 60-90 seconds
            120, 180  // off for 120-180 seconds
        );
        intermittentConsumers.add(reportingConsumer);
        intermittentScheduler.submit(reportingConsumer);
        
        // Flaky consumer - simulates unstable consumer
        IntermittentConsumer flakyConsumer = new IntermittentConsumer(
            "flaky-consumer-001",
            "unstable-service",
            List.of("ecommerce.page-views", "notifications.email-outbound"),
            10, 45,  // on for 10-45 seconds
            15, 45   // off for 15-45 seconds
        );
        intermittentConsumers.add(flakyConsumer);
        intermittentScheduler.submit(flakyConsumer);
        
        log.info("Added 5 intermittent consumers that will come and go randomly");
    }
    
    /**
     * Stop all consumers gracefully.
     */
    public void stopConsumers() {
        running.set(false);
        consumers.forEach(DemoConsumer::stop);
        intermittentConsumers.forEach(IntermittentConsumer::stop);
        
        executor.shutdown();
        intermittentScheduler.shutdown();
        
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
            if (!intermittentScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                intermittentScheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            intermittentScheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        long totalMessages = consumers.stream().mapToLong(DemoConsumer::getMessagesConsumed).sum();
        long intermittentMessages = intermittentConsumers.stream().mapToLong(IntermittentConsumer::getMessagesConsumed).sum();
        log.info("All consumers stopped. Permanent consumers: {}, Intermittent total: {}", totalMessages, intermittentMessages);
    }
    
    /**
     * Get statistics about running consumers.
     */
    public Map<String, Long> getConsumerStats() {
        Map<String, Long> stats = new LinkedHashMap<>();
        for (DemoConsumer consumer : consumers) {
            stats.put(consumer.getConsumerId(), consumer.getMessagesConsumed());
        }
        for (IntermittentConsumer consumer : intermittentConsumers) {
            stats.put(consumer.getConsumerId() + " (intermittent)", consumer.getMessagesConsumed());
        }
        return stats;
    }
    
    /**
     * Demo consumer that consumes and discards messages.
     */
    private class DemoConsumer implements Runnable {
        private final String consumerId;
        private final String groupId;
        private final List<String> topics;
        private final AtomicBoolean consumerRunning = new AtomicBoolean(true);
        private final AtomicLong messagesConsumed = new AtomicLong(0);
        private KafkaConsumer<String, String> consumer;
        
        public DemoConsumer(String consumerId, String groupId, List<String> topics) {
            this.consumerId = consumerId;
            this.groupId = groupId;
            this.topics = topics;
        }
        
        @Override
        public void run() {
            try {
                consumer = createConsumer();
                consumer.subscribe(topics);
                log.debug("Consumer {} subscribed to topics: {}", consumerId, topics);
                
                while (running.get() && consumerRunning.get()) {
                    try {
                        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                        
                        if (!records.isEmpty()) {
                            messagesConsumed.addAndGet(records.count());
                            
                            // Simulate some processing time (very small)
                            if (records.count() > 0) {
                                Thread.sleep(Math.min(10, records.count()));
                            }
                        }
                        
                        // Commit periodically (auto-commit is enabled, but explicit commit for demo)
                        if (messagesConsumed.get() % 100 == 0 && messagesConsumed.get() > 0) {
                            consumer.commitAsync();
                        }
                        
                    } catch (Exception e) {
                        if (running.get() && consumerRunning.get()) {
                            log.warn("Consumer {} poll error: {}", consumerId, e.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Consumer {} failed: {}", consumerId, e.getMessage());
            } finally {
                if (consumer != null) {
                    try {
                        consumer.close(Duration.ofSeconds(5));
                    } catch (Exception e) {
                        log.debug("Error closing consumer {}: {}", consumerId, e.getMessage());
                    }
                }
                log.debug("Consumer {} stopped after consuming {} messages", consumerId, messagesConsumed.get());
            }
        }
        
        private KafkaConsumer<String, String> createConsumer() {
            Properties props = new Properties();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
            props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
            props.put(ConsumerConfig.CLIENT_ID_CONFIG, consumerId);
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
            props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "5000");
            props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "500");
            props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, "1");
            props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, "500");
            
            return new KafkaConsumer<>(props);
        }
        
        public void stop() {
            consumerRunning.set(false);
            if (consumer != null) {
                consumer.wakeup();
            }
        }
        
        public String getConsumerId() {
            return consumerId;
        }
        
        public String getGroupId() {
            return groupId;
        }
        
        public long getMessagesConsumed() {
            return messagesConsumed.get();
        }
    }
    
    /**
     * Temporary consumer that runs for a fixed duration then stops permanently.
     */
    private class TemporaryConsumer implements Runnable {
        private final String consumerId;
        private final String groupId;
        private final List<String> topics;
        private final Duration runDuration;
        private final AtomicLong messagesConsumed = new AtomicLong(0);
        
        public TemporaryConsumer(String consumerId, String groupId, List<String> topics, Duration runDuration) {
            this.consumerId = consumerId;
            this.groupId = groupId;
            this.topics = topics;
            this.runDuration = runDuration;
        }
        
        @Override
        public void run() {
            KafkaConsumer<String, String> consumer = null;
            long startTime = System.currentTimeMillis();
            long endTime = startTime + runDuration.toMillis();
            
            try {
                consumer = createConsumer();
                consumer.subscribe(topics);
                log.info("Temporary consumer {} started, will run for {} seconds", consumerId, runDuration.toSeconds());
                
                while (running.get() && System.currentTimeMillis() < endTime) {
                    try {
                        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                        if (!records.isEmpty()) {
                            messagesConsumed.addAndGet(records.count());
                        }
                    } catch (Exception e) {
                        if (running.get()) {
                            log.warn("Temporary consumer {} poll error: {}", consumerId, e.getMessage());
                        }
                    }
                }
                
                log.info("Temporary consumer {} completed after {} seconds, consumed {} messages", 
                    consumerId, (System.currentTimeMillis() - startTime) / 1000, messagesConsumed.get());
                    
            } catch (Exception e) {
                log.error("Temporary consumer {} failed: {}", consumerId, e.getMessage());
            } finally {
                if (consumer != null) {
                    try {
                        consumer.close(Duration.ofSeconds(5));
                    } catch (Exception e) {
                        log.debug("Error closing temporary consumer {}: {}", consumerId, e.getMessage());
                    }
                }
            }
        }
        
        private KafkaConsumer<String, String> createConsumer() {
            Properties props = new Properties();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
            props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
            props.put(ConsumerConfig.CLIENT_ID_CONFIG, consumerId);
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
            props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "500");
            return new KafkaConsumer<>(props);
        }
    }
    
    /**
     * Intermittent consumer that comes and goes at random intervals.
     */
    private class IntermittentConsumer implements Runnable {
        private final String consumerId;
        private final String groupId;
        private final List<String> topics;
        private final int minOnSeconds;
        private final int maxOnSeconds;
        private final int minOffSeconds;
        private final int maxOffSeconds;
        private final AtomicBoolean consumerRunning = new AtomicBoolean(true);
        private final AtomicLong messagesConsumed = new AtomicLong(0);
        private volatile KafkaConsumer<String, String> consumer;
        
        public IntermittentConsumer(String consumerId, String groupId, List<String> topics,
                                     int minOnSeconds, int maxOnSeconds, 
                                     int minOffSeconds, int maxOffSeconds) {
            this.consumerId = consumerId;
            this.groupId = groupId;
            this.topics = topics;
            this.minOnSeconds = minOnSeconds;
            this.maxOnSeconds = maxOnSeconds;
            this.minOffSeconds = minOffSeconds;
            this.maxOffSeconds = maxOffSeconds;
        }
        
        @Override
        public void run() {
            log.info("Intermittent consumer {} started (on: {}-{}s, off: {}-{}s)", 
                consumerId, minOnSeconds, maxOnSeconds, minOffSeconds, maxOffSeconds);
            
            while (running.get() && consumerRunning.get()) {
                try {
                    // Consumer ON phase
                    int onDuration = minOnSeconds + random.nextInt(maxOnSeconds - minOnSeconds + 1);
                    log.debug("Intermittent consumer {} coming online for {} seconds", consumerId, onDuration);
                    
                    consumer = createConsumer();
                    consumer.subscribe(topics);
                    
                    long onEndTime = System.currentTimeMillis() + (onDuration * 1000L);
                    while (running.get() && consumerRunning.get() && System.currentTimeMillis() < onEndTime) {
                        try {
                            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
                            if (!records.isEmpty()) {
                                messagesConsumed.addAndGet(records.count());
                            }
                        } catch (Exception e) {
                            if (running.get() && consumerRunning.get()) {
                                log.warn("Intermittent consumer {} poll error: {}", consumerId, e.getMessage());
                            }
                            break;
                        }
                    }
                    
                    // Close consumer for OFF phase
                    if (consumer != null) {
                        try {
                            consumer.close(Duration.ofSeconds(2));
                        } catch (Exception e) {
                            log.debug("Error closing intermittent consumer {}: {}", consumerId, e.getMessage());
                        }
                        consumer = null;
                    }
                    
                    if (!running.get() || !consumerRunning.get()) break;
                    
                    // Consumer OFF phase
                    int offDuration = minOffSeconds + random.nextInt(maxOffSeconds - minOffSeconds + 1);
                    log.debug("Intermittent consumer {} going offline for {} seconds", consumerId, offDuration);
                    
                    Thread.sleep(offDuration * 1000L);
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("Intermittent consumer {} error: {}", consumerId, e.getMessage());
                    try {
                        Thread.sleep(5000); // Wait before retrying
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
            
            // Final cleanup
            if (consumer != null) {
                try {
                    consumer.close(Duration.ofSeconds(2));
                } catch (Exception e) {
                    log.debug("Error closing intermittent consumer {}: {}", consumerId, e.getMessage());
                }
            }
            log.info("Intermittent consumer {} stopped, total consumed: {}", consumerId, messagesConsumed.get());
        }
        
        private KafkaConsumer<String, String> createConsumer() {
            Properties props = new Properties();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
            props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
            props.put(ConsumerConfig.CLIENT_ID_CONFIG, consumerId + "-" + System.currentTimeMillis());
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
            props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "500");
            props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "10000");
            return new KafkaConsumer<>(props);
        }
        
        public void stop() {
            consumerRunning.set(false);
            if (consumer != null) {
                consumer.wakeup();
            }
        }
        
        public String getConsumerId() {
            return consumerId;
        }
        
        public long getMessagesConsumed() {
            return messagesConsumed.get();
        }
    }
}
