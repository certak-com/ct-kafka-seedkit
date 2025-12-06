package com.certak.kafka.seedkit.schemas;

/**
 * Protobuf schema definitions for Kafka topics.
 */
public final class ProtobufSchemas {
    private ProtobufSchemas() {}
    
    public static final String CHARGEBACK = """
        syntax = "proto3";
        package com.certak.kafka.payments;
        
        message Chargeback {
          string chargeback_id = 1;
          string transaction_id = 2;
          string order_id = 3;
          double amount = 4;
          string currency = 5;
          string reason_code = 6;
          string reason_description = 7;
          ChargebackStatus status = 8;
          string card_last4 = 9;
          string card_brand = 10;
          int64 initiated_at = 11;
          int64 due_date = 12;
          int64 resolved_at = 13;
          string resolution = 14;
          repeated Document documents = 15;
          
          enum ChargebackStatus {
            CHARGEBACK_STATUS_UNSPECIFIED = 0;
            PENDING = 1;
            UNDER_REVIEW = 2;
            WON = 3;
            LOST = 4;
            ACCEPTED = 5;
          }
          
          message Document {
            string document_id = 1;
            string document_type = 2;
            string file_name = 3;
            int64 uploaded_at = 4;
          }
        }
        """;
    
    public static final String FIRMWARE_UPDATE = """
        syntax = "proto3";
        package com.certak.kafka.iot;
        
        message FirmwareUpdate {
          string update_id = 1;
          string device_id = 2;
          string current_version = 3;
          string target_version = 4;
          string firmware_url = 5;
          string checksum = 6;
          ChecksumType checksum_type = 7;
          int64 file_size = 8;
          UpdatePriority priority = 9;
          bool force_update = 10;
          int64 scheduled_at = 11;
          int64 created_at = 12;
          map<string, string> metadata = 13;
          
          enum ChecksumType {
            CHECKSUM_TYPE_UNSPECIFIED = 0;
            MD5 = 1;
            SHA256 = 2;
            SHA512 = 3;
          }
          
          enum UpdatePriority {
            UPDATE_PRIORITY_UNSPECIFIED = 0;
            LOW = 1;
            NORMAL = 2;
            HIGH = 3;
            CRITICAL = 4;
          }
        }
        """;
    
    public static final String CARRIER_EVENT = """
        syntax = "proto3";
        package com.certak.kafka.shipping;
        
        message CarrierEvent {
          string event_id = 1;
          string tracking_number = 2;
          string carrier_code = 3;
          CarrierEventType event_type = 4;
          string location_city = 5;
          string location_state = 6;
          string location_country = 7;
          string location_postal_code = 8;
          int64 event_timestamp = 9;
          string description = 10;
          string exception_code = 11;
          string exception_description = 12;
          SignatureInfo signature = 13;
          
          enum CarrierEventType {
            CARRIER_EVENT_TYPE_UNSPECIFIED = 0;
            PICKED_UP = 1;
            IN_TRANSIT = 2;
            OUT_FOR_DELIVERY = 3;
            DELIVERED = 4;
            DELIVERY_ATTEMPT = 5;
            EXCEPTION = 6;
            RETURNED = 7;
          }
          
          message SignatureInfo {
            string signer_name = 1;
            int64 signed_at = 2;
            string relationship = 3;
          }
        }
        """;
    
    public static final String TRADE_ORDER = """
        syntax = "proto3";
        package com.certak.kafka.trading;
        
        message TradeOrder {
          string order_id = 1;
          string account_id = 2;
          string symbol = 3;
          OrderSide side = 4;
          OrderType order_type = 5;
          TimeInForce time_in_force = 6;
          int64 quantity = 7;
          double limit_price = 8;
          double stop_price = 9;
          int64 filled_quantity = 10;
          double average_fill_price = 11;
          OrderStatus status = 12;
          string exchange = 13;
          int64 created_at = 14;
          int64 updated_at = 15;
          string client_order_id = 16;
          string reject_reason = 17;
          
          enum OrderSide {
            ORDER_SIDE_UNSPECIFIED = 0;
            BUY = 1;
            SELL = 2;
          }
          
          enum OrderType {
            ORDER_TYPE_UNSPECIFIED = 0;
            MARKET = 1;
            LIMIT = 2;
            STOP = 3;
            STOP_LIMIT = 4;
          }
          
          enum TimeInForce {
            TIME_IN_FORCE_UNSPECIFIED = 0;
            DAY = 1;
            GTC = 2;
            IOC = 3;
            FOK = 4;
          }
          
          enum OrderStatus {
            ORDER_STATUS_UNSPECIFIED = 0;
            PENDING = 1;
            OPEN = 2;
            PARTIALLY_FILLED = 3;
            FILLED = 4;
            CANCELLED = 5;
            REJECTED = 6;
            EXPIRED = 7;
          }
        }
        """;
    
    public static final String TRADE_EXECUTION = """
        syntax = "proto3";
        package com.certak.kafka.trading;
        
        message TradeExecution {
          string execution_id = 1;
          string order_id = 2;
          string account_id = 3;
          string symbol = 4;
          ExecutionSide side = 5;
          int64 quantity = 6;
          double price = 7;
          double commission = 8;
          string exchange = 9;
          string liquidity_indicator = 10;
          int64 executed_at = 11;
          string settlement_date = 12;
          
          enum ExecutionSide {
            EXECUTION_SIDE_UNSPECIFIED = 0;
            BUY = 1;
            SELL = 2;
          }
        }
        """;
}
