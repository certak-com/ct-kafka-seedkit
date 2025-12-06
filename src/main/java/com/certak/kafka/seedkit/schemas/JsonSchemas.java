package com.certak.kafka.seedkit.schemas;

/**
 * JSON Schema definitions for Kafka topics.
 */
public final class JsonSchemas {
    private JsonSchemas() {}
    
    public static final String PRODUCT_REVIEW = """
        {
          "$schema": "http://json-schema.org/draft-07/schema#",
          "title": "ProductReview",
          "type": "object",
          "required": ["reviewId", "productId", "customerId", "rating", "createdAt"],
          "properties": {
            "reviewId": {
              "type": "string",
              "description": "Unique review identifier"
            },
            "productId": {
              "type": "string",
              "description": "Product being reviewed"
            },
            "customerId": {
              "type": "string",
              "description": "Customer who wrote the review"
            },
            "rating": {
              "type": "integer",
              "minimum": 1,
              "maximum": 5,
              "description": "Star rating 1-5"
            },
            "title": {
              "type": "string",
              "maxLength": 200,
              "description": "Review title"
            },
            "reviewText": {
              "type": "string",
              "maxLength": 5000,
              "description": "Full review text"
            },
            "pros": {
              "type": "array",
              "items": {"type": "string"},
              "description": "List of pros"
            },
            "cons": {
              "type": "array",
              "items": {"type": "string"},
              "description": "List of cons"
            },
            "verifiedPurchase": {
              "type": "boolean",
              "default": false,
              "description": "Whether reviewer purchased the product"
            },
            "helpfulVotes": {
              "type": "integer",
              "default": 0,
              "description": "Number of helpful votes"
            },
            "images": {
              "type": "array",
              "items": {
                "type": "object",
                "properties": {
                  "imageUrl": {"type": "string"},
                  "caption": {"type": "string"}
                }
              },
              "description": "Review images"
            },
            "createdAt": {
              "type": "integer",
              "description": "Timestamp when review was created"
            },
            "updatedAt": {
              "type": "integer",
              "description": "Timestamp when review was last updated"
            }
          }
        }
        """;
    
    public static final String REORDER_ALERT = """
        {
          "$schema": "http://json-schema.org/draft-07/schema#",
          "title": "ReorderAlert",
          "type": "object",
          "required": ["alertId", "productId", "warehouseId", "currentStock", "reorderPoint", "timestamp"],
          "properties": {
            "alertId": {
              "type": "string"
            },
            "productId": {
              "type": "string"
            },
            "sku": {
              "type": "string"
            },
            "productName": {
              "type": "string"
            },
            "warehouseId": {
              "type": "string"
            },
            "currentStock": {
              "type": "integer"
            },
            "reorderPoint": {
              "type": "integer"
            },
            "reorderQuantity": {
              "type": "integer"
            },
            "supplierId": {
              "type": "string"
            },
            "estimatedLeadTimeDays": {
              "type": "integer"
            },
            "priority": {
              "type": "string",
              "enum": ["LOW", "MEDIUM", "HIGH", "CRITICAL"]
            },
            "salesVelocity": {
              "type": "number",
              "description": "Units sold per day"
            },
            "daysUntilStockout": {
              "type": "number"
            },
            "timestamp": {
              "type": "integer"
            }
          }
        }
        """;
    
    public static final String CUSTOMER_FEEDBACK = """
        {
          "$schema": "http://json-schema.org/draft-07/schema#",
          "title": "CustomerFeedback",
          "type": "object",
          "required": ["feedbackId", "customerId", "feedbackType", "createdAt"],
          "properties": {
            "feedbackId": {
              "type": "string"
            },
            "customerId": {
              "type": "string"
            },
            "orderId": {
              "type": "string"
            },
            "feedbackType": {
              "type": "string",
              "enum": ["NPS", "CSAT", "CES", "GENERAL", "COMPLAINT", "SUGGESTION"]
            },
            "score": {
              "type": "integer",
              "minimum": 0,
              "maximum": 10
            },
            "comment": {
              "type": "string",
              "maxLength": 2000
            },
            "category": {
              "type": "string"
            },
            "subcategory": {
              "type": "string"
            },
            "sentiment": {
              "type": "string",
              "enum": ["POSITIVE", "NEUTRAL", "NEGATIVE"]
            },
            "channel": {
              "type": "string",
              "enum": ["WEB", "MOBILE_APP", "EMAIL", "PHONE", "CHAT", "SOCIAL"]
            },
            "agentId": {
              "type": "string"
            },
            "resolved": {
              "type": "boolean",
              "default": false
            },
            "followUpRequired": {
              "type": "boolean",
              "default": false
            },
            "tags": {
              "type": "array",
              "items": {"type": "string"}
            },
            "createdAt": {
              "type": "integer"
            }
          }
        }
        """;
    
