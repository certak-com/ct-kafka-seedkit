package com.certak.kafka.seedkit.schemas;

/**
 * Avro schema definitions with multiple versions to simulate schema evolution.
 */
public final class AvroSchemas {
    private AvroSchemas() {}
    
    // ============================================================
    // ORDER SCHEMAS - 4 versions showing evolution
    // ============================================================
    public static final String ORDER_KEY = """
        {
          "type": "record",
          "name": "OrderKey",
          "namespace": "com.certak.kafka.ecommerce",
          "fields": [
            {"name": "orderId", "type": "string"}
          ]
        }
        """;
    
    public static final String ORDER_V1 = """
        {
          "type": "record",
          "name": "Order",
          "namespace": "com.certak.kafka.ecommerce",
          "fields": [
            {"name": "orderId", "type": "string"},
            {"name": "customerId", "type": "string"},
            {"name": "totalAmount", "type": "double"},
            {"name": "status", "type": "string"},
            {"name": "createdAt", "type": "long", "logicalType": "timestamp-millis"}
          ]
        }
        """;
    
    public static final String ORDER_V2 = """
        {
          "type": "record",
          "name": "Order",
          "namespace": "com.certak.kafka.ecommerce",
          "fields": [
            {"name": "orderId", "type": "string"},
            {"name": "customerId", "type": "string"},
            {"name": "totalAmount", "type": "double"},
            {"name": "currency", "type": "string", "default": "USD"},
            {"name": "status", "type": "string"},
            {"name": "createdAt", "type": "long", "logicalType": "timestamp-millis"},
            {"name": "updatedAt", "type": ["null", "long"], "default": null, "logicalType": "timestamp-millis"}
          ]
        }
        """;
    
    public static final String ORDER_V3 = """
        {
          "type": "record",
          "name": "Order",
          "namespace": "com.certak.kafka.ecommerce",
          "fields": [
            {"name": "orderId", "type": "string"},
            {"name": "customerId", "type": "string"},
            {"name": "totalAmount", "type": "double"},
            {"name": "currency", "type": "string", "default": "USD"},
            {"name": "status", "type": {"type": "enum", "name": "OrderStatus", "symbols": ["PENDING", "CONFIRMED", "SHIPPED", "DELIVERED", "CANCELLED"]}},
            {"name": "shippingAddress", "type": ["null", {
              "type": "record",
              "name": "Address",
              "fields": [
                {"name": "street", "type": "string"},
                {"name": "city", "type": "string"},
                {"name": "state", "type": "string"},
                {"name": "zipCode", "type": "string"},
                {"name": "country", "type": "string"}
              ]
            }], "default": null},
            {"name": "createdAt", "type": "long", "logicalType": "timestamp-millis"},
            {"name": "updatedAt", "type": ["null", "long"], "default": null}
          ]
        }
        """;
    
    public static final String ORDER_V4 = """
        {
          "type": "record",
          "name": "Order",
          "namespace": "com.certak.kafka.ecommerce",
          "fields": [
            {"name": "orderId", "type": "string"},
            {"name": "customerId", "type": "string"},
            {"name": "customerEmail", "type": ["null", "string"], "default": null},
            {"name": "totalAmount", "type": "double"},
            {"name": "taxAmount", "type": ["null", "double"], "default": null},
            {"name": "discountAmount", "type": ["null", "double"], "default": null},
            {"name": "currency", "type": "string", "default": "USD"},
            {"name": "status", "type": {"type": "enum", "name": "OrderStatus", "symbols": ["PENDING", "CONFIRMED", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED", "REFUNDED"]}},
            {"name": "shippingAddress", "type": ["null", {
              "type": "record",
              "name": "Address",
              "fields": [
                {"name": "street", "type": "string"},
                {"name": "city", "type": "string"},
                {"name": "state", "type": "string"},
                {"name": "zipCode", "type": "string"},
                {"name": "country", "type": "string"}
              ]
            }], "default": null},
            {"name": "billingAddress", "type": ["null", "Address"], "default": null},
            {"name": "paymentMethod", "type": ["null", "string"], "default": null},
            {"name": "notes", "type": ["null", "string"], "default": null},
            {"name": "createdAt", "type": "long", "logicalType": "timestamp-millis"},
            {"name": "updatedAt", "type": ["null", "long"], "default": null},
            {"name": "metadata", "type": ["null", {"type": "map", "values": "string"}], "default": null}
          ]
        }
        """;
    
    public static final String ORDER_ITEM = """
        {
          "type": "record",
          "name": "OrderItem",
          "namespace": "com.certak.kafka.ecommerce",
          "fields": [
            {"name": "orderItemId", "type": "string"},
            {"name": "orderId", "type": "string"},
            {"name": "productId", "type": "string"},
            {"name": "productName", "type": "string"},
            {"name": "quantity", "type": "int"},
            {"name": "unitPrice", "type": "double"},
            {"name": "totalPrice", "type": "double"},
            {"name": "sku", "type": ["null", "string"], "default": null}
          ]
        }
        """;
    
    // ============================================================
    // PAGE VIEW SCHEMAS - 3 versions
    // ============================================================
    public static final String PAGE_VIEW_V1 = """
        {
          "type": "record",
          "name": "PageView",
          "namespace": "com.certak.kafka.analytics",
          "fields": [
            {"name": "viewId", "type": "string"},
            {"name": "userId", "type": "string"},
            {"name": "pageUrl", "type": "string"},
            {"name": "timestamp", "type": "long"}
          ]
        }
        """;
    
    public static final String PAGE_VIEW_V2 = """
        {
          "type": "record",
          "name": "PageView",
          "namespace": "com.certak.kafka.analytics",
          "fields": [
            {"name": "viewId", "type": "string"},
            {"name": "userId", "type": "string"},
            {"name": "sessionId", "type": ["null", "string"], "default": null},
            {"name": "pageUrl", "type": "string"},
            {"name": "referrer", "type": ["null", "string"], "default": null},
            {"name": "userAgent", "type": ["null", "string"], "default": null},
            {"name": "timestamp", "type": "long"}
          ]
        }
        """;
    
