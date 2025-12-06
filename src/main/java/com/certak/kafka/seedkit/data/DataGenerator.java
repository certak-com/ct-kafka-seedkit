package com.certak.kafka.seedkit.data;

import net.datafaker.Faker;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.time.Instant;
import java.util.*;

/**
 * Generates realistic fake data for various Kafka topics.
 */
public class DataGenerator {
    private final Faker faker = new Faker();
    private final ObjectMapper mapper = new ObjectMapper();
    private final Random random = new Random();
    
    // Pools for consistent reference data
    private final List<String> customerIds = new ArrayList<>();
    private final List<String> productIds = new ArrayList<>();
    private final List<String> orderIds = new ArrayList<>();
    private final List<String> deviceIds = new ArrayList<>();
    private final List<String> warehouseIds = List.of("WH-EAST-001", "WH-WEST-001", "WH-CENTRAL-001", "WH-SOUTH-001", "WH-NORTH-001");
    private final List<String> symbols = List.of("AAPL", "GOOGL", "MSFT", "AMZN", "META", "TSLA", "NVDA", "JPM", "V", "JNJ", "WMT", "PG", "UNH", "HD", "MA");
    private final List<String> categories = List.of("Electronics", "Clothing", "Home & Garden", "Sports", "Books", "Toys", "Beauty", "Automotive", "Food", "Health");
    private final List<String> carriers = List.of("UPS", "FEDEX", "USPS", "DHL", "ONTRAC");
    
    public DataGenerator() {
        // Pre-populate reference data pools
        for (int i = 0; i < 1000; i++) {
            customerIds.add("CUST-" + UUID.randomUUID().toString().substring(0, 8));
            productIds.add("PROD-" + UUID.randomUUID().toString().substring(0, 8));
            deviceIds.add("DEV-" + faker.number().digits(12));
        }
    }
    
    // ============================================================
    // E-COMMERCE DATA
    // ============================================================
    
    public Map<String, Object> generateOrder() {
        String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 12);
        orderIds.add(orderId);
        if (orderIds.size() > 10000) orderIds.remove(0);
        
        Map<String, Object> order = new LinkedHashMap<>();
        order.put("orderId", orderId);
        order.put("customerId", randomFrom(customerIds));
        order.put("customerEmail", faker.internet().emailAddress());
        order.put("totalAmount", roundTo2(faker.number().randomDouble(2, 10, 5000)));
        order.put("taxAmount", roundTo2(faker.number().randomDouble(2, 1, 500)));
        order.put("discountAmount", random.nextDouble() < 0.3 ? roundTo2(faker.number().randomDouble(2, 5, 100)) : null);
        order.put("currency", random.nextDouble() < 0.9 ? "USD" : randomFrom(List.of("EUR", "GBP", "CAD", "AUD")));
        order.put("status", randomFrom(List.of("PENDING", "CONFIRMED", "PROCESSING", "SHIPPED", "DELIVERED")));
        order.put("shippingAddress", generateAddress());
        order.put("billingAddress", random.nextDouble() < 0.7 ? order.get("shippingAddress") : generateAddress());
        order.put("paymentMethod", randomFrom(List.of("CREDIT_CARD", "DEBIT_CARD", "PAYPAL", "APPLE_PAY", "GOOGLE_PAY")));
        order.put("notes", random.nextDouble() < 0.2 ? faker.lorem().sentence() : null);
        order.put("createdAt", Instant.now().minusSeconds(random.nextInt(86400 * 30)).toEpochMilli());
        order.put("updatedAt", Instant.now().toEpochMilli());
        order.put("metadata", Map.of("source", randomFrom(List.of("web", "mobile_ios", "mobile_android", "api")), "campaign", faker.marketing().buzzwords()));
        
