package com.certak.kafka.seedkit.ksql;

import com.certak.kafka.seedkit.config.SeedKitConfig;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Manages KSQL streams and tables for demo purposes.
 */
public class KsqlManager {
    private static final Logger log = LoggerFactory.getLogger(KsqlManager.class);
    
    private final SeedKitConfig config;
    private final CloseableHttpClient httpClient;
    
    public KsqlManager(SeedKitConfig config) {
        this.config = config;
        this.httpClient = createTrustingHttpClient();
    }
    
    private CloseableHttpClient createTrustingHttpClient() {
        try {
            SSLContext sslContext = SSLContextBuilder.create()
                .loadTrustMaterial(null, TrustAllStrategy.INSTANCE)
                .build();
            
            HttpClientConnectionManager cm = PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(SSLConnectionSocketFactoryBuilder.create()
                    .setSslContext(sslContext)
                    .setHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .build())
                .build();
            
            return HttpClients.custom()
                .setConnectionManager(cm)
                .build();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            log.warn("Failed to create trusting HTTP client, falling back to default: {}", e.getMessage());
            return HttpClients.createDefault();
        }
    }
    
    /**
     * Create all KSQL streams and tables.
     */
    public void setupKsql() {
        log.info("Setting up KSQL streams and tables...");
        
        // Drop existing objects first (ignore errors if they don't exist)
        dropKsqlObjects();
        
        // Create streams
        createStreams();
        
        // Create tables
        createTables();
        
        // Create derived streams (joins, aggregations)
        createDerivedStreams();
        
        log.info("KSQL setup completed");
        
        // Print sample queries
        printSampleQueries();
    }
    