    public static final String PAGE_VIEW_V3 = """
        {
          "type": "record",
          "name": "PageView",
          "namespace": "com.certak.kafka.analytics",
          "fields": [
            {"name": "viewId", "type": "string"},
            {"name": "userId", "type": "string"},
            {"name": "sessionId", "type": ["null", "string"], "default": null},
            {"name": "pageUrl", "type": "string"},
            {"name": "pageTitle", "type": ["null", "string"], "default": null},
            {"name": "referrer", "type": ["null", "string"], "default": null},
            {"name": "userAgent", "type": ["null", "string"], "default": null},
            {"name": "ipAddress", "type": ["null", "string"], "default": null},
            {"name": "country", "type": ["null", "string"], "default": null},
            {"name": "region", "type": ["null", "string"], "default": null},
            {"name": "deviceType", "type": ["null", "string"], "default": null},
            {"name": "browser", "type": ["null", "string"], "default": null},
            {"name": "timestamp", "type": "long"},
            {"name": "durationMs", "type": ["null", "long"], "default": null}
          ]
        }
        """;
    
    public static final String USER_SESSION = """
        {
          "type": "record",
          "name": "UserSession",
          "namespace": "com.certak.kafka.analytics",
          "fields": [
            {"name": "sessionId", "type": "string"},
            {"name": "userId", "type": "string"},
            {"name": "startTime", "type": "long"},
            {"name": "endTime", "type": ["null", "long"], "default": null},
            {"name": "pageViews", "type": "int", "default": 0},
            {"name": "deviceType", "type": ["null", "string"], "default": null},
            {"name": "browser", "type": ["null", "string"], "default": null},
            {"name": "country", "type": ["null", "string"], "default": null}
          ]
        }
        """;
    
    public static final String CART_EVENT = """
        {
          "type": "record",
          "name": "CartEvent",
          "namespace": "com.certak.kafka.ecommerce",
          "fields": [
            {"name": "eventId", "type": "string"},
            {"name": "cartId", "type": "string"},
            {"name": "userId", "type": "string"},
            {"name": "eventType", "type": {"type": "enum", "name": "CartEventType", "symbols": ["ITEM_ADDED", "ITEM_REMOVED", "ITEM_UPDATED", "CART_CLEARED", "CHECKOUT_STARTED"]}},
            {"name": "productId", "type": ["null", "string"], "default": null},
            {"name": "quantity", "type": ["null", "int"], "default": null},
            {"name": "timestamp", "type": "long"}
          ]
        }
        """;
    
    public static final String PRODUCT = """
        {
          "type": "record",
          "name": "Product",
          "namespace": "com.certak.kafka.ecommerce",
          "fields": [
            {"name": "productId", "type": "string"},
            {"name": "name", "type": "string"},
            {"name": "description", "type": ["null", "string"], "default": null},
            {"name": "category", "type": "string"},
            {"name": "subcategory", "type": ["null", "string"], "default": null},
            {"name": "brand", "type": ["null", "string"], "default": null},
            {"name": "price", "type": "double"},
            {"name": "salePrice", "type": ["null", "double"], "default": null},
            {"name": "currency", "type": "string", "default": "USD"},
            {"name": "sku", "type": "string"},
            {"name": "inStock", "type": "boolean"},
            {"name": "stockQuantity", "type": "int"},
            {"name": "imageUrl", "type": ["null", "string"], "default": null},
            {"name": "tags", "type": {"type": "array", "items": "string"}, "default": []},
            {"name": "attributes", "type": ["null", {"type": "map", "values": "string"}], "default": null},
            {"name": "createdAt", "type": "long"},
            {"name": "updatedAt", "type": "long"}
          ]
        }
        """;
    
    public static final String RECOMMENDATION = """
        {
          "type": "record",
          "name": "Recommendation",
          "namespace": "com.certak.kafka.ml",
          "fields": [
            {"name": "recommendationId", "type": "string"},
            {"name": "userId", "type": "string"},
            {"name": "productIds", "type": {"type": "array", "items": "string"}},
            {"name": "scores", "type": {"type": "array", "items": "double"}},
            {"name": "modelVersion", "type": "string"},
            {"name": "algorithm", "type": "string"},
            {"name": "timestamp", "type": "long"}
          ]
        }
        """;
    
    // ============================================================
    // PAYMENT SCHEMAS
    // ============================================================
    public static final String TRANSACTION_V1 = """
        {
          "type": "record",
          "name": "Transaction",
          "namespace": "com.certak.kafka.payments",
          "fields": [
            {"name": "transactionId", "type": "string"},
            {"name": "orderId", "type": "string"},
            {"name": "amount", "type": "double"},
            {"name": "status", "type": "string"},
            {"name": "timestamp", "type": "long"}
          ]
        }
        """;
    
    public static final String TRANSACTION_V2 = """
        {
          "type": "record",
          "name": "Transaction",
          "namespace": "com.certak.kafka.payments",
          "fields": [
            {"name": "transactionId", "type": "string"},
            {"name": "orderId", "type": "string"},
            {"name": "customerId", "type": ["null", "string"], "default": null},
            {"name": "amount", "type": "double"},
            {"name": "currency", "type": "string", "default": "USD"},
            {"name": "paymentMethod", "type": "string"},
            {"name": "status", "type": {"type": "enum", "name": "TransactionStatus", "symbols": ["PENDING", "AUTHORIZED", "CAPTURED", "DECLINED", "REFUNDED"]}},
            {"name": "timestamp", "type": "long"}
          ]
        }
        """;
    
    public static final String TRANSACTION_V3 = """
        {
          "type": "record",
          "name": "Transaction",
          "namespace": "com.certak.kafka.payments",
          "fields": [
            {"name": "transactionId", "type": "string"},
            {"name": "orderId", "type": "string"},
            {"name": "customerId", "type": ["null", "string"], "default": null},
            {"name": "amount", "type": "double"},
            {"name": "fee", "type": ["null", "double"], "default": null},
            {"name": "netAmount", "type": ["null", "double"], "default": null},
            {"name": "currency", "type": "string", "default": "USD"},
            {"name": "paymentMethod", "type": "string"},
            {"name": "cardLast4", "type": ["null", "string"], "default": null},
            {"name": "cardBrand", "type": ["null", "string"], "default": null},
            {"name": "status", "type": {"type": "enum", "name": "TransactionStatus", "symbols": ["PENDING", "AUTHORIZED", "CAPTURED", "DECLINED", "REFUNDED", "CHARGEBACK"]}},
            {"name": "gatewayResponse", "type": ["null", "string"], "default": null},
            {"name": "riskScore", "type": ["null", "double"], "default": null},
            {"name": "timestamp", "type": "long"},
            {"name": "processedAt", "type": ["null", "long"], "default": null}
          ]
        }
        """;
    
