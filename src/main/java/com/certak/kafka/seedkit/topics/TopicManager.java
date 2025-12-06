package com.certak.kafka.seedkit.topics;

import com.certak.kafka.seedkit.config.SeedKitConfig;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Manages Kafka topic creation and deletion.
 */
public class TopicManager {
    private static final Logger log = LoggerFactory.getLogger(TopicManager.class);
    
    private final AdminClient adminClient;
    private final SeedKitConfig config;
    
    public TopicManager(SeedKitConfig config) {
        this.config = config;
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
        props.put(AdminClientConfig.CLIENT_ID_CONFIG, "seedkit-admin");
        props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        this.adminClient = AdminClient.create(props);
    }
    
    /**
     * Create all topics defined in TopicDefinitions.
     */
    public void createAllTopics() {
        List<TopicDefinitions.TopicDef> topics = TopicDefinitions.getAllTopics();
        log.info("Creating {} topics...", topics.size());
        
        // Get existing topics
        Set<String> existingTopics = getExistingTopics();
        log.info("Found {} existing topics", existingTopics.size());
        
        // Delete existing topics if clean start
        if (config.isCleanStart()) {
            Set<String> toDelete = topics.stream()
                .map(TopicDefinitions.TopicDef::name)
                .filter(existingTopics::contains)
                .collect(Collectors.toSet());
            
            if (!toDelete.isEmpty()) {
                deleteTopics(toDelete);
                // Wait for deletion to complete
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        
        // Create topics in batches
        List<NewTopic> newTopics = topics.stream()
            .map(this::createNewTopic)
            .toList();
        
        createTopics(newTopics);
        log.info("Topic creation completed");
    }
    
    private NewTopic createNewTopic(TopicDefinitions.TopicDef def) {
        Map<String, String> configs = new HashMap<>();
        
        // Configure based on topic type
        switch (def.type()) {
            case CHANGELOG -> {
                configs.put(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_COMPACT);
                configs.put(TopicConfig.MIN_COMPACTION_LAG_MS_CONFIG, "0");
            }
            case LOGS -> {
                configs.put(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(TimeUnit.DAYS.toMillis(7)));
                configs.put(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_DELETE);
            }
            case METRICS -> {
                configs.put(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(TimeUnit.DAYS.toMillis(3)));
            }
            default -> {
                configs.put(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(TimeUnit.DAYS.toMillis(14)));
            }
        }
        
        // Large message support
        if (def.isLargeMessages()) {
            configs.put(TopicConfig.MAX_MESSAGE_BYTES_CONFIG, "1048576"); // 1MB
        }
        
        return new NewTopic(def.name(), def.partitions(), def.replicationFactor())
            .configs(configs);
    }
    
    private void createTopics(List<NewTopic> topics) {
        try {
            CreateTopicsResult result = adminClient.createTopics(topics);
            
            for (Map.Entry<String, org.apache.kafka.common.KafkaFuture<Void>> entry : result.values().entrySet()) {
                try {
                    entry.getValue().get();
                    log.debug("Created topic: {}", entry.getKey());
                } catch (ExecutionException e) {
                    if (e.getCause() instanceof TopicExistsException) {
                        log.debug("Topic already exists: {}", entry.getKey());
                    } else {
                        log.error("Failed to create topic {}: {}", entry.getKey(), e.getMessage());
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while creating topics", e);
        }
    }
    
    private void deleteTopics(Set<String> topicNames) {
        log.info("Deleting {} existing topics for clean start...", topicNames.size());
        try {
            DeleteTopicsResult result = adminClient.deleteTopics(topicNames);
            for (Map.Entry<String, org.apache.kafka.common.KafkaFuture<Void>> entry : result.topicNameValues().entrySet()) {
                try {
                    entry.getValue().get();
                    log.debug("Deleted topic: {}", entry.getKey());
                } catch (ExecutionException e) {
                    if (e.getCause() instanceof UnknownTopicOrPartitionException) {
                        log.debug("Topic not found for deletion: {}", entry.getKey());
                    } else {
                        log.warn("Failed to delete topic {}: {}", entry.getKey(), e.getMessage());
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while deleting topics", e);
        }
    }
    
    private Set<String> getExistingTopics() {
        try {
            return adminClient.listTopics().names().get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to list existing topics", e);
            return Set.of();
        }
    }
    
    public void close() {
        adminClient.close();
    }
}