    private void dropKsqlObjects() {
        // Drop in reverse order of dependencies
        List<String> objectsToDrop = List.of(
            "DROP TABLE IF EXISTS CUSTOMER_ORDER_TOTALS DELETE TOPIC;",
            "DROP TABLE IF EXISTS PAGEVIEWS_BY_REGION DELETE TOPIC;",
            "DROP TABLE IF EXISTS FRAUD_CANDIDATES DELETE TOPIC;",
            "DROP STREAM IF EXISTS ENRICHED_ORDERS DELETE TOPIC;",
            "DROP STREAM IF EXISTS HIGH_VALUE_ORDERS;",
            "DROP STREAM IF EXISTS SUSPICIOUS_TRANSACTIONS;",
            "DROP TABLE IF EXISTS CUSTOMERS_TABLE;",
            "DROP STREAM IF EXISTS ORDERS_STREAM;",
            "DROP STREAM IF EXISTS PAGEVIEWS_STREAM;",
            "DROP STREAM IF EXISTS TRANSACTIONS_STREAM;",
            "DROP STREAM IF EXISTS SENSOR_READINGS_STREAM;",
            "DROP STREAM IF EXISTS APP_LOGS_STREAM;",
            "DROP STREAM IF EXISTS MARKET_DATA_STREAM;"
        );
        
        for (String sql : objectsToDrop) {
            executeKsql(sql, true);
        }
        
        // Wait for drops to complete
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private void createStreams() {
        // Orders stream
        executeKsql("""
            CREATE STREAM ORDERS_STREAM (
                orderId VARCHAR KEY,
                customerId VARCHAR,
                customerEmail VARCHAR,
                totalAmount DOUBLE,
                taxAmount DOUBLE,
                discountAmount DOUBLE,
                currency VARCHAR,
                status VARCHAR,
                paymentMethod VARCHAR,
                notes VARCHAR,
                createdAt BIGINT,
                updatedAt BIGINT
            ) WITH (
                KAFKA_TOPIC = 'ecommerce.orders',
                VALUE_FORMAT = 'AVRO'
            );
            """, false);
        
        // Page views stream
        executeKsql("""
            CREATE STREAM PAGEVIEWS_STREAM (
                viewId VARCHAR KEY,
                userId VARCHAR,
                sessionId VARCHAR,
                pageUrl VARCHAR,
                pageTitle VARCHAR,
                referrer VARCHAR,
                userAgent VARCHAR,
                ipAddress VARCHAR,
                country VARCHAR,
                region VARCHAR,
                deviceType VARCHAR,
                browser VARCHAR,
                timestamp BIGINT,
                durationMs BIGINT
            ) WITH (
                KAFKA_TOPIC = 'ecommerce.page-views',
                VALUE_FORMAT = 'AVRO'
            );
            """, false);
        
        // Transactions stream
        executeKsql("""
            CREATE STREAM TRANSACTIONS_STREAM (
                transactionId VARCHAR KEY,
                orderId VARCHAR,
                customerId VARCHAR,
                amount DOUBLE,
                fee DOUBLE,
                netAmount DOUBLE,
                currency VARCHAR,
                paymentMethod VARCHAR,
                cardLast4 VARCHAR,
                cardBrand VARCHAR,
                status VARCHAR,
                gatewayResponse VARCHAR,
                riskScore DOUBLE,
                timestamp BIGINT,
                processedAt BIGINT
            ) WITH (
                KAFKA_TOPIC = 'payments.transactions',
                VALUE_FORMAT = 'AVRO'
            );
            """, false);
        
        // Sensor readings stream
        executeKsql("""
            CREATE STREAM SENSOR_READINGS_STREAM (
                readingId VARCHAR KEY,
                deviceId VARCHAR,
                sensorId VARCHAR,
                sensorType VARCHAR,
                value DOUBLE,
                unit VARCHAR,
                quality DOUBLE,
                timestamp BIGINT,
                receivedAt BIGINT
            ) WITH (
                KAFKA_TOPIC = 'iot.sensor-readings',
                VALUE_FORMAT = 'AVRO'
            );
            """, false);
        
        // Application logs stream (JSON format)
        executeKsql("""
            CREATE STREAM APP_LOGS_STREAM (
                timestamp VARCHAR,
                level VARCHAR,
                service VARCHAR,
                traceId VARCHAR,
                spanId VARCHAR,
                message VARCHAR,
                host VARCHAR,
                environment VARCHAR
            ) WITH (
                KAFKA_TOPIC = 'logs.application',
                VALUE_FORMAT = 'JSON'
            );
            """, false);
        
        // Market data stream
        executeKsql("""
            CREATE STREAM MARKET_DATA_STREAM (
                symbol VARCHAR KEY,
                exchange VARCHAR,
                price DOUBLE,
                bid DOUBLE,
                ask DOUBLE,
                bidSize BIGINT,
                askSize BIGINT,
                volume BIGINT,
                vwap DOUBLE,
                open DOUBLE,
                high DOUBLE,
                low DOUBLE,
                previousClose DOUBLE,
                timestamp BIGINT
            ) WITH (
                KAFKA_TOPIC = 'trading.market-data',
                VALUE_FORMAT = 'AVRO'
            );
            """, false);
    }
    
    private void createTables() {
        // Customers table (compacted topic simulation)
        executeKsql("""
            CREATE TABLE CUSTOMERS_TABLE (
                customerId VARCHAR PRIMARY KEY,
                email VARCHAR,
                firstName VARCHAR,
                lastName VARCHAR,
                phone VARCHAR,
                loyaltyTier VARCHAR,
                loyaltyPoints INTEGER,
                totalOrders INTEGER,
                totalSpent DOUBLE,
                createdAt BIGINT,
                updatedAt BIGINT
            ) WITH (
                KAFKA_TOPIC = 'customers.profiles',
                VALUE_FORMAT = 'AVRO'
            );
            """, false);
    }
    
    private void createDerivedStreams() {
        // High-value orders stream
        executeKsql("""
            CREATE STREAM HIGH_VALUE_ORDERS AS
            SELECT 
                orderId,
                customerId,
                customerEmail,
                totalAmount,
                currency,
                status,
                createdAt
            FROM ORDERS_STREAM
            WHERE totalAmount > 500
            EMIT CHANGES;
            """, false);
        
        // Suspicious transactions (high risk score)
        executeKsql("""
            CREATE STREAM SUSPICIOUS_TRANSACTIONS AS
            SELECT 
                transactionId,
                orderId,
                customerId,
                amount,
                riskScore,
                cardBrand,
                timestamp
            FROM TRANSACTIONS_STREAM
            WHERE riskScore > 70
            EMIT CHANGES;
            """, false);
        
        // Customer order totals (windowed aggregation)
        executeKsql("""
            CREATE TABLE CUSTOMER_ORDER_TOTALS AS
            SELECT 
                customerId,
                COUNT(*) AS orderCount,
                SUM(totalAmount) AS totalSpent,
                AVG(totalAmount) AS avgOrderValue
            FROM ORDERS_STREAM
            WINDOW TUMBLING (SIZE 1 HOUR)
            GROUP BY customerId
            EMIT CHANGES;
            """, false);
        
        // Pageviews by region (windowed aggregation)
        executeKsql("""
            CREATE TABLE PAGEVIEWS_BY_REGION AS
            SELECT 
                region,
                COUNT(*) AS viewCount,
                COUNT_DISTINCT(userId) AS uniqueUsers
            FROM PAGEVIEWS_STREAM
            WINDOW TUMBLING (SIZE 5 MINUTES)
            WHERE region IS NOT NULL
            GROUP BY region
            EMIT CHANGES;
            """, false);
        
        // Fraud candidates (multiple transactions in short window)
        executeKsql("""
            CREATE TABLE FRAUD_CANDIDATES AS
            SELECT 
                customerId,
                COUNT(*) AS transactionCount,
                SUM(amount) AS totalSpent,
                MAX(riskScore) AS maxRiskScore
            FROM TRANSACTIONS_STREAM
            WINDOW TUMBLING (SIZE 10 MINUTES)
            GROUP BY customerId
            HAVING COUNT(*) > 5 OR SUM(amount) > 5000
            EMIT CHANGES;
            """, false);
    }
    
    private void executeKsql(String sql, boolean ignoreErrors) {
        try {
            String url = config.getKsqlUrl() + "/ksql";
            HttpPost request = new HttpPost(url);
            
            String body = String.format("{\"ksql\": \"%s\", \"streamsProperties\": {}}", 
                sql.replace("\"", "\\\"").replace("\n", " ").replace("\r", ""));
            
            request.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
            request.setHeader("Content-Type", "application/vnd.ksql.v1+json");
            request.setHeader("Accept", "application/vnd.ksql.v1+json");
            
            httpClient.execute(request, response -> {
                int status = response.getCode();
                String responseBody = EntityUtils.toString(response.getEntity());
                
                if (status == 200) {
                    log.debug("KSQL executed successfully: {}", sql.substring(0, Math.min(50, sql.length())));
                } else if (!ignoreErrors) {
                    log.warn("KSQL execution failed ({}): {} - Response: {}", status, 
                        sql.substring(0, Math.min(50, sql.length())), responseBody);
                }
                return null;
            });
        } catch (IOException e) {
            if (!ignoreErrors) {
                log.error("Error executing KSQL: {}", e.getMessage());
            }
        }
    }
    
    /**
     * Print sample queries that can be used for demos.
     */
    public void printSampleQueries() {
        log.info("""
            
            ====================================================================
            SAMPLE KSQL QUERIES FOR DEMO
            ====================================================================
            
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
            
            -- Potential fraud candidates (multiple transactions)
            SELECT * FROM FRAUD_CANDIDATES EMIT CHANGES;
            
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
            
            -- Join orders with customers (if customers populated)
            SELECT 
                o.orderId,
                o.totalAmount,
                o.status,
                c.email,
                c.firstName,
                c.lastName,
                c.loyaltyTier
            FROM ORDERS_STREAM o
            LEFT JOIN CUSTOMERS_TABLE c ON o.customerId = c.customerId
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
            
            ====================================================================
            """);
    }
    
    public void close() {
        try {
            httpClient.close();
        } catch (IOException e) {
            log.debug("Error closing HTTP client: {}", e.getMessage());
        }
    }
}