    public static final String CARD_EVENT = """
        {
          "type": "record",
          "name": "CardEvent",
          "namespace": "com.certak.kafka.payments",
          "fields": [
            {"name": "eventId", "type": "string"},
            {"name": "transactionId", "type": "string"},
            {"name": "eventType", "type": {"type": "enum", "name": "CardEventType", "symbols": ["AUTHORIZATION", "CAPTURE", "VOID", "REFUND"]}},
            {"name": "amount", "type": "double"},
            {"name": "cardLast4", "type": "string"},
            {"name": "cardBrand", "type": "string"},
            {"name": "responseCode", "type": "string"},
            {"name": "timestamp", "type": "long"}
          ]
        }
        """;
    
    public static final String FRAUD_ALERT = """
        {
          "type": "record",
          "name": "FraudAlert",
          "namespace": "com.certak.kafka.payments",
          "fields": [
            {"name": "alertId", "type": "string"},
            {"name": "transactionId", "type": "string"},
            {"name": "customerId", "type": "string"},
            {"name": "riskScore", "type": "double"},
            {"name": "riskFactors", "type": {"type": "array", "items": "string"}},
            {"name": "alertLevel", "type": {"type": "enum", "name": "AlertLevel", "symbols": ["LOW", "MEDIUM", "HIGH", "CRITICAL"]}},
            {"name": "recommendedAction", "type": "string"},
            {"name": "timestamp", "type": "long"}
          ]
        }
        """;
    
    public static final String SETTLEMENT = """
        {
          "type": "record",
          "name": "Settlement",
          "namespace": "com.certak.kafka.payments",
          "fields": [
            {"name": "settlementId", "type": "string"},
            {"name": "batchId", "type": "string"},
            {"name": "transactionIds", "type": {"type": "array", "items": "string"}},
            {"name": "totalAmount", "type": "double"},
            {"name": "feeAmount", "type": "double"},
            {"name": "netAmount", "type": "double"},
            {"name": "currency", "type": "string"},
            {"name": "status", "type": "string"},
            {"name": "settledAt", "type": "long"}
          ]
        }
        """;
    
    // ============================================================
    // INVENTORY SCHEMAS
    // ============================================================
    public static final String STOCK_UPDATE_V1 = """
        {
          "type": "record",
          "name": "StockUpdate",
          "namespace": "com.certak.kafka.inventory",
          "fields": [
            {"name": "productId", "type": "string"},
            {"name": "warehouseId", "type": "string"},
            {"name": "quantity", "type": "int"},
            {"name": "timestamp", "type": "long"}
          ]
        }
        """;
    
    public static final String STOCK_UPDATE_V2 = """
        {
          "type": "record",
          "name": "StockUpdate",
          "namespace": "com.certak.kafka.inventory",
          "fields": [
            {"name": "updateId", "type": "string"},
            {"name": "productId", "type": "string"},
            {"name": "sku", "type": ["null", "string"], "default": null},
            {"name": "warehouseId", "type": "string"},
            {"name": "previousQuantity", "type": "int"},
            {"name": "newQuantity", "type": "int"},
            {"name": "delta", "type": "int"},
            {"name": "reason", "type": {"type": "enum", "name": "StockChangeReason", "symbols": ["SALE", "RETURN", "ADJUSTMENT", "RECEIVING", "TRANSFER", "DAMAGE"]}},
            {"name": "referenceId", "type": ["null", "string"], "default": null},
            {"name": "timestamp", "type": "long"}
          ]
        }
        """;
    
    public static final String WAREHOUSE_EVENT = """
        {
          "type": "record",
          "name": "WarehouseEvent",
          "namespace": "com.certak.kafka.inventory",
          "fields": [
            {"name": "eventId", "type": "string"},
            {"name": "warehouseId", "type": "string"},
            {"name": "eventType", "type": {"type": "enum", "name": "WarehouseEventType", "symbols": ["RECEIVING", "PICKING", "PACKING", "SHIPPING", "CYCLE_COUNT"]}},
            {"name": "productId", "type": ["null", "string"], "default": null},
            {"name": "quantity", "type": ["null", "int"], "default": null},
            {"name": "location", "type": ["null", "string"], "default": null},
            {"name": "workerId", "type": ["null", "string"], "default": null},
            {"name": "timestamp", "type": "long"}
          ]
        }
        """;
    
    public static final String TRANSFER = """
        {
          "type": "record",
          "name": "Transfer",
          "namespace": "com.certak.kafka.inventory",
          "fields": [
            {"name": "transferId", "type": "string"},
            {"name": "productId", "type": "string"},
            {"name": "quantity", "type": "int"},
            {"name": "sourceWarehouseId", "type": "string"},
            {"name": "destinationWarehouseId", "type": "string"},
            {"name": "status", "type": {"type": "enum", "name": "TransferStatus", "symbols": ["REQUESTED", "IN_TRANSIT", "COMPLETED", "CANCELLED"]}},
            {"name": "requestedAt", "type": "long"},
            {"name": "completedAt", "type": ["null", "long"], "default": null}
          ]
        }
        """;
    
    // ============================================================
    // CUSTOMER SCHEMAS
    // ============================================================
    public static final String CUSTOMER_PROFILE_V1 = """
        {
          "type": "record",
          "name": "CustomerProfile",
          "namespace": "com.certak.kafka.customer",
          "fields": [
            {"name": "customerId", "type": "string"},
            {"name": "email", "type": "string"},
            {"name": "firstName", "type": "string"},
            {"name": "lastName", "type": "string"},
            {"name": "createdAt", "type": "long"}
          ]
        }
        """;
    