        return order;
    }
    
    public Map<String, Object> generatePageView() {
        Map<String, Object> pageView = new LinkedHashMap<>();
        pageView.put("viewId", UUID.randomUUID().toString());
        pageView.put("userId", randomFrom(customerIds));
        pageView.put("sessionId", "sess-" + UUID.randomUUID().toString().substring(0, 8));
        pageView.put("pageUrl", generatePageUrl());
        pageView.put("pageTitle", faker.lorem().sentence(3));
        pageView.put("referrer", random.nextDouble() < 0.6 ? generateReferrer() : null);
        pageView.put("userAgent", faker.internet().userAgent());
        pageView.put("ipAddress", faker.internet().ipV4Address());
        pageView.put("country", faker.address().countryCode());
        pageView.put("region", faker.address().state());
        pageView.put("deviceType", randomFrom(List.of("desktop", "mobile", "tablet")));
        pageView.put("browser", randomFrom(List.of("Chrome", "Firefox", "Safari", "Edge")));
        pageView.put("timestamp", Instant.now().toEpochMilli());
        pageView.put("durationMs", random.nextInt(300000));
        
        return pageView;
    }
    
    public Map<String, Object> generateCartEvent() {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("eventId", UUID.randomUUID().toString());
        event.put("cartId", "CART-" + UUID.randomUUID().toString().substring(0, 8));
        event.put("userId", randomFrom(customerIds));
        event.put("eventType", randomFrom(List.of("ITEM_ADDED", "ITEM_REMOVED", "ITEM_UPDATED", "CART_CLEARED", "CHECKOUT_STARTED")));
        event.put("productId", randomFrom(productIds));
        event.put("quantity", random.nextInt(5) + 1);
        event.put("timestamp", Instant.now().toEpochMilli());
        
        return event;
    }
    
    public Map<String, Object> generateProduct() {
        String productId = randomFrom(productIds);
        String category = randomFrom(categories);
        
        Map<String, Object> product = new LinkedHashMap<>();
        product.put("productId", productId);
        product.put("name", faker.commerce().productName());
        product.put("description", faker.lorem().paragraph(3));
        product.put("category", category);
        product.put("subcategory", faker.commerce().department());
        product.put("brand", faker.company().name());
        product.put("price", roundTo2(faker.number().randomDouble(2, 5, 2000)));
        product.put("salePrice", random.nextDouble() < 0.3 ? roundTo2((double) product.get("price") * 0.8) : null);
        product.put("currency", "USD");
        product.put("sku", "SKU-" + faker.number().digits(10));
        product.put("inStock", random.nextDouble() < 0.85);
        product.put("stockQuantity", random.nextInt(1000));
        product.put("imageUrl", "https://images.example.com/products/" + productId + ".jpg");
        product.put("tags", List.of(faker.commerce().material(), faker.color().name()));
        product.put("attributes", Map.of("color", faker.color().name(), "size", randomFrom(List.of("S", "M", "L", "XL"))));
        product.put("createdAt", Instant.now().minusSeconds(random.nextInt(86400 * 365)).toEpochMilli());
        product.put("updatedAt", Instant.now().toEpochMilli());
        
        return product;
    }
    
    // ============================================================
    // PAYMENT DATA
    // ============================================================
    
    public Map<String, Object> generateTransaction() {
        Map<String, Object> txn = new LinkedHashMap<>();
        txn.put("transactionId", "TXN-" + UUID.randomUUID().toString().substring(0, 12));
        txn.put("orderId", orderIds.isEmpty() ? "ORD-" + UUID.randomUUID().toString().substring(0, 12) : randomFrom(orderIds));
        txn.put("customerId", randomFrom(customerIds));
        txn.put("amount", roundTo2(faker.number().randomDouble(2, 10, 5000)));
        txn.put("fee", roundTo2((double) txn.get("amount") * 0.029 + 0.30));
        txn.put("netAmount", roundTo2((double) txn.get("amount") - (double) txn.get("fee")));
        txn.put("currency", "USD");
        txn.put("paymentMethod", randomFrom(List.of("CREDIT_CARD", "DEBIT_CARD", "ACH", "WIRE")));
        txn.put("cardLast4", faker.number().digits(4));
        txn.put("cardBrand", randomFrom(List.of("VISA", "MASTERCARD", "AMEX", "DISCOVER")));
        txn.put("status", randomFrom(List.of("PENDING", "AUTHORIZED", "CAPTURED", "DECLINED")));
        txn.put("gatewayResponse", random.nextDouble() < 0.95 ? "APPROVED" : "DECLINED");
        txn.put("riskScore", roundTo2(random.nextDouble() * 100));
        txn.put("timestamp", Instant.now().toEpochMilli());
        txn.put("processedAt", Instant.now().toEpochMilli());
        
        return txn;
    }
    
    // ============================================================
    // IoT DATA
    // ============================================================
    
    public Map<String, Object> generateSensorReading() {
        String sensorType = randomFrom(List.of("TEMPERATURE", "HUMIDITY", "PRESSURE", "LIGHT", "MOTION", "CO2", "VOLTAGE"));
        
        Map<String, Object> reading = new LinkedHashMap<>();
        reading.put("readingId", UUID.randomUUID().toString());
        reading.put("deviceId", randomFrom(deviceIds));
        reading.put("sensorId", "SENSOR-" + faker.number().digits(6));
        reading.put("sensorType", sensorType);
        reading.put("value", generateSensorValue(sensorType));
        reading.put("unit", getSensorUnit(sensorType));
        reading.put("quality", roundTo2(0.9 + random.nextDouble() * 0.1));
        reading.put("location", Map.of(
            "latitude", faker.address().latitude(),
            "longitude", faker.address().longitude()
        ));
        reading.put("tags", Map.of("facility", "Building-" + random.nextInt(10), "floor", String.valueOf(random.nextInt(20))));
        reading.put("timestamp", Instant.now().toEpochMilli());
        reading.put("receivedAt", Instant.now().plusMillis(random.nextInt(100)).toEpochMilli());
        
        return reading;
    }
    
    public Map<String, Object> generateDeviceStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("deviceId", randomFrom(deviceIds));
        status.put("status", randomFrom(List.of("ONLINE", "ONLINE", "ONLINE", "OFFLINE", "MAINTENANCE", "ERROR")));
        status.put("batteryLevel", random.nextInt(101));
        status.put("signalStrength", -30 - random.nextInt(70));
        status.put("firmwareVersion", faker.app().version());
        status.put("lastSeen", Instant.now().toEpochMilli());
        status.put("errorMessage", status.get("status").equals("ERROR") ? faker.lorem().sentence() : null);
        
        return status;
    }
    
    // ============================================================
    // LOG DATA
    // ============================================================
    
    public String generateApplicationLog() {
        ObjectNode log = mapper.createObjectNode();
        log.put("timestamp", Instant.now().toString());
        log.put("level", randomFrom(List.of("DEBUG", "INFO", "INFO", "INFO", "WARN", "ERROR")));
        log.put("service", randomFrom(List.of("order-service", "payment-service", "inventory-service", "user-service", "notification-service", "api-gateway")));
        log.put("traceId", UUID.randomUUID().toString());
        log.put("spanId", UUID.randomUUID().toString().substring(0, 16));
        log.put("message", generateLogMessage());
        log.put("host", faker.internet().domainWord() + "-" + random.nextInt(10));
        log.put("environment", randomFrom(List.of("production", "staging")));
        
        return log.toString();
    }
    
    public String generateAccessLog() {
        String method = randomFrom(List.of("GET", "GET", "GET", "POST", "PUT", "DELETE"));
        int status = randomFrom(List.of(200, 200, 200, 201, 204, 301, 400, 401, 403, 404, 500));
        
        return String.format("%s - - [%s] \"%s %s HTTP/1.1\" %d %d \"%s\" \"%s\"",
            faker.internet().ipV4Address(),
            Instant.now().toString(),
            method,
            generateApiPath(),
            status,
            random.nextInt(50000),
            random.nextDouble() < 0.7 ? "https://example.com" + generatePageUrl() : "-",
            faker.internet().userAgent()
        );
    }
    
    // ============================================================
    // TRADING DATA
    // ============================================================
    
    public Map<String, Object> generateMarketData() {
        String symbol = randomFrom(symbols);
        double basePrice = getBasePrice(symbol);
        double change = (random.nextDouble() - 0.5) * 0.02 * basePrice;
        double price = roundTo2(basePrice + change);
        
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("symbol", symbol);
        data.put("exchange", randomFrom(List.of("NYSE", "NASDAQ", "BATS")));
        data.put("price", price);
        data.put("bid", roundTo2(price - 0.01));
        data.put("ask", roundTo2(price + 0.01));
        data.put("bidSize", random.nextInt(1000) * 100L);
        data.put("askSize", random.nextInt(1000) * 100L);
        data.put("volume", random.nextInt(10000000));
        data.put("vwap", roundTo2(price * (0.99 + random.nextDouble() * 0.02)));
        data.put("open", roundTo2(basePrice));
        data.put("high", roundTo2(Math.max(price, basePrice) * 1.01));
        data.put("low", roundTo2(Math.min(price, basePrice) * 0.99));
        data.put("previousClose", roundTo2(basePrice));
        data.put("timestamp", Instant.now().toEpochMilli());
        
        return data;
    }
    
    // ============================================================
    // NOTIFICATION DATA
    // ============================================================
    
    public Map<String, Object> generateEmailNotification() {
        Map<String, Object> email = new LinkedHashMap<>();
        email.put("notificationId", UUID.randomUUID().toString());
        email.put("recipientEmail", faker.internet().emailAddress());
        email.put("recipientName", faker.name().fullName());
        email.put("subject", generateEmailSubject());
        email.put("templateId", randomFrom(List.of("order_confirmation", "shipping_update", "password_reset", "welcome", "promotional", "abandoned_cart")));
        email.put("templateData", Map.of(
            "firstName", faker.name().firstName(),
            "orderId", orderIds.isEmpty() ? "ORD-123" : randomFrom(orderIds),
            "amount", String.valueOf(roundTo2(faker.number().randomDouble(2, 10, 500)))
        ));
        email.put("priority", randomFrom(List.of("LOW", "NORMAL", "HIGH")));
        email.put("scheduledAt", random.nextDouble() < 0.2 ? Instant.now().plusSeconds(3600).toEpochMilli() : null);
        email.put("createdAt", Instant.now().toEpochMilli());
        
        return email;
    }
    
    // ============================================================
    // METRICS DATA
    // ============================================================
    
    public Map<String, Object> generateAppMetric() {
        String metricName = randomFrom(List.of("request_count", "request_latency_ms", "error_count", "active_connections", "queue_depth", "memory_used_mb", "cpu_percent"));
        
        Map<String, Object> metric = new LinkedHashMap<>();
        metric.put("metricId", UUID.randomUUID().toString());
        metric.put("serviceName", randomFrom(List.of("order-service", "payment-service", "inventory-service", "user-service", "api-gateway")));
        metric.put("instanceId", "i-" + faker.number().digits(8));
        metric.put("metricName", metricName);
        metric.put("metricType", metricName.contains("count") ? "COUNTER" : "GAUGE");
        metric.put("value", generateMetricValue(metricName));
        metric.put("tags", Map.of("env", "production", "region", randomFrom(List.of("us-east-1", "us-west-2", "eu-west-1"))));
        metric.put("timestamp", Instant.now().toEpochMilli());
        
        return metric;
    }
    
    // ============================================================
    // XML DATA
    // ============================================================
    
    public String generateErpSyncXml() {
        String orderId = orderIds.isEmpty() ? "ORD-123" : randomFrom(orderIds);
        return String.format("""
            <?xml version="1.0" encoding="UTF-8"?>
            <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
              <soap:Header>
                <TransactionId>%s</TransactionId>
                <Timestamp>%s</Timestamp>
              </soap:Header>
              <soap:Body>
                <SyncOrderRequest xmlns="http://erp.example.com/orders">
                  <Order>
                    <OrderId>%s</OrderId>
                    <CustomerId>%s</CustomerId>
                    <OrderDate>%s</OrderDate>
                    <TotalAmount currency="USD">%.2f</TotalAmount>
                    <Status>%s</Status>
                    <ShippingAddress>
                      <Street>%s</Street>
                      <City>%s</City>
                      <State>%s</State>
                      <ZipCode>%s</ZipCode>
                      <Country>US</Country>
                    </ShippingAddress>
                    <Items>
                      <Item>
                        <ProductId>%s</ProductId>
                        <ProductName>%s</ProductName>
                        <Quantity>%d</Quantity>
                        <UnitPrice>%.2f</UnitPrice>
                      </Item>
                    </Items>
                  </Order>
                </SyncOrderRequest>
              </soap:Body>
            </soap:Envelope>
            """,
            UUID.randomUUID().toString(),
            Instant.now().toString(),
            orderId,
            randomFrom(customerIds),
            Instant.now().toString().substring(0, 10),
            faker.number().randomDouble(2, 50, 2000),
            randomFrom(List.of("NEW", "PROCESSING", "SHIPPED", "COMPLETED")),
            faker.address().streetAddress(),
            faker.address().city(),
            faker.address().stateAbbr(),
            faker.address().zipCode(),
            randomFrom(productIds),
            faker.commerce().productName(),
            random.nextInt(5) + 1,
            faker.number().randomDouble(2, 10, 500)
        );
    }
    
    public String generateCrmXml() {
        return String.format("""
            <?xml version="1.0" encoding="UTF-8"?>
            <CRMEvent xmlns="http://crm.example.com/events">
              <EventId>%s</EventId>
              <EventType>%s</EventType>
              <Timestamp>%s</Timestamp>
              <Customer>
                <CustomerId>%s</CustomerId>
                <Email>%s</Email>
                <FirstName>%s</FirstName>
                <LastName>%s</LastName>
                <Phone>%s</Phone>
                <Company>%s</Company>
                <LeadSource>%s</LeadSource>
                <LeadScore>%d</LeadScore>
              </Customer>
              <Activity>
                <Type>%s</Type>
                <Description>%s</Description>
                <AssignedTo>%s</AssignedTo>
                <DueDate>%s</DueDate>
              </Activity>
            </CRMEvent>
            """,
            UUID.randomUUID().toString(),
            randomFrom(List.of("LEAD_CREATED", "CONTACT_UPDATED", "OPPORTUNITY_WON", "TASK_COMPLETED")),
            Instant.now().toString(),
            randomFrom(customerIds),
            faker.internet().emailAddress(),
            faker.name().firstName(),
            faker.name().lastName(),
            faker.phoneNumber().phoneNumber(),
            faker.company().name(),
            randomFrom(List.of("Website", "Referral", "Trade Show", "Cold Call", "Social Media")),
            random.nextInt(100),
            randomFrom(List.of("Call", "Email", "Meeting", "Demo")),
            faker.lorem().sentence(),
            faker.name().fullName(),
            Instant.now().plusSeconds(86400 * random.nextInt(30)).toString().substring(0, 10)
        );
    }
    
    // ============================================================
    // LARGE MESSAGE DATA
    // ============================================================
    
    public String generateLargeJson(int targetSizeKb) {
        ObjectNode root = mapper.createObjectNode();
        root.put("batchId", UUID.randomUUID().toString());
        root.put("timestamp", Instant.now().toEpochMilli());
        root.put("source", "bulk-import-" + faker.app().name());
        
        ArrayNode records = root.putArray("records");
        int recordCount = (targetSizeKb * 1024) / 500; // Approx 500 bytes per record
        
        for (int i = 0; i < recordCount; i++) {
            ObjectNode record = records.addObject();
            record.put("id", UUID.randomUUID().toString());
            record.put("name", faker.name().fullName());
            record.put("email", faker.internet().emailAddress());
            record.put("company", faker.company().name());
            record.put("department", faker.commerce().department());
            record.put("title", faker.job().title());
            record.put("phone", faker.phoneNumber().phoneNumber());
            record.put("address", faker.address().fullAddress());
            record.put("notes", faker.lorem().paragraph());
        }
        
        ObjectNode metadata = root.putObject("metadata");
        metadata.put("recordCount", recordCount);
        metadata.put("version", "1.0");
        metadata.put("encoding", "UTF-8");
        
        return root.toString();
    }
    
    public String generateLargeXml(int targetSizeKb) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<DataExport xmlns=\"http://example.com/export\">\n");
        sb.append("  <BatchId>").append(UUID.randomUUID()).append("</BatchId>\n");
        sb.append("  <Timestamp>").append(Instant.now()).append("</Timestamp>\n");
        sb.append("  <Records>\n");
        
        int recordCount = (targetSizeKb * 1024) / 600;
        for (int i = 0; i < recordCount; i++) {
            sb.append("    <Record id=\"").append(UUID.randomUUID()).append("\">\n");
            sb.append("      <Name>").append(escapeXml(faker.name().fullName())).append("</Name>\n");
            sb.append("      <Email>").append(faker.internet().emailAddress()).append("</Email>\n");
            sb.append("      <Company>").append(escapeXml(faker.company().name())).append("</Company>\n");
            sb.append("      <Address>").append(escapeXml(faker.address().fullAddress())).append("</Address>\n");
            sb.append("      <Description>").append(escapeXml(faker.lorem().paragraph())).append("</Description>\n");
            sb.append("    </Record>\n");
        }
        
        sb.append("  </Records>\n");
        sb.append("</DataExport>");
        
        return sb.toString();
    }
    
    // ============================================================
    // AUDIT DATA
    // ============================================================
    
    public Map<String, Object> generateAuditEvent() {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("eventId", UUID.randomUUID().toString());
        event.put("eventType", randomFrom(List.of("ORDER_CREATED", "USER_LOGIN", "PAYMENT_PROCESSED", "CONFIG_CHANGED", "DATA_EXPORTED")));
        event.put("entityType", randomFrom(List.of("Order", "User", "Product", "Payment", "Config")));
        event.put("entityId", UUID.randomUUID().toString().substring(0, 12));
        event.put("action", randomFrom(List.of("CREATE", "READ", "UPDATE", "DELETE", "LOGIN", "EXPORT")));
        event.put("actorId", "user-" + faker.number().digits(6));
        event.put("actorType", randomFrom(List.of("USER", "SYSTEM", "API_CLIENT")));
        event.put("previousState", random.nextDouble() < 0.5 ? "{\"status\": \"old\"}" : null);
        event.put("newState", "{\"status\": \"new\"}");
        event.put("metadata", Map.of("ipAddress", faker.internet().ipV4Address(), "userAgent", faker.internet().userAgent()));
        event.put("timestamp", Instant.now().toEpochMilli());
        
        return event;
    }
    
    // ============================================================
    // HELPER METHODS
    // ============================================================
    
    private Map<String, Object> generateAddress() {
        Map<String, Object> address = new LinkedHashMap<>();
        address.put("street", faker.address().streetAddress());
        address.put("city", faker.address().city());
        address.put("state", faker.address().stateAbbr());
        address.put("zipCode", faker.address().zipCode());
        address.put("country", "US");
        return address;
    }
    
    private String generatePageUrl() {
        String type = randomFrom(List.of("product", "category", "search", "cart", "checkout", "account", "home"));
        return switch (type) {
            case "product" -> "/products/" + randomFrom(productIds);
            case "category" -> "/category/" + randomFrom(categories).toLowerCase().replace(" ", "-");
            case "search" -> "/search?q=" + faker.commerce().productName().replace(" ", "+");
            case "cart" -> "/cart";
            case "checkout" -> "/checkout";
            case "account" -> "/account/" + randomFrom(List.of("orders", "profile", "settings"));
            default -> "/";
        };
    }
    
    private String generateReferrer() {
        return randomFrom(List.of(
            "https://www.google.com/search?q=" + faker.commerce().productName().replace(" ", "+"),
            "https://www.facebook.com/",
            "https://twitter.com/",
            "https://www.instagram.com/",
            "https://email.example.com/campaign/123"
        ));
    }
    
    private String generateApiPath() {
        return randomFrom(List.of(
            "/api/v1/orders",
            "/api/v1/orders/" + UUID.randomUUID().toString().substring(0, 8),
            "/api/v1/products",
            "/api/v1/users",
            "/api/v1/cart",
            "/api/v2/search",
            "/health",
            "/metrics"
        ));
    }
    
    private String generateLogMessage() {
        return randomFrom(List.of(
            "Request processed successfully",
            "Order " + (orderIds.isEmpty() ? "ORD-123" : randomFrom(orderIds)) + " status updated",
            "User authentication successful",
            "Cache miss for key: product:" + randomFrom(productIds),
            "Database query executed in " + random.nextInt(100) + "ms",
            "External API call to payment gateway",
            "Rate limit check passed",
            "Connection pool statistics: active=" + random.nextInt(50) + ", idle=" + random.nextInt(20)
        ));
    }
    
    private String generateEmailSubject() {
        return randomFrom(List.of(
            "Your order has been confirmed!",
            "Your package is on its way",
            "Don't forget items in your cart",
            "Special offer just for you",
            "Your password has been reset",
            "Welcome to our store!"
        ));
    }
    
    private double generateSensorValue(String sensorType) {
        return switch (sensorType) {
            case "TEMPERATURE" -> roundTo2(15 + random.nextDouble() * 25);
            case "HUMIDITY" -> roundTo2(30 + random.nextDouble() * 50);
            case "PRESSURE" -> roundTo2(990 + random.nextDouble() * 40);
            case "LIGHT" -> roundTo2(random.nextDouble() * 1000);
            case "MOTION" -> random.nextDouble() < 0.3 ? 1.0 : 0.0;
            case "CO2" -> roundTo2(400 + random.nextDouble() * 600);
            case "VOLTAGE" -> roundTo2(110 + random.nextDouble() * 20);
            default -> roundTo2(random.nextDouble() * 100);
        };
    }
    
    private String getSensorUnit(String sensorType) {
        return switch (sensorType) {
            case "TEMPERATURE" -> "°C";
            case "HUMIDITY" -> "%";
            case "PRESSURE" -> "hPa";
            case "LIGHT" -> "lux";
            case "MOTION" -> "detected";
            case "CO2" -> "ppm";
            case "VOLTAGE" -> "V";
            default -> "unit";
        };
    }
    
    private double generateMetricValue(String metricName) {
        return switch (metricName) {
            case "request_count" -> random.nextInt(10000);
            case "request_latency_ms" -> roundTo2(random.nextDouble() * 500);
            case "error_count" -> random.nextInt(100);
            case "active_connections" -> random.nextInt(500);
            case "queue_depth" -> random.nextInt(1000);
            case "memory_used_mb" -> roundTo2(500 + random.nextDouble() * 1500);
            case "cpu_percent" -> roundTo2(random.nextDouble() * 100);
            default -> roundTo2(random.nextDouble() * 100);
        };
    }
    
    private double getBasePrice(String symbol) {
        return switch (symbol) {
            case "AAPL" -> 175.0;
            case "GOOGL" -> 140.0;
            case "MSFT" -> 380.0;
            case "AMZN" -> 180.0;
            case "META" -> 500.0;
            case "TSLA" -> 250.0;
            case "NVDA" -> 480.0;
            case "JPM" -> 195.0;
            case "V" -> 280.0;
            case "JNJ" -> 155.0;
            case "WMT" -> 165.0;
            case "PG" -> 160.0;
            case "UNH" -> 520.0;
            case "HD" -> 350.0;
            case "MA" -> 450.0;
            default -> 100.0;
        };
    }
    
    private double roundTo2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
    
    private <T> T randomFrom(List<T> list) {
        return list.get(random.nextInt(list.size()));
    }
    
    private String escapeXml(String text) {
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }
    
    public String getRandomCustomerId() {
        return randomFrom(customerIds);
    }
    
    public String getRandomProductId() {
        return randomFrom(productIds);
    }
    
    public String getRandomOrderId() {
        return orderIds.isEmpty() ? "ORD-" + UUID.randomUUID().toString().substring(0, 12) : randomFrom(orderIds);
    }
    
    public String getRandomDeviceId() {
        return randomFrom(deviceIds);
    }
    
    public String getRandomWarehouseId() {
        return randomFrom(warehouseIds);
    }
    
    public String getRandomSymbol() {
        return randomFrom(symbols);
    }
}
