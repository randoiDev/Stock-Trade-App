package com.example.trading.simple_trading_app.dto.response;

import java.math.BigDecimal;

public record UserStockDTO(
        String stock,
        int quantity
) {
}