    public static final String CUSTOMER_PROFILE_V2 = """
        {
          "type": "record",
          "name": "CustomerProfile",
          "namespace": "com.certak.kafka.customer",
          "fields": [
            {"name": "customerId", "type": "string"},
            {"name": "email", "type": "string"},
            {"name": "firstName", "type": "string"},
            {"name": "lastName", "type": "string"},
            {"name": "phone", "type": ["null", "string"], "default": null},
            {"name": "dateOfBirth", "type": ["null", "string"], "default": null},
            {"name": "gender", "type": ["null", "string"], "default": null},
            {"name": "createdAt", "type": "long"},
            {"name": "updatedAt", "type": ["null", "long"], "default": null}
          ]
        }
        """;
    
    public static final String CUSTOMER_PROFILE_V3 = """
        {
          "type": "record",
          "name": "CustomerProfile",
          "namespace": "com.certak.kafka.customer",
          "fields": [
            {"name": "customerId", "type": "string"},
            {"name": "email", "type": "string"},
            {"name": "emailVerified", "type": "boolean", "default": false},
            {"name": "firstName", "type": "string"},
            {"name": "lastName", "type": "string"},
            {"name": "phone", "type": ["null", "string"], "default": null},
            {"name": "phoneVerified", "type": "boolean", "default": false},
            {"name": "dateOfBirth", "type": ["null", "string"], "default": null},
            {"name": "gender", "type": ["null", "string"], "default": null},
            {"name": "addresses", "type": {"type": "array", "items": {
              "type": "record",
              "name": "CustomerAddress",
              "fields": [
                {"name": "addressId", "type": "string"},
                {"name": "type", "type": "string"},
                {"name": "street", "type": "string"},
                {"name": "city", "type": "string"},
                {"name": "state", "type": "string"},
                {"name": "zipCode", "type": "string"},
                {"name": "country", "type": "string"},
                {"name": "isDefault", "type": "boolean"}
              ]
            }}, "default": []},
            {"name": "loyaltyTier", "type": ["null", "string"], "default": null},
            {"name": "loyaltyPoints", "type": "int", "default": 0},
            {"name": "totalOrders", "type": "int", "default": 0},
            {"name": "totalSpent", "type": "double", "default": 0.0},
            {"name": "createdAt", "type": "long"},
            {"name": "updatedAt", "type": ["null", "long"], "default": null},
            {"name": "lastLoginAt", "type": ["null", "long"], "default": null}
          ]
        }
        """;
    
    public static final String CUSTOMER_PREFERENCES = """
        {
          "type": "record",
          "name": "CustomerPreferences",
          "namespace": "com.certak.kafka.customer",
          "fields": [
            {"name": "customerId", "type": "string"},
            {"name": "emailMarketing", "type": "boolean", "default": true},
            {"name": "smsMarketing", "type": "boolean", "default": false},
            {"name": "pushNotifications", "type": "boolean", "default": true},
            {"name": "language", "type": "string", "default": "en"},
            {"name": "currency", "type": "string", "default": "USD"},
            {"name": "timezone", "type": "string", "default": "America/New_York"},
            {"name": "favoriteCategories", "type": {"type": "array", "items": "string"}, "default": []},
            {"name": "updatedAt", "type": "long"}
          ]
        }
        """;
    
    public static final String LOYALTY_EVENT = """
        {
          "type": "record",
          "name": "LoyaltyEvent",
          "namespace": "com.certak.kafka.customer",
          "fields": [
            {"name": "eventId", "type": "string"},
            {"name": "customerId", "type": "string"},
            {"name": "eventType", "type": {"type": "enum", "name": "LoyaltyEventType", "symbols": ["POINTS_EARNED", "POINTS_REDEEMED", "TIER_UPGRADE", "TIER_DOWNGRADE", "BONUS_AWARDED"]}},
            {"name": "points", "type": "int"},
            {"name": "balanceBefore", "type": "int"},
            {"name": "balanceAfter", "type": "int"},
            {"name": "referenceId", "type": ["null", "string"], "default": null},
            {"name": "description", "type": ["null", "string"], "default": null},
            {"name": "timestamp", "type": "long"}
          ]
        }
        """;
    
    // ============================================================
    // NOTIFICATION SCHEMAS
    // ============================================================
    public static final String EMAIL_NOTIFICATION = """
        {
          "type": "record",
          "name": "EmailNotification",
          "namespace": "com.certak.kafka.notifications",
          "fields": [
            {"name": "notificationId", "type": "string"},
            {"name": "recipientEmail", "type": "string"},
            {"name": "recipientName", "type": ["null", "string"], "default": null},
            {"name": "subject", "type": "string"},
            {"name": "templateId", "type": "string"},
            {"name": "templateData", "type": {"type": "map", "values": "string"}},
            {"name": "priority", "type": {"type": "enum", "name": "Priority", "symbols": ["LOW", "NORMAL", "HIGH"]}},
            {"name": "scheduledAt", "type": ["null", "long"], "default": null},
            {"name": "createdAt", "type": "long"}
          ]
        }
        """;
    
    public static final String EMAIL_EVENT = """
        {
          "type": "record",
          "name": "EmailEvent",
          "namespace": "com.certak.kafka.notifications",
          "fields": [
            {"name": "eventId", "type": "string"},
            {"name": "notificationId", "type": "string"},
            {"name": "eventType", "type": {"type": "enum", "name": "EmailEventType", "symbols": ["SENT", "DELIVERED", "OPENED", "CLICKED", "BOUNCED", "COMPLAINED", "UNSUBSCRIBED"]}},
            {"name": "metadata", "type": ["null", {"type": "map", "values": "string"}], "default": null},
            {"name": "timestamp", "type": "long"}
          ]
        }
        """;
    
    public static final String PUSH_NOTIFICATION = """
        {
          "type": "record",
          "name": "PushNotification",
          "namespace": "com.certak.kafka.notifications",
          "fields": [
            {"name": "notificationId", "type": "string"},
            {"name": "userId", "type": "string"},
            {"name": "deviceTokens", "type": {"type": "array", "items": "string"}},
            {"name": "title", "type": "string"},
            {"name": "body", "type": "string"},
            {"name": "imageUrl", "type": ["null", "string"], "default": null},
            {"name": "deepLink", "type": ["null", "string"], "default": null},
            {"name": "data", "type": ["null", {"type": "map", "values": "string"}], "default": null},
            {"name": "createdAt", "type": "long"}
          ]
        }
        """;
    