    public static final String IN_APP_NOTIFICATION = """
        {
          "$schema": "http://json-schema.org/draft-07/schema#",
          "title": "InAppNotification",
          "type": "object",
          "required": ["notificationId", "userId", "title", "message", "createdAt"],
          "properties": {
            "notificationId": {
              "type": "string"
            },
            "userId": {
              "type": "string"
            },
            "title": {
              "type": "string",
              "maxLength": 100
            },
            "message": {
              "type": "string",
              "maxLength": 500
            },
            "notificationType": {
              "type": "string",
              "enum": ["INFO", "SUCCESS", "WARNING", "ERROR", "PROMOTION", "SYSTEM"]
            },
            "category": {
              "type": "string"
            },
            "iconUrl": {
              "type": "string"
            },
            "actionUrl": {
              "type": "string"
            },
            "actionText": {
              "type": "string"
            },
            "data": {
              "type": "object",
              "additionalProperties": true
            },
            "priority": {
              "type": "string",
              "enum": ["LOW", "NORMAL", "HIGH"],
              "default": "NORMAL"
            },
            "read": {
              "type": "boolean",
              "default": false
            },
            "readAt": {
              "type": "integer"
            },
            "expiresAt": {
              "type": "integer"
            },
            "createdAt": {
              "type": "integer"
            }
          }
        }
        """;
    
    public static final String ROUTE_OPTIMIZATION = """
        {
          "$schema": "http://json-schema.org/draft-07/schema#",
          "title": "RouteOptimization",
          "type": "object",
          "required": ["routeId", "driverId", "vehicleId", "stops", "createdAt"],
          "properties": {
            "routeId": {
              "type": "string"
            },
            "driverId": {
              "type": "string"
            },
            "vehicleId": {
              "type": "string"
            },
            "depotLocation": {
              "type": "object",
              "properties": {
                "latitude": {"type": "number"},
                "longitude": {"type": "number"},
                "address": {"type": "string"}
              }
            },
            "stops": {
              "type": "array",
              "items": {
                "type": "object",
                "properties": {
                  "stopId": {"type": "string"},
                  "orderId": {"type": "string"},
                  "sequence": {"type": "integer"},
                  "latitude": {"type": "number"},
                  "longitude": {"type": "number"},
                  "address": {"type": "string"},
                  "estimatedArrival": {"type": "integer"},
                  "timeWindowStart": {"type": "integer"},
                  "timeWindowEnd": {"type": "integer"},
                  "serviceTimeMinutes": {"type": "integer"},
                  "priority": {"type": "string"}
                }
              }
            },
            "totalDistanceKm": {
              "type": "number"
            },
            "totalDurationMinutes": {
              "type": "integer"
            },
            "optimizationScore": {
              "type": "number"
            },
            "algorithmUsed": {
              "type": "string"
            },
            "constraints": {
              "type": "object",
              "properties": {
                "maxStops": {"type": "integer"},
                "maxDistanceKm": {"type": "number"},
                "maxDurationMinutes": {"type": "integer"},
                "vehicleCapacity": {"type": "number"}
              }
            },
            "status": {
              "type": "string",
              "enum": ["PLANNED", "IN_PROGRESS", "COMPLETED", "CANCELLED"]
            },
            "createdAt": {
              "type": "integer"
            },
            "updatedAt": {
              "type": "integer"
            }
          }
        }
        """;
    
    public static final String MODEL_METRICS = """
        {
          "$schema": "http://json-schema.org/draft-07/schema#",
          "title": "ModelMetrics",
          "type": "object",
          "required": ["metricsId", "modelId", "modelVersion", "timestamp"],
          "properties": {
            "metricsId": {
              "type": "string"
            },
            "modelId": {
              "type": "string"
            },
            "modelVersion": {
              "type": "string"
            },
            "environment": {
              "type": "string",
              "enum": ["DEVELOPMENT", "STAGING", "PRODUCTION"]
            },
            "accuracy": {
              "type": "number",
              "minimum": 0,
              "maximum": 1
            },
            "precision": {
              "type": "number",
              "minimum": 0,
              "maximum": 1
            },
            "recall": {
              "type": "number",
              "minimum": 0,
              "maximum": 1
            },
            "f1Score": {
              "type": "number",
              "minimum": 0,
              "maximum": 1
            },
            "auc": {
              "type": "number",
              "minimum": 0,
              "maximum": 1
            },
            "rmse": {
              "type": "number"
            },
            "mae": {
              "type": "number"
            },
            "confusionMatrix": {
              "type": "object",
              "properties": {
                "truePositives": {"type": "integer"},
                "trueNegatives": {"type": "integer"},
                "falsePositives": {"type": "integer"},
                "falseNegatives": {"type": "integer"}
              }
            },
            "featureImportance": {
              "type": "object",
              "additionalProperties": {"type": "number"}
            },
            "predictionCount": {
              "type": "integer"
            },
            "avgLatencyMs": {
              "type": "number"
            },
            "p99LatencyMs": {
              "type": "number"
            },
            "errorRate": {
              "type": "number"
            },
            "datasetSize": {
              "type": "integer"
            },
            "trainingDurationSeconds": {
              "type": "integer"
            },
            "timestamp": {
              "type": "integer"
            }
          }
        }
        """;
}
