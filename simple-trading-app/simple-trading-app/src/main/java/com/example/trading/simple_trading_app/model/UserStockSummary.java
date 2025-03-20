package com.example.trading.simple_trading_app.model;

import java.math.BigDecimal;
import java.util.Map;

public interface UserStockSummary {
    String getUsername();
    BigDecimal getBalance();
    Map<Stock, Integer> getStocks();
}