    public static final String SMS_NOTIFICATION = """
        {
          "type": "record",
          "name": "SmsNotification",
          "namespace": "com.certak.kafka.notifications",
          "fields": [
            {"name": "notificationId", "type": "string"},
            {"name": "recipientPhone", "type": "string"},
            {"name": "message", "type": "string"},
            {"name": "senderId", "type": ["null", "string"], "default": null},
            {"name": "priority", "type": "string", "default": "NORMAL"},
            {"name": "createdAt", "type": "long"}
          ]
        }
        """;
    
    // ============================================================
    // IoT SCHEMAS
    // ============================================================
    public static final String SENSOR_READING_V1 = """
        {
          "type": "record",
          "name": "SensorReading",
          "namespace": "com.certak.kafka.iot",
          "fields": [
            {"name": "deviceId", "type": "string"},
            {"name": "sensorType", "type": "string"},
            {"name": "value", "type": "double"},
            {"name": "timestamp", "type": "long"}
          ]
        }
        """;
    
    public static final String SENSOR_READING_V2 = """
        {
          "type": "record",
          "name": "SensorReading",
          "namespace": "com.certak.kafka.iot",
          "fields": [
            {"name": "readingId", "type": "string"},
            {"name": "deviceId", "type": "string"},
            {"name": "sensorType", "type": {"type": "enum", "name": "SensorType", "symbols": ["TEMPERATURE", "HUMIDITY", "PRESSURE", "LIGHT", "MOTION", "SOUND", "CO2", "VOLTAGE"]}},
            {"name": "value", "type": "double"},
            {"name": "unit", "type": "string"},
            {"name": "timestamp", "type": "long"}
          ]
        }
        """;
    
    public static final String SENSOR_READING_V3 = """
        {
          "type": "record",
          "name": "SensorReading",
          "namespace": "com.certak.kafka.iot",
          "fields": [
            {"name": "readingId", "type": "string"},
            {"name": "deviceId", "type": "string"},
            {"name": "sensorId", "type": ["null", "string"], "default": null},
            {"name": "sensorType", "type": {"type": "enum", "name": "SensorType", "symbols": ["TEMPERATURE", "HUMIDITY", "PRESSURE", "LIGHT", "MOTION", "SOUND", "CO2", "VOLTAGE", "CURRENT", "POWER", "VIBRATION", "GPS"]}},
            {"name": "value", "type": "double"},
            {"name": "unit", "type": "string"},
            {"name": "quality", "type": ["null", "double"], "default": null},
            {"name": "location", "type": ["null", {
              "type": "record",
              "name": "GeoLocation",
              "fields": [
                {"name": "latitude", "type": "double"},
                {"name": "longitude", "type": "double"},
                {"name": "altitude", "type": ["null", "double"], "default": null}
              ]
            }], "default": null},
            {"name": "tags", "type": ["null", {"type": "map", "values": "string"}], "default": null},
            {"name": "timestamp", "type": "long"},
            {"name": "receivedAt", "type": ["null", "long"], "default": null}
          ]
        }
        """;
    
    public static final String DEVICE_STATUS = """
        {
          "type": "record",
          "name": "DeviceStatus",
          "namespace": "com.certak.kafka.iot",
          "fields": [
            {"name": "deviceId", "type": "string"},
            {"name": "status", "type": {"type": "enum", "name": "DeviceStatusEnum", "symbols": ["ONLINE", "OFFLINE", "MAINTENANCE", "ERROR"]}},
            {"name": "batteryLevel", "type": ["null", "int"], "default": null},
            {"name": "signalStrength", "type": ["null", "int"], "default": null},
            {"name": "firmwareVersion", "type": ["null", "string"], "default": null},
            {"name": "lastSeen", "type": "long"},
            {"name": "errorMessage", "type": ["null", "string"], "default": null}
          ]
        }
        """;
    
    public static final String DEVICE_REGISTRY = """
        {
          "type": "record",
          "name": "DeviceRegistry",
          "namespace": "com.certak.kafka.iot",
          "fields": [
            {"name": "deviceId", "type": "string"},
            {"name": "deviceType", "type": "string"},
            {"name": "manufacturer", "type": ["null", "string"], "default": null},
            {"name": "model", "type": ["null", "string"], "default": null},
            {"name": "serialNumber", "type": ["null", "string"], "default": null},
            {"name": "firmwareVersion", "type": "string"},
            {"name": "location", "type": ["null", "string"], "default": null},
            {"name": "tags", "type": {"type": "map", "values": "string"}, "default": {}},
            {"name": "registeredAt", "type": "long"},
            {"name": "updatedAt", "type": "long"}
          ]
        }
        """;
    
    public static final String IOT_ALERT = """
        {
          "type": "record",
          "name": "IoTAlert",
          "namespace": "com.certak.kafka.iot",
          "fields": [
            {"name": "alertId", "type": "string"},
            {"name": "deviceId", "type": "string"},
            {"name": "alertType", "type": {"type": "enum", "name": "IoTAlertType", "symbols": ["THRESHOLD_BREACH", "DEVICE_OFFLINE", "LOW_BATTERY", "ANOMALY_DETECTED", "MAINTENANCE_DUE"]}},
            {"name": "severity", "type": {"type": "enum", "name": "Severity", "symbols": ["INFO", "WARNING", "ERROR", "CRITICAL"]}},
            {"name": "message", "type": "string"},
            {"name": "thresholdValue", "type": ["null", "double"], "default": null},
            {"name": "actualValue", "type": ["null", "double"], "default": null},
            {"name": "acknowledged", "type": "boolean", "default": false},
            {"name": "acknowledgedBy", "type": ["null", "string"], "default": null},
            {"name": "timestamp", "type": "long"}
          ]
        }
        """;
    
