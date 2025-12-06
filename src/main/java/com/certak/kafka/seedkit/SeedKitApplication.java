package com.certak.kafka.seedkit;

import com.certak.kafka.seedkit.config.SeedKitConfig;
import com.certak.kafka.seedkit.connectors.ConnectorManager;
import com.certak.kafka.seedkit.consumers.ConsumerManager;
import com.certak.kafka.seedkit.ksql.KsqlManager;
import com.certak.kafka.seedkit.producers.MessageProducer;
import com.certak.kafka.seedkit.schemas.SchemaManager;
import com.certak.kafka.seedkit.topics.TopicManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * Main application class for Certak Kafka SeedKit.
 * 
 * This application sets up and populates a Kafka ecosystem with realistic data
 * for demo and testing purposes of Kafka tools like KafkIO.
 */
public class SeedKitApplication {
    private static final Logger log = LoggerFactory.getLogger(SeedKitApplication.class);
    
    private final SeedKitConfig config;
    private TopicManager topicManager;
    private SchemaManager schemaManager;
    private MessageProducer messageProducer;
    private ConsumerManager consumerManager;
    private ConnectorManager connectorManager;
    private KsqlManager ksqlManager;
    private final CountDownLatch shutdownLatch = new CountDownLatch(1);
    
    public SeedKitApplication(SeedKitConfig config) {
        this.config = config;
    }
    
    public void start() {
        log.info("====================================================================");
        log.info("Certak Kafka SeedKit - Starting");
        log.info("====================================================================");
        log.info("Kafka Bootstrap: {}", config.getBootstrapServers());
        log.info("Schema Registry: {}", config.getSchemaRegistryUrl());
        log.info("Primary Connect: {}", config.getPrimaryConnectUrl());
        log.info("Secondary Connect: {}", config.getSecondaryConnectUrl());
        log.info("KSQL DB: {}", config.getKsqlUrl());
        log.info("====================================================================");
        
        try {
            // Step 1: Create topics
            log.info("\n[Step 1/6] Creating topics...");
            topicManager = new TopicManager(config);
            topicManager.createAllTopics();
            topicManager.close();
            log.info("Topics created successfully\n");
            
            // Step 2: Register schemas
            log.info("[Step 2/6] Registering schemas...");
            schemaManager = new SchemaManager(config.getSchemaRegistryUrl());
            schemaManager.registerAllSchemas();
            log.info("Schemas registered successfully\n");
            
            // Step 3: Deploy connectors
            log.info("[Step 3/6] Deploying Kafka Connect connectors...");
            connectorManager = new ConnectorManager(config);
            connectorManager.deployConnectors();
            log.info("Connectors deployed successfully\n");
            
            // Step 4: Setup KSQL
            log.info("[Step 4/6] Setting up KSQL streams and tables...");
            ksqlManager = new KsqlManager(config);
            ksqlManager.setupKsql();
            log.info("KSQL setup completed successfully\n");
            
            // Step 5: Seed initial data
            log.info("[Step 5/6] Seeding initial data...");
            messageProducer = new MessageProducer(config);
            messageProducer.seedInitialData();
            log.info("Initial data seeded successfully\n");
            
            // Step 6: Start consumers
            log.info("[Step 6/6] Starting consumer groups...");
            consumerManager = new ConsumerManager(config);
            consumerManager.startConsumers();
            log.info("Consumer groups started successfully\n");
            
            // Start continuous production
            if (config.isContinuousProducerEnabled()) {
                log.info("Starting continuous message production...");
                messageProducer.startContinuousProduction();
            }
            
            log.info("====================================================================");
            log.info("Certak Kafka SeedKit - Ready");
            log.info("====================================================================");
            log.info("The Kafka ecosystem is now seeded and running.");
            log.info("Continuous producers are sending messages to selected topics.");
            log.info("Consumer groups are actively consuming messages.");
            log.info("Press Ctrl+C to stop.");
            log.info("====================================================================\n");
            
            // Register shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
            
            // Wait for shutdown signal
            shutdownLatch.await();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.info("Application interrupted");
        } catch (Exception e) {
            log.error("Error starting SeedKit: {}", e.getMessage(), e);
            shutdown();
        }
    }
    
    private void shutdown() {
        log.info("\n====================================================================");
        log.info("Certak Kafka SeedKit - Shutting down");
        log.info("====================================================================");
        
        try {
            if (messageProducer != null) {
                log.info("Stopping message producer...");
                messageProducer.stop();
            }
            
            if (consumerManager != null) {
                log.info("Stopping consumers...");
                consumerManager.stopConsumers();
            }
            
            if (connectorManager != null) {
                connectorManager.close();
            }
            
            if (ksqlManager != null) {
                ksqlManager.close();
            }
            
            log.info("====================================================================");
            log.info("Certak Kafka SeedKit - Shutdown complete");
            log.info("====================================================================");
            
        } finally {
            shutdownLatch.countDown();
        }
    }
    
    public static void main(String[] args) {
        log.info("Loading configuration...");
        SeedKitConfig config = SeedKitConfig.load();
        
        SeedKitApplication app = new SeedKitApplication(config);
        app.start();
    }
}
