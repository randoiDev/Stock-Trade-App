package com.example.trading.simple_trading_app.enums;

public enum OrderStatus {
    NEW,      // Order is open and waiting for a match
    MATCHED,  // Order has been fully matched
    LOCKED,   // Order is being processed
    CANCELLED // Order was manually cancelled
}