    // ============================================================
    // AUDIT/LOGS SCHEMAS
    // ============================================================
    public static final String SECURITY_LOG = """
        {
          "type": "record",
          "name": "SecurityLog",
          "namespace": "com.certak.kafka.audit",
          "fields": [
            {"name": "logId", "type": "string"},
            {"name": "eventType", "type": "string"},
            {"name": "userId", "type": ["null", "string"], "default": null},
            {"name": "ipAddress", "type": ["null", "string"], "default": null},
            {"name": "userAgent", "type": ["null", "string"], "default": null},
            {"name": "resource", "type": ["null", "string"], "default": null},
            {"name": "action", "type": "string"},
            {"name": "result", "type": {"type": "enum", "name": "SecurityResult", "symbols": ["SUCCESS", "FAILURE", "BLOCKED"]}},
            {"name": "details", "type": ["null", {"type": "map", "values": "string"}], "default": null},
            {"name": "timestamp", "type": "long"}
          ]
        }
        """;
    
    public static final String AUDIT_EVENT = """
        {
          "type": "record",
          "name": "AuditEvent",
          "namespace": "com.certak.kafka.audit",
          "fields": [
            {"name": "eventId", "type": "string"},
            {"name": "eventType", "type": "string"},
            {"name": "entityType", "type": "string"},
            {"name": "entityId", "type": "string"},
            {"name": "action", "type": {"type": "enum", "name": "AuditAction", "symbols": ["CREATE", "READ", "UPDATE", "DELETE", "LOGIN", "LOGOUT", "EXPORT"]}},
            {"name": "actorId", "type": "string"},
            {"name": "actorType", "type": "string"},
            {"name": "previousState", "type": ["null", "string"], "default": null},
            {"name": "newState", "type": ["null", "string"], "default": null},
            {"name": "metadata", "type": ["null", {"type": "map", "values": "string"}], "default": null},
            {"name": "timestamp", "type": "long"}
          ]
        }
        """;
    
    public static final String USER_ACTION = """
        {
          "type": "record",
          "name": "UserAction",
          "namespace": "com.certak.kafka.audit",
          "fields": [
            {"name": "actionId", "type": "string"},
            {"name": "userId", "type": "string"},
            {"name": "sessionId", "type": ["null", "string"], "default": null},
            {"name": "actionType", "type": "string"},
            {"name": "targetType", "type": ["null", "string"], "default": null},
            {"name": "targetId", "type": ["null", "string"], "default": null},
            {"name": "details", "type": ["null", "string"], "default": null},
            {"name": "ipAddress", "type": ["null", "string"], "default": null},
            {"name": "timestamp", "type": "long"}
          ]
        }
        """;
    
    public static final String DATA_ACCESS_LOG = """
        {
          "type": "record",
          "name": "DataAccessLog",
          "namespace": "com.certak.kafka.audit",
          "fields": [
            {"name": "logId", "type": "string"},
            {"name": "userId", "type": "string"},
            {"name": "dataType", "type": "string"},
            {"name": "dataId", "type": ["null", "string"], "default": null},
            {"name": "accessType", "type": {"type": "enum", "name": "AccessType", "symbols": ["VIEW", "EXPORT", "DOWNLOAD", "PRINT", "API_ACCESS"]}},
            {"name": "purpose", "type": ["null", "string"], "default": null},
            {"name": "recordCount", "type": ["null", "int"], "default": null},
            {"name": "timestamp", "type": "long"}
          ]
        }
        """;
    
    public static final String CONFIG_CHANGE = """
        {
          "type": "record",
          "name": "ConfigChange",
          "namespace": "com.certak.kafka.audit",
          "fields": [
            {"name": "changeId", "type": "string"},
            {"name": "configType", "type": "string"},
            {"name": "configKey", "type": "string"},
            {"name": "previousValue", "type": ["null", "string"], "default": null},
            {"name": "newValue", "type": "string"},
            {"name": "changedBy", "type": "string"},
            {"name": "reason", "type": ["null", "string"], "default": null},
            {"name": "timestamp", "type": "long"}
          ]
        }
        """;
    
    // ============================================================
    // ML SCHEMAS
    // ============================================================
    public static final String FEATURE_UPDATE = """
        {
          "type": "record",
          "name": "FeatureUpdate",
          "namespace": "com.certak.kafka.ml",
          "fields": [
            {"name": "featureId", "type": "string"},
            {"name": "entityType", "type": "string"},
            {"name": "entityId", "type": "string"},
            {"name": "featureName", "type": "string"},
            {"name": "featureValue", "type": "double"},
            {"name": "featureVersion", "type": "string"},
            {"name": "timestamp", "type": "long"}
          ]
        }
        """;
    
    public static final String MODEL_PREDICTION = """
        {
          "type": "record",
          "name": "ModelPrediction",
          "namespace": "com.certak.kafka.ml",
          "fields": [
            {"name": "predictionId", "type": "string"},
            {"name": "modelId", "type": "string"},
            {"name": "modelVersion", "type": "string"},
            {"name": "inputFeatures", "type": {"type": "map", "values": "double"}},
            {"name": "prediction", "type": "double"},
            {"name": "confidence", "type": ["null", "double"], "default": null},
            {"name": "explanations", "type": ["null", {"type": "map", "values": "double"}], "default": null},
            {"name": "latencyMs", "type": "long"},
            {"name": "timestamp", "type": "long"}
          ]
        }
        """;
    
    public static final String TRAINING_DATA = """
        {
          "type": "record",
          "name": "TrainingData",
          "namespace": "com.certak.kafka.ml",
          "fields": [
            {"name": "batchId", "type": "string"},
            {"name": "datasetId", "type": "string"},
            {"name": "features", "type": {"type": "array", "items": {"type": "map", "values": "double"}}},
            {"name": "labels", "type": {"type": "array", "items": "double"}},
            {"name": "metadata", "type": ["null", {"type": "map", "values": "string"}], "default": null},
            {"name": "timestamp", "type": "long"}
          ]
        }
        """;
    
