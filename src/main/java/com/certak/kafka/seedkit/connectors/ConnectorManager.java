package com.certak.kafka.seedkit.connectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.certak.kafka.seedkit.config.SeedKitConfig;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Manages Kafka Connect connectors across multiple Connect clusters.
 */
public class ConnectorManager {
    private static final Logger log = LoggerFactory.getLogger(ConnectorManager.class);
    
    private final SeedKitConfig config;
    private final ObjectMapper mapper = new ObjectMapper();
    private final CloseableHttpClient httpClient;
    
    public ConnectorManager(SeedKitConfig config) {
        this.config = config;
        this.httpClient = HttpClients.createDefault();
    }
    
    /**
     * Deploy all connectors to both Connect clusters.
     */
    public void deployConnectors() {
        log.info("Deploying Kafka Connect connectors...");
        
        String primaryConnect = config.getPrimaryConnectUrl();
        String secondaryConnect = config.getSecondaryConnectUrl();
        
        // Delete existing connectors first
        deleteConnector(primaryConnect, "datagen-users");
        deleteConnector(primaryConnect, "datagen-pageviews");
        deleteConnector(primaryConnect, "file-source-logs");
        deleteConnector(primaryConnect, "file-sink-orders");
        deleteConnector(primaryConnect, "jdbc-source-failing");
        
        deleteConnector(secondaryConnect, "datagen-stock-trades");
        deleteConnector(secondaryConnect, "datagen-clickstream");
        deleteConnector(secondaryConnect, "file-source-events");
        
        // Wait for deletion to complete
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Deploy connectors to primary cluster
        deployDatagenUsersConnector(primaryConnect);
        deployDatagenPageviewsConnector(primaryConnect);
        deployFileSourceConnector(primaryConnect);
        deployFileSinkConnector(primaryConnect);
        deployFailingJdbcConnector(primaryConnect); // This will fail - intentional
        
        // Deploy connectors to secondary cluster
        deployDatagenStockTradesConnector(secondaryConnect);
        deployDatagenClickstreamConnector(secondaryConnect);
        deployFileSourceEventsConnector(secondaryConnect);
        
        log.info("Connector deployment completed");
    }
    
    // ============================================================
    // PRIMARY CLUSTER CONNECTORS
    // ============================================================
    
    private void deployDatagenUsersConnector(String connectUrl) {
        ObjectNode config = mapper.createObjectNode();
        config.put("connector.class", "io.confluent.kafka.connect.datagen.DatagenConnector");
        config.put("tasks.max", "1");
        config.put("kafka.topic", "connect.datagen-users");
        config.put("quickstart", "users");
        config.put("key.converter", "org.apache.kafka.connect.storage.StringConverter");
        config.put("value.converter", "io.confluent.connect.avro.AvroConverter");
        config.put("value.converter.schema.registry.url", this.config.getSchemaRegistryUrl());
        config.put("max.interval", "3000");
        config.put("iterations", "-1");
        
        createConnector(connectUrl, "datagen-users", config);
    }
    
    private void deployDatagenPageviewsConnector(String connectUrl) {
        ObjectNode config = mapper.createObjectNode();
        config.put("connector.class", "io.confluent.kafka.connect.datagen.DatagenConnector");
        config.put("tasks.max", "1");
        config.put("kafka.topic", "connect.datagen-pageviews");
        config.put("quickstart", "pageviews");
        config.put("key.converter", "org.apache.kafka.connect.storage.StringConverter");
        config.put("value.converter", "io.confluent.connect.avro.AvroConverter");
        config.put("value.converter.schema.registry.url", this.config.getSchemaRegistryUrl());
        config.put("max.interval", "2000");
        config.put("iterations", "-1");
        
        createConnector(connectUrl, "datagen-pageviews", config);
    }
    
    private void deployFileSourceConnector(String connectUrl) {
        ObjectNode config = mapper.createObjectNode();
        config.put("connector.class", "org.apache.kafka.connect.file.FileStreamSourceConnector");
        config.put("tasks.max", "1");
        config.put("file", "/tmp/seedkit-input.txt");
        config.put("topic", "connect.file-input");
        config.put("key.converter", "org.apache.kafka.connect.storage.StringConverter");
        config.put("value.converter", "org.apache.kafka.connect.storage.StringConverter");
        
        createConnector(connectUrl, "file-source-logs", config);
    }
    
    private void deployFileSinkConnector(String connectUrl) {
        ObjectNode config = mapper.createObjectNode();
        config.put("connector.class", "org.apache.kafka.connect.file.FileStreamSinkConnector");
        config.put("tasks.max", "1");
        config.put("file", "/tmp/seedkit-output.txt");
        config.put("topics", "connect.file-output");
        config.put("key.converter", "org.apache.kafka.connect.storage.StringConverter");
        config.put("value.converter", "org.apache.kafka.connect.storage.StringConverter");
        
        createConnector(connectUrl, "file-sink-orders", config);
    }
    
    private void deployFailingJdbcConnector(String connectUrl) {
        // This connector is intentionally configured to fail (no database)
        ObjectNode config = mapper.createObjectNode();
        config.put("connector.class", "io.confluent.connect.jdbc.JdbcSourceConnector");
        config.put("tasks.max", "1");
        config.put("connection.url", "jdbc:postgresql://nonexistent-db:5432/testdb");
        config.put("connection.user", "test_user");
        config.put("connection.password", "test_password");
        config.put("topic.prefix", "jdbc-");
        config.put("mode", "incrementing");
        config.put("incrementing.column.name", "id");
        config.put("table.whitelist", "users,orders");
        config.put("poll.interval.ms", "10000");
        
        log.info("Deploying intentionally failing JDBC connector...");
        createConnector(connectUrl, "jdbc-source-failing", config);
    }
    
