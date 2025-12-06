package com.certak.kafka.seedkit.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Configuration loader and holder for Kafka SeedKit.
 */
public class SeedKitConfig {
    private static final Logger log = LoggerFactory.getLogger(SeedKitConfig.class);
    
    private Map<String, Object> kafka;
    private Map<String, Object> schemaRegistry;
    private Map<String, Object> kafkaConnect;
    private Map<String, Object> ksqldb;
    private Map<String, Object> topics;
    private Map<String, Object> seeding;
    private Map<String, Object> consumerGroups;
    
    public static SeedKitConfig load() {
        return load("application.yaml");
    }
    
    public static SeedKitConfig load(String resourceName) {
        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            InputStream is = SeedKitConfig.class.getClassLoader().getResourceAsStream(resourceName);
            if (is == null) {
                throw new RuntimeException("Configuration file not found: " + resourceName);
            }
            Map<String, Object> raw = mapper.readValue(is, Map.class);
            
            SeedKitConfig config = new SeedKitConfig();
            config.kafka = (Map<String, Object>) raw.get("kafka");
            config.schemaRegistry = (Map<String, Object>) raw.get("schema-registry");
            config.kafkaConnect = (Map<String, Object>) raw.get("kafka-connect");
            config.ksqldb = (Map<String, Object>) raw.get("ksqldb");
            config.topics = (Map<String, Object>) raw.get("topics");
            config.seeding = (Map<String, Object>) raw.get("seeding");
            config.consumerGroups = (Map<String, Object>) raw.get("consumer-groups");
            
            log.info("Loaded configuration from {}", resourceName);
            return config;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load configuration", e);
        }
    }
    
    // Kafka getters
    public String getBootstrapServers() {
        return (String) kafka.get("bootstrap-servers");
    }
    
    public String getClientId() {
        return (String) kafka.get("client-id");
    }
    
    public Map<String, Object> getProducerConfig() {
        return (Map<String, Object>) kafka.get("producer");
    }
    
    public Map<String, Object> getConsumerConfig() {
        return (Map<String, Object>) kafka.get("consumer");
    }
    
    // Schema Registry getters
    public String getSchemaRegistryUrl() {
        return (String) schemaRegistry.get("primary-url");
    }
    
    public String getSecondarySchemaRegistryUrl() {
        return (String) schemaRegistry.get("secondary-url");
    }
    
    // Kafka Connect getters
    @SuppressWarnings("unchecked")
    public List<Map<String, String>> getConnectClusters() {
        return (List<Map<String, String>>) kafkaConnect.get("clusters");
    }
    
    public String getPrimaryConnectUrl() {
        List<Map<String, String>> clusters = getConnectClusters();
        return clusters.isEmpty() ? null : clusters.get(0).get("url");
    }
    
    public String getSecondaryConnectUrl() {
        List<Map<String, String>> clusters = getConnectClusters();
        return clusters.size() > 1 ? clusters.get(1).get("url") : null;
    }
    
    // KSQL getters
    public String getKsqlUrl() {
        return (String) ksqldb.get("url");
    }
    
    // Topics getters
    public int getDefaultReplicationFactor() {
        return (int) topics.getOrDefault("default-replication-factor", 1);
    }
    
    public int getDefaultPartitions() {
        return (int) topics.getOrDefault("default-partitions", 3);
    }
    
    public int getHighThroughputPartitions() {
        return (int) topics.getOrDefault("high-throughput-partitions", 12);
    }
    
    // Seeding getters
    public boolean isCleanStart() {
        return (boolean) seeding.getOrDefault("clean-start", true);
    }
    
    public boolean isContinuousProducerEnabled() {
        Map<String, Object> cp = (Map<String, Object>) seeding.get("continuous-producer");
        return cp != null && (boolean) cp.getOrDefault("enabled", true);
    }
    
    public int getMinIntervalMs() {
        Map<String, Object> cp = (Map<String, Object>) seeding.get("continuous-producer");
        return cp != null ? (int) cp.getOrDefault("min-interval-ms", 500) : 500;
    }
    
    public int getMaxIntervalMs() {
        Map<String, Object> cp = (Map<String, Object>) seeding.get("continuous-producer");
        return cp != null ? (int) cp.getOrDefault("max-interval-ms", 5000) : 5000;
    }
    
    public int getInitialMessageCount() {
        return (int) seeding.getOrDefault("initial-message-count", 50);
    }
    
    public int getLargeMessageCount() {
        return (int) seeding.getOrDefault("large-message-count", 10);
    }
    
    // Consumer groups getters
    public boolean isConsumerGroupsEnabled() {
        return consumerGroups != null && (boolean) consumerGroups.getOrDefault("enabled", true);
    }
    
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getConsumerGroupConfigs() {
        return consumerGroups != null ? 
            (List<Map<String, Object>>) consumerGroups.get("groups") : 
            List.of();
    }
}