    // ============================================================
    // SHIPPING SCHEMAS
    // ============================================================
    public static final String TRACKING_UPDATE = """
        {
          "type": "record",
          "name": "TrackingUpdate",
          "namespace": "com.certak.kafka.shipping",
          "fields": [
            {"name": "updateId", "type": "string"},
            {"name": "trackingNumber", "type": "string"},
            {"name": "orderId", "type": "string"},
            {"name": "carrier", "type": "string"},
            {"name": "status", "type": {"type": "enum", "name": "ShippingStatus", "symbols": ["LABEL_CREATED", "PICKED_UP", "IN_TRANSIT", "OUT_FOR_DELIVERY", "DELIVERED", "FAILED_ATTEMPT", "RETURNED"]}},
            {"name": "location", "type": ["null", "string"], "default": null},
            {"name": "estimatedDelivery", "type": ["null", "long"], "default": null},
            {"name": "timestamp", "type": "long"}
          ]
        }
        """;
    
    public static final String LABEL_REQUEST = """
        {
          "type": "record",
          "name": "LabelRequest",
          "namespace": "com.certak.kafka.shipping",
          "fields": [
            {"name": "requestId", "type": "string"},
            {"name": "orderId", "type": "string"},
            {"name": "carrier", "type": "string"},
            {"name": "serviceType", "type": "string"},
            {"name": "shipFrom", "type": {
              "type": "record",
              "name": "ShipAddress",
              "fields": [
                {"name": "name", "type": "string"},
                {"name": "street", "type": "string"},
                {"name": "city", "type": "string"},
                {"name": "state", "type": "string"},
                {"name": "zipCode", "type": "string"},
                {"name": "country", "type": "string"}
              ]
            }},
            {"name": "shipTo", "type": "ShipAddress"},
            {"name": "weight", "type": "double"},
            {"name": "dimensions", "type": ["null", {
              "type": "record",
              "name": "Dimensions",
              "fields": [
                {"name": "length", "type": "double"},
                {"name": "width", "type": "double"},
                {"name": "height", "type": "double"},
                {"name": "unit", "type": "string"}
              ]
            }], "default": null},
            {"name": "timestamp", "type": "long"}
          ]
        }
        """;
    
    // ============================================================
    // METRICS SCHEMAS
    // ============================================================
    public static final String APP_METRIC = """
        {
          "type": "record",
          "name": "ApplicationMetric",
          "namespace": "com.certak.kafka.metrics",
          "fields": [
            {"name": "metricId", "type": "string"},
            {"name": "serviceName", "type": "string"},
            {"name": "instanceId", "type": "string"},
            {"name": "metricName", "type": "string"},
            {"name": "metricType", "type": {"type": "enum", "name": "MetricType", "symbols": ["COUNTER", "GAUGE", "HISTOGRAM", "TIMER"]}},
            {"name": "value", "type": "double"},
            {"name": "tags", "type": {"type": "map", "values": "string"}, "default": {}},
            {"name": "timestamp", "type": "long"}
          ]
        }
        """;
    
    public static final String INFRA_METRIC = """
        {
          "type": "record",
          "name": "InfrastructureMetric",
          "namespace": "com.certak.kafka.metrics",
          "fields": [
            {"name": "metricId", "type": "string"},
            {"name": "hostName", "type": "string"},
            {"name": "metricCategory", "type": {"type": "enum", "name": "MetricCategory", "symbols": ["CPU", "MEMORY", "DISK", "NETWORK", "PROCESS"]}},
            {"name": "metricName", "type": "string"},
            {"name": "value", "type": "double"},
            {"name": "unit", "type": "string"},
            {"name": "tags", "type": {"type": "map", "values": "string"}, "default": {}},
            {"name": "timestamp", "type": "long"}
          ]
        }
        """;
    
    public static final String BUSINESS_KPI = """
        {
          "type": "record",
          "name": "BusinessKPI",
          "namespace": "com.certak.kafka.metrics",
          "fields": [
            {"name": "kpiId", "type": "string"},
            {"name": "kpiName", "type": "string"},
            {"name": "category", "type": "string"},
            {"name": "value", "type": "double"},
            {"name": "previousValue", "type": ["null", "double"], "default": null},
            {"name": "target", "type": ["null", "double"], "default": null},
            {"name": "unit", "type": "string"},
            {"name": "periodStart", "type": "long"},
            {"name": "periodEnd", "type": "long"},
            {"name": "dimensions", "type": {"type": "map", "values": "string"}, "default": {}},
            {"name": "timestamp", "type": "long"}
          ]
        }
        """;
    
    // ============================================================
    // TRADING SCHEMAS
    // ============================================================
    public static final String MARKET_DATA_V1 = """
        {
          "type": "record",
          "name": "MarketData",
          "namespace": "com.certak.kafka.trading",
          "fields": [
            {"name": "symbol", "type": "string"},
            {"name": "price", "type": "double"},
            {"name": "volume", "type": "long"},
            {"name": "timestamp", "type": "long"}
          ]
        }
        """;
    
    public static final String MARKET_DATA_V2 = """
        {
          "type": "record",
          "name": "MarketData",
          "namespace": "com.certak.kafka.trading",
          "fields": [
            {"name": "symbol", "type": "string"},
            {"name": "exchange", "type": "string"},
            {"name": "price", "type": "double"},
            {"name": "bid", "type": ["null", "double"], "default": null},
            {"name": "ask", "type": ["null", "double"], "default": null},
            {"name": "bidSize", "type": ["null", "long"], "default": null},
            {"name": "askSize", "type": ["null", "long"], "default": null},
            {"name": "volume", "type": "long"},
            {"name": "vwap", "type": ["null", "double"], "default": null},
            {"name": "open", "type": ["null", "double"], "default": null},
            {"name": "high", "type": ["null", "double"], "default": null},
            {"name": "low", "type": ["null", "double"], "default": null},
            {"name": "previousClose", "type": ["null", "double"], "default": null},
            {"name": "timestamp", "type": "long"}
          ]
        }
        """;
    
    public static final String POSITION = """
        {
          "type": "record",
          "name": "Position",
          "namespace": "com.certak.kafka.trading",
          "fields": [
            {"name": "positionId", "type": "string"},
            {"name": "accountId", "type": "string"},
            {"name": "symbol", "type": "string"},
            {"name": "quantity", "type": "long"},
            {"name": "averageCost", "type": "double"},
            {"name": "marketValue", "type": "double"},
            {"name": "unrealizedPnL", "type": "double"},
            {"name": "realizedPnL", "type": "double"},
            {"name": "timestamp", "type": "long"}
          ]
        }
        """;
    