    // ============================================================
    // SECONDARY CLUSTER CONNECTORS
    // ============================================================
    
    private void deployDatagenStockTradesConnector(String connectUrl) {
        ObjectNode config = mapper.createObjectNode();
        config.put("connector.class", "io.confluent.kafka.connect.datagen.DatagenConnector");
        config.put("tasks.max", "1");
        config.put("kafka.topic", "trading.market-data");
        config.put("quickstart", "stock_trades");
        config.put("key.converter", "org.apache.kafka.connect.storage.StringConverter");
        config.put("value.converter", "io.confluent.connect.avro.AvroConverter");
        config.put("value.converter.schema.registry.url", this.config.getSchemaRegistryUrl());
        config.put("max.interval", "1000");
        config.put("iterations", "-1");
        
        createConnector(connectUrl, "datagen-stock-trades", config);
    }
    
    private void deployDatagenClickstreamConnector(String connectUrl) {
        ObjectNode config = mapper.createObjectNode();
        config.put("connector.class", "io.confluent.kafka.connect.datagen.DatagenConnector");
        config.put("tasks.max", "1");
        config.put("kafka.topic", "ecommerce.page-views");
        config.put("quickstart", "clickstream");
        config.put("key.converter", "org.apache.kafka.connect.storage.StringConverter");
        config.put("value.converter", "io.confluent.connect.avro.AvroConverter");
        config.put("value.converter.schema.registry.url", this.config.getSchemaRegistryUrl());
        config.put("max.interval", "2500");
        config.put("iterations", "-1");
        
        createConnector(connectUrl, "datagen-clickstream", config);
    }
    
    private void deployFileSourceEventsConnector(String connectUrl) {
        ObjectNode config = mapper.createObjectNode();
        config.put("connector.class", "org.apache.kafka.connect.file.FileStreamSourceConnector");
        config.put("tasks.max", "1");
        config.put("file", "/tmp/seedkit-events.txt");
        config.put("topic", "integration.webhook-inbound");
        config.put("key.converter", "org.apache.kafka.connect.storage.StringConverter");
        config.put("value.converter", "org.apache.kafka.connect.storage.StringConverter");
        
        createConnector(connectUrl, "file-source-events", config);
    }
    
    // ============================================================
    // HTTP HELPERS
    // ============================================================
    
    private void createConnector(String connectUrl, String name, ObjectNode config) {
        try {
            ObjectNode connector = mapper.createObjectNode();
            connector.put("name", name);
            connector.set("config", config);
            
            String url = connectUrl + "/connectors";
            HttpPost request = new HttpPost(url);
            request.setEntity(new StringEntity(mapper.writeValueAsString(connector), ContentType.APPLICATION_JSON));
            request.setHeader("Content-Type", "application/json");
            
            httpClient.execute(request, response -> {
                int status = response.getCode();
                String body = EntityUtils.toString(response.getEntity());
                
                if (status == 201 || status == 200) {
                    log.info("Created connector '{}' on {}", name, connectUrl);
                } else if (status == 409) {
                    log.info("Connector '{}' already exists on {}", name, connectUrl);
                } else {
                    log.warn("Failed to create connector '{}': {} - {}", name, status, body);
                }
                return null;
            });
        } catch (IOException e) {
            log.error("Error creating connector '{}': {}", name, e.getMessage());
        }
    }
    
    private void deleteConnector(String connectUrl, String name) {
        try {
            String url = connectUrl + "/connectors/" + name;
            HttpDelete request = new HttpDelete(url);
            
            httpClient.execute(request, response -> {
                int status = response.getCode();
                if (status == 204 || status == 200) {
                    log.debug("Deleted connector '{}'", name);
                } else if (status != 404) {
                    log.debug("Connector '{}' delete returned status {}", name, status);
                }
                return null;
            });
        } catch (IOException e) {
            log.debug("Error deleting connector '{}': {}", name, e.getMessage());
        }
    }
    
    /**
     * Get status of all connectors.
     */
    public void printConnectorStatus() {
        log.info("=== Connector Status ===");
        printClusterStatus(config.getPrimaryConnectUrl(), "Primary");
        printClusterStatus(config.getSecondaryConnectUrl(), "Secondary");
    }
    
    private void printClusterStatus(String connectUrl, String clusterName) {
        try {
            String url = connectUrl + "/connectors?expand=status";
            HttpGet request = new HttpGet(url);
            
            httpClient.execute(request, response -> {
                int status = response.getCode();
                if (status == 200) {
                    String body = EntityUtils.toString(response.getEntity());
                    log.info("{} cluster connectors: {}", clusterName, body);
                } else {
                    log.warn("Failed to get {} cluster status: {}", clusterName, status);
                }
                return null;
            });
        } catch (IOException e) {
            log.error("Error getting {} cluster status: {}", clusterName, e.getMessage());
        }
    }
    
    public void close() {
        try {
            httpClient.close();
        } catch (IOException e) {
            log.debug("Error closing HTTP client: {}", e.getMessage());
        }
    }
}