    public static final String RISK_ALERT = """
        {
          "type": "record",
          "name": "RiskAlert",
          "namespace": "com.certak.kafka.trading",
          "fields": [
            {"name": "alertId", "type": "string"},
            {"name": "accountId", "type": "string"},
            {"name": "alertType", "type": {"type": "enum", "name": "RiskAlertType", "symbols": ["MARGIN_CALL", "POSITION_LIMIT", "LOSS_LIMIT", "CONCENTRATION", "VOLATILITY"]}},
            {"name": "severity", "type": "string"},
            {"name": "message", "type": "string"},
            {"name": "thresholdValue", "type": "double"},
            {"name": "currentValue", "type": "double"},
            {"name": "timestamp", "type": "long"}
          ]
        }
        """;
    
    // ============================================================
    // HEALTHCARE SCHEMAS
    // ============================================================
    public static final String APPOINTMENT = """
        {
          "type": "record",
          "name": "Appointment",
          "namespace": "com.certak.kafka.healthcare",
          "fields": [
            {"name": "appointmentId", "type": "string"},
            {"name": "patientId", "type": "string"},
            {"name": "providerId", "type": "string"},
            {"name": "facilityId", "type": "string"},
            {"name": "appointmentType", "type": "string"},
            {"name": "status", "type": {"type": "enum", "name": "AppointmentStatus", "symbols": ["SCHEDULED", "CONFIRMED", "CHECKED_IN", "IN_PROGRESS", "COMPLETED", "CANCELLED", "NO_SHOW"]}},
            {"name": "scheduledStart", "type": "long"},
            {"name": "scheduledEnd", "type": "long"},
            {"name": "notes", "type": ["null", "string"], "default": null},
            {"name": "createdAt", "type": "long"},
            {"name": "updatedAt", "type": "long"}
          ]
        }
        """;
    
    public static final String LAB_RESULT = """
        {
          "type": "record",
          "name": "LabResult",
          "namespace": "com.certak.kafka.healthcare",
          "fields": [
            {"name": "resultId", "type": "string"},
            {"name": "orderId", "type": "string"},
            {"name": "patientId", "type": "string"},
            {"name": "testCode", "type": "string"},
            {"name": "testName", "type": "string"},
            {"name": "value", "type": "string"},
            {"name": "unit", "type": ["null", "string"], "default": null},
            {"name": "referenceRange", "type": ["null", "string"], "default": null},
            {"name": "status", "type": {"type": "enum", "name": "ResultStatus", "symbols": ["PENDING", "PRELIMINARY", "FINAL", "CORRECTED", "CANCELLED"]}},
            {"name": "abnormalFlag", "type": ["null", "string"], "default": null},
            {"name": "collectedAt", "type": "long"},
            {"name": "reportedAt", "type": "long"}
          ]
        }
        """;
    
    // ============================================================
    // CONNECT/DATAGEN SCHEMAS
    // ============================================================
    public static final String DATAGEN_USER = """
        {
          "type": "record",
          "name": "DatagenUser",
          "namespace": "com.certak.kafka.connect",
          "fields": [
            {"name": "userId", "type": "string"},
            {"name": "username", "type": "string"},
            {"name": "email", "type": "string"},
            {"name": "firstName", "type": "string"},
            {"name": "lastName", "type": "string"},
            {"name": "gender", "type": "string"},
            {"name": "regionId", "type": "string"},
            {"name": "registeredAt", "type": "long"}
          ]
        }
        """;
    
    public static final String DATAGEN_PAGEVIEW = """
        {
          "type": "record",
          "name": "DatagenPageview",
          "namespace": "com.certak.kafka.connect",
          "fields": [
            {"name": "viewId", "type": "string"},
            {"name": "userId", "type": "string"},
            {"name": "pagePath", "type": "string"},
            {"name": "viewTime", "type": "long"}
          ]
        }
        """;
    
    // ============================================================
    // KSQL DERIVED SCHEMAS
    // ============================================================
    public static final String ORDER_TOTAL = """
        {
          "type": "record",
          "name": "OrderTotal",
          "namespace": "com.certak.kafka.ksql",
          "fields": [
            {"name": "customerId", "type": "string"},
            {"name": "orderCount", "type": "long"},
            {"name": "totalAmount", "type": "double"},
            {"name": "avgOrderValue", "type": "double"},
            {"name": "windowStart", "type": "long"},
            {"name": "windowEnd", "type": "long"}
          ]
        }
        """;
    
    public static final String PAGEVIEW_REGION = """
        {
          "type": "record",
          "name": "PageviewRegion",
          "namespace": "com.certak.kafka.ksql",
          "fields": [
            {"name": "region", "type": "string"},
            {"name": "viewCount", "type": "long"},
            {"name": "uniqueUsers", "type": "long"},
            {"name": "windowStart", "type": "long"},
            {"name": "windowEnd", "type": "long"}
          ]
        }
        """;
    
    public static final String ENRICHED_ORDER = """
        {
          "type": "record",
          "name": "EnrichedOrder",
          "namespace": "com.certak.kafka.ksql",
          "fields": [
            {"name": "orderId", "type": "string"},
            {"name": "customerId", "type": "string"},
            {"name": "customerEmail", "type": ["null", "string"], "default": null},
            {"name": "customerName", "type": ["null", "string"], "default": null},
            {"name": "loyaltyTier", "type": ["null", "string"], "default": null},
            {"name": "totalAmount", "type": "double"},
            {"name": "status", "type": "string"},
            {"name": "createdAt", "type": "long"}
          ]
        }
        """;
    
    public static final String FRAUD_CANDIDATE = """
        {
          "type": "record",
          "name": "FraudCandidate",
          "namespace": "com.certak.kafka.ksql",
          "fields": [
            {"name": "transactionId", "type": "string"},
            {"name": "customerId", "type": "string"},
            {"name": "amount", "type": "double"},
            {"name": "transactionCount", "type": "long"},
            {"name": "totalAmount", "type": "double"},
            {"name": "riskScore", "type": "double"},
            {"name": "windowStart", "type": "long"},
            {"name": "windowEnd", "type": "long"}
          ]
        }
        """;
}
